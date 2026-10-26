# Planetary — `3R-2j`: original contextual piston collision, foreign BlockGetter provenance, and dynamic shape-cache contract

**2026-10-26 · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · branch `2.0`. Research-only packet 4/4, target original CI PASS.** No changes to production Java, Mixins, V8 developer test lab, mod data, or `main`. **Research gate 3A and full Phase3 3A–3E remain OPEN.**

## 1/4 — Real registered original BlockState → MovingPistonBlock → PistonMovingBlockEntity virtual dispatch

The original 3R-2i target-ABI scan resolved exact declarations and direct callsites for `BlockStateBase.getCollisionShape`, `MovingPistonBlock.getCollisionShape`, `MovingPistonBlock.getBlockEntity`, and `PistonMovingBlockEntity.getCollisionShape(BlockGetter,BlockPos)`. The new **research-only JUnit [`Phase3R2jDynamicShapeAndForeignGetterDispatchTest`](../../src/test/java/dev/planetary/world/Phase3R2jDynamicShapeAndForeignGetterDispatchTest.java)** executes this chain on the actual bootstrapped NeoForge 21.1.215 `Blocks.MOVING_PISTON.defaultBlockState()` and original registered `Blocks.STONE`/`Blocks.AIR` moved state classes.

**A material correction during research:** an initial synthetic new `Block` subclass test failed: the bootstrapped original target registry had already frozen intrusive block registration. [CI #40329608142](https://github.com/Abbygree11/Planetary/actions/runs/40329608142) also exposed incorrect synthetic override parameter ordering for `Block.hidesNeighborFace` at compile time; fixed against original exact JVM descriptor but rejected the synthetic strategy entirely when [CI #40330640865](https://github.com/Abbygree11/Planetary/actions/runs/40330640865) proved `IllegalStateException: Registry is already frozen` at `Block.<init>`. Neither failure was in existing production code. Superseded runs #40330309338, #40331546364 and #40331635878 were cancelled by concurrency. We did **not** hack the frozen registry, globally mutate Minecraft types, or conflate synthetic behavior with mod gameplay.

The successful test creates only ordinary **`PistonMovingBlockEntity` instances** with known original public constructor (`BlockPos,BlockState,BlockState,Direction,boolean,boolean`) and two non-Level `BlockGetter` fakes whose `getBlockEntity(pos)` selects a moving entity; both getters return the *same exact BlockState instance*. A replaced BE instance changes the moved state without modifying the query getter, BlockPos or BlockState.

**[Java21 CI #40331903029](https://github.com/Abbygree11/Planetary/actions/runs/40331903029) COMPLETED SUCCESS** on original test-source commit [`0c50012`](https://github.com/Abbygree11/Planetary/commit/0c500129fb5d0e90205a720e355e9cf1d511bf86). The exact original 23-file research artifact **12654844087** was downloaded and independently inspected: ZIP SHA256 **`412e1cfff891342752d7f04485c2a2c58914db55f51fa56f2bc6eb5e0833a62e`**. The exact generated `phase3-r2j-original-moving-be-context-dispatch.tsv` SHA256 **`75e0384f42c6e67771e70922e50d94adda139b1acf444d982eb16e5b74de85bf`**. All five samples passed actual JUnit and an independent local report parser. The committed [Python original-ZIP verifier](../../scripts/research/phase3_r2j_context_key_verify.py) can repeat the same row/context checks; it was committed separately after the successful test source and **is not claimed as its CI test step**.

| Case | Getter / BlockPos | Moved BlockEntity state | Actual `getCollisionShape` |
| --- | --- | --- | --- |
| `planet_even_stone` | planet / (2,70,4) | STONE | nonempty |
| `planet_odd_air` | planet / (3,70,4) | AIR | empty |
| `foreign_even_air` | foreign / (2,70,4) | AIR | empty |
| `planet_even_air_after_be_change` | planet / (2,70,4) | AIR after replacement | empty |
| `planet_even_stone_restored` | planet / (2,70,4) | STONE after replacement | nonempty |

**Bounded conclusion:** This is **original registered vanilla moving-piston collision source-context evidence**, demonstrating actual virtual dispatch and getter/position/BE dependency. It does not demonstrate original `getFaceOcclusionShape` changing with the same BlockState; it does not exercise loaded Create/AE2, client Mixins, active survival game or apply Planetary frame transforms.

## 2/4 — Original cache scope and direct consequence for generalized state-only shape caches

The prior original [3R-2e](PHASE3_R2E_DYNAMIC_SHAPE_CACHE_AND_FOREIGN_GETTER_1_21_1.md) full registry reflection artifact is included with this successful JUnit. It records **1060** vanilla block IDs, **241** distinct concrete block Java classes, **26684** concrete original state values, **26505** eligible cached states, **179** uncached, and **22** block IDs with `hasDynamicShape=true`. Rechecked original per-ID TSV: *all* 179 uncached states belong to those 22 dynamic IDs across exactly **six Java implementation classes**:

- 17 `ShulkerBoxBlock` IDs including plain shulker box and 16 dye variants;
- `BambooStalkBlock`, `MovingPistonBlock`, `PointedDripstoneBlock`, `PowderSnowBlock`, `ScaffoldingBlock` — one block ID each.

This is an exact **original vanilla registry** result, not proof that arbitrary compatible mod blocks have the same cache eligibility, position semantics or dynamic flags.

The five real compiled/tested collision samples prove **no cache keyed by BlockState alone can represent this moving-piston collision shape faithfully under arbitrary changed BlockEntity context**. The actual underlying source is `BlockGetter.getBlockEntity(pos)`, and the PistonMovingBlockEntity's current moved state. `BlockStateBase.Cache` is a separate vanilla static-cache mechanism with explicit original eligibility/`dynamicShape` behavior; this test is not a bug report about that vanilla cache.

Planet's [`PlanetBlockRenderCulling`](../../src/main/java/dev/planetary/client/render/PlanetBlockRenderCulling.java) uses `CacheKey(BlockState state, BlockState neighborState, Direction sourceLocalSide, Direction targetLocalSide)` to memoize a **face-occlusion Boolean**, after first calculating `state.getFaceOcclusionShape(level,pos,sourceLocalSide)` and the neighbor's corresponding face. The key excludes getter/world/position/BlockEntity/time/epoch. Its 2048-entry ThreadLocal LRU bounds memory but is not update invalidation. This is a **source-confirmed general risk if any mod/vanilla geometry consumed by this route can vary face occlusion under identical states and directions**, but the *moving piston collision test is not direct proof of that specific visibility cache bug*. A safe eligibility bypass for dynamic/caller-relative forms versus simple static cached forms, original NeoForge hide hooks and model data, and invalidation policy remain research/design gates, not implemented solutions.

## 3/4 — Exact source-of-world provenance and external mod bypass matrix

[24-route source/ABI/dynamic/context/acceptance matrix](PHASE3_R2J_DYNAMIC_CONTEXT_PROVENANCE_AND_CACHE_OWNER_MATRIX_1_21_1.tsv) records a nontrivial mismatch in current Planetary pathways:

1. `PlanetBlockShapeRuntime.finishPhysical(getter,pos,shape)` rotates only at the outermost query when the supplied getter itself `instanceof Level` and the Level is Planet-field-bound; **non-Level getters retain canonical original shape**.
2. `PlanetClientRenderFrame.stateFrameAt / candidateFacesAt(getter,pos)` does `instanceof Level` check, but if the supplied getter is **not** a Level, it switches to `Minecraft.getInstance().level` with the *same BlockPos*, without proving that its block data has the same world/position provenance.
3. `PlanetBlockRenderCulling.shouldRenderFace` reads `neighborState` using the **supplied getter**, while both local side frames can be computed using a **different ClientLevel**. This permits mixed getter/frame origin in the source-level algorithm.

Existing 1.21.1 released source plus real binary declarations from `3R-2h` establish source families where blanket assumptions are unsafe: Create6.0.10's `ContraptionWorld extends WrappedLevel` but supplies blocks in contraption space; `VirtualRenderWorld extends Level` and owns separate state/light; Ponder1.0.82's `WrappedLevel` delegates; AE2 19.2.18's `FacadeBlockAccess implements BlockAndTintGetter` but **not** Level and can substitute facade state; AE2 `GlassBakedModel` and `CableBusBakedModel` own custom `ModelData`/quad/cache output. Real loaded client/third-party dispatch and actual result at seams are still **PENDING**, not claimed broken.

**Proposed, NOT implemented, producer provenance policy:** treat real physically bound Level+BlockPos as authorized; transparent wrapped Level only if its exact coordinate transform/delegation is verified; independent fake Level gets its *own* canonical frame or none; arbitrary non-Level getter/region must carry trustworthy originating Level/transform before assigning Planet face context; unknown, different-dimension and no-Planet contexts should not inherit ambient ClientLevel automatically. Preserve source and target block-local sides separately across seams, avoid globally rewriting coordinates, and maintain original NeoForge and custom model hooks.

## 4/4 — Research gate and next owner census

**Verified JUnit:** [#40331903029](https://github.com/Abbygree11/Planetary/actions/runs/40331903029) SUCCESS; original 23-file archive 12654844087 SHA256 `412e1cfff891342752d7f04485c2a2c58914db55f51fa56f2bc6eb5e0833a62e`; raw original context TSV SHA256 `75e0384f42c6e67771e70922e50d94adda139b1acf444d982eb16e5b74de85bf` independently matched its five outcomes. The final original test *source code* passed Java21 compile and all JUnit tests; the post-test standalone verifier was committed in a later, separate commit, not a tested CI action. No loaded Minecraft client was run in this packet. 

**OPEN 3A:** real installed mod dynamic face **occlusion** rather than collision; BlockGetter origin/position transform and worker lifecycle; BE/time/dynamic shape invalidation and per-position correctness; real Create/Ponder/AE2 target-NeoForge loader + 6 faces/12 seams/8 corners/no-Planet gameplay. Original dynamic 22 IDs are **not** an arbitrary mod coverage gate. Full 3B–3E Phase3 source/ABI/owner research still **OPEN**. The old 2.4A renderer acceptance remains suspended; no implementation changes authorized by this packet.

**NEXT `3R-2k` — research only:** inventory actual chunk/client `BlockAndTintGetter` producer classes, `RenderChunkRegion`/`SectionCompiler`/NeoForge `ModelData` getter origin, custom mod facades and safe frame authorization; tie source consumer maps to concrete tested provenance and acceptance. Do not jump into Phase3 rendering fixes before comprehensive full-phase research.
