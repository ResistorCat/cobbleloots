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

export default (defineAgent as any)({
  instructions,
  model: aiGateway("mistral/mistral-nemo"),
  tools,
});
