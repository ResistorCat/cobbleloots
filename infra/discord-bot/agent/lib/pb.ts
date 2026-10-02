import PocketBase from "pocketbase";

let pbInstance: PocketBase | null = null;

export function resetPbClientForTesting(): void {
  pbInstance = null;
}

export function getPbClient(): PocketBase {
  if (pbInstance) return pbInstance;
  const url = process.env.POCKETBASE_URL || "http://127.0.0.1:8090";
  pbInstance = new PocketBase(url);
  return pbInstance;
}

export async function ensurePocketBaseAuth(client = getPbClient()): Promise<boolean> {
  const email = process.env.POCKETBASE_ADMIN_EMAIL;
  const password = process.env.POCKETBASE_ADMIN_PASSWORD;

  if (!email || !password) {
    return false;
  }

  try {
    // PocketBase v0.23+ superusers or legacy admins
    if ("_superusers" in client.collection) {
      await client.collection("_superusers").authWithPassword(email, password);
    } else if ((client as any).admins?.authWithPassword) {
      await (client as any).admins.authWithPassword(email, password);
    }
    return true;
  } catch (err) {
    console.warn("[PocketBase] Authentication failed:", (err as Error).message);
    return false;
  }
}

export async function ensureFaqsCollection(client = getPbClient()): Promise<void> {
  const authed = await ensurePocketBaseAuth(client);
  if (!authed) return;

  try {
    await client.collections.getOne("faqs");
  } catch {
    // Collection does not exist, create it
    try {
      await client.collections.create({
        name: "faqs",
        type: "base",
        schema: [
          { name: "question", type: "text", required: true },
          { name: "answer", type: "text", required: true },
          { name: "keywords", type: "text", required: false },
          { name: "category", type: "text", required: true },
          { name: "created_by", type: "text", required: false },
        ],
      });
      console.log("[PocketBase] Auto-created 'faqs' collection schema.");
    } catch (createErr) {
      console.warn("[PocketBase] Could not auto-create 'faqs' collection:", (createErr as Error).message);
    }
  }
}
