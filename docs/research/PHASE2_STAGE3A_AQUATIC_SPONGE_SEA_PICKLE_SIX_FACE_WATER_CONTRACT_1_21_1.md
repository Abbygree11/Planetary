# Stage 3A-14.3 — Sponge BFS, wet sponge animation and SeaPickle six-face/edge/corner future acceptance contract

**2026-10-10 · Planetary 2.0 · Minecraft 1.21.1 · NeoForge 21.1.215 · 32 future cases ALL NOT RUN.** No Java/Mixin changes, no NeoForge transformed ASM verification, no client/server acceptance. [Stage14.1/14.2 exact compiled original owner+Item/writer graph](PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_ORIGINAL_BLOCK_ITEM_WRITERS_1_21_1.md).

## Source-specific coordinate policies

**Physical world storage invariant:** Minecraft block and fluid cells always exist at unique actual \`BlockPos(x,y,z)\`, with physical XZ chunks and single canonical \`PlanetBlockStateFrame\`. \`PlanetBlockFrameContext\` can transport a chart orientation **only during intentional local surface traversal**. Edges/corners do not alias physical root/water cells. Never globally convert \`BlockPos.relative\`, \`Level.getFluidState\`, fluid state metadata or \`Level.setBlock\`.

**SpongeBlock** [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SpongeBlock.java#L34-L118): \`onPlace\` and \`neighborChanged\` (NOT randomTick) call bounded \`breadthFirstTraversal(spongePos,6,65)\`. Source enumerates **all six physical axial** offsets; accepted starting sponge root counts toward the **65 node bound**, so expect at most **64 other accepted physical water cells**, not 65 absorbed water blocks. The only valid progression graph is WATER-tagged FluidState with successful \`BucketPickup\`, vanilla \`LiquidBlock\` or explicitly named WATER vegetation (kelp/seagrass variants). Failed pickup or wrong water-bearing block does not count. The root converts to WET_SPONGE with sound iff >1 nodes accepted; no transformation for zero water. Do not rotate each neighbor by local-up or substitute 18/26 neighbor physical propagation. The actual BFS count/visited semantics and chunk interactions must be profiled in game.

**WetSpongeBlock** [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WetSpongeBlock.java#L29-L98): \`onPlace\` into ultraWarm directly rewrites SPONGE with sound/event 2009; no scheduled dryer. Client \`animateTick\` chooses physical direction and ignores physical \`UP\`, with dripping particle world-Y/side offsets. On a Planet side/bottom local gravity, a future **Phase3 visual** adapter might map semantic drip direction, but must preserve vanilla no-Planet behavior and avoid changing the actual ultraWarm writer.

**SeaPickleBlock** [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SeaPickleBlock.java#L27-L213): four legal \`PICKLES\` states, \`WATERLOGGED\`; same-cell \`BlockItem\` stacking, \`canSurvive\` world-\`below()\` and target upward face collision shape **nonempty OR** target face sturdiness, \`updateShape\` returns AIR on ANY lost support and schedules real water fluid tick otherwise. Target fluid type WATER suffices for initial waterlogged (source unlike coral's amount8 condition). \`performBonemeal\` has real mutation gate own WATERLOGGED=true and block **below** in \`BlockTags.CORAL_BLOCKS\`, while \`isValidBonemealTarget\` and \`isBonemealSuccess\` unconditionally true. Bonemeal generates positions in world-XZ/Y coordinate arithmetic, requires exact WATER target and coral below, sets new PICKLES 1..4 and original PICKLES4. \`CoralFeature\` independently creates sea pickles above reef, not via BlockItem/bonemeal. On Planet POS_X, **source-local DOWN→physical −X**; NEG_Y DOWN→+Y; every actual target's exposed support face must be derived from its own physical/canonical frame. The bonemeal XZ/Y *volume* must be adapted at its own generator/algorithm boundary, not by globally rewriting \`BlockPos\`. Distinct 1..4 voxel shapes are Phase3, water Phase5, reef/bonemeal Phase8/9.

**Six canonical gravity faces:** POS_Y UP=+Y; NEG_Y UP=−Y; POS_X UP=+X; NEG_X UP=−X; POS_Z UP=+Z; NEG_Z UP=−Z. Every source has a single immutable physical position even on all 12 cube face-pair edges and 8 triple corners. Physical adjacency for fluid absorption must remain six actual axial blocks; semantic local support and feature-space are different concerns.

**Required future test logs:** fixture ID, Git/NeoForge/Minecraft version, initial Planet field enabled? and canonical source+target face/chart, exact real root and water blocks/chunk, original/patched method owner and applied Mixin verified? yes/no, original BLOCK and ITEM registry IDs, fluid tags/types, \`BucketPickup\` returned item, accepted BFS nodes and exact removed positions, RNG/seed, time+dimension ultraWarm, post BlockStates PICKLES/WATERLOGGED, actual water fluid ticks, particle world/local positions, bonemeal sample coordinates, events and sounds, save/reload, vanilla no-Planet parity, per-fixture observed vs expected. Test cases below are SPECS ONLY, no automatic PASS.

## All 32 proposed future acceptance cases — NOT RUN

| ID | Fixture/action | Required evidence |
|---|---|---|
| AQ14-01 | Vanilla/no-Planet Sponge placed next to one still WATER source | Root+one accepted water, water removed and sponge becomes WET_SPONGE, one absorption sound |
| AQ14-02 | Sponge with no adjacent WATER/only non-water fluid | No successful visit >1, remains dry Sponge |
| AQ14-03 | Sponge on same block replaced with Sponge state | OnPlace previous-same-block guard, distinguish neighborChanged trigger |
| AQ14-04 | NeighborChanged multiple times with new water sources | Physical BFS repeated, no duplicate root aliases or unbounded traversal |
| AQ14-05 | Line of WATER exactly depth6, then water at depth7 | Only nodes accepted up to original depth6; outer ring untouched |
| AQ14-06 | More than 64 absorbable water cells, deterministic set | Original max 65 accepted nodes including root; no >64 distinct removed water cells |
| AQ14-07 | Sponge traversing WATER through blocked non-water cells | No phantom diagonal or non-water conduit; six axial neighbor graph only |
| AQ14-08 | Sponge next to waterlogged BucketPickup block returning item | Original pickupBlock concrete writer consulted; no blanket AIR rewrite |
| AQ14-09 | Sponge next to waterlogged block with unsuccessful pickup | Do not remove unsupported solid block solely from FluidTags.WATER |
| AQ14-10 | Sponge next to source LiquidBlock | Physical AIR write flag3 and other block/fluid neighbor updates |
| AQ14-11 | Sponge next to KELP, KELP_PLANT, SEAGRASS, TALL_SEAGRASS in water | Explicit vegetation destroy/drop path; actual physical block entity/resources observed |
| AQ14-12 | Sponge on each six canonical Planet face interiors | Same physical 6-axial BFS radius, water continuity Phase5; no chart-translated neighbors |
| AQ14-13 | Sponge located at all 12 face-pair edges with two approaches | One real root, same accepted water set independent of traversal entry chart |
| AQ14-14 | Sponge at all 8 triple corners with three approaches | No duplicate physical water absorption/tick/sound based on chart identity |
| AQ14-15 | Sponge absorption crossing XZ chunk seam/unloaded chunk | One physical cell per position, chunk-loading and edge behavior instrumented |
| AQ14-16 | Place WetSponge into ultraWarm dimension via BlockItem | Immediate direct WET_SPONGE→SPONGE, event2009 and drying sound |
| AQ14-17 | Place WetSponge in normal Overworld and wait ticks | No fabricated scheduled automatic drying from client drips |
| AQ14-18 | WetSponge animateTick at fixed seed for all six physical directions | Vanilla skips world UP and computes physical world-Y/XZ particle face offsets |
| AQ14-19 | WetSponge on POS_X,NEG_Y,NEG_Z face with local gravity | Future local visual droplet presentation tested separately from ultraWarm conversion |
| AQ14-20 | SeaPickle ordinary BlockItem single underwater placement | PICKLES=1, WATERLOGGED from exact target Fluids.WATER type |
| AQ14-21 | SeaPickle four successive same-item clicks on same physical block | PICKLES increments 1→4 in same physical BlockPos, fifth does not stack |
| AQ14-22 | SeaPickle secondary-use active or different held item | canBeReplaced alternate branch vs new target, no fake extra pickle cell |
| AQ14-23 | SeaPickle crafted ITEM BLOCK_STATE PICKLES/WATERLOGGED overrides | Legal late component changes occur after placement; no FACING or BE |
| AQ14-24 | SeaPickle atop full face, thin top collision, no supporting face | Source mayPlaceOn nonempty collision-face shape OR sturdiness and canSurvive true/false |
| AQ14-25 | SeaPickle unsupported, waterlogged vs dry, any neighbor update | updateShape AIR on actual lost support; otherwise WATER tick if WATERLOGGED |
| AQ14-26 | SeaPickle dry vs wet, bonemeal success predicate true | Actual performBonemeal mutates only wet and below coral tag; not predicate alone |
| AQ14-27 | SeaPickle performBonemeal on wet coral at fixed RNG seed | Exact XZ/Y 5-column sampled positions; target exact WATER and coral below; new states 1..4; source sets PICKLES4 |
| AQ14-28 | CoralFeature reef alternate sea pickle writer | Direct pos.above BlockState creation bypasses BlockItem/bonemeal, correct physical water |
| AQ14-29 | SeaPickle on six Planet face interiors and all edges/corners | One canonical local support, physically valid fluid, rotated thin shapes and no duplicate growth |
| AQ14-30 | Bonemeal growth on ±X/±Z/NEG_Y face with physical 3D water volume | Source feature-space local chart instead of naïve global XZ/Y; no fake world pos |
| AQ14-31 | Vanilla direct /setblock, /fill, synthetic StructureTemplate with these three states | State writers bypass BlockItem, only actual legal PICKLES/WATERLOGGED allowed |
| AQ14-32 | NeoForge server/client + save/reload/other-mod/no-Planet control after future implementation | Actual patched ASM, applied Mixin, performance, particles, fluid, block state gameplay all tested individually |

## Outstanding integration/acceptance boundaries

- **Phase 2:** SeaPickle same-cell stacking and source-local support/target sturdy face, distinguish physical clicked hit from canonical state and seam traversal. 
- **Phase 3:** SeaPickle 1..4 world-Y source shapes, wet sponge dripping particles. 
- **Phase 5:** Sponge breadth-first WATER-tag graph and actual waterlogged \`BucketPickup\` implementations, own wet fluid state, underwater water continuity, fluid schedule and chunk order.
- **Phase 7:** growing coral habitat/mobs and block event/sound/particles as distinct systems; no user request for runtime physics patches in this stage.
- **Phase 8/9:** \`CoralFeature\` and SeaPickle bonemeal world-XZ/Y generator-space/shell continuity; commands and possible saved StructureTemplate writers (specific shipped palette not verified).
- **Future acceptance:** all 241 NeoForge patched-method bytecode, Planet adapter, gameplay fields \`REVIEW_PENDING\`; all 32 AQ14 tests NOT RUN. Ledger reviewed 92/241 classes and 241/1060 exact BLOCK IDs after three source+original declaring-owner promotions. Stage14.4 must independently reconcile original 241/92 full-signature nearest owner and exact ID with live GitHub and hand off next family under SAME user “кк”.
