# Client geometry / rendering / particle matrix — Minecraft 1.21.1

Status: R2 COMPLETE research result for Planetary 2.0.
Runtime target researched: Minecraft 1.21.1 / NeoForge 21.1.215.

This document is the concrete Phase-3/Phase-4 family matrix produced by global
research batch R2.

It complements:
- `SHAPES_1_21_1.md`
- `RENDERING_1_21_1.md`
- `PARTICLES_1_21_1.md`
- `PARTICLE_MATRIX_1_21_1.md`
- `GRAVITY_MECHANISM_MAP_1_21_1.md`

R2 is architecture/research only. Existing partial runtime implementation is
evidence; it is not automatically accepted unless the user already manually
accepted that behavior.

---

## 1. R2 source coverage

R2 audited the following 1.21.1 client/world mechanisms:

Geometry/state:
- BlockBehaviour / BlockStateBase shape APIs;
- cached collision/occlusion/support state;
- Block.shouldRenderFace;
- BlockBehaviour OffsetType XZ/XYZ.

Static rendering:
- BlockRenderDispatcher;
- ModelBlockRenderer;
- BakedModel / BakedQuad path;
- AO adjacency/remap tables;
- flat lighting;
- breaking overlay path;
- LevelRenderer light lookup;
- world directional shade.

Custom/block-entity rendering:
- all 28 classes under client/renderer/blockentity were inventoried;
- representative complete transform flows were inspected for:
  Bed, Chest, Sign, HangingSign, Banner, Skull, Shulker, Bell, Lectern,
  PistonHead, Campfire, BrushableBlock, DecoratedPot, Conduit, Beacon,
  EnchantTable, Spawner, StructureBlock, EndPortal/Gateway, TrialSpawner,
  Vault.

Moving/entity render boundary:
- FallingBlockRenderer;
- MinecartRenderer;
- BoatRenderer;
- TNT renderer;
- ItemFrameRenderer;
- ownership relationship to entity/body mechanics.

Particles:
- all 74 Java classes under net/minecraft/client/particle;
- ParticleEngine provider registration;
- base Particle tick/move;
- custom tick/move/trajectory/render subclasses;
- nested emitters;
- block animateTick families;
- ParticleUtils call families;
- LevelRenderer world-event/weather emitters;
- entity/tracking/status source families;
- portal/fluid integration gates.

Compatibility:
- standard NeoForge BakedModel/ModelData/RenderType pipeline;
- BER/entity/custom renderer bypass paths;
- accelerated/instanced renderer boundary.

---

## 2. Phase 3A — shape state-space boundary

### 2.1 Physical outer shape queries

The following world-position-aware queries are PHYSICAL geometry consumers:

- getShape / outline;
- getCollisionShape;
- getVisualShape;
- getInteractionShape.

Collision, selection and raycast consumers expect actual world-space VoxelShape.

Planet rule:
canonical local BlockState geometry is rotated exactly once at the OUTERMOST
bound-Level query.

Existing PlanetBlockShapeRuntime architecture is correct:
- nested query depth prevents double rotation;
- cached canonical state remains canonical;
- Shapes.block and empty identity fast paths remain intact.

### 2.2 Canonical support and occlusion state

The following must not be blindly converted to position-specific physical state
inside BlockState cache:

- getBlockSupportShape;
- getOcclusionShape;
- cached collision/occlusion booleans.

Support logic consumes canonical local Direction and evaluates via the owning
Planet support adapter.

Culling separately maps source/target physical sides into source/target
canonical faces.

### 2.3 Face occlusion

Block.shouldRenderFace receives:
- physical source position;
- physical neighbor position;
- physical neighbor direction.

But state semantics are canonical local.

Correct frame-complete culling key needs:
- source state;
- target state;
- source canonical side;
- target canonical side.

At a seam:
target side is NOT assumed to be sourceSide.opposite() in canonical space.

