# Stage 3A-15.3 — six-face amethyst & cross-mod crystals source-target FACING and 32 future tests

**2026-10-11 · Planetary branch 2.0 · Minecraft 1.21.1 / NeoForge 21.1.215; 32 future tests ALL NOT RUN.** Research only, not patched ASM/in-game/adapter acceptance. See [original Amethyst/BuddingAmethyst method owner & GeodeFeature writer report](PHASE2_STAGE3A_AMETHYST_BUDDING_ORIGINAL_BLOCK_ITEM_GEODE_AUDIT_1_21_1.md) and [pinned AE2+BuddingCrystals source comparison](PHASE2_STAGE3A_MODDED_CRYSTAL_AE2_BUDDINGCRYSTALS_1_21_1.md).

## Coordinate and class-owner contract

- **Growth and geodes are physical 3D algorithms:** vanilla `BuddingAmethystBlock.randomTick` uses all 6 physical `Direction.values()`, 1/5 chance PER RANDOM TICK, then target `pos.relative(direction)`, requiring AIR or SOURCE WATER amount8 for initial bud, or previous bud stage with `FACING=direction` for upgrades. Actual `setBlockAndUpdate` target must stay the ONE real Minecraft physical BlockPos, even when source and target have different Planet canonical face. Rotating the six physical neighbor offsets into chart-local neighbors may erroneously spread growth beyond or duplicate actual physical target positions at an edge/corner.
- **Stored orientation and support are a different owner:** `AmethystClusterBlock` holds **6-direction FACING**, `WATERLOGGED`, has six direction-specific physical voxel shapes, places on clickedFace, checks actual support at `pos.relative(FACING.opposite)` and support block physical face `FACING`; `updateShape` returns AIR only on opposite support update when invalid and schedules true fluid tick if waterlogged. Source and support BlockState frames must each be resolved as canonical, not assume a movement chart transform equals storage. A strict rule of rotating only semantic local-up, not every `BlockPos.relative`, avoids corrupting actual physical 3D growth.
- **Planet six exterior faces:** POS_Y localUP=physical+Y; NEG_Y localUP=−Y; POS_X=+X; NEG_X=−X; POS_Z=+Z; NEG_Z=−Z. At six face interiors, 12 true face-pair edges and 8 triple corners one physical BlockPos and one real chunk exist, no six chart aliases. As needed `PlanetBlockFrameContext` traverses local chart tangents, while `PlanetBlockStateFrame` denotes physical canonical storage.
- **Creation paths differ:** normal BlockItem and valid late `DataComponents.BLOCK_STATE` legal only on clusters, randomTick direct source writes, GeodeFeature 3D `WorldGenLevel` and `innerPlacements` provider using actual physical offsets (and fluid `isSource`), commands/templates or other-mod direct setBlock. There are no invented shipped structure palettes.
- **AE2 1.21.1 pinned SHA `db17504a86128fdf3dae31f5fb7a112a646e0b93`:** own `BuddingCertusQuartzBlock` and `CertusQuartzClusterBlock` classes extend AEBaseBlock NOT vanilla BuddingAmethyst/AmethystCluster, but replicate six physical growth directions, vanilla static FACING/WATERLOGGED property references and support flow, and add source degradation. Adapter that targets ONLY vanilla class misses AE2 independent method owners.
- **BuddingCrystals 1.21.1 SHA `7f01672151a04e2a02fcde86e25d7ae37990db68`:** `BuddingCrystalBlock extends BuddingAmethystBlock` but overrides `randomTick`; `CrystalSet.Builder` registers real vanilla `AmethystClusterBlock` objects for its four buds (see registration source), so vanilla cluster adaptation MAY cover them if method applied but vanilla budding parent randomTick adaptation does NOT automatically cover override. `growthChance` is config data; `BCContent.SALT` sets 1, not 5. Declare compatibility only after confirming active mod resources and actual NeoForge 21.1.215 class transformations.
- Cross-phase Phase2 source-target support and item click; Phase3 six physical voxel shapes/particles/visuals; Phase5 true target WATER/FluidState; Phase8/9 GeodeFeature and spatial placement; Phase10 cross-mod loaded class adapters. No Java changes, no gaming tests, all modded acceptance PENDING. Modded registered IDs are NOT included in vanilla 241/1060 census.

## Required reproducible fixture observations

For each test log source version/target NeoForge build, mod versions+SHA, canonical physical source+target coordinates and PlanetFace/entry chart, actual six-neighbor selected Direction, 1/5 RNG and other chances, initial/after block and FACING/WATERLOGGED/FluidState, particle/shape/client render, actual world fluid tick, geode configuration and WorldGenLevel, Mixin apply+transformed method owner verification, world chunk/reload, single source vs target BlockPos write, item/loot author, vanilla no-Planet baseline and expected vs OBSERVED. **No test below has been run.**

