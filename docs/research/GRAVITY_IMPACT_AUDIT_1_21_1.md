# Research: complete gravity / local-frame impact audit for Minecraft 1.21.1

Status: cross-cutting architecture baseline for Planetary 2.0.
Target runtime: Minecraft 1.21.1 / NeoForge 21.1.x.

This is the master checklist for every subsystem whose behavior can change when
local UP/DOWN, local horizontal axes, or an entity body frame no longer match
physical world XYZ.

The goal is NOT to special-case every vanilla block/entity. The goal is to
adapt the smallest stable Minecraft/NeoForge boundaries so vanilla and mods
that use those boundaries inherit Planet semantics automatically.

Universal compatibility with arbitrary mods cannot be guaranteed. A mod that
performs raw world-axis math such as pos.below(), pos.relative(local FACING),
Direction.Plane.HORIZONTAL, or y -= gravity can bypass vanilla/NeoForge
boundaries and may require an integration adapter. The architecture must make
those adapters small and composable instead of creating a second gravity
implementation.

Sources inspected so far:
- exact Minecraft 1.21.1 source paths for Entity collision/movement,
  BlockPlaceContext, SignalGetter, SpawnPlacementTypes, Projectile, Boat,
  Particle, BlockState support/cache, FlowingFluid, block rendering and
  representative directional blocks;
- NeoForge 1.21.1 sided BlockCapability/EntityCapability documentation and
  standard item/fluid/energy capabilities;
- NeoForge baked-model extension contracts (ModelData, RenderType-aware quads);
- GravityChanger / Gravity API family as coverage references for entity gravity;
- Create 1.21.1 as a stress test for direct Direction/BlockPos math plus
  NeoForge capability use;
- Valkyrien Skies 2 as a reference for composing position, direction, raycast,
  AABB/collision, camera, particle and external moving-coordinate transforms.

## 1. Fundamental coordinate model

Planet keeps one ordinary physical Minecraft world grid:
- BlockPos remains physical XYZ;
- chunks remain physical X/Z storage;
- world positions and networked positions remain physical;
- raw Level storage and persistence remain physical;
- world Vec3 motion is physical unless a method explicitly documents local/body
  semantics.

Planet adds explicit frames on top.

### 1.1 Canonical BlockState frame

Position-only, deterministic, path-independent.

Used for:
- Direction/Axis BlockState semantics;
- shapes;
- support/sturdy faces;
- static model orientation;
- block-entity local face semantics;
- sided capability provider context.

Implementation: PlanetBlockStateFrame.

### 1.2 Traversal chart

Path-dependent on exact edges/corners.

Used for:
- seam-aware local neighbor stepping;
- ordered surface walks;
- recursive searches that must preserve how they entered a corner;
- transported directions.

Implementation: PlanetBlockFrameContext / PlanetBlockStep / PlanetBlockWalk.

### 1.3 Entity/body frame

Entity-position dependent and potentially hysteretic.

Used for:
- body local UP/DOWN;
- movement input and jump;
- ground/fall semantics;
- eye/body orientation;
- camera/input;
- navigation/body-relative AI.

### 1.4 External moving frame

For ships, contraptions or other physics-space mods.

Transforms must compose:

external-local -> physical world -> Planet-local

and the inverse.

Position, direction/vector, normal, AABB/shape and rotation are different
transform kinds. Do not treat them as one generic transform.

Valkyrien Skies is a useful design reference because it explicitly separates
position transforms, direction transforms, AABB/collision transforms, raycasts,
camera transforms and particles instead of globally redefining Minecraft XYZ.

## 2. Primitives that MUST stay physical

Do not globally change semantics of:
- BlockPos.relative/above/below/north/south/east/west;
- Direction.getStepX/Y/Z;
- Direction.Axis;
- raw Vec3 x/y/z;
- AABB min/max axes;
- generic matrix math.

Those are physical primitives used by chunks, storage, networking, rendering,
physics engines and third-party code. Context-sensitive global changes would
create invisible call-order/thread-local bugs.

