# Phase 2 Stage 3A-6.2 — all 16 sensor ITEM creators and non-item state authors

**2026-10-10** · Planetary `2.0`, Minecraft **1.21.1** /
NeoForge **21.1.215**, Java 21. A single **source and
original compiled registry evidence** package. Three
BLOCK classes were already source+reflection reviewed
in [3A-6.1](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md).
**No additional class, runtime acceptance or gameplay
PASS is claimed here.**

## 1. Original untouched NeoForge registry exact ITEM → placed BLOCK join

Primary source is the original **unaltered** ZIP from
[GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, generated from commit
`aa39572950a15403ea0a9003eefccf3bf6675ff7`.
The source file `/mnt/data/phase2-neo1211-registry-census.zip`
was independently reopened and its SHA-256 verified:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
This path is local analysis provenance, **not** a
file to be loaded by the mod itself.

Original ZIP members: **1060 BLOCK rows** in
`phase2-neo1211-block-registry.tsv`,
**1333 ITEM rows** in
`phase2-neo1211-item-registry.tsv`,
**1712 state property rows** in
`phase2-neo1211-state-properties.tsv`.
Selected BLOCK rows by exact
`java_class` in `DaylightDetectorBlock`,
`PressurePlateBlock`, `WeightedPressurePlateBlock`.
For every one of **16 exact registered BLOCK IDs**,
looked up all ITEM registry records with
`item.placed_block == block.registry_id`.
**Every block has exactly ONE such item, and
all 16 item registry IDs are distinct**.
No guess based on a block suffix was used.

| Actual registered BLOCK ID | Original joined ITEM registry ID | Compiled ITEM concrete class | Target BLOCK class |
|---|---|---|---|
| `minecraft:acacia_pressure_plate` | `minecraft:acacia_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:bamboo_pressure_plate` | `minecraft:bamboo_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:birch_pressure_plate` | `minecraft:birch_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:cherry_pressure_plate` | `minecraft:cherry_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:crimson_pressure_plate` | `minecraft:crimson_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:dark_oak_pressure_plate` | `minecraft:dark_oak_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:daylight_detector` | `minecraft:daylight_detector` | `BlockItem` | `DaylightDetectorBlock` |
| `minecraft:heavy_weighted_pressure_plate` | `minecraft:heavy_weighted_pressure_plate` | `BlockItem` | `WeightedPressurePlateBlock` |
| `minecraft:jungle_pressure_plate` | `minecraft:jungle_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:light_weighted_pressure_plate` | `minecraft:light_weighted_pressure_plate` | `BlockItem` | `WeightedPressurePlateBlock` |
| `minecraft:mangrove_pressure_plate` | `minecraft:mangrove_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:oak_pressure_plate` | `minecraft:oak_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:polished_blackstone_pressure_plate` | `minecraft:polished_blackstone_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:spruce_pressure_plate` | `minecraft:spruce_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:stone_pressure_plate` | `minecraft:stone_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| `minecraft:warped_pressure_plate` | `minecraft:warped_pressure_plate` | `BlockItem` | `PressurePlateBlock` |
| **Total: 16** | **16 1:1 unique records** | **16 ordinary BlockItem** | **3 actual classes** |

All 16 ITEM records have:
- `block_item=true`;
- `java_class=net.minecraft.world.item.BlockItem`;
- `class_hierarchy=BlockItem>Item`;
- same-named exact `placed_block` and `registry_id`;
- **seven** original NeoForge compiled
  `effective_method_owners` all declaring
  `BlockItem` for the lifecycle methods:
  `useOn(UseOnContext)`,
  `place(BlockPlaceContext)`,
  `updatePlacementContext(BlockPlaceContext)`,
  `getPlacementState(BlockPlaceContext)`,
  `placeBlock(BlockPlaceContext,BlockState)`,
  `canPlace(BlockPlaceContext,BlockState)`,
  `registerBlocks(Map,Item)`.

In contrast, the separate `use(Level,Player,InteractionHand)`
method's nearest declaring owner is **`Item`**.
Hence it would be incorrect to claim every
method in this item family was declared by BlockItem.
There are **NO `ItemNameBlockItem` aliases**, unlike
the prior tripwire/redstone cohort.

**Independent diagnostic digest (cryptographic
SHA-256, source registry content only):**
`49c88b217ca7b0dd560634c2c8cb01560b265b29a85030ebda92b66f7596ebe9`.
Reproduction: join the 16 original ITEM rows,
sort by complete BLOCK `registry_id` ASCII,
concatenate for each block
`block.registry_id|item.registry_id|item.java_class|item.placed_block|item.effective_method_owners\n`
using literal `|` field separators and UTF-8.
Hash verified on the original ZIP; **not a
NeoForge game/ASM verification**.

## 2. Exact item lifecycle and post-placement state component

Pinned comparative 1.21.1 Java revision
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).

[`BlockItem.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L61)
resolves `BlockPlaceContext`,
calls `getPlacementState` and `canPlace`,
then [`placeBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L192)
(`Level.setBlock`) and reads the resulting
`BlockState` back before further processing.
If the resulting block is still the intended
block, the item path calls
[`updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158):
it reads an **optional**
`DataComponents.BLOCK_STATE` component,
applies legal property values to the placed
state and may write `setBlock(...,2)`
**after initial placement and before
`setPlacedBy`**.
It then considers block entity tag/components,
and calls `setPlacedBy`.

