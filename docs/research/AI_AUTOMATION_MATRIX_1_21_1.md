# AI / navigation / automation / logistics matrix — Minecraft 1.21.1 / NeoForge 21.1.215

Status: R5 COMPLETE research result for Planetary 2.0.

This document is the concrete Phase-6 + Phase-7A ownership map produced by
global research batch R5.

R5 is research only. No runtime implementation is performed here.

Primary source families audited:
- Node / Target / NodeEvaluator / WalkNodeEvaluator / PathFinder / Path;
- Ground/Flying/WaterBound/Amphibious/WallClimber navigation;
- Fly/Swim/Amphibious node evaluators;
- Move/Look/FlyingMove/BodyRotation controls;
- RandomPos / DefaultRandomPos / LandRandomPos / AirRandomPos /
  AirAndWaterRandomPos / HoverRandomPos / GoalUtils;
- representative navigation-bypassing goals and the MoveToBlockGoal family;
- SignalGetter;
- RedStoneWireBlock;
- diode/repeater/comparator;
- observer/torch/button/lever/pressure plate and complete signal-source sweep;
- tripwire/sculk calibration;
- powered/detector rail behavior;
- piston signal graph / PistonStructureResolver / moving piston;
- hopper/dispenser/dropper/crafter;
- NeoForge 1.21.1 path type, redstone, piston, capability and vanilla-inventory
  integration hooks.

R5 assumes the R1 topology kernel, R3 fluid contract and R4 physical entity
contract.

---

## 1. R5 fundamental rule: graph state can be richer than BlockPos

Several vanilla subsystems use a physical BlockPos as both:
1. world storage location; and
2. complete algorithm state.

That second assumption fails at a Planet seam.

The same physical cell can be reached with different traversal charts and
different transported continuation directions.

Therefore distinguish:

    physical world identity
        BlockPos

from:

    semantic graph identity
        (BlockPos, traversal frame/chart, mechanism-specific state)

This is already required by:
- rails;
- fluid slope traversal;
- ground navigation;
- piston push traversal;
- long line mechanisms such as tripwire.

Do not globally change BlockPos equality.
Each owning graph keeps its richer key.

---

# PART I — PHASE 6: NAVIGATION AND AI

## 2. Ground navigation needs a chart-aware node identity

Vanilla Node contains only:
- x;
- y;
- z;
- hash derived only from xyz.

NodeEvaluator.nodes also caches by Node.createHash(x,y,z).

At a Planet seam this can merge two semantically different traversal states.

### Required Planet state

Use a stable key such as:

    PlanetNavigationNodeKey(
        physical BlockPos,
        traversal PlanetFace/chart
    )

and a node carrying the same traversal state.

The graph must not infer the chart later from:
- entity current face;
- preferred-face hysteresis;
- bare BlockPos.

That is the root cause class behind the existing edge/spin experiments.

### Current first-pass status

The current PlanetWalkNodeEvaluator is useful evidence but NOT the final
architecture because:
- it uses vanilla xyz-only Node identity;
- it re-resolves a frame from position + entity preferred face;
- it implements only a reduced path-type set;
- it omits full vanilla diagonal/door/rail/hazard behavior.

PlanetPathGeometry.EDGE_CROSSING_OVERSHOOT is explicitly a symptom workaround,
not a future semantic boundary.

Do not build Phase 6 on top of that overshoot.

---

## 3. Vanilla PathFinder can probably remain

R5 found no requirement to replace A* itself.

BinaryHeap operates on Node fields such as:
- f;
- heapIdx.

PathFinder obtains neighbors through NodeEvaluator and reconstructs the
cameFrom chain.

Therefore a portable design can retain vanilla PathFinder while replacing:
- node identity/cache;
- neighbor generation;
- path metadata that must preserve traversal chart.

