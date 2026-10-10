## 2026-10-10 Stage 3A-6.3 — six-face sensor support/AABB and sky/signal contracts

[Position-only canonical BlockState, support target
and physical AABB, daylight sky and tick, queried
signal-side chart](PHASE2_STAGE3A_SENSOR_FACE_FRAME_AND_SIGNAL_CONTRACT_1_21_1.md).
Read actual Planetary `PlanetFace`,
`PlanetBlockStateFrame`, `PlanetBlockFrameContext`,
`PlanetBlockNeighborQuery`,
`PlanetBlockSupportQuery`,
`PlanetBlockShapeRuntime`,
`PlanetVoxelShapeRotation`, plus pinned
vanilla 1.21.1 sensor sources.
Six canonical gravity normals and local
UP/DOWN exact physical axes tabulated.
Pressure plate `canSurvive` needs physical
local-DOWN support target canonical side
and rigid or center face; `updateShape`
support-direction callback may differ from
source-local DOWN at seam. Direct neighbor
updates at support's physical BlockPos must
not confuse `getDirectSignal(Direction.UP)`
*query argument* with real source-to-target
physical displacement.
Raw `TOUCH_AABB` (X/Z 1/16..15/16,
local Y 0..4/16) used by `getEntityCount`
is not VoxelShape used by `getShape`;
it needs independent canonical-frame
rotation and `entityInside` dispatch
verification. Existing ShapeRuntime rotates
outermost VoxelShape for Level queries,
not arbitrary raw AABB.
World `LightLayer.SKY` and sun angle
are not local radial sunlight; source-only
design contract deliberately does NOT
create invented per-face skies.
Daylight server `BlockEntityTicker` checks
`gameTime%20` vs plate scheduled
20/10 after pressed; not randomTick.
15 proposed six-face, seam/corner and
vanilla-control fixtures defined, **NONE run**.

Current full registry ledger unchanged:
**69/241 SOURCE_REVIEWED_INTEGRATION_PENDING
(190/1060 BLOCK IDs), 172/241 REVIEW_PENDING
(870/1060 IDs)**. All 241 patched ASM,
Planetary runtime/gameplay gates pending.
Next first checkbox 3A-6.4 original CI ZIP
69 reviewed class/190 IDs/five nearest
declaration owner reconciliation and
next independent family card.

## 2026-10-10 Stage 3A-6.2 exact 16 original sensor ITEM creators and non-item authors

[Original 1333 ITEM registry exact 16-block
placed_block join + nonitem sky/entity/structure
author report](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md)
checks unchanged NeoForge ZIP artifact 11643813158:
for each of 1 daylight, 13 ordinary and 2 weighted
plate BLOCK ID, exactly ONE same-name ITEM ID
`block_item=true`, concrete class `BlockItem`;
all SEVEN exact item lifecycle method owners
are `BlockItem` (except separate `use` from
`Item`). Reproducible SHA256 of canonical
16 source ITEM join rows:
`49c88b217ca7b0dd560634c2c8cb01560b265b29a85030ebda92b66f7596ebe9`.
Normal BlockItem `placeBlock` precedes
optional DataComponents.BLOCK_STATE
`updateBlockStateFromTag` and `setPlacedBy`.
Off-item writes:
DaylightDetectorBlock sky/time server
BlockEntityTicker and `useWithoutItem` INVERTED;
BasePressurePlateBlock entityInside/checkPressed,
scheduled 20/10-tick POWERED/POWER update, physical
TOUCH_AABB and below callback; generic StructureTemplate
setBlock and neighbor update bypass item, but no
specific sensor-containing worldgen structure
verified. Phase3 physical hit AABB, Phase7A
signal direction, sky environment policy and
Phase8 generic structures remain future gates.

**Counts stable: 69/241 reviewed 190/1060 IDs,
172/241 pending 870/1060 IDs**. All 241
ASM/Planet/gameplay statuses pending. Next
3A-6.3 local physical frame/sky/pressure plate
port and tests, one research commit.

