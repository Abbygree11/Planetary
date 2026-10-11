# Stage 3A-21.1/21.2 — original FlowerPotBlock and NeoForge plant registration vs independent modded pots

**2026-10-11 · Planetary branch `2.0` · Minecraft1.21.1/NeoForge21.1.215 Java21 · SOURCE/compiled original declaring owners reviewed, GAMEPLAY/ASM/Planet integration NOT REVIEWED.** Original zip physically opened, [deepest-safe contract](PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md) enforced. One registered vanilla concrete `FlowerPotBlock` class covers **35 distinct original BLOCK IDs**. This study deliberately does not modify Java/Mixins.

## 1. Independent original exact BLOCK ID, ITEM and state census

Original immutable artifact `11643813158` GitHub Actions run `37988064055` local physical ZIP `/mnt/data/phase2-neo1211-registry-census.zip`, SHA256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**; original registry totals **1060 BLOCK IDs / 241 concrete Java class groups / 1333 ITEM IDs / 1712 state property entries**. Original `FlowerPotBlock > Block > BlockBehaviour` comprises exact **35 IDs:**

```text
minecraft:flower_pot
minecraft:potted_acacia_sapling
minecraft:potted_allium
minecraft:potted_azalea_bush
minecraft:potted_azure_bluet
minecraft:potted_bamboo
minecraft:potted_birch_sapling
minecraft:potted_blue_orchid
minecraft:potted_brown_mushroom
minecraft:potted_cactus
minecraft:potted_cherry_sapling
minecraft:potted_cornflower
minecraft:potted_crimson_fungus
minecraft:potted_crimson_roots
minecraft:potted_dandelion
minecraft:potted_dark_oak_sapling
minecraft:potted_dead_bush
minecraft:potted_fern
minecraft:potted_flowering_azalea_bush
minecraft:potted_jungle_sapling
minecraft:potted_lily_of_the_valley
minecraft:potted_mangrove_propagule
minecraft:potted_oak_sapling
minecraft:potted_orange_tulip
minecraft:potted_oxeye_daisy
minecraft:potted_pink_tulip
minecraft:potted_poppy
minecraft:potted_red_mushroom
minecraft:potted_red_tulip
minecraft:potted_spruce_sapling
minecraft:potted_torchflower
minecraft:potted_warped_fungus
minecraft:potted_warped_roots
minecraft:potted_white_tulip
minecraft:potted_wither_rose
```

**ZERO original blockstate properties** on ALL 35! No `FACING`, `WATERLOGGED`, `HAS_PLANT`, Boolean or AGE properties. An empty vs filled pot is a **different registered Block object with own `potted` Java field**, not a legal property on one mutable block. **ONE** original ITEM `minecraft:flower_pot`, `net.minecraft.world.item.BlockItem` with `placed_block=minecraft:flower_pot`; **ZERO** potted variants have directly registered ITEM IDs; the held plant is the item's own ID with BlockItem→plant block and pot's content registry maps to a full pot BLOCK, not to new direct-potted BlockItem. `minecraft:decorated_pot` is independent `DecoratedPotBlock` with BE, NOT part of these 35 IDs.

Original exact **full-parameter nearest JVM declaring owners**, shared across all 35 IDs:

| Method exact fully-qualified signature | Original declaring owner |
| --- | --- |
| `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)` | `Block` |
| `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)` | **`BlockBehaviour`** |
| `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)` | **`FlowerPotBlock`** |
| `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)` | `BlockBehaviour` |
| `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)` | `Block` |

Additional original effective author slots: `useItemOn=FlowerPotBlock`, `useWithoutItem=FlowerPotBlock`, `getShape=FlowerPotBlock`, `getCloneItemStack=FlowerPotBlock` in pinned comparable vanilla source; `rotate/mirror/getFluidState/neighbourChanged=BlockBehaviour`, `onPlace=BlockBehaviour`. The original five owners are compiled original NeoForge and do **not** indicate ASM patched method bodies reviewed or 35 different mechanics implemented.

## 2. Full source call trace, critical semantic traps

Pinned vanilla comparable source `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`: [FlowerPotBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FlowerPotBlock.java), [Blocks registry](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/Blocks.java), [BlockItem](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java), [Items](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/Items.java), [DecoratedPotBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DecoratedPotBlock.java). This is comparable vanilla Java and **not** proof of the transformed NeoForge runtime body, especially since NeoForge patches the pot registry.

