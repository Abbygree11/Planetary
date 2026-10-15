# Planetary — CURRENT SESSION HANDOFF (read before historical AI_CONTEXT)

**As of 2026-10-15 (verify live GitHub):** repo `Abbygree11/Planetary`, branch **`2.0` only**, Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21. The branch SHA and actions can change; always check live `2.0` before claiming status. **Never edit `main`.**

## Active working point
- Phase: **2.4A — placement/orientation**. `2.4A-1…8` are prior selected implementation families; `2.4A-9` Crafter/Jigsaw FrontAndTop placement is CODE COMMITTED, **CI and gameplay separately checked**, not an entire survival automation/structure fix. The broader owner-property phase is not complete.
- Most recent code checkpoint *before the present maintenance task*: [`f1f05cf`](https://github.com/Abbygree11/Planetary/commit/f1f05cfae7ee3dd662e56379a73c71397a5e242b) — V6 catch-up lab combining eight `2.4A-6` gate + eight `2.4A-7` furnace + eight `2.4A-8` AXIS stations **on each of six faces**; [CI #38644260176](https://github.com/Abbygree11/Planetary/actions/runs/38644260176) **SUCCESS**.
- Owner-family code checks: `2.4A-6` [CI #38478594846](https://github.com/Abbygree11/Planetary/actions/runs/38478594846) SUCCESS; `2.4A-7` [CI #38622053271](https://github.com/Abbygree11/Planetary/actions/runs/38622053271) SUCCESS; `2.4A-8` [CI #38643250260](https://github.com/Abbygree11/Planetary/actions/runs/38643250260) SUCCESS.
- The user replied **«все окей»** following V6 report; record this as a positive smoke report, **not** proof of every six-face/seam/loaded-mod scenario.
- **2.4A-9 dev-visibility prelude, CODE COMMITTED:** runClient-only vanilla NIGHT_VISION for all six Planet faces; per-player `/planetary test light on|off`; restores actual darkness on OFF or exiting Planet, uses identity check to avoid overwriting/removing other effects. Dev-visibility code [`b995188`](https://github.com/Abbygree11/Planetary/commit/b9951886bfc2f998440e370ae330f16bfb5569b8) validated by [CI #38787987493](https://github.com/Abbygree11/Planetary/actions/runs/38787987493) **SUCCESS**. This is not actual sky/block light correction. **New 2.4A-9 placement owners:** [`69a04f2`](https://github.com/Abbygree11/Planetary/commit/69a04f27ed97c89da5ef2cd604c4b0c06317b2b2) — CrafterBlock and JigsawBlock original state RETURN adapters, shared 12-value `FrontAndTop` domain, JUnit + exact selected ASM. [CI #38789046409](https://github.com/Abbygree11/Planetary/actions/runs/38789046409) **IN PROGRESS at handoff write, VERIFY LIVE**. Real Minecraft, loaded mods and all seam/corner acceptance are **PENDING**. [Full research](research/PHASE2_STAGE4A9_FRONTANDTOP_CRAFTER_JIGSAW_1_21_1.md).
- True normal sky/block light, AO, seams, quad shading, and mod render pipelines belong to **Phase 3B** (original [C12 lighting evidence](research/PHASE2_STAGE3C12_LIGHT_AO_BIOME_TINT_NEOFORGE_MODEL_ASM_1_21_1.md)). Generic missing seam side-quads: Phase 3B / 3.2S, **not** a grass-specific or placement fix.
- **NEXT gate:** check CI of `2.4A-9`; fix true compile/ASM failures if any; independent applied-client FrontAndTop verification once green. V6 laboratory remains a temporary exception until old user-delayed gameplay checks are accepted, then V7 may replace it with Crafter/Jigsaw stations. Next new placement mechanism after acceptance: complete `ROTATION_16` author family, not selected single materials. Crafter output redstone/inventory direction is an outstanding Phase 7A physical consumer; Jigsaw structure graph is Phase 9.

**New AI startup convention:** root [START_HERE](../START_HERE.md) + [CHAT_REPORT_TEMPLATE](CHAT_REPORT_TEMPLATE.md) + this file; historical `AI_CONTEXT.md` requires selective reads only. Verify exact HEAD/CI live each turn.

## The project in ten facts
1. Cube planetary world: one **physical** XYZ block, six gravity frames, seams transport semantics; avoid duplicating blocks or rotating the entire world.
2. Shared safe boundary: stable Planet-owned algorithms plus thin versioned Mixins/NeoForge integration; never globally rewrite `BlockPos.relative` / `Direction` / `Level.getBlockState`.
3. Raw physical neighbor, render normal, lighting, redstone ports, placement intent, synthetic/fake level, worldgen and canonical blockstate are **different** contracts.
4. Core research: 2.3A **241/241** vanilla registered block classes (1060 IDs), 2.3B **87/87** item classes (1333 IDs), 2.3C **19 research packets** with only selected ASM, not full game/mod acceptance.
5. `2.4A-6` entire inherited FenceGateBlock algorithm includes placement, wall-neighbor `IN_WALL`, opening actor direction. Mod same-owner inherited gates not tested with loaded mod; independent overrides not covered.
6. `2.4A-7` inherited AbstractFurnaceBlock placement preserves concrete block state, GUI/BE/recipes. Those systems have separate behavior tests.
7. `2.4A-8` RotatedPillar/Chain/Infested AXIS algorithm covers 55 original IDs via shared + independent owner. Modded logs with same owner may inherit; worldgen bypasses.
8. Active dev lab **V6** marker OBSIDIAN, one 41×33 per face with 24 stations; CYAN is preinstalled reference, LIME is real player-placement lane. Prior V1–V5 markers auto-migrate. Never infer gameplay correctness from a preset BlockState.
9. User wants mod compatibility by **deepest safe common owner**, not individual patches; all relevant items/materials including modded paths. 6 faces/12 edges/8 corners, no-Planet and dedicated server remain independent gates.
10. `AGENTS.md` stage-labelled response and bounded microtasks, CI-before-user-test, existing `docs/CHAT_REPORT_TEMPLATE.md` are process hard gates. `2.4A-9` placement is NOT an end-to-end Crafter output/Jigsaw worldgen fix.

## Read only as needed
- [Quick bootstrap](../START_HERE.md), [response template](CHAT_REPORT_TEMPLATE.md), [AGENTS](../AGENTS.md).
- [Phase 2.4A card](phases/phase-02/05-implementation-a.md), [roadmap](IMPLEMENTATION_PLAN.md), [V6 lab](acceptance/PLANET_TEST_LAB.md).
- [Architectural portability](research/PORTABILITY_STRATEGY.md), [mechanism map](research/GRAVITY_MECHANISM_MAP_1_21_1.md), [historical continuity archive](AI_CONTEXT.md).
- [Phase 3 rendering owner](phases/phase-03.md).

## Handoff rule
At the end of each new packet, update THIS file's **Active working point** with the actual new commit, exact CI outcome, honest gameplay/mod status, outstanding acceptance and NEXT named stage. Never append a large historical transcript here. Archive detailed analysis in appropriate stage research / AI_CONTEXT. If CI is still running, say IN PROGRESS instead of PASS.
