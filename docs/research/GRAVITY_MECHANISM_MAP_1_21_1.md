# Gravity-dependent mechanism map — Minecraft 1.21.1

Status: authoritative mechanism/phase classification for Planetary 2.0.  
Target: Minecraft 1.21.1 / NeoForge 21.1.215.

This document complements `GRAVITY_IMPACT_AUDIT_1_21_1.md`.

The master impact audit answers:

> What parts of Minecraft can be affected by non-world-Y gravity?

This document answers the next architectural question:

> Which INTERNAL VANILLA MECHANISM owns each behavior, which concrete classes
> share that mechanism, which Planet phase must implement it, and which other
> phases must re-check it?

The roadmap must be organized by these mechanisms, not by a flat list of blocks
or visible bugs.

---

## 1. Audit method and scope

The audit used the Minecraft 1.21.1 source tree from
`hackersense/OptiFine-Source` as a readable Mojang-mapped source reference.

The 1.21.1 tree contains 5,171 Java files under `net/minecraft`.

Broad source sweeps searched for semantic-axis patterns including:

- `.above()`, `.below()`;
- `Direction.UP`, `Direction.DOWN`;
- `Direction.Plane.HORIZONTAL`;
- `Direction.Axis.Y`;
- placement helpers such as `getClickedFace()`,
  `getHorizontalDirection()`, `getNearestLookingDirections()`;
- `onGround`, `fallDistance`, vertical/horizontal collision;
- raw Y acceleration / `yd`;
- `horizontalDistance*`;
- heightmaps / precipitation / build-height scans;
- fluid height / flow;
- support and sturdy-face queries;
- neighbor updates and signal queries.

Representative call flows were then inspected manually rather than treating a
text match as automatically gravity-dependent.

Important source families inspected in detail include:

- `BlockPlaceContext`, `BlockItem`, `StandingAndWallBlockItem`;
- `FaceAttachedHorizontalDirectionalBlock`, `RotatedPillarBlock`,
  slab/stair/trapdoor/door placement;
- `BaseRailBlock`, `RailState`, powered/detector rails,
  `AbstractMinecart`;
- `CrossCollisionBlock`, fence/wall/multiface/vine/scaffolding;
- `BushBlock`, `GrowingPlantBlock`, `DoublePlantBlock`;
- `SignalGetter`, `RedStoneWireBlock`, diode/repeater/comparator,
  observer;
- piston base, structure resolver and moving piston block entity;
- `Entity.move`, `LivingEntity`, projectiles, item/XP/falling entities,
  boats and minecarts;
- `FlowingFluid`, `LiquidBlock`, `FluidState`, bucket and liquid renderer;
- `Particle` and custom particle ticks/emitters;
- `ModelBlockRenderer` and representative block-entity renderers;
- spawn placement, natural spawning, precipitation/heightmap paths;
- portal shape / nether portal;
- worldgen heightmap/gravity processors.

Search-result limits mean raw text-match counts are NOT used as completeness
claims. Completeness comes from mechanism/family coverage plus registry/base
class sweeps.

---

## 2. The six semantic spaces

Before deciding that a world-axis occurrence must rotate, classify which space
owns it.

### 2.1 Physical world space

Must remain physical XYZ:

- BlockPos storage;
- chunk addressing;
- network positions;
- raw world Vec3;
- physical AABB coordinates;
- world collision shapes after canonical->physical conversion;
- world ray segments after body/view conversion;
- world forces that are not gravity-relative.

### 2.2 Canonical BlockState-local space

Position-only deterministic local frame.

Owns semantic state values such as:

- local FACING;
- local AXIS;
- local UP/DOWN support;
- local HALF / AttachFace;
- local N/E/S/W connection properties;
- RailShape meaning;
- canonical model and shape orientation.

### 2.3 Traversal-chart space

Path-dependent local topology.

Owns ordered neighbor movement where seam entry matters:

- walks along a face;
- graph propagation;
- support traversal across an exact edge;
- rail/connectivity/growth networks;
- recursive slope/search algorithms.

### 2.4 Entity/body-local space

Owns:

- local UP/DOWN for an entity;
- movement input;
- jump/fall/ground;
- body horizontal plane;
- eye/view orientation;
- navigation and body-local animation.

### 2.5 Planet-generation space

Owns generated terrain semantics:

- planet elevation;
- surface;
- hydrology;
- caves/features;
- structure placement policy.

Do not runtime-patch thousands of worldgen `Y` accesses individually.

### 2.6 Global environment/physical-world space

Some world semantics may intentionally remain global rather than local gravity:

- skylight;
- sky visibility;
- clouds;
- build-height bounds;
- some weather policy;
- dimension-level time/environment rules.

These require an explicit product decision, not automatic rotation.

---

## 2A. Portability rule for every mechanism

Cross-version strategy:
`docs/research/PORTABILITY_STRATEGY.md`.

This mechanism map is intentionally based on semantic ownership rather than
current 1.21.1 method names because that is also the portability boundary.

