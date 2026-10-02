import { describe, it, expect, beforeEach, vi } from "vitest";
import PocketBase from "pocketbase";
import {
  getPbClient,
  resetPbClientForTesting,
  ensurePocketBaseAuth,
  ensureFaqsCollection,
} from "../agent/lib/pb.ts";

describe("PocketBase Client Singleton", () => {
  beforeEach(() => {
    resetPbClientForTesting();
    delete process.env.POCKETBASE_URL;
    delete process.env.POCKETBASE_ADMIN_EMAIL;
    delete process.env.POCKETBASE_ADMIN_PASSWORD;
  });

  it("should return a PocketBase client instance pointing to configured URL", () => {
    process.env.POCKETBASE_URL = "http://127.0.0.1:8090";
    const client = getPbClient();
    expect(client).toBeDefined();
    expect(client.baseUrl).toBe("http://127.0.0.1:8090");
  });

  it("should reuse the same client singleton on subsequent calls", () => {
    const client1 = getPbClient();
    const client2 = getPbClient();
    expect(client1).toBe(client2);
  });

  it("should reset client instance when resetPbClientForTesting is called", () => {
    const client1 = getPbClient();
    resetPbClientForTesting();
    const client2 = getPbClient();
    expect(client1).not.toBe(client2);
  });
});

describe("ensurePocketBaseAuth", () => {
  beforeEach(() => {
    resetPbClientForTesting();
    delete process.env.POCKETBASE_ADMIN_EMAIL;
    delete process.env.POCKETBASE_ADMIN_PASSWORD;
  });

  it("should return false if admin credentials are missing", async () => {
    const authed = await ensurePocketBaseAuth();
    expect(authed).toBe(false);
  });

  it("should return true when authentication succeeds via admins", async () => {
    process.env.POCKETBASE_ADMIN_EMAIL = "admin@example.com";
    process.env.POCKETBASE_ADMIN_PASSWORD = "password123";

    const mockClient = {
      collection: () => ({}),
      admins: {
        authWithPassword: vi.fn().mockResolvedValue({ token: "fake-token" }),
      },
    } as unknown as PocketBase;

    const result = await ensurePocketBaseAuth(mockClient);
    expect(result).toBe(true);
    expect((mockClient as any).admins.authWithPassword).toHaveBeenCalledWith("admin@example.com", "password123");
  });

  it("should return true when authentication succeeds via _superusers collection", async () => {
    process.env.POCKETBASE_ADMIN_EMAIL = "admin@example.com";
    process.env.POCKETBASE_ADMIN_PASSWORD = "password123";

    const mockAuthWithPassword = vi.fn().mockResolvedValue({ token: "fake-token" });
    const mockCollection = Object.assign(
      (name: string) => ({ authWithPassword: mockAuthWithPassword }),
      { _superusers: true }
    );
    const mockClient = {
      collection: mockCollection,
    } as unknown as PocketBase;

    const result = await ensurePocketBaseAuth(mockClient);
    expect(result).toBe(true);
    expect(mockAuthWithPassword).toHaveBeenCalledWith("admin@example.com", "password123");
  });

  it("should return false and log warning if auth throws error", async () => {
    process.env.POCKETBASE_ADMIN_EMAIL = "admin@example.com";
    process.env.POCKETBASE_ADMIN_PASSWORD = "wrongpassword";

    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
    const mockClient = {
      collection: () => ({}),
      admins: {
        authWithPassword: vi.fn().mockRejectedValue(new Error("Invalid credentials")),
      },
    } as unknown as PocketBase;

    const result = await ensurePocketBaseAuth(mockClient);
    expect(result).toBe(false);
    expect(warnSpy).toHaveBeenCalledWith("[PocketBase] Authentication failed:", "Invalid credentials");
    warnSpy.mockRestore();
  });
});

describe("ensureFaqsCollection", () => {
  beforeEach(() => {
    resetPbClientForTesting();
    delete process.env.POCKETBASE_ADMIN_EMAIL;
    delete process.env.POCKETBASE_ADMIN_PASSWORD;
  });

  it("should return early if not authenticated", async () => {
    const mockClient = {
      collection: () => ({}),
      collections: {
        getOne: vi.fn(),
        create: vi.fn(),
      },
    } as unknown as PocketBase;

    await ensureFaqsCollection(mockClient);
    expect(mockClient.collections.getOne).not.toHaveBeenCalled();
    expect(mockClient.collections.create).not.toHaveBeenCalled();
  });

  it("should not create collection if faqs already exists", async () => {
    process.env.POCKETBASE_ADMIN_EMAIL = "admin@example.com";
    process.env.POCKETBASE_ADMIN_PASSWORD = "password123";

    const mockClient = {
      collection: () => ({}),
      admins: {
        authWithPassword: vi.fn().mockResolvedValue({ token: "fake-token" }),
      },
      collections: {
        getOne: vi.fn().mockResolvedValue({ id: "col1", name: "faqs" }),
        create: vi.fn(),
      },
    } as unknown as PocketBase;

    await ensureFaqsCollection(mockClient);
    expect(mockClient.collections.getOne).toHaveBeenCalledWith("faqs");
    expect(mockClient.collections.create).not.toHaveBeenCalled();
  });

  it("should create collection if faqs does not exist", async () => {
    process.env.POCKETBASE_ADMIN_EMAIL = "admin@example.com";
    process.env.POCKETBASE_ADMIN_PASSWORD = "password123";

    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const mockClient = {
      collection: () => ({}),
      admins: {
        authWithPassword: vi.fn().mockResolvedValue({ token: "fake-token" }),
      },
      collections: {
        getOne: vi.fn().mockRejectedValue(new Error("Not found")),
        create: vi.fn().mockResolvedValue({ id: "new_faqs", name: "faqs" }),
      },
    } as unknown as PocketBase;

    await ensureFaqsCollection(mockClient);
    expect(mockClient.collections.getOne).toHaveBeenCalledWith("faqs");
    expect(mockClient.collections.create).toHaveBeenCalledWith(
      expect.objectContaining({
        name: "faqs",
        type: "base",
        schema: expect.arrayContaining([
          { name: "question", type: "text", required: true },
          { name: "answer", type: "text", required: true },
          { name: "keywords", type: "text", required: false },
          { name: "category", type: "text", required: true },
          { name: "created_by", type: "text", required: false },
        ]),
      })
    );
    expect(logSpy).toHaveBeenCalledWith("[PocketBase] Auto-created 'faqs' collection schema.");
    logSpy.mockRestore();
  });

  it("should catch error if collection creation fails", async () => {
    process.env.POCKETBASE_ADMIN_EMAIL = "admin@example.com";
    process.env.POCKETBASE_ADMIN_PASSWORD = "password123";

    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
    const mockClient = {
      collection: () => ({}),
      admins: {
        authWithPassword: vi.fn().mockResolvedValue({ token: "fake-token" }),
      },
      collections: {
        getOne: vi.fn().mockRejectedValue(new Error("Not found")),
        create: vi.fn().mockRejectedValue(new Error("Permission denied")),
      },
    } as unknown as PocketBase;

    await ensureFaqsCollection(mockClient);
    expect(warnSpy).toHaveBeenCalledWith("[PocketBase] Could not auto-create 'faqs' collection:", "Permission denied");
    warnSpy.mockRestore();
  });
});
