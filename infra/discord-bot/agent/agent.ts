import { defineAgent } from "eve";
import { google } from "@ai-sdk/google";

const defaultModel = process.env.DEFAULT_MODEL || "gemini-3.8-flash";
const modelName = defaultModel.replace(/^google\//, "");

export default defineAgent({
  model: google(modelName),
});