For every mechanism listed below:
- Planet-owned frame/topology/graph/semantic logic should stay stable;
- the exact Minecraft/NeoForge hook feeding that logic is version-sensitive;
- a future port re-researches the vanilla owner and rewires the adapter first;
- only a true semantic change should force a core algorithm rewrite.

A mechanism implemented mostly inside mixins is considered insufficiently
portable until its reusable logic is extracted behind a Planet-owned helper/API.

## 3. Critical roadmap rule: phases own BEHAVIOR, not objects

A Minecraft class/block/entity can participate in multiple phases.

Never assign an entire object to one phase merely because its first visible bug
appeared there.

Examples:

### Rail

A rail is four different mechanisms:

1. block placement/support;
2. rail connection/slope graph;
3. powered/detector/activator signal behavior;
4. minecart movement on RailShape geometry.

Therefore:
- placement/support + RailState graph -> Phase 2;
- rail redstone -> Phase 7A;
- minecart rail physics -> Phase 7;
- rail model/shape orientation -> Phase 3.

### Door

- placement/upper-lower pairing/support -> Phase 2;
- static model/shape -> Phase 3;
- redstone open/power behavior -> Phase 7A;
- mob door navigation/interaction -> Phase 6;
- sleep/respawn does not belong to DoorBlock but analogous bed behavior reaches
  entity/network/environment phases.

### Piston

- canonical FACING placement -> Phase 2;
- signal/quasi-connectivity -> Phase 7A;
- push graph -> Phase 7A;
- moving block collision and entity push force -> Phase 7A + entity boundary;
- moving piston rendering -> Phase 3/7A integration.

### Chest

- local FACING and pair direction -> Phase 2;
- pair-combiner topology -> Phase 2;
- block entity renderer -> Phase 3;
- automation/capability sides -> Phase 7A.

This split is intentional.

---

## 4. Mechanism ownership matrix

| Mechanism | Vanilla ownership / representative source | Main gravity assumptions | Planet owner |
|---|---|---|---|
| Placement input frame | UseOnContext, BlockPlaceContext, BlockItem | clicked face, look directions, click Y, horizontal direction | Phase 2A |
| Canonical state orientation | BlockStateProperties, Directional/HorizontalDirectional families | FACING, AXIS, HALF, AttachFace, connection properties | Phase 1 + Phase 2 |
| Support / survival | canSurvive, sturdy faces, Block.canSupport* | below/above, UP/DOWN support side | Phase 2B |
| Neighbor shape/update semantics | updateShape, neighborChanged, NeighborUpdater | physical source neighbor vs state-local Direction | Phase 2B / owning subsystem |
| Multiblock part topology | Door, Bed, DoublePlant, chest pairing | above/below, head/foot, upper/lower, pair direction | Phase 2C |
| Tangent connection graphs | CrossCollision, Fence, Wall, Multiface, Vine | N/E/S/W, Plane.HORIZONTAL | Phase 2D |
| Runtime growth/ecology | Bush, crops, GrowingPlant, vines, cactus, sugar cane, grass/snow | below/above, growth direction, tangent spread | Phase 2E |
| Rail block graph | BaseRailBlock, RailState | below support, 4 tangent neighbors, ascending + local UP | Phase 2F |
| Falling/support-trigger block logic | FallingBlock, dripstone, scaffolding, brushable blocks | below/free/support chain | Phase 2G + entity Phase 7 as needed |
| Shapes/collision/support geometry | BlockStateBase shape APIs | canonical shape vs physical shape | Phase 3A |
| Static baked rendering | ModelBlockRenderer, BakedModel/BakedQuad | quad Direction, neighbor AO/light/shade | Phase 3B |
| Block entity/custom render | Bed/Chest/Sign/etc renderers | Y-axis pose rotations, local FACING | Phase 3C |
| Particles | Particle + emitters + custom tick classes | Y gravity/rise, XZ tangent, onGround, above/below emitters | Phase 4 |
| Fluid simulation | FlowingFluid, LiquidBlock | DOWN first, 4 horizontal spread, slope recursion | Phase 5A |
| Fluid shape/flow/entity interaction | FluidState, Entity fluid update | height along Y, XZ flow | Phase 5B |
| Fluid render | LiquidBlockRenderer | UP top face, XZ corners, above/below sampling | Phase 5C |
| Generic entity collision | Entity.move/collide | Y vertical, XZ horizontal, step/onGround/fall | Phase 7A-entity core (roadmap Phase 7.1) |
| Living locomotion | LivingEntity.travel/jump | jump Y, swim Y, XZ tangent, climb/elytra | Phase 7.2 |
| Non-living entity motion | Item/XP/TNT/FallingBlock/projectiles | Y gravity/bounce, XZ friction | Phase 7.3 |
| Vehicles | AbstractMinecart, Boat | rail XZ/Y slopes, water maxY/buoyancy | Phase 7.4 |
| Navigation/AI | WalkNodeEvaluator, GroundPathNavigation, MoveControl, RandomPos | floor Y, horizontal graph, above/below targets | Phase 6 |
| Signal graph | SignalGetter, wire, diode, observer | six-neighbor Direction conventions, UP/DOWN support, XZ wire | Phase 7A |
| Piston/moving automation | PistonBase, resolver, moving BE | FACING push graph, quasi-power, Axis.Y entity logic | Phase 7A |
| Sided capabilities/logistics | NeoForge BlockCapability/EntityCapability | side Direction context | Phase 7A + Phase 10 |
| Interaction/picking/view | Entity.pick, Level.clip, BlockHitResult, view vectors | eye UP, yaw/pitch, hit-side physical vs local | Phase 7B |
| Client/server movement consistency | LocalPlayer, ServerGamePacketListener | packet Y, onGround/floating/fall | Phase 7B |
| Spawn placement | SpawnPlacementTypes, NaturalSpawner | above/below, heightmap, global Y scan | Phase 7B policy |
| Weather/environment | LevelRenderer precipitation, Biome, heightmaps | world-Y rain columns, sky/height | Phase 7B policy |
| Worldgen | ChunkGenerator/features/placement/surface | enormous global-Y assumption surface | Phase 8 |
| Generated structures | structure templates/processors | rigid orientation, heightmaps, GravityProcessor | Phase 9 |
| Runtime portals | PortalShape/NetherPortalBlock | UP height, horizontal axis, rigid rectangle | Phase 9 runtime topology + Phase 7B entity transition |
| Mod compatibility | Minecraft/NeoForge API boundaries | direct raw semantic XYZ in third-party code | Phase 10 continuous |

