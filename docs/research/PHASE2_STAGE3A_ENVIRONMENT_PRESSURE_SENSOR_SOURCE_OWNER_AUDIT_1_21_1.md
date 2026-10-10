# Stage 3A-6.1 — original NeoForge sensor owner declarations and daylight/plate behavior

**2026-10-10**, Planetary branch `2.0`,
Minecraft **1.21.1**, NeoForge **21.1.215**, Java 21.

**One independently committable owner-family source audit**:
3 previously source-pending concrete Java classes,
**16 distinct registered BLOCK IDs**. All five
nearest method-declaring owners per class were
resolved directly from original compiled NeoForge
reflection by **complete method signature**;
mechanism analysis is from a pinned comparative
Minecraft 1.21.1 source snapshot.
No code changes or gameplay/NeoForge patched
bytecode verification in this packet.

## 1. Primary original runtime census and exact owner tuples

Original untouched [CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, original source
`aa39572950a15403ea0a9003eefccf3bf6675ff7`,
ZIP SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.

Parsed `phase2-neo1211-block-registry.tsv`
(**1060 IDs, 241 distinct classes**);
`phase2-neo1211-item-registry.tsv` (**1333 ITEM rows**);
`phase2-neo1211-state-properties.tsv`
(**1712 property rows**). The exact block ID
and class/dispatch identity are from original
`registry_id`, `java_class`, `class_hierarchy`,
`declared_property_names` and
`effective_method_owners` records, **not
guessed by suffix**.

| Concrete class | Exact registered block IDs | Property names | Five exact compiled declaring owners: placement / survive / updateShape / randomTick / setPlacedBy |
|---|---|---|---|
| `DaylightDetectorBlock` | `minecraft:daylight_detector` | `inverted,power` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` |
| `PressurePlateBlock` | `minecraft:acacia_pressure_plate`, `minecraft:bamboo_pressure_plate`, `minecraft:birch_pressure_plate`, `minecraft:cherry_pressure_plate`, `minecraft:crimson_pressure_plate`, `minecraft:dark_oak_pressure_plate`, `minecraft:jungle_pressure_plate`, `minecraft:mangrove_pressure_plate`, `minecraft:oak_pressure_plate`, `minecraft:polished_blackstone_pressure_plate`, `minecraft:spruce_pressure_plate`, `minecraft:stone_pressure_plate`, `minecraft:warped_pressure_plate` | `powered` | `Block / BasePressurePlateBlock / BasePressurePlateBlock / BlockBehaviour / Block` |
| `WeightedPressurePlateBlock` | `minecraft:heavy_weighted_pressure_plate`, `minecraft:light_weighted_pressure_plate` | `power` | `Block / BasePressurePlateBlock / BasePressurePlateBlock / BlockBehaviour / Block` |
| **TOTAL** | **16 unique BLOCK IDs: 1 + 13 + 2** | | **3 concrete classes** |

Original class hierarchies:

- `DaylightDetectorBlock > BaseEntityBlock > Block > BlockBehaviour`
- `PressurePlateBlock > BasePressurePlateBlock > Block > BlockBehaviour`
- `WeightedPressurePlateBlock > BasePressurePlateBlock > Block > BlockBehaviour`

Exact checked method argument signatures:
`getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`,
`canSurvive(BlockState,LevelReader,BlockPos)`,
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`,
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`,
`setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`.
All are full `net.minecraft...` qualified types
in actual source ZIP; abbreviations above are
for human readability.

**Additional compiled owner evidence** from the same
reflection corpus:

| Signature path | DaylightDetectorBlock | Both plate subclasses |
|---|---|---|
| `neighborChanged(...)` | `BlockBehaviour` | `BlockBehaviour` |
| `onPlace(...)` | `BlockBehaviour` | `BlockBehaviour` |
| `getShape(...)` | `DaylightDetectorBlock` | `BasePressurePlateBlock` |
| `tick(BlockState,ServerLevel,BlockPos,RandomSource)` | `BlockBehaviour` | `BasePressurePlateBlock` |
| `useWithoutItem(...)` | `DaylightDetectorBlock` | `BlockBehaviour` |
| `getFluidState(...)` | `BlockBehaviour` | `BlockBehaviour` |

`DaylightDetectorBlock.getTicker` and its
private static `tickEntity` are **outside
this five-method reflection census**. Their
Java call chain is separately supported by
pinned source below. Do NOT equate scheduled
`BasePressurePlateBlock.tick` with daylight's
`BlockEntityTicker`, and do NOT infer
`randomTick` processing from the unimportant
inherited `BlockBehaviour.randomTick`.

## 2. Original 1.21.1 comparative source: daylight sensor

Pinned source:
[`DaylightDetectorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L28),
[`BaseEntityBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseEntityBlock.java#L14),
[`DaylightDetectorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/DaylightDetectorBlockEntity.java#L4),
revision
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).

