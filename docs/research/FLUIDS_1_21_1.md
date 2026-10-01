# Research: Minecraft 1.21.1 / NeoForge 21.1.215 fluid pipeline

Status: research baseline. Do not implement the full fluid phase until the
Phase-1 local block/topology kernel exists.

Primary source inspected:
NeoForm-unpacked Minecraft 1.21.1 sources (official Mojang mappings), including
FlowingFluid, LiquidBlock, FluidState, LiquidBlockRenderer, ClientLevel and
BucketItem.

## 1. Server simulation call flow

A fluid tick ultimately calls FlowingFluid.tick(level, pos, state).

FlowingFluid.tick:
1. For a non-source state, calls getNewLiquid(level, pos, blockState).
2. If the state changes, replaces the legacy block, schedules the next fluid
   tick and updates neighbors.
3. Calls spread(level, pos, state).

This means direction adaptation cannot live only in LiquidBlock. The actual
simulation decisions are inside FlowingFluid.

## 2. World-axis assumptions inside FlowingFluid

### getFlow
- iterates Direction.Plane.HORIZONTAL
- neighbor position is pos + horizontal direction
- when neighbor is empty, checks neighbor.below()
- falling flow adds the literal vector (0, -6, 0)
- returned Vec3 is a WORLD vector used by entity/fluid movement

Planet requirement:
- iterate four local tangent directions
- local-down neighbor for lower-fluid sampling
- falling vector is physical frame.worldDown
- tangent contribution must be mapped to physical vectors

### spread
Vanilla:
- chooses pos.below()
- attempts Direction.DOWN first
- otherwise spreads to sides

Planet requirement:
- choose seam-aware local DOWN
- then four seam-aware local tangent neighbors

### spreadToSides / getSpread
Vanilla:
- side directions are Direction.Plane.HORIZONTAL
- candidate position is pos.relative(direction)
- checks candidate.below() for a water hole
- recursively computes slope distance in the XZ plane

Planet requirement:
all of those operations use local tangent/down topology. At a cube edge, a
local tangent step is NOT necessarily raw BlockPos.relative(worldDirection).

### getNewLiquid
Vanilla:
- scans Direction.Plane.HORIZONTAL for same-fluid neighbors
- counts source neighbors for source creation
- source creation checks pos.below() as support
- falling state is determined from pos.above() and Direction.UP

Planet requirement:
- four local tangent neighbors
- local DOWN support
- local UP incoming falling column
- preserve NeoForge EventHooks.canCreateFluidSource call exactly

### getSlopeDistance
Vanilla:
- recursive search over Direction.Plane.HORIZONTAL
- excludes the direction it came from
- each candidate checks candidate.below()
- bounded by getSlopeFindDistance(level)

Planet requirement:
same algorithm and same bound, but local tangent directions are transported
through every seam. The previous direction must also be transported when the
frame changes at an edge.

### isWaterHole
Vanilla uses canPassThroughWall(Direction.DOWN, ...).

Planet requirement:
use the local-down physical side for the current cell.

### sourceNeighborCount
Vanilla counts sources only in Direction.Plane.HORIZONTAL.

Planet requirement:
count the four local tangent neighbors.

### getCacheKey
Vanilla encodes only X/Z deltas into a short because slope search is planar.

Planet problem:
this cache key is invalid once the search can rotate across a cube edge and use
world Y. A Planet fluid slope search needs a seam-aware local search key
(relative topological coordinates or a full BlockPos/local chart key) while
keeping the search bounded.

## 3. Shapes and height

FlowingFluid.hasSameAbove uses pos.above().
getHeight is full height if the same fluid is above.
getShape constructs Shapes.box(0,0,0,1,height,1), so height always grows +Y.

Planet requirement:
- same-fluid check is local UP
- canonical fluid shape may remain local-Y, then rotate through the shared
  VoxelShape local-frame adapter
- cache by (FluidState, PlanetFace) or rotate one cached canonical shape into
  one of six frame variants

Do not special-case water shape separately from the general shape layer.

## 4. Occlusion and block shapes

canPassThroughWall compares two collision VoxelShapes using
Shapes.mergedFaceOccludes(..., direction).

Planet requirement:
- direction passed here is the PHYSICAL face being crossed
- collision shapes returned for Planet blocks must already be physical rotated
  shapes, or the comparison must be run in a canonical local view
- this is why the block-shape layer is a prerequisite

The 200-entry OCCLUSION_CACHE key includes (state, spreadState, direction).
If block states are local semantics, physical frame also matters. Reusing this
cache without frame in the key can return an answer computed for a different
orientation.

Therefore either:
- bypass vanilla cache for non-POS_Y Planet frames, or
- introduce a frame-aware cache key.

## 5. LiquidBlock

LiquidBlock is the block representation/scheduling front-end around FluidState.
It schedules fluid ticks when placed/updated and handles liquid block behavior.

Rule:
reuse vanilla/NeoForge tick scheduling. Do not create an independent Planet
fluid scheduler. Adapt neighbor directions/queries at the simulation boundary.

## 6. Waterlogging / LiquidBlockContainer

FlowingFluid.spreadTo preserves the LiquidBlockContainer path:
- if target block implements LiquidBlockContainer, call placeLiquid
- otherwise destroy replaceable block as vanilla does and set legacy fluid block

