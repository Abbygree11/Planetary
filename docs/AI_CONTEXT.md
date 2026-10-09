# Planetary continuity

> **MANDATORY FIRST READ:** `/AGENTS.md` is the canonical development process.
> Every new AI/coding session must read it before using this continuity log,
> researching or changing code.

Repository: Abbygree11/Planetary
Branch: 2.0 only. Never write main.
Minecraft 1.21.1, NeoForge 21.1.215, Java 21, Gradle 8.12.
User local check: git pull && .\test.ps1 && .\run-client.ps1
Never claim build/runtime success before user confirms it.

## Core architecture
Physical world is ordinary Minecraft XYZ / BlockPos.
Planet is one cube with odd diameter D=2R+1 and one core voxel.
Gravity is six infinite square-pyramid regions around the core.
Local frame is always X=EAST, Y=UP, Z=SOUTH; local DOWN points to core.
Entity deltaMovement is stored in LOCAL coordinates while in Planet gravity.
Entity.move converts local -> world for collision, then world -> local before vanilla collision/onGround/fall logic.
Face change preserves world momentum by old local -> world -> new local.
Camera rotation is smoothed; physics face switch is immediate.

## Worldgen architecture
Do NOT generate six faces independently.
PlanetGenerationSpace maps physical cube shells into one continuous generation space.
Terrain/caves/lakes/biome fields are WRAP.
Rigid structures are AVOID_EDGE.
Goal is normal BiomeSource/NeoForge biome pipeline compatibility, including TerraBlender/BOP where possible.

Dedicated world:
- selectable Planet preset works
- legacy automatic Overworld debug planet/gravity fallback is DISABLED; ordinary Overworld must remain vanilla
- PlanetChunkGenerator registered
- initial test planet radius 48, core (0,128,0)
- one bedrock core, stone interior, grass shell
- fixed plains biome for now
- Planet gravity binds automatically

## Verified player behavior
Working on all faces:
- walking
- jumping
- gravity transitions
- camera
- first person overlay
- walking animation
- pose fit / false swimming fixed
- side push-out fixed

## Current bugs
1. Block rendering is still global-Y oriented.
   Grass top texture does not rotate to local UP.
2. Partial/directional blocks are global-Y.
   Torches are misplaced/oriented incorrectly.
   Pointed dripstone can only be placed along vanilla Y.
   Desired model: vanilla state directions are LOCAL semantics; placement/shape/render translate local<->world.
3. Fluids are global-Y.
   FlowingFluid hardcodes below/above, DOWN/UP, Plane.HORIZONTAL and Y-oriented fluid shapes.
4. Falling blocks:
   d165339 changed support/landing to local DOWN.
   Sand now physically falls in Planet gravity.
   FallingBlock.animateTick now emits FALLING_DUST from local-DOWN face.
   FallingDustParticle now accelerates/caps terminal speed along local DOWN.
   ParticleGravityMixin rotates base Particle gravity toward local DOWN.
   User verified block-breaking particles now fall correctly.
   Base Particle gravity is local and block-breaking particles are verified.
   DripParticle-only handling was insufficient: WaterDropParticle and several
   other classes also override tick() and execute yd -= gravity directly.
   DirectGravityParticleMixin covers gravity-backed override tick classes.
   ClientLevel fluid drip source now redirects below() to local DOWN and, for
   ordinary full blocks, emits from the block's local-DOWN face.
5. Mob AI:
   IMPORTANT performance fix: GroundPathNavigation previously activated custom
   PlanetWalkNodeEvaluator for every mob in any level that merely had a gravity
   binding. In the legacy debug Overworld only the player had gravity, yet all
   Overworld mobs got custom A*. Hooks now use findFor(mob), and automatic
   Overworld debug binding is disabled.
   before d165339 mobs repeatedly jumped on side faces.
   d165339 localized MoveControl target deltas.
   user then reported mobs spin/rotate instead of walking.
   PlanetWalkNodeEvaluator makes normal mobs walk correctly on side faces.
   User verified ordinary walking now works, but mobs spin at gravity-zone boundaries.
   Tie nodes preserve the mob's current gravity face, but user still observed
   spinning at gravity-zone boundaries. Root cause also includes vanilla
   RandomStrollGoal/LandRandomPos choosing targets in global XZ/Y.
   RandomStroll targets are local tangent/same-shell, but entities still stopped
   at the actual edge because a side-face waypoint lies exactly on the surface
   before entity center crosses gravity hysteresis.
   Path node anchors now add a temporary 0.35-block outward overshoot while the
   next node belongs to another face; once entity face changes, anchor returns
   to the normal surface. Awaiting edge-crossing verification.
6. Internal mining across gravity boundary is very disorienting.
   Physics is conceptually correct, but UX needs stronger hysteresis/camera/input transition assist.

## Relevant vanilla hardcodes found
Particle.tick: yd -= 0.04 * gravity.
Particle.move: onGround is blocked negative world-Y.
FallingBlock.tick: pos.below().
PointedDripstoneBlock TIP_DIRECTION only stores UP/DOWN and placement uses getNearestLookingVerticalDirection().
GroundPathNavigation/PathNavigation/WalkNodeEvaluator use below/above, Plane.HORIZONTAL, y+1 and world-Y floor logic.

## Preferred next architecture
Particles:
- ParticleGravityMixin handles classes that use Particle.tick().
- DirectGravityParticleMixin handles vanilla classes that override tick() and
  independently execute yd -= gravity (including DripParticle/WaterDropParticle).
- local-down collision/onGround classification is still future work if visible settling/collision artifacts remain.

Blocks:
- treat vanilla blockstate directions as local values in Planet
- general shape/model transform by gravity frame
- placement/support logic translates local<->world
- avoid per-block hacks when a general boundary transform can solve the class

Mob navigation:
- PlanetWalkNodeEvaluator is now implemented as a first-pass land evaluator.
- per-node gravity frame chooses local N/S/E/W tangent neighbors.
- support is candidate.relative(local DOWN); local UP/DOWN handle one-block steps.
- edge crossing naturally changes frame.
- Path entity target anchor uses cellCenter - localUp*0.5.
- PathNavigation follow distance is measured in the mob local frame.
- RandomStrollGoal/WaterAvoidingRandomStrollGoal now use a local tangent target
  projected onto the same Chebyshev shell instead of vanilla global XZ/Y RandomPos.
- still TODO: other AI target generators, diagonals, complete doors/fences/rails/hazard/water semantics and large-mob node volume handling.

## Recent commits
CURRENT: legacy Overworld debug auto-attach disabled; navigation hooks require findFor(mob).
d165339 local gravity for falling blocks + first MoveControl patch
844008f dimension type light-provider JSON fix
a1696da selectable Planet preset
d6d7b05 restore seamless generation-space files
70a9f7a Planet chunk generator core
90445a3 seamless PlanetGenerationSpace
1bb352a player pose fit
b383327 first-person overlay + AABB grounding epsilon
8a73319 local camera/movement animation
2407453 local-player push-out fix
5f9cd965 yaw transport
35869da local velocity architecture

## External references
Useful entity/camera reference: FugLord77/GravityChanger-1.21.1 branch 1.21.
Vanilla 1.21.1 NeoForm sources inspected through WhosLucid/CobbleLib.
GravityChanger does not solve our complete block/fluid/worldgen layer.

Update this file whenever architecture, verified behavior or active bugs materially change.


## Execution process update
Canonical roadmap: docs/IMPLEMENTATION_PLAN.md
Fluid research baseline: docs/research/FLUIDS_1_21_1.md
Do not resume symptom-by-symptom runtime patching. Each subsystem now requires
source research + architecture + tests + acceptance matrix before implementation.
Current fluid drip runtime mixin is deliberately disabled until the full fluid
phase because its Redirect failed in ClientLevel.doAnimateTick.

Block-frame research baseline: docs/research/BLOCK_FRAME_1_21_1.md
Key discovery: BlockStateBase caches collision/support data without pos/frame, so
canonical local state caches must remain untouched; position-aware APIs adapt
shape/directions at the boundary.


## Mandatory workflow rule: post-change checks
After every code/runtime/worldgen change, always provide the user with a
numbered verification checklist. Include exact run command, expected result for
each check, edge/gravity-boundary cases when relevant, and regression checks for
previously working behavior. Never finish a fix with only "run and test".


## Phase 1 active
Added physical PlanetBlockFrameContext / PlanetBlockStep foundation.
Runtime seam semantics are intentionally distinct from old virtual face-atlas
alias semantics: entering an edge BlockPos transports the chart immediately;
continued movement folds around the adjacent physical cube face.
No runtime mixins use this API yet.


## Phase 1 shape rotation foundation
Added pure cached VoxelShape orientation support after physical block stepping.
PlanetVoxelShapeRotation treats vanilla shapes as canonical local-Y-up,
rotates AABB components around the block center through PlanetGravityFrame,
and caches local->world variants by weak canonical shape key + PlanetFace.
POS_Y returns the original shape. worldToLocal exists for inverse conversion
and round-trip verification. PlanetBlockFrameContext.rotateShape delegates to
the same authoritative transform. No runtime shape mixin uses it yet.


## Phase 1 local side and axis foundation
Added Direction.Axis local<->world conversion using the same PlanetGravityFrame
basis as signed Direction conversion. PlanetBlockFrameContext delegates both.
PlanetBlockStep.targetLocalSideTowardSource now exposes the target block's
canonical local side that physically faces the source. Important: this is NOT
the opposite of transportedDirection at an edge. Edge entry physically travels
along the adjacent face's local UP axis, so the target geometric face toward
the source is local DOWN; transportedDirection is the tangent direction for
continued seam-aware traversal. This distinction is the pure foundation for
isFaceSturdy/support-side logic and future physical sided-query adapters. No
new runtime mixins were added.


## Phase 1 ordered block traversal
Generic local offset(dx,dy,dz) is intentionally NOT part of the kernel.
Unlike vanilla Euclidean BlockPos displacement, compound local displacement on
the cubic shell is path-dependent after frame transport at edges.

PlanetBlockFrameContext.walk(direction, steps) is the authoritative ordered
multi-step primitive: every step continues with transportedDirection.
tangentSteps() returns NORTH/SOUTH/WEST/EAST as four independent seam-aware
PlanetBlockStep values. Exact three-face corners are a deliberate singularity:
four logical tangent transitions map to three physical BlockPos targets; the
two outward transitions share one physical target but keep different target
PlanetFace charts. Physical-cell algorithms must dedupe by BlockPos when
appropriate, while chart-aware traversal may need (BlockPos, PlanetFace).
Tests cover zero-step identity, a deterministic two-edge walk plus exact
reverse, and this corner rule on all 24 face-local corners. See
docs/research/BLOCK_WALK_1_21_1.md. No runtime mixin uses these APIs yet.


## Phase 1 canonical BlockState frame
Traversal chart and physical BlockState orientation are now separate concepts.
PlanetBlockFrameContext is path-dependent and may preserve either valid chart
on an exact edge/corner for reversible topology traversal. PlanetBlockStateFrame
is position-only and canonical for state properties, shapes and future support/
render queries.

PlanetGravityField.selectCanonicalBlockFace uses an explicit X -> Y -> Z axis
tie priority, matching zero-hysteresis entity selection without a preferred
face. PlanetBlockFrameContext no longer exposes rotateShape or axis-property
helpers; those live on PlanetBlockStateFrame. No runtime mixin uses the new
state frame yet.


## Phase 1 canonical support query
Minecraft 1.21.1 BlockStateBase caches isFaceSturdy per Direction x
SupportType in canonical state space. SupportType FULL/CENTER/RIGID therefore
remain vanilla; Planet only reframes the queried side.

PlanetBlockSupportQuery resolves source canonical state frame -> seam-aware
physical neighbor step -> support canonical state frame -> support local side
toward source. Exact edges/corners can legitimately turn source local DOWN into
a tangent support side such as local EAST on the support block.

PlanetBlockStep.targetTraversalSideTowardSource is now named explicitly as
traversal semantics and must not be used for canonical BlockState support.
The zero-gravity core keeps no traversal/gravity face but has canonical POS_Y
BlockState orientation so neighboring support queries can terminate there.
No runtime support mixin is installed yet.


## Master gravity-impact audit
The authoritative cross-cutting checklist is
docs/research/GRAVITY_IMPACT_AUDIT_1_21_1.md.

Planetary must be treated as a local-coordinate/frame layer over the ordinary
physical Minecraft XYZ world, not as a collection of gravity patches.

Four frame concepts are now explicit:
1. canonical BlockState frame: position-only, path-independent;
2. traversal chart: path-dependent at seams/corners;
3. entity/body frame: entity-position dependent/hysteretic;
4. external moving frame: ships/contraptions, composed through physical world.

Do NOT globally redefine BlockPos, Direction, Axis or raw XYZ. Prefer stable
Minecraft/NeoForge boundaries so mods using standard APIs inherit behavior.
Direct third-party world-axis math may require explicit integration.

The master audit now covers blocks, redstone, capabilities, collision, entity
movement, projectiles/raycast, AI, particles, rendering, vehicles, networking,
spawning, environment/weather/skylight, worldgen, commands and external physics
coordinate systems.

Create 1.21.1 is a compatibility stress case because it combines NeoForge sided
capabilities (good generic boundary) with direct Direction/BlockPos/Axis math
(requires further generic adapter research or explicit compat).

Valkyrien Skies is a reference for frame composition: position, direction,
AABB/collision, raycast, camera and particles are transformed separately.
Planet external-frame integration should compose external-local <-> world <->
Planet-local rather than make the coordinate systems compete.


## Phase 1 runtime block resolver and capability boundary
PlanetBlockRuntime is now the shared runtime entry point for block semantics. It
resolves PlanetGravityRuntime activation at the physical BlockPos center and
then exposes canonical BlockState frame, traversal context, support query and
physical/local side conversion.

Dedicated Planet worlds use ordinary physical BlockPos. NeoForge
BlockCapability queries MUST keep their queried target position unchanged.
BlockCapabilityMixin now converts only non-null physical Direction context into
the target block's canonical local BlockState side before provider dispatch.

This matches NeoForge's target-block + context contract and is compatible with
BlockCapabilityCache: cache/invalidation positions remain physical. The old
PlanetSidedQueryFrame position canonicalization is retained only for the legacy
virtual-atlas prototype.

Important limitation: this boundary cannot safely repair a third-party mod that
already chose the wrong target by doing raw BlockPos.relative(localFacing).
That belongs to explicit neighbor-API/integration work; never globally patch
BlockPos.relative.


## Capability runtime acceptance probe
PlanetCapabilityDiagnostics registers private capability
planetary:internal_side_echo on STONE. The provider returns the Direction it
actually receives.

Dedicated Planet player login now verifies 36 real sided dispatches (6 faces x
6 physical Direction values) through ServerLevel.getCapability and
BlockCapabilityMixin, then creates a real BlockCapabilityCache, invalidates the
same physical target position and requires exactly one listener callback plus a
correct post-invalidation lookup.

The six probe targets are STONE blocks one block outward from the core so they
remain in the central loaded area. This probe is diagnostic only and adds no
standard item/fluid/energy gameplay capability.


## Standard NeoForge capability acceptance
PlanetCapabilityDiagnostics now also registers the 1.21.1 standard block
capabilities on STONE using ItemStackHandler, FluidTank and EnergyStorage.
Each provider deliberately returns a handler only for canonical local UP.

Runtime login acceptance checks every gravity face: the physical side mapping to
local UP must return the exact handler for item/fluid/energy, while the opposite
physical side (local DOWN) must return null. This gives 36 standard capability
checks in addition to the 36 generic side-echo mappings and BlockCapabilityCache
invalidation test.

After this passes, generic NeoForge sided capability adaptation is considered
established; the remaining capability task is a real third-party pipe/machine
stress case, especially code that may choose its target with raw
BlockPos.relative(localFacing) before querying a capability.


## Public local-neighbor API / Create-like stress harness
PlanetBlockNeighborQuery is now the neutral canonical one-step neighbor
primitive. PlanetBlockSupportQuery delegates to it so support and generic
mod-neighbor semantics cannot diverge.

Public dev.planetary.api.PlanetFrameApi exposes canonicalBlockFace,
localSideToPhysical, physicalSideToLocal and localNeighbor. localNeighbor accepts
a canonical LOCAL Direction and returns the physical adjacent target plus
physical direction, target canonical face/side and boundary flag.

PlanetCompatibilityDiagnostics runs 36 local-neighbor checks on login and counts
how often the common foreign-mod pattern source.relative(localDirection) would
choose a different physical target. Such mismatches are expected on rotated
faces and are evidence that BlockPos must remain physical.

Create 1.21.1 source audit found this direct-neighbor pattern in many systems,
including pump, fan/nozzle, gantry, redstone link, smart observer, packager and
deployer code. Standard capabilities are automatically covered, but these
direct calls need PlanetFrameApi through a generic callback or optional compat
mixin.


## Phase 2 placement-frame foundation
Detailed research: docs/research/PLACEMENT_1_21_1.md.

BlockHitResult direction/location and BlockPlaceContext physical target BlockPos
remain PHYSICAL. They cannot be globally converted because the same getters are
also used for real neighbor geometry.

PlanetBlockPlacementFrame resolves semantic placement values in the TARGET
block's canonical state frame:
- physicalClickedFace -> localClickedFace;
- physical world click location -> localHitOffset inside the block.

The local hit offset is required for stairs/trapdoors and other code that
currently uses world clickLocation.y to choose local top/bottom.

PlanetFrameApi exposes placementFrame(...) and interactive
localNearestLookingDirections(context). Current Planet entity yaw/pitch are
body-local, so Direction.orderedByNearest(player) already produces local
orientation ordering; the API fixes BlockPlaceContext's non-replacing reorder
to use localClickedFace.opposite instead of the physical hit face.

Player-less DirectionalPlaceContext is intentionally not inferred and must be
audited separately for dispenser/falling-block paths.

No placement runtime mixin is installed yet.


## First Phase-2 runtime placement adapters
Mixins now adapt RotatedPillarBlock, HopperBlock and SlabBlock placement.

RotatedPillar AXIS and Hopper FACING are stored as canonical LOCAL BlockState
semantics derived from PlanetBlockPlacementFrame. Slab placement and replacement
use local clicked face plus localHitOffset.y, so TOP/BOTTOM is no longer tied to
physical world Y.

PlanetFrameApi.localNearestLookingDirections was corrected for exact edges:
Direction.orderedByNearest(player) is player-body-local, so each result is now
re-expressed player-local -> physical -> target-canonical-local before the
non-replacing clicked-face reorder.

PlanetPlacementDiagnostics invokes the real vanilla getStateForPlacement methods
for log/hopper/slab over 6 faces x 6 local clicked directions = 108 state checks
without placing blocks. Successful login prints the placement-probe message.

Shapes/models are still Phase 3; canonical local state can therefore be correct
while the physical rendered/collision orientation is not yet rotated.


## Phase 2 support/update runtime adapters
Detailed research: docs/research/SUPPORT_UPDATES_1_21_1.md.

PlanetBlockSupportRuntime is the shared LevelReader -> Planet support bridge. It
activates only for actual bound Level instances and delegates support geometry to
PlanetBlockSupportQuery.

Runtime adapters now cover:
- BaseTorchBlock local-DOWN support;
- WallTorchBlock local FACING support + local placement;
- RedstoneWallTorchBlock updateShape (its signal math remains Phase 7A);
- LadderBlock local placement/support/updateShape with water tick preservation;
- FaceAttachedHorizontalDirectionalBlock static canAttach, placement and
  updateShape, benefiting lever/button and subclasses that inherit the base
  behavior.

The key update invariant is physical neighborPos vs resolved physical supportPos.
Do not compare updateShape's physical Direction directly with canonical local
FACING/FACE.

PlanetFrameApi.localHorizontalDirection converts player body-local horizontal
orientation through physical world into target canonical local frame, with a
horizontal fallback from reframed nearest-looking order at exact frame changes.

PlanetSupportDiagnostics performs 30 real canSurvive + 30 real updateShape
checks across all six faces using interior cells two blocks from core and does
not mutate the world.


## Phase 2 support acceptance verified
User verified in-game support removal after 333cbf9: standing torch, wall torch,
ladder and lever on rotated faces survive with their real local support and are
removed when that physical support block is broken. Existing movement and probes
remained healthy.

## Phase 3 first physical VoxelShape boundary
Detailed research: docs/research/SHAPES_1_21_1.md.

Vanilla BlockState shape APIs nest, so rotating every RETURN independently would
double/triple-transform default/dynamic shapes. PlanetBlockShapeRuntime now uses
a ThreadLocal query depth and only the outermost physical query rotates.

Physical shape APIs in this patch:
- getShape, both overloads
- getCollisionShape, both overloads
- getVisualShape
- getInteractionShape

Canonical-only scopes:
- getBlockSupportShape
- getOcclusionShape

