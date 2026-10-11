# Stage3A-30.1/30.2 — 55 original axis-pillar blocks, ALL materials, independent infested and mod/worldgen owners

**2026-10-11, Minecraft1.21.1, NeoForge21.1.215, branch `2.0`. SOURCE research ONLY.** Original immutable ZIP GitHub Actions artifact `11643813158` run `37988064055`, physically re-opened and hashed `SHA256 7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Physical original records: **1060 registered BLOCK IDs, 241 concrete classes,1333 ITEM IDs,1712 STATE rows**. Separate Python `csv.DictReader` original physical ZIP parser asserted unique all 1060 IDs, class name, all legal states, exact original class hierarchy, **each of five fully qualified nearest method signatures independently for EACH registered BLOCK ID**, and `ITEM.placed_block` per-ID creators (55/55). Cohort canonical original ZIP SHA256: `2bad3a06438d220246838a7d9361e4efcc874c12713ac67374a2a25c994d47b5`. Comparative source pinned [MC1.21.1 vanilla 1.21.1 tree](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1); it is NOT a NeoForge-patched method-body bytecode dump.

## All original registered concrete classes and complete BLOCK/ITEM provenance

### `RotatedPillarBlock` — 52 original registered BLOCK/ITEM IDs

Exact original per-ID `BLOCK` and 1:1 `ITEM.placed_block` registry strings:

`minecraft:acacia_log`, `minecraft:acacia_wood`, `minecraft:bamboo_block`, `minecraft:basalt`, `minecraft:birch_log`, `minecraft:birch_wood`, `minecraft:bone_block`, `minecraft:cherry_log`, `minecraft:cherry_wood`, `minecraft:crimson_hyphae`, `minecraft:crimson_stem`, `minecraft:dark_oak_log`, `minecraft:dark_oak_wood`, `minecraft:deepslate`, `minecraft:jungle_log`, `minecraft:jungle_wood`, `minecraft:mangrove_log`, `minecraft:mangrove_wood`, `minecraft:muddy_mangrove_roots`, `minecraft:oak_log`, `minecraft:oak_wood`, `minecraft:ochre_froglight`, `minecraft:pearlescent_froglight`, `minecraft:polished_basalt`, `minecraft:purpur_pillar`, `minecraft:quartz_pillar`, `minecraft:spruce_log`, `minecraft:spruce_wood`, `minecraft:stripped_acacia_log`, `minecraft:stripped_acacia_wood`, `minecraft:stripped_bamboo_block`, `minecraft:stripped_birch_log`, `minecraft:stripped_birch_wood`, `minecraft:stripped_cherry_log`, `minecraft:stripped_cherry_wood`, `minecraft:stripped_crimson_hyphae`, `minecraft:stripped_crimson_stem`, `minecraft:stripped_dark_oak_log`, `minecraft:stripped_dark_oak_wood`, `minecraft:stripped_jungle_log`, `minecraft:stripped_jungle_wood`, `minecraft:stripped_mangrove_log`, `minecraft:stripped_mangrove_wood`, `minecraft:stripped_oak_log`, `minecraft:stripped_oak_wood`, `minecraft:stripped_spruce_log`, `minecraft:stripped_spruce_wood`, `minecraft:stripped_warped_hyphae`, `minecraft:stripped_warped_stem`, `minecraft:verdant_froglight`, `minecraft:warped_hyphae`, `minecraft:warped_stem`

- Hierarchy: `RotatedPillarBlock>Block>BlockBehaviour`.
- Five original full-qualified argument-signature nearest declaring owners (`getStateForPlacement`, `canSurvive`, `updateShape`, `randomTick`, `setPlacedBy`): `RotatedPillarBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block`.
- Legal original STATE schema: `axis=x,y,z`. Every original BLOCK has an ITEM with matching ID, `block_item=true`, concrete `net.minecraft.world.item.BlockItem`.
- [Pinned comparable MC1.21.1 RotatedPillarBlock source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RotatedPillarBlock.java).

### `HayBlock` — 1 original registered BLOCK/ITEM IDs

Exact original per-ID `BLOCK` and 1:1 `ITEM.placed_block` registry strings:

`minecraft:hay_block`

- Hierarchy: `HayBlock>RotatedPillarBlock>Block>BlockBehaviour`.
- Five original full-qualified argument-signature nearest declaring owners (`getStateForPlacement`, `canSurvive`, `updateShape`, `randomTick`, `setPlacedBy`): `RotatedPillarBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block`.
- Legal original STATE schema: `axis=x,y,z`. Every original BLOCK has an ITEM with matching ID, `block_item=true`, concrete `net.minecraft.world.item.BlockItem`.
- [Pinned comparable MC1.21.1 HayBlock source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/HayBlock.java).

### `ChainBlock` — 1 original registered BLOCK/ITEM IDs

Exact original per-ID `BLOCK` and 1:1 `ITEM.placed_block` registry strings:

`minecraft:chain`

- Hierarchy: `ChainBlock>RotatedPillarBlock>Block>BlockBehaviour`.
- Five original full-qualified argument-signature nearest declaring owners (`getStateForPlacement`, `canSurvive`, `updateShape`, `randomTick`, `setPlacedBy`): `ChainBlock / BlockBehaviour / ChainBlock / BlockBehaviour / Block`.
- Legal original STATE schema: `axis=x,y,z`, `waterlogged=false,true`. Every original BLOCK has an ITEM with matching ID, `block_item=true`, concrete `net.minecraft.world.item.BlockItem`.
- [Pinned comparable MC1.21.1 ChainBlock source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ChainBlock.java).

### `InfestedRotatedPillarBlock` — 1 original registered BLOCK/ITEM IDs

Exact original per-ID `BLOCK` and 1:1 `ITEM.placed_block` registry strings:

`minecraft:infested_deepslate`

- Hierarchy: `InfestedRotatedPillarBlock>InfestedBlock>Block>BlockBehaviour`.
- Five original full-qualified argument-signature nearest declaring owners (`getStateForPlacement`, `canSurvive`, `updateShape`, `randomTick`, `setPlacedBy`): `InfestedRotatedPillarBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block`.
- Legal original STATE schema: `axis=x,y,z`. Every original BLOCK has an ITEM with matching ID, `block_item=true`, concrete `net.minecraft.world.item.BlockItem`.
- [Pinned comparable MC1.21.1 InfestedRotatedPillarBlock source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/InfestedRotatedPillarBlock.java).

### The non-obvious fourth owner is unrelated by inheritance

`InfestedRotatedPillarBlock` is **not a descendant of `RotatedPillarBlock`**. It is `InfestedRotatedPillarBlock > InfestedBlock > Block > BlockBehaviour`, but its original `getStateForPlacement` independently uses the same `context.getClickedFace().getAxis()`, and `rotate` delegates to `RotatedPillarBlock.rotatePillar`. Hence `@Mixin(RotatedPillarBlock.class)` does **not** intercept that independent `getStateForPlacement`. Deepest shared SAFE semantic Axis resolution helper can support both, but a generic class-based automatic override cannot. `InfestedBlock.hostStateByInfested / infestedStateByHost` uses property-by-property mapping, retaining `AXIS` when both host and infested share the property; its `spawnAfterBreak` spawns silverfish conditionally (tool enchantments/gamerule) with physical spawn positions, Phase7 entity gravity separately.

### Actual algorithm ownership and content-specific behavior

1. **Shared `RotatedPillarBlock` owner (52 exact vanilla IDs + `HayBlock` inherited + `ChainBlock` explicit super).** `getStateForPlacement` takes clicked physical world Direction.Axis, sets `AXIS` x/y/z. Planet local canonical placement must replace *the interpretation* of clicked face via target `PlanetBlockPlacementFrame.localClickedFace().getAxis()`, never change global BlockHitResult, Level coordinates or the registered `BlockItem` pipeline. [Existing `RotatedPillarBlockPlacementMixin`](../../src/main/java/dev/planetary/mixin/RotatedPillarBlockPlacementMixin.java) performs such a callback on base-family method for in-Planet placement; existence/compile is NOT ASM/gameplay acceptance.
2. **Independent axis transforms:** `RotatedPillarBlock.rotatePillar` swaps X↔Z only for CLOCKWISE_90 / COUNTERCLOCKWISE_90 around canonical Y, preserving Y for all rotations and X/Z for 180; `mirror` inherited BlockBehaviour. Never rotate absolute world state just because `AXIS` is present: `NetherPortalBlock` independently has `AXIS=x,z`, a different portal algorithm and P39/Phase9 owner, explicitly **source-pending** outside this 55-ID cohort. State transport through structure/clone across Planet faces must use explicit source and target canonical charts and intended structure transform.
3. **`ChainBlock` override:** inherits `RotatedPillarBlock` but overrides own `getStateForPlacement` to call `super.getStateForPlacement` and add `WATERLOGGED` from real fluid at target; overrides `getShape` with narrow three `AXIS` shapes; overrides `updateShape` to schedule physical WATER tick; `getFluidState` and `isPathfindable` distinct. No universal 'solid 16³ pillar' assumption. Existing base Mixin could run via explicit `super` but must prove exact ASM and mod overridden methods, not count inheritance as test PASS. Chain collision, fluid, occlusion and client renderer Phase3/5 crosschecks are independent.
4. **`HayBlock`** inherits base placement owner but independently overrides `fallOn` with `causeFallDamage(fallDistance,0.2F,fallSource)`. Canonical gravity entity distance and fall damage owner is Phase7; rotating only an AXIS field does not prove hay damage behavior on walls/underside. Physical coordinate/signals remain original.
5. **AxeItem direct late world mutation:** pinned [`AxeItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/AxeItem.java) declares explicit `STRIPPABLES` mapping all original vanilla log/wood/stem/bamboo variants (not universal for every `RotatedPillarBlock` ID). `getStripped` creates target block default state then directly copies `RotatedPillarBlock.AXIS`; `useOn` commits `Level.setBlock(...,11)` without executing original BlockItem placement callback. This **must preserve canonical AXIS state**, but do NOT remap physical world BlockPos/side/physical particles. Modded registration maps and other tools may author different states; they must be independently inspected and not hardcoded by vanilla ID.
6. **BlockItem state components and alternate creators:** all 55 original registered item creators are plain `BlockItem` (including registered infested_deepslate). `BlockItem` uses actual target physical `BlockPos` and collision; item-authored `DataComponents.BLOCK_STATE` may overwrite legal axis *after* `getStateForPlacement`. Also direct `Level.setBlock`, vanilla `StructureTemplate`, `WorldGenLevel`, trees, log features, command operations, modded microblocks and mechanical contraptions bypass ordinary click-derived axis authoring. SOURCE/OWNER audit does not imply late-writer runtime coverage.
7. **All materials are different despite same class:** 52 exact original IDs include eleven wood families and stripped variants, bamboo, crimson/warped, muddy mangrove roots, deepslate, basalt/polished basalt, bone, quartz/purpur pillars, ochre/pearlescent/verdant froglights, etc. No universal 'wood can strip', 'pillar grows', 'all are axis-dependent cube occlusion' rules. Material identity controls sound, drops, tool properties, map color/emission and behavior; shape transforms, flat texture/model quads and culling should not be guessed from block family alone.

