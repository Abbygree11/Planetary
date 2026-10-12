# Planetary 2.3C — Final scoped source / selected exact NeoForge ABI gate C1–C19 versus original P01–P40

**2026-10-13 · branch `2.0` · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.** Closure of **bounded 2.3C source-owner + selected exact classpath ABI research inventory ONLY**, not complete runtime lifecycle, not all 241/87 declaring-method AST/bytecode paths, not applied Mixin or universal mod compatibility.

This document supersedes the *historical* [C1–C15 gap snapshot](PHASE2_STAGE3C_1_TO_15_SCOPE_RECONCILIATION_AND_NEXT_OWNER_GATES_1_21_1.md). It must be read together with the live [40-row P01–P40 evidence ledger](PHASE2_STAGE3C_P01_P40_ABI_COVERAGE_DISPOSITION_1_21_1.tsv): the ledger is authoritative for current per-family dispositions.

## 1/4 — actual repository corpus and independent CI provenance

The current `2.0` tree has **19 reports** `docs/research/PHASE2_STAGE3C{1..19}_*.md` and **19 actual JUnit/ASM tests** `src/test/java/dev/planetary/world/Phase23C{1..19}*AsmContractTest.java`. Each is a bounded mechanism family covering source callpaths, alternative authors, extension/mod boundaries, and selected **Gradle-resolved** NeoForge 21.1.215 classfiles. They are **not** an exhaustive independent registry-driven enumeration of all 241 original BLOCK, 87 original ITEM declaring method owners, nor every mod override, and source branches like OptiFine's mapped 1.21.1 comparison may differ from actual NeoForge target code.

