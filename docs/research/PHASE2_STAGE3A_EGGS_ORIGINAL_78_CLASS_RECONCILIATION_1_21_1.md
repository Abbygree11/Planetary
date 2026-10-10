# Stage 3A-11.4 — independent NeoForge 241-class / 78-reviewed original census reconciliation; hatch→cold-surfaces family handoff

**2026-10-10; Planetary `2.0`; Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.** This closes **research-only** Stage 3A-11, all four internal steps executed from ONE user “кк”; none of the 30 proposed EG11 tests run. No Java/NeoForge patched ASM application/Planet adapter/gameplay acceptance. Phase 2 still OPEN.

## 1. Independent physical archive rehash and full original registry reparse

Actually reopened physical `/mnt/data/phase2-neo1211-registry-census.zip`, immutable NeoForge original ZIP from [GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, originating commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`. SHA-256 **MATCH** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Python `zipfile.ZipFile`, `csv.DictReader(delimiter='\t')` counted **1060 exact BLOCK registry IDs**, **241 distinct concrete registered Java block classes**, **1333 exact ITEM rows**, **1712 state-property records**. Original ZIP's historical `audit_disposition` is separate from mutable source-review ledger.

**Current LIVE GitHub source disposition** at internal Stage 11.1/11.2 checkpoint commit [`adb5f98d4da58299fa60d3b720828a5196ab87ea`](https://github.com/Abbygree11/Planetary/commit/adb5f98d4da58299fa60d3b720828a5196ab87ea): [`PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) blob SHA `00ced2899d519ff8e6955ebe9eecf6f458f1990f`, **241 rows × 16 columns**, 241 unique fully qualified Java classes. Full original BLOCK ID class/registered count roster matches live GitHub 241 class rows; class counts add to 1060. The original Python 78 reviewed-class set is previous audited 75 exact original class names plus TurtleEggBlock, SnifferEggBlock and FrogspawnBlock; current GitHub JavaScript instead selects *independently* actual live `SOURCE_REVIEWED_INTEGRATION_PENDING` records (not a copied Python set).

Original Python parses all five original method nearest declaring owners for full-qualified argument signature (including overload separators), groups by concrete class and checks every original registered ID of a class has identical 5-tuple. Five signatures unchanged across cohorts: `getStateForPlacement(BlockPlaceContext)`, `canSurvive(BlockState,LevelReader,BlockPos)`, `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`, `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`, `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`; see exact fully qualified signature text in previous [LightningRod reconciliation](PHASE2_STAGE3A_LIGHTNING_ROD_ORIGINAL_75_CLASS_RECONCILIATION_1_21_1.md). The original data compared with independent JavaScript parsing of current live GitHub 16-column TSV; JS sorted full class names using **raw codepoint lexical comparison**, not localeCompare, and calculated FNV-1a32 and FNV-1a64 over each identical canonical string.

### Four independent original-ZIP versus live-GitHub FNV32/FNV64 matches

Each canonical string is sorted full class name; each row ends exactly one `\n`. Registered IDs joined comma-separated in ascending order. Owner tuples ordered the five signatures above.

| Data / canonical row format | FNV32 original ZIP **AND** live GitHub | FNV64 original ZIP **AND** live GitHub | SHA-256 original ZIP only | Match |
|---|---|---|---|---|
| 241 classes `class|count\n` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | **YES** |
| 78 reviewed exact IDs `class|id1,id2\n` | `0x90d476ae` | `0xb39b4282da32bd8e` | `cea6bab30bc80611d96970b92a5abae757b972926564a8a9840b17dadb3a16f5` | **YES** |
| 78×5 original nearest declaring owners `class|owner1|...|owner5\n` | `0x808678fb` | `0xdb3b35355c39543b` | `fcec1a308f1b4e88afe63bd02eb079b1fa38d8cac3261b2070f3c2ad093d2bde` | **YES** |
| 78 exact IDs AND owners `class|id1,id2|owner1|...|owner5\n` | `0xb665b763` | `0xbc7b6ceceb7cbf23` | `3569379bbb977c05c7d4beee83be287dacfe30a2e4f1d097131c119691be7c88` | **YES** |

Original-side SHA-256 computed in Python; **not independently SHA-256 recomputed on JS side**. FNV is diagnostic mismatch detector, not a cryptographic equivalence proof. Combined with cryptographic original ZIP hash, exact source row validations and independently enumerated concrete class/IDs/owners, the 241/78 source-disposition audit is internally consistent. No claim NeoForge-transformed methods actually passed runtime integration.

