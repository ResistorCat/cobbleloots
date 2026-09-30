import type Database from "better-sqlite3";

export interface FaqRecord {
  id: number;
  question: string;
  answer: string;
  category: string;
  status: string;
  approved_by: string | null;
  created_at: string;
}

export interface NewFaq {
  question: string;
  answer: string;
  category?: string;
  approvedBy?: string;
}

export function initFaqSchema(db: Database.Database): void {
  db.exec(`
    CREATE TABLE IF NOT EXISTS faqs (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      question TEXT NOT NULL,
      answer TEXT NOT NULL,
      category TEXT NOT NULL DEFAULT 'general',
      status TEXT NOT NULL DEFAULT 'approved',
      approved_by TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );
    CREATE INDEX IF NOT EXISTS idx_faqs_category ON faqs(category);
  `);
}

export function insertFaq(db: Database.Database, faq: NewFaq): number {
  const stmt = db.prepare(`
    INSERT INTO faqs (question, answer, category, approved_by)
    VALUES (?, ?, ?, ?)
  `);
  const info = stmt.run(
    faq.question.trim(),
    faq.answer.trim(),
    faq.category?.trim() || "general",
    faq.approvedBy || null
  );
  return Number(info.lastInsertRowid);
}

export function getFaqById(db: Database.Database, id: number): FaqRecord | null {
  const stmt = db.prepare(`SELECT * FROM faqs WHERE id = ?`);
  const row = stmt.get(id) as FaqRecord | undefined;
  return row ?? null;
}

export function searchFaqs(db: Database.Database, query: string, limit = 5): FaqRecord[] {
  const trimmed = query.trim();
  if (!trimmed) return [];

  const stmt = db.prepare(`
    SELECT * FROM faqs
    WHERE question LIKE ? OR answer LIKE ?
    ORDER BY id DESC
    LIMIT ?
  `);
  const pattern = `%${trimmed}%`;
  return stmt.all(pattern, pattern, limit) as FaqRecord[];
}
