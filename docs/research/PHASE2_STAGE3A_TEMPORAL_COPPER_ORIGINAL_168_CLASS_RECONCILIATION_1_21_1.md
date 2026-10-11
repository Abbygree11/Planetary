# Stage3A-34.4 — original NeoForge 21.1.215 ZIP 24 copper BLOCK IDs / four classes and frozen 164→168 ×16 source ledger

**2026-10-12, SOURCE ONLY.** Reopen exact immutable original ZIP artifact **11643813158** from Actions workflow run **37988064055**: physical local archive SHA256 **`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`**; Python `zipfile.ZipFile`, `csv.DictReader(delimiter="\\t")` independently parse the 1060 BLOCK, 1333 ITEM, 1712 state-property TSV; exactly 241 original unique concrete Java classes. Exact **24/24** new BLOCK IDs and **24/24** same-name original `ITEM.placed_block` joined, 24 concrete original `net.minecraft.world.item.BlockItem` (`BlockItem>Item` hierarchy, `block_item=true`). No false no-Item synthetic blocks. Full original class hierarchy and five full declaring-owner signatures per original ID separately selected by method name (do not choose an overload just by name). Confirm all IDs under same class have consistent legal state names/values and five effective declaring owner signatures.

## Four originally pending source owner classes / 24 exact IDs

| Original registered Java class | BLOCK IDs | Exact original registered BLOCK IDs | Effective five owner names in order below | Original state legal properties | Original creator |
| --- | ---: | --- | --- | --- | --- |
| `CopperBulbBlock` | 4 | `minecraft:waxed_copper_bulb`, `minecraft:waxed_exposed_copper_bulb`, `minecraft:waxed_oxidized_copper_bulb`, `minecraft:waxed_weathered_copper_bulb` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | lit:false,true; powered:false,true | 4 × BlockItem |
| `WeatheringCopperBulbBlock` | 4 | `minecraft:copper_bulb`, `minecraft:exposed_copper_bulb`, `minecraft:oxidized_copper_bulb`, `minecraft:weathered_copper_bulb` | `Block / BlockBehaviour / BlockBehaviour / WeatheringCopperBulbBlock / Block` | lit:false,true; powered:false,true | 4 × BlockItem |
| `WeatheringCopperFullBlock` | 12 | `minecraft:chiseled_copper`, `minecraft:copper_block`, `minecraft:cut_copper`, `minecraft:exposed_chiseled_copper`, `minecraft:exposed_copper`, `minecraft:exposed_cut_copper`, `minecraft:oxidized_chiseled_copper`, `minecraft:oxidized_copper`, `minecraft:oxidized_cut_copper`, `minecraft:weathered_chiseled_copper`, `minecraft:weathered_copper`, `minecraft:weathered_cut_copper` | `Block / BlockBehaviour / BlockBehaviour / WeatheringCopperFullBlock / Block` | none | 12 × BlockItem |
| `WeatheringCopperGrateBlock` | 4 | `minecraft:copper_grate`, `minecraft:exposed_copper_grate`, `minecraft:oxidized_copper_grate`, `minecraft:weathered_copper_grate` | `WaterloggedTransparentBlock / BlockBehaviour / WaterloggedTransparentBlock / WeatheringCopperGrateBlock / Block` | waterlogged: false,true | 4 × BlockItem |

Original hierarchy: `CopperBulbBlock>Block>BlockBehaviour`; `WeatheringCopperBulbBlock>CopperBulbBlock>Block>BlockBehaviour`; `WeatheringCopperFullBlock>Block>BlockBehaviour`; `WeatheringCopperGrateBlock>WaterloggedTransparentBlock>TransparentBlock>HalfTransparentBlock>Block>BlockBehaviour`.

Exactly FULL effective five method signatures with declaring owners from original ZIP, in table order:
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

For all 24 ITEM instances `useOn=BlockItem(net.minecraft.world.item.context.UseOnContext)`, `place/updatePlacementContext/getPlacementState/placeBlock/canPlace/registerBlocks` owners are `BlockItem`; `use=Item(net.minecraft.world.level.Level,net.minecraft.world.entity.player.Player,net.minecraft.world.InteractionHand)`. Additional non-Item writers include `ChangeOverTimeBlock` copper progression, axe scrape, honeycomb waxing, LightningBolt reset/undo, Bulb redstone neighbor signal, WaterloggedTransparentBlock water fluid tick, commands/structure/WorldGenLevel and late item `BLOCK_STATE` DataComponent override. Already SOURCE-reviewed copper variant Stair/Slab/Door/TrapDoor owners not altered. **Terminal oxidized (no next) and waxed (non-WeatheringCopper) are not time-aged, though some are still redstone-interactive**.

Independent deterministic original six-column per exact ID canonical cohort SHA256 **`3ba1ef88f7f2a39401b577a43da82ce7316eedd8bd147590f9dcfc995f613035`**, Python script: for each 24 original BLOCK registry_id in lexical order concatenate `registry_id|java_class|class_hierarchy|semicolon(sorted original name=legalValues)|comma(sorted ITEM.registry_id|class|block_item|placed_block)|five NAME=FULL_FQN_OWNER(signature) in fixed lifecycle order\n`. SHA256 across 24 ASCII records from immutable ZIP. This is distinct from GitHub JavaScript FNV ledger. The original registry class+count full 241 record FNV remains matched.

## Before/after source reviewed live GitHub 241×16 ledger independently protected

Exact previously committed Stage33 `164/241 class, 701/1060 original ID` stream FNV32 and FNV64 rehashed LIVE **before editing** and matches Stage33 frozen originals, not guessed. Only these **four** `REVIEW_PENDING` rows replaced with source-reviewed data.

| Canonical ASCII FQN lexical `\n` stream | BEFORE 164 FNV32 / FNV64 | AFTER 168 FNV32 / FNV64 |
| --- | --- | --- |
| 241 `class|count` roster | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| reviewed `class|sortedExactOriginalIDs` | `0x9d2c499f / 0x525ec5011088c91f` | `0xe889fce9 / 0x7a69f202e2ecd589` |
| reviewed `class|fiveOwners` | `0x80dcedf7 / 0x1f99f5bbc1dcaad7` | `0x094ce6a7 / 0x032b30543d249a27` |
| reviewed `class|sortedExactOriginalIDs|fiveOwners` | `0x7bedd6f1 / 0x1baaa834b955ff51` | `0x7eabe093 / 0xbb969f24f144d813` |

Post source status **168/241 classes**, **725/1060 vanilla BLOCK IDs**; **73 classes /335 IDs source REVIEW_PENDING**. **All 241×3=723 patched-NeoForge ASM, Planet adaptation, gameplay acceptance remain `REVIEW_PENDING`**; source owner proof is NOT NeoForge Mixin applied bytecode or mod load. [143 future ACCEPTANCE groups](PHASE2_STAGE3A_ALL_TEMPORAL_MUTATION_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md) remain **NOT RUN**. No Java/Mixin/build/CI/game/loaded-mod runtime work. Whole [temporal dispatcher/mod family atlas](PHASE2_STAGE3A_TEMPORAL_MUTATION_MECHANISM_MOD_SOURCE_ATLAS_1_21_1.md) is an algorithm inventory, not a source-reviewed seal on all original 1060 vanilla block IDs or any third-party registered IDs. Next full 4/4 Stage35 focuses wider time-changing owners and actual mod extension seams.