Adapt at explicit engine/API boundaries instead.

## 3. Mandatory source-search patterns

Every subsystem research pass must search exact 1.21.1 sources for:

Grid/direction assumptions:
- .above / .below
- .north / .south / .east / .west
- .relative
- Direction.UP / DOWN
- Direction.Plane.HORIZONTAL
- Direction.Axis.Y
- getAxis().isHorizontal()

Vector assumptions:
- y / yd where semantic verticality matters
- add(0, ..., 0)
- horizontalDistance / horizontalDistanceSqr
- yaw/pitch calculations

Collision/body assumptions:
- AABB.minY/maxY
- Shapes.collide(Direction.Axis.Y, ...)
- onGround
- fallDistance
- maxUpStep
- floor/support position

Interaction:
- getClickedFace
- getNearestLookingDirection(s)
- getViewVector / calculateViewVector
- getEyePosition
- BlockHitResult.getDirection

Signals/logistics:
- getSignal / getDirectSignal
- updateNeighbors*
- getCapability(..., Direction)

Rendering:
- BakedQuad.getDirection
- BakedModel.getQuads(..., Direction, ...)
- ModelData
- PoseStack
- hard-coded normals such as (0,1,0)

A match is not automatically a bug. Each occurrence must be classified as
PHYSICAL geometry or LOCAL semantic geometry.

## 4. Block placement and interaction

BlockPlaceContext already mixes two concepts:
- physical clicked/relative target position;
- orientation helpers such as nearest-looking Direction.

Audit:
- UseOnContext;
- BlockPlaceContext;
- DirectionalPlaceContext;
- BlockItem;
- StandingAndWallBlockItem;
- DoubleHighBlockItem;
- PlaceOnWaterBlockItem;
- hanging entities/items;
- bucket interaction;
- spawn eggs;
- beds/respawn interaction;
- portal-frame interaction.

Rule:
BlockHitResult location and hit side stay physical.
Expose a canonical-local hit-side helper separately for state/orientation
choices.

Never globally replace BlockHitResult.direction with a local Direction.

## 5. BlockState property semantics

Audit standard orientation-bearing properties:
- DirectionProperty / FACING;
- HORIZONTAL_FACING;
- AXIS / HORIZONTAL_AXIS;
- vertical Direction properties such as dripstone TIP_DIRECTION;
- AttachFace;
- slab/stair half;
- stair facing/shape;
- door facing/hinge/half;
- trapdoor;
- bed facing/part;
- rail shape;
- wall/fence/pane/multiface connections;
- piston/observer/dispenser/dropper/hopper;
- rotating pillar/log;
- jigsaw/orientation.

Critical rule:
registered property values are canonical LOCAL semantics.

This is required because a property's legal domain may not contain the physical
direction after rotation. HORIZONTAL_FACING, for example, cannot store physical
UP/DOWN even when a local-horizontal face maps there.

Unknown custom property types cannot be transformed generically unless the mod
opts into the Planet Frame API.

## 6. Neighbor traversal, survival and block updates

Vanilla has widespread world-axis calls:
- pos.below/above for plants, torches, rails and falling blocks;
- Direction.Plane.HORIZONTAL for cactus, vine, fluid, redstone and growth;
- direct pos.relative(direction) in directional blocks.

Shared foundation:
- canonical state frame gives local Direction meaning;
- traversal chart resolves seam-aware physical neighbor;
- PlanetBlockSupportQuery resolves the target block's canonical support side.

Future adapters:
- canSurvive;
- updateShape;
- neighborChanged;
- scheduled/random ticks where directions are semantic;
- connection recomputation;
- support/sturdy checks.

Third-party limitation:
a mod that directly executes pos.relative(localFACING) inside custom code can
bypass vanilla helpers. Do NOT globally patch BlockPos to compensate. Prefer a
safe generic callback boundary when one exists, otherwise an integration module.

## 7. Shapes, collision, support and occlusion

