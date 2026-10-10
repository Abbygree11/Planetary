# Phase 2 Stage 3A-7.2 — sculk and calibrated-sculk ITEM creators and alternate state authors

**2026-10-10**, branch `2.0`, Minecraft **1.21.1**
NeoForge **21.1.215** / Java 21.
One bounded evidence-only research package:
**2 actual registered BLOCK IDs**, **2 exact
ITEM→BLOCK source creators**, and **off-item
BlockState / BlockEntity / GameEvent authors**.
The two concrete sensor Java BLOCK classes were
already source+compiled declaration reviewed by
[Stage 3A-7.1](PHASE2_STAGE3A_SCULK_VIBRATION_SOURCE_OWNER_AUDIT_1_21_1.md).
No additional class promotion, NeoForge patched-ASM
verification, Planetary fix or gameplay acceptance.

## 1. Original immutable runtime census: exact ITEM.placed_block join

Directly reopened original `/mnt/data/phase2-neo1211-registry-census.zip`,
GitHub Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, built at repository
revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`.
Exact ZIP SHA-256:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.

Unmodified source members read as actual tabular data:
- `phase2-neo1211-block-registry.tsv`:
  **1060** registered BLOCK IDs and 241 unique
  concrete `java_class` values;
- `phase2-neo1211-item-registry.tsv`:
  **1333** registered ITEM IDs;
- `phase2-neo1211-state-properties.tsv`:
  **1712** property rows.

The join key was **`ITEM.placed_block == BLOCK.registry_id`**
for all real block-registry IDs whose exact
`BLOCK.java_class` is one of
`SculkSensorBlock`, `CalibratedSculkSensorBlock`.
The result is exactly **one** ITEM row per BLOCK ID,
**two** unique ITEM rows overall, with no
item-type inference from suffix or human-readable name.

| Original actual BLOCK registry ID | Actual ITEM registry ID where `placed_block` matches | Original ITEM concrete class | Original BLOCK concrete class |
|---|---|---|---|
| `minecraft:sculk_sensor` | `minecraft:sculk_sensor` | `net.minecraft.world.item.BlockItem` | `net.minecraft.world.level.block.SculkSensorBlock` |
| `minecraft:calibrated_sculk_sensor` | `minecraft:calibrated_sculk_sensor` | `net.minecraft.world.item.BlockItem` | `net.minecraft.world.level.block.CalibratedSculkSensorBlock` |

**Both original ITEM records** have
`block_item=true`, `class_hierarchy=BlockItem>Item`,
and `audit_disposition=BLOCKITEM_CREATION_REVIEW_PENDING`
in the original **census** itself: that census
flag means the CI artifact enumerated registered
types but did not accept their item gameplay.
The report audits source creators; it does
**not** rewrite the historical ZIP flag.

Original compiled `ITEM.effective_method_owners`
on both records was independently checked by
**full argument signatures**:

| ITEM method | Declaring owner in each of BOTH original 21.1.215 ITEM records |
|---|---|
| `useOn(UseOnContext)` | `BlockItem` |
| `place(BlockPlaceContext)` | `BlockItem` |
| `updatePlacementContext(BlockPlaceContext)` | `BlockItem` |
| `getPlacementState(BlockPlaceContext)` | `BlockItem` |
| `placeBlock(BlockPlaceContext,BlockState)` | `BlockItem` |
| `canPlace(BlockPlaceContext,BlockState)` | `BlockItem` |
| `registerBlocks(Map,Item)` | `BlockItem` |
| **separate** `use(Level,Player,InteractionHand)` | **`Item`** |

This is **7/7** placing/lifecycle owner declarations
in `BlockItem` per sensor, **not 8/8**.
There are no `ItemNameBlockItem` subclasses
or special `BlockItem` subclasses involved
in these **two** selected registry entries.

**Reproducibility hash** generated directly from
the original ZIP, SHA-256:
`6225d8898725346b8f37d34d97d64a4a7a86a17a5cf39e02d339f2bc0972fee5`.
Canonical hash input (UTF-8) sorts by **full
BLOCK registry ID ascending**. For each matching
BLOCK, append exactly
`BLOCK.registry_id|ITEM.registry_id|ITEM.java_class|ITEM.placed_block|ITEM.effective_method_owners\n`,
with the **unmodified original**
`effective_method_owners` string and trailing
newline. Hash proves faithful reproduction
of these two selected source records, **not**
runtime game compatibility.

## 2. Ordinary BlockItem initial placement is only one author

Pinned original comparative Minecraft 1.21.1 Java
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).

[`BlockItem.useOn/place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L45)
resolves `BlockPlaceContext`, checks placement
and obstruction, calls
[`getPlacementState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L151)
which dispatches **concrete block**
`getStateForPlacement`, then
[`placeBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L192)
via `Level.setBlock`. Therefore
the **BLOCK** classes, not the ITEM
classes, own these initial state details:

