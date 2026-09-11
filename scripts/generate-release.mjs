#!/usr/bin/env node
/**
 * Release and Changelog Generator for Cobbleloots
 * Generates SemVer 2.0 with +<mc_version> build metadata and compiles release notes from PRs
 */

import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(__dirname, '..');

// Helper to parse CLI flags
function parseArgs() {
  const args = process.argv.slice(2);
  const options = {
    bump: 'patch',
    custom: '',
    channel: 'beta',
    dryRun: false,
  };

  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--bump' && args[i + 1]) options.bump = args[++i];
    if (args[i] === '--custom' && args[i + 1]) options.custom = args[++i];
    if (args[i] === '--channel' && args[i + 1]) options.channel = args[++i];
    if (args[i] === '--dry-run') options.dryRun = true;
  }
  return options;
}

// Read gradle.properties
function getGradleProperties() {
  const content = fs.readFileSync(path.join(ROOT_DIR, 'gradle.properties'), 'utf8');
  const props = {};
  for (const line of content.split('\n')) {
    const trimmed = line.trim();
    if (trimmed && !trimmed.startsWith('#')) {
      const [k, v] = trimmed.split('=');
      if (k && v) props[k.trim()] = v.trim();
    }
  }
  return props;
}

// Calculate next SemVer
function computeNextVersion(currentVersion, bump, channel, custom) {
  if (bump === 'custom') {
    if (!custom) throw new Error('Custom bump requested but no --custom version provided');
    return custom;
  }

  // Remove pre-release suffix if present (e.g. 2.3.0-beta.1 -> 2.3.0)
  const baseMatch = currentVersion.match(/^(\d+)\.(\d+)\.(\d+)/);
  if (!baseMatch) throw new Error(`Cannot parse current version: ${currentVersion}`);

  let major = parseInt(baseMatch[1], 10);
  let minor = parseInt(baseMatch[2], 10);
  let patch = parseInt(baseMatch[3], 10);

  if (bump === 'major') {
    major++;
    minor = 0;
    patch = 0;
  } else if (bump === 'minor') {
    minor++;
    patch = 0;
  } else if (bump === 'patch') {
    patch++;
  }

  const core = `${major}.${minor}.${patch}`;
  if (channel === 'release') {
    return core;
  }
  return `${core}-${channel}.1`;
}

// Extract merged PRs/commits since last tag
function getMergedChanges() {
  let lastTag = '';
  try {
    lastTag = execSync('git describe --tags --abbrev=0', { cwd: ROOT_DIR, encoding: 'utf8' }).trim();
  } catch {
    console.log('No previous git tag found; including all commits.');
  }

  const gitRange = lastTag ? `${lastTag}..HEAD` : 'HEAD';
  let logOutput = '';
  try {
    logOutput = execSync(`git log ${gitRange} --pretty=format:"%s"`, { cwd: ROOT_DIR, encoding: 'utf8' });
  } catch (e) {
    console.warn('Could not read git log:', e.message);
  }

  const lines = logOutput.split('\n').map((l) => l.trim()).filter(Boolean);

  const features = [];
  const fixes = [];
  const technical = [];

  for (const line of lines) {
    // Skip release commits
    if (line.startsWith('chore(release):') || line.includes('[skip ci]')) continue;

    // Check conventional commit pattern: [ISSUE-ID] type(scope): desc or type(scope): desc
    const match = line.match(/^(\[[A-Z0-9_-]+\]\s*)?([a-z]+)(\([^)]+\))?:\s*(.+)$/i);
    if (match) {
      const issueTag = match[1] ? match[1].trim() + ' ' : '';
      const type = match[2].toLowerCase();
      const scope = match[3] ? `**${match[3].replace(/[()]/g, '')}**: ` : '';
      const desc = match[4];
      const entry = `- ${issueTag}${scope}${desc}`;

      if (type === 'feat') {
        features.push(entry);
      } else if (type === 'fix') {
        fixes.push(entry);
      } else {
        technical.push(entry);
      }
    } else {
      technical.push(`- ${line}`);
    }
  }

  return { lastTag, features, fixes, technical };
}

