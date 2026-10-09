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