Preferred direction:

    PlanetNavigationNode extends Node
        + traversal chart/face

    PlanetGroundNodeEvaluator
        own Map<PlanetNavigationNodeKey, PlanetNavigationNode>
        no use of vanilla xyz-only NodeEvaluator.nodes for Planet nodes

    vanilla PathFinder
        remains A* owner where possible

This keeps the version-sensitive surface smaller than a complete A* fork.

### Path integration still needs chart awareness

Vanilla Path loses chart information in several places:
- sameAs compares only xyz;
- getEntityPosAtNode derives an xyz/y-up anchor;
- navigation timeoutCachedNode is xyz-only;
- debug serialization writes ordinary Node only.

Planet path adapter must use node chart for:
- waypoint physical anchor;
- path equality;
- timeout identity;
- seam continuation.

Debug serialization can remain a development/version adapter concern unless
Planet begins persisting paths. AI paths are not Planet-owned save data.

---

## 4. WalkNodeEvaluator is one local-ground graph family

Do not patch its world-axis expressions independently.

Vanilla assumptions include:
- Direction.Plane.HORIZONTAL;
- y+1 step-up;
- y-1 floor/drop;
- getFloorLevel(pos.below());
- collision shape max(Axis.Y);
- XZ diagonals;
- OPEN -> WALKABLE classification from block below;
- rail check from y-1;
- fall search in decreasing Y;
- entity width/depth in XZ and height in Y.

Planet semantic replacement:

### Cardinal graph
- four local tangent neighbors through PlanetBlockStep;
- local UP for step;
- local DOWN for floor/drop;
- transported direction after seam crossing.

### Floor
Resolve physical local-DOWN support first.
Evaluate physical collision shape and project the relevant support face/extent
through the local body frame.

Do not use physical Axis.Y max on side faces.

### Entity volume
EntityDimensions remain body-local per R4.
At a candidate navigation frame derive the physical AABB occupied by the mob.

Do not reinterpret raw world-Y height as body height.

### Falling/drop search
Search repeated local DOWN steps, preserving traversal context.

### Build-height
Minecraft min/max build Y remains a PHYSICAL world constraint.
Apply it to resolved physical positions, not to semantic local vertical.

---

## 5. Ground diagonals at seams

Vanilla diagonals are sums of two XZ unit directions.

That is invalid as a generic seam operation.

Planet diagonal policy:
1. start from the same chart-aware node;
2. evaluate the two local tangent cardinal directions;
3. evaluate both legal ordered two-step routes where necessary;
4. require the same physical/chart destination and required intermediate
   clearance;
5. otherwise reject the diagonal at that seam/corner.

This avoids:
- cutting through a folded edge;
- aliasing two corner routes;
- inventing a raw Cartesian diagonal that the surface graph does not contain.

Away from seams, behavior is vanilla-equivalent.

---

## 6. Preserve vanilla/NeoForge path classification

Do NOT replace Minecraft hazard semantics with a Planet hard-coded enum table.

Reuse physical BlockState/FluidState at the resolved candidate cell and preserve:
- doors;
- fences/walls;
- rails;
- trapdoors;
- powder snow;
- fire/damage;
- water/lava;
- cocoa/leaves;
- mob pathfinding malus.

NeoForge 1.21.1 additionally calls:
- BlockState.getBlockPathType;
- BlockState.getAdjacentBlockPathType;
- FluidState.getBlockPathType;
- FluidState.getAdjacentBlockPathType.

These extension points must receive the real physical Level/BlockPos/Mob.

Only topology/floor/body semantics are replaced.

---

## 7. Navigation mode families

### 7.1 Ground

Full chart-aware surface graph.
This is the strongest Phase-6 adapter.

### 7.2 Flying

FlyNodeEvaluator already explores a physical 3D grid.
Full 3D adjacency itself is largely rotation-invariant.

Gravity-sensitive pieces still include:
- body dimensions/clearance;
- start anchor;
- OPEN/hazard checks that use world below;
- steering yaw/pitch.

Do not force flying movement onto a surface graph.