The roadmap keeps historical phase numbers, but subphases must use this
mechanism ownership rather than named-block ownership.

---

## R1 block/world topology research status

R1 COMPLETE.

Concrete Phase-2 family matrix:
`docs/research/BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md`

R1 confirmed:
- physical NeighborUpdater fan-out remains physical;
- HorizontalDirectionalBlock is property vocabulary, not one behavior family;
- FaceAttachedHorizontalDirectionalBlock is a strong base-family boundary;
- DoublePlantBlock is a strong local-vertical pair boundary;
- MultifaceBlock is a six-face attachment family, distinct from four-tangent
  CrossCollision graphs;
- WallBlock is its own tangent + local-UP-post algorithm;
- GrowingPlantBlock is a strong direction-owned growth family;
- Scaffolding is a local-DOWN + tangent distance graph;
- BaseRailBlock + RailState form a dedicated block-topology graph;
- RailState's physical X/Z identity assumptions require a Planet-owned
  local/traversal graph, not simple above/below replacement;
- SimpleWaterloggedBlock default hooks are mostly frame-independent scheduling;
- build-height remains a physical-world constraint applied after a semantic
  target position is resolved.

## 5. Block semantic mechanisms

## 5.1 Placement input is one shared boundary

Vanilla `BlockPlaceContext` exposes a mixture of:

Physical facts:
- hit location;
- physical clicked side;
- actual target BlockPos.

Semantic orientation helpers:
- nearest-looking direction;
- nearest-looking vertical direction;
- ordered nearest-looking directions;
- horizontal direction.

Planet rule:

- preserve physical hit/result geometry;
- derive a canonical-local VIEW of orientation inputs for state selection;
- never globally mutate BlockHitResult or BlockPos.

Existing `PlanetBlockPlacementFrame` / `PlanetPlacementRuntime` are the right
foundation.

Family coverage must include:

- DirectionalBlock;
- HorizontalDirectionalBlock;
- RotatedPillarBlock;
- FaceAttachedHorizontalDirectionalBlock;
- StandingAndWallBlockItem;
- slab/stair/trapdoor half logic using click offset;
- specialized directional placement (hopper, dispenser, observer, piston,
  lightning rod, etc.).

Before adding a new placement mixin, inspect the base class and sibling family.

---

## 5.2 BlockState properties are canonical LOCAL vocabulary

Gravity-sensitive vanilla properties include:

- FACING;
- HORIZONTAL_FACING;
- FACING_HOPPER;
- AXIS / HORIZONTAL_AXIS;
- UP/DOWN/NORTH/EAST/SOUTH/WEST booleans;
- ORIENTATION / FrontAndTop;
- ATTACH_FACE;
- WALL side properties;
- REDSTONE side properties;
- DOUBLE_BLOCK_HALF;
- HALF;
- RAIL_SHAPE;
- BED_PART / CHEST_TYPE / DOOR_HINGE / STAIRS_SHAPE;
- VERTICAL_DIRECTION.

Property values stay canonical local.

Do NOT attempt to store physical UP/DOWN in a HORIZONTAL_FACING property: the
property domain itself proves why physical direction cannot be the canonical
state representation.

---

## 5.3 Support/survival/update is a family, not a torch problem

Common vanilla pattern:

    supportPos = pos.below()
    supportState.isFaceSturdy(... Direction.UP)

or:

    supportPos = pos.relative(FACING.opposite())

This occurs across:

- BushBlock descendants;
- rails;
- torches;
- ladders;
- face-attached blocks;
- lanterns/hanging blocks;
- doors/double plants;
- redstone components;
- scaffolding;
- coral/fans and other attachments.

