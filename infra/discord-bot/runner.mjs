import { spawn } from "node:child_process";
import { setTimeout } from "node:timers/promises";
import { existsSync, mkdirSync, copyFileSync } from "node:fs";
import { resolve, dirname } from "node:path";

// 0. Ensure better-sqlite3 native binding is copied to the path expected by the bundled server
const targetBinding = resolve(".output/server/build/Release/better_sqlite3.node");
if (!existsSync(targetBinding)) {
  const sourceCandidates = [
    resolve("node_modules/better-sqlite3/build/Release/better_sqlite3.node"),
    resolve("/app/node_modules/better-sqlite3/build/Release/better_sqlite3.node"),
  ];
  const sourceBinding = sourceCandidates.find((p) => existsSync(p));
  if (sourceBinding) {
    try {
      mkdirSync(dirname(targetBinding), { recursive: true });
      copyFileSync(sourceBinding, targetBinding);
      console.log(`[Runner] Copied better_sqlite3.node to ${targetBinding}`);
    } catch (err) {
      console.warn("[Runner] Could not copy better_sqlite3.node:", err.message);
    }
  }
}

// 1. Start Eve HTTP Server
const serverEntry = ".output/server/index.mjs";
console.log(`[Runner] Starting Eve server from ${serverEntry}...`);

const server = spawn(process.execPath, [serverEntry], {
  stdio: "inherit",
  env: process.env,
});

server.on("error", (err) => {
  console.error("[Runner] Failed to start Eve server:", err);
  process.exit(1);
});

server.on("exit", (code, signal) => {
  console.log(`[Runner] Eve server exited with code ${code}, signal ${signal}`);
  process.exit(code ?? 0);
});

// 2. Poll for Health Readiness
async function waitForHealth(port, maxAttempts = 60) {
  const url = `http://127.0.0.1:${port}/eve/v1/health`;
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const res = await fetch(url);
      if (res.ok) {
        return true;
      }
    } catch {
      // Server not ready yet
    }
    await setTimeout(500);
  }
  throw new Error(`[Runner] Eve server did not respond at ${url} within timeout`);
}

const port = process.env.PORT || 3000;
try {
  await waitForHealth(port);
  console.log(`[Runner] Eve server is healthy and listening on port ${port}!`);
} catch (err) {
  console.error(err.message);
  server.kill("SIGTERM");
  process.exit(1);
}

// 3. Start Discord Gateway Listener
if (process.env.DISCORD_BOT_TOKEN && process.env.DISABLE_GATEWAY !== "true") {
  console.log("[Runner] Starting Discord Gateway listener for @mentions...");
  try {
    const { startGateway } = await import("./agent/gateway.ts");
    await startGateway({ port });
  } catch (err) {
    console.error("[Runner] Failed to start Discord Gateway listener:", err);
  }
} else {
  console.log("[Runner] DISCORD_BOT_TOKEN not provided or DISABLE_GATEWAY=true. Gateway listener skipped.");
}

// 4. Graceful Shutdown Handlers
function shutdown(signal) {
  console.log(`[Runner] Received ${signal}, shutting down gracefully...`);
  server.kill(signal);
  process.exit(0);
}

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));
