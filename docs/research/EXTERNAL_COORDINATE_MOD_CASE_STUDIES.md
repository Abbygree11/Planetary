# External spatial/coordinate Minecraft mods — case studies for Planetary

Status: EXTERNAL PRECEDENT RESEARCH COMPLETE, NOT implementation proof.
Date: 2026-10-08. Target for Planetary: Minecraft 1.21.1 / NeoForge 21.1.215.
All URLs below are public upstream documentation/source/issues; versions differ.
This matrix supplements R1-R6 and the final completeness sweep. It does NOT
grant universal mod compatibility or justify copying implementation wholesale.

## Method / evidence confidence

- HIGH: upstream source of a concrete mechanism or upstream project's own docs.
- MEDIUM: specific reproduced upstream bug report / issue or maintainer README;
  observed failure is evidence, not proof of a permanent unsolved root cause.
- HYPOTHESIS: Planet integration opportunity; must be tested on exact 1.21.1.

## Case 1 — GravityChanger: entity-local velocity is a compatibility contract

Repository: https://github.com/FugLord77/GravityChanger-1.21.1/tree/1.21
API source:
https://github.com/FugLord77/GravityChanger-1.21.1/blob/1.21/src/main/java/gravity_changer/api/GravityChangerAPI.java
Original design / future goal:
https://github.com/qouteall/GravityChanger

Observed (HIGH):
- getWorldVelocity converts the entity's stored local deltaMovement into
  physical velocity; setWorldVelocity converts physical velocity back.
- The API separately returns body-local eye offset and actual gravity direction.
- A distinct early-construction guard exists because gravity components may
  not be initialized when an entity's bounding box is first calculated.
- The upstream README independently contemplates world-coordinate deltaMovement
  storage to reduce debugging/maintenance complexity, although it can mean
  additional Minecraft interception.

Planet decision:
- R4 ALREADY decided physical deltaMovement as the future internal contract.
  Current Planet runtime is still LOCAL and requires an explicit migration;
  do not describe this as a new R6 discovery or already implemented.
- Provide named get/set physical velocity and body-local motion transforms.
- Test constructor-before-frame readiness, gravity change mid-tick, one-frame
  teleport/knockback/explosion/passenger/server packet invariants.
- Keep actual vanilla/world velocity physical at cross-mod boundaries.
No direct copy from Fabric component internals into NeoForge.

## Case 2 — Immersive Portals: composing two spatial transforms

Repository: https://github.com/iPortalTeam/ImmersivePortalsMod/tree/1.21
Direct gravity integration:
https://github.com/iPortalTeam/ImmersivePortalsMod/blob/1.21/src/main/java/qouteall/imm_ptl/core/compat/GravityChangerInterface.java
Official implementation notes:
https://github.com/iPortalTeam/ImmersivePortalsMod/wiki/Implementation-Details
World wrapping behavior/limitations:
https://github.com/iPortalTeam/ImmersivePortalsMod/wiki/Portals
Performance:
https://github.com/iPortalTeam/ImmersivePortalsMod/wiki/Config-Options

Observed (HIGH):
- GravityChangerInterface.Invoker is a no-mod fallback; OnGravityChangerPresent
  explicitly connects eye offsets, physical velocity get/set, local/world
  vectors, local/world directions, and extra camera quaternion rotation.
- Immersive Portals explains client visual teleport before frame rendering,
  not only server/entity tick teleport, and iterative corner teleport.
- Its multi-world solution adds packet dimension context and remote chunk/entity
  tracking; it does not magically alter Minecraft storage geometry.
- Documented virtual world wrapping/vertical connecting portals can give an
  illusion of extra space, but redstone, fluid and AI do not generally propagate
  through a portal connection; collision/render also have exceptional cases.
- Recursive portal rendering can amplify render/lighting/GC costs.

Planet decision:
- Maintain ONE physical BlockPos grid and real adjacent blocks across gravity
  seam. Do not replace Planet topology with hidden teleport/portal wrappers.
- Compose foreign-body/external-local <-> PHYSICAL <-> Planet-body, with
  transform kind explicit: position vs direction/velocity vs normal vs quaternion
  vs bounds. Where applicable, handle successive seam/chart transitions.
