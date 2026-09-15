#!/usr/bin/env node
import { spawn } from 'child_process';
import os from 'os';
import path from 'path';

// Resolve home directory dynamically on any OS (Windows, Linux, macOS)
// without hardcoding user paths.
const configDir = path.join(os.homedir(), '.mcp-auth', 'linear-games');
const isWin = process.platform === 'win32';

const cmd = isWin ? 'cmd.exe' : 'npx';
const args = isWin
  ? ['/c', 'npx', '-y', 'mcp-remote', 'https://mcp.linear.app/mcp']
  : ['-y', 'mcp-remote', 'https://mcp.linear.app/mcp'];

const child = spawn(cmd, args, {
  stdio: 'inherit',
  env: {
    ...process.env,
    MCP_REMOTE_CONFIG_DIR: configDir,
  },
});

child.on('exit', (code) => {
  process.exit(code ?? 0);
});