### 7.3 Aquatic

SwimNodeEvaluator uses:
- six axial neighbors;
- four extra world-XZ diagonals;
- water/breach classification.

The axial 3D grid is physical.
Its "horizontal" diagonal family is local/body semantic and needs re-evaluation
against the chosen swimming body frame.

Final closure depends on Phase 5 fluid surface/contact semantics.

### 7.4 Amphibious

Combines WalkNodeEvaluator ground semantics with explicit world UP/DOWN
neighbors and water-border logic.

Must compose:
- Planet ground graph;
- Phase-5 fluid geometry;
- R4 body frame.

Its shallow-water penalty based on global sea level is an R6 environment policy
dependency, not generic gravity math.

### 7.5 Wall climber

Navigation target/path inherits the ground graph.
Actual climb velocity is Phase 7 living locomotion.

---

## 8. Steering/body controls

The stable boundary is:

    physical target - physical entity anchor
        -> project into entity body-local frame
        -> vanilla-semantic yaw/pitch/tangent/vertical decisions
        -> physical locomotion output through Phase 7

### MoveControl

Vanilla assumes:
- X/Z target tangent plane;
- Y target height;
- atan2 on X/Z for yaw;
- positive Y obstacle jump;
- shape max(Axis.Y);
- XZ walkability probe.

Planet should use a dedicated ground steering helper.

The current ordinal getX/getY/getZ redirects and the temporary
isWalkable()=true bypass are rejected as the long-term design.

### LookControl

Compute:
- physical target minus Planet eye position;
- project into body-local frame;
- use vanilla yaw/pitch math locally.

### BodyRotationControl

Movement detection must use local tangent displacement, not only world X/Z.

### FlyingMoveControl

Project physical target delta into body frame for authored yaw/pitch.
Resulting physical movement remains under the R4 physical velocity contract.

---

## 9. Random target generation is a separate shared layer

RandomPos does NOT merely generate arbitrary world coordinates.

Vanilla authors:
- X/Z as horizontal radius/cone;
- Y as vertical span;
- moveUpOutOfSolid via above();
- moveUpToAboveSolid via above().

For ground/body-relative AI these are local semantics.

Preferred stable helpers:
- local tangent random offset;
- local vertical range;
- local directional cone;
- local-UP escape from solid;
- local target -> physical BlockPos.

Preserve vanilla RNG call count/order.

Do NOT globally alter RandomPos for all AI modes:
- flying may intentionally want a physical 3D volume;
- aquatic depends on fluid topology;
- ground wants a local surface neighborhood.

---

## 10. MoveToBlockGoal is a strong family boundary

MoveToBlockGoal itself performs a reusable search:
- horizontal square radius;
- vertical search range;
- target approach at blockPos.above().

Many concrete goals inherit this.

Planet should adapt the family/local-search helper rather than patch each goal.

Examples then covered through the family include many:
- bed/site searches;
- harvest/block interaction goals;
- nest/home-like target searches.

### Explicit goal bypass families

Some goals contain independent world-axis logic and remain small adapters:

- DoorInteractGoal:
  path-node + local-UP door half and tangent crossing math;
- EatBlockGoal:
  local-DOWN grass lookup;
- RemoveBlockGoal:
  local DOWN/tangents + local-UP clearance;
- ClimbOnTopOfPowderSnowGoal:
  local UP;
- Cat bed validation:
  local-UP clearance;
- BreathAirGoal:
  fluid/body local vertical air search; Phase-5 integration;
- TryFindWaterGoal:
  fluid target selection + steering; Phase-5 integration.

Most chase/flee/melee goals only produce a target and call PathNavigation.
They should inherit the common graph rather than receive class-specific gravity
patches.

---

## 11. Phase-6 portability boundary

Preferred dependency direction:

    PlanetNavigationNodeKey / Planet navigation topology
        -> Minecraft 1.21.1 NodeEvaluator adapter
            -> thin navigation mixins