### Sky input and POWER state writer

[`updateSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L65)
reads `Level.getBrightness(LightLayer.SKY,pos)`
minus `getSkyDarken()`, and `getSunAngle(1.0F)`.
For normal mode it applies a sun-angle cosine
adjustment when brightness is positive; for
`INVERTED=true` it instead uses `15 - i`.
It clamps to **0–15** and writes
`BlockState.POWER` with `Level.setBlock`
only if the power actually changed.

**This is NOT a direction/FACING property.**
Its input is the world's sky-light evaluation
at a physical BlockPos and world time/sun angle.
Gravity-local `UP` does not make
`LightLayer.SKY` point toward a new sky.
A future design must explicitly decide what
“daylight” means for the sides and underside
of a cubic planet; simply rotating
`Direction.UP` is not a verified solution.

### Actual tick lifecycle: BlockEntity, not randomTick

[`getTicker`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L134)
returns a server-side BE ticker **only when
the current dimension has sky light** and the
block entity type matches
`BlockEntityType.DAYLIGHT_DETECTOR`.
[`tickEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L140)
calls `updateSignalStrength` whenever
`getGameTime()%20==0`.

[`DaylightDetectorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/DaylightDetectorBlockEntity.java#L5)
is a minimal BlockEntity with a fixed type;
**it does not implement the daylight calculation**.
The calculation and time gate live in the
Block class's static ticker callback.
`BaseEntityBlock.createTickerHelper`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseEntityBlock.java#L42))
checks the exact block entity type.
A blanket block-state random-tick adapter
would entirely miss this mechanism.

[`useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L91)
cycles `INVERTED` on player interaction when
permitted, writes the new state, emits
`GameEvent.BLOCK_CHANGE` and **immediately**
recalculates POWER on server.
This is an alternative state author/trigger
independent of item placement and the
20-tick BE heartbeat.

[`getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L60)
returns its stored POWER regardless of the
queried direction; it does not define a special
up-only `getDirectSignal` override.
[`getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L48)
is a flat **6/16-high** canonical slab shape,
whose physical interaction/render rotation
must be assessed separately in Phase 3.
The class does NOT override
`canSurvive` or `updateShape` from the
ordinary `BlockBehaviour` baseline:
**do not invent a rigid underside support
requirement in that block's class semantics**.

## 3. Original 1.21.1 comparative source: common pressure plate owner

[`BasePressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L25)
declares the actual entity sensing,
state change, neighbor notification,
support and scheduled-tick mechanisms.
Subclasses supply `getSignalStrength`,
`getSignalForState`, `setSignalForState`.

### Support and neighbor lifecycle

[`canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L66)
requires the block **below** to support a
rigid block OR its center upward face to
provide support; `updateShape`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L58))
replaces the pressure plate with AIR if
the relevant **DOWN** neighbor update
finds missing support.

That comparison is a source-local DOWN support
contract for Planetary on side/underside faces:
the actual supporting `BlockPos` and the
support target's local face need resolution
per physical block, not simply
`pos.below()` in global Y.

