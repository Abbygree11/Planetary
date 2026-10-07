# Final completeness sweep R1-R6 — Planetary 2.0 / Minecraft 1.21.1

Status: FINAL RESEARCH/ARCHITECTURE CROSS-SWEEP COMPLETE (2026-10-08).
Runtime acceptance: NOT DONE. No runtime code modified.
Scope: cross-reference *known source/mechanism research* in six completed R batches,
plus external spatial mod case studies. No claim to have mathematically audited
every branch of all Minecraft or third-party classes.
See: AGENTS.md, IMPLEMENTATION_PLAN.md, AI_CONTEXT.md and
EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md.

## 1. Evidence catalog and status normalization

R1: BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md — source/state placement/support,
physical neighbor update, pairs, connections, growth, rails, falling blocks.
R2: CLIENT_RENDER_PARTICLE_MATRIX_1_21_1.md — world VoxelShape vs local
model frame, AO/culling, BER, particles, emitters, accelerated renderer.
R3: FLUID_MECHANISM_MATRIX_1_21_1.md — FlowingFluid graph, fluid caches,
FluidState height/shape, fluids/mod hooks, renderer, entity fluid semantics.
R4: ENTITY_BODY_NETWORK_MATRIX_1_21_1.md — local entity/body vs physical
motion/collision/packet, forces, projectiles, vehicles, camera, raycast.
R5: AI_AUTOMATION_MATRIX_1_21_1.md — chart-aware navigation, RandomPos,
signal/redstone/pistons/rail, two-stage capabilities/logistics.
R6: ENVIRONMENT_WORLDGEN_STRUCTURE_COMPAT_MATRIX_1_21_1.md —
surface/heightmap, weather/light/skylight, spawn, climate, worldgen stages,
seam-safe features, whole-structure bounds, portals, persistence/compat.
EXTERNAL: EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md — upstream real-world
pitfalls (GravityChanger, Immersive Portals, Valkyrien Skies, Cubic Chunks,
Up And Down And All Around, Starminer).

Meaning of COMPLETE here: research has owners, key source paths, open
engineering blockers and test plans; does not mean implemented, compiled,
performance-tested or manually accepted.

## 2. Final semantic contract (first-pass architecture FROZEN)

| Domain | Source of truth | Permanent forbidden shortcut |
| --- | --- | --- |
| Physical block storage/chunks/networked positions | vanilla XYZ / BlockPos / ChunkPos | contextually overriding BlockPos/XYZ |
| Canonical BlockState orientation | position-derived PlanetBlockStateFrame | storing physical UP in local-only Direction properties |
| Ordered seam traversal | chart-aware PlanetBlockFrameContext / PlanetBlockStep | treating physical cell alone as every graph node |
| Entity movement and external forces | physical deltaMovement TARGET; body-local in explicit PlanetEntityBodyFrame | silently passing body-local velocity to world APIs |
| Collision/raycast/hit | physical collision geometry, explicit body/frame projection | treating all AABB/Y or ray calculations as local |
| Rendering | canonical model queries + transformed physical quads/geometry | global all-VertexConsumer rotation or AO duplication |
| Block updates/neighbor notification | physical six neighbors, semantic local side as explicit data | globally redefining NeighborUpdater |
| Fluid topology | ordered chart-aware graph, cached by sufficient graph state | treating world Y as fluid DOWN or relying on position-only slope cache |
| Nav graph | physical BlockPos + traversal chart where needed | position-only A* cache at seam/corner |
| Signal/logistics | physical target FIRST, target canonical side SECOND | translating capability side and assuming target was correct |
| Weather/spawns/surface | PlanetSurfaceQuery/ClimateQuery + physical candidate | globally replacing XZ Heightmap |
| Procedural terrain/biomes | continuous PlanetGenerationSpace, physical voxel writes | six independent face generators |
| Structure / Portal | whole rigid extent plus local-orientation policy | bending an arbitrary rigid structure through seam |
| External mod frames | explicit typed transform + optional compat integration | blanket claim of mod compatibility |
| Persisted worldgen | codec/profile version + physical saved positions | changing generated-world noise silently |

Physical and local/world semantics are specific to a domain. In particular:
- Physical Direction for network packets/hits and generic neighbor enumeration.
- Canonical LOCAL properties for blocks (even where limited to HORIZONTAL).
- Traversal CHART when multiple conceptual graph states share one physical cell.
- Entity BODY/local motion and camera have their own frame and hysteresis.
- Generation coordinates and global ENVIRONMENT are different from gravity.
- External contraption coordinates are composed, never overridden.

