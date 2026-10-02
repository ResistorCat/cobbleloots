import { defineAgent } from "eve";
import instructions from "./instructions.ts";
import searchDocs from "./tools/search_docs.ts";
import searchFaqs from "./tools/search_faqs.ts";
import listFaqs from "./tools/list_faqs.ts";
import getFaq from "./tools/get_faq.ts";
import saveFaq from "./tools/save_faq.ts";
import deleteFaq from "./tools/delete_faq.ts";
import inspectCode from "./tools/inspect_code.ts";
import getReleases from "./tools/get_releases.ts";
import askQuestion from "./tools/ask_question.ts";

export const aiGateway = (model: string) => model;

export const tools = [
  searchDocs,
  searchFaqs,
  listFaqs,
  getFaq,
  saveFaq,
  deleteFaq,
  inspectCode,
  getReleases,
  askQuestion,
];

const agent = defineAgent({
  model: process.env.DEFAULT_MODEL || aiGateway("mistral/mistral-nemo"),
  defaultTools: false,
});

Object.defineProperty(agent, "instructions", {
  value: instructions,
  enumerable: false,
  configurable: true,
});

Object.defineProperty(agent, "tools", {
  value: tools,
  enumerable: false,
  configurable: true,
});

export default agent;

