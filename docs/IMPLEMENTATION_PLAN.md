# Planetary 2.0 implementation roadmap

This is the execution plan for Planetary. Treat it as a gate, not a wishlist.
Do not start a phase by patching the first visible symptom.

For every phase:
1. Read the exact Minecraft 1.21.1 / NeoForge 21.1.215 source paths involved.
2. Write a research note listing the call flow and every world-axis assumption.
3. Decide the smallest architectural boundary that can adapt the whole subsystem.
4. Add pure/unit tests for transforms and seam cases before runtime mixins.
5. Implement one coherent patch set.
6. Run the acceptance matrix on +Y, -Y, X, Z, edge and (when relevant) corner.
7. Only then mark the phase verified and update AI_CONTEXT.md.

## Cross-cutting gravity/local-frame audit [MANDATORY]

Master audit: `docs/research/GRAVITY_IMPACT_AUDIT_1_21_1.md`

This audit is a gate above every numbered phase. Gravity changes are not limited
to player falling and block UP/DOWN. Before closing the project, the following
families must all have an explicit PHYSICAL-vs-LOCAL policy and acceptance
coverage:

- block placement/state properties/neighbor traversal/support/shapes
- redstone/signals/neighbor notifications
- NeoForge sided item/fluid/energy capabilities
- entity collision/step/onGround/fall/pose
- living movement, swimming, climbing, elytra and forces
- projectiles, raycasts, eye/view vectors and interaction
- AI/navigation/look/body controls
- particles
- static block models, BERs and accelerated/custom renderers
- vehicles, rails, pistons, passengers, leash/dismount
- client/server movement prediction and floating validation
- spawning
- environment/weather/skylight/heightmaps
- worldgen/growth/structures
- external coordinate systems such as physics ships/contraptions
- public Planet Frame API for mod integrations

Compatibility rule:
do NOT globally redefine BlockPos, Direction, Axis or raw XYZ. Adapt stable
Minecraft/NeoForge boundaries first. Mods that use standard boundaries should
inherit Planet behavior automatically; mods doing their own raw world-axis math
may require a small integration adapter.

## Status legend
- DONE: user verified in game or deterministic tests fully cover the item.
- PARTIAL: architecture exists and some behavior is verified, but the phase is not closed.
- PLANNED: no runtime implementation should be considered stable yet.

## Phase 0 — stable dedicated Planet runtime [PARTIAL]

Goal: one dedicated Planet world, no legacy debug behavior, no accidental work in
ordinary Overworlds, and a stable performance baseline.

DONE:
- selectable Planet world preset
- PlanetChunkGenerator codec and dedicated dimension type
- automatic Planet gravity binding
- legacy automatic Overworld debug gravity/teleport disabled
- entity-scoped gravity lookups available through PlanetGravityRuntime.findFor(entity)
- player movement/camera/pose baseline works on all six faces
- seamless PlanetGenerationSpace exists

Required before closing:
- audit every entity/navigation hook: use findFor(entity), never only find(level)
  when entity activation can differ
- remove or quarantine obsolete debug render/runtime registrations
- 10 minute empty-world performance smoke test
- 10 minute test with a controlled mob population
- no progressive client/server tick degradation
- record a known-good baseline commit in AI_CONTEXT.md

## Phase 1 — local block/topology kernel [PARTIAL, active]

Goal: define ONE authoritative way for vanilla-local block operations to map to
physical world positions/directions.

Existing useful pieces:
- PlanetGravityFrame
- PlanetVanillaDirection.localToWorld/worldToLocal
- PlanetBlockTopology / PlanetTopology / FaceTransform
- PlanetSidedQueryFrame for sided NeoForge capabilities

Implemented foundation:
- PlanetBlockFrameContext(field, physical BlockPos, preferredFace)
- PlanetBlockStep with physical direction, target frame and transported local direction
- 24 directed edge-entry/fold transitions covered by tests
- edge step + transported opposite is tested reversible
- cached six-face VoxelShape rotation around block center
- inverse physical -> local VoxelShape transform for deterministic round-trip tests
- local Direction <-> physical Direction for all six gravity frames
- local Direction.Axis <-> physical Direction.Axis helpers
- target-local side-toward-source mapping on PlanetBlockStep for support/capability queries
- ordered straight local walk across multiple gravity-boundary crossings
- four seam-aware local tangent steps with explicit three-face corner singularity semantics
- explicit decision to reject unordered local offset(dx,dy,dz) as path-ambiguous
- traversal chart separated from canonical position-only BlockState frame
- explicit canonical block tie policy: X axis, then Y, then Z
- canonical seam-aware support-neighbor/face resolution via PlanetBlockSupportQuery
- deterministic POS_Y BlockState-frame fallback for the zero-gravity core block

