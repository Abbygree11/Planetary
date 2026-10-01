# Research: local block frame for Minecraft 1.21.1

Status: architecture baseline for roadmap Phases 1-3.
No broad runtime mixin should be added until the pure transforms and caches below
are tested.

Sources inspected:
- BlockPlaceContext
- StandingAndWallBlockItem
- BlockBehaviour / BlockStateBase cache
- PointedDripstoneBlock
- BlockRenderDispatcher
- ModelBlockRenderer
- BakedModel / BakedQuad
- existing Planetary PlanetVanillaDirection / PlanetBlockTopology /
  PlanetSidedQueryFrame

## 1. Semantic rule

In a Planet world, ordinary vanilla BlockState directional values are LOCAL.

Examples:
- Direction.UP stored in a vertical property means local UP.
- PointedDripstone TIP_DIRECTION=DOWN means local DOWN.
- horizontal FACING values mean local NORTH/SOUTH/WEST/EAST.
- axis Y means local vertical axis.

The physical world remains ordinary XYZ BlockPos. The adapter layer converts
between state-local semantics and physical directions at engine boundaries.

Do NOT rewrite registered BlockState property domains to six new meanings.
That would break vanilla and mod model/state compatibility.

## 2. Existing reusable Planetary code

PlanetVanillaDirection already provides:
- localToWorld(frame, Direction)
- worldToLocal(frame, Direction)
- transformAcrossEdge(...)

PlanetBlockTopology / PlanetTopology / FaceTransform already encode directed
cube-edge transport in a face-local chart.

PlanetSidedQueryFrame already proves the pattern for NeoForge Direction-context
capabilities: reinterpret side context without requiring knowledge of the mod
that owns the machine/pipe.

Phase 1 should unify these concepts into one runtime block-frame context rather
than create another independent direction system.

## 3. Placement context

BlockPlaceContext constructor uses the physical BlockHitResult direction to
compute relativePos. This is GOOD: the actual targeted adjacent physical block
must remain physical.

But orientation helpers are global:
- getNearestLookingDirection -> Direction.orderedByNearest(player)
- getNearestLookingVerticalDirection -> Direction.getFacingAxis(player, Axis.Y)
- getNearestLookingDirections -> global player/world directions

StandingAndWallBlockItem uses getNearestLookingDirections to choose standing
vs wall variants and then calls canSurvive.

PointedDripstoneBlock.getStateForPlacement uses
getNearestLookingVerticalDirection().getOpposite(), so it can only naturally
produce world UP/DOWN today.

Design implication:
separate PHYSICAL placement position from LOCAL semantic directions.

A Planet placement adapter should preserve:
- hit BlockPos
- physical relative target position
- physical ray hit location

while exposing local semantic direction helpers to block state selection.

Do not globally replace BlockHitResult.direction with a local value; many call
sites use it to move to the physical adjacent block.

Research gate before implementation:
catalog direct getClickedFace() consumers, because some use it for state
orientation and others for physical adjacency.

## 4. BlockState shape caching

BlockBehaviour.BlockStateBase.Cache precomputes/caches data for a BlockState:
- collisionShape
- occlusion face shapes per Direction
- faceSturdy per Direction x SupportType
- full-block flags

The cache has NO BlockPos and NO PlanetFace.

Therefore a rotated physical shape must NOT be stored in the vanilla state
cache. The same BlockState can exist on different Planet faces.

Recommended boundary:
- vanilla cache remains canonical local-Y-up
- position-aware shape APIs return a rotated view/result for the frame at pos
- support queries translate physical Direction -> local Direction before using
  cached support data

Examples:
physical EAST on a POS_X face is local UP.
isFaceSturdy(level,pos,EAST) should ask canonical cache whether local UP is
sturdy.

Orientation-invariant full-block checks can remain cached.

## 5. VoxelShape strategy

Need a six-way orthogonal VoxelShape rotation around block center.

Required APIs:
- rotate local VoxelShape -> physical frame
- inverse when needed
- preserve unions/boxes exactly on 1/16 coordinates where possible
- cache immutable rotated variants by (canonical shape identity or state/query,
  PlanetFace) where safe

Test with asymmetric shapes:
- torch-like narrow shape
- slab
- stair-like multi-box union
- dripstone shape
- shape extending outside [0,1] if vanilla/mod supports it

Do not allocate rotated shape trees on every collision query.

Implemented Phase-1 pure shape foundation:
- PlanetVoxelShapeRotation.localToWorld rotates every source AABB around
  block center (0.5, 0.5, 0.5) through PlanetGravityFrame;
- POS_Y is an identity fast path;
- non-POS_Y local->world results are weak-key cached per canonical
  VoxelShape + PlanetFace;
- worldToLocal provides the inverse transform without globally caching
  transient physical query shapes;
- tests cover one explicitly expected asymmetric box on all six faces,
  multi-box unions, coordinates outside [0,1], round-trip, and cache reuse.

## 6. Support / survival / neighbor directions

Many blocks implement canSurvive/updateShape using:
- pos.above()/below()
- pos.relative(direction)
- comparisons to Direction.UP/DOWN
- horizontal direction loops

A general shape rotation alone does NOT fix this behavior.

Need a local-neighbor API:
PlanetBlockFrameContext.step(localDirection)

Result should include:
- target physical BlockPos
- target frame/chart if a seam is crossed
- physical side crossed
- transported local direction if the operation continues recursively

This is a prerequisite for fluids and edge-aware block behavior.

Implemented pure side/axis foundation:
- PlanetVanillaDirection converts Direction.Axis in both directions without
  inventing signed-axis semantics;
