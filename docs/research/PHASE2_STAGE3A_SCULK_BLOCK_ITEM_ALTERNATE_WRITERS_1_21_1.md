# Stage 3A-9.2 — exact ITEM registry creator and non-item SCULK state writers

**2026-10-10 · Planetary `2.0` · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.**

**Scope:** originally registered `net.minecraft.world.level.block.SculkBlock` / `minecraft:sculk`; no new concrete BLOCK owner review and no runtime implementation or test. Complements [3A-9.1 class/inherited declaration and charge review](PHASE2_STAGE3A_SCULK_BLOCK_SOURCE_OWNER_AUDIT_1_21_1.md). This report distinguishes **an ITEM which directly places SCULK**, **a charged sculk vein which directly creates SCULK**, and other worldgen/structure/command writers. Comparative vanilla 1.21.1 Java (pinned commit `b77c5c6995874f6cf2755bc5234428906b337b75`) is **not** a proof of original NeoForge 21.1.215 *patched method bodies*, applied Planet Mixin or gameplay correctness.

## 1. Independent immutable 1333-ITEM → original BLOCK join

Original GitHub Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158** at `aa39572950a15403ea0a9003eefccf3bf6675ff7`. Reopened the existing original ZIP **bytes**, parsed independently via `zipfile` and `csv.DictReader(delimiter='\t')`. **SHA-256 matches** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Actual inventory: **1060** BLOCK IDs, **241** unique concrete BLOCK classes, **1333** ITEM entries and **1712** state-property records; ZIP bytes and these four members unmodified.

Join: select original `BLOCK.java_class = net.minecraft.world.level.block.SculkBlock` → exact `BLOCK.registry_id = minecraft:sculk` → **all** `ITEM.placed_block == minecraft:sculk`. This yields **exactly ONE ITEM**:

| Original field | Exact value |
|---|---|
| `BLOCK.registry_id` / `BLOCK.java_class` | `minecraft:sculk` / `net.minecraft.world.level.block.SculkBlock` |
| `ITEM.registry_id` | `minecraft:sculk` |
| `ITEM.java_class` / `ITEM.class_hierarchy` | `net.minecraft.world.item.BlockItem` / `BlockItem>Item` |
| `ITEM.block_item` / `ITEM.placed_block` | `true` / `minecraft:sculk` |
| Original `ITEM.audit_disposition` | `BLOCKITEM_CREATION_REVIEW_PENDING` (historical ZIP unchanged) |
| Exact join cardinality | **1 ITEM / 1 target BLOCK**; no other matching registered ITEM subclasses/aliases |

Original `ITEM.effective_method_owners` full original method arguments (not name patterns):

| Method signature in original compiled inventory | Nearest declaring owner |
|---|---|
| `useOn(net.minecraft.world.item.context.UseOnContext)` | **BlockItem** |
| `place(net.minecraft.world.item.context.BlockPlaceContext)` | **BlockItem** |
| `updatePlacementContext(net.minecraft.world.item.context.BlockPlaceContext)` | **BlockItem** |
| `getPlacementState(net.minecraft.world.item.context.BlockPlaceContext)` | **BlockItem** |
| `placeBlock(net.minecraft.world.item.context.BlockPlaceContext,net.minecraft.world.level.block.state.BlockState)` | **BlockItem** |
| `canPlace(net.minecraft.world.item.context.BlockPlaceContext,net.minecraft.world.level.block.state.BlockState)` | **BlockItem** |
| `registerBlocks(java.util.Map,net.minecraft.world.item.Item)` | **BlockItem** |
| **Separate** `use(net.minecraft.world.level.Level,net.minecraft.world.entity.player.Player,net.minecraft.world.InteractionHand)` | **Item**, not BlockItem |

