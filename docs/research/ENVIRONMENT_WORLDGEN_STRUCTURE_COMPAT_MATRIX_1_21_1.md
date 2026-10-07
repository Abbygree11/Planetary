# R6 — Environment, worldgen, structures and compatibility (Minecraft 1.21.1)

Status: RESEARCH COMPLETE for architecture/ownership; runtime acceptance NOT claimed.
Target: Minecraft 1.21.1, NeoForge 21.1.215, Planetary branch 2.0.
Scope owners: Phase 7B.3-4, Phase 8, Phase 9, Phase 10, persistence/portability gates.
Rule: documentation-only research. No worldgen, rendering, lighting or entity runtime modification.

## 1. Evidence and scope

Primary source inspected: Mojang-named 1.21.1 snapshot in
https://github.com/hackersense/OptiFine-Source/tree/main/1.21.1/net/minecraft
(the snapshot may contain OptiFine differences; NeoForge 21.1.215 patch/hook equivalence
must be confirmed against the exact developer source before implementation).

Inspected complete or representative call paths:
- NaturalSpawner; SpawnPlacementTypes; SpawnPlacements; ServerPlayer.adjustSpawnLocation;
- Heightmap; ChunkGenerator; NoiseBasedChunkGenerator; HeightmapPlacement;
  HeightRangePlacement; SurfaceRelativeThresholdFilter; EnvironmentScanPlacement;
  BiomeFilter; ConfiguredFeature; Feature; SnowAndFreezeFeature;
- Biome temperature, freeze/snow/precipitation methods; ServerLevel.tickChunk,
  tickPrecipitation, findLightningTargetAround; LevelRenderer rain/snow paths;
- SkyLightEngine, SkyLightSectionStorage, LevelLightEngine;
- Structure, StructureStart, StructurePlacement, RandomSpreadStructurePlacement,
  JigsawPlacement, JigsawStructure, GravityProcessor, StructureTemplate;
- PortalShape; existing PlanetChunkGenerator, PlanetGenerationSpace,
  PlanetGenerationPlacementPolicy, PlanetInitialTerrain, PlanetWorldSettings,
  PlanetWorldgenRegistries, PlanetFrameApi.
- NeoForge 1.21.1 Biome Modifiers:
  https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/

Source patterns traced: getHeight/getHeightmapPos, above/below, XZ fixed columns,
Direction.UP/DOWN, physical Y for temperature and snow, world-seeded chunk XZ
placement, feature-local vertical iteration, light-column origin and portal rigid
rectangle. Results below distinguish observed source from proposed Planet policy.

## 2. Semantic axes and boundaries

| Mechanism | Vanilla meaning | Planet owner | Boundary |
| --- | --- | --- | --- |
| Chunk storage, ChunkPos and section Y | PHYSICAL | Minecraft | Keep physical XYZ and original chunk index |
| Vanilla Heightmap.Types | PHYSICAL XZ -> Y summary | Minecraft | Preserve for vanilla consumers; not an all-face surface API |
| Planet terrain/climate/caves | GENERATION | Phase 8 | Single PlanetGenerationSpace continuous field |
| Local ground/surface elevation | PLANET SURFACE | Phase 8 shared by 7B/9 | PlanetSurfaceQuery with face/chart and shell elevation |
| Placement of block-structured features | LOCAL + PHYSICAL writes | Phase 8E | Explicit feature placement frame/permission, not magic transformed BlockPos |
| Spawn collision/validity | ENTITY/LOCAL + PHYSICAL | Phase 7B.3 | PlanetSpawnCandidateProvider + existing modded SpawnPlacements |
| Weather particles, deposition | ENVIRONMENT + LOCAL | Phase 7B.4 | PlanetPrecipitationQuery; server and client agree |
| Sun/sky light propagation | PHYSICAL LIGHT ENGINE + ENVIRONMENT | Phase 7B.4 | Separate radial-light source policy; do not rotate Heightmap |
| Rigid structures and templates | PHYSICAL bounding boxes + LOCAL orientation | Phase 9A | PlanetStructurePolicy on whole StructureStart and processors |
| Runtime portal rectangle | LOCAL vertical rigid region + PHYSICAL cells | Phase 9B | PlanetRigidPlaneQuery and portal travel adapter |
| Biome/feature registries, Modifiers | VANILLA/NEOFORGE DATA | Phase 10 continuous | Preserve external registries and codecs |

