# Task 4 Report: Dynamic Instructions with Bilingual Logic & Agent Definition

## Execution Summary
- **Status**: DONE
- **Commit**: `9e9fc53` - `feat(bot): configure bilingual dynamic instructions and register pocketbase tools in agent`
- **Subproject**: `infra/discord-bot`

## Work Completed

### 1. Dynamic Instructions (`agent/instructions.ts`)
- Configured bilingual dynamic instructions via `defineDynamic` and `buildInstructionsPrompt`:
  - **Default Language**: English is enforced as default and primary community language.
  - **Spanish Adaptability**: Natural, fluent Spanish responses when addressed in Spanish, while strictly preserving Minecraft commands (e.g. `/cobbleloots reset`), item identifiers, and configs.
  - **Role-Based Adaptation**:
    - **Admins**: Grants maintainer mode, technically precise responses with Java class names (`CobblelootsLootBall.java`), code inspection (`inspect_code`), and PocketBase FAQ management (`save_faq`, `list_faqs`, `get_faq`, `delete_faq`).
    - **Standard Players**: Clear player-facing gameplay guidance without internal Java jargon, grounded in docs, FAQs, and releases.
  - **Anti-Rush Protocol**: Mandates asking clarifying questions with 2-3 options when critical environment details (Minecraft version, Fabric vs NeoForge loader, gamemode) are omitted before answering.
  - **Thread Context Rules**: Analyzes `[Thread History]` transcripts to avoid duplicate advice and preserve established context.

### 2. Agent Definition & Tool Registration (`agent/agent.ts`)
- Registered all 9 grounding and PocketBase management tools:
  - `searchDocs` (`search_docs.ts`)
  - `searchFaqs` (`search_faqs.ts`)
  - `listFaqs` (`list_faqs.ts`)
  - `getFaq` (`get_faq.ts`)
  - `saveFaq` (`save_faq.ts`)
  - `deleteFaq` (`delete_faq.ts`)
  - `inspectCode` (`inspect_code.ts`)
  - `getReleases` (`get_releases.ts`)
  - `askQuestion` (`ask_question.ts`)
- Routed through Vercel AI Gateway model (`mistral/mistral-nemo`) via `aiGateway` helper.

### 3. Test Suites Cleaned & Updated
- `tests/agent-config.test.ts`:
  - Verified bilingual instructions rules and thread context.
  - Verified standard player vs admin maintainer mode prompts.
  - Verified model, dynamic instructions, and registration of all 9 tools on `agentConfig`.
- `tests/tools.test.ts`:
  - Removed legacy SQLite `db.ts` and `faq-store.ts` imports and obsolete SQLite FAQ test.
  - Preserved grounding tools tests (`search_docs`, `inspect_code`, `get_releases`, and GitHub client).
- `tests/hitl-tools.test.ts`:
  - Removed obsolete `saveFaqTool.approval.response` check (since `save_faq` now uses PocketBase and `isAuthorizedAdmin` inside `execute`, tested in `faq-tools.test.ts`).
  - Added clean assertions verifying `ask_question` tool definition, description, schema, and execution function.

## Verification Results
- **Vitest**: `pnpm test` -> 6 test files passed, 52 total tests passed (100% green).
- **TypeScript**: `pnpm run typecheck` (`tsc --noEmit`) -> 0 type errors.

## Concerns / Recommendations
- None. All packages and test suites in `infra/discord-bot` are clean and passing.