The original per-block property rows prove the
legal state vocabulary:

| Block family | Original allowed state properties | Important property |
|---|---|---|
| Daylight detector (1 ID) | `INVERTED` Boolean; `POWER` integer 0..15 | No FACING property; time and sky determine POWER |
| Ordinary pressure plates (13 IDs) | `POWERED` Boolean | Distinct item/material variants but same state vocabulary |
| Weighted pressure plates (2 IDs) | `POWER` integer 0..15 | Analog output, not Boolean POWERED |

The component is **optional**. The audit does not
assert that normal inventory items necessarily
ship with `DataComponents.BLOCK_STATE`,
or that `BlockItem.placeBlock` is the only
valid way a block can enter the world.

## 3. Off-item writers, lifecycle triggers and consumers

| Trigger/path | Actual semantic author/reader | BlockState effect or concern |
|---|---|---|
| Normal item placement | `BlockItem.place`, inherited `Block.getStateForPlacement` | Initial default detector/plate state, then optional `BLOCK_STATE` component application; not a guarantee about subsequent signal updates |
| Daylight periodic update | `DaylightDetectorBlock.getTicker → tickEntity → updateSignalStrength` | Reads `LightLayer.SKY`, `getSkyDarken`, sun angle, inverted; directly writes `POWER` by `Level.setBlock` when changed |
| Daylight player interaction without item | `DaylightDetectorBlock.useWithoutItem` | Toggles `INVERTED`, writes state and recomputes `POWER` **immediately**, outside item placement and periodic ticker |
| Plate entity contact | `BasePressurePlateBlock.entityInside → checkPressed`, polymorphic `getSignalStrength` | Reads world AABB and entity filters; writes `POWERED`/analog `POWER`, updates neighbors at **own cell and physical below**, emits game events |
| Plate scheduled recheck | `BasePressurePlateBlock.tick → checkPressed` | Calls the same state/signal writer; normal plates 20-tick pressed polling, weighted 10 ticks, enabling release when entities leave |
| Plate support loss/removal | `BasePressurePlateBlock.updateShape` / `onRemove` | Downward support invalidation produces AIR; removal of powered plate notifies neighboring cells |
| Generic structure placement | `StructureTemplate.placeInWorld` | Writes deserialized/rotated `BlockState` directly via `setBlock` and updates neighbor shapes as configured; **no BlockItem involved** |
| Generic world/command edit | Generic Level/ServerLevel block-state write callers | Also bypass the ITEM lifecycle; this audit did **not** establish a specific structure containing one of these sensor blocks or a particular command execution trace |

### 3.1 Daylight BlockEntity is not the POWER algorithm owner

