import type Database from "better-sqlite3";
import { MemoryDocumentConflictError, type MemoryDocumentBackend } from "eve/memory/file";
import { getDb } from "./db.ts";

export function createSqliteMemoryBackend(dbInstance?: Database.Database): MemoryDocumentBackend {
  const db = dbInstance || getDb();

  db.exec(`
    CREATE TABLE IF NOT EXISTS player_memory (
      key TEXT PRIMARY KEY,
      content TEXT NOT NULL,
      version INTEGER NOT NULL DEFAULT 1
    );
  `);

  const selectStmt = db.prepare("SELECT content, version FROM player_memory WHERE key = ?");
  const insertStmt = db.prepare(
    "INSERT INTO player_memory (key, content, version) VALUES (?, ?, ?)"
  );
  const updateStmt = db.prepare(
    "UPDATE player_memory SET content = ?, version = ? WHERE key = ? AND version = ?"
  );

  return {
    async read({ key, signal }) {
      signal?.throwIfAborted();
      const row = selectStmt.get(key) as { content: string; version: number } | undefined;
      if (!row) return null;
      return { content: row.content, version: String(row.version) };
    },

    async write({ key, content, expectedVersion, signal }) {
      signal?.throwIfAborted();
      const current = selectStmt.get(key) as { content: string; version: number } | undefined;
      const currentVersion = current ? String(current.version) : null;

      if (currentVersion !== expectedVersion) {
        throw new MemoryDocumentConflictError(key);
      }

      if (!current) {
        insertStmt.run(key, content, 1);
        return { content, version: "1" };
      }

      const nextVersion = current.version + 1;
      const info = updateStmt.run(content, nextVersion, key, current.version);
      if (info.changes === 0) {
        throw new MemoryDocumentConflictError(key);
      }

      return { content, version: String(nextVersion) };
    },
  };
}