## 2026-10-10 Stage 3A-6.1 sensor BLOCK owner source audit (3 new classes, 16 IDs)

[Original NeoForge 21.1.215 compiled exact-five owner
and pinned vanilla source audit](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md):
DaylightDetectorBlock (minecraft:daylight_detector),
PressurePlateBlock (**13** vanilla plate IDs),
WeightedPressurePlateBlock (**2** weighted plates).
Compiled exact method owners:
DaylightDetector [Block/BlockBehaviour/
BlockBehaviour/BlockBehaviour/Block];
both pressure-plate subclasses
[Block/BasePressurePlateBlock/
BasePressurePlateBlock/BlockBehaviour/Block].
Source daylight behavior via DaylightDetectorBlock
ticker only on server if dimension.hasSkyLight,
every gameTime%20==0, sky brightness/sun-angle and
INVERTED direct player-interaction recalculation.
DaylightDetectorBlockEntity only holds type; no
own light processing or custom randomTick.
BasePressurePlateBlock owns non-spectator physical
TOUCH_AABB, tick/neighbor callback, local DOWN
support query (rigid or center UP), direct
getSignal queried UP and notifications at position
and world-below; PressurePlateBlock Boolean
POWERED 20-tick recheck and type-dependent
entity sensitivity, WeightedPressurePlateBlock
analog POWER using ceil(entity count/maxWeight *
15) with 10-tick recheck. Sky light is a WORLD
sampling policy, NOT FACING rotation; plate
trigger entity AABB is not automatically rotated
by visual shape mixin.

**Updated cumulative 69/241 source+reflection
reviewed (190/1060 IDs), 172 REVIEW_PENDING
(870/1060 IDs)**. Sculk and lightning families
still pending; all patched ASM/Planet/gameplay
gates remain REVIEW_PENDING. Next 3A-6.2
original ITEM placed_block join and other authors.

## 2026-10-10 Stage 3A-5.4: FULL original ZIP vs all 66 source-review class declarations

[Exact 241-class/66-reviewed NeoForge 21.1.215 census recheck](PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md)
loaded original ZIP CI artifact 11643813158
SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
66 reviewed class names + **all 174 registered
BLOCK ID strings** + exact five-owner tuple
by fully-qualified method ARGUMENT signatures
independently reconstructed from runtime ZIP;
all 241 class/count pairs reconciled.
Four computed original-vs-branch fingerprints
matched: roster `0xf188a064`,
reviewed IDs `0x6c5c67d7`,
reviewed five owners `0x57a01200`,
combined `0xb3945a6e`.
Full ledger intact: **66/241 source+compiled
declarations reviewed (174/1060 IDs)**,
**175/241 REVIEW_PENDING (886 IDs)**.
All 241 ASM/Planet/gameplay acceptance gates
REVIEW_PENDING, no Java/CI/client actions.
`RailBlock`, `DetectorRailBlock` and
`PoweredRailBlock` specifically reconciled
(3 classes/4 IDs). Stage 3A-5 research card
4/4 done; NOT Stage 3A/Phase 2 complete.

Next [Stage 3A-6 environment/contact sensor card](../phases/phase-02/02d-environment-pressure-sensor-owners.md):
DaylightDetectorBlock (1), PressurePlateBlock
(13), WeightedPressurePlateBlock (2) = 3
still-pending classes / 16 IDs, with sky/BE
and entity collision signal authors distinct.

## 2026-10-10 Stage 3A-5.3 local rail endpoint, slope and seam graph