- [`SculkSensorBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L82)
  sets `WATERLOGGED` from the physical target
  cell fluid and defaults to `INACTIVE`, `POWER=0`.
  It has **no `FACING` BlockState property**.
- [`CalibratedSculkSensorBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L60)
  delegates to base sensor fluid placement and
  adds a four-value `HORIZONTAL_FACING`
  from the placement context.

After initial placement, `BlockItem.place`
reads the resulting block state and invokes
[`updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158)
**only when appropriate and with an optional
`DataComponents.BLOCK_STATE`**.
This component can set any *legal* properties
which are present, e.g. `POWER`,
`SCULK_SENSOR_PHASE`, `WATERLOGGED`, and
for calibrated only, `FACING`.
The component is optional; no ordinary
inventory item is asserted to include it
by default.

The same ITEM path can apply
`DataComponents.BLOCK_ENTITY_DATA`
(subject to vanilla permissions and validity)
and `updateBlockEntityComponents`
**before** `setPlacedBy`.
It is essential not to assume the source
BlockState is the only state of the just-placed
sensor when it begins receiving vibration.
Component rewriting may leave an unusual
`POWER/PHASE` combination whose scheduled
tick/checkpoint behavior must be tested.
No claim that such a combination is accepted
in client gameplay has been made.

## 3. State changes without BlockItem: full writer/reader separation

| Physical source event / non-item path | Declaring Java semantic owner | Which state or other payload is changed |
|---|---|---|
| Physical game-event emission from movement, entity or block action | `ServerLevel` game-event dispatch / `VibrationSystem.Listener.handleGameEvent` | **Receives and filters** event and physical Vec3 candidate; this is *not* a placement call or immediately a BlockState POWER write |
| Listener stores candidate and transports it over time | `VibrationSystem.Listener.scheduleVibration` + `VibrationSystem.Ticker.tick` | `VibrationSystem.Data` candidate/current vibration/travel ticks; server BE ticker, not scheduled block tick |
| Event delivered at sensor after travel | `SculkSensorBlockEntity.VibrationUser.onReceiveVibration` | Sets **BE.lastVibrationFrequency**, computes distance-based redstone strength and invokes `SculkSensorBlock.activate` |
| `INACTIVE → ACTIVE` transition | `SculkSensorBlock.activate` | `BlockState.PHASE=ACTIVE`, `POWER=strength`, physical world callbacks and a *new* scheduled block tick (30 plain / 10 calibrated active ticks) |
| `ACTIVE → COOLDOWN` transition | `SculkSensorBlock.tick → deactivate` | `PHASE=COOLDOWN`, `POWER=0`, schedules 10-tick block cooldown and neighbor updates |
| `COOLDOWN → INACTIVE` | `SculkSensorBlock.tick` | `PHASE=INACTIVE`; not caused by item placement |
| Placement/wrapper loads block with unexpected active power | `SculkSensorBlock.onPlace` | When replacing a different block, server may clear `POWER>0` if no matching scheduled block tick; separate state writer and tick coherence issue |
| Breaking/replacing an ACTIVE sensor | `SculkSensorBlock.onRemove` | Notifies redstone graph; doesn't require item creation |
| Entity contact directly on sensor | `SculkSensorBlock.stepOn` | Can force-schedule `GameEvent.STEP` into BE's vibration listener (non-item event path) |
| Active sensor near resonator | `SculkSensorBlock.tryResonateVibration` | Checks **all six physical adjacent** directions and emits frequency-dependent resonance `GameEvent`; this is an event **author**, not a claim of neighboring BlockState mutation |
| Water state and neighbor callback | `SculkSensorBlock.updateShape`, `getFluidState` | For `WATERLOGGED`, schedules water fluid tick; does not directly author vibration/POWER |
| Calibrated filter input | `CalibratedSculkSensorBlockEntity.VibrationUser.getBackSignal` | **Reads**, not writes, `Level.getSignal` at physical `pos.relative(FACING.getOpposite())`, with that queried direction |
| Calibrated redstone output | `CalibratedSculkSensorBlock.getSignal` | **Reads** current POWER, suppresses output for query `Direction==FACING`; does not change stored state |
| Comparator / analog read | `SculkSensorBlock.getAnalogOutputSignal` | **Reads** BE.lastVibrationFrequency *only while PHASE=ACTIVE*, a different data source than BlockState POWER |
| Generic copied/rotated/loaded BlockState | `StructureTemplate.placeInWorld` and general Level block state writes | Can directly write an entire legal BlockState without entering `BlockItem.place`, with separate BE/neighbor processing |

### 3.1 Exact BE writer path and frequency data persistence

[`SculkSensorBlockEntity.VibrationUser.onReceiveVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java#L144)
is the live **frequency and strength author**:
`setLastVibrationFrequency(getGameEventFrequency(...))`,
`getRedstoneStrengthForDistance(distance, getListenerRadius())`,
then `SculkSensorBlock.activate(...)`.
The concrete active-state write follows only after
BE acceptance and event transport.
[`SculkSensorBlockEntity.loadAdditional/saveAdditional`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java#L51)
also persists BE `last_vibration_frequency`
and serialized `listener` vibration data.
This makes BE reload and chunk load/unload a
distinct lifecycle worth future verification.

[`VibrationSystem.Listener.handleGameEvent`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L225)
selects only valid/not-occluded game events;
[`Ticker.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L315)
advances vibration travel and invokes the User.
The real source checks the receiving BE's
listener state/radius and physical-world
distance/occlusion. Merely rotating calibrated
sensor FACING does **not** establish
end-to-end vibration acceptance across a
cube edge or loaded chunk boundary.

### 3.2 Calibrated backside input and output are distinct reader sites

[`CalibratedSculkSensorBlockEntity.VibrationUser.canReceiveVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L41)
first measures the **opposite-`FACING`
neighbor** with
[`getBackSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L47).
If its level signal is nonzero, it accepts
only a matching event frequency; with
zero input it accepts any otherwise eligible
frequency. This query reads a *physical*
neighbor plus an API `Direction`, **not
simply one stored POWER bit**.

[`CalibratedSculkSensorBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L66)
independently suppresses outgoing weak signal
on the queried `Direction==FACING`.
Inherited `SculkSensorBlock.getDirectSignal`
requires queried `Direction.UP`.
These two output side conditions and the
BE's opposite-side input must be charted
separately. **Phase 7A** owns actual
redstone consumer/neighbor integration,
while Phase 2 owns canonical four-value
BlockState FACING authoring.

### 3.3 Generic structure-template direct state writer only

[`StructureTemplate.placeInWorld`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L300)
can deserialize/rotate/mirror and
`LevelAccessor.setBlock` a state directly,
without `BlockItem`; may load BlockEntity
data and perform neighbor-state updates.
This demonstrates an **alternate generic
engine author**, including potential direct
creation of either sensor in a deliberately
constructed template. It does **NOT** prove
the shipped vanilla worldgen templates include
these sensors or that a specific vanilla
worldgen generation call creates one.
Commands/other mods and programmatic
block writes are also generic bypasses,
not audited concrete sensor instances.
**Phase 8** must verify particular structure
source assets separately.

## 4. Tests to define in task 3A-7.3 (NOT executed)

Suggested future fixtures, not runtime results:
both registered items on all six faces,
calibrated four local FACING options,
item `DataComponents.BLOCK_STATE` override
of PHASE/POWER/WATERLOGGED plus BE_DATA,
server BE listener startup and save/reload,
physical vibration emission and delayed
state activation, radius 8 vs 16 and
block/BE tick transitions,
`stepOn` synthetic event vs emitted
world GameEvent, calibrated zero/matching/
nonmatching backside redstone signal,
weak/direct/comparator output by queried side,
physical resonator event on each side,
sensor waterlogged and physical direct
StructureTemplate fixture, vanilla
non-Planet regression. Full exact six-face
seam/corner chart and phase boundaries belong
to **3A-7.3**, not this item review.

## 5. Strict status and single next unchecked task

No class added this packet: original Stage 3A-7.1
still holds source+compiled declaration review
for **2 sensor Java classes / 2 exact BLOCK IDs**.
Main 16-column ledger remains **71/241**
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(**192/1060 registered BLOCK IDs**),
**170/241 `REVIEW_PENDING`**
(**868/1060 registered BLOCK IDs**).
`SculkShriekerBlock`,
`SculkCatalystBlock` and
`LightningRodBlock` are independent
source-`REVIEW_PENDING` classes.

All **241** `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and
`gameplay_acceptance` statuses are still
`REVIEW_PENDING`; original ITEM census
`BLOCKITEM_CREATION_REVIEW_PENDING` flags
are historical. No Java changes, builds,
actual server/client gameplay or patched-ASM
bytecode audit performed.

**NEXT FIRST open microtask: Stage 3A-7.3**,
checkbox 3 in
[`02e-sculk-vibration-sensor-owners.md`](../phases/phase-02/02e-sculk-vibration-sensor-owners.md):
source canonical FACING vs physical
world neighbour/query direction and
seam/corner path, vibration Euclidean
event propagation, timing, water/shape
and exact runnable future tests; one
bounded docs commit, stop.
