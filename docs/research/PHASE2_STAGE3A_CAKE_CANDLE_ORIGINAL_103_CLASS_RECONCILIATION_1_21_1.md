# Stage 3A-19.4 — physical original NeoForge ZIP vs live GitHub 241/103 class exact FULL-owner reconciliation

**2026-10-11 · Stage3A-19 SOURCE 4/4; NO compiled NeoForge PATCH ASM, Planet adapter, dedicated server/client or gameplay acceptance.**

## Independent inputs and isolation

Original, untouched actual GitHub Actions `phase2-neo1211-registry-census.zip` artifact **11643813158**, run **37988064055**, stored physical `/mnt/data/phase2-neo1211-registry-census.zip`, SHA256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**. Source original 1060 BLOCK ID rows, 241 distinct original registered classes, 1333 ITEM rows and 1712 property entries. A separately executed original Python `ZipFile/csv.DictReader` audit `/mnt/data/_audit_phase2_19_4.py` used the **frozen explicit 100-short-class Stage18 reviewed roster** from the previous Stage18 state + exactly **CakeBlock, CandleBlock, CandleCakeBlock**, NOT a current GitHub ledger disposition query, so class selection is independent. Python asserted 103 distinct valid fully qualified original class names, every ID within each group had one consistent FULL declaring owner for each of five exact parameter signatures (overload guards), 18 original direct ITEM joins, no candleCake ITEMS, and exact total 285 reviewed IDs.

The independent LIVE GitHub JavaScript side actually fetched current 241-row ×16-column `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` blob SHA **ca890129039157510b90cb958a94fd9f8028dc94**, and applied only the three source-pending rows to the in-memory ledger before recomputing canonical record stream FNV-1a32 and FNV-1a64. Original Python and live-ledger JS confirmed **MATCH 4/4 FNV32/FNV64 pairs**. Canonical sorted-by-full-Java-name ASCII records have exactly one trailing newline, IDs sorted ASCII within class, owners in frozen five-signature order:

| Canonical stream | Original ZIP Python FNV32 == live GitHub JS | Original ZIP Python FNV64 == live GitHub JS | SHA256 original Python only |
| --- | --- | --- | --- |
| All 241 `class\|registeredBlockCount` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| Reviewed 103 `class\|sortedExactRegisteredIDs` | `0x7a3a2059` | `0xcbf232daa79eb079` | `fc485e5eed3e2084e450216b00096fdf0762e03ff3a65b997d380b5c1c9b84b0` |
| Reviewed 103 `class\|owner1..owner5` | `0xeb4e5c5d` | `0x6838cb5b21f3495d` | `82e2b932ab3e3d5d75750b28e86c35eee2e6e0169df31002f57dcd08d961a91a` |
| Reviewed 103 `class\|IDs\|owner1..owner5` | `0xd5d82cc4` | `0xc6953ef9f04f1ce4` | `6d0a1753cbcd1460c4809441ea6f00c2d10bc2d71d9249f9f58a81e1101da00c` |

The original Python SHA256 canonical stream values were not independently rehashed in JS (FNV cross-checks performed), FNV diagnostics are non-cryptographic; none of these compare actual NeoForge transformed bytecode.

## Newly reviewed exact original class group and five fully qualified declaring-owner signatures

Ordered five signatures:

1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

| Concrete original class | Registered exact original IDs | Five nearest declaring owners |
| --- | ---: | --- |
| CakeBlock | 1: `minecraft:cake` | `Block/CakeBlock/CakeBlock/BlockBehaviour/Block` |
| CandleBlock | 17: `minecraft:candle` + 16 colored `*_candle` | `CandleBlock/CandleBlock/CandleBlock/BlockBehaviour/Block` |
| CandleCakeBlock | 17: `minecraft:candle_cake` + 16 colored `*_candle_cake` | `Block/CandleCakeBlock/CandleCakeBlock/BlockBehaviour/Block` |

Important: `AbstractCandleBlock` abstract algorithm superowner NOT original concrete registered class; `CandleCakeBlock` 17 colors are NOT 17 ITEM IDs. Original direct ITEM join is precisely **18 BlockItems** (1 cake +17 normal candle) and ZERO candleCake BlockItems. An independently implemented mod class `MixedCandleBlock extends AbstractCandleBlock` at pinned mod SHA `388a67254d15ac526e983c492558f957fbc58d84` is NOT added to original 241/1060 registry. Its minimum NeoForge21.1.235 exceeds target21.1.215. Genuine loaded modded compatibility is not established.

## Reconciled source-only disposition and exact independent runtime gates

| LIVE 16-column original registry ledger disposition | Concrete original BLOCK Java classes | Exact original registered BLOCK IDs |
| --- | ---: | ---: |
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **103** | **285** |
| `REVIEW_PENDING` | **138** | **775** |
| TOTAL | **241** | **1060** |

**All 241×3=723** original class-level columns `neoforge_patch_bytecode_review`, `planet_adapter_acceptance` and `gameplay_acceptance` remain **REVIEW_PENDING**, including three newly researched classes. [45 CK19-01..CK19-45 proposed tests](PHASE2_STAGE3A_CAKE_CANDLE_SIX_FACE_FIRE_MOD_ACCEPTANCE_1_21_1.md) are **ALL NOT RUN**. Last manual side/bottom Planet candle/cake defects remain open; no Java/Mixin/source runtime code edits, no JUnit/client/server/CI or mod loading. [Detailed original source/ITEM/mod](PHASE2_STAGE3A_CAKE_CANDLE_ORIGINAL_OWNER_ITEM_FIRE_MOD_1_21_1.md).

NEXT future user one «кк» → **whole Stage3A-20** [CampfireBlock, FireBlock and SoulFireBlock shared fire/campfire source owners](../phases/phase-02/02r-campfire-fire-soulfire-mechanism-owners.md), exactly **3 remaining SOURCE PENDING concrete classes/4 original BLOCK IDs**: `minecraft:campfire`, `minecraft:soul_campfire`, `minecraft:fire`, `minecraft:soul_fire`. Abstract `BaseFireBlock` is a shared algorithm superowner, NOT fourth original registered class.