[`getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L41)
returns pressed **0.5/16-high** and released
**1/16-high** shapes, with X/Z inset.
This thin visual/selection shape is **NOT
the detection box**.

### Entity collision and the independent detection AABB

[`entityInside`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L84)
only initiates `checkPressed` on the
server when the prior signal was zero.
[`checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L97)
calls subclass
`getSignalStrength(Level,BlockPos)`,
writes a new block state if changed, notifies
neighbors and changes events/sound.
The entity enumeration uses
[`getEntityCount`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L166)
with the physical `TOUCH_AABB`:
**X/Z 0.0625…0.9375,
Y 0…0.25** relative to world block,
excluding spectators and entities which
ignore block triggers. In comparison,
`getShape` is not the same spatial region.

On ±X/±Z or −Y Planetary faces, a
rotated model/selection VoxelShape alone does
not necessarily rotate
`TOUCH_AABB.move(pos)` used by a level entity
query. This distinction must be tested
before claiming physical pressure plate
trigger acceptance.

[`updateNeighbours`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L142)
notifies at the plate's own position and
**global `pos.below()`**. Local-down and
world-physical notification positions must
not be conflated at cube seams.
[`onRemove`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L126)
notifies while a powered plate is being replaced.

[`getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L149)
returns subclass strength, whereas
[`getDirectSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L155)
returns power **only for API query
`Direction.UP`**. The API query-side
direction is not interchangeable with the
physical notification vector.
Its target side and caller semantics need
independent NeoForge 21.1.215 patched-code
and Phase 7A verification.

### Scheduled plate ticks and output state

