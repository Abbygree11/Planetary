# Phase 2 Stage 3A-6.4 — original NeoForge registry and five-owner reconciliation of all 69 reviewed classes

**2026-10-10** · Planetary `2.0` · Minecraft **1.21.1** /
NeoForge **21.1.215**, Java 21.

**Scope:** one independent evidence/ledger-status reconciliation
after the daylight and pressure-plate family research.
The **sensor research family** has 4/4 bounded tasks
completed; **entire Stage 3A/Phase 2 is not done**.
No Java source, runtime ASM, Mixin, CI or gameplay tests
were executed in this package.

## 1. Provenance and exact original input

Directly reopened the **unmodified original NeoForge
21.1.215 runtime census ZIP** at
`/mnt/data/phase2-neo1211-registry-census.zip`,
GitHub Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, compiled project revision
`aa39572950a15403ea0a9003eefccf3bf6675ff7`.
Verified SHA-256 of the actual archive:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Census members parsed independently with exact tab-
separated columns:

| ZIP member | Data records |
|---|---:|
| `phase2-neo1211-block-registry.tsv` | **1060** BLOCK IDs |
| `phase2-neo1211-item-registry.tsv` | **1333** ITEM IDs |
| `phase2-neo1211-state-properties.tsv` | **1712** state property records |
| Distinct registered `java_class` in BLOCK TSV | **241** |

