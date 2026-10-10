# Phase 2 Stage 3A-5 — rail topology/ascending slopes and signal graph owners

**Status: ACTIVE / tasks 1–2 of 4 completed; no ASM/Planet/gameplay PASS.**
Branch `2.0`; Minecraft 1.21.1 / NeoForge 21.1.215.

[Phase 2](../phase-02.md) ·
[Original 241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[Exact 3A-4.4 reconciliation](../../research/PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md)

## Original compiled registry selection (3 SOURCE_REVIEWED_INTEGRATION_PENDING)

These 3 concrete registered BLOCK classes represent
**4 exact registered IDs**, proven against unmodified
NeoForge 21.1.215 original CI ZIP `11643813158`.
As of `3A-5.1`, all three have source+reflection owner
review; **patched bytecode, Planetary runtime and gameplay
remain REVIEW_PENDING**.
Unlike wire/tension, the P28 rail family uses
a `RailShape` graph, ascent and rail-neighbor state.

| Concrete class | Exact registered BLOCK ID(s) | Count |
|---|---|---:|
| `RailBlock` | `minecraft:rail` | 1 |
| `DetectorRailBlock` | `minecraft:detector_rail` | 1 |
| `PoweredRailBlock` | `minecraft:activator_rail`, `minecraft:powered_rail` | 2 |
| **Total** | **4 BLOCK IDs** | **4** |

Nonregistered essential algorithms:
`BaseRailBlock`, `RailState` including its
connection/update helpers, full inheritance and
call graph; `RailShape` properties and
`RailState` virtual neighbor positions and
slope/turn connectivity.

`DaylightDetectorBlock` (1 ID) remains
`REVIEW_PENDING` but must be given a **different
independent environmental-signal author card**,
not silently promoted with rails.

**Boundaries**: Phase 2 authors LOCAL `RailShape`,
`UPDATE_SHAPE`, source/target support/neighbor
mapping. Phase 3 handles rendered rail slope and
collision. Phase 7A handles powering, detector
entity events, `PoweredRailBlock` circuit walk.
Movement of minecarts (entity) needs its own
traversal/rail-shape world semantics outside
Phase-2 block-state-only claims. P28 and P36
are distinct and partially overlapping.

## Independent bounded commits — one checkbox per answer

- [x] **1.** Verify original runtime NeoForge 21.1.215
  class/ID and **five exact method signature owners**
  plus full pinned comparative Minecraft 1.21.1
  class hierarchy/source: `RailBlock`,
  `DetectorRailBlock`, `PoweredRailBlock`,
  `BaseRailBlock`, `RailState`,
  graph connectivity, placement/support, updateShape,
  onPlace/neighborChanged and physical rise/fall.
  Check alternate state authors. Promote **only**
  these 3 rows after actually auditing them, never
  DaylightDetector. **DONE 2026-10-10 / Stage 3A-5.1:** [exact NeoForge owner and complete comparative rail-graph source audit](../../research/PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md). 3 formerly pending classes / 4 IDs promoted to `SOURCE_REVIEWED_INTEGRATION_PENDING`; critical `RailState` multi-cell geometry, XZ-only connection match, hardcoded Y slopes, rail junction and detector/powered signal paths isolated. Counts **66/241 reviewed (174 IDs)**, 175 pending (886 IDs). All three acceptance gates pending.
- [x] **2.** Independently join original ITEM
  `placed_block` registry for 4 rail IDs and
  examine alternate graph/signal writers:
  minecart crossing/detection, `PoweredRailBlock`
  up-to-8 segment power propagation, activator
  rail variants, BlockState components and
  worldgen/structure placements; do not assert
  complete minecart movement. **DONE 2026-10-10 / Stage 3A-5.2:** [original ZIP ITEM and actual non-item rail authors](../../research/PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md): 4 exact ordinary BlockItems joined 1:1 with 4 registered rail blocks; all seven item lifecycle declarations own `BlockItem`; `RailState` multi-cell, DetectorRail cart+analog signal, PoweredRail and activator distinct block-instance/vehicle effects, verified mineshaft direct rail creation and generic templates audited. No new class promotion or gameplay PASS.
- [ ] **3.** Source/target local chart,
  `RailShape` facing/slope under local gravity,
  `RailState` target coordinates across seams,
  physical callbacks, dynamic signal semantics,
  collision/render and movement cross-phase
  contracts. List runnable game/CI tests, no PASS
  without actual running.
- [ ] **4.** Reconcile exactly reviewed rail
  class/ID/method owners and pending class statuses
  against original CI ZIP; create another
  independent owner-family card. Keep
  DaylightDetector source pending until then.

## Resume checkpoint

**Last completed:** **Stage 3A-5.2** exact NeoForge
registered rail ITEM join and non-item creation,
graph, entity/signal author research.
[Complete evidence](../../research/PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md).
4/4 exact `placed_block` → same-named ordinary
`BlockItem`; 7/7 item declaration owners all
`BlockItem` per item. Other authors:
`RailState.place/connectTo`, detector minecart
entry and 20-tick/analog output,
PoweredRail eight-rail signal recursion with separate
activator/powered block instances,
AbstractMinecart type-specific track consumers,
verified direct MineshaftPieces `Blocks.RAIL`
placement and generic StructureTemplate write.
No new registered class reviewed this turn.

Full ledger unchanged: **66/241 source+reflection
reviewed (174/1060 BLOCK IDs)**, **175/241
REVIEW_PENDING (886 IDs)**.
`DaylightDetectorBlock` separate pending.
All 241 ASM/Planet adapter/gameplay gate fields
REVIEW_PENDING.

**NEXT FIRST unchecked task**: microtask **3**,
source-local/physical frame chart, `RailShape`
slopes, seam/corner `RailState` contract,
neighbor callback and minecart cross-phase
tests. One bounded docs commit and stop.
