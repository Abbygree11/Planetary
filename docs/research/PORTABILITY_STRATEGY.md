# Planetary cross-version portability strategy

Status: mandatory architectural constraint for Planetary 2.0 and later ports.

Current runtime target:
- Minecraft 1.21.1
- NeoForge 21.1.215
- Java 21

Goal:
newer Minecraft/NeoForge ports should primarily require replacing thin
integration adapters and re-validating changed vanilla mechanisms, not rewriting
Planetary's topology, frame math, local-gravity semantics or subsystem algorithms.

This document does NOT promise zero-effort ports. Minecraft internals change.
The target is to keep version churn localized and measurable.

---

## 1. Dependency direction

Preferred dependency direction:

    Planetary semantic/core logic
        <- Minecraft/NeoForge adaptation layer
            <- tiny Mixins / event hooks / platform entrypoints

Core/semantic code must not depend on the existence of a particular vanilla
method body when the concept can be expressed independently.

Examples of logic that should be version-stable:
- cube/planet face selection;
- local/world frame transforms;
- direction/vector/normal transforms;
- seam traversal;
- canonical BlockState semantic policy;
- local tangent graph concepts;
- shape rotation math;
- entity local-frame math;
- generic acceptance invariants.

Minecraft-specific adapters own:
- locating the current vanilla hook/call boundary;
- translating Minecraft types into Planet semantic inputs;
- translating Planet outputs back to Minecraft types;
- preserving version-specific NeoForge hooks.

---

## 2. Mixins must be thin version adapters

A mixin is not the preferred home for Planetary business logic.

Preferred shape:

    vanilla callback
        -> collect exact vanilla context
        -> Planet helper/service
        -> adapt args/result
        -> return to vanilla

Avoid:
- large copied vanilla methods;
- long switch tables embedded in mixins;
- subsystem algorithms written directly inside @Inject methods;
- many mixins duplicating the same frame math;
- dependence on local variable ordinals when a stable invocation/result boundary
  exists.

When a copied vanilla fragment is unavoidable:
- keep it as small as possible;
- isolate it in a clearly version-sensitive adapter/helper;
- document the exact source/version it mirrors;
- add vanilla-equivalence tests;
- treat it as a mandatory port-review hotspot.

---

## 3. Stable Planet APIs before vanilla hooks

Every substantial subsystem should expose a stable Planet-owned semantic API
before wiring it to Minecraft internals.

Examples:
- PlanetBlockFrameContext / PlanetBlockStep / PlanetBlockWalk;
- PlanetBlockSupportQuery;
- Planet placement-frame helpers;
- particle emitter/move semantic helpers;
- future rail graph/topology helpers;
- future fluid topology helpers;
- entity body-frame/collision helpers;
- sided capability frame adapters.

A port should be able to ask:

> Which vanilla hook now feeds this same Planet API?

rather than:

> How do we reimplement this whole feature for the new Minecraft version?

---

## 4. Keep Minecraft primitives physical

Do not globally redefine:
- BlockPos;
- Direction step vectors;
- Direction.Axis;
- raw Vec3;
- AABB;
- chunk coordinates.

This is important for both correctness and portability.

Global semantic monkey-patches become tightly coupled to huge portions of
Minecraft internals and are likely to break on every version.

Keep physical primitives stable; adapt semantic meaning at explicit boundaries.

---

## 5. Base-family / mechanism adapters reduce port surface

Prefer adapting a stable vanilla mechanism family rather than dozens of concrete
blocks.

Examples:
- BaseRailBlock + RailState rather than every rail block;
- FaceAttachedHorizontalDirectionalBlock rather than lever/button/etc separately;
- GrowingPlantBlock rather than each growing plant;
- FlowingFluid rather than water and lava independently;
- Entity.move semantic boundary rather than per-living-entity collision patches.

This improves both current completeness and future porting:
when vanilla changes a family, there is one adaptation point to re-audit.

---

## 6. Explicit version-sensitive layer

Version-sensitive code should be easy to find.

Preferred organization as the project grows:

    planetary/core or equivalent
        version-agnostic math/topology/semantic algorithms

    planetary/minecraft or equivalent
        Minecraft-facing semantic adapters

    planetary/platform/neoforge or equivalent
        NeoForge registration/capability/event integration

    planetary/mixin or equivalent
        thin injection hooks only