- Keep camera smooth interpolation separate from authoritative tick/frame.
- Public API must fail safely when external gravity/physics module is absent.
- Add *semantic* cross-seam redstone/fluid/AI acceptance; portal visuals alone
  do not close any of these behaviors.

## Case 3 — Valkyrien Skies 2: physical/shipyard split, broadphase and saves

Repository:
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/tree/1.20.1/main
Shipyard entity engineering notes:
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/blob/1.20.1/main/common/src/main/java/org/valkyrienskies/mod/mixin/feature/shipyard_entities/FEATURE.md
Raycast:
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/blob/1.20.1/main/common/src/main/kotlin/org/valkyrienskies/mod/common/world/RaycastUtils.kt
Particle transforms:
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/blob/1.20.1/main/common/src/main/java/org/valkyrienskies/mod/mixin/feature/transform_particles/MixinParticle.java
Collision integration:
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/blob/1.20.1/main/common/src/main/java/org/valkyrienskies/mod/mixin/feature/entity_collision/MixinEntity.java
Representative issues:
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/issues/1563
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/issues/229
https://github.com/ValkyrienSkies/Valkyrien-Skies-2/issues/1604

Observed (HIGH source, MEDIUM issue reports):
- RaycastUtils transforms physical ray start/end into ship-local, tests hits,
  converts hit location back into physical world, and picks nearest physical
  hit; one opaque global 'rotate coordinates' call is not enough.
- Particle mixin transforms PARTICLE POSITION with transformPosition and
  VELOCITY with transformDirection, adding ship velocity separately.
- Entity collision adapts ship collision input and the vanilla movement/collision
  result; collision response is not just rotated gravity.
- Shipyard FEATURE.md lists explicit handling for entity positions leaving
  shipyard, passenger saved-position pitfalls, projectiles, entity sections,
  renderer visibility and entity lookup. This is valuable written root-cause
  evidence; these patches do not imply current versions are bug-free.
- Issue #1563 reports wrong/unloaded shipyard coordinate teleports;
  #229 reports Botania projectile stuck in shipyard coords;
  #1604 reports invalid/huge collision boxes causing performance problems.
  Reported fixes/workarounds must not be treated as confirmed merged upstream.

Planet decision:
- Position, ray direction, hit normal, velocity, AABB are separate types.
- Validate transformed bounds finite, ordered, bounded and do not run unbounded
  entity scans on NaN/Infinity or enormous AABBs. Never silently use guessed
  positions as saved player coordinates.
- Keep physical packet/persistence positions; deterministic on-demand frame
  resolver for loaded/unloaded chunks; safe entity spawn/teleport behavior.
- Add save/reload while riding, unloaded-chunk teleport and transformed entity
  section queries to Phase 7/7B/10 integration smoke matrix.
- Full VS compat remains an EXPLICIT later module, not automatic.

## Case 4 — Cubic Chunks: changing world storage is engine-wide

Older implementation: https://github.com/OpenCubicChunks/CubicChunks/tree/MC_1.12
Cube provider:
https://github.com/OpenCubicChunks/CubicChunks/blob/MC_1.12/CubicChunksAPI/src/main/java/io/github/opencubicchunks/cubicchunks/api/world/ICubeProvider.java
Heightmap extension:
https://github.com/OpenCubicChunks/CubicChunks/blob/MC_1.12/CubicChunksAPI/src/main/java/io/github/opencubicchunks/cubicchunks/api/world/IHeightMap.java
Modern rewrite (archived, branch 1.18):
https://github.com/OpenCubicChunks/CubicChunks2

Observed (HIGH):
- Adds a three-coordinate cube provider and separates loaded cube from
  vanilla-style XZ column, with a dedicated height map API and generation API.
- Changes world-storage and vertical loading assumptions, instead of pretending
  extending ChunkGenerator or dimension config alone can remove world height.
- A 1.18 rewrite repository was archived in 2024; this is not evidence for
  a ready-to-use NeoForge 1.21.1 3D storage backend.

