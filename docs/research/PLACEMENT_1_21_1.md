# Research: block placement / hit-frame semantics in Minecraft 1.21.1

Status: Phase-2 foundation; pure placement-frame API implemented, no placement
runtime mixin installed yet.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Exact vanilla call flow inspected

Relevant sources:
- UseOnContext
- BlockPlaceContext
- DirectionalPlaceContext
- BlockItem
- StandingAndWallBlockItem
- RotatedPillarBlock
- HopperBlock
- StairBlock
- TrapDoorBlock
- FaceAttachedHorizontalDirectionalBlock
- WallTorchBlock
- LadderBlock
- WallSignBlock / StandingSignBlock
- ObserverBlock
- Direction.orderedByNearest / getFacingAxis

Representative Create 1.21.1 placement code was also inspected:
- SmartObserverBlock
- PackagerBlock
- FunnelBlock
- DirectedDirectionalBlock
- DoubleFaceAttachedBlock
- SmartFluidPipeBlock

## 2. Physical geometry that MUST remain physical

UseOnContext exposes:
- getClickedPos() from BlockHitResult BlockPos;
- getClickedFace() from BlockHitResult Direction;
- getClickLocation() from BlockHitResult world Vec3.

BlockPlaceContext constructor computes:
    relativePos = hitBlockPos.relative(hitPhysicalDirection)

This is physical Minecraft adjacency and MUST NOT be globally made local.

BlockPlaceContext.getClickedPos() then chooses either:
- the clicked physical block if replaceClicked=true; or
- relativePos otherwise.

Therefore the final placement target BlockPos is also physical.

Rule:
BlockHitResult, its Direction, world click location and target BlockPos stay
ordinary physical world geometry.

## 3. Canonical LOCAL placement semantics

BlockState properties in Planet space use canonical LOCAL semantics.

Examples:
- RotatedPillarBlock writes getClickedFace().getAxis() into AXIS;
- HopperBlock derives FACING from getClickedFace().getOpposite();
- face-attached blocks classify Direction.Axis.Y as floor/ceiling;
- wall torch/ladder/sign choose horizontal FACING;
- observer stores nearest-looking Direction;
- stairs/trapdoor store player-relative FACING plus top/bottom-style state.

These semantic values cannot safely receive raw physical Direction on side or
bottom Planet faces.

PlanetBlockPlacementFrame therefore resolves a physical hit against the
CANONICAL state frame of the TARGET block and exposes:
- physicalClickedFace;
- localClickedFace;
- physical world click location;
- localHitOffset inside the target block.

## 4. Local hit coordinates

Several vanilla placement implementations use world-axis fractions directly.

Examples:
- StairBlock decides Half.TOP/BOTTOM from clickLocation.y - blockPos.y;
- TrapDoorBlock does the same for side placement.

That is only correct when canonical local UP is physical +Y.

PlanetBlockPlacementFrame converts the vector from target block CENTER into the
target canonical frame, then adds 0.5 on each local axis.

Thus localHitOffset is the target-block-local coordinate:
- local x = east coordinate inside block;
- local y = up coordinate inside block;
- local z = south coordinate inside block.

Use localHitOffset.y for local TOP/BOTTOM decisions.

Do not mutate the original world Vec3.

## 5. Clicked-face conversion uses TARGET frame

When placing into an adjacent block, the physical clicked face originates on the
source block but the property being constructed belongs to the target block.

Therefore:
    localClickedFace =
        targetCanonicalFrame.worldToLocal(physicalClickedFace)

This matters at exact gravity edges where clicked source and placement target can
have different canonical faces.

Do not interpret state orientation using the clicked source block's frame.

## 6. Nearest-looking directions

Direction.orderedByNearest(player) directly reads player view pitch/yaw and
returns vanilla Direction values.

In Planet runtime entity yaw/pitch are body-local and are transported when the
gravity face changes. Therefore the base Direction.orderedByNearest(player)
ordering already represents LOCAL orientation semantics for interactive Planet
placement.