Never context-patch the general BlockPos, Direction, Level.getHeight, Heightmap
or ChunkPos. A planet-specific query must advertise its surface/frame semantics.

## 3. Spawning: four separate entry paths

### 3.1 Natural spawning

Observed: NaturalSpawner.spawnCategoryForChunk calls getRandomPosWithin;
this chooses a random physical XZ cell, gets WORLD_SURFACE height, then picks
physical Y between build minimum and that height. NaturalSpawner later
uses valid mob category/biome selection, distance checks, SpawnPlacements and
a physical body-space collision/clearance test.

This is not repaired by replacing above/below in SpawnPlacementTypes:
the candidate distribution itself is biased to physical Y columns. One
XZ column can intersect multiple cube faces, while another has no reachable
local surface. Even all-face valid placement would still have biased density.

Future PlanetSpawnCandidateProvider:
1. Enumerate/sample loaded, eligible Planet surface patches, with canonical
   face/chart and physical spawn BlockPos + local UP/DOWN; select surface or
   volume mode according to entity placement type.
2. Preserve Minecraft biome spawn lists, structure spawn overrides, mob caps,
   spawn weights, mob/group and player-distance checks.
3. Delegate final EntityType / SpawnPlacements / NeoForge registered predicate
   with original physical LevelReader + BlockPos; adapt only standard local
   ground/air clearance and local support checks at the owning boundary.
4. Density sampling must be in comparable surface-area/volume units rather than
   treating every Minecraft XZ column as one patch. Maintain vanilla RNG order
   within any retained algorithm; explicitly document the changed seed stream
   when candidate sampling itself must change.

### 3.2 World generation / passive populations

NoiseBasedChunkGenerator.spawnOriginalMobs seeds generation by physical chunk
coordinates and calls NaturalSpawner.spawnMobsForChunkGeneration. PlanetChunkGenerator
currently overrides spawnOriginalMobs with a no-op. Treat population in Phase 8
separately from runtime mob spawning in 7B.3; both use one Planet surface query,
not independent world-Y scans.

### 3.3 Special placement and explicit spawn

SpawnPlacementTypes.ON_GROUND uses pos.below (support), pos and pos.above
(body clearance); IN_WATER uses above. These are LOCAL once a candidate is
already physical. Spawn eggs, spawners, entity summon and custom modded spawn
paths bypass the NaturalSpawner candidate search and must retain their own
positional contract rather than being moved to arbitrary surface cells.

### 3.4 World/bed/respawn

ServerPlayer.adjustSpawnLocation uses world-border XZ distance, global Y scans,
build height and vanilla collision AABB. World spawn and respawn need a
PlanetSpawnSafetyQuery with physical result/rotation, local standing clearance,
bed/respawn-anchor semantics delegated to Phase 2 pairs and Phase 7 body frame.
Do not invent new arbitrary teleport/respawn coordinates. Plain Overworld and
other non-Planet dimensions remain completely vanilla.

## 4. Heightmaps, climate, precipitation, lightning, light

### 4.1 Heightmap is not local gravity

