import { defineAgent } from "eve";

export default defineAgent({
  model: process.env.DEFAULT_MODEL || "google/gemini-2.5-flash",
});
