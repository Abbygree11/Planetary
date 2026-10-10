# Stage 3A-7.1 — exact NeoForge SculkSensor/CalibratedSculkSensor source owners and vibration pathways

**2026-10-10**, branch `2.0`; Minecraft **1.21.1**,
NeoForge **21.1.215**, Java **21**. One
bounded **source+original compiled declaration**
audit for exactly **two concrete registered BLOCK
classes / two BLOCK IDs**. Does NOT implement or
test local gravity interaction, signal, water
or patched-NeoForge method bodies.

## 1. Original 21.1.215 runtime registry evidence

Primary **unmodified original** source is CI
[run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, generated source
revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`.
Directly verified
`/mnt/data/phase2-neo1211-registry-census.zip`
SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`,
not a reconstructed web file.

Original `phase2-neo1211-block-registry.tsv`
**1060 registered BLOCK rows, 241 unique classes**;
`phase2-neo1211-item-registry.tsv` **1333 ITEM rows**,
and `phase2-neo1211-state-properties.tsv` **1712
state property rows**. This stage used only the
exact two BLOCK-class registry dispatch owners
and their property vocabulary; the separate
**exact `ITEM.placed_block` join remains
Stage 3A-7.2**, not yet accepted.

| Java concrete class (exact original) | Exact BLOCK ID | Inheritance hierarchy | Exact supported BlockState properties |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkSensorBlock` | `minecraft:sculk_sensor` | `SculkSensorBlock > BaseEntityBlock > Block > BlockBehaviour` | `power` integer 0–15; `sculk_sensor_phase` inactive/active/cooldown; `waterlogged` boolean |
| `net.minecraft.world.level.block.CalibratedSculkSensorBlock` | `minecraft:calibrated_sculk_sensor` | `CalibratedSculkSensorBlock > SculkSensorBlock > BaseEntityBlock > Block > BlockBehaviour` | same three, **plus** `facing` only north/east/south/west |
| **Total** | **2 unique BLOCK IDs** | **2 registered implementation classes** | Calibrated has one additional orientation candidate |

Five exact registered effective nearest declaring
owners from the original compiled 21.1.215
`effective_method_owners` column, selected by
**complete qualified parameter signature**,
not a name-only string match:

| Exact method signature (short arg names in table, fully qualified in original ZIP) | Plain `SculkSensorBlock` owner | `CalibratedSculkSensorBlock` owner |
|---|---|---|
| `getStateForPlacement(BlockPlaceContext)` | `SculkSensorBlock` | `CalibratedSculkSensorBlock` |
| `canSurvive(BlockState,LevelReader,BlockPos)` | `BlockBehaviour` | `BlockBehaviour` |
| `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)` | `SculkSensorBlock` | `SculkSensorBlock` |
| `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)` | `BlockBehaviour` | `BlockBehaviour` |
| `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)` | `Block` | `Block` |

Additional **exact compiled dispatch declarations**:
both classes inherit `SculkSensorBlock.onPlace`,
`SculkSensorBlock.getShape`,
`SculkSensorBlock.getFluidState`,
`SculkSensorBlock.tick(BlockState,ServerLevel,BlockPos,RandomSource)`,
and `BlockBehaviour.neighborChanged`.
The plain sensor inherits
`BlockBehaviour.rotate/mirror`;
the calibrated class overrides
`rotate/mirror` to change its `FACING`.
`getTicker`, `getSignal`,
`getDirectSignal`, `getAnalogOutputSignal`
and BE vibration callbacks are **additional
important Java sources**, not among the
original chosen five-method census.

**Crucial:** normal sensor is original ZIP
`has_orientation_candidate=false`,
audit code must **not** overlook it just
because it lacks `FACING`: its sound,
world-Y neighbor notifications, event
transport and `POWER` are still relevant.
Neither sensor has the rigid
`canSurvive` support override of
pressure plates.

## 2. Pinned comparative Minecraft 1.21.1 source graph

Revision
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).
The source is comparative unpatched vanilla
1.21.1 text, **not** proof of identical
NeoForge 21.1.215 patched ASM method bodies.

- [`SculkSensorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L43)
  — properties, placement, water, state machine,
  signal and neighbor writes.
