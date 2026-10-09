# Phase 2 Stage 3A-5 — rail topology/ascending slopes and signal graph owners

**Status: NEXT / task 1 not started.**
Branch `2.0`; Minecraft 1.21.1 / NeoForge 21.1.215.

[Phase 2](../phase-02.md) ·
[Original 241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[Exact 3A-4.4 reconciliation](../../research/PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md)

## Original compiled registry selection (all still REVIEW_PENDING)

These 3 concrete registered BLOCK classes represent
**4 exact registered IDs**, proven against unmodified
NeoForge 21.1.215 original CI ZIP `11643813158`.
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

- [ ] **1.** Verify original runtime NeoForge 21.1.215
  class/ID and **five exact method signature owners**
  plus full pinned comparative Minecraft 1.21.1
  class hierarchy/source: `RailBlock`,
  `DetectorRailBlock`, `PoweredRailBlock`,
  `BaseRailBlock`, `RailState`,
  graph connectivity, placement/support, updateShape,
  onPlace/neighborChanged and physical rise/fall.
  Check alternate state authors. Promote **only**
  these 3 rows after actually auditing them, never
  DaylightDetector. Commit evidence and checkpoint.
- [ ] **2.** Independently join original ITEM
  `placed_block` registry for 4 rail IDs and
  examine alternate graph/signal writers:
  minecart crossing/detection, `PoweredRailBlock`
  up-to-8 segment power propagation, activator
  rail variants, BlockState components and
  worldgen/structure placements; do not assert
  complete minecart movement.
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

Last completed: Stage **3A-4.4** reconciled all
**241** original registered classes (1060 IDs)
and the **63** already source+reflection reviewed
classes (170 IDs) with all five exact method
declaring owners; all four digests matched.
**178/241** remain `REVIEW_PENDING`
(890 IDs), including these **3 rail classes
/ 4 IDs** and separately the daylight detector.
All 241 ASM/Planetary/gameplay verdicts pending.

**NEXT FIRST unchecked task: checkbox 1**.
Commit ONLY that package, then stop.
No Java implementation, gameplay acceptance or
full Phase 2 completion implied.
