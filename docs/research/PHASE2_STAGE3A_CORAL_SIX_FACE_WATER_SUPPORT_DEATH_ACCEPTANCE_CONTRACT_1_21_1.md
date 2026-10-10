# Stage 3A-13.3 — living/dead coral on six Planet faces: water, floor-vs-wall, scheduled death and seam contract

**2026-10-10 · Planetary 2.0 · Minecraft 1.21.1 · NeoForge 21.1.215. Research-only proposed future acceptance: all 32 CF13-01…CF13-32 fixtures NOT RUN.** Original compiled declaring-owner, item dual-creator and reef writer research: [Stage13.1/13.2](PHASE2_STAGE3A_CORAL_BLOCK_SOURCE_ITEM_ALTERNATE_WRITERS_1_21_1.md). Nothing here indicates applied NeoForge patch, Planetary Mixin, client/server compile or acceptance success.

## 1. Three separate axes: physical six-neighbor water, source canonical local UP and target canonical FACING

All future adapters must start from existing `PlanetBlockStateFrame` canonical physical block storage and `PlanetBlockFrameContext` temporary transported chart for cross-seam traversal. A true Minecraft `BlockPos` belongs to one block and physical XZ chunk at edges and corners; never create six local copies or globally rewrite `Direction.DOWN`, `BlockPos.relative`, `FluidState` or `Level.setBlock`. Existing Planet helper classes existing does not imply coral already uses them.

| Planet canonical face | local UP → physical | local DOWN → physical |
|---|---|---|
| POS_Y | +Y | −Y |
| NEG_Y | −Y | +Y |
| POS_X | +X | −X |
| NEG_X | −X | +X |
| POS_Z | +Z | −Z |
| NEG_Z | −Z | +Z |

