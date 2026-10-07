# Fluid mechanism matrix — Minecraft 1.21.1 / NeoForge 21.1.215

Status: R3 COMPLETE research result for Planetary 2.0.

This is the concrete Phase-5 ownership map produced by global research batch R3.
It refines the earlier baseline in `FLUIDS_1_21_1.md`.

Primary source families audited:
- vanilla FlowingFluid / Fluid / FluidState;
- LiquidBlock / LiquidBlockContainer / SimpleWaterloggedBlock;
- BucketItem / BucketPickup;
- WaterFluid / LavaFluid;
- BubbleColumnBlock / SoulSandBlock / MagmaBlock;
- PointedDripstone fluid lookup / cauldron integration;
- Entity fluid-height/current path;
- LiquidBlockRenderer;
- NeoForge 1.21.1 FlowingFluid/LiquidBlock/Bucket/Entity/render patches;
- NeoForge FluidType / BaseFlowingFluid / FluidInteractionRegistry /
  IFluidStateExtension / IClientFluidTypeExtensions.

No runtime implementation is performed by R3.

---

## 1. Fundamental model

A Planet fluid step has TWO different direction meanings which vanilla stores in
one Direction value:

1. semantic LOCAL direction
   - local DOWN/UP;
   - local tangent NORTH/SOUTH/WEST/EAST;
   - used by fluid behavior such as falling, source conversion and mod fluid
     methods which compare against Direction.DOWN;

2. PHYSICAL world edge direction
   - actual adjacent BlockPos face crossed;
   - used for collision/occlusion geometry between source and target cells.

These values are equal only on +Y vanilla gravity.

Therefore the central Phase-5 type is a fluid graph edge backed by
`PlanetBlockStep`:

    source frame/context
    semantic local direction
    physical direction
    physical target BlockPos/context
    transported direction for recursive continuation

Do not pass one Direction through all vanilla fluid checks.

---

## 2. PlanetBlockStep is the topology foundation

Existing `PlanetBlockStep` already provides:
- source chart;
- requested local direction;
- physical direction;
- target chart/position;
- transported direction;
- seam-crossing flag.

Therefore Phase 5 does NOT need a second topology implementation.

Preferred architecture:

    PlanetBlockStep
        -> PlanetFluidEdge / PlanetFluidGraphContext
            -> 1.21.1 FlowingFluid adapter

PlanetFluidEdge may additionally cache:
- source/target BlockState;
- source/target FluidState;
- canonical target side toward source where needed.

At exact edge/corner:
- physical-cell counting/mutation/scheduling deduplicates by physical BlockPos;
- recursive chart-aware traversal may preserve (BlockPos, face/chart) because
  continuation direction is path-dependent.

Preserve stable vanilla direction iteration order before deduplication.

---

## 3. Phase 5A — FlowingFluid graph

### 3.1 Methods with gravity semantics

The following FlowingFluid operations are one graph family:

- getFlow;
- spread;
- spreadToSides;
- getSpread;
- getSlopeDistance;
- getNewLiquid;
- sourceNeighborCount;
- isWaterHole;
- canSpreadTo;
- canPassThroughWall integration.

They must be adapted coherently.

### 3.2 Local DOWN

Vanilla assumptions:
- pos.below();
- Direction.DOWN;
- candidate.below();
- falling column semantics.

Planet:
- source.step(local DOWN);
- use PHYSICAL target for world access;
- retain LOCAL DOWN as semantic direction passed into fluid behavior.

This distinction is required because:
- WaterFluid/BaseFlowingFluid.canBeReplacedWith compares Direction.DOWN;
- LavaFluid.spreadTo has special behavior when direction == DOWN.

Passing the physical world direction here would break rotated-face behavior.

### 3.3 Local UP

Vanilla assumptions:
- pos.above();
- same fluid above;
- incoming FALLING column.

Planet:
- source.step(local UP).