Those canonical-only scopes suppress nested getShape rotation and preserve the
already verified canonical isFaceSturdy/support contract.

PlanetVoxelShapeRotation preserves Shapes.block and empty instances exactly so
BlockCollisions keeps its full-cube identity fast path.

PlanetShapeDiagnostics checks all six faces: rotated slab outline/collision/
visual, canonical support+occlusion, rotated hopper interaction shape and full
stone collision identity. Baked-model rendering/culling is intentionally not
rotated yet.


## StandingAndWallBlockItem placement fix
User verified Phase-3 slab shapes and ladder behavior, but found torch item
variant selection wrong on rotated faces: floor clicks could place wall torch,
wall clicks could place standing torch, with face-dependent behavior.

Root cause is item-level, not support/shape:
StandingAndWallBlockItem.getPlacementState still iterated raw
BlockPlaceContext.getNearestLookingDirections while attachmentDirection=DOWN is
canonical local semantics.

StandingAndWallBlockItemMixin now redirects only that direction-order call to
PlanetFrameApi.localNearestLookingDirections, preserving all physical geometry,
canPlace/canSurvive and obstruction logic. Outside Planet it falls back to
vanilla.

PlanetStandingWallPlacementDiagnostics tests a deliberately multi-supported
"hole" target on all six faces: 6 local-floor clicks must choose standing torch
and 24 local-wall clicks must choose wall torch with matching local FACING.


## Standing/wall diagnostic registry-freeze fix
The first standing/wall diagnostic incorrectly constructed a new
StandingAndWallBlockItem lazily on player login. Minecraft registries are
already frozen at that point, so Item construction threw
IllegalStateException: Registry is already frozen.

The diagnostic now uses the real registered Items.TORCH and invokes its
protected getPlacementState through StandingAndWallBlockItemAccessor (@Invoker).
No new Item/Block is constructed after registry freeze. The production
StandingAndWallBlockItemMixin behavior is unchanged.


## Phase 3 static baked-model + culling boundary
Detailed research: docs/research/RENDERING_1_21_1.md.

User verified that the rotated VoxelShape selection outline is correct in game.

Static MODEL rendering now uses a cached oriented BakedModel view supplied at
the extended NeoForge ModelBlockRenderer.tesselateBlock boundary. The wrapper
keeps ModelData/RenderType and all BakedModelWrapper delegated extensions.

Renderer physical side -> source canonical local side before original
getQuads. Returned quads use NeoForge QuadTransformers to rotate vertex
positions and packed normals around block center, then receive a physical
BakedQuad.direction. Original registered models/quads are never mutated.

Caches:
- weak original BakedModel -> six face views;
- weak original BakedQuad -> six physical quad views.

Block.shouldRenderFace receives physical world geometry but canonical BlockState
semantics. Client culling is therefore intercepted in Planet space and converts
the physical source/target faces independently into each block's canonical
local frame. A custom thread-local 2048-entry LRU uses both local sides in its
key because vanilla's state-pair + one-Direction cache cannot distinguish
gravity frames.

Exact seam unit coverage uses POS_X source -> physical WEST -> POS_Y target:
source local side DOWN while target side toward source is local EAST.

Still separate: BER/Flywheel/custom renderers, model offsets, fluid rendering,
and the policy for directional shade/environment lighting.


## 2026-10-04 broad runtime acceptance findings
After static baked-model/culling work, user performed a broad manual sweep.

Confirmed good:
- slab shape/model broadly correct away from exact seams;
- ladder placement/behavior correct;
- fences/gates basic placement orientation correct;
- pointed dripstone falling behavior already follows local gravity.

Observed open issues:
- grass generated correctly but random-ticked back to dirt / failed to spread on
  all faces except +Y;
- grass side overlay differs by face and side-face shading is suspicious;
- slabs behave ambiguously on exact gravity edges;
- torch flame/smoke sprite emission origin is not local-frame aware;
- fence gets unwanted physical +Y arms;
- door and pressure plate placement fails;
- pointed dripstone cannot be placed where expected even though falling is local;
- bed placement is misoriented;
- thrown potions and arrows still accelerate in global -Y;
- fluids still spread using global gravity.

These observations are now explicit roadmap items rather than isolated bugs.

## Local grass growth + fence connection pass
Detailed research: docs/research/GROWTH_CONNECTIONS_1_21_1.md.

SpreadingSnowyDirtBlock natural tick now treats canonical local UP as "above"
for survival, light occlusion, water, brightness and SNOWY checks.

PlanetGrowthTopology defines the ONLY growth-specific compound random offset:
local Y segment -> local Z -> local X, using transported traversal at seams.
This does not weaken the project-wide rule that generic unordered local
offset(dx,dy,dz) is forbidden.

SnowyDirtBlock placement/update also tracks local UP.

FenceBlock N/E/S/W are canonical local properties. Placement resolves four
PlanetBlockNeighborQuery tangent neighbors. updateShape matches physical
neighborPos against those logical queries rather than trusting physical
Direction.Plane.HORIZONTAL; this is seam/corner safe and preserves target-local
sturdy/gate direction semantics.


## Grass shared-edge green-rim diagnosis/fix
User verified grass growth and fence topology were otherwise correct but a green
rim remained:
- +/-Z on every boundary;
- -Y only where adjacent to +/-X.

The pattern exactly matches canonical tie order X -> Y -> Z, proving the rim is
not random culling. One shared edge BlockPos has one canonical BlockState frame,
while grass physically exposes two cube-surface normals.

Do not change canonical tie order; that would only move the defect and destabilize
directional state semantics.

Client static-model rendering now has a narrow surface seam rule for
SpreadingSnowyDirtBlock (GrassBlock/MyceliumBlock): every physical side that is
the outward UP of one candidate face uses the original model's local-UP quad
rotated through that candidate face. Other sides still use the one canonical
frame. At cube corners this naturally supports three outward surface faces.

Collision/support/placement/state frames remain untouched.


## Multi-block/support placement pass
Detailed research: docs/research/MULTIBLOCK_PLACEMENT_1_21_1.md.

Pressure plates now use local DOWN for support/update and rotate their thin
entity-trigger AABB into physical world coordinates. Direct signal-side and
neighbor-notification redstone semantics remain Phase 7A.

Door placement now uses target-local horizontal FACING, local-UP second half,
local left/right hinge topology and local X/Z hit offsets. Upper FACING is
reframed into its own canonical frame. Survival/update use local DOWN/UP pair
positions. Door redstone neighborChanged remains open.

Bed placement now uses local FACING for FOOT->HEAD physical topology and reframes
HEAD FACING into the head cell's canonical frame. Pair update uses physical
neighborPos resolved from local FACING. Bed BER/sleep/dismount/bounce are still
separate.

Pointed dripstone manual placement, support, thickness and local vertical
updateShape are adapted. Natural growth, fluid/cauldron vertical scans, drip
particles and chain-fall scanning are explicitly not claimed complete.

PlanetPlacementRuntime adds the reusable multi-block invariant:
source local direction -> physical Direction -> target canonical local direction.


## Planned transition-comfort / underground-navigation work
User explicitly requested smoother gravity-zone transitions and called out
underground mining as particularly disorienting.

Do not treat this as merely a camera lerp at the exact seam.

Phase 11 now requires research of two distinct layers:
1. a pre-transition visual frame that begins bending the camera/horizon before
   the exact tie plane;
2. a possible ENTITY-only continuous gravity frame in a configurable transition
   band, while block/state semantics remain discrete canonical PlanetFace frames.

Promising architecture:
    blocks = discrete canonical frames
    player/entity transition frame = continuous near a boundary

A continuous entity frame would derive from the two strongest competing face
scores and smoothly blend their UP vectors, with a transported tangent/forward
basis. Camera, movement and possibly physical acceleration should consume the
same frame if this approach survives collision/client-server research.

Underground is a separate acceptance gate:
- straight 1x2 tunnel through internal gravity boundaries;
- no visible horizon required to understand the bend;
- stronger hysteresis/orientation inertia to avoid oscillation;
- preserve forward heading through the bend;
- block targeting must remain physical/correct while the body frame rotates;
- optional subtle environmental cue may indicate the upcoming bend;
- cave widening is only optional worldgen polish, never the underlying fix.

Also evaluate an optional mild perspective/horizon distortion toward the upcoming
face, increasing before the boundary. It must be disableable and a reduced-motion
mode must exist.

No runtime change was made for this planning update.


## Locked worldgen/far-terrain architecture decisions
User approved the following long-term direction. Treat these as design
constraints unless later runtime research disproves them.

### Macro elevation
Planet terrain may have a smooth global macro field that raises the statistical
MEAN land elevation toward face centers. This is not six per-face mountains and
must not depend on canonical face tie-breaking. Continentalness/erosion/peaks
remain independent enough that oceans/plains/mountains can occur anywhere.

### Ocean
Ocean does NOT follow the macro dome. Sea level is one constant Planet
elevation/cube-shell radius.

Do not pursue:
- a continuously sloped custom fluid surface;
- a one-block sea staircase following the dome;
- special boat "auto-climb water step" behavior.

Land intersects the constant sea shell. Any coast smoothing is local to a
bounded coastal band; interior terrain is not globally a function of distance to
the ocean. Cliffs/fjords are valid high-relief coast types.

### Rivers/hydrology
Base terrain exists FIRST. Hydrology analyzes that terrain and chooses drainage
routes, then modifies only a local neighborhood around channels.

Never define world terrain height as distance to the nearest river. This avoids
the artificial "hills between every river/ocean" landscape.

River water is piecewise constant in Planet elevation over long reaches. Height
loss is concentrated into intentional rapids/waterfalls/gorges/cascades.
Large navigable rivers prefer low gradients/long flat reaches; mountain streams
need not be navigable upstream. Lakes have constant local levels. Deltas converge
to the constant sea shell.

River carving is bounded/local:
small difference -> channel/floodplain
medium -> valley
large -> canyon
sharp drop -> waterfall/rapids

### Biome/worldgen mod compatibility
Planetary should keep standard/data-driven BiomeSource and ordinary biome
decoration where possible. Planetary owns topology/terrain adaptation/hydrology,
not a hard-coded biome registry.

Phase 8 must test TerraBlender-style composition, Biomes O' Plenty, Oh The
Biomes We've Gone and representative datapack/mod features early.

Planet Normal / Large Biomes / Amplified should be Planet terrain profiles, not
replacement vanilla generators. Mods that replace the entire ChunkGenerator or
perform custom global-Y terrain math require explicit adapters and are not
promised automatic compatibility.

### Far terrain / LOD
Goal: ~1000-block and potentially farther terrain visibility without full chunk
simulation.

Far terrain must NOT instantiate ordinary full chunks merely to retain top
blocks. It should sample the deterministic Planet worldgen/surface representation
directly and build render-only coarse meshes.

Near world remains ordinary chunks. Far LOD has no entities, AI, block entities,
ticks, collision, fluid simulation or invisible caves/ores. Research
quadtree/geometry-clipmap LOD with progressively coarser sampling and seamless
PlanetGenerationSpace edge stitching. Full and far terrain must derive from the
same seed/worldgen inputs so approaching a region does not change its surface.

Current active runtime stage remains Phase 2 multi-block placement acceptance:
pressure plate, door, bed and pointed dripstone from commits 3d49fd3/2decb8e.
Those gameplay changes are still awaiting the user's manual verification.


## 2026-10-04 multi-block acceptance follow-up
Manual acceptance after the multi-block pass:
- pressure plate placement, physical trigger and support: PASS;
- bed visible geometry on rotated gravity disagreed with its state/collision;
- pointed dripstone placement worked, but chains visually split and unsupported
  stalactites fell as delayed individual pieces without reliable damage;
- redstone wire topology and piston extension remain wrong on rotated faces.

Exact 1.21.1 root causes:
- BedRenderer bypasses ModelBlockRenderer and applies fixed vanilla world-axis
  transforms.
- PointedDripstone OffsetType.XZ seeds from physical BlockPos.x/z. Local vertical
  on +/-X or +/-Z changes one of those coordinates, randomizing each segment.
- PointedDripstoneBlock.spawnFallingStalactite scans raw Direction.DOWN and only
  the reached TIP/TIP_MERGE receives FallingBlockEntity.setHurtsEntities. On a
  rotated face the chain scan stopped after the root.

Runtime follow-up commit 398b334:
- BedRendererGravityMixin applies canonical-local -> physical block-center
  rotation around vanilla BedRenderer;
- PlanetBlockOffsetRuntime provides canonical-local integer seed coordinates;
- pointed dripstone model and shape offsets now use local X/Z seed semantics;
- PlanetDripstoneFalling reproduces the vanilla one-tick chain fall through
  local DOWN and preserves terminal-tip setHurtsEntities(size, 40).

Redstone/piston intentionally remain unpatched here. They are cross-cutting:
wire horizontal/up/down topology, signal-side conventions, PistonStructureResolver
movement, MovingPistonBlockEntity and moving-piston rendering all need one
coherent Phase 7A/piston pass rather than a one-off Direction substitution.


## Pending fall-distance verification after 4ec055f
Runtime follow-up commit 4ec055f changes the Entity.move -> checkFallDamage
boundary so vanilla receives the already-classified LOCAL vertical displacement
(PlanetEntityMotion.CollisionResult.actualLocal.y) instead of depending on
physical world-Y movement.

Reason:
FallingBlockEntity.setHurtsEntities was restored for pointed-dripstone tips, but
falling-stalactite damage still did not occur on rotated faces. Vanilla
FallingBlockEntity.causeFallDamage scales damage from Entity.fallDistance, which
is accumulated by Entity.checkFallDamage from its vertical-movement argument.
On side gravity, physical world-Y may be zero even during a real local fall.

This is intentionally a generic entity fall-distance boundary, not a
dripstone-only damage hook. It should also restore ordinary fall-distance
semantics for entities on rotated Planet faces. Manual verification is pending;
do not mark the issue closed until both falling stalactite damage and ordinary
player fall damage are checked.


## Deep fall-damage investigation after c055319
User verified that neither player fall damage nor falling pointed-dripstone
damage was fixed by the previous generic Entity.move patch.

Exact source research changed the diagnosis:

1. ServerPlayer is NOT covered by Entity.move fall accounting.
   ServerPlayer.checkFallDamage is intentionally empty in 1.21.1.
   ServerGamePacketListenerImpl.handleMovePlayer calls
   ServerPlayer.doCheckFallDamage with physical world packet dx/dy/dz.
   doCheckFallDamage later passes its dy to parent checkFallDamage.
   This is the proven root cause for player fall damage on rotated faces.

2. GravityChanger 1.21 independently carries a dedicated ServerPlayer
   fall-distance mixin for this reason, confirming the separate boundary.

3. FallingBlockEntity does use ordinary Entity.checkFallDamage. NeoForge 1.21.1
   does not replace that damage path. Since our generic local movement adapter
   should be sufficient, do NOT add another speculative dripstone damage rule.

Next runtime patch:
- ServerGamePacketListenerGravityMixin reframes all three arguments of
  ServerPlayer.doCheckFallDamage world -> local, so both support rewind and the
  parent fall-distance call receive local movement.
- temporary pointed-dripstone-only FallTrace diagnostics report:
  armed terminal tip; checkFallDamage localDy/grounded/fallDistance; and
  causeFallDamage hurtEntities/damage/AABB/eligible-target counts.

Interpret logs using docs/research/FALL_DAMAGE_1_21_1.md.


## Falling-block spawn anchor diagnosis after 8f73253
Manual verification:
- player fall damage now PASS;
- falling pointed-dripstone damages player PASS;
- creeper centered in a one-block hole was not damaged.

Diagnostic line:
eligibleTargets=0, nearbyLiving1=1 with hurtEntities=true and positive
fallDistance. This proved the remaining issue was exact entity AABB overlap.

Root cause:
FallingBlockEntity.fall vanilla constructor anchor is
(x+0.5, y, z+0.5), i.e. center of WORLD-DOWN face. Planet rotates the AABB but
previously did not rotate/generalize that anchor. On side gravity this leaves the
falling entity shifted by 0.5 block along a local tangent axis.

Generic invariant now implemented in 4cfff7c:
    anchor = source block center + 0.5 * physical(local DOWN)

FallingBlockEntityGravityMixin modifies constructor arguments inside static
fall(...) before addFreshEntity. World-DOWN remains byte-for-byte equivalent in
position. The fix applies to pointed dripstone, sand, gravel, anvils and other
FallingBlockEntity users.

Temporary FallTrace diagnostics were removed. Manual acceptance pending:
centered mob in 1x1 hole should be hit; falling sand/gravel/anvil should remain
centered on rotated faces.


## FallingBlockRenderer follow-up after 4cfff7c
User verified falling-block/dripstone damage and mob hits now work. Remaining
issue is visual: sand and anvils shift by 0.5 block on entity conversion and
appear to snap while falling through a hole.

Exact vanilla FallingBlockRenderer:
- samples BlockPos from (entity.x, entity.boundingBox.maxY, entity.z);
- translates model by (-0.5, 0, -0.5).

Those are valid only when Entity.position is the center of WORLD-DOWN face.

Renderer invariant mirrors physical spawn:
    cellCenter = entityAnchor - 0.5 * physical(local DOWN)
    translation = -0.5 * physical(local DOWN) - (0.5,0.5,0.5)

FallingBlockRendererGravityMixin changes only client render BlockPos and
PoseStack translation. Do not move the entity again; physical anchor/collision/
damage from 4cfff7c are already correct.


## Block-destroy particle physical +Y drift
After falling blocks were accepted, user found block destruction particles on
+/-X and +/-Z consistently displaced/drifting toward physical +Y.

Exact source:
ParticleEngine.destroy samples the physical rotated VoxelShape and passes a
radial physical velocity into TerrainParticle. TerrainParticle delegates to the
generic Particle velocity constructor.

Particle(ClientLevel,x,y,z,xd,yd,zd) normalizes/randomizes velocity and then
unconditionally adds +0.1 to yd. Particle.setPower also preserves a world-Y
baseline via (yd - 0.1)*power + 0.1.

The semantic mistake is therefore the generic UP bias, not destroy sampling.

Implemented:
- PlanetParticleMotion.rotateVanillaUpBias:
  remove world +Y 0.1 and add local UP * 0.1;
- PlanetParticleMotion.scaleAroundLocalUpBias:
  bias + (velocity-bias)*power;
- ParticleGravityMixin applies those at the generic constructor and setPower
  boundaries, while +Y/POS_Y stays vanilla.

Do not rotate ParticleEngine.destroy's radial velocity; it is physical geometry.

Still open: Particle.move onGround/stoppedByCollision world-Y semantics and
emitter-specific origins (notably torch flame/smoke).


## 2026-10-04 torch particle emitter follow-up
Manual acceptance of the previous block-destroy particle fix:
- destruction/crack sprites no longer drift toward physical +Y on rotated faces;
- ebad1e1 local-UP launch-bias correction is PASS.

Next Phase 4 source audit separated particle MOTION from particle EMISSION.

Exact vanilla emitter assumptions:
- TorchBlock.animateTick spawns smoke/flame at block center + world UP * 0.2;
- WallTorchBlock.animateTick spawns at block center + world UP * 0.22
  + FACING.opposite() * 0.27 in world X/Z.

Planet policy:
- TorchBlock/WallTorchBlock FACING and vertical meaning are canonical LOCAL;
- keep particle type/count/velocity vanilla;
- transform only the block-local emitter offset into physical world XYZ.

Implemented:
- PlanetParticleEmitter pure local-offset -> physical Vec3 helper;
- standing torch offset local (0, +0.2, 0);
- wall torch offset local UP * 0.22 + local FACING.opposite() * 0.27;
- TorchParticleGravityMixin modifies only Level.addParticle XYZ arguments;
- POS_Y invocation remains untouched;
- PlanetParticleEmitterTest covers all six faces and all four wall facings;
- detailed audit: docs/research/PARTICLES_1_21_1.md.

Manual runtime acceptance is pending for standing and wall torches on all faces
and at one gravity edge.

Do not close Phase 4 after this. The next generic particle item is
Particle.move/tick: onGround, stoppedByCollision, vertical-block detection and
ground friction are still expressed in physical world-Y/world-XZ rather than
the selected local gravity frame.


## 2026-10-04 torch emitter acceptance correction
Manual screenshots after the first torch-emitter pass found:
- redstone torch particles still followed physical +Y;
- ordinary/soul wall-torch flame particles sat too far toward local DOWN on all
  rotated faces; the visual mismatch became especially obvious deeper toward the
  core.

Exact source re-check found a concrete arithmetic error in the first adapter:
WallTorchBlock.animateTick first sets d1 = pos.y + 0.7, then adds another +0.22.
Relative to the block center y+0.5, the full semantic local-UP offset is +0.42,
not +0.22. The first PlanetParticleEmitter implementation dropped +0.20.

