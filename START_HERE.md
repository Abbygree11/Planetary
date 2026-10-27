# START HERE — Planetary (AI / developer quick entry)

## Возврат к Phase 3 без истории чата

После чтения SESSION_HANDOFF и живого HEAD открыть **[индекс итогов Phase 3](docs/phases/phase-03/results/README.md)** и конкретную [карточку 3A–3E](docs/phases/phase-03/results/README.md), затем [план](docs/phases/phase-03.md), [полный gate](docs/research/PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md) и соответствующие `docs/research/PHASE3_R*.md`/TSV/Java tests. По мере следующих research-пакетов обновлять и **план, и файл результатов**, чтобы уже изученные original NeoForge/моды/решения/риски не исчезли при позднем переходе к разработке третьей фазы. В файлах результатов SOURCE/CI/game/mod-gates строго различаются.

> **MANDATORY for a new chat:** first read this short index and [SESSION_HANDOFF](docs/SESSION_HANDOFF.md). The authoritative runtime state is **current GitHub branch `2.0`**; never infer HEAD/CI from a previously saved markdown snapshot.

## 60-second bootstrap

1. Check HEAD `Abbygree11/Planetary`, branch **`2.0`**. **Never modify `main`.**
2. Read [SESSION_HANDOFF](docs/SESSION_HANDOFF.md): current stage, latest code commit, CI and manual acceptance, next micro-task, known regressions.
3. Read [CHAT_REPORT_TEMPLATE](docs/CHAT_REPORT_TEMPLATE.md) for mandatory stage-labelled Russian updates. User's `кк` means continue with the next **one whole mechanism-family / bounded micro-task**; no repeated long introductions.
4. Consult only **relevant** sections of [AGENTS.md](AGENTS.md), [2.4A card](docs/phases/phase-02/05-implementation-a.md), [implementation roadmap](docs/IMPLEMENTATION_PLAN.md), and stage-specific research. Deep archive [AI_CONTEXT](docs/AI_CONTEXT.md) is not a first-pass full read. If uncertain, expand the exact needed section.
5. Before code: audit real declaring algorithm owner, subclass overrides and mod bypasses, physical-vs-canonical space, 6 faces / 12 edges / 8 corners and no-Planet behavior. Core semantics in stable Java helpers, narrow NeoForge/Mixin adapters; no global world-coordinate rewrites.
6. After changes: commit to `2.0`, check **that code-changing commit's** Java 21 compile/JUnit/ASM GitHub Actions outcome. CI != applied client Mixins != gameplay != loaded mod.
7. Update [SESSION_HANDOFF](docs/SESSION_HANDOFF.md) and relevant roadmap to a truthful, resumable checkpoint **before ending the turn**. No speculative PASS.

### Fixed environment
Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · Gradle 8.12 · single physical XYZ block world with six canonical face frames. Goal: survival and mod compatibility without breaking vanilla/NeoForge extension hooks.

### Primary user expectations
Answer in Russian. Every user-visible progress and final message identifies `Planetary — 2.4A-N: <mechanism>` and reports source/code, branch+commit, CI link/status, actual client/game/mod gates, checks and exact next stage. Commit one useful bounded packet per turn, not a monolithic hours-long answer. Keep dev acceptance lab disposable; preserve previously skipped **2.4A-6/7/8 V6** scenarios until their user checks are completed.

**Do not** assume this file replaces full [AGENTS.md](AGENTS.md) when implementing a mechanism. It is a fast router, not a waiver of hard gates.