[Full source-based rail RailShape direction / physical
endpoint and minecart cross-phase contract](PHASE2_STAGE3A_RAIL_SEAM_FRAME_CONTRACT_1_21_1.md)
defines ten RailShape logical port layouts (four
ASCENDING local-UP height offsets, four quarter
turns available only on RailBlock). Distinguishes
source canonical BlockState frame, actual physical
neighbor BlockPos and target local inbound side,
source local UP grade, transported seam continuation.
RailState.updateConnections uses global N/S/E/W plus
world-Y `above`, `getRail` samples world above/
below and `hasConnection` matches X,Z while
ignoring world Y. BaseRailBlock extra ascending
support, shape/waterlogging and neighbor callbacks
must be handled separately; existing
PlanetBlockStateFrame, NeighborQuery,
BlockFrameContext, SupportQuery and shape mixin
are primitives but not rail algorithm acceptance.
PoweredRail power depth≤8, detector entity AABB/
20-tick and analog side, AbstractMinecart global
XZ/Y detection and curve physics, Mineshaft
worldgen are **separate phase-owned gates**.
11 future fixtures defined for six faces,
edges/corners, slopes/turns, power, carts,
water and worldgen. No tests actually run.

**No new class status:** 66/241 source+reflection
reviewed (174/1060 IDs), 175 REVIEW_PENDING
(886/1060 IDs), DaylightDetector still pending;
all ASM/Planet/gameplay verdicts pending.
**Next 3A-5.4 exact CI ZIP ledger and owner
signature reconciliation + next family card.**

## 2026-10-10 Stage 3A-5.2 exact rail ITEM registry and verified non-item writers

