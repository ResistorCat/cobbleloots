/**
 * CobbleLoots Linear Webhook Relay
 * Cloudflare Worker entry point
 */

import {
  LinearWebhookPayload,
  verifyLinearSignature,
  updateIssueStateByName,
  addIssueComment,
} from './linear';
import { triggerGitHubDispatch, buildBranchName } from './github';

export interface Env {
  // Configured in wrangler.toml [vars]
  GITHUB_OWNER: string;
  GITHUB_REPO: string;
  AGENT_LABEL: string;
  AGENT_ASSIGNEE_NAME: string;

  // Secrets set via `wrangler secret put`
  LINEAR_WEBHOOK_SECRET: string;
  LINEAR_API_KEY: string;
  GITHUB_PAT: string;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // Only accept POST requests
    if (request.method !== 'POST') {
      return new Response(JSON.stringify({ error: 'Method not allowed' }), {
        status: 405,
        headers: { 'Content-Type': 'application/json' },
      });
    }

    const rawBody = await request.text();
    const signature = request.headers.get('linear-signature');

    // 1. Verify HMAC Signature
    const isValid = await verifyLinearSignature(
      rawBody,
      signature,
      env.LINEAR_WEBHOOK_SECRET
    );

    if (!isValid) {
      console.error('Invalid Linear signature received');
      return new Response(JSON.stringify({ error: 'Invalid signature' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      });
    }

    let payload: LinearWebhookPayload;
    try {
      payload = JSON.parse(rawBody);
    } catch {
      return new Response(JSON.stringify({ error: 'Invalid JSON payload' }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' },
      });
    }

    // 2. Filter: Only process Issue create or update events
    if (payload.type !== 'Issue' || (payload.action !== 'create' && payload.action !== 'update')) {
      return new Response(JSON.stringify({ status: 'ignored', reason: 'Not an Issue create/update' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    }

    const issue = payload.data;
    const isAssignedToAgent =
      issue.assignee?.name?.toLowerCase() === (env.AGENT_ASSIGNEE_NAME || 'agent').toLowerCase();
    const hasAgentLabel =
      issue.labels?.some(
        (l) => l.name.toLowerCase() === (env.AGENT_LABEL || 'agent:run').toLowerCase()
      ) ?? false;

    // 3. Match condition
    if (!isAssignedToAgent && !hasAgentLabel) {
      return new Response(
        JSON.stringify({ status: 'ignored', reason: 'Issue not assigned to agent and missing agent label' }),
        { status: 200, headers: { 'Content-Type': 'application/json' } }
      );
    }

    // 4. Guard: Prevent loops and redundant runs if the issue is in review, in progress, or closed
    const stateType = issue.state?.type?.toLowerCase() || '';
    const stateName = issue.state?.name?.toLowerCase() || '';
    if (
      stateType === 'completed' ||
      stateType === 'canceled' ||
      stateName === 'in progress' ||
      stateName === 'in review'
    ) {
      return new Response(
        JSON.stringify({
          status: 'ignored',
          reason: `Issue is already ${stateName || stateType}`,
        }),
        { status: 200, headers: { 'Content-Type': 'application/json' } }
      );
    }

    const identifier = issue.identifier || issue.id;
    const title = issue.title || 'Untitled Issue';
    const description = issue.description || '';
    const issueUrl = issue.url || `https://linear.app/issue/${identifier}`;
    const branchName = buildBranchName(identifier, title);

    console.log(`Processing ticket ${identifier}: "${title}" -> branch ${branchName}`);

    try {
      // 5. Update Linear state to "In Progress"
      if (issue.team?.id) {
        await updateIssueStateByName(env.LINEAR_API_KEY, issue.id, issue.team.id, 'In Progress');
      }

      // 6. Post initial comment in Linear
      const initialComment = `🤖 **CobbleLoots Software Factory**: Ticket recibido. Despachando tarea hacia GitHub Actions en la rama \`${branchName}\`...`;
      await addIssueComment(env.LINEAR_API_KEY, issue.id, initialComment);

      // 7. Trigger GitHub Actions repository_dispatch
      await triggerGitHubDispatch(env.GITHUB_OWNER, env.GITHUB_REPO, env.GITHUB_PAT, {
        id: issue.id,
        identifier,
        title,
        description,
        url: issueUrl,
        branchName,
      });

      return new Response(
        JSON.stringify({
          status: 'dispatched',
          issue: identifier,
          branch: branchName,
        }),
        {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        }
      );
    } catch (error) {
      console.error('Error during relay execution:', error);
      return new Response(
        JSON.stringify({
          error: 'Internal relay error',
          message: error instanceof Error ? error.message : String(error),
        }),
        {
          status: 500,
          headers: { 'Content-Type': 'application/json' },
        }
      );
    }
  },
};