## Real external pinned 1.21.1 mod source: inherited vs worldgen bypass vs independent owner

**Biomes O' Plenty** pinned [`ModBlocks.java`](https://github.com/Glitchfiend/BiomesOPlenty/blob/32ec5f0a17c2101802fc0458f3c6ea85052f70fb/common/src/main/java/biomesoplenty/init/ModBlocks.java), `gradle.properties` declared **Minecraft1.21.1, NeoForge21.1.23** (Planet target21.1.215; runtime compatibility not established). The actual `log()` and `logNonIgniting()` factories each explicitly `return new RotatedPillarBlock(...)`, and `HELLBARK_WOOD` or `EMPYREAL_WOOD` constructs new RotatedPillarBlock directly. That **genuinely inherits** vanilla AXIS placement callback. BOP also has custom `FallenLogFeature` and `FallenJacarandaLogFeature` pinned [source](https://github.com/Glitchfiend/BiomesOPlenty/blob/32ec5f0a17c2101802fc0458f3c6ea85052f70fb/common/src/main/java/biomesoplenty/worldgen/feature/misc/FallenJacarandaLogFeature.java) that choose `Direction.Plane.HORIZONTAL` then explicitly `BlockState.setValue(RotatedPillarBlock.AXIS,direction.getAxis())`, calling physical worldgen `setBlock`, **never BlockItem placement**. This is a proven third-party direct write path; local radial log worldgen policy belongs to Phase8. Don't globally replace Direction.Plane.HORIZONTAL: many terrain algorithms intentionally use physical world XZ.

