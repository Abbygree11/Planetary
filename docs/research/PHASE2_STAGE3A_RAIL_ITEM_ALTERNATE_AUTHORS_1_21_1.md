# Stage 3A-5.2 — exact NeoForge rail ITEM creators and non-item rail state authors

**2026-10-10 · Planetary branch `2.0`** · Minecraft **1.21.1** /
NeoForge **21.1.215**, Java 21.

**One bounded research-only package:** no new concrete BLOCK
class source dispositions; the same three previously
source+reflection-reviewed classes / four registered BLOCK IDs
from [3A-5.1 rail hierarchy and topology audit](PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md).
The purpose is an independent ITEM-census join and alternate
writer/caller ownership audit. No Java modification,
NeoForge patched ASM verification, gameplay, client or CI run.

## 1. Exact four ITEM rows, joined by placed_block from original runtime census

Primary evidence: original immutable
[GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055)
artifact **11643813158**, source revision
`aa39572950a15403ea0a9003eefccf3bf6675ff7`.
The ZIP SHA-256 verified independently:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Parsed `phase2-neo1211-item-registry.tsv` (**1333 rows**),
`phase2-neo1211-block-registry.tsv` (**1060 rows**),
`phase2-neo1211-state-properties.tsv` (**1712 rows**).
Matched exact ITEM `placed_block` to registered BLOCK
`registry_id` (not inferred from names), checked
unique ITEM/BLOCK keys, class and `block_item`.

| Exact BLOCK ID | Original exact ITEM ID | ITEM Java concrete class | Block Java concrete class |
|---|---|---|---|
| `minecraft:rail` | `minecraft:rail` | `net.minecraft.world.item.BlockItem` | `RailBlock` |
| `minecraft:detector_rail` | `minecraft:detector_rail` | `net.minecraft.world.item.BlockItem` | `DetectorRailBlock` |
| `minecraft:powered_rail` | `minecraft:powered_rail` | `net.minecraft.world.item.BlockItem` | `PoweredRailBlock` |
| `minecraft:activator_rail` | `minecraft:activator_rail` | `net.minecraft.world.item.BlockItem` | `PoweredRailBlock` |
| **Total** | **4 distinct ITEM rows** | **4 ordinary BlockItems** | **3 BLOCK classes, 4 IDs** |

All 4 item rows have `block_item=true`, one exact
`placed_block`, class hierarchy `BlockItem>Item`,
and all seven **exact, original NeoForge 21.1.215 compiled
nearest declaring owners** below are `BlockItem`:

- `useOn(UseOnContext)`
- `place(BlockPlaceContext)`
- `updatePlacementContext(BlockPlaceContext)`
- `getPlacementState(BlockPlaceContext)`
- `placeBlock(BlockPlaceContext,BlockState)`
- `canPlace(BlockPlaceContext,BlockState)`
- `registerBlocks(Map,Item)`

The `use(Level,Player,InteractionHand)` owner is the
unrelated base `Item`, not `BlockItem`.
This nuance prevents falsely asserting that every ITEM method
is overridden by BlockItem. These are **declaring owners**,
not original patched bytecode INVOKE-sites, applied Mixins
or evidence that a placement actually completed in game.
There are NO string/dust-like `ItemNameBlockItem` aliases
in this four-rail cohort. Two distinct ordinary ITEM
instances map to two distinct BLOCK instances even though
both are `PoweredRailBlock` at the Java class level.

## 2. Item placement and optional post-placement component write

Comparative pinned Minecraft 1.21.1 source revision
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).
[`BlockItem.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L61)
calls `updatePlacementContext`, `getPlacementState`
(which calls the concrete block
`getStateForPlacement`), `placeBlock` to write
world `BlockState`, and **then** may call
[`updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158).
That applies an ITEM `DataComponents.BLOCK_STATE`
component when supplied and can rewrite
`RailShape`, `WATERLOGGED`, and where legal
`POWERED` on the already placed state.
Only **after** this stage comes block
`setPlacedBy`. We do **not** assert ordinary
survival items carry override components.

Actual family author:
[`BaseRailBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L173)
chooses initial `NORTH_SOUTH/EAST_WEST` from
vanilla player horizontal view and waterlogging
from the clicked fluid. The **indirect** placement
and update author
[`BaseRailBlock.onPlace`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L68)
invokes `updateState` / `updateDir`, which calls
`new RailState(...).place(...).getState()`.
[`RailState.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L281)
can write the current AND nearby rail BlockStates
through `connectTo`; these writes are not
`BlockItem.placeBlock` calls. An adapter handling
only the initial `BlockItem` state fails to
cover topology changes triggered by placement.