Stable Planet-owned code:
- chart-aware node identity;
- cardinal/diagonal graph;
- local floor/step/drop;
- waypoint anchor;
- local steering math;
- local target-generation math.

Version-sensitive wiring:
- Node/Path method layout;
- WalkNodeEvaluator private helpers;
- exact PathType hooks;
- navigation subclass method signatures.

Avoid:
- ordinal redirects as the core architecture;
- reimplementing every Goal;
- a full PathFinder fork unless later evidence proves it necessary.

---

# PART II — PHASE 7A: SIGNALS / AUTOMATION / LOGISTICS

## 12. Generic signal query boundary

SignalGetter physical neighbor enumeration is valid and stays physical.

Vanilla pattern:

    receiverPos.relative(physicalDirection)
        -> getSignal(sourcePos, same physicalDirection)

Planet must preserve:
- physical receiver;
- physical source;
- physical six-neighbor scan.

Before dispatching the Direction-sensitive source-state signal API, reframe the
Direction argument through the SOURCE block's canonical PlanetBlockStateFrame.

This common boundary must cover:
- BlockState.getSignal;
- BlockState.getDirectSignal;
- NeoForge shouldCheckWeakPower;
- other source-state hooks receiving the same query-side convention.

Do not reverse directions ad hoc.
Preserve the exact vanilla call convention; only convert the physical Direction
into the queried source state's canonical vocabulary.

On +Y this is identity.

### Neighbor notifications remain physical

Generic:
- updateNeighborsAt;
- physical NeighborUpdater fan-out;
- NeoForge NeighborNotifyEvent notifiedSides

remain physical world neighbor operations.

Semantic local graphs choose WHICH physical positions to ask/update before
calling them.

---

## 13. Redstone wire is its own tangent graph

Wire combines:
- Phase-2 local-DOWN support;
- canonical N/E/S/W connection properties;
- four local tangent candidates;
- local UP/DOWN climb/drop connections;
- signal propagation;
- manual neighboring-wire update topology.

Phase 7A owns:
- target-strength propagation;
- local wire-neighbor enumeration;
- connection-side signal semantics.

Use PlanetBlockStep rather than Direction.Plane.HORIZONTAL/raw relative().

### NeoForge wire hook

NeoForge uses BlockState.canRedstoneConnectTo(level, targetPos, direction).

The target physical BlockPos remains unchanged.
The supplied side/direction must be expressed in the TARGET block's canonical
frame according to the same physical shared edge.

Preserve:
- modded redstone connection hooks;
- shouldSignal feedback suppression;
- physical generic neighbor notifications.

Wire particles remain Phase 4.

---

## 14. Diode / repeater / comparator family

Strong shared local-tangent algorithm:

- canonical FACING;
- forward input step;
- two side inputs from local clockwise/counter-clockwise tangent directions;
- output neighbor;
- repeater lock behavior.

All actual signal queries use the generic signal boundary.

### Comparator additions

Comparator may inspect:
- one block forward;
- a second block forward behind a conductor;
- an ItemFrame in the forward physical cell.

Use traversal/walk semantics for forward cells.

ItemFrame orientation integrates with the R4 hanging-entity attachment contract.
Do not compare canonical block FACING directly to a raw physical hanging
Direction without reframing.

---

## 15. Ordinary signal sources mostly inherit the common boundary

The complete 1.21.1 signal-source sweep includes:
- powered block;
- trapped chest;
- target;
- observer;
- jukebox;
- calibrated sculk sensor;
- lever/button;
- diode family;
- redstone torches;
- lightning rod;
- daylight detector;
- lectern;
- pressure plate;
- sculk sensor;
- tripwire hook;
- detector rail;
- wire.

Most do not need a new graph.

Examples:
- pressure plate/sculk/lectern direct "UP" output becomes canonical local UP
  through the source signal boundary;
