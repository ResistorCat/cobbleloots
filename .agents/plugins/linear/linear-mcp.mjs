#!/usr/bin/env node
import fs from 'fs';
import path from 'path';
import readline from 'readline';
import { fileURLToPath } from 'url';

// 1. Resolve project root (three levels up from .agents/plugins/linear/)
const currentDir = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(currentDir, '..', '..', '..');
const debugLogFile = path.join(currentDir, 'debug.log');

function dlog(...args) {
  const line = `[${new Date().toISOString()}] [pid:${process.pid}] ` +
    args.map(a => typeof a === 'object' ? JSON.stringify(a) : a).join(' ') + '\n';
  try {
    fs.appendFileSync(debugLogFile, line);
  } catch {}
}

dlog('=== Native Linear MCP Bridge Started ===');
dlog('cwd:', process.cwd());

// 2. Read LINEAR_API_KEY from environment or .env
let apiKey = process.env.LINEAR_API_KEY || process.env.LINEAR_TOKEN;

if (!apiKey) {
  const candidateEnvPaths = [
    path.join(projectRoot, '.env'),
    path.join(process.cwd(), '.env'),
    path.resolve(currentDir, '..', '..', '.env'),
  ];
  for (const envPath of candidateEnvPaths) {
    if (fs.existsSync(envPath)) {
      try {
        const content = fs.readFileSync(envPath, 'utf8');
        for (const line of content.split(/\r?\n/)) {
          const trimmed = line.trim();
          if (!trimmed || trimmed.startsWith('#')) continue;
          const match = trimmed.match(/^(?:LINEAR_API_KEY|LINEAR_TOKEN)\s*=\s*(.+)$/);
          if (match) {
            apiKey = match[1].trim().replace(/^["']|["']$/g, '');
            dlog('Found apiKey in', envPath);
            break;
          }
        }
      } catch (err) {
        dlog('Error reading envPath:', envPath, err.message);
      }
      if (apiKey) break;
    }
  }
}

if (!apiKey) {
  dlog('FATAL: Neither LINEAR_API_KEY nor LINEAR_TOKEN is set.');
  console.error(
    '[Linear MCP] Error: Neither LINEAR_API_KEY nor LINEAR_TOKEN is set.\n' +
    'Please configure LINEAR_API_KEY in your system environment or in the project root .env file.'
  );
  process.exit(1);
}

dlog('apiKey found (length: ' + apiKey.length + ')');

// 3. Direct Native HTTP JSON-RPC Bridge to https://mcp.linear.app/mcp
// Eliminates mcp-remote child processes, npm dependencies, and Windows pipe latency.
let sessionId = undefined;
const LINEAR_ENDPOINT = 'https://mcp.linear.app/mcp';

async function handleMessage(msg) {
  dlog('RECV from client:', msg.id !== undefined ? `id:${msg.id}` : 'notification', msg.method || 'result');

  const headers = {
    'Authorization': `Bearer ${apiKey}`,
    'Content-Type': 'application/json',
    'Accept': 'application/json, text/event-stream',
  };

  if (msg.method) {
    headers['Mcp-Method'] = msg.method;
  }
  if (msg.params?.name) {
    headers['Mcp-Name'] = msg.params.name;
  }
  if (msg.params?.uri) {
    headers['Mcp-Uri'] = msg.params.uri;
  }
  if (sessionId) {
    headers['Mcp-Session-Id'] = sessionId;
  }

  try {
    const res = await fetch(LINEAR_ENDPOINT, {
      method: 'POST',
      headers,
      body: JSON.stringify(msg),
    });

    const newSessionId = res.headers.get('mcp-session-id');
    if (newSessionId) {
      sessionId = newSessionId;
    }

    // Notifications (no id) do not expect a response in JSON-RPC
    if (msg.id === undefined || msg.id === null) {
      dlog('Notification delivered, HTTP status:', res.status);
      return;
    }

    const text = await res.text();
    let sent = false;

    // Parse SSE stream format: lines starting with 'data:'
    for (const rawLine of text.split(/\r?\n/)) {
      const line = rawLine.trim();
      if (line.startsWith('data:')) {
        const jsonStr = line.slice(5).trim();
        if (jsonStr) {
          dlog('SEND to client (SSE data):', jsonStr.slice(0, 150));
          process.stdout.write(jsonStr + '\n');
          sent = true;
        }
      }
    }

    // Direct JSON response fallback
    if (!sent && text.trim().startsWith('{')) {
      dlog('SEND to client (JSON):', text.trim().slice(0, 150));
      process.stdout.write(text.trim() + '\n');
      sent = true;
    }

    if (!sent) {
      dlog('Warning: No JSON-RPC response found in HTTP body:', text.slice(0, 200));
      const errResponse = {
        jsonrpc: '2.0',
        id: msg.id,
        error: { code: -32603, message: `Linear returned HTTP ${res.status} without payload` },
      };
      process.stdout.write(JSON.stringify(errResponse) + '\n');
    }
  } catch (err) {
    dlog('Error forwarding message to Linear:', err.message);
    if (msg.id !== undefined && msg.id !== null) {
      const errResponse = {
        jsonrpc: '2.0',
        id: msg.id,
        error: { code: -32603, message: `Bridge connection error: ${err.message}` },
      };
      process.stdout.write(JSON.stringify(errResponse) + '\n');
    }
  }
}

// 4. Stdio line reader
const rl = readline.createInterface({
  input: process.stdin,
  terminal: false,
});

rl.on('line', (line) => {
  const trimmed = line.trim();
  if (!trimmed) return;
  try {
    const msg = JSON.parse(trimmed);
    handleMessage(msg);
  } catch (err) {
    dlog('Failed to parse incoming JSON line:', err.message, trimmed.slice(0, 100));
  }
});

rl.on('close', () => {
  dlog('STDIN closed by client, exiting.');
  process.exit(0);
});

process.on('SIGINT', () => process.exit(0));
process.on('SIGTERM', () => process.exit(0));
