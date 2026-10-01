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

Still need:
- runtime level -> field resolver wrapper

- seam-aware local neighbor step; MUST NOT be equivalent to raw pos.relative()
  at a gravity/cube edge
- local offset across multiple edge crossings
- local UP/DOWN support lookup
- local tangent four-neighbor iteration
- rotated face/property helpers beyond Direction and Direction.Axis
- deterministic tie policy at gravity boundaries

Critical design rule:
Vanilla blockstate directions are interpreted as LOCAL semantics in Planet space.
Example: PointedDripstone TIP_DIRECTION=UP means local UP, not physical world +Y.

Acceptance:
- transform round-trip for all 6 faces x all 6 Direction values
- seam step tests for all 24 directed cube-edge transitions
- VoxelShape rotation tests for asymmetric shapes
- no duplicate/missing neighbor at edge/corner
- sided NeoForge capability direction remains correct across seams

## Phase 2 — block placement, survival and updates [PLANNED]

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
adapt placement context and neighbor/support queries at framework boundaries,
not one mixin per block where avoidable.

Acceptance examples:
- standing torch on every gravity face
- wall torch relative to local wall
- ladder, door, trapdoor, bed
- slabs/stairs keep intended player-relative orientation
- pointed dripstone local UP/DOWN placement and survival
- blocks survive/remove when LOCAL support changes
- behavior remains vanilla on POS_Y

## Phase 3 — block collision/selection/render frame [PLANNED]

Research targets:
- BlockState shape accessors
- collision / outline / occlusion / support shapes
- BlockRenderDispatcher
- ModelBlockRenderer
- baked model quads and quad facing
- chunk rebuild/model-data path
- face culling, AO/light neighbor sampling
- BlockEntityRenderer orientation

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

## Phase 11 — transition UX and polish [PLANNED]

After correctness:
- mining through internal gravity boundary
- camera pivot around feet/support point
- input continuity during transition
- tuned hysteresis by entity size/use case
- optional generated cave widening near boundaries
- particles/sounds/screenshake local-frame polish

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