[Actual NeoForge 21.1.215 four-item/four-BLOCK join and
alternate rail-world state authors](PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
original ZIP SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`
independently parsed and used to join
`minecraft:rail`, `detector_rail`,
`powered_rail`, `activator_rail` to
four distinct, same-named regular `BlockItem`
item records; all seven exact item lifecycle
nearest declaring owners `BlockItem` for
each entry. This is unlike redstone/string
`ItemNameBlockItem` aliases.
Source-authored additional state/consumer paths:
`RailState.place/connectTo` multi-rail
shape writes triggered by support and callbacks;
`DetectorRailBlock.entityInside/checkPressed`
entity search, 20-tick `POWERED` update and
analog minecart command/container output;
`PoweredRailBlock` eight-rail power scan
with distinct block instances,
`AbstractMinecart.tick` separate
`Blocks.POWERED_RAIL` motion and
`Blocks.ACTIVATOR_RAIL` activateMinecart hook;
real MineshaftPieces `Blocks.RAIL` direct
worldgen `placeBlock` (no ITEM) and generic
StructureTemplate block state writes.
Item component `BLOCK_STATE` can rewrite shape
after placeBlock before setPlacedBy when supplied.
**No new class review:** 66/241 reviewed
(174 BLOCK IDs), 175 pending (886 IDs);
DaylightDetector still pending. All ASM,
Planet runtime and gameplay gates PENDING.
Next 3A-5.3 source-local and physical rail
shape/rail/carts cross-phase research.

## 2026-10-10 Stage 3A-5.1 — rail exact owner + RailState graph (3 new classes)

[Original NeoForge 21.1.215 compiled declaration owner
plus pinned vanilla rail-graph source review](PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md)
newly source+reflection audits RailBlock (1 BLOCK ID),
DetectorRailBlock (1), PoweredRailBlock (2
IDs: activator_rail and powered_rail): **3/241
new class rows, 4 IDs**, all five effective owners
at BaseRailBlock/BaseRailBlock/BaseRailBlock/
BlockBehaviour/Block respectively.
Nonregistered BaseRailBlock and **RailState**
perform placement, survival, neighbor onPlace,
slope support, per-cell graph re-evaluation and
multi-rail state mutation; RailState has hardcoded
physical XZ-only connection check, world-Y
above/below and world-horizontal scan. RailBlock
can turn and change track at 3-neighbor powered
junction; detector scheduled minecart sensing,
PoweredRail eight-step graph power scan are
distinct Phase7A signal pathways. `updateShape`
primarily schedules water ticks (P37), not rail
graph writes. No NeoForge patched ASM, integration
or gameplay acceptance claimed.

**New ledger totals: 66/241 source+reflection
reviewed (174/1060 IDs), 175 REVIEW_PENDING
(886/1060 IDs)**. DaylightDetectorBlock still
REVIEW_PENDING. Next Stage 3A-5.2 original ITEM
and alternative authored state paths.
Historical earlier counts below are past snapshots.

## 2026-10-10 Stage 3A-4.4 primary CI ZIP original full-lattice reconciliation

[Exact 241-class and 63-source-reviewed owner
reconciliation](PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md)
used original unmodified NeoForge 21.1.215
CI run 37988064055 artifact 11643813158,
ZIP SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
All 241 fully qualified classes and 1060 registered
ID counts matched; **63 reviewed classes with
170 registered block IDs** match original exact
five signature method declaring owners and ID lists.
Four original ZIP/committed TSV fingerprints match:
`0xf188a064` roster, `0xdb8afbd5` IDs,
`0x6052a7cf` owners, `0x66be3dcb` combined.
All 178 other classes (890 IDs) source REVIEW_PENDING,
all 241 ASM/Planet/gameplay verdicts REVIEW_PENDING.
Eight signal-class source and item aliases previously
checked; no new gameplay, code, or CI run here.

**Stage 3A-4 four research card tasks DONE, NOT
full Stage 3A/Phase 2 DONE.** Next first unfinished:
[3A-5 rails](../phases/phase-02/02c-rail-owners.md),
3 still-pending rail concrete classes/4 IDs, daylight
separate; one bounded source-owner packet.

## 2026-10-10 Stage 3A-4.3 source-local vs physical redstone signal-port chart

[Complete eight-reviewed-class signal port, callback,
seam-walk, shape-cache, and timing contract](PHASE2_STAGE3A_REDSTONE_PORT_TOPOLOGY_1_21_1.md)
separates source-local BlockState port, **real physical
neighborPos**, target-local inward face, signal-getter
API Direction, and transported continuation direction.
TripWireBlock + TripWireHookBlock multi-cell up to
41-step cable state, RedStoneWireBlock
NONE/SIDE/UP+DOT/CROSS geometry, synchronous POWER
notification/instance `shouldSignal` recursion guard,
DiodeBlock/Repeater/Comparator FACING versus
physical opposite neighbor output and scheduled tick
priorities, ComparatorBlockEntity, Observer two-tick
watched/output sides, target projectile face and
lamp four-tick off were investigated via **pinned
comparative source** and Planet frame infrastructure.

Original rail/daylight classes (3 classes/4 IDs) were
read ONLY for P28/7A architectural boundary, **not
promoted**. **Totals still 63/241 source+reflection
reviewed (170 IDs) / 178 REVIEW_PENDING (890 IDs)**;
all ASM/Planet/gameplay gate fields remain pending.
Next microtask **3A-4.4** exact original ZIP and
status reconciliation / next bounded owner-family card.

## 2026-10-10 Stage 3A-4.2 exact redstone item and alternative authors (NO NEW CLASSES)

[ITEM + alternate writer source audit](PHASE2_STAGE3A_REDSTONE_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
eight reviewed redstone/tension BLOCK IDs join exactly to
eight actual original NeoForge 21.1.215 ITEM entries,
**6 ordinary `BlockItem` + 2 `ItemNameBlockItem`**:
`minecraft:string` creates `minecraft:tripwire`;
`minecraft:redstone` creates `minecraft:redstone_wire`.
Seven compiled item lifecycle method declarations for
ALL eight resolve to `BlockItem`, including aliases.
Separate authors: tripwire cable `calculateState`,
power/shape/neighbor updates, observer/diode
scheduled ticks, comparator BE output, target projectile
and lamp delay; Item `BLOCK_STATE` component changes
may occur before `setPlacedBy`. No automatic
Phase 7A correctness derived.

**Current ledger unchanged:** 63/241 class source+reflection
reviewed (170 BLOCK IDs), **178 REVIEW_PENDING** (890 IDs).
Deferred DetectorRail/PoweredRail/DaylightDetector remain
pending. ASM, Planet adapter acceptance and gameplay
all REVIEW_PENDING. Next 3A-4.3 source/target signal
frame and callback/propagation audit.

## 2026-10-10 Stage 3A-4.1 exact signal graph source+NeoForge declaring owner review

[8 registered signal-class source+reflection audit](PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md)
covers **8** formerly pending classes and **8** exact
registered BLOCK IDs: TripWireBlock, TripWireHookBlock,
RedStoneWireBlock, RepeaterBlock, ComparatorBlock,
ObserverBlock, TargetBlock and RedstoneLampBlock.
RedStoneWireBlock `RedstoneSide.NONE/SIDE/UP`
vs global ABOVE/BELOW; TripWire/Hook 41-cell
double-end chain; DiodeBlock directional front/lateral
ports and comparator block entity; observer watched
vs emitted output face; non-FACING target projectile
hit and lamp signal state separately audited.
Original ZIP exact method declaration owners and
pinned 1.21.1 source verified. **No ASM/runtime/Phase7A
signal gameplay PASS**.

**Current: 63/241** source+reflection-reviewed classes
(**170/1060** BLOCK IDs), **178/241 REVIEW_PENDING**
(**890/1060** BLOCK IDs). `DetectorRailBlock`,
`PoweredRailBlock` and `DaylightDetectorBlock`
remain pending (three classes, four registered IDs).
Next card microtask **3A-4.2 ITEM authors** for these eight.
All earlier stage counts below are historical snapshots.

## 2026-10-10 Stage 3A-3.4 exact original CI reconciliation (current numbers)

[Independent original ZIP vs full ledger integrity report](PHASE2_STAGE3A_GRAPH_COHORT_RECONCILIATION_1_21_1.md)
confirms all **241** registered concrete class names and
per-class ID counts match **1060** original CI BLOCK rows.
All **55** source+NeoForge reflection-reviewed classes match
their **162 exact registered IDs** and five exact signature
method-declaring owners; four independent FNV diagnostic
fingerprints MATCH. All eight graph classes / **69** IDs
also have 69 exact distinct ordinary BlockItems in
original ITEM registry; TripWireBlock/TripWireHookBlock
remain untouched pending. All **186** other classes
(898 IDs) explicitly `REVIEW_PENDING`. **No ASM, Planet
runtime or gameplay PASS**, and no TSV disposition changed.

The `2.3A-3` research-card checkboxes are all complete,
not the Phase-2/Stage-3A acceptance gates.
NEXT: [2.3A-4 exact candidate signal class card](../phases/phase-02/02b-redstone-signal-owners.md),
11 still-pending classes / 12 registered block IDs.
All earlier guide counts are HISTORICAL checkpoints.

## 2026-10-10 Stage 3A-3.3 graph shape/cache/physical callback source audit (no class changes)

[New exact source-vs-target and shape/callback chart](PHASE2_STAGE3A_GRAPH_SHAPE_CALLBACK_CHART_1_21_1.md)
records physical vanilla `BlockStateBase.updateNeighbourShapes`,
target-local cached `isFaceSturdy`, static state-shape
caches of CrossCollisionBlock, WallBlock, VineBlock and
MultifaceBlock, and existing registered Planetary
`BlockStateShapeMixin` + `PlanetBlockShapeRuntime`,
`PlanetBlockNeighborQuery`, and render culling boundary.
**Critical integration contracts, not confirmed bugs:**
MultifaceBlock support-shape canonical vs collision-shape
physical OR predicate, WallBlock above collision-face
comparisons, caller physical callback vs local property
mapping, cached support/occlusion shapes.
No runtime or patched ASM result.

**Counts unchanged:** 55/241 class-level
`SOURCE_REVIEWED_INTEGRATION_PENDING` (162 IDs);
186/241 `REVIEW_PENDING` (898 IDs);
241 classes / 1060 block IDs total.
TripWire and hook still pending.
Next Stage 3A-3.4 class/status invariant checkpoint.

## 2026-10-10 Stage 3A-3.2 — ITEM + alternative authors (no new classes)

**69/69** registered block IDs from the eight source-reviewed
connected-graph classes map by `placed_block` to
**69 distinct ordinary BlockItem** instances in original
NeoForge 21.1.215 item registry. All seven item method
owners resolve to BlockItem. There are **no special
item variants for these 69**, but there are numerous
non-item state authors: `BLOCK_STATE` component updates
**after** initial item placement, gate interaction/redstone,
Vine random growth, lichen MultifaceSpreader, sculk
regrow/discharge/spread, neighbor graph updates and
worldgen feature placement overload. Full exact evidence:
[PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md](PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md).

**Ledger unchanged:** **55 / 241 source+reflection-reviewed**
classes (162 IDs), **186 / 241 REVIEW_PENDING** (898 IDs),
**1060 total**. All bytecode, Planet integration and
actual gameplay fields REVIEW_PENDING. Next card item 3,
shape-cache/physical neighbor + chart boundary.

## Current verified Phase-2 owner-disposition ledger (updated 2026-10-10, Stage 3A-3.1)

| Quantity | Current |
|---|---:|
| Registered concrete block classes in exact original CI census | **241** |
| `SOURCE_REVIEWED_INTEGRATION_PENDING`, exact reflection owners | **55** (162 registered block IDs) |
| `REVIEW_PENDING` class-level source/owner disposition | **186** (898 block IDs) |
| All BLOCK registry IDs | **1060** |
| NeoForge patched ASM, Planet adapter acceptance and gameplay PASS | **Not established** |

[First connected-graph source owner audit](PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md):
8 new classes (69 IDs), with P25/P26/P27 **different algorithms**.
The two TripWire candidates remain `REVIEW_PENDING`.
All older counts and “next” pointers below are historical
source-batch snapshots rather than current ledger totals.

# Phase 2 Stage 3A — class-level owner-disposition ledger

**Target:** Minecraft 1.21.1 / NeoForge 21.1.215 / Planetary `2.0`.

**Primary TSV:** [PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).

## Current VERIFIED ledger status — 2026-10-10 / 2.3A-2.4

| Current measurement | Count |
|---|---:|
| Registered concrete classes (exact original CI ZIP roster) | **241** |
| Source+NeoForge reflection-reviewed classes | **47** (93 registered IDs) |
| Classes still `REVIEW_PENDING` | **194** (967 registered IDs) |
| Registered block IDs total | **1060** |
| NeoForge patched ASM/Mixin handler and gameplay PASS | **0 verified in this census** |

**Ledger reconciliation:** [2.3A-2.4 exact census and four
matching fingerprints](PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md).
The historical per-package sections below preserve what was
known **at that earlier package's commit**. Do not interpret
their smaller review counts, pending owner uncertainty,
or old next-task pointers as the current status.

## Historical initial snapshot: first 12-class ledger (2.3A-1.3)

This is an **exhaustive actual registry-derived class roster**, **NOT
an exhaustive semantic class or gameplay audit**. Generated from the
original 2026-10-09 `phase2-neo1211-registry-census` artifact
([CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact ID `11643813158`, revision `aa395729`).
The exact original `java_class` names and counts of associated
registered block IDs were reconciled for the class roster; this
checkpoint's 12 reviewed IDs were additionally checked by name.

| Measurement | Verified count |
|---|---:|
| Registered BLOCK IDs in original census | **1060** |
| Registered concrete Java block classes | **241** |
| Concrete classes with source-reviewed lifecycle owner chains in this **class-level ledger** | **12** |
| Registered block IDs represented by those 12 | **18** |
| Concrete classes explicitly `REVIEW_PENDING` | **229** |
| IDs belonging to those 229 pending classes | **1042** |
| NeoForge patched-bytecode/ASM owner sites fully reviewed here | **0** |
| Gameplay-accepted classes from this research ledger | **0** |

The 22 earlier researched algorithm-owner **clusters** are
**not** secretly counted as 22 extra class-level completed rows.
This keeps the evidence levels and class denominator consistent.

## TSV interpretation

One row per actual **concrete runtime class**, NOT per BLOCK ID.
The literal `SEE_REGISTRY_ARTIFACT` in pending rows means
the ID values are available in the authoritative raw
`phase2-neo1211-block-registry.tsv` CI artifact but have **not
yet** been promoted into per-class reviewed evidence.
`registered_block_ids_count` is derived from the source artifact,
not estimated from names.

The five owner columns contain **the declaring class of one exact
method signature**, not hypothetical values of a block property:

- `getStateForPlacement_owner` =
  `getStateForPlacement(BlockPlaceContext)`
- `canSurvive_owner` =
  `canSurvive(BlockState, LevelReader, BlockPos)`
- `updateShape_owner` =
  `updateShape(BlockState, Direction, BlockState, LevelAccessor, BlockPos, BlockPos)`
- `randomTick_owner` =
  `randomTick(BlockState, ServerLevel, BlockPos, RandomSource)`
- `setPlacedBy_owner` =
  `setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)`

Declaring owners are from **exact 21.1.215 class-hierarchy reflection**.
Reflection cannot establish an actual caller's JVM `INVOKE`
owner, which Mixin handlers apply, or the real patched NeoForge
call path. The `registry_dispatch_evidence` column separates
those assertions. `SOURCE_REVIEWED_INTEGRATION_PENDING` means
source call flow was inspected for this owner/subclass, NOT that
the current Planetary adapter handles all cases. All NeoForge
bytecode, Planet acceptance and gameplay statuses remain pending
until later separate gates.

Comparative source is pinned to
[hackersense/OptiFine-Source `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).
This is not used as proof that the exact NeoForge-patched code
has identical bodies. Full 12-class source review:
[PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md).