Used by:
- getNewLiquid falling-state creation;
- getHeight / full-column detection;
- renderer top-neighbor logic;
- bubble/drip integration.

### 3.4 Four local tangent neighbors

Vanilla uses Direction.Plane.HORIZONTAL for:
- side spread;
- source-neighbor counting;
- flow gradient;
- slope search;
- hole search.

Planet:
- source.tangentSteps();
- recurse with transported direction after seam crossing.

Do not use physical world horizontal plane.

### 3.5 Flow vector contract

FluidState.getFlow should remain a PHYSICAL world-space vector.

Algorithm:
- compute gradient contributions over local tangent edges;
- each local tangent contribution points through that edge's physical direction;
- falling contribution uses physical local DOWN;
- normalize following vanilla order/numeric behavior.

This keeps FluidState as a world-fluid API.

Entity code must convert the resulting physical vector at the entity/body
boundary if its stored velocity representation is local.

### 3.6 Source creation and NeoForge event semantics

Exact NeoForge 1.21.1 behavior:
- neighboring source cells are counted only when
  EventHooks.canCreateFluidSource(level, neighborPos, neighborState) permits it;
- CreateFluidSourceEvent defaults to
  FluidState.canConvertToSource(level, pos);
- BaseFlowingFluid delegates that to FluidType.canConvertToSource.

Planet must preserve this event-driven source counting exactly.

Do NOT restore the older vanilla-only:
    canConvertToSource(level) && sourceCount >= 2

Source support check:
- local DOWN physical target;
- target solid or same-fluid source.

### 3.7 Physical neighbor notifications remain physical

After a fluid state mutation:
- scheduled tick remains at the physical cell;
- Level.updateNeighborsAt remains ordinary physical neighbor fan-out.

Do not rotate generic world notification machinery.

---

## 4. FlowingFluid caches are NOT frame-complete

R3 found two critical vanilla cache assumptions.

### 4.1 Slope cache key

FlowingFluid.getCacheKey stores only:

    delta X
    delta Z

as one short.

On a rotated face, local tangent traversal can change physical Y.
Across seams this key no longer identifies the graph node.

Planet rule:
slope/cache identity must use topology-aware state, for example:
- physical BlockPos for physical-cell caches;
- (BlockPos, traversal face/chart) where continuation is chart-sensitive.

Do not reuse vanilla XZ short key.

### 4.2 Wall-occlusion cache

Vanilla OCCLUSION_CACHE key:
- source BlockState;
- target BlockState;
- one Direction.

Planet block collision shapes are position/frame dependent.
The same state pair can have different physical orientation on different faces.

Therefore vanilla key is incomplete.

A Planet fluid wall cache must include enough frame information, such as:
- source state + source frame;
- target state + target frame;
- physical edge direction.

Alternatively bypass caching for rotated cases until a correct bounded cache is
implemented.

Do not use the vanilla cache with canonical directions.

---

## 5. Fluid wall/shape passage

FlowingFluid.canPassThroughWall evaluates collision geometry with
Shapes.mergedFaceOccludes.

For Planet:
- source/target collision shapes must be PHYSICAL world shapes;
- mergedFaceOccludes receives the PHYSICAL edge direction.

This is deliberately different from:
- canBeReplacedWith/spreadTo semantic Direction, which is LOCAL.

At a seam, source local side and target local side are not assumed opposite in
canonical state-space.

The fluid graph edge is the correct owner of this conversion.

---

## 6. Phase 5B — FluidState height and shape

### 6.1 Scalar amount remains canonical

Fluid amount/LEVEL semantics do not change:
- getAmount;
- getOwnHeight;
- source/flowing state.

They represent scalar fill height in the fluid's LOCAL vertical frame.

### 6.2 getHeight uses local UP

Vanilla FlowingFluid.getHeight returns full height if same fluid exists
pos.above().

Planet:
- same-fluid check at local UP target.

### 6.3 Fluid shape