Existing PlanetBlockRenderCulling architecture is therefore the correct
boundary.

### 2.4 Entity floor/stand semantics are NOT Phase 3

BlockStateBase.entityCanStandOnFace uses a Direction plus collision shape.

Collision shape is physical after the shape boundary, but the meaning of
"entity floor/local UP" belongs to the entity/body/navigation phases.

Do not solve local floor semantics inside shape rendering.

---

## 3. Phase 3B — static baked model pipeline

### 3.1 Stable standard pipeline boundary

NeoForge/vanilla path:

    BlockRenderDispatcher
        -> ModelBlockRenderer.tesselateBlock
            -> BakedModel.getQuads
            -> BakedQuad geometry/direction
            -> AO/flat light/shade

The stable Planet boundary is an oriented VIEW over the BakedModel passed into
ModelBlockRenderer.

Do not mutate registered baked models.

### 3.2 Physical side -> canonical model side

ModelBlockRenderer iterates PHYSICAL Direction values.

Planet wrapper:
1. physical requested render side;
2. convert through source block frame to canonical local side;
3. query original model with canonical side;
4. transform returned canonical-local quad geometry to physical world.

For side=null:
- query original null side;
- transform unculled quads.

### 3.3 Quad data that must transform

Required:
- vertex positions;
- packed normals;
- BakedQuad.direction.

Required to preserve:
- UV;
- tint index;
- sprite;
- shade flag;
- emissive metadata;
- ModelData;
- RenderType;
- random seed ordering.

Existing NeoForge QuadTransformers + final physical BakedQuad.direction is a
good extension-friendly boundary.

### 3.4 AO/light does NOT need a second local-frame algorithm

R2 verified ModelBlockRenderer AO in detail.

AmbientOcclusionFace receives the BakedQuad.direction and uses it to choose:
- physical neighbor position;
- physical corner directions;
- physical diagonal samples;
- vertex remap.

Flat lighting also uses:

    pos.relative(bakedQuad.direction)

after geometry classification.

Therefore, once transformed quads have the correct PHYSICAL direction,
AO/light adjacency already follows physical geometry.

Do NOT rotate AO neighbor tables separately.

Doing so would double-rotate the neighborhood.

### 3.5 Breaking overlay uses the same static pipeline

BlockRenderDispatcher.renderBreakingTexture also calls
ModelBlockRenderer.tesselateBlock.

Therefore:
- the oriented BakedModel boundary should cover block breaking overlay;
- no independent "breaking geometry rotation" architecture is required.

A separate bug would only exist if a mod/custom renderer bypasses this path.

### 3.6 Directional shade is an environment policy, not geometry

ClientLevel.getShade hard-codes physical world Direction values:

- UP strongest;
- DOWN darkest;
- NORTH/SOUTH one level;
- EAST/WEST another.

This is global directional lighting convention.

Question:
should Planet blocks visually shade according to:
1. physical world direction; or
2. local gravity UP/DOWN/tangents?

R2 does NOT choose this automatically.

Ownership:
- Phase 3 exposes correct physical quad directions;
- Phase 7B/R6 environment policy decides whether getShade itself should become
  local-gravity-aware.

Do not secretly reinterpret global lighting inside BakedModel rotation.

---

## 4. Standard block random offset is its own semantic layer

BlockBehaviour.OffsetType XZ/XYZ is NOT model geometry itself.

Vanilla standard offset:
- seed uses physical BlockPos X/Z only;
- XZ outputs physical X/Z displacement;
- XYZ adds a physical Y displacement.

This assumption breaks local-vertical continuity on rotated faces.

Observed example:
pointed-dripstone local vertical chain changes physical X or Z, so raw vanilla
seed produces a different offset per segment.

### R2 policy

For STANDARD vanilla OffsetType semantics:
- author/seed the offset in canonical local coordinates;
- then transform the resulting local offset into physical world space.

This preserves:
- stable offset while moving along local Y for XZ-style offsets;
- local tangent meaning.

