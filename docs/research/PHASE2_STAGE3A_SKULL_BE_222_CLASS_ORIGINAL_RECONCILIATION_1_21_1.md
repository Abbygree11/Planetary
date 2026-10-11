# Stage3A-38.4 — physical original NeoForge21.1.215 class/ITEM/STATE registry 208→222 source audit and freezes

Reopened unchanged ORIGINAL NeoForge21.1.215 GitHub Actions ZIP from run **37988064055** artifact **11643813158** at `/mnt/data/planetary-registry-raw.zip`, physically `sha256sum` and Python `hashlib.sha256` **`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`**. Actual Python audit `/mnt/data/_audit_stage38.py` enumerates original ZIP TSV exactly **1060 registered BLOCK IDs,1333 ITEM registrations,1712 STATE-property rows,241 unique original concrete Java classes**. Original actual 14 class/30 registered ID cohort canonical SHA256 **`3c8e89b389694a330ffe6f6de336f1be7aad4061ebec32c5930d1a549fa70b6c`**. Audit evidence local JSON `/mnt/data/_stage38_original_evidence.json`; not a user file or claimed game runtime.

Exact reproducible canonical hash fields for all 30 registered original rows sorted by `registry_id`: one UTF-8/LF line each `registry_id|java_class|class_hierarchy|semicolon(sorted original property_name=legal_values)|comma(sorted actual ITEM.registry_id|java_class|block_item|placed_block) OR NONE|five name=FULL_FQN_DECLARING_OWNER(actual full argument signature) in fixed order`. Original ZIP actual 30-ID source hash is distinct from current GitHub ledger FNV32/64 freeze.

## All actual original registered class IDs and method owner families