- PlanetBlockFrameContext exposes the same axis conversion beside Direction
  conversion and shape rotation;
- PlanetBlockStep.targetLocalSideTowardSource converts the physical side back
  toward the source into the target block's local frame;
- on all 24 directed edge entries that target side equals the opposite of the
  transported continuation direction. This is the value later support,
  capability and survival adapters must pass to canonical local block logic.

Important:
raw pos.relative(localToWorld(frame, dir)) is valid only while the operation
stays within one chart. At an edge, topology transport must be explicit.

## 7. Pointed dripstone as a representative block

PointedDripstoneBlock demonstrates almost every issue:
- TIP_DIRECTION property only permits UP/DOWN
- placement chooses global vertical direction
- canSurvive calls direction-based support logic
- updateShape ignores directions other than global UP/DOWN
- growth searches along direction
- cauldron search compares Y distance
- shapes are authored with Y as vertical
- drip particles use vertical placement assumptions

Correct interpretation:
TIP_DIRECTION remains UP/DOWN but is LOCAL.
Then all support/search/shape/render operations pass through the shared block
frame.

Do not make a dripstone-only six-direction property.

## 8. Static baked-model rendering

ModelBlockRenderer iterates all physical Direction values and:
1. model.getQuads(state, direction, ...)
2. pos.relative(direction) for culling
3. Block.shouldRenderFace(... direction ...)
4. uses BakedQuad.getDirection() for shape/AO/light calculations

A simple PoseStack rotation is insufficient:
- model.getQuads would be asked for the wrong face
- quad direction metadata would remain local
- AO/light/culling would sample wrong neighbors

Promising architecture:
PlanetOrientedBakedModelView cached by (base BakedModel, PlanetFace).

For requested PHYSICAL side:
- convert physical side -> local side
- delegate baseModel.getQuads(state, localSide, ...)
- return quad views/copies whose direction metadata is localToWorld(frame,...)
- geometry itself can be rotated about block center by a block-level PoseStack
  transform OR by cached transformed vertices

For unculled null-side quads:
- preserve list membership
- rotate direction metadata and geometry consistently

Need to preserve NeoForge model extension behavior:
- ModelData
- RenderType-specific getQuads
- getRenderTypes
- AO flags
- custom renderers
- particle icon and model transforms where applicable

Do not wrap a modded BakedModel in a way that silently loses NeoForge extension
methods.

## 9. AO / face culling

ModelBlockRenderer AO tables are indexed by BakedQuad physical direction and
sample neighbors around that direction.

If oriented model quads expose PHYSICAL direction metadata after rotation, much
of vanilla AO logic can remain unchanged.

Culling still needs the correct physical neighbor position. For an ordinary
same-chart face this is pos.relative(physicalDir). At a topology seam, research
whether the rendered physical block adjacency is direct XYZ adjacency or needs
the same seam neighbor adapter.

Acceptance must include full cubes at edges so hidden faces do not create
visible cracks.

## 10. Block entity renderers

Static BakedModel rotation does not affect BlockEntityRenderer.

Potential boundary:
BlockEntityRenderDispatcher/BER call wrapped in a local-frame PoseStack rotation
about block center.

But BER code can also read directional BlockState values and make world-axis
queries, so this needs its own research/acceptance stage rather than assuming
PoseStack alone is enough.

## 11. Signals/capabilities

BlockState directional signal APIs and NeoForge sided capabilities receive
Direction contexts.

Existing PlanetSidedQueryFrame / BlockCapabilityMixin is the correct generic
pattern for mod compatibility.

Redstone signal methods have vanilla's own reversed-direction semantics, so
local/world translation must preserve those semantics rather than add another
opposite() blindly.

Redstone is a separate acceptance group; do not declare block-frame complete
after only visual blocks work.

## 12. Phase-1 pure API proposal

PlanetBlockFrameContext:
- frame
- localToWorld(Direction)
- worldToLocal(Direction)
- step(Direction local)
- offset(local dx,dy,dz)
- rotateShape(localShape)
- maybe inverseRotateShape

PlanetBlockStep:
- sourcePos/sourceFrame
- localDirection
- physicalDirection at source boundary
- targetPos/targetFrame
- transportedDirection

Exact names may change, but one object should own these semantics.

## 13. Tests before runtime mixins

Pure tests:
- all 6 faces x 6 directions round trip
- all 24 directed cube edge transitions
- repeated step across edge then inverse step returns source
- multi-step offset across two edges
- asymmetric VoxelShape rotation on all faces
- support side local/world mapping
- no ambiguity at edge/corner with explicit preferred face

Runtime smoke tests only after pure suite:
- full grass cube texture orientation
- slab/stair collision + outline
- standing/wall torch
- ladder
- door/trapdoor
- pointed dripstone
- a modded directional block


## 14. Physical BlockPos seam semantics (implemented foundation)

Important distinction discovered during Phase 1:
the older PlanetBlockTopology / PlanetTopology face grid has alias semantics at
an edge: crossing maps the boundary cell to the corresponding boundary cell in
the next face chart. That is correct for the virtual/canonical face topology,
but a runtime physical BlockPos operation needs ordinary one-block adjacency.

PlanetBlockFrameContext therefore uses the gravity field plus FaceTransform:
- when a step ENTERS a shared edge BlockPos, the target chart is immediately
  transported to the adjacent face;
- the next continued local step moves along the adjacent face;
- when a source context is already on the shared edge in the old chart, the
  requested outward step folds immediately using the target frame;
- no duplicate physical alias block is introduced.

The existing FaceTransform remains authoritative for orientation transport.
PlanetBlockFrameContext only adds physical BlockPos stepping semantics.