Heightmap packs exactly 16x16 entries and scans downward through physical Y
for each physical XZ column. ChunkGenerator.getBaseHeight and getBaseColumn
also use (x,z) with physical Y. These signatures cannot encode six surface
patches or their competing faces. Keep Heightmap.Types semantics physical.
Implement PlanetSurfaceQuery / PlanetSurfaceIndex with:
- physical BlockPos, selected canonical face and optional traversal chart;
- surface shell radius/elevation and local normal/support;
- multiple candidates per physical XZ column where necessary;
- deterministic tie/edge/corner handling and unloaded-chunk bounds;
- separate queries for topmost physical Y, local exposed face and traversable
  terrain. Do not conflate air exposure with navigable local floor.

PlanetSurfaceIndex is a derived index/cache, not a replacement for Minecraft
heightmap serialization. Invalidation must follow physical block changes/chunk
load/save and may be deferred for generated immutable terrain when safe.

### 4.2 Biome temperature and freeze

Biome.getHeightAdjustedTemperature explicitly checks physical y > 80 and
injects x/z temperature noise; using this unchanged makes temperatures differ
arbitrarily on opposite faces at equal Planet elevation. Biome.shouldFreeze and
shouldSnow also include world build-height and local-neighbor (e.g. water
west/east/north/south) assumptions.

Introduce PlanetClimateQuery at a physical position:
selected registry Biome remains unchanged; Planet elevation/altitude drives
its temperature lapse adjustment, while environmental sampling/noise is in
PlanetGenerationSpace. Preserve biome temperature modifiers, downfall and
NeoForge climate changes; avoid mutating shared Biome or its physical-position
temperature cache. Actual modded climate hooks require targeted integration.

### 4.3 Server precipitation and freeze

ServerLevel.tickPrecipitation gets MOTION_BLOCKING heightmap at XZ, steps
below, then Biome.shouldFreeze/shouldSnow and block.handlePrecipitation.
SnowAndFreezeFeature repeats this during world generation with a 16x16 XZ loop
and Direction.DOWN. These are different entry paths and must consume the SAME
PlanetPrecipitationQuery / climate decisions. Weather effect callbacks on blocks
should receive actual physical block positions and unchanged biome/NeoForge hooks.
Rain/snow particle render and rain impact/audio (LevelRenderer) are a THIRD,
client-owned XZ/heightmap family, not automatically fixed by server deposition.

Proposed native Planet weather: precipitation approaches exposed surfaces along
their local DOWN, with visual streaks, impact volumes and deposited snow
resolved from the same face and weather exposure. Do not emit through the stone
interior or independently rain on the far side of a solid shell.

### 4.4 Lightning

ServerLevel.tickChunk chooses random chunk XZ, obtains a heightmap-based strike
target and searches for lightning rods using a world-Y height criterion;
findLightningTargetAround searches a vertical AABB for entities. All three
selection layers must be adapted together: weather-exposed candidate patch,
rod/POI priority and physical entity/rod targeting. Preserve world lightning
event hooks, physical strike entities and no-duplicate event guarantees.

### 4.5 Sky and skylight are NOT cosmetic

SkyLightEngine.propagateLightSources uses ChunkSkyLightSources and physical
columns/sections, while SkyLightSectionStorage walks UP on missing data.
Rotating client sky alone will leave rotated and underside faces dark,
impacting survival, plants and hostile-mob spawning. Reinterpreting Heightmap
globally is unsafe.

Product target for a six-face survival Planet: six-face outward sky visibility
and consistent natural exposure. This REQUIRES an explicit Planet sky/light
source integration or equivalent proven engine extension; whether the current
vanilla skylight storage/propagation can be reused with radial injection is
an implementation research gate, NOT an established solution. Keep world-space
block-light propagation physical. Separate lighting propagation, exposure
predicates, sun/time, cloud rendering and atmospheric visuals.

Explicit policy still to settle: physically directional sun vs ambient radial
daylight; cloud geometry; whether weather is global synchronized or face-local;
and how other celestial dimensions behave. Do not silently claim the
entire light engine has been solved in R6.

## 5. Worldgen: current status and required composition

### 5.1 Actual Planet 2.0 runtime today