- [`CalibratedSculkSensorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L21)
  — FACING, rotation, output-side suppression,
  BE type, active duration.
- [`SculkSensorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java#L23)
  — listener/data/user, NBT persistence, receive
  callback and distance signal.
- [`CalibratedSculkSensorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L14)
  — listener radius and redstone-backside
  frequency filtering.
- [`VibrationSystem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L36)
  — event filter, occlusion, selection,
  physical-distance travel, chunk tick gate,
  server delivery and event-user callback.
- [`BaseEntityBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseEntityBlock.java#L14)
  — nonregistered block parent,
  BlockEntity type ticker helper.

## 3. State authors and scheduled phase transitions

[`SculkSensorBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L82)
sets `WATERLOGGED` from clicked cell fluid.
[`CalibratedSculkSensorBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L60)
first calls it, then writes `FACING`
from `BlockPlaceContext.getHorizontalDirection()`
(four vanilla source-local possible enum values).
[`rotate/mirror`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L79)
mutate FACING in structure transformations.
Both start `SCULK_SENSOR_PHASE=INACTIVE`,
`POWER=0`.

[`SculkSensorBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L159)
schedules a WATER fluid tick when waterlogged;
[`getFluidState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L90)
returns water source. It does NOT author
vibration reception/phase transitions.
`WATERLOGGED` additionally silences
some click sounds but does **not** imply
that all vibration/redstone pathways stop.

**Phase graph** explicitly authored by
[`activate`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L251)
after vibration delivery:
`INACTIVE → ACTIVE`, `POWER=distance-strength`,
schedules `getActiveTicks()` block tick and
notifies own and physically
**world-below** neighbors.
[`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L96)
when ACTIVE calls
[`deactivate`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L238):
`ACTIVE → COOLDOWN`, POWER=0,
schedules **10-tick** cool-down check,
notifies both positions, and later
COOLDOWN → INACTIVE.

- Plain `SculkSensorBlock.getActiveTicks()`
  **30 ticks**.
- `CalibratedSculkSensorBlock.getActiveTicks()`
  overrides to **10 ticks**.
- Both then use **10-tick** COOLDOWN.

[`onPlace`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L133)
can reset preloaded `POWER>0` state to zero
if inserted new without scheduled block tick;
[`onRemove`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L145)
notifies neighbors when ACTIVE.
These are alternative multi-state
authors/notify pathways beyond
the initial placement function.
`randomTick` is NOT the mechanism
behind activation or scheduled phase ticks.

**Two different tick loops, neither redundant:**
`getTicker` ([plain](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L187),
[calibrated](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L47))
is server-only with exact matching
BlockEntityType (`SCULK_SENSOR` or
`CALIBRATED_SCULK_SENSOR`) and runs
`VibrationSystem.Ticker.tick`;
the **scheduled block tick** runs later
in `SculkSensorBlock.tick` to advance
ACTIVE/COOLDOWN/INACTIVE.
Vibration selection/arrival and
signal phase timing cannot be implemented
by only a single block scheduled tick.

## 4. Event and BlockEntity sender/receiver graph

[`SculkSensorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java#L23)
implements `GameEventListener.Provider<
VibrationSystem.Listener>` and
`VibrationSystem`, has an owned
`VibrationSystem.Data`, listener and
`VibrationSystem.User`. It persists
`last_vibration_frequency` plus listener
data through NBT codecs, and uses its
actual position as the `BlockPositionSource`.
Its [`VibrationUser`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java#L103)
listens within **8 blocks** and accepts
events only while the source BlockState
is INACTIVE, plus special self-placement/
destroy filtering. On receipt it persists
the event frequency and computes
`getRedstoneStrengthForDistance(distance,
listenerRadius)` and calls the block
`activate`.
It requests
`requiresAdjacentChunksToBeTicking=true`.

[`CalibratedSculkSensorBlockEntity.VibrationUser`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L27)
inherits the graph but increases radius to
**16 blocks**, and
[`canReceiveVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L41)
filters the incoming GameEvent frequency
when the measured backside redstone signal
is nonzero and mismatches the event.
[`getBackSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L47)
reads physical
`pos.relative(FACING.getOpposite())`
and asks the Level's
`getSignal(neighborPos, FACING.getOpposite())`.
This is **both** a physical neighbor lookup
**and** a query Direction. These two roles
are **not semantically identical** under a
Planet seam transformation.

[`CalibratedSculkSensorBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L66)
independently **suppresses redstone output**
on the API queried side that equals its
`FACING`, while the inherited
[`SculkSensorBlock.getDirectSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L223)
returns signal only for queried `UP`.
**Do not conflate calibrated sensor's
input back-face, output-suppression
FACING, and strong `UP` query.**
Redstone graph consumer integration is
Phase 7A, not completed with a
BlockState FACING rewrite.

[`VibrationSystem.Listener.handleGameEvent`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L225)
checks existing vibration, event tag and
entity/status validity, block state/user
acceptance, and
[`isOccluded`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L289)
ray checks from all six **physical
world-direction** offsets against blocks
tagged `OCCLUDES_VIBRATION_SIGNALS`.
The candidate selection
[`scheduleVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L276)
stores Euclidean `Vec3.distanceTo`.
[`VibrationSystem.Ticker.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L315)
selects, advances travel time, respawns
the traveling particle on reload and
[`receiveVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L399)
delivers events to the BE VibrationUser.
The default travel time is
`floor(distance)`, but the receipt callback's
redstone distance argument recomputes
block-to-block physical distance.
`areAdjacentChunksTicking`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L421)) checks a **physical
3×3 world XZ chunk area**, not six
cube-face local chunks.