Architecture correction:
- do not reconstruct vanilla emitter constants in the mixin;
- intercept the final XYZ already computed by vanilla;
- subtract physical block center;
- interpret that complete numeric delta as canonical LOCAL coordinates;
- rotate only that delta through the block PlanetGravityFrame.

This preserves the exact ordinary-wall +0.42 rise and also preserves random
jitter without consuming extra RNG.

RedstoneTorchBlock and RedstoneWallTorchBlock have separate animateTick methods,
so the normal TorchBlock adapter never touched them. Added
RedstoneTorchParticleGravityMixin using the same final-coordinate transform.
This changes only visual particle origin; redstone signal-side/topology behavior
remains Phase 7A.

Manual acceptance pending after commits b25439d..7c499be:
- ordinary standing + wall torch particles on all six faces;
- soul standing + wall torch particles;
- redstone standing + wall torch particles;
- compare multiple depths toward the core on the same gravity face;
- exact-edge placement;
- +Y remains vanilla.


## 2026-10-04 torch emitter acceptance PASS
User manually verified after the corrected full-offset transform:
- ordinary standing/wall torch particles: PASS on rotated faces;
- soul torch variants: PASS;
- redstone standing/wall torch particles: PASS;
- depth-dependent drift toward local DOWN is gone.

Remaining visible mismatch exists only exactly on a gravity edge.

Do NOT patch that in torch-specific code. This is the already-known generic
exact-edge policy problem: one physical BlockPos may admit multiple gravity-face
charts, while BlockState semantics intentionally use one position-only canonical
frame. The roadmap already tracks exact-edge placement/player-body-vs-canonical
policy separately.

Torch/redstone particle emission is therefore accepted for ordinary face-local
positions. Exact-edge visual placement remains deferred to the common block-edge
mechanism.


## 2026-10-04 generic Particle.move/tick local-axis pass
After torch emitters were manually accepted away from exact edges, Phase 4
continued with the base Particle physics boundary.

Exact vanilla issue:
- collision clipping is correctly physical XYZ;
- semantic consequences were not:
  - stoppedByCollision used blocked world Y;
  - onGround used blocked negative world Y;
  - collision zeroed world X/Z velocity;
  - base tick speedUpWhenYMotionIsBlocked checked y==yo and scaled world X/Z;
  - onGround friction scaled world X/Z.

Implemented in 809516a:
- Particle.move still runs vanilla collision geometry unchanged;
- requested/actual displacement are re-expressed in Planet local frame after
  movement;
- local DOWN collision determines onGround;
- local vertical blocking determines stoppedByCollision;
- only collided local tangent X/Z velocity components are zeroed;
- base tick physical-X/Z 1.1/0.7 effects are algebraically undone and reapplied
  to local X/Z;
- scalar friction remains vanilla;
- POS_Y stays vanilla;
- pure tests cover all six faces plus local tangent/ground cases.

Detailed source audit: docs/research/PARTICLES_1_21_1.md.

Runtime acceptance is pending. Do not claim Phase 4 complete afterward: direct
custom-tick particles (for example DragonBreath/Bubble/world-XZ current logic),
remaining emitter helpers, weather and fluid-coupled particles still require
their owning audits.


## 2026-10-05 destroy-particle regression from stoppedByCollision
Manual acceptance of the generic Particle.move pass found block-destroy
TerrainParticles remained confined to the broken block instead of dispersing.

Source check:
ParticleEngine.destroy samples TerrainParticle origins from inside the destroyed
VoxelShape.

Root cause:
the first local-axis move adapter rewrote Particle.stoppedByCollision from
physical world-Y clipping to local-Y clipping. stoppedByCollision is sticky:
future Particle.move() returns immediately once it becomes true. That is too
strong to reinterpret generically for particles initially inside block shapes.

Correction:
- do NOT rewrite stoppedByCollision;
- keep local onGround;
- keep local tangent collision velocity zeroing;
- keep local ground friction / blocked-vertical tangent speedup;
- leave the sticky internal stop to vanilla.

Manual re-acceptance required:
block-destroy particles must again disperse outside the source block while
retaining the previously fixed local-UP launch bias.


## 2026-10-05 generic Particle.move/tick pass ROLLED BACK
User rejected the destroy-particle result after the attempted stoppedByCollision
correction: TerrainParticle fragments still failed to disperse radially around
the broken block.

Do not continue patching individual flags on top of that unaccepted runtime
state. The entire generic Particle.move/tick runtime pass was rolled back to
known-good commit 5f4ddde for:
- PlanetParticleMotion.java
- ParticleGravityMixin.java
- PlanetParticleMotionTest.java

Accepted particle work that remains intact:
- local-UP constructor/setPower launch bias;
- falling-dust dedicated gravity adapter;
- ordinary/soul/redstone torch emitter transforms;
- falling-block particle origin work.

Next redesign requirement:
start from vanilla ParticleEngine.destroy -> TerrainParticle -> Particle.tick/
move as the acceptance reference. Any local onGround/collision/friction adapter
must preserve destroy dispersion before being allowed to affect other particles.


## 2026-10-05 narrow particle landing pass
After rollback, user confirmed a smaller remaining asymmetry:
destroy fragments on side and -Y gravity fall correctly but slide along the
local floor for a short time; +Y fragments settle immediately.

Diagnosis:
vanilla Particle.move still sets onGround only from blocked negative WORLD Y.
Therefore rotated-face particles can physically land without entering vanilla
ground-friction behavior.

New patch is intentionally narrower than the rejected generic move pass:
- capture requested and actual displacement around vanilla Particle.move;
- overwrite ONLY onGround using local Y collision/downward intent;
- do not alter stoppedByCollision;
- do not alter vanilla collision velocity zeroing;
- when base Particle.tick applies world-X/Z 0.7F ground friction, remap only that
  multiplier to local X/Z on side faces;
- +/-Y friction plane remains vanilla world X/Z.

Regression gate:
ParticleEngine.destroy/TerrainParticle radial dispersion must remain unchanged.
Manual acceptance pending.


## 2026-10-05 residual particle slide diagnosis
User verified the narrow landing pass preserved destroy dispersion, but side/-Y
destroy fragments still slid briefly after touching the local floor.

Exact cause:
+Y vanilla landing also sets Particle.stoppedByCollision because requested
world-Y becomes fully blocked. That sticky flag prevents future move() calls.
On side gravity, local vertical is world X/Z, so vanilla never sets it.

New narrow adaptation:
- only after confirmed local-ground contact;
- only if requested local Y is downward and >=1e-5;
- only if actual local Y is ~0;
- set stoppedByCollision=true.

No collision velocity components are rewritten. Head/tangent collisions keep
vanilla behavior. This must again pass destroy radial-dispersion regression.


## 2026-10-05 TerrainParticle landing trace
Manual acceptance after the narrow local stoppedByCollision landing patch:
- destroy radial dispersion: still PASS;
- residual side/-Y sliding after local-floor contact: STILL PRESENT.

Therefore the stoppedByCollision hypothesis is not accepted as the root cause.

Per AGENTS.md, no further speculative runtime patch should be stacked on top.
Added a temporary capped TerrainParticle diagnostic in ParticleGravityMixin.

The trace records, for up to 8 local-ground landings:
- selected gravity face;
- requested and actual LOCAL displacement;
- vanilla onGround before Planet rewrite;
- local onGround after rewrite;
- vanilla stoppedByCollision before Planet write;
- stoppedByCollision after the narrow local landing rule;
- particle position/velocity at landing;
- position/velocity/onGround/stopped for the next four tick returns.

Expected log prefix:
    [Planetary/ParticleTrace]

Use this to determine whether the visible slide is:
- missing local landing classification;
- stoppedByCollision not being set;
- position still changing despite sticky stop;
- or a different render/interpolation path.

Remove this diagnostic immediately after the failing gate is identified.


## 2026-10-05 TerrainParticle landing root cause confirmed
Runtime trace disproved the previous assumptions and exposed the exact bug.

Example false LAND:
    requestedLocal.y = -0.0024128631882789006
    actualLocal.y    = -0.0024128631882760487

Difference is ~2.8e-15. No collision occurred.

Cause:
Planetary reconstructed actual movement as:
    particleNewPos - particleOldPos

That subtraction introduces coordinate-rounding noise. Then
isLocalGroundCollision used exact inequality, matching vanilla's source shape
but NOT vanilla's data provenance. Vanilla compares requested movement against
the exact clipped Vec3 returned directly by Entity.collideBoundingBox.

Consequences of the bad reconstruction:
- ordinary airborne ticks were marked onGround;
- ground friction was applied in the air;
- trace budget was consumed before true floor contact;
- sticky local landing stop never triggered because actualLocal.y was not near 0;
- visible motion diverged from +Y behavior.

Correction in 77734b4:
- Redirect only the Entity.collideBoundingBox invocation inside Particle.move;
- call the exact same vanilla method and return its result unchanged;
- capture that exact returned Vec3 for semantic post-processing;
- if vanilla skips collision solving, actual movement defaults exactly to
  requested movement;
- no position-delta reconstruction and no epsilon heuristic.

Temporary TerrainParticle trace remains capped for one acceptance pass and must
be removed once behavior is confirmed.


## 2026-10-07 TerrainParticle landing policy finalized for acceptance
User clarified the visual target precisely:
- +Y destroy particles fall and disappear without a visible surface-crawling
  phase;
- rotated-face particles were reaching the local floor, visibly spreading along
  it, then disappearing.

The exact collision-result capture fixed false airborne onGround, but did not
remove the remaining visible post-contact slide.

Current narrow policy:
- keep vanilla physical collision solver unchanged;
- use the exact clipped Vec3 from Entity.collideBoundingBox;
- detect the first REAL LOCAL-DOWN clipping event;
- on that first local-ground event set onGround=true and
  stoppedByCollision=true immediately;
- do not wait for a second tick/full-zero vertical displacement;
- do not rewrite tangent velocity components or collision geometry.

This is intentionally targeted at eliminating the extra rotated-face
post-contact movement phase while preserving the already accepted destroy burst.

Temporary ParticleTrace diagnostics have been removed.

Runtime acceptance pending:
- radial burst unchanged;
- no false in-air landing;
- no visible crawl/spread after first local-floor contact;
- +Y remains vanilla.


## 2026-10-07 TerrainParticle crawl root cause moved to render anchor
Manual acceptance rejected the first-contact stoppedByCollision attempt: rotated
destroy particles still visibly touched the local floor, spread/crawled, then
disappeared.

A deeper exact-source review changed the owning mechanism.

Vanilla facts:
- TerrainParticle does NOT override tick to remove on ground.
- Particle.setPos stores x/z at AABB center but y at AABB.minY.
- Particle.setLocationFromBoundingbox restores the same convention.
- SingleQuadParticle.renderRotatedQuad interpolates Particle xo/yo/zo -> x/y/z
  directly as the visual quad anchor.

Therefore on +Y the visual anchor is already the center of the local-DOWN face
of the particle AABB (world minY). Once the AABB reaches the floor, the quad is
centered on the floor plane and is naturally depth-occluded, which visually
looks like fall -> disappear rather than a surface-crawling phase.

On rotated gravity the same stored position is NOT the local-DOWN face:
- -Y should use maxY;
- +/-X should use minX/maxX face center;
- +/-Z should use minZ/maxZ face center.

This explains why collision/onGround/stoppedByCollision changes did not remove
the visible crawl.

Runtime cleanup:
- restored PlanetParticleMotion, ParticleGravityMixin and its tests to the last
  accepted pre-landing-experiment state (5f4ddde behavior);
- removed all unaccepted generic landing hooks and collision Redirects.

New boundary:
- PlanetParticleRenderAnchor computes the offset from vanilla
  (centerX,minY,centerZ) to the LOCAL-DOWN AABB-face center;
- TerrainParticleRenderGravityMixin applies this offset only while rendering
  TerrainParticle quads;
- +Y offset is exactly zero;
- particle physics, AABB, lifetime and destroy radial velocity are unchanged.

Runtime acceptance pending.


## 2026-10-07 TerrainParticle render-anchor experiment REJECTED; move owner identified
Manual acceptance after the TerrainParticle local-DOWN render-anchor adapter:
- user reported no visible change;
- surface crawl remained.

The render-anchor files/mixin were removed completely.

Re-reading the complete vanilla Particle.move path identified a deeper owning
asymmetry that all earlier post-processing attempts left intact:

Entity.collideBoundingBox -> collideWithShapes resolves:
1. WORLD Y first;
2. WORLD Z/X in horizontal order.

Particle.move then:
- derives stoppedByCollision only from WORLD Y;
- derives onGround only from negative WORLD Y clipping;
- zeros xd/zd only for WORLD X/Z clipping.

For +Y gravity that entire method is internally self-consistent. On +/-X and
+/-Z, local vertical is one of vanilla's "horizontal" world axes, so fixing flags
after the call still leaves the actual collision path resolved in the wrong
semantic axis order.

New runtime design:
TerrainParticleMoveGravityMixin targets Particle.move but activates ONLY when:
- the concrete particle is TerrainParticle;
- a Planet frame exists;
- the face is not POS_Y.

It mirrors vanilla Particle.move, preserving:
- stoppedByCollision early return;
- physical AABB storage;
- hasPhysics;
- the private vanilla hasNearBlocks optimization (exposed via accessor invoker);
- the 100^2 maximum collision movement gate;
- vanilla setLocationFromBoundingbox world position convention.

The one changed boundary is collision semantics:
- block collision shapes are collected from the same expanded physical AABB;
- PlanetEntityCollision.collideWithShapes resolves them in local
  Y -> local X/Z order;
- PlanetParticleCollisionResponse applies vanilla stopped/onGround/XZ clipping
  semantics to local axes and transforms corrected velocity back to world XYZ.

No render offset remains. No generic Particle.move adapter remains.
+Y is pure vanilla.

Runtime acceptance pending.


## 2026-10-07 TerrainParticle move mixin startup crash
First runtime attempt of the local TerrainParticle move adapter failed during
Mixin application before the game initialized.

Observed error:
    InvalidAccessorException:
    No candidates were found matching hasNearBlocks(DDD)Z
    in net.minecraft.client.particle.Particle

Cause:
ParticleGravityAccessor used @Invoker("hasNearBlocks") for a private vanilla
Particle helper. The external 1.21.1 source name is not a safe runtime contract
through the NeoForge dev transformation/mapping pipeline.

Correction:
- removed the @Invoker entirely;
- copied the exact vanilla hasNearBlocks pre-check logic into
  TerrainParticleMoveGravityMixin;
- copied logic still checks bbWidth/bbHeight, current block and the projected
  movement-edge block before invoking the collision solver;
- added one lazily allocated MutableBlockPos per particle mixin instance to
  avoid allocating block positions every tick;
- collision semantics themselves are unchanged from the previous experimental
  local TerrainParticle move design.

Runtime acceptance remains pending; startup must pass before visual behavior can
be evaluated.


## 2026-10-07 TerrainParticle local move MANUAL PASS
User explicitly confirmed the final TerrainParticle move design fixed the
rotated-face surface crawl.

Accepted behavior:
- destroy radial burst remains correct;
- particles fall in local gravity;
- +/-X, +/-Z and -Y no longer visibly crawl/spread along the local floor before
  disappearing relative to the +Y baseline;
- +Y remains vanilla.

Accepted architectural boundary:
- TerrainParticleMoveGravityMixin only on rotated Planet faces;
- full vanilla Particle.move semantics are rotated as one coherent unit:
  collision order + collision response;
- PlanetEntityCollision.collideWithShapes performs local Y first, then local
  X/Z;
- PlanetParticleCollisionResponse applies vanilla stopped/onGround/tangent
  velocity rules in local axes;
- vanilla physical AABB storage is retained;
- the private hasNearBlocks optimization is copied locally, not invoked via
  @Invoker.

Rejected approaches remain rejected:
- post-processing onGround/stoppedByCollision after vanilla world-axis solve;
- reconstructing actual movement from newPos-oldPos;
- TerrainParticle render-anchor shifting.

This closes the specific TerrainParticle destroy-crawl regression. Continue
Phase 4 with the remaining direct/custom-tick particle audit; do not reopen this
item unless a new regression appears.


## 2026-10-07 Cherry leaf particle complete local-frame pass
After TerrainParticle local move was manually accepted, the next Phase 4
non-fluid custom particle audit focused on CHERRY_LEAVES.

Exact vanilla path:
CherryLeavesBlock.animateTick:
- calls super.animateTick;
- 1/10 chance;
- pos.below();
- get collision shape of that block;
- Block.isFaceFull(shape, Direction.UP);
- ParticleUtils.spawnParticleBelow.

ParticleUtils.spawnParticleBelow:
- random X in block;
- y = blockY - 0.05;
- random Z in block;
- zero initial velocity.

CherryParticle.tick:
- wind curve adds only to world xd/zd;
- subtracts gravity from world yd;
- move(xd,yd,zd);
- removes on onGround OR, after first tick, when xd==0 or zd==0;
- scalar friction.

Adaptation:
- CherryLeavesParticleGravityMixin injects immediately AFTER
  LeavesBlock.animateTick, so vanilla super behavior remains intact;
- on rotated faces it cancels only CherryLeavesBlock's own world-Y branch;
- the same 1/10 random check is consumed once;
- local DOWN physical neighbor replaces pos.below();
- collision face toward the leaves replaces world Direction.UP;
- the same two nextDouble calls are consumed X-sample then Z-sample;
- PlanetParticleEmitter.belowBlock reinterprets vanilla relative
  (sampleX-0.5, -0.55, sampleZ-0.5) in the local frame.

CherryParticleGravityMixin owns the full rotated CherryParticle.tick:
- POS_Y stays vanilla;
- local tangent wind is computed from the exact vanilla curve;
- gravity magnitude is unchanged and applied along local DOWN;
- roll/spin/lifetime/friction remain vanilla;
- LocalGravityParticleMoveMixin now allowlists both TerrainParticle and
  CherryParticle so collision ordering/response is local as one unit;
- blocked-axis removal tests local X/Z after move.

CherryParticle was removed from DirectGravityParticleMixin so gravity is not
double-adapted.

Fluid-coupled custom particles are intentionally deferred to Phase 5.

Runtime acceptance pending.


## 2026-10-07 particle workflow changed to subsystem batch
User requested that particle work stop being delivered/tested one class at a
time.

Decision:
- ALL particle work is consolidated under Phase 4;
- no more manual class-by-class acceptance requests while Phase 4 is under
  implementation;
- perform a full vanilla 1.21.1 particle registry/source audit;
- classify particles by shared mechanisms;
- implement shared adapters + dedicated custom-tick adapters as needed;
- run one final manual particle acceptance matrix.

Fluid-coupled particles are still audited/implemented on the particle side in
Phase 4. Any behavior that fundamentally requires unfinished fluid
topology/flow remains an explicit integration gate for Phase 5 rather than being
faked temporarily.

AGENTS.md now contains a general subsystem-level batch acceptance rule so this
workflow applies in future chats as well.

The previously requested cherry-only manual acceptance is superseded: do not ask
the user to test cherry particles now. Continue Phase 4 implementation first.


## 2026-10-07 global mechanism-oriented roadmap audit
User generalized the particle batch lesson to the whole Planetary project:
do not implement/test gravity support as a sequence of named blocks (door,
ladder, rail, etc.). Research the underlying vanilla mechanism families first,
then implement and accept them as coherent subsystem batches.

A broad Minecraft 1.21.1 source audit was performed.

Audit scope:
- source tree inventory: 5,171 Java files under net/minecraft;
- broad searches for above/below, Direction.UP/DOWN, Plane.HORIZONTAL,
  Axis.Y, placement direction helpers, onGround/fallDistance, raw Y motion,
  heightmaps, fluid height/flow and support/signal patterns;
- detailed representative source inspection across placement, support/update,
  BlockState properties, rails, connection graphs, growth, redstone, pistons,
  entity physics, vehicles, fluids, rendering, portals, spawning/weather and
  worldgen.

New authoritative document:
    docs/research/GRAVITY_MECHANISM_MAP_1_21_1.md

Core decision:
ROADMAP PHASES OWN ENGINE MECHANISMS, NOT WHOLE NAMED OBJECTS.

A single object can be split across phases.

Canonical rail example:
- BaseRailBlock support/placement + RailState graph -> Phase 2F;
- powered/detector/activator signal -> Phase 7A.3;
- AbstractMinecart rail movement -> Phase 7.4;
- rail rendering -> Phase 3.

Canonical piston example:
- FACING placement -> Phase 2A;
- power/quasi-connectivity + push graph + moving block automation -> Phase 7A.4;
- entity push integration -> Phase 7.5;
- moving render -> Phase 3D.

Important source conclusion:
there is no safe global "make Direction local" fix for blocks.
Generic callbacks such as updateShape receive Direction, but many implementations
then call raw BlockPos.relative(direction), above()/below() or
Direction.Plane.HORIZONTAL. Physical primitives must remain physical. Shared
adapters must live at stable engine/base-family/algorithm-family boundaries.