Runtime gap: current active code still stores local entity deltaMovement in the
Planet gravity region. R4 already rejected this as future design and requires
explicit migration. Do not relabel the runtime as meeting the frozen contract.

## 3. Cross-batch coverage review and dispositions

### 3.1 R1 ↔ R2 — semantic block vs physical geometry
- BlockState property meaning is position-only canonical; geometry gets rotated
  through position-aware shapes/BakedQuad metadata, not through state cache.
- All physical face occlusion/quad culling must stay consistent with shape
  rendering and canonical block support.
- FrontAndTop Crafter/Jigsaw discovered R5 is already in R1; check all
  properties & model rotation with its two-direction data.
- World lighting direction/shading is Phase 7B.4; static model geometry
  Phase 3 should not hardcode environmental daylight assumptions.
- ACCEPTANCE owner: Phase 2 for property/support, Phase 3 for visual.

### 3.2 R1 ↔ R3 ↔ R5/7A — traversal vs physical neighbors
- NeighborUpdater physically notifies all six physical cells; local tangent
  traversal/graph edge must be handled in consumer families.
- Fluid slope cache, rail graph, redstone wire graph, piston push graph and
  AI node identity are separate semantics with chart-aware states.
- Target physical neighbor and canonical local receiving side are separate,
  especially across exact three-face corners and logistics.
- At a corner, graph paths may retain distinct chart even for same physical
  BlockPos; physical changes/signals should deduplicate by physical cell.
- ACCEPTANCE owners: Phase 2F, 5, 6, 7A respectively. No duplicate RailShape
  or contradictory edge stepping implementations.

### 3.3 R2 ↔ R4 ↔ R7B ↔ R11 — entities and presentation
- Collision and actual position/velocity are physical; body frame resolves
  local locomotion, step, fall, eyes, ray, camera, passenger offsets.
- Particle emitter coordinates and particle force/physics must be separated
  from world-space sound/visual events and physical world hit normals.
- Smooth camera visual rotation need not equal instantaneous authoritative
  block chart; preserve client/server projection agreement.
- External cases add NaN/huge-AABB, unloaded teleport, rider persistence,
  early constructor frame availability, and bad entity section selection.
- ACCEPTANCE: core Entity phase before AI and before Phase-11 comfort polish.

### 3.4 R3 ↔ R4/6/8 — fluid and terrain
- Constant planet SEA_SHELL is a worldgen radius policy, NOT a vanilla global-Y
  ChunkGenerator.getSeaLevel replacement, and not a modified fluid height.
- Flood fill, currents, waterlogged state and boats each use their owning
  subsystem. Hydrology carves channels, runtime fluids simulate afterwards.
- Precipitation/freeze invokes real physical BlockPos but derives environment
  from Planet climate/exposure. FluidState shape renderer remains Phase 5.
- Underwater mobs need both aquatic navigation and spawn candidate volume.
- ACCEPTANCE: Phase 5 fluid + Phase 7 boat/current + Phase 8 terrain.

### 3.5 R4 ↔ R5 ↔ R7B — entity motion and AI
- Future physical entity deltaMovement is an explicit foundational migration.
- Ground walk is a local-frame graph; steering uses physical target displacement
  projected into body frame; entity collision/support is Phase 7, not AI.
- Node cache includes chart where path-dependent; no global walkable=true.
- Spawn candidates are Phase 7B, not goal/random target generation Phase 6.
- Registered SpawnPlacements/mob caps and NeoForge hooks remain in effect.
- ACCEPTANCE: collision/entity core before Phase 6 navigation closure.

### 3.6 R5 ↔ R6/9/10 — automation, worldgen, compatibility
- Powered rail consumes Phase-2F RailGraph; piston structure state transport
  consumes Phase-2 canonical property schema.
- Crafter/Jigsaw share FrontAndTop orientation, but generated Jigsaw placement
  and rigid structure extents remain Phase 9.
- Surface scans used by features/spawns/weather are distinct algorithms:
  do not globally change Heightmap.
- NeoForge sided capability adapter doesn't fix wrong upstream target BlockPos.
- A third-party Feature that constructs physical world-Y block patterns cannot
  be made seamlessly WRAP by simply changing noise sampling.