Vanilla FlowingFluid.getShape creates:
    box(0,0,0, 1,height,1)

This is canonical-local fluid geometry.

Preferred boundary:
- keep canonical shape generation/cache canonical;
- rotate the outer FluidState.getShape result local->physical by position/frame,
  analogous to block shape handling.

Do not store position-specific rotated shapes in the FluidState/FlowingFluid
state cache.

Full-block identity may stay Shapes.block when physically full.

### 6.4 LiquidBlock stable-surface collision

LiquidBlock.getCollisionShape uses:
- CollisionContext.isAbove;
- source level;
- fluid above;
- canStandOnFluid.

Those are world-Y/entity-surface assumptions.

Ownership:
- Phase 5 defines the physical local fluid surface;
- Phase 7 entity/body integration defines whether the entity is above/standing
  on that local surface.

Do not solve this with a generic block-shape rotation alone.

---

## 7. Phase 5B2 — buckets and waterlogging

### Bucket placement/pickup

BucketItem raycasts a physical face and uses:
    hitPos.relative(hitDirection)

That is correct physical interaction geometry.

Do NOT rotate the clicked face merely because gravity is local.

Bucket fluid placement then operates at the resolved physical target.

NeoForge bucket hooks to preserve:
- FluidType placement state/block;
- vaporization-on-placement;
- fluid-specific sounds;
- container ItemStack path.

### LiquidBlockContainer / SimpleWaterloggedBlock

These mostly operate at the SAME BlockPos:
- canPlaceLiquid;
- placeLiquid;
- pickup;
- WATERLOGGED state;
- schedule fluid tick.

No independent gravity transform is inherent.

Gravity-sensitive behavior belongs to the containing block's own support/update
algorithm and to FlowingFluid once the fluid tick runs.

---

## 8. Phase 5B3 — lava/water and mod fluid interactions

There are TWO different interaction paths.

### 8.1 Local-DOWN interaction during spread

LavaFluid.spreadTo special-cases Direction.DOWN and water target to create stone.

Planet:
- target is physical local-DOWN step;
- semantic argument passed to LavaFluid behavior remains LOCAL DOWN.

### 8.2 Surrounding source interaction

NeoForge 1.21.1 replaces LiquidBlock.shouldSpreadLiquid with
FluidInteractionRegistry.canInteract.

Registry vanilla/Neo assumptions:
- LiquidBlock.POSSIBLE_FLOW_DIRECTIONS;
- effectively local-UP + four tangent surroundings, excluding local DOWN;
- built-in basalt predicate additionally calls currentPos.below().

Planet:
- enumerate local UP + local tangents through Planet topology;
- deduplicate physical targets while preserving order.

Compatibility hotspot:
FluidInteractionRegistry predicates receive raw BlockPos values and arbitrary
mod predicates may call below()/above()/relative() themselves.

Built-in basalt also hard-codes currentPos.below().

Therefore:
- standard registered interaction enumeration can be topology-adapted;
- built-in NeoForge basalt DOWN check needs an explicit local-DOWN adaptation;
- arbitrary custom predicate internals using raw axes are Phase-10 compatibility
  territory unless NeoForge exposes a semantic-direction context in a future
  version.

Do not globally redefine BlockPos to fix these predicates.

---

## 9. Lava fluid-owned side effects

### animateTick

Lava pop particle origin is at world above surface.

Planet:
- source position uses local UP surface;
- particle class motion remains Phase 4;
- emitter ownership is Phase 5 -> Phase 4 integration.

Preserve exact RNG order.

### randomTick fire ignition

Vanilla candidate generation uses:
- random X/Z offsets;
- +1 world Y;
- another branch uses tangent random position then above().

Planet semantics:
- random X/Z become local tangent offsets;
- +1 becomes local UP step.

The subsequent "any flammable neighbor" check loops all six adjacent physical
cells and is ordinary adjacency; it does not itself need gravity rotation.

Physical build-height checks remain physical after the semantic target is
resolved.

