# Block/world semantic topology matrix — Minecraft 1.21.1

Status: R1 COMPLETE research result for Planetary 2.0.  
Runtime target researched: Minecraft 1.21.1 / NeoForge 21.1.215.

This document is the concrete Phase-2 family matrix produced by global research
batch R1.

It complements:
- `GRAVITY_IMPACT_AUDIT_1_21_1.md`
- `GRAVITY_MECHANISM_MAP_1_21_1.md`
- `PLACEMENT_1_21_1.md`
- `SUPPORT_UPDATES_1_21_1.md`
- `GROWTH_CONNECTIONS_1_21_1.md`
- `MULTIBLOCK_PLACEMENT_1_21_1.md`

The purpose is NOT to enumerate every block. The purpose is to map every
gravity-sensitive BLOCK/WORLD semantic family to the vanilla algorithm that
owns it.

---

## 1. R1 source coverage

The R1 audit covered these mechanism families directly in 1.21.1 source:

Placement/context:
- UseOnContext / BlockPlaceContext;
- BlockItem;
- StandingAndWallBlockItem;
- DirectionalPlaceContext;
- DirectionalBlock / HorizontalDirectionalBlock;
- RotatedPillarBlock;
- FaceAttachedHorizontalDirectionalBlock;
- slab/stair/trapdoor click-offset placement.

Support/update:
- canSurvive/sturdy-face patterns;
- BaseTorch/WallTorch;
- ladder;
- lantern;
- signs/hanging signs;
- coral wall attachments;
- amethyst cluster full-direction attachments;
- pressure plates;
- diode support;
- physical NeighborUpdater/CollectingNeighborUpdater fan-out.

Multiblock/pairs:
- DoorBlock;
- BedBlock;
- DoublePlantBlock;
- chest + DoubleBlockCombiner;
- pointed dripstone vertical topology.

Connection graphs:
- CrossCollisionBlock;
- FenceBlock / IronBars / TripWire;
- WallBlock;
- MultifaceBlock;
- GlowLichen/SculkVein;
- VineBlock.

Runtime growth/ecology:
- BushBlock family;
- GrowingPlantBlock/Head/Body;
- cactus;
- sugar cane;
- bamboo;
- vine spread;
- scaffolding distance graph;
- spreading snowy dirt / snow cover.

Rails:
- BaseRailBlock;
- RailState;
- ordinary RailBlock;
- powered/detector rail boundary classification.

Support-triggered/falling:
- FallingBlock;
- BrushableBlock;
- pointed dripstone;
- scaffolding.

Waterlogged block hooks:
- SimpleWaterloggedBlock;
- WATERLOGGED scheduling paths in representative block families.

The audit also checked the inheritance inventory for major family roots rather
than assuming a representative class was the whole family.

---

## 2. Fundamental R1 conclusions

### 2.1 Physical world traversal and semantic local direction are different types

The single most important block-side rule:

    canonical local Direction
        != physical BlockPos step

A local semantic direction may be stored in BlockState, but raw:

    pos.relative(direction)

always performs a physical world step.

Therefore a generic "rotate Direction everywhere" patch is incorrect.

The correct pattern is:

1. read canonical local semantic direction;
2. use Planet frame/traversal topology to obtain the physical target;
3. when the target block interprets a side, convert the physical relation into
   that target's canonical frame.

Existing PlanetBlockStep / PlanetBlockWalk / PlanetBlockFrameContext are the
correct foundational layer.

### 2.2 NeighborUpdater must remain physical

NeighborUpdater.UPDATE_ORDER iterates:

    WEST, EAST, DOWN, UP, NORTH, SOUTH

and physically notifies six adjacent world cells.

That is correct.

Do NOT rotate this fan-out.

Gravity semantics begin when the receiving block interprets:
- which local side changed;
- whether that side is support;
- which local graph edge the physical neighbor represents.

### 2.3 HorizontalDirectionalBlock is vocabulary, not one behavior family

HorizontalDirectionalBlock has many descendants, but it mostly establishes a
property domain.

Its descendants include unrelated algorithms:
- ladder support;
- door pairing;
- chest pairing;
- diode signal;
- stair shape;
- fence gate;
- wall sign;
- bed;
- cocoa/stems;
- furnace-like blocks.

Therefore:
- share canonical local FACING helpers;
- do not create one giant behavioral mixin on HorizontalDirectionalBlock.

### 2.4 Build-height is physical world policy, not local gravity

Vanilla vertical multiblock/growth code often checks:
- getY();
- min/max build height.