| Original registered implementation | Actual class hierarchy (abbreviated complete original) | Registered original block IDs | Five original method nearest declaring owners (placement, survival, updateShape, randomTick, setPlacedBy) | Actual item join / properties |
| --- | --- | --- | --- | --- |
| `SkullBlock` | `SkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (5) `minecraft:creeper_head, minecraft:dragon_head, minecraft:piglin_head, minecraft:skeleton_skull, minecraft:zombie_head` | `SkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | 5×StandingAndWallBlockItem; `powered=false,true;rotation=0..15` |
| `WallSkullBlock` | `WallSkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (4) `minecraft:creeper_wall_head, minecraft:dragon_wall_head, minecraft:skeleton_wall_skull, minecraft:zombie_wall_head` | `WallSkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | NO direct placed_block ITEM (4 wall variants), originating StandingAndWallBlockItem; `facing=east,north,south,west;powered=false,true` |
| `PiglinWallSkullBlock` | `PiglinWallSkullBlock>WallSkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (1) `minecraft:piglin_wall_head` | `WallSkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | NO direct placed_block ITEM; piglin_head standing-wall creator; `facing=east,north,south,west;powered=false,true` |
| `PlayerHeadBlock` | `PlayerHeadBlock>SkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (1) `minecraft:player_head` | `SkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | PlayerHeadItem; `powered=false,true;rotation=0..15` |
| `PlayerWallHeadBlock` | `PlayerWallHeadBlock>WallSkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (1) `minecraft:player_wall_head` | `WallSkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | NO direct placed_block ITEM; PlayerHeadItem alt wall; `facing=east,north,south,west;powered=false,true` |
| `WitherSkullBlock` | `WitherSkullBlock>SkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (1) `minecraft:wither_skeleton_skull` | `SkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / WitherSkullBlock` | StandingAndWallBlockItem; `powered=false,true;rotation=0..15` |
| `WitherWallSkullBlock` | `WitherWallSkullBlock>WallSkullBlock>AbstractSkullBlock>BaseEntityBlock>Block>BlockBehaviour` | (1) `minecraft:wither_skeleton_wall_skull` | `WallSkullBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / WitherWallSkullBlock` | NO direct placed_block ITEM; wither_skeleton_skull standing-wall; `facing=east,north,south,west;powered=false,true` |
| `BeehiveBlock` | `BeehiveBlock>BaseEntityBlock>Block>BlockBehaviour` | (2) `minecraft:bee_nest, minecraft:beehive` | `BeehiveBlock / BlockBehaviour / BeehiveBlock / BlockBehaviour / Block` | 2×BlockItem; `facing=east,north,south,west;honey_level=0..5` |
| `BrushableBlock` | `BrushableBlock>BaseEntityBlock>Block>BlockBehaviour` | (2) `minecraft:suspicious_gravel, minecraft:suspicious_sand` | `Block / BlockBehaviour / BrushableBlock / BlockBehaviour / Block` | 2×BlockItem; `dusted=0..3` |
| `ComposterBlock` | `ComposterBlock>Block>BlockBehaviour` | (1) `minecraft:composter` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | BlockItem; `level=0..8` |
| `InfestedBlock` | `InfestedBlock>Block>BlockBehaviour` | (6) `minecraft:infested_chiseled_stone_bricks, minecraft:infested_cobblestone, minecraft:infested_cracked_stone_bricks, minecraft:infested_mossy_stone_bricks, minecraft:infested_stone, minecraft:infested_stone_bricks` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | 6×BlockItem; `none` |
| `HugeMushroomBlock` | `HugeMushroomBlock>Block>BlockBehaviour` | (3) `minecraft:brown_mushroom_block, minecraft:mushroom_stem, minecraft:red_mushroom_block` | `HugeMushroomBlock / BlockBehaviour / HugeMushroomBlock / BlockBehaviour / Block` | 3×BlockItem; `down/east/north/south/up/west all boolean` |
| `RootedDirtBlock` | `RootedDirtBlock>Block>BlockBehaviour` | (1) `minecraft:rooted_dirt` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | BlockItem; `none` |
| `HangingRootsBlock` | `HangingRootsBlock>Block>BlockBehaviour` | (1) `minecraft:hanging_roots` | `HangingRootsBlock / HangingRootsBlock / HangingRootsBlock / BlockBehaviour / Block` | BlockItem; `waterlogged=false,true` |

Actual original legal `rotation` and level strings are enumerated complete in original TSV and independently verified; abbreviated display `0..15`, `0..8`, `0..5`, `0..3` is an explicit lossless shorthand, not inferred from block names.

One original FULL nearest declaring method signature FQN/args for **every original exact registered ID**, in fixed canonical order:
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

**Original ITEM joins:** 23 of 30 exact BLOCK IDs have original same-placed-block ITEM registry row (16 ordinary `BlockItem`, 6 `StandingAndWallBlockItem`, 1 `PlayerHeadItem`). Seven original WALL skull/head IDs have **no direct `ITEM.placed_block` join** (creeper/dragon/piglin/player/skeleton/wither_skeleton/zombie wall variants), but are **normally placed through the standing variant's StandingAndWallBlockItem.wallBlock** and PLAYER_HEAD's PlayerHeadItem, as the source demonstrates. This is an alternate creator, not missing game capability.

## Frozen source 208 proof before promotion and current 222 independent hash streams

Connected LIVE GitHub file `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` at branch2.0 HEAD `b825e86178963c253595aeffebc5201483bef1cd` verified exact **241 rows×16 columns**, no duplicate original class FQN, all **241×3=723** runtime gate entries `REVIEW_PENDING`. Recomputed and matched Stage37 all four prior class+ID+five-owner canonical ASCII sorted newline FNV32/64 streams **before any writes**. Changed **only original 14 SOURCE_PENDING** class rows selected from immutable ZIP, preserving all previous 208 source-reviewed class tuples unchanged.

| Canonical lexical source FQN newline stream | Before 208 FNV32 / FNV64 | After 222 FNV32 / FNV64 |
| --- | --- | --- |
| ALL241 `class|registeredIDcount` | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| REVIEWED `class|sorted exact actual registered BLOCK IDs` | `0x83dda6d9 / 0x78f7e1dc96626d99` | `0xd1f75e2d / 0xd9195dffe69b0ccd` |
| REVIEWED `class|five nearest actual declaring Java owner classes` | `0xd5c2048f / 0x25d7399b880f60cf` | `0x1c852b5c / 0x78a503fb4085ddbc` |
| REVIEWED `class|original sorted BLOCK IDs|five owner classes` | `0x0534aea8 / 0x2e79ef619d74f528` | `0xb66d2b21 / 0xbdf306d865fbba21` |

**Current after Stage38 source reviewed 222/241 original concrete Java classes and 824/1060 registered original BLOCK IDs; remaining 19 classes, 236 IDs SOURCE_PENDING.** The 168-ID generic `Block` class remains unreviewed and must not be silently declared done based only on base Java source. All **723** patched NeoForge target 21.1.215 ASM, Planet adapter and actual gameplay acceptance gates remain `REVIEW_PENDING`; future [156 test groups](PHASE2_STAGE3A_SKULL_HIVE_ARCHAEOLOGY_156_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md) all `NOT RUN`. No Java/Mixin/Gradle/CI/client/server/loaded mod JAR attempted or claimed PASS.
