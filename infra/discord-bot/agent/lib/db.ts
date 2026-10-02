import Database from "better-sqlite3";
import path from "node:path";
import fs from "node:fs";

let defaultDb: Database.Database | null = null;

function resolveNativeBinding(): string | undefined {
  const target = `${process.platform}-${process.arch}.node`;
  const candidates = [
    path.resolve(process.cwd(), "node_modules/better-sqlite3/prebuilds", target),
    path.resolve("/app/node_modules/better-sqlite3/prebuilds", target),
    path.resolve(process.cwd(), ".output/server/prebuilds", target),
    path.resolve("/app/.output/server/prebuilds", target),
    path.resolve(process.cwd(), ".output/server/build/Release/better_sqlite3.node"),
    path.resolve("/app/.output/server/build/Release/better_sqlite3.node"),
    path.resolve(process.cwd(), "node_modules/better-sqlite3/build/Release/better_sqlite3.node"),
    path.resolve("/app/node_modules/better-sqlite3/build/Release/better_sqlite3.node"),
  ];

  for (const p of candidates) {
    if (fs.existsSync(p)) return p;
  }
  return undefined;
}

function getDatabaseOptions(): Database.Options {
  const binding = resolveNativeBinding();
  return binding ? { nativeBinding: binding } : {};
}

export function getDb(customPath?: string): Database.Database {
  const opts = getDatabaseOptions();

  if (customPath) {
    const db = new Database(customPath, opts);
    db.pragma("journal_mode = WAL");
    return db;
  }

  if (defaultDb) return defaultDb;

  const dbPath = process.env.DATABASE_PATH || path.resolve(process.cwd(), "data/bot.db");
  const dir = path.dirname(dbPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  defaultDb = new Database(dbPath, opts);
  defaultDb.pragma("journal_mode = WAL");
  return defaultDb;
}
