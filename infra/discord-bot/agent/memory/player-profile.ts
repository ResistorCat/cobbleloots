import { defineMemory } from "eve/memory";
import { byPrincipal } from "eve/memory/scope";
import { fileMemory } from "eve/memory/file";
import { createSqliteMemoryBackend } from "../lib/sqlite-memory-backend.ts";

export default defineMemory({
  description: "Remember player preferences, Minecraft loader (Fabric/NeoForge), and playstyle across sessions.",
  provider: fileMemory({
    backend: createSqliteMemoryBackend(),
  }),
  scope: byPrincipal,
});
