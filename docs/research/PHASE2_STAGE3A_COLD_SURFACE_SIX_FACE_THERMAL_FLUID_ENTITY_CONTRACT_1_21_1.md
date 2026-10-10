# Stage 3A-12.3 — six-face cold surfaces: snow support, powder collision, ice phase changes and physical weather

**2026-10-10 | Planetary branch `2.0` | Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21 | proposed acceptance, all 32 tests NOT RUN.** Full concrete owner/ITEM and alternate writer audit [Stage 12.1–12.2](PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_BLOCK_ITEM_ALTERNATE_AUTHOR_AUDIT_1_21_1.md). Nothing here proves NeoForge patched ASM was inspected, a Planet adapter was applied, or client/server gameplay works. Coordinate-domain and provenance contract, not global automatic rotation.

## 1. Canonical physical BlockPos, local support, temporary seam traversal and feature/weather coordinates

Actual current Planetary `PlanetFace`, `PlanetBlockStateFrame`, `PlanetBlockFrameContext`, `PlanetBlockNeighborQuery`, `PlanetBlockRuntime` must be resolved at their intended stable owners. A physical Minecraft `BlockPos(x,y,z)` belongs to exactly **one** world cell and chunk `(x>>4,z>>4)` even at an edge or triple corner; `PlanetBlockStateFrame.resolve` deterministically yields one canonical face from that real cell, not the entrance path. `PlanetBlockFrameContext` holds path-dependent tangent orientation for deliberately chart-following traversals, **not duplicate face-local cells**. `Level.setBlock`, `LevelAccessor.getBlockState`, actual FluidState and entity world Vec3 always use physical world coordinates. No global rewrite of all `BlockPos.above/below`, `Direction.UP`, `Heightmap.getHeight` or engine entity coordinate APIs.

| Canonical face | local UP as world physical Direction | local DOWN as world physical Direction |
|---|---|---|
| POS_Y | +Y | −Y |
| NEG_Y | −Y | +Y |
| POS_X | +X | −X |
| NEG_X | −X | +X |
| POS_Z | +Z | −Z |
| NEG_Z | −Z | +Z |

At POS_X, snow support below its own local floor means physical **−X**, not vanilla world −Y; a snow layer's actual **world-Y AABB** may need collision/render orientation independent of the BlockState that stores only LAYERS, no FACING. A physically adjacent real neighbor at edge can have different target canonical frame; check physical support **face of target toward source**, not a raw source-local enum reused in another frame. At a 3-face corner one physical block can be approached by multiple entry charts but stays one state, light lookup, fluid and tick queue.

**Vanilla no-Planet worlds** must remain bit-for-bit behavior parity as relevant: non-Planet Overworld/Nether/End, creative or fake/WorldGenLevel, mod-integrated direct Level writers. All future adapters explicitly query whether Planet gravity/chart is active.

## 2. Source-owner-specific contracts (do not collapse into one “cold block” gravity patch)

### Snow layer stacking is source BlockItem contact + source/target support + distinct shapes