- lightning rod directional output follows canonical FACING;
- lever/button direct side follows canonical attachment state;
- target/jukebox-like all-side weak output is otherwise unchanged.

Trigger source policy can belong elsewhere:
- daylight/lightning/environment -> R6;
- entity collision triggers -> Phase 7;
- signal direction/output -> Phase 7A.

---

## 16. Torch / attachment-powered sources

Standing redstone torch:
- input support query is local DOWN;
- output excludes canonical local UP;
- direct output convention is canonical local DOWN.

Wall torch:
- attached support/input direction follows its canonical FACING family.

Phase 2 owns support/placement.
Phase 7A owns power semantics.
The generic signal boundary handles external queries.

---

## 17. Door and multiblock power queries

Door checks signal on BOTH physical cells of the semantic two-block pair.

This is not piston/dispenser quasi-connectivity.

Use Phase-2 pair topology:
- resolve both physical halves;
- call ordinary hasNeighborSignal on each.

The same principle applies to any future paired automation block:
pair topology chooses positions; SignalGetter remains physical.

---

## 18. Automation local-UP power probes

Do NOT globally redefine hasNeighborSignal.

Some machines deliberately probe an additional semantic cell.

### Dispenser / Dropper

Vanilla checks:
    hasNeighborSignal(pos)
    || hasNeighborSignal(pos.above())

Planet:
    pos
    || local-UP target

Dropper inherits the dispenser trigger family.

### Piston quasi-connectivity

Piston has a broader special graph:
- surrounding direct power excluding its facing-side rule;
- signal at piston position;
- a local-UP cell and that cell's surrounding sources.

Model this as a dedicated automation power-query helper built over:
- Planet local steps;
- generic physical SignalGetter queries.

Do not copy a world-above special case into multiple mixins.

---

## 19. Tripwire is a long line graph

TripWireHookBlock scans up to 41 cells via raw relative(FACING, distance).
TripWireBlock separately searches fixed world SOUTH/WEST directions for a hook.

Planet:
- represent the tripwire line using PlanetBlockWalk;
- transport the tangent direction at seam crossings;
- validate the opposite hook in the target chart;
- mutate physical cells once each;
- preserve schedule/update order.

Do not interpret source canonical opposite as automatically target canonical
opposite after a seam.

Signal output then uses the generic source signal boundary.

---

## 20. Sculk/calibrated signal behavior

Sculk vibration propagation is a separate physical game-event mechanism and is
not made local merely because redstone output is local.

Calibrated sensor does have a local signal-side algorithm:

    back = FACING.opposite()
    getSignal(pos.relative(back), back)

Use:
- local tangent topology for the back target;
- generic signal query for the target state.

This avoids modifying the VibrationSystem itself.

---

## 21. Powered rail signal graph

PoweredRailBlock repeats a rail topology algorithm independently from RailState:
- RailShape switch;
- physical i/j/k changes;
- ascending world Y;
- fallback one block below;
- recursion up to 8.

Do not maintain a second rail interpretation.

Phase 7A should consume the Planet-owned Phase-2 rail graph:
- follow either rail connection;
- preserve canonical RailShape semantics;
- traverse ascending local UP;
- cross seams through graph edges;
- query ordinary power at each physical rail cell.

Preserve NeoForge rail extension behavior such as getRailDirection and modded
rail subclasses.

Minecart movement remains Phase 7.4.

### Detector rail

Detector signal output uses the generic boundary.

Its minecart search AABB is also gravity-sensitive:
vanilla authors an inset rail-local volume as physical XZ + Y.

Use canonical local AABB -> physical AABB.

Final minecart/body behavior integrates with Phase 7.4.

---

## 22. Piston requires a Planet-owned push graph

PistonStructureResolver is not portable through simple Direction substitution.

Vanilla assumes:
- one constant physical pushDirection for the whole line;
- raw relative() forward/backward scanning;
- sticky branching across axes perpendicular to that constant direction;
- max 12 moved blocks.

