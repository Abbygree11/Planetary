# Planetary — 2.4A-8: whole AXIS placement owner, Chain and independent Infested

Target: Minecraft **1.21.1**, NeoForge **21.1.215**, Java 21. Branch `2.0`.

## Previously researched 55 original IDs and exact owners

Original physical 241/1060 registry + source evidence: [whole 55-ID stage3A axis audit](PHASE2_STAGE3A_ROTATED_PILLAR_ALL_55_ORIGINAL_AXES_MOD_1_21_1.md). 52 exact registered `RotatedPillarBlock` blocks (logs, stripped woods, basalt, froglights, bone, etc), one inherited `HayBlock`, one subclass `ChainBlock` and one independent `InfestedRotatedPillarBlock` = **55**, not 55 different owners.

Comparative original 1.21.1 source: [RotatedPillarBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RotatedPillarBlock.java), [ChainBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ChainBlock.java), [InfestedRotatedPillarBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/InfestedRotatedPillarBlock.java). Chain's override explicitly calls super and adds physical fluid-derived WATERLOGGED; Hay inherits the base owner. Infested extends `InfestedBlock`, **NOT** `RotatedPillarBlock`, with its own getStateForPlacement but the same AXIS(clicked physical face) rule.

## Full semantic adapter (2.4A-8)

`PlanetPillarPlacement` is the single typed common semantic helper for target-canonical AXIS = target-local original clicked-face AXIS. The inherited `RotatedPillarBlockPlacementMixin` was changed from **HEAD cancellation/default-state reconstruction** to **RETURN with original BlockState preservation**; the independent `InfestedRotatedPillarPlacementMixin` explicitly opts into the same safe helper on its own RETURN. Both are narrow versioned adapter classes, no generic Level/Direction rewrite.

This fixes loss of NeoForge/mod-authored returned properties and permits ChainBlock's explicit `super.getStateForPlacement` to carry canonical AXIS while its own WATERLOGGED is read from real physical fluid and added afterward. The helper never modifies extra properties, original block identity, registered item, worldgen, axe stripping, infested entity spawning, hay fall damage or renderer textures.

### Deepest safe compatibility contract

| Boundary | Guarantee |
| --- | --- |
| SHARED_SAFE | Base `RotatedPillarBlock` AXIS after original method for 52 base ID + Hay and Chain subclasses, plus mods inheriting that method |
| OPT_IN_ALGORITHM | Independent `InfestedRotatedPillarBlock` separate Mixin, using same target-local AXIS helper; separate override owners from mods must explicitly integrate |
| MOD_INDEPENDENT | Modded `BlockItem` bypass/override, specialized axes, fake Levels, tree/worldgen and custom structures. Do not imply inherited base Mixin intercepts an unrelated mod class |
| PHYSICAL_DO_NOT_TOUCH | Physical `BlockPos`, `Direction` in the rest of engine, fluid at Chain target, AxeItem strip `Level.setBlock`, worldgen features, structures, actual item state components and rendering |

Pinned independent 1.21.1 Biomes O' Plenty source [ModBlocks.java at 32ec5f0](https://github.com/Glitchfiend/BiomesOPlenty/blob/32ec5f0a17c2101802fc0458f3c6ea85052f70fb/common/src/main/java/biomesoplenty/init/ModBlocks.java) uses `new RotatedPillarBlock` for new log variants, sharing the base owner, but its [FallenJacarandaLogFeature](https://github.com/Glitchfiend/BiomesOPlenty/blob/32ec5f0a17c2101802fc0458f3c6ea85052f70fb/common/src/main/java/biomesoplenty/worldgen/feature/misc/FallenJacarandaLogFeature.java) directly writes physical worldgen AXIS and bypasses player placement. Mod runtime compatibility NOT established by source alone; check actual mod loader binary and NeoForge constraint.

### Independent evidence and V5 lab

- New `PlanetOrientationPropertiesTest` covers six Planet faces × six clicked normals × eight concrete BlockStates: ordinary/stripped logs, crimson stem, basalt, muddy roots, hay, waterlogged chain, infested deepslate; block identity and WATERLOGGED exact preservation, regular no-Planet passthrough.
- `Phase2PlacementFamilyMixinContractTest` asserts loaded 21.1.215 exact method owners (RotatedPillarBlock, ChainBlock super invocation, InfestedRotatedPillarBlock, Hay inherited), exact two compiled callback descriptors, RETURN, cancellable, and mixins.json registration. This proves ABI shapes, not live transformation.
- Current-only six-face V5 Planet Test Lab uses **AMETHYST** central marker and eight CYAN/LIME stations, selected real click support scaffolds for X/Z axis, and prior NETHERITE/EMERALD/DIAMOND/GOLD owned pad migration. Existing old scene is intentionally retired; unknown occupied pads preserved. No unnecessary regression museum.
- Code+tests+NeoForge registry CI: **PENDING** until verified. **Game/client:** placement on +Y and five rotated faces, local X/Y/Z click direction, Chain waterlogging, infested block state, real compatible BOP extra log and unsupported mod override, no-Planet world, 12 true seams/8 corners all remain independently OPEN. Rendering seam vignette bug is Phase3B, not patched here.
