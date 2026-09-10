#!/usr/bin/env node
/**
 * CobbleLoots Local Factory Runner
 * Launch Antigravity CLI on a Linear ticket with local Google account authentication
 * Usage: node scripts/factory-runner.mjs <TICKET_ID>
 */

import { execSync, spawn } from 'child_process';
import fs from 'fs';
import path from 'path';
import readline from 'readline';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(__dirname, '..');

// Simple .env loader
function loadEnv() {
  const envPath = path.join(ROOT_DIR, '.env');
  if (fs.existsSync(envPath)) {
    const lines = fs.readFileSync(envPath, 'utf8').split('\n');
    for (const line of lines) {
      const trimmed = line.trim();
      if (trimmed && !trimmed.startsWith('#') && trimmed.includes('=')) {
        const [k, ...v] = trimmed.split('=');
        if (!process.env[k.trim()]) {
          process.env[k.trim()] = v.join('=').trim();
        }
      }
    }
  }
}

loadEnv();

const ticketId = process.argv[2];
if (!ticketId) {
  console.error('\nUso: node scripts/factory-runner.mjs <TICKET_ID>');
  console.error('Ejemplo: node scripts/factory-runner.mjs DEV-5\n');
  process.exit(1);
}

const linearApiKey = process.env.LINEAR_API_KEY;

async function fetchLinearTicket(id) {
  if (!linearApiKey) {
    console.warn('[Aviso] No se encontró LINEAR_API_KEY en .env ni en el entorno.');
    return {
      identifier: id,
      title: 'Tarea Local ' + id,
      description: 'Sin descripción remota (falta LINEAR_API_KEY).',
      url: `https://linear.app/issue/${id}`,
    };
  }

  const query = `
    query GetIssue($identifier: String!) {
      issue(id: $identifier) {
        id
        identifier
        title
        description
        url
      }
    }
  `;

  try {
    const res = await fetch('https://api.linear.app/graphql', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: linearApiKey,
      },
      body: JSON.stringify({ query, variables: { identifier: id } }),
    });
    const result = await res.json();
    if (result.data?.issue) {
      return result.data.issue;
    }
  } catch (e) {
    console.warn('No se pudo consultar Linear:', e.message);
  }

  return {
    identifier: id,
    title: 'Tarea ' + id,
    description: '',
    url: `https://linear.app/issue/${id}`,
  };
}

function promptQuestion(query) {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });
  return new Promise((resolve) =>
    rl.question(query, (ans) => {
      rl.close();
      resolve(ans.trim());
    })
  );
}

async function main() {
  console.log(`\n====================================================`);
  console.log(`🚀 CobbleLoots Local Factory Runner`);
  console.log(`Ticket: ${ticketId}`);
  console.log(`====================================================\n`);

  const ticket = await fetchLinearTicket(ticketId);
  console.log(`Título: ${ticket.title}`);
  console.log(`URL:    ${ticket.url}\n`);

  // Create sanitized branch name
  const slug = ticket.title
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 40);
  const branchName = `agent/${ticket.identifier.toLowerCase()}-${slug}`;

  console.log(`Creando y cambiando a la rama: ${branchName}`);
  try {
    execSync(`git checkout -b ${branchName}`, { cwd: ROOT_DIR, stdio: 'inherit' });
  } catch {
    execSync(`git checkout ${branchName}`, { cwd: ROOT_DIR, stdio: 'inherit' });
  }

  // Assemble prompt for Antigravity CLI (agy)
  const agentPrompt = `
Estás trabajando en el repositorio de Cobbleloots resolviendo el ticket:
[${ticket.identifier}] ${ticket.title}
URL de Linear: ${ticket.url}

DESCRIPCIÓN Y CRITERIOS:
${ticket.description || 'Cumplir con el objetivo del título del ticket.'}

INSTRUCCIONES DEL REPOSITORIO:
- Lee obligatoriamente AGENTS.md y .agents/rules/minecraft-loaders.md.
- Todo cambio de código debe compilar con ./gradlew build.
- No modifiques CHANGELOG.md directamente.
- Al terminar, asegúrate de que el código esté listo para el Quality Gate.
`.trim();

  console.log(`\nLanzando Antigravity CLI ('agy') con tu cuenta de Google activa...\n`);

  // Launch agy interactively
  const agyProcess = spawn('agy', ['-p', agentPrompt], {
    cwd: ROOT_DIR,
    stdio: 'inherit',
    shell: true,
  });

  agyProcess.on('close', async (code) => {
    console.log(`\nSesión de agy finalizada (código ${code}).\n`);

    const answerVerify = await promptQuestion('¿Deseas verificar los cambios con ./gradlew build ahora? (S/n): ');
    if (answerVerify.toLowerCase() !== 'n') {
      console.log('\nEjecutando ./gradlew build...');
      const isWindows = process.platform === 'win32';
      const gradlewCmd = isWindows ? 'gradlew.bat' : './gradlew';
      try {
        execSync(`${gradlewCmd} build --stacktrace`, {
          cwd: ROOT_DIR,
          stdio: 'inherit',
          env: {
            ...process.env,
            GRADLE_OPTS: '-Dorg.gradle.jvmargs=-Xmx5g -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false',
          },
        });
        console.log('\n✅ Build exitoso!');

        const answerPR = await promptQuestion('¿Deseas crear el Pull Request en GitHub ahora? (S/n): ');
        if (answerPR.toLowerCase() !== 'n') {
          console.log('\nEmpujando rama a origin...');
          execSync(`git push -u origin ${branchName}`, { cwd: ROOT_DIR, stdio: 'inherit' });

          const prTitle = `[${ticket.identifier}] feat: ${ticket.title}`;
          console.log(`Creando PR: "${prTitle}"...`);
          execSync(
            `gh pr create --base main --head ${branchName} --title "${prTitle}" --body-file .agents/templates/agent-pr-template.md`,
            { cwd: ROOT_DIR, stdio: 'inherit' }
          );
          console.log('\n🎉 ¡Pull Request creado exitosamente!');
        }
      } catch (err) {
        console.error('\n❌ El build falló. Revisa los errores y vuelve a intentarlo con agy.');
      }
    }
  });
}

main();

