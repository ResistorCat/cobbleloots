import { describe, it, expect, beforeEach, afterEach } from "vitest";
import saveFaqTool, { isAuthorizedAdmin } from "../agent/tools/save_faq";
import { getAdminIds, isAuthorizedAdmin as isAuthorizedAdminFromLib } from "../agent/lib/auth-utils";
import askQuestionTool from "../agent/tools/ask_question";

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

    expect(isAuthorizedAdminFromLib("111111")).toBe(true);
    expect(isAuthorizedAdminFromLib("999999")).toBe(false);
  });

  it("save_faq approval policy allows admins and rejects non-admins", () => {
    const policy = (saveFaqTool as any).approval.response;
    expect(policy).toBeDefined();

    const adminDecision = policy({
      responder: { principalId: "111111" },
    });
    expect(adminDecision.status).toBe("allowed");

    const nonAdminDecision = policy({
      responder: { principalId: "999999" },
    });
    expect(nonAdminDecision.status).toBe("rejected");
  });

  it("ask_question is defined as an Eve tool", () => {
    expect(askQuestionTool).toBeDefined();
  });
});
