# Stage 3A-14.4 — independently verified original 241/92 BLOCK class, exact ID and full-signature five-owner reconciliation

**2026-10-10 · Planetary 2.0 · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21. Stage 3A-14 complete 4/4 SOURCE RESEARCH ONLY, entire package for one user “кк”.** The 32 future AQ14 fixture tests [in Stage14.3](PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_SIX_FACE_WATER_CONTRACT_1_21_1.md) are **NOT RUN**, Java and Mixins unchanged, ASM/gameplay acceptance unverified.

## 1. Actual independent physical unmodified ZIP / current GitHub comparison

Original [Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, original repository commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`; actual `/mnt/data/phase2-neo1211-registry-census.zip` independently reopened using Python `ZipFile` / `csv.DictReader(delimiter='\t')`, SHA-256 recomputed byte-for-byte **MATCH** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Exact original records **1060 registered BLOCK IDs / 241 distinct registered concrete BLOCK classes, 1333 ITEM entries and 1712 state properties**.

Local reproducible audit script `/mnt/data/_audit_phase2_14_4.py` (not pushed to repo, only used for this research) freezes the **89 exact previously reviewed fully qualified class names** from pre-Stage14 audited checkpoint, adds exactly three `SpongeBlock`, `WetSpongeBlock`, `SeaPickleBlock`, and enumerates **all original ZIP IDs and FULL ARGUMENT-SIGNATURE nearest declaring owners** for those 92 actual registered classes. The script checks all original registered IDs of each reviewed class share the method owner tuple, and computes canonical fingerprints for every group of 241 original classes. An independent JavaScript parser of actually LIVE GitHub branch `2.0` mutable `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` (241 rows, 16 header-named columns; Stage14 internal checkpoint commit [`9e660e6fa5eed60fdf3c5b457b0807057704d552`](https://github.com/Abbygree11/Planetary/commit/9e660e6fa5eed60fdf3c5b457b0807057704d552), ledger blob `d2ed91c9ddb88bce16000ea982b7513db39d1737`) selects class names by CURRENT actual `SOURCE_REVIEWED_INTEGRATION_PENDING` status (92 rows) and constructs identical canonical line formats in raw case-sensitive class ordering. This is a second independent parser, not an invented hash.

**IMPORTANT AUDIT-PARSER CORRECTION:** An initial temporary Python parser incorrectly selected the **first overload** of every `getStateForPlacement` method by method name only. This surfaced an apparent owner mismatch at existing `KelpBlock`, and an intermediate progress note incorrectly described it as a previously wrong ledger row. Checking the **actual original `effective_method_owners` field** shows two explicit signatures:
- `getStateForPlacement(net.minecraft.world.level.LevelAccessor)` → nearest **GrowingPlantHeadBlock**;
- `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)` → nearest **KelpBlock**.

The **actual prior ledger was CORRECT** (`getStateForPlacement_owner=KelpBlock` for the required BlockPlaceContext signature). We did **NOT modify this old class row**. The Python audit script was FIXED to tokenize overloads separated by semicolons within each `method=...` field and exact-match ALL FIVE complete qualified argument lists. Only AFTER this fix all four original-vs-live fingerprints **MATCH**. This is a parser bug caught and corrected within the same Stage14 audit, NOT a change to NeoForge semantics or a hidden source promotion. Future audits MUST retain overload exact matching, not naïve `.split('(',1)[0]` or first overload selection.

## 2. Four original Python vs live JavaScript canonical fingerprints — verified after overload fix

Each UTF-8 canonical stream is sorted lexicographically by fully qualified Java class (NOT localeCompare), exact class ID entries lexicographically comma-joined, row ends exactly one newline; method owner fields ordered `getStateForPlacement(BlockPlaceContext)`, `canSurvive(BlockState,LevelReader,BlockPos)`, `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`, `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`, `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`, all resolved by **fully qualified** parameter lists from original compiled reflection.

| Canonical row format | Python original FNV-1a32 = LIVE JS FNV32 | Python original FNV-1a64 = LIVE JS FNV64 | Python original-side-only SHA-256 | Verdict |
|---|---|---|---|---|
| 241 `class|count\n` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | **MATCH** |
| 92 `class|exactSortedID1,...\n` | `0x76a8b909` | `0x63046b42a4f46589` | `76bb07ee2844beb3035e840b05f246116c5a7cf3ba18301cfaeb387f3bfad35f` | **MATCH** |
| 92 `class|owner1|...|owner5\n` | `0xf79e414f` | `0x9fc66b3f3b5fd8ef` | `ce1a26db793646aa958de5b00b0f8549b31613d842696210662b246f7f8a52aa` | **MATCH** |
| 92 `class|ids|owner1|...|owner5\n` | `0x0f95f852` | `0xe30dda5d6ad1b152` | `ff10bf507d844d641e7fc50c8db0f53f1de54f8c2c1157722696faed508f4c7d` | **MATCH** |

SHA-256 canonical stream values are **original Python-side only**, not independently JS-recomputed; the two JS FNV pair matches diagnose byte equality, and original ZIP hash verifies ZIP integrity, but NO SOURCE research fingerprint proves applied runtime patched ASM.

## 3. Three new original registered class and exact ITEM authors

| New class / only BLOCK ID | Original five full-signature nearest declaring owners | Original exact ITEM.placed_block join |
|---|---|---|
| `SpongeBlock` / `minecraft:sponge` | **Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block** | 1 `minecraft:sponge` `BlockItem` |
| `WetSpongeBlock` / `minecraft:wet_sponge` | **Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block** | 1 `minecraft:wet_sponge` `BlockItem` |
| `SeaPickleBlock` / `minecraft:sea_pickle` | **SeaPickleBlock / SeaPickleBlock / SeaPickleBlock / BlockBehaviour / Block** | 1 `minecraft:sea_pickle` `BlockItem` |

All 3 original seven placement API methods of their ITEM `effective_method_owners` belong to BlockItem, and `use(Level,Player,InteractionHand)` owner Item, no special ITEM. Sponge and wet sponge have no original state properties. SeaPickle has only `pickles=1..4` and `waterlogged=false,true`. Original source hierarchy SpongeBlock>Block>BlockBehaviour, WetSpongeBlock>Block>BlockBehaviour, SeaPickleBlock>BushBlock>Block>BlockBehaviour. Full original detailed source ownership [14.1+14.2](PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_ORIGINAL_BLOCK_ITEM_WRITERS_1_21_1.md).

**Specific algorithm-owner separation:** SpongeBlock `onPlace/neighborChanged` physical six-adjacent-cell WATER-only bounded BFS (depth6, 65 accepted visits including root) → WET_SPONGE if at least one absorbed, BucketPickup and permitted vegetation removal; WetSpongeBlock `onPlace` in ultraWarm directly→SPONGE, client world-Y droplet animation; SeaPickleBlock source-local support/waterlogged/same-cell PICKLES1..4, `performBonemeal` actual wet+CORAL_BLOCKS substrate gate and direct physical world-XZ/Y 5-column growth source, while `CoralFeature` independently direct writes at world pos.above, bypassing Item. No class other than these 3 newly promoted.

## 4. Final strict 241-class status and next family

| Current live 16-column ledger disposition | Registered concrete classes | Registered exact BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **92** | **241** |
| `REVIEW_PENDING` | **149** | **819** |
| **TOTAL** | **241** | **1060** |

All 241 classes unique, all 92 reviewed rows exact registered ID strings and five full signature declaring owners source+original reflection, remaining 149 pending rows untouched; all **241×3=723** NeoForge patched bytecode, Planet adapter and gameplay acceptance flags **REVIEW_PENDING**. **32 proposed AQ14 future acceptance tests NOT RUN**; no code/CI/client/server tested. Stage14 source research DONE 4/4 only; Phase2/Stage3A still OPEN.

**Next [Stage 3A-15 two actual source-pending AmethystBlock and BuddingAmethystBlock classes](../phases/phase-02/02m-amethyst-budding-growth-owners.md), entire next “кк”**: two original concrete classes, **one exact registered ID each**, source-pending; dependent already source-reviewed `AmethystClusterBlock` owns four registered bud/cluster BLOCK IDs and FACING/WATERLOGGED, must be REUSED as sibling/inherited behavior, NOT re-promoted. [`BuddingAmethystBlock.randomTick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BuddingAmethystBlock.java) chooses actual six physical Direction.values(), places/ages aligned `AmethystClusterBlock` states with `FACING` and target water detection; gravity-shell faces and local chart distinction. The two original ITEM joins: `minecraft:amethyst_block` regular BlockItem, `minecraft:budding_amethyst` regular BlockItem; four cluster IDs remain already reviewed, not newly counted. New family will retain all physical/entity/NeoForge acceptance gates.