### Shared base dispatch is conditional, not uniform

The 12 `BushBlock` descendants represent 18 registered IDs with
their **own effective** `canSurvive` declaring class. All 18 are
source-reviewed at the relevant owner chains. `CropBlock` and the
lower half of `DoublePlantBlock` can still delegate into the
base `BushBlock.canSurvive`, so the existing
`BushBlockLocalSupportMixin` can cover **only that delegated
portion**, not all growth, upper halves, fluids, or side attachment
paths. `MushroomBlock` implements an independent support check;
`MangrovePropaguleBlock` has an independent HANGING branch.

The 19th independently dispatched Bush-descendant registry ID is
`minecraft:sea_pickle`, class `SeaPickleBlock`, which is left
`REVIEW_PENDING` here despite previous cluster-level research.
That is intentional, not a lost row.

## Historical audit invariants / next micro-task at initial 12-class snapshot

Expected class status distribution: **12 reviewed + 229 pending = 241**.
Expected ID distribution: **18 reviewed + 1042 pending = 1060**.
No unreviewed row is classified `NOT_APPLICABLE`, `PASS`
or `IMPLEMENTED` by automatic heuristics.

Next separate packet (2.3A-1 task 4): review the next bounded
8–15 concrete-class **owner cluster** with the same evidence
standard. Update the existing TSV rows in place and update
the counters. Do not add arbitrary mixins; Phase 3A owner
census is not implementation.

