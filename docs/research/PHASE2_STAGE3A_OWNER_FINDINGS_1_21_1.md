# Stage 3A — compiled BLOCK lifecycle owners: initial evidence-backed review

Status: IN PROGRESS. This is the first bounded disposition batch;
NOT 241/241, NOT a complete P01–P40 class-level review.

Exact runtime evidence: `PHASE2_EFFECTIVE_OWNER_FINDINGS_1_21_1.md`,
actual NeoForge 21.1.215 JUnit registry census produced by
GitHub Actions run 37988064055 (compile+JUnit PASS).
Comparative Minecraft 1.21.1 source-level evidence:
`PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md`,
`PHASE2_SOURCE_OWNER_AUDIT_WAVE_B_1_21_1.md`,
`PHASE2_SOURCE_OWNER_AUDIT_WAVE_C_1_21_1.md`.

## What can already be asserted (and what cannot)

Method-owner *diversity* across 1060 registered Block IDs:
- 103 effective `getStateForPlacement` declaring owners;
- 60 effective `canSurvive` declaring owners;
- 103 effective `updateShape` declaring owners;
- 15 `setPlacedBy`, 25 `neighborChanged`, 61 `rotate`,
  57 `mirror`, 24 `useItemOn`, 52 `useWithoutItem`.
These are **separate method dimensions** with overlapping
registered IDs; sums are meaningless. Orientation problems are
not restricted to 528 property-name candidates.

The method-owner scan is reflection-level ownership, not
verification that a mixin matches all compiled INVOKE sites.
A superclass authoring Mixin does not affect subclass overrides.
BlockStateBase/cached survival and block-entity/StructureTemplate
state mutation are separate dispatch/creation boundaries.

## Disposition of researched algorithm-owner clusters

`RESEARCHED_GAP` means source semantics and lack of accepted
family-wide Planetary coverage were established; it does NOT
mean every associated block ID has been checked in the game.

| Algorithm owner / owning family | P mechanism(s) | Evidence for independent path | Implementation/acceptance disposition |
|---|---|---|---|
| `RotatedPillarBlock.getStateForPlacement` | P06 | 53 IDs reflect effective owner | PARTIAL_ADAPTER; check Chain/Rod override and alternate authors |
| `StairBlock` | P08/P14/P25 | 56 IDs have own placement/update; FACING+HALF+SHAPE | RESEARCHED_GAP; full corner shape graph/half classification pending |
| `SlabBlock` | P15 | 60 IDs, replacement stacking and top/bottom state | PARTIAL_ADAPTER; replacement and seam acceptance pending |
| `DoorBlock` | P17/P22 | 20 IDs, independent upper-half `setPlacedBy` and physical update | PARTIAL_ADAPTER; pair survival, hinge/neighbor interactions unaccepted |
| `TrapDoorBlock` | P08/P16/P34 | 20 IDs, clicked-face+hit-Y branch and POWERED/OPEN | RESEARCHED_GAP; not inherited from HorizontalDirectional generic behavior |
| `BedBlock` | P19/P34 | 16 IDs with head+foot and part interaction | PARTIAL_ADAPTER; pairing and BER handoff pending |
| `BushBlock` | P22/P29 | 40 IDs via canSurvive; virtual mayPlaceOn soil predicate | PARTIAL_ADAPTER; only descendants inheriting canSurvive, growth separate |
| `SeaPickleBlock` | P22/P30 | own canSurvive, independent below/support/stacking | RESEARCHED_GAP; explicitly bypasses Bush canSurvive Mixin |
| `CocoaBlock` | P07/P23/P30 | own canSurvive reads state-local FACING neighbor | RESEARCHED_GAP; not Bush support; growth and collision separate |
| `LanternBlock` | P22/P23 | own HANGING placement/support/update water ticks | RESEARCHED_GAP; cannot be solved by torch/Candle Mixin |
| `AmethystClusterBlock` | P07/P23 | own directional FACING support and update | RESEARCHED_GAP; preserve six faces and water ticks |
| `GrowingPlantBlock/Head/Body` | P20/P29 | growthDirection and own head/body graph updates | RESEARCHED_GAP; no guaranteed FACING property at all |
| `WallBlock` | P25/P27 | 25 IDs, side height/TALL/LOW, independent update graph | RESEARCHED_GAP; FenceBlock adapter is not WallBlock coverage |
| `BaseRailBlock/RailState` | P28/P22 | four registered rail family IDs, RailShape topology | RESEARCHED_GAP; support, corner/ascending local UP |
| `ChestBlock` | P08/P21/P34 | independent LEFT/RIGHT pair, DoubleBlockCombiner | RESEARCHED_GAP; EnderChest single adapter irrelevant |
| `EnderChestBlock` | P08 | own horizontal placement, no double-chest combine | PARTIAL_ADAPTER; Phase-3 BER remains separate |
| `EndRodBlock` | P07 | own clicked face+existing-neighbor facing toggle | PARTIAL_ADAPTER; physical neighbor/canonical state interplay |
| `CakeBlock/CandleCakeBlock/CandleBlock` | P22 | distinct isSolid versus canSupportCenter predicates | PARTIAL_ADAPTER; full family override and natural placement acceptance pending |
| `SporeBlossomBlock` | P23 | local ceiling canSupportCenter + water restriction | PARTIAL_ADAPTER; local UP and update tested only in code, no game acceptance |
| `FenceGateBlock` | P08/P25/P34 | wall-neighbor IN_WALL, interaction+power state | RESEARCHED_GAP; not solved by fence connection code |
| `CrafterBlock/JigsawBlock` | P09/P39 | 12-value FrontAndTop orientation, two orthogonal axes | RESEARCHED_GAP; distinct algorithms and cross-phase consumers |
| `ChiseledBookShelfBlock` | P08/P34 | FACING and hit-dependent six slot selection | RESEARCHED_GAP; BE interaction/hit transform required |

These entries map ALGORITHM OWNER CLUSTERS, not all 241
concrete classes, and the source waves establish only the
specific asserted owner semantics; no individual registered
ID-level PASS should be inferred. CI source-owner verification
for all override sites remains outstanding.

## Remaining Stage 3A work

1. Read actual `phase2-neo1211-registry-census` artifact's
   class-hierarchy/effective-owner TSV and enumerate ALL 241
   concrete classes. Join to registered block IDs, source
   ownership, P01-P40 (multi-label allowed) and a documented
   NOT_APPLICABLE/CROSS_PHASE disposition when supported.
2. Include owners without an orientation-named property, and
   BlockBehaviour.BlockStateBase methods/caches.
3. For each newly found override, follow full placement,
   survival, neighbor update, block interaction and mirror/
   rotate contracts, including NeoForge-specific changes.
4. Keep every unreviewed owner visibly REVIEW_PENDING.
   Commit comprehensive per-class TSV only once actual
   registry evidence supplies its rows; do NOT generate
   fabricated class IDs or report 241/241 complete.
5. After Stage 3A, begin Stage 3B item/alternate author
   census before any additional runtime-only Mixin wave.

The existing gameplay verdict is unchanged: particle families
accepted by the user; Phase-2 placement+orientation still
not accepted on side/bottom faces. No extra local client
run is needed for this research-only commit.