The source itself computes vibration
distance and ray occlusion in the actual
**world-coordinate physics**, which should
not be forcibly rotated to local tangent
space. Whether special seam occlusion,
worldgen/reloaded chunks or mod collisions
require additional handling is **PENDING**
and is not inferred from this audit alone.

Additional independent entrypoint:
[`SculkSensorBlock.stepOn`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L114)
can force-schedule a `GameEvent.STEP` through
the BE listener even before an ordinary
world game-event dispatcher path.
[`tryResonateVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L273)
reads **six physical adjacent** positions
for `BlockTags.VIBRATION_RESONATORS`,
publishes resonance game events and sounds.
These alternatives must be included in
a later non-item-creator test/author audit.

## 5. Signal readers, BE output and rendering/water implications

[`SculkSensorBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L217)
returns `POWER` in all queried directions.
Inherited `getDirectSignal`
returns strength only for
`Direction.UP` query. For calibrated
variant the own `getSignal` overrides
the source direction matching `FACING`
to 0. Both
[`getAnalogOutputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L325)
query the BE's **last vibration
frequency while PHASE=ACTIVE**, not
necessarily the current redstone POWER,
so the comparator/analog path has a
different data owner than the BlockState.

[`getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L205)
is a canonical **8/16-high** shape,
not the event radius or vibration
occlusion AABB. On Planet faces, block
render/collision geometry and particle
animation may be local-gravity sensitive
even when physical vibration distance
is rightly world-Euclidean; separate
Phase 3/7A acceptance is needed.
`animateTick` ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L288))
uses world-Y and world XZ to add
particles; existing previously accepted
Planetary particle behavior does NOT
establish this new special sensor particle
path as accepted.

## 6. Mechanism boundaries and precise next work

| Component / source algorithm | Mechanism / next owner |
|---|---|
| Block state placement, four FACING values, getStateForPlacement and rotate/mirror | **Phase 2** `P01/P03/P08/P35` state frames, real item creators still task 3A-7.2 |
| Waterlogged fluid scheduling `updateShape` | **Phase 2/P37** block state, **Phase 5** actual fluid flow |
| `VibrationSystem.Listener`, selection/travel, adjacent chunk gate and BE ticker | **Phase 7A** physical event routing, server tick, stable BE identity |
| `getSignal/getDirectSignal/getAnalogOutputSignal`, calibrated filter neighbor | **Phase 7A**, query Direction and physical target distinct |
| `SculkSensorBlock.updateNeighbours`, `tryResonateVibration` physical six neighbors, state transitions | **Phase 2/7A**, support physical callbacks, phase timing |
| `SculkSensorBlock.getShape` & special `animateTick` | **Phase 3** rendering/collision/particles and existing particle regression |
| `SculkSensorBlockEntity` NBT listener state, save/reload, network listener registration | **BE/runtime** and **Phase 7A**; actual patched event binding pending |
| Structure/block-entity placement bypasses | **Phase 2 / Phase 8**, precise item and direct creator audit next 3A-7.2 |

**Validation examples for subsequent cards,
NOT tests run now:** two exact blocks on
all six faces, each of four calibrated FACING
values, each PHASE and POWER valid state,
waterlogged vs dry, server-only BE
ticker, 8 vs 16 radius, source GameEvent
STEP and manual stepOn alternative,
frequency filtered and unfiltered,
direct/weak/analog signal, activation
30 vs 10 and cooldown 10 ticks,
resonance physical neighbors,
world physical clip-ray occlusion,
listener save/load and adjacent world
chunks, vanilla non-Planet world control.
These are preliminary scenarios; full
seam/face acceptance matrix is task 3A-7.3.

## 7. Strict status after one microtask

This packet source+runtime-declaration reviewed
**2/241 concrete classes / 2/1060 registered
IDs** previously pending. Ledger totals:
**71/241**
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(**192/1060 BLOCK IDs**),
**170/241 `REVIEW_PENDING`**
(**868/1060 BLOCK IDs**). Included only
`minecraft:sculk_sensor` and
`minecraft:calibrated_sculk_sensor`.
`SculkShriekerBlock`, `SculkCatalystBlock`
and `LightningRodBlock` still
`REVIEW_PENDING`.

**All 241** `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance` columns still
`REVIEW_PENDING`. No production code
was changed and **no build or gameplay
test run** by this docs task.

**NEXT FIRST incomplete: Stage 3A-7.2**,
checkbox 2 on
[`02e-sculk-vibration-sensor-owners.md`](../phases/phase-02/02e-sculk-vibration-sensor-owners.md):
independently join **both original ITEM
`placed_block` records** and verify
all item creation/alternate BE,
vibration/filter and structure state
writers, no new class promotion without
an independent source audit. ONE
research commit, checkpoint, stop.