For Planet, LOCAL UP may be physical X/Z.

The semantic operation should first compute the actual physical target position.
Then world-bound checks apply to that physical position.

Do not rotate build-height limits themselves.

### 2.5 Waterlogging hooks are mostly orientation-independent scheduling

SimpleWaterloggedBlock itself:
- checks WATERLOGGED;
- writes fluid state at the same position;
- schedules the fluid tick at the same position.

That part does not require local-direction transformation.

The gravity-sensitive part is the BLOCK algorithm that decides which neighbor
changed or where support/growth occurs.

Actual fluid propagation/height/flow belongs to Phase 5.

---

## 3. Phase 2A — placement input and canonical state orientation

### Stable engine boundary

Physical input:
- clicked BlockPos;
- physical hit face;
- physical hit Vec3.

Semantic local view:
- nearest-looking direction;
- nearest-looking vertical direction;
- ordered nearest directions;
- local tangent direction;
- click offset relative to local UP/DOWN.

Preferred Planet API:
- PlanetBlockPlacementFrame;
- PlanetPlacementRuntime.

### Families

#### A. RotatedPillarBlock family

Owner:
`RotatedPillarBlock.getStateForPlacement`

Semantic:
clicked/placement AXIS.

Planet:
AXIS property is canonical local axis.

Current:
implemented.

Portability:
excellent base-family boundary.

#### B. Full 3D FACING family

Examples:
- observer;
- piston placement aspect;
- dispenser/dropper;
- lightning rod;
- amethyst cluster;
- shulker box / directional devices.

Semantic:
canonical local full Direction.

Important:
support and signal behavior may belong elsewhere.

#### C. Horizontal FACING family

Examples:
- ladder;
- stair;
- trapdoor;
- bed;
- chest;
- diode;
- furnace-like blocks;
- fence gate;
- wall signs/banners/skulls;
- lectern/beehive/stonecutter.

Rule:
property value is canonical LOCAL tangent direction.

Do not store a physical side in a property whose domain is horizontal-only.

#### D. FaceAttachedHorizontalDirectionalBlock

Shared owner:
`FaceAttachedHorizontalDirectionalBlock`

Concrete family:
- LeverBlock;
- ButtonBlock;
- GrindstoneBlock;
- BellBlock.

Shared semantics:
- FLOOR/WALL/CEILING AttachFace;
- tangent FACING;
- support side;
- survival;
- update invalidation.

This is a strong base-family adapter boundary.

Current:
partial/implemented foundation.

#### E. StandingAndWallBlockItem family

Shared item-level algorithm:
- inspect ordered nearest-looking directions;
- choose standing vs wall variant;
- ask candidate state canSurvive.

Examples:
- torch variants;
- sign/banner/skull standing/wall combinations;
- analogous modded pairs.

Use:
local direction ordering + existing support API.

Do not change physical clicked position.

#### F. Lantern/hanging boolean family

LanternBlock is not a FaceAttached subclass but shares the semantic concept:
- HANGING=true means local-UP support;
- HANGING=false means local-DOWN support.

Recommendation:
share support-direction helper, not inheritance mixin.

#### G. Slab/stair/trapdoor local-half placement

These use:
- clicked face;
- fractional click coordinate;
- vertical/horizontal orientation.

Rule:
evaluate TOP/BOTTOM/HALF using local-frame click offset.

Current slab foundation exists.
Stair/trapdoor need family completion.

### Phase-2A test strategy

Pure:
- physical clicked face -> canonical local direction all six faces;
- local click offsets;
- nearest-direction ordering;
- canonical horizontal property validity.

Runtime representative:
- pillar;
- one full-FACING device;
- one horizontal-FACING block;
- face-attached block;
- standing/wall item;
- lantern;
- slab/stair/trapdoor.

---

## 4. Phase 2B — support, survival and update semantics

### Shared support concept

Given source block + canonical local support direction:

- seam-aware physical support target;
- target canonical face toward source;
- support predicate against physical rotated shape;
- physical neighbor update interpreted as local source side.

Existing:
- PlanetBlockSupportQuery;
- PlanetBlockSupportRuntime.

### Family B1 — local-DOWN support

Examples:
- BushBlock;
- pressure plates;
- rails;
- diodes/repeaters/comparators support aspect;
- doors lower-half ground support;
- many plants;
- scaffolding initial support.

Vanilla pattern:

    pos.below()
    support.isFaceSturdy(... Direction.UP)

Planet:
local DOWN target + target local side toward source.