The current repository does not need a disruptive module split merely for
appearance. Refactor incrementally when subsystem boundaries justify it.

Rule:
do not spread version-specific vanilla implementation details through core
packages.

---

## 7. Avoid brittle private-internal dependencies when possible

Portability preference order:

1. public/protected stable API or standard NeoForge hook;
2. argument/result transformation around a stable call boundary;
3. base-family method injection;
4. accessor/invoker to private member;
5. local-variable/ordinal-dependent injection;
6. copied/reimplemented large vanilla method.

Lower items are allowed when necessary, but must be documented as port hotspots.

Runtime mapping failures already encountered in particle work are evidence that
private invokers/accessors can be less stable than small locally-owned helpers.

---

## 8. Preserve extension points

Do not "simplify" vanilla/NeoForge behavior by bypassing extension points.

Preserve where applicable:
- capabilities;
- model data / render types;
- codecs;
- biome/worldgen data-driven paths;
- block/entity hooks;
- fluid hooks;
- registry-driven behavior.

A port is easier when Planetary composes with supported platform APIs instead of
shadowing them.

---

## 9. Serialization and saved-data compatibility

Any Planetary-owned persistent format must be versioned.

Examples:
- world/preset configuration;
- custom saved data;
- future per-world Planet parameters;
- custom data components or serialized execution state.

Requirements:
- explicit schema/data version when Planetary owns the format;
- migrations for incompatible changes;
- never encode transient class names or mixin-specific details into saves;
- prefer Minecraft codecs/data components where they are stable platform
  boundaries.

Minecraft's own world upgrade remains Minecraft's responsibility; Planetary
must only migrate Planetary-owned data.

---

## 10. Tests must separate semantic invariants from version integration

### Version-stable tests

Pure tests should cover:
- all six face transforms;
- direction/vector round trips;
- seam traversal;
- canonical state semantics;
- graph/topology invariants;
- shape math;
- local movement math;
- +Y vanilla-equivalence where expressible without client runtime.

These should survive a Minecraft port with little or no change.

### Version-integration tests

Per-version tests cover:
- exact vanilla method wiring;
- mixin application;
- class hierarchy assumptions;
- NeoForge hooks;
- representative runtime families.

A port should fail loudly in this layer if Minecraft changed the hook.

---

## 11. Port procedure for a future Minecraft version

For each subsystem:

1. keep Planet core helpers unchanged initially;
2. diff/research the new vanilla mechanism against the last supported version;
3. identify whether semantics changed or only call structure/signatures changed;
4. replace/rework only the version adapter/mixin boundary first;
5. run version-stable tests;
6. run per-version integration tests;
7. run the subsystem acceptance matrix;
8. change core semantics only if Minecraft behavior or Planet requirements truly
   changed.

Do not start a port by mechanically updating every mixin until it compiles.

Compilation is not semantic compatibility.

---

## 12. Portability review is a gate for every new runtime mechanism

Before accepting a new architecture, ask:

- Is the frame/topology logic outside the mixin?
- Is the vanilla-specific detail localized?
- Could a new Minecraft version replace this hook without rewriting the helper?
- Did we copy a large vanilla method unnecessarily?
- Are we relying on a private field/local variable that can be avoided?
- Are extension points preserved?
- Are pure semantic tests independent of the exact vanilla implementation?
- Is any Planetary-owned serialized data versioned?

If the answer is poor, improve the boundary before expanding the mechanism to
more classes.

---

## 13. Compatibility with multiple supported Minecraft versions

Do not prematurely maintain several live Minecraft versions in one source set.

If/when simultaneous support becomes valuable:
- keep shared semantic/core code common;
- maintain small version/platform source sets or modules;
- avoid preprocessor-style conditionals scattered through runtime code;
- version-specific adapters implement the same Planet-owned contracts.

The immediate goal for 1.21.1 is to build those contracts cleanly so a future
port has a small diff surface.


## 14. R6 worldgen/environment portability hotspots

R6 research matrix:
docs/research/ENVIRONMENT_WORLDGEN_STRUCTURE_COMPAT_MATRIX_1_21_1.md

The following MUST be isolated as exact-version adapters when implementation
resumes on Minecraft 1.21.1 / NeoForge 21.1.215:
- ChunkGenerator.createBiomes, fillFromNoise, buildSurface, applyCarvers,
  applyBiomeDecoration, createStructures/createReferences and structure starts;