- **Real ocean-water and reef scan:** [`CoralBlock.scanForWater`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CoralBlock.java#L60-L73) loops `Direction.values()` six PHYSICAL `pos.relative(direction)` FluidStates (not own cell), tests `FluidTags.WATER`, no state WATERLOGGED. [`BaseCoralPlantTypeBlock.scanForWater`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseCoralPlantTypeBlock.java#L44-L62) first checks own WATERLOGGED then same six physical neighbor FluidStates. **Do not rotate the six immediate physical offsets through seam traversal** — six XYZ axial positions already define actual neighborhood. A water-retaining block on any side face must actually have corresponding Minecraft FluidState; Phase 5 owns continuity and current water physics.
- **Floor support:** [`BaseCoralPlantTypeBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseCoralPlantTypeBlock.java#L91-L96) checks source world `pos.below()` and target `isFaceSturdy(...,Direction.UP)`. For Planet POS_X surface this source-local support projects toward physical −X, while at NEG_Y it projects toward physical +Y. The *target block's exposed physical face toward coral* is important; convert precisely at source/target boundary and don't double-rotate a target's collision/face-sturdy shape.
- **Wall support:** [`BaseCoralWallFanBlock.FACING/getStateForPlacement/canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseCoralWallFanBlock.java#L22-L118) stores **four legal** directions NORTH/SOUTH/EAST/WEST only; choose from player context's ordered nearest-looking directions, skips non-horizontal; physical support is at `pos.relative(FACING.getOpposite())` and supports side `FACING`. The source local frame can have four tangential semantic directions whose physical projection includes vanilla physical Y on side faces, **but source BlockState property itself cannot store UP/DOWN**. Future runtime integration must choose and prove an explicit representation strategy at the owner frame; **do not** assign illegal property values, sneak world UP into FACING or assume a horizontal rotating outer voxel shape alone fixes placement/survival.
- **Floor-vs-wall item:** [`StandingAndWallBlockItem.getPlacementState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/StandingAndWallBlockItem.java#L30-L54) tries nearest-looking ordered directions, excludes `attachmentDirection.opposite=UP`; DOWN candidate uses floor BLOCK, others use wall BLOCK, then checks candidate's `canSurvive` and unobstructed physical collision. This is a **dual creator**. For native Planet side/underside, “floor” means canonical-local outward surface, “wall” tangential local face, but target+source canonical and original clicked HitResult remain physical; mapping should be at this item algorithm and produce a real registered floor or wall state, not rewrite all item contexts globally. `registerBlocks` maps both original blocks to same registered Item.
- **Dead/dry transitions:** full `CoralBlock` has no WATERLOGGED, schedules death 60..99 ticks at placement/updateShape if no adjacent physical water. Live floor/wall `onPlace` and `updateShape` schedule 60..99 death when neither own WATERLOGGED nor six physically adjacent neighbor fluids WATER. Scheduled `tick` *rechecks* water; if still dry, writes the matching **dead** state; floor dead WATERLOGGED=false, wall dead WATERLOGGED=false and copies FACING. Dead `BaseCoralPlantBlock`/`BaseCoralFanBlock`/`BaseCoralWallFanBlock` have support/fluid mechanisms but no live→dead scheduler. Lost support can return AIR before later death; separate behavior.
- **Waterlogging:** BaseCoralPlantTypeBlock `getStateForPlacement` sets own WATERLOGGED=true only if the actual target WATER FluidState `getAmount()==8`, while `scanForWater` only checks its own boolean or nearby water **tags** (near water may have lower amount). Base `getFluidState` returns WATER source if waterlogged. `updateShape` schedules WATER fluid ticks; Phase 5 is responsible for actual fluid behavior in a curved-world shell. `BlockItem` may write legal WATERLOGGED/FACING properties after initial place via `DataComponents.BLOCK_STATE`. Neither fluid tick scheduling nor saved template direct setBlock is equivalent to a player facing action.
- **Feature writers:** [`CoralFeature.placeCoralBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/CoralFeature.java#L27-L89) directly authors CoralBlock, sometimes CoralPlantBlock and CoralWallFanBlock, with world `pos.above` and `Direction.Plane.HORIZONTAL`; [`CoralTreeFeature`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/CoralTreeFeature.java), [`CoralClawFeature`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/CoralClawFeature.java) and [`CoralMushroomFeature`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/CoralMushroomFeature.java) use physical world Y/XZ steps and reef volume tests. These need Phase 8/9 per-generator chart/biome and true water; rotating class `getStateForPlacement` cannot fix naturally generated coral. Do not claim any shipped structure .nbt or preconfigured worldgen succeeded without test.

## 2. Expected ownership of physical Minecraft / Planet state

| Operation | Algorithm owner | Intended Phase / risk |
|---|---|---|
| Target physical `BlockPos` and real voxel shape | Level / BlockState / BlockItem physical hit | Always remain genuine world identity; Phase 2 selection, Phase 3 shape and culling |
| Source local floor and target physical exposed support face | `BaseCoralPlantTypeBlock.canSurvive/updateShape` | Phase 2 (support/canonical face), target collision may be rotated Phase 3 |
| Source tangential wall FACING and nearest item candidates | `StandingAndWallBlockItem` plus `BaseCoralWallFanBlock` | Phase 2; only 4 original property values; physical Y projection issue unresolved until adapter and acceptance |
| Neighbor water and own WATERLOGGED | CoralBlock vs BaseCoralPlantTypeBlock | Phase 5 real physical water and liquid scheduler, not six chart neighbors |
| Dry/death 60..99 scheduled tick, original species preservation | CoralBlock/CoralPlantBlock/CoralFanBlock/CoralWallFanBlock | Phase 2 block lifecycle and Phase 7A ticks/events, distinct from randomTick |
| Reef feature and player/cmd/template/bonemeal authors | CoralFeature + 3 patterns, BlockInput and StructureTemplate, Items | Phase 8/9 worldgen, Phase 2 creator and Phase 5 underwater source |
| Client shapes/particles/audio | BaseCoralPlantBlock, BaseCoralFanBlock, BaseCoralWallFanBlock | Phase 3. Thin floor fan and wall fan cannot be assumed world-Y valid after local frame |

**No invented universal six-face FACING enum**: the legal original wall-fan property has exactly four values. The test should document adapter's chosen state/frame representation, compare physical axis+geometry and preserve vanilla parity.

## 3. Thirty-two future deterministic fixtures — ALL NOT RUN

Every fixture log: exact NeoForge/Planet SHA, applied Mixin yes/no (must be demonstrated), non-Planet or Planet field, source physical BlockPos and chunk, target physical BlockPos and supporting physical face, canonical source and target PlanetFace (and temporary chart on seam), BlockState registered ID and FACING/WATERLOGGED before+after item component, actual fluid amount/tag in own and six neighbors, item owner (registered floor/wall and context candidates) or WorldGenLevel feature, scheduled block + fluid tick time/count, palette and source real ID, sounds/events/entity/blocks, expected vs observed. Compare vanilla no-Planet first, 6 faces, 12 edges and 8 corners; after patch run real NeoForge client/server with save/reload and fluid.

| ID | Setup/action | Expected future verification |
|---|---|---|
| CF13-01 | Normal vanilla full live CoralBlock with WATER in one axial neighbor | Scan six real adjacent fluid cells; no WATERLOGGED property and no scheduled dry death |
| CF13-02 | Normal full live CoralBlock in all-air six neighbor cells | Placement/updateShape schedules death 60..99 and tick writes exact matching dead species |
| CF13-03 | CoralBlock dry when scheduled, water added before scheduled tick | Recheck prevents conversion; don't assume irreversible death |
| CF13-04 | CoralBlock originally wet, remove only last adjacent water and update shape | Schedule dry death exactly by original owner; physical six neighbors |
| CF13-05 | Live floor CoralPlantBlock in target full water amount8 | WATERLOGGED=true and FluidState WATER source, even without external water |
| CF13-06 | Live CoralPlantBlock in flowing water amount<8, other neighboring water present | WATERLOGGED=false on place; neighbor water scan still returns alive |
| CF13-07 | Floor CoralPlantBlock WATERLOGGED=true and six neighbors dry | Own water counts, no scheduled death unless written WATERLOGGED=false |
| CF13-08 | Live floor CoralPlantBlock dry isolated on valid sturdy floor | OnPlace schedules 60..99 then matching dead floor plant WATERLOGGED=false |
| CF13-09 | Unsupported floor CoralPlantBlock with water around | canSurvive checks support; DOWN update removes AIR independent of wet/death |
| CF13-10 | Live CoralFanBlock floor valid support, WATERLOGGED and dry variants | Distinct fan thin VoxelShape, fluid and matching dead fan after delay |
| CF13-11 | BaseCoralPlantBlock dead floor placement into full water | WATERLOGGED true but no live→dead die scheduler; same support rules |
| CF13-12 | BaseCoralFanBlock dead floor, remove sturdy support | Returns AIR via base updateShape, not new dead block |
| CF13-13 | Live wall fan ordinary vanilla click SOUTH/EAST/NORTH/WEST | Legal 4-value FACING only, correct physical rear support and shape |
| CF13-14 | Wall fan placement with click above/below in vanilla | Source nearest-looking candidate + horizontal filtering; no illegal FACING UP/DOWN |
| CF13-15 | Lose wall supporting block on actual FACING.opposite | Opposite-neighbor update removes AIR when unsupported; other neighbor update doesn't improperly remove |
| CF13-16 | Dry live wall fan after 60..99 tick | Converts to matching dead wall fan with **identical FACING**, WATERLOGGED=false |
| CF13-17 | BaseCoralWallFanBlock dead wall support and fluid tick | Same wall FACING/support, water behavior, no repeat death tick |
| CF13-18 | StandingAndWallBlockItem live floor placement on eligible DOWN candidate | Selects CoralFanBlock state, maps item, no double authoring |
| CF13-19 | Same ITEM wall placement from alternate nearest-looking context | Selects CoralWallFanBlock state with legal FACING and sturdy support |
| CF13-20 | StandingAndWallBlockItem dead floor vs wall placement | 5 dead floor+wall species pairs share registered Item; no direct wall-only item needed |
| CF13-21 | ItemStack BLOCK_STATE WATERLOGGED and FACING override after placement | Legal StateDefinition apply on resulting concrete floor/wall BlockState only, no invented properties |
| CF13-22 | Source-local floor support on six Planet face interiors | Canonical local DOWN mapped to physical actual neighboring sturdy face without duplicating cell |
| CF13-23 | Wall fans on six Planet face interiors with four local tangents each | Runtime representation of physical axis valid, **only four legal original FACING states**, shape/collision coherent |
| CF13-24 | Waterlogged live and dead floor/wall coral on POS_X,NEG_Y,NEG_Z | Actual FluidState not visual water, source own-water vs adjacent-water correctly distinguished |
| CF13-25 | Each of 12 cube face-pair edges, approach fan placement through both entry charts | One actual physical target, canonical frame identical, no duplicate scheduled death/fluid tick |
| CF13-26 | Each of 8 triple-face corners, all three chart entry paths | Same true BlockPos and registered BlockState, no imaginary 6-face FACING |
| CF13-27 | Two neighboring corals on different canonical faces sharing a water neighbor | Physical six axial FluidStates queried, not remote chart-translated water |
| CF13-28 | Real fluid removal/pour at chunk XZ seam and scheduled death pending | Both block tick and WATER tick load/save behavior stable, no misplaced coral variant |
| CF13-29 | CoralFeature reef placing blocks/floor coral and wall fan in actual worldgen | Direct non-ITEM writer and world XZ/UP paths traced, correct original FACING on wall fan, no bypass claim |
| CF13-30 | CoralTree/CoralClaw/CoralMushroom patterns on 6 planetary faces | Future feature-local chart and true water needed, biome/structure fluid validity; no universal BlockPos rewrite |
| CF13-31 | /setblock, /fill and StructureTemplate containing chosen live/dead wall fan FACING | Direct writer bypasses StandAndWall item, preserves legal state and optional water property; no fake shipped NBT |
| CF13-32 | Full NeoForge client/server after future implementation, vanilla no-Planet and 6+12+8 controls | Mixin patched method, source/target frame, visuals, physical water, tick, performance and gameplay individually verified; no source-only PASS |

**All CF13-01…CF13-32 are NOT RUN.** Test design is not an implemented feature. Do not mark acceptance until actual compiled transformed owner matches and a recorded gameplay observation proves it.

## 4. Remaining blockers and future acceptance

This Stage13.3 contract defines no new register or Java class: **89/241 reviewed original source+compiled declaring-owner classes, 238/1060 exact BLOCK IDs** after seven actual registered classes promoted; **152/241 /822/1060** source pending. All **241** each of `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, `gameplay_acceptance` still REVIEW_PENDING (723 outstanding fields). Full stage 13.4 must now independently reparse original untouched ZIP and compare 241 complete class/ID roster, 89 audited exact ID lists, five full-signature declaring owners and pending gates to current GitHub ledger, choose next original source-pending cohesive family and commit roadmap, all in this same user message.
