import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { getAdminIds, isAuthorizedAdmin, isSessionAuthorizedAdmin } from "../agent/lib/auth-utils.ts";
import askQuestionTool from "../agent/tools/ask_question.ts";

describe("HITL & Approval Tools", () => {
  const originalEnv = process.env.DISCORD_ADMIN_IDS;

  beforeEach(() => {
    process.env.DISCORD_ADMIN_IDS = "111111,222222";
  });

  afterEach(() => {
    process.env.DISCORD_ADMIN_IDS = originalEnv;
  });

  it("getAdminIds parses and trims comma-separated admin IDs", () => {
    process.env.DISCORD_ADMIN_IDS = "  111111 , 222222 ,  , 333333  ";
    expect(getAdminIds()).toEqual(["111111", "222222", "333333"]);

    process.env.DISCORD_ADMIN_IDS = "";
    expect(getAdminIds()).toEqual([]);
  });

  it("isAuthorizedAdmin validates admin IDs correctly", () => {
    expect(isAuthorizedAdmin("111111")).toBe(true);
    expect(isAuthorizedAdmin("222222")).toBe(true);
    expect(isAuthorizedAdmin("999999")).toBe(false);
    expect(isAuthorizedAdmin(undefined)).toBe(false);
  });

  it("isSessionAuthorizedAdmin validates session context across principalId, id, and attributes", () => {
    // Via principalId
    expect(isSessionAuthorizedAdmin({ session: { auth: { current: { principalId: "111111" } } } })).toBe(true);
    // Via legacy id
    expect(isSessionAuthorizedAdmin({ session: { auth: { current: { id: "222222" } } } })).toBe(true);
    // Via attribute isAdmin = "true"
    expect(isSessionAuthorizedAdmin({ session: { auth: { current: { attributes: { isAdmin: "true" } } } } })).toBe(true);
    // Unauthorized user
    expect(isSessionAuthorizedAdmin({ session: { auth: { current: { principalId: "random_user" } } } })).toBe(false);
    expect(isSessionAuthorizedAdmin(null)).toBe(false);
  });

  it("ask_question is defined as an Eve tool with description and schemas", () => {
    expect(askQuestionTool).toBeDefined();
    expect((askQuestionTool as any).description).toContain("Ask the user a question");
    expect((askQuestionTool as any).inputSchema).toBeDefined();
    expect(typeof (askQuestionTool as any).execute).toBe("function");
  });
});