At a Planet seam a semantic straight push can bend in physical XYZ.

Preferred architecture:

    PlanetPistonPushGraph
        source piston canonical direction
        -> PlanetBlockStep sequence
        -> transported semantic direction
        -> physical move edge per moved cell
        -> sticky branch graph
        -> ordered move/destroy result

Preserve the vanilla max-block and ordering semantics.

### Sticky adjacency

Slime/honey attachment is to physically adjacent blocks, but deciding which
branches are perpendicular to the semantic push path must use the local graph
frame.

Preserve NeoForge:
- BlockState.canStickTo;
- isSlimeBlock;
- piston push reaction hooks;
- PistonEvent.Pre/Post.

### Entity push and renderer

Piston moving-block physical displacement:
- Phase 7.5 entity force integration;
- Phase 3D rendering.

Do not convert arbitrary entity deltaMovement into local storage.

---

## 23. Block-state transport when automation moves blocks

R5 found a cross-phase gap.

PlanetBlockStateFrame can INTERPRET a canonical BlockState at a physical cell,
but there is no generic state transport API for moving an oriented state between
different gravity frames.

This matters when a piston moves a block across a seam.

Examples of properties needing a policy:
- Direction FACING;
- horizontal FACING;
- AXIS;
- FrontAndTop ORIENTATION;
- attachment/pair directions;
- modded orientation properties.

Do NOT put a property switch table inside piston mixins.

Required portability boundary:

    PlanetBlockStateTransport
        source state frame
        traversal/movement transform
        target canonical state frame
        -> transported BlockState where safely supported

Rules:
- preserve physical orientation according to the selected product/topology
  transport policy;
- use standard property/rotation APIs where possible;
- unsupported custom mod properties remain a compatibility gate;
- +Y/non-seam move is identity.

This helper also benefits any future block-moving mechanism.

---

## 24. Sided logistics has TWO separate problems

### 24.1 Target position

Hopper/dropper/crafter must first resolve the correct PHYSICAL target.

Vanilla/NeoForge currently use raw:
- pos.relative(FACING);
- hopper Y + 1 for suction;
- Direction stepX/Y/Z.

These are local semantic neighbor operations on Planet.

### 24.2 Target side

After the target physical block is known, a sided container/provider needs the
side of THAT TARGET facing the source.

Convert the physical shared face into the target block's canonical frame.

These stages must not be conflated.

Preferred helper:

    PlanetSidedLogisticsEdge
        source position/state
        source semantic direction
        physical target
        physical shared face
        target canonical side

---

## 25. Hopper family

### Output

Hopper FACING is canonical local output direction.
Resolve target through Planet topology.

### Input/suction

Vanilla hopper suction assumes source at world +Y.

Planet source is local UP.

Also rotate the item-entity collection volume authored above the hopper into a
physical AABB.

### WorldlyContainer path

Vanilla directly calls:
- getSlotsForFace;
- canPlaceItemThroughFace;
- canTakeItemThroughFace.

Supply the target canonical side from PlanetSidedLogisticsEdge.

### NeoForge capability path

NeoForge VanillaInventoryCodeHooks repeats raw neighbor math before calling
Capabilities.ItemHandler.BLOCK.

Therefore the existing Planet capability-side adapter is necessary but NOT
sufficient.

The logistics adapter must choose the correct physical target first.
The generic capability adapter then remains a safety/compatibility boundary for
all direct mod queries.

---

## 26. Dropper / dispenser family

### Dropper insertion

Resolve canonical FACING through Planet topology.
Use target canonical input side for:
- vanilla container;
- NeoForge item capability.

### Dispenser emission point

Dispenser FACING is canonical local.
Convert the authored local dispense point to physical world position.

### Default item launch

DefaultDispenseItemBehavior contains two semantics:
- forward component from FACING;
- unconditional positive world-Y launch bias.

