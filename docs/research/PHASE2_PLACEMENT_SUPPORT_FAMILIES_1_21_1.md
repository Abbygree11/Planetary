# Phase 2: natural placement and physical support families — 1.21.1

Status: FIRST coherent Phase-2 runtime batch IMPLEMENTED; Java 21
GitHub Actions compile + full JUnit PASS (run 37941416965);
client/physical placement and support acceptance PENDING.

## Reported gameplay regression

User Phase-4 acceptance (2026-10-09): sideways/downward faces cannot
place candle/candle cake, spore blossom or saplings; End Rod and Ender
Chest orientations wrong. Earlier 108 BlockState placement probes and
90 canSurvive/updateShape probes passed, but only tested previously
registered representatives. These failures are Phase-2 mechanism
coverage gaps, NOT evidence particles are broken.

## Vanilla Minecraft 1.21.1 call graph, comparative sources

Source: hackersense/OptiFine-Source, 1.21.1
`net/minecraft/world/item/BlockItem.java`
`net/minecraft/world/item/context/BlockPlaceContext.java`
`net/minecraft/world/level/block/{BushBlock,CandleBlock,CakeBlock,
CandleCakeBlock,SporeBlossomBlock,EndRodBlock,EnderChestBlock}.java`.

- `BlockItem.place` -> block.getStateForPlacement(context) ->
  `BlockItem.canPlace` -> state.canSurvive(level, targetPhysicalPos)
  -> Level.setBlock. Physical target/hit must stay physical.
- `BushBlock.canSurvive` -> pos.below() -> virtual
  `this.mayPlaceOn(level.getBlockState(supportPos), level, supportPos)`.
  Its `updateShape` already unconditionally rechecks canSurvive.
  **Chosen base-family adapter:** scoped redirect ONLY the `below()`
  call in `BushBlock.canSurvive` to physically resolved local DOWN
  neighbor, preserving polymorphic mayPlaceOn (vanilla/modded plants)
  and vanilla updateShape.
- `CandleBlock.canSurvive` -> Block.canSupportCenter(level,
  pos.below(), Direction.UP). Its `updateShape` schedules water
  ticks and delegates to Block default; it needs support invalidation
  on the actual physical support neighbor.
  **Chosen adapter:** canSurvive HEAD result uses shared
  PlanetBlockSupportRuntime.canSupportCenter (including support-side
  frame conversion). On updateShape at RETURN, if the updated physical
  neighbor was the support and support no longer holds, change
  vanilla return to AIR. Keep vanilla water tick scheduling untouched.
- `CakeBlock` and `CandleCakeBlock` both implement identical
  canSurvive: `level.getBlockState(pos.below()).isSolid()` and
  updateShape if `direction == DOWN && !canSurvive` -> AIR.
  **Chosen shared two-class adapter:** scoped redirect of
  `BlockPos.below` in canSurvive, letting original isSolid run on
  physical local-DOWN support; injected updateShape HEAD on Planet
  only guards the actual physical supportPos (neighborPos),
  preserving vanilla +Y and non-Planet behavior. Do NOT replace
  vanilla isSolid with canSupportCenter: that changes Cake survival.
- `SporeBlossomBlock.canSurvive` -> Block.canSupportCenter(level,
  pos.above(), Direction.DOWN) && !level.isWaterAt(pos).
  `updateShape` checks physical Direction.UP against support.
  **Chosen adapter:** canSurvive HEAD re-evaluates the same predicate
  at physically local UP support, with correctly reframed support
  face DOWN. updateShape HEAD checks supportPos == physical neighborPos.
  Preserve water query and unchanged vanilla fallbacks.
- `EndRodBlock.getStateForPlacement`: takes physical clickedFace,
  reads physical neighbor `target.relative(clickedFace.opposite)`,
  tests whether neighboring EndRod.FACING == clickedFace, then
  toggles if same. Physical neighbor lookup MUST stay physical;
  FACING property is canonical LOCAL. Reinterpret new rod facing
  in target frame; compare existing rod's FACING in existing support
  block's canonical frame -> PHYSICAL clicked face. Tiny
  version-local algorithm with shared Planet state-frame helper,
  no global BlockPlaceContext rewrite.
- `EnderChestBlock.getStateForPlacement`: horizontal player
  direction in `BlockPlaceContext.getHorizontalDirection`
  is player BODY-local; `PlanetFrameApi.localHorizontalDirection`
  already translates player frame -> target block frame, including
  edge player/target mismatch. Preserve original vanilla WATERLOGGED
  state and replace only canonical LOCAL FACING in RETURN adapter.

## Frames/semantics

- Physical hit/target `BlockPos` is never changed; support
  physical neighbor is resolved by PlanetBlockRuntime.supportQuery
  and its `PlanetBlockSupportQuery`. This is the SAME boundary
  already verified for torch/ladder/lever families.
- A local BlockState FACING stores Direction in the target canonical
  frame. When comparing FACING across adjacent blocks, DO NOT compare
  raw enum values. Convert the neighbor's state-local direction to
  physical using its OWN canonical frame.
- All adapters use `LevelReader instanceof Level` gate via
  PlanetBlockSupportRuntime; virtual worldgen contexts fall back
  to original vanilla calls. +Y equivalence is tested separately.
- General-purpose Direction/BlockPos, physical updates and
  world sounds/RNG must NOT be globally changed.

## Explicitly deferred, not silently fixed by this batch

- Ender Chest uses ENTITYBLOCK_ANIMATED; its BlockEntityRenderer
  rotates independently (Phase 3), as does the book on the
  Enchanting Table. A correctly authored FACING does not repair BER.
- `FlintAndSteelItem.useOn` delegates to BaseFireBlock.canBePlacedAt
  and portal plane; Phase-2 ignition + Phase-9 portal pair later.
- Plant randomTick/growTree/light checks and worldgen structures
  belong to Phase-2 plant growth/Phase 10, even after basic
  BushBlock support works.
- Full 1.21.1 family inventory is larger (corals, hanging entities,
  crops, vines, rails, trapdoors) and remains OPEN. Do not label
  entire Phase 2 DONE after this first batch.
- NeoForge modified mayPlaceOn extensions and random placement must
  be preserved; avoid a global BlockState.canSurvive injection.

## Acceptance & regression

1. GitHub Actions Java 21 Gradle compileJava/compileTestJava/JUnit
   must be green for the exact Java patch before requesting game.
2. New integration contract tests should assert exact callback
   descriptors and target method ownership; pure all-six-face frame
   transform tests should prove +Y pass-through, local DOWN/UP support
   and end-rod adjacent orientation round trips.
3. One developer fixture run on +Y and one side and -Y:
   CYAN prebuilt references vs LIME actual BlockItem/useOn. Candles,
   cakes, spore blossom and sapling should place on their correct
   physical locally supporting block. End Rod should orient as
   clicked locally; Ender Chest FACING should match player.
4. Remove physical support: affected objects must break/drop;
   changing unrelated physical neighbor should NOT destroy them.
5. Test edge where target-state frame differs from player body
   or support frame, and vanilla ordinary +Y/foreign world.
6. Ender Chest/enchant table special render errors are Phase 3;
   do not treat this placement patch as their visual acceptance.