PlanetChunkGenerator:
- extends ChunkGenerator and serializes BiomeSource plus a NoiseGeneratorSettings holder;
- fillFromNoise writes ONLY an R=48 test cube around (0,128,0) using
  PlanetInitialTerrain (one bedrock core, stone, grass on cube shell, air outside);
- buildSurface, applyCarvers and spawnOriginalMobs do nothing;
- applyBiomeDecoration is explicitly disabled until adapted;
- getBaseHeight/getBaseColumn return a global-Y top-of-cube model;
- getSeaLevel returns CORE_Y=128, which is an ordinary global Y number;
- PlanetGenerationSpace exists but is not consumed by fillFromNoise.

The 1.21.1 ChunkGenerator.createBiomes default calls
chunk.fillBiomesFromNoise(biomeSource, randomState.sampler()) at PHYSICAL
quart coordinates, while NoiseBasedChunkGenerator uses a NoiseChunk and cached
climate sampler. Simply storing a pluggable BiomeSource in the codec does NOT
make biome climate field seam-correct. Need a generation-space BiomeResolver /
Climate.Sampler adapter with exact quart-coordinate/seed semantics and
blending/retrogen decisions. Do not assume a given modded BiomeSource tolerates
arbitrary sampling coordinates or a vanilla-vs-Planet NoiseRouter substitution.

### 5.2 Generator stage contract

Future PlanetChunkGenerator remains the registered codec/entrypoint and owns
the generation execution plan, while semantic algorithms remain in pure helpers:

1. Generation-space climate / biome sampling (registries and modifiers intact).
2. Base density + macro relief -> physical block samples, chunk-correct writes.
3. Constant sea SHELL and local coast intersection, not a global Y sea plane.
4. Surface material rules + local-up support; separate from runtime growth.
5. Cave/ore/field WRAP sampling with one seed and continuous coordinates.
6. Hydrology derived from base terrain/drainage -> bounded local carving,
   piecewise-constant river/lake shell levels; rapids/waterfalls where needed.
7. Placed features / decoration via ordinary biome selection and feature order,
   but Planet-aware placement contexts for incompatible local-axis modifiers.
8. Rigid structures via Phase-9 structure policy and reference integration.
9. Chunk-generation initial mob population with Planet surface candidates.

Preserve Minecraft / NeoForge registry-based BiomeSource, PlacedFeature,
ConfiguredFeature, StructureSet, BiomeModifier, spawn lists and codecs.
Data-driven PlanetTerrainProfile for Normal/Large Biomes/Amplified; larger biome
scale is climate sampling, Amplified biases relief/erosion/peaks, neither
replaces the Planet generator.

### 5.3 IMPORTANT: continuous fields != seam-safe discrete features

PlanetGenerationSpace maps physical point p to normalize(p)*maxAbs(p) about core.
It is continuous and face-independent, but its derivative can change at a maxAbs
tie plane; C0 continuity alone does NOT prove seamless slopes, gradients,
surface normals, density discretization or decoration.

Scalar density, humidity, biome climate and cave noise can be sampled at that
continuous position. Vanilla feature placement is DIFFERENT: HeightmapPlacement,
HeightRangePlacement, SurfaceRelativeThresholdFilter and EnvironmentScanPlacement
expect world-XZ/world-Y semantics; TreeFeature and many others iterate an
integer local vertical tree/block pattern. Rounding an arbitrary inverse
3D coordinate transform to BlockPos is not a reversible lattice isomorphism:
rounding can duplicate or skip cells, change feature topology and RNG.

Therefore separate feature policies:
- FIELD_WRAP: one continuous value per real physical cell (terrain/caves/ores
  when realized as per-cell fields);
- LOCAL_CHART: a local orientation at an origin, with seam-safe path/volume
  validation, actual physical writes and a deterministic block owner;
- RIGID_AVOID_EDGE: ordinary rigid vanilla/modded feature, bounded region
  entirely within one frame; reject before side effects;