### Family B2 — state-directed support

Examples:
- ladder;
- wall torch;
- amethyst cluster;
- wall coral fan;
- wall signs/skulls/banners;
- wall hanging sign lateral supports.

Vanilla pattern:

    direction = state.FACING
    support = pos.relative(direction.opposite)

Planet:
FACING is canonical local.
Physical step must use frame/traversal conversion.

### Family B3 — attach-face support

Owner:
FaceAttachedHorizontalDirectionalBlock.

AttachFace:
- FLOOR -> local DOWN support;
- CEILING -> local UP support;
- WALL -> local FACING.opposite support.

Current:
foundation implemented.

### Family B4 — hanging/center support

Lantern:
- local UP or DOWN based on HANGING;
- Block.canSupportCenter.

Ceiling hanging sign:
- local UP support;
- special relation with another hanging sign;
- face-full/support check is on the physical face toward the source.

Wall hanging sign:
- two local tangent side supports using clockwise/counter-clockwise directions.

This deserves a hanging-support algorithm family, not one generic "sign fix".

### Update rule

NeighborUpdater remains physical.

At receiving block boundary:
- derive physical source relation;
- map to canonical local side;
- run support/graph logic.

Do not globally replace updateShape's Direction argument unless the method's
internal physical traversal is also adapted.

### Phase-2B representative acceptance

- remove local-DOWN support;
- remove local-UP hanging support;
- remove wall support;
- remove one of two wall-hanging-sign side supports;
- check no duplicate/drop loop;
- all five rotated faces + +Y baseline.

---

## 5. Phase 2C — multiblock and pair topology

### Family C1 — local vertical two-part blocks

Strong owner:
`DoublePlantBlock`

Vanilla assumptions:
- UPPER/LOWER;
- above()/below();
- update side UP/DOWN;
- placement requires replaceable above block.

Family:
- tall flowers;
- tall grass;
- tall seagrass;
- small dripleaf;
- pitcher crop;
- related descendants.

Planet:
- UPPER means local UP part;
- LOWER means local DOWN part;
- seam-aware step;
- target physical world bounds checked AFTER target is computed.

DoublePlantBlock is a good base-family adapter.

### Family C2 — door

Same local vertical pair concept, plus:
- local tangent FACING;
- hinge;
- redstone open state later in Phase 7A.

Current:
partially implemented.

Do not treat redstone completion as Phase 2.

### Family C3 — bed

Directed local tangent pair:
- FOOT -> HEAD along canonical local FACING.

Current:
topology implementation exists.

Renderer remains Phase 3.
sleep/spawn/entity logic remains later phase.

### Family C4 — chest / DoubleBlockCombiner

Chest state:
- canonical local FACING;
- LEFT/RIGHT/SINGLE.

Important vanilla boundary:

DoubleBlockCombiner accepts a function returning a Direction and then performs:

    pos.relative(direction)

Therefore DoubleBlockCombiner itself should remain generic/physical.

Planet caller must derive the PHYSICAL traversal step from canonical pair
semantics before using the combiner, or use a Planet pair-combiner adapter.

Do not globally reinterpret every DoubleBlockCombiner Direction.

### Family C5 — vertical chains

Examples:
- pointed dripstone;
- analogous modded chains.

Requires:
- local UP/DOWN neighbor traversal;
- chain state derivation;
- support/fall trigger;
- seam-aware walk.

Current pointed-dripstone foundation exists.

### Phase-2C acceptance

Representative:
- door;
- DoublePlant;
- bed;
- double chest;
- dripstone chain;
- seam-crossing pair where policy permits;
- physical build-limit edge for a local-UP target.

---

## 6. Phase 2D — local tangent and all-face attachment graphs

This phase needs TWO shared traversal primitives, not one universal state
mutator.

### D1 — four-local-tangent graph

Canonical directions:
- local NORTH/EAST/SOUTH/WEST.

Used by:
- FenceBlock;
- IronBarsBlock;
- TripWireBlock;
- WallBlock sides;
- scaffolding distance;
- cactus side-clearance;
- sugar-cane water-neighbor search;
- portions of vine growth.

Existing seam-aware local tangent walk should be reused.

### CrossCollisionBlock family

CrossCollisionBlock mainly owns:
- shape/state combinations;
- canonical N/E/S/W properties.

Concrete descendants own actual connection predicates.

FenceBlock:
- four physical neighbor queries in vanilla;
- sturdy side tested toward source;
- update only for world horizontal direction in vanilla.

