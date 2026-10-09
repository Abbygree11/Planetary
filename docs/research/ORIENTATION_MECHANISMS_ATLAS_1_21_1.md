# Orientation mechanism atlas — Minecraft 1.21.1 / Planetary Phase 2

**Status: cross-family SOURCE RESEARCH / frozen mechanism inventory for implementation planning. NOT Phase-2 completion.**  
Game target: Minecraft 1.21.1, NeoForge 21.1.215, Java 21.  
Source reference: `hackersense/OptiFine-Source` 1.21.1 Mojang-named comparative source. **Actual NeoForge 21.1.215 bytecode and patchset must win over comparative sources.**  
Existing foundations: `GRAVITY_MECHANISM_MAP_1_21_1.md`, `BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md`, `PLACEMENT_1_21_1.md`, `SUPPORT_UPDATES_1_21_1.md`, `PHASE2_PLACEMENT_SUPPORT_FAMILIES_1_21_1.md`, `PORTABILITY_STRATEGY.md`.

## 0. Why this audit exists

The user accepted the core particle motion, debris, torches and seam/load behavior, but repeatedly discovered placement/survival/model failures on rotated faces (saplings, candle/cake, hanging spore blossom, End Rod, Ender Chest, enchanting table, flint/steel). Prior tests of several placement properties and survival cases passed, but the end-to-end behavior was NOT accepted. Subsequent fixes targeted several concrete classes before proving that every sibling algorithm and alternative creation path had been enumerated.

**Phase 2 will NOT be declared complete until every mechanism below has (1) a source-owner inventory, (2) a shared semantic contract, (3) a disposition for all subclasses/bypass paths, (4) automated regression evidence AND (5) a real six-face/edge integration acceptance.** "Mentioned in an old research note" and "Mixin exists" are not completion states. "All blocks with FACING" is not one code family.

## 1. What was actually enumerated

The comparative 1.21.1 GitHub source tree (`main` under `1.21.1/net/minecraft`) exposes:
- **293** top-level Java files in `world/level/block` (including utility/abstract classes and `package-info`, **not** 293 registered block IDs).
- **121** top-level Java files in `world/item` (not all are placement items).
- **74** files in `world/level/block/entity`.
- **28** files in `client/renderer/blockentity`.

Every one of the 293 source filenames is indexed at `PHASE2_VANILLA_CLASS_CENSUS_1_21_1.tsv`. Classification there is **candidate family from declared class name**, not an assertion that every method has been individually examined. The full first-pass comparative-source **pattern inspection is now recorded for 293/293 top-level block-directory Java files**. Exactly **53** have direct representative method spot-checks (`SOURCE_METHODS_SPOT_CHECKED`) and **240** have initial source-pattern inspection only (`SOURCE_PATTERN_SCANNED_SEMANTIC_REVIEW_PENDING`). No file remains merely name-indexed, but this **does not** mean 293 block implementations are behaviorally verified: pattern matches miss inheritance, virtual dispatch, alternate sources and NeoForge patches. The initial family classification is still a candidate map, not an authoritative registry-derived coverage verdict. Before final Phase-2 acceptance, produce a version-locked compiled class/registry census, and resolve all inherited/overridden orientation algorithms. See §8.

### Sources directly rechecked in this pass

`BlockPlaceContext`, `UseOnContext`, `DirectionalPlaceContext`, `BlockItem`, `StandingAndWallBlockItem`, `ScaffoldingBlockItem`, `PlaceOnWaterBlockItem`, `HangingSignItem`, `ArmorStandItem`, `EndCrystalItem`, `FlintAndSteelItem`, `FireChargeItem`, `BoneMealItem`, `BlockStateProperties`, `BlockBehaviour`, `Rotation`, `Mirror`, `StructureTemplate`; `DirectionalBlock`, `HorizontalDirectionalBlock`, `FaceAttachedHorizontalDirectionalBlock`, `AbstractFurnaceBlock`, `BushBlock`, `CakeBlock`, `CandleBlock`, `CandleCakeBlock`, `SporeBlossomBlock`, `EndRodBlock`, `EnderChestBlock`, `StairBlock`, `TrapDoorBlock`, `SlabBlock`, `FenceGateBlock`, `LanternBlock`, `WallSignBlock`, `StandingSignBlock`, `CeilingHangingSignBlock`, `WallHangingSignBlock`, `CrafterBlock`, `JigsawBlock`, `DecoratedPotBlock`, `DoorBlock`, `DoublePlantBlock`, `BedBlock`, `ChestBlock`, `RailState`, `BaseRailBlock`, `ScaffoldingBlock`, `GrowingPlantBlock`, `MultifaceBlock`, `WallBlock`, `VineBlock`, `ChainBlock`, `AbstractChestBlock`, `AttachedStemBlock`, `CaveVinesBlock`, `CaveVinesPlantBlock`, `ChiseledBookShelfBlock`, `CocoaBlock`, `ConduitBlock`, `SeaPickleBlock`.

