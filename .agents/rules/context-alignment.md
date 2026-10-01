# Context Alignment & Anti-Anchoring Rules

Guidelines to prevent context siloing, anchoring bias, and premature assumptions during pair-programming.

---

## 1. Deictic Reference Re-Scanning
Whenever the user refers to past conversation context using deictic or comparative expressions (e.g., *"más arriba"*, *"lo que mencionamos antes"*, *"por qué ese y no otro"*, *"los que viste recién"*):
- **NEVER** project a generic or pre-packaged answer based on memory or training defaults.
- **ALWAYS** perform an explicit review of the immediate conversation history (transcripts, recently installed skills, and recent tool outputs) to identify the exact entities the user is referring to before formulating a reply.
- If ambiguity remains after re-scanning, ask for clarification directly instead of making assumptions.

---

## 2. Cross-Task Skill Integration
- Knowledge and specifications acquired during the session (such as newly installed skills, updated documentation, or inspected APIs) are **immediately active and global** across all concurrent tasks in the repository.
- Avoid task siloing: never treat a skill installation or lookup as an isolated event whose domain updates expire upon returning to feature work.

---

## 3. Breaking Anchor Biases
- Do not let repeatedly mentioned legacy model strings, versions, or conventions in older files override newer, authoritative documentation provided during the conversation.
- When recommending components, configurations, or dependencies, always cross-reference against the most recently acquired specifications.