Shared Planet concepts:

- canonical local support direction;
- seam-aware physical target;
- target canonical side toward source;
- local support invalidation/update.

Already existing support infrastructure should be expanded by family rather than
adding one helper per block.

Important limitation:

A generic `updateShape(Direction,...)` Direction remap is NOT sufficient.
Many block methods then call `pos.relative(direction)` internally. That
Direction is local semantic data but `BlockPos.relative` is physical.

Therefore adapt at a boundary where both direction AND neighbor position are
known, or implement a base-family adapter.

---

## 5.4 Physical NeighborUpdater remains physical

`NeighborUpdater.UPDATE_ORDER` iterates all six physical neighbors.

That is not automatically wrong.

Its job is physical fan-out: notify actual adjacent world cells.

The gravity-sensitive part begins when a target block interprets:

- which local side changed;
- whether that side is support;
- which graph edge that direction represents.

Therefore:

- do not globally rotate NeighborUpdater's six physical positions;
- reframe the physical source-target relation at the receiving semantic
  boundary;
- family algorithms that perform additional local traversal must use
  PlanetBlockStep/Walk rather than raw relative(localDirection).

---

## 6. Block families that should be batch-implemented

### 6.1 Face-attached family

Base:
`FaceAttachedHorizontalDirectionalBlock`.

Covers at least:
- lever;
- button;
- grindstone;
- bell.

Shared assumptions:
- ceiling/floor mapping from UP/DOWN;
- wall FACING;
- support neighbor;
- sturdy support side;
- updateShape invalidation.

This is a good base-class adapter family.

### 6.2 Standing/wall attachment families

Relevant roots:
- StandingAndWallBlockItem;
- BaseTorchBlock;
- wall torch;
- standing/wall signs;
- banners;
- skulls;
- wall coral fans.

Shared pattern:
choose standing vs wall or wall side based on ordered directions, then survive
against that support.

Not every family can use one concrete mixin, but all share the same placement
frame + support API.

### 6.3 Horizontal directional family

`HorizontalDirectionalBlock` has many descendants:
- ladder;
- trapdoor;
- bed;
- chest;
- stair;
- diode/repeater/comparator;
- furnace-like blocks;
- wall signs/skulls/banners/torches;
- fence gate;
- tripwire hook;
- lectern;
- beehive;
- stonecutter;
- etc.

The base property is shared, but behavior is not.

Do NOT make one giant HorizontalDirectionalBlock runtime override.
Use:
- shared canonical FACING transform helpers;
- algorithm-family adapters for support, pairing, signal, render, etc.

### 6.4 Tangent connection family

Core examples:
- CrossCollisionBlock;
- FenceBlock;
- IronBarsBlock;
- TripWireBlock;
- WallBlock;
- PipeBlock;
- MultifaceBlock;
- GlowLichenBlock;
- SculkVeinBlock;
- VineBlock.

Shared concept:
four local tangent directions, not world X/Z.

Required common abstraction:
ordered seam-aware local tangent neighbor query.

But properties differ:
- fence/iron bar booleans;
- wall WallSide + UP post;
- redstone RedstoneSide;
- multiface has all six face booleans;
- vine has growth semantics.

So share traversal, not state mutation code.

### 6.5 Local vertical / growth family

Examples:
- BushBlock descendants;
- crops;
- saplings/mushrooms/flowers;
- sugar cane / cactus / bamboo;
- snow/grass/mycelium spreading;
- GrowingPlantBlock head/body systems;
- weeping/twisting/cave vines;
- kelp;
- double plants;
- scaffolding.

Subfamilies differ:

Support-only:
    local DOWN support.

Directed growth:
    growthDirection local semantic Direction.

Tangent spread:
    four local tangent directions.

Two-part growth:
    local UP paired block.

Scaffolding:
    local DOWN + tangent distance graph.

One generic `below()` patch is not enough.

---

## 7. Rails: explicit mechanism decomposition

Rails are the strongest example of why named-block patching must stop.

### 7.1 BaseRailBlock support and placement — Phase 2F

Vanilla:

- survives on `pos.below()`;
- ascending rail also requires side/up support;
- initial shape derives from world horizontal player direction;
- waterlogging is separate fluid integration.

Planet:
- support is local DOWN;
- initial RailShape directions are local tangent directions;
- shape remains canonical local state.

### 7.2 RailState graph — Phase 2F

`RailState` is a dedicated graph engine.

Vanilla connections are constructed from:
- north/south/east/west;
- above those tangent neighbors for ascending shapes;
- same/above/below when locating adjacent rails;
- `Direction.Plane.HORIZONTAL`;
- recursive reconnect propagation.

Planet must implement the graph in traversal-chart terms:

    local tangent neighbor
    optional local UP slope step
    target canonical RailShape semantics

This is NOT equivalent to fixing BaseRailBlock.canSurvive.

Acceptance should cover a whole rail network:
- straight;
- curve;
- ascending;
- reconnect after placement/removal;
- seam crossing.

### 7.3 Rail redstone — Phase 7A

