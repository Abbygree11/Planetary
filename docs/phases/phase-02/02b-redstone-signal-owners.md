# Phase 2 Stage 3A-4 — redstone connection, sensing and signal-facing owners

**Status:** DONE — 4 bounded research packages, no runtime/gameplay PASS; entire Stage 3A/Phase 2 incomplete.
Branch `2.0`, Minecraft 1.21.1 / NeoForge 21.1.215.
This card does **NOT** mark additional classes reviewed.

[Phase 2 plan](../phase-02.md) ·
[Exact Stage 3A-3.4 reconciliation](../../research/PHASE2_STAGE3A_GRAPH_COHORT_RECONCILIATION_1_21_1.md) ·
[241-class disposition ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).

## Candidates drawn from the real original CI registry

These **11 original candidate** concrete BLOCK classes
correspond to **12 registered BLOCK IDs**; after Stage 3A-4.4,
**8/11** have source+reflection owner review (8 IDs),
**3/11** remain `REVIEW_PENDING` (4 IDs). Independently
checked against artifact `11643813158`.
They are one **research cluster of interacting signal
nodes**, NOT one interchangeable implementation or
authorization to rewrite NeoForge signal evaluation.

| Actual registered class | BLOCK IDs (count) | Candidate mechanism — verify source before relabeling |
|---|---:|---|
| `TripWireBlock` | 1 | string/tension/hook and attachment network |
| `TripWireHookBlock` | 1 | string/tension/hook and attachment network |
| `RedStoneWireBlock` | 1 | four-way redstone wire connectivity, neighbor/update propagation |
| `RepeaterBlock` | 1 | DiodeBlock-derived directed delay/comparison |
| `ComparatorBlock` | 1 | DiodeBlock-derived directed delay/comparison |
| `ObserverBlock` | 1 | physical side observation and scheduled pulse |
| `DetectorRailBlock` | 1 | rail geometry/sensing and power propagation |
| `PoweredRailBlock` | 2 | rail geometry/sensing and power propagation |
| `TargetBlock` | 1 | hit face and powered state |
| `RedstoneLampBlock` | 1 | neighbor signal and lit tick |
| `DaylightDetectorBlock` | 1 | world signal sample, block entity interaction |

`PoweredRailBlock` has **2** registered block IDs;
every other candidate has **1**.

**Boundary note:** Phase 2 owns block orientation,
placement/state authoring, local-support/shape
callbacks and physical target mapping; Phase 7A
owns redstone signal semantics, update ordering,
direct/weak output and world network causality.
Rail traversal/geometry and later entity logic
have separate owners. This cluster must split
further by independently proven owner family
where needed.

## Independently committed microtasks

- [x] **1.** Select a coherent subset of **8–11** of these 11 still pending concrete classes, verify exact NeoForge 21.1.215 reflection owners and pinned Minecraft 1.21.1 source placement, neighbor/update, support, scheduled tick and interaction/alternative authors. Preserve exact `P01–P40` labels only when proven; commit evidence and promote only actually reviewed TSV rows. Split if 11 are too different. **DONE 2026-10-10 / 3A-4.1:** [8-class 8-ID source and exact NeoForge declaring owner audit](../../research/PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md). TripWire/Hook, RedStoneWire, Repeater, Comparator, Observer, Target, RedstoneLamp reviewed; **3 other candidates (4 IDs: DetectorRail, PoweredRail and DaylightDetector) still PENDING**. Total 63 reviewed/178 pending, no ASM/Signal/gameplay PASS.
- [x] **2.** Independently join actual item creator records and examine off-item redstone writers (neighborChanged, signal sources, projectiles/entity detection, scheduled ticks, BlockEntity) with exact creation/notification chains. Commit evidence without claiming Phase 7A acceptance. **DONE 2026-10-10 / 3A-4.2:** [original NeoForge 1333 ITEM census join + pinned source alternate-author audit](../../research/PHASE2_STAGE3A_REDSTONE_ITEM_ALTERNATE_AUTHORS_1_21_1.md), 8/8 registered placed blocks, **6 ordinary BlockItems + 2 ItemNameBlockItem aliases** (`minecraft:string`→`minecraft:tripwire`, `minecraft:redstone`→`minecraft:redstone_wire`). All 7 item methods declare in `BlockItem`; separate cable, wire, diode, BE, observer, projectile and lamp writers documented. Counts stay 63 reviewed/178 pending; bytecode/runtime/7A/gameplay pending.
- [x] **3.** Separate physical directions from local edge semantics and redstone signal port identity at every seam; investigate recursion, update order, rail/tension graphs, dynamic shape/cached support and Phase 2/7A contract. Commit explicit hypotheses and tests, not runtime PASS. **DONE 2026-10-10 / 3A-4.3:** [source-level signal ports, physical update callbacks, tripwire 41-step walk, wire POWER recursion/shape caching, diode/observer tick priority and 2-way ports](../../research/PHASE2_STAGE3A_REDSTONE_PORT_TOPOLOGY_1_21_1.md). Rails/daylight source glimpses used only for Phase 7A/P28 handoff, NOT class promotion. All eight existing reviewed class dispositions and three deferred candidates retain their status; ASM/Planet/gameplay PENDING.
- [x] **4.** Independently reconcile every newly reviewed class against original ZIP and keep every other class including deferred candidates visibly `REVIEW_PENDING`; checkpoint and queue next independent family. **DONE 2026-10-10 / 3A-4.4:** [original unmodified CI ZIP vs all 241 classes and 63 reviewed exact IDs+five method owners](../../research/PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md), four independently computed hashes MATCH. `DetectorRailBlock`, `PoweredRailBlock`, `DaylightDetectorBlock` still pending; next separate [3A-5 rail graph card](02c-rail-owners.md) includes also ordinary `RailBlock` (3 classes / 4 registered IDs). No patched ASM/Planet/gameplay PASS.

## Resume

**Last completed:** Stage **3A-4.4**, all **241**
class/1060 ID census matches original CI ZIP;
**63 source+compiled reflection-reviewed classes**
(170 exact registered IDs) and all five declaring
method signatures match four canonical checksums:
`0xf188a064`, `0xdb8afbd5`, `0x6052a7cf`,
`0x66be3dcb`. [Full reconciliation](../../research/PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md).
Full ledger **63/241 reviewed / 178/241 pending**,
170/890 registered ID split. No bytecode, runtime or
gameplay acceptance, no Java changes.

**NEXT FIRST unchecked task:** new
[Stage 3A-5 rail topology owners card](02c-rail-owners.md),
checkbox **1**; `RailBlock`, `DetectorRailBlock`,
`PoweredRailBlock` (**3** pending concrete classes /
**4** exact block IDs), with unregistered
`BaseRailBlock`/`RailState` methods.
`DaylightDetectorBlock` remains separate pending.
