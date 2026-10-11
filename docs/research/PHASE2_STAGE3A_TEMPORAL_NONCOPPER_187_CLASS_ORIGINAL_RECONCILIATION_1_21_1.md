# Stage3A-35.4 — immutable original NeoForge21.1.215 ZIP 19 classes/30 IDs and 168→187 241×16 ledger

**2026-10-12; SOURCE+compiled declaring owner reflection ONLY, no runtime acceptance.** Physically opened unchanged ZIP `/mnt/data/planetary-registry-raw.zip` (local working path, not user download) supplied from GitHub Actions run **37988064055**, original artifact **11643813158**; independently recomputed **ZIP SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`**, then `zipfile.ZipFile` and `csv.DictReader(delimiter='\\t')` original TSVs:
- `phase2-neo1211-block-registry.tsv`: **1060 actual registered vanilla BLOCK IDs**, **241** distinct FQN Java classes;
- `phase2-neo1211-item-registry.tsv`: **1333 ITEM registrations**;
- `phase2-neo1211-state-properties.tsv`: **1712 state property records**.

All source 19 class original ID strings and original full-fqn five effective callback declaring owners verified with FULL actual argument signatures and corresponding ITEM.placed_block joins; not guessed from `Blocks` name. Exact original new cohort **30 registry BLOCK IDs**, **27 actual item placed_block joins**, three no Item joins (attached_pumpkin_stem, attached_melon_stem, big_dripleaf_stem). ITEM concrete types: 23 `BlockItem`, 3 `ItemNameBlockItem`, 1 anonymous `Items$1` subclass of `BlockItem`, not name-matched for cocoa/seed placement. Total 19 distinct original class names and no already-reviewed source row rewritten.

## All original 19 class+ID/five-method owner tuples

| Original registered class | Original exact BLOCK IDs (count) | 5 declaring method owners (getStateForPlacement,canSurvive,updateShape,randomTick,setPlacedBy) | Legal original state properties | ITEM joins |
| --- | --- | --- | --- | --- |
| `AttachedStemBlock` | 2: `minecraft:attached_melon_stem, minecraft:attached_pumpkin_stem` | `Block / BushBlock / AttachedStemBlock / BlockBehaviour / Block` | facing: four horizontal | NO ITEM |
| `BigDripleafBlock` | 1: `minecraft:big_dripleaf` | `BigDripleafBlock / BigDripleafBlock / BigDripleafBlock / BlockBehaviour / Block` | facing 4, tilt 4, waterlogged bool | minecraft:big_dripleaf => Items$1 > BlockItem > Item |
| `BigDripleafStemBlock` | 1: `minecraft:big_dripleaf_stem` | `Block / BigDripleafStemBlock / BigDripleafStemBlock / BlockBehaviour / Block` | facing 4, waterlogged bool | NO ITEM |
| `CherryLeavesBlock` | 1: `minecraft:cherry_leaves` | `LeavesBlock / BlockBehaviour / LeavesBlock / LeavesBlock / Block` | distance 1..7, persistent bool, waterlogged bool | BlockItem same ID |
| `CocoaBlock` | 1: `minecraft:cocoa` | `CocoaBlock / CocoaBlock / CocoaBlock / CocoaBlock / Block` | age 0..2, facing 4 | minecraft:cocoa_beans => ItemNameBlockItem |
| `FarmBlock` | 1: `minecraft:farmland` | `FarmBlock / FarmBlock / FarmBlock / FarmBlock / Block` | moisture 0..7 | BlockItem same ID |
| `GrassBlock` | 1: `minecraft:grass_block` | `SnowyDirtBlock / BlockBehaviour / SnowyDirtBlock / SpreadingSnowyDirtBlock / Block` | snowy bool | BlockItem same ID |
| `LeavesBlock` | 8: `minecraft:acacia_leaves, minecraft:azalea_leaves, minecraft:birch_leaves, minecraft:dark_oak_leaves, minecraft:flowering_azalea_leaves, minecraft:jungle_leaves, minecraft:oak_leaves, minecraft:spruce_leaves` | `LeavesBlock / BlockBehaviour / LeavesBlock / LeavesBlock / Block` | distance 1..7,persistent bool,waterlogged bool | 8 x BlockItem same IDs |
| `MangroveLeavesBlock` | 1: `minecraft:mangrove_leaves` | `LeavesBlock / BlockBehaviour / LeavesBlock / LeavesBlock / Block` | distance 1..7,persistent bool,waterlogged bool | BlockItem same ID |
| `MossBlock` | 1: `minecraft:moss_block` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | no properties | BlockItem same ID |
| `MyceliumBlock` | 1: `minecraft:mycelium` | `SnowyDirtBlock / BlockBehaviour / SnowyDirtBlock / SpreadingSnowyDirtBlock / Block` | snowy bool | BlockItem same ID |
| `NyliumBlock` | 2: `minecraft:crimson_nylium, minecraft:warped_nylium` | `Block / BlockBehaviour / BlockBehaviour / NyliumBlock / Block` | no properties | 2 x BlockItem same IDs |
| `PinkPetalsBlock` | 1: `minecraft:pink_petals` | `PinkPetalsBlock / BushBlock / BushBlock / BlockBehaviour / Block` | facing 4, flower_amount 1..4 | BlockItem same ID |
| `RedStoneOreBlock` | 2: `minecraft:deepslate_redstone_ore, minecraft:redstone_ore` | `Block / BlockBehaviour / BlockBehaviour / RedStoneOreBlock / Block` | lit bool | 2 x BlockItem same IDs |
| `SnowyDirtBlock` | 1: `minecraft:podzol` | `SnowyDirtBlock / BlockBehaviour / SnowyDirtBlock / BlockBehaviour / Block` | snowy bool | BlockItem same ID |
| `SpawnerBlock` | 1: `minecraft:spawner` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | no properties | BlockItem same ID |
| `StemBlock` | 2: `minecraft:melon_stem, minecraft:pumpkin_stem` | `Block / BushBlock / BushBlock / StemBlock / Block` | age 0..7 | melon_seeds,pumpkin_seeds => ItemNameBlockItem |
| `TrialSpawnerBlock` | 1: `minecraft:trial_spawner` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | ominous bool,trial_spawner_state six enum states | BlockItem same ID |
| `VaultBlock` | 1: `minecraft:vault` | `VaultBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | facing four, ominous bool,vault_state four enum states | BlockItem same ID |