Vanilla BlockState caches are canonical and position-free. Keep them that way.

Adapt position-aware results:
- collision shape;
- outline/selection;
- interaction shape;
- support shape;
- occlusion;
- sturdy-face Direction;
- face culling.

Use PlanetBlockStateFrame to rotate canonical shapes to physical space.

Implementation foundation: PlanetVoxelShapeRotation.

## 8. Entity collision and movement

Changing gravity acceleration alone is insufficient.

Exact Entity source contains world-Y assumptions in:
- step-up candidate collection using Axis.Y;
- step-up vectors;
- collision resolution ordering Y before tangent axes;
- ground/floor classification;
- AABB minY anchoring in multiple paths.

Audit:
- Entity.move;
- collideBoundingBox;
- step height;
- supporting block position;
- onGround / verticalCollision / horizontalCollision;
- fall distance and landing;
- pose/bounding box;
- suffocation/push-out;
- entity pushing.

Design target:
keep collision geometry world-space, but run semantic vertical/ground/step logic
through entity frame. If an algorithm is easiest in canonical local coordinates,
rotate input motion/shapes into local space and the result back to world.

## 9. Living locomotion

Audit all vertical assumptions in:
- LivingEntity.travel;
- jump;
- slow falling / levitation;
- climbing;
- swimming/fluid drag;
- sprint/crouch/crawl;
- elytra/fall flying;
- riptide;
- knockback;
- fall damage;
- powder snow;
- honey/slime;
- bubble columns.

A scalar vanilla getGravity() is not enough. Planet needs an entity gravity
VECTOR in world space.

## 10. Non-living entities

Audit individually:
- item entities;
- XP orbs;
- FallingBlockEntity;
- TNT;
- arrows/tridents/throwables/fireballs/wind charges;
- fishing hook;
- armor stands;
- display/hanging entities where orientation matters;
- minecarts;
- boats/chest boats;
- leash/attachment entities.

GravityChanger-family mods are useful coverage references: current forks
explicitly cover more than players, including living entities, projectiles and
minecarts. Planet must also audit class-specific extra Y logic.

## 11. Projectiles, lasers and ray-like mechanics

Projectile launch source uses world yaw/pitch to build world velocity and often
mixes in shooter velocity with special onGround/Y behavior.

Audit:
- shootFromRotation;
- ProjectileUtil;
- arrows/tridents;
- fireballs/wind charges;
- fishing hook;
- custom beams/lasers.

Rule:
projectile velocity stays a physical world Vec3.
Body-local aim is converted to world once at the launch/view boundary.

A modded laser using standard getEyePosition/getViewVector/Level.clip can inherit
Planet behavior. A laser that manually constructs world-Y vectors can bypass it
and may need integration.

## 12. Eye position, camera, input and picking

Audit:
- eye position/eye height;
- view vector/up vector;
- yaw/pitch;
- first/third-person camera;
- hand/item rendering;
- body-local overlays;
- crosshair picking;
- block/entity reach;
- interaction raycast.

Entity.pick, item POV hit helpers and ProjectileUtil combine eye position, view
vector and Level.clip. They must agree on one PHYSICAL world ray after local
camera/body conversion.

## 13. AABB, raycast and line-of-sight

Audit:
- BlockGetter.clip;
- ClipContext;
- projectile/entity hit tests;
- LOS;
- selection;
- collision broad phase;
- occlusion.

For external moving coordinate spaces, compose external transforms with Planet
frames. Valkyrien Skies separately transforms ray positions and direction
vectors; preserve that conceptual separation.

## 14. AI and navigation

Audit:
- GroundPathNavigation;
- PathNavigation;
- PathFinder;
- NodeEvaluator / WalkNodeEvaluator;
- swim/fly navigation;
- MoveControl / FlyingMoveControl;
- LookControl / BodyRotationControl;
- RandomPos family;
- goals/behaviors using above/below or XZ-only math.

Exact corner path state may require (BlockPos, traversalFace), not just BlockPos.