## 2026-10-10 second 12-class Bush-inherited audit: incremental ledger update

The class roster is unchanged (241 real concrete classes / 1060
registered BLOCK IDs) but the **evidence statuses are updated**:

| Ledger status | Concrete classes | Associated Block IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **24** | **52** |
| `REVIEW_PENDING` | **217** | **1008** |
| **Total** | **241** | **1060** |

The new 12-class, 34-ID source audit is:
[PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md).
The first 12-class, 18-ID source audit remains:
[PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md).

This supersedes only the **older counts and next task** printed in
earlier sections of this ledger guide. Interpret their numbers as
historical snapshots, not current totals.
`SOURCE_REVIEWED_INTEGRATION_PENDING` still means actual
method-declaration owners + comparative vanilla source; exact
NeoForge-patched ASM, adaptation and gameplay remain pending.
Next task is Stage 2.3A-2, one new owner-family batch.



## 2026-10-10 Stage 2.3A-2.1: attachments batch / current totals

- 11 additional real concrete block classes / **29 IDs**
  studied in [FACE_ATTACHMENT_AUDIT](PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md).
- **35 SOURCE_REVIEWED_INTEGRATION_PENDING** / **206 REVIEW_PENDING**
  out of **241** registered classes, representing **81/1060**
  vs **979/1060** registered block IDs.