Source mapping differences or NeoForge patches MUST be verified at Gradle's actual 21.1.215 class-file layer. Do not equate upstream GitHub's current NeoForge `main` with this historic game version; patch path coincidence alone is insufficient.


### Additional source-owner spot-checks of formerly unclassified subclasses

- `AbstractFurnaceBlock` is the actual furnace-like horizontal-facing placement owner, so a fix limited to the concrete FurnaceBlock would miss BlastFurnace/Smoker.
- `ShulkerBoxBlock` writes its full six-direction `FACING` from the **physical clicked face**; it is a new P07 priority candidate, plus a custom block-entity renderer and collision/open animation.
- `VaultBlock` independently writes horizontal FACING; it does not inherit AbstractFurnace placement even though the placement expression resembles it.
- `ChiseledBookShelfBlock` has independent horizontal placement **and** maps physical hit position/face into an inventory slot. This is a Phase-2 interaction-frame problem that cannot be solved by model rotation alone.
- `CocoaBlock` has state-directed jungle-log support and custom nearest-direction placement; it bypasses BushBlock's standard soil-survival implementation.
- `SeaPickleBlock` overrides canSurvive and support face-shape (not just BushBlock's below rule), plus replace-to-increment and waterlogging.
- `AttachedStemBlock` updates a FACING-directed fruit neighbor after growth and has independent rotate/mirror behavior.
- `WeatheringCopperDoorBlock/SlabBlock/StairBlock/TrapDoorBlock` inherit algorithms from their respective base mechanism families: a correct family adapter should cover weathering variants **without class-specific Mixins**. The audit must still inspect overrides.
- `CopperBulbBlock` is mostly neighbor redstone signal/POWERED/LIT logic (Phase 7A), not a new geometric placement orientation vocabulary.
- `RespawnAnchorBlock` is a useItemOn/charge/respawn-interaction owner (part Phase 7B) rather than a universal facing mechanism.
- `TrialSpawnerBlock` and `VaultBlock` have effect/event and UI state ownership distinct from placing oriented basic models.

These findings revise the candidate census but **do not imply 193 individually verified gameplay paths or completion of the 100 remaining entries**. Registry/bytecode census remains mandatory.

### 121-item source census: primary alternative creation paths recorded

`docs/research/PHASE2_ITEM_CREATION_CENSUS_1_21_1.tsv` lists
**all 121** top-level `world/item` Java source files. All received an
initial method/direction **source-pattern scan**, not full inherited-method
analysis. In particular, `BlockItem`, `StandingAndWallBlockItem`,
`ScaffoldingBlockItem`, `PlaceOnWaterBlockItem`, `HangingSignItem`,
`ArmorStandItem`, `EndCrystalItem`, `FlintAndSteelItem`,
`FireChargeItem`, `BoneMealItem`, `DoubleHighBlockItem`,
`BedItem`, `HoeItem`, `ShovelItem`, `DyeItem`, `BrushItem`,
`ItemFrameItem`, `HangingEntityItem`, `SpawnEggItem`,
and `MobBucketItem` require appropriate owner classification.
Not all are **block** placement, but they can own spatially oriented
world creation, vegetation or interaction.

`BlockItem` source has late component-driven `BLOCK_STATE`
overrides; `ScaffoldingBlockItem` can change the target physical
cell; `PlaceOnWaterBlockItem` alters the physical hit/cell;
`HangingSignItem` imposes subclass-specific attachment rules.
These and `DirectionalPlaceContext` are proof that testing only the
standard player `Block.getStateForPlacement` does not prove complete
block orientation.

**Coverage limitation:** a source file may contain multiple methods
with distinct semantic owners; item subclasses and registered item IDs
often outnumber top-level source filenames. Compiled
`BuiltInRegistries.ITEM` plus method-override/bytecode census
is required before claiming no bypass creation pathways remain.

## 2. Invariants and coordinate ownership (NON-NEGOTIABLE)

| Input/output | Semantic space | Required handling |
|---|---|---|
| Level BlockPos, chunks, physical hit point, raw neighbor notification | PHYSICAL | Keep physical; no global BlockPos/Direction interception |
| Source click face | PHYSICAL hit normal | May *derive* target-local clicked face, preserving original hit normal |
| Player look order/yaw/pitch, sneaking intent | BODY/view + physical hit | Derive local tangent/vertical orientation at target; account for player frame differing from block frame across seams |
| Stored BlockState FACING/HALF/AXIS/rail/attachment | CANONICAL BLOCK-LOCAL | All values interpreted in the owning **target's** local chart |
| One step to a supporting/pair/graph neighbor | TRAVERSAL | Resolve physical adjacent block with source chart, then translate direction into **neighbor's** state frame |
| Support faces and shape queries | CANONICAL shape -> PHYSICAL face | Preserve support predicate's actual semantics (`isSolid` != `canSupportCenter`) |
| `updateShape` physicalDirection and physical neighborPos | PHYSICAL callback | Determine which local semantic neighbor changed by physical position, do not globally rotate callback |
| Level/block entity persistence, fluid positions, network | PHYSICAL position + semantic BlockState | Preserve vanilla serialization/NeoForge components and owner frame |
| Structures/WorldEdit/commands/fake-player placement | external or declared provenance | Do NOT assume body-local player context; require explicit transform policy |
| Rendered model/BER and texture orientation | RENDER local -> camera/world | Phase 3, separate from whether state/placement is correct |

A single "rotate FACING" Mixin, a global rewrite of `getClickedFace`, `BlockPos.relative` or `Direction.Plane.HORIZONTAL` breaks physical semantics, subclass quirks, server interactions, or other mods.

## 3. Full player-to-world placement pipeline: ALL adaptation points

1. Input/picking: `BlockHitResult` and `UseOnContext` carry **physical** position/face; `UseOnContext.getHorizontalDirection` uses player yaw; `getRotation` is yaw, not target-local by default.
2. `BlockPlaceContext` determines replacing clicked block vs. physically adjacent target using `BlockPos.relative(hit.getDirection())`; `getNearestLookingDirection(s)`, `getNearestLookingVerticalDirection`, `getHorizontalDirection` combine body/view and click behavior. `DirectionalPlaceContext` is an independent context with explicitly manufactured direction ordering (dispenser/special placements): not guaranteed a real Player.
3. `ItemStack.useOn` / `BlockItem.useOn` / `BlockItem.place`: item may override `updatePlacementContext` or `getPlacementState`, changing target **after** initial click.
4. `Block.getStateForPlacement(context)`: virtual per-class/base-family algorithm derives properties from physical hit, body look, world-Y, local fractions and neighboring states. Some return null; do not insert invalid states.
5. `BlockItem.canPlace` tests `mustSurvive()/canSurvive` AND unobstructed collision; support must use correct physical position/face. `canBeReplaced` is also a placement algorithm (slab stacking, snow layers, sea pickles, multiface).
6. `Level.setBlock` creates PHYSICAL cell, triggers neighbor notifications and block entity lifecycle.
7. **Late state override**: `BlockItem.updateBlockStateFromTag` applies `DataComponents.BLOCK_STATE` through `BlockItemStateProperties.apply` **after placement**. This bypasses the earlier placement property's computed value; explicit context/orientation policy and authority needed, not merely a `getStateForPlacement` patch.
8. `setPlacedBy` may place more physical cells, create an upper part/head, write block entity component data, generate sounds, trigger achievements and hooks. Multi-block survival/update must refer to all installed cells and own their state frames.
9. Server `neighborChanged`, `updateShape`, scheduled/random ticks and physical six-neighbor `NeighborUpdater` continuously maintain/replace states. Source direction and neighbor direction are NOT generically interchangeable.
10. Worldgen, `StructureTemplate.placeInWorld`, commands and mod automation may call `setBlock` directly, without any `BlockPlaceContext`. `StructureTemplate` explicitly calls `BlockState.mirror(...).rotate(...)`: these are *vanilla horizontal orientation operations*, not a universal 3D Planet transform.
11. `getShape`/collision/support/occlusion and static baked models vs BER may independently interpret state properties. Rendering cannot be used as proof that placement/survival is correct.

**Hard research finding:** a future comprehensive patch limited to `getStateForPlacement` WILL miss points 2–3, 5, 7–10. Tests that instantiate a BlockState directly cannot validate the full pipeline.

## 4. Complete mechanism-owner inventory: Phase-2 orientation and structure

The mechanism families below are distinct contracts even if they share `Direction`. Status is from current `2.0` **source presence** and prior explicit user feedback, not newly executed gameplay.

| ID | Source owner / family | Orientation-bearing submechanisms | Current status / gate |
|---|---|---|---|
| P01 | `UseOnContext`, `BlockPlaceContext` | physical hit/target, replacement, player horizontal, nearest/vertical look, local click fractions | Shared `PlanetFrameApi` and `PlanetBlockPlacementFrame` EXIST; full alternate-path coverage OPEN |
| P02 | `DirectionalPlaceContext` + automation | no-player/fake-player direction ordering; item-generated placement | OPEN: do not assume player exists or rotate physical dispenser context |
| P03 | `BlockItem` | canPlace, placementState, state/component post-override, setPlacedBy, collision | VANILLA boundary audited; late BLOCK_STATE override / null-player tests OPEN |
| P04 | Specialized BlockItems | `StandingAndWallBlockItem` standing choice, HangingSignItem extra restriction, ScaffoldingBlockItem context traversal, PlaceOnWaterBlockItem forced above, SolidBucket/Bed/DoubleHigh | Standing/wall helper EXISTS; rest OPEN |
| P05 | Non-BlockItem placement tools | FlintAndSteel / FireCharge, BoneMeal, ArmorStand, EndCrystal, buckets, spawn items | OPEN: separate useOn/level geometry; some fluid/portal/entity-dependent |
| P06 | `RotatedPillarBlock` / `RodBlock` / `ChainBlock` | AXIS including six physical faces, waterlogged state | RotatedPillar adapter EXISTS; variant/overrides acceptance OPEN |
| P07 | Full 6-direction state | `DirectionalBlock` vocabulary; Observer, Dispenser, Dropper, lightning rod, End Rod, piston, clusters, shulker | No unified algorithm; Hopper and EndRod adapters EXIST; other owners OPEN |
| P08 | Horizontal 4-direction state | `HorizontalDirectionalBlock`, `AbstractFurnaceBlock`, chest/barrel/EnderChest, lectern, jukebox? stonecutter, machine blocks | `HorizontalDirectionalBlock` only provides rotate/mirror, NOT placement. EnderChest adapter EXISTS; rest OPEN |
| P09 | Dual-axis `FrontAndTop` | CrafterBlock `ORIENTATION`, JigsawBlock `ORIENTATION`; front and top orthogonal | R5 research only; no coverage confirmed |
| P10 | 16-way rotations | StandingSignBlock, CeilingHangingSignBlock, standing banners/skulls; yaw quantization and ROTATION_16 | UNIMPLEMENTED general family; model/BER separate |
| P11 | Face attachment | `FaceAttachedHorizontalDirectionalBlock`: button, lever, grindstone, bell | Shared placement/support/update adapter EXISTS; interaction variants OPEN |
| P12 | Standing/wall item dual variants | torch/sign/banner/skull and hanging variants | StandingAndWallBlockItem adapter EXISTS; multiple special subclass checks OPEN |
| P13 | HANGING boolean + attachments | LanternBlock, ceiling/wall hanging signs; local UP/DOWN, dual lateral supports | Most only research, implementation OPEN |
| P14 | Stairs | `StairBlock` HALF from click + FACING + derived shape from neighboring stairs | OPEN; slab's `HALF` implementation does NOT cover stairs |
| P15 | Slabs/replacement | `SlabBlock` TYPE TOP/BOTTOM/DOUBLE, `canBeReplaced`, click height | Slab adapter EXISTS; +Y and all-face user placement unverified |
| P16 | Trapdoors | `TrapDoorBlock` HALF, FACING, OPEN, POWERED from physical face and click height | OPEN; wall-click and top/bottom must both work |
| P17 | Hinged doors | `DoorBlock` FACING, hinge computed from surrounding physical blocks, UPPER/LOWER pairing, powered/open state | Door adapter EXISTS; geometry + full interaction still needs acceptance |
| P18 | Fence gate | `FenceGateBlock` FACING, IN_WALL, OPEN, nearby walls, player direction when opening | OPEN, separate from fences |
| P19 | Bed pair | `BedBlock` FOOT/HEAD + local tangent FACING + neighbor ownership | Bed adapter EXISTS; pairing/user behavior needs acceptance |
| P20 | Double plants and vertical chains | `DoublePlantBlock` UPPER/LOWER; TallFlower, tall grass, pitcher, dripleaf | INCOMPLETE; distinct from simple BushBlock support |
| P21 | Chest double pairing | `ChestBlock` `ChestType.LEFT/RIGHT/SINGLE`, FACING, placement sneaking, pair detection, `DoubleBlockCombiner` | OPEN; EnderChest placement adapter does NOT fix double chests |
| P22 | Canonical support local DOWN | `BushBlock`, candle/cake, torch, pressure plate, diode/rail, crops, signs | Partial family Mixins EXIST with successful CI; gameplay cross-family OPEN |
| P23 | Ceiling/side/sturdy support | SporeBlossom, Lantern, WallSign, Cocoa, coral/amethyst, Multiface, vine, hanging signs | Partial spore/ladder/walltorch/face-attached EXISTS; others OPEN |
| P24 | Local support invalidation | `canSurvive` vs `updateShape` and physical neighborPos; waterlogged rescheduling | Several adapters EXIST; all alternate subclass overrides / no-false-break regressions OPEN |
| P25 | Tangent four-way graph | FenceBlock, IronBars, TripWire, WallBlock, fence gates, scaffolding | Fence adapter EXISTS; graph-wide completeness OPEN |
| P26 | Six-face attachment graph | MultifaceBlock, GlowLichen, SculkVein, VineBlock state/replacement; face removal | OPEN; Vine has a distinct growth graph |
| P27 | Walls | WallBlock tall/low sides, UP post and above collision/down-facing shape | OPEN; cannot inherit Fence solution |
| P28 | Rails and slopes | `BaseRailBlock`, `RailState`, RailShape, rising local-UP, physical-X/Z identity bug | Dedicated 2F engine missing; gameplay OPEN |
| P29 | GrowingPlant directional graphs | GrowingPlantBlock, head/body, kelp/cave vines/weeping/twisting vines | OPEN; base Bush support is NOT sufficient |
| P30 | Growth columns and vegetation | cactus, sugar cane, bamboo, saplings/growTree, roots, attached stems/cocoa | OPEN beyond base support; worldgen structures cross Phase 8 |
| P31 | Scaffolding | ScaffoldingBlockItem context redirect, distance, BOTTOM, 4 tangent graph, local support, collapse | OPEN; cannot inherit rail/plant |
| P32 | Falling/support-trigger chains | FallingBlock, BrushableBlock, pointed dripstone, gravity-trigger/scheduled tick | Falling/dripstone adapters EXIST; brushable/scaffolding integration OPEN |
| P33 | Flat/attached plant variants | sea pickles, carpet/snow layers, moss, lily pad/water placement, frogspawn | OPEN; multiple non-Bush overrides |
| P34 | Direction-directed interaction | `useItemOn`, `useWithoutItem`, `BlockHitResult` face and local hit slot, `FlintAndSteelItem`, FireCharge, bonemeal | OPEN Phase-2 useOn; portal result Phase 9, fluids Phase 5 |
| P35 | Rotation/mirroring/persistence | `BlockState.rotate/mirror`, 16-way, FrontAndTop, state data components, structure transforms | OPEN policy: Phase-2 state transport + Phase-9 structure placement |
| P36 | Redstone-facing consumers | neighbor signal, observer watched face, repeater/comparator input/output, pistons | Phase 2 authored state only; functional graph Phase 7A OPEN |
| P37 | Fluid/waterlogging | WATERLOGGED state, fluid tick scheduling and liquid identity | preserve in Phase 2; actual hydrodynamics Phase 5 |
| P38 | Block entities and attached rotation metadata | BE NBT orientation, sign text/BER, skull/banner, chest pair storage, side config | State + lifecycle Phase 2; BE render Phase 3; machine capabilities Phase 7A/10 |
| P39 | Structure, clone, schematic, debug commands | rigid 3D placement, mirror, local frame policy, multi-cell copying, synthetic placement context | Phase-2 semantic portability gate, Phase-9 generators, Phase-10 external tools |
| P40 | Virtual/foreign worlds and mod blocks | fake Levels, Contraptions, placement-by-dispenser, WorldEdit, custom state properties | OPEN typed provenance policy; require safe physical/pass-through defaults |

### Mechanism-specific source findings that invalidate broad shortcuts

- `HorizontalDirectionalBlock` defines `HORIZONTAL_FACING` and a vanilla `rotate/mirror` implementation, but has **no universal getStateForPlacement**. `AbstractFurnaceBlock`, `ChestBlock`, `FenceGateBlock`, `DecoratedPotBlock`, etc author facing separately. A base-class placement Mixin would miss these siblings.
- `BlockStateProperties` contains `FACING`, `FACING_HOPPER`, `HORIZONTAL_FACING`, `AXIS`, `ROTATION_16`, `ORIENTATION` (`FrontAndTop`), `ATTACH_FACE`, rail shapes, wall-side, hinge, half, bed/chest pair, north/east/south/west flags. Same human word "orientation" maps to DIFFERENT state domains and algorithms.
- `StairBlock`, `TrapDoorBlock` and `SlabBlock` each compute local halves differently; stair `SHAPE` depends on adjacent oriented stair states.
- `StandingSignBlock` and `CeilingHangingSignBlock` quantize player yaw into 16 segments (not simply horizontal FACING); `WallHangingSignBlock` has lateral support and item-specific placement rules.
- `CrafterBlock` and `JigsawBlock` store an orthogonal pair `FrontAndTop`, with nontrivial local UP selection; treating them as one Direction loses roll orientation.
- `FenceGateBlock` reads nearby walls at world `north/east/south/west` to set IN_WALL; clicking to open can also read player's direction. This is BOTH placement AND interaction.
- `WallBlock` queries a world-ABOVE block to decide whether the UP post is raised, in addition to four tangent neighbor connections.
- `ChestBlock` uses nearby physical positions to infer LEFT/RIGHT chest type and `DoubleBlockCombiner` later; state-facing equality on two different Planet faces is not a valid physical comparison near a seam.
- `RailState.place` uses explicit world north/south/east/west and `getRail` checks above/below, while connection identity assumes matching physical X/Z. One rotated block state does not fix the rail graph.
- `GrowingPlantBlock` `growthDirection` is stored as a direction but explicitly calls `pos.relative(growthDirection)` for head/body/survival, which must be a traversed local step.
- `ScaffoldingBlockItem.updatePlacementContext` can move the effective placement point and `ScaffoldingBlock.getDistance` traverses below + four horizontal neighbors; two separate mechanisms.
- `MultifaceBlock` stores up to six Boolean face properties and `VineBlock` shares concepts but owns additional growth/attachment logic.
- `CocoaBlock` survives based on a `BlockState.FACING` directed neighboring jungle log, independent of BushBlock soil behavior.
- `SeaPickleBlock` overrides `canSurvive` to query support via `below()`, `mayPlaceOn` and face-shape; inherited base BushBlock support does NOT cover overridden methods.
- `ChiseledBookShelfBlock` derives insertion slot using a **physical hit face and local hit point**, in addition to horizontal placement and a block entity; rotating the model alone cannot make its hit detection correct.
- `PlaceOnWaterBlockItem.use` creates a hit result at physical `blockPos.above()`, bypassing normal clicked-face placement. `ArmorStandItem` and `EndCrystalItem` enforce world-axis space/support assumptions via bespoke `Item.useOn`. `FlintAndSteelItem`/`FireChargeItem` create fire based on real clicked face and `BaseFireBlock.canBePlacedAt`; portal geometry is a separate phase.
- `StructureTemplate.placeInWorld` uses `BlockState.mirror` then `rotate`, and block-entity NBT. Vanilla `Rotation` rotates around physical/global Y (leaves UP/DOWN unchanged). This is NOT a generic cube-frame remapping.
- `BlockItem.updateBlockStateFromTag` runs **after** base placement and may modify facing/half through DataComponents.BLOCK_STATE: a second state authoring path.
- Worldgen, command `setblock/fill/clone`, structure paste and optional-mod tools may bypass placement entirely, so a player's BlockPlaceContext patch alone can never be the final orientation contract.

## 5. Existing Planetary source coverage vs actual acceptance

**Confirmed prior gameplay:** particle base/gravity/debris/torches, manually placed cherry leaves; face-edge navigation, core probe families. These do not validate generic phase-2 orientation.

**Implemented + compiled/JUnit, but NOT all manually accepted:** `PlanetFrameApi` / `PlanetBlockPlacementFrame`; `PlanetBlockRuntime` support/traversal; `RotatedPillarBlockPlacementMixin`, `SlabBlockPlacementMixin`, `StandingAndWallBlockItemMixin`, `FaceAttachedHorizontalDirectionalBlockSupportMixin`, `HopperBlockPlacementMixin`, `BedBlockGravityMixin`, `DoorBlockGravityMixin`, `FenceBlockGravityMixin`, `BushBlockLocalSupportMixin`, `CandleBlockLocalSupportMixin`, `CakeFamilyLocalSupportMixin`, `SporeBlossomLocalSupportMixin`, `EndRodLocalPlacementMixin`, `EnderChestLocalPlacementMixin`, various torch/pressure-plate/ladder/snow/dripstone/falling-block adaptations. Presence in source is **not** proof of all subclass paths, edges, renderers, or a live NeoForge apply.

**Still known missing/insufficient:** generalized horizontal facing vocabulary adapter, 16-way yaw, front+top orientation, stair/trapdoor/fence gate, ordinary double chests, signs/hanging signs, full rail graph, six-face attachments, scaffold, growing plants, non-BlockItem placement routes, interaction hit slots, full rotate/mirror/state override, block-entity metadata frame, mechanism-wide exhaustive acceptance. Latest block family Mixins have green CI but no in-game acceptance after that patch.

**Important:** do NOT rollback earlier green mechanics just because an untested sibling is missing. Class-specific Mixins can remain as THIN algorithm-family/version adapters if justified by full source call graphs; the architectural violation is treating one class-specific patch as evidence that a *family* is complete.

## 6. Recommended shared architecture BEFORE wider code changes

### Reuse and extend stable Planet semantics, not global rewrites

- `PlanetPlacementInput`: immutable physical hit, physical target, optional entity-body frame, target canonical chart and local hit fraction/directions; preserve separate no-player/automation placement provenance. Backed by existing `PlanetFrameApi` and `PlanetBlockPlacementFrame`.
- `PlanetPropertyOrientation`: typed rules by property **domain**, not by shared property NAME: 6D facing, 4D tangent-only facing, 3D axis, 16-way yaw/quantized tangent rotation, two-direction FrontAndTop, AttachFace, HALF, hinge, rail shape, four/six-way connection flags. Validate domain for rotated face; preserve per-family meaning.
- `PlanetBlockSupport`: existing `PlanetBlockSupportRuntime` + `PlanetBlockNeighborQuery`, with explicit local support relation, target physical location, target-local support face, predicate type and physical neighbor invalidation. No universal reinterpretation of `isSolid`.
- `PlanetBlockConnectionGraph`: ordered seam-aware local neighbor iteration with support for tangent four-way, six-face, vertical chain, slope/rail and physically adjacent pair. Maintain *identity of nodes* at seams/corners.
- `PlanetMultiBlockTransaction`: placement validation for all physical cells, local-to-target state transfer, server set/update lifecycle, support and neighbor validation, disallow partial placements. Thin Door/Bed/DoublePlant/Chest callers.
- `PlanetBlockInteractionFrame`: physical useOn/hit face and local normal/tangent hit position; adapter families for flint/fire, hit slots, directional control, bonemeal/growth and item-specific behavior. Never rewrite the global raycast hit result.
- `PlanetStateTransform`: explicit target-local frame transport for structure/clone/manual programmatic placement, with decisions on mirror handedness and metadata; implementation also has a Phase-9/10 dependency.
- `PlanetOrientationCoverageRegistry` (research/CI metadata, NOT intrusive runtime registry): assigns all vanilla block item/model family paths to owning adapters and acceptance tests.

### Mod compatibility and portability

The public semantic computation must be in `dev.planetary.world`/`api`, with 1.21.1 method-specific adapters in reserved Mixin packages. All unknown foreign providers continue receiving PHYSICAL `Direction` unless they opt in to local semantics (existing capability policy). Do not silently treat virtual worlds/fake player or generated structures as the real physical Planet `Level`. Preserve NeoForge state components, block placement events, sided automation, model hooks and regular worldgen.

## 7. Phase-2 implementation waves (LARGE COHERENT MECHANISMS)

Each wave must finish source inventory, core algorithm, mandatory ASM/semantic tests, green GitHub Actions, six-face test-lab fixture coverage and meaningful player/server acceptance **before being marked PASS**. If the wave is enormous, implementation can be committed in subpackages while status remains PARTIAL.

- **Wave A — placement input and state-property authoring:** all `P01–P16` in one frozen architecture, divided by caller boundary: normal BlockItem, special/no-player Items, full facing, horizontal, AXIS, 16-way, front/top, HALF, attach. Existing EndRod/EnderChest patches reviewed for shared mechanisms. Requires all six faces and at least two arbitrary orientations on each.
- **Wave B — support/survival/update:** `P22–P24`, local DOWN, UP, face-directed, center/rigid/solid predicates, waterlogging and physical neighbor; includes subclass override sweep. Support deletion and unrelated-neighbor regression across rotated faces mandatory.
- **Wave C — multiblock/pairs and interactions:** `P17–P21`, `P34`: doors/double plants/beds/chests, flint+fire charge, hit location slot and player interactions. Pair-part ownership and physical inventory/BER references tested.
- **Wave D — connection/growth/rails/falling graphs:** `P25–P33` as tangent, six-face, vertical, slope and collapse graph families; deliberately not a single mutation in `Direction.Plane.HORIZONTAL`.
- **Wave E — alternate-authoring, compatibility and closure:** `P35–P40`, tags/components, structure transforms, command placement, automation/fake/virtual worlds, edges/corners. Leave genuinely worldgen-only and redstone-only functionality to owning phases but explicitly integration-test their Phase-2 authored states.

**Phase 3 integration:** special static baked model, `BlockEntityRenderer`, item model, lighting/culling and animation are separate owners, but a Phase-2 wave cannot claim that the user's facing/placement issue is completely resolved until the matching Phase-3 renderer acceptance is recorded. Ender Chest and enchanting book remain explicit blockers.

## 8. Automatic completeness gates: prevent the "forgotten block after JAR" outcome

Before general Phase-2 acceptance, create the following CI diagnostics (not yet implemented; this document is a RESEARCH gate):

1. **Version-locked block-family census:** enumerate `BuiltInRegistries.BLOCK` after bootstrap (all registered vanilla block IDs, not just source classes); for every distinct class/superclass/method owner collect `getStateForPlacement`, `canSurvive`, `updateShape`, `neighborChanged`, `setPlacedBy`, `onPlace`, `rotate/mirror`, `useItemOn/useWithoutItem`, `getShape`, `getFluidState`. Use ASM/mapped bytecode where access is protected. Classify all relevant orientation-bearing owners, subclass overrides and NeoForge-added hooks; unknown entries must require review instead of silently passing.
2. **State property domain census:** enumerate each registered BlockState's `FACING`, `HORIZONTAL_FACING`, `AXIS`, `ORIENTATION`, `ROTATION_16`, `ATTACH_FACE`, `HALF`, `CHEST_TYPE`, `BED_PART`, `RAIL_SHAPE`, wall/tangent/face flags, plus custom/unrecognized orientation-bearing properties. Enumerate all legal enum values and combinations, avoid assuming one block per class.
3. **Item creation census:** enumerate registered `BlockItem` and non-BlockItem `Item.useOn/use` overrides; correlate with actual block state creation and clicked normal. Include null-player `DirectionalPlaceContext`, dispensers/fake players, `DataComponents.BLOCK_STATE` after-placement overrides, replace/stack actions and modded placements.
4. **Source bytecode call-site contracts:** for every adapter verify the ACTUAL compiled NeoForge class method owner and call count, complete handler signature/staticness, `@At` form (array/single), inheritance override coverage and `defaultRequire=1`. Fail CI on unexpected drift.
5. **Pure semantic test matrix:** six frames, all six clicked normals, all body tangent look quadrants, 16 yaw positions, front/top legal orthogonal pairs, hit-fraction boundary 0 / 0.5 / 1, neighbor support faces, +Y exact baseline, edge/corner source/target frame mismatch and handed left/right transforms.
6. **Client/server in-world test fixtures:** expand the existing 20-station six-face debug lab to have one **direct canonical-state reference** (render/geometry) AND one **genuine player BlockItem/useOn** station for every mechanism family; support removal, alternate creation, interaction, replace, reload/save/chunk reload and version data pack (where applicable). CI does not simulate a full client/BER.
7. **Coverage report:** one authoritative table for every mechanism with `SOURCE_AUDITED / IMPLEMENTED / CI_GREEN / CLIENT_STARTUP / SIX_FACE_GAMEPLAY / CROSS_PHASE_INTEGRATION` columns; never infer one from another. Publishing a JAR with a "Phase-2 complete" label requires **zero untriaged relevant families, no unresolved P0 mechanism and recorded representative gameplay evidence**.
8. **CI vs runtime:** GitHub Actions Java21 compile+JUnit mandatory before local client test. Successful JUnit does not prove Mixin transformation, actual block placement, multiplayer server sync, renderer, or mod interoperability.

## 9. Out-of-scope yet cross-dependent orientation owners

| Outside main Phase 2 | Mechanism | Why still tracked |
|---|---|---|
| Phase 3 | baked quad/model UV, culling/AO/light, BlockEntityRenderer (Ender Chest, enchanting book, skull/sign/banner, item display), block breaking animations | correct state can render wrongly |
| Phase 4 | block-/entity-authored particles and initial spawn origin | smoke/drop/portal particle source may use old world axes |
| Phase 5 | fluid flow, buckets, waterlogging fluid semantics, liquid collision/render | directional useOn/fluid sources intersect Phase 2 |
| Phase 6 | pathfinding, door traversal and vertical support | mobs interact with placed orientation |
| Phase 7 | item entities, falling blocks, vehicles, body/hit direction, leash, projectiles | frame provenance after interaction |
| Phase 7A | redstone, piston push graph, comparators and observer sides, machine connection/capability direction | placed properties are inputs to larger graphs |
| Phase 7B | sky/precipitation/heightmap and interaction rays | physical world UP may intentionally differ from local UP |
| Phase 8/9 | worldgen/structure templates, Jigsaw connectors, nether portal plane | generated/pasted blocks don't use player placement |
| Phase 10 | mods/fake Levels, WorldEdit, Create/AE2/Mekanism, object state/naming variants | no hidden physical/local remapping in foreign contexts |

## 10. Required next engineering action

1. Treat this atlas and the 293-class/121-item **source-pattern** censuses as an **audit baseline**, not proof of full source-by-source semantic completion.
2. First implement the **automatic registry + item + override census** against the exact NeoForge 21.1.215 compiled classes, resolve remaining inheritance/semantics (240 block files have only initial pattern scan), and map all `2.0` Mixins to actual bytecode owners. This is the only scalable way to prevent a new vanilla mechanism from being silently omitted.
3. Freeze a first implementation wave only once its source owner and bypass list is complete; start with `BlockPlaceContext/BlockItem` and orientation-domain vocabulary, NOT new per-block patches.
4. Continue through waves A–E. Re-check existing accepted behaviors but do not demand full repetitive gameplay retests after each small fix.
5. **Do not mark Phase 2, or other orientation-dependent phases, DONE/PASS until the coverage gate is satisfied.**

**Research integrity:** This atlas identifies all major orientation-bearing engine categories and their owning class families known from the 1.21.1 audit. It does NOT claim every registered block and every NeoForge subclass override has been individually inspected yet; the 293-class census and its pending classification make that limitation measurable.