Roadmap changes:
- Phase 2 is now the block semantic subsystem with subphases for placement,
  support/update, multiblock pairs, tangent graphs, runtime growth, rail block
  topology, falling/support-trigger behavior and waterlogged hooks;
- Phase 3 is block geometry/render with shape/static/BER/moving-render subphases;
- Phase 4 remains full particle subsystem batch;
- Phase 5 remains coherent fluids;
- Phase 6 is navigation/AI batch;
- Phase 7 is entity collision/locomotion/non-living/projectiles/vehicles/
  attachments+forces;
- Phase 7A is signal graph, redstone, rail signals, pistons and sided logistics;
- Phase 7B is interaction/raycast, networking, spawn and environment policy;
- Phase 9 now explicitly separates generated structures from runtime rigid
  topology/portals;
- Phase 10 is a continuous compatibility gate, not an end-only cleanup phase.

AGENTS.md now makes this mechanism-first classification mandatory before any
class-specific runtime patch and requires checking base families + siblings.

Batch acceptance is global project policy:
finish a coherent mechanism family/subsystem and ask for one representative
runtime acceptance matrix, rather than making the user test every vanilla class
one by one.

No runtime behavior was changed by this roadmap/research reorganization.
Current runtime acceptance states remain unchanged.


## 2026-10-07 cross-version portability requirement
User added a global architectural constraint: Planetary must be designed so
porting from Minecraft 1.21.1 / NeoForge 21.1.215 to newer Minecraft versions
does not require rewriting a large fraction of the mod.

New canonical strategy:
    docs/research/PORTABILITY_STRATEGY.md

Mandatory design direction:
    stable Planet semantic/core logic
        <- Minecraft/NeoForge adaptation layer
            <- thin Mixins/hooks/entrypoints

Implications:
- frame/topology/graph/shape/entity semantic algorithms should live in
  Planet-owned helpers/services rather than mixin bodies;
- mixins should mainly capture exact vanilla context and adapt args/results;
- prefer stable public/protected or platform extension points over private
  accessors, locals and ordinals;
- avoid copying large vanilla methods;
- unavoidable mirrored vanilla fragments are explicit version-sensitive
  hotspots with vanilla-equivalence tests;
- preserve capabilities/codecs/model/worldgen extension points;
- keep pure semantic tests as version-independent as practical;
- any Planet-owned persistent format must be versioned/migratable.

AGENTS.md and IMPLEMENTATION_PLAN.md now make portability a hard gate.
GRAVITY_MECHANISM_MAP_1_21_1.md explicitly treats mechanism ownership as the
future porting boundary.

No runtime code changed in this documentation/process update.


## 2026-10-07 Phase 4 batch checkpoint: base/custom particle engine
Completed one bounded Phase-4 implementation batch after changing the workflow
to avoid very long monolithic tool runs.

Implemented/shared boundaries:
- LocalGravityParticleMoveMixin generalized from Terrain/Cherry allowlist to the
  BASE Particle.move engine boundary; custom move overrides bypass it naturally.
- PlanetParticleMoveRuntime extracted from the mixin behind
  PlanetParticleMoveAccess for better cross-version portability.
- base Particle.tick local gravity + blocked-vertical speedup + ground tangent
  friction are handled as one shared engine boundary.
- SemanticTickDeltaParticleMixin replaces the older direct-gravity-only patch
  for additive custom-tick deltas.
- constructor helpers cover fixed vertical terms, generated local launch,
  component scaling and base-bias recovery.
- BaseAshSmoke family, WhiteAsh, Spell, TrialSpawnerDetection, DustPlume,
  CampfireSmoke, WaterDrop/Splash, WaterCurrentDown, Snowflake, SquidInk,
  DragonBreath now have the audited local-axis adapters needed outside the
  shared engine.
- PortalParticle and FlyTowardsPositionParticle keep their physical start/vector
  interpolation but rotate the explicit world-Y trajectory arc into local Y.
- PlanetRenderTransform now exposes a local->world quaternion; ShriekParticle
  composes its two vanilla local quad rotations with that frame.
- Campfire particle emitters are local-frame adapted.

Important classification decisions:
- ParticleUtils is NOT a global local-frame boundary. The same helpers represent
  physical block axes, semantic below, and body-ground effects in different
  callers. Adapt callers at their owning mechanism.
- GlowSquid ink source velocity is authored from Squid body orientation and is
  therefore a Phase-7 entity/body integration row, not a particle-local fix.
- Firework Spark gravity is covered by the shared particle engine. Star/burst
  orientation is authored by Firework Starter/rocket and is deferred to the
  Phase-7 rocket/entity frame decision.
- weather particle source geometry is a Phase-7B environment-policy gate.
- fluid surface/membership parts remain Phase-5 integration gates.

No gameplay acceptance requested at this checkpoint. Phase 4 remains batch
acceptance. Next bounded batch: remaining non-fluid emitter/source sweep
(block/event/entity sources), then documentation cleanup and one build/startup
checkpoint before the final gameplay matrix.


## 2026-10-07 global audit workflow refined into bounded research batches
User clarified that the whole gravity/mechanism research should be finished
first, but not as one enormous all-day pass and not as tiny per-class work.

Decision:
- temporarily freeze further runtime implementation;
- finish the global architecture audit in six substantial research batches;
- do not interleave implementation while a research batch is still discovering
  mechanism ownership;
- after R1-R6, run one final completeness sweep and only then resume runtime
  implementation from the dependency-ordered roadmap.

Canonical plan:
    docs/research/GLOBAL_RESEARCH_BATCH_PLAN_1_21_1.md

Batches:
- R1 block/world semantic topology;
- R2 client geometry/render/particles;
- R3 fluids;
- R4 entity/body/interaction/network;
- R5 AI/navigation + signals/automation/logistics;
- R6 environment/worldgen/structures/compatibility;
- final completeness sweep.

Current partial particle runtime work is preserved as the freeze point and is
not considered manually accepted merely because it is implemented.

AGENTS.md and IMPLEMENTATION_PLAN.md now enforce the research/implementation
separation.


## 2026-10-07 R1 block/world semantic topology research COMPLETE
Global research batch R1 is complete. No runtime code was changed during R1.

New canonical Phase-2 family matrix:
    docs/research/BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md

Source audit covered:
- placement/context families;
- Directional/HorizontalDirectional/RotatedPillar;
- FaceAttached + standing/wall + hanging support;
- physical NeighborUpdater boundary;
- DoublePlant/door/bed/chest pair topology;
- CrossCollision/Wall/Multiface/Vine graphs;
- Bush/GrowingPlant/cactus/sugar-cane/bamboo/scaffolding growth;
- BaseRailBlock/RailState;
- FallingBlock/Brushable/support-trigger behavior;
- SimpleWaterloggedBlock hooks.

Important accepted research conclusions:
1. NeighborUpdater six-neighbor fan-out stays PHYSICAL.
2. A canonical local Direction cannot be passed directly to
   BlockPos.relative; semantic direction and physical step are separate.
3. HorizontalDirectionalBlock is only shared orientation vocabulary; behavior
   must be grouped by real algorithm families.
4. FaceAttachedHorizontalDirectionalBlock, DoublePlantBlock,
   GrowingPlantBlock and selected other roots are strong family boundaries.
5. WallBlock is a distinct tangent + local-UP-post algorithm.
6. MultifaceBlock is an all-six-face attachment graph, distinct from
   four-tangent connection graphs.
7. Scaffolding is a local support + tangent distance-propagation graph.
8. RailState is a dedicated graph engine. In addition to above/below and
   horizontal assumptions, hasConnection compares physical X/Z and ignores Y;
   this is invalid on rotated faces. Phase 2F therefore needs a Planet-owned
   local/traversal rail graph, not a sequence of RailBlock patches.
9. SimpleWaterloggedBlock default operations are mostly same-position scheduling
   and remain reachable; fluid topology itself stays Phase 5.
10. Vanilla build-height is a physical world constraint. Compute the semantic
    target first, then validate the resulting physical position.

Phase-2 deterministic and single batch runtime acceptance matrices are now
defined.

Global research advances to R2:
client geometry/render/particles.

Existing runtime remains frozen at the pre-research partial Phase-4 checkpoint.


## 2026-10-07 R2 client geometry/render/particle research COMPLETE
Global research batch R2 is complete. No runtime code was changed during R2.

New canonical matrix:
    docs/research/CLIENT_RENDER_PARTICLE_MATRIX_1_21_1.md

R2 source coverage:
- BlockState shape/cache boundaries;
- Block.shouldRenderFace;
- BlockRenderDispatcher / ModelBlockRenderer;
- AO/flat-light/shade/breaking overlay;
- standard OffsetType XZ/XYZ;
- all BlockEntityRenderer classes inventoried with representative transform
  call flows;
- moving block/entity renderer ownership;
- complete existing ParticleEngine/class audit reconciled with emitter-source
  ownership;
- block animateTick / ParticleUtils / LevelRenderer event and weather source
  families;
- accelerated/custom renderer compatibility boundary.

Important research conclusions:
1. Physical outer VoxelShape queries and canonical support/occlusion state are
   separate layers.
2. Correct PHYSICAL BakedQuad.direction already drives vanilla AO/light
   neighbor sampling; rotating AO again would be a double transform.
3. Breaking overlay uses the same ModelBlockRenderer tesselation path.
4. ClientLevel directional shade is global world-direction lighting policy and
   is deferred to R6 environment policy.
5. Standard vanilla OffsetType should use canonical-local coordinates/seed and
   then transform the offset physically. Arbitrary custom OffsetFunction is not
   globally assumed local.
6. A global BlockEntityRenderDispatcher frame transform is unsafe.
   BER families are:
   - rigid block-local;
   - runtime-direction;
   - world/camera-space.
7. Falling/moving block rendering is distinct from vehicle/entity body
   rendering. Minecart/boat/TNT/item-frame orientation belongs to Phase 7.
8. Particle motion and emitter/source geometry are independent ownership axes.
9. ParticleUtils cannot be globally reframed; caller semantics decide.
10. Entity/fluid/weather/portal emitters are explicit integration gates of
    Phases 7/5/7B/9 respectively.
11. Custom accelerated renderers bypassing ModelBlockRenderer require explicit
    compatibility integration, not raw VertexConsumer interception.

The Phase-3 and Phase-4 deterministic and single runtime acceptance matrices are
now defined.

Global research advances to R3: fluids.
Existing runtime remains frozen.


## 2026-10-07 R3 fluid research COMPLETE
Global research batch R3 is complete. No runtime code was changed during R3.

New canonical matrix:
    docs/research/FLUID_MECHANISM_MATRIX_1_21_1.md

Exact target sources included vanilla 1.21.1 and NeoForge branch 1.21.1.

Important findings:
1. FlowingFluid is one topology graph. PlanetBlockStep already supplies the
   required seam-aware edge foundation.
2. Semantic local direction and physical edge direction must be separate.
   Fluid subclass logic such as Direction.DOWN uses semantic local direction;
   wall collision uses physical direction.
3. FlowingFluid.getCacheKey uses only physical delta X/Z and is invalid for
   rotated/seam slope traversal.
4. FlowingFluid OCCLUSION_CACHE state-pair+Direction key is not frame-complete
   once collision shapes are position/frame dependent.
5. NeoForge source creation must preserve CreateFluidSourceEvent and
   FluidType.canConvertToSource.
6. NeoForge BaseFlowingFluid inherits FlowingFluid and is the primary mod-fluid
   compatibility win if the shared graph boundary is adapted correctly.
7. FluidInteractionRegistry has its own world-axis enumeration and built-in
   basalt currentPos.below() assumption; registry integration belongs to Phase 5.
8. Bucket click direction is physical interaction geometry and should remain
   physical.
9. SimpleWaterloggedBlock/LiquidBlockContainer are mostly same-position hooks.
10. BubbleColumnBlock is a local-UP/local-DOWN fluid topology family; entity
    velocity impulse is Phase-7 integration.
11. Pointed-dripstone chain topology remains Phase 2, while fluid-above/root and
    cauldron drip source lookup integrate in Phase 5.
12. FluidState.getFlow should remain a PHYSICAL world vector.
13. NeoForge entity fluid-contact measurement is still world-Y despite FluidType
    generalization; local immersion/body response belongs to Phase 7.
14. LiquidBlockRenderer is a standalone world-Y/XZ mesh generator, so Phase 5C
    needs a dedicated local fluid mesh algorithm.
15. Preserve NeoForge fluid sprites/tint/overlay/hide-adjacent hooks; custom
    extension semantics must be re-audited per newer NeoForge port.

Global research advances to R4: entity/body/interaction/network.
Runtime remains frozen.


## 2026-10-08 R4 entity/body/interaction/network research COMPLETE
Global research batch R4 is complete. No runtime code was changed during R4.

New canonical matrix:
    docs/research/ENTITY_BODY_NETWORK_MATRIX_1_21_1.md

Exact source audit covered vanilla Minecraft 1.21.1 and NeoForge 1.21.1:
- Entity movement/collision/support/fall/body/view;
- LivingEntity locomotion;
- representative non-living entities;
- projectile families;
- minecart and boat;
- passengers/attachments/dismount/leash/sleeping/hanging entities;
- knockback/explosion/piston/external forces;
- GameRenderer/Item/ProjectileUtil picking;
- LocalPlayer/ServerPlayer/client packet listener/server packet listener;
- movement/vehicle/teleport packet semantics;
- NeoForge entity/living/projectile/vehicle/interaction hooks.

Critical architecture correction:
The current partial runtime converts Entity.deltaMovement into LOCAL coordinates
inside a Planet gravity field. R4 rejects that as the long-term architecture.

Vanilla/NeoForge contracts demonstrate that deltaMovement is PHYSICAL world XYZ:
- Explosion adds a physical radial world vector directly;
- Leashable adds holder-position minus entity-position directly;
- Entity.push directly adds supplied physical components;
- ProjectileUtil uses deltaMovement as the physical collision ray;
- ClientPacketListener relative teleport preserves deltaMovement components by
  physical RelativeMovement.X/Y/Z flags;
- server player/vehicle validation compares physical packet displacement to
  deltaMovement magnitude;
- third-party mods naturally rely on the same API contract.

Required future Phase-7 migration:
    physical deltaMovement
        -> project into body-local frame for semantic locomotion
        -> run local Y/tangent algorithm
        -> recompose physical result
        -> store physical deltaMovement

Entity.move displacement must likewise remain physical.

This correction does NOT reject or roll back manually accepted player behavior.
Accepted movement/camera/jump/fall behavior remains the product target and
evidence for semantic correctness. Only the internal velocity storage boundary
is marked for replacement before broad Phase-7 expansion.

Other R4 conclusions:
1. Existing PlanetEntityCollision is retained: physical AABB/colliders, local
   collision order, physical displacement result.
2. onGround/vertical/tangent/step/fall are body-local semantics.
3. EntityDimensions remain canonical local dimensions; physical AABB is derived
   from the body frame. Preserve NeoForge EntityEvent.Size.
4. Eye/view is local-authored and converted once to a physical world ray.
5. BlockHitResult remains physical through client/server networking.
6. Packet and teleport XYZ remain physical.
7. Server jump/floating/fall/vehicle validation must project physical deltas
   into the entity body frame rather than reading world Y.
8. Yaw/pitch may remain body-local scalar vocabulary only if client/server share
   the same gravity frame.
9. Current gravity-face selection uses preferred-face hysteresis and prediction
   state. An explicit authoritative face synchronization path is the safer
   long-term design unless deterministic equivalence is proven.
10. Projectiles separate launch/body semantics from physical collision/raycast.
11. Minecart consumes Phase-2 rail topology; boat consumes Phase-5 fluid
    surfaces.
12. Passenger/seat/leash/sleep authored offsets are body-local; final endpoints
    are physical.
13. Dismount requires a dedicated local-floor/clearance search.
14. Explosion/leash/source-target attraction are physical forces and must not
    be rotated merely because gravity differs.
15. Living knockback is a mixed boundary and needs explicit caller semantics
    while preserving NeoForge LivingKnockBackEvent.
16. Preserve NeoForge entity tick/size/mount/fall/fluid/projectile/minecart/boat
    and interaction extension points.

Global research advances to R5:
AI/navigation + signals/automation/logistics.

Runtime remains frozen.


## 2026-10-08 R5 AI/navigation + signals/automation/logistics research COMPLETE
Global research batch R5 is complete. No runtime code was changed during R5.

New canonical matrix:
    docs/research/AI_AUTOMATION_MATRIX_1_21_1.md

Important findings:
1. Vanilla Node and NodeEvaluator cache navigation state by physical xyz only.
   A Planet ground-navigation node must additionally carry traversal chart/face
   or seam states alias.
2. Vanilla BinaryHeap/PathFinder can likely remain; Planet should own the
   chart-aware node/cache/neighbor graph and adapt Path metadata.
3. Current PlanetWalkNodeEvaluator edge overshoot and MoveControl ordinal
   redirects are explicitly experimental and rejected as the long-term
   architecture.
4. WalkNodeEvaluator floor/step/drop/body-volume/diagonal behavior is one local
   ground-graph family.
5. NeoForge BlockState/FluidState path type hooks must be preserved on the real
   physical candidate cells.
6. Steering uses physical target delta projected to the entity body frame.
7. RandomPos and MoveToBlockGoal are shared local target-generation families;
   most chase/flee/melee goals should inherit common navigation rather than get
   class patches.
8. SignalGetter physical six-neighbor enumeration remains physical. Its
   Direction-sensitive source-state queries are reframed into the queried
   source state's canonical block frame.
9. Redstone wire, diode/comparator, tripwire, powered rail and piston are
   distinct graph families.
10. Powered rail must consume the Phase-2 Planet rail graph; DetectorRail search
    volume needs canonical-local AABB -> physical AABB.
11. PistonStructureResolver's constant world Direction model fails at seams.
    Phase 7A needs a PlanetPistonPushGraph with transported push direction.
12. Moving oriented blocks across a gravity seam creates a new shared
    PlanetBlockStateTransport requirement; do not hide property rewrites in the
    piston mixin.
13. Hopper/dropper/crafter target selection happens BEFORE vanilla/NeoForge
    sided container/capability dispatch. Capability-side reframing alone cannot
    repair a wrong target BlockPos.
14. Sided logistics therefore has two stages: topology-aware physical target,
    then physical shared face -> target canonical side.
15. Default dispenser item launch contains a separate vanilla world-Y upward
    bias; it must be authored as local UP while preserving RNG order.
16. Crafter/Jigsaw use FrontAndTop ORIENTATION. R5 adds this concrete canonical
    two-direction vocabulary to Phase 2A.
17. Dispenser/dropper have an extra local-UP power probe; door power over both
    halves is instead a Phase-2 pair-topology signal query.
18. Preserve NeoForge path-type, redstone connection, piston event/stickiness,
    VanillaInventoryCodeHooks and capability extension points.

R1 block matrix and capability research were amended with the newly discovered
FrontAndTop and upstream-logistics findings.

Global research advances to R6:
environment/worldgen/structures/compatibility.

Runtime remains frozen.


## 2026-10-08 R6 environment/worldgen/structures/compatibility research COMPLETE

R6 is a DOCS-ONLY complete research batch; runtime remains frozen.
Canonical source/call-flow/matrix:
    docs/research/ENVIRONMENT_WORLDGEN_STRUCTURE_COMPAT_MATRIX_1_21_1.md

Exact source inspection (1.21.1 Mojang-named baseline) verified:
1. NaturalSpawner candidate selection samples physical XZ + global-Y heightmap.
   SpawnPlacementTypes.ON_GROUND uses below/above separately. Phase 7B.3
   therefore needs PlanetSpawnCandidateProvider, not just ground-rule patches.
2. Heightmap is 16x16 physical XZ->Y; it is not a six-face surface.
   Introduce explicit PlanetSurfaceQuery/Index while leaving global Heightmap.
3. ServerLevel precipitation + lightning, worldgen SnowAndFreezeFeature and
   LevelRenderer weather visuals are independent XZ/Y code paths; shared
   PlanetClimateQuery/PrecipitationQuery policy, separate adapters.
4. Biome height-adjusted temperature directly tests physical Y > 80 and uses
   physical X/Z noise/cached position. Cannot treat equal-shell opposite faces
   as equal temperature without a Planet climate boundary.
5. SkyLightEngine and SkyLightSectionStorage inject/read physical Y columns.
   Six-face skylight is a distinct engine integration, not a camera rotation.
6. PlanetChunkGenerator today only fills a fixed R48 test cube; surface,
   carvers, decoration and original mobs are intentionally disabled.
   It does not yet use PlanetGenerationSpace for noise fill.
