# Phase 2 Stage 3A-3.3 — graph source/target frames, neighbor callbacks and shape/cache contracts

**Research-only checkpoint:** 2026-10-10, Planetary branch `2.0`,
Minecraft **1.21.1**, NeoForge target **21.1.215**, Java 21.

**Scope:** the SAME eight source+NeoForge-reflection-reviewed concrete
classes (69 registered block IDs) from
[3A-3.1](PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md)
and [3A-3.2](PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md).
No additional block class was promoted. `TripWireBlock` and
`TripWireHookBlock` remain `REVIEW_PENDING` (2 IDs).

**Evidence tier:** pinned comparative Java source
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1),
plus direct reading of current Planetary source at this checkpoint
(parent Git SHA `bbeeff3c38803f3f22d87705f013af6f9dfd54a4`). Prior class-level registered
method declaring owners came from original compiled NeoForge
reflection [CI artifact 11643813158](https://github.com/Abbygree11/Planetary/actions/runs/37988064055).
Neither source comparison nor reflection validates actual
NeoForge-patched JVM INVOKE bytecode, Mixin application, or gameplay.

## 1. Four coordinate/shape domains, never one universal Direction

| Value or domain | Meaning | Correct consumer |
|---|---|---|
| `sourceLocalDirection` | A cardinal semantic slot on the source block's own canonical Planet face | `NORTH/EAST/SOUTH/WEST` graph flags, `UP` post, a vine/multiface attachment flag |
| `physicalDirection` | Real adjacent step between immutable world BlockPos values | `LevelAccessor.neighborShapeChanged`, `updateShape` callback, `BlockPos.relative(physicalDirection)` |
| `targetLocalSideTowardSource` | **Target** block's canonical side pointing back to source across physical adjacency | Cached `BlockState.isFaceSturdy` and locally canonical support face/shape predicates |
| Shape coordinate frame | `BlockState`-local cached voxel geometry vs the actual world's physical collision/visibility direction | Call-specific shape boundary; never blindly rotate twice or mix canonical support with physical collision |

A seam example, **illustrative not a new physical test**:
a source's local EAST reaches a physically adjacent target.
The physical step may be world UP on a side face; target's
physical reciprocal DOWN may map to target-local SOUTH.
The boolean must be written to source `EAST`, while the
support predicate receives target `SOUTH`.
`physicalDirection == sourceLocalDirection` or
`targetLocalSideTowardSource == sourceLocalDirection.opposite()`
are NOT valid invariants at face seams.

Project contracts already exist:
[`PlanetBlockStateFrame`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockStateFrame.java)
maps `localToWorld`/`worldToLocal` and offers `rotateShape`;
[`PlanetBlockNeighborQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockNeighborQuery.java)
records **all three** directions plus source and target frames
and enforces actual physical adjacency; and
[`PlanetBlockSupportQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockSupportQuery.java)
keeps the target-local support side.
The interfaces are SOURCE-PRESENT, **not proof of every block
family's use of them**.

## 2. Vanilla shape propagation is physical, block flags are semantic

[`BlockBehaviour.BlockStateBase.updateNeighbourShapes`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L825)
iterates `UPDATE_SHAPE_ORDER`, takes **physical**
`sourcePos.relative(direction)`, and calls
`LevelAccessor.neighborShapeChanged(direction.opposite(), state,
targetPos, sourcePos, flags, depth)`.
[`Block.updateFromNeighbourShapes`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/Block.java#L173)
similarly enumerates physical cells and calls
`BlockState.updateShape(direction, neighborState, accessor,
sourcePos, neighborPos)`;
[`BlockStateBase.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L911)
delegates to the block's concrete 6-parameter method.

Consequences:
1. Do **not** globally rotate the callback `Direction`.
   Existing worlds, NeoForge extension hooks and unrelated consumers
   need the physical notification to stay physical.
2. For local graph-state authors: identify the source-local slot
   whose `PlanetBlockNeighborQuery.targetPos()` equals the
   *actual* callback `neighborPos`. Only that slot changes,
   except a class that explicitly recomputes other coupled slots.
3. If a world update changes the **target** frame while source
   remains the same, a graph re-evaluation must respect the
   position-owned canonical chart, not reuse the chart of the
   previous traversal or the player's camera.
4. The caller is allowed to supply a world `LevelAccessor` that
   is not a `Level`: code paths for worldgen / virtual accessors
   and non-Planet terrain must not be silently reclassified.
   The current fence source adapter exits if
   `!(accessor instanceof Level level)`, a **known integration
   scope limitation** rather than proof of runtime error.

Existing [`FenceBlockGravityMixin`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/mixin/FenceBlockGravityMixin.java)
already performs source-local tangent neighbor search, compares
the actual `neighborPos` to each resolved `targetPos`, and
queries target-local sturdy side. It is an **algorithm example for
FenceBlock**, not a reusable safety guarantee for WallBlock,
IronBarsBlock, Gate or MultifaceBlock.
Its `@Inject(at=HEAD, cancellable=true)` returns a locally
rebuilt state, so a later Stage 3C ASM/NeoForge verification
must explicitly account for any patched lifecycle hooks
skipped by cancellation.

## 3. Two levels of shape caching, and why orientation needs policy

### State base cache: isFaceSturdy and collision shapes

Pinned [`BlockBehaviour.BlockStateBase.initCache`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L562)
precomputes `fluidState`, `isRandomlyTicking` and
initializes the state `Cache` when the block does
**not** have `dynamicShape`.
[`Cache`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L1053)
evaluates base `solidRender`, `lightBlock`, occlusion
faces, collision shape, and for **each** `Direction`
and `SupportType` the stable face-sturdy boolean
using `EmptyBlockGetter.INSTANCE` and `BlockPos.ZERO`.
[`BlockStateBase.isFaceSturdy`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L1023)
reads that cached **direction ordinal** when cache exists;
`getCollisionShape` can also return cached geometry
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L764)).
This cache is keyed by `BlockState`, **not planet
position/face**.

Safe initial policy: keep those cached states/booleans in
canonical local BlockState orientation, select the **target
block's local side** before making `isFaceSturdy` calls.
Do NOT attempt to write position-dependent physical faces into
the global state cache; that would cause unrelated chunks/faces
to share invalid orientation. Position-aware shape
queries/rotation belong at an explicit wrapper boundary.
This is a **research design contract**, not an implementation
change or proven end-to-end coverage.

### Class-specific shape lookup tables

- [`CrossCollisionBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CrossCollisionBlock.java#L33) builds
  16 tangent combinations of `getShape` and
  `getCollisionShape`, indexed by four connection bits.
  `FenceBlock`, `IronBarsBlock` and
  `StainedGlassPaneBlock` **share its shape-code owner**,
  but not their connectivity predicates.
- [`WallBlock.makeShapes`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L78) precomputes
  mapping from `BlockState` (UP + 4 `NONE/LOW/TALL`
  sides) to shapes, with **distinct collision heights**;
  `WallBlock.updateShape`/helper
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L251))
  computes additional `WallSide` variants from the
  **above target collision shape's DOWN face**.
- [`MultifaceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L43) caches a
  six-face cover VoxelShape per state, looked up by
  [`getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L155).
  `VineBlock` also caches an `UP` + 4-face shape
  (see [`VineBlock.getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L103)).
  A property/face set can be correct while its physical
  voxel geometry, support or culling is still wrong.

Existing Planetary
[`BlockStateShapeMixin`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/mixin/BlockStateShapeMixin.java)
is registered in `planetary.mixins.json`.
It wraps `getShape`, `getCollisionShape`,
`getVisualShape`, `getInteractionShape` and
`getBlockSupportShape`/`getOcclusionShape`;
[`PlanetBlockShapeRuntime`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockShapeRuntime.java)
keeps a thread-local nesting depth so physical result
rotation occurs only on an **outermost** supported query.
The project deliberately leaves support/occlusion
shape entrypoints **canonical**, rather than physically
rotating the returned support shape.
This distinction can be valid under a target-local support
contract, but all consumers must agree on the **same space**
of both the shape and its Direction argument.
Source presence/registration != runtime acceptance.

### High-priority mixed-frame hazards (not confirmed bugs)

**(A) Multiface support OR collision**:
[`MultifaceBlock.canAttachTo`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L281)
calls BOTH
`target.getBlockSupportShape(getter,targetPos)`
and `target.getCollisionShape(getter,targetPos)`,
then `Block.isFaceFull(shape, direction.opposite())`.
The Planetary shape wrappers treat the first as canonical
and the second as position-aware/physical **at the outer
query**. Vanilla passes the **same** direction enum to
both branches. A new local-frame integration must
specify whether each `VoxelShape` is canonical or
physical *at this call site*, then select the correct
target-local/physical face independently. Passing one
direction unchanged to both may be wrong on non-+Y faces;
do **not** “fix” by globally rotating `BlockState` cache
or blindly rotating both shape objects.

**(B) Wall above collision face**:
[`WallBlock.updateShape` helper](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L251)
takes above target's collision face `Direction.DOWN`
and compares it against canonical source-owned
`POST_TEST/NORTH_TEST/EAST_TEST/...` shapes.
At side/bottom faces the source-local UP cell and
target-facing physical/down faces must be resolved
separately. If the collision shape is returned in
world coordinates, any required projection into the
source's local frame must be handled explicitly;
otherwise a physical-world face shape is compared
to a differently oriented canonical wall test.

**(C) cached sturdy vs rotated collision**:
`BlockStateBase.isFaceSturdy` reads target-local
cache direction; `getCollisionShape` may be physically
rotated at public query. Calling both with a
shared direction without explicit frame type risks
disagreement. A mismatch here is a *candidate issue*
for Phase 2/3C tests, not evidence of a current crash.

**(D) culling**:
Planetary's
[`BlockRenderCullingMixin`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/mixin/BlockRenderCullingMixin.java)
and [`PlanetBlockRenderCulling`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/client/render/PlanetBlockRenderCulling.java)
already map **source and target** local sides for
`skipRendering`, face-hiding, occlusion and
the render-only LRU cache. Do not modify that
cache as a blanket substitute for graph state
or collision correctness. Visual acceptance is
**Phase 3**, while correct connected flag authorship
is a Phase-2 prerequisite.

## 4. Each family has different update invariants

| Family | State change triggered by physical callback | Additional data/shape owner | Phase integration risk |
|---|---|---|---|
| Fence | Re-evaluate one **local tangent** bit, using target face/tag/gate predicate | `CrossCollisionBlock` bit-to-shape | Phase 2 graph; Phase 3 visibility; Phase 5 water tick |
| Pane + stained pane | One tangent via `IronBarsBlock.attachsTo` (panes/walls/sturdy) | Inherited `CrossCollisionBlock`; `IronBarsBlock.skipRendering` | Phase 2 neighbor; Phase 3 connected faces; Phase 5 water |
| Gate | Perpendicular wall neighbor and other side produce `IN_WALL`; interaction may flip `FACING` | Gate `getShape`, `getBlockSupportShape`, `getCollisionShape` | Phase 2 orientation/state; Phase 7A POWERED/OPEN |
| Wall | Side update may recalc all 4 side heights and UP post using above collision face | `WallBlock.makeShapes` with UP/LOW/TALL | Phase 2 graph AND shape-face contract; Phase 3 render/collision integration |
| Vine | Direction-limited face prune; can depend on **vine above** | `VineBlock` cached multi-face geometry | Phase 2 support/shape update, growth; Phase 8 generation |
| Lichen and sculk vein | Each has its own water tick override, then `MultifaceBlock.updateShape` removes unsupported one face | `MultifaceBlock` 6-face cache + SpreadPos authors | Phase 2 six-face support; Phase 5 fluid tick; Phase 8 spreading/worldgen |

**P26 property nuance:** `VineBlock` offers
`UP` and 4 horizontal bits but **no DOWN**;
`MultifaceBlock` supports all 6 face properties.
Do not infer absence of spatial semantics from an
absent `FACING` property, and do not conflate the
`VineBlock` fallback support with the stricter
`MultifaceBlock` all-present-faces survival rule.

### Execution split / future test matrix (not executed)

1. Each of the six Planet faces **+Y, -Y, +X, -X, +Z, -Z**:
   author same graph/shape state by item placement; compare
   physical block collision and target-local sturdiness.
2. Cross a **face edge** and each **corner**:
   source-local tangent property remains stable,
   callbacks map by **actual neighborPos**;
   target-local support side must face back at source.
3. Fence next to wood/nether compatible fences and
   gates, pane next to wall, wall below slab/fence
   or a block with nontrivial collision face.
   Validate `NONE/LOW/TALL/UP` including above.
4. Vine keep-one-face/remove-last-face, lichen/sculk
   add/remove one of six faces, WATERLOGGED preservation
   and bonemeal/spreader bypass; support vs collision
   OR predicate (test support-only, collision-only,
   neither, both).
5. Gate manually toggle/open from opposite side,
   POWERED signal changes, water fluid ticks,
   vanilla non-Planet world equivalent behavior.
6. Render culling, light/occlusion, collision and
   ray-interaction shapes must each be checked
   independently; screenshots alone do not prove
   collision/support behavior.
7. Verify `LevelAccessor` alternate worldgen/virtual
   contexts, no cancellation of NeoForge patched
   extension points and correct bytecode handler
   matching **before** implementation acceptance.

## 5. Boundaries and status

**Phase 2:** state property authors, source/target
face mapping, update invalidation, coherent graph
support and cell write semantics.
**Phase 3:** visual/culling, geometric representation
and render-only caches. **Phase 5:** actual liquid
evolution, while Phase 2 must preserve waterlogging
and fluid tick notifications. **Phase 7A:** gate
signal/OPEN/POWERED graph. **Phase 8:** feature-based
lichen/sculk growth placement.

No new compiled class census or gameplay test ran.
No Java/Mixin source modification. All 8 class
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance` statuses remain
`REVIEW_PENDING`.

**Cumulative ledger unchanged:** 55/241 source+reflection
reviewed (162/1060 registered IDs),
186/241 `REVIEW_PENDING` (898/1060 IDs).
TripWire and TripWireHook still pending.

**Next FIRST independent microtask:** Stage `2.3A-3.4`,
final class/status/metadata reconciliation against
the original CI artifact, then a separately scoped
next class-family card; do not prematurely declare
full Stage 3A or Phase 2 accepted.