Planet:
local tangent neighbor set.

Current:
Fence foundation implemented.

IronBars/TripWire:
same tangent topology, different connection/state rules.

### WallBlock is not merely CrossCollisionBlock

WallBlock has:
- four WallSide properties;
- local UP post;
- neighbor above;
- DOWN face of above collision shape;
- special post-raising algorithm.

Planet adaptation must rotate BOTH:
- tangent connections;
- local-UP post/above-block logic.

WallBlock should be its own algorithm-family adapter using shared tangent/support
primitives.

### D2 — six-face attachment graph

Owner:
`MultifaceBlock`

It can attach to any supported canonical face, not only tangent faces.

Examples:
- GlowLichenBlock;
- SculkVeinBlock;
- VineBlock derives related face-property behavior but adds unique growth.

Planet requirements:
- canonical face properties;
- physical target lookup via local/traversal step;
- target physical support face;
- seam-safe face removal/update.

MultifaceBlock is a candidate base-family boundary.

### VineBlock is a special growth graph

Vine explicitly:
- excludes DOWN attachment;
- has UP + four tangent properties;
- random growth uses above/below and tangent directions;
- diagonally composes tangent steps;
- checks support on several derived neighbors.

It must not be considered complete from Multiface support alone.

Treat Vine as Phase-2D + Phase-2E integration.

---

## 7. Phase 2E — runtime growth and ecology

### E1 — BushBlock support family

Strong base owner:
`BushBlock.canSurvive`

Vanilla:
support below.

Large descendant family includes:
- crops;
- flowers;
- saplings;
- mushrooms;
- roots;
- berry bushes;
- nether plants;
- seagrass;
- double plants.

Base support can be shared.
Subclass random growth still requires family audit.

### E2 — GrowingPlantBlock directional family

Strong base owner:
`GrowingPlantBlock`

It already stores an explicit `growthDirection`.

Vanilla uses raw:

    pos.relative(growthDirection)

for:
- placement/body conversion;
- support;
- head growth;
- scheduled survival.

Planet:
growthDirection is canonical LOCAL semantic direction.
All physical steps must use Planet traversal.

Strong base-family adapter candidate.

Covers head/body systems such as:
- kelp;
- cave vines;
- weeping/twisting vines;
- analogous growing plants.

### E3 — fixed local-UP columns

Examples:
- cactus;
- sugar cane;
- bamboo.

These are not all BushBlock behavior.

Shared concepts:
- local DOWN support;
- local UP growth;
- column scan local UP/DOWN.

Cactus additionally:
- four local tangent clearance;
- lava-neighbor check.

Sugar cane additionally:
- water/frosted-ice lookup around support block in four local tangents.

Bamboo additionally:
- leaves/age propagation along local column;
- brightness check at local-UP target (brightness itself remains environmental).

Recommendation:
shared local-column traversal helper + class/family adapters.

### E4 — spreading surface ecology

Already partially implemented:
- SpreadingSnowyDirtBlock;
- SnowyDirtBlock.

Concepts:
- local UP cover/surface;
- local tangent/local vertical candidate spread;
- seam traversal.

Worldgen placement remains Phase 8.

### E5 — vine spread

See D2.

Random-growth graph is unique enough for dedicated Vine adapter using shared
six-face/tangent traversal.

### E6 — scaffolding stability graph

Scaffolding is a graph, not a simple support block.

Vanilla:
- starts at pos.below();
- sturdy support from DOWN;
- scans four HORIZONTAL neighbors;
- propagates DISTANCE up to 7;
- BOTTOM depends on below.

Planet:
- local DOWN support;
- local tangent neighbors;
- distance propagation through local graph;
- BOTTOM canonical local concept;
- waterlogged scheduling unchanged.

Dedicated algorithm-family adapter required.

### Phase-2E acceptance

Representative:
- simple Bush/crop;
- cactus;
- sugar cane;
- bamboo;
- GrowingPlant head/body;
- vine;
- scaffolding;
- spreading grass/snow;
- seam growth in both directions;
- no scheduled-tick explosion.

---

## 8. Phase 2F — rail BLOCK graph

R1 confirms rails are a dedicated graph engine.

### 8.1 BaseRailBlock responsibilities

Vanilla BaseRailBlock owns:
- local-equivalent support check via pos.below();
- initial NORTH_SOUTH / EAST_WEST from player horizontal direction;
- placement/update entry into RailState;
- support-removal checks;
- waterlogging tick scheduling;
- neighbor notifications above/below for some rail types.

