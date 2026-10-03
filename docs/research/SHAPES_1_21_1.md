# Research: runtime block VoxelShape boundary in Minecraft 1.21.1

Status: first Phase-3 shape adapter implemented; user verified rotated slab
selection/outline in game.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Exact vanilla call flow

BlockBehaviour.BlockStateBase exposes multiple public shape APIs:
- getShape(BlockGetter, BlockPos)
- getShape(BlockGetter, BlockPos, CollisionContext)
- getCollisionShape(BlockGetter, BlockPos)
- getCollisionShape(BlockGetter, BlockPos, CollisionContext)
- getVisualShape(...)
- getInteractionShape(...)
- getBlockSupportShape(...)
- getOcclusionShape(...)
- getFaceOcclusionShape(...)

Important nesting:
- getShape(simple) delegates to getShape(context);
- getCollisionShape(simple) may return BlockState cache directly or delegate to
  getCollisionShape(context);
- BlockBehaviour default getCollisionShape calls state.getShape(simple);
- BlockBehaviour default getVisualShape calls block getCollisionShape, which can
  call state.getShape;
- default getBlockSupportShape calls block getCollisionShape and can therefore
  also nest a public state.getShape call;
- default getOcclusionShape calls state.getShape(simple).

A RETURN injection on every physical method independently would therefore rotate
dynamic/default shapes multiple times.

## 2. Collision/raycast consumers

BlockCollisions uses:
    state.getCollisionShape(collisionGetter, pos, collisionContext)

ClipContext routes:
- COLLIDER -> BlockStateBase.getCollisionShape(context)
- OUTLINE  -> BlockStateBase.getShape(context)
- VISUAL   -> BlockStateBase.getVisualShape(context)

BlockGetter.clipWithInteractionOverride additionally calls:
    state.getInteractionShape(blockGetter, pos)

Therefore these shape families form the common physical boundary for collision,
selection and raycast.

## 3. Canonical cache rule

BlockStateBase.Cache has no BlockPos/PlanetFace and stores canonical state data.

PlanetBlockShapeRuntime only rotates when the outermost query's BlockGetter is
an actual bound Level. EmptyBlockGetter/cache initialization therefore remains
canonical automatically.

## 4. Nested query scope

PlanetBlockShapeRuntime maintains a ThreadLocal query depth.

Physical outer queries:
- outline shape, both overloads
- collision shape, both overloads
- visual shape
- interaction shape

rotate exactly once when leaving the outermost scope.

Nested calls return canonical results unchanged.

## 5. Support and occlusion deliberately remain canonical

The verified support runtime asks isFaceSturdy/canSupportCenter with canonical
LOCAL Direction.

Dynamic SupportType evaluation can call getBlockSupportShape, whose default can
nest state.getShape. getBlockSupportShape therefore enters the same scope but
finishes canonical.

Occlusion is also deferred. getOcclusionShape participates in the scope but
finishes canonical so default nested state.getShape cannot accidentally rotate.

Physical face occlusion/culling requires a separate adapter because vanilla
Block.shouldRenderFace caches by BlockState pair + Direction with no PlanetFace.

## 6. Full-cube performance

BlockCollisions has an identity fast path:
    voxelshape == Shapes.block()

PlanetVoxelShapeRotation therefore returns the original instance for:
- Shapes.block()
- empty shapes
- POS_Y identity frame

This preserves the hot full-terrain collision path.

## 7. Runtime diagnostic

PlanetShapeDiagnostics performs no world mutation.

Across all six PlanetFace values it verifies:
- bottom-slab outline, both overloads: physical
- bottom-slab collision, both overloads: physical
- bottom-slab visual shape: physical
- bottom-slab block support shape: canonical
- bottom-slab occlusion shape: canonical
- hopper interaction shape: physical
- full stone collision remains exact Shapes.block singleton

Login counts:
- 36 physical checks
- 12 canonical checks
- 6 full-block identity checks

## 8. Deliberate limitations

Not solved here:
- static baked-model/culling handled by docs/research/RENDERING_1_21_1.md
- BlockEntityRenderer/custom renderer orientation
- block entity renderers
- neighbor-dependent state shape selection that itself uses raw world-axis
  neighbor math
- entityCanStandOn / getBlockFloorHeight world-Y semantics
- pathfinding floor semantics
- custom render engines/Flywheel
- fluid shapes

## 9. Manual acceptance

A side-face BOTTOM slab should now have its selection/collision half-volume
normal to local UP/DOWN.

The baked model is not rotated yet. Model and outline/collision may therefore
visibly disagree; that mismatch is expected and proves the shape and model
layers are independent.


## 10. Manual acceptance update

User verified after the shape patch that side-face slab selection outline is
physically correct. This confirms the outermost shape-query rotation in real
client interaction. Static model rendering is the next independent layer and is
documented in RENDERING_1_21_1.md.
