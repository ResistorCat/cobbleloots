import { defineState } from "eve/context";

export interface BugContext {
  loader?: "Fabric" | "NeoForge";
  version?: string;
  gameMode?: "survival" | "creative";
  details?: string;
}

export const bugSessionState = defineState<BugContext>(
  "cobbleloots.bugContext",
  () => ({})
);