- BiomeResolver/Climate.Sampler, surface/placement modifiers and their RNG/
  getHeight Y semantics; BiomeModifier hooks are data/registry-owned;
- NaturalSpawner candidate sampling versus SpawnPlacements validity and mob caps;
- ServerLevel precipitation/lightning, LevelRenderer weather visuals and
  SkyLightEngine/SkyLightSectionStorage vertical light-source assumptions;
- StructureStart bounding-box registration, jigsaw placement, gravity processor,
  PortalShape plus dimension-transfer and persisted worldgen settings.

Keep Planet-native surface geometry, continuous field sampling, drainage,
whole-box seam predicates, and spawn candidate math independent of these
method signatures. Tests should cover exact six-face semantic math and
version-specific vanilla-equivalence separately. Codec/data versions and
old generated-world fingerprints require an explicit migration policy.

Not a portable generic solution: a worldgen view that globally redefines
BlockPos/Heightmap/Direction, or an arbitrary float-to-integer conversion of
modded feature placement. Minecraft's world height is finite even if the
Planet surface is defined by an outward shell; document the runtime/world
envelope instead of implying six infinite playable directions.


## 15. External coordinate mod integration precedents

External research:
docs/research/EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md
Cross-phase freeze:
docs/research/FINAL_COMPLETENESS_SWEEP_1_21_1.md

Real mod source confirms the interoperability cost of ambiguous coordinates:
GravityChanger exposes explicit getWorldVelocity/setWorldVelocity over a
local-stored entity delta; Immersive Portals adapts body eye offset, physical
velocity, directions and quaternion camera rotation as separate APIs.
Valkyrien Skies separates world-to-ship ray positions, hit locations,
particle directions/positions and motion, while documenting shipyard passenger
save positions and unloaded-world issues. Cubic Chunks uses a dedicated
3D cube provider instead of a fake ChunkGenerator height extension.

For Planet: document per-API frame, require finite/valid transformed bounds,
typed position/vector/normal/AABB/rotation composition, safe missing-mod
fallback and correct physical saved/network positions. Never promise
cross-seam block graph simulation with a fake portal seam; seamless rendering
does not imply fluid/redstone/AI adjacency. Performance smoke checks must
include transformed AABB and loaded/unloaded frame boundaries.

Historical mods target different Minecraft versions; their source can
support the design principles but cannot validate the current NeoForge
21.1.215 mixin integration or mod compatibility.


## 16. Sided-provider contract and foreign coordinates

Cross-mod audit: docs/research/ULTIMATE_CROSS_MOD_COMPATIBILITY_REVIEW_1_21_1.md.

NeoForge sided Direction is an API query context, not necessarily a local
Planet direction. Unknown receivers must retain PHYSICAL side; local
transformation must be an explicit receiving provider/family policy.
The generic current BlockCapabilityMixin assumes the latter universally,
which is a P0 compatibility risk (not a reproduced modded-machine failure).

Create ContraptionWorld, WorldEdit clipboards, AE2 virtual grid nodes and
external LOD samples carry coordinate provenance distinct from physical Level.
Do not globally convert BlockPos, Level, Direction or network graph APIs.
Expose optional typed transforms; preserve signed kinetic/port semantics,
BlockEntity NBT and cache lifecycle through specific adapters.


### Chosen 1.21.1 runtime adapter, acceptance pending

PlanetCapabilityAdapters wraps individual NeoForge IBlockCapabilityProvider
and ICapabilityProvider registrations that explicitly want canonical LOCAL.
This is safer than a block/capability-wide policy because BlockCapability
dispatches a LIST of independently registered providers for a Block.
The physical Planet BlockCapabilityMixin now leaves the query untouched;
legacy virtual-atlas alias handling remains a separate guarded path.
Unknown providers see original PHYSICAL Direction as before. See
docs/research/CAPABILITY_PROVIDER_CONTEXT_1_21_1.md for exact call flow.

PlanetCoordinateContext supports explicitly labelled physical vs foreign
block coordinates and only permits physical queries through its
PlanetFrameApi overloads. It does not invent a foreign-to-physical mapping.
No runtime verification is claimed until build/game acceptance.
