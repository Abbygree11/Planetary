# Phase 2 Stage 3A — class-level owner-disposition ledger

**Target:** Minecraft 1.21.1 / NeoForge 21.1.215 / Planetary `2.0`.

**Primary TSV:** [PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).

## Verified scope and what the numbers mean

This is an **exhaustive actual registry-derived class roster**, **NOT
an exhaustive semantic class or gameplay audit**. Generated from the
original 2026-10-09 `phase2-neo1211-registry-census` artifact
([CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact ID `11643813158`, revision `aa395729`).
The exact original `java_class` names and counts of associated
registered block IDs were reconciled for the class roster; this
checkpoint's 12 reviewed IDs were additionally checked by name.

| Measurement | Verified count |
|---|---:|
| Registered BLOCK IDs in original census | **1060** |
| Registered concrete Java block classes | **241** |
| Concrete classes with source-reviewed lifecycle owner chains in this **class-level ledger** | **12** |
| Registered block IDs represented by those 12 | **18** |
| Concrete classes explicitly `REVIEW_PENDING` | **229** |
| IDs belonging to those 229 pending classes | **1042** |
| NeoForge patched-bytecode/ASM owner sites fully reviewed here | **0** |
| Gameplay-accepted classes from this research ledger | **0** |

The 22 earlier researched algorithm-owner **clusters** are
**not** secretly counted as 22 extra class-level completed rows.
This keeps the evidence levels and class denominator consistent.

## TSV interpretation

One row per actual **concrete runtime class**, NOT per BLOCK ID.
The literal `SEE_REGISTRY_ARTIFACT` in pending rows means
the ID values are available in the authoritative raw
`phase2-neo1211-block-registry.tsv` CI artifact but have **not
yet** been promoted into per-class reviewed evidence.
`registered_block_ids_count` is derived from the source artifact,
not estimated from names.

The five owner columns contain **the declaring class of one exact
method signature**, not hypothetical values of a block property:

- `getStateForPlacement_owner` =
  `getStateForPlacement(BlockPlaceContext)`
- `canSurvive_owner` =
  `canSurvive(BlockState, LevelReader, BlockPos)`
- `updateShape_owner` =
  `updateShape(BlockState, Direction, BlockState, LevelAccessor, BlockPos, BlockPos)`
- `randomTick_owner` =
  `randomTick(BlockState, ServerLevel, BlockPos, RandomSource)`
- `setPlacedBy_owner` =
  `setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)`

Declaring owners are from **exact 21.1.215 class-hierarchy reflection**.
Reflection cannot establish an actual caller's JVM `INVOKE`
owner, which Mixin handlers apply, or the real patched NeoForge
call path. The `registry_dispatch_evidence` column separates
those assertions. `SOURCE_REVIEWED_INTEGRATION_PENDING` means
source call flow was inspected for this owner/subclass, NOT that
the current Planetary adapter handles all cases. All NeoForge
bytecode, Planet acceptance and gameplay statuses remain pending
until later separate gates.

Comparative source is pinned to
[hackersense/OptiFine-Source `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).
This is not used as proof that the exact NeoForge-patched code
has identical bodies. Full 12-class source review:
[PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md).

### Shared base dispatch is conditional, not uniform

The 12 `BushBlock` descendants represent 18 registered IDs with
their **own effective** `canSurvive` declaring class. All 18 are
source-reviewed at the relevant owner chains. `CropBlock` and the
lower half of `DoublePlantBlock` can still delegate into the
base `BushBlock.canSurvive`, so the existing
`BushBlockLocalSupportMixin` can cover **only that delegated
portion**, not all growth, upper halves, fluids, or side attachment
paths. `MushroomBlock` implements an independent support check;
`MangrovePropaguleBlock` has an independent HANGING branch.

The 19th independently dispatched Bush-descendant registry ID is
`minecraft:sea_pickle`, class `SeaPickleBlock`, which is left
`REVIEW_PENDING` here despite previous cluster-level research.
That is intentional, not a lost row.

## Audit invariants / next micro-task

Expected class status distribution: **12 reviewed + 229 pending = 241**.
Expected ID distribution: **18 reviewed + 1042 pending = 1060**.
No unreviewed row is classified `NOT_APPLICABLE`, `PASS`
or `IMPLEMENTED` by automatic heuristics.

Next separate packet (2.3A-1 task 4): review the next bounded
8–15 concrete-class **owner cluster** with the same evidence
standard. Update the existing TSV rows in place and update
the counters. Do not add arbitrary mixins; Phase 3A owner
census is not implementation.

## 2026-10-10 second 12-class Bush-inherited audit: incremental ledger update

The class roster is unchanged (241 real concrete classes / 1060
registered BLOCK IDs) but the **evidence statuses are updated**:

| Ledger status | Concrete classes | Associated Block IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **24** | **52** |
| `REVIEW_PENDING` | **217** | **1008** |
| **Total** | **241** | **1060** |

The new 12-class, 34-ID source audit is:
[PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md).
The first 12-class, 18-ID source audit remains:
[PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md](PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md).

This supersedes only the **older counts and next task** printed in
earlier sections of this ledger guide. Interpret their numbers as
historical snapshots, not current totals.
`SOURCE_REVIEWED_INTEGRATION_PENDING` still means actual
method-declaration owners + comparative vanilla source; exact
NeoForge-patched ASM, adaptation and gameplay remain pending.
Next task is Stage 2.3A-2, one new owner-family batch.



## 2026-10-10 Stage 2.3A-2.1: attachments batch / current totals

- 11 additional real concrete block classes / **29 IDs**
  studied in [FACE_ATTACHMENT_AUDIT](PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md).
- **35 SOURCE_REVIEWED_INTEGRATION_PENDING** / **206 REVIEW_PENDING**
  out of **241** registered classes, representing **81/1060**
  vs **979/1060** registered block IDs.
- **Crucial provenance downgrade specific to these new 11 rows**:
  `registry_dispatch_evidence =
  COMPARATIVE_SOURCE_OWNER_ONLY_NEOFORGE_REFLECTION_RECHECK_PENDING`.
  The owner columns are source-declaration/inheritance matches,
  **not exact patched NeoForge reflection verification yet**.
  This differs from the preceding 24 classes'
  `REFLECTION_OWNER_VERIFIED` results and is not silently merged
  into that evidence level.
- The new 11 rows deliberately use
  `SEE_REGISTRY_ARTIFACT_ID_JOIN_PENDING` instead of unverified
  individual block-ID mappings; per-class registry **count**
  values remain from the original artifact.
- `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`,
  `gameplay_acceptance`: REVIEW_PENDING **for all 241**.
  No class PASS, no actual code modifications.
- The counts printed in earlier sections are historical
  snapshots. This block supersedes them.


## 2026-10-10 Stage 2.3A-2.2: compiled NeoForge owner/item join

The previous `COMPARATIVE_SOURCE_OWNER_ONLY_NEOFORGE_REFLECTION_RECHECK_PENDING`
status of the 11 attachment rows has been resolved by reading the
**original unmodified ZIP** generated by Java 21 / NeoForge 21.1.215
run 37988064055, artifact 11643813158, SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
All **35 source-reviewed class rows** now have
`registry_dispatch_evidence=REFLECTION_OWNER_VERIFIED`, with
the same method exact-signature scope as defined above. All
11 new attachment class rows now carry individually
verified `minecraft:` block IDs. 3 wall torch block IDs have
no direct registered item; their variant is chosen by the
3 standing `StandingAndWallBlockItem` entries.

Original 11-class source/reflect/item/Planet adapter evidence:
[FACE_ATTACHMENT_AUDIT addendum](PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md#2026-10-10-addendum--ci-artifact-exact-owner-and-item-reconciliation).
Scope totals **unchanged**: 35/241 source-reviewed, 206/241
pending; 81/1060 reviewed-class block IDs, 979/1060 pending.
Compiled nearest method declaration is **not** ASM INVOKE-site
or NeoForge-patched method-body verification. `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and `gameplay_acceptance`
remain REVIEW_PENDING.


## 2026-10-10 Stage 2.3A-2.3: non-FACING growth topology

[New source + compiled reflection audited 12-class family](PHASE2_STAGE3A_NONFACING_GROWTH_GRAPHS_1_21_1.md)
covers **12 block IDs that have no candidate orientation
property**, yet their state lifecycle depends on UP/DOWN,
tangent XZ, grow/survive/scheduled ticks, directional
head/body graph, water, direct bonemeal creation and
item-placed alternate states.

Exact total is now **47 SOURCE_REVIEWED_INTEGRATION_PENDING**
classes (**93** block IDs), with
**194 REVIEW_PENDING** classes (**967** block IDs);
**241** classes and **1060** registry IDs unchanged.
All 47 source-reviewed have an exact compiled NeoForge
reflection declaring owner join. **NeoForge patched-method
ASM, actual Planet adapter correctness and gameplay**
remain REVIEW_PENDING even for these 47.
5 of the new runtime variant blocks have no directly
registered BlockItem, including `bamboo_sapling`
which is authored by `BambooStalkBlock.getStateForPlacement`
on an ordinary bamboo item placement.
Earlier summaries' figures are historical snapshots;
these current totals supersede them.
