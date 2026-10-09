# 2.3A-3 — tangent connectivity and multi-face graph owners

**Status:** ACTIVE / 1 of 4 research packages completed; no gameplay PASS.
[Phase-2 queue](../phase-02.md) ·
[2.3A-2.4 registry reconciliation](../../research/PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md) ·
[Class-level TSV](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).

## Real registry candidate cohort (NOT semantic dispositions)

**10** still-`REVIEW_PENDING` concrete Java implementation classes /
**71** registered BLOCK IDs, verified against original
NeoForge 21.1.215 artifact 11643813158:

| Concrete class | Registered IDs (count) | Initial candidate mechanism |
|---|---:|---|
| `FenceBlock` | 12 | P25 connected graph |
| `FenceGateBlock` | 11 | P25 connected graph |
| `WallBlock` | 25 | P25 connected graph |
| `IronBarsBlock` | 2 | P25 connected graph |
| `StainedGlassPaneBlock` | 16 | P25 connected graph |
| `TripWireBlock` | 1 | P25 connected graph |
| `TripWireHookBlock` | 1 | P25 connected graph |
| `VineBlock` | 1 | P26 multi-face attachment |
| `GlowLichenBlock` | 1 | P26 multi-face attachment |
| `SculkVeinBlock` | 1 | P26 multi-face attachment |

These candidates are **NOT equivalent**: a fence/pane tangent
connection is not a vine six-face survival/expansion path, nor
a tripwire signal graph. P36 / Phase 7A redstone consumer
contracts must remain separate. Source method and exact
owner discovery can split this list across later calls;
never blend incompatible algorithms merely to hit a count.

## Small independently committable tasks

- [x] **1.** Inspect pinned Minecraft 1.21.1 source paths and real NeoForge 21.1.215 reflection owners for these up-to-10 classes (or a coherent 8–10-class subset); record placement, survival, update, connectivity queries and source-vs-target chart, including inherited/overridden paths. **DONE 2026-10-10:** [8-class P25/P26/P27 comparative source and original CI owner audit](../../research/PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md), 69 actual registered IDs; `FenceBlock`, `FenceGateBlock`, `WallBlock`, `IronBarsBlock`, `StainedGlassPaneBlock`, `VineBlock`, `GlowLichenBlock`, `SculkVeinBlock`. Two `TripWire` variants still pending. **55/241** source+reflection reviewed, **186** pending; NO ASM/Planet/gameplay PASS.
- [ ] **2.** Independently join original ITEM registry and alternative state/interaction authors; distinguish `BlockState` neighboring connections from power propagation, `getShape`, waterlogging and propagation. Commit new evidence.
- [ ] **3.** Reconcile no-property multi-face graphs, BlockStateBase cached state/shape and physical neighbor callbacks; isolate Phase-2 boundary versus Phase 5/7A. Do not implement or claim runtime acceptance.
- [ ] **4.** Check per-class `REVIEW_PENDING`/source/reflection/ASM/gameplay statuses against the exact registry artifact. Commit one checkpoint and plan the next bounded family while classes remain unreviewed.

## Resume checkpoint

- Last completed: **2.3A-3.1** original NeoForge reflection + pinned 1.21.1 source for 8 registered connection/attachment classes, **69** exact block IDs. [Audit](../../research/PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md).
- Cumulative ledger: **55/241** source+reflection reviewed (162 IDs), **186/241** REVIEW_PENDING (898 IDs). `TripWireBlock` and `TripWireHookBlock` not source reviewed yet; remain pending.
- Next FIRST action: **item 2** above, independently join the full ITEM creator/alternate author and interaction paths for these 8 before moving onto item 3; do not claim compiled ASM or gameplay acceptance.
- No Java/runtime changes and no unaccepted gameplay claims.