- INTEGRATION_REQUIRED: unknown modded world-XZ/global-Y algorithm with no safe
  generic transformation.

Placement modifier classification must be explicit; NOT all PlacedFeatures can
be transparently redirected using the existing boolean WRAP/AVOID_EDGE enum.
Extend that policy via a separate typed feature plan, do not change the
already-used general field/structure enum without migration.

### 5.4 Height, sea, worlds and bounds

getSeaLevel() is a global-Y integer contract. Constant Planet sea SHELL is a
separate radial scalar and must NOT be misrepresented by returning the radius
or by silently interpreting CORE_Y as equivalent. Aquifer, surface rules and
other sea-level consumers require Planet adapters.

All world chunks, structure references, lighting and saved Blocks remain
physical X/Y/Z. 1.21.1 chunk sections and dimension minY/maxY are finite;
an endless radial shell in all six directions is impossible with standard
finite vertical build bounds without a separate major engine architecture.
Before enabling outward unlimited terrain, decide finite playable Planet
envelope vs opt-in height expansion and explicitly handle ±Y caps. Test all
six faces for consistent generation limits. Do not promise infinite generation.

### 5.5 Deterministic stage invariants

- Same world seed/settings/physical cell -> identical biome, density and
  block result regardless of chunk generation/neighbor load order.
- Same WRAP value at both sides of a seam without duplicated features.
- Gradients/normal discontinuity monitored at 12 cube edges and 8 corners;
  no deterministic ridges produced only by chart/tie selection.
- Stable feature/structure placement with physical chunk ownership and
  bounded cross-chunk writes; never generate per face independently.
- No global state or mutable random shared across concurrent fillFromNoise.
- Vanilla and NeoForge feature modifier ordering preserved where possible.
- Separate chunk-generation regression tests for existing saved worlds.

## 6. Structures and rigid topology

### 6.1 ChunkGenerator / StructureStart lifecycle

ChunkGenerator.createStructures works with registered StructureSets and
StructurePlacement, invokes Structure.generate and persists StructureStart.
StructureStart caches the full bounding box, with pieces possibly extending
into several chunks; ChunkGenerator.createReferences tracks starts across
nearby physical chunks. ChunkGenerator.applyBiomeDecoration later invokes
StructureStart.placeInChunk BEFORE placed features at matching stages.

Correct Phase-9 placement policy:
1. Origin clearance can cheaply reject obvious seam overlap but is NOT final.
2. Validate the finished StructureStart with the COMPLETE adjusted bounding
   box and a configurable clearance/tolerance. Every box corner must lie
   strictly inside the same face's open dominance region with margin; use a
   conservative inflated box when processors expand beyond the original bounds.
3. Reject an unsafe start before it is registered or referenced and before
   any blocks are written; do not hide invalid structures after placement.
4. Preserve structure selection priority, seed/frequency, structure biome
   predicates and modded StructureSet rules. Define how a rejected candidate
   affects retries in a deterministic and documented way.
5. Preserve chunk references and locate/map lookup invariants; do not leave
   orphan starts, duplicate references or false positives.

Structure placement is based on PHYSICAL ChunkPos and seeded by physical chunk
indices; a mapping to six virtual face chunks would corrupt distribution and
references. Rigid box does NOT bend through a seam. Generated terrain's
structure adaptation (beardifier/jigsaw support) is Phase 8+9 integration,
not just a bbox filter.

### 6.2 Heightmap processors and jigsaw

GravityProcessor.processBlock obtains physical getHeight(X,Z) + offset, then
adds original local Y. JigsawPlacement can call getFirstFreeHeight(X,Z)
for template projection. They require a PlanetStructureSurfaceAdapter with
a clearly selected local frame; do not globally change getHeight.
RandomSpreadStructurePlacement still uses physical 2D ChunkPos grid.
Preserve template rotation/mirror, processor lists, loot/entity NBT, pool and
spawn rules, structure biome tags. Unrecognized modded processors are
integration-required unless a tested surface query boundary is enough.