## 15. Redstone and signal direction

Dedicated subsystem.

Exact SignalGetter source hard-codes all six physical neighbors using
below/above/north/south/west/east. Many blocks compare signal Direction with
local FACING or literal UP/DOWN.

Audit:
- SignalGetter;
- BlockState.getSignal/getDirectSignal;
- RedStoneWireBlock;
- repeater/comparator/diode;
- redstone torches;
- lever/button/pressure plate;
- observer;
- target/sculk;
- tripwire;
- powered/detector rails;
- neighbor notify fan-out.

Vanilla signal APIs contain reversed-direction conventions. Trace each call
before applying opposite().

## 16. NeoForge sided capabilities

NeoForge BlockCapability and EntityCapability support nullable Direction context.
Standard item/fluid/energy capabilities are a major mod-compatibility boundary.

Block rule:
- target BlockPos stays physical;
- null side remains null;
- non-null physical side is converted into target canonical BlockState-local
  side BEFORE provider dispatch;
- capability cache semantics must use the same canonical side convention.

This can automatically fix a large class of pipes/cables/machines.

Existing PlanetSidedQueryFrame/BlockCapabilityMixin must be re-audited against
PlanetBlockStateFrame, exact edges/corners and BlockCapabilityCache.

Entity-sided capabilities need entity-frame semantics separately.

## 17. Third-party logistics/machine stress test: Create

Create 1.21.1 demonstrates both compatibility classes.

Likely automatic through shared boundary:
- NeoForge item/fluid capability providers.

Direct code that still needs generic adapter or explicit integration:
- worldPosition.relative(side);
- Direction iteration;
- context.getAxis().isHorizontal();
- literal Direction.UP;
- BlockPlaceContext nearest-looking helpers;
- signal queries around directional blocks;
- vertical behavior using .below(offset);
- custom kinetic shaft/axis graphs.

Representative Create examples found during audit include directional redstone
links, smart observer placement, rotated-pillar kinetic blocks, item drains and
hose-style vertical machinery.

Acceptance should include at least:
- sided item;
- sided fluid;
- sided energy;
- pipe/cable network;
- directional kinetic block;
- direct-neighbor-scanning custom block.

## 18. Fluids

Detailed implementation research: FLUIDS_1_21_1.md.

Must cover as one subsystem:
- FlowingFluid;
- local DOWN fall;
- tangent spread;
- slope recursion;
- source creation;
- exact-corner physical dedupe vs chart-aware traversal;
- waterlogging;
- buckets;
- entity flow vector;
- fluid shape;
- LiquidBlockRenderer;
- drip particles;
- water/lava interactions;
- representative modded FlowingFluid.

## 19. Particles

Generic Particle is Y-up in more than acceleration:
- AABB anchoring;
- negative-Y onGround classification;
- subclasses directly changing yd;
- spawn helpers using above/below.

Audit:
- generic Particle;
- falling dust;
- drip/water;
- smoke/fire/explosion;
- block hit/break;
- entity-attached particles;
- weather particles.

Position and velocity transforms are separate for external moving frames.

## 20. Static block rendering

Audit:
- BlockRenderDispatcher;
- ModelBlockRenderer;
- BakedModel;
- BakedQuad direction;
- AO/light neighbor sampling;
- culling;
- breaking overlay.

NeoForge extensions that must survive any model wrapper:
- ModelData;
- RenderType-aware getQuads;
- getRenderTypes;
- particle icon;
- custom model extensions.

PoseStack rotation alone is insufficient because side lookup and quad Direction
metadata also drive culling/AO/light.

## 21. BlockEntity/custom/accelerated rendering

Audit:
- BlockEntityRenderDispatcher;
- BERs reading FACING/AXIS;
- custom renderers;
- Flywheel/Create visuals;
- instanced rendering;
- shader hooks.

A geometry transform does not fix renderer code that performs its own world-axis
queries.

Valkyrien Skies maintains explicit Flywheel/Create compatibility; Planet should
treat accelerated render engines as a first-class integration boundary.

