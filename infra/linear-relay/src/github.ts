/**
 * GitHub API client for triggering repository_dispatch
 */

export interface DispatchIssuePayload {
  id: string;
  identifier: string;
  title: string;
  description: string;
  url: string;
  branchName: string;
}

/**
 * Creates a sanitized git branch name from an issue identifier and title
 */
export function buildBranchName(identifier: string, title: string): string {
  const cleanTitle = title
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 40);

  return `agent/${identifier.toLowerCase()}-${cleanTitle}`;
}

/**
 * Dispatches a repository_dispatch event to the GitHub repository
 */
export async function triggerGitHubDispatch(
  owner: string,
  repo: string,
  token: string,
  issuePayload: DispatchIssuePayload
): Promise<void> {
  const url = `https://api.github.com/repos/${owner}/${repo}/dispatches`;

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      Accept: 'application/vnd.github.v3+json',
      Authorization: `Bearer ${token}`,
      'User-Agent': 'Cobbleloots-Linear-Relay',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      event_type: 'linear_agent_task',
      client_payload: {
        issue: issuePayload,
      },
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `GitHub API error (${response.status}) when triggering dispatch: ${errorText}`
    );
  }
}

