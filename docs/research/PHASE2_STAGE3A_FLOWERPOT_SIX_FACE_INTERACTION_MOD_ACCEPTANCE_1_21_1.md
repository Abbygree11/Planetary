# Stage3A-21.3 — 50 future six-face / edges / corners / flower pot item/NeoForge/mod acceptance fixtures

**2026-10-11 · FP21-01 .. FP21-50 ALL NOT RUN.** No runtime Java/Mixin change, dedicated server/client execution, actual mod/gameplay/CI proof. Source knowledge only, not Phase2 gameplay completion. [Owner/mod report](PHASE2_STAGE3A_FLOWERPOT_ORIGINAL_ITEM_REGISTRATION_MOD_MECHANISMS_1_21_1.md), [original 107-class ZIP-vs-ledger reconciliation](PHASE2_STAGE3A_FLOWERPOT_ORIGINAL_107_CLASS_RECONCILIATION_1_21_1.md).

Future fixture evidence required: exact target NeoForge21.1.215 transformed ASM effective owners and mixin invoke/handler signatures, world mode and mod binary with all required versions, original physical BlockPos and true BlockHitResult/item/hand, source/target canonical gravity face and tangent+chart, actual registered Block and legal properties (all original pots zero), original empty-pot ownership and supplier content map, GameEvents/stats/stack deltas, target real collision and client geometry, external BE sided Direction and recipe state, 12 edges/8 corners and chunks/fake worlds. Independently mark **ASM, Planet adapter, server, client render, mod-loaded binary and performance** PASS/FAIL/SKIP, NEVER green from source only.

