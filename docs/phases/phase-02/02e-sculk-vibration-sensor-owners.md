# Phase 2 Stage 3A-7 — sculk vibration and calibrated sensor owners

**Status: NEXT / 0 of 4 independent research tasks completed.**
Branch `2.0`; Minecraft **1.21.1** / NeoForge
**21.1.215**, Java 21.

[Phase 2 roadmap](../phase-02.md) ·
[Original 241-class registry ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[Full 69-class / 190 ID original NeoForge reconciliation](../../research/PHASE2_STAGE3A_SENSOR_COHORT_RECONCILIATION_1_21_1.md)

## Two exact concrete classes / two original BLOCK IDs, source REVIEW_PENDING

| Class | Registered BLOCK ID | Original property names | Original hierarchy |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkSensorBlock` | `minecraft:sculk_sensor` | `power,sculk_sensor_phase,waterlogged` | `SculkSensorBlock > BaseEntityBlock > Block > BlockBehaviour` |
| `net.minecraft.world.level.block.CalibratedSculkSensorBlock` | `minecraft:calibrated_sculk_sensor` | `facing,power,sculk_sensor_phase,waterlogged` | `CalibratedSculkSensorBlock > SculkSensorBlock > BaseEntityBlock > Block > BlockBehaviour` |

Original physical NeoForge 21.1.215 CI ZIP artifact
**11643813158** SHA256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
These 2 classes / 2 BLOCK IDs are still
**REVIEW_PENDING** for source.
All three patched ASM, Planet integration
and gameplay acceptance fields are also pending.
Selecting them is **not** a source review, and
does not mean the vibrator/neighbor/block-event
paths work on any cube face yet.

## Mechanism routing / required nonregistered owners

Before implementation, audit complete call paths:
`SculkSensorBlock` placement and waterlogging,
`POWER`, `SCULK_SENSOR_PHASE` transitions,
`VibrationSystem` game-event listener/user and
server ticker/BlockEntity data;
`CalibratedSculkSensorBlock` `FACING`
placement/rotate/mirror and suppression of its
redstone output on the input face; calibrated
`CalibratedSculkSensorBlockEntity.VibrationUser`
back-direction `Level.getSignal` frequency
sampling, event filtering and game-event transport.
Validate actual nearest NeoForge owners for
**five exact method signatures** and additional
`getTicker`, `neighborChanged`, `onPlace`,
`updateShape`, `getSignal`,
`getDirectSignal`, analog comparator and
scheduled block ticks; distinguish signal
**API queried side** from physical neighbor
position and canonical target block frame.

Phase 2 owns canonical BlockState direction,
placement, survival, physical neighbor lookup
and fluid-state graph. Phase 3 owns rendered
and physical model/collision geometry. Phase 7A
owns redstone read/write/dispatch,
vibration game-event frequency/tick phases and
BlockEntity listeners. Phase 5 owns waterlogging
flow; Phase 8 owns any verified direct worldgen
or structure creation. No blanket Direction
remapping or global vibration behavior change.

**Excluded, still pending independent families:**
`SculkShriekerBlock`, `SculkCatalystBlock`
(shriek/spread/warden and bloom author paths),
`LightningRodBlock` (world lightning strike),
other non-scuk signal sensors, and all
minecraft entity motion physics.

## Four separately committable substeps — only FIRST unchecked per `кк`

- [ ] **1. Stage 3A-7.1.** Verify two original
  NeoForge registered Java BLOCK classes/2 exact
  IDs and **five full-signature nearest compiled
  method declaring owners** for each, plus pinned
  comparative Minecraft 1.21.1 complete
  `SculkSensorBlock`,
  `CalibratedSculkSensorBlock`,
  `SculkSensorBlockEntity`,
  `CalibratedSculkSensorBlockEntity`,
  `VibrationSystem`, `BaseEntityBlock`
  graph. Check waterlogging, activation/cooldown,
  `FACING`, side-filtering and alternate
  state authors; promote **only actually
  audited two rows**, do not touch shrieker
  and catalyst. Evidence + ledger + checkpoint.
- [ ] **2. Stage 3A-7.2.** Independently join
  exact original 1333-item registry by
  `ITEM.placed_block` for both BLOCK IDs,
  distinguish ordinary BlockItem vs aliases,
  `DataComponents.BLOCK_STATE`,
  BlockEntity/game-event/neighbor and direct
  structure/world state writers, frequency
  selection and calibrated-signal alternative
  authors. Evidence + checkpoint; no runtime PASS.
- [ ] **3. Stage 3A-7.3.** Canonical local
  BlockState FACING, real-world neighbor
  and receiver-query direction,
  vibration propagation / game-event
  source-target physical coordinates across
  cube edges/corners, power phases,
  waterlogged restrictions, physical shapes
  and crossphase contracts. Detailed test
  matrix for six faces, seam/corner, acoustic
  game events, signal filtering, water, and
  vanilla non-Planet regression. Don't claim
  actually executed game tests.
- [ ] **4. Stage 3A-7.4.** Independent original
  NeoForge 21.1.215 CI ZIP verification of
  updated class roster, exact reviewed IDs,
  all five declaring owners, pending class
  statuses and all ASM/Planet/gameplay
  statuses; queue next small owner family.
  One commit, full Stage 3A still open.

## Durable resume checkpoint

**Last completed:** preceding [3A-6.4
full original 69-class/190 ID sensor reconciliation](../../research/PHASE2_STAGE3A_SENSOR_COHORT_RECONCILIATION_1_21_1.md).
All four original-vs-ledger fingerprints match:
roster `0xf188a064`,
reviewed ID `0x6ae8047b`,
reviewed declaring owners `0xba6ef72f`,
combined `0x328b390e`.

**Current total:** 69/241 source+compiled
declaration reviewed (190 registered IDs),
172/241 source pending (870 registered IDs),
all 241 patch-ASM/runtime/Planet/gameplay
fields pending.

**NEXT FIRST unchecked:** Stage **3A-7.1**,
task 1 above, 2 exact unreviewed Java
classes/2 IDs, method owners and vibration
BlockEntity source. Single research commit,
checkpoint, stop.