Implemented runtime/compat foundation:
- PlanetBlockRuntime as shared Level + physical BlockPos -> active field/frame boundary
- block activation evaluated at the physical block center
- physical capability Direction -> canonical local BlockState side
- dedicated physical capability queries keep the queried BlockPos authoritative
- legacy virtual-atlas capability alias routing retained only as fallback

Implemented placement-frame foundation:
- physical hit-side -> target canonical local hit-side
- physical world click point -> canonical local hit offset
- interactive local nearest-looking reorder without mutating BlockHitResult

Still need:
- third-party pipe/machine stress test after standard item/fluid/energy capability acceptance
- Vec3 direction/vector and normal conversion helpers
- external-frame composition contract for ship/contraption integrations
- rotated face/property helpers beyond Direction and Direction.Axis

Critical design rule:
Vanilla blockstate directions are interpreted as LOCAL semantics in Planet space.
Example: PointedDripstone TIP_DIRECTION=UP means local UP, not physical world +Y.

Acceptance:
- transform round-trip for all 6 faces x all 6 Direction values
- seam step tests for all 24 directed cube-edge transitions
- VoxelShape rotation tests for asymmetric shapes
- edge traversal has no alias physical cell; exact corners preserve 4 logical tangent transitions over 3 physical target cells
- sided NeoForge capability provider receives canonical local side while physical target BlockPos remains unchanged
- diagnostic capability provider: 36 six-face side mappings + BlockCapabilityCache invalidation
- standard item/fluid/energy providers: local-UP accept + local-DOWN reject on all six faces
- public PlanetFrameApi local-neighbor primitive
- Create-like raw BlockPos.relative(local FACING) stress harness across all six faces

## Phase 2 — block placement, survival and updates [PARTIAL, active]

Research targets before implementation:
- UseOnContext
- BlockPlaceContext
- StandingAndWallBlockItem
- DirectionalBlock / HorizontalDirectionalBlock families
- canSurvive / updateShape / neighborChanged call paths
- BlockStateProperties direction/axis properties
- PointedDripstoneBlock
- torch/wall torch, ladder, vine, door, trapdoor, bed, rail, piston
- waterlogged SimpleWaterloggedBlock behavior

Implementation target:
keep BlockHitResult/target BlockPos physical and adapt only semantic orientation
reads through PlanetBlockPlacementFrame / PlanetFrameApi. Do not globally
override BlockPlaceContext.getClickedFace or getClickLocation because vanilla
and mods also use them for physical neighbor/ray geometry.

Detailed placement research:
- docs/research/PLACEMENT_1_21_1.md

Implemented first runtime placement adapters:
- RotatedPillarBlock AXIS from canonical local clicked face
- HopperBlock FACING from canonical local clicked face
- SlabBlock TOP/BOTTOM and replacement checks from local face + local hit Y
- runtime diagnostic: 108 real vanilla getStateForPlacement checks across six faces
- StandingAndWallBlockItem standing/wall variant selection uses target-local
  nearest-direction ordering
- runtime diagnostic: 6 standing + 24 wall variant-selection checks

Implemented first runtime support/update adapters:
- PlanetBlockSupportRuntime shared canonical support bridge
- BaseTorchBlock local-DOWN canSurvive/updateShape
- WallTorchBlock placement/support/updateShape
- RedstoneWallTorchBlock support update (signal semantics still Phase 7A)
- LadderBlock placement/support/updateShape + water tick preservation
- FaceAttachedHorizontalDirectionalBlock placement/canAttach/updateShape
- runtime diagnostic: 30 survival + 30 updateShape checks across six faces
- manual support-removal acceptance verified by user for standing torch,
  wall torch, ladder and lever on rotated faces

Detailed support research:
- docs/research/SUPPORT_UPDATES_1_21_1.md

Implemented local growth/cross-neighbor pass:
- SpreadingSnowyDirtBlock natural survival/spread uses canonical local UP and
  explicit seam-aware growth topology