| Fixture ID | Future setup | Required result |
| CR15-01 | Vanilla no-Planet AMETHYST_BLOCK normal placement, projectile hit | No FACING/AGE or random grow; projectile sounds at physical BlockHitResult pos |
| CR15-02 | Vanilla no-Planet BUDDING_AMETHYST ordinary placement | No own FACING property; randomTick source may grow at physical 6 neighbors |
| CR15-03 | Controlled randomTick with nextInt(5)!=0 | No bud source target write |
| CR15-04 | Controlled nextInt(5)==0, choose one of six source DIRECTIONS | Probe exactly one real physical axial neighbor, not local chart-transformed adjacency |
| CR15-05 | Each of six physical neighbors empty AIR | One small amethyst bud FACING=selected Direction, waterlogged false |
| CR15-06 | Each of six physical neighbors WATER full amount8 | Small bud created with WATERLOGGED true and source water replaced appropriately |
| CR15-07 | Flowing WATER amount<8 and other water-bearing target states | No initial small bud when target is not AIR/full WATER source |
| CR15-08 | Existing small bud same physical facing vs different facing | Upgrade to medium ONLY on matching FACING, preserve physical true target |
| CR15-09 | Medium and large bud same facing, fourth cluster stage | small→medium→large→cluster without duplicating block identity |
| CR15-10 | Bud wrong FACING despite correct stage | No growth into wrongly aligned existing bud |
| CR15-11 | Player BlockItem place cluster on all six physical clicked faces | Six legal FACING and actual target fluid-derived waterlogged state |
| CR15-12 | Cluster support block sturdy vs not sturdy for each of 6 FACING | canSurvive opposite-side physical neighbor face, no virtual aliases |
| CR15-13 | Neighbor update from support side vs unrelated side | Remove AIR only when support lost, schedule water tick when waterlogged |
| CR15-14 | All stages' shape, rotate/mirror, and fluidState | Exact six voxel AABBs/original FACING rotation plus real physical water |
| CR15-15 | Late BlockItem BLOCK_STATE component on cluster | Legal FACING/WATERLOGGED after placement, not on AMETHYST_BLOCK/BUDDING_AMETHYST |
| CR15-16 | All six Planetary face interiors, six growth directions per budding source | Actual physical 3D adjacency preserved; canonical source and target frames not conflated |
| CR15-17 | Every 12 cube face edges, approach budding cell from both charts | Exactly one real BlockPos per target, no duplicated growth cells |
| CR15-18 | All 8 cube triple corners, 3 chart entry paths | One canonical BlockState and stable FACING/per-block water |
| CR15-19 | Across real XZ chunk boundaries and reload | Random ticks/neighbor water schedules do not duplicate stages |
| CR15-20 | GeodeFeature with configured alternate/inner providers | Physical 3D geode volume, innerPlacements and 6 true Directions, no assumed shipped palette |
| CR15-21 | GeodeFeature growth-eligible target vs water source vs waterlogged state | Config-driven FACING and getFluidState().isSource waterlogged path distinct from runtime bud tick |
| CR15-22 | Commands /setblock /fill, synthetic structure template containing buds | Direct writer bypass BlockItem.place; retains legal blockstate |
| CR15-23 | AmethystCluster removal then projectile/sounds/drops | Projectile callback inherited AmethystBlock, Phase3/7 effects separate |
| CR15-24 | No-Planet vanilla server/client plus all Planet source faces after real patch | No renderer, voxel shape, ASM signature or biome parity regression |
| CR15-25 | AE2 Certus Quartz cluster six-face clicked placement and support | Independent CertusQuartzClusterBlock source owner, not vanilla AmethystClusterBlock Mixin |
| CR15-26 | AE2 4 bud stages six physical directions with SOURCE water | Own BuddingCertusQuartzBlock randomTick and physical FACING/WATERLOGGED |
| CR15-27 | AE2 flawed/chipped/damaged growth success and random decay | Source degradation 1/12 after successful bud growth, correct source parent cell |
| CR15-28 | AE2 flawless budding source vs degrading tier | Flawless does not degrade while existing siblings degrade when selected |
| CR15-29 | AE2 optional Growth Accelerator and QuartzFixtureBlock | Accelerator mechanism verified separately; QuartzFixture support six FACING and shape/water independent |
| CR15-30 | BuddingCrystals custom budding source override vs vanilla base Mixin | Inherited BuddingAmethystBlock but mod overrides randomTick, mod method dispatch validated |
| CR15-31 | BuddingCrystals actual registered AmethystClusterBlock 4 bud stages | Vanilla cluster runtime class hooks potentially shared; configured growthChance difference (e.g. salt) |
| CR15-32 | Both mods loaded together in NeoForge21.1.215 client/server with 6+12+8 | Inspect applied transformed bytecode/actual registered IDs/mod compatibility/axis math/fluids and acceptance |

## Boundaries and checkpoints

**All 32 CR15-01..CR15-32 future fixtures NOT RUN.** Separately, mod-specific [cross-mod source report](PHASE2_STAGE3A_MODDED_CRYSTAL_AE2_BUDDINGCRYSTALS_1_21_1.md) identifies 16 more detailed hypothetical modded probes, also NOT RUN; this 32-row table is the per-family higher-level acceptance suite with 8 mod-specific rows. Stage15 covers two newly source-reviewed vanilla original block IDs and previously reviewed four-registered-ID AmethystClusterBlock unchanged. Phase 2/3/5/8/10 integration remains unproven. All 241×3 vanilla original patched bytecode/Planet/gameplay flags REVIEW_PENDING. Next same-turn Stage15.4 full original 241/94 five-full signature declaring-owner ledger verification, next real pending family card and final GitHub commit.