- **Crucial provenance downgrade specific to these new 11 rows**:
  `registry_dispatch_evidence =
  COMPARATIVE_SOURCE_OWNER_ONLY_NEOFORGE_REFLECTION_RECHECK_PENDING`.
  The owner columns are source-declaration/inheritance matches,
  **not exact patched NeoForge reflection verification yet**.
  This differs from the preceding 24 classes'
  `REFLECTION_OWNER_VERIFIED` results and is not silently merged
  into that evidence level.
- The new 11 rows deliberately use
  `SEE_REGISTRY_ARTIFACT_ID_JOIN_PENDING` instead of unverified
  individual block-ID mappings; per-class registry **count**
  values remain from the original artifact.
- `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`,
  `gameplay_acceptance`: REVIEW_PENDING **for all 241**.
  No class PASS, no actual code modifications.
- The counts printed in earlier sections are historical
  snapshots. This block supersedes them.


## 2026-10-10 Stage 2.3A-2.2: compiled NeoForge owner/item join

The previous `COMPARATIVE_SOURCE_OWNER_ONLY_NEOFORGE_REFLECTION_RECHECK_PENDING`
status of the 11 attachment rows has been resolved by reading the
**original unmodified ZIP** generated by Java 21 / NeoForge 21.1.215
run 37988064055, artifact 11643813158, SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
All **35 source-reviewed class rows** now have
`registry_dispatch_evidence=REFLECTION_OWNER_VERIFIED`, with
the same method exact-signature scope as defined above. All
11 new attachment class rows now carry individually
verified `minecraft:` block IDs. 3 wall torch block IDs have
no direct registered item; their variant is chosen by the
3 standing `StandingAndWallBlockItem` entries.

