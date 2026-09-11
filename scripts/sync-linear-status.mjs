#!/usr/bin/env node
/**
 * Linear Status Synchronization Script
 * Updates issue state to "In Review" (on success) or "Blocked" (on failure)
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(__dirname, '..');

const apiKey = process.env.LINEAR_API_KEY;
const status = process.env.STATUS;
const issueId = process.env.ISSUE_ID;
const identifier = process.env.ISSUE_IDENTIFIER || '';

// Only proceed on failure; successful tasks are handled automatically by Linear's GitHub integration
if (status !== 'failure') {
  process.exit(0);
}

if (!apiKey) {
  process.exit(0);
}

if (!issueId && !identifier) {
  process.exit(0);
}

async function executeLinearGraphQL(query, variables = {}) {
  const response = await fetch('https://api.linear.app/graphql', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: apiKey,
    },
    body: JSON.stringify({ query, variables }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Linear API HTTP ${response.status}: ${errorText}`);
  }

  const result = await response.json();
  if (result.errors && result.errors.length > 0) {
    throw new Error(`Linear GraphQL error: ${result.errors.map((e) => e.message).join(', ')}`);
  }
  return result.data;
}

async function findIssueId(idOrIdentifier) {
  if (!idOrIdentifier.includes('-')) {
    return idOrIdentifier;
  }
  const query = `
    query GetIssue($identifier: String!) {
      issue(id: $identifier) {
        id
        team {
          id
        }
      }
    }
  `;
  const data = await executeLinearGraphQL(query, { identifier: idOrIdentifier });
  return data.issue ? data.issue : null;
}

async function addComment(id, body) {
  const mutation = `
    mutation CreateComment($issueId: String!, $body: String!) {
      commentCreate(input: { issueId: $issueId, body: $body }) {
        success
      }
    }
  `;
  await executeLinearGraphQL(mutation, { issueId: id, body });
}

async function updateState(id, teamId, targetStateName) {
  const queryStates = `
    query GetTeamStates($teamId: String!) {
      team(id: $teamId) {
        states {
          nodes {
            id
            name
          }
        }
      }
    }
  `;
  const data = await executeLinearGraphQL(queryStates, { teamId });
  const targetState = data.team.states.nodes.find(
    (s) => s.name.toLowerCase() === targetStateName.toLowerCase()
  );

  if (!targetState) {
    console.warn(`State "${targetStateName}" not found in team.`);
    return;
  }

  const mutationUpdate = `
    mutation UpdateIssue($issueId: String!, $stateId: String!) {
      issueUpdate(id: $issueId, input: { stateId: $stateId }) {
        success
      }
    }
  `;
  await executeLinearGraphQL(mutationUpdate, { issueId: id, stateId: targetState.id });
}

async function main() {
  try {
    const issue = await findIssueId(issueId || identifier);
    if (!issue) {
      console.warn(`Could not locate issue in Linear: ${issueId || identifier}`);
      return;
    }

    const realIssueId = issue.id;
    const teamId = issue.team?.id;

    console.log(`Marking issue ${identifier} as Blocked due to quality gate failure...`);
    let errorLog = 'Error desconocido durante la compilación de Gradle.';
    const errorLogPath = path.join(ROOT_DIR, '.agent_error.log');
    if (fs.existsSync(errorLogPath)) {
      errorLog = fs.readFileSync(errorLogPath, 'utf8').slice(-1500);
    }
    const comment = `❌ **CobbleLoots Software Factory**: El agente no logró compilar exitosamente el proyecto tras 3 intentos de auto-reparación.\n\n**Logs de error del compilador:**\n\`\`\`\n${errorLog}\n\`\`\``;
    await addComment(realIssueId, comment);
    if (teamId) {
      await updateState(realIssueId, teamId, 'Blocked');
    }
  } catch (err) {
    console.error('Error syncing failure status with Linear:', err.message);
  }
}

main();