---

## 10. Phase 5D — bubble column family

BubbleColumnBlock is a fluid topology mechanism, not merely a particle effect.

Vanilla assumptions:
- column source is below;
- propagation walks Direction.UP;
- survival checks below;
- SoulSand/Magma trigger update above;
- above-surface detection checks block above;
- bubble particles use world-UP offsets/velocity;
- entity impulse modifies velocity.y only.

Planet ownership:

Phase 5:
- local-DOWN source block;
- propagate column local UP;
- local-UP surface detection;
- scheduling/update semantics;
- bubble/current particle SOURCE geometry.

Phase 4:
- particle-class motion/render once emitted.

Phase 7:
- onAboveBubbleCol/onInsideBubbleColumn local vertical entity impulse.

DRAG_DOWN remains a semantic scalar:
- true = local DOWN drag;
- false = local UP lift.

---

## 11. Dripstone / cauldron fluid integration

PointedDripstone block-chain topology is primarily Phase 2.

Fluid integration still has hard-coded vertical assumptions:
- fluid above stalactite root;
- search from cauldron upward to stalactite;
- drip path downward;
- source water/lava identification;
- particle source.

Phase split:

Phase 2:
- local vertical dripstone chain and tip topology.

Phase 5:
- fluid source lookup relative to local stalactite UP;
- cauldron-to-tip local vertical search;
- fluid identity/source behavior.

Phase 4:
- drip particle motion/emission visuals.

Cauldron block behavior remains physical at its resolved target position.

---

## 12. Phase 5E / Phase 7 integration — entity fluid contact and current

### 12.1 Vanilla/NeoForge current scanner is world-Y

NeoForge Entity.updateFluidHeightAndDoFluidPushing generalizes vanilla to
FluidType, but still computes immersion with:

    blockY + fluidState.getHeight(...)
    versus AABB.minY

and stores one depth per FluidType.

This is invalid for side-face local vertical.

### 12.2 Required split

Phase 5 provides:
- physical fluid surface geometry;
- physical FluidState.getFlow vector;
- fluid type/state.

Phase 7 provides:
- entity/body frame;
- local bottom/top/eye relation to fluid surface;
- immersion depth;
- conversion of physical fluid push into the entity's velocity representation.

### 12.3 Preserve NeoForge FluidType hooks

Do not replace the NeoForge system with water/lava-only logic.

Preserve:
- FluidType.motionScale;
- canPushEntity;
- canSwim;
- canDrownIn;
- canExtinguish;
- fall-distance modifier;
- FluidType.move custom living movement;
- setItemMovement;
- supportsBoating;
- fluid path types.

Ownership:
- surface/contact measurement -> Phase 7 using Phase-5 geometry;
- custom movement/boats/items -> Phase 7;
- path type -> Phase 6/R5 AI;
- fluid graph remains Phase 5.

Custom FluidType.move implementations with raw world-axis assumptions are an
explicit compatibility gate.

---

## 13. Phase 5C — LiquidBlockRenderer

LiquidBlockRenderer bypasses BakedModel/PoseStack and emits its own mesh.

It hard-codes:
- world DOWN/UP;
- four world-horizontal neighbors;
- world-XZ diagonal corner samples;
- world-Y surface heights;
- atan2(flow.z, flow.x);
- world-Y light sample above;
- world-direction side shading;
- world-oriented vertex positions/normals.

Therefore it needs a dedicated fluid mesh boundary.

### 13.1 Preferred render algorithm

Author the fluid mesh in canonical LOCAL cell coordinates:

1. resolve local UP/DOWN + four tangent neighbors;
2. calculate four local surface corner heights;
3. use local tangent components of physical flow for flow-texture UV angle;
4. classify visible local faces;
5. evaluate neighbor occlusion using the physical shared edge / target frame;
6. emit local vertices;
7. transform positions/normals to physical world orientation.

