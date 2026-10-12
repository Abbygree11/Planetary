# Planetary Stage 2.3C scope gate — independently reconcile C1–C15 against P01–P40, exact target ABI and implementation

**2026-10-12; branch `2.0`; Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.**

**Decision: close the bounded research packages `2.3C-1` through `2.3C-15`, NOT all of Stage `2.3C` or Phase 2.** The source/selected Gradle target classfile contract portion of those 15 packets is checked. There are **six specifically identified missing mechanism-level target ABI families** (below); they are real follow-on owner work, not justification for a random renderer survey. Original 241/241 BLOCK class and 87/87 ITEM class inventories are **source-level** only, not exhaustive exact NeoForge 21.1.215 method/callsite ABI, transformed Mixins, client/server or six-face acceptance. No production Java/Mixin code was changed in this scope reconciliation. A green selected ASM/JUnit suite does NOT establish a full unknown-owner fail gate or correctness of runtime callbacks.

This is an actionable **scope reconciliation (four internal checks in one packet)**:
1. compare committed C1–C15 reports against the *original* P01–P40 source/registered-owner atlas and previous 3A/3B findings;
2. map each core physical/canonical/mod boundary to an actually selected compiled ABI test, or explicitly distinguish general dispatcher test from a missing concrete family;
3. assign genuinely missing version-specific ABI checks plus implementation owner, deepest safe shared boundary and bypasses to a coherent next stage;
4. preserve confirmed GitHub CI evidence separately from actual applied Mixin, gameplay, shaderpack/mod-client acceptance; update navigation, roadmap and next packet.

## Evidence inventory and limits

