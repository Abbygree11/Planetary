# Phase 2 Stage 3A-7 — sculk vibration and calibrated sensor owners

**Status: DONE — 4/4 bounded sculk sensor RESEARCH tasks; ASM/Planet/gameplay still pending.**
Branch `2.0`; Minecraft **1.21.1** / NeoForge
**21.1.215**, Java 21.

[Phase 2 roadmap](../phase-02.md) ·
[Original 241-class registry ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[Full 69-class / 190 ID original NeoForge reconciliation](../../research/PHASE2_STAGE3A_SENSOR_COHORT_RECONCILIATION_1_21_1.md)

## Two exact concrete classes / two original BLOCK IDs, source+compiled owners reviewed, runtime pending

| Class | Registered BLOCK ID | Original property names | Original hierarchy |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkSensorBlock` | `minecraft:sculk_sensor` | `power,sculk_sensor_phase,waterlogged` | `SculkSensorBlock > BaseEntityBlock > Block > BlockBehaviour` |
| `net.minecraft.world.level.block.CalibratedSculkSensorBlock` | `minecraft:calibrated_sculk_sensor` | `facing,power,sculk_sensor_phase,waterlogged` | `CalibratedSculkSensorBlock > SculkSensorBlock > BaseEntityBlock > Block > BlockBehaviour` |

Original physical NeoForge 21.1.215 CI ZIP artifact
**11643813158** SHA256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
These **2 classes / 2 BLOCK IDs** were promoted
in **3A-7.1** to **SOURCE_REVIEWED_INTEGRATION_PENDING**
only after exact compiled five-owner dispatch and
pinned source algorithm audit. All three patched
ASM, Planet adapter/integration and gameplay
acceptance fields remain **REVIEW_PENDING**.
No six-face or game-event runtime PASS.

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

- [x] **1. Stage 3A-7.1.** Verify two original
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
  **DONE 2026-10-10 / 3A-7.1:** [original compiled 5 method owners/2 exact BLOCK IDs and pinned six-class BE/VibrationSystem source review](../../research/PHASE2_STAGE3A_SCULK_VIBRATION_SOURCE_OWNER_AUDIT_1_21_1.md); 8/16 radius, 30/10 ACTIVE and 10 COOLDOWN ticks, calibrated FACING and back-signal frequency filtering, raw physical-world event occlusion and neighbor callback paths. Two rows promoted to source-only reviewed, **71/241 classes and 192/1060 BLOCK IDs**; all runtime/gameplay gates pending.
- [x] **2. Stage 3A-7.2.** Independently join
  exact original 1333-item registry by
  `ITEM.placed_block` for both BLOCK IDs,
  distinguish ordinary BlockItem vs aliases,
  `DataComponents.BLOCK_STATE`,
  BlockEntity/game-event/neighbor and direct
  structure/world state writers, frequency
  selection and calibrated-signal alternative
  authors. Evidence + checkpoint; no runtime PASS.
  **DONE 2026-10-10 / 3A-7.2:** [original two exact one-to-one BlockItem registry joins, seven compiled placement method owners and ITEM `Item.use` distinction, optional block/BE item component state rewrite, vibration BlockEntity writer paths, state phase scheduling, calibrated input/output readers and generic StructureTemplate bypass](../../research/PHASE2_STAGE3A_SCULK_ITEM_ALTERNATE_AUTHORS_1_21_1.md). Reproducible joined ITEM SHA-256 `6225d8898725346b8f37d34d97d64a4a7a86a17a5cf39e02d339f2bc0972fee5`. No specific worldgen sensor placement or runtime/gameplay PASS.
- [x] **3. Stage 3A-7.3.** Canonical local
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
  **DONE 2026-10-10 / Stage 3A-7.3:** [actual six-face PlanetFace directions and canonical BlockState FACING, physical backside receiver and API signal query separation, world XYZ vibration/ray/chunk semantics, two tick loops, local half-voxel shape/water/audio and 19 **not run** future vanilla/six-face/seam/corner game fixtures](../../research/PHASE2_STAGE3A_SCULK_SIX_FACE_VIBRATION_SIGNAL_CONTRACT_1_21_1.md). No new class disposition or runtime PASS.
- [x] **4. Stage 3A-7.4.** Independent original
  NeoForge 21.1.215 CI ZIP verification of
  updated class roster, exact reviewed IDs,
  all five declaring owners, pending class
  statuses and all ASM/Planet/gameplay
  statuses; queue next small owner family.
  One commit, full Stage 3A still open.
  **DONE 2026-10-10 / 3A-7.4:** [independently reopened immutable 21.1.215 ZIP, verified 241 class roster/1060 block IDs, all 71 reviewed class/192 exact IDs and five complete method signature declaring owners, four matching original-vs-GitHub FNV hashes, 170 pending rows and 241×3 acceptance gates](../../research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md). No new source promotion and no ASM/gameplay PASS. NEXT [3A-8 source owner card](02f-sculk-shrieker-catalyst-owners.md) (two original pending classes/2 IDs).

## Durable resume checkpoint

**Last completed: Stage 3A-7.4**, source
reconciliation **4/4 sculk vibration
family research tasks DONE**. This does
not complete Stage 3A/Phase 2 and does
not claim Planet/gameplay compatibility.

[Original unmodified NeoForge compiled
71-class/192 ID full owner evidence](../../research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md).
Original CI artifact 11643813158
SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`;
1060 BLOCK rows/241 unique classes,
1333 ITEM rows, 1712 properties.
All 71 source-reviewed class names,
192 registered exact ID strings and
five declaring owner tuple signatures
matched original ZIP.
Original vs GitHub digests:
roster `0xf188a064`,
IDs `0x8a6840ba`, owners
`0xa81e5835`, combined
`0x59d9c0aa`.
Full ledger unchanged **71/241**
source+compiled-reviewed classes/
**192/1060** BLOCK IDs,
**170/241 source REVIEW_PENDING** /
868 IDs. 241×3 patched bytecode/
Planet/gameplay gates remain pending;
no Java patch, CI/client test or
gameplay run.

Created NEXT
[Stage 3A-8 SculkShriekerBlock and
SculkCatalystBlock owner family](02f-sculk-shrieker-catalyst-owners.md)
card 0/4, exactly two still-unreviewed
concrete BLOCK classes / two exact IDs.
Different algorithms: shriek/BE
VibrationSystem/warden-spawn vs catalyst
entity-death GameEvent/BE tick,
BLOOM and SculkSpreader cursors.
LightningRodBlock stays pending separately.

**NEXT FIRST unchecked: Stage 3A-8.1**,
task 1 of new 02f card, full original
compiled class+five signature declaration
owners, pinned Java source of both
different BE writer graphs; one bounded
commit/updated checkpoint and STOP.