- SnowyDirtBlock SNOWY placement/update follows local UP
- FenceBlock N/E/S/W placement/update uses four seam-aware local tangent
  neighbors instead of world horizontal directions
- detailed research: docs/research/GROWTH_CONNECTIONS_1_21_1.md

Implemented multi-block/support placement pass:
- pressure plates: local-DOWN survival/update + rotated trigger AABB
- doors: local FACING, hinge, upper-half placement, survival and pair updates
- beds: local FOOT->HEAD topology, target-frame FACING and pair updates
- pointed dripstone: local vertical placement/support/thickness/update
- shared source-local -> physical -> target-local direction reframe helper
- detailed research: docs/research/MULTIBLOCK_PLACEMENT_1_21_1.md

Runtime findings from 2026-10-04 acceptance:
- grass random-tick decay/spread wrong outside +Y -> addressed by local growth pass
- fence unwanted physical +Y arms -> addressed by local connection pass
- exact-edge slab placement still needs player-body-vs-canonical policy
- torch flame/smoke emission origin remains world-axis
- door, pressure plate, pointed dripstone and bed placement -> addressed by
  multi-block/support placement pass; gameplay/render follow-ups remain
- thrown potions/arrows still use global projectile gravity
- fluids still use global FlowingFluid topology
- grass side-overlay and side-face shadows need render follow-up

Acceptance examples:
- runtime login placement probe reports 108 vanilla state checks
- runtime login support probe reports 30 survival + 30 updateShape checks
- breaking the actual local support removes standing/wall torch, ladder and lever
- standing torch on every gravity face
- wall torch relative to local wall
- ladder, door, trapdoor, bed
- slabs/stairs keep intended player-relative orientation
- pointed dripstone local UP/DOWN placement and survival
- blocks survive/remove when LOCAL support changes
- behavior remains vanilla on POS_Y

## Phase 3 — block collision/selection/render frame [PARTIAL, active]

Implemented first physical shape boundary:
- BlockStateBase outline/collision/visual/interaction shapes rotate canonical
  local -> physical on the outermost bound-Level query
- nested shape queries use a ThreadLocal scope to prevent double/triple rotation
- getBlockSupportShape and getOcclusionShape remain canonical for now
- Shapes.block()/empty preserve vanilla singleton fast paths
- runtime diagnostic: 36 physical + 12 canonical + 6 full-block identity checks
- detailed research: docs/research/SHAPES_1_21_1.md

Implemented first static baked-model/render-culling boundary:
- cached BakedModel wrapper per original model x PlanetFace
- cached transformed BakedQuad per original quad x PlanetFace
- physical renderer side -> canonical local getQuads side
- NeoForge QuadTransformers rotates positions + packed normals
- final BakedQuad.direction is re-expressed in physical frame
- ModelData/RenderType/AO/render passes remain delegated through BakedModelWrapper
- frame-aware Block.shouldRenderFace path with source+target canonical local sides
- dedicated thread-local occlusion LRU because vanilla cache lacks gravity frame
- exact seam culling unit test
- grass/mycelium surface seam rendering: every ambiguous outward candidate face
  uses the model's canonical local-UP quad, without changing BlockState frame
- detailed research: docs/research/RENDERING_1_21_1.md

Still open / research targets:
- physical directional shade/environment-light policy
- model offset vectors
- BlockEntityRenderer orientation
- Flywheel/custom accelerated rendering integration
- fluid renderer

Implementation target:
cache rotated shapes and rotated baked-model views for the six gravity frames.
Do not mutate registered BlockState objects.

Acceptance:
- grass top texture points local UP
- asymmetric full model rotates correctly
- torch/dripstone model matches collision shape
- culling does not expose holes between full cubes
- AO/light does not sample the wrong world neighbors
- block entity renderers have a defined local-frame policy

## Phase 4 — falling blocks and generic particles [PARTIAL]

Already verified:
- FallingBlock/FallingBlockEntity physical gravity mostly follows local DOWN
- ordinary block-breaking particles follow local gravity

Still open:
- FallingBlock client animateTick emission must use local DOWN
- FallingDustParticle has its own hard-coded yd -= 0.003 and terminal world-Y clamp
- direct-gravity particle subclasses need complete audit
- Particle.move onGround still classifies negative world-Y collision
- emission helpers such as ParticleUtils.spawnParticleBelow use world Y