function buildChangelogSection(tagName, changes) {
  const sections = [];
  sections.push(`## ${tagName}\n`);

  if (changes.features.length > 0) {
    sections.push(`### Gameplay Changes\n\n${changes.features.join('\n')}\n`);
  }
  if (changes.fixes.length > 0) {
    sections.push(`### Bug Fixes\n\n${changes.fixes.join('\n')}\n`);
  }
  if (changes.technical.length > 0) {
    sections.push(`### Technical Changes\n\n${changes.technical.join('\n')}\n`);
  }

  if (changes.features.length === 0 && changes.fixes.length === 0 && changes.technical.length === 0) {
    sections.push(`### Changes\n\n- General maintenance and improvements.\n`);
  }

  return sections.join('\n');
}

function main() {
  const opts = parseArgs();
  const props = getGradleProperties();

  const currentVersion = props['mod_version'] || '2.3.0';
  const mcVersion = props['minecraft_version'] || '1.21.1';
  const newVersion = computeNextVersion(currentVersion, opts.bump, opts.channel, opts.custom);
  const tagName = `v${newVersion}+${mcVersion}`;

  console.log(`Calculating release:`);
  console.log(`  Current Version: ${currentVersion}`);
  console.log(`  Target Version:  ${newVersion}`);
  console.log(`  Channel:         ${opts.channel}`);
  console.log(`  Target Tag:      ${tagName}`);

  const changes = getMergedChanges();
  const newChangelogEntry = buildChangelogSection(tagName, changes);

  if (opts.dryRun) {
    console.log(`\n[DRY RUN] Generated Changelog Entry:\n`);
    console.log(newChangelogEntry);
    console.log(`[DRY RUN] No files were modified.`);
    return;
  }

  // 1. Prepend to CHANGELOG.md
  const changelogPath = path.join(ROOT_DIR, 'CHANGELOG.md');
  let currentChangelog = fs.existsSync(changelogPath) ? fs.readFileSync(changelogPath, 'utf8') : '# CHANGELOG\n\n';

  let updatedChangelog = '';
  if (currentChangelog.startsWith('# CHANGELOG')) {
    const afterHeader = currentChangelog.replace(/^# CHANGELOG\s*\n+/, '');
    updatedChangelog = `# CHANGELOG\n\n${newChangelogEntry}\n${afterHeader}`;
  } else {
    updatedChangelog = `${newChangelogEntry}\n${currentChangelog}`;
  }
  fs.writeFileSync(changelogPath, updatedChangelog, 'utf8');
  console.log(`Updated CHANGELOG.md`);

  // 2. Update gradle.properties
  const gradlePropsPath = path.join(ROOT_DIR, 'gradle.properties');
  let gradlePropsContent = fs.readFileSync(gradlePropsPath, 'utf8');
  gradlePropsContent = gradlePropsContent.replace(/^mod_version=.*/m, `mod_version=${newVersion}`);
  gradlePropsContent = gradlePropsContent.replace(/^mod_version_type=.*/m, `mod_version_type=${opts.channel}`);
  fs.writeFileSync(gradlePropsPath, gradlePropsContent, 'utf8');
  console.log(`Updated gradle.properties with mod_version=${newVersion}, mod_version_type=${opts.channel}`);

  // 3. Write .release_notes.md
  const releaseNotesPath = path.join(ROOT_DIR, '.release_notes.md');
  fs.writeFileSync(releaseNotesPath, newChangelogEntry, 'utf8');

  // 4. Output to GitHub Actions if available
  if (process.env.GITHUB_OUTPUT) {
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `tag_name=${tagName}\n`);
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `version=${newVersion}\n`);
  }
}

main();