This is **7/7** placement/registration owners on `BlockItem`; inherited `Item.use` is a separate eighth method. Pinned [`Items.SCULK`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/Items.java#L414-L418) registers `registerBlock(Blocks.SCULK)`; [`Items.registerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/Items.java#L1808-L1837) builds a `new BlockItem`.

**Exact reproducible original joined-row SHA-256**: `e3e5e3822245209bfb6923e4cf3226e7649a24af9481d8cf1d3d46b7fde246e4`. Construct UTF-8 line `BLOCK.registry_id|ITEM.registry_id|ITEM.java_class|ITEM.placed_block|ITEM.effective_method_owners\n`, no column normalizations or delimiters removed, sort by full original BLOCK.registry_id ascending (one entry), then hash; 810 characters. This is an original-ZIP audit fingerprint, not a claim about NeoForge patched body.

## 2. ITEM placement and post-place components — what is possible for SCULK

Pinned [`BlockItem.useOn/place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L45-L120) does `BlockPlaceContext` → `updatePlacementContext` → `getPlacementState` (calls block `getStateForPlacement`) → `canPlace` → `placeBlock` (physical Level `setBlock(target,state,11)`) → reread state → optional components → `setPlacedBy` → play sound, `GameEvent.BLOCK_PLACE` and consume item. `SculkBlock`'s default placement state is inherited `Block.getStateForPlacement`. `canSurvive` is inherited from `BlockBehaviour`. `setPlacedBy` is inherited `Block`, not custom growth logic. `BlockItem.canPlace` includes physical collision/unobstructed check; a Planet placement adapter must not reinterpret click/target as face-atlas storage.

**Important negative claim backed by the real property definition and component code:**

- `SculkBlock` declares **ZERO BlockState properties** in the original compiled registry (confirmed 3A-9.1). [`BlockItem.updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158-L176) reads optional `DataComponents.BLOCK_STATE` and invokes [`BlockItemStateProperties.apply`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/component/BlockItemStateProperties.java#L42-L60). That method iterates requested property names but only applies entries found in the target block's `StateDefinition`; unknown names are ignored. **Thus an arbitrary BLOCK_STATE component cannot add FACING, WATERLOGGED or a custom property to vanilla SCULK**, and cannot mutate any defined property because there are none. Normal no-component stacks skip the component path. This differs from a SENSOR/SHRIEKER target, which has genuine legal properties.
- [`BlockItem.updateCustomBlockEntityTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L197-L225) and [`updateBlockEntityComponents`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L135-L144) conditionally operate only if a **BlockEntity** exists at the target. Vanilla `SculkBlock` **has no BE**. An arbitrary `BLOCK_ENTITY_DATA` component therefore does not create one or write BE NBT for ordinary sculk placement. Do not generalize this to a different modded BE-owning block.
- State shape/collision is still a *physical* BlockState query and eligible growth **is not invoked on player `BlockItem.place`**. There is no implicit `SculkBehaviour.attemptUseCharge` when placing an ordinary sculk item.
- `BlockItem` also accepts player-null/fake/automated `BlockPlaceContext`, subject to owning engine checks. Original exact ITEM runtime paths do not prove all third-party tools/commands or alternate creation; those are distinct write boundaries.

## 3. Confirmed vanilla non-item writer: CHARGE on SculkVeinBlock → replace substrate with SCULK

[`SculkVeinBlock.attemptUseCharge`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L113-L169) is the **concrete target-SCULK state writer**, NOT `SculkBlock.attemptUseCharge`. Its relevant path:

1. `SculkSpreader.ChargeCursor.update` gets the **actual block at physical `cursor.pos`** and selects its `SculkBehaviour` implementation; `SculkVeinBlock.attemptUseCharge` is used *when a cursor currently sits on a vein*. The optional spreading pass can change block state via `attemptSpreadVein` first, subject to charge cursor flags; don't equate all charge updates with a block conversion.
2. Only if `spreadFlag` is true, call `SculkVeinBlock.attemptPlaceSculk`. It shuffles **all six** physical `Direction` values and considers only a direction for which the current **vein BlockState has that face**. Let `target = cursor.pos.relative(direction)`. Its existing target state must belong to `spreader.replaceableBlocks()`: normal mode `SCULK_REPLACEABLE`, worldgen mode `SCULK_REPLACEABLE_WORLD_GEN`. No blind replacement of every neighboring block or a guarantee on every charge update.
3. On matching face + replaceable target, **direct** `LevelAccessor.setBlock(target, Blocks.SCULK.defaultBlockState(),3)`, not `BlockItem.place` and not `SculkBlock.getStateForPlacement`. Next `Block.pushEntitiesUp` handles displaced physical entities, sound fires at target, `SculkVeinBlock.veinSpreader.spreadAll` attempts new attached vein states around new sculk, and nearby vein positions other than the opposite direction may be `onDischarged` (remove faces or replace empty vein with AIR/WATER). This is a **multi-cell writer graph** with multiple real physical positions and target canonical attachment sides.
4. A successful vein-to-sculk mutation costs **one cursor charge**. Otherwise the vein `attemptUseCharge` runs its decay-rate branch (potential halving). This is **not** the SculkBlock `growthSpawnCost` (10 or 50) for a spawned SENSOR/SHRIEKER.

Relevant world-coordinate caveat: `Block.pushEntitiesUp` is in the replacement writer; its physically upward displacement must be assigned to the actual block/entity-movement owner (entity Phase 7), rather than assuming the block's placement state alone accounts for local gravity. `SculkVeinBlock` is ALREADY `SOURCE_REVIEWED_INTEGRATION_PENDING` from the earlier Phase-2 graph cohort; **do not re-promote or double-count it**.

Additional source paths: [`SculkBehaviour.DEFAULT.attemptSpreadVein`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBehaviour.java#L14-L53) may create/re-grow SCULK_VEIN (`getSameSpaceSpreader` / `SculkVeinBlock.regrow`). [`SculkBehaviour.attemptSpreadVein` default](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBehaviour.java#L69-L72) uses `MultifaceSpreader.spreadAll`. [`MultifaceSpreader`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceSpreader.java#L37-L115) has SAME_POSITION/SAME_PLANE/WRAP_AROUND `SpreadPos(pos,face)`; these writes create/update **VEIN**, not directly SCULK, until a later successful charge on a vein replaces tagged substrate. [`SculkVeinBlock.onDischarged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L87-L110) may remove faces or replace empty vein with AIR/WATER, not produce sculk.

**Reverse direction must remain distinct:** [`SculkBlock.attemptUseCharge`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L30-L92) when a cursor sits on existing SCULK produces **SCULK_SENSOR or SCULK_SHRIEKER** at world `pos.above()` under charge, radius, density and random gates. It does NOT create a new SCULK block. The two call paths share `SculkSpreader` but their **target BLOCK IDs and charge costs differ**.

## 4. Vanilla worldgen writer provenance — NOT a direct setBlock(SCULK) in SculkPatchFeature

[`SculkPatchFeature.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/SculkPatchFeature.java#L23-L94) is the concrete feature pipeline. It checks `canSpreadFrom` (at a SCULK-behaviour block or air/source water with a neighboring physical full-collider). It instantiates `SculkSpreader.createWorldGenSpreader()` with different settings; seeds cursors at feature origin, loops `spreadRounds + growthRounds`, runs `updateCursors`, and clears cursors per round. The actual SCULK block mutation occurs **indirectly** if the cursor reaches an eligible SCULK_VEIN writer and `attemptPlaceSculk` succeeds. The feature's **own** explicit direct `setBlock` statements write **SCULK_CATALYST** at feature origin (chance and world-below full support) and **SCULK_SHRIEKER** at some physical XZ candidates (air with world-UP sturdy support), **not** `Blocks.SCULK`. Don't misclassify their outputs.

Actual [`CaveFeatures` registrations](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/data/worldgen/features/CaveFeatures.java#L471-L497):
- `SCULK_PATCH_DEEP_DARK`: `new SculkPatchConfiguration(10,32,64,0,1,ConstantInt.of(0),0.5F)` — 0 extra direct rare shrieker attempts, sculk spread still possible via spreader.
- `SCULK_PATCH_ANCIENT_CITY`: `new SculkPatchConfiguration(10,32,64,0,1,UniformInt.of(1,3),0.5F)` — 1–3 extra rare shrieker *attempts*, not guaranteed states.
- `SCULK_VEIN`: a separate `MULTIFACE_GROWTH` feature with `Blocks.SCULK_VEIN` and specified stone-like substrate whitelist. This feature places veins, **not** directly `Blocks.SCULK`. Subsequent charge/spread may convert suitable substrate via the vein algorithm, but do not assert the standalone feature by itself creates sculk.

Feature/local gravity contract: `SculkPatchFeature` seed/up/below/XZ and `SculkBlock` growth pos.above/density scan operate in **physical world axes**. Planet needs an intentional **Phase 8/9 worldgen-space sampling/target policy**, while `LevelAccessor.setBlock`, world positions and chunk keys stay physical. Do not globally rewrite vanilla `Direction.UP` or imply generation currently passes six-face tests.

## 5. Generic direct state authors: template and commands, not ITEM creators

Pinned [`StructureTemplate.placeInWorld`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java#L270-L335) iterates saved `StructureBlockInfo` palette, transforms state by mirror/rotation and writes via `ServerLevelAccessor.setBlock`, independent of `BlockItem`. A saved template with an ordinary `Blocks.SCULK` state **can** place it directly. It may load saved NBT only if a BE exists and template block NBT is present; sculk itself has no BE. **No particular ancient-city/vanilla .nbt palette was audited**, so there is no claim that a specific shipped structure contains or writes SCULK; generic capability ≠ verified concrete shipped asset.

Pinned [`SetBlockCommand.setBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/server/commands/SetBlockCommand.java#L85-L123) and [`FillCommand.fillBlocks`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/server/commands/FillCommand.java#L152-L193) call [`BlockInput.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/commands/arguments/blocks/BlockInput.java#L70-L103), which first `Block.updateFromNeighbourShapes` and then `ServerLevel.setBlock` with a parsed BlockState; unlike `BlockItem.place` it does not run item-specific context, collision checks, post-item `BLOCK_STATE` update or `BlockItem` callbacks. Commands can write sculk state directly when invoked (not proof of any Planet command behavior). Mod/programmatic direct `Level.setBlock`/state-copy writers are another **open-ended** bypass class, **not an audited enumeration of all external mods**.

| Author / pipeline | Writes `Blocks.SCULK`? | Uses ordinary `BlockItem.place`? | Material ownership |
|---|---|---|---|
| Original `minecraft:sculk` `BlockItem` | **Yes**, directly | **Yes** | Phase 2 placement and physical target |
| `SculkVeinBlock.attemptPlaceSculk` / charged cursor | **Yes**, conditional direct neighbor replacement | **No** | Phase 2/8/9 multi-face graph + target tag/position, Phase 7 entity push |
| `SculkPatchFeature` | **Indirectly**, via `SculkSpreader`/vein writer | **No** | Phase 8/9 feature origin, local sampling; physical writer |
| `SculkBlock.attemptUseCharge` on existing SCULK | **No**; writes SENSOR or SHRIEKER | **No** | Phase 2/8/9 growth and distinct target blocks |
| `MultifaceSpreader` / `SCULK_VEIN` feature | **No direct SCULK**; writes VEIN | **No** | Phase 2/8/9 attachment graph |
| Synthetic/saved `StructureTemplate` containing SCULK | **Yes**, if palette contains state | **No** | Phase 9 generic template mirror/rotation/BE metadata |
| `/setblock` or `/fill` with SCULK state | **Yes**, conditionally | **No** | Phase 9 commands/physical target |
| Other mods / arbitrary `setBlock` | **Possible** | **Not guaranteed** | Unverified external extension; Stage 3B/10 completeness remains open |

## 6. Scope boundary, ledger nonchanges and next microtask

**Research verdict:** exact original SCULK ITEM creator is one ordinary BlockItem with all 7 placing full-signature owner methods declared on BlockItem; separate `Item.use`. Explicit vanilla non-item writer is **SculkVeinBlock**, not SculkBlock itself. Feature SCULK_PATCH reaches that writer **indirectly** through worldgen spread, and direct worldgen `SCULK_VEIN` feature only authors vein. Commands/templates are independent state writers. NO new concrete source+compiled block owner was audited/promoted here. The original ITEM census is immutable and historic `BLOCKITEM_CREATION_REVIEW_PENDING` must not be silently rewritten. This is a bounded source writer-provenance audit, not an exhaustive proof about all structure NBT/mod writers.

**Ledger stays at 74/241** source+original compiled declaration reviewed (**195/1060 BLOCK IDs**), **167/241** pending (**865/1060 IDs**). All 241 NeoForge modified ASM, Planet adapter and gameplay columns remain **REVIEW_PENDING**. No Java code changes, source patch, compiled ASM inspection, successful client/server build or actual gameplay/worldgen test. Both current and next runtime policies **unaccepted**.

**NEXT FIRST Stage 3A-9.3** in [02g research card](../phases/phase-02/02g-sculk-spread-growth-owners.md): define explicit six-face/18-offset seed/charge/vein/seam-corner local growth contract and proposed future tests, one independent docs-only commit then STOP; Stage 3A/Phase 2 remains open.