Powered/detector/activator behavior is not Phase 2.

PoweredRailBlock recursively follows RailShape and also performs signal queries.

DetectorRailBlock:
- detects minecarts;
- emits signal;
- has an UP-specific direct-signal rule;
- notifies below.

These require the signal frame after the rail graph exists.

### 7.4 Minecart movement — Phase 7.4

`AbstractMinecart` has its own RailShape exit map.

Ascending exits are encoded with Y offsets.
Movement uses world XZ horizontal distance and explicit Y position changes.

Therefore a rail can place/connect correctly while a minecart still moves
incorrectly.

Minecart acceptance is a separate entity/vehicle gate that depends on Phase 2F.

---

## 8. Multiblock and pair topology

### Vertical/local-UP pairs

Examples:
- door;
- DoublePlantBlock family;
- tall seagrass;
- pitcher crop;
- some generated two-part plants.

Vanilla commonly uses:
- `above()`;
- `below()`;
- `Direction.Axis.Y`;
- `DoubleBlockHalf`.

Planet must map upper/lower to local UP/DOWN.

### Directed horizontal/local-tangent pairs

Examples:
- bed FOOT/HEAD;
- double chest LEFT/RIGHT;
- potentially modded machine pairs.

Chest shows another useful reusable pattern:
`DoubleBlockCombiner` accepts a function returning the connection Direction,
then calls `pos.relative(direction)`.

That direction must be physically stepped from canonical local state before
physical lookup.

Pair state, traversal direction and renderer orientation are separate concerns.

---

## 9. Redstone and automation is a graph subsystem

`SignalGetter.hasNeighborSignal` hard-codes all six physical neighbors and
passes Direction values into block signal APIs.

Physical six-neighbor scanning itself is valid, but signal Direction semantics
must be traced carefully because vanilla APIs often describe the direction from
the queried block toward the receiver, not simply the emitter FACING.

### 9.1 Wire

RedStoneWireBlock combines:

- local DOWN support;
- four tangent connection graph;
- tangent +/- local UP climbing;
- signal side semantics;
- neighbor fan-out;
- particle emission.

These aspects belong to multiple mechanisms:
- support/connectivity foundation from Phase 2;
- signal propagation in Phase 7A;
- particles in Phase 4.

### 9.2 Diodes/repeater/comparator

Combine:
- local DOWN support;
- local tangent FACING;
- side inputs via clockwise/counter-clockwise tangent directions;
- output signal direction.

Support can be Phase 2 while signal behavior remains Phase 7A.

### 9.3 Observer

FACING is full 3D Direction.

It observes one canonical-local side and notifies the opposite side.
Requires proper physical source-target reframing.

### 9.4 Piston

Piston requires one coherent automation subphase:

- FACING placement;
- quasi-connectivity signal search;
- PistonStructureResolver push graph;
- slime/honey branching;
- MovingPistonBlock/PistonMovingBlockEntity geometry;
- entity pushes;
- moving rendering.

Do not fix piston placement and call pistons complete.

---

## R2 client geometry/render/particle research status

R2 COMPLETE.

Concrete Phase-3/Phase-4 matrix:
`docs/research/CLIENT_RENDER_PARTICLE_MATRIX_1_21_1.md`

R2 confirmed:
- physical outer shape queries and canonical support/occlusion state are
  different boundaries;
- transformed physical BakedQuad.direction is sufficient to drive vanilla
  AO/light adjacency; AO must NOT be rotated a second time;
- breaking overlay uses the same ModelBlockRenderer pipeline;
- directional world shade is an environment-policy question, not a geometry
  transform;
- standard OffsetType XZ/XYZ should be authored/seeded in canonical local
  coordinates, while arbitrary custom OffsetFunction remains a compatibility
  boundary;
- BlockEntityRenderers split into rigid block-local, runtime-direction and
  world/camera-space families; a global dispatcher transform is unsafe;
- moving block render and entity render semantics have different owners;
- Particle base engine and emitter/source semantics are independent;
- ParticleUtils is caller-semantic and must not be globally reframed;
- entity/fluid/weather/portal emitter source geometry is an explicit
  integration gate of the owning phase;
- custom/accelerated render engines that bypass ModelBlockRenderer require
  explicit Phase-10 integration rather than raw-vertex interception.

## 10. Shapes and rendering

### 10.1 Position-aware block shapes

Canonical cached BlockState shape remains local.
Position-aware query returns physical rotated shape.

This is already the correct Planet architecture.

### 10.2 Static baked models

ModelBlockRenderer relies on Direction for:
- `getQuads` side;
- culling;
- AO neighbor sampling;
- light neighbor lookup;
- shade.

Therefore rotating only vertex positions is insufficient.

Existing BakedModel wrapper + physical quad direction work is the correct family
boundary.

### 10.3 Block entity renderers

BERs are custom algorithms and cannot be covered by static baked model rotation.

Examples:
- BedRenderer;
- ChestRenderer rotates around world Y from horizontal FACING;
- SignRenderer rotates around world Y;
- hanging signs/skulls/banners have their own pose assumptions.

