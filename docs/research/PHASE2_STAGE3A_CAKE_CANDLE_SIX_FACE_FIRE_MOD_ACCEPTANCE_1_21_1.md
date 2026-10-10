# Stage 3A-19.3 — 45 future cake/candle/candle-cake source, six-face, geometry and mod acceptance fixtures

**2026-10-11 · CK19-01…CK19-45 ALL NOT RUN.** Source analysis never establishes applied mixin, NeoForge21.1.215 bytecode, actual Planet runtime, or a successful 1.21.1 mod game load. [Full source / mod audit](PHASE2_STAGE3A_CAKE_CANDLE_ORIGINAL_OWNER_ITEM_FIRE_MOD_1_21_1.md) · [independent 103 class ZIP reconciliation](PHASE2_STAGE3A_CAKE_CANDLE_ORIGINAL_103_CLASS_RECONCILIATION_1_21_1.md). Preserve historical reports of failed side/bottom cake/candle placement; no fix claimed.

## Evidence to log when fixtures are eventually run

Original & transformed NeoForge signatures/sha, Planet git revision and applied Mixin, loaded-mod version and Minecraft build; actual target/support REAL `BlockPos`, incoming physical hit and item, local frame `PlanetFace` and chart, concrete physical/canonical tangent basis, legal original BlockState properties and compound BE data, incoming neighboring BlockState and direct ITEM/event callbacks including cancellations, physical fluid, particle client sample, comparator and inventory delta, real collision face and obtained item stats, no-Planet baseline. Per test independent **ASM-applied**, **Planet adapter**, **server**, **client rendering**, **modded runtime**, **performance** verdict; no invented green test state.

## ALL 45 proposed tests — NOT RUN

