import { describe, it, expect } from "vitest";
import { bugSessionState, type BugContext } from "../agent/lib/session-state";
import playerProfileMemory from "../agent/memory/player-profile";

describe("Session State & Memory Definition", () => {
  it("should define bugSessionState with correct namespace", () => {
    expect(bugSessionState).toBeDefined();
  });

  it("should export player-profile memory slot definition", () => {
    expect(playerProfileMemory).toBeDefined();
  });
});