Close only after:
- sand/gravel/anvil on each face
- falling dust origin + acceleration + settling correct
- block breaking particles
- rain/drop/drip particles
- no duplicated particles from dual vanilla/custom emission

## Phase 5 — fluids [PLANNED; do not implement before Phases 1-2 kernel]

Detailed research: docs/research/FLUIDS_1_21_1.md

Subsystems that must be handled together:
- FlowingFluid simulation
- LiquidBlock scheduling/interactions
- FluidState height/shape
- waterlogging / LiquidBlockContainer
- bucket placement/pickup
- fluid flow vector used by entities
- LiquidBlockRenderer
- ambient/drip particles
- water/lava interactions
- NeoForge source-creation hook and modded fluids

Hard requirement:
local DOWN/UP/tangent neighbors use the seam-aware block topology layer. Raw
below()/above()/Direction.Plane.HORIZONTAL replacements are insufficient at edges.

Acceptance:
- source bucket on all six faces
- falls local DOWN
- spreads across four local tangent directions
- wraps continuously around an edge
- stable behavior exactly on gravity boundary
- infinite water source rule preserved
- waterlogging
- lava
- water/lava conversion
- entity flow vector points in local physical flow direction
- fluid collision/shape height grows along local UP
- renderer top/side surfaces and normals rotate
- drip particle originates from and falls from local-DOWN face
- one representative NeoForge/modded FlowingFluid

## Phase 6 — ground mob navigation [PARTIAL]

Existing:
- PlanetWalkNodeEvaluator first pass
- local MoveControl target interpretation
- path node local anchor
- local random-stroll target experiment

Known unresolved:
- mobs fail or spin at gravity-zone edge
- raw physical neighbor stepping is not a complete seam transition
- edge waypoint overshoot is experimental, not architecture
- target generators other than RandomStroll remain world-axis based
- node volume, diagonals, hazards, doors, fences, rails and water incomplete

Research before next runtime change:
- GroundPathNavigation
- PathNavigation
- PathFinder
- WalkNodeEvaluator
- NodeEvaluator
- MoveControl / LookControl / BodyRotationControl
- RandomPos family and common goals
- how mob width/height maps to local frame
- existing gravity-mod approaches, noting which versions actually support AI

Implementation target:
path nodes use the Phase-1 seam-aware local topology. An edge is a legitimate
topological neighbor transition, not a target overshoot hack.

Acceptance:
- random stroll flat face
- chase/flee target
- step up/down 1 block
- cross every edge direction both ways
- stop on edge without spinning
- door/fence hazard tests
- no path-search explosion; maxVisitedNodes behavior preserved
- controlled performance test with many mobs

## Phase 7 — other entity subsystems [PLANNED]

### Cross-cutting entity/interaction additions

The master gravity audit makes the following mandatory here, even when their
code lives outside Entity subclasses:
- Entity.move collision/step/onGround/fall semantics
- eye position/view/up vectors and picking
- projectile launch/deflection
- generic force/knockback semantics
- passenger/leash/attachment/dismount
- client/server prediction and floating validation
- body-local render orientation
- world-ray vs local-hit-side distinction


Separate research/implementation gates for:
- swimming and fluid movement
- climbing
- flying mobs
- projectiles
- minecarts/rails
- boats
- elytra
- item/xp entities
- leash/passenger positioning
- knockback/explosions where world-vector assumptions matter

Do not mark generic "entities" done from player walking alone.

## Phase 7A — signals, automation and interaction [PLANNED]

Detailed source audit required for:
- SignalGetter and redstone signal-side conventions
- RedStoneWireBlock, diode/repeater/comparator, observer, torches, levers,
  buttons, pressure plates, sculk/target/tripwire and rail signals
- neighbor notification Direction sets
- physical BlockHitResult vs canonical local hit-side semantics
- Level.clip / Entity.pick / ProjectileUtil
- NeoForge sided BlockCapability and EntityCapability
- BlockCapabilityCache side/context behavior

Acceptance:
- vanilla redstone around all six faces and across an edge
- sided item/fluid/energy capability on side and bottom faces
- capability at exact gravity edge
- one Create-like directional machine/pipe stress test
- raycast/interaction side remains correct while placement state is local