[`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L73)
only rechecks the plate while current
strength is positive. `checkPressed`
schedules another tick while occupied
and writes changes to the state before
sending neighbor updates.
An entity leaving does not require an
`entityInside` callback to release the
plate: the scheduled check handles
deactivation.

[`PressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PressurePlateBlock.java#L17)
holds Boolean `POWERED` and converts
`true→15`, `false→0`.
[`getSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PressurePlateBlock.java#L50)
uses `BlockSetType.pressurePlateSensitivity`:
`EVERYTHING` examines Entity, while
`MOBS` examines LivingEntity; exact
type mapping is per registered BlockSetType,
**not guessed solely from the ID string**.
It inherits a **20-tick** pressed recheck.

[`WeightedPressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java#L18)
holds integer `POWER` (0–15), not Boolean
`POWERED`. [`getSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java#L45)
uses
`ceil(15 * min(entityCount,maxWeight)/maxWeight)`
when occupied, so more entities can produce
higher analog power; capped by the block
instance's `maxWeight`.
[`getPressedTime`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java#L73)
overrides the recheck interval to **10 ticks**.
Actual heavy/light `maxWeight` construction
parameters require later verified registry
creator evidence; this packet does not assert
specific values.

## 4. Ownership chart and phase dependency consequences

| Source author / reader | Actual data or callback | Further integration owner |
|---|---|---|
| `Block.getStateForPlacement` | All three initial states; no sensor-specific placement override | Phase 2 placement and item bypasses, later task 3A-6.2 |
| `BasePressurePlateBlock.canSurvive/updateShape` | Support position/local sturdy face; DOWN invalidation | Phase 2 source/target position+frame |
| `BasePressurePlateBlock.entityInside/getEntityCount` | Physical AABB intersecting entities; entity filters | Phase 3 collision/query; Phase 7A causal trigger |
| `BasePressurePlateBlock.tick/checkPressed/updateNeighbours` | POWERED or POWER, scheduled priorities and physically below notifications | Phase 2/7A; not randomTick |
| `DaylightDetectorBlock.getTicker/tickEntity` | 20 game-tick daylight SKY query + POWER author | BlockEntity lifecycle, Phase 7A/world light policy |
| `DaylightDetectorBlock.useWithoutItem` | INVERTED toggle and immediate signal recalculation | Player interaction + state writer |
| `DaylightDetectorBlock/plate getSignal/getDirectSignal` | Side-dependent vs all-side output query conventions | Phase 7A/NeoForge getSignal consumer checks |
| `DaylightDetectorBlock.getShape/BasePressurePlateBlock.getShape` | Local canonical model/selection shape dimensions | Phase 3 rendered and physical geometry |
| `StructureTemplate/BlockItem` | Direct/serialized BlockState authors | Phase 2/8, further exact ITEM audit needed |

**Do not globally replace vanilla Directions.**
Distinct semantic values:
source local block support DOWN,
physical adjacent support cell,
target block's canonical inward face,
entity detection AABB orientation,
API signal queried `Direction`, and
sky-light evaluation world environment.
Existing Planetary `PlanetBlockStateFrame`,
`PlanetBlockNeighborQuery`,
`PlanetBlockSupportQuery`,
`PlanetBlockShapeRuntime` supply partial
infrastructure but have not been proven
to fix these pressure plate/light-sensor
behaviors. Exact seam policy and acceptance
belongs in later task **3A-6.3**.

## 5. Acceptance gates and packet status

**Confirmed in this packet:**
original compiled registry and 5-argument-signature
nearest declaring owners for the 3 classes /
16 exact registered IDs, pinned comparative
Java algorithm owners and alternate non-item
signal writers.

**NOT confirmed:** compiled NeoForge patched
method *bodies* or Mixin/ASM reachability,
original exact ITEM `placed_block` creators
(the next card subtask), real support/entity AABB
rotation, sky/daylight design on side faces,
power-signaling causality, runtime compiled
application or client/server gameplay.
All integration and gameplay verdicts
therefore remain `REVIEW_PENDING`.

Ledger before: **66/241** source+reflection
reviewed (174 registered BLOCK IDs),
**175/241** source `REVIEW_PENDING`
(886 registered IDs). This packet adds
**3 source+reflection reviewed classes / 16 IDs**.

Ledger after: **69/241**
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(190/1060 registered BLOCK IDs) and
**172/241 `REVIEW_PENDING`**
(870/1060 registered IDs). All 241 ASM /
Planet adapter / gameplay acceptance
fields remain `REVIEW_PENDING`.
Sculk/vibration and lightning strike owners
remain source-pending.

**NEXT FIRST unchecked:** Stage **3A-6.2**,
card checkbox 2: independently join all
16 exact registered `placed_block` ITEM
rows from original 1333-item census and
trace direct non-item world placement,
`DataComponents.BLOCK_STATE`, BE/sky/entity
readers/writers. One separately committed
bounded research task, no gameplay PASS.


## 2026-10-10 Stage 3A-6.2 exact ITEM source plus alternate authors

[Original 16 placed_block actual ITEM
records and source writer audit](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
original ZIP from NeoForge 21.1.215, 1333
ITEM rows and all 16 selected BLOCK IDs,
one exact 1:1 `placed_block` link per ID;
16 concrete `BlockItem`, seven placement
method owners each all BlockItem, unrelated
`Item.use` inherited from Item.
Optional `DataComponents.BLOCK_STATE` may apply
legal Power/Powered/Inverted after initial
`placeBlock` before `setPlacedBy`. Other authors:
DaylightDetectorBlock BE 20-game-time ticker
using SKY brightness and INVERTED player use,
BasePressurePlateBlock entityInside/TOUCH_AABB
and scheduled tick 20/10 for Boolean POWERED or
analog POWER, support loss and onRemove, generic
StructureTemplate directly sets block states
without item placement. No specific structures
containing sensors verified and no game acceptance.
Same **69 reviewed classes (190 IDs),
172 pending (870 IDs)**, all NeoForge patched
ASM/Planet/gameplay gates pending.
NEXT 3A-6.3 physical/local frame and
multi-face acceptance matrix.
