# Research: support, canSurvive and updateShape semantics in Minecraft 1.21.1

Status: first runtime support adapters implemented; runtime acceptance pending.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Exact vanilla flows inspected

Representative classes:
- BaseTorchBlock
- WallTorchBlock
- RedstoneWallTorchBlock
- LadderBlock
- FaceAttachedHorizontalDirectionalBlock
- LeverBlock
- ButtonBlock
- BlockBehaviour.BlockStateBase.updateNeighbourShapes
- BlockBehaviour.BlockStateBase.Cache
- Block.canSupportCenter

## 2. Physical update direction vs local state direction

BlockState.updateNeighbourShapes iterates PHYSICAL world Directions around a
physical BlockPos, then calls neighborShapeChanged/updateShape with a Direction
that identifies the changed PHYSICAL neighbor relative to the block being
updated.

That Direction must remain physical.

By contrast Planet BlockState properties such as FACING/FACE are canonical
LOCAL semantics.

Therefore code like:
    updateDirection.getOpposite() == state.getValue(FACING)
is invalid on rotated Planet faces unless the physical update Direction is
explicitly reframed.

Do NOT globally reinterpret updateShape's Direction as local. Neighbor fan-out
is physical world geometry and many callers depend on that.

## 3. Safer support-update rule

Instead of comparing physical update Direction with local FACING:

1. derive the block's LOCAL support direction from its state;
2. resolve PlanetBlockSupportQuery;
3. obtain the exact PHYSICAL supportPos;
4. compare updateShape's neighborPos with that supportPos;
5. if it is the support neighbor, re-run the canonical support predicate.

This avoids all physical/local Direction comparison ambiguity and works across
gravity edges.

## 4. Runtime support predicate

PlanetBlockSupportRuntime is the shared runtime bridge.

It:
- accepts a LevelReader + physical source BlockPos + source LOCAL support
  Direction;
- activates only when LevelReader is an actual bound Level;
- resolves PlanetBlockSupportQuery through PlanetBlockRuntime;
- exposes vanilla-compatible canSupportCenter and isFaceSturdy checks on the
  actual physical support block using the support block's canonical local side.

Non-Level LevelReader contexts intentionally return empty so vanilla behavior
continues. Worldgen feature survival must later be handled through
PlanetGenerationSpace instead of pretending runtime bindings exist in
WorldGenRegion/EmptyBlockGetter.

## 5. Vanilla adapters

### BaseTorchBlock

Standing torch support is source local DOWN.

canSurvive:
- resolve local DOWN support;
- call Block.canSupportCenter on supportPos using support canonical side.

updateShape:
- compare physical neighborPos to supportPos;
- destroy only when that actual support changed and canSupportCenter is false;
- otherwise return state, matching BlockBehaviour's default updateShape.

This also covers standing RedstoneTorchBlock support because it inherits
BaseTorchBlock support behavior.

### WallTorchBlock

Wall torch FACING is canonical local and points away from the wall.

Support direction:
    local FACING.opposite

The public static WallTorchBlock.canSurvive(LevelReader, pos, facing) is adapted
because RedstoneWallTorchBlock also delegates to it.

Placement uses PlanetFrameApi.localNearestLookingDirections and stores local
horizontal FACING.

updateShape compares physical neighborPos with resolved physical supportPos.

### RedstoneWallTorchBlock

It does NOT subclass WallTorchBlock. It subclasses RedstoneTorchBlock and only
reuses WallTorchBlock static support/placement logic.

Therefore it needs its own updateShape adapter.

Its redstone signal behavior remains separate Phase 7A work:
- hasNeighborSignal still contains raw pos.relative(local FACING);
- signal Direction comparisons are not solved by support adaptation.

### LadderBlock

Support direction:
    local FACING.opposite

Placement:
- keeps physical clicked-source lookup physical;
- converts the clicked physical side to the clicked source block's canonical
  local side before comparing against an existing ladder FACING;
- uses target-local nearest-looking directions for the new FACING;
- preserves waterlogging.

updateShape:
- identifies support by physical neighborPos;
- preserves vanilla water tick scheduling.

### FaceAttachedHorizontalDirectionalBlock

This is an important shared vanilla/mod boundary used by lever/button and many
modded blocks.

Connected direction is canonical LOCAL:
- FACE=CEILING -> local DOWN
- FACE=FLOOR -> local UP
- FACE=WALL -> local FACING

Support direction is connectedDirection.opposite.

The static canAttach(LevelReader,pos,Direction) is adapted so inherited vanilla
and mod code using that helper gets Planet support semantics automatically.

Placement uses:
- target-local nearest-looking order;
- target-local horizontal player direction;
- local AttachFace classification.

updateShape uses physical neighborPos == resolved supportPos.

## 6. Horizontal player direction

UseOnContext.getHorizontalDirection returns player.getDirection(), which in
Planet runtime is player BODY-local yaw semantics.

At an exact gravity edge player body frame and target canonical block frame can
differ.

PlanetFrameApi.localHorizontalDirection therefore converts:
    player body-local horizontal -> physical Direction -> target canonical local

If the transformed direction becomes local vertical at an exact frame change,
the helper falls back to the first horizontal direction in the already
reframed localNearestLookingDirections ordering.

## 7. Runtime diagnostic

PlanetSupportDiagnostics does not mutate the world.

For each of the six canonical faces, at an interior cell two blocks outward from
the core, it verifies hypothetical:
- standing torch
- wall torch
- redstone wall torch
- ladder
- floor lever

For each block it checks:
1. the resolved physical support block is sturdy for the required canonical
   support side;
2. BlockState.canSurvive returns true through the actual mixin path;
3. BlockState.updateShape invoked from the exact physical support neighbor does
   not drop the supported block.

Coverage:
- 6 faces x 5 states = 30 survival checks;
- 6 faces x 5 states = 30 updateShape checks.

Successful login:
[Planetary] Support probe passed: 30 survival checks, 30 updateShape checks.

This probe validates stable support. Manual removal of support is still required
to verify the destruction path in gameplay.

## 8. Deliberate limitations

Not closed by this support step:
- redstone signal direction / neighbor notification semantics;
- rendering/collision shapes;
- particles;
- worldgen LevelReader contexts;
- generic plant/crop/snow support families;
- doors/beds/double blocks;
- support removal at exact three-face corners in runtime;
- third-party blocks that override these base methods and perform their own raw
  world-axis math.

Those remain in the master gravity impact audit.