Need a BER mechanism audit:
- renderer consumes canonical BlockState;
- apply local->world frame at renderer root where possible;
- then let renderer use canonical local geometry;
- avoid per-renderer coordinate hacks when a root-frame wrapper is possible.

Accelerated/custom renderers (Flywheel/Create) remain a compatibility boundary.

---

## 11. Particles

Phase 4 owns the complete particle subsystem.

Research already proved why batching matters:

- emitter origin;
- constructor launch bias;
- gravity/rise;
- tangent drift;
- Particle.move collision ordering;
- onGround/stoppedByCollision;
- lifecycle tests;
- custom tick subclasses;
- nested emitters;
- fluid-coupled particles.

The accepted TerrainParticle fix rotates the complete move semantic unit rather
than post-processing individual flags.

Particle work follows the Phase-4 batch plan; no class-by-class user acceptance.

---

## R3 fluid research status

R3 COMPLETE.

Concrete Phase-5 matrix:
`docs/research/FLUID_MECHANISM_MATRIX_1_21_1.md`

R3 confirmed:
- FlowingFluid is one local-topology graph, not isolated DOWN/HORIZONTAL patches;
- PlanetBlockStep already supplies the required seam-aware edge foundation;
- semantic local direction and physical edge direction must be carried separately;
- FlowingFluid slope cache's physical X/Z short key is invalid on rotated faces;
- FlowingFluid wall-occlusion cache is not frame-complete;
- FluidState scalar amount stays canonical while height lookup uses local UP and
  physical shape is position/frame aware;
- bucket clicked-face placement is physical and should not be rotated;
- SimpleWaterloggedBlock/LiquidBlockContainer are mostly same-position hooks;
- NeoForge CreateFluidSourceEvent / FluidType / BaseFlowingFluid must remain in
  the shared graph path;
- NeoForge FluidInteractionRegistry also contains world-axis assumptions and is
  part of Phase 5;
- bubble columns are a local-UP fluid topology family with entity impulse deferred
  to Phase 7;
- entity immersion/current measurement is a Phase-5 geometry + Phase-7 body integration;
- LiquidBlockRenderer requires a dedicated local fluid-mesh algorithm and cannot
  be solved by BakedModel/PoseStack rotation;
- NeoForge fluid sprite/tint/overlay extension points must be preserved.

## 12. Fluids

Fluids are one coherent algorithm and must remain one phase.

### Simulation

FlowingFluid hard-codes:
- DOWN first;
- four HORIZONTAL side spread;
- below-based slope/water-hole recursion;
- horizontal source-neighbor count;
- above checks.

This must run on seam-aware local topology.

### Fluid state geometry

Fluid height/shape is authored from world bottom Y to height.
Planet needs local-UP physical geometry.

### Entity flow

FluidState.getFlow ultimately feeds entity movement.
The vector must be physical world output of local flow topology.

### Rendering

LiquidBlockRenderer independently hard-codes:
- DOWN/UP/N/S/E/W samples;
- top face as UP;
- four world-XZ height corners;
- above/below light;
- XZ flow texture direction.

Simulation correctness does not automatically fix rendering.

### Interaction

Buckets and waterlogging use physical hit positions plus fluid semantic state.
Keep hit geometry physical, but integrate local fluid topology.

---

## R4 entity/body/interaction/network research status

R4 COMPLETE.

Concrete Phase-7 / Phase-7B.1-2 matrix:
`docs/research/ENTITY_BODY_NETWORK_MATRIX_1_21_1.md`

R4 confirmed:
- Entity position, physical AABB, deltaMovement, Entity.move displacement,
  packet XYZ, rays/HitResults and externally authored world forces must remain
  PHYSICAL world-space contracts;
- local UP/DOWN/tangent, step, onGround, fall distance, jump, climb, buoyancy,
  body offsets and yaw/pitch semantics belong to a body-local frame;
- the current partial runtime's LOCAL deltaMovement storage is rejected as the
  long-term architecture because vanilla network, explosion, leash,
  ProjectileUtil, Entity.push and mod APIs consume deltaMovement as a physical
  world vector;
- accepted player behavior remains desired evidence; the implementation
  boundary must later migrate to physical velocity plus temporary local
  projection inside semantic algorithms;
- existing PlanetEntityCollision is the right generic collision foundation:
  physical geometry, local-axis solve, physical displacement result;
- LivingEntity.travel is mostly one local-Y/tangent locomotion family, with
  explicit physical boundaries for world geometry and external vectors;
- projectiles split launch/body semantics, physical collision/raycast,
  class-specific flight and local orientation-from-motion;
- minecart and boat are dedicated rail/fluid engines, not ordinary Entity
  subclasses;
- passenger/seat/leash/sleep attachments are body-local authored offsets while
  their final positions are physical;
- dismount is a dedicated local-floor/clearance search;
- BlockHitResult remains physical across client/server; Phase 2 converts its
  side only at semantic block-use/placement boundaries;
- movement/vehicle packet XYZ and teleport relative XYZ are physical, while
  floating/jump/fall/onGround validation must project displacement into the
  entity body frame;