However BlockPlaceContext.getNearestLookingDirections has a second rule when
replaceClicked=false:
- it takes getClickedFace().getOpposite();
- moves that Direction to the front of the array.

In vanilla both values share one global frame.
In Planet the returned orientation array is local while getClickedFace() remains
physical. That mixes frames.

PlanetFrameApi.localNearestLookingDirections(context) reproduces the vanilla
reordering using:
    localClickedFace.getOpposite()

It intentionally returns empty for player-less contexts.

## 7. Player-less DirectionalPlaceContext is separate

DirectionalPlaceContext is used by at least:
- FallingBlockEntity replacement checks;
- ShulkerBox dispenser placement.

It overrides nearest-looking/horizontal helpers from constructor Direction
arguments and has no Player.

Those Direction arguments come from subsystem-specific physical logic. They must
not be blindly classified as Planet-local.

Decision:
do NOT make localNearestLookingDirections guess for player-less contexts.
Audit dispenser/falling-block placement separately during Phase 2/4.

## 8. Why getClickedFace cannot be globally overridden

The same API has both geometry and semantic consumers.

Examples:
- RotatedPillar/Hopper use clicked face for orientation property selection;
- Ladder, when not replacing the clicked block, computes:
      clickedPos.relative(clickedFace.opposite())
  to inspect the actual physical block behind the placement target.

If getClickedFace globally returned local Direction, the latter physical lookup
would be wrong.

Therefore no global BlockPlaceContext.getClickedFace replacement.

The same warning applies to getClickLocation: returning synthetic local
coordinates would break code that genuinely needs the physical world hit point.

## 9. Public compatibility API

PlanetFrameApi now exposes:
- placementFrame(level, targetPos, physicalClickedFace, worldClickLocation);
- placementFrame(BlockPlaceContext);
- localNearestLookingDirections(BlockPlaceContext) for interactive placement.

PlacementFrame exposes:
- target physical BlockPos;
- target canonical PlanetFace;
- physical clicked face;
- canonical local clicked face;
- original physical world click location;
- local hit offset;
- localHitUpperHalf() convenience.

This allows optional compat mixins to replace only semantic use sites while
retaining physical geometry.

## 10. Runtime integration strategy

Do NOT wrap all BlockPlaceContext methods with one fake-local context.

Instead classify each call site:

A. Physical:
- target BlockPos;
- BlockHitResult;
- physical clicked/source neighbor lookup;
- ray position.

B. Local semantic:
- Direction/Axis property chosen from clicked face;
- AttachFace floor/wall/ceiling classification;
- player-relative FACING;
- local nearest-looking directions;
- local top/bottom click half.

C. Mixed:
- methods that choose a local FACING and then immediately call
  pos.relative(FACING) need PlanetFrameApi.localNeighbor rather than only a
  direction conversion.

Initial vanilla runtime adapters should target representative semantic families,
then be expanded from source audit:
- RotatedPillar AXIS;
- Hopper FACING;
- stairs/trapdoor local hit half;
- FaceAttachedHorizontalDirectionalBlock;
- wall torch/ladder/sign;
- observer/directional blocks.

## 11. Create/mod compatibility implication

Create placement contains all three classes:
- nearest-looking orientation state;
- physical neighbor inspection;
- raw clickedPos.relative(face).

Therefore changing vanilla getters globally is especially unsafe.

Create-specific compatibility, if needed, should use PlanetFrameApi at exact
semantic call sites or a higher reusable callback boundary discovered later.

## 12. Pure acceptance

Required before any placement runtime mixin:
- all 6 canonical faces x all 6 clicked Direction round-trip;
- asymmetric local hit offset round-trip on all six faces;
- exact edge chooses target canonical frame;
- POS_X example: physical EAST -> local UP;
- local hit upper-half depends on local Y, not world Y;
- interactive nearest-looking non-replacing reorder uses local clicked face;
- player-less nearest-looking helper deliberately has no inferred result.

Runtime acceptance will be added with the first placement mixin.
