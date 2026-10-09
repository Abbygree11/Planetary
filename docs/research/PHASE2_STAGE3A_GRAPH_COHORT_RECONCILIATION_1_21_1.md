# Phase 2 Stage 3A-3.4 — connected-graph cohort vs original NeoForge census

**2026-10-10 / branch `2.0`** — one bounded documentary
status/integrity checkpoint. Minecraft **1.21.1**,
NeoForge **21.1.215**, Java 21. Original GitHub commit
before this checkpoint: `1abe1c6a6f39f2bfff6cee0615cedac6220d1a27`.

**Outcome: PASS for exact registry metadata reconciliation
only.** Not an ASM/Mixin/Planet adapter/gameplay PASS.

## Primary evidence and independent comparison

Downloaded original unchanged [GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055)
artifact `11643813158` (source commit
`aa39572950a15403ea0a9003eefccf3bf6675ff7`).
ZIP SHA-256:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.

Original ZIP contents and primary row counts:

| Exact ZIP member | Rows |
|---|---:|
| `phase2-neo1211-block-registry.tsv` | **1060** |
| `phase2-neo1211-item-registry.tsv` | **1333** |
| `phase2-neo1211-state-properties.tsv` | **1712** |
| Unique fully qualified concrete BLOCK class names | **241** |

Independently parsed the source ZIP and compared to the
GitHub branch `2.0` [16-column class ledger](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv),
including *all* class names + per-class BLOCK-ID counts and
the **55 reviewed classes**' complete sets of exact
registered block IDs and the five original exact-signature
method-declaration owner columns:
`getStateForPlacement(BlockPlaceContext)`,
`canSurvive(BlockState,LevelReader,BlockPos)`,
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`,
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`,
`setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`.
Signature matching treats overloads independently; never
join by bare method name.

Canonical comparison: sort by fully qualified Java class
name, sort registered IDs within each reviewed class, concatenate
ASCII rows with a final newline, FNV-1a 32-bit. **FNV is a
diagnostic fingerprint, not a cryptographic proof**; exact
counts, unique class keys, method-owner/signature parsing,
and provenance fields were checked in addition.

| Canonical input | Original source ZIP | Branch TSV | Result |
|---|---|---|---|
| All 241 `class + "|" + ID count + "\\n"` | `0xf188a064` | `0xf188a064` | MATCH |
| 55 reviewed `class + "|" + sorted IDs + "\\n"` | `0x5e998532` | `0x5e998532` | MATCH |
| 55 reviewed `class + "|" + 5 nearest owners + "\\n"` | `0x98207d0` | `0x98207d0` | MATCH |
| 55 reviewed `class + "|" + IDs + "|" + owners + "\\n"` | `0x1f4133ac` | `0x1f4133ac` | MATCH |

Other invariants on committed TSV:

- **241/241 unique concrete class names**, none duplicated;
  **1060 registered BLOCK IDs** summed across classes.
- **55 `SOURCE_REVIEWED_INTEGRATION_PENDING`**
  (162 registered BLOCK IDs); each has
  `REFLECTION_OWNER_VERIFIED`, source URL and concrete
  exact owner columns.
- **186 `REVIEW_PENDING`** (898 BLOCK IDs); their exact
  owner, semantics and source-level disposition were
  **not** promoted automatically.
- **All 241 rows** remain `REVIEW_PENDING` for each
  `neoforge_patch_bytecode_review`,
  `planet_adapter_acceptance` and
  `gameplay_acceptance`. Compiled *declaration owner*
  reflection is not an ASM INVOKE-site or applied Mixin
  handler check.
- No malformed TSV rows; all **16** tab-delimited
  columns preserved. Source/patch/runtime/gameplay gates
  remain differentiated.

## The Stage 3A-3 graph cohort: 8 source owners / 69 BLOCK IDs

| Concrete class | Exact registry ID count | Source-level disposition | Compiled nearest declaration |
|---|---:|---|---|
| `FenceBlock` | 12 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `FenceGateBlock` | 11 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `WallBlock` | 25 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `IronBarsBlock` | 2 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `StainedGlassPaneBlock` | 16 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `VineBlock` | 1 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `GlowLichenBlock` | 1 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |
| `SculkVeinBlock` | 1 | `SOURCE_REVIEWED_INTEGRATION_PENDING` | `REFLECTION_OWNER_VERIFIED` |

All eight were previously reviewed across independent
[subtask 1 — method/source graph authors](PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md),
[subtask 2 — exact item and alternative authors](PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md),
and [subtask 3 — local/physical frame and shape/cache](PHASE2_STAGE3A_GRAPH_SHAPE_CALLBACK_CHART_1_21_1.md).
No new semantic claims or gameplay approval in 3A-3.4.

### Independent ITEM census recheck

Original `phase2-neo1211-item-registry.tsv` was joined
by **`placed_block`**, not guessed by item name.
All **69/69** block IDs in these eight classes have
one **uniquely registered ordinary `BlockItem`** item,
and all 69 entries have `registry_id=placed_block`,
`block_item=true`. This does NOT exclude non-item
state authors previously proven by source (random
vine spread, lichen/spreader, sculk propagation,
gate/open redstone, or post-placement item components).

**Explicitly not classified:** `TripWireBlock` and
`TripWireHookBlock` still are **two**
`REVIEW_PENDING` classes / two IDs, not conflated
with fence connection. Their wire/hook signal
network belongs in the next batch. All 186 unknown
classes remain unknown; no silent promotion from
a candidate table.

## Unaccepted runtime mechanisms carried forward

- Exact **NeoForge 21.1.215 patched method bodies /
  bytecode INVOKEs** and required Mixin matches are
  not established by the ZIP.
- **Six-face and seam** placement, connected collision,
  `BlockStateBase.Cache` vs per-block immutable
  cached `VoxelShape` and target-local support shape
  queries need in-game/compiled evidence.
- Mixed-frame candidates, not confirmed game defects:
  `MultifaceBlock.canAttachTo` uses canonical block
  support-shape and position-aware collision-shape
  together; `WallBlock` compares above target
  collision face with canonical wall tests.
- Preserve Phase-2 authors and graph state, Phase-3
  rendering/face culling, Phase-5 water behavior,
  Phase-7A signal networks and Phase-8 environmental
  generation without inventing a universal redirect.
- No production Java, ASM verification, new CI
  build or gameplay test was part of this batch.

**Next:** bounded [Stage 3A-4 redstone network source-owner card](../phases/phase-02/02b-redstone-signal-owners.md)
starts **11 real pending classes / 12 BLOCK IDs** as
candidates; its first source research is **not**
performed by this integrity-only checkpoint.
