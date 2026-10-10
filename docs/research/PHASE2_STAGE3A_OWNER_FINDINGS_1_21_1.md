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

## 2026-10-10 verified reconciliation of original CI artifact

**Checkpoint 2.3A-1 / micro-task 1: DONE, source-row mapping ONLY.**
Read the original `phase2-neo1211-registry-census` ZIP from
[run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact ID `11643813158`, generated by `aa395729`.
The ZIP contains `phase2-neo1211-block-registry.tsv` (1060 rows),
`phase2-neo1211-item-registry.tsv` (1333 rows), and
`phase2-neo1211-state-properties.tsv` (1712 rows), plus a summary.
The TSVs have unique registered IDs and unique ID/property pairs;
241 actual concrete block classes appear in `java_class`.
No reconstructed or imagined class rows were used.

The **22 owner-cluster descriptions above** were cross-checked with
registered `class_hierarchy` and exact-signature
`effective_method_owners`. Representative verified joins:

| Family / method | Registered IDs with effective owner | Observed descendant or concrete IDs | Caveat |
|---|---:|---:|---|
| RotatedPillarBlock / placement | 53 | 54 in its hierarchy; 52 exact class | Another descendant can override placement |
| StairBlock / placement+updateShape | 56 | 56 descendants; 52 exact class | Includes inherited subclass implementations |
| SlabBlock / placement+updateShape | 60 | 60 descendants; 56 exact class | Replacement/stacking still pending |
| DoorBlock / survival+placement+pair | 20 | 20 descendants; 16 exact class | Multi-cell paths not gameplay-accepted |
| TrapDoorBlock / placement+updateShape | 20 | 20 descendants; 16 exact class | Redstone and water paths separate |
| BushBlock / canSurvive | 40 | 59 descendant IDs across 29 concrete types | **19 IDs override** inherited canSurvive |
| SeaPickleBlock / canSurvive | 1 | 1 exact class | Explicit BushBlock override |
| LanternBlock / support+placement | 2 | 2 exact class | Hanging/water behavior separate |
| AmethystClusterBlock / support+placement | 4 | 4 exact class | Directional attachment |
| GrowingPlantBlock / canSurvive | 8 | 8 descendants, no exact concrete class | Head/body override update and growth |
| WallBlock / placement+updateShape | 25 | 25 exact class | State graph is family-specific |
| BaseRailBlock / support+placement | 4 | 4 descendants, no exact concrete class | RailState is helper, NOT a registry class |
| ChestBlock / placement+updateShape | 2 | 2 descendants, 1 exact class | EnderChestBlock is NOT this pair family |
| FenceGateBlock / placement+updateShape | 11 | 11 exact class | Distinct from fence connectivity |
| CrafterBlock and JigsawBlock / placement | 1 + 1 | 1 + 1 exact classes | Composite FrontAndTop semantics |

Other named entries (BedBlock, CocoaBlock, EnderChestBlock,
EndRodBlock, CakeBlock/CandleCakeBlock/CandleBlock,
SporeBlossomBlock and ChiseledBookShelfBlock) are likewise present
and retain the original **PARTIAL_ADAPTER / RESEARCHED_GAP** research
labels. These labels were NOT promoted to runtime PASS.

The `getStateForPlacement(BlockPlaceContext)` owner diversity is
**103**. Counting all same-name overloaded signatures yields
**104** different declaring classes; the latter is NOT the number
of owners for a single placement invocation. Details and ZIP hash:
[`PHASE2_EFFECTIVE_OWNER_FINDINGS_1_21_1.md`](PHASE2_EFFECTIVE_OWNER_FINDINGS_1_21_1.md).

**Remaining:** no new 8–15-class semantic disposition group was
completed in this micro-task. Do NOT create a 241-row TSV with
unsupported review labels. Next: 12 confirmed registered concrete
`BushBlock` descendant classes identified in
[`01-block-owners.md`](../phases/phase-02/01-block-owners.md);
inspect method bodies, subclass overrides, real call paths and
NeoForge patch/bytecode scope before assigning first reviewed rows.

## 2026-10-10 BushBlock 12-class subclass-method audit

Second bounded checkpoint is now complete: **12 real concrete Java
classes / 18 registered Block IDs**, representing 18 of 19 actual
`BushBlock` subclass IDs whose effective `canSurvive` owner is
NOT `BushBlock`. The 19th ID is `SeaPickleBlock`.
Source call paths and actual compiled registry owner signatures are
documented in [PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md).

Important new confirmed algorithm paths: `CropBlock.getGrowthSpeed`
XZ soil/crop neighborhood; `DoublePlantBlock` Y-axis pair update,
upper placement and `DoubleHighBlockItem` pre-write;
`PitcherCropBlock` age-triggered pair rather than ordinary
`setPlacedBy`; `SmallDripleafBlock` water/soil/FACING pairing;
`TallSeagrassBlock` full source-water dependency; standing/hanging
`MangrovePropaguleBlock`; `MushroomBlock` independent canSurvive
and 3D random spread. Shared Bush support works **only** for
the delegated branch and does NOT imply subclass acceptance.

**Status: RESEARCHED source owners; NeoForge bytecode/actual Mixin
runtime, 241-class dispositions, item lifecycle and gameplay still
open.** The next distinct micro-task records dispositions in TSV;
do not start implementation based on an incomplete mapping.

## 2026-10-10 first exhaustive pending-aware class ledger

Created [PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv)
and [evidence/status interpretation](PHASE2_BLOCK_OWNER_DISPOSITION_GUIDE_1_21_1.md),
using the **original registry ZIP**, not the 293-source-file census
or inferred subclass names. Every one of **241** distinct concrete
registered classes has a row; the associated count totals **1060**
real registry IDs. **12** source-reviewed Bush descendants map
**18 IDs**, and the remaining **229** classes / **1042** IDs
are explicitly REVIEW_PENDING.

The five declared-method-owner dimensions in 12 reviewed rows are
taken from the actual effective-method-owner scan with exact
parameter signatures, and pinned 1.21.1 comparative-source URLs
describe the independent class/subclass call-path risk.
All **NeoForge patch/ASM, adapter runtime, and gameplay
acceptance** columns remain REVIEW_PENDING: no false 241/241
semantic or 1060/1060 gameplay pass.

Next: card 2.3A-1, task 4, a different 8–15-class owner family.
No runtime modifications and no extra client checks.

## 2026-10-10 second 12-class Bush inherited-support review

Completed [PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md):
12 real concrete `BushBlock` descendants / 34 IDs with actual
`canSurvive` inherited from `BushBlock`. Separate source
call-paths confirmed for sapling/light+tree growth, azalea/fluid
above bonemeal, fungus substrate/feature creation, plant
substrates, tall-grass -> DoublePlant transformation, seagrass
full-water/tick/upper-growth, sweet berry interactions/entity
motion, and nether wart age. **Do not equate working BushBlock
support with all those other correct behaviors.**

The 241-class class roster is unchanged. The evidence TSV now
records **24 SOURCE_REVIEWED / 217 REVIEW_PENDING** classes,
covering **52/1060 block IDs**. The reviewed 24 remain
`INTEGRATION_PENDING`: no exact NeoForge ASM/handler, item
creation, gameplay or all-face PASS claimed. Stage 2.3A-1
microtask card is finished; NEXT is 2.3A-2 in a new answer.

## 2026-10-10 Stage 2.3A-2.1 physical face attachments

[New evidence-backed source audit](PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md)
covers **11 registered concrete Java classes / 29 ID counts**
in a bounded support/attachment batch: floor/wall torches and
redstone torches, ladder, lantern, spore blossom, amethyst
cluster, EndRod, lever and button. Actual NeoForge registry
class roster/count were verified; comparison source determines
nearest defining methods, independently of compiled NeoForge
reflection. Special boundaries: StandingAndWallBlockItem,
RedstoneWallTorchBlock delegating to WallTorchBlock without
inheritance, FaceAttachedHorizontalDirectionalBlock shared
nonconcrete ancestor, EndRod without custom survival,
and six-direction amethyst. Redstone signaling/tick remains
a Phase-7A consumer; fluid tick Phase 5.

Ledger cumulative statuses: **35 SOURCE_REVIEWED / 206
REVIEW_PENDING**, **81/1060 IDs by class count**, and no
ASM/Planet/gameplay PASS. New 11 rows explicitly say
`COMPARATIVE_SOURCE_OWNER_ONLY_NEOFORGE_REFLECTION_RECHECK_PENDING`.
Next: 2.3A-2.2 exact patch-bytecode and alternate authors,
separate checkpoint.

## 2026-10-10 Stage 2.3A-2.2 attachment exact runtime-owner confirmation

Re-opened original CI ZIP artifact 11643813158. All 11
attachment classes in the
[face-attachment audit addendum](PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md#2026-10-10-addendum--ci-artifact-exact-owner-and-item-reconciliation)
now have source-to-NeoForge **exact-signature reflection owner**
reconciliation and **29 actual block IDs** (not names guessed).
Their 26 registered block-item creators consist of 3
StandingAndWallBlockItem and 23 BlockItem. The three
wall-torch block IDs have no direct registered BlockItem;
their items select wall variants through StandingAndWall.
8 Planetary mixin classes exist AND appear in mixin config;
LanternBlock and AmethystClusterBlock have no dedicated
support mixin in the inspected directory. 11 new rows promoted
to REFLECTION_OWNER_VERIFIED; total remains
**35/241 source-reviewed** (81/1060 IDs). **ASM invocation,
patch methods, Planet integration correctness and gameplay
remain REVIEW_PENDING**. Next: 2.3A-2.3 non-FACING family.

## 2026-10-10 Stage 2.3A-2.3: non-FACING source+runtime growth graph owners

Added [12-class growth graph source and actual NeoForge
reflection owners audit](PHASE2_STAGE3A_NONFACING_GROWTH_GRAPHS_1_21_1.md):
`kelp/kelp_plant`, `cave_vines/cave_vines_plant`,
`weeping_vines/weeping_vines_plant`,
`twisting_vines/twisting_vines_plant`, `sugar_cane`,
`cactus`, `bamboo`, `bamboo_sapling`.
All 12 were `has_orientation_candidate=false` in the original CI
census. None should be classified orientation-independent on
this heuristic: head/body use non-state `growthDirection`,
SugarCane and Cactus use horizontal checks, Bamboo
creates sapling states and chains age/height.
The original item census found 7 direct block item
authors and 5 block state variants with no registered
own item. One exact-signature overload in `KelpBlock`
would be misattributed by method-name-only join.
Cumulative ledger now **47 source+reflection reviewed /
194 REVIEW_PENDING** of 241 classes and
**93 / 967** associated registered block IDs. No runtime
ASM / Mixin application or gameplay gate accepted.
Next is 2.3A-2.4 final census/disposition audit, separately.

## 2026-10-10 Stage 3A-2.4 exhaustive registry ledger integrity gate

The original artifact `11643813158` (SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`)
was compared independently with the current
[241-class ledger](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).
[Exact matching results](PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md):
241/241 full class names + per-class registered ID counts,
47/47 reviewed exact registered ID sets + 5 full
method-signature declaring owners. Four independent
canonical fingerprints equal the original ZIP;
all 194 pending classes remain pending and all
ASM, adaptation/runtime and gameplay fields are
REVIEW_PENDING. No silent PASS or registry metadata
misclassification. 47 source-reviewed classes represent
93 IDs, 194 pending represent 967 IDs.
Next **Stage 2.3A-3** has a separate
[10-class actual-registry connectivity candidate card](../phases/phase-02/02a-block-graph-owners.md);
those candidates are *not* classified in this checkpoint.

## 2026-10-10 Stage 3A-3.1 eight connected-graph class owners

[Verified source + NeoForge reflection audit](PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md):
8 new classes / **69 actual registered block IDs**,
`FenceBlock`, `FenceGateBlock`, `WallBlock`,
`IronBarsBlock`, `StainedGlassPaneBlock`,
`VineBlock`, `GlowLichenBlock`,
`SculkVeinBlock`. The source base
`CrossCollisionBlock` is distinct from `MultifaceBlock`,
and `WallBlock` uses above-cell collision-face
and post/LOW/TALL calculations, not Fence flags.
Existing FenceBlockGravityMixin source is not evidence
of other families' runtime correctness. TripWire and
TripWireHook remain source REVIEW_PENDING
(2 IDs), intentionally deferred from redstone network.
Full current 241-class TSV distribution:
**55 SOURCE_REVIEWED_INTEGRATION_PENDING**
(162 IDs); **186 REVIEW_PENDING** (898 IDs).
All 241 ASM/adapter/gameplay gates pending.
Next first task Stage 3A-3.2: item creation and
spreading/interaction alternative author join.

## 2026-10-10 Stage 3A-3.2 exact ITEM creator and alternate-state writers

[69-item direct join and alternative-author audit](PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md): all 69 reviewed connected-graph block IDs have distinct ordinary `BlockItem` creators (no aliases). Item-method declarations all `BlockItem`, but `BLOCK_STATE` component overrides may rewrite the resulting state after placement. Additional independently read 1.21.1 authors: gate `useWithoutItem`, `neighborChanged`, `onExplosionHit`; fence leash interaction; vine `randomTick`; GlowLichen `MultifaceSpreader`; SculkVein `regrow`, charge placement, discharge; and generic `MultifaceGrowthFeature` non-player placement overload. Source and target frame and Phase 5/7A/8 semantics remain to audit; no patched NeoForge bytecode or game test accepted. No new class dispositions: **55 reviewed/186 pending**, 162/898 block IDs. Next `2.3A-3.3`, independent chart/shape/cache source analysis.

## 2026-10-10 Stage 3A-3.3 source/target physical event + shape/cache chart

[Dedicated graph state/shape/callback evidence](PHASE2_STAGE3A_GRAPH_SHAPE_CALLBACK_CHART_1_21_1.md)
covers the same eight classes and all 69 prior block IDs.
BlockStateBase `initCache` precomputes face-sturdy
per Direction+SupportType, collision and occlusion
for non-dynamic BlockStates; graph concrete classes
also cache state-to-geometry mappings.
Vanilla BlockStateBase neighbor event walkers operate on
**physical** Direction and actual BlockPos; correct Planet
semantics require a **source-local slot** and separately
**target-local inward face**.
The existing PlanetBlockNeighborQuery and
BlockStateShapeMixin/PlanetBlockShapeRuntime
implement relevant source-side boundaries but do not
prove all callers have been adapted. Particular
future tests: MultifaceBlock
`getBlockSupportShape` (canonical) OR
`getCollisionShape` (position-physical)
under one `Block.isFaceFull` direction argument,
and WallBlock above-cell collision DOWN face
combined with canonical POST/NORTH/etc tests.
Potential mismatches are **not** proven bugs or PASS.
Phase2/3/5/7A/8 boundaries and all-face/corner
test matrix recorded. No class/ID statuses promoted:
55 source+reflection reviewed (162 IDs),
186 REVIEW_PENDING (898 IDs), all ASM and game fields
pending. Next Stage 3A-3.4 final cohort invariant check.

## 2026-10-10 Stage 3A-3.4 original CI ledger and next-family gate

[Exact reconciled 241-class / 55-reviewed class evidence](PHASE2_STAGE3A_GRAPH_COHORT_RECONCILIATION_1_21_1.md):
the original NeoForge 21.1.215 ZIP was independently
parsed and four canonical digests match current branch:
0xf188a064 (class roster/count), 0x5e998532 (55 exact
ID sets), 0x98207d0 (55 sets of 5 method declaring
owners), and 0x1f4133ac (reviewed IDs+owners combined).
69 source-reviewed graph BLOCK IDs have 69 ordinary
registered BlockItems, with 2 TripWire classes
explicitly still `REVIEW_PENDING`. Current totals:
**55/241** source+reflection reviewed (162 IDs) and
**186/241** source REVIEW_PENDING (898 IDs).
All NeoForge ASM/Planet/gameplay fields unchanged
REVIEW_PENDING; **Stage 3A itself remains incomplete**.
Follow-on [card 3A-4](../phases/phase-02/02b-redstone-signal-owners.md)
has 11 actual still-pending redstone/tension/directed
signal classes / 12 IDs. No new class source audits
or runtime acceptance in this reconciliation batch.

## 2026-10-10 Stage 3A-4.1 actual signal owner source/reflection audit

[8 signal classes / 8 original NeoForge registry BLOCK IDs](PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md)
have exact-signature nearest declaring owners checked
against original 21.1.215 runtime census and source
algorithms against pinned 1.21.1 Java:
TripWireBlock/TripWireHookBlock, RedStoneWireBlock,
RepeaterBlock and ComparatorBlock (shared nonregistered
DiodeBlock), ObserverBlock, TargetBlock,
RedstoneLampBlock. Distinct paths:
TripWireHook.calculateState iterates and writes
up to 41-block tension segment/hook graph;
RedStoneWireBlock RedstoneSide 3-valued tangent states
and power recursion; repeater locking/delay;
comparator MODE/BE output/ItemFrame input; observer
watched vs opposite output face; target impact
physical face and lamp no-directional-signal state.
Three class candidates DetectorRail/PoweredRail/
DaylightDetector (four IDs) **unreviewed**.
Cumulative **63/241 source+compiled declaration
reviewed** (170 IDs); **178/241 REVIEW_PENDING**
(890 IDs). All ASM, integration, signal functional
and gameplay acceptance remain pending.
Next 3A-4.2 item/non-item state-author research.

## 2026-10-10 Stage 3A-4.2 exact signal ITEM census and off-item writers

[Eight reviewed signal-class registered items and additional
state authors](PHASE2_STAGE3A_REDSTONE_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
independent original NeoForge ITEM `placed_block`
join confirms 8/8 exact block creators, split
6 `BlockItem` and 2 `ItemNameBlockItem`:
`minecraft:string` -> `minecraft:tripwire`,
`minecraft:redstone` -> `minecraft:redstone_wire`.
All 7 item signature nearest declaring owners
resolve to `BlockItem` for all 8. Other source-authored
state writers include cable graph, entity press,
RedStoneWire POWER propagation, DiodeBlock scheduled
and neighbor updates, ComparatorBlockEntity
persisted OutputSignal, Observer pulse, projectile
TargetBlock and RedstoneLamp. Item component
`BLOCK_STATE` can mutate newly placed properties.
**No new disposition**: 63 reviewed/178 pending,
170/890 registered IDs. Rail and daylight sensors
remain pending; no ASM/Planet/7A/gameplay PASS.
NEXT 3A-4.3 signal port/chart/timing contract.

## 2026-10-10 Stage 3A-4.3 redstone source/target signal-port and causality contract

[Eight-class port/graph/timer and frame analysis](PHASE2_STAGE3A_REDSTONE_PORT_TOPOLOGY_1_21_1.md)
compares pinned vanilla 1.21.1 Java with
PlanetBlockStateFrame, NeighborQuery and
BlockFrameContext.walk. Local signal ports,
physical notification Direction, neighbor-local
inward face, signal getter queried Direction
and seam-transported direction are explicitly
distinct. Key future acceptance gates:
TripWireHook.calculateState must traverse ordered
physical cable cells up to 41 (including seams)
and update both hooks + segment states; RedStoneWire
NONE/SIDE/UP DOT/CROSS versus POWER propagation,
up/down corner notifications, `shouldSignal`
instance reentrancy and correct local/physical shape;
DiodeBlock front/side input vs opposite output
callback and tick priority; ComparatorBlockEntity
analog output; Observer FACING watched side versus
physically opposite signal output and two-tick pulse.
Rails (BaseRailBlock/RailState, PoweredRail eight-step
path, DetectorRail entity sensor) and daylight
sky-signal inputs were viewed ONLY to establish a
separate P28/Phase7A boundary, **not full owners**.
No class promotion, runtime code change or PASS:
63/241 reviewed (170 IDs), 178 pending (890 IDs).
Next 3A-4.4 status/registry reconciliation.

## 2026-10-10 Stage 3A-4.4 verified NeoForge signal class ledger

[Original CI ZIP-vs-ledger reconciliation for 63 reviewed
classes](PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md):
all 241 original class/count pairs match; 63
source+runtime-reflection reviewed classes and all
170 registered ID strings + five complete-signature
method declaring owners match original CI corpus.
Independent FNV-1a fingerprints: 0xf188a064,
0xdb8afbd5, 0x6052a7cf, 0x66be3dcb.
Original signal batch 8 classes/8 block IDs
source-reviewed, 6 ordinary BlockItems plus
2 ItemNameBlockItem aliases; no new source review
or code testing in the reconciliation itself.
Totals **63 SOURCE_REVIEWED_INTEGRATION_PENDING**
(170 ID), **178 REVIEW_PENDING** (890 ID).
Deferred: DetectorRailBlock/PoweredRailBlock (3 IDs)
and DaylightDetectorBlock (1 ID), plus RailBlock
(1 ID), all pending. New independent next
[3A-5 rail owner card](../phases/phase-02/02c-rail-owners.md):
3 pending rail classes/4 registered IDs,
nonregistered BaseRailBlock and RailState.
All 241 patched NeoForge ASM/Planet runtime/gameplay
gates still REVIEW_PENDING.
Stage 3A and Phase 2 incomplete.

## 2026-10-10 Stage 3A-5.1 rail topology actual owner audit

[Detailed original NeoForge and pinned Minecraft
rail owner source evidence](PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md):
3 newly source+compiled runtime declaration
reviewed concrete classes (4 BLOCK IDs),
RailBlock, DetectorRailBlock and PoweredRailBlock
(activator + powered). All five core nearest
owners inherited:
BaseRailBlock placement/survive/updateShape,
BlockBehaviour randomTick and Block setPlacedBy.
Graph update actually delegated from BaseRailBlock
onPlace/neighborChanged/updateDir to `RailState`
which walks actual physical cells and writes
neighbor rails. Notably `RailState.hasConnection`
compares physical X and Z while ignoring Y;
ascending shapes use world above/below and
horizontal cardinals, requiring source/target local
chart contract across cube faces and seams.
RailBlock.updateState at 3-way redstone junction,
DetectorRailBlock own onPlace/scheduled minecart
power check, PoweredRailBlock up-to-eight segment
power walk/source variant check identified.
Rail water logging preserved as separate Phase5
concern. DaylightDetectorBlock still unreviewed.
**66/241** SOURCE_REVIEWED_INTEGRATION_PENDING
(174 IDs), **175/241** REVIEW_PENDING (886 IDs).
All ASM/Planet/gameplay gates remain pending.
Next 3A-5.2 exact item and alternate authors.

## 2026-10-10 Stage 3A-5.2 exact 4 ITEM creators and non-ITEM rail writers

[Full original item and alternate source author
evidence](PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
four actual NeoForge 21.1.215 BLOCK IDs of
RailBlock/DetectorRailBlock/PoweredRailBlock
uniquely map to four ordinary BlockItem ITEM
records, `minecraft:rail`, detector, powered,
activator. All seven effective item method
declarations are BlockItem for each.
State writes bypassing item include
RailState.place/connectTo graph recalculation,
DetectorRail minecart search/20-tick check
and analog command/inventory minecart output,
PoweredRailBlock 8-segment power propagation
and distinct powered/activator block instances.
AbstractMinecart treats powered rail acceleration
and activator-rail activateMinecart differently.
MineshaftPieces has proven direct Blocks.RAIL
worldgen writes; StructureTemplate generic
transformed state+shape update is another
possible caller, no particular rail structure
template claimed.
No new class status: 66/241 source+reflection
reviewed/174 ID, 175/241 pending/886 IDs.
DaylightDetector pending; all ASM/Planet/gameplay
acceptance fields remain pending. Next 3A-5.3
seam/rail topology source test matrix.

## 2026-10-10 Stage 3A-5.3 source and target rail seam geometry

[RailShape ten-valued local port/grade,
RailState physical seam/corner and minecart
cross-phase test chart](PHASE2_STAGE3A_RAIL_SEAM_FRAME_CONTRACT_1_21_1.md)
revisited original pinned Minecraft 1.21.1 Java
and real PlanetBlockStateFrame/NeighborQuery/
BlockFrameContext.walk/SupportQuery/ShapeRuntime
sources. RailShape cardinal and corner ports
must be authored in source's canonical chart;
`ASCENDING_*` adds LOCAL UP height, even when
source local tangent maps to world Y at a cube seam.
At physical target, inward side must derive from
target's own canonical chart, never simply
`sourcePort.getOpposite()`. RailState
`hasConnection` XZ-only projected equivalence,
worldY `getRail` +/- offset, multi-cell
`connectTo/place`, BaseRailBlock ascending
support, detector/powered physical neighbor
signals and AbstractMinecart world-XZ/world-Y
velocity and detection are distinct unchecked
integration duties. Separate Phase3 visual,
Phase5 fluid, Phase7A power/timing,
Phase8 direct mineshaft worldgen and cart
physics acceptance. 11 runnable future game/
CI fixtures specified, none executed.
Totals unchanged **66/241 reviewed 174 IDs,
175 pending 886 IDs**. Next 3A-5.4
original compiled owner/ID/status checkpoint.

## 2026-10-10 Stage 3A-5.4 original NeoForge rail cohort and full reviewed ledger reconciliation

[Freshly verified exact 241-class/1060-ID registry and
66 reviewed-class five-owner signature census](PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md):
original unmodified NeoForge 21.1.215 artifact
11643813158 (ZIP SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`)
independently parsed, preserving five method
overload signatures and actual ID lists.
All four original ZIP/ledger diagnostic checksums
match `0xf188a064` (roster),
`0x6c5c67d7` (66 reviewed exact ID),
`0x57a01200` (five owners),
`0xb3945a6e` (combined).
Three rail classes/four BLOCK IDs from Stage3A-5
included and reconciled. No source-pending class
promoted this packet: **66/241 reviewed (174 IDs),
175/241 REVIEW_PENDING (886 IDs)**.
All bytecode/Planetary/gameplay acceptance pending.
Four rail research subtasks done; *not* full Stage 3A.
Next owner card:
[environment and physical pressure sensors 3A-6](../phases/phase-02/02d-environment-pressure-sensor-owners.md)
DaylightDetectorBlock 1, PressurePlateBlock 13,
WeightedPressurePlateBlock 2, all currently
pending and distinct source-author algorithms.

## 2026-10-10 Stage 3A-6.1 daylight and pressure sensor actual owner/source audit

[Verified 3 concrete sensor class/16 exact registered IDs
in original runtime NeoForge ZIP plus pinned 1.21.1
complete source algorithms](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md):
DaylightDetectorBlock one ID, PressurePlateBlock
13 IDs, WeightedPressurePlateBlock two IDs.
Method nearest declaring owners
DaylightDetector Block / BlockBehaviour
(survive,updateShape,randomTick) / Block(setPlacedBy);
plate subclasses Block / BasePressurePlateBlock
(canSurvive,updateShape) / BlockBehaviour
(randomTick) / Block (setPlacedBy).
Crucial algorithm split: DaylightDetectorBlock
sky LightLayer.SKY/skyDarken/sunAngle sampling
and server-only BE ticker in hasSkyLight
dimensions every 20 game ticks, interaction
cycles INVERTED & recalculates POWER;
BasePressurePlateBlock collision `TOUCH_AABB`
physical entity query, scheduled tick
checkPressed, sturdy below/center face support,
direct signal queried UP and world below
notifications; PressurePlateBlock Boolean
20-tick entity sensitivity and WeightedPressurePlateBlock
integer 0..15 10-tick scaled entity-count.
Local support and physically rotated entity hitbox
are NOT yet proven; BE ticker is not randomTick.
3 source class rows/16 IDs promoted, no ASM/game
acceptance: **69 reviewed (190 IDs), 172 pending
(870 IDs)**. Next 3A-6.2 actual ITEM registry.

## 2026-10-10 Stage 3A-6.2 actual ITEM registry sensor join and other source authors

[Full independently verified original NeoForge
16-ITEM/16-BLOCK 1:1 census](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md)
matched `placed_block` from original 1333
ITEM rows to selected 16 BLOCK IDs:
all same-name ordinary BlockItem (1/daylight,
13 ordinary plates, 2 weighted plates),
7/7 item placement method owners each
BlockItem; `Item.use` belongs to Item.
Validated reproducible canonical 16-row
SHA256 `49c88b217ca7b0dd560634c2c8cb01560b265b29a85030ebda92b66f7596ebe9`.
Post-place component update may override legal
`POWERED/POWER/INVERTED` state before
`setPlacedBy`, distinct from source
owner `BasePressurePlateBlock` entityInside/
20- or 10-tick press rechecks (physical
TOUCH_AABB), daylight BE 20-tick SKY read
and player-use INVERTED change. Generic
StructureTemplate direct state writer bypasses
items; specific sensor-generating vanilla
templates/worldgen not found or claimed here.
Full ledger remains **69/241 source-reviewed
(190 BLOCK IDs)**, **172/241 pending
(870 IDs)**, all ASM/Planet/gameplay acceptance
still pending. Next 3A-6.3 physical/sky/seam
and testing contract.

## 2026-10-10 Stage 3A-6.3 sensor six-face and physical world contract

[Complete source-only canonical frame, plate
TOUCH_AABB, sky and redstone direction
test specification](PHASE2_STAGE3A_SENSOR_FACE_FRAME_AND_SIGNAL_CONTRACT_1_21_1.md)
reads real Planetary PlanetFace/StateFrame/
FrameContext/NeighborQuery/SupportQuery/
ShapeRuntime classes plus pinned vanilla sensor
source. Six exact local-UP/DOWN axes from
PlanetFace used to document pressure plate
support (source local DOWN, target physical cell,
target local face), invalidation and neighbor
notifications. `BasePressurePlateBlock`
`TOUCH_AABB` is a raw entity query AABB,
not VoxelShape and not automatically rotated
by `BlockStateShapeMixin`; even rotating
the box may not suffice if source
`entityInside` isn't invoked.
`BasePressurePlateBlock.getDirectSignal`
Direction.UP is a signal **queried port**
and separate from world `pos.below()`
physical callback. DaylightDetector
world LightLayer.SKY/getSkyDarken/getSunAngle
has no FACING property; local six-face
skylight semantics require separate design.
Daylight BE ticker server every gameTime%20
versus pressure plate scheduled 20/10
relative ticks. Phase2 support state,
Phase3 entity AABB/model, Phase7A power/tick,
Phase8 structure authors distinct.
15 future vanilla-control, six-face, seam/
corner fixtures specified; none executed.
**Ledger unchanged: 69/241 source-reviewed
(190 IDs), 172 pending (870 IDs)**,
all ASM/Planetary/gameplay PENDING.
NEXT 3A-6.4 original ZIP reconciliation,
new small owner-family card.

## 2026-10-10 Stage 3A-6.4 full 69-class NeoForge ZIP five-owner reconciliation

[Directly parsed original runtime artifact
vs current source+reflection reviewed
exact IDs and method declaring owners](PHASE2_STAGE3A_SENSOR_COHORT_RECONCILIATION_1_21_1.md):
241 exact class/1060 ID source roster,
69 reviewed classes/190 individually exact
registered IDs, all five full-signature
declaration owners for each checked; original
vs GitHub independent FNV fingerprints MATCH
roster `0xf188a064`, IDs `0x6ae8047b`,
five owners `0xba6ef72f`, combined
`0x328b390e`.
No class promotion; **69/241 source-reviewed
(190 IDs), 172/241 pending (870 IDs)**;
all 241 patched ASM/Planetary integration/
gameplay gate statuses REVIEW_PENDING.
3A-6 daylight/plate research 4/4 done
but Phase 2 not done.
Next [3A-7 sculk sensor/calibrated vibration
source owner family](../phases/phase-02/02e-sculk-vibration-sensor-owners.md)
2 pending classes/2 exact original IDs;
other sculk shrieker/catalyst pending
independently.

## 2026-10-10 Stage 3A-7.1 sculk vibration sensor true owner family source audit

[Primary original NeoForge compiled 5 method
owners / 2 concrete registered sensor IDs,
plus pinned comparative 1.21.1 source VibrationSystem
and two BlockEntity user subclasses](PHASE2_STAGE3A_SCULK_VIBRATION_SOURCE_OWNER_AUDIT_1_21_1.md).
SculkSensorBlock owners:
SculkSensorBlock, BlockBehaviour,
SculkSensorBlock, BlockBehaviour, Block;
CalibratedSculkSensorBlock owners:
CalibratedSculkSensorBlock, BlockBehaviour,
SculkSensorBlock, BlockBehaviour, Block.
Plain sensor needs no FACING property but
owns vibration listeners and scheduled phase
transitions, physical world-below callbacks,
six physical resonator neighbors and
world XYZ particle animation.
VibrationSystem.Listener checks events,
ray-occlusion and Euclidean distance;
Ticker selects/delivers over travel time and
physical 3×3 XZ chunk tick gate, not an
instant block `neighborChanged`.
SculkSensorBlockEntity receiver radius 8,
calibrated 16, LAST vibration frequency BE,
source getAnalogOutputSignal when ACTIVE;
plain ACTIVE 30 ticks, calibrated ACTIVE
10 ticks, both cooldown 10. Calibrated
BlockState HORIZONTAL FACING/rotate/mirror
and own `getSignal` output suppression
on query==FACING; Calibrated BE VibrationUser
`getBackSignal` uses world
`pos.relative(FACING.getOpposite())`
and `Level.getSignal(pos,direction)` to
filter event frequency. Waterlogged state
schedules water tick; alternate `stepOn`
listener and BE event authors considered.
No actual NeoForge patched ASM/Planetary
adapter/gameplay acceptance: all 241 pending.
Ledger **71/241 reviewed/192 IDs**,
**170/241 pending/868 IDs**.
Next 3A-7.2 original ITEM creators and
alternate events.

## 2026-10-10 Stage 3A-7.2 2 real sculk ITEM creators and alternative event/state authors

[Source-verified original NeoForge 21.1.215
ITEM.placed_block census and actual
VibrationSystem/BE/BlockItem alternate
author trace](PHASE2_STAGE3A_SCULK_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
2/2 exact ITEM IDs one-to-one with
minecraft:sculk_sensor and
minecraft:calibrated_sculk_sensor BLOCK
IDs, both ordinary BlockItem, all
seven item placement lifecycle owners
BlockItem; separate Item.use declared
in Item. Full original joined row SHA256
`6225d8898725346b8f37d34d97d64a4a7a86a17a5cf39e02d339f2bc0972fee5`. BlockItem.place initial
BlockState from SculkSensorBlock water
and calibrated FACING, optional post-place
DataComponents.BLOCK_STATE and BE data
writers. Non-item causal writers:
VibrationSystem.Listener/Ticker and BE
onReceiveVibration writes last frequency,
SculkSensorBlock.activate writes POWER/
ACTIVE, scheduled tick → COOLDOWN → INACTIVE,
onPlace reset, `stepOn` additional
forced event, six physical resonator
GameEvent producers, calibrated backside
getSignal filter (reader) and BE last
frequency analog comparator output (reader).
Generic StructureTemplate direct block
writer exists, specific sensor worldgen
feature NOT established. SculkShrieker/
Catalyst/lightning remain unreviewed.
Totals unchanged **71 reviewed class/
192 exact IDs; 170 source pending/868 IDs**,
all patched ASM/Planet/gameplay gates pending.
NEXT 3A-7.3 canonical local FACING/physical
events and future acceptance fixtures.

## 2026-10-10 Stage 3A-7.3 canonical FACING vs real-world vibration/acoustic and signal owners

[Complete real Planetary face table and
pinned Minecraft 1.21.1 world XYZ
vibration, port and shape contracts](PHASE2_STAGE3A_SCULK_SIX_FACE_VIBRATION_SIGNAL_CONTRACT_1_21_1.md)
cover two registered sculk sensor Java
classes. Real canonical local HORIZONTAL
FACING can map to physical vertical
world direction on ±X/±Z cube faces.
Calibrated BE getBackSignal combines
physical FACING-opposite neighbor and
Level.getSignal queried-side, which
must be separately verified in Phase7A;
own getSignal query==FACING weak output
suppressed and inherited direct signal
only UP. VibrationSystem.Listener/Ticker
uses physical Vec3 distance/rays and
actual world XZ 3x3 chunk ticks;
not gravity-rotated topological route.
Six physical resonator neighbors
remain distinct from world-below
redstone callback. Waterlogged fluid
tick, sounds and half-height local
VoxelShape with separate animateTick
world-Y particle path + stepOn physical
dispatch recorded. Server BE listener
travel vs scheduled ACTIVE 30/10 and
COOLDOWN10 block ticks separated.
19 future runnable tests written, NONE
executed. Source-only ledger unchanged
**71/241 source-reviewed (192 IDs),
170/241 pending (868 IDs)**, all ASM/
Planetary runtime and gameplay pending.
Next 3A-7.4 original ZIP whole cohort
reconciliation and separate new family.

## 2026-10-10 Stage 3A-7.4 — all 71 exact reviewed NeoForge class/owner tuples reconciled

[Direct original unchanged NeoForge 21.1.215
CI ZIP vs actual 16-column
GitHub ledger](PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md):
241 unique registered class names/
1060 exact BLOCK ID rows/1333 ITEM rows/
1712 state property rows. All 71
SOURCE_REVIEWED_INTEGRATION_PENDING
classes, **192** exact registered IDs
and their nearest five declaring
method owners via **full qualified
signatures**, all match original
runtime reflection ZIP:
roster `0xf188a064`,
reviewed IDs `0x8a6840ba`,
owners `0xa81e5835`,
combined `0x59d9c0aa`.
All 170 remaining classes still source
REVIEW_PENDING; all 241×3 patched NeoForge
ASM/Planet/runtime/gameplay acceptance
columns REVIEW_PENDING. No class promotion,
no patched bytecode, game test or Java code
change. Sculk sensor 3A-7 research 4/4
complete only. Next
[3A-8 shrieker/catalyst original pending
family](../phases/phase-02/02f-sculk-shrieker-catalyst-owners.md)
two exact IDs and distinct VibrationSystem/
warden vs EntityDie/charge cursor
spreader writers. LightningRod separately
pending. Phase 2 not complete.

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
