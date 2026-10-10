# Phase 2 Stage 3A-6 — environment and entity-contact signal sensors

**Status: ACTIVE — 3/4 independently bounded research tasks done; no ASM/Planet/gameplay PASS.**
Branch `2.0`, Minecraft **1.21.1** / NeoForge **21.1.215**.

[Main Phase 2 roadmap](../phase-02.md) ·
[241-class class-level ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[3A-5.4 exact original ZIP owner checksum audit](../../research/PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md)

## Original NeoForge registry cohort — source-reviewed, integration pending

Three concrete classes / **16 registered BLOCK IDs**
verified against the unmodified original 21.1.215
compiled BLOCK census (artifact **11643813158**),
all now `SOURCE_REVIEWED_INTEGRATION_PENDING` for source and reflection declarations (since 3A-6.1), while patched ASM, Planetary integration and gameplay still `REVIEW_PENDING`.

| Concrete Java block class | Original registered block IDs | Number |
|---|---|---:|
| `DaylightDetectorBlock` | `minecraft:daylight_detector` | **1** |
| `PressurePlateBlock` | `minecraft:acacia_pressure_plate`, `bamboo_pressure_plate`, `birch_pressure_plate`, `cherry_pressure_plate`, `crimson_pressure_plate`, `dark_oak_pressure_plate`, `jungle_pressure_plate`, `mangrove_pressure_plate`, `oak_pressure_plate`, `polished_blackstone_pressure_plate`, `spruce_pressure_plate`, `stone_pressure_plate`, `warped_pressure_plate` (all prefixed `minecraft:`) | **13** |
| `WeightedPressurePlateBlock` | `minecraft:heavy_weighted_pressure_plate`, `minecraft:light_weighted_pressure_plate` | **2** |
| **Total** | **16 exact BLOCK IDs** | **16** |

**Family-level commonality:** read a non-item
environmental trigger (sky/time) or entity contact,
then write/emit BlockState redstone power; **these
are three different author algorithms**, not one
generic trigger replacement. Sensor block entities
and tick scheduling matter; collision and
local support differ from a rail's `RailShape`.

Essential nonregistered owners to inspect:
`BasePressurePlateBlock` for support, entity
inside, removal, tick, signal side and AABB;
`BaseEntityBlock`, `DaylightDetectorBlockEntity`,
`BlockEntityTicker` for daylight sky/time;
`SignalGetter`, actual redstone direction and
`BlockItem`/alternative item creators.

**Cross-phase boundary:** Phase 2 owns local
support, shape, property/component writes and
physical neighbor callbacks. Phase 3 owns pressure
plate physical collision AABB and active model.
Phase 7A owns signal API query-side, output
strength, event/tick ordering, comparator/analog
readers; Phase 8 handles direct generated/structure
states when verified. Environmental brightness
sampling is a sky/world-position policy, **not
a generic Direction rotation**.
No NeoForge patch bytecode, Mixin application
or gameplay acceptance implied by this selection.

**Not part of this card:** `SculkSensorBlock`,
`CalibratedSculkSensorBlock`, `SculkShriekerBlock`,
`SculkCatalystBlock` (vibration/sculk graph)
and `LightningRodBlock` (weather strikes).
All remain `REVIEW_PENDING`; those require
separate owner-family cards.

## Microtasks — commit exactly one checkbox per answer

- [x] **1.** Fully verify original **compiled NeoForge
  21.1.215** exact class identities / all 16 registry
  IDs, five method-signature nearest declaring
  owners, plus pinned 1.21.1 comparative source
  for `DaylightDetectorBlock`,
  `PressurePlateBlock`,
  `WeightedPressurePlateBlock`,
  `BasePressurePlateBlock`,
  `DaylightDetectorBlockEntity` and
  `BaseEntityBlock`. Audit placement, support,
  neighbor updates, collision, sky and
  entity signal authors, scheduled/BE ticking,
  physical callback directions and analog modes.
  Promote **only actually source-reviewed**
  class rows, none of the excluded sensor group.
  Commit independent evidence and checkpoint.
  **DONE 2026-10-10 / 3A-6.1:** [original NeoForge five-signature owners and pinned source for 3 sensor classes/16 exact BLOCK IDs](../../research/PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md), daylight `BlockEntityTicker` 20-game-tick sky-input and inversion, plate `BasePressurePlateBlock` support, entity query AABB, 20-/10-tick power state, direct signal UP and physical below notification. Ledger **69/241 reviewed (190 IDs)**; **172/241 pending (870 IDs)**; all ASM/Planet/game acceptance REVIEW_PENDING.
- [x] **2.** Independently join original 1333 ITEM
  registry via `placed_block` for all 16 BLOCK IDs;
  distinguish any direct structure/commands,
  comparator/BE writers, `DataComponents.BLOCK_STATE`
  and entity/sky updates which bypass item placement.
  Exact per-item evidence and no runtime PASS. **DONE 2026-10-10 / 3A-6.2:** [16 original compiled ITEM→BLOCK joins, optional post-place state components, BE/sky/entity writers and generic StructureTemplate bypass](../../research/PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md). Each exact BLOCK has one same-named ordinary `BlockItem` ITEM record; 7/7 lifecycle owners = `BlockItem`, `Item.use` remains `Item`. No confirmed specific worldgen sensor structure. No new class promotions or game PASS.
- [x] **3.** Verify position-only local BlockState
  frame vs source-local support and physical neighbor
  notifications, sky light/time sampling vs local
  gravity, detector hitboxes and analog signal
  direction query, tick scheduler/BE and cross-phase
  contracts. Document concrete runnable six-face
  plus seam/corner tests, not PASS. **DONE 2026-10-10 / 3A-6.3:** [canonical support/target mapping, all 6 actual PlanetFace axes, rotated raw TOUCH_AABB and entityInside dispatch caveat, queried direct-signal UP vs physical below notifications, SKY world policy, BE 20-global-tick vs plate 20/10-relative-tick and 15 future acceptance fixtures](../../research/PHASE2_STAGE3A_SENSOR_FACE_FRAME_AND_SIGNAL_CONTRACT_1_21_1.md). No code, tests or new class promotions.
- [ ] **4.** Reconcile original 21.1.215 CI ZIP
  class roster, reviewed IDs, full five-signature
  owner tuples and all acceptance gate fields,
  prepare next independent owner card, commit
  checkpoint. Do not claim Phase 2 complete.

## Resume checkpoint

**Last completed: Stage 3A-6.3:** canonical
local BlockState frame vs source-local support
DOWN, real world support BlockPos and support
block's local inward face; physical neighbor
notifications and API queried signal direction
remain separate domains. Reviewed actual
Planetary `PlanetFace`, `PlanetBlockStateFrame`,
`PlanetBlockFrameContext`,
`PlanetBlockNeighborQuery`,
`PlanetBlockSupportQuery`,
`PlanetBlockShapeRuntime` and
`PlanetVoxelShapeRotation`. The
`BasePressurePlateBlock.TOUCH_AABB` entity
box needs its own physical rotation and
`entityInside` dispatch verification; the
visual VoxelShape rotation is not enough.
Sky LightLayer.SKY and sun angle are
global environment readings, not automatically
local-radial; daylight BE updates on world
gameTime%20, whereas plate scheduled ticks
are relative 20/10. Signal `getDirectSignal`
argument UP is query-side, not a notification
direction. [Complete chart and 15 not-run
acceptance fixtures](../../research/PHASE2_STAGE3A_SENSOR_FACE_FRAME_AND_SIGNAL_CONTRACT_1_21_1.md).

Full status unchanged **69/241 source-reviewed
(190/1060 IDs)**, **172/241 source pending
(870/1060 IDs)**. All 241 patched ASM,
Planetary runtime and gameplay statuses PENDING.
No code changes or tests.

**NEXT FIRST unchecked Stage 3A-6.4**
checkbox 4: independently reconcile original
NeoForge 21.1.215 CI ZIP against all current
69 exact reviewed classes / 190 IDs and
five method-declaring owner signatures;
queue next independent source-owner family.
One commit and stop; full Phase 2 incomplete.