[`SnowLayerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SnowLayerBlock.java#L56-L180) `LAYERS=1..8`, `canBeReplaced` checks item match, max8 and (when `replacingClickedOnBlock`) vanilla **clickedFace==Direction.UP**. A source-local clicked-face policy can change placement semantics on lateral and lower Planet surfaces **only after** physical ray target provenance and chosen canonical BlockState frame are both known. BlockItem `getStateForPlacement` adds a layer to the **SAME physical BlockPos** when already snow. Late `DataComponents.BLOCK_STATE` can rewrite LAYERS after initial placement; generic `/setblock`/`/fill` bypasses normal clicked-face and item-stack rules.

`canSurvive` uses physical `pos.below` and precedence `SNOW_LAYER_CANNOT_SURVIVE_ON` ⇒ false, `SNOW_LAYER_CAN_SURVIVE_ON` ⇒ true, else actual below collision shape full on world `UP` **OR** below snow LAYERS8. Proposed local floor query must preserve this exact tag precedence and surface contact face; don't assume any snow below supports snow regardless of layer count. `updateShape` returns AIR if source `canSurvive` false; `randomTick` checks `LightLayer.BLOCK >11`, not biomes/snowfall. [`getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SnowLayerBlock.java#L75-L96) indexes LAYERS, `getCollisionShape` indexes LAYERS-1, `getBlockSupportShape` indexes LAYERS, `getVisualShape` indexes LAYERS. Phase 3 must map all physical shapes and culling, not just BlockState/texture; pathfindable LAND only LAYERS<5. Phase 8/9 natural generation separately sources snow through heightmap.

### PowderSnow collision, falling/boots, physics and solid-bucket are separate entity/Item paths

[`PowderSnowBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PowderSnowBlock.java#L57-L188): no FACING, LAYERS, WATERLOGGED or BE. `getCollisionShape` depends on `EntityCollisionContext`, `fallDistance>2.5`, `FallingBlockEntity`, special walkable entity tag/leather boots and isAbove/notDescending, otherwise empty; `entityInside` physically passes `Vec3(0.9,1.5,0.9)` to `makeStuckInBlock`, sets powder snow freezing status, physical Y+1 particle and world XZ motion gate; server-side burning entity can destroy powder block according to mob-grief/player+permissions. Phase7/Phase3 must transform **semantic vertical** collision/particle/movement, not blindly rotate entity world coordinates or alter stored BlockPos. `fallOn` uses distances 4/7 for small/large fall sound; gameplay damage is another entity boundary.

[`SolidBucketItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/SolidBucketItem.java#L16-L71) is the **only original registered powder_snow creator** via ITEM join, but has two distinct writes:
- `useOn` → inherited `BlockItem.useOn` / `place` physical placement and **successful player held item replaced with empty bucket**.
- `DispensibleContainerItem.emptyContents` → **direct** physical `setBlock(target, POWDER_SNOW,3)` only if in bounds and AIR, plus `GameEvent.FLUID_PLACE`, independent of normal player placement callbacks.
Opposite [`BucketItem.use`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BucketItem.java#L42-L89) with EMPTY bucket checks `BucketPickup` and calls `PowderSnowBlock.pickupBlock` which directly writes AIR and returns powder_snow_bucket. No water FluidState or WATERLOGGED arises from storing powder snow in a bucket. Phase5 fluid BucketItem APIs and Phase7 collision are independent.

### IceBlock and FrostedIceBlock: thermal tick and world-water support vs six physical graph neighbors

[`IceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IceBlock.java#L38-L79) has different state authors:
1. `randomTick`, enabled for **Blocks.ICE** by registration, melts when `getBrightness(LightLayer.BLOCK,pos)>11 - state.getLightBlock(...)`. `melt` if dimension ultraWarm removes source; otherwise writes physical **Blocks.WATER** and explicitly neighborChanged. No local-up support needed for the thermal threshold itself.
2. `playerDestroy` when no `PREVENTS_ICE_MELTING` enchantment and dimension not ultraWarm checks **physical `pos.below()`** `blocksMotion() || liquid()` to decide whether breaking source creates WATER; in ultraWarm removes. Propose translating **only this support test** into canonical local-DOWN, separately from `melt`. Preserve original enchantment and air/water outcomes.
3. `SnowAndFreezeFeature` and other explicit writer sources can create ICE without any BlockItem; source `Biome.shouldFreeze` uses physical world-Y temperature/light/build limits.

[`FrostedIceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FrostedIceBlock.java#L19-L129) inherits IceBlock `randomTick` **declaring owner**, but [`Blocks.FROSTED_ICE` registration](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/Blocks.java#L4821-L4833) **has no `randomTicks()` enable flag**. Own `onPlace` schedules 60–120 initial ticks, own `tick` ages AGE0..3 based on actual brightness and neighbor count, then either ages neighbors of the same frosted ice subclass or schedules further 20–40 tick checks. Own `neighborChanged` condition uses immediate **six physical Direction.values() neighbors**, and when changed neighbor block is frosted ice and fewer than two such neighbors remain triggers melt. Do **NOT** treat six neighbor counts as **six face-local dirs at each source frame** and thus potentially teleport cross-shell; the baseline 6-cardinal 3D physical graph is already consistent and should stay unless an intentional algorithmic policy with regression proof changes it. On six shell faces geometric neighbor samples may lie in face-translated paths; changing this should be a separately reviewed gameplay design, NOT global coordinate primitive rotation.

Frosted Ice does not have normal registered Item placed_block. Generic [`ReplaceDisk enchantment effect`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/enchantment/effects/ReplaceDisk.java#L43-L59) is a BlockState creation **capability** with physical XZ disk; this source audit did not independently locate the shipped enchantment datapack or confirm which configured effect writes FROSTED_ICE. Worldgen [`SnowAndFreezeFeature`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/SnowAndFreezeFeature.java#L21-L58) produces ordinary ICE/SNOW in physical 16×16 XZ heightmap columns, not frosted-ice by its own code. Exact whether FrostWalker effect is configured to ReplaceDisk(FROSTED_ICE) **requires datapack-config or compiled datapack evidence**. No invented creation claim.

## 3. Future deterministic acceptance scenarios, ALL 32 NOT RUN

Every test record must include exact Git HEAD/mod & NeoForge version, vanilla original declaring owner and potential patched ASM site, actual Mixin applied? yes/no (do not assume), Level/dimension + Planet field on/off, random seed/time, real `BlockPos` and chunk, physical hit/target, source and target canonical PlanetFace, path traversal chart if any, relevant physical support face and property/BlockState and FluidState, item author/late properties, block and scheduled ticks, light values, entity fall/boots/status, before/after shapes/particles/age/evaporation and exact expected vs observed. Controls: vanilla non-Planet, Planet POS_Y, other five face interiors, 12 face-pair edges and 8 physical triple corners, real chunk XZ seams, fluid, save/reload. Cases are future executable specs, **NONE HAVE BEEN EXECUTED**.

| ID | Deterministic fixture / action | Exact future expected verification |
|---|---|---|
| CS12-01 | No-Planet flat world ordinary `minecraft:snow` BlockItem with empty target | LAYERS1, physical clicked hit and expected BlockState |
| CS12-02 | Vanilla click snow 1→2→3→…→8 with held same snow item | Same **one** real BlockPos; canBeReplaced clickedFace UP gate respected |
| CS12-03 | Snow stack from side/DOWN physical hit and secondary-use; exactly 8 layers | Preserve original disallowed/allowed branches, no duplicate virtual cell |
| CS12-04 | Snow ItemStack with BLOCK_STATE LAYERS7 on normal placement | Late legal component state mutation, independently from initial stack selection |
| CS12-05 | Snow LAYERS1 support below CAN tag, below CANNOT tag, and below neither | CAN/CANNOT tag order preserved, ordinary collision-face fallback |
| CS12-06 | Snow on full-collision UP face, partial face, and snow LAYERS8 substrate | Actual support shape toward source, only legal support; updateShape AIR when invalid |
| CS12-07 | Snow per LAYERS outline/collision/visual/support shape and LAND pathfinding | Shape index LAYERS vs collision index LAYERS-1, LAND allowed LAYERS<5 |
| CS12-08 | Snow BLOCK light brightness 11 vs 12 with controlled randomTick | Only >11 drops/removes, not erroneously gated on biome temp in this path |
| CS12-09 | Six canonical faces, snow on intended outward local floor | Source local-down support translation and physical voxel plane; POS_Y parity |
| CS12-10 | Twelve cube edges, snow item stacking from either entry chart | One real target cell, canonical state stable, support side target resolved once |
| CS12-11 | Eight triple corners, snow LAYERS8 and nearby support | No chart aliases/collision duplication, no false self-support loop |
| CS12-12 | No-Planet SolidBucketItem `useOn` successful player placement | Physical POWDER_SNOW, empty bucket hand result, no fake water FluidState |
| CS12-13 | SolidBucketItem dispenser `emptyContents` on AIR vs occupied block | Direct actual setBlock only in bounds and AIR; FLUID_PLACE event despite solid |
| CS12-14 | Empty bucket `BucketItem.use` targeting PowderSnowBucketPickup | Original POWDER_SNOW source -> AIR, returns powder_snow_bucket, no actual liquid |
| CS12-15 | Powder snow entity collision no entity context, FallingBlockEntity and fall distance>2.5 | Original dynamic collision branch, no generic full solid cube |
| CS12-16 | Powder snow Leather Boots vs other boots, walkable mob tag, descending | World collision support vs pass-through, source physical pose and target frame |
| CS12-17 | Burning Player/nonplayer entity inside powder, mobGriefing on/off | Correct setIsInPowderSnow/fire cancellation, optional destroyBlock, no double drop |
| CS12-18 | Powder snow client moving entity, snowflake emitted | Physical Y+1 and world X/Z source offset recognized, intended local-up future visual adaptation Phase3 |
| CS12-19 | Powder entity fall 3.9, 4.0, 7.0 and stuck motion | Original source sound thresholds/makeStuckInBlock vertical Vec3, future Phase7 local semantic vertical |
| CS12-20 | Six face interiors with physical powder snow and test entities/boots | Local surfaces coherent with actual world collision, no counterfeit FACING/WATERLOGGED |
| CS12-21 | Ice world with BLOCK light around threshold; real randomTick | Only ICE with configured randomTicks gets source IceBlock.randomTick; WATER replacement and neighborChanged |
| CS12-22 | ICE dimension ultraWarm vs non-ultraWarm with forced melt | Physical AIR vs WATER output, actual fluid propagation Phase5 |
| CS12-23 | Player destroys ICE with/without PREVENTS_ICE_MELTING and block below air/full/liquid | Correct enchantment and **harvest-only** support-conditioned water outcome |
| CS12-24 | ICE break on ±X/±Z/NEG_Y face with real inward support | Source-local DOWN physical neighbor iff intentional adapter, no unrelated melt perturbation |
| CS12-25 | Place FrostedIceBlock AGE0 via command, schedule initial 60..120 tick | Subclass own onPlace owner, no false randomTick-only progression |
| CS12-26 | Frosted ICE age 0,1,2,3 under controlled brightness and adjacent 0..6 same blocks | Source stage advances/melts with correct 6 **physical** adjacent neighbors and threshold |
| CS12-27 | FrostedIceBlock neighborChanged when nearby frosted neighbor removed, 1 vs 2 adjacency | Immediate melting only when original changed block type and fewer-than-two rule holds |
| CS12-28 | Full Frosted Ice melt + neighboring ages and resulting scheduled 20..40 ticks | Actual physical 6-neighbor traversal, one distinct event per cell, no terrain chart fan-out |
| CS12-29 | Six Planet interiors and all 12 edges/8 corners AGE3 clusters | Canonical physical identities, no duplicate age due to multiple entry charts, deterministic neighbor graph |
| CS12-30 | SnowAndFreezeFeature 16×16 world XZ with fixed biome/heightmap/temp/light | Direct source writer ICE/SNOW, correct `shouldFreeze(...false)` vs `shouldSnow` decisions; future continuous feature-space policy separate |
| CS12-31 | Synthetic template/`/setblock`/`/fill` and configurable ReplaceDisk owner path for frosted ice | Commands/palettes can author legal AGE; don't claim shipped configured FrostWalker data before proof |
| CS12-32 | Real NeoForge server+client tests after future patch, normal no-Planet + 6+12+8 + fluid/entity/chunk/restart | Mixin applied bytecode/patch and gameplay results evaluated per phase, no source-only PASS and no off-Planet regression |

**All CS12-01 through CS12-32 are NOT RUN**; fixture “expected” denotes future checks, not measured Minecraft results.

## 4. Cross-phase handoff and remaining research questions

- Phase **2**: SnowLayerBlock canBeReplaced clicked-face and target canonical, support tag precedence and actual support collision face; special SolidBucketItem physical target and conditional Item author; existing source physical neighbor graph not globally rotated.
- Phase **3**: Snow shape/collision differential LAYERS/ LAYERS-1, culling/local planes; PowderSnow particle/world-Y and FrostedIce surface; never claim pathfinding or render acceptance from BLOCK properties.
- Phase **5**: PowderSnow != water despite bucket, BucketPickup/FLUID_PLACE event, Ice/WATER creation, snow/ice precipitation and actual side/down surface fluid behavior.
- Phase **7**: Dynamic powder collision/fall/boots/stuck motion/freezing; actual entities, physical Vec3 and movement/local gravity interactions.
- Phase **8/9**: Biome temp/light, precipitation and `SnowAndFreezeFeature` world XZ and Heightmap.MOTION_BLOCKING, possible ReplaceDisk freeze enchantment config data verification, structure/command/synthetic creation; six face natural/weather continuous chart not proven by source.
- All **241** original runtime-patched NeoForge ASM, Planet adapter and gameplay gates remain REVIEW_PENDING; Stage 12.1/12.2 promotes only **4** original source+compiled declaring owners, **82/241 classes, 203/1060 IDs** reviewed and **159/241 /857/1060** pending. Stage 12.4 next validates entire 241/82 original census and opens new concrete registered family card. **Source report and this acceptance contract do NOT change Java or run tests.**
