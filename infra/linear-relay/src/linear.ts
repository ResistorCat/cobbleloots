/**
 * Linear Webhook and GraphQL utilities
 */

export interface LinearWebhookPayload {
  action: 'create' | 'update' | 'remove';
  type: string;
  data: {
    id: string;
    identifier?: string;
    title?: string;
    description?: string;
    url?: string;
    state?: {
      id: string;
      name: string;
      type: string;
    };
    assignee?: {
      id: string;
      name: string;
      email?: string;
    } | null;
    labels?: Array<{
      id: string;
      name: string;
    }>;
    team?: {
      id: string;
      key: string;
    };
  };
  updatedFrom?: Record<string, unknown>;
  url?: string;
}

/**
 * Performs constant-time string comparison to prevent timing attacks.
 */
function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) {
    return false;
  }
  let result = 0;
  for (let i = 0; i < a.length; i++) {
    result |= a.charCodeAt(i) ^ b.charCodeAt(i);
  }
  return result === 0;
}

/**
 * Validates the HMAC-SHA256 signature sent in the 'linear-signature' header.
 */
export async function verifyLinearSignature(
  rawBody: string,
  signatureHeader: string | null,
  secret: string
): Promise<boolean> {
  if (!signatureHeader || !secret) {
    return false;
  }

  try {
    const encoder = new TextEncoder();
    const key = await crypto.subtle.importKey(
      'raw',
      encoder.encode(secret),
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign']
    );

    const signatureBuffer = await crypto.subtle.sign(
      'HMAC',
      key,
      encoder.encode(rawBody)
    );

    const computedHex = Array.from(new Uint8Array(signatureBuffer))
      .map((b) => b.toString(16).padStart(2, '0'))
      .join('');

    return timingSafeEqual(computedHex, signatureHeader.trim());
  } catch (error) {
    console.error('Error verifying Linear signature:', error);
    return false;
  }
}

/**
 * Execute a GraphQL query or mutation against Linear API
 */
async function executeLinearGraphQL<T>(
  apiKey: string,
  query: string,
  variables: Record<string, unknown> = {}
): Promise<T> {
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
    throw new Error(`Linear API error (${response.status}): ${errorText}`);
  }

  const result = (await response.json()) as { data?: T; errors?: Array<{ message: string }> };
  if (result.errors && result.errors.length > 0) {
    throw new Error(`Linear GraphQL error: ${result.errors.map((e) => e.message).join(', ')}`);
  }

  return result.data!;
}

/**
 * Adds a comment to a Linear issue
 */
export async function addIssueComment(
  apiKey: string,
  issueId: string,
  body: string
): Promise<void> {
  const mutation = `
    mutation CreateComment($issueId: String!, $body: String!) {
      commentCreate(input: { issueId: $issueId, body: $body }) {
        success
      }
    }
  `;
  await executeLinearGraphQL(apiKey, mutation, { issueId, body });
}

/**
 * Updates the state of a Linear issue (e.g. to "In Progress")
 */
export async function updateIssueStateByName(
  apiKey: string,
  issueId: string,
  teamId: string,
  targetStateName: string
): Promise<void> {
  // First, find the workflow state ID for the given team
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

  interface TeamStatesResponse {
    team: {
      states: {
        nodes: Array<{ id: string; name: string }>;
      };
    };
  }

  const data = await executeLinearGraphQL<TeamStatesResponse>(apiKey, queryStates, { teamId });
  const targetState = data.team.states.nodes.find(
    (s) => s.name.toLowerCase() === targetStateName.toLowerCase()
  );

  if (!targetState) {
    console.warn(`Target state "${targetStateName}" not found in team ${teamId}`);
    return;
  }

  const mutationUpdate = `
    mutation UpdateIssue($issueId: String!, $stateId: String!) {
      issueUpdate(id: $issueId, input: { stateId: $stateId }) {
        success
      }
    }
  `;

  await executeLinearGraphQL(apiKey, mutationUpdate, { issueId, stateId: targetState.id });
}