The exact original FULL method descriptors, selected one each per original class by FULL name and arguments, in this order:
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

**Canonical independently derived SHA256 for the original 30-ID cohort: `3804abf4f77b5b57937b1e1ec30c58e582bfe83c10d7228e8263662701d9dbeb`.** Python executable `audit_stage35_original.py` protocol: read unchanged ZIP bytes and sha256; load BLOCK/ITEM/STATE via csv.DictReader; select 19 exact original Java FQNs; sort all 30 original rows lexically by `registry_id`. For each row produce a LF line `registry_id|java_class|class_hierarchy|semicolon(sorted property_name=legal_values)|comma(sorted original ITEM.registry_id|java_class|block_item|placed_block) OR NONE|five name=FULL_FQN_DECLARING_OWNER(full_arg_descriptor) in fixed order\n`. Concatenate UTF-8 and compute SHA256; no synthetic original IDs. The FNV hashes of live GitHub TSV are a different independent control.

## Frozen prior 168 reviewed owner tuples and after one 19-class promotion

Re-read LIVE exact connected GitHub **branch 2.0** `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` (241×16, unique FQN) **BEFORE changes**. Recomputed **all four FNV-1a32/FNV-1a64** canonical ASCII FQN lexical newline streams and confirmed byte-for-byte hash equality with Stage34 frozen values. Promoted **only 19 original pending class rows**, including all 8 different base LeavesBlock material IDs and 2 stem/nylium/redstone ore variants, no existing reviewed row mutated. Each newly promoted row has exact original five nearest declaring-owner class names (FULL method FQN parameter proof original cohort above), exact original BLOCK IDs, original ITEM creator distinctions and specific algorithm notes.

| Full original ledger ASCII canonical stream | Before 168 (FNV32 / FNV64) | After 187 (FNV32 / FNV64) |
| --- | --- | --- |
| All241 `javaClass|count` | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| Reviewed `javaClass|commaSortedExactIDs` | `0xe889fce9 / 0x7a69f202e2ecd589` | `0x8f6a7ace / 0xfdb67e8bb9e7b0ae` |
| Reviewed `javaClass|fiveNearestOwnerNames` | `0x094ce6a7 / 0x032b30543d249a27` | `0x11171184 / 0x0b50f32ff014f1a4` |
| Reviewed `javaClass|commaSortedExactIDs|fiveOwners` | `0x7eabe093 / 0xbb969f24f144d813` | `0xac27e65c / 0x4dea19809b06005c` |

**After 35:** `187/241` classes SOURCE_REVIEWED_INTEGRATION_PENDING; **755/1060** original registered BLOCK IDs belong to those classes. **54 classes and 305 vanilla BLOCK IDs** remain SOURCE_REVIEW_PENDING. All **241 × 3 = 723** `neoforge_patch_bytecode_review`, `planet_adapter_acceptance` and `gameplay_acceptance` columns **REVIEW_PENDING** for all 241 rows. 163 prospective tests are NOT RUN; no Java/Mixin edit, CI, client/server/gameplay PASS, mod JAR load, exact patched NeoForge21.1.215 ASM verification nor mod compatibility acceptance.
