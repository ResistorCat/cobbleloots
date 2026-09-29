# Discord Release Announcement Template

Use this template when preparing player-facing release announcements for the Cobbleloots Discord server.

## Strict Constraints
- **Character Limit**: Standard Discord messages without Nitro have a **strict hard limit of 2,000 characters**.
- **Buffer Safety Target**: Keep total characters <= **1,800** (accounting for emoji markup, markdown formatting, and URLs).
- **Avoid Redundant IDs**: Summarize feature lists concisely; do not dump long raw identifier lists (e.g. `cobbleloots:item_id`) if they risk approaching the character limit.
- **Persistence**: Write the output to `DISCORD.md` (git-ignored) and display it to the user in a copy-pasteable code block.

---

## Server Custom Emoji IDs

- **Cobbleloots Logo**: `<:cobbleloots:1354451836500840519>`
- **Cobblemon Logo**: `<:cobblemon:1354449763189129330>`
- **Modrinth**: `<:modrinth:1354448797803221072>`
- **CurseForge**: `<:curseforge:1354449021883646105>`

---

## Markdown Template

```markdown
## <:cobbleloots:1354451836500840519> Hi @everyone!
<:cobblemon:1354449763189129330> **Cobbleloots v<version>** (<CHANNEL>) is out! 🎉

### ✨ What's New?
<Brief introductory summary of the release>

- 🌟 **<Feature 1 Name>** — <Short description>
  - <Sub-bullet if needed>
  > _Huge thanks to **<CommunityUser>** on Discord for suggesting the idea!_ <!-- If applicable -->

- 🎨 **<Feature 2 Name>** — <Short description>
- 🌐 **<Localization / Other>** — <Short description>

> See the full [changelog](https://modrinth.com/mod/cobbleloots/changelog) for all details.
> Report any issues in the [bug-reports](https://discord.com/channels/1314379156951269427/1354422908356923492) channel.

<:modrinth:1354448797803221072> [Modrinth Download](https://modrinth.com/mod/cobbleloots)
<:curseforge:1354449021883646105> [Curseforge Download](https://www.curseforge.com/minecraft/mc-mods/cobbleloots)
```
