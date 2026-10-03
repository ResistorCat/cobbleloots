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
    // Attempt PocketBase v0.23+ superuser authentication first
    try {
      if (typeof client.collection === "function") {
        const col = client.collection("_superusers");
        if (col && typeof col.authWithPassword === "function") {
          await col.authWithPassword(email, password);
          return true;
        }
      }
    } catch {
      // Fall through to legacy admin authentication if _superusers collection fails
    }

    // Fallback for PocketBase < v0.23 legacy admin authentication
    if ((client as any).admins?.authWithPassword) {
      await (client as any).admins.authWithPassword(email, password);
      return true;
    }
    return false;
  } catch (err) {
    console.warn("[PocketBase] Authentication failed:", (err as Error).message);
    return false;
  }
}

export async function getAuthenticatedPbClient(): Promise<PocketBase> {
  const client = getPbClient();
  if (!client.authStore?.isValid) {
    await ensurePocketBaseAuth(client);
  }
  return client;
}

export async function ensureFaqsCollection(client = getPbClient()): Promise<void> {
  const authed = await ensurePocketBaseAuth(client);
  if (!authed) {
    console.warn("[PocketBase] Skipping collection setup: authentication failed or credentials missing.");
    return;
  }

  try {
    await client.collections.getOne("faqs");
    console.log("[PocketBase] Collection 'faqs' already exists.");
  } catch {
    // Collection does not exist, create it
    try {
      await client.collections.create({
        name: "faqs",
        type: "base",
        fields: [
          { name: "id", type: "text", primaryKey: true },
          { name: "question", type: "text", required: true },
          { name: "answer", type: "text", required: true },
          { name: "keywords", type: "text", required: false },
          { name: "category", type: "text", required: true },
          { name: "created_by", type: "text", required: false },
        ],
        schema: [
          { name: "question", type: "text", required: true },
          { name: "answer", type: "text", required: true },
          { name: "keywords", type: "text", required: false },
          { name: "category", type: "text", required: true },
          { name: "created_by", type: "text", required: false },
        ],
        listRule: "",
        viewRule: "",
        createRule: null,
        updateRule: null,
        deleteRule: null,
      });
      console.log("[PocketBase] Auto-created 'faqs' collection schema.");
    } catch (createErr) {
      console.warn("[PocketBase] Could not auto-create 'faqs' collection:", (createErr as Error).message);
    }
  }
}
