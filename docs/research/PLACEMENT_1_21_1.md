# Research: block placement / hit-frame semantics in Minecraft 1.21.1

Status: Phase-2 foundation + first runtime placement adapters implemented;
runtime acceptance pending user verification.

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
gravity face changes. Direction.orderedByNearest(player) therefore starts in the
PLAYER BODY-local frame. On a face interior that normally matches the target
block frame, but at an exact gravity edge the player's preferred/hysteretic
body frame can differ from the target block's position-only canonical frame.

PlanetFrameApi.localNearestLookingDirections therefore re-expresses every
candidate:
    player local -> physical Direction -> target canonical local
before applying BlockPlaceContext's reorder rule.

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


## 13. First runtime adapters

Implemented as narrowly scoped semantic adapters:

RotatedPillarBlockPlacementMixin
- getStateForPlacement only;
- physical hit remains unchanged;
- stores AXIS = localClickedFace.axis.

HopperBlockPlacementMixin
- getStateForPlacement only;
- stores FACING from localClickedFace.opposite;
- preserves vanilla rule that any local vertical output becomes local DOWN;
- ENABLED remains true as in vanilla default placement.

SlabBlockPlacementMixin
- getStateForPlacement uses local clicked face + localHitOffset.y;
- canBeReplaced uses the same local semantic values for slab stacking;
- physical target/fluid lookup remains vanilla physical geometry.

Why Slab was chosen before stairs/trapdoor:
- it exercises the local-hit-half problem directly;
- it has no stair neighbor-shape graph and no trapdoor redstone dependency;
- failures can therefore be attributed to placement-frame semantics rather than
  yet-unadapted update/redstone subsystems.

## 14. Runtime diagnostic

PlanetPlacementDiagnostics performs real vanilla getStateForPlacement calls
without mutating the world.

Coverage:
- 6 canonical faces;
- 6 local clicked directions;
- RotatedPillar AXIS;
- Hopper FACING;
- Slab TOP/BOTTOM with both lower/upper local Y samples across horizontal
  directions.

Total: 108 runtime state checks through the actual mixins.

A successful dedicated Planet login prints:
[Planetary] Placement probe passed: 108 vanilla state checks.

Important:
this verifies stored canonical BlockState semantics only. Runtime rotated
collision/model rendering is Phase 3, so a side-face slab/log/hopper may still
LOOK physically wrong until shape/model adapters are connected.


## 15. StandingAndWallBlockItem variant selection

Runtime acceptance after the first Phase-2 placement/support adapters exposed a
separate item-level bug: torch support and wall-torch BlockState semantics were
correct, but the ITEM still chose standing vs wall using vanilla
BlockPlaceContext.getNearestLookingDirections.

Exact vanilla flow:
StandingAndWallBlockItem.getPlacementState:
1. precomputes wallBlock.getStateForPlacement(context);
2. iterates context.getNearestLookingDirections();
3. skips attachmentDirection.opposite;
4. if direction == attachmentDirection, tries the standing block;
5. otherwise tries the wall block;
6. keeps vanilla canPlace + isUnobstructed checks.

For TORCH / SOUL_TORCH / REDSTONE_TORCH and coral fans,
attachmentDirection = Direction.DOWN. In Planet semantics this is canonical
LOCAL DOWN.

Therefore the loop directions must also be target-canonical LOCAL. Feeding raw
vanilla/world ordering mixes frames and produces exactly the observed behavior:
on rotated faces floor clicks can choose wall variants and wall clicks can choose
standing variants depending on physical axis and neighboring support.

Implementation:
StandingAndWallBlockItemMixin redirects only the single
BlockPlaceContext.getNearestLookingDirections invocation inside
getPlacementState to PlanetFrameApi.localNearestLookingDirections.

Everything else remains vanilla:
- physical clicked/target BlockPos;
- wallBlock.getStateForPlacement;
- standing block getStateForPlacement;
- canPlace/canSurvive;
- obstruction checks;
- subclass behavior.

Outside Planet, or when PlanetFrameApi cannot classify the context, the redirect
falls back to the original vanilla method.

This is intentionally a generic item boundary rather than a TorchItem special
case, so modded StandingAndWallBlockItem subclasses inherit the same local-frame
variant selection.

Runtime diagnostic:
PlanetStandingWallPlacementDiagnostics deliberately selects a target surrounded
by valid stone supports. For every PlanetFace:
- local floor click must still choose standing TORCH even though wall placement
  is also possible;
- four local wall clicks must choose WALL_TORCH with matching canonical local
  FACING.

Coverage:
- 6 standing checks;
- 24 wall checks.