However:
do NOT globally assume every modded/custom OffsetFunction has local semantics.

Custom offset callbacks may inspect:
- BlockGetter;
- physical BlockPos;
- external world data.

Therefore:
- standard OffsetType can have a shared Planet adapter;
- arbitrary custom OffsetFunction is a Phase-10 compatibility boundary unless
  its semantics are declared.

Current pointed-dripstone narrow implementation is evidence for the rule, not
the final generic architecture.

---

## 5. Phase 3C — BlockEntityRenderer families

A global transform around BlockEntityRenderDispatcher is UNSAFE.

BERs divide into multiple semantic families.

### 5.1 Family C1 — rigid block-local renderers

These mostly:
- read canonical BlockState;
- translate around block-local center;
- rotate around vanilla X/Y/Z as MODEL-LOCAL axes;
- render rigid local content.

Strong candidates for a common root local->world frame wrapper:

- BedRenderer;
- ChestRenderer;
- SignRenderer;
- HangingSignRenderer;
- BannerRenderer;
- SkullBlockRenderer;
- ShulkerBoxRenderer;
- LecternRenderer;
- CampfireRenderer;
- DecoratedPotRenderer;
- EnchantTableRenderer;
- Vault item display;
- Spawner/TrialSpawner internal display, subject to displayed entity policy.

Preferred architecture:

    dispatcher supplies ordinary block-local PoseStack
        -> Planet local->world block-center frame
            -> vanilla renderer executes in canonical-local model space

Then renderer-owned Y rotations can remain vanilla-local rotations.

Existing BedRenderer adapter is the first proof of this boundary.

### 5.2 Family C2 — local renderer with runtime directional data

Examples:
- BellRenderer clickDirection;
- BrushableBlockRenderer hitDirection;
- some sign/banner attachment variants.

These need classification of the DIRECTION DATA itself.

Important distinction:
- canonical BlockState FACING should be interpreted local;
- an interaction hit Direction may already be physical.

Do not root-transform a physical runtime Direction twice.

The renderer adapter must know the ownership of its directional state.

### 5.3 Family C3 — moving block renderers

Examples:
- PistonHeadRenderer;
- FallingBlockRenderer.

These combine:
- moving physical position/translation;
- canonical BlockState model;
- light/frame sampling.

FallingBlockRenderer:
existing local anchor correction is the right type of adapter.

PistonHeadRenderer:
depends on Phase-7A piston movement semantics.
Phase 3 owns only the visual frame once physical moving position/direction is
defined.

### 5.4 Family C4 — world/camera-space effect renderers

Do NOT globally wrap these in a local block frame:

- BeaconRenderer beam;
- ConduitRenderer camera-facing eye/shell portions;
- StructureBlockRenderer physical debug boxes;
- End portal/gateway special effects;
- other renderer sections explicitly facing the camera or extending in world
  coordinates.

These need per-effect product/owner policy.

Examples:
- beacon beam may intentionally remain global/world vertical;
- structure debug boxes must describe physical world coordinates;
- conduit camera-facing quaternion belongs to camera/world space;
- portal orientation depends on Phase-9 rigid portal topology.

### 5.5 Root-frame API should be reusable

Phase 3 should provide one stable Planet-owned render-frame helper:
- local->world quaternion/matrix;
- apply around block center;
- physical/canonical Direction conversion.

Renderer-specific mixins should remain thin.

This is a cross-version portability boundary.

---

## 6. Phase 3D — moving/entity/accelerated rendering

### 6.1 FallingBlockRenderer

Vanilla assumes:
- entity position is center of WORLD-DOWN block face;
- render BlockPos uses boundingBox.maxY;
- translation is (-0.5, 0, -0.5).

Planet already proved this requires an entity-anchor-aware adapter.

Ownership:
- entity physics/landing -> Phase 7;
- block model orientation -> standard Phase-3 BakedModel path;
- renderer anchor translation -> Phase 3D.

