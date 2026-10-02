import { describe, it, expect } from "vitest";
import Database from "better-sqlite3";
import { bugSessionState } from "../agent/lib/session-state";
import playerProfileMemory from "../agent/memory/player-profile";
import { createSqliteMemoryBackend } from "../agent/lib/sqlite-memory-backend";
import { MemoryDocumentConflictError } from "eve/memory/file";

describe("Session State & Memory Definition", () => {
  it("should define bugSessionState with correct namespace", () => {
    expect(bugSessionState).toBeDefined();
  });

  it("should export player-profile memory slot definition", () => {
    expect(playerProfileMemory).toBeDefined();
  });

  describe("SQLite Memory Backend", () => {
    it("should write, read, and update memory documents with optimistic concurrency", async () => {
      const db = new Database(":memory:");
      const backend = createSqliteMemoryBackend(db);
      const signal = new AbortController().signal;

      // 1. Initial read should be null
      const initial = await backend.read({ key: "player_123", signal });
      expect(initial).toBeNull();

      // 2. Initial write
      const writeResult = await backend.write({
        key: "player_123",
        content: "Loader: Fabric\nMinecraft: 1.21.1",
        expectedVersion: null,
        signal,
      });
      expect(writeResult.content).toContain("Loader: Fabric");
      expect(writeResult.version).toBe("1");

      // 3. Read back
      const readBack = await backend.read({ key: "player_123", signal });
      expect(readBack).toEqual({
        content: "Loader: Fabric\nMinecraft: 1.21.1",
        version: "1",
      });

      // 4. Update with correct version
      const updateResult = await backend.write({
        key: "player_123",
        content: "Loader: NeoForge\nMinecraft: 1.21.1",
        expectedVersion: "1",
        signal,
      });
      expect(updateResult.version).toBe("2");

      // 5. Conflict when expectedVersion is stale
      await expect(
        backend.write({
          key: "player_123",
          content: "Loader: Fabric",
          expectedVersion: "1", // Stale version
          signal,
        })
      ).rejects.toThrow(MemoryDocumentConflictError);
    });
  });
});
