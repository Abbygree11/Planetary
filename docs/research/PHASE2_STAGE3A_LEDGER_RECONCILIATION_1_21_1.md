# Phase 2 Stage 3A-2.4 — independent ledger-to-registry reconciliation

**Date:** 2026-10-10. **Branch:** `2.0`. Target **Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21**.
**Status:** registry and reflection-declaration metadata **RECONCILED**;
`SOURCE_REVIEWED_INTEGRATION_PENDING` is a source-evidence label only,
**not implemented / bytecode-verified / in-game PASS**.

## Primary evidence

Original GitHub Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
source revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`,
artifact `phase2-neo1211-registry-census`, ID **11643813158**.
Downloaded original ZIP, verified SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Inspected actual:
- `phase2-neo1211-block-registry.tsv` — **1060** individual BLOCK rows
- `phase2-neo1211-item-registry.tsv` — **1333** ITEM rows
- `phase2-neo1211-state-properties.tsv` — **1712** property rows

Compared with the GitHub-committed
[`PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv)
as of commit `26e633621c4850eb4235d66a22b6423462f38cc5` (before this docs-only checkpoint).

## Independently reconciled invariants — all PASS

| Gate | Result | Meaning |
|---|---|---|
| Class roster | **241/241 exact**, zero missing/extra/duplicate | Full runtime class names checked, not guessed Java source names |
| Per-class registry ID count | **241/241 exact**, sum **1060** | Including the 168 IDs whose concrete Java class is plain `Block` |
| Reviewed registry ID membership | **47/47 exact**, **93 actual IDs** | Every `minecraft:` string reconciled as a *set* per class |
| Reviewed method declaration owners | **47/47 exact**, five signatures | Not inferred by method name or class naming |
| Reviewed + pending distribution | **47 + 194 = 241** | Pending IDs **967**; original block count unchanged |
| TSV data shape | **241** unique 16-column rows | No malformed rows |
| Reviewed provenance | **47/47** `REFLECTION_OWNER_VERIFIED` | Pinned comparative source URLs retained |
| Pending provenance | **194/194** `REVIEW_PENDING` | No owner, semantic mechanism or gameplay PASS silently inferred |
| Runtime acceptance columns | **241/241 remain REVIEW_PENDING** | NeoForge patched-bytecode, Planet adapter behavior, gameplay acceptance not conflated with reflection |

Five exact-signature method dimensions were matched against the
`effective_method_owners` column in the original ZIP, by **full
parameter signature**, and **not** by the first method of the same name:
`getStateForPlacement(BlockPlaceContext)`,
`canSurvive(BlockState,LevelReader,BlockPos)`,
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`,
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`, and
`setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`.
In the raw effective-owner string, overload implementations are
separated by `;` **inside the same method-name entry** and
method names by `|`. For example `KelpBlock` has a
`getStateForPlacement(LevelAccessor)` declaration in
`GrowingPlantHeadBlock` **and** a
`getStateForPlacement(BlockPlaceContext)` declaration in
`KelpBlock`. A first-match-only parser would incorrectly fail.

### Independent exact-content fingerprints

Canonicalization is lexicographic sort by **fully qualified class name**,
ASCII text and trailing newline per class. Fingerprints are
**FNV-1a 32-bit diagnostic checksums**, not cryptographic hashes.
Four exact-match checks independently computed on
the original ZIP data and committed GitHub TSV:

| Canonical series | Original ZIP | Committed ledger |
|---|---|---|
| `class + "|" + ID count + "\\n"` for **all 241** classes | `0xf188a064` | `0xf188a064` |
| `class + "|" + sorted registered IDs + "\\n"` for **47** reviewed classes | `0x6c35dd77` | `0x6c35dd77` |
| `class + "|" + five exact declaring owners + "\\n"` for **47** | `0xacd03109` | `0xacd03109` |
| `class + "|" + sorted IDs + "|" + five owners + "\\n"` for **47** | `0x9fc5dbf5` | `0x9fc5dbf5` |

Additionally checked actual source artifact rows, class/key uniqueness,
signature-precise extraction, presence and status of source evidence,
and unchanged gameplay gates. Fingerprints supplement, rather
than replace, exact row/key and provenance checks.

## Class counts and evidence: do not confuse stages

**241** is *the entire concrete registered block implementation
roster*, NOT 241 different orientation features, and NOT 241
completed semantic audits.
The 47 rows tagged `SOURCE_REVIEWED_INTEGRATION_PENDING`
represent **comparative source path + runtime declaring owner**
evidence (93 BLOCK IDs); **194** remain explicitly
`REVIEW_PENDING` (967 BLOCK IDs).
A registered item may author a state belonging to a
*different block class*; e.g. ordinary bamboo item placement
can yield `bamboo_sapling`; distinct `ItemNameBlockItem`
and `StandingAndWallBlockItem` pathways are described
in their corresponding family audits. Stage 3B must still
reconcile the full item creator graph.

All earlier owner-cluster descriptions (22 clusters),
the **103 exact `getStateForPlacement(BlockPlaceContext)`
owners** vs 104 including overloads, the 528
property-name candidates, and prior per-stage counts
are **historical or different-denominator metrics**,
not extras to add to the 47/241 numerator.

## Known limitations and next independently committed tranche

This census uses dynamic **reflection of the nearest
method-declaring class**, not JVM INVOKE call-site analysis,
exact NeoForge-transformed body/patch diff,
actual Mixin handler matching, end-to-end
Planet frame/tangent support, worldgen, multiplayer,
fluids, redstone graphs, or six-face/seam gameplay.
No runtime code changed, no compilation or client game
test performed by this metadata-only checkpoint.

First next **10 classes / 71 registered IDs** remain
`REVIEW_PENDING`, picked from the **actual** ZIP registry:
`FenceBlock` (12), `FenceGateBlock` (11),
`WallBlock` (25), `IronBarsBlock` (2),
`StainedGlassPaneBlock` (16),
`TripWireBlock` (1), `TripWireHookBlock` (1),
`VineBlock` (1), `GlowLichenBlock` (1),
`SculkVeinBlock` (1).
They share the research theme **tangent/four-way or
multi-face connectivity**, but not one interchangeable algorithm.
See [next packet card](../phases/phase-02/02a-block-graph-owners.md);
investigate as actual owner-specific families and split
if call-path differences demand it. **Do not promote any
of these to reviewed until the next committed source audit.**