| ID | Future action | Expected evidence |
| --- | --- | --- |
| CK19-01 | Original ordinary cake BlockItem normal non-Planet placement | BITES0, real support cell beneath, Block.getStateForPlacement, no facing property |
| CK19-02 | Original candle every one of 17 variants normal world | CANDLES1, LIT=false, true target physical water decides WATERLOGGED |
| CK19-03 | Cake below non-solid block, candle below center-support-only block | Distinct Cake.isSolid below versus Candle.canSupportCenter(UP) preserved |
| CK19-04 | Cake support removed after placement | CakeBlock.updateShape on actual DOWN returns AIR, no ghost cake |
| CK19-05 | Candle support removed after placement | Capture actual BlockBehaviour.updateShape and survival outcome; no invented identical Cake behavior |
| CK19-06 | Candle 1→2→3→4 stack same ordinary item | Source canBeReplaced, cycling CANDLES, no overflow beyond 4 |
| CK19-07 | Candle stacked while secondary-use/sneaking | No automatic same-cell stacking, original BlockPlaceContext semantics |
| CK19-08 | Candle stacking different dye of vanilla candle | Vanilla item equality enforced; no cross-color merging without mod callback |
| CK19-09 | Place unlit candle in physical water | WATERLOGGED true from actual FluidState, cannot light |
| CK19-10 | Place water into lit candle, then nearby fluid update | SimpleWaterloggedBlock.placeLiquid extinguishes and schedules WATER tick |
| CK19-11 | Waterlogged candle water tick on neighbors | Fluid schedule and actual FluidState unchanged; no imaginary cauldron-type fluid |
| CK19-12 | All candles custom ITEM BLOCK_STATE components | Only legal CANDLES1..4/LIT/WATERLOGGED, no FACING |
| CK19-13 | All 7 cake bite states plus useWithoutItem | Gates hunger, increments precisely; final bite removes block |
| CK19-14 | Cake eaten at no hunger requirement | canEat(false) yields PASS; no consumed slice or invalid new state |
| CK19-15 | Comparator on cake BITES0..6 | Output formula 14,12,10,8,6,4,2 |
| CK19-16 | Candle item on pristine cake BITES0 | CakeBlock.useItemOn direct replacement to mapped CandleCakeBlock, one candle consumed |
| CK19-17 | Candle item on partially eaten cake BITES1..6 | No CandleCake conversion; vanilla interaction fallback respected |
| CK19-18 | All 17 candle colors on full cake | byCandle maps correct 17 distinct composite BLOCK IDs; ZERO composite ITEM IDs |
| CK19-19 | Click lit candle tip on CandleCake empty hand | Only candleHit top-semantic half extinguishes |
| CK19-20 | Click cake part of CandleCake empty hand | CakeBlock.eat invoked with full-cake initial state, drop candle and make bitten cake |
| CK19-21 | Flint-and-steel ignition on unlit candle/candleCake | SKIP_DEFAULT preserved, item useOn writes lit and original item effects |
| CK19-22 | Fire charge ignition on unlit candle/candleCake | Item useOn real legal gate, no duplicate events/stack consumption |
| CK19-23 | Flame projectile on eligible dry candle and candleCake | AbstractCandleBlock.onProjectileHit sets lit only when projectile on fire |
| CK19-24 | Explosion on lit candle and candleCake | AbstractCandleBlock.onExplosionHit trigger-blocks gate, extinguish and original explosion behavior |
| CK19-25 | Physical waterlogged candle vs candleCake not waterlogged | Candle.canLight rejects WATERLOGGED, CandleCake has no WATERLOGGED property |
| CK19-26 | Smoke and small-flame client particle at all 1..4 candle counts | All per-count offsets and deterministic active original geometry, no fake server effect |
| CK19-27 | CandleCake client particle and flame/voxelshape | Single flame y1.0 canonical local-up; cake candle composite model transform consistent |
| CK19-28 | Cake partially eaten shape on six-face local support | All 7 local shapes remove intended tangent-side slice, physical collision matches render |
| CK19-29 | Original normal non-Planet world vs Planet disabled | No transformed hit, state, block coords, inventory or comparator differences |
| CK19-30 | Player placement on each six Planet surface faces | Original real hit and support positions, valid state without illegal face property |
| CK19-31 | Cake/candle/candleCake under floor +Y face | Local structural DOWN, local apparent vertical/shape and top-face candle placement |
| CK19-32 | CandleCake hit top vs side on ±X and ±Z faces | Only local tip region extinguishes; world-y >0.5 test cannot leak into cake eating |
| CK19-33 | All 12 face-pair cube edges, both crossing directions | Unique physical clicked/support cell, expected canonical chart, no double placement |
| CK19-34 | All 8 triple-face corners, three approach charts | No duplicated candleCake, stable surface normal and tangent choice |
| CK19-35 | Physical chunk XZ boundary, save/reload of 1..4 candle stacks | Physical block and BlockState retained, no shifted shapes and no invalid candle count |
| CK19-36 | Light/candleCake on true physical water and lava neighbors | Only candle waterlogging from true WATER, no false level-fluid conversion |
| CK19-37 | Actual mod source MixedCandleBlock unloaded due to version constraint | Record NeoForge21.1.235 minimum; no fake compatibility PASS on 21.1.215 |
| CK19-38 | Future Mixed Candles runtime on compatible NeoForge + Planet no-Planet baseline | Inherited AbstractCandleBlock callbacks AND independently overridden support/shape/tool preserve mod contract |
| CK19-39 | Mixed Candles ITEM_AFTER_BLOCK event merges multiple dye colors | Vanilla CandleBlock placing bypass proven at actual mod event, BE colors preserved |
| CK19-40 | Mixed Candles sneak placement and vanilla stack conversion | Secondary-use prevents merge, direct setBlock and BE consistency checked |
| CK19-41 | Mixed Candles custom tool lighting and projectile lighting | IBlockExtension.getToolModifiedState and AbstractCandleBlock inherited projectile independently checked |
| CK19-42 | Mixed Candles client BE renderer and source item restore | Physical/canonical offset and clone/drops keep distinct dye colors |
| CK19-43 | Commands /setblock /fill and palette insertion of lit composite | Direct LevelAccessor writer legal LIT only; cannot assume player item path |
| CK19-44 | Foreign Level/contraption/clipboard generated block and mod synthetic hit | No coordinate rewriting without typed provenance; physical default |
| CK19-45 | Future changed patched NeoForge ASM, actual dedicated server/client end-to-end | Exact applied transformed signature and observed 6face/modded outcomes independently logged |

## Explicit pending phase boundaries

- Phase2 structural local support (cake solid block vs candle center support) and placement/stacking/item hit, including `CakeBlock.useItemOn` direct conversion, six-face and edge/corner unique BlockPos.
- Phase3 rotated cake bite geometry, 1..4 candle positions, candleCake composite geometry, projected hit-region and flame/smoke particle.
- Phase5 true physical WATERLOGGED source and fluid tick; no invented CandleCake water property.
- Phase6 cake comparator and candleCake full-cake signal.
- Phase7 player hunger/food callbacks, projectile/explosion and candle sound/events.
- Phase8/9 command/palette/feature direct writers and real/foreign Level.
- Phase10 inherited + copied modded owners with event ordering, BE and compatibility minimum-version check, unknown mod default PHYSICAL.

**No Java, Mixin, unit tests, CI, dedicated server or client were executed.** The prior dripping/falling partial manual observations are unchanged. CK19 fixtures are design documents only, all 241×3 original runtime acceptance fields remain REVIEW_PENDING.