Do NOT rotate a finished vanilla mesh after the fact if its neighbor sampling
was already wrong.

### 13.2 Face occlusion

LiquidBlockRenderer has its own fluid-face occlusion code.

At a seam:
- source local fluid face;
- physical shared face;
- target canonical block face

must be distinguished.

NeoForge BlockState.shouldHideAdjacentFluidFace must receive the target side in
the semantic convention expected by the target block adapter, not a blindly
reused source-local Direction.

### 13.3 Lighting

Renderer getLightColor samples current + world above.

Fluid surface light sampling should use the physical cell reached by LOCAL UP.

Directional getShade policy remains the R6 environment decision established by
R2.

Do not mix surface-neighbor selection with the global shade policy.

### 13.4 NeoForge render extensions to preserve

Exact NeoForge 1.21.1 uses:
- FluidSpriteCache / fluid still+flow+overlay sprites;
- IClientFluidTypeExtensions tint;
- BlockState.shouldDisplayFluidOverlay /
  shouldHideAdjacentFluidFace.

Preserve them.

IClientFluidTypeExtensions declares renderFluid in this branch, but R3 found no
1.21.1 SectionCompiler/LiquidBlockRenderer wiring that replaces vanilla
tessellation through that method. Treat that as a cross-version portability
hotspot: re-audit on newer NeoForge versions.

---

## 14. NeoForge/modded fluid compatibility contract

### Strong inheritance boundary

NeoForge BaseFlowingFluid extends FlowingFluid.

Therefore a correct shared FlowingFluid graph adapter should automatically cover
most conventional mod fluids.

Preserve virtual/extension calls for:
- getSlopeFindDistance;
- getDropOff;
- getTickDelay;
- getFlowing/getSource;
- canConvertToSource;
- canBeReplacedWith;
- spreadTo overrides;
- beforeDestroyingBlock;
- FluidType/source events.

Do not detect only WaterFluid/LavaFluid.

### Registry/event boundaries

Preserve:
- CreateFluidSourceEvent;
- FluidInteractionRegistry;
- FluidType;
- IFluidStateExtension;
- IClientFluidTypeExtensions;
- bucket vaporization/sounds;
- overlay/hide-adjacent hooks.

### Compatibility limitations

Third-party code that directly uses raw:
- pos.below()/above();
- Direction.Plane.HORIZONTAL;
- world-Y height math;
- custom fluid renderer raw vertices;
- custom FluidInteractionRegistry predicate axes;
- custom FluidType.move world-axis math

may require Phase-10 integration.

Planet should expose reusable topology/frame APIs rather than patching every mod.

---

## 15. Implementation architecture and portability

FlowingFluid has many private methods and hard-coded loops.

A large collection of ordinal/locals redirects would be fragile.

Preferred direction:

    stable Planet topology/math
        PlanetFluidEdge / PlanetFluidGraph
            -> localized Minecraft 1.21.1 FlowingFluid runtime adapter
                -> thin Mixin entrypoints

The semantic graph/core should own:
- local DOWN/UP/tangent enumeration;
- seam continuation;
- physical-cell dedup;
- topology-aware slope search/cache;
- physical flow vector construction.

The 1.21.1 adapter owns:
- exact vanilla state transition ordering;
- virtual Fluid/FluidType calls;
- NeoForge events;
- scheduling/block mutation details.

Because vanilla FlowingFluid hides much of the graph in private methods, some
version-sensitive mirrored control flow may be unavoidable.

If so:
- isolate it in one explicit 1.21.1 runtime adapter;
- do not spread copied vanilla code through mixins;
- add +Y vanilla-equivalence tests;
- document exact mirrored methods;
- re-audit only that adapter on port.

---

## 16. Performance requirements

Fluid simulation is a hot scheduled-tick graph.