### 6.2 Minecart/boat/TNT/item-frame renderers

These are NOT ordinary block renderers.

They use entity:
- yaw/pitch;
- physical interpolation;
- rail geometry;
- buoyancy/bubble angle;
- attachment direction;
- entity body orientation.

Ownership:
- reusable render-frame primitives may live in Phase 3;
- actual orientation semantics belong to Phase 7 entity/vehicle/body work.

Do not "fix" them from block-render assumptions.

### 6.3 Piston moving rendering

Piston renderer consumes:
- PistonMovingBlockEntity movement direction;
- moving offset;
- moved canonical BlockState.

Ownership split:
- push/movement direction -> Phase 7A;
- physical moving pose -> 7A/entity integration;
- rendering of canonical moved block -> Phase 3D.

### 6.4 Accelerated/custom render engines

Any renderer bypassing ModelBlockRenderer/BakedModel wrapper is a separate
integration boundary.

Examples:
- Flywheel/Create instancing;
- custom chunk renderers;
- custom VertexConsumer geometry;
- custom block/entity render pipelines.

Do NOT globally intercept raw vertices.

Preferred compatibility API:
- Planet render frame for BlockPos/entity;
- canonical<->physical direction/vector helpers;
- shape/model transform helpers where safe.

Phase 10 owns explicit integrations.
Each owning subsystem must still run compatibility gates during implementation.

---

## 7. Phase 4 — particle engine ownership

R2 confirms particle work has TWO independent ownership dimensions:

1. particle-class motion/lifecycle/render behavior;
2. emitter/source coordinate and velocity semantics.

Fixing one does not imply the other.

### 7.1 Complete particle-class inventory

The audited 1.21.1 client particle package contains 74 Java classes.

ParticleEngine provider registration and every custom tick/move family were
covered by the existing Phase-4 audit.

Canonical detailed matrix:
`PARTICLE_MATRIX_1_21_1.md`.

R2 accepts its CLASSIFICATION model:
- base Particle engine;
- custom tick;
- custom move;
- custom trajectory;
- render-oriented;
- frame-independent;
- fluid/entity/portal integration gates.

### 7.2 Base Particle engine is the strongest common boundary

If a subclass dispatches to base Particle.move:
- vanilla vertical-vs-tangent collision semantics are inherited;
- the local-frame move algorithm belongs at base Particle.move.

Classes overriding move() bypass it naturally.

Similarly:
base Particle.tick owns common:
- gravity;
- blocked-vertical speedup;
- ground tangent friction.

This is more portable than an instanceof list of ordinary particle subclasses.

### 7.3 Custom particle behavior remains explicit families

Separate audited adapters may still be required for:
- custom tick delta;
- component-wise damping;
- constructor-authored vertical/tangent launch;
- custom curved trajectory;
- custom render plane orientation;
- fluid membership/surface conditions.

Do not infer that a class is complete only because base gravity is local.

---

## 8. Particle emitter/source ownership

### 8.1 Emitter E1 — canonical block-local geometry

Examples:
- torch/redstone torch;
- campfire;
- furnace/blast furnace/smoker;
- candle;
- end rod;
- redstone wire visual line;
- many block animateTick effects.

Typical assumptions:
- center + local UP offset;
- local tangent random offset;
- canonical FACING-relative face;
- local tangent/vertical velocity.

Owner:
Phase 4 emitter adapter using Phase-2 canonical BlockState semantics.

Preferred API:
PlanetParticleEmitter / stable block-local offset transform.

Preserve exact RNG call count/order.

### 8.2 Emitter E2 — isotropic block-volume effects

Some events simply sample uniformly inside a block volume or all six physical
faces without a gravity-relative concept.

These may be intentionally frame-independent.

Do not rotate isotropic random points merely because the block is on a side
face.

### 8.3 Emitter E3 — ParticleUtils is NOT globally local