## 2. Three newly source+original owner-reviewed BLOCK classes and original ITEM creators

| Exact concrete class / only registered BLOCK ID | Five nearest full-signature declaring owners: placement / survive / shape-update / randomTick / setPlacedBy | Original exact ITEM |
|---|---|---|
| `TurtleEggBlock` / `minecraft:turtle_egg` | `TurtleEggBlock / BlockBehaviour / BlockBehaviour / TurtleEggBlock / Block` | 1 ordinary `minecraft:turtle_egg` BlockItem |
| `SnifferEggBlock` / `minecraft:sniffer_egg` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | 1 ordinary `minecraft:sniffer_egg` BlockItem |
| `FrogspawnBlock` / `minecraft:frogspawn` | `Block / FrogspawnBlock / FrogspawnBlock / BlockBehaviour / Block` | 1 `minecraft:frogspawn` special `PlaceOnWaterBlockItem`, whose `useOn` and `use` are BOTH overridden |

All three original exact BLOCK row byte SHA-256 and exact ITEM row hashes are in [11.1+11.2 source audit](PHASE2_STAGE3A_EGGS_BLOCK_SOURCE_ITEM_ALTERNATE_WRITERS_1_21_1.md); no other BLOCK class promoted. All 78 reviewed classes have `registry_dispatch_evidence=REFLECTION_OWNER_VERIFIED`, original exact ID list, original five full method owner names and pinned comparative source pointer. The remaining 163 source-pending retain `registered_block_ids_reviewed_only=SEE_REGISTRY_ARTIFACT` and `registry_dispatch_evidence=REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY`. Original 1333 ITEM source "BLOCKITEM_CREATION_REVIEW_PENDING" flags are historic immutable census, not rewritten. Not an exhaustive mod writer audit.

**Final class coverage accounting:**

| Current live class/source disposition | Concrete classes | Exact registered BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **78** | **199** |
| `REVIEW_PENDING` | **163** | **861** |
| **TOTAL original/ledger** | **241** | **1060** |

ALL **241×3 = 723** `neoforge_patch_bytecode_review` / `planet_adapter_acceptance` / `gameplay_acceptance` fields remain **REVIEW_PENDING**; no fake PASS based on source, bytecode-owner reflection or proposed acceptance scenarios. Turtle, Sniffer and Frog source+Item alternate creation contract [Stage 11.3](PHASE2_STAGE3A_EGGS_SIX_FACE_HATCH_SUBSTRATE_ACCEPTANCE_CONTRACT_1_21_1.md) contains **30 EG11-01…EG11-30 test proposals — ALL NOT RUN**. No Java, ASM/Mixin, build/CI, client/server or actual gameplay test.

## 3. Next REAL pending class family selected by original registry and source triage

New [Stage 3A-12 cold-surface snow/powder/ice owners](../phases/phase-02/02j-cold-surfaces-snow-ice-owners.md) is built on four current **source-pending, original registered 1.21.1** block classes:

| Original exact class | Only original registered BLOCK ID | Original state properties | Original registered ITEM creator(s) |
|---|---|---|---|
| `SnowLayerBlock` | `minecraft:snow` | `layers` (1..8) | `minecraft:snow` standard BlockItem |
| `PowderSnowBlock` | `minecraft:powder_snow` | *none* | `minecraft:powder_snow_bucket` specialized **SolidBucketItem** |
| `FrostedIceBlock` | `minecraft:frosted_ice` | `age` | **No original ITEM placed_block match** |
| `IceBlock` | `minecraft:ice` | *none* | `minecraft:ice` ordinary BlockItem |

**Triage not source-verified for promotion yet:** pinned comparable [`SnowLayerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SnowLayerBlock.java) has neighbor support + snow layer count/stacking + random melt; [`PowderSnowBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PowderSnowBlock.java) has `BucketPickup` and collision/`entityInside`/`fallOn` entity owner, physically permeable snow unlike layered snow; [`FrostedIceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FrostedIceBlock.java) extends [`IceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IceBlock.java) with random/scheduled aging/melt and water/brightness/neighbor considerations. Source of world freezing and entities/buckets independently belongs to Phases 5/7/8/9. They share snow/water/phase-change world-axis assumptions but **do not form one inherited Java superclass**. Read exact source+original declaring owner signatures first, and handle IceBlock and FrostedIceBlock as distinct original registered classes.

**Next single “кк” = entire Stage 3A-12 (12.1–12.4 internally)**. Stage 3A and Phase 2 still OPEN; Stage 3A-11 concluded only source research.
