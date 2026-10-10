# Stage 3A-7.4 — 71 reviewed owner classes / 192 exact IDs cross-check against original NeoForge 21.1.215

**Date 2026-10-10** · Planetary branch `2.0` · Minecraft
**1.21.1** · NeoForge **21.1.215** · Java **21**.

**Scope: exactly one bounded source-registry reconciliation**.
This checks original compiled NeoForge reflection outputs
against the full current 16-column GitHub disposition
ledger after sculk and calibrated sculk research.
It is **not** an ASM patch-body examination, Java
implementation, NeoForge Mixin application test,
a client/server build or gameplay result.

## 1. Immutable compiled source and exact extraction procedure

Primary artifact: original CI
[GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, source commit
`aa39572950a15403ea0a9003eefccf3bf6675ff7`.
Directly reopened
`/mnt/data/phase2-neo1211-registry-census.zip`
and verified its real SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
This is the **unchanged original binary ZIP**,
not a regenerated or manually edited class list.

ZIP members and actual parsed record counts:

| Exact original ZIP TSV member | Rows |
|---|---:|
| `phase2-neo1211-block-registry.tsv` | **1060 registered BLOCK IDs** |
| `phase2-neo1211-item-registry.tsv` | **1333 registered ITEM IDs** |
| `phase2-neo1211-state-properties.tsv` | **1712 state-property records** |
| Unique `BLOCK.java_class` values | **241 classes** |

Independent original-data verification:
all original 1060 `registry_id` strings
were grouped under exact `java_class`.
Verified **all 241 class names and counts**
against GitHub ledger. For each of the
current **71 source+reflection-reviewed
concrete classes**, all original registered
BLOCK IDs were sorted and compared with
ledger ID strings (total **192 exact IDs**).

Parsed `effective_method_owners` from each
original BLOCK registry row, matching the
**complete qualified argument signature**
of each of the five methods, not matching
only simple method names; where overloaded
owners were separated by semicolon, selected
the exact signature. Asserted each
registry ID of the same concrete class
resolved to the *same* five owners.
Compared all five to current GitHub ledger
columns in this order:

1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

**Note:** actual `SculkSensorBlock.tick` scheduled
phase transitions and BE `VibrationSystem.Ticker`
are **not** the same method as signature
4's inherited `randomTick`. A compiled
nearest declaring owner is evidence of
dispatch declaration **only**, not that
Minecraft or Planetary calls it correctly.

## 2. Four independent original-vs-GitHub fingerprints (all match)

FNV-1a **32-bit diagnostic hashes**, not
collision-resistant hashes or proof of bytecode
patch correctness. Source digest calculated
from original ZIP in Python; independent
ledger digest calculated from actual live
GitHub 16-column TSV. Both sort by exact
fully qualified `java_class` using ordinal
sorting; IDs within a class are sorted
lexically. UTF-8 lines terminated by `\n`:

- Roster: `full_class_name|registered_id_count\n`
  for **all 241 classes**.
- Reviewed ID digest: `full_class_name|id1,id2,...\n`
  for **71 reviewed classes**.
- Owners: `full_class_name|owner1|owner2|owner3|owner4|owner5\n`.
- Combined: `full_class_name|id1,id2,...|owner1|...|owner5\n`.

| Independently rederived set | Original immutable NeoForge ZIP | GitHub ledger | Result |
|---|---|---|---|
| All 241 class roster/count tuples | `0xf188a064` | `0xf188a064` | **MATCH** |
| 71 reviewed classes / 192 exact registered BLOCK IDs | `0x8a6840ba` | `0x8a6840ba` | **MATCH** |
| 71 × 5 exact method declaring owners | `0xa81e5835` | `0xa81e5835` | **MATCH** |
| Reviewed IDs + all five owner tuples | `0x59d9c0aa` | `0x59d9c0aa` | **MATCH** |

Also verified 241 unique class keys, sums
1060 /192 /868 registered IDs, exactly
**16 fields per ledger row**,
all 71 reviewed class statuses
`SOURCE_REVIEWED_INTEGRATION_PENDING`
and reflection owner statuses
`REFLECTION_OWNER_VERIFIED`,
and **170** remaining rows retain
`REVIEW_PENDING` and
`REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY`.
All **241×3** patched ASM/Planet adapter/
gameplay gate fields retain
`REVIEW_PENDING`.

**Exact registry identities for the
just-completed sculk family:**

| Registered BLOCK class | Exact registry ID | Five original compiled nearest owner classes: placement/survival/updateShape/randomTick/setPlacedBy | Actual class status |
|---|---|---|---|
| `CalibratedSculkSensorBlock` | `minecraft:calibrated_sculk_sensor` | `CalibratedSculkSensorBlock / BlockBehaviour / SculkSensorBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `SculkSensorBlock` | `minecraft:sculk_sensor` | `SculkSensorBlock / BlockBehaviour / SculkSensorBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |

Their source reasoning was already committed:
[3A-7.1 source ownership and BE/VibrationSystem](PHASE2_STAGE3A_SCULK_VIBRATION_SOURCE_OWNER_AUDIT_1_21_1.md),
[3A-7.2 exact ITEM creators and other authors](PHASE2_STAGE3A_SCULK_ITEM_ALTERNATE_AUTHORS_1_21_1.md),
[3A-7.3 six-face calibrated FACING and
19 future acceptance fixtures](PHASE2_STAGE3A_SCULK_SIX_FACE_VIBRATION_SIGNAL_CONTRACT_1_21_1.md).
Those 19 test descriptions are **not executed**.

## 3. Next two smallest unreviewed, related but DISTINCT sculk author families

Created [next Stage 3A-8 card](../phases/phase-02/02f-sculk-shrieker-catalyst-owners.md)
with **two other concrete NeoForge BLOCK classes /
two registered IDs**, both still in the
**170 source REVIEW_PENDING** cohort:

| Exact original class | Original exact BLOCK ID | Actual property names | Original class hierarchy |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkShriekerBlock` | `minecraft:sculk_shrieker` | `can_summon,shrieking,waterlogged` | `SculkShriekerBlock > BaseEntityBlock > Block > BlockBehaviour` |
| `net.minecraft.world.level.block.SculkCatalystBlock` | `minecraft:sculk_catalyst` | `bloom` | `SculkCatalystBlock > BaseEntityBlock > Block > BlockBehaviour` |

**One card does not mean one identical algorithm.**
Pinned 1.21.1 comparative
[`SculkShriekerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java)
and [`SculkShriekerBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java)
show waterlogged placement, SHRIEKING
state, a BE `VibrationSystem` listener,
90-tick shriek, player-sensitive event,
warning/warden summons in world physical
positions, gamerule and chunk implications.
Pinned
[`SculkCatalystBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java),
[`SculkCatalystBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java),
and [`SculkSpreader`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java)
show `GameEvent.ENTITY_DIE` listener,
`BLOOM` state, **8-tick scheduled
bloom reset**, and independent physical
`SculkSpreader.ChargeCursor` growth paths.
SculkCatalyst's listener does **not**
equate to a VibrationSystem.Listener; their
ticker algorithms and world-side effects
require separate sub-sections and checks.
The next card's first step **will** conduct
full source + original compiled exact-method-owner
review; the present card's source routing
is **NOT** that review.

**`LightningRodBlock`** with
`minecraft:lightning_rod`, FACING/
POWERED/WATERLOGGED and lightning strike
callbacks remains **pending as a separate
weather/redstone owner family**.
This card does not touch its disposition.
All nonregistered sculk spread/warden/world
event and feature-generation writers need
their own cross-phase owners rather than
being blindly added to the 241
registered BLOCK class census.

## 4. Final scoped disposition and hard stop

Stage 3A-7 vibration/calibrated-sensor
research **4/4 separate evidence packets
complete**. This does **not** mean
these blocks work under Planet gravity,
and does **not** mean Stage 3A/Phase 2
is complete.

Full ledger **unchanged by 3A-7.4**:
**71/241** source+compiled declaration
reviewed classes (**192/1060 exact BLOCK IDs**);
**170/241 `REVIEW_PENDING`**
(**868/1060 BLOCK IDs**).
All **241** `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and
`gameplay_acceptance` remain
`REVIEW_PENDING`.
No new Java code, Minecraft gameplay,
NeoForge patched bytecode review,
CI build, or acceptance test in this step.

**NEXT FIRST unchecked: Stage 3A-8.1** —
the first subtask on new
[`02f-sculk-shrieker-catalyst-owners.md`](../phases/phase-02/02f-sculk-shrieker-catalyst-owners.md).
One bounded source+compiled owner audit for
two pending Java classes/2 exact BLOCK IDs;
distinct shriek/vibration/warden and
catalyst/death/spread call paths, then
commit and stop.