ParticleUtils call sites have different meanings.

Examples:
- spawnParticleBelow: local semantic DOWN for Cherry/Falling-like callers;
- spawnParticlesAlongAxis: may use a block's explicit physical/canonical axis
  supplied by the caller;
- spawnParticlesOnBlockFaces: often a physical all-face block effect;
- spawnSmashAttackParticles: entity/body/ground-owned.

Therefore:
adapt the CALLER, not ParticleUtils globally.

### 8.4 Emitter E4 — entity/body-owned

Examples:
- TrackingEmitter;
- hearts/totem/crit/status bursts;
- GlowSquid ink;
- projectile impact;
- firework rocket/trail/star orientation;
- entity eye/body height offsets.

Owner:
Phase 7 generates physical emitter coordinates/vectors from the entity body
frame.

Phase 4 consumes those physical inputs.

### 8.5 Emitter E5 — fluid-owned

Examples:
- bubble/current/splash;
- lava random display;
- drip source/landing;
- fluid surface particles.

Owner split:
- particle-local motion stays Phase 4;
- source topology/height/current belongs Phase 5.

### 8.6 Emitter E6 — portal-owned

NetherPortalBlock/EndGateway emitters encode portal axis and portal-plane
geometry.

Particle custom trajectory can be Phase 4.

Final source position/vector orientation belongs to Phase 9 portal topology.

### 8.7 Emitter E7 — weather/environment-owned

LevelRenderer rain impact uses:
- world precipitation column;
- collisionShape.max(Axis.Y);
- FluidState height;
- global weather surface.

This is not a generic particle emitter problem.

Owner:
- weather direction/surface policy -> Phase 7B/R6;
- fluid surface integration -> Phase 5;
- resulting particle motion -> Phase 4.

### 8.8 Emitter E8 — world-event packet effects

LevelRenderer levelEvent handles many block/world event particle bursts.

Packet/event data may encode:
- Direction.Axis;
- block state;
- raw event integer;
- entity/body-independent physical event position.

Rule:
classify the event producer's semantic contract first.

Do not globally rotate all LevelRenderer event particles.

Examples:
- waxing/scrape all-face burst may be frame-independent;
- lightning-rod axis spark depends on the encoded block axis;
- smash attack is entity/body-local;
- portal-frame fill has block-local UP geometry;
- trial/vault effects inherit their block/source semantics.

---

## 9. Particle render orientation

Most TextureSheetParticle quads are camera-facing and therefore should remain
camera-space.

Do NOT rotate all particle render quads with gravity.

Only particles whose render shape itself encodes a gravity-relative local plane
need adaptation.

Example:
ShriekParticle's authored planes are semantic local geometry and can compose:
- Planet local->world quaternion;
- vanilla local plane rotations.

Camera-facing billboard particles remain billboards.

---

## 10. Random/model offset and particle emitter are separate

A recurring visual bug can come from three independent places:

1. block model offset;
2. block-local particle emitter offset;
3. particle trajectory.

Never compensate one layer to hide another.

Example:
pointed dripstone chain offset belongs to model/shape offset semantics;
falling dust trajectory belongs to particle motion.

---

## 11. Phase-3 deterministic test matrix

### Shapes
- all six frame basis transforms;
- outline/collision/visual/interaction once-only rotation;
- support/occlusion canonical cache remains canonical;
- full-cube singleton identity.

### Static model
- asymmetric quad all six faces;
- physical getQuads side -> canonical source side;
- vertex + normal + BakedQuad.direction;
- null-side quads;
- ModelData and RenderType delegation;
- cache identity/reload behavior.

### Culling
- source/target canonical side at seam;
- no opposite-side assumption across frame boundary.

### AO/light
- transformed physical quad direction selects expected physical neighbor;
- no second local rotation;
- +Y exact vanilla path.

### Offset
- standard XZ offset stable along local vertical;
- standard XYZ local vertical/tangent transform;
- seed equivalence on +Y.