**Every Compat/WoodGood** source SHA `18c5c1dbd04b4ffdcbcd5316f8cff88f43062996` MC1.21.1 but NeoForge21.1.248 (**above** target 21.1.215), its real [MacawStairsModuleAbstract](https://github.com/MehVahdJukaar/WoodGood/blob/18c5c1dbd04b4ffdcbcd5316f8cff88f43062996/common/src/main/java/net/mehvahdjukaar/every_compat/modules/macaw/MacawStairsModuleAbstract.java) requests STRIPPED_LOG as a real dynamically generated `WoodType` child and factory input. This verifies modded wood dependency/writer/texture pathways are not finite '11 vanilla tree IDs', **not** that WoodGood supplies its own `RotatedPillarBlock` constructor, or that modded logging/stripping needs a new axis owner. Actual loaded Java classes and overriding methods must be checked, with at least one independent non-vanilla axis owner. For Create shafts, external machines, worldgen and virtual Contraptions use explicit source (physical, canonical or virtual graph) provenance and typed mod opt-in; no automatic local-axis patch based on a name, `AxisProperty`, tag or class name.

## Deepest-safe owner/extension decision

| Call/domain | Safe integration boundary | Unsafe blanket shortcut / next owner |
|---|---|---|
| Real physical Level/BlockPos/Direction, worldgen XZ, fake/contraption Level | `PHYSICAL_DO_NOT_TOUCH`; preserve actual world coordinates and third-party graph behavior | Global rewrite `Direction.Axis`, clicked physical normal, `BlockItem.place`, `Level.setBlock` |
| Vanilla `RotatedPillarBlock` including modded constructor/inherited subclass | `SHARED_SAFE`: stable Planet typed placement-frame local clicked-face axis; thin known callback adapter | Per-material hardcoded mixins; expecting subclass `getStateForPlacement` override to invoke super |
| `ChainBlock` subclass | `SHARED_SAFE` **only** through known `super.getStateForPlacement` route; independent WATERLOGGED/getShape/updateShape preserved | Treat axis-dependent chain like 16³ log or fabricate support |
| `InfestedRotatedPillarBlock` unrelated AXIS algorithm | `OPT_IN_ALGORITHM`: explicitly adapt its independent placement caller by same stable axis API after ASM proof | Claim existing RPB Mixin covers unrelated class |
| Modded independent `AXIS` class and overridden `getStateForPlacement` | `OPT_IN_ALGORITHM` based on actual versioned method owner + semantic contract | `BlockStateProperties.AXIS` wildcard state rewrite |
| Axe stripping, commands, structure clones, feature code | `MOD_INDEPENDENT`: inspect each writer; property-preservation and explicit source/dest charts; worldgen policy Phase8 | Force click-placement orientation onto direct state writer |
| Hay fall damage, infested silverfish motion, chain fluid, pillar model | Crossphase owner Phase7 / Phase5 / Phase3 respectively; no source behavior rewrites here | Pretend Axis placement verifies fall damage, fluid or render compatibility |

**Research disposition**: four new concrete original classes/55 registered IDs -> `SOURCE_REVIEWED_INTEGRATION_PENDING`; all 241×3 ASM/Planet adapter/gameplay flags remain REVIEW_PENDING. No Java/Mixin/CI/client/server/game JAR change or PASS.
