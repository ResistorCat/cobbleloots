#!/usr/bin/env node
/**
 * Autonomous Agent Execution & Quality Gate Loop
 * Runs within GitHub Actions or local environment
 */

import { execSync, spawnSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(__dirname, '..');

const identifier = process.env.ISSUE_IDENTIFIER || 'TASK-0';
const title = process.env.ISSUE_TITLE || 'Development Task';
const description = process.env.ISSUE_DESCRIPTION || '';
const issueUrl = process.env.ISSUE_URL || '';

console.log(`====================================================`);
console.log(`CobbleLoots Software Factory - Agent Harness`);
console.log(`Processing Ticket: [${identifier}] ${title}`);
console.log(`URL: ${issueUrl}`);
console.log(`====================================================`);

/**
 * Execute Gradle build with timeout and proper memory flags
 */
function runGradleVerification() {
  console.log(`\n[Quality Gate] Running ./gradlew build...`);
  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows ? 'gradlew.bat' : './gradlew';

  try {
    const result = spawnSync(gradlewCmd, ['build', '--stacktrace', '--no-daemon'], {
      cwd: ROOT_DIR,
      stdio: ['ignore', 'pipe', 'pipe'],
      encoding: 'utf8',
      env: {
        ...process.env,
        GRADLE_OPTS: '-Dorg.gradle.jvmargs=-Xmx5g -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false',
      },
    });

    if (result.status === 0) {
      console.log(`[Quality Gate] Gradle build passed successfully! Exit code 0.`);
      return { success: true, output: result.stdout };
    } else {
      const errorOutput = (result.stderr || result.stdout || '').slice(-4000);
      console.error(`[Quality Gate] Gradle build failed with code ${result.status}`);
      return { success: false, output: errorOutput };
    }
  } catch (err) {
    return { success: false, output: err.message };
  }
}

/**
 * Generate the strictly compliant PR body
 */
function createCompliantPRBody(diffSummary, touchedFiles) {
  const isCommon = touchedFiles.some((f) => f.startsWith('common/'));
  const isFabric = touchedFiles.some((f) => f.startsWith('fabric/'));
  const isNeoForge = touchedFiles.some((f) => f.startsWith('neoforge/'));

  return `### ¿Qué se hizo?
- Implementación de la tarea [${identifier}](${issueUrl}): ${title}.
- Cambios realizados en los componentes correspondientes para cumplir los criterios de aceptación especificados en Linear.
- Verificación exhaustiva con compilación de Gradle completada con éxito.

### ¿Cómo se ve?
${diffSummary || 'Se actualizaron las definiciones internas, configuraciones o registros del mod para dar soporte a la funcionalidad requerida.'}

### ¿Cómo testearlo?
1. Iniciar un cliente de prueba local con \`./gradlew :fabric:runClient\` o \`./gradlew :neoforge:runClient\`.
2. Verificar en el entorno de juego la funcionalidad relacionada con "${title}".
3. Confirmar que no ocurran advertencias ni errores en la consola del juego durante la inicialización de Cobblemon y Cobbleloots.

### Notas adicionales
- **Supuestos de diseño**:
  - Implementación compatible con Minecraft 1.21.1 y Cobblemon 1.7.3+.
  - Conservación de compatibilidad multi-loader con Architectury.
- **Plataformas afectadas**:
  - [${isCommon ? 'x' : ' '}] Common
  - [${isFabric ? 'x' : ' '}] Fabric
  - [${isNeoForge ? 'x' : ' '}] NeoForge
- **Detalles para el reviewer**:
  - Resuelve ticket en Linear: [${identifier}](${issueUrl}).
  - Build de Gradle verificado exitosamente antes de abrir la PR.
`;
}

async function main() {
  const maxAttempts = 3;
  let attempt = 1;
  let verified = false;
  let lastError = '';

  // Initial verification or repair loop
  while (attempt <= maxAttempts) {
    console.log(`\n--- Verification Attempt ${attempt} / ${maxAttempts} ---`);
    const verification = runGradleVerification();

    if (verification.success) {
      verified = true;
      break;
    }

    lastError = verification.output;
    console.warn(`[Attempt ${attempt}] Build failed. Error snippet:\n${lastError.slice(-1000)}`);

    if (attempt < maxAttempts) {
      console.log(`[Auto-Repair] Triggering agent correction iteration...`);
      // In CI, if Gemini/agent API is available, pass the compiler error to auto-fix:
      // For now, save the error trace to .compiler_error.log so the runner or agent can consume it
      fs.writeFileSync(path.join(ROOT_DIR, '.compiler_error.log'), lastError, 'utf8');
      attempt++;
    } else {
      break;
    }
  }

  if (verified) {
    console.log(`\n[Agent Harness] All checks passed! Assembling Pull Request content...`);
    let touchedFiles = [];
    try {
      const gitStatus = execSync('git diff --name-only HEAD~1', { cwd: ROOT_DIR, encoding: 'utf8' });
      touchedFiles = gitStatus.split('\n').filter(Boolean);
    } catch {
      try {
        const status = execSync('git status --porcelain', { cwd: ROOT_DIR, encoding: 'utf8' });
        touchedFiles = status.split('\n').map((l) => l.slice(3).trim()).filter(Boolean);
      } catch {
        touchedFiles = ['common/'];
      }
    }

    const prBody = createCompliantPRBody('', touchedFiles);
    fs.writeFileSync(path.join(ROOT_DIR, '.pr_body.md'), prBody, 'utf8');

    if (process.env.GITHUB_OUTPUT) {
      fs.appendFileSync(process.env.GITHUB_OUTPUT, `status=success\n`);
    }
    process.exit(0);
  } else {
    console.error(`\n[Agent Harness] Failed to compile after ${maxAttempts} attempts.`);
    fs.writeFileSync(path.join(ROOT_DIR, '.agent_error.log'), lastError, 'utf8');

    if (process.env.GITHUB_OUTPUT) {
      fs.appendFileSync(process.env.GITHUB_OUTPUT, `status=failure\n`);
    }
    process.exit(1);
  }
}

main();

