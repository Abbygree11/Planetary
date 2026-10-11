# Stage3A-36.4 — original immutable NeoForge21.1.215 ZIP exact 12 classes/30 vanilla registered BLOCK IDs, frozen prior 187→199 audit

**2026-10-12 SOURCE ONLY.** Physically SHA256 checked unchanged `/mnt/data/planetary-registry-raw.zip` from original run37988064055 artifact11643813158: `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Independently Python `zipfile.ZipFile` + `csv.DictReader(delimiter='\\t')` parsed original 1060 BLOCK records, 1333 ITEM records, 1712 state-property rows, 241 concrete Java FQN owners. Checked each original ID's registered Java hierarchy, five complete original declaring-owner descriptors, all state legal values and `ITEM.placed_block` joins. **Exact original sorted 30-ID cohort SHA256 `d2802bb6fab4070bf86ba203f801b3ae570071d99a30c7ff12c931c98499da23`**.

Reproducible canonical original record each registry ID lexical, one ASCII LF: `registry_id|java_class|class_hierarchy|semicolon(sorted property_name=legal_values)|comma(sorted original ITEM.registry_id|java_class|block_item|placed_block) OR NONE|five name=FULL_FQN_DECLARING_OWNER(full_arg_signature) in order getStateForPlacement,canSurvive,updateShape,randomTick,setPlacedBy`; hash 30 lines. Computed from original compiled registry, not guessed class name.

## Actual original class/ID/method owner/state/item inventory

| Original concrete class | Exact original BLOCK IDs | Five nearest declaring METHOD owners (order defined below) | Original legal BlockState properties | Item creators |
| --- | --- | --- | --- | --- |
| `ColoredFallingBlock` | (3) `minecraft:gravel, minecraft:red_sand, minecraft:sand` | `Block → BlockBehaviour → FallingBlock → BlockBehaviour → Block` | none | 3 original BlockItem |
| `ConcretePowderBlock` | (16) `minecraft:black_concrete_powder, minecraft:blue_concrete_powder, minecraft:brown_concrete_powder, minecraft:cyan_concrete_powder, minecraft:gray_concrete_powder, minecraft:green_concrete_powder, minecraft:light_blue_concrete_powder, minecraft:light_gray_concrete_powder, minecraft:lime_concrete_powder, minecraft:magenta_concrete_powder, minecraft:orange_concrete_powder, minecraft:pink_concrete_powder, minecraft:purple_concrete_powder, minecraft:red_concrete_powder, minecraft:white_concrete_powder, minecraft:yellow_concrete_powder` | `ConcretePowderBlock → BlockBehaviour → ConcretePowderBlock → BlockBehaviour → Block` | none | 16 original BlockItem |
| `LiquidBlock` | (2) `minecraft:lava, minecraft:water` | `Block → BlockBehaviour → LiquidBlock → LiquidBlock → Block` | level 0..15 | NONE (3 synthetic fluid/column IDs) |
| `BubbleColumnBlock` | (1) `minecraft:bubble_column` | `Block → BubbleColumnBlock → BubbleColumnBlock → BlockBehaviour → Block` | drag=false,true | NONE (3 synthetic fluid/column IDs) |
| `MagmaBlock` | (1) `minecraft:magma_block` | `Block → BlockBehaviour → MagmaBlock → BlockBehaviour → Block` | none | 1 original BlockItem |
| `SoulSandBlock` | (1) `minecraft:soul_sand` | `Block → BlockBehaviour → SoulSandBlock → BlockBehaviour → Block` | none | 1 original BlockItem |
| `WebBlock` | (1) `minecraft:cobweb` | `Block → BlockBehaviour → BlockBehaviour → BlockBehaviour → Block` | none | 1 original BlockItem |
| `TntBlock` | (1) `minecraft:tnt` | `Block → BlockBehaviour → BlockBehaviour → BlockBehaviour → Block` | unstable=false,true | 1 original BlockItem |
| `DragonEggBlock` | (1) `minecraft:dragon_egg` | `Block → BlockBehaviour → FallingBlock → BlockBehaviour → Block` | none | 1 original BlockItem |
| `ScaffoldingBlock` | (1) `minecraft:scaffolding` | `ScaffoldingBlock → ScaffoldingBlock → ScaffoldingBlock → BlockBehaviour → Block` | bottom=false,true;distance=0..7;waterlogged=false,true | ScaffoldingBlockItem |
| `MudBlock` | (1) `minecraft:mud` | `Block → BlockBehaviour → BlockBehaviour → BlockBehaviour → Block` | none | 1 original BlockItem |
| `DirtPathBlock` | (1) `minecraft:dirt_path` | `DirtPathBlock → DirtPathBlock → DirtPathBlock → BlockBehaviour → Block` | none | 1 original BlockItem |

Full original effective method descriptors matched exactly for **every original BLOCK ID**, one per name (do not confuse overloads):
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

Hierarchy: `ColoredFallingBlock > FallingBlock > Block > BlockBehaviour`; `ConcretePowderBlock > FallingBlock > Block > BlockBehaviour`; `DragonEggBlock > FallingBlock > Block > BlockBehaviour`; remaining nine respective class names `> Block > BlockBehaviour`. Actual original `ITEM.placed_block` joins: **27 total**: 26 `BlockItem` with exact same ID and one `ScaffoldingBlockItem`; three **NO ITEM** `water`, `lava`, `bubble_column`. No fake block item for fluids. Their bucket item authors are separate items and WorldGen writes, not `ITEM.placed_block`.

## Protect previous full frozen 187 source-reviewed class tuples in connected GitHub before promotion

The live connected GitHub `2.0` file `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` was loaded at pre-commit HEAD `d1fda872507fffd74e3b7830c64a206b4116e6d2`, validated 241×16 and 241 unique classes, all 241×3 runtime gates REVIEW_PENDING. Independently recomputed and matched exactly all four FNV-1a32 and FNV-1a64 streams against frozen Stage35. Replaced only original twelve source-pending class records (30 IDs), did not re-promote any prior reviewed tuples.

| Canonical sorted original Java FQN newline stream | BEFORE187 FNV32 / FNV64 | AFTER199 FNV32 / FNV64 |
| --- | --- | --- |
| all241 `class|count` | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| reviewed `class|commaSortedOriginalIDs` | `0x8f6a7ace / 0xfdb67e8bb9e7b0ae` | `0x677fbca4 / 0xbd774337c58df484` |
| reviewed `class|fiveOriginalDeclaringOwners` | `0x11171184 / 0x0b50f32ff014f1a4` | `0x2a3a87a6 / 0x14205e472f7a71e6` |
| reviewed `class|commaSortedOriginalIDs|fiveOwners` | `0xac27e65c / 0x4dea19809b06005c` | `0x880155d5 / 0x9e71be7a8a795315` |

**After Stage36:** **199/241 concrete registered vanilla classes source-reviewed, 785/1060 corresponding vanilla BLOCK registry IDs**; **42 classes /275 IDs source pending**. All **241×3=723** runtime NeoForge transformed ASM/Planet adapter/gameplay gates remain `REVIEW_PENDING`; all [148 acceptance designs](PHASE2_STAGE3A_PHYSICAL_MEDIUM_148_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md) `NOT RUN`, no Java/Mixin/Gradle, real client/server, mod JAR or actual NeoForge21.1.215 bytecode check. NeoForge comparative patch pinned `3cae64530de368936d4c07497d3f0a25e2f9ffec` is a later 1.21.1 source branch, NOT exact version proof.
