import { defineAgent } from "eve";

export default defineAgent({
  model: process.env.DEFAULT_MODEL || "mistral/mistral-nemo",
});
