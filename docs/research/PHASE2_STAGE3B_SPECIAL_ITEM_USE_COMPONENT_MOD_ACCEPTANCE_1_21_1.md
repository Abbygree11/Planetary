# 2.3B-2 — special item/direct state authors, NeoForge hooks and mod acceptance

**2026-10-12, DESIGN ONLY. ALL 32 item-author fixture scenarios NOT RUN**. All six face interiors (POS_Y,NEG_Y,POS_X,NEG_X,POS_Z,NEG_Z); all **12** seam face pairs and **eight** triple corners where meaningful; plus non-Planet vanilla/foreign Level regression. Scope = user naturally uses real item/hand or actual dispenser/tool/synthetic action, not forced `Level.setBlock` fixture posing as `useOn`. Each test must assert real clicked/target BlockPos, hit normal, initial and final registered block IDs/legal properties, side effect/event, exact method dispatch, BE data, permission, loaded mod if claimed, client/server consistency, persistence and no unintended cross-world rewrites. Split cross-phase owners, do not mark partial tests PASS.

| Case | Action | Expected evidence | Status |
| --- | --- | --- | --- |
| S01 | Axe strip vanilla AXIS logs on +Y, -Y, 4 sides | original new block ID and supported AXIS preserved; event once | NOT RUN |
| S02 | Axe oxidation scrape and wax removal | NeoForge tool ability path and canonical properties preserved | NOT RUN |
| S03 | Modded waxable C&C blocks (compatible mod runtime prerequisite) | DataMap & exact mod class recognized; no hardcoded copper-only check | NOT RUN |
| S04 | Honeycomb wax block (all material/ages) | NeoForge WAXABLES mapping, withPropertiesOf, no axis corruption | NOT RUN |
| S05 | Honeycomb wax sign vs block | sign BE/text state preserved; not conflated with block transform | NOT RUN |
| S06 | Hoe clicked normal on 6 face interiors | till mod extension rule; local soil support semantics as intended | NOT RUN |
| S07 | Hoe rooted dirt extraction at 12 seams | drop uses correct physical face, growth/support not inverted | NOT RUN |
| S08 | Shovel flatten local open side on 6 faces | correct air exclusion without bypassing SHOVEL_FLATTEN | NOT RUN |
| S09 | Shovel douse nonvanilla mod block | SHOVEL_DOUSE dispatch, original event result | NOT RUN |
| S10 | Flint and steel clicked lit-block and adjacent fire | FIRESTARTER_LIGHT then BaseFireBlock; two paths distinct | NOT RUN |
| S11 | Fire charge clicked lit-block and physical neighbor | same owner tool dispatch but own consumption semantics | NOT RUN |
| S12 | Shears plant head and modded trim | SHEARS_TRIM route, blockstate change once | NOT RUN |
| S13 | Shears modded entity | IShearable action, no spurious block write | NOT RUN |
| S14 | Bonemeal crop under six local gravities | cancellable BonemealEvent plus BonemealableBlock callback | NOT RUN |
| S15 | Bonemeal underwater wall coral in each local face | physical target, legal FACING4, survival and fluid | NOT RUN |
| S16 | Bonemeal custom mod RiceBlock | event plus independently authored upper block, local support | NOT RUN |
| S17 | Lily pad PlaceOnWaterBlockItem.use (not useOn) | SOURCE_ONLY ray and synthetic hit, correct water surface | NOT RUN |
| S18 | Bucket empty / fill for water or lava | correct SOURCE_ONLY/NONE ray and BucketPickup per hit | NOT RUN |
| S19 | Bucket source in waterloggable block | clicked container vs adjacent real target selected correctly | NOT RUN |
| S20 | NeoForge modded FluidType bucket and vaporization | stack-aware emptyContents, canBlockContainFluid/FluidType callback | NOT RUN |
| S21 | SolidBucketItem super BlockItem placement | special place sound, empty bucket, final state components | NOT RUN |
| S22 | SolidBucketItem dispenser emptyContents | direct block-state write distinct from super.place | NOT RUN |
| S23 | MobBucketItem fluid + mob | checkExtraContent entity spawn once and correct fluid | NOT RUN |
| S24 | ItemNameBlockItem redstone/wheat seeds | actual registered item inherited BlockItem semantics with support | NOT RUN |
| S25 | GameMasterBlockItem no permission / with permission | no bypass of security gate, world-space target | NOT RUN |
| S26 | DebugStickItem cycling FACING/AXIS/ROTATION and mod props | legal domain from StateDefinition and explicit user intent | NOT RUN |
| S27 | EnderEyeItem frame insertion + portal activation | frame state, physical 3x3 portal, Phase9 ownership | NOT RUN |
| S28 | BrushItem onUseTick brushable block | physical hit direction, BE progression and particles separate | NOT RUN |
| S29 | ArmorStandItem, HangingEntityItem, ItemFrameItem, EndCrystalItem | entity placement, collision and phase ownership, not counted as blocks | NOT RUN |
| S30 | FD RiceItem failed place and fallback | useOn overridden path, physical clicked normal UI gate, null safety | NOT RUN |
| S31 | FD RopeItem + MushroomColonyItem custom overrides | post-rewrite target + mature colony age + legal state | NOT RUN |
| S32 | Vanilla non-Planet, foreign Level, mod fake player on 6 faces/12 edges/8 corners | no global coordinate mutation; actual loaded mod runtime required | NOT RUN |

**Runtime blockers:** current Planetary NeoForge **21.1.215**, source mod Farmer's Delight SHA dd8b9b0 requires **21.1.219+**, Caverns & Chasms SHA d5600c6 built for **21.1.222**. Crossmod tests require a version-compatible build and **actually loaded** mod before claiming compatibility; source-only study does not count. Exact NeoForge 21.1.215 **transformed bytecode** and Mixin invocation descriptors are **pending 2.3C**. No Java/Mixin/CI/JUnit/client/server/gameplay run during this documentation stage.