7. Default ChunkGenerator.createBiomes still samples physical quart XYZ even
   when an external BiomeSource is stored in PlanetChunkGenerator.CODEC.
   Seam-coherent sampling requires generation-space climate/biome resolver.
8. Continuous scalar fields and discrete block structures/features differ:
   arbitrary integer world-Y modded placement cannot be made seam-safe by
   unqualified wrap-and-round. Classify FIELD_WRAP, LOCAL_CHART,
   RIGID_AVOID_EDGE, INTEGRATION_REQUIRED.
9. PlanetGenerationSpace is continuous in value but its maxAbs shell
   definition does not prove continuous gradient at tie planes.
10. getSeaLevel is a global-Y integer API. Constant Planet SEA_SHELL needs
    its own radial interface and explicit aquifer/surface adapters.
11. StructureStart final bounding box must be validated before registration
    and cross-chunk refs/placement. JigsawPlacement and GravityProcessor use
    getHeight/global Y. Preserve structure seeds, biome filters and locate.
12. Nether PortalShape is a bounded local-vertical rigid rectangle algorithm,
    normally forbidden to span a gravity seam. Entity transition is separate.
13. BiomeModifier/PlacedFeature/StructureSet and mod biome registries remain
    vanilla/NeoForge-owned. TerraBlender, BOP and BYG are future acceptance
    cases, NOT verified working yet.
14. Worldgen settings need codecs/data versions/world fingerprints; vanilla
    Y build limits cannot represent infinite exterior ±Y radial surfaces
    without a much larger storage-world redesign.

Documents updated: R6 matrix, GRAVITY_MECHANISM_MAP_1_21_1.md,
IMPLEMENTATION_PLAN.md, GLOBAL_RESEARCH_BATCH_PLAN_1_21_1.md,
PORTABILITY_STRATEGY.md, this AI_CONTEXT.md.

Global batches: R1 ✅ R2 ✅ R3 ✅ R4 ✅ R5 ✅ R6 ✅.
NEXT: final cross-batch COMPLETENESS SWEEP, architecture freeze,
dependency-ordered implementation plan. Do not start runtime before sweep.
No build or gameplay acceptance required for this documentation-only change.


## 2026-10-08 Final global research sweep plus external mods COMPLETE

Completed final cross-batch R1-R6 architecture reconciliation, research only.
New canonical docs:
- docs/research/EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md
- docs/research/FINAL_COMPLETENESS_SWEEP_1_21_1.md

External source/experience:
- FugLord77 GravityChanger 1.21 API exposes physical getWorldVelocity /
  setWorldVelocity over local entity storage, and constructor-early guard.
- iPortalTeam Immersive Portals explicitly integrates eye offsets, velocity,
  local/world directions, quaternion camera frames and fallback when Gravity
  Changer is absent; virtual world-wrap does not simulate redstone/fluid/AI
  across portal boundary. Camera/client/server crossing can be asynchronous.
- VS2 raycasts world->ship->world, transforms particle positions separately
  from direction/velocity and documents save bugs with passengers, entities
  in shipyard coords; source issues report unloaded teleports and oversized
  / invalid AABB performance. Take lessons, NOT code.
- Cubic Chunks has genuine CubePos/3D provider and separate column/heightmap
  APIs; unlimited physical Y cannot be supported merely by generator settings.
- Old Up And Down/Starminer are coverage/product analogs only.

Critically, R4 already REJECTED current local deltaMovement as future design.
Future entity frame contract = PHYSICAL world deltaMovement, but no runtime
migration occurred yet. This is a missing implementation gate, not new PASS.

R1-R6 cross-phase ownership is now frozen for first implementation planning.
Major explicitly OWNED unsolved engineering gates: skylight six faces, finite
world height/storage limits, format/seed worldgen migration, chart-aware
corner graph semantics, NeoForge version-specific hooks and saved/passenger
teleport/AABB guard invariants. Subsequent implementation batches follow
docs/IMPLEMENTATION_PLAN.md and FINAL_COMPLETENESS_SWEEP_1_21_1.md.

NO runtime changes, code build, Minecraft client launch or gameplay acceptance
during external research/final sweep. Next action is Batch 0 baseline/gates,
then Phase-1 foundation and dependency-ordered coherent subsystem work.


## 2026-10-08 Ultimate third-party compatibility review COMPLETE

Docs-only research: docs/research/ULTIMATE_CROSS_MOD_COMPATIBILITY_REVIEW_1_21_1.md.
Compared Planet 2.0 code to NeoForge 1.21.1 capability docs and upstream
Create 1.21.1 kinetic + virtual contraption, AE2 grid/API, Mekanism side
port/network lifecycle, WorldEdit clipboard, TerraBlender and Distant
Horizons API concepts. Exact third-party version runtime NOT tested.

NEW P0 CONTRACT RISK: current BlockCapabilityMixin maps EVERY physical
Direction query to target canonical LOCAL side. Previous stone diagnostics
intentionally authored LOCAL-aware providers; they do NOT prove all real
modded providers are safe. Mekanism indexes its own physical side ports
by Direction.ordinal; AE2 exposes in-world Direction keyed connections.
Target semantics: unknown external provider PHYSICAL by default; LOCAL
only declared by receiver provider/Block/BE capability policy. This is
research-confirmed INTEROPERABILITY RISK, not reproduced live bug, and
accepted runtime must be preserved while migrating with explicit tests.

NEW P0 FOUNDATION: distinguish actual physical Level/BlockPos from Create
virtual ContraptionWorld, WorldEdit clipboards, AE2 virtual nodes,
generation samples, render frame. Connection geometry must carry source
and target physical/local ports plus signed axial/handedness information;
Create must still own its kinetic network, AE2 its grid, Mekanism its
transmitter network. BlockState orientation transport is not enough for
BlockEntity persisted side config, NBT and attach/detach/load/unload.

Implementation Plan updated with compatibility gate, and stale research
status corrected: R1-R6 + global final + bounded cross-mod review COMPLETE.
Runtime untouched, no compile or gameplay testing in this audit.
Next: Batch 0 baseline then P0 provider contract + Phase-1 context kernel
in one coherent implementation batch; no broad compatibility claim yet.


## 2026-10-08 P0 capability-provider context implementation — acceptance PENDING

Implementation on 2.0, not yet built or manually accepted:
- BlockCapabilityMixin no longer blindly changes Direction for any
  NeoForge BlockCapability on a physical PlanetGravityRuntime-bound Level.
  It retains the separate guarded legacy virtual-atlas alias branch.
- Public PlanetCapabilityAdapters.canonicalLocalBlock and
  canonicalLocalBlockEntity wrap individual providers at registration.
  Unknown/unwrapped mod providers continue to receive PHYSICAL Direction.
  Null, non-Planet world, unattached BE and cache contexts remain unchanged.
- PlanetCapabilityDiagnostics now wraps its intentionally canonical-LOCAL
  stone echo and item/fluid/energy probe providers, and adds an independent
  PHYSICAL_SIDE_ECHO on the SAME STONE block. Both are checked for every
  gravity face/direction; prior cache invalidation acceptance path retained.
- PlanetCoordinateContext explicitly distinguishes physical Level/BlockPos
  from foreign space; PlanetFrameApi's new context overloads reject foreign
  positions unless a future integration resolves them. Other existing Level
  overloads still require callers to pass genuine physical coordinates.
- Added PlanetCapabilityAdaptersTest and PlanetCoordinateContextTest to
  check local/physical coexistence, six faces, corner, null, unattached BE,
  non-Planet fallback and fail-closed virtual coordinates.

Root-cause/source note:
  docs/research/CAPABILITY_PROVIDER_CONTEXT_1_21_1.md
NeoForge 1.21.1 BlockCapability.getCapability enumerates providers per Block
and passes original context; exact RegisterCapabilitiesEvent can wrap a
specific provider. This is safer than the review's initial idea of a global
(capability, Block) semantic policy/registry, which would also touch other
providers for that same pair. The resolved chosen design is PER PROVIDER.

Last accepted runtime before patch: old Planet capability-local diagnostic
(36 sided echo, local-UP standard handlers, cache invalidation) on STONE.
DO NOT mark new behavior PASS until a user build and runtime probes; no
third-party Mekanism, AE2, Create acceptance performed. If normal vanilla
machine IO regresses after physical-default conversion, investigate specific
receiving family/provider; do not restore blanket side transformation.

Needed next: user git pull && .\\test.ps1 && .\\run-client.ps1,
login diagnostic chat and single capability subsystem manual matrix.
No build or client run has been executed by the assistant this turn.


## 2026-10-08 Phase-4 SemanticTickDeltaParticleMixin startup crash

User pasted full PowerShell log from:
git pull && .\test.ps1 && .\run-client.ps1

User LOCAL evidence after pulling a9c683f:
- Gradle 8.12 build/unit test SUCCESS in 23 seconds;
- NeoForge 21.1.215 / Minecraft 1.21.1 / Java 21 startup FAILED;
- ParticleEngine.registerProviders -> MixinTransformerError:
  SemanticTickDeltaParticleMixin.planetary$reinterpretTickDeltaBeforeMove,
  target @At INVOKE Particle.move(DDD)V matched 0/1, scanned 0 targets.
The last known accepted individual particle behavior remains TerrainParticle
local move and CherryLeaves/torch effects. Newer Phase-4 broad batch was
NOT startup accepted. Do not confuse this with the capability-provider
registration added at a9c683f; that code had passed compilation/tests but
could NOT be gameplay accepted because a particle mixin crashes first.

Source diagnosis:
Six Minecraft 1.21.1 custom tick classes DripParticle, WaterDropParticle,
BubblePopParticle, WakeParticle, CampfireSmokeParticle, BubbleParticle each
use this.move(xd,yd,zd). Java compiler emits concrete subclass-owned
INVOKEVIRTUAL target even though move is inherited from Particle. A separate
javac/javap control reproduced subclass-owned Methodref. Old shared
multi-target injector looked for LParticle;move and could not match.
Reference class source: hackersense/OptiFine-Source 1.21.1 (not a
stand-in for exact NeoForge 21.1.215 transformed bytecode).
A new ASM unit test asserts actual target class files agree.

Implemented architecture revision, startup acceptance PENDING:
- remove SemanticTickDeltaParticleMixin multi-target injection;
- add six exact owner-targeted *TickDeltaMixin class bridges, each required
  HEAD capture and required pre-move INVOKE, no require=0 masking;
- PlanetSemanticTickDeltaState shared per-instance tick semantic delta
  capture/physical velocity preservation, lazy only on active rotated
  Planet frame; existing PlanetParticleMotion math unchanged;
- config replaces old mixin with six; bytecode/reg test checks all;
- vanilla RNG, base Particle.move, child tick, emitter, fluids, all Phase-1
  capability provider fixes untouched by this repair.
Detailed matrix: docs/research/PARTICLE_MATRIX_1_21_1.md.

User next intermediate startup gate (per AGENTS.md 7A): execute
git pull && .\test.ps1 && .\run-client.ps1.
Expect build success, client main menu, Planet world login and Phase-1
capability diagnostic (36 local/36 physical/36 standard, cache listener).
If a new failing INVOKE remains, use the exact owner from the ASM test and
crash log to correct the one owning adapter; do not disable required
injections globally. Full Phase-4 visual game acceptance deferred to
subsystem batch; new startup test NOT yet confirmed by user.


## 2026-10-08 Follow-up: NeoForge test-class IllegalClassLoadError (exact root cause)

User ran test.ps1 after prior Phase-4 mixin rewrite. compileTestJava succeeded,
but :test FAILED before any method of ParticleTickInvocationTargetTest ran:

    Could not execute test class 'dev.planetary.mixin.ParticleTickInvocationTargetTest'
    Caused by: IllegalClassLoadError: ... is in a defined mixin package
    dev.planetary.mixin.* owned by planetary.mixins.json and cannot be
    referenced directly

This overrides the previous speculative theory that the bytecode assertion
itself failed: it DID NOT RUN. The cause was putting a JUnit test in the
Mixin-transformer-reserved package. A second bug was exposed by inspection:
PlanetSemanticTickDeltaState, a regular runtime utility instantiated from
injected code, also lived in dev.planetary.mixin and risked the same
IllegalClassLoadError on client initialization.

Fix IMPLEMENTED / build+client acceptance pending:
- moved the regular per-particle state helper to
  src/main/java/dev/planetary/gravity/PlanetSemanticTickDeltaState.java;
- helper now consumes shared PlanetParticleMoveAccess contract instead of
  referencing Mixin's ParticleGravityAccessor directly;
- six per-class tick Mixins import shared gravity helper;
- moved ASM test to src/test/java/dev/planetary/gravity/
  ParticleTickInvocationTargetTest.java;
- deleted BOTH old source files from reserved dev.planetary.mixin package;
- added permanent reserved Mixin package rule to AGENTS.md.
- no gravity math, particle tick callback algorithms, RNG or saved-state
  behavior altered.

User evidence establishes compile success BEFORE this relocation but NOT
a working test suite or game launch after it. No claim of a successful
Mixin application, or of capabilities acceptance. Next:
git pull && .\\test.ps1 && .\\run-client.ps1, check test results first,
then main menu / login. Do not start full particle gameplay acceptance
until startup passes.


## 2026-10-08 Third startup smoke: WaterCurrentDown tick Mixin owner crash

User's third pasted command output after previous package relocation:
runClient starts then fatal MixinTransformerError during
ParticleEngine.registerProviders:114,
WaterCurrentDownParticleGravityMixin.planetary$rotateCurrentSpiral,
target "Lnet/minecraft/client/particle/Particle;move(DDD)V",
"0/1 succeeded. Scanned 0 target(s)." Test step had allowed the
PowerShell && chain to reach runClient; no preceding test failure reported.

This is SAME Phase-4 boundary problem as original multi-target
SemanticTickDeltaParticleMixin, but in an independent remaining hook;
not a capability issue, JVM crash, or a particle math issue.
The actual Java tick() in WaterCurrentDownParticle invokes inherited
this.move via its concrete subclass owner. Source audit finds identical
error in DragonBreathParticleGravityMixin; ShriekParticleRenderGravityMixin
similarly expects superclass SingleQuadParticle renderRotatedQuad owner
despite two subclass-owned this.renderRotatedQuad call sites.

Batch committed on branch 2.0 (startup/test ACCEPTANCE PENDING):
- change only @At targets in WaterCurrentDown and DragonBreath to
  their concrete subclass move owners;
- change Shriek @ModifyArg target from SingleQuadParticle to
  ShriekParticle.renderRotatedQuad owner;
- ASM JUnit guards: eight custom tick move call sites, eight exact
  compiled Mixin @Inject annotation targets, Shriek render's two
  subclass-owned calls, and compiled @ModifyArg annotation;
- focused exact-version source note added:
  docs/research/PARTICLE_MIXIN_INVOKE_ANCHORS_1_21_1.md;
- permanent AGENTS.md rule for inherited-call INVOKEs and classfile
  tests, not a broad rewrite of particle motion.

The legacy +Y / non-Planet pass-through, emitter RNG, player/entity
mechanics, inherited Particle.move runtime and new per-provider
capability policy are unchanged.

User next: git pull && .\\test.ps1 && .\\run-client.ps1.
Report any failing ASM assertion or Mixin's nested InjectionError.
Required first acceptance: test task green, client main menu and
Planet login, then capability diagnostic. Phase-4 gameplay acceptance
remains deferred to the ONE subsystem matrix, not per-class testing.


## 2026-10-08 user screenshot: main client/world startup and probes now run

User posted an in-game screenshot after the Phase-4 inherited Mixin
INVOKE owner audit (HEAD ba80f72d). Direct user-provided runtime
evidence: Minecraft 1.21.1/NeoForge client reached active Planet
world; startup no longer blocked by the previously observed particle
Mixin injection crashes. On-screen probe output:

- Dedicated Planet gravity attached.
- Standing/wall placement probe passed: 6 standing + 24 wall.
- Shape probe passed: 36 physical shapes, 18 canonical support/
  occlusion, 6 full-block identity.
- Support probe passed: 90 survival + 90 updateShape.
- Placement probe passed: 108 vanilla states.
- Frame API probe passed: 36 local-neighbor checks, and 22 raw
  BlockPos.relative mismatches DETECTED.
- Capability probe passed: 36 explicit local mappings, 36 physical-
  side passthrough, 36 standard item/fluid/energy, 1 cache invalidation.
- Dedicated Planet world active; core=(0, 128, 0), diameter=97.

Important: raw BlockPos.relative mismatches are the deliberately
EXPECTED failure of treating a physical Direction as local; code in
PlanetCompatibilityDiagnostics.assertFrameApi (rawMismatches == 0)
throws. Therefore detecting 22 is positive proof that the typed
neighbor frame adaptation matters, not a failed test.

Status: CLIENT STARTUP + PLANET LOGIN + DIAGNOSTIC PROBES OBSERVED
RUNNING/PASSING in user screenshot, clearing the startup-blocker gate.
This does NOT constitute acceptance of the entire Phase-4 particle
visual/motion matrix, actual third-party cross-mod machine compatibility,
edge gameplay or the later worldgen phases. Do not mark those PASS.
We did not receive a verbatim post-fix Gradle test log; screenshot is
direct in-game evidence, not a complete transcript of :test.

Next work: resume the coherent remaining Phase-4 particle emitter/
constructor source inventory and implementation; gameplay acceptance
deferred to one comprehensive subsystem matrix (AGENTS 7A).


## 2026-10-08 Phase-4 block-local animateTick emitter family — code checkpoint

User confirmed we should continue autonomously using roadmap and avoid
tiny class-by-class gameplay tests; latest user screenshot proves client
startup and in-world diagnostics now run, but Phase-4 manual matrix
still NOT accepted.

Detailed vanilla family source inventory:
docs/research/BLOCK_LOCAL_PARTICLE_EMITTERS_1_21_1.md
Looked up 1.21.1 source (comparative OptiFine decompilation) for
Furnace, BlastFurnace, Smoker, BrewingStand, EndRod, RespawnAnchor,
EnderChest, AbstractCandleBlock/CandleBlock/CandleCakeBlock,
EnchantingTable, SporeBlossom, NetherPortal. Seven first classes
each own animateTick and call Level.addParticle (Furnace twice);
their final sampled positions have canonical block-LOCAL meaning.
RespawnAnchor local UP and EnderChest local vertical/tangent momentum
are also local; EndRod Gaussian velocity is isotropic and left
physical. Candle uses getParticleOffsets lambda/static helper and
different extinguish path, so is explicitly NOT in this mixin.

IMPLEMENTED on branch 2.0 (BUILD/STARTUP/GAMEPLAY UNVERIFIED after patch):
- src/main/java/dev/planetary/mixin/
  BlockLocalParticleEmitterGravityMixin.java, 7-class @Mixin,
  one @ModifyArgs at actual Level.addParticle(ParticleOptions,6*double)
  inside each animateTick.
- On rotated Planet frames only: preserve the vanilla random samples,
  adapt origin relative to center with existing
  PlanetParticleEmitter.transformVanillaLocalEmitter;
  rotate local momentum for RespawnAnchor/EnderChest with
  PlanetParticleMotion.localVelocityToWorld.
- +Y/non-Planet is byte-for-byte pass-through through the injected
  callback; emit count, RNG, vanilla addParticle and sound never
  overwritten; no caching/state added. Other sources use original
  Gaussian/zero velocity.
- json client mixin registration added, defaultRequire=1 retained.
- src/test/java/dev/planetary/gravity/
  BlockLocalParticleEmitterInvocationTest.java verifies exact seven
  animateTick class-file invoke owners/call counts (Furnace=2),
  compiled @Mixin targets/@ModifyArgs anchor and JSON entry.
- PlanetBlockLocalEmitterBatchTest.java checks seven representative
  emitted positions and 2 local authored motion vectors for all
  six frames (+Y baseline and invertibility).

Still pending: candles (different lambda/extinguish mechanism),
enchanting table bookshelf local-neighbor graph, spore blossom
distribution-space sampling; fluid/portal/weather/body sources
belong to owning later integration phases. All statuses recorded in
IMPLEMENTATION_PLAN and PARTICLE_MATRIX; no per-source gameplay
requests before coherent Phase-4 batch acceptance.

IMPORTANT: assistant did NOT run Gradle/JUnit or launch game for this
patch. Do NOT label as PASS merely because source exists in GitHub.
Next: continue remaining non-blocked emitter family if no required
baseline failure; eventually user runs
git pull && .\\test.ps1 && .\\run-client.ps1
and whole Phase-4 gameplay matrix.


## 2026-10-08 user screenshot after seven block emitter Mixin (second smoke)