- ACCEPTANCE: adapter at each owning mechanism; mod compatibility is continuous.

### 3.7 R6 ↔ all phases — environment and data
- SkyLightEngine has vertical source assumptions: six-face survival daylight
  is a Phase 7B.4 engineering gate, NOT render-only.
- Source of day/night, local vs global precipitation and weather exposure
  must be documented before climate/lightning implementations.
- StructureStart whole-box clearance BEFORE reference/save/placement; preserve
  regular physical ChunkPos ownership.
- World's finite min/max Y forbids limitless physical +Y/-Y without a storage
  engine rewrite; first pass explicitly finite while unlimited 3D storage is
  an independent future feasibility investigation.
- Persist worldgen algorithm/profile version BEFORE replacing generated
  terrain so old saves do not change silently.

## 4. Missing-mechanism / boundary ledger

These are important explicit implementation RESEARCH GATES, not uncatalogued
features or implied proofs:

P0 CRITICAL:
G1. Physical velocity migration and compatibility adapter contract. Current
    local stored deltaMovement mismatches future cross-mod/world physical plan.
G2. Six-face skylight/source/sky exposure vs vanilla SkyLightEngine; decide
    technical feasibility and user-visible day/night contract.
G3. Height/build limit policy, finite envelope, world settings serialization
    and migration before real persistent worldgen.
G4. Chart-safe exact-corner semantics across fluid/signal/AI/piston and
    canonical state as separate identity.
G5. Actual 1.21.1 NeoForge injected method/field/extension compatibility for
    each high-risk class, not only a Mojang-named third-party source snapshot.

P1 INTEGRATION:
G6. Feature-local integer lattice/placement modifier semantics, seam-safe
    duplicate prevention, and unknown modded feature fallback policy.
G7. World-structure seed/selection/ref validation after whole bbox rejection.
G8. Multi-chunk and save/load versioned generated state determinism;
    multiplayer generation staging and cache invalidation.
G9. PlanetSurfaceQuery/Index mutation invalidation and chunk-boundary loads.
G10. Third-party renderer vs external ship/frame integration; model/particle
    transforms are typed, preserve RenderType/ModelData/accelerated APIs.
G11. Camera view/raycast/packet/gravity-face determinism and network sync.
G12. Performance of physical frame lookup in hot loops and large structures,
     transformed NaN/huge AABB rejection and bounded scanning.

P2 FOLLOW-UP / RISK RECHECK:
G13. Ambient sounds, sky/cloud visuals, structure-map/locate and sky/height
     predicates after environment policy; never infer from rotated gravity.
G14. World-border, commands /tp, summon, bed/anchor, passengers,
     portals/gateways, POI/village AI/raid mechanics on local faces.
G15. Cross-mod mixin order/third-party source class overrides; non-Planet world
     passthrough and real API fallback.

The gates have owners already (7B, 8/9, 10, 1/7, etc.). They are NOT reasons
to resume indefinite global discovery. Investigate each at its owning
implementation boundary.

## 5. Dependency-ordered implementation *batches* (not class-by-class)

Batch 0 — BASELINE/GATES:
- verify branch 2.0, build/test/startup without new runtime changes;
- snapshot accepted and experimental runtime status (especially particle
  checkpoint and current first-pass pathfinding);
- choose finite worldgen envelope and save/profile migration contract;
- set authoritative VS-physical velocity API requirement.

Batch A — SHARED KERNEL/FRAME API (Phase 1 remaining):
- canonical BlockState, traversal chart, ordered physical neighbor, support,
  axis/normal/shape helpers, typed world/body/generation/external transforms;
- deterministic all-6-face/exact-corner tests, vanilla POS_Y identity;
- public API stable names; test zero-gravity core and activation boundaries.

Batch B — BLOCK SEMANTICS + PHYSICAL GEOMETRY (Phase 2 + Phase 3):
- grouped block families (place/support/pairs/connections/ecology/falling/
  RailGraph/FrontAndTop) before render acceptance;
- shapes/culling/quad orientation, AO, BER and moving block/static rendering;
- preserve NeoForge capabilities/model APIs; one acceptance matrix each.

