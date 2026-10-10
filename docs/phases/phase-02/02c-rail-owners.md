# Phase 2 Stage 3A-5 — rail topology/ascending slopes and signal graph owners

**Status: DONE / all 4 bounded rail research packages completed; runtime/gameplay still PENDING.**
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
- [x] **3.** Source/target local chart,
  `RailShape` facing/slope under local gravity,
  `RailState` target coordinates across seams,
  physical callbacks, dynamic signal semantics,
  collision/render and movement cross-phase
  contracts. List runnable game/CI tests, no PASS
  without actual running. **DONE 2026-10-10 / 3A-5.3:** [ten `RailShape` variants, physical rail endpoint/slope and reciprocal port chart, RailState XYZ/XZ assumptions, multi-cell, detector/powered rail, minecart and phase 3/5/7A/8 boundaries](../../research/PHASE2_STAGE3A_RAIL_SEAM_FRAME_CONTRACT_1_21_1.md); 11 sets of proposed game/CI fixtures across all faces/edges/corners. No runtime acceptance or new class promoted.
- [x] **4.** Reconcile exactly reviewed rail
  class/ID/method owners and pending class statuses
  against original CI ZIP; create another
  independent owner-family card. Keep
  DaylightDetector source pending until then. **DONE 2026-10-10 / 3A-5.4:** [fresh independent original ZIP/compiled exact-signature recheck of all 66 reviewed classes, 174 exact registered IDs and all five method owners](../../research/PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md); four independent FNV checksums match, rail three classes/four block IDs match, all ASM/Planet/gameplay acceptance PENDING. Created [3A-6 environment + pressure sensor cohort](02d-environment-pressure-sensor-owners.md) with 3 `REVIEW_PENDING` concrete classes / 16 exact BLOCK IDs, including `DaylightDetectorBlock`.

## Resume checkpoint

**Last completed: Stage 3A-5.4** original unmodified
NeoForge 21.1.215 ZIP reconciliation:
all **241** class/count rows, all **66** previously
source+reflection reviewed classes (**174 exact BLOCK
IDs**) and **five exact-signature declaring owners**
per reviewed class checked against independently
decoded original ZIP. Fingerprints match:
`0xf188a064` roster, `0x6c5c67d7` IDs,
`0x57a01200` owners, `0xb3945a6e` combined.
[Full exact checkpoint](../../research/PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md).

**Ledger unchanged:** 66/241 source+reflection
reviewed (174/1060 registered BLOCK IDs),
175/241 REVIEW_PENDING (886 IDs). All three
ASM/Planet runtime/gameplay statuses REVIEW_PENDING,
and Phase 2/7A + minecart acceptance unfinished.

**NEXT FIRST independent card:** [Stage
3A-6.1 environment and pressure sensors](02d-environment-pressure-sensor-owners.md)
checkbox 1, `DaylightDetectorBlock` (1 ID),
`PressurePlateBlock` (13 IDs),
`WeightedPressurePlateBlock` (2 IDs):
**3 still-pending concrete classes/16 IDs**.
No code changes or runtime PASS.
