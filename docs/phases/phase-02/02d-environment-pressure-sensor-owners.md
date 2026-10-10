# Phase 2 Stage 3A-6 — environment and entity-contact signal sensors

**Status: ACTIVE — 2/4 independently bounded research tasks done; no ASM/Planet/gameplay PASS.**
Branch `2.0`, Minecraft **1.21.1** / NeoForge **21.1.215**.

[Main Phase 2 roadmap](../phase-02.md) ·
[241-class class-level ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[3A-5.4 exact original ZIP owner checksum audit](../../research/PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md)

## Still-unreviewed original NeoForge class registry cohort

Three concrete classes / **16 registered BLOCK IDs**
verified against the unmodified original 21.1.215
compiled BLOCK census (artifact **11643813158**),
all presently `REVIEW_PENDING`.

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
- [ ] **3.** Verify position-only local BlockState
  frame vs source-local support and physical neighbor
  notifications, sky light/time sampling vs local
  gravity, detector hitboxes and analog signal
  direction query, tick scheduler/BE and cross-phase
  contracts. Document concrete runnable six-face
  plus seam/corner tests, not PASS.
- [ ] **4.** Reconcile original 21.1.215 CI ZIP
  class roster, reviewed IDs, full five-signature
  owner tuples and all acceptance gate fields,
  prepare next independent owner card, commit
  checkpoint. Do not claim Phase 2 complete.

## Resume checkpoint

**Last completed: Stage 3A-6.2**.
[Original 16 ITEM `placed_block` join and off-item
sensor state authors](../../research/PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md)
checks original unmodified NeoForge ZIP SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`,
all **16** original 1:1 BLOCK↔ITEM mappings,
all 16 concrete class `BlockItem`, all seven item
placement lifecycle declaring owners `BlockItem`;
`Item.use` inherited from `Item`. Cryptographic
source ITEM-row join SHA-256
`49c88b217ca7b0dd560634c2c8cb01560b265b29a85030ebda92b66f7596ebe9`.
Separate non-ITEM sources: daylight server BE ticker
plus player `INVERTED` override, plate entity contact
and scheduled tick/release, `BasePressurePlateBlock`
support/remove callbacks, optional item
`DataComponents.BLOCK_STATE` component,
generic direct `StructureTemplate` write;
**no confirmed specific sensor-containing template**.

Full ledger **69/241 source+reflection reviewed
(190/1060 BLOCK IDs)**, **172/241 REVIEW_PENDING
(870/1060 BLOCK IDs)**; Sculk/Lightning excluded.
All ASM/Planet/gameplay statuses remain pending.
No Java/test changes.

**NEXT FIRST unchecked microtask: Stage 3A-6.3**,
checkbox 3: local/physical frame versus pressure
plate support+entity AABB, signal-port direction,
daylight sky/ticker, runtime phase contracts and
six-face/seam test design. ONE research commit then
stop. Do not mark gameplay PASS.
