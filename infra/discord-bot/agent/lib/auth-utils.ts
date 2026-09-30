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
