# Research: ordered local block traversal on the cubic Planet shell

Status: Phase-1 design decision before fluid/block-neighbor runtime adapters.

Sources inspected:
- Minecraft / NeoForge 1.21.1 BlockPos relative(Direction, distance) API
- FlowingFluid spread/getSpread/getSlopeDistance Direction-based neighbor flow
- existing Planetary FaceTransform / PlanetBlockFrameContext / PlanetBlockStep
- GravityChanger / Gravity API family as a comparison for fixed gravity-frame
  coordinate rotation

## 1. Why generic offset(dx, dy, dz) is not a safe primitive

Vanilla BlockPos lives in one Euclidean XYZ frame. Integer displacement is
commutative there, so applying X then Z or Z then X gives the same physical
BlockPos.

The Planet shell is different. Its local tangent frame is transported whenever
a path crosses a cube edge. A displacement containing more than one local axis
therefore needs an ORDERED path definition.

For example, near a cube corner:
- walk local EAST until/through its edge, then local SOUTH in the transported
  frame;
- walk local SOUTH first, then local EAST.

Those routes can traverse different adjacent faces. Treating both as a single
unordered offset(dx,dy,dz) would hide this path dependence and recreate bugs at
corners.

Decision:
DO NOT add a BlockPos-style generic local offset(dx,dy,dz) to
PlanetBlockFrameContext.

If a future subsystem genuinely needs a compound displacement, its API must
make the path order explicit.

## 2. Single-direction walk is well-defined

Continuing one local Direction is unambiguous:

1. call PlanetBlockFrameContext.step(currentDirection);
2. move to step.target();
3. continue with step.transportedDirection();
4. repeat.

This defines:
walk(localDirection, steps)

It works for:
- tangent movement across one or many cube edges;
- local UP/DOWN traversal where the dominant gravity face changes;
- reverse traversal by starting from the result and walking the opposite of
  the final transported direction.

The result must retain the final transported Direction because callers doing
bounded searches may need to continue from there.

## 3. Tangent neighbors should stay individual steps

Vanilla subsystems such as FlowingFluid reason about individual Direction
neighbors rather than one compound XYZ surface displacement.

PlanetBlockFrameContext therefore exposes four independent seam-aware tangent
steps:
NORTH, SOUTH, WEST, EAST.

Every item is a PlanetBlockStep, so the caller gets:
- physical target BlockPos;
- physical side crossed;
- target gravity frame;
- transported direction;
- boundary-crossing flag.

This is directly usable by:
- fluid side spreading;
- source-neighbor counting;
- support/survival scans;
- pathfinding cardinal expansion;
- block update fan-out.

## 4. Corner rule

A physical cube corner belongs to three gravity-face candidates.

The current context's preferred face remains authoritative for interpreting the
four LOCAL tangent directions. Each tangent step must still land on exactly one
ordinary physical adjacent BlockPos.

Corner topology is singular in the discrete physical grid.

At an exact three-face cube corner, the current preferred face still has four
logical local tangent directions, but a cube vertex has only three physical
surface edges. The two local directions that point outward through the two
incident face edges fold onto the SAME next physical BlockPos along the third
physical cube edge.

They are not the same logical traversal state:
- both transitions have the same target BlockPos and physicalDirection;
- each carries a different target PlanetFace/chart;
- subsequent transported traversal can therefore differ.

The correct distinction is:
- physical-neighbor identity: BlockPos;
- chart-aware traversal identity: (BlockPos, PlanetFace).

Acceptance:
- at all 6 faces x 4 local corners, tangentSteps returns four logical steps;
- those four steps occupy exactly three physical target BlockPos values;
- they occupy four distinct (BlockPos, PlanetFace) traversal states;
- the two outward directions converge to one physical target but select the
  two different adjacent target faces;
- each target is exactly source.pos.relative(step.physicalDirection());
- no extra/alias physical block is invented.

## 5. Multi-edge straight-walk acceptance

Use a deterministic path starting one cell before the POS_Y EAST edge.

With shell radius R:
- step 1 enters the shared POS_Y/POS_X edge cell and transports to POS_X;
- the next 2R steps continue along POS_X local EAST (physical -Y);
- the last of those enters the POS_X/NEG_Y edge and transports to NEG_Y.

Expected after 1 + 2R steps:
- physical relative coordinates from core: (+R, -R, 0);
- target face NEG_Y;
- transported tangent direction WEST;
- exactly two gravity-boundary crossings.

Walking the opposite of that final transported direction for the same number of
steps must return to the exact starting BlockPos and starting face.

## 6. Performance note

walk() is a semantic/reference primitive and currently composes ordinary
PlanetBlockStep objects.

Hot recursive systems such as fluid slope search and pathfinding may later use
the same step semantics in specialized loops to reduce allocation. They must not
replace it with raw BlockPos.relative() at seams.

Correctness of the topology contract comes first; hot-path allocation is a
separate performance gate.