### 6.3 Runtime portal is a distinct family

PortalShape.calculateBottomLeft walks physical below, derives width from a
horizontal axis/rightDir, and checks an UP-extended rigid rectangular frame.
A general local-support adapter cannot repair this algorithm.

Future PlanetRigidPlaneQuery supplies a frame-aligned local UP and horizontal
axis in PHYSICAL BlockPos, checks a full bounded rectangle in ONE canonical
frame, and disallows seam-crossing portal activation by default. The portal
surface stays physically planar and solid world blocks stay physical.
Activation/sustain/completion and destination/transit are separate test cases;
portal entity exit position, momentum and facing integrate with Phase 7B.2.
Nether/End portal and gateways have different construction/travel paths:
do not claim all portals from just Nether PortalShape acceptance.

## 7. Compatibility and public APIs

### 7.1 NeoForge / datapack

NeoForge 1.21.1 BiomeModifiers affect features, mobs, carvers and climate.
Compatible Planet generation must consume modified Biome generation settings,
modded BiomeSources, PlacedFeatures, StructureSets and configured spawns, not
hard-code biome names. Respect feature order/cycles and registry lookup.
TerraBlender, Biomes O' Plenty and Oh The Biomes We've Gone are acceptance
TARGETS, not currently verified compatible.

Compatibility classes:
A — standardized biome/capability/model/fluids event boundaries after adapter;
B — vanilla helper delegation; C — direct world-axis mod code requiring integration;
D — external moving coordinates requiring frame composition.
No global BlockPos, Direction, Heightmap or Vec3 replacement.
Expose PlanetSurfaceQuery, ClimateQuery, local feature frame and rigid safety
policy through a versioned opt-in integration API; public PlanetFrameApi remains
the basis. External mods can opt out of worldgen features safely and visibly.

### 7.2 Persistence

Present PlanetWorldSettings CORE/RADIUS/MIN_Y/HEIGHT are static constants, not
a complete per-world serialized profile. New Planet generator settings must be
codec/data-driven: format version, core, radius, seed-affecting profile inputs,
sea-shell level, climate scale, macro field, hydrology algorithm version,
structure policy defaults. Persist/generated-chunk compatibility must be
designed BEFORE replacing the test cube.

Store generated-version fingerprints by world/region as appropriate; do not
silently regenerate existing populated chunks with new noise/biome semantics.
Explicit migration, warning/compatibility-mode or a new world is required for
non-reconstructible terrain changes. Preserve vanilla registry/data fixer
handling and do not serialize arbitrary JVM object implementation names.
Saved Spawn/Bed/Portal positions remain physical.

### 7.3 Portability / risk ledger

High-risk version-specific adapters:
- ChunkGenerator lifecycle, createBiomes/NoiseBased pipeline and codec;
- biome resolver/climate sampler, surface/noise/PlacedFeature ordering;
- NaturalSpawner candidate selection, SpawnPlacements and NeoForge events;
- ServerLevel weather/lightning, LevelRenderer weather particles;
- sky light data structure and source injection;
- StructureStart/StructurePlacement/Jigsaw/GravityProcessor;
- PortalShape and dimension transfer;
- saved-format/registry/datafix lifecycle.

Keep Planet field math, surface candidate geometry, bbox predicates,
river/drainage and spawn sampling policy independent of these exact method
bodies. Mixins should be argument/result adapters and preserve non-Planet
pass-through. Prefer explicit integration to ordinal injections and whole
vanilla method copies.

## 8. Mechanism acceptance matrix (future runtime, NOT performed in R6)