After Phase-4 block-local emitters commit b9e5e64c, user posted a new
in-game Planet diagnostic screenshot with the same complete positive
messages as the earlier baseline: gravity attached, standing/wall
6+24, shape 36+18+6, support 90+90, placement 108, frame API 36
local neighbor checks and 22 EXPECTED raw physical mismatches,
capabilities 36 local / 36 physical / 36 standard, cache invalidation 1.
Planet world active core=(0,128,0), diameter=97. This is direct
evidence that the newly added seven-block emitter mixin does NOT
prevent NeoForge client startup / Planet login / automatic diagnostics.
No visual candle/furnace/other emitter-specific behavior was
manually tested, and no full Gradle log is supplied. Preserve
startup gate as observed passing and entire particle gameplay
matrix as PENDING; continue coherent Phase-4 emitter implementation.


## 2026-10-08 candle light and extinguish emitter package — IMPLEMENTED, PENDING ACCEPTANCE

After the previous seven-emitter Mixin, user provided second screenshot:
dedicated Planet world starts, same standing/wall, shape, support,
placement, local-neighbor and capabilities diagnostics all PASSED.
This proves startup, NOT visible emitter alignment on all faces.

Next bounded Phase-4 implementation follows
docs/research/CANDLE_PARTICLE_EMITTERS_1_21_1.md. Vanilla 1.21.1
AbstractCandleBlock owns BOTH source paths, inherited by CandleBlock
and CandleCakeBlock:

- lit animateTick invokes Iterable.forEach(Consumer) of
  getParticleOffsets(state), lambda adds BlockPos and executes the
  original smoke/chance/ambient sound/flame logic with RandomSource;
- static extinguish invokes another Iterable.forEach(Consumer), its
  lambda emits exactly one smoke per supplied offset with world +Y
  (double)0.1F and vanilla method subsequently plays extinguish
  sound and sends a game event.

IMPLEMENTED in branch 2.0, NO Gradle/client acceptance yet:
- stable PlanetParticleEmitter.rotateUnitBlockEmitterOffset maps
  block-CORNER local offset around center and back into physical
  unit-block coordinates, supports candle-cake local offset y=1;
- single AbstractCandleParticleEmitterGravityMixin on BASE family
  uses @ModifyArg at lit animateTick forEach to wrap the vanilla
  Consumer on rotated Planet frames only; original lambda retains
  RNG/sound/particle execution;
- second @Redirect on extinguish forEach on rotated Level only.
  Emits exact same number/order smoke from rotated offsets, with
  float-accurate 0.1F local-up momentum. Original setLit,
  extinguish sound and block game-event remain VANILLA.
  Non-Level LevelAccessor, +Y, non-Planet delegate to original
  forEach unchanged. No global LevelAccessor interception.
- client Mixin JSON registration added, defaultRequire=1 unchanged.
- PlanetParticleEmitterTest extended to 1–4 candle + cake offsets
  on six frames and float-derived smoke velocity; new
  CandleParticleEmitterInvocationTest reads vanilla
  AbstractCandleBlock ASM classfile to assert the two exact
  java/lang/Iterable.forEach invokes and checks compiled Mixin
  @Mixin/@ModifyArg/@Redirect annotation targets plus JSON registration.

KNOWN RISK: new java/lang/Iterable.forEach @ModifyArg/@Redirect mixin
integration has never been exercised in live client by assistant.
Tests and startup must be checked before PASS. Do not request tiny
per-candle gameplay retests: wait for the full Phase-4 gameplay matrix.
Remaining non-fluid source paths to audit/implement:
EnchantingTable bookshelf-neighbor topology and SporeBlossom
distributed-air candidate cells; more portal/body/fluid/weather
sources are gated by their owning later phase.


## 2026-10-08 candle startup regression: InvalidInjectionException (@ModifyArg handler signature)

User's big-paste crash log from the client launched after 69eb709:
runClient FAILED before Minecraft Bootstrap finished while applying
planetary.mixins.json:AbstractCandleParticleEmitterGravityMixin to
AbstractCandleBlock.

Deepest `Caused by`:
`org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException`:
`@ModifyArg injector planetary$reframeLitCandleOffsets targets a method
with an invalid signature (Ljava/util/function/Consumer;), expected
(Ljava/util/function/Consumer;L.../BlockState;L.../Level;L.../BlockPos;
L.../RandomSource;)`.

Root cause: our lit-candle handler declared four enclosing animateTick
arguments after the Consumer but `@ModifyArg` does not permit that
capture. The previous ASM test checked only @At owner/call target and
did not validate the callback method descriptor, so it could not
catch this class-load failure. GL wrong-thread log is a secondary
system-report consequence after bootstrap crash, not the root cause.

Corrective implementation on branch 2.0 (BUILD/STARTUP STILL PENDING):
- For lit animateTick, replace only `@ModifyArg` with `@Redirect`
  of same `Iterable.forEach(Consumer)` invocation. Handler now
  takes Iterable receiver, original Consumer, THEN enclosing
  BlockState/Level/BlockPos/RandomSource; Sponge Mixin official
  @Redirect source explicitly supports appended target-method args.
- For +Y/outside Planet call offsets.forEach(originalConsumer)
  without an extra Consumer allocation. For rotated Planet call
  offsets.forEach(offset ->
    originalConsumer.accept(PlanetParticleEmitter.rotateUnitBlockEmitterOffset(
      offset,frame))).
  Preserve every vanilla lambda particle, RNG and sound.
- Static extinguish @Redirect, smoke velocity and stable Planet
  offset helper unchanged.
- Add `CandleParticleEmitterInvocationTest` guard for exact compiled
  **BOTH** Redirect method descriptors and correct staticness,
  plus updated @At annotation expectations (two Redirects).
- Update research and roadmap with rejected @ModifyArg path and
  pending recheck; main startup from previous seven-block source
  batch remains last confirmed runtime good.

Next gate (must obtain user evidence before proceeding with
further unaccepted candle-dependent Mixin work):
git pull && .\\test.ps1 && .\\run-client.ps1
Expect Gradle tests green and client world attachment/diagnostics;
CANDLE gameplay and full Phase-4 subsystem gameplay NOT accepted.


## 2026-10-08 user screenshot after candle @Redirect signature fix, and next topology source gate

User provided another in-game screenshot after code ending at ad53f3a7.
It shows an active Planet world (core 0/128/0, diameter 97) and
the familiar diagnostic messages: 90 survival + 90 updateShape
support tests passed, 108 vanilla placement states passed, 36
local-neighbor Frame API tests passed, 22 expected physical raw
BlockPos.relative differences detected, 36 local capability mappings,
36 physical passthrough, 36 standard item/fluid/energy and one
cache invalidation passed. This is direct evidence the candle
@Redirect no longer prevents client bootstrap/Planet login.
It is NOT a visible candle/particle behavior PASS.

NEXT ROOT RESEARCH (completed):
- docs/research/LOCAL_VOLUME_SOURCE_SAMPLING_1_21_1.md.
- EnchantingTableBlock.BOOKSHELF_OFFSETS uses 3D local ring samples,
  isValidBookShelf static helper checks physical provider/transmitter
  positions, and EnchantmentMenu uses that helper SERVER-SIDE to
  compute enchantment level. Must adapt one shared query for both
  client particles AND server enchant gameplay; particle-only hook
  is wrong.
- SporeBlossomBlock.animateTick does 1 falling spore then 14
  candidate local cells. Each checks collision shape BEFORE the
  THREE extra random double samples/emission. Global-Y-only cell
  selection is wrong on rotated faces; require transformed sampled
  candidate + matched transported-frame unit-cell coordinates.
- Existing PlanetBlockFrameContext.walk handles one direction,
  not simultaneous local X/Z/Y displacements. Near corners, axis
  order matters.

CODE IMPLEMENTED on branch 2.0, NOT YET VERIFIED:
- src/main/java/dev/planetary/world/PlanetLocalBlockOffset.java
  static traverse(sourceContext,dx,dy,dz), deterministic X->Z->Y
  ordered topology; steps via existing PlanetBlockFrameContext,
  after seam maps the **entire** EAST/SOUTH basis via FaceTransform,
  preserving local UP semantics. Returns target context with real
  physical BlockPos; rejects radial face flips without an edge
  transform and overflow MIN_VALUE offsets.
- src/test/java/dev/planetary/world/PlanetLocalBlockOffsetTest.java
  verifies 6-face interior 450 cells, 24 directed edge one-axis
  equivalents to walk, six X->Z transported seams, 24 triple-face
  corner samples, zero offset and invalid integer.
- This new API is pure/non-injected and NOT used by production
  block/particle code yet. No change to Mixin registrations, world
  generation, accepted collisions or capabilities. Research
  documents and roadmap updated with prerequisite and ownership.

Before hooking EnchantingTable membership or SporeBlossom block
samples, run Gradle JUnit (user checkpoint). Do not assume PASS
from committed code. No per-source manual gameplay tests yet;
whole Phase-4 gameplay matrix remains pending.


## 2026-10-08 Pure three-axis offset Gradle acceptance, Phase-4 enchanting/spore integration

User replied **BUILD SUCCESSFUL** after pulling branch 2.0 HEAD
6010863e and running the previously requested `git pull && .\test.ps1`.
That is real user-local confirmation of JUnit passing for the initial
`PlanetLocalBlockOffsetTest`. NOT a new game/client smoke, because
this checkpoint was pure topology code with no new Mixins.

Next coherent source implementation researched in
`docs/research/LOCAL_VOLUME_SOURCE_SAMPLING_1_21_1.md`
and written to branch 2.0 (NEW RUNTIME TEST/CLIENT UNVERIFIED):

- `PlanetLocalBlockProjection.physicalOffset(level,pos,dx,dy,dz)`:
  explicit source-local physical coordinate bridge, ordinary world
  retains exact vanilla BlockPos.offset. On Planet uses transported
  ordered 3D projection (even from +Y if crossing an edge).
- `EnchantingTableBookshelfGravityMixin`, registered in COMMON mixins:
  precisely two @Redirects inside the static
  EnchantingTableBlock.isValidBookShelf for BlockPos.offset(Vec3i)
  (provider) and BlockPos.offset(III) (transmitter). Hooks receive
  original BlockPos receiver/call args and enclosing Level/tablePos/
  offset context. Original vanilla getBlockState and block tags
  remain the gameplay policy; EnchantmentMenu(server) and
  animateTick(client) both call this exact static predicate.
  This avoids desynchronizing particles from enchantment power.
- `EnchantingTableParticleGravityMixin`, CLIENT:
  one @ModifyArgs at animateTick Level.addParticle to rotate original
  sampled ENCHANT origin and authored local bookshelf velocity,
  no extra random samples/particle calls.
- `SporeBlossomParticleGravityMixin`, CLIENT:
  @Inject at animateTick HEAD, cancellable, delegates on rotated
  Planet ONLY to PlanetSporeBlossomSourceRuntime.emit. Vanilla
  ordinary worlds/+Y run unmodified.
  Runtime mirrors tiny 1.21.1 vanilla source sampler: sample 2
  random doubles for first FALLING_SPORE_BLOSSOM at local y=0.7;
  14 attempts of Mth.nextInt(-10,10) X,
  -random.nextInt(10) Y, Mth.nextInt(-10,10) Z;
  candidate physical cell and transported chart from
  PlanetLocalBlockOffset.traverse; getBlockState and
  isCollisionShapeFullBlock on correct physical candidate;
  ONLY if non-full sample three nextDouble() local jitter and
  emit SPORE_BLOSSOM_AIR with that candidate's chart.
  All vanilla probabilities/RNG order and emission counts conserved.
  This is a version-sensitive SOURCE METHOD COPY because candidate
  and jitter must remain coupled and must not rely on mutable global
  state. Carefully re-review during version ports.
- `PlanetLocalBlockOffset` now has strict same-face-dominance
  arithmetic fast path, one target frame resolution; all seams,
  ties and corners use existing cell-by-cell FaceTransform walk.
  No cache or per-tick persistent state.
- `VolumeParticleMixinContractTest` inspects actual 1.21.1
  class-file offset/addParticle call-sites, all four Mixin
  handlers' EXACT JVM descriptors/staticness plus annotations
  and mixin registration. `VolumeParticleFrameTest` checks
  enchantment source/motion and spore subcell chart on all faces.

Known gameplay integration complexities intentionally NOT marked PASS:
- enchanted bookshelf offsets near cube vertex may converge to the
  same physical provider (requires later Phase-2 count-policy audit);
- SporeBlossom canSurvive/updateShape are Phase-2 block support;
  source sampler itself is Phase-4 particles;
- source bytecode/different NeoForge transformed class semantics
  must be confirmed by user Gradle/client; the assistant cannot run
  the actual game in the connected GitHub environment;
- normal/non-Planet and +Y must remain vanilla; accepted earlier
  emitter particle physics/capabilities must regress-test at the
  ONE final particle gameplay matrix.

Next user checkpoint because multiple new method-level Mixins:
git pull && .\\test.ps1 && .\\run-client.ps1
Expect BUILD SUCCESSFUL including two new tests, menu + Planet
startup diagnostics and no Mixin InvalidInjectionException.
No claim of runtime/gameplay PASS on new changes yet.


## 2026-10-08 user Gradle failure in VolumeParticleMixinContractTest: TWO assertions

User ran Gradle tests after Phase-4 volumetric EnchantingTable/Spore
source integration HEAD 35f2c5df and pasted two test failures.
No client launch success/failure after this point was supplied:

1. allFourVolumeMixinHandlersHaveExactSignatureAndAtContract:
   expected Redirect targetMethod=isValidBookShelf but observed null.
   Actual compiled handler descriptors, staticness and @At INVOKE
   targets all matched. Root: ASM test only implemented
   AnnotationVisitor.visit("method"), but Mixin annotation's method
   property is String[], encoded through visitArray("method").
   Fix: read both the real array and optional scalar defensively,
   require one method for the expected handler. No runtime callback
   method changes.

2. vanillaBookshelfPredicateAndParticleBodiesPreserveTargets:
   expected Vec3i offset + III offset; actual NeoForge target class
   has Vec3i offset, Vec3i offset, III offset.
   ROOT source: official NeoForge EnchantingTableBlock.java.patch,
   which changes the provider predicate from vanilla
   is(BlockTags.ENCHANTMENT_POWER_PROVIDER) to
   getEnchantPowerBonus(level,pos.offset(offset)) != 0,
   therefore two Vec3i offset calls. Existing provider @Redirect
   matches BOTH. ASM test now asserts exactly the three call sites.
   No production runtime code changed to fix these tests.

IMPORTANT NEWLY DISCOVERED REAL GAP, NOT A JUNIT ASSERTION BUG:
NeoForge EnchantmentMenu.java.patch separately accumulates
numerical bookcases += level.getBlockState(pos.offset(offset))
.getEnchantPowerBonus(level, pos.offset(offset)) AFTER the
isValidBookShelf check. Those raw menu offsets remain PHYSICAL and
are NOT converted by our shared isValidBookShelf hook. On rotated
faces, valid local bookshelves may be detected but enchant power
still use the wrong physical block. This requires a separate
bounded server EnchantmentMenu bonus query adapter, retaining
NeoForge getEnchantPowerBonus and EventHooks hooks; not done yet.
DO NOT CLAIM functional enchantment power correct/accepted.
Source patch cited in
docs/research/LOCAL_VOLUME_SOURCE_SAMPLING_1_21_1.md.
New AGENTS.md rule: inspect transformed NeoForge call counts
and read Mixin annotation String[] correctly.

Tests corrected on branch 2.0; no Gradle/Minecraft run by assistant.
Immediate user gate: git pull && .\\test.ps1
Only if passing can next round address menu numerical bonus and
then game startup / final Phase-4 gameplay matrix.


## 2026-10-09 remaining ASM unit regression: Inject.at is At[] (test-only fix)

User followed up after the two `VolumeParticleMixinContractTest`
fixes with one remaining failure:
`allFourVolumeMixinHandlersHaveExactSignatureAndAtContract`.
Actual SporeBlossomParticleGravityMixin data decoded by ASM:
- expected/actual JVM handler signature MATCH,
- targetMethod animateTick MATCH,
- kind Inject, static=false, cancellable=true MATCH,
- expected atValue HEAD but parsed atValue null.

Confirmed in official SpongePowered/Mixin Java annotation sources:
`@Inject.at(): At[]`, while `@Redirect.at(): At` and
`@ModifyArgs.at(): At`. The test previously handled only direct
`visitAnnotation("at",...)`, not
`visitArray("at") -> visitAnnotation(null, At)`, even when
the Java annotation source spells just `@At("HEAD")`.

FIX committed in branch `2.0`:
- `VolumeParticleMixinContractTest.hooks`: shared `visitAt`
  visitor called from both direct single-At and array-At forms.
- Additional strict assert: EXACTLY ONE target method and one
  @At per registered handler. Retains all original bytecode
  invocation, descriptor, staticness, cancellable and target
  checks; no runtime source changed.
- AGENTS.md, roadmap, local-volume research updated with this
  exact failure and test architecture.

Status: TEST-ONLY FIX IMPLEMENTED / USER `git pull && .\\test.ps1`
PENDING. DO NOT claim Gradle, startup or source behavior accepted.
Known future runtime issue untouched: server EnchantmentMenu
numerical NeoForge `getEnchantPowerBonus` still uses raw physical
pos.offset and requires targeted Phase-2 integration once tests
pass. Existing user-confirmed screenshot last proves candle
startup only, not new enchanting/spore runtime paths.


## 2026-10-09 user confirmed Gradle green; numerical NeoForge enchanting bonus adapter

User: **BUILD SUCCESSFUL** after `VolumeParticleMixinContractTest`
corrections to ASM `@Inject.at(): At[]` parsing. This is verified
JUnit acceptance for existing enchanting/spore volume Mixins
**before** the new menu adapter. No post-fix user client-world
startup evidence was supplied.

Root completeness audit:
NeoForge EnchantingTableBlock.isValidBookShelf has two
`BlockPos.offset(Vec3i)` for its modded-power predicate plus one
`offset(III)` for transmitter. Our COMMON EnchantingTableBookshelfGravityMixin
correctly reframes all, so provider validity in both client and server
is canonical. However NeoForge EnchantmentMenu.slotsChanged's
`ContainerLevelAccess.execute` callback separately sums
`getBlockState(pos.offset(offset)).getEnchantPowerBonus(level,
pos.offset(offset))` for numerical enchanting strength.
Both menu offset calls had remained unadapted physical XYZ.
Official source patch confirmed exact site; Parchment mapping and
independent 1.21.1 mixins confirmed method name
`lambda$slotsChanged$0(ItemStack,Level,BlockPos)V`.

NEW implementation on branch 2.0 (RUNTIME STARTUP PENDING):
- COMMON `EnchantmentMenuBookshelfPowerMixin`
  @Redirect into synthetic instance lambda$slotsChanged$0
  of exactly `BlockPos.offset(Vec3i)`, applied at its two
  NeoForge callsites. Handler receives source BlockPos+local
  Vec3i and enclosing ItemStack,Level,BlockPos; maps via
  `PlanetLocalBlockProjection.physicalOffset`.
  Thus getBlockState receiver, getEnchantPowerBonus context
  and previously validated isValidBookShelf provider all agree.
- Non-Planet and vanilla +Y still map to same physical positions;
  NeoForge method bonus (float), EnchantmentHelper cost integer
  conversion, onEnchantmentLevelSet hooks, random seed, UI and
  modded bookshelf extension point are PRESERVED/UNCHANGED.
- Mixin registered in common planetary.mixins.json.
- Added `EnchantmentMenuBookshelfPowerMixinContractTest`
  outside reserved package, asserting exact synthetic
  instance lambda signature, TWO BlockPos.offset(Vec3i)
  invokes, one getEnchantPowerBonus call and predicate INVOKE,
  exact compiled @Redirect method[]/@At, handler JVM descriptor
  and correct registration. No run by assistant.
- Details in `docs/research/LOCAL_VOLUME_SOURCE_SAMPLING_1_21_1.md`
  and `docs/IMPLEMENTATION_PLAN.md`.
- LIMIT: lambda name is version-sensitive; cross-mod @Redirect
  competing at same INVOKE might conflict. Keep port audit.
- Edge 3-face unique physical provider bonus counting remains
  phase-2 design pending; do not claim gameplay accepted.

NEXT user gate:
`git pull && .\\test.ps1 && .\\run-client.ps1`.
Need green test and user-confirmed menu/world startup, then
full enchanting/spore gameplay acceptance later with whole
Phase-4 matrix. Do not claim current menu/particle behavior PASS
from last green JUnit (which preceded this code).


## 2026-10-09 user frustration: compileTestJava failure and permanent green-CI rule

User's latest local compile after adding EnchantmentMenuBookshelfPower
reported TWO javac diagnostics caused by ONE bug:
`EnchantmentMenuBookshelfPowerMixinContractTest.java`,
wrong ASM override `public AnnotationVisitor visit(String,Object)`
should be `public void visit(String,Object)`.
No JUnit tests executed; compileTestJava stopped first. The user
explicitly objected to repeated uncompiled commits and long
back-and-forth. This is valid feedback; avoid doing that again.