## Phase 7B — client/server frame consistency and environment [PLANNED]

Audit:
- LocalPlayer prediction
- ServerGamePacketListenerImpl movement/floating validation
- teleport corrections and rotation sync
- entity/body render frame
- spawn placement
- precipitation/weather
- skylight/sky visibility/heightmaps
- environment rules that should remain physical-world rather than gravity-local

Acceptance:
- no rubber-band/floating false positives during side/bottom movement
- ray/camera/server interaction agree
- spawning policy documented and tested
- weather/sky/light semantics explicitly chosen rather than inherited by accident

## Phase 8 — real terrain/biome generation [PARTIAL foundation]

Existing:
- PlanetGenerationSpace continuous mapping
- WRAP / AVOID_EDGE policy
- dedicated PlanetChunkGenerator
- fixed plains test terrain

Need:
- standard BiomeSource / climate integration
- NoiseGeneratorSettings / RandomState compatibility
- seamless terrain density in PlanetGenerationSpace
- surface rules
- caves/carvers
- ores/features with seam-safe ownership
- biome decoration

Critical invariant:
six gravity pyramids are physics/local-frame regions, NOT six worldgen regions.

Acceptance:
- terrain/noise continuity across all edges
- biome continuity across edges
- cave crosses edge as one cave
- ore/feature crosses edge once, no duplicate generation
- deterministic seed/reload behavior

## Phase 9 — structures [PLANNED]

Rigid structures use AVOID_EDGE.
- cheap origin clearance
- final StructureStart bounding-box validation
- data-driven per-structure safety margin
- structure-like PlacedFeatures classified separately

Acceptance:
- villages/temples never bend across edge
- no half structure or duplicate start
- modded structure can opt/configure policy

## Phase 10 — mod compatibility [PARTIAL foundation]

Compatibility classes:
- automatic: standard Minecraft/NeoForge boundaries
- mostly automatic: mods delegating to vanilla helpers/subclasses
- integration needed: mods performing direct semantic XYZ/Direction math
- external-frame integration: physics ships/contraptions

Required public foundation:
- one Planet Frame API used by Planetary itself and compat modules
- position vs direction/vector vs normal vs shape transforms kept distinct
- frame composition for external moving coordinate spaces
- no global context-sensitive patch of BlockPos/Direction


Targets:
- TerraBlender / Biomes O' Plenty biome pipeline
- NeoForge BiomeModifiers / PlacedFeatures
- sided block capabilities: item/fluid/energy
- modded block models/shapes
- modded FlowingFluid
- pipes/cables/machines
- custom entity navigation where possible

Rules:
- prefer vanilla/NeoForge extension points
- preserve NeoForge hooks rather than reimplementing them
- document incompatibility when a mod directly assumes global Y in custom code

## Phase 11 — transition comfort, underground navigation and polish [PLANNED]

Goal:
crossing a gravity-zone boundary must feel like a continuous bend of local space,
not an instantaneous 90-degree camera/world snap. This applies both on the outer
surface edge and to internal zone boundaries encountered while mining underground.

Do NOT solve this as only a camera animation after the gravity face has already
changed. Research and compare two layers:

### 11A — pre-transition visual frame

Before the actual zone boundary, define a configurable transition band measured
by distance to the nearest competing gravity face / tie plane.

Candidate behavior:
- camera/local horizon begins rotating BEFORE the exact boundary;
- rotation strength grows smoothly as distance to the boundary approaches zero;
- use quaternion/spherical interpolation between source and target entity frames;
- pivot around the player's support/feet point rather than camera origin;
- keep mouse/input continuous in the interpolated body-local frame;
- no sudden yaw inversion when the dominant face changes;
- transition rate should be limited by angular velocity/acceleration, not only a
  fixed lerp per tick;
- evaluate a subtle spatial cue toward the upcoming face (very mild perspective
  warp, horizon bend, vignette/field cue, or none). Any distortion must be
  optional and must not cause excessive motion sickness.

Important:
visual interpolation alone may hide but not solve a discrete physical gravity
switch. It is therefore only one candidate layer, not automatically the final
architecture.

### 11B — continuous entity gravity frame near a boundary

Research an ENTITY-only continuous gravity frame in a narrow transition region.

Candidate mathematical policy:
- ordinary block/state topology remains the existing six discrete canonical
  PlanetFace frames;