Planet decision:
- Short-/mid-term: preserve normal physical chunk storage and define a FINITE
  vertical playable envelope. Planet world generation may be unbounded
  conceptually in X/Z, but six unlimited physical directions cannot be met by
  normal finite Y dimension storage.
- Never promise endless ±Y shells. Any future limitless physical Y mode
  needs independent research/major port to chunk storage, lighting, network,
  heightmaps, persistence, world border, structures and mod compatibility.
- Heightmaps remain vanilla physical column summaries, with separate Planet
  surface index for local face-aware queries.

## Case 5 — Up And Down And All Around: axis gravity compatibility failures

Old project: https://github.com/Mysteryem/Up_And_Down_And_All_Around
Release/compat notes:
https://www.curseforge.com/minecraft/mc-mods/up-and-down-and-all-around

Observed (maintainer notes HIGH for its own old releases):
- Cardinal-direction gravity, gravity generators and changing equipment.
- Known Elytra under rotated gravity and external cosmetic rendering failures;
  listed incompatibilities with some mods that transform runtime classes.
- Old versions and obsolete modding APIs; nothing establishes NeoForge 1.21.1
  compatibility or direct reusability.

Planet decision:
- Reaffirm Phase 7.2 elytra + render layer test ownership.
- Test injection/mod composition and non-Planet pass-through, not only single
  mod running in isolation. Avoid class-wide overrides as 'compat solution'.

## Case 6 — Starminer / Simply Starminer: matching fantasy, limited evidence

Historic cube/sphere star gravity:
https://mcmodkit.com/mod/StarMiner
Modern-inspired mod:
https://github.com/Fusion-Flux/Simply-Starminer

Observed:
- Historic Starminer describes gravity-core generated spheres and cubes as
  gameplay features. Modern alpha-inspired rewrite contains localized gravity
  cores/plates/anchors.
- Public descriptions alone do not establish exact source algorithm,
  seamless six-face topology, 1.21.1 compatibility or resolved cross-face
  redstone/fluid/AI/worldgen.

Planet decision:
- Use as product comparison only. DO NOT treat gravity field resemblance
  as proof of a generic engine architecture; no R1-R6 decisions change.

## Cross-project failure pattern and verdict

| Failure | Seen in | Planet implication |
| --- | --- | --- |
| Local velocity leaks to physical API | GravityChanger/Immersive Portals explicit adapter | R4 physical deltaMovement migration |
| Entry/exit/corner camera and coordinate discontinuity | Immersive Portals | Phase 7B+11 separate authority/render frames |
| Virtual border lacks real physical neighbors | Immersive Portals wrapping docs | Preserve physical block adjacency |
| Transforming only positions loses directions and normals | VS2 source and IP compat | Typed transforms + cross-frame API |
| Wrong/stale coordinate saved across transforms | VS2 shipyard entity notes | physical save and packet invariants |
| Bad transformed AABB causes huge physics lookup | VS2 issue | finite and bounded AABB guards + performance |
| Vertical height implies storage rewrite | Cubic Chunks | separate finite-envelope vs 3D backend decision |
| Mod overrides differ from vanilla behavior | Up & Down | compatibility matrix and extension points |
| Rotated gravity not automatically worldgen/block-local | all above | continue Planetary's distinct R1-R6 architecture |

## Specific changes to final completeness sweep

1. Annotate R4 physical-velocity migration as CRITICAL PRECONDITION for
   external forces, portals, passenger sync and server teleports.
2. Ensure transform APIs name type and frame; add phys position/vectors/normals,
   AABB invariants, orientation/frame composition, and safe non-Planet fallback.
3. Include engine-created entities before frame/component initialization.
4. Expand Phase 7/7B test families for cross-frame save/load while riding,
   unloaded destination teleports, projectiles and bounds, client/server motion.
5. Prove seam block adjacency, fluid, redstone and AI without portal-based
   discontinuity.
6. Record finite Y envelope as explicit user-visible project limitation;
   do not slip a Cubic Chunks-style storage rewrite into Phase 8.
7. Do not mark third-party integration PASS from documentation alone.
8. Re-review phase 11 camera transition against distinct visual and
   authoritative frames with quaternion composition.

No runtime changes or gameplay/compile verification performed in this document.
