# Research: local growth and cross-neighbor connections in Minecraft 1.21.1

Status: first natural-grass and fence local-neighbor adapters implemented;
build/runtime acceptance pending.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Runtime findings that triggered this pass

Manual acceptance after the static model/culling patch found:
- grass generated with the correct visual local orientation but decayed back to
  dirt on every gravity face except +Y and did not spread correctly;
- fences/gates visually oriented correctly, but fences acquired incorrect arms
  along physical +Y on rotated faces.

These are not renderer bugs. Both classes still perform world-axis neighbor
queries internally.

## 2. SpreadingSnowyDirtBlock world-axis assumptions

Vanilla 1.21.1 SpreadingSnowyDirtBlock:

canBeGrass:
- pos.above();
- LightEngine.getLightBlockInto(... Direction.UP ...).

canPropagate:
- pos.above();
- water check in that world-above cell.

randomTick:
- converts to DIRT when canBeGrass fails;
- brightness is read at pos.above();
- spread target is raw
  pos.offset(random X [-1,1], random Y [-3,1], random Z [-1,1]);
- SNOWY is derived from target.above().

Therefore a correctly generated side/bottom grass block is judged against the
wrong physical neighbor on its first random tick.

## 3. Grass local policy

For Planet runtime:
- "above" means canonical local UP;
- light occlusion Direction passed to LightEngine remains PHYSICAL and is the
  local-UP neighbor query's physicalDirection;
- brightness is sampled at the physical local-UP target;
- water and snow checks use the same local-UP target.

Outside Planet runtime vanilla code remains untouched.

## 4. Random spread displacement and seams

Vanilla Cartesian offset(dx,dy,dz) is order-independent.

Planet's curved cube surface intentionally has NO generic unordered
offset(dx,dy,dz) API because an offset crossing a seam is path-dependent.

Natural grass spreading therefore defines a subsystem-specific ordered path:
1. local Y segment (UP/DOWN);
2. local Z segment (NORTH/SOUTH);
3. local X segment (WEST/EAST).

Every step uses PlanetBlockFrameContext and the current transported chart.

On an ordinary face this is exactly the same local Cartesian target as vanilla.
At a seam it is deterministic and explicitly documented rather than hiding an
arbitrary order inside a generic block API.

This policy lives in PlanetGrowthTopology and must not be reused as a generic
replacement for BlockPos.offset.

## 5. SnowyDirtBlock

SnowyDirtBlock itself also contains physical +Y assumptions:
- placement reads clickedPos.above();
- updateShape reacts only to physical Direction.UP.

Those are adapted so SNOWY tracks canonical local UP.

This is required even if natural grass spreading is fixed; otherwise snow state
would still update incorrectly after neighbor changes.

## 6. FenceBlock world-axis assumptions

Vanilla FenceBlock placement:
- reads blockpos.north/east/south/west;
- writes canonical NORTH/EAST/SOUTH/WEST properties from those physical cells.

Vanilla updateShape:
- only reacts when physical update Direction is in world HORIZONTAL;
- PROPERTY_BY_DIRECTION is indexed by that physical direction.

On a rotated face:
- a valid local tangent can be physical UP/DOWN;
- one physical horizontal axis can be local UP/DOWN.

This directly explains unwanted physical +Y arms/missing local arms.

## 7. Fence local policy

Fence NORTH/EAST/SOUTH/WEST remain canonical LOCAL BlockState properties.

Placement independently resolves four PlanetBlockNeighborQuery values for:
- local NORTH;
- local EAST;
- local SOUTH;
- local WEST.

For each logical tangent:
- target BlockPos is physical/seam-aware;
- targetLocalSideTowardSource is used for isFaceSturdy;
- the same target-local side is passed to FenceBlock.connectsTo, so a neighboring
  FenceGateBlock sees a direction in its own canonical state frame.

updateShape does NOT translate the physical Direction directly.

Instead it compares the physical neighborPos against all four logical tangent
queries. This matters at seams/corners, where topological adjacency is not
equivalent to sourceFrame.worldToLocal(physicalDirection), and multiple logical
directions can intentionally share one physical target at a three-face corner.

Every matching local property is recomputed; non-tangent physical updates leave
the fence connection state unchanged. Water tick scheduling is preserved.

## 8. Why this boundary helps mod compatibility

The core invariant is reusable:
    semantic local connection direction
        -> PlanetBlockNeighborQuery
        -> physical target
        -> target canonical side toward source

Do not globally patch Direction.Plane.HORIZONTAL or BlockPos.relative.

WallBlock, IronBarsBlock, pane/pipe-like vanilla or mod blocks can later reuse
this topology contract without redefining physical Minecraft coordinates.

## 9. Tests

PlanetGrowthTopologyTest:
- all six faces, interior spread offset equals local east/up/south basis;
- explicit seam case locks the documented Y -> Z -> X segment order.

Existing PlanetBlockNeighborQuery and corner tests already cover:
- physical target adjacency;
- target local side toward source;
- edge/corner canonical-frame behavior.

## 10. Deliberate limitations

Not solved in this patch:
- GrassBlock bonemeal feature placement (its feature pipeline has additional
  world-Y/worldgen assumptions);
- vanilla skylight/environment policy on bottom/side surfaces;
- WallBlock and IronBarsBlock local connection state;
- fence-gate IN_WALL neighbor logic;
- plant/crop survival families generally;
- grass side-overlay/culling/shadow visual issue reported separately.

## 11. Manual acceptance

Grass:
- newly generated grass on +X/-X/+Z/-Z/-Y must not immediately decay because a
  physical-world-above dirt block exists;
- with adequate light, dirt in local neighboring soil should naturally spread
  to grass;
- +Y remains vanilla-equivalent.

Fence:
- on every rotated face, arms appear only toward local N/E/S/W neighbors;
- a physical +Y neighbor creates an arm only when +Y is actually one of those
  local tangent directions;
- adding/removing a non-tangent physical neighbor must not create/remove a
  fence arm;
- fence-to-gate connection remains valid.