Planet:
- local DOWN support;
- initial canonical RailShape from local tangent orientation;
- physical neighbor notifications remain physical where they are generic;
- rail-specific semantic notifications use graph adapter.

### 8.2 RailState is the actual topology engine

Vanilla RailState contains hard-coded world topology:

Connections:
- north/south/east/west;
- tangent neighbor + world above for ascending shape.

Neighbor discovery:
- same position;
- above;
- below.

Potential connections:
- Direction.Plane.HORIZONTAL.

Shape selection:
- physical north/south/east/west booleans;
- physical above checks.

Propagation:
- recursively creates RailState for neighbors;
- removeSoftConnections;
- connectTo;
- reconnects neighbors after state change.

### 8.3 Critical physical-XZ bug

RailState.hasConnection compares only:

    blockpos.getX() == target.getX()
    && blockpos.getZ() == target.getZ()

This intentionally ignores vanilla Y because a slope connection may differ in
height.

On a Planet side face, physical X/Z are NOT the canonical tangent plane.

Therefore simply replacing above()/below() is insufficient.

The Planet rail graph must compare/identify connections in LOCAL/TRAVERSAL
topology.

### 8.4 Required Planet rail abstraction

Do not copy all of RailState into one giant mixin if avoidable.

Preferred version-portable design:
- Planet-owned rail graph helper/service;
- input node = physical position + traversal/canonical frame context;
- canonical RailShape interpretation;
- local tangent step;
- optional local UP/DOWN slope lookup;
- neighbor identity based on topology, not physical X/Z column assumptions;
- connection/reconnect algorithm isolated from mixin wiring.

The exact vanilla RailState hook is version-sensitive.
The graph semantics should be Planet-owned and testable.

### 8.5 Dependencies

Phase 2F owns:
- placement/support;
- ordinary rail connection/slope/reconnect graph.

Phase 3 owns:
- visual/model/shape equivalence.

Phase 5 owns:
- waterlogging fluid behavior beyond scheduling hook.

Phase 7A owns:
- powered/detector/activator signal propagation.

Phase 7.4 owns:
- minecart movement over canonical RailShape.

### 8.6 Rail pure tests

All six faces:
- straight N/S;
- straight E/W;
- four curves;
- four ascending shapes;
- neighbor same/local-UP/local-DOWN resolution;
- remove/reconnect;
- seam crossing;
- graph identity independent of physical XZ assumption;
- +Y exact vanilla equivalence.

### 8.7 Rail runtime acceptance

One network matrix:
- place straight/curve/slope;
- extend existing network;
- remove middle rail;
- reconnect alternatives;
- support removal;
- seam crossing;
- ordinary rail first.

Powered/detector/activator behavior is tested later with Phase 7A.
Minecart is tested later with Phase 7.

---

## 9. Phase 2G — falling/support-triggered block semantics

### FallingBlock family

Vanilla block-side behavior:
- onPlace/updateShape schedule block tick;
- tick checks `pos.below()`;
- if free, spawn FallingBlockEntity;
- global min-build-height guard.

Planet:
- check local DOWN physical target;
- if free, spawn entity through local-down anchor;
- world build bounds apply to actual physical target/current position, not
  "local height".

Current:
partial runtime foundation exists.

### BrushableBlock

Repeats the same block-side fall trigger pattern rather than simply inheriting
FallingBlock behavior.

Therefore the reusable owner should be a Planet falling-trigger helper, not only
a FallingBlock mixin.

### Pointed dripstone

Combines:
- local chain topology;
- support;
- scheduled falling trigger;
- spawned entity.

Current partial implementation exists.

### Scaffolding

Collapse is derived from its stability graph, not FallingBlock.isFree.
Owned primarily by E6 with spawned falling behavior integrated here if needed.

### Phase split

Block-side decision/spawn trigger -> Phase 2G.
Falling entity movement/damage -> Phase 7.3.
Falling visual -> Phase 3.
Particles -> Phase 4.

---

## 10. Phase 2H — waterlogged block hooks

R1 conclusion:
SimpleWaterloggedBlock is mostly frame-independent.

Its default methods operate at the same BlockPos:
- canPlaceLiquid;
- set WATERLOGGED;
- schedule fluid tick;
- pickupBlock.

No local direction is inherent there.

Phase-2 requirements:
- preserve WATERLOGGED state when block state is transformed;
- ensure family updateShape adapters still schedule the fluid tick;
- do not cancel/bypass vanilla scheduling accidentally;
- preserve SimpleWaterloggedBlock/FluidContainer extension points.