Planet implementation must preserve this dispatch. Local gravity changes which
neighbor is targeted, not how a target container accepts a fluid.

SimpleWaterloggedBlock and waterlogged block update scheduling must be included
in Phase-2 block-neighbor research.

## 7. Bucket placement and pickup

BucketItem selects a physical block hit by raycast and uses the hit Direction
to choose the adjacent placement position. It then delegates to:
- BucketPickup
- LiquidBlockContainer
- FlowingFluid source block placement

The hit face itself is a physical interaction and generally should remain
physical. Gravity primarily affects what the placed fluid does AFTER placement.

Research item:
verify player raycast/hit Direction is already correct under rotated camera and
whether any UI/local semantic translation is needed for special containers.

## 8. Entity flow vector

FlowingFluid.getFlow returns a world Vec3.
Vanilla computes it from world X/Z slopes and adds world -Y for falling fluid.

Planet requirement:
compute in canonical local coordinates, then localToWorld through the current
fluid cell frame. Do not store this vector as local entity deltaMovement without
respecting the existing entity movement boundary.

## 9. Client fluid rendering

LiquidBlockRenderer is strongly Y-up:
- top face is the +Y face
- bottom face is -Y
- side loop is Direction.Plane.HORIZONTAL
- per-corner heights are interpreted as Y offsets
- vertex coordinates add height to Y
- normals are emitted as (0,1,0)
- same-fluid-above checks use pos.above()
- lighting samples pos.above()/below()

Planet requirement:
prefer one canonical local fluid mesh and transform vertices/normals to the
physical Planet frame, rather than rewriting six versions of the height math.

The renderer must also query seam-aware local neighbors for corner heights.
A pure PoseStack rotation is not enough if the neighbor samples are still
global XZ/Y.

## 10. Ambient/drip particles

ClientLevel.doAnimateTick:
- gets FluidState.getDripParticle()
- tests block face sturdy on Direction.DOWN
- passes blockPos.below() into trySpawnDripParticles

trySpawnDripParticles:
- inspects collision shape along Axis.Y
- emits at Y min/max positions

ParticleUtils.spawnParticleBelow likewise emits at pos.y - 0.05.

Planet requirement:
drip SOURCE placement uses the local-DOWN block face and rotated shape.
Particle acceleration is a separate concern.

Previous ClientLevelFluidDripGravityMixin was disabled because a fragile
Redirect failed at runtime. Do not re-enable it. Reimplement this only as part
of the full fluid/client-frame patch.

## 11. Falling dust is not normal Particle.gravity

FallingDustParticle.tick independently:
- move(xd,yd,zd)
- yd -= 0.003
- yd = max(yd, -0.14)

Therefore the generic Particle.tick gravity hook cannot fix it.

Also FallingBlock.animateTick checks pos.below() and calls
ParticleUtils.spawnParticleBelow, so both origin and acceleration must be
adapted.

## 12. NeoForge compatibility constraints

Preserve NeoForge hooks already inside vanilla code, including
EventHooks.canCreateFluidSource.

NeoForge sided block fluid handlers are capability-based and separate from
world FluidState simulation. Existing Planetary BlockCapability direction
adaptation should remain compatible with IFluidHandler-style sided queries.

Do not assume every modded fluid is exactly WaterFluid/LavaFluid. Adapt at the
FlowingFluid / FluidState / rendering boundaries where practical.

## 13. Edge topology: unresolved design gate

This is the most important prerequisite.

For a cell on/near a convex cube edge, local EAST/WEST/NORTH/SOUTH cannot be
implemented by blindly converting a local direction once and calling
pos.relative(worldDirection).

The next local cell may require:
- crossing into a new gravity chart,
- rotating the local basis,
- transporting the 'came from' direction for slope recursion,
- choosing a unique physical cell so fluid is neither duplicated nor lost.

Before fluid implementation, Phase 1 must provide and test:
PlanetBlockFrameContext.step(localDirection) -> target physical BlockPos +
target frame + transported local direction.

The operation must be deterministic for all 24 directed cube-edge transitions.

### Exact-corner deduplication rule

A three-face cube corner is a special discrete topology case. Four local
tangent directions exist in the preferred chart, but the two outward tangent
steps converge onto one physical BlockPos while carrying different target
PlanetFace values.

Fluid code must distinguish two use cases:
- physical-cell operations such as source-neighbor counting, block mutation and
  tick scheduling must deduplicate by physical BlockPos so one cell is not
  counted or scheduled twice;
- chart-aware recursive traversal such as slope search may need to preserve
  (BlockPos, PlanetFace) as the search state, because the same physical cell
  reached through two corner routes can have different transported directions
  and therefore different continuations.

Do not blindly convert tangentSteps().size() into a physical neighbor count at
a corner.

## 14. Fluid acceptance matrix

At minimum test each on POS_Y, NEG_Y, one X face, one Z face, and an edge:
- one source in open air
- source above solid local floor
- vertical local-down waterfall
- four local tangent spread directions
- blocked side / partial collision shape
- slope search toward a local-down hole
- two-source infinite water formation
- falling column from local UP
- waterlogged slab/stair
- water/lava interaction
- bucket place/pickup
- entity flow vector
- client mesh orientation and corner heights
- drip particle source + falling direction
- reload and scheduled-tick determinism
- modded FlowingFluid smoke test
