# Stage 3A-11.3 — six-face egg stacking, hatch/support, water-surface placement and entity-spawn acceptance contract

**2026-10-10, branch `2.0` · Minecraft 1.21.1 / NeoForge 21.1.215.** This is a **proposed future acceptance matrix, all 30 tests NOT RUN**. No source patch, Java adapter, patched NeoForge ASM validation, CI or client/server gameplay run. [Exact original compiled declarations, BlockItem vs water-special ITEM and real entity source paths](PHASE2_STAGE3A_EGGS_BLOCK_SOURCE_ITEM_ALTERNATE_WRITERS_1_21_1.md). “Eggs” are **three different engine algorithm owners**; never add one generic hatch/UP patch.

## 1. Source semantics: three independent ownership chains

| Concrete original block | Vanilla physical world-axis operation | Future intentional Planet local-frame boundary |
|---|---|---|
| [`TurtleEggBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TurtleEggBlock.java#L33-L220) | Stacking replaces **same real BlockPos** (EGGS1..4); `onSand/isSand` checks physical `pos.below()`; randomTick only progresses hatch when sand below and time-of-day/random gate passes; source `canSurvive` inherited base (not sand requirement); actor `stepOn/fallOn/playerDestroy` can lower count | Phase 2 local substrate for hatch *gate*, canonical physical source state and same-cell stacking; Phase 7 actor collision and 1..4 turtle spawn, in actual physical cells; Phase 3 shapes and debris |
| [`SnifferEggBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SnifferEggBlock.java#L26-L120) | `hatchBoost` physical `pos.below()` and `BlockTags.SNIFFER_EGG_HATCH_BOOST` accelerates scheduled tick rate; no obligatory boost for survival/hatch; `onPlace` schedules hatch ticks; `tick` increments stage 0→1→2 and creates baby sniffer | Phase 2 substrate **rate modifier**, not survival. Phase 7 scheduler/lifecycle, entity spawn/physical center |
| [`FrogspawnBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FrogspawnBlock.java#L26-L161) | `canSurvive` calls `mayPlaceOn(pos.below())`: below cell fluid=WATER and frogspawn cell fluid=EMPTY; `updateShape` may remove to AIR on any neighbor change; scheduled tick rechecks survival and hatches 2–5 tadpoles at physical egg position `Y-0.5` | Phase 2/5 actual water/empty fluid-interface topology and invalidation, Phase 7 tadpole world motion and FALLING_BLOCK collision, Phase 3 thin surface geometry |

**Canonical six face local-UP basis actually defined by Planetary `PlanetFace`:**

| Face | Local UP projected physical | Local DOWN projected physical |
|---|---|---|
| POS_Y | +Y | −Y |
| NEG_Y | −Y | +Y |
| POS_X | +X | −X |
| NEG_X | −X | +X |
| POS_Z | +Z | −Z |
| NEG_Z | −Z | +Z |

`PlanetBlockStateFrame.resolve(field,physicalPos)` produces the **same source canonical frame** regardless of which path reached that physical cell, especially on cube edges/corners. `PlanetBlockFrameContext` and `PlanetBlockNeighborQuery` are intentional seam/path **traversal** frames and cannot create virtual alias BlockPos or globally rewrite `BlockPos.below()/above()`. `BlockPlaceContext` and `BlockHitResult` physical coordinate provenance distinct from canonical target BlockState frame; physical game-event `Vec3`, chunk `x>>4,z>>4`, block ticks, water and entity spawn remain true Minecraft physical identities.

For a POS_X face, source-local DOWN → physical **−X**, rather than vanilla `pos.below()` world −Y. For a NEG_Y face, source-local DOWN → physical **+Y**. A simple one-block local support lookup at a **source-local** egg is not sufficient to solve **actual water flow on vertical shell walls**, nearby chunk ticking, collision, mob physics and terrain feature generation. Each owner needs a separate boundary:
1. Turtle `onSand`/random hatch *only* checks local-support sand. Do not make inherited `canSurvive` suddenly require sand or silently break eggs that were placed off-sand. On natural Turtle AI [`TurtleLayEggGoal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/animal/Turtle.java#L576-L636) sand search, MoveToBlockGoal, body pose and direct `setBlock(sandPos.above(),EGGS=1..4)` must use coherent worldgen/AI/Phase 7 logic.
2. Sniffer `hatchBoost` checks tagged physical support (typically faster hatch), not required placement, and `Sniffer.spawnChildFromBreeding` [drops an ITEM entity](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/animal/sniffer/Sniffer.java#L399-L408) rather than new egg BLOCK.
3. Frogspawn `mayPlaceOn` requires exactly one **real source-fluid-neighbor WATER** and its own fluid EMPTY. Translated local_DOWN must preserve honest water surface and avoid a six-face “floating water” invented state; Phase 5 must actually guarantee water on planet exterior. [`PlaceOnWaterBlockItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/PlaceOnWaterBlockItem.java#L20-L33) bypasses normal on-block use via `useOn PASS` and `use` does SOURCE_ONLY water raycast, modifies hit physical `BlockPos` by vanilla world `above()` before calling BlockItem.useOn. An adaptive target must come from actual physical water hit and local surface semantics **at this item algorithm**, not a global `getPlayerPOVHitResult` hack.
4. [`FrogAi.TryLaySpawnOnWaterNearLand`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/ai/behavior/TryLaySpawnOnWaterNearLand.java#L17-L65) is another **direct frogspawn BlockState author**: frog entity `blockPosition.below`, physical world XZ `Direction.Plane.HORIZONTAL`, water fluid+collision shape check, physical `waterPos.above` as target. Treat as distinct Phase 7 AI and Phase 5 fluid owner from item use and from Frogspawn.tick. Four candidate tangents need a continuous seam policy, not a blanket vanilla Direction.Plane.HORIZONTAL rewrite.

**Non-Planet vanilla pass-through** is mandatory for all these algorithms and synthetic/fake Levels. Never infer successful loading of a WorldGenLevel or Mixin from an inherited source method.

## 2. Scheduling/hatch-specific traps

- Turtle `randomTick` is server stochastic and its day-phase gate matters; use controlled seeded calls or a deterministically fixed time-of-day. Actual baby count should equal EGGS, not a generic fixed baby count. Egg destruction by `stepOn` skips carefully stepping; `fallOn` (except Zombie) uses separate chance; players can break under different mob-grief rules, and turtles/bats are immune actors. Entity motion/world block collision Phase 7 tests independent of block orientation.
- Sniffer `onPlace` chooses boosted `(12000/3)+[0,299]` vs regular `(24000/3)+[0,299]`; `tick` **does not schedule itself**, but its own `setBlock(HATCH+1,2)` may retrigger `onPlace` through actual BlockState lifecycle. This requires *actual source-to-world neighbor-change trigger tracing* and runnable tests for multi-stage timing. Don't assert a second stage scheduler without evidence. Hatch completes on final tick when HATCH=2, spawning **one** Sniffer at physical center with randomized yaw.
- Frogspawn's scheduled hatch delay `nextInt(3600,12000)` is an *interval*, not a fixed tick or randomTick; `updateShape` immediately replaces invalid egg with AIR (may differ from `destroyBlock(false)` event visibility), scheduled `tick` rechecks water and can destroy if invalid. `spawnTadpoles` emits `nextInt(2,6)` ⇒ **2..5** at world `Y - 0.5`; on side/down planet faces, local downward ejection and amphibian water survival are a **separate entity/Phase 7 policy** not proven by source. `FrogspawnBlock.entityInside` specifically handles FALLING_BLOCK — do not omit this creation-independent destroy path.
- [`BlockItem.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L61-L120) may apply valid `BLOCK_STATE` item components **after initial placement**. Turtle `eggs/hatch` and Sniffer `hatch` are valid properties. Frogspawn has **zero properties**, so it must never acquire FACING/HATCH/WATERLOGGED in vanilla. Synthetic command/structure placement without BlockItem may also write legal state, and may skip subclass Item methods.

## 3. Reproducible future acceptance fixtures — all **NOT RUN**

One fixture requires source-version hash, NeoForge 21.1.215, branch/commit, deterministic seed, precise physical source/water/target/chunk positions, canonical block frame and optional traversal entry chart, Level/Item/AI/command writing path, pre/post BlockState and FluidState, scheduled tick count & origin, emitted entity count and physical spawn pose, event/sound/particle metadata, physical neighbor updates, Mixin applied true/false, expected vs observed. Run real NeoForge **client and server** before any PASS.

| Test | Independent setup | Required check |
|---|---|---|
| **EG11-01** | Non-Planet Level Turtle ordinary BlockItem click on sand | Initial EGGS=1 HATCH=0; normal vanilla physical world-Y placement |
| **EG11-02** | Non-Planet Turtle item target off sand | Egg may exist; inherited canSurvive doesn't enforce sand, random hatch blocked |
| **EG11-03** | Turtle item repeated click 1,2,3,4 eggs same physical BlockPos | Only one physical state, increments until 4, fifth click not stacked |
| **EG11-04** | Same turtle item with secondary-use active on existing egg | Original canBeReplaced branch changes result; no duplicate/alias |
| **EG11-05** | Turtle `BLOCK_STATE` item component eg EGGS=4,HATCH=2 | Legal post-place properties applied (or actual component handling traced); do not claim impossible state |
| **EG11-06** | Turtle `randomTick` forced 0.65–0.69 day fraction, on sand | HATCH stages and final 1..4 baby turtles, sound/event counts |
| **EG11-07** | Turtle `randomTick` outside favorable time with deterministic 1/500 hit or miss | No false stage transition, source RNG path preserved |
| **EG11-08** | Turtle standing, sneaking, falling player; zombie vs living mob, Turtle/Bat | Separate `stepOn` vs `fallOn` rolls, mob-grief/actor exclusions; physical overlap Phase7 |
| **EG11-09** | Turtle `playerDestroy` multi-egg vs one-egg | Proper EGGS decrement or destroy, no double drops, event source |
| **EG11-10** | TurtleLayEggGoal natural sand-block target & nested AI navigation | Direct `setBlock` authors 1..4 eggs above true sand and assigns actual world coordinates |
| **EG11-11** | Sniffer ordinary BlockItem and `BLOCK_STATE(hatch)` | Legal stage 0..2, no BE or waterlogged |
| **EG11-12** | Sniffer egg on hatch-boost tag below vs ordinary block below | Both survive, boosted initial `onPlace` event 3009 and earlier scheduled first tick |
| **EG11-13** | Sniffer `onPlace` schedule fixed random seed and actual block lifecycle | `4000+0..299` vs `8000+0..299` first delay, do not infer other elapsed cycles |
| **EG11-14** | Sniffer HATCH=0,1,2 and scheduled ticks, reload mid-cycle | Stage reaches 2 then exactly one baby Sniffer physical center; tick rescheduling provenance logged |
| **EG11-15** | Breed two sniffers on any Planet face, no block placement by players | Breeding drops one SNIFFER_EGG ItemEntity rather than writing egg BlockState |
| **EG11-16** | Frogspawn PlaceOnWaterBlockItem `useOn` with ordinary physical block hit | `PASS` without direct placement, right-click air via `use` path separately |
| **EG11-17** | Frogspawn ITEM `use` source-only fluid raycast hitting water vs flowing water | Exact hit/adjusted physical hit and eventual real target BlockPos, no “normal BlockItem only” inference |
| **EG11-18** | Frogspawn target block fluid EMPTY with below source water | Successful placement, no WATERLOGGED/other property, schedule hatch tick |
| **EG11-19** | Frogspawn below water removed or source block replaced, any neighbor updates | Survival invalidates AIR promptly; scheduled tick cannot hatch without water |
| **EG11-20** | Frogspawn scheduled hatch with seed min/max delay and empty source-fluid above | `3600..11999` delay, spawn exactly 2..5 persistent tadpoles at true water side |
| **EG11-21** | Frogspawn `entityInside` with FALLING_BLOCK vs turtle/player control | Falling block destroys frogspawn; not a general collision kill |
| **EG11-22** | Natural FrogAi pregnant LaySpawn triggered near land+water | Direct BlockState write via physical XZ search and waterPos.above; no PlaceOnWaterBlockItem callback |
| **EG11-23** | All **6** Planet face interiors, Turtle eggs on locally beneath-sand | No false inherited support requirement; intended local support for random hatch, single real BlockPos |
| **EG11-24** | All **6** Planet faces, Sniffer with hatch-boost substrate inward | Correct local downward tagged support, timer physics same regardless of global Y |
| **EG11-25** | All **6** Planet faces, Frogspawn on real exterior water with actual fluid behavior | Local-down source WATER and target fluid EMPTY; Phase5 water flow and gravity actually valid, no imaginary WATERLOGGED |
| **EG11-26** | Each of **12** face-pair edges, approach egg/frogspawn state from both face charts | One canonical physical target BlockPos, correct source/target support map, no duplicate block/tick |
| **EG11-27** | Each of **8** triple-face corners, three face chart entry paths | Same canonical position, legal same-cell Turtle stacking, no alias baby spawns |
| **EG11-28** | Custom \`StructureTemplate\`, `/setblock`, `/fill` place legal egg states and frogspawn | State writer bypasses item; no claimed shipped vanilla NBT containing these states |
| **EG11-29** | Force physical chunk x/z seam, schedule hatch, save/reload, unload while water updates | Real chunk/block tick validity and fluid state, no replayed duplicate hatch |
| **EG11-30** | Side/down face hatch entities and no-Planet baseline using same seed | Baby Turtle, Sniffer and Tadpole spawned with coherent world/local gravity; client/server Mixin/ASM acceptance independent of source |

**All tests EG11-01…EG11-30 NOT RUN**. Item/AI post-place extra state may be legal even if it deviates from default player placed state. Do not “fix” gameplay with a class-specific one-off when general support/traversal/physics mechanisms are missing.

## 4. Outstanding integration decisions and owner phase

- **Phase 2:** neutral generic source-local support query for sand/boost/water and physical target canonical face at seams. Distinguish egg **hatch rate**, egg **survival**, and frogspawn **water constraint**. Real physical BlockPos for all writes; Turtle stack uses same target cell.
- **Phase 5:** fluid surface, source-only water raycast and true water on side/down planet faces, flow and neighbor schedule.
- **Phase 7:** turtle entity layEgg, frog AI horizontal neighborhood with local water surface, Sniffer ItemEntity drop, actor collision/destroy paths, baby turtle/sniffer/tadpole real positions/orientation and spawn collisions. Not a block orientation task only.
- **Phase 8/9:** biome/natural laying search, generation/structure direct writers/feature provenance and no-Planet fallback.
- **Phase 3:** egg/frogspawn shape/culling, appearance, events/audio effects and particles.

**Not proven:** exact NeoForge-patched bytecode semantics and applied Mixin, actual Planet adapter coverage, complete other-mod nonitem setBlock writers, real six-face hatch gameplay, natural generation compatibility. Ledger source+compiled declaration reviewed 78/241 (199/1060 BLOCK IDs) only, remaining 163/241 (861/1060 IDs), **all 241 ASM/Planet/gameplay statuses REVIEW_PENDING**. Next internal Stage 11.4 reparse immutable original ZIP independently against live GitHub ledger, prepare next whole-family card, then STOP.