[`DaylightDetectorBlock.getTicker`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L134)
returns a ticker on **server only**, only if
`dimensionType.hasSkyLight()`, and for the matching
BlockEntity type. Its static
[`tickEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L140)
runs signal update on `gameTime % 20 == 0`.
[`updateSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L65)
performs all light/time calculations and conditional
`setBlock(POWER)`.
[`useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L91)
is another direct property writer: cycles INVERTED
and recalculates POWER immediately.

[`DaylightDetectorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/DaylightDetectorBlockEntity.java#L5)
is a minimal typed storage object; it does **not**
calculate sunlight itself.
A blanket `randomTick` or BlockItem-only adapter
would miss these alternate state authors.
The meaning of side-face/underside daylight
is a **separate environmental design decision**,
not solved by mapping `Direction.UP`.

### 3.2 Pressure plate entity query is not the visual shape

[`BasePressurePlateBlock.entityInside`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L84)
calls `checkPressed` for an initially
unpowered plate.
[`getEntityCount`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L166)
gets world entities in `TOUCH_AABB.move(pos)`,
excluding spectators and entities ignoring block
triggers. This raw **world-XYZ** entity AABB
is distinct from
[`BasePressurePlateBlock.getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L40)
(the visibly pressed/released `VoxelShape`).
Rotating the rendered/selection shape does **not**
automatically rotate `TOUCH_AABB.move(pos)`.

[`checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L97)
calls the subclass's actual signal strength,
writes `BlockState` and sends neighbor notifications.
[`PressurePlateBlock.getSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PressurePlateBlock.java#L50)
uses `BlockSetType` sensitivity EVERYONE/EVERYTHING
vs MOBS (see original source enum `EVERYTHING`
or `MOBS`, not an ITEM-vs-block distinction):
result is Boolean `POWERED`, 0 or 15.
[`WeightedPressurePlateBlock.getSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java#L45)
returns analog ceil-scaled entity count up to
the actual maxWeight for its block instance.
Don't collapse the two into the same power model.
The actual maxWeight values per ID must be
verified through block constructor registration
or dedicated runtime test before using them
as acceptance constants.

[`updateNeighbours`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L141)
notifies own position and world `pos.below()`,
whereas `getDirectSignal` from
[`BasePressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L155)
is gated to its queried `Direction.UP`.
The API signal query-side convention must be
verified separately from the physical neighbor
update direction; both are Phase 7A integration
and Phase 2 local-support concerns.

### 3.3 Direct structure author: generic capability, not a specific sensor structure claim

[`StructureTemplate.placeInWorld`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L303)
applies mirrored/rotated BlockState and invokes
`LevelAccessor.setBlock` directly without
`BlockItem.place`, with an optional
[`Block.updateFromNeighbourShapes`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L418)
pass. This **proves a generic direct-state author
mechanism**, but we did **not** locate/verify
a specific worldgen feature or builtin template
containing a daylight detector or pressure plate.
Hence Phase 8 must separately check actual
data/templates before claiming a live sensor
worldgen pathway.

## 4. Boundaries, future acceptance and next first task

Future tests (not run): all 16 exact item placements,
ordinary item states vs `DataComponents.BLOCK_STATE`
overrides, six-face physical support, direct
structure-template fixture with a sensor block,
entity physical AABB on side/bottom face,
20/10 tick count-based plate signal output,
daylight normal/inverted in sky-light and non-sky
dimensions, comparisons between world sky field
and gravity-local face, API query-side/getDirectSignal,
and non-Planet-world regression.

**Not accepted in this packet:** NeoForge 21.1.215
patched ASM/INVOKE method body semantics,
Mixin reachability, Planetary integration,
visual shape/physical hitbox, input/side power,
build and client/server gameplay.
All these remain `REVIEW_PENDING`.

**Statuses unchanged by 3A-6.2:**
**69/241** source+reflection reviewed classes
(190/1060 BLOCK IDs);
**172/241 `REVIEW_PENDING`** (870 IDs).
Three current sensor classes already reviewed
in 3A-6.1; no new ones promoted.
All **241** class rows retain each of
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance` = `REVIEW_PENDING`.
Sculk and LightningRod remain source-unreviewed.

**NEXT first open independent microtask 3A-6.3:**
position-only local frame vs physical support,
sky bright/sun angle semantics, pressure-plate
entity box, signal queried direction, BE and
scheduled tick, detailed six-face/seam/corner
tests and phase boundaries. One bounded commit,
then stop.
