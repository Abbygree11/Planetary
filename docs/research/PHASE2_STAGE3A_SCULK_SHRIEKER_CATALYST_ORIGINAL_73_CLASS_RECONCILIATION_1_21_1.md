# Stage 3A-8.4 — immutable NeoForge 21.1.215 241-class / 73-reviewed-class reconciliation; next sculk growth owner

**2026-10-10** · Planetary branch `2.0` · Minecraft `1.21.1` / NeoForge `21.1.215` / Java 21.

**Purpose:** final independent source/compiled-*declaration* reconciliation of sculk-shrieker/catalyst packet 3A-8, against the complete historical NeoForge runtime registry **and current live 16-column disposition ledger**. No class promotion, patched-bytecode verification, Mixin application, Java edit, runtime/CI execution or gameplay PASS. The present task must end with one documentation commit and first unchecked next-family card.

## 1. Independent inputs and exact original archive

Primary immutable compiled runtime census from [Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, generated at commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`. The **original ZIP bytes** `phase2-neo1211-registry-census.zip` were reopened and checked by Python `zipfile`/`hashlib.sha256` at this microtask; no handwritten or regenerated registry substitution.

- Actual ZIP SHA-256: `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` — **MATCH** against historical checksum.
- `phase2-neo1211-block-registry.tsv`: **1060 real BLOCK registry IDs, 241 distinct original concrete Java classes**.
- `phase2-neo1211-item-registry.tsv`: **1333 ITEM registry IDs** (not 1333 blocks).
- `phase2-neo1211-state-properties.tsv`: **1712 property records**.
- Cross-check against current actual [GitHub disposition](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) on `2.0` HEAD `df04c03466d5dd4cc0b72d56e1cff061de6d9920`: exactly **241 data rows × 16 columns**, unique full `java_class`, and total registered block count **1060**. The original all-class roster is grouped by exact class and checked by independently recomputed canonical digest (below).
- **73** currently source+compiled declaring-owner-reviewed classes account for exactly **194** original distinct BLOCK IDs. **168** unreviewed classes account for **866** original IDs. This does **not** mean 73 mechanisms, item creators, or successfully tested blocks.

No `ITEM.placed_block` rows are being confused with the source `BLOCK.java_class` roster. Prior [Stage 3A-8.2](PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_ITEM_WORLDGEN_ALTERNATE_AUTHORS_1_21_1.md) already audited the two exact Item creators.

## 2. Reconcile 73 original reviewed classes: every exact BLOCK ID and five qualified-signature method owners

**Parsing discipline:** independently read the original ZIP TSV with tabs (not lexical class names or a list reconstructed from docs); group by **full** `BLOCK.java_class`. For all **73** GitHub-ledger rows whose `disposition=SOURCE_REVIEWED_INTEGRATION_PENDING`, compare their **entire sorted** `registered_block_ids_reviewed_only` list against original `registry_id` strings belonging to that class; compare the five ledger owner columns against **the exact original `effective_method_owners` overload signatures**, not closest-looking method names. Original member declaration entries can have multiple `;`-separated overloads — select by complete argument signature. For every original BLOCK ID of one reviewed class assert all five selected declaring owners agree.

The five exact signatures are:

1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

Separate scheduled `tick`, `getTicker`, BlockEntity GameEvent and growth/`setBlock`/worldgen writers are **not** these five effective declaring-owner census slots and are not declared accepted by these matches.

### Independent immutable-original vs GitHub diagnostic fingerprints

Both original and ledger canonical strings are sorted by **full `java_class`** ascending using ordinal comparison, IDs within one class ascending; UTF-8 and one `\n` per row. The original side was recomputed independently in Python; the GitHub live ledger side separately in JavaScript. Both checked **FNV-1a 32-bit and 64-bit**, including the combined exact-class/IDs/five-owners data, not just summary counts. These are diagnostic checksums and not cryptographic proof of equal records; the original archive itself has a verified SHA-256.

| Canonical data per sorted class | Original ZIP FNV32 | Live GitHub FNV32 | Original ZIP FNV64 | Live GitHub FNV64 | Verdict |
|---|---|---|---|---|---|
| **All 241:** `fullClass|registeredIDCount\n` | `0xf188a064` | `0xf188a064` | `0x9ba3c115841e18e4` | `0x9ba3c115841e18e4` | **MATCH** |
| **73 reviewed:** `fullClass|id1,id2,...\n` | `0xf453bff2` | `0xf453bff2` | `0x7e3976d81853d2b2` | `0x7e3976d81853d2b2` | **MATCH** |
| **73 reviewed:** `fullClass|owner1|owner2|owner3|owner4|owner5\n` | `0xdc616312` | `0xdc616312` | `0xfda34bc299a40b72` | `0xfda34bc299a40b72` | **MATCH** |
| **73 reviewed:** `fullClass|ids|owner1|owner2|owner3|owner4|owner5\n` | `0x85777651` | `0x85777651` | `0xb16fcc9a93f76e51` | `0xb16fcc9a93f76e51` | **MATCH** |

Additional original-side SHA-256 fingerprints of these four canonical strings, respectively: `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d`; `a1686544ffc5d74a743fa10db9832eaa65f0f0680488944c4ae7055d705a542c`; `3e52c16f8c1f71b66a7a1583d5e3429e90ab5222bcf433a561c9fab25a7bc46b`; `ee6a49935e110becca9be46871986100a785059c393eff54308517613f256562`. These SHA values describe only **original-source canonical strings**, not an independently computed GitHub SHA-256; the independently compared values are the FNV32/64 pairs.

**All 73** reviewed ledger entries retain `REFLECTION_OWNER_VERIFIED` and actual exact ID lists and five populated owner columns. **All 168** still pending entries retain `REVIEW_PENDING`, `REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY` and `SEE_REGISTRY_ARTIFACT` rather than invented exact full-ID audits.

All **241** values in **each** of `neoforge_patch_bytecode_review`, `planet_adapter_acceptance` and `gameplay_acceptance` remain `REVIEW_PENDING`. All 241 source statuses have exactly 73 reviewed and 168 pending. These field checks are independent of the checksum matches.

## 3. Correct a misleading earlier sculk-status statement

**Correction:** `SculkVeinBlock` was already source+compiled-declaration reviewed **before** packet 3A-8 (covered in [prior Stage 3A graph reconciliation](PHASE2_STAGE3A_GRAPH_COHORT_RECONCILIATION_1_21_1.md)). Actual current `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` has:

| Exact original concrete class / ID | Original five effective declaration owners in fixed method order | Actual ledger disposition |
|---|---|---|
| `SculkVeinBlock` / `minecraft:sculk_vein` | `MultifaceBlock / MultifaceBlock / SculkVeinBlock / BlockBehaviour / Block` | **SOURCE_REVIEWED_INTEGRATION_PENDING** |
| `SculkBlock` / `minecraft:sculk` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | **REVIEW_PENDING** |
| `LightningRodBlock` / `minecraft:lightning_rod` | `LightningRodBlock / BlockBehaviour / LightningRodBlock / BlockBehaviour / Block` | **REVIEW_PENDING** |

This invalidates any earlier implication that both `SculkBlock` and `SculkVeinBlock` were source-pending or both would need future promotion. **Their source statuses are different**. Previous 3A-8.1/8.2/8.3 correctly *did not promote* either as a new row; however **“not promoted by this packet” != “still REVIEW_PENDING”**. Cross-owner inspection of already-reviewed `SculkVeinBlock` does not cause re-promotion or remove its Phase 3/5/8/7A and ASM/Planet/gameplay obligations.

No ledger mutation here: the 73 count already includes `SculkVeinBlock` (and the 194 IDs already include `minecraft:sculk_vein`). Correcting the prose must NOT increase it.

## 4. Selection of NEXT algorithm-owner family — Stage 3A-9 (not audited in this step)

**Next smallest connected, genuinely source-pending registered owner:** `net.minecraft.world.level.block.SculkBlock` → exact original `minecraft:sculk` (**1** BLOCK ID). It has **no declared BlockState orientation properties** in the original compiled registry; this does NOT imply gravity independence. Its five original nearest declaring-owner tuple is `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block`. Source `SculkBlock.attemptUseCharge`, `getRandomGrowthState`, `canPlaceGrowth` and inherited `SculkBehaviour` methods have been encountered as **additional writer graph dependencies** in 3A-8; their *full registered-class* audit is not yet complete or promoted.

[New Stage 3A-9 task card](../phases/phase-02/02g-sculk-spread-growth-owners.md) splits four independent commits: (1) original registered class five-method exact owner + complete source/inheritance/charge-growth graph, (2) exact ITEM / replacement / worldgen alternate author sources, (3) six-face/18-neighbor seam/corner growth contract and future tests, (4) full original-vs-GitHub recheck and next priority. The reviewed `SculkVeinBlock`/`MultifaceBlock` graph is **reused** rather than incorrectly counted again. `LightningRodBlock` remains an independently source-pending lightning/weather family for later selection, not silently swallowed by sculk growth.

**Potential Phase-2/8/9 cross-boundary:** `SculkBlock` `pos.above()` growth, physical radius checks, cursors with 18 physical offsets, replaceable-state/tag graph and generation origin. Preserve actual physical `BlockPos`/`Vec3` and canonical target BlockState; choose local up/support and traversal only at the owning source/feature boundary; don't globally rotate Minecraft `Direction.UP`.

## 5. Final status and exact resume target

**Packet 3A-8 (four separately committed source-research microsteps) DONE**, not overall Stage 3A or Phase 2. **73/241** concrete classes source+original compiled *declaring-owner* reviewed / **194/1060 BLOCK IDs**. **168/241** classes and **866/1060 IDs** await such audits. **241/241** patched NeoForge ASM, Planet adapter and gameplay gates **PENDING**. No Java code, CI/build, Mixin verification or live Minecraft gameplay test performed.

**Next “кк” → Stage 3A-9.1:** the FIRST unchecked checkbox in new `02g-sculk-spread-growth-owners.md`. One bounded independent research commit then STOP. Preserve original immutable ZIP and all prior 241 ledger entries; do not prematurely award `SculkBlock` reviewed status or re-review `SculkVeinBlock` as a new class.
