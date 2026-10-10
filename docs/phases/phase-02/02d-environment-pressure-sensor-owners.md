# Phase 2 Stage 3A-6 — environment and entity-contact signal sensors

**Status: NEXT — 0/4 independently bounded research tasks done.**
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

- [ ] **1.** Fully verify original **compiled NeoForge
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
- [ ] **2.** Independently join original 1333 ITEM
  registry via `placed_block` for all 16 BLOCK IDs;
  distinguish any direct structure/commands,
  comparator/BE writers, `DataComponents.BLOCK_STATE`
  and entity/sky updates which bypass item placement.
  Exact per-item evidence and no runtime PASS.
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

Last completed **Stage 3A-5.4**:
all original ZIP class names/counts and all **66**
source+reflection-reviewed classes' **174 exact IDs
and five method owners** independently reconciled
(four digests all matching). Full ledger:
**66/241 source-reviewed** (174/1060 IDs),
**175/241 REVIEW_PENDING** (886/1060 IDs),
all ASM/Planet/gameplay acceptance PENDING.

**NEXT first unchecked: checkbox 1**, Stage **3A-6.1**;
three still-pending concrete classes / 16 BLOCK
IDs, no Java implementation, one evidence commit.