**Ordinary empty pot placement** uses `Items.FLOWER_POT` → `BlockItem.useOn` → `place(BlockPlaceContext)` → `getPlacementState` → `Block.getStateForPlacement` → `BlockItem.canPlace` (`canSurvive` + unobstructed) → `Level.setBlock` and after-placement data components/events. It authors only **empty pot**; *placing plants inside* is a separate block-interaction author.

**Full-pot conversion (actual vanilla handler)**: on empty `FlowerPotBlock.useItemOn(ItemStack,BlockState,Level,BlockPos,Player,Hand,Hit)`, inspect the held ITEM, require `instanceof BlockItem`, obtain **that held BlockItem.getBlock()** (not a literal item-name registry), look up `POTTED_BY_CONTENT` mapping to a real filled FlowerPotBlock; if unsupported, returns `PASS_TO_DEFAULT_BLOCK_INTERACTION`; if pot already filled, returns `CONSUME` (no replacement); otherwise **direct `Level.setBlock(pos,filledPot.defaultBlockState(),3)`**, `GameEvent.BLOCK_CHANGE`, statistic `Stats.POT_FLOWER`, consumes one held item, returns sided success. Note 1.21.1 modded NeoForge changes the lookup API: see separate next paragraph. Don't assume `BlockItem.place` or `getStateForPlacement` ran for the **filled** pot. Any simulation that fixes just BlockItem misses in-place swap and support/frame propagation.

**Extraction / no item**: `FlowerPotBlock.useWithoutItem`: empty pot returns `InteractionResult.CONSUME`; filled produces `new ItemStack(potted)`, tries `player.addItem`, drops if full, directly replaces with `Blocks.FLOWER_POT.defaultBlockState()`, emits `BLOCK_CHANGE` and sided success. With item in hand the block may take different action per `useItemOn`; preserve server/client phase order. `getCloneItemStack` in comparable vanilla returns content item for full pot, pot item for empty; no common state property to represent plant removal. The actual precise NeoForge event interception and synchronous client/server ordering remain TRANSFORMED ASM acceptance pending.

**Support / updateShape trap — do NOT overclaim:** original compiled nearest `canSurvive` is **BlockBehaviour**, not overridden by FlowerPotBlock. Comparable `FlowerPotBlock.updateShape` checks *physical `Direction.DOWN`* and then calls `state.canSurvive(level,pos)` to potentially AIR. Since the pot itself has no custom `canSurvive`, **do not infer a required sturdy-below survival predicate or falling/destruction behavior for vanilla pots** without inspecting target patched method/default behavior. This differs from `CakeBlock`'s `below().isSolid()` and `CandleBlock.canSupportCenter`; reusing their support predicate for pots would break parity. `FlowerPotBlock` source declares `SHAPE=Block.box(5,0,5,11,6,11)` physical world-Y shape (same shape all 35 IDs), a hard-coded orientation/render issue in Planet, but absent FACING state means local rotation must be client/voxel transform, not illegal property assignment.

**No WATERLOGGED for FlowerPotBlock** (all 35 original properties empty); no custom `getFluidState` or real SimpleWaterloggedBlock method. Do not retrofit fluid property by algorithm. For true water/fluids, physical world graph remains engine authoritative. `potted` content is a Block reference/Codec `BuiltInRegistries.BLOCK.byNameCodec().fieldOf("potted")`, NOT a plant-position coordinate nor a BE. The pot's contained plant is visual/state identity; **it should NOT run the independent soil/growth/bonemeal/survival logic of the underlying `SaplingBlock`, `CactusBlock`, fungi etc.** Do not treat `minecraft:potted_cactus` as a placed actual cactus block.

**Direct other writer inventory:** `BlockItem` for empty pot; in-place interaction for filled states; `/setblock`, `/fill`, structure/template palette and feature placement of `potted_* ` ID directly (no ordinary player item or FlowerPotBlock.useItemOn), NBT command potential, mod-dispatched item events, custom fake Level/clipboard/contraption, loot/drop state, and server save/reload registry codecs. Do not invent count of vanilla worldgen potted features or treat absence from player ITEM registry as proof they cannot be written directly.

## 3. Critical NeoForge integration surface: not just vanilla static POTTED map