Fix committed to `2.0` (a52b3a0), exactly the invalid override
corrected; inspected all remaining visitor callbacks in that test.
No production Mixin/world code was altered.

**NEW PERSISTENT DEV WORKFLOW**: created
`.github/workflows/test.yml` in commit
`afceb3b0fc9c675eb1d7c319a161920156474e1a`.
On every `2.0` push: Java 21 setup, Gradle 8.12 setup/cache,
Gradle compileJava compileTestJava, then Gradle test,
upload test reports on failures.
Verified by reading GitHub Actions job status:
run ID `37845321513`, job `113544635577`, complete SUCCESS:
both Gradle compilation and JUnit tasks returned SUCCESS.
Public run URL:
`https://github.com/Abbygree11/Planetary/actions/runs/37845321513`.

AGENTS.md now **requires** assistant to monitor green Github CI
for latest code changes BEFORE requesting any manual user build
or Minecraft game check. Avoid replacing GitHub green test evidence
with source inspection claims. For commits that only change
documentation after the green test, GitHub Actions still runs,
and CI should remain green; follow up on branch HEAD if needed.

IMPORTANT NOT ACCEPTED:
- NeoForge client startup after EnchantmentMenuBookshelfPowerMixin
  is not yet confirmed. CI JUnit does not run actual client
  transformation. Next game check once branch CI is green:
  git pull && .\\run-client.ps1, main menu + Planet world probes.
- Enchanting numerical power on rotated faces, spore particles,
  boundary/corner behavior, and Phase-4 complete gameplay matrix
  remain unaccepted.
- No unexplained developer tool capability claims: connected
  GitHub API alone cannot execute Gradle, but GitHub Actions
  CI now handles it and job status can be inspected via API.


## 2026-10-09 Phase-4 readiness after green code CI / gameplay pending

User asked to continue if everything okay. GitHub Actions for
code commit afceb3b0 ran Java 21 Gradle 8.12 compileJava +
compileTestJava + JUnit test and completed SUCCESS (run 37845321513).
Later HEAD 340c2516 contained documentation-only changes and was
still executing at the time of the next review; do not invent a
green HEAD if not observed.

Current source implementation covers all known non-blocked Phase-4
particle families, including six-face base motion, special custom
tick/render, diverse block emitters, candle family, shared
enchantment table provider predicate AND numeric menu bonus, and
spore-blossom candidate-volume sampling. **NONE of the new
enchantment/spore runtime hooks has been user-confirmed after
client startup**. Compile/JUnit green is not sufficient to mark
real Mixin application PASS.

New canonical acceptance document:
docs/acceptance/PHASE_4_PARTICLES.md
- one startup gate (client boots/loads Planet, built-in probes),
- one coherent cross-face family visual/motion matrix,
- explicit Phase-5 fluid, Phase-7 body sources, Phase-7B weather,
  Phase-9 portal layout, Phase-2 triple-corner bookshelf identity
  deferrals. Source audit and work queue are docs/research/
  PARTICLE_MATRIX_1_21_1.md and IMPLEMENTATION_PLAN.md.

Next workflow:
1. Confirm latest branch 2.0 HEAD GitHub Actions CI success.
2. Because post-candle EnchantmentMenu/Spore Mixins have not yet
   been runtime smoke-tested, request a single user
   git pull && .\\run-client.ps1, with main menu/world/probe
   acceptance. Do not demand repeated local Gradle, CI owns that.
3. Then one Phase-4 matrix gameplay pass, not per-class testing.
4. If runtime Mixin crashes, investigate exact nested exception,
   fix on 2.0 and wait for CI green before asking client retest.


## 2026-10-09 in-game startup confirmation after enchanting/spore/menu Mixins

User posted a Planet in-game screenshot following client launch after
the new enchanting-table, spore-blossom and EnchantmentMenu numerical
bonus Mixins. Screen shows:
- support probe passed 90 survival + 90 updateShape;
- placement probe passed 108 vanilla state checks;
- Frame API probe passed 36 local-neighbor checks;
- 22 raw BlockPos.relative mismatches DETECTED (expected positive diagnostic,
  not failed tests);
- capability probe passed 36 local-side mappings, 36 physical-side
  pass-through, 36 item/fluid/energy checks, 1 cache invalidation;
- Dedicated Planet world active, core (0,128,0), diameter 97.

This is direct evidence that the post-candle EnchantingTable,
SporeBlossom and EnchantmentMenu Mixins do not prevent the client from
entering Planet world; the screenshot shows that the listed in-world
compatibility probes passed. Earlier standing/shape lines are out of
this screenshot's viewport and should not be claimed from this image.

Independently GitHub Actions run 37846622714 on branch 2.0 HEAD
a9c5368e3687bce2ca417f486452f097e1798058 completed SUCCESS,
including compileJava/compileTestJava and JUnit suite.

Acceptance status:
- CI / build+JUnit: PASS, independent GitHub CI;
- client startup / Planet attach / visible probes: OBSERVED PASS from
  user screenshot;
- actual enchantment POWER, bookshelf placement near a seam, spore
  distribution, candle/block particle orientation, visual full six-face
  matrix: GAMEPLAY ACCEPTANCE PENDING, NOT PASS.

Canonical one-session acceptance matrix:
docs/acceptance/PHASE_4_PARTICLES.md. No further individual build
or client-smoke request necessary before this manual gameplay matrix.
Known fluid/vehicle/body/weather/portal/3-face provider policy gaps
remain deferred to owning subsequent phases.


## 2026-10-09 first Phase-4 manual matrix found Phase-2/3 blockers; dev fixture lab committed

User submitted screenshots and exact 10-row Phase-4 results:
1 BASE shared particle gravity PASS;
2 block destruction/collision/falling debris PASS;
3 vanilla + redstone torch emitters PASS;
4 cherry leaf particle motion PASS after manual leaf placement,
  while saplings cannot be planted on side/bottom (Phase-2 growth);
5 furnace/campfire/local emitter particles reportedly PASS where
  supported, but End Rod and Ender Chest placement/model are
  incorrect on side/bottom (Phase-2/3);
6 candle & candle cake +Y PASS, side/bottom cannot place
  (Phase-2 support/item placement BLOCKER);
7 enchantment particle PASS, table/book model visually broken
  on rotated faces (Phase-3 BE renderer; numeric enchant POWER
  not yet observed in-game);
8 spore blossom cannot be installed on rotated faces (Phase-2
  ceiling support BLOCKER; airborne emission NOT ACCEPTED);
9 FlintAndSteel cannot ignite rotated/bottom worlds (Phase-2
  useOn + Phase-9 portal geometry BLOCKER). Water-drip falls
  in correct local direction but originates at wrong point
  (Phase-4 emitter / Phase-5 integration). Other particle types
  too cumbersome to recreate manually;
10 seam/regression/performance user says OK.

Prior Phase 2/3 research was comprehensive but implementation
and genuine integrated gameplay acceptance remain PARTIAL.
Root defects must be assigned to their actual mechanism families;
not repaired with particle patches merely to make Phase 4 tests pass.
The exact screenshots prove symptoms, not unique call-path causes.
User emphasized their earlier warning that skipping prior phases
was premature. Phase-4 cannot be fully accepted yet; user-confirmed
rows 1/2/3/10 and part 4 should be retained as accepted, NOT
retested on each new patch unless touched.

NEW fixture prerequisite planned/researched at
docs/research/DEBUG_TEST_FIXTURES_1_21_1.md and implemented:
- PlanetTestFixtures.java builds 20 paired reference / natural-placement
  stations on 6 face interiors (120 pairs). Physical positions from
  PlanetGravityFrame, central Planet worldgen only.
- cyan floor tile = explicitly server-installed canonical-state block
  to test render/emitter. Lime = EMPTY adjacent vanilla placement
  test area; must NOT falsely call forced setBlock a placement PASS.
- stations: end rod/chest, enchantment table + bookshelf, ceiling
  spore blossom support, candles/cake, cherry leaves, torches,
  campfires, fueled furnaces, brewing stand, respawn anchor,
  unlit portal ignition frame, obsidian interaction, dripstone
  scaffold. Exact grid in docs/acceptance/PLANET_TEST_LAB.md.
- /planetary test build / legend / go <face> / rebuild <face>
  commands from PlanetTestCommands, gated to Planet world and
  permission level 2. Rebuild explicitly destructive within bounded
  footprint; auto-build is SAFE/no-overwrite.
- auto-install at login only when Gradle runClient sets
  -Dplanetary.debug.fixtures.auto=true, not in normal exported jars.
  Marker guard persists across server restart, previously
  occupied test region is preserved. Only fresh-spawn players
  may be auto-relocated to the +Y pad.
- new pure layout test, Github Actions CI for latest code commit
  is PENDING at time of authoring. NO GAMEPLAY/STARTUP acceptance
  yet for fixture infrastructure; do not ask user to compile until
  CI is successful.

NEW execution order documented in IMPLEMENTATION_PLAN:
(1) stable fixture infrastructure, (2) Phase-2 placement/survival
& interaction families, (3) Phase-3 special renderers, (4) redo
blocked subset of Phase-4 integrated acceptance, (5) Phase-5
fluid subsystem when dependencies are stable.


## 2026-10-09 fixture initial GitHub CI compile failure and correction

First full fixture code CI run 37856767439, based on commit
76a4dd8, reached compileJava but FAILED with one exact Java error:
`PlanetTestFixtures.java:280` referenced undefined
`CherryLeavesBlock.PERSISTENT`. The actual Minecraft 1.21.1
owner is `LeavesBlock.PERSISTENT` (verified against
net.minecraft.world.level.block.LeavesBlock).
The original import had already been replaced by LeavesBlock,
but the static property reference on one source line was missed.
Corrected in 2.0 commit e663b23 (runtime behavior unchanged).
This is explicitly NOT a user-local build failure: GitHub Actions
caught it before user was asked to pull/test.

Second CI run 37857156948 automatically started for the corrected
commit. Do not claim PASS until compileJava + compileTestJava +
JUnit both complete SUCCESS. No user runtime test requested yet.


## 2026-10-09 six-face fixture lab CI green; runtime acceptance pending

Second GitHub Actions CI run 37857156948 for the latest Java/Gradle
change e663b23ea935b48380a8f80e047644bf06c0a1a9 completed SUCCESS:
- Compile production and test sources: success;
- JUnit tests: success;
- includes new PlanetTestFixturesTest.
No user-local compile was needed to find/correct the initial
CherryLeavesBlock property reference bug.
All subsequent branch 2.0 commits through this note update ONLY
documentation/acceptance/research files, ignored by CI design.

Version of lab now ready for ONE client test:
- runClient dev JVM automatically requests first-time safe six-face
  fixture install on the dedicated Planet world only.
- Auto-install never force-clears player builds and repeats nothing
  where a persistent gold marker exists.
- Preset cyan sample vs empty lime placement cell isolates Phase-3
  rendering/particle motion from Phase-2 natural placement.
- /planetary test go <face> navigation, /planetary test legend,
  /planetary test build and explicit destructive rebuild <face>.
- 20 stations x six faces, portal unlit, fueled furnace stations;
  drip source and non-reproducible entity particle families
  remain genuine deferred integration, not fake passes.

STATUS: CI PASS; actual fixture appearance, startup time and
state survival PENDING first user launch. NEXT: ask only
`git pull && .\\run-client.ps1` (no test.ps1; CI owns tests).
If occupied past saves skip face pads, use a fresh test save or
explicit one-face rebuild only after user has considered local
builds. On success begin Phase-2/3 families with the lab.
Do not mark Phase-4 completely done or blindly start Phase-5.


## 2026-10-09 Phase-2 family-wide support+placement implementation, acceptance pending

User requested substantial Phase-2 repair following manual results:
particles are fine but actual placements and orientations on rotated/
negative-Y faces are faulty; prior Phase-0/1/2/3 diagnostic probes
only covered partial mechanisms. Plan must treat Phases 2/3 as OPEN
until integrated gameplay passes, not skip straight to Phase 5.

NEW research (comparative MC 1.21.1, exact ownership):
docs/research/PHASE2_PLACEMENT_SUPPORT_FAMILIES_1_21_1.md
BlockItem canPlace -> canSurvive; BushBlock canSurvive delegates to
polymorphic mayPlaceOn on world-Y below; Cake/CandleCake require
isSolid() physical support; Candle requires canSupportCenter;
SporeBlossom requires local UP ceiling and no water; updateShape
gets PHYSICAL neighborPos; End Rod state FACING is local but
its clicked face and neighbor block inspection are PHYSICAL;
Ender Chest horizontal FACING must be translated from player-body
frame to target-local frame, while preserving WATERLOGGED.

IMPLEMENTED in branch 2.0, GREEN compile+JUnit for first package
run 37940713012, code commit b43ea8f3:
- BushBlockLocalSupportMixin @Redirect on actual canSurvive below()
  returns local-DOWN physical support position, preserves virtual
  subclass mayPlaceOn and vanilla updateShape.
- CandleBlockLocalSupportMixin canSurvive local-DOWN with
  PlanetBlockSupportRuntime.canSupportCenter; updateShape RETURN
  handles physical support removal without skipping water ticks.
- CakeFamilyLocalSupportMixin targets CakeBlock+CandleCakeBlock:
  canSurvive local-DOWN support position keeps original isSolid();
  updateShape identifies physical support-neighbor rather than
  raw world-DOWN direction.
- SporeBlossomLocalSupportMixin local-UP canSupportCenter,
  preserves !level.isWaterAt; updateShape physical ceiling neighbor.
- PlanetDirectionalPlacement stable helper + EndRodLocalPlacementMixin
  preserves physical hit/adjacent neighbor and converts both rod
  and neighbor local FACING via their own canonical frames.
- EnderChestLocalPlacementMixin RETURN changes only FACING via
  existing PlanetFrameApi.localHorizontalDirection; waterlogged
  part remains vanilla.
- All six registered in common mixins JSON defaultRequire=1.
- Phase2PlacementFamilyMixinContractTest confirms class-file
  canSurvive below/above INVOKEs, actual declared methods,
  compiled exact handler JVM descriptors and registration.

After that first green run, fixture code needed minor correction
because natural cherry sapling placement lane was polished andesite,
and spore blossom lane lacked a local-UP ceiling. Commit a7c480b0
changes only PlanetTestFixtures: cherry_leaves GREEN slot now uses
grass soil; spore_blossom GREEN empty slot now has a STONE ceiling
at local-UP +1 from empty placement position. Other automatic
fixture safety and 20 station pairs unchanged. NEW CI run
37941416965 is IN PROGRESS for that fixture code; do not claim green
yet until it completes.

No user runtime acceptance of new Phase-2 Mixins as of this note.
Remaining broad phase 2 includes many more placement/update families,
plant growth/light, portal ignition, three-face corners. Phase-3 BER
for Ender Chest and enchanting book remains a separate OPEN owner.
The first combined Phase-2 family gameplay acceptance can now be
done on CYAN reference/LIME empty+soil+ceiling stations once
current code CI turns green and client successfully starts.


## 2026-10-09 Phase-2 family package and corrected fixture code CI PASS

GitHub Actions run 37940713012 (Java source and new six-family
Mixin contract test, commit b43ea8f3) completed SUCCESS for
compileJava, compileTestJava and full JUnit.
After a fixture support adjustment (grass soil for cherry sapling
and real ceiling for GREEN spore blossom natural placement lane),
subsequent run 37941416965 for code commit a7c480b0 ALSO
completed SUCCESS: both compilations and JUnit green.
Thus latest Phase-2 code is compiled/tested automatically. No user
runtime login and natural placement confirmation since this patch;
new Mixins could still fail to transform at Minecraft startup or
be semantically incomplete. Next single client + family spot-check
on +/-X, -Y using six-face fixtures, not another hand-built suite.
Existing accepted particle behavior must remain unchanged.


## 2026-10-09 Phase-2 interrupted response recovery and staged whole-mechanism audit

User requested comprehensive non-per-block Phase-2 orientation audit, after
long previous response disconnected: deliver the SAME comprehensive scope
in independently committed research/engineering stages so progress survives.
Only branch 2.0, no main modifications.

On inspecting repo HEAD fc3d98d7, the interrupted attempt
ACTUALLY SAVED earlier research/roadmap:
- ORIENTATION_MECHANISMS_ATLAS_1_21_1.md P01-P40 owner families;
- PHASE2_VANILLA_CLASS_CENSUS_1_21_1.tsv all 293 top-level
  comparative block java files (53 representative source method
  spotchecks, 240 pattern-only; NOT registered block ids);
- PHASE2_ITEM_CREATION_CENSUS_1_21_1.tsv all 121 item package
  files (pattern-only; NOT registered IDs);
- PHASE2_ORIENTATION_ACCEPTANCE_1_21_1.md full six-face release gates;
- AGENTS.md new hard phase2 mechanism completeness rule and
  IMPLEMENTATION_PLAN P0 comprehensive census/owner requirements.
Preserve them, do NOT redo the entire audit monolithically.

NEW Stage 2A independent source research:
docs/research/PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md
compared Mojang 1.21.1 source for BlockItem/place/updatePlacementContext/
getPlacementState/canPlace/setPlacedBy/late DataComponents.BLOCK_STATE,
DirectionalPlaceContext synthetic player, StandingAndWallBlockItem,
HangingSignItem, ScaffoldingBlockItem, PlaceOnWaterBlockItem, FireCharge,
FlintAndSteel, tools, and precise historical NeoForge patches.
Important NeoForge ItemAbilities.FIRESTARTER_LIGHT hook and sound API
must not be accidentally overwritten by copying vanilla BlockItem.

NEW Stage 2A executable registry census:
src/test/java/dev/planetary/world/Phase2RegistryOrientationCensusTest.java
uses actual bootstrapped MC BuiltInRegistries.BLOCK/ITEM (ModDevGradle),
captures registry IDs, class names, orientation candidate property
names+legal domains, method declaration chains, BlockItem link.
Writes four files to build/reports/planetary. All orientations
explicitly REVIEW_PENDING, not accepted. CI workflow uploads as
phase2-neo1211-registry-census artifact and prints counts.
CI run 37986315706 started for last code change a0141fa8;
may still be pending when this note is read. Do not claim PASS
without exact CI success. This research/test patch changes NO
runtime/Mixin or accepted particle behavior.

The next independent stages:
2B inspect actual registry census plus superclass/override families
support/survival/update/tangent/pairs/rails;
3 compare actual owners vs Planetary current adapters with
IMPLEMENTED/GAP/CROSS_PHASE/NA/REVIEW_PENDING dispositions;
4 implement whole mechanism waves A-E, green Java CI,
client startup and physical six-face natural placement acceptance.
No actual finished Phase-2 gameplay claim yet.

Prior user accepted particles and shared gravity; Phases 2/3
remain unclosed until integration.


## 2026-10-09 Phase-2 source wave B and provisional forty-family crosswalk

Following the interrupted audit recovery, completed second
independently checkpointed source research note:
`docs/research/PHASE2_SOURCE_OWNER_AUDIT_WAVE_B_1_21_1.md`.
Direct comparative Mojang 1.21.1 source inspected:
SeaPickleBlock overrides canSurvive and independently calls below(),
so existing BushBlock canSurvive Mixin does NOT affect sea pickle;
CocoaBlock overrides support using state.FACING; LanternBlock
HANGING/canSupportCenter/waterlogging own algorithm;
AmethystClusterBlock six-facing support and physical callback update;
CropBlock preserves polymorphic super.canSurvive but adds light gate;
SaplingBlock actual randomTick above/light; Cactus/SugarCane above/below
and growth column scans; GrowingPlantBlock+Head+Body growthDirection
and head/body role transitions; ScaffoldingBlockItem/Block distance;
MultifaceBlock six-face attachments and VineBlock distinct spread;
BaseRailBlock supportRigid and RailState adjacency/sloped tracks.
These are named SOURCE OWNERS, not just user-symptom patches.
No runtime behavior changed during this source research.

Added initial comparison
`docs/research/PHASE2_PLANETARY_COVERAGE_SEED_1_21_1.tsv`:
exact 40 P01-P40 mechanism rows mapped to source-present
Planetary helpers/Mixins or explicit gaps. PRE-REGISTRY
source-only disposition counts:
5 PARTIAL_FOUNDATION, 14 PARTIAL_FAMILY, 18 GAP,
3 CROSS_PHASE. All rows
`PENDING_ACTUAL_NEOFORGE_REGISTRY_CENSUS`, actual six-face
natural-placement `NOT_VERIFIED`. These are NOT 40 tested
groups and not 18 factual runtime regressions, just known gaps.

CI audit pipeline note:
New Phase2RegistryOrientationCensusTest Java compilation and
JUnit suite passed in GitHub Actions 37986315706, but overall
workflow FAILED at the newly added "Show registered orientation
census" step: file not found at
`build/reports/planetary/phase2-neo1211-summary.txt`.
Most likely source is custom NeoForge unit test worker's CWD:
the test wrote a RELATIVE report path, while workflow expected
repository-root build/reports. An unverified diagnosis, not a
conclusion about test execution.