- gravity-face selection includes hysteresis/prediction state, so client/server
  body-frame agreement needs an explicit synchronization design unless exact
  deterministic equivalence is proven;
- NeoForge Entity/Living/Projectile/Vehicle/interaction hooks must stay on the
  normal runtime paths.

## 13. Entity mechanics

## 13.1 Generic Entity.move is the core

Exact vanilla Entity.move contains all of:

- world-Y vertical collision;
- world-XZ horizontal collision;
- local-ground classification from negative Y clipping;
- step-up Axis.Y geometry;
- supporting block/floor semantics;
- fall-damage Y delta;
- tangent velocity clipping;
- horizontal movement distance.

Entity gravity acceleration alone cannot solve entity physics.

Existing Planet entity collision/frame work belongs here.

## 13.2 Living locomotion is separate from collision

LivingEntity adds:

- jump velocity along Y;
- water/lava jump Y;
- swimming;
- climb checks;
- elytra/fall flight;
- ground friction;
- powder snow;
- honey/slime interactions;
- horizontal-distance movement math.

Batch these by locomotion mechanism.

## 13.3 Non-living entities

Examples:
- ItemEntity;
- ExperienceOrb;
- TNT;
- FallingBlockEntity;
- projectiles;
- fishing hook;
- armor stands.

Common pattern:
base gravity plus class-specific Y bounce/friction/float behavior.

Do not infer coverage merely because Entity.move is fixed.

## 13.4 Projectiles

Projectile launch is its own boundary.

`shootFromRotation` mixes:
- view direction;
- shooter velocity;
- special onGround treatment of shooter Y velocity.

Aim conversion and projectile gravity are distinct.

## 13.5 Vehicles

Minecart:
rail graph + RailShape geometry + local tangent speed.

Boat:
fluid surface height + buoyancy + local vertical/tangent motion.

These are dependency-heavy subphases, not ordinary Entity subclasses.

---

## 14. Navigation and AI

Ground navigation combines:

- node graph;
- floor/support search;
- entity local width/height;
- step/drop;
- doors/fences/rails/water hazard classification;
- MoveControl;
- target generation (RandomPos family);
- goals with above/below assumptions.

The correct state near a seam may require more than BlockPos: traversal face
matters.

Do not patch each goal independently until the shared navigation/body-frame
foundation is stable.

AI-specific world semantics still need classification:
- flying AI may intentionally use full 3D world vectors;
- ground AI needs local floor;
- aquatic AI depends on Phase 5 fluid topology.

---

## 15. Interaction, camera, raycasts and networking

These are coupled by the entity/body frame.

### View/picking

Eye position and view vector are body-local concepts converted to one physical
world ray.

BlockHitResult remains physical.
State placement consumes a canonical-local interpretation of the physical hit
side.

### Networking

Server validation still sees physical packet positions.

Audit:
- client prediction;
- ServerPlayer fall/ground state;
- floating checks;
- teleport corrections;
- vehicle movement;
- yaw/pitch sync.

The already fixed player packet fall-damage path is evidence that client-visible
correctness is not sufficient.

---

## 16. Spawning, environment and heightmaps

This group must NOT be auto-rotated without a product decision.

### Spawn placement

SpawnPlacementTypes uses above/below.
NaturalSpawner also uses:
- global Y;
- heightmaps;
- downward vertical scans.

Question to decide:
Does a mob spawn on any local planet face as local ground, or is some spawn
selection tied to the global environment/sky?

Likely answer for surface mobs: local ground semantics.
But heightmap candidate acquisition must be redesigned for Planet surface, not
merely rotate `below()`.

### Weather

LevelRenderer precipitation uses:
- world XZ columns;
- MOTION_BLOCKING heightmap;
- global Y limits;
- below impact block.

A cube planet may want precipitation relative to each face, but skylight/clouds
may remain global dimension concepts.

Do not derive weather policy from entity gravity automatically.

### Heightmaps

Vanilla heightmaps are fundamentally XZ -> Y summaries.

They cannot represent six independent planet faces as a universal local-surface
query.

Planet needs explicit surface/elevation APIs rather than pretending vanilla
heightmap is local gravity-aware everywhere.

---

## 17. Worldgen and structures

Runtime local gravity and world generation must stay separate.

Worldgen contains thousands of intentional Y operations.

Use PlanetGenerationSpace.

### Features

Features that mean "surface/up/down" need Planet generation-space adaptation.

Do not runtime-mixin every `above()` in every feature.

### Structures

Rigid vanilla structures should generally remain rigid and avoid seams.

Structure processors such as GravityProcessor use heightmaps and global Y; they
need Planet generation-space or policy integration.

### Runtime portals

PortalShape is a runtime rigid-rectangle algorithm with explicit:
- horizontal axis;
- repeated UP;
- below scan;
- entity height Y.

It is not just a directional block.

Treat runtime portal topology as its own mechanism:
- decide whether portal plane is local vertical;
- keep rectangle physically rigid;
- define seam policy;
- separately adapt entity transition/orientation.