Requirements:
- no unbounded global per-cell caches;
- per-tick slope caches are bounded/local;
- reuse PlanetBlockStep/frame context inside one graph traversal;
- dedup corner physical cells without allocating large sets where avoidable;
- preserve source/tick ordering;
- no duplicate vanilla + Planet spread;
- frame-aware occlusion cache must be bounded/thread-safe in the same spirit as
  vanilla thread-local cache;
- renderer should avoid per-vertex frame lookup.

---

## 17. Deterministic test matrix

### Topology
All six faces:
- local DOWN;
- local UP;
- four tangent edges;
- seam transport;
- 24 directed edge transitions;
- corner physical dedup;
- chart-aware recursive continuation.

### Flowing graph
+Y exact vanilla equivalence:
- one source open;
- downward fall;
- tangent spread;
- blocked wall;
- slope hole search;
- source-neighbor count;
- falling state;
- decay/drop-off;
- scheduled state replacement.

Rotated:
- same cases on +/-X, +/-Z, -Y.

### Caches
- slope search with physical-Y tangent movement has no XZ-key collision;
- wall cache differentiates frame/orientation;
- no duplicate corner source count.

### Source creation
- NeoForge CreateFluidSourceEvent default;
- event forced allow/deny;
- BaseFlowingFluid FluidType canConvertToSource.

### Interaction
- lava local-DOWN into water;
- water/lava surrounding interaction;
- basalt local-DOWN soul soil + blue ice;
- one custom FluidInteractionRegistry interaction.

### Shape
- own height;
- same fluid local UP => full height;
- physical rotated VoxelShape;
- +Y identity.

### Bucket/waterlogging
- physical clicked-face placement;
- same-cell waterlogging;
- pickup;
- vaporization hook.

### Bubble/drip
- bubble column local UP propagation;
- drag state;
- drip source local UP;
- cauldron vertical search.

### Renderer pure geometry
- four corner heights in local tangent square;
- flow UV angle from local tangent components;
- top/bottom/four side vertex transform all six faces;
- normals;
- seam occlusion side mapping;
- local-UP light sample;
- NeoForge sprite/tint/overlay callbacks preserved.

---

## 18. Single Phase-5 runtime acceptance matrix

Do not test individual fluid patches one by one.

One batch acceptance should cover:

1. +Y vanilla baseline.
2. source placed on each rotated face.
3. waterfall follows local DOWN.
4. spread covers four local tangent directions.
5. continuous edge wrap.
6. exact edge/corner no duplication/loss.
7. blocked side and partial collision shape.
8. slope search finds local-DOWN hole.
9. two-source infinite-water behavior.
10. incoming local-UP falling column.
11. waterlogged slab/stair representative.
12. bucket place/pickup.
13. lava local fall/spread.
14. lava-water stone/obsidian/cobblestone interactions.
15. basalt interaction.
16. bubble column local-UP propagation.
17. dripstone -> cauldron/source integration.
18. entity current physical direction after Phase-7 integration.
19. fluid surface shape/local height.
20. renderer top/side/corner/flow texture on rotated faces.
21. NeoForge modded BaseFlowingFluid representative.
22. custom source-conversion event allow/deny.
23. reload/scheduled-tick determinism.
24. performance smoke test on a substantial flowing volume.

Entity immersion/body, custom FluidType.move, boats and swimming receive their
final acceptance in Phase 7 while reusing the Phase-5 fluid geometry.

---

## 19. R3 completion decision

R3 is COMPLETE when:
- FlowingFluid is classified as one topology graph;
- semantic and physical directions are explicitly separated;
- slope/occlusion cache defects are accounted for;
- FluidState height/shape ownership is explicit;
- buckets/waterlogging are classified;
- NeoForge source/interaction/FluidType hooks are preserved;
- bubble/drip/lava side effects are assigned;
- entity-current integration gate is explicit;
- LiquidBlockRenderer has a dedicated local-mesh architecture;
- deterministic and batch acceptance matrices are defined.

Further runtime implementation remains frozen until R4-R6 and the final global
completeness sweep are complete.
