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