---

## 18. Stable generic boundaries vs family adapters

### Good generic boundaries

Already or potentially reusable:

- canonical BlockState frame;
- traversal chart;
- local direction/vector conversion;
- position-aware shape rotation;
- BakedModel wrapper;
- sided capability side conversion;
- Entity/body frame;
- local collision solver;
- FlowingFluid local topology layer;
- particle emitter/move helpers;
- PlanetGenerationSpace.

### Good base-family adapters

Examples:

- FaceAttachedHorizontalDirectionalBlock;
- StandingAndWallBlockItem;
- BaseTorchBlock;
- RotatedPillarBlock;
- BushBlock where behavior is truly shared;
- GrowingPlantBlock;
- DoublePlantBlock;
- BaseRailBlock + RailState;
- CrossCollision-related tangent traversal;
- MultifaceBlock;
- Sign/banner/skull standing/wall families.

### Bad global patches

Do NOT globally redefine:

- BlockPos.relative/above/below;
- Direction step vectors;
- Direction.Axis;
- all BlockPlaceContext clicked faces;
- all updateShape Direction values without positional context;
- all world forces;
- all heightmaps.

Those primitives are also used for intentionally physical geometry.

---

## 19. Phase dependency graph

Use the existing phase numbers, but apply these dependencies.

### Phase 0
Stable Planet runtime / activation / diagnostics.

### Phase 1
Frame and topology kernel.

Everything semantic depends on this.

### Phase 2
Block semantic behavior:
placement, state orientation, support, updates, pairing, tangent networks,
growth, rail BLOCK graph, support-triggered block behavior.

Depends on Phase 1.

### Phase 3
Block shapes and rendering:
physical shapes, culling, AO/light/shade, static models, BERs.

Depends on Phase 1 and canonical state semantics from Phase 2.

### Phase 4
Particles.

Depends on Phase 1.
Emitter-specific cases may consume Phase-2 block semantics.
Fluid-coupled final gates depend on Phase 5.

### Phase 5
Fluids.

Depends heavily on Phase 1/2 topology and Phase 3 for renderer output.

### Phase 6
Navigation / AI.

Depends on Phase 1, block support/connectivity from Phase 2, entity-body
collision from Phase 7, and Phase 5 for aquatic paths.

Implementation can proceed incrementally, closure waits on those dependencies.

### Phase 7
Entity physics / locomotion / projectiles / vehicles.

Depends on Phase 1.
Minecart closure depends on Phase-2 rail graph.
Boat/swimming closure depends on Phase 5.

### Phase 7A
Signals, automation, pistons, sided logistics.

Depends on Phase 2.
Powered/detector rail signal depends on Phase-2 rail graph.
Piston entity interaction also integrates with Phase 7 and moving rendering.

### Phase 7B
View/interaction/networking/spawning/environment policy.

Depends on body frame/entity work.
Environment policy informs worldgen and spawn acceptance.

### Phase 8
Planet worldgen/biomes/hydrology.

Uses PlanetGenerationSpace, not runtime block-local patches.

### Phase 9
Generated structures + runtime rigid topology such as portals.

Depends on Phase 8 generation policy and Phase 2 canonical block semantics.

### Phase 10
Compatibility.

Not an "after everything" phase.
Run compatibility gates during the owning subsystem and maintain public frame
APIs here.

### Phase 11
Transition comfort / continuous entity frame / polish.

Depends on correctness of block/entity semantics first.

---

## 20. Revised acceptance strategy

Do not test every vanilla block.

For each mechanism family:

1. deterministic tests cover all six frames, seam transforms and pure state math;
2. one or two representative vanilla classes validate the shared adapter;
3. sibling classes get source/static coverage and targeted tests when their
   algorithm differs;
4. one family-level runtime acceptance matrix validates the mechanism;
5. special subclasses with extra behavior get their owning phase's acceptance.

Examples:

Face-attached family:
- lever + grindstone are representative, not lever/button/grindstone/bell
  separately unless source differences require it.

Rail graph:
- ordinary + powered + detector rail network is one Phase-2/7A integration
  matrix; minecart is Phase 7.

Growth:
- one BushBlock crop, one GrowingPlant, vine/multiface, scaffolding, and one
  tangent-spread block represent distinct subfamilies.

This prevents "test Minecraft one block at a time until the second coming".

---

## 21. Mandatory rule for future bug reports

When the user reports:

> block/entity X behaves incorrectly under local gravity

Do NOT immediately patch X.

First classify the failing behavior:

- placement input?
- state property?
- support?
- neighbor traversal?
- graph connectivity?
- multiblock relation?
- random/scheduled growth?
- shape/collision?
- static render?
- BER/custom render?
- signal graph?
- entity physics?
- vehicle-specific?
- fluid?
- particle?
- environment/worldgen?

Then inspect:
1. the class;
2. its superclass/base family;
3. siblings using the same mechanism;
4. the engine boundary feeding that family.

Update this mechanism map if a new family is discovered.

Only after ownership is clear should runtime code change.