**Method ownership boundary:** `BaseRailBlock`
implements placement, survival and shape notifications;
`RailState` owns much of the connected-track
write algorithm. The original exact method
declaring owners are documented in
[3A-5.1](PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md).

## 3. Non-ITEM paths: trigger, writer and alternate consumer

| Trigger path | Actual author or reader | Consequence on Planet faces / phase |
|---|---|---|
| Rail placement, rail neighbor changes | `BaseRailBlock.onPlace/neighborChanged/updateDir` -> `RailState.place` + `connectTo` | Mutates this and neighbor `RailShape`; path uses global X/Z and world above/below. **Phase 2** state graph |
| Three-way junction under changed redstone source | `RailBlock.updateState` -> `RailState.countPotentialConnections/updatedir` | Signal changes **which turn** is selected; **Phase 2** track shape, **7A** power/update causality |
| Minecart enters detector | `DetectorRailBlock.entityInside` -> `checkPressed` | Queries `AbstractMinecart` entities in a detector AABB; writes `POWERED`, updates connected rails and neighbors; **Phase 7A**, collision/box **Phase 3** |
| Detector scheduled revisit | `DetectorRailBlock.tick` -> `checkPressed` | Re-samples cart after **20 ticks** while occupied; `getAnalogOutputSignal` reads command cart success count or container cart comparator strength |
| Powered or activator rail neighbor change | `BaseRailBlock.neighborChanged` -> polymorphic `PoweredRailBlock.updateState` | Writes `POWERED`; direct neighbor input or up-to-8 rail chain; distinct power networks for two actual block IDs; **Phase 7A** |
| Physical cart passes on rails | `AbstractMinecart.tick/moveAlongTrack` reads `RailShape` and `POWERED` | `Blocks.POWERED_RAIL` boosts/brakes, `Blocks.ACTIVATOR_RAIL` invokes `activateMinecart`; is **entity path**, not rail `BlockState` authored by item |
| Mineshaft generation (rail cells/chest cart) | `MineshaftPieces` `placeBlock/maybeGenerateBlock` with ready `Blocks.RAIL.defaultBlockState().setValue(SHAPE,...)` | **Direct worldgen BlockState write, no BlockItem creator**; generation's structure local-to-world rotation and support must be reviewed separately in Phase 8 |
| Structure template placement | `StructureTemplate.placeInWorld` writes transformed `BlockState` using `setBlock`, optionally `Block.updateFromNeighbourShapes` / `blockUpdated` | Can replay already serialized rail states without item, but **we did not prove any one particular template contains rails**; Phase 8 call path |
| Waterlog / fluid tick | `BaseRailBlock.updateShape` schedules water tick, `getFluidState` reads `WATERLOGGED` | **Phase 5** hydrodynamics; notification does not itself replace RailState graph resolver |

### 3.1 Detector: exact minecart search and analog signal chain

[`DetectorRailBlock.entityInside`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L60)
tests an entity entry, while the
[scheduled `tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L72)
calls `checkPressed` again. The latter
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L99)) queries
`AbstractMinecart` within a bounded detector
AABB; an occupancy change writes `POWERED` and
calls [`updatePowerToConnected`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L141)
to notify each `RailState.getConnections()`
neighbor as well as physically below and the
detector position. A minecart remaining there
causes a **20-tick** scheduled recheck.

[`getAnalogOutputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L172)
is a **separate reader**: while POWERED it first
checks `MinecartCommandBlock.getSuccessCount()`,
then inventories of matching container minecarts
(`AbstractContainerMenu.getRedstoneSignalFromContainer`);
otherwise returns 0. Correct detector POWERED
alone therefore does **not** guarantee correct
comparator/analog signal. This needs independent
Phase 7A/BE and minecart/entity acceptance tests.

[`getSearchBB`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L203)
constructs an XYZ axis-aligned local-space box
with small horizontal margins. **Whether/how
that query's physical box should rotate on
Planet faces must be verified**; simply
rotating visual rail geometry does not make
entity detection work. No definitive game
bug claimed from reading source.

### 3.2 Activated vs powered rails: same Java class, different effects

[`PoweredRailBlock.updateState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L185)
writes `POWERED` after testing neighbor signal
and calling `findPoweredRailSignal` in two
directions. [`findPoweredRailSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L39)
recurses to at most **8** rails, following
explicit world XYZ displacement and slope cases.
[`isSameRailWithPower`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L145)
requires `blockstate.is(this)` and compatible
`RailShape`, so the two block instances
(`minecraft:powered_rail` and
`minecraft:activator_rail`) do not
automatically share a propagation chain.

