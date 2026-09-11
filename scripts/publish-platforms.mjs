#!/usr/bin/env node
/**
 * Platform Publisher Bridge
 * Wraps scripts/publish.py to upload to Modrinth & CurseForge during release workflow
 */

import { spawnSync } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(__dirname, '..');

const args = process.argv.slice(2);
let platform = 'all';

for (let i = 0; i < args.length; i++) {
  if (args[i] === '--platform' && args[i + 1]) {
    platform = args[++i];
  }
}

console.log(`[Publisher Bridge] Publishing to platform: ${platform}`);

// Check if python publish script is available
const publishPy = path.join(__dirname, 'publish.py');
const pyArgs = [publishPy, 'publish', '--yes', '--skip-build'];

if (platform === 'modrinth') {
  pyArgs.push('--modrinth', '--no-curseforge');
} else if (platform === 'curseforge') {
  pyArgs.push('--curseforge', '--no-modrinth');
}

const result = spawnSync('python', pyArgs, {
  cwd: ROOT_DIR,
  stdio: 'inherit',
  env: {
    ...process.env,
    CI: 'true',
    NON_INTERACTIVE: 'true',
  },
});

if (result.status !== 0) {
  console.warn(`[Publisher Bridge] Python publisher exited with status ${result.status}`);
  // Don't fail the whole release workflow if third-party mod API fails
  process.exit(0);
}