- determine the two strongest competing gravity-face scores for the entity
  position;
- outside the transition width, entity UP is exactly the dominant face UP;
- inside the band, blend source/target UP vectors continuously using a smooth
  curve;
- derive a stable orthonormal entity frame from the blended UP plus transported
  forward/tangent orientation;
- physical acceleration, player movement frame, camera and entity pose may all
  consume this same continuous entity frame;
- block placement/support/collision queries must continue to resolve through
  discrete canonical block frames.

This separation is intentional:
    block semantics = discrete/canonical
    entity transition frame = potentially continuous

Research risks before implementation:
- diagonal gravity can make axis-aligned tunnel floor/wall contact ambiguous;
- Entity.move/onGround/step logic must agree with the blended down vector;
- client/server prediction must use the exact same blend;
- jumping while mid-transition must not inject/lose velocity;
- crossing back and forth around the tie plane must not oscillate;
- exact cube corners have three competing faces and need an explicit policy;
- vehicles/mobs/projectiles may need different transition widths or may initially
  remain on discrete frames.

### 11C — underground transition UX

Underground transitions require their own acceptance, because there is no sky,
horizon or visible planet edge to explain the orientation change.

Research candidate aids:
- start orientation blending several blocks before the internal gravity boundary;
- stronger hysteresis/orientation inertia underground so small movements around
  the tie plane do not repeatedly rotate the player;
- preserve the player's forward heading through the bend using transported
  tangent orientation rather than recomputing yaw from world axes;
- optionally expose a subtle non-HUD environmental cue that indicates the
  direction local DOWN/UP is beginning to bend;
- consider slightly widening/generated smoothing of caves near known gravity
  boundaries only as OPTIONAL worldgen polish, never as the correctness fix;
- verify mining a straight 1x2 tunnel through a boundary without needing the
  player to stop and manually re-orient.

Do not silently move/teleport the player to hide the transition and do not alter
the physical block grid.

### Acceptance matrix

Surface:
- slowly walk toward every one of the 24 directed face transitions;
- camera begins changing before the exact edge and reaches the target frame
  without a visible snap;
- sprint, jump and strafe across the transition;
- reverse direction halfway through the blend;
- stop exactly inside the transition band;
- no camera roll discontinuity and no input inversion.

Underground:
- mine a straight 1x2 tunnel through each representative X/Y, X/Z and Y/Z
  internal boundary;
- continue holding forward while the local frame bends;
- player can understand where floor/wall/ceiling are throughout the transition;
- break/place blocks during the blend without targeting the wrong physical face;
- no repeated 90-degree oscillation when moving one block back and forth near
  the boundary;
- test enclosed rooms where no sky/horizon is visible.

Corner:
- approach a three-face tie from several trajectories;
- transition choice is deterministic and reversible;
- no arbitrary full-spin camera path.

Comfort/config:
- transition width and maximum angular speed should be configurable;
- optional visual distortion/cue can be disabled independently;
- provide a reduced-motion mode with slower/no perspective distortion while
  preserving orientation continuity.

Other polish after correctness:
- particles/sounds/screenshake local-frame polish
- transition behavior for mobs/vehicles/projectiles after player solution is
  stable

## Performance gate for every phase

No phase is closed without checking allocations/tick behavior.
Especially avoid:
- unbounded per-tick maps/caches
- pathfinder activation for entities without Planet gravity
- recursive searches without vanilla-equivalent distance limits
- rebuilding rotated models/shapes every frame instead of caching six variants
- duplicate scheduled fluid/block ticks caused by both vanilla and custom paths


## Mandatory post-change verification format

After EVERY runtime change, fix, refactor that can affect behavior, or meaningful
worldgen change, the assistant must give the user a concrete numbered checklist
of what to verify in game/build.

The checklist must:
1. start with the exact command to run when applicable:
   `git pull && .\test.ps1 && .\run-client.ps1`
2. list each behavior separately, one item per check
3. state the expected result for each item
4. include regression checks for behavior that was working before if the change
   could plausibly affect it
5. include edge/boundary checks when gravity/topology is involved
6. distinguish build/startup checks from gameplay checks
7. avoid vague instructions such as "check that it works"

Example structure:
- Build/startup
- Primary fix
- Edge case
- Regression check
- Performance check (when relevant)

This rule is part of the development process and must survive chat/context resets.