The actual repository tree at prior HEAD `69561c52bc9ba70dbb7647fcb9cb287fa0d6c2de` contains **15 distinct `PHASE2_STAGE3C{1..15}` reports and 15 matching `Phase23C{1..15}*AsmContractTest.java` tests**. Every one has a linked **completed successful** GitHub Actions run for its latest corrected test revision as recorded in [active 2.3C card](../phases/phase-02/04-lifecycle-neoforge.md). One final selected-target suite [C15 Actions 38301516694](https://github.com/Abbygree11/Planetary/actions/runs/38301516694) passed; see per-family runs below. This verifies *the tests written*, not an exhaustive independent enumeration of every original class method, all 241 class dispatch paths, 87 item overrides, bytecode after live Mixin weaving or mod JAR paths.

| Stage | Reviewed mechanism owner family (4/4 source + selected exact target ASM) | Last passing Actions run |
| --- | --- | --- |
| C1 | BlockItem / StandingAndWallBlockItem physical/semantic placement | [38258262302](https://github.com/Abbygree11/Planetary/actions/runs/38258262302) |
| C2 | Placement contexts, player-less/synthetic/special items | [38260347819](https://github.com/Abbygree11/Planetary/actions/runs/38260347819) |
| C3 | Two-block authoring, BedItem/Door/DoublePlant lifecycle | [38263911324](https://github.com/Abbygree11/Planetary/actions/runs/38263911324) |
| C4 | NeighborUpdater, shape/signal callbacks and NeoForge events | [38269226491](https://github.com/Abbygree11/Planetary/actions/runs/38269226491) |
| C5 | Block/fluid scheduled/random ticking and temporal mutations | [38277245530](https://github.com/Abbygree11/Planetary/actions/runs/38277245530) |
| C6 | BlockEntity ticker, save/load, capability hooks | [38279436034](https://github.com/Abbygree11/Planetary/actions/runs/38279436034) |
| C7 | WorldGenRegion / Feature / StructureTemplate / commands | [38280585886](https://github.com/Abbygree11/Planetary/actions/runs/38280585886) |
| C8 | Piston / moving-placeholder / moving BlockEntity | [38282239773](https://github.com/Abbygree11/Planetary/actions/runs/38282239773) |
| C9 | Redstone/signals/NeoForge weak power and modded ports | [38284973530](https://github.com/Abbygree11/Planetary/actions/runs/38284973530) |
| C10 | Fluid graph/waterlogging/mod FluidType interaction | [38288455692](https://github.com/Abbygree11/Planetary/actions/runs/38288455692) |
| C11 | Collision/support/occlusion/culling and dynamic model shapes | [38289949215](https://github.com/Abbygree11/Planetary/actions/runs/38289949215) |
| C12 | Light/AO/QuadLighter/biome tint | [38297478474](https://github.com/Abbygree11/Planetary/actions/runs/38297478474) |
| C13 | BER/BEWLR/mod fluid renderer/Flywheel/Iris comparisons | [38298758219](https://github.com/Abbygree11/Planetary/actions/runs/38298758219) |
| C14 | Camera/entity/item/frame/hand/moving/world-shader passes | [38300577734](https://github.com/Abbygree11/Planetary/actions/runs/38300577734) |
| C15 | Resource/model reload/extra section geometry/ModelData/weather/shader epochs | [38301516694](https://github.com/Abbygree11/Planetary/actions/runs/38301516694) |

**Independent registered-source proof:** `2.3A` reports 241/241 concrete vanilla BLOCK Java classes, 1060/1060 registered BLOCK IDs; `2.3B-4` reports 87/87 concrete ITEM Java classes, 1333/1333 ITEM IDs and 925 BlockItem relations against original immutable registry ZIP. These are **original source/effective-owner review counts**. The StageC bytecode tests check a **selected set** of exact Gradle-resolved methods, annotations, and invocations, not a revalidated all-class patched owner ledger. Claims like “15/15 packages committed/selected tests green” are valid; claims like “40/40 mechanics exact ABI proven” or “Planetary compatible with all mods” are **invalid**.

## P01–P40 disposition — evidence-preserving complete mechanism map

[40-row **machine-readable reconciliation TSV**](PHASE2_STAGE3C_P01_P40_ABI_COVERAGE_DISPOSITION_1_21_1.tsv). Its dispositions are intentional and **not gameplay statuses**:

- `SELECTED_SPECIFIC_ABI`: 3 mechanisms with selected family-specific source and target-class tests; still no applied gameplay acceptance.
- `SELECTED_SHARED_ABI`: 11 mechanisms with selected common/shared owner bytecode coverage but not all subclasses and mod overrides.
- `SHARED_ABI_ONLY`: 16 mechanisms where a relevant engine/dispatcher contract passed, **not** a dedicated class-specific algorithm contract.
- `PARTIAL_SHARED_ABI`: 4 where only some lifecycle/authoring paths are targeted.
- `SPECIFIC_TARGET_GAP`: **6** missing target-level algorithm-owner families. These may have extensive Stage3A source review, but C1–C15 do NOT establish their individual NeoForge21.1.215 exact ABI.

The six demonstrable gaps (not a claim that no other owner may ever emerge):

| Priority | Mechanism | What IS known | What C1–C15 do NOT establish | Destination |
| --- | --- | --- | --- | --- |
| 1 | **P09** `FrontAndTop`, `CrafterBlock`, `JigsawBlock` | Original Stage3A orientation domain source and C7 structure/template route | Actual per-owner getStateForPlacement, rotate/mirror and `FrontAndTop` state evolution in compiled 21.1.215; mod serializers | **C16** exact property-domain owner family, then 2.4A |
| 1 | **P10** ROTATION_16 standing/hanging sign/banner/skull and angle quantization | Stage3A sign/skull/banner source, C1 standing/wall items, C13 BER | Actual exact standing 16-angle state property authors and rotate/mirror rendering semantics beyond generic item | **C16** exact property-domain owner family, then 2.4A/Phase3 |
| 1 | **P35** rotation/mirror/copy of canonical `BlockState` vs physical StructureTemplate transform | C7 template-processing callback/processor selected ABI | Polymorphic concrete block rotate/mirror, multi-domain property transport, mod overrides, double transform avoidance | **C16** canonical BlockState transform family, Phase9 contract |
| 2 | **P28** `BaseRailBlock` / `RailState` / slope/tangent physical graph | Stage3A-5 full rail source and 4 original registered IDs; C9 selected signal + a RailShape-related hook | Dedicated `RailState` placement/connectivity/ascending local graph NeoForge ABI | Separate coherent rail graph package after C16; 2.4C and Phase7 |
| 2 | **P26** `MultifaceBlock` / `VineBlock` attachment and growth graph | Stage3A original registered graph source; C4/C11 shared neighbor and shapes | Actual polymorphic six-face stored-property graph, separate vine growth and modded variants | Coherent attachment graph package; 2.4C |
| 2 | **P29** `GrowingPlantBlock` head/body chains and directional traversal | Stage3A growth owners; C5 generic random/scheduled ticks | Compiled head/body growth direction, survival and target-frame transported local step | Coherent directional growth package; 2.4C/Phase8 |

These are actual **mechanisms**, not 6 arbitrary block-name fixups. One C16 packet will combine **P09/P10/P35** because the shared concern is **canonical state orientation domain and rotate/mirror**; C16 should still research independent concrete declaring owners and real mod overrides, not assume one common Block implementation. P26/P28/P29 have independent graph algorithms, so do not combine with C16 solely to save stage numbers. After these packages, rerun the scope gate, and only then decide if `2.3C` source/selected ABI research can be closed. Do **not** default all remaining P IDs to ABI PASS based on generic `BlockStateBase` dispatch.

## High-priority runtime blockers already revealed, not further source-browsing tasks

| Concrete implementation gap | Source evidence | Owner / safety rule | Acceptance |
| --- | --- | --- | --- |
| Lack of typed physical/source/canonical placement intent across alternate authors | C1, C2, C3; Stage3B-4 87 item owners | 2.4A `PlanetPlacementIntent`-like helper, preserve vanilla/NeoForge BlockItem hooks and real target pos | Real player + dispenser + no-player/foreign world; six faces and seams |
| Potential loss of NeoForge neighbor events on Planet HEAD cancellation | C4 | 2.4B and 7A, keep physical event dispatch/flags, opt-in semantic support | Event count+order and canSurvive 6-face tests incl mod listener |
| Two schedulers / lacking alternative random tick driver coverage | C5 | Phase2 tick bridge and Phase7A/8, no doubled unrelated ticks | Original source tick identity/rate/time and save/relog |
| Planet virtual BE store missing ordinary LevelChunk ticker/persistence parity | C6 | 2.4C + Phase7A, preserve actual LevelChunk/NeoForge capability onLoad | BE ticker/save/load/capability real game |
| Empty decoration/carvers/surface implementation | C7 | Phase8/9 worldgen; no assumption block-state source research implements biome/structure world | Vanilla-like decorations/biome/mod structures |
| Local fluid simulation vs NeoForge reaction and physical fluid identity | C10 | Phase5; typed local horizontal+down graph, maintain physical collision/Level/FluidState | Flow/lava/water interactions and modified mod fluids |
| Model/quad cache epoch + ThreadLocal position-dependent culling + fake Level fallback | C11, C15 | Phase3/10: explicit source Level+section+reload epoch, dynamic shape bypass; avoid universal `VertexConsumer` | Reload/other Level/foreign guidebook/ModelData on side faces |
| Shader GBuffer, shadow pass, Flywheel instances and arbitrary BER are independent | C12–C15 | Phase3 and Phase10: explicit owner/render pass, no global shader world-Y mutation | Real loaded Iris, Create/Flywheel and shaders; screenshots/no-Planet regressions |

**Key difference:** unresolved *implementation* does not imply another *research* packet is useful. The specific target-ABI families above warrant an additional research packet; the already-identified runtime blockers should proceed to assigned 2.4/Phase3/5/7/8/9/10 engineering waves instead of reopening C4/C5/C6/C7/C10/C15.

## Gate / next handoff

- The 15 prior StageC packets: **source/selected exact ABI RESEARCH COMPLETE 15/15; CI for each selected suite PASS**.
- The StageC **P01–P40 specific target-ABI coverage is NOT complete**, and the six `SPECIFIC_TARGET_GAP` rows plus generic-only rows remain explicitly open. Do **not** mark the four phase-wide lifecycle checkboxes as completed for every method.
- Applied Planet Mixin transformation, physical/modded client/server lifecycle, six faces/12 seams/8 corners, real survival gameplay, shaders and GPU: **NOT RUN** as a StageC exhaustive acceptance.
- The next `кк` should be **`2.3C-16` property-orientation domain and state-transform family**: check actual patched target declaring owners `FrontAndTop`, `ROTATION_16`, `BlockState.rotate/mirror`, all source/synthetic/structure/fake-world state authors, NeoForge/mod override dispatch, original 3A registry class/ID joins, strict ASM/JUnit against 21.1.215 (full 4/4), then link CI. No production changes unless a separate implementation ticket requires them.
- Once that research is finished, choose a genuinely uncovered graph family (rails/attachment/growth) from this matrix, rather than launching another generic renderer survey. Implementation entrypoint `2.4A-1` is already separately defined and should be scheduled without confusing any source audit with live game readiness.

**Independent documentation-only scope audit**: no new Java/Gradle files; the prior successful test run stands, but is not an Actions run for this new narrative.