On Planet:
- forward = physical transform of canonical FACING;
- +0.2 launch term = local UP;
- random spread remains the same local-authored stochastic vector;
- preserve RNG call count/order;
- transform final vector to physical world deltaMovement.

Do not merely pass a physical Direction into the vanilla method and leave the
world-Y bias unchanged.

### Projectile dispenser

Once dispenser-local geometry has produced:
- physical spawn point;
- physical forward vector/direction,

ProjectileItem/asProjectile/shoot should receive world-space inputs.
Projectile flight is Phase 7.3.

---

## 27. Crafter / FrontAndTop family discovered by R5

R5 found a missing R1 orientation family.

Crafter stores:
    FrontAndTop ORIENTATION

Jigsaw also uses the same vocabulary.

FrontAndTop is a pair:
- front Direction;
- top Direction.

### Cross-phase ownership

Phase 2A:
- canonical FrontAndTop placement/orientation vocabulary.

Phase 7A:
- Crafter redstone trigger;
- output target;
- sided insertion;
- fallback dispense.

R6/worldgen:
- Jigsaw runtime/structure semantics where applicable.

Crafter output uses ORIENTATION.front() for:
- target container;
- NeoForge capability target;
- fallback item spawn.

All must pass through the same local->physical logistics/dispense boundary.

---

## 28. NeoForge automation extension points to preserve

Signal:
- shouldCheckWeakPower;
- canRedstoneConnectTo;
- NeighborNotifyEvent.

Pathfinding:
- block/fluid path type hooks.

Piston:
- PistonEvent.Pre/Post;
- canStickTo;
- isSlimeBlock;
- push-reaction callbacks.

Inventory/logistics:
- VanillaInventoryCodeHooks;
- ItemHandler block capability;
- entity automation item capability;
- existing BlockCapability cache/invalidation semantics.

Capabilities:
- physical queried BlockPos remains authoritative;
- provider side is target canonical local;
- null side remains null.

Planet must adapt upstream topology rather than bypass these hooks.

---

## 29. Phase-6 deterministic tests

### Node identity
- same physical seam BlockPos + different traversal face => different node key;
- same physical/chart => same node;
- +Y ordinary cells match vanilla position identity.

### Ground graph
All six faces:
- four tangents;
- local step UP;
- local drop DOWN;
- floor height;
- body clearance.

Seams:
- 24 directed edge transitions;
- chart transport;
- no node alias;
- diagonal consistency/rejection;
- exact corner bounded search.

### Path semantics
- path equality includes chart where relevant;
- waypoint physical anchor from stored node frame;
- timeout identity does not merge chart states;
- vanilla PathFinder can reconstruct Planet node chain.

### Path types
- doors;
- fences;
- rail;
- water/lava;
- damage;
- NeoForge custom BlockState path type;
- NeoForge custom FluidState path type.

### Steering
- physical target -> local yaw/vertical/tangent;
- all six body frames;
- look pitch/yaw;
- body rotation movement detection.

### Target generation
- RNG-equivalent +Y baseline;
- local tangent radius;
- local vertical range;
- local-UP escape;
- toward/away cone.

### Goal family
- MoveToBlock local volume;
- door local pair/approach;
- local-DOWN eat/remove;
- fluid air/water integration gate.

---

## 30. Phase-6 single runtime acceptance matrix

One batch, not per mob class:

1. +Y vanilla baseline.
2. ordinary random stroll on each rotated face.
3. chase a target.
4. flee from target.
5. one-block local step UP.
6. safe local drop DOWN.
7. door traversal.
8. fence/rail/trapdoor hazard representative.
9. seam crossing in both directions for representative edge orientations.
10. no edge spin/oscillation.
11. exact corner bounded behavior.
12. MoveToBlock-family representative.
13. local look/body steering.
14. flying representative.
15. wall-climber representative.
16. aquatic/amphibious after Phase 5.
17. NeoForge custom path-type representative.
18. many-mob pathfinding performance / maxVisitedNodes smoke test.