| ID | Future action | Exact expected evidence |
| --- | --- | --- |
| FP21-01 | Original empty pot BlockItem physical normal overworld | Target set only minecraft:flower_pot using BlockItem.place, original real hit and collision |
| FP21-02 | Original empty pot placed via null-player/DirectionalPlaceContext | Real target remains physical, no player-driven fake face |
| FP21-03 | Each exact 34 potted contents on an empty pot | Item→BlockItem.getBlock→registered map→correct filled original BLOCK variant |
| FP21-04 | All original 35 states have zero BlockState properties | No FACING, WATERLOGGED, PLANT, AGE or synthetic property created |
| FP21-05 | FlowerPot original 1 item vs 34 filled variants no ITEM | Only minecraft:flower_pot item placed directly, filled are state-swap block IDs |
| FP21-06 | Empty pot insert held dandelion | One held item consumed, real block changes to potted_dandelion, stat/event emitted |
| FP21-07 | Empty pot insert unsupported block or non-BlockItem | PASS_TO_DEFAULT_BLOCK_INTERACTION; no item consume or unintended plant |
| FP21-08 | Filled pot attempt to insert a second supported plant | CONSUME, filled state preserved and held count unchanged |
| FP21-09 | Extract content empty hand from filled pot | Original plant item added inventory, pot becomes empty, BLOCK_CHANGE event |
| FP21-10 | Extract filled pot when player inventory full | Exactly one content item dropped with correct owner/direction, pot empty after |
| FP21-11 | Click empty pot without item | InteractionResult.CONSUME, no extraneous item or state |
| FP21-12 | Clone item / pick block empty versus filled | Empty gives flower pot item, full gives corresponding plant item |
| FP21-13 | Break empty pot, then break filled pot | Check actual loot tables and no invented potted BlockItems or dupes |
| FP21-14 | Check source FlowerPotBlock.updateShape after physical DOWN neighbor | Inherited canSurvive actual behavior; no fabricated CandleBlock support predicate |
| FP21-15 | Remove physical block beneath potted pot | Record actual NeoForge target survival and updateShape, not automatically assume detaches |
| FP21-16 | Run randomTick/bonemeal on contained potted cactus | No ordinary cactus growth or replacing whole pot with cactus |
| FP21-17 | Run randomTick/bonemeal on potted sapling/mangrove | No independent sapling generation from pot without explicit mod algorithm |
| FP21-18 | Potted flowers/mushrooms/roots/cactus biome constraints | Potted variant identity retained, no inherited raw-plant survival wrong positives |
| FP21-19 | Use block state item component with ordinary empty pot | No legal property change, item stack component cannot invent facing |
| FP21-20 | Water adjacent or within pot physical cell | Vanilla FlowerPotBlock has no waterlogged property nor SimpleWaterloggedBlock contract |
| FP21-21 | Verify all 35 VoxelShape and render shapes | Physical base box [5..11,0..6,5..11], independent visual plant geometry |
| FP21-22 | Vanilla no-Planet compared with Planet disabled | Identical interaction ordering, stats, items, shape, render and support |
| FP21-23 | Actual player empty pot placement on six Planet faces | Same real target, valid state with canonical model and no phantom support |
| FP21-24 | Actual player insert and extract plant on six Planet faces | Preserve real clicked face, unique pot block swap, one item transaction |
| FP21-25 | Six-face render potted flower and bamboo | Local pot normal + foliage models aligned; physical collision matches render |
| FP21-26 | All 12 two-face edges, both travel directions | Unique real cell with one filled pot, no double item consumption, safe chart conversion |
| FP21-27 | All 8 three-face corners with three approaches | Deterministic local face/tangent and unique target; no alias or duplicate placement |
| FP21-28 | Real physical chunk boundary and save/reload | Exact original full registered BLOCK ID persists, correct plant and item inventory |
| FP21-29 | Structure template directly places potted_* | Writer bypasses pot interaction mapping; chart documented, no fabricated item action |
| FP21-30 | /setblock or /fill potted content | State available without FlowerPotBlock.useItemOn; no item consumption |
| FP21-31 | Mod empty-pot owner NeoForge supplier registration | Actual target21.1.215 exact constructor, addPlant(getBlockId,supplier) patch+coremod verified |
| FP21-32 | Modded registered plant content correct full pot block | Deferred supplier and getEmptyPot owner preserved; no global POTTED map rewrite |
| FP21-33 | Modded plant not registered in pot mapping | No accidental placement from tag/name inference |
| FP21-34 | Modded item without BlockItem wraps a plant | Vanilla pot does not infer from item name, mod event override separately monitored |
| FP21-35 | BotanyPots actual 1.21.1 binary plus dependencies | Record available NeoForge21.1.209 dev baseline, actual 21.1.215 launch proof before PASS |
| FP21-36 | BotanyPotBlock independent getStateForPlacement | Own HORIZONTAL_FACING, LEVEL, physical true WATERLOGGED preserved |
| FP21-37 | BotanyPotBlock useItemOn harvest, fertilizer and recipe | Own BlockEntity and menu; bypass FlowerPotBlock entirely |
| FP21-38 | BotanyPotBlockEntity ticking soil/crop growth | Recipe cache, harvest, output and no vanilla pot semantics injected |
| FP21-39 | BotanyPots hopper automation on +Y face | Physical pos.below and Direction.UP insertion default when unknown provider |
| FP21-40 | BotanyPots sided inventory Direction.DOWN automation | Explicit provider opt-in needed to map local sides; no global Direction rewriting |
| FP21-41 | BotanyPots BE rendering + custom VoxelShape | Local shape, pose, plant display and BE sync verified independently |
| FP21-42 | BotanyPots save/reload inventory and external comparator | Full BE items/progress and comparator level consistent after chunk save |
| FP21-43 | Unknown mod custom FlowerPotBlock subclass | Actual overridden method owner captured, no claim base Mixin covers override |
| FP21-44 | Foreign fake Level/contraption/clipboard modifies pot | Default physical source/target positions, no cross-world chart conversion without provenance |
| FP21-45 | Actual target patched ASM verification | NeoForge 21.1.215 useItemOn/getPotted coremod owner, invoke targets and handler signature verified |
| FP21-46 | Player on surface boundary while world updates neighbor | Original incoming physical Direction preserved; no fake canSurvive true/false |
| FP21-47 | Concurrent two-player insert/extract same pot | Server single item transaction, no duplicate plant or unexpected state |
| FP21-48 | Actual loaded 1.21.1 modded NeoForge event cancellation | Native item/block event layering preserved; records original/opt-in paths |
| FP21-49 | Client render and interaction under inverted underside face | No hardcoded world-UP pot geometry/particle mismatch, collision still physical |
| FP21-50 | Large pot inventory/six-face placement performance | No per-tick global allocations/registry scans; benchmark client and server |

## Whole mechanism phase ownership and exclusions

Phase2: BlockItem physical hit/placement, pot in-place direct `useItemOn/useWithoutItem`, NeoForge supplier registration, original inherited canSurvive + updateShape; Phase3: all 35 pot/plant renderer variants, rotated voxel geometry and under-face ray picks; Phase5: no vanilla FlowerPot WATERLOGGED (modded BotanyPots own TRUE WATERLOGGED); Phase7: drops/stats/inventory/BE harvest; Phase8/9: commands/structure/WorldGenLevel/fake-world bypass; Phase10: mod pot supplier registration and independent BotanyPots BE/recipes and sided automation, unknown providers physical default.

No claim that the source itself confirms user-facing Planet side/bottom flower pot placement. Future acceptance must use **actual player placement** on all six faces, not force-placed block state fixture.

**All 241×3=723 original ASM/Planet/gameplay columns remain REVIEW_PENDING. All 50 future cases NOT RUN.**
