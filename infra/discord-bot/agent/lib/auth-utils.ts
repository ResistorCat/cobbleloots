export function getAdminIds(): string[] {
  return (process.env.DISCORD_ADMIN_IDS || "")
    .split(",")
    .map((id) => id.trim())
    .filter(Boolean);
}

export function isAuthorizedAdmin(principalId: string | undefined): boolean {
  if (!principalId) return false;
  return getAdminIds().includes(principalId);
}

export function isSessionAuthorizedAdmin(ctx: any): boolean {
  if (!ctx) return false;
  const auth = ctx?.session?.auth;
  const current = auth?.current;
  const initiator = auth?.initiator;

  for (const principal of [current, initiator]) {
    if (!principal) continue;
    if (principal.attributes?.isAdmin === "true" || principal.attributes?.isAdmin === true) {
      return true;
    }
    const id = principal.principalId || principal.id;
    if (isAuthorizedAdmin(id)) {
      return true;
    }
  }
  return false;
}

export function getSessionCallerId(ctx: any): string | undefined {
  return ctx?.session?.auth?.current?.principalId || ctx?.session?.auth?.current?.id;
}
