# START HERE — Planetary (AI / developer quick entry)

## 2026-10-30 — `3R-7G`: all 151 owner provenance reconciled; next bounded source `3R-7H-1`

[Original four-part 3R-7G research report](docs/research/PHASE3_R7G_COMPLETE_151_SOURCE_OWNER_R_GATE_RECONCILIATION_1_21_1.md) · [effective 151 owner gate](docs/research/PHASE3_R7G_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) · [all 151 evidence/grades](docs/research/PHASE3_R7G_ALL_151_OWNER_EVIDENCE_AUDIT_1_21_1.tsv) · [27 exact source contracts](docs/research/PHASE3_R7G_27_OPEN_SOURCE_CONTRACT_GUARDS_1_21_1.tsv) · [18 stale source anchors](docs/research/PHASE3_R7G_18_RECONCILED_PRIOR_SOURCES_1_21_1.tsv) · [64-row allowed delta](docs/research/PHASE3_R7G_SOURCE_GATE_RECONCILIATION_DELTA_1_21_1.tsv). [CI #40969013764 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40969013764); independently downloaded artifact **12940220210**, ZIP SHA256 `7ace9104abcf864fa1b115d9b095cf2d115350a3899f4f7594f8f31e001f87bc`, all 7 members individually SHA checked. This is a **151-row source-report provenance/integrity CI**, NOT new target NeoForge JVM or game/Mixin/loaded-mod CI. Audited exactly 41 Phase3A + 25 Phase3B + 48 Phase3C + 15 Phase3D + 22 Phase3E, unique; 18 old source anchors reconciled **without grade promotion**, 46 old source guards divided into **27 actual open source-owner contract guards** + **19 selected-source accepted but real gameplay V pending**. Other 74 selected source/runtime-gap rows unchanged, 11 prior bounded SOURCE PASS unchanged, G07 D14/E22 original published shader JAR source/binary **with Iris Minecraft `[1.21,1.21.1)` loader metadata discrepancy** and real game V unresolved. **Full Phase3 research R remains OPEN (27 source contracts + shader loader scope decision), Phase2 visual V remains OPEN. NO production/Mixin/game worlds, never main, no 2.4A.** The next research worklist is split 3R-7H-1 (6 A/B cache and occlusion owners), 7H-2 (7 original shape callers), 7H-3 (10 foreign/mod-world/worker owners), 7H-4 (4 BER special-source owners). **NEXT user `кк` = `3R-7H-1` complete bounded 4/4 with original 21.1.215 Java21/JVM source, mod hooks, CI/member SHA, exact A03,A05,A07,A16,A30,B14 ledger delta, and phase results/handoff.**


## Архив результатов этапов 1–3

Для возврата к ранее изученной фазе помимо плана читать **[Phase1 results](docs/phases/phase-01/results/README.md)**, **[Phase2 results](docs/phases/phase-02/results/README.md)** и **[Phase3 results](docs/phases/phase-03/results/README.md)**. Данные отдельно от исследовательских планов: выводы по владельцам механизмов, accepted/failed alternatives, original target NeoForge ABI/CI, mod paths и всё незакрытое. После каждого 4/4 пакета обновлять задействованную итоговую карточку, original source/TSV и SESSION_HANDOFF. Phase1 PARTIAL, Phase2 2.4/2.5 OPEN, Phase3 R OPEN, Phase2 visual V OPEN.

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