The old edge-overshoot workaround is NOT an acceptance criterion.

---

## 31. Phase-7A deterministic tests

### Signal boundary
- all six physical query directions x all six source gravity faces;
- +Y identity;
- weak/direct signal;
- shouldCheckWeakPower;
- target canonical side.

### Wire
- four local connections;
- local UP climb;
- local DOWN drop;
- seam connectivity;
- mod canRedstoneConnectTo target side.

### Diode/comparator
- forward;
- left/right side inputs;
- repeater lock;
- two-block comparator input;
- hanging item-frame orientation.

### Pair/line graphs
- door both halves;
- tripwire straight + seam line;
- calibrated sensor back input.

### Rail
- powered recursion straight/ascending/seam;
- recursion limit;
- detector physical AABB transform.

### Piston
- local power/quasi probe;
- straight push;
- retract;
- destroy reaction;
- max 12;
- slime/honey branches;
- seam push transport;
- block-state orientation transport;
- NeoForge Pre/Post/canStickTo.

### Logistics
- hopper local-UP suction;
- hopper output all canonical directions;
- WorldlyContainer side;
- NeoForge item capability side;
- dropper target;
- crafter FrontAndTop front target;
- dispenser item local-UP bias;
- projectile physical launch;
- +Y RNG equivalence.

---

## 32. Phase-7A single runtime acceptance matrix

1. +Y redstone/logistics baseline.
2. wire flat/local-UP/local-DOWN connections on rotated faces.
3. wire seam connection.
4. repeater input/output/lock.
5. comparator front/side/analog two-block input.
6. observer.
7. torch + wall torch.
8. lever/button/pressure plate.
9. door pair power.
10. tripwire line including one seam.
11. calibrated sculk side input.
12. powered rail recursion straight/slope/seam.
13. detector rail minecart detection.
14. piston extend/retract.
15. piston slime/honey branch.
16. piston seam move + oriented moved block.
17. hopper output.
18. hopper local-UP suction.
19. vanilla sided container.
20. NeoForge item capability target/side.
21. dropper insertion.
22. dispenser item trajectory.
23. projectile dispenser trajectory.
24. crafter output/insertion.
25. no duplicate neighbor/update storms.
26. representative third-party directional machine/pipe.
27. automation performance smoke test.

---

## 33. Cross-phase dependencies found by R5

R5 adds/clarifies:

- Phase 2A:
  FrontAndTop canonical orientation family (Crafter/Jigsaw).
- Phase 2F:
  Planet rail graph is consumed by powered rail.
- Phase 3:
  wire/rail/piston/crafter rendering remains render-owned.
- Phase 4:
  wire/torch/lightning/sculk/etc particles remain emitter-owned.
- Phase 5:
  aquatic/amphibious AI and fluid logistics interaction.
- Phase 7:
  detector minecart, piston entity pushes, projectile dispenser flight.
- Phase 7B:
  interaction/network stays physical as established by R4.
- R6:
  daylight/lightning/environment trigger policy and Jigsaw/worldgen integration.
- Phase 10:
  custom orientation properties, custom raw-axis AI, pipes/machines and custom
  piston/fluid/redstone algorithms as compatibility gates.

---

## 34. R5 completion decision

R5 is COMPLETE when:
- navigation graph identity is chart-aware;
- vanilla A* reuse vs Planet node/path responsibilities is explicit;
- ground/flying/aquatic/amphibious/wall-climber families are separated;
- steering and target generation have shared body/local boundaries;
- common Goal families vs true bypasses are classified;
- generic signal query direction ownership is explicit;
- wire/diode/tripwire/rail/piston graph families are separated;
- hopper/dropper/dispenser/crafter upstream target math is separated from
  capability side conversion;
- NeoForge hooks are preserved;
- FrontAndTop and block-state transport cross-phase gaps are recorded;
- deterministic and batch acceptance matrices are defined.

After R5, global research advances to R6.
Runtime remains frozen.
