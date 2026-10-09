# 2.3A-3 — tangent connectivity and multi-face graph owners

**Status:** DONE — 4 bounded research packages; Stage 3A and Phase 2 remain INCOMPLETE, no gameplay PASS.
[Phase-2 queue](../phase-02.md) ·
[2.3A-2.4 registry reconciliation](../../research/PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md) ·
[Class-level TSV](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).

## Real registry candidate cohort (NOT semantic dispositions)

**Original candidate cohort:** 10 initially `REVIEW_PENDING` classes /
**71** BLOCK IDs. As of **2.3A-3.4**, 8 of these are
`SOURCE_REVIEWED_INTEGRATION_PENDING` (69 IDs), and
**only 2 TripWire classes** (2 IDs) remain `REVIEW_PENDING`.
Selection was verified against original
NeoForge 21.1.215 artifact 11643813158:

| Concrete class | Registered IDs (count) | Initial candidate mechanism |
|---|---:|---|
| `FenceBlock` | 12 | P25 connected graph |
| `FenceGateBlock` | 11 | P25 connected graph |
| `WallBlock` | 25 | P25 connected graph |
| `IronBarsBlock` | 2 | P25 connected graph |
| `StainedGlassPaneBlock` | 16 | P25 connected graph |
| `TripWireBlock` | 1 | P25 connected graph |
| `TripWireHookBlock` | 1 | P25 connected graph |
| `VineBlock` | 1 | P26 multi-face attachment |
| `GlowLichenBlock` | 1 | P26 multi-face attachment |
| `SculkVeinBlock` | 1 | P26 multi-face attachment |

These candidates are **NOT equivalent**: a fence/pane tangent
connection is not a vine six-face survival/expansion path, nor
a tripwire signal graph. P36 / Phase 7A redstone consumer
contracts must remain separate. Source method and exact
owner discovery can split this list across later calls;
never blend incompatible algorithms merely to hit a count.

## Small independently committable tasks

- [x] **1.** Inspect pinned Minecraft 1.21.1 source paths and real NeoForge 21.1.215 reflection owners for these up-to-10 classes (or a coherent 8–10-class subset); record placement, survival, update, connectivity queries and source-vs-target chart, including inherited/overridden paths. **DONE 2026-10-10:** [8-class P25/P26/P27 comparative source and original CI owner audit](../../research/PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md), 69 actual registered IDs; `FenceBlock`, `FenceGateBlock`, `WallBlock`, `IronBarsBlock`, `StainedGlassPaneBlock`, `VineBlock`, `GlowLichenBlock`, `SculkVeinBlock`. Two `TripWire` variants still pending. **55/241** source+reflection reviewed, **186** pending; NO ASM/Planet/gameplay PASS.
- [x] **2.** Independently join original ITEM registry and alternative state/interaction authors; distinguish `BlockState` neighboring connections from power propagation, `getShape`, waterlogging and propagation. **DONE 2026-10-10:** [exact 69/69 original CI item join and alternate-author audit](../../research/PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md). All 69 matching BlockItems, all seven item methods declare at `BlockItem`; direct post-placement `BLOCK_STATE` property component, gate interaction/redstone/explosion, fence lead interaction, vine random growth, lichen `MultifaceSpreader`, sculk `regrow`/discharge/charge and generic worldgen authors kept distinct. 55/241 class-level statuses unchanged; **ASM/gameplay PENDING**.
- [x] **3.** Reconcile no-property multi-face graphs, BlockStateBase cached state/shape and physical neighbor callbacks; isolate Phase-2 boundary versus Phase 5/7A. Do not implement or claim runtime acceptance. **DONE 2026-10-10:** [source/target graph chart, BlockStateBase and per-family shape/cache audit](../../research/PHASE2_STAGE3A_GRAPH_SHAPE_CALLBACK_CHART_1_21_1.md). Documented physical `updateNeighbourShapes` / `updateShape` event directions, canonical source flags, **target-local** cached sturdiness, distinct physically rotated collision vs canonically exposed support/occlusion shapes, and potential `MultifaceBlock.canAttachTo` and WallBlock above-face mixed-frame contracts. Existing Planetary frame/shape/render adapters source-reviewed. Not proven runtime bugs; ASM and gameplay remain pending. 55/241 class statuses unchanged.
- [x] **4.** Check per-class `REVIEW_PENDING`/source/reflection/ASM/gameplay statuses against the exact registry artifact. Commit one checkpoint and plan the next bounded family while classes remain unreviewed. **DONE 2026-10-10:** [original CI ZIP vs all-241/55-reviewed exact ledger reconciliation](../../research/PHASE2_STAGE3A_GRAPH_COHORT_RECONCILIATION_1_21_1.md) with four matching canonical fingerprints, **69/69 BlockItem** verified separately; no class or runtime acceptance status changed. [Next 11-class / 12-ID still-pending redstone owners card](02b-redstone-signal-owners.md).

## Resume checkpoint

- Last completed: **2.3A-3.4** exact original NeoForge ZIP
  vs full 241/1060 ledger and all 55 reviewed classes'
  exact owner/ID signatures. [Reconciliation](../../research/PHASE2_STAGE3A_GRAPH_COHORT_RECONCILIATION_1_21_1.md).
  Four separate fingerprints MATCH; **no acceptance promotion**.
- The 8 connected-graph classes / 69 IDs were source reviewed
  in 3A-3.1, ITEM/alternate-author joined in 3A-3.2,
  shape/callback/chart contract researched in 3A-3.3.
  `TripWireBlock` and `TripWireHookBlock` remain pending.
- Entire ledger: **55/241 source+reflection-reviewed**
  (162/1060 IDs), **186/241 REVIEW_PENDING** (898 IDs).
  All 241 ASM, Planet runtime and gameplay verdict fields PENDING.
- **NEXT FIRST unchecked task:** separate card
  [Stage 3A-4](02b-redstone-signal-owners.md), checkbox 1,
  actual 11-class redstone/tension/sensing/rail candidate
  cohort (12 registered IDs), no source audit yet.
- No production Java/NeoForge patches; no CI/gameplay PASS.