For all **241** original registered concrete Java classes,
grouped the original `registry_id` rows by exact
`java_class`, checked exact count and class identity
against the branch ledger.
For each of the **69** current class rows marked
`SOURCE_REVIEWED_INTEGRATION_PENDING`, independently
parsed the original ZIP's `effective_method_owners`,
selected the **full exact qualified argument signature**
(not simply the method's name or first overload),
and compared the nearest declaring Java class of
each of these **five** methods:

1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

All registered ID rows of a given concrete class
were required to yield the same five method owners.
The **190 exact registered BLOCK IDs** of those 69
classes were individually reconstructed and checked.
The 172 other registered class rows remain source
`REVIEW_PENDING`, not silently assigned owners.

## 2. Four independently calculated ZIP-vs-GitHub digests

Format: sort by exact full `java_class` ascending.
For each class append its name and literal delimiters,
include a trailing `\n` in each row. Exact registered
BLOCK IDs are comma-joined in sorted order; five
method owners are pipe-separated in the signature
order above. FNV-1a is **32-bit diagnostic checksum**,
not a collision-resistant proof or acceptance of
runtime execution.

| Canonical data joined to class | Original ZIP | Current GitHub ledger | Verdict |
|---|---|---|---|
| **241** classes + each registered ID **count** | `0xf188a064` | `0xf188a064` | **MATCH** |
| **69** reviewed classes + **190 exact BLOCK IDs** | `0x6ae8047b` | `0x6ae8047b` | **MATCH** |
| **69** reviewed classes + 5 nearest declaring owners each | `0xba6ef72f` | `0xba6ef72f` | **MATCH** |
| **69** reviewed classes + 190 IDs + 5 owners per class | `0x328b390e` | `0x328b390e` | **MATCH** |

Checks also included: exactly 241 unique nonempty
`java_class` rows, 1060 summed registered
block counts, **16 TSV fields** per class,
all 69 reviewed rows carry 5 non-pending declaring
owners and `REFLECTION_OWNER_VERIFIED`,
and all **172 unreviewed** rows retain
`REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY`.
Every `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and
`gameplay_acceptance` field is **REVIEW_PENDING**
for all 241 rows. **No class disposition changed
in this 3A-6.4 check.**

## 3. Sensor family specifically included in these digests

| Registered concrete class | Registered BLOCK IDs | Exact IDs | Nearest declaring owners: place / survive / updateShape / randomTick / setPlacedBy | Disposition |
|---|---:|---|---|---|
| `DaylightDetectorBlock` | 1 | `minecraft:daylight_detector` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `PressurePlateBlock` | 13 | `minecraft:acacia_pressure_plate,minecraft:bamboo_pressure_plate,minecraft:birch_pressure_plate,minecraft:cherry_pressure_plate,minecraft:crimson_pressure_plate,minecraft:dark_oak_pressure_plate,minecraft:jungle_pressure_plate,minecraft:mangrove_pressure_plate,minecraft:oak_pressure_plate,minecraft:polished_blackstone_pressure_plate,minecraft:spruce_pressure_plate,minecraft:stone_pressure_plate,minecraft:warped_pressure_plate` | `Block / BasePressurePlateBlock / BasePressurePlateBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `WeightedPressurePlateBlock` | 2 | `minecraft:heavy_weighted_pressure_plate,minecraft:light_weighted_pressure_plate` | `Block / BasePressurePlateBlock / BasePressurePlateBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| **Total** | **16** | **1 daylight, 13 ordinary plate, 2 weighted plate** | **3 concrete classes** | **source+compiled declaration reviewed ONLY** |

Original known hierarchy:
`DaylightDetectorBlock > BaseEntityBlock > Block > BlockBehaviour`,
`PressurePlateBlock/WeightedPressurePlateBlock > BasePressurePlateBlock > Block > BlockBehaviour`.
These are **compiled declaration owners**, not
a proof that NeoForge 21.1.215 patched method
bodies and injected mixins actually run at
all intended block/callback paths.

Previous, **already committed** family evidence:
[3A-6.1 source and 5-owner method audit](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md),
[3A-6.2 actual 16 ITEM creators and alternate
BE/entity/structure authors](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md),
[3A-6.3 six-face canonical-support, TOUCH_AABB,
redstone signal and sky contract](PHASE2_STAGE3A_SENSOR_FACE_FRAME_AND_SIGNAL_CONTRACT_1_21_1.md).
Fifteen **future game acceptance fixtures** were specified
there; **none have been executed**.

## 4. Next independent *unreviewed* owner family

Stage [**3A-7**](../phases/phase-02/02e-sculk-vibration-sensor-owners.md)
now queues exactly **2 unreviewed source-owner Java
classes / 2 registered BLOCK IDs**, both present
in original NeoForge 21.1.215 compiled registry:

| Pending class | Exact registered BLOCK ID | Original property values | Original hierarchy |
|---|---|---|---|
| `SculkSensorBlock` | `minecraft:sculk_sensor` | `power,sculk_sensor_phase,waterlogged` | `SculkSensorBlock>BaseEntityBlock>Block>BlockBehaviour` |
| `CalibratedSculkSensorBlock` | `minecraft:calibrated_sculk_sensor` | `facing,power,sculk_sensor_phase,waterlogged` | `CalibratedSculkSensorBlock>SculkSensorBlock>BaseEntityBlock>Block>BlockBehaviour` |

Both are **REVIEW_PENDING**, no promotion in
this task. Initial source *routing*
(not completed semantic audit or compiled
target verification) identifies
[`SculkSensorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java),
[`CalibratedSculkSensorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java),
and [`SculkSensorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java),
[`CalibratedSculkSensorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java)
plus `VibrationSystem.Ticker`, game-event listeners
and client/server signal outputs.
Calibrated sensor's `FACING` influences filtered
incoming signal and game-event frequency, and
BE callback may sample the **back** neighbor;
its physical query and the source receiver port
must not be collapsed to a global Direction rewrite.

**Out of scope of this small next card:**
`SculkShriekerBlock` (`minecraft:sculk_shrieker`)
and `SculkCatalystBlock` (`minecraft:sculk_catalyst`)
are **other unreviewed** concrete classes
with different shriek/spread behavior; as are
lightning-strike owners. They stay pending and
need separate research cards.

## 5. Status and hard stop

After Stage 3A-6.4 (unchanged since 3A-6.1):
**69/241** `SOURCE_REVIEWED_INTEGRATION_PENDING`
(**190/1060 BLOCK IDs**);
**172/241** `REVIEW_PENDING`
(**870/1060 BLOCK IDs**).
Every real patched NeoForge method-body/ASM,
Planetary adapter and gameplay field still
`REVIEW_PENDING`; not one fixture/Java change
in this reconciliation. The **3A-6 family
research card** is now 4/4, but the full
Stage 3A, Phase 2, minecart/other cross-phase
work and runtime acceptance remain open.

**NEXT FIRST independent microtask: Stage 3A-7.1**
checkbox 1 of
[`02e-sculk-vibration-sensor-owners.md`](../phases/phase-02/02e-sculk-vibration-sensor-owners.md):
read original 21.1.215 exact compiled ID/five
method owners and pinned source vibration,
BlockEntity and signal paths for those two
pending classes, promote only if fully audited,
commit checkpoint and stop.