Original 11-class source/reflect/item/Planet adapter evidence:
[FACE_ATTACHMENT_AUDIT addendum](PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md#2026-10-10-addendum--ci-artifact-exact-owner-and-item-reconciliation).
Scope totals **unchanged**: 35/241 source-reviewed, 206/241
pending; 81/1060 reviewed-class block IDs, 979/1060 pending.
Compiled nearest method declaration is **not** ASM INVOKE-site
or NeoForge-patched method-body verification. `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and `gameplay_acceptance`
remain REVIEW_PENDING.


## 2026-10-10 Stage 2.3A-2.3: non-FACING growth topology

[New source + compiled reflection audited 12-class family](PHASE2_STAGE3A_NONFACING_GROWTH_GRAPHS_1_21_1.md)
covers **12 block IDs that have no candidate orientation
property**, yet their state lifecycle depends on UP/DOWN,
tangent XZ, grow/survive/scheduled ticks, directional
head/body graph, water, direct bonemeal creation and
item-placed alternate states.

Exact total is now **47 SOURCE_REVIEWED_INTEGRATION_PENDING**
classes (**93** block IDs), with
**194 REVIEW_PENDING** classes (**967** block IDs);
**241** classes and **1060** registry IDs unchanged.
All 47 source-reviewed have an exact compiled NeoForge
reflection declaring owner join. **NeoForge patched-method
ASM, actual Planet adapter correctness and gameplay**
remain REVIEW_PENDING even for these 47.
5 of the new runtime variant blocks have no directly
registered BlockItem, including `bamboo_sapling`
which is authored by `BambooStalkBlock.getStateForPlacement`
on an ordinary bamboo item placement.
Earlier summaries' figures are historical snapshots;
these current totals supersede them.