## 22. Entity rendering/animation

Audit:
- EntityRenderDispatcher;
- LivingEntityRenderer rotations;
- shadows;
- nameplates;
- fire/freeze overlays;
- armor/item layers;
- sleeping/death poses;
- limb/head/body transforms.

Preferred model:
place the entity BODY frame correctly in world space first, then let vanilla or
mod animation run in body-local coordinates.

## 23. Vehicles

Dedicated acceptance, not generic Entity-only:
- Boat buoyancy/surface sampling;
- bubble column impulses;
- land friction;
- passenger placement;
- minecart rail/slope logic;
- dismount search;
- controlled movement.

Physics-ship mods are an external coordinate system and require frame
composition.

## 24. Passengers, leash, attachments, dismount

Audit:
- EntityAttachment;
- seat offsets;
- riding rotation;
- leash anchors;
- dismount floor checks;
- bed/sleep attachment;
- shoulder entities;
- pickup offsets.

Offsets authored around an entity are commonly BODY-local and must be classified.

## 25. Forces, knockback and explosions

Explosion geometry itself is physical/world-space.

Audit consumers:
- explosion knockback;
- melee knockback;
- mace/ram;
- wind charges;
- piston/entity pushes;
- fluid currents.

Do not rotate a general world force just because gravity differs. Only logic that
specifically means vertical/up/down is frame-dependent.

## 26. Networking, prediction and movement validation

Audit client and server together:
- LocalPlayer prediction;
- ServerPlayer movement state;
- ServerGamePacketListenerImpl floating/flying checks;
- vehicle packets;
- teleport corrections;
- yaw/pitch sync;
- fall/ground state sync;
- interpolation.

A client can look correct while server anti-cheat/validation still assumes world
Y and rubber-bands the player.

## 27. Spawning

Exact SpawnPlacementTypes uses physical above/below for ground/water checks.

Audit:
- SpawnPlacementTypes;
- NaturalSpawner;
- heightmap spawn search;
- mob-specific rules;
- spawn eggs;
- world spawn/respawn/bed spawn.

Product decision:
which rules are local-ground semantics and which intentionally remain global
environment/sky semantics.

## 28. Environment, weather, sky and lighting

Gravity and world environment are NOT automatically the same frame.

Require explicit design for:
- rain/snow direction;
- precipitation height;
- snow/ice formation;
- skylight and sky visibility;
- lightning;
- clouds/sky;
- heightmaps;
- day/night environmental tests.

Do not rotate skylight simply because gravity rotates. Specify the planet sky
model first.

## 29. Worldgen and runtime growth

Worldgen has extensive global Y/horizontal assumptions:
- terrain density;
- surface rules;
- trees/features;
- caves/carvers;
- ores;
- lakes/springs;
- structures.

Use PlanetGenerationSpace rather than runtime-patching every feature.

Runtime plant/growth behavior is separate and may need local block-frame
semantics.

## 30. Rails, pistons and moving blocks

Dedicated audit:
- BaseRailBlock/RailState/rail shapes;
- powered/detector/activator rail;
- minecart movement;
- piston facing;
- piston push graph;
- MovingPistonBlock/BE;
- slime/honey moved blocks.

These combine BlockState direction, multi-block traversal, collision and entity
forces.

## 31. Portals and multiblocks

Audit:
- Nether portal shape/orientation;
- End portal/frame;
- gateways;
- doors/beds/double blocks;
- chests;
- third-party multiblock machines.

Some systems should remain physically planar and should NOT bend across a seam.
Decide per subsystem.

## 32. Commands and public rotation/vector APIs

Audit:
- /tp yaw/pitch;
- facing/look commands;
- summon Motion/Rotation;
- predicates using movement/fall distance;
- Direction/Axis codecs.

Prefer vanilla world serialization/network representation and adapt at
entity/block semantic boundaries.

## 33. Mod compatibility classes

Compatibility depends on HOW a mod obtains geometry.