NeoForge 1.21.1 exposes modded APIs `FlowerPotBlock(Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> potted, Properties)`, `getEmptyPot()`, `addPlant(ResourceLocation flowerBlockId, Supplier<? extends Block> fullPot)`, `getFullPotsView()` and `getPotted()` with registration/empty-pot ownership, per-pot mapping and deferred suppliers. Public [NeoForge 1.21.1-21.1.248 Javadoc](https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/minecraft/world/level/block/flowerpotblock) exhibits these contracts, and an independent earlier NeoForge 1.21.0 Javadoc also documents supplier constructor/addPlant. [NeoForge patch follow-up #3254](https://github.com/neoforged/NeoForge/issues/3254) quotes deprecated legacy constructor forwarding to supplier constructor and registration. **These are external comparative version proofs only; the exact 21.1.215 PATCHED method bodies, coremod field redirection, supplier initialization ordering and true create/getPotted call-site signatures have NOT been dumped or ASM-checked in this stage.** Therefore do not globally rewrite vanilla `POTTED_BY_CONTENT` and do NOT mark 21.1.215 target runtime registration works by a comparable Javadoc. For mods, `addPlant` needs the **BLOCK registry ID** and supplier for a **filled** pot, not ItemStack or tag-only inference; no guarantee all third-party plants are registered, no shared global pot owner. A version adapter must preserve exact NeoForge registrations and error/duplicate semantics; loaded binary/ASM verification future.

**Deep compatibility consequence:** patching only old `FlowerPotBlock.useItemOn` via vanilla `POTTED_BY_CONTENT` access is inadequate or can break NeoForge supplier registrations and independent custom pot subclasses. The lowest safe common layer is the *actual item→content ID→empty-pot-owned filled-state lookup contract* with physical input provenance and typed opt-in orientation, while reusing native public extension points; unknown mod callback remains physical. No generic `Block.byItem`/ItemStack name rewriting across all blocks.

## 4. Actual pinned 1.21.1 independent mod implementation: BotanyPots

Public [Darkhax-Minecraft/BotanyPots](https://github.com/Darkhax-Minecraft/BotanyPots/tree/335c997dc456f6ea2b9bc3ff13319dfb791803e6) **PINNED commit 335c997dc456f6ea2b9bc3ff13319dfb791803e6** on 1.21.1 branch; [gradle.properties](https://github.com/Darkhax-Minecraft/BotanyPots/blob/335c997dc456f6ea2b9bc3ff13319dfb791803e6/gradle.properties) sets `minecraft_version=1.21.1`, NeoForge build baseline `neoforge_version=21.1.209`, Java21 and *Bookshelf/Prickle dependencies*. That development baseline is **older** than Planetary target21.1.215, so unlike the Stage20 mod's too-new build it is at least plausible to attempt loading a compatible binary; no guarantee dependency ranges or actual binaries are compatible, **NO mod runtime PASS claimed**.

[BotanyPotBlock](https://github.com/Darkhax-Minecraft/BotanyPots/blob/335c997dc456f6ea2b9bc3ff13319dfb791803e6/common/src/main/java/net/darkhax/botanypots/common/impl/block/BotanyPotBlock.java) extends **`BaseEntityBlock`, NOT FlowerPotBlock**; implements `SimpleWaterloggedBlock`, custom Bookshelf `IBlockHooks`. Own legal original *modded* `WATERLOGGED`, `LEVEL`, four-way `HORIZONTAL_FACING`; own `getStateForPlacement` reads TRUE target FluidState, uses `getNearestLookingDirection` opposite only when horizontal; own voxel shape (2,0,2)-(14,8,14) and renderer; own `useItemOn` distinguishes BASIC harvest, Fertilizer/PotInteraction recipes, then open BE menu. `BotanyPotBlockEntity` tick-based crop/soil processing, item inventories and recipe cache, Comparator output, onRemove inventory drops. A vanilla FlowerPotBlock Mixin is **guaranteed not to apply** to this independent class by inheritance; global waterlogged/Level/Direction patches would break its independent behavior.

[BotanyPotBlockEntity](https://github.com/Darkhax-Minecraft/BotanyPots/blob/335c997dc456f6ea2b9bc3ff13319dfb791803e6/common/src/main/java/net/darkhax/botanypots/common/impl/block/entity/BotanyPotBlockEntity.java) additionally caches a **physical `pot.below`** for hopper exports and calls `Services.GAMEPLAY.inventoryInsert(...,Direction.UP,...)` into the target below, with world-aware `ServerLevel`; [AbstractBotanyPotBlockEntity](https://github.com/Darkhax-Minecraft/BotanyPots/blob/335c997dc456f6ea2b9bc3ff13319dfb791803e6/common/src/main/java/net/darkhax/botanypots/common/impl/block/entity/AbstractBotanyPotBlockEntity.java) defines sided inventory `getSlotsForFace(Direction.DOWN)`, `canTakeItemThroughFace(Direction.DOWN)` and separate fake player facing. These are machine/logistics sidedness semantics, **not** generic FlowerPot potted-content lookups; unknown mods must default to ORIGINAL PHYSICAL directions, with a separate opted-in sided-inventory/logistics adapter only after verifying every caller/provider. `BotanyPotRenderer` and context data recipe/display APIs require separate client rendering tests on side/bottom world faces; no assertion of loaded-game correctness.

This is an **independent implementation** demonstration. An inherited `FlowerPotBlock` mod extension could use NeoForge `addPlant`/supplier API, but no separately pinned 1.21.1 source subclass was verified in this packet; **do not invent a claimed tested inherited-mod case**. NeoForge public supplier API is demonstrated by comparative Javadoc and is an explicit future actual-module loader test.

## 5. Whole-family deepest SAFE provenance and adapter matrix

| Call/domain | Deepest safe boundary / future policy | Why shallower or deeper globally is wrong |
| --- | --- | --- |
| **PHYSICAL_DO_NOT_TOUCH** | Always preserve real Level/BlockPos, source BlockItem, actual clicked BlockHitResult, true six-face graph, FluidState and mod events | Rewriting `Level.getBlockState/setBlock`, `BlockPos.below` or `Direction` silently corrupts BE/foreign worlds and modded machines |
| **SHARED_SAFE** | Carry original source/target physical coords, clicked face, original `BlockItem` `getBlock`, local frame and chart provenance through generic dispatch; unknown mod receives original physical values | BlockItem-only changes do not cover FlowerPotBlock direct state swap, fake world generation and independent mod pot |
| **OPT_IN_ALGORITHM: vanilla pot** | Use exact NeoForge empty-pot owner + `addPlant`/deferred supplier lookup and `useItemOn/useWithoutItem` direct conversion with real item consume/stat/drop; typed local visual normal and target canonical chart | Replacing static POTTED_BY_CONTENT map or inventing a property globally breaks NeoForge and 35 distinct IDs |
| **OPT_IN_ALGORITHM: support** | Recognize original inherited BlockBehaviour `canSurvive` / FlowerPot updateShape physical DOWN check; only adapt proven semantic support while retaining original predicate | Claiming `Block.canSupportCenter` for pots copies Candle rule not present in source and may make floating pots wrongly break |
| **OPT_IN_ALGORITHM: client** | Pot base/plant composite model and VoxelShape canonical local normal/tangent, per-client shape transform without illegal FACING state | BlockState has NO orientation legal property on all 35 IDs; global model rotation breaks other transparent blocks |
| **MOD_INDEPENDENT: BotanyPots** | Own custom BaseEntityBlock, soil/crop/GUI/water/shape/BE/recipe and typed sided inventory API; explicitly opt in if Planet semantic intended | FlowerPot Mixin cannot cover independent class, generic direction remap changes transport direction chosen by its own provider |
| **WORLDGEN/FOREIGN** | Per authored structure/command/feature/fake Level code path declared physical or canonical before transformation | Direct `Level.setBlock(potted_variant)` bypasses FlowerPotBlock item lookup entirely and cannot be globally intercepted with semantics |
| **PORTED NEOFORGE PATCH GATE** | Verify exact 21.1.215 effective methods and compiled Mixin invoke owners, registration ordering, supplier/getPotted coremod redirection before runtime implementation | Comparable vanilla source and 21.1.248 Javadoc are NOT the actual patched 21.1.215 bytecode proof |

One proposed (NOT IMPLEMENTED) typed stable core: `PlanetPotInteractionContext(realPotPos, realHit, clickedHandAndItem, canonicalFace, localSurfaceNormal, sourceAndTargetChart, registryMappedContentId, implementationKind, foreignWorldKind)` and a separate `PlanetPotSidedLogisticsContext` with **explicit caller+provider opt-in** for machine sides. Exact API/classes are design placeholders, not existing Java declarations. Version-specific NeoForge adapter around public pot mapping; no Mixin redirect of `ItemStack.consume` or generic whole Level. Phase2 placement/interaction, Phase3 render geometry, Phase5 true fluid (only modded pots), Phase7 inventory drop/extraction, Phase8/9 structures/palettes, Phase10 mod BE/sided automation and NeoForge extension patch. **No code or gameplay changes by this research packet.**

[50 future acceptance contracts](PHASE2_STAGE3A_FLOWERPOT_SIX_FACE_INTERACTION_MOD_ACCEPTANCE_1_21_1.md) · [original independent 107-class audit](PHASE2_STAGE3A_FLOWERPOT_ORIGINAL_107_CLASS_RECONCILIATION_1_21_1.md).
