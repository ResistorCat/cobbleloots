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
  const current = ctx?.session?.auth?.current;
  if (current?.attributes?.isAdmin === "true" || current?.attributes?.isAdmin === true) {
    return true;
  }
  const principalId = current?.principalId || current?.id;
  return isAuthorizedAdmin(principalId);
}

export function getSessionCallerId(ctx: any): string | undefined {
  return ctx?.session?.auth?.current?.principalId || ctx?.session?.auth?.current?.id;
}
