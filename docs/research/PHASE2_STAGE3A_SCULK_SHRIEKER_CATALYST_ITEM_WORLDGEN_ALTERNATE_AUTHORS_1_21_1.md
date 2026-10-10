# Planetary Stage 3A-8.2 — SculkShrieker/Catalyst exact ITEM creators and direct worldgen authors

**2026-10-10 · branch 2.0 · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.**
Scope: two originally registered BLOCK classes already source+compiled-declaration-audited in [3A-8.1](PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_SOURCE_OWNER_AUDIT_1_21_1.md); no other class promoted. This is an **independent ITEM-registry and comparative 1.21.1 source study**, NOT NeoForge patched ASM, Planetary code, gameplay or CI acceptance.

## 1. Original unchanged NeoForge artifact — exact 1333-item join

Re-downloaded the original ZIP from [CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, built at `aa39572950a15403ea0a9003eefccf3bf6675ff7`. Reverified **SHA-256** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Its actual members contain **1060 BLOCK IDs / 241 concrete classes**, **1333 ITEM records**, **1712 BlockState properties**. Parsed original rows and joined *all* items by exact `ITEM.placed_block == BLOCK.registry_id`, selecting BLOCK class precisely `SculkShriekerBlock` or `SculkCatalystBlock` (not by guessed item names).

| Original BLOCK.registry_id | Original concrete BLOCK class | Exact ITEM.registry_id / java_class | Original ITEM class_hierarchy / audit status |
|---|---|---|---|
| `minecraft:sculk_catalyst` | `net.minecraft.world.level.block.SculkCatalystBlock` | `minecraft:sculk_catalyst` / `net.minecraft.world.item.BlockItem` | `BlockItem>Item` / `BLOCKITEM_CREATION_REVIEW_PENDING` |
| `minecraft:sculk_shrieker` | `net.minecraft.world.level.block.SculkShriekerBlock` | `minecraft:sculk_shrieker` / `net.minecraft.world.item.BlockItem` | `BlockItem>Item` / `BLOCKITEM_CREATION_REVIEW_PENDING` |

Exactly **one** matching ITEM per target BLOCK and no special subclass or alias in these two actual entries. For **both** full-signature `ITEM.effective_method_owners`: `useOn(UseOnContext)`, `place(BlockPlaceContext)`, `updatePlacementContext(BlockPlaceContext)`, `getPlacementState(BlockPlaceContext)`, `placeBlock(BlockPlaceContext,BlockState)`, `canPlace(BlockPlaceContext,BlockState)`, `registerBlocks(Map,Item)` are **BlockItem** (7/7); separate `use(Level,Player,InteractionHand)` is **Item**, not BlockItem. This is exact original compiled *declaring-owner* evidence, not an ASM patch-body audit.

Reproduction digest **SHA-256** `ac59674578124bb46a566e7a22f7432d3814acc6dfba22859c43578f89adaf0b`: sort matching rows by full `BLOCK.registry_id` ascending, append UTF-8 lines `BLOCK.registry_id|ITEM.registry_id|ITEM.java_class|ITEM.placed_block|ITEM.effective_method_owners\n` with the original last field unchanged (including internal separators). The original ZIP audit flag is historical and remains unchanged.

## 2. Normal item author vs optional post-placement state/BE authors

Pinned **comparative** Java 1.21.1 source commit [`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1). [`BlockItem.useOn/place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L45-L119) constructs `BlockPlaceContext`, invokes `updatePlacementContext`, `getPlacementState` → block `getStateForPlacement`, `canPlace` and `placeBlock` → `Level.setBlock`. Two blocks have different state authors:

- [`SculkShriekerBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L148-L153): normal `CAN_SUMMON=false`, `SHRIEKING=false` and target-fluid-derived `WATERLOGGED`. No FACING state.
- [`SculkCatalystBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java#L22-L44) inherits `Block.getStateForPlacement`, default `BLOOM=false` only. No WATERLOGGED/FACING.
- [`BlockItem.updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158-L176) applies optional `DataComponents.BLOCK_STATE` **after** the initial setBlock and may write *legal* properties with a second `setBlock(...,2)`. Thus a component-bearing shrieker item can change legal `CAN_SUMMON/SHRIEKING/WATERLOGGED` values; a catalyst item can change `BLOOM`. **CAN_SUMMON=false is the default, not an invariant of every arbitrary item-created shrieker.** Do not assume ordinary stacks carry a component.
- [`BlockItem.updateCustomBlockEntityTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L197-L225) handles optional `BLOCK_ENTITY_DATA` subject to its permission checks; [`updateBlockEntityComponents`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L135-L144) applies stack components to BE **before** `setPlacedBy`. Shrieker BE warning/listener and catalyst BE spread cursor data are independent of block properties. Item placement also emits `GameEvent.BLOCK_PLACE` and sound; the non-item writers below do not execute this item lifecycle.

## 3. Direct SculkBlock charge-growth writer (level AND worldgen)

[`SculkBlock.attemptUseCharge`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L30-L65) conditionally writes **a new SENSOR or SHRIEKER** through `LevelAccessor.setBlock(pos.above(), state,3)`, **not** `BlockItem.place`. It is subject to charge, growth cost, decay, growth radius, random chance and [`canPlaceGrowth`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L94-L123) (target air/water; physical density window X/Z ±4 and Y from 0 to +2).

[`getRandomGrowthState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L76-L92) conditionally selects shrieker at **1/11 among choices when growth actually occurs**, else sensor, sets shrieker `CAN_SUMMON = SculkSpreader.isWorldGeneration()`; derives WATERLOGGED from target fluid where supported. This is **not** 1/11 probability per death or cursor event. **Catalyst is not one of this method's chosen growth states.** The world `above()` and world-axis density window are potential local-gravity semantic mismatches, not coordinates safe to rotate indiscriminately.

## 4. NEW confirmed direct vanilla worldgen owner — SculkPatchFeature

[`Feature.SCULK_PATCH`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/Feature.java#L153) registers a concrete [`SculkPatchFeature.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/SculkPatchFeature.java#L23-L79). Its **three** routes are separate:
 
| Source writer | Exact state / conditions | Physical-world assumption |
|---|---|---|
| `SculkPatchFeature` → `SculkSpreader.createWorldGenSpreader()` → `updateCursors` → `SculkBlock.attemptUseCharge` | May grow sensor/shrieker; shrieker `CAN_SUMMON=true` from worldgen spreader mode; also sculk/vein graph effects | Physical charge positions and `pos.above()`; `canSpreadFrom` checks real six neighbor coordinates |
| `SculkPatchFeature.place` **direct catalyst** | If `random.nextFloat() <= catalystChance` AND `origin.below()` has full collision shape: `worldgenlevel.setBlock(origin, SCULK_CATALYST.defaultBlockState(),3)` | Explicit **world DOWN/-Y** support |
| `SculkPatchFeature.place` **direct rare shrieker** | For `extraRareGrowths` sampled attempts, choose `origin.offset(dx,0,dz)`, dx/dz in [-2,2]; if AIR with physical `below()` sturdy `Direction.UP`, `setBlock(..., SCULK_SHRIEKER.defaultBlockState().setValue(CAN_SUMMON,true),3)` | Explicit **world XZ plane, world UP and DOWN** |

Actual vanilla configuration authors [`CaveFeatures.bootstrap`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/data/worldgen/features/CaveFeatures.java#L471-L472):

| Configured feature | `SculkPatchConfiguration(chargeCount,amountPerCharge,spreadAttempts,growthRounds,spreadRounds,extraRareGrowths,catalystChance)` |
|---|---|
| `sculk_patch_deep_dark` | `(10,32,64,0,1,ConstantInt(0),0.5)` |
| `sculk_patch_ancient_city` | `(10,32,64,0,1,UniformInt(1,3),0.5)` |

**Deep dark:** direct rare-shrieker branch runs **zero** attempts, but worldgen spreader may still create shriekers. **Ancient city configured feature:** 1–3 *attempts*, not guaranteed placements. Both catalyst branches apply 0.5 probability **plus** the full-collider support gate; not 50% of all chunks/blocks. Original [`SculkPatchConfiguration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/configurations/SculkPatchConfiguration.java#L8-L21) defines those fields. [`CavePlacements`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/data/worldgen/placement/CavePlacements.java#L236-L245) registers separate placed feature holders; deep-dark version has CountPlacement 256 + range/biome filters; ancient-city version no additional modifiers there. This is concrete vanilla worldgen source, **not proof the current PlanetChunkGenerator actually executes it**.

A direct feature `setBlock` is **not** the item `getStateForPlacement`/`setPlacedBy`/`BLOCK_STATE` components path. Future test must check BE instantiation/load and callbacks independently. Extra-rare created shrieker **CAN_SUMMON=true** independently of item default; sculk-growth created shrieker inherits worldgen flag.

## 5. Generic template world-state author, not unverified vanilla template claims

[`StructureTemplate.placeInWorld`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L270-L341) mirrors/rotates saved `BlockState`, calls `ServerLevelAccessor.setBlock`, and can restore saved BE NBT / water effects. Thus a specific **template containing** either selected state can place it without `BlockItem`; saved legal `CAN_SUMMON` is not automatically overwritten by initial item defaults. **No specific vanilla ancient-city .nbt palette was inspected**: do NOT infer that a specific shipped template contains either block. Generic commands, programmatic state copy, load and other mod writes likewise bypass item placement, but particular external callers are not audited in this packet. The concrete `SculkPatchFeature` above suffices to establish both actual vanilla feature writers.

## 6. Phase contract and nonclaims

| Ownership site | Future Planetary boundary |
|---|---|
| `BlockItem` state/BE component writes | Phase 2 item target, legal state composition and callback; Phase 7A BE consistency |
| `SculkBlock` growth | Phase 2/8/9 growth target/support and world-physical density/seam canonical ownership; Phase 5 WATERLOGGED |
| `SculkPatchFeature` direct set | Phase 8/9 generation on six local surfaces: physical world XZ/UP/below gates, feature sampling and non-Planet vanilla behavior |
| `StructureTemplate` | Phase 8/9 direct saved state, transforms and BE NBT; validate particular assets separately |
| `SculkCatalystBlockEntity` death/spread/bloom, `SculkShriekerBlockEntity` vibration/warden | Phase 7A event/tick/data; Phase 3 particles/collision; **not equivalent to block creation** |

Preserve physical `BlockPos`/`Vec3` storage and world event positions; only explicitly local block support, growth plane, face semantics and display should be transformed. No blanket rotation of all world directions.

**Status:** 3A-8.2 **research DONE**, no additional registered concrete BLOCK class promoted. Ledger stays **73/241** reviewed / **194/1060 BLOCK IDs**; **168/241**, **866/1060 IDs** still source pending. All 241 NeoForge patched-ASM, Planet adapter and gameplay acceptance fields **REVIEW_PENDING**. Original ITEM census remains immutable; full Stage 3B uncompleted. No Java runtime/code changes, build, CI/gameplay checks or confirmed Planet worldgen. **NEXT FIRST 3A-8.3:** six-face/edge/corner and BE/source-vs-physical contract with explicit proposed tests; one bounded doc commit and STOP.