Phase 5 owns:
- local fluid DOWN;
- tangent spread;
- fluid height/shape;
- current;
- rendering;
- water/lava interactions.

---

## 11. Cross-phase ownership examples

### Rail

Phase 2:
support + RailState graph.

Phase 3:
shape/model/render.

Phase 5:
waterlogging integration.

Phase 7:
minecart movement.

Phase 7A:
rail signals.

### Door

Phase 2:
placement/pair/support.

Phase 3:
shape/model.

Phase 6:
mob navigation/door pathing.

Phase 7A:
redstone power/open propagation.

### Chest

Phase 2:
FACING + pair topology.

Phase 3:
BER.

Phase 7A:
sided logistics/capability interaction where applicable.

### Redstone wire

Phase 2:
support + local tangent/climb connection geometry foundation.

Phase 4:
particles.

Phase 7A:
power/signal propagation.

### Scaffolding

Phase 2:
support/distance/collapse graph.

Phase 3:
shape/render.

Phase 7:
entity climb/fall interaction.

Phase 5:
waterlogging integration.

---

## 12. Version-portability hotspots from R1

Preferred stable Planet-owned algorithms:
- placement frame conversion;
- support query;
- local/traversal step;
- local tangent neighbor enumeration;
- pair traversal;
- local vertical column traversal;
- rail graph;
- scaffolding stability graph.

Version-sensitive wiring:
- exact getStateForPlacement/updateShape signatures;
- class hierarchy changes;
- private helper locations;
- RailState method layout;
- vanilla property names/domains;
- scheduling hook signatures.

Avoid:
- large copied vanilla methods inside mixins;
- private invokers when a Planet helper can own the algorithm;
- local-variable ordinal injection as the primary semantic boundary.

When mirroring a vanilla algorithm is unavoidable:
- document exact version/source owner;
- extract semantic math into Planet helper;
- add +Y vanilla-equivalence tests.

---

## 13. Phase-2 deterministic test matrix

### Frame/placement
- all six faces;
- clicked-face conversion;
- nearest-looking order;
- local click Y;
- full FACING / horizontal FACING / AXIS / HALF.

### Support
- local DOWN;
- local UP;
- wall;
- state-directed arbitrary face;
- two-side hanging support;
- seam support target.

### Pair topology
- local vertical pair;
- tangent head/foot pair;
- chest left/right;
- chain.

### Connection graph
- four tangent directions;
- all-face attachment;
- wall UP post;
- vine diagonal growth;
- seam target canonical frame.

### Growth
- local column;
- tangent clearance;
- local support;
- random/scheduled propagation without loops.

### Rail
see section 8.6.

### Falling
- free local-DOWN target;
- blocked local-DOWN target;
- seam target;
- physical world-bound guard.

### Waterlogged
- scheduled fluid tick still emitted after semantic update;
- no duplicate scheduling introduced by adapter.

---

## 14. Phase-2 single runtime acceptance matrix

Do not test every block.

Represent distinct mechanisms:

1. +Y vanilla baseline.
2. Rotated pillar.
3. Full-FACING attached block.
4. FaceAttached family representative.
5. standing/wall representative.
6. lantern/hanging support.
7. slab + stair + trapdoor.
8. door + DoublePlant + bed + double chest.
9. fence + wall + iron bars.
10. multiface + vine.
11. crop/Bush + cactus/sugar cane/bamboo.
12. GrowingPlant head/body.
13. scaffolding.
14. ordinary rail network:
    straight/curve/ascending/reconnect/seam.
15. falling/support-trigger representative.
16. waterlogged representative ensuring scheduled fluid behavior is preserved.
17. support removal for DOWN/UP/wall/two-side variants.
18. edge/seam cases only where the family owns traversal topology.
19. update-loop/performance smoke check.

Phase-2 acceptance explicitly excludes:
- redstone signal correctness;
- minecart physics;
- fluid simulation;
- BER/render completion;
- mob navigation.

Those are later owning phases.

---

## 15. R1 completion decision

R1 is COMPLETE when this matrix, the mechanism map and the roadmap agree on:
- owners;
- family boundaries;
- cross-phase dependencies;
- portability hotspots;
- deterministic tests;
- batch runtime acceptance.

No further runtime implementation should occur until the global research plan
moves through R2-R6 and the final completeness sweep.

New block-specific discoveries during later research should only reopen R1 if
they reveal a genuinely missing BLOCK semantic mechanism, not merely another
concrete class using one of the families above.
