import Database from "better-sqlite3";
import path from "node:path";
import fs from "node:fs";

let defaultDb: Database.Database | null = null;

export function getDb(customPath?: string): Database.Database {
  if (customPath) {
    const db = new Database(customPath);
    db.pragma("journal_mode = WAL");
    return db;
  }

  if (defaultDb) return defaultDb;

  const dbPath = process.env.DATABASE_PATH || path.resolve(process.cwd(), "data/bot.db");
  const dir = path.dirname(dbPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  defaultDb = new Database(dbPath);
  defaultDb.pragma("journal_mode = WAL");
  return defaultDb;
}
