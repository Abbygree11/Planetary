# Planetary continuity

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