### BER
- root-frame quaternion basis all six faces;
- rigid renderer representative;
- physical runtime-direction representative remains single-transformed;
- world/camera renderer excluded from generic wrapper.

### Moving
- falling block anchor/model agreement;
- moving piston integration contract.

---

## 12. Phase-4 deterministic test matrix

Detailed class matrix remains in `PARTICLE_MATRIX_1_21_1.md`.

Cross-cutting pure tests:
- local velocity/position term transforms;
- base Particle.move local collision order;
- local onGround/stopped/tangent clipping;
- constructor bias/scales;
- custom trajectory local arc;
- emitter local offset all six faces;
- RNG order preserved where emitter is replaced;
- camera billboard remains camera-facing;
- explicit local-plane render particle uses render-frame quaternion.

Integration gate tests are owned later by:
- Phase 5 fluid;
- Phase 7 entity/body;
- Phase 7B weather;
- Phase 9 portals.

---

## 13. Phase-3 single runtime acceptance matrix

Representative mechanisms, not every renderer:

1. +Y vanilla baseline.
2. asymmetric static block all six faces.
3. slab/stair/torch/dripstone model matches physical shape.
4. full-cube adjacency and exact seam culling.
5. AO/light physical neighbor sanity.
6. breaking overlay alignment.
7. standard random-offset representative on side face/local vertical chain.
8. rigid BER: bed + chest + sign/banner/skull representative.
9. runtime-direction BER: bell/brushable representative.
10. moving block: falling block + piston after Phase-7A integration.
11. world/camera-space renderer smoke check proving no global BER wrapper damage.
12. ModelData/RenderType compatibility.
13. cache/performance smoke test.

Directional shade acceptance waits for the R6 environment-policy decision if
the desired policy differs from physical vanilla shade.

---

## 14. Phase-4 single runtime acceptance matrix

The existing Phase-4 matrix remains valid, with ownership clarified by R2.

Must cover:
- +Y baseline;
- five rotated faces;
- block destroy/Terrain;
- falling dust;
- torch/redstone torch;
- cherry;
- campfire;
- representative base Particle gravity;
- custom tick;
- custom trajectory;
- render-oriented particle;
- block-local emitter;
- no duplicate emission;
- no physical +Y drift;
- no extra surface crawl;
- performance smoke test.

Rows whose SOURCE depends on fluid/entity/weather/portal semantics remain
integration gates and do not block particle-local Phase-4 closure if explicitly
documented.

---

## 15. Cross-version portability conclusions

Strong reusable Planet-owned boundaries:
- VoxelShape local->physical transform;
- render frame matrix/quaternion;
- oriented BakedModel view;
- frame-aware culling;
- standard OffsetType local authoring;
- particle motion vector/frame helpers;
- particle emitter local-offset helpers.

Version-sensitive wiring:
- BlockStateBase shape method signatures;
- ModelBlockRenderer tesselate overloads;
- NeoForge ModelData/RenderType hooks;
- BakedQuad constructor/layout;
- BER class/method signatures;
- Particle subclass method layout;
- ParticleEngine registration/provider layout.

Avoid:
- global VertexConsumer interception;
- global BlockEntityRenderDispatcher frame transform;
- global ParticleUtils direction transform;
- duplicate AO/light rotation;
- copying whole renderer/particle methods where a narrow semantic adapter is
  possible.

---

## 16. R2 completion decision

R2 is COMPLETE when:
- Phase-3 geometry/render families have explicit ownership;
- AO/light vs shade policy is separated;
- standard offset semantics are explicit;
- BERs are split into rigid-local vs runtime-direction vs world/camera families;
- moving/entity render dependencies are explicit;
- particle class inventory and emitter ownership are explicit;
- accelerated/custom renderer boundary is explicit;
- deterministic and batch acceptance matrices are defined.

No runtime code should be expanded until R3-R6 and the final completeness sweep
are complete.