### A. Automatic engine/NeoForge boundary

Examples:
- sided capabilities after side canonicalization;
- standard shape/model accessors;
- standard entity movement hooks after entity-frame completion;
- standard raycast after eye/view correction.

### B. Mostly automatic through vanilla helpers/subclasses

Mods that delegate behavior to vanilla methods should inherit it.

### C. Generic callback adapter or explicit compat required

Direct raw semantic math:
- pos.below/above;
- pos.relative(local FACING);
- Direction.Plane.HORIZONTAL;
- raw y -= gravity;
- custom AABB/onGround.

Do not global-patch BlockPos/Direction to fix this class.

### D. External coordinate/physics engine

Valkyrien-Skies-like ships/contraptions require explicit frame composition and
usually collision/raycast/render integration.

## 33.5 Create-like direct-neighbor stress finding

Create 1.21.1 contains many representative direct-neighbor patterns:
- worldPosition.relative(getBlockState().getValue(FACING));
- pos.relative(direction);
- Direction.Plane.HORIZONTAL loops;
- placement code using BlockPlaceContext.getNearestLookingDirections.

Examples found in pump, fan/nozzle, gantry, redstone link, smart observer,
packager, deployer and other subsystems.

This confirms compatibility class C: standard capability-side reframing is not
enough when a mod chooses its target BlockPos with a canonical-local Direction
before the capability/query boundary.

PlanetFrameApi.localNeighbor is the first public integration primitive for this
case. It returns the physical adjacent target, physical direction, source/target
canonical faces, target canonical local side toward source, and boundary flag.

The runtime compatibility probe compares this API with raw
BlockPos.relative(localDirection) on all six faces. Mismatches are EXPECTED on
rotated frames and demonstrate why BlockPos must remain physical rather than be
globally patched.

Actual Create compatibility remains a later optional integration module; this
generic API is intentionally not Create-specific.

## 34. Public Planet Frame API target

Planetary itself and compat modules should consume the SAME API.

Implemented initial public surface:
- PlanetFrameApi.canonicalBlockFace;
- localSideToPhysical / physicalSideToLocal;
- localNeighbor with physical target + target canonical side.

Candidate extensions:
- canonical block-state frame by Level + BlockPos;
- traversal context with preferred chart;
- entity frame;
- local/world Direction;
- local/world Axis;
- local/world Vec3 direction/vector;
- normal conversion;
- canonical shape rotation;
- seam-aware step/walk;
- physical hit side -> canonical local hit side;
- physical capability side -> canonical local side;
- external-frame composition hooks.

Names must state source/target semantics. Avoid ambiguous transform(...).

## 35. Compatibility test pack

Vanilla representatives:
- asymmetric directional block;
- support block;
- redstone source/sink;
- fluid;
- projectile;
- pathfinding mob;
- rail/minecart;
- boat;
- falling block;
- particle.

NeoForge generic:
- sided IItemHandler;
- sided IFluidHandler;
- sided IEnergyStorage;
- BakedModel with ModelData + RenderTypes;
- directional BER.

Third-party stress:
- Create-like directional/kinetic block;
- pipe/cable graph;
- direct pos.relative(FACING) block;
- Flywheel-like accelerated renderer;
- Valkyrien-Skies-like external transform if available in the target 1.21.1
  stack.

## 36. Phase closure rule

No subsystem is complete because one showcase block/entity works.

For every subsystem record:
1. exact Minecraft 1.21.1 / NeoForge call flow;
2. every world-axis assumption found;
3. PHYSICAL vs LOCAL classification;
4. the smallest shared adapter boundary;
5. pure transform/seam tests;
6. POS_Y, NEG_Y, X, Z, edge and corner runtime acceptance as applicable;
7. representative modded smoke test when an extension API exists;
8. allocation/tick/render performance;
9. any direct third-party math that still bypasses the boundary.

This file is the top-level audit. Per-subsystem research files contain detailed
call graphs and implementation decisions.