Batch C — ENTITY/MOTION/INTERACTION FOUNDATION (Phase 7.1/7.2/7B.1-2):
- migrate to physical deltaMovement contract before adding more consumers;
- local collision/body/support/fall, then locomotion, projectile, vehicle,
  attachments, eye/ray and packet/client authority;
- controls for early entity initialization, unloaded target, rides/teleports;
- deterministic tests, one shared entity acceptance, not many single fixes.

Batch D — FLUIDS + PARTICLES (Phase 5 / Phase 4 integration):
- FlowingFluid graph/caches, local current, waterlogged/buckets, FluidState
  geometry, renderer and mod hooks; ocean generation later must reuse this;
- finish class-override particles and emitters; biome/weather source emitters
  finish under 7B; one fluid and one particle subsystem acceptance.

Batch E — SIGNALS/AUTOMATION + AI (Phase 7A, Phase 6):
- first rail/redstone/wire/tripwire/piston/logistics using Phase-2/7;
- chart-aware navigation graph/path targets and body-frame controls AFTER
  entity geometry and fluid graph are ready; surface/underwater mob tests;
- no duplicated rail, piston or block-state transport algorithms.

Batch F — SURFACE/CLIMATE/WORLDGEN (Phase 8 + Phase 7B.3-4):
- build and prove PlanetSurfaceQuery + climate/sky exposure contract;
- stage 8A data-driven biome resolver and 8B-8D terrain, constant radial sea
  shell, caves, hydrology; feature integer-lattice policy and cross-chunk
  placement; dimension/world settings version;
- spawn candidate + sky/light/weather server/client integration (do not
  claim daylight as render-only), original mobs, performance budget;
- pause at a major LIGHT ENGINE feasibility gate before promising full
  survival worldgen across all six faces.

Batch G — STRUCTURES/PORTALS/COMPAT/POLISH (Phase 9, 10, 11):
- rigid structure bbox/refs/jigsaw/GravityProcessor, local vertical portals,
  portal travel, per-structure modded policy; accept per batch;
- continuous mod API/NeoForge port and shader/Create/VS integration gates;
- smooth pre-transition camera/body UX, underground and corner transitions,
  with client/server acceptance after physics correctness.

There is no single total sequence inside these batches: actual tasks can
parallelize when dependencies don't overlap (e.g. early terrain field math and
render math). The gates above govern when a batch is eligible for gameplay PASS.

## 6. Gates and acceptance rules

For each batch:
- exact version source route identified and tested (Minecraft 1.21.1,
  NeoForge 21.1.215); third-party Mojang-named copies are research inputs
  only, not NeoForge integration proofs;
- stable pure semantics with identity on POS_Y; relevant six faces, 12 cube
  edges, 8 corners and core;
- deterministic local/world frame round-trip and physical-position invariants;
- preserve RNG count/order whenever retaining vanilla semantics, with explicit
  seeded test for required custom sampling;
- ensure world vanilla/non-Planet bypass;
- preserve NeoForge extension points and public codecs;
- bounded allocation/invalidation per chunk/tick/frame and no NaN/Infinity
  AABB/position query leaks;
- chunk/saved-game/network consistency and one subsystem-level manual matrix;
- user confirms runtime acceptance before marking PASS.

**Test command ONLY when implementing or user requests:** 
git pull && .\test.ps1 && .\run-client.ps1

For this docs-only sweep: no build or gameplay tests required/claimed.

## 7. Scope freeze and future resumption

ARCHITECTURE FROZEN for first implementation *planning*: R1–R6 maps and
the explicit cross-phase contracts above own the next batches. This freeze
does not mean exact Minecraft hook choices are guaranteed, impossible
behavior is solved, or product-policy open issues have magically closed.
A substantive new source discovery can reopen the relevant owning mechanism
with a documented reason; routine class-specific surprises cannot trigger
another global indefinite research pass.

Remaining choices:
- daylight policy (radial ambient vs physically directed sun), weather
  geographic policy and skylight injection feasibility;
- finite radius/height cap and future 3D storage extension;
- terrain/profile codec/version and existing-world migration;
- fallback for arbitrary modded features and structure processors;
- precise continuous/smoothed entity transition.
Record choice/acceptance in IMPLEMENTATION_PLAN.md / AI_CONTEXT.md before
runtime change.

HANDOFF: final global research ends here; the next coding task can start at
Batch 0 (baseline) then Batch A, followed by dependency-driven subsystem
implementation. No modifications to runtime have been made in this sweep.