Corrective code commits b9157a0 and da2d01a:
test reads system property `planetary.phase2.censusReportDir`,
Gradle test task passes explicit absolute location under
project build/reports/planetary. GitHub CI run 37987135612
launched for the second commit; monitor its actual report
counts and success before marking Stage2A executable census
accepted. If report STILL missing, inspect test XML/execution,
don't weaken coverage gate.

Mandatory next: finalize real registered owner & property data,
then replace preliminary 40-row crosswalk review-pending with
source-verified owner dispositions and run family-wide repairs
across waves A-E. No additional player build requests during
research; accepted Phase-4 particles unchanged.


## 2026-10-09 Phase-2 source wave C multiblock/structure audit

Added docs/research/PHASE2_SOURCE_OWNER_AUDIT_WAVE_C_1_21_1.md:
source inspected DoorBlock (upper part, hinge, updateShape, open),
BedBlock (head/foot and interactions), DoublePlantBlock (upper/lower),
ChestBlock (left/right pairing + waterlogged + BER consumer),
TrapDoorBlock (clicked face/half, power, water ticks),
FenceGateBlock (IN_WALL graph + powered OPEN interaction),
CrafterBlock and JigsawBlock (FrontAndTop legal 3D pairs),
ChiseledBookShelfBlock (hit-region-to-slot not covered by FACING
model rotation), StructureTemplate (direct physical placement
with mirror/rotate + block entity metadata; bypasses
BlockPlaceContext). This research is owned by P09/P16-P21/P34-P40,
with phases 3/5/7A/9 integration explicitly named.
No runtime Java/Mixin patches added by wave C.

All stage files are durable in GitHub branch 2.0, even if answer
connection drops again. Current dynamic register census CI must
provide actual NeoForge registered BLOCK/ITEM owners to
complete stage3 final coverage map. Initial source-only 40-row
crosswalk is explicitly preliminary, not a full acceptance.


## 2026-10-09 full real-owner NeoForge registry census v2 CI GREEN

Continued bounded full-Phase-2 research after user's previous
long-run UI connection loss. Three independent source audit
files are now saved in `docs/research`:
- PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md: physical
  BlockPlaceContext, synthetic DirectionalPlaceContext,
  specialized BlockItem, late BLOCK_STATE component,
  NeoForge tool ability and sound hooks;
- PHASE2_SOURCE_OWNER_AUDIT_WAVE_B_1_21_1.md: support
  override bypasses SeaPickle/Cocoa/Lantern/Amethyst,
  Bush/Crop, Sapling/column growth, GrowingPlant Head/Body,
  Multiface/Vine, Scaffolding and RailState graphs;
- PHASE2_SOURCE_OWNER_AUDIT_WAVE_C_1_21_1.md:
  Door/Bed/DoublePlant/Chest physical pair transitions,
  Trapdoor/FenceGate orientation+interaction,
  Crafter/Jigsaw FrontAndTop, bookshelf hit slots,
  structure template/alternate creation bypasses.

P01–P40 preliminary crosswalk to existing Planetary adapters
committed as PHASE2_PLANETARY_COVERAGE_SEED_1_21_1.tsv;
5 PARTIAL_FOUNDATION, 14 PARTIAL_FAMILY, 18 GAP,
3 CROSS_PHASE = 40 investigative categories, ALL
registered owner/gameplay acceptance REVIEW_PENDING.
These are NOT counts of actual broken blocks.

Phase2RegistryOrientationCensusTest uses true bootstrapped
NeoForge 21.1.215 registries, exports real IDs, class
hierarchies, actual declared nearest lifecycle method
owner, all ancestor declarations and BlockState property
domains, specialized Item use/placement owner chain.
GitHub Actions uploads generated TSV artifact. First
attempt had relative output path error, corrected with
absolute Gradle test systemProperty path. First successful
CI run 37987135612: 1060 BLOCK IDs, 241 concrete classes,
1333 ITEM IDs, 87 item concrete classes, 925 BlockItems,
1712 property instances, 527 initial property-candidates.
Second code revision aa395729 improves candidate names
attached/attachment/in_wall/bottom and maps full class
hierarchy+effective owners.

**GitHub Actions run 37988064055 finished SUCCESS** for code
aa395729: compileJava and compileTestJava SUCCESS,
all JUnit SUCCESS, output summary and artifact upload SUCCESS.
Second-version candidate count 528. The artifact is available
under https://github.com/Abbygree11/Planetary/actions/runs/37988064055
and stored in current model workspace as
/mnt/data/phase2_registry_v2.zip (tool-scoped, not a
persistent user project path). Generated TSV source was
inspected by pandas:
- Block getStateForPlacement effective owners: 103;
- Block canSurvive: 60;
- Block updateShape: 103;
- setPlacedBy: 15; neighborChanged: 25;
- rotate:61; mirror:57;
- useItemOn:24; useWithoutItem:52;
- Item getPlacementState:
  BlockItem 862, StandingAndWallBlockItem 58,
  GameMasterBlockItem 5;
- Item updatePlacementContext:
  BlockItem 924, ScaffoldingBlockItem 1;
- 1060 total blocks, including 532 false by simple
  property name heuristic: many still have positional
  checks (SeaPickle/Cactus/SugarCane/etc).
Report:
docs/research/PHASE2_EFFECTIVE_OWNER_FINDINGS_1_21_1.md.
The effective owner scan is a Java reflection classification
of nearest declared method, NOT bytecode-level semantics
or full BlockBehaviour.BlockStateBase state-cache ownership.

docs/research/PHASE2_NEOFORGE_REGISTRY_FINDINGS_1_21_1.md
and PHASE2_REGISTERED_FAMILY_PRIORITY_MAP_1_21_1.md
document source verified counts and priorities.
Roadmap UPDATED to Stage 2A+B+C source/audit GREEN,
Stage 3 ongoing real owner disposition/matrix,
Stage 4 mechanism wave A-E implementation PENDING.

NO new production Mixins or gameplay behavior changed in
this research phase. NO new user-local Gradle/client check
required. Previously accepted particles remain accepted.
Next deliver Stage-3 review of 241 effective block classes,
87 item concrete classes, compiled override/BlockStateBase
and all P01-P40 owner semantics; then implement coherent
family waves A-E and CI/client gameplay batches.
Never promise entire Phase 2 done solely from research
or this JUnit census.


## 2026-10-10 resumable full Phase-2 research checkpoint

User explicitly confirmed the mandate to research and repair all
orientation-related Minecraft 1.21.1 engine mechanism families,
not example blocks, and to split the long investigation into
independently persisted stages after a response-disconnection.
Existing repository work is substantial: P01-P40 atlas,
293/121 comparative source-file inventories, source waves A/B/C,
NeoForge registry owner census 1060/1333, CI run 37988064055
green. NOT a full proven owner disposition or gameplay acceptance.

Committed canonical staged checkpoint at
docs/research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md.
At this note, next step Stage 3A: block owner/override dispositions
for all actual 241 concrete block implementation classes, then
Stage 3B: items and alternate placement, 3C: compiled/NeoForge
lifecycle, Stage 4: complete common-mechanism implementation waves,
Stage 5: full acceptance matrix. Persist each completed stage and
resume earliest incomplete stage; DO NOT redo existing source
or fake a 241/241 reviewed claim from heuristics.


## 2026-10-10 connection-loss mitigation: phase cards in docs/phases

User reports three successive long research responses hanging/disconnecting.
All previous Phases 0–11 (including 7A and 7B) are now split into
independently resumable working checklists under `docs/phases/`.
Active Phase 2 is further split into ten stage files under
`docs/phases/phase-02/`; each stage file has even smaller checkbox
micro-tasks. Canonical roadmap + research remain authoritative.

IMPORTANT: existing Phase-2 research (P01–P40, 293 source block files,
121 source item files, NeoForge registry class/owner census and prior
successful CI) MUST NOT be repeated. Resume precisely Stage 3A owner
disposition from existing `PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md`,
starting with one evidence-reviewed group of 8–15 owner classes in
`docs/phases/phase-02/01-block-owners.md`. Never claim 241/241
audit pass from heuristic categories.

No runtime code/test was changed in this documentation-only restructure.
No new Gradle or client run required. Every next interaction should
complete ONE small bounded task, commit it, update checkbox/status,
and end with a short summary + exact next task.


## 2026-10-10 Phase-2 micro-checkpoint: original registry artifact verified

Stage 3A, card 2.3A-1, micro-task 1/4 DONE: the original run
37988064055 downloadable artifact (ID 11643813158) was independently
parsed, confirming 1060 unique BLOCK IDs, 241 concrete block types,
1333 ITEM IDs, 87 item types, 925 BlockItems, 1712 state properties,
528/532 name-heuristic split. All 22 initial owner clusters were
reconciled to real type/hierarchy/effective method-owner evidence;
NO 241/241 semantic review or gameplay acceptance is claimed.
The published 103 getStateForPlacement owner number is specific to
BlockPlaceContext; including all same-name overloads gives 104 distinct
owner class names. BushBlock hierarchy holds 59 registry IDs across
29 concrete classes, only 40 of which dispatch canSurvive to BushBlock.
Research details: PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md.
Next checkpoint: 2.3A-1 micro-task 2 (12 concrete BushBlock descendants).
User organization: ONE CHAT PER PHASE, ONE COMMITTED MICRO-TASK PER RESPONSE.
No runtime/test changes; no new CI or gameplay check requested.


## 2026-10-10 Phase 2, Stage 3A: BushBlock descendant audit checkpoint

Step 2 of docs/phases/phase-02/01-block-owners.md DONE:
12 real registered concrete descendants, 18 registered IDs,
source evidence and effective NeoForge method-owner signature
joins in docs/research/PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md.
This covers 18 of 19 Bush hierarchy block IDs overriding inherited
canSurvive; SeaPickleBlock is the nineteenth and already has initial
cluster-level research. Observed distinct mechanisms: CropBlock farmland
XZ growthSpeed, DoublePlant world-Y paired lifecycle, PitcherCrop
age-triggered pair, small dripleaf soil/water/direction, full-water
tall seagrass, standing vs hanging mangrove, mushroom independent
survival/3D spread. Source evidence is comparative, not exact patched
NeoForge bytecode. All gameplay and full family acceptance remain pending.
No production code changed; no CI/client rerun needed. NEXT: step 3/4
first 12 evidence-backed class-level TSV dispositions, others pending.


## 2026-10-10 Phase 2 Stage3A first 241-row owner ledger

Micro-task 3/4 committed (documentation only). Created
PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv with one row per actual
registry concrete class: 241 rows totaling 1060 BLOCK IDs from
CI artifact 11643813158 (run 37988064055). 12 BushBlock descendant
classes with 18 IDs now have evidence-backed comparative-source
lifecycle + exact method-declaring-owner dispositions. Other 229
classes (1042 IDs) are explicitly REVIEW_PENDING. No extra source
classes were silently declared PASS, nor do reflection method owners
prove compiled Mixin INVOKE or patched NeoForge behaviors. A new
PHASE2_BLOCK_OWNER_DISPOSITION_GUIDE_1_21_1.md specifies field
meaning, method signatures, and status gates. Next: card 2.3A-1
micro-task 4/4, another bounded family. No runtime code changed,
no game test or CI re-run requested.


## 2026-10-10 Phase2 Stage3A second inherited Bush owner batch

Task 2.3A-1 / 4 of 4 source research + ledger checkpoint:
12 more actual concrete BushBlock descendants (34 registered IDs)
reviewed against pinned 1.21.1 source and real 21.1.215
effective-method owner census. New research file:
docs/research/PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md.
New source findings: SaplingBlock randomTick samples pos.above then
TreeGrower (Phase8); Azalea bonemeal pos.above fluid; Fungus bonemeal
required below substrate and configured feature; TallGrass bonemeal
creates DoublePlant pair directly; Seagrass fluid source placement
and updateShape water tick + tall pair growth; SweetBerryBush
light sampling, harvest and body-dependent entity collision; NetherWart
AGE growth and soul sand support. All 12 canSurvive declare at
BushBlock, but independent consumer paths are not covered by the
BushBlockLocalSupportMixin's below() support redirect.
241-class ledger now has 24/241 comparative-source-reviewed and
217 pending; 52/1060 block IDs under reviewed classes and 1008
pending. No actual gameplay or NeoForge patch-bytecode validated,
no Java modified. Next FIRST pending Phase2 card:
docs/phases/phase-02/02-block-owners-rest.md, task 1,
one 8–15-class algorithm family, separate committed response.


## 2026-10-10 Phase2 2.3A-2.1 face-attachment source audit

First microtask of docs/phases/phase-02/02-block-owners-rest.md DONE,
one atomic docs-only commit. Research file
PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md covers 11
actual registry classes (29 block-ID count): TorchBlock,
WallTorchBlock, RedstoneTorchBlock, RedstoneWallTorchBlock,
LadderBlock, LanternBlock, SporeBlossomBlock,
AmethystClusterBlock, EndRodBlock, LeverBlock, ButtonBlock.
New 11 have 1.21.1 comparative Java declaring-owner evidence
only; exact NeoForge 21.1.215 reflection and ASM are explicitly
pending (unlike previous 24 class rows with verified reflection).
StandingAndWallBlockItem, redstone signal paths, hanging support,
six-direction Facing and FaceAttachedHorizontalDirectionalBlock
ancestor bypasses are documented. Ledger remains 241 concrete
classes / 1060 registry block IDs, now 35 source-reviewed /
206 REVIEW_PENDING, corresponding 81 and 979 ID counts.
No item path accepted, no NeoForge patched bytecode or gameplay
PASS, and no production code changed. NEXT microtask 2.3A-2.2,
verify compiled NeoForge method owners and creation/interaction
bypass of these 11, then update 11 ledger evidence cells.


## 2026-10-10 Phase2 2.3A-2.2 attachment owner reflection and item join

Recovered original GitHub Actions CI ZIP 11643813158
SHA256 7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e,
parsed original block and item TSVs. For 11 attachment
classes from previous 2.3A-2.1 package, compiled NeoForge
21.1.215 nearest method-declaration reflection owners
confirmed, 29 actual registered BLOCK IDs recorded and
26 Item registry creators audited: 3 StandingAndWallBlockItem
and 23 simple BlockItem. WallTorchBlock 2 IDs and
RedstoneWallTorchBlock 1 ID have no direct BlockItem:
the standing torch item selects wall variant. Eight
relevant Planetary Mixins inspected and registered;
no dedicated LanternBlock or AmethystClusterBlock
support Mixin in scanned source directory. Changes saved
to PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md
addendum and 241-row disposition TSV, with all 35
reviewed rows now `REFLECTION_OWNER_VERIFIED`.
35 source-reviewed classes (81 IDs); 206 class pending
(979 IDs). ASM INVOKE, NeoForge patch body, actual Mixin
handler application and gameplay remain REVIEW_PENDING.
NEXT FIRST undone: 2.3A-2.3, small non-FACING class group.
No Java code changes and no additional CI/gameplay result.


## 2026-10-10 Phase 2 stage 2.3A-2.3 non-FACING growth graph review

Docs-only committed microtask (step 3/4) for 12 previously
REVIEW_PENDING block classes without FACING or any named
orientation property. Source+NeoForge runtime reflection
owners taken from original CI artifact 11643813158
(SHA256 7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e).
Groups: 4 GrowingPlant head/body pairs (Kelp, CaveVines,
WeepingVines, TwistingVines) with virtual growthDirection
UP/DOWN and head/body conversion, plus SugarCane,
Cactus, BambooStalk and BambooSapling, featuring local
horizontal support/water/forbidden-neighbor checks,
vertical columns and direct generation. BambooStalk
getStateForPlacement can author BAMBOO_SAPLING even
though BAMBOO_SAPLING has no registered own item.
CaveVines and CaveVinesPlant override useWithoutItem
for berries. KelpBlock getStateForPlacement has
BlockPlaceContext vs LevelAccessor overloaded signatures
with different declaring owners. Research:
docs/research/PHASE2_STAGE3A_NONFACING_GROWTH_GRAPHS_1_21_1.md.
241-row disposition: 47 source/compiled reflection reviewed
classes covering 93 registered IDs; 194 REVIEW_PENDING
covering 967 registered IDs. No patch-body/ASM or gameplay
acceptance, no production code change. NEXT task 2.3A-2.4:
registry/status invariant audit, separate committed turn.


## 2026-10-10 Phase2 Stage3A-2.4 original CI ZIP reconciliation

Completed exactly one documentation-only microtask for
`docs/phases/phase-02/02-block-owners-rest.md` step 4.
Original artifact 11643813158 ZIP SHA-256
7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e.
Compared all 241 concrete class names and exact per-class
registry ID counts (1060 IDs), plus all 47 reviewed
class-level exact registered ID sets (93 IDs) and
nearest NeoForge declaring class of 5 exact method
signatures. All four FNV-1a fingerprints matched
original raw CI source:
roster 0xf188a064, reviewed ID sets 0x6c35dd77,
five declaring owner fields 0xacd03109, combined
0x9fc5dbf5. 194 concrete classes remain
REVIEW_PENDING, 967 associated BLOCK IDs. No patched
bytecode, Mixin application, runtime or gameplay PASS.
Guide front matter now shows current totals and labels
older per-package snapshots historical. Research:
docs/research/PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md.
Stage3A NOT DONE. Next separate card
docs/phases/phase-02/02a-block-graph-owners.md,
microtask 1, 10 candidate P25/P26 graph owner classes
(71 registry IDs), source audit ONLY next turn.
No production code, tests, or game run modified.


## 2026-10-10 Phase2 Stage3A-3.1 connected graph class owner audit

Independent eight-class/source research package (task 1/4) completed:
FenceBlock, FenceGateBlock, WallBlock, IronBarsBlock,
StainedGlassPaneBlock, VineBlock, GlowLichenBlock and
SculkVeinBlock; **69** exact registered IDs sourced from original
NeoForge 21.1.215 CI ZIP 11643813158 and pinned comparative 1.21.1
Java source. Mechanisms: P25 tangent 4-neighbor boolean links,
FenceGate FACING/IN_WALL/perpendicular wall and redstone P36;
P27 WallSide NONE/LOW/TALL plus source-local above collision
DOWN face shape and UP post; IronBarsBlock inherited by
StainedGlassPaneBlock; P26 Vine UP+4 with upper vine support fallback,
random spread; 6-face MultifaceBlock inherited by GlowLichenBlock
and SculkVeinBlock, distinct full support OR collision face
predicate, face-removal updates, bonemeal and sculk spread authors.
FenceBlockGravityMixin already exists SOURCE_ONLY; no game PASS.
Audit: docs/research/PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md.
Ledger now 55/241 class SOURCE_REVIEWED_INTEGRATION_PENDING
(162/1060 associated block IDs), 186 pending (898 IDs).
TripWireBlock and TripWireHookBlock explicitly remain REVIEW_PENDING,
not conflated with fence/face algorithm. All ASM/Planet adapter
/gameplay acceptance REVIEW_PENDING; no runtime code change.
NEXT card docs/phases/phase-02/02a-block-graph-owners.md
step 2/4: full registered ITEM + alternative author / interactions.


## 2026-10-10 Phase2 Stage3A-3.2 exact 69 BlockItem authors + alternate state writers

Completed bounded docs-only task 2/4 on
`docs/phases/phase-02/02a-block-graph-owners.md`.
Artifact original NeoForge 21.1.215 ZIP `11643813158`:
all 69 source-reviewed graph-block IDs from 8 concrete
classes have unique ordinary `BlockItem` entries,
`placed_block=registry_id`, and all 7 item creation
method declaration owners at `BlockItem`. No special
item aliases in these 69. Independent comparative
source audit established `DataComponents.BLOCK_STATE`
can override graph bits AFTER item placement; fence
`LeadItem` is leash interaction, not block placement;
FenceGateBlock can author OPEN/FACING/POWERED via
useWithoutItem, neighborChanged and onExplosionHit;
VineBlock direct randomTick spread; GlowLichenBlock
bonemeal -> MultifaceSpreader -> non-player placement;
SculkVeinBlock regrow/onDischarged/attemptUseCharge;
MultifaceGrowthFeature generic source call can author
six-face states with nonplayer overload. Detailed research:
`docs/research/PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md`.
No class statuses promoted: 55 source+reflection reviewed
classes (162 registered IDs), 186 REVIEW_PENDING (898 IDs),
241/1060 total. TripWire and hook remain pending.
No patched NeoForge ASM/Mixin, runtime or gameplay PASS.
NEXT FIRST incomplete 2.3A-3.3: graph cached shapes,
physical neighbor event vs local chart; Phase2/3/5/7A.