| Batch | Deterministic tests | One manual integration acceptance |
| --- | --- | --- |
| 7B spawn | six face candidates, edges/corners, physical invalidation, distributions, mob caps | hostile/passive/underwater on all faces; spawn egg, spawner, modded predicate; world/bed/anchor respawn |
| 7B environment | equal-shell climate, rain/snow deposition, weather exposure, source/visual agreement | rain/snow/lightning on X/Y/Z ± surfaces; rods; sky-visible cave and underside; dusk/night and modded weather |
| 8 worldgen | mapping roundtrip/C0 and gradient, seed+chunk order, macro/dome, shore shell, drainage, cross-chunk writes, finite Y bounds | all-face biome, coast/ocean/river/cave continuity; no steps across seams; Normal/Large/Amplified |
| 8 features | placement modifier families, RNG, edges, reject safety, no duplicate ore/trees | biome modifier + trees/ores/carvers on +Y/-Y/±X/±Z; modded feature fallback recorded |
| 9 structures | complete bbox and clearance, reference stability, retry/seed, processors, locate | village + jigsaw + gravity processor on several faces; rigid avoids seams; modded structure |
| 9 portals | local plane invariants, face crossing rejection, entity relative position | Nether frame/activation on rotated faces; transition velocity/yaw; End/gateway separately |
| 10 compatibility | API non-Planet bypass, codecs, migration, allocation budgets | TerraBlender/BOP/BYG, datapack feature, capability stress, Create-like local neighbor, modded renderer/fluid |

All relevant rows require vanilla POS_Y baseline, six face checks, seam and
exact-corner cases where the subsystem can encounter them. Reuse one
subsystem-level manual matrix, not one approval per source class.

## 9. Performance and failure budget

Hot paths: per-block density/surface queries, heightmap callbacks, natural
spawn attempts, rain visuals and lightning exposure, lighting propagation and
structure bbox placement. Cache immutable per-seed noise and chunk-local
generation data, avoid per-cell allocs and repeated biome lookups; invalidate
mutable surface/light caches on physical world edits. Bound structure probes,
feature retries and local scans; run chunk generator RNG + mutable state per
task; do not hold Level/world objects in static caches. Instrument time per
chunk/stage, GC/allocations, spawn attempts, weather render CPU/GPU work,
light update queue and structure retries. A frame-local cache must never leak
a face assumption across concurrent chunks.

## 10. Decisions / blockers carried to implementation

Decided:
1. Six faces are ONE Planet world and one continuous WRAP field, NOT six chunks.
2. Vanilla Heightmap stays PHYSICAL; PlanetSurfaceQuery is separate.
3. Local-ground spawn semantics and explicit candidate selection; vanilla
   spawn rules/caps remain authoritative.
4. Constant SEA SHELL, no global-Y sea substitution; terrain -> drainage ->
   bounded river incision and piecewise-flat river reaches.
5. Rigid generated structures and portals avoid/break off at seam, never bend.
6. Biomes/feature catalogs stay data driven; compatibility is continuous.
7. Current runtime remains untouched until final R1-R6 completeness sweep.

Open product/engineering decisions (not falsified as implemented):
A. finite reachable Planet envelope and ±Y world build limits;
B. radial skylight implementation feasibility, daylight model/cloud presentation;
C. weather uniform across faces or climate-local;
D. precise PlanetTerrainProfile codec and versioned migration boundary;
E. feature categorization/opt-in API for arbitrary external geometry algorithms;
F. guaranteed sample/seam gradient quality of current generation transform;
G. whether structures entirely underground can have different seam rules.
These belong to Phase 7B/8/9/10 design gates, not a reason for runtime hacks.

## 11. Phase handoff

- Phase 7B.3: candidate sampler + existing SpawnPlacements & caps.
- Phase 7B.4: surface/temperature/weather contract, separate sky-light-engine gate.
- Phase 8: biome sampler + terrain/sea-shell + features and hydrology.
- Phase 9: complete bbox policy + processors + rigid portal rectangles.
- Phase 10: standard extensions, mod opt-in interfaces, formats/performance.
- Final global completeness sweep is NEXT. Do not implement R6 runtime yet.