[`AbstractMinecart.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/vehicle/AbstractMinecart.java#L312)
separately reads the physical track below it
and recognizes the two specific registered
block instances. `Blocks.ACTIVATOR_RAIL` calls
`activateMinecart(...,POWERED)` while
[`moveAlongTrack`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/vehicle/AbstractMinecart.java#L440)
checks `Blocks.POWERED_RAIL` for motion
acceleration/braking. The base
`AbstractMinecart.activateMinecart` is an empty
hook ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/vehicle/AbstractMinecart.java#L417));
**concrete cart subclass responses must be
audited separately** before claiming activator
support. These paths prove that equating
`PoweredRailBlock` class with identical gameplay
effect would be wrong.

Actual cart motion source hardcodes
world Y, selected rail cell, RailShape exits and
movement physics. Its entire path (including
experimental 1.21.1 behavior configurations)
is **out of scope** for this rail-BLOCK item
and authors task, and **no minecart motion PASS**
is asserted.

### 3.3 Worldgen and structure placement really bypass BlockItem

Specific 1.21.1 reference is available:
[`MineshaftPieces.createChest`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/structures/MineshaftPieces.java#L437)
creates a ready `Blocks.RAIL` state, chooses
a `NORTH_SOUTH` or `EAST_WEST` shape, calls
structure `placeBlock` and adds a chest minecart;
[`MineShaftCorridor.postProcess`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/structures/MineshaftPieces.java#L544)
uses `maybeGenerateBlock` with explicit
`RailShape.NORTH_SOUTH`.
This is a **verified non-ITEM rail creator**,
not merely hypothetical “worldgen may place rails”.

[`StructureTemplate.placeInWorld`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L380)
has a generic `setBlock` path for transformed
states and may later run
[`Block.updateFromNeighbourShapes`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L418).
Template reading/writing does not imply an item
`BlockPlaceContext` or the same caller paths.
Do not claim specific rail templates exist without
checking the relevant structure data.

## 4. Preservation contracts / follow-up matrix (NOT EXECUTED)

1. Test 4 exact registered rail-item placements with
`BLOCK_STATE` absent and explicitly supplied:
straight/ascending `RailShape`, both values
of `WATERLOGGED`, and for powered variants
`POWERED`. Check actual item `getStateForPlacement`,
post-component and secondary `RailState` writes
in that order, not only the final screen render.
2. Put rails on all six faces and two adjacent
faces' shared edge; verify `RailState.place`
does not author states in wrong physical cells.
Check 3-way junction power changes.
3. For detector: cart at center/edge on each
surface, entry and exit, 20-tick timer,
analog output from command/container carts,
physical above/below neighbor notifications.
4. For powered + activator: parallel chains
independently spanning up to eight rail segments,
slopes and crossings; confirm different
cart behavior despite shared class implementation.
Do **not** assume a shared signal network.
5. Generate abandoned mineshaft segments
and rail chest carts; inspect worldgen direct
rail `RailShape`, support and transform handling
before player touches the track.
6. For structure templates, supply a fixture
containing 1–4 rail types, apply rotations/mirrors,
and check template block state + neighbor update
without item placement.
7. Runtime gate: verify *actual NeoForge 21.1.215*
modified class bytes/ASM, Mixin target reachability,
local support/collision boundaries and Phase 7A
port calls, then build and test a client/server.
No check above was run in this research turn.

## 5. Status and next first task

- Four exact BLOCK registry IDs / four ordinary ITEM
records, four `placed_block` joins, seven effective
declaring owner signatures each resolved to
`BlockItem`: original CI ZIP verified.
- Same **3/241** rail implementation class rows
remain `SOURCE_REVIEWED_INTEGRATION_PENDING`;
no further classes promoted by 3A-5.2.
- Full ledger **66/241 source+reflection reviewed**
(174/1060 BLOCK IDs), **175/241 REVIEW_PENDING**
(886/1060 BLOCK IDs).
`DaylightDetectorBlock` still `REVIEW_PENDING`.
All **241** ASM, Planet adapter and gameplay
acceptance columns remain `REVIEW_PENDING`.
- No production Java modified, no new CI compile,
no minecart/entity/structure test actually run.

**NEXT**: card
[`3A-5.3`](../phases/phase-02/02c-rail-owners.md)
source-local vs physical rail shape/slope/neighbor
chart and minecart cross-phase acceptance matrix;
one independently committed bounded microtask.