| Corpus and accepted evidence | Status and CI |
| --- | --- |
| C1–C15 vanilla/NeoForge lifecycle & graphics source/selected exact target mechanism packets | Fifteen test+source families green; see [C1–C15 provenance table](PHASE2_STAGE3C_1_TO_15_SCOPE_RECONCILIATION_AND_NEXT_OWNER_GATES_1_21_1.md) and last [C15 CI38301516694](https://github.com/Abbygree11/Planetary/actions/runs/38301516694) |
| [C16](PHASE2_STAGE3C16_FRONT_AND_TOP_ROTATION16_STATE_TRANSFORMS_NEOFORGE_ASM_1_21_1.md) P09/P10/P35 orientation property domain and state transform | [CI38303484382](https://github.com/Abbygree11/Planetary/actions/runs/38303484382) **COMPLETED SUCCESS** (source test revision 640b4ec) |
| [C17](PHASE2_STAGE3C17_RAILSTATE_PHYSICAL_GRAPH_NEOFORGE_MOD_ASM_1_21_1.md) P28 RailState graph / IBaseRailBlockExtension / independent Create tracks | [CI38306600972](https://github.com/Abbygree11/Planetary/actions/runs/38306600972) **COMPLETED SUCCESS**, corrected new-test-only wrong initial method call, not game fix |
| [C18](PHASE2_STAGE3C18_MULTIFACE_VINE_SPREAD_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md) P26 Multiface six faces, Vine five, spread/bonemeal/sculk/worldgen and independent mod pipe | [CI38307680178](https://github.com/Abbygree11/Planetary/actions/runs/38307680178) **COMPLETED SUCCESS** |
| [C19](PHASE2_STAGE3C19_GROWINGPLANT_HEAD_BODY_DIRECTED_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md) P29 GrowingPlant up/down head/body/NeoForge CropGrow Pre/Post, three feature authors, real BOP1.21.1 source subclasses | [CI38311748785](https://github.com/Abbygree11/Planetary/actions/runs/38311748785) **COMPLETED SUCCESS**: Java compile, full JUnit including six new exact-ABI selected tests, original registry census; [new test](../../src/test/java/dev/planetary/world/Phase23C19GrowingPlantDirectionalChainAsmContractTest.java) commit caad811 |

**Precisely what is proven:** all 19 selected test classes are present and compile/run green in the latest full Gradle CI; tested classfile methods, fields, method owners/invocations and selected source semantics are real for the Gradle-loaded 21.1.215 target. The **original registry audit** established 241/241 concrete BLOCK classes, 1060/1060 BLOCK IDs at *source/registered owner* level and 87/87 ITEM classes, 1333 registered ITEM IDs/925 BlockItems. These counts are **independent levels of assurance**, not a proof of all 241+87 exact NeoForge patched bytecodes, Mixin injection success with other mods or correct Planet-specific gameplay.

## 2/4 — current original 40 mechanism dispositions, exact residual risk

Independent row-by-row recheck of the [live P01–P40 TSV](PHASE2_STAGE3C_P01_P40_ABI_COVERAGE_DISPOSITION_1_21_1.tsv) after C19:

| Disposition | Number | Concrete P IDs | Interpretation |
| --- | ---: | --- | --- |
| `SELECTED_SPECIFIC_ABI` | **9** | P09, P10, P17, P19, P20, P26, P28, P29, P35 | Dedicated individual target family bytecode assertions, not complete game |
| `SELECTED_SHARED_ABI` | **11** | P01, P02, P03, P04, P12, P22, P24, P36, P37, P38, P39 | Selected common owner hooks tested, some override subclasses/alternate authors untested |
| `SHARED_ABI_ONLY` | **16** | P06, P07, P08, P11, P13, P14, P15, P16, P18, P21, P23, P25, P27, P30, P31, P33 | Shared/dispatcher callpath only; **concrete polymorphic declaring-owner algorithms do not have exact family-specific proofs yet** |
| `PARTIAL_SHARED_ABI` | **4** | P05, P32, P34, P40 | Explicit known partial implementations/alternate producer/fake-world paths |
| `SPECIFIC_TARGET_GAP` | **0** | *None of the six originally named concrete algorithm families remain unexamined* | **Scope gate for bounded mechanism source survey only; not universal 40/40 exact per-owner method coverage** |
| **TOTAL** | **40** | P01–P40 exactly once | Many remaining runtime, mod compatibility and focused integration method gates |

**Important decision:** C16–19 closed the **six specific named algorithm families** found by C1–15 reconciliation (P09 FrontAndTop, P10 ROTATION_16, P35 rotate/mirror, P28 rail graph, P26 multiface/Vine graph, P29 head/body growth) with selected ABI green CI. But remaining **16 SHARED_ABI_ONLY and 4 PARTIAL_SHARED_ABI** must **NOT** be silently promoted to “fully verified” simply because a related shared dispatch method was present. Per-class exact target ABI may be needed **just-in-time for each implementation ticket**, not an unbounded C20 generic inventory. Every family receiving production adaptation MUST prove actual targeted 21.1.215 method owner/injection and retain non-Planet behavior.

## 3/4 — real engineering handoff / deepest safe boundaries / ownership

Next action is **2.4A-1 source/target typed placement semantic API** defined in [2.4A card](../phases/phase-02/05-implementation-a.md), *not an arbitrary 2.3C-20*. Its output must be Java production unit (core typed `PlanetPlacementIntent`-like semantics) and explicit Java tests, preserve physical world `Level/BlockPos/Direction`, source author provenance, local gravity frame only for Planet-owned Level/target block, no global callsite rewrites, no fabricated compatibility claims. Existing C1–C3/C16 exact ABI research is already sufficient to begin semantic API implementation. Do not prematurely mark even 2.4A item-gameplay PASS after a pure unit-level API.

Subsequent independently owned implementation domains:

| Real behavior gap | Owner and current source evidence | Implementation/test gate |
| --- | --- | --- |
| Placement authoring / real world-vs-player local frame / mod source + target | **2.4A**, C1–C3, C16, P01–P16 | Typed source/target/chart API + opt-in exact owner adapters; BlockItem, mod overrides, dispenser/structure foreign world |
| Neighbor support / scheduled world ticks / multi-cell event preservation | **2.4B**, C4/C5, P22–P24/P31/P32 | Do not bypass NeoForge onNeighborNotify/events with HEAD cancellation; preserve water/random/scheduled cadence and physical write flags |
| Directed and connected topology / head-body / rails / attachments / multiblocks | **2.4C**, C3/C9/C17/C18/C19, P17–P33 | Local `PlanetBlockStep` each port, transition edges and corners, legal source subclass property vocabularies, mod virtual hooks, graph reciprocal cleanup |
| Direct use, tool, structure/transformation | **2.4D**, C2/C7/C16, P05/P34/P35/P39 | Preserve `IBlockStateExtension.rotate(LevelAccessor,pos,rotation)`, mirror/rotate order, explicit source semantics and double transform avoidance |
| Actual fluid flow, Kelp LiquidBlockContainer | **Phase5**, C10/C19, P37 | Full liquids local geometry + simulation; do not double scheduled water tick or invent water |
| Shape/occlusion/lighting/BER, reload/foreign guidebook render world | **Phase3**, C11–C15, P40 | Explicit real source Level + reload epoch/dynamic per-position culling and loaded mod GPU/shaders; fix missing source provenance, no current Minecraft.level fallback |
| Powered/detector signal and minecart rail traversal | **Phase7/7A**, C9/C17, P28/P36 | Graph endpoint ≠ vehicle movement; preserve `IBaseRailBlockExtension`, `IAbstractMinecartExtension`, redstone events |
| Plant growth/features/structure/worldgen/biome compatibility | **Phase8/9**, C7/C18/C19, P29/P39 | NeoForge crop Pre/Post once where vanilla base genuinely owns tick, exact target pos/source state, BOP own virtual overrides, direct worldgen authors |
| Third-party mod custom mechanisms, Create and BOP | **Phase10**, C13–C19, P40 | Tested with **loaded compatible mod JARs**, independent algorithms vs shared inheritance; foreign guidebook and virtual worlds |
| Real all-faces acceptance | All implementation phases | Positive +Y, 5 other faces, 12 seams, 8 corners, save/relog, client/server, loaded mods, vanilla no-Planet baseline; **NOT RUN by StageC source survey** |

**Do not hide phase-specific API risk** behind the good source/selected test count. Concrete already visible blockers include Planet render model cache epoch; `ThreadLocal` culling keyed only by BlockState/side and not position-dependent mod shape; `PlanetClientRenderFrame` fallback to active `Minecraft.level` for non-Level/foreign render regions; virtual BE ticker/persistence and NeoForge neighbor event cancellation; worldgen and fluids; approp. runtime integration work still TODO.

## 4/4 — final scoped verdict / explicit gates

- **DONE — original registered VANILLA source inventory:** 241 BLOCK classes/1060 IDs, 87 ITEM classes/1333 IDs/925 BlockItems. Not a promise for unknown mod world.
- **DONE — 2.3C bounded 19 mechanism source packages and selected target ABI tests** with green full CI **19/19**; all 40 atlas rows accounted, **0 newly identified specifically missing algorithm family** in defined C1–19 source survey. This is a **RESEARCH-SCOPE gate completed**.
- **NOT DONE — per-declaring-class exact target 21.1.215 method audit for 40 families:** 11 common selected, 16 shared-only, 4 partial remain as recorded; focused proof should accompany each implementing owner.
- **NOT DONE — applied Mixin weaving, transformed runtime target, actual physical/semantic behavior in Minecraft or arbitrary mods; 6 faces/12 edges/8 corners/loaded mods GPU/biomes/shaders/real entity movement.** No claim of mod compatibility acceptance.
- **NEXT packet on “кк”: `2.4A-1` implement stable typed physical source/target `PlanetPlacementIntent` semantic API + Java unit/compatibility tests and green GitHub Actions**. Source survey is not reopened without a **named, material unreviewed engine owner**, not another generic rendering/mod brainstorm. 2.4A–E, Phase3/5/7/8/9/10 acceptance remain OPEN.

**Actions in this final gate:** document current disposition, report ABI-only scope closure, route actual engineering into ownership. The gate itself is **documentation only** and neither changes production Java nor claims a new CI run.
