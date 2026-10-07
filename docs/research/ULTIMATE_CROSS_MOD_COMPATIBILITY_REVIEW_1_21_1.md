# Ultimate cross-mod compatibility audit — Planetary 2.0 / 1.21.1

2026-10-08. Status: RESEARCH/ARCHITECTURE REVIEW COMPLETE; runtime unchanged.
Target: Minecraft 1.21.1, NeoForge 21.1.215, Java 21, Planetary branch 2.0.
Scope: official NeoForge API and concrete mechanisms from Create, AE2,
Mekanism, WorldEdit, TerraBlender, Distant Horizons, and wider mod families.
Study augments R1-R6 and FINAL_COMPLETENESS_SWEEP; it does not claim
all mods or version-specific hooks were verified in-game.

Confidence: DIRECT = inspected named upstream source or 1.21.1 official
documentation; INFERRED = compatibility risk based on source, not reproduced
runtime bug; DESIGN = suggested adapter and future acceptance, not implemented.

## 1. Source and version ledger

Planet current:
- src/main/java/dev/planetary/mixin/BlockCapabilityMixin.java
- src/main/java/dev/planetary/world/PlanetBlockRuntime.java
- src/main/java/dev/planetary/world/PlanetBlockFrameContext.java
- src/main/java/dev/planetary/world/PlanetBlockStateFrame.java
- src/main/java/dev/planetary/world/PlanetBlockNeighborQuery.java
- src/main/java/dev/planetary/gravity/PlanetGravityRuntime.java
- src/main/java/dev/planetary/api/PlanetFrameApi.java
- docs/AI_CONTEXT.md old capability acceptance and Create-like test reports.

Official NeoForge:
- https://docs.neoforged.net/docs/1.21.1/inventories/capabilities/
- https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/
- https://docs.neoforged.net/docs/1.21.1/resources/client/models/modelloaders/

Named third-party source:
- Create 1.21.1: https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/RotationPropagator.java
- Create virtual Level: https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/contraptions/ContraptionWorld.java
- Create actors: https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/api/behaviour/movement/MovementBehaviour.java
- AE2 API: https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/main/API.md
- AE2 grid side API: https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/main/src/main/java/appeng/api/networking/IGridNode.java
- Mekanism transmitter: https://github.com/mekanism/Mekanism/blob/1.21.x/src/main/java/mekanism/common/content/network/transmitter/Transmitter.java
- Mekanism tile lifecycle: https://github.com/mekanism/Mekanism/blob/1.21.x/src/main/java/mekanism/common/tile/transmitter/TileEntityTransmitter.java
- WorldEdit copy: https://github.com/EngineHub/WorldEdit/blob/version/7.4.x/worldedit-core/src/main/java/com/sk89q/worldedit/internal/util/ClipboardTransformBaker.java
- TerraBlender example: https://github.com/Glitchfiend/TerraBlender/blob/26.3/example/NeoForge/src/main/java/terrablender/example/TestMod.java
- DH generator API: https://distant-horizons-team.gitlab.io/distant-horizons/com/seibel/distanthorizons/api/interfaces/override/worldGenerator/IDhApiWorldGenerator.html
- DH examples: https://gitlab.com/distant-horizons-team/distant-horizons-api-example

Upstream branch versions: Create explicitly 1.21.1; AE2 main,
Mekanism 1.21.x, WorldEdit 7.4.x, TerraBlender 26.3 and current DH docs
are not necessarily the exact Planetary target artifact. Confirm concrete
mod and hook versions before integration. Do not claim proven compatibility.

## 2. CRITICAL NEW FINDING — global capability Direction rewriting

DIRECT Planetary:
BlockCapabilityMixin intercepts BlockCapability.getCapability globally for
any non-null Direction context in a Planet region. It unconditionally rewrites
physical Direction to target canonical local Direction before invoking the
provider. Previous Planet capability probes used custom STONE providers
DELIBERATELY expecting local UP; successful echo, item/fluid/energy handler
and cache-invalidation tests do not prove correct behavior of arbitrary
third-party providers.

DIRECT official API:
NeoForge 1.21.1 accepts a nullable Direction context for sided capabilities
and dispatches provider callbacks by Block and BlockEntityType. There is NO
guarantee every foreign provider stores its ports in Planet canonical-local
coordinates. The same context participates in BlockCapabilityCache queries.
Providers need invalidation on lifecycle and configuration changes.

DIRECT Mekanism and AE2:
- Mekanism Transmitter indexes persisted connection state/bit masks using
  Direction.ordinal(). Its side settings are tied to actual port indices.
- AE2 in-world grid connections expose Direction-keyed connected sides.
  Their meaning is the mod's own in-world physical topology.

INFERRED risk, NOT reproduced:
A modded machine expects physical NORTH but receives Planet-local equivalent;
it can return the wrong capability, accept/reject an item/fluid/energy on the
wrong port, connect the wrong side or invalidate the wrong assumption.

Correct policy to implement after baseline:
1. PHYSICAL Direction = safe default for unknown NeoForge provider.
2. LOCAL conversion ONLY under an explicit provider/block/BE capability policy,
   such as Planet-aware vanilla-family adapter or third-party opt-in.
3. Contract keyed by provider/capability plus Block/BlockEntityType; avoid
   blanket mod-ID or Direction type inference.
4. Calculate caller's physical target BlockPos first, then target physical
   entry side; convert to target-local ONLY if target provider declares LOCAL.
5. Do not retransform at nested calls; preserve null, unknown context, cache
   identity and all provider dispatch hooks.
6. Registry precedence deterministic; optional mod absent => safe fallback.
7. Keep existing accepted Planet LOCAL echo as an explicitly opted-in case;
   add PHYSICAL echo and genuine third-party machine/cable tests across 6
   faces, 12 cube edges, 8 corners, reload and cache invalidation.
8. Current working code is NOT modified here; design gate is Phase 1/7A.5/10.

This materially narrows the earlier assumption of automatic support for
ALL sided capabilities. It does not undermine the confirmed local-specific
diagnostic tests; it corrects what those tests prove.

## 3. CRITICAL NEW FINDING — coordinate provenance and virtual levels

DIRECT:
- Create ContraptionWorld wraps Level, reads captured contraption BlockStates
  and limits local height; actors are separately dispatched by a registered
  MovementBehaviour interface.
- WorldEdit clipboard copy uses BlockTransformExtent plus affine transforms
  and ForwardExtentCopy, including biome copying. Not ordinary player placing.
- AE2 distinguishes in-world grid nodes created with Level+BlockPos from
  virtual nodes connected explicitly without physical adjacency.
- PlanetGravityRuntime binds physical Level INSTANCE via IdentityHashMap;
  PlanetBlockRuntime.fieldAt treats passed BlockPos as a physical coordinate
  in that bound Level.

INFERRED hazard:
Virtual contraption/schematic positions must never be interpreted as
real physical Planet BlockPos, even if a wrapper exposes a backing Level.
Neither should an unbound wrapper automatically become Planet-active.
No reliable universal answer can be inferred from Level type alone.

DESIGN:
- Explicit SpaceProvenance enum/token: PHYSICAL_WORLD,
  CANONICAL_BLOCK, TRAVERSAL_CHART, ENTITY_BODY,
  FOREIGN_CONTRAPTION, SCHEMATIC, GENERATION_SAMPLE, RENDER_FRAME.
- Registered foreign provider supplies mapping to physical frame and
  lifecycle, or returns unsupported. Never globally rewrite Level methods.
- WorldEdit and moving Create BE placement must remain distinct from ordinary
  BlockPlaceContext. Unknown virtual coordinates pass through unaffected.
- Test virtual captured block, contraption moves/re-places across a seam,
  emitted particles, BE ticks, save/reload and client/server geometry.

## 4. Network graph families are not the same as Planet traversal

### Create: kinetic graph and signed angular velocity

DIRECT RotationPropagator:
- physical toPos - fromPos integer displacement and Direction.getNearest;
- adjacent shaft axes and hasShaftTowards for each endpoint;
- distinct chain, small/large gear, gear ratio and signed speed rules;
- update cycles, speed limit, source tree, destroyed-block failure paths;
- special BELOW physical check in speed controller algorithm.
A generic PlanetNeighbor (target + receiving side) is insufficient for
kinetics. Need optional ConnectionGeometry including source/target physical
ports, local basis/chart transform, axial orientation and signed transfer,
with Create owning actual transfer ratio and its graph lifecycle.
Important: preserve chirality/handedness on edge and corner; rotating
Direction.Axis indiscriminately can reverse rotational sign or break loops.
Test same-face baseline, six-face connections, corner loops, speed
controller and conflict/speed cap, without duplicating Create logic.

### AE2: side-exposed server graph, and virtual nodes

DIRECT API:
- IManagedGridNode.create(Level, BlockPos) creates in-world node; AE2 handles
  physical adjacency and its own repathing on exposed side changes.
- Virtual nodes are manually linked, not physical adjacency.
- Node create/destroy/load NBT/save NBT and chunk unload are explicit
  lifecycle requirements. Server grid is not client-owned.
Design: physical neighbor/port default; no synthetic Planet graph edges in
AE2 unless explicit plugin. Keep network owner, side semantics, lifecycle.
Test side exposure, merge/split, chunk reload, loops and seam crossing.

### Mekanism: port enum persistence, acceptor caches and custom network

DIRECT:
- Transmitter.connectionTypes array and bitmask index by Direction.ordinal().
- AcceptorCache and DynamicNetwork are owned by Mekanism.
- TileEntityTransmitter refreshes connections, serializes on save, removes
  from networks when block removed, handles chunk unloaded.
Design: never reinterpret persisted side ordinals as local without a
versioned explicit machine adapter. Port visualization/GUI interaction may
have separate local orientation. Caller-target selection MUST be physical.
Tests: connection after reload, directional config tool, 6-face/edge pipes,
energy/fluid/item acceptance and cache invalidation.

## 5. Transforming machines and structures means transporting state

Moving contraptions, WorldEdit, pistons, templates and mod machines may hold:
- BlockState properties, including orientation and HALF/SHAPE;
- BlockEntity NBT, side-port arrays, own network identifiers, inventories;
- scheduled ticks, BE lifecycle registration/removal, event notifications;
- entity passengers and angular/linear velocity;
- render-state/partial tick and arbitrary 3D rigid transforms.

Define optional FrameTransportPolicy:
canTransport, preservePhysicalOrientation vs relocalizeAtDestination,
transformState, transformBEData, detachAndInvalidate, attachAndRepath.
Default unknown machine policy: preserve physically rigid state or reject
unsupported relocalization; do NOT guess how unknown NBT fields rotate.
Transport transaction: validate FIRST, detach, update/invalidate source,
place target, restore attachments, re-register/repath, notify, rollback/no-op
on invalid. WorldEdit paste should not call player-only placement logic.
Real hook sequencing must be researched per owning runtime batch.

## 6. Mod family compatibility matrix

| Family | Underlying mechanism | Foundation/fallback | Target class |
| --- | --- | --- | --- |
| Vanilla-derived mod blocks | vanilla BlockState/placement/support/render | canonical family + adapters | Mostly inherited; test |
| NeoForge item/fluid/energy capabilities | sided provider callbacks, cache | PHYSICAL default; explicit LOCAL opt-in | Potentially automatic AFTER new P0 fix |
| Create power graphs | connected kinetic components, signed ratios | ConnectionGeometry + Create optional bridge | Explicit |
| Create moving constructs | virtual Level / actors / motion | ForeignSpace + rigid transform, lifecycle | Explicit |
| AE2 ME/part/cable networks | server grid, exposed sides, virtual nodes | preserve AE2 side/graph | Mostly physical; test + seam bridge when needed |
| Mekanism tubes/pipes/machines | side ordinal masks, acceptor cache and graph | physical port, mod-specific frame policy | Test/explicit |
| Programmable turtles/builders | script-local forward/up, automated movement | actor body frame vs physical BlockPos | Explicit; not exact 1.21.1 proven |
| WorldEdit / building schematics | affine clipboard, direct block writes, BE data | physical world writes, typed transport | Explicit |
| TerraBlender / BOP / BYG | biome region injection, surface rule, placed features | Planet biome/climate sampling, keep registries | Phase 8 integration |
| Custom world noise / structure mods | direct physical-Y features/noise/height | field adapter vs rigid policy | Varies / explicit |
| Extra fluids | FlowingFluid or FluidType / tank APIs | fluid graph, correct capability policies | Mixed |
| Extra mobs and vehicles | direct motion/AI/eyes/attack math | physical entity motion/body APIs | Inherited + exception adapters |
| Sodium/Embeddium/Iris/CTM | alternate mesh/connected textures/lighting/shaders | BakedModel/ModelData, quads/normals and targeted render adapters | Exact version tests |
| Distant Horizons | independent LOD data/world generator, XZ chunk source | explicit Planet six-face LOD and lifecycle bridge | Explicit; not automatic |
| KubeJS/datapacks/commands | data registries vs raw XYZ scripting | vanilla physical API + separate local helpers | Mixed |
| Recipe/GUI mods | non-world geometry APIs | vanilla pass-through | Normally unaffected |

Specific environment note: DH current worldgenerator API includes generateLod
and bounded full-data source usage. It cannot prove automatically that a
column-oriented LOD represents six exposed faces, radial lighting and
cave/overhang occlusion. Version-pin the actual 1.21.1 API before design.
TerraBlender currently inspected branch 26.3 is conceptual reference only.

Additional dimensional integration: Planet is a NEW dimension. Worldgen
mods may target overworld/biome/dimension tags; do not silently advertise
Planet as vanilla Overworld to trick hooks. Declare compatible tags and
Planet biome registry selection explicitly, preserving datapack semantics.

## 7. Minimal universal architecture (not a catch-all coordinate rewrite)

Layer A: Typed coordinate provenance
    WorldPhysicalPos, CanonicalBlockFrame, TraversalChart,
    EntityBodyFrame, ForeignFrame, WorldGenSampleFrame and RenderFrame.
    Position != direction != velocity != normal != AABB != quaternion.
    Frame composition explicit and deterministic, not global ThreadLocal.

Layer B: Physical/semantic topology API
    getPhysicalNeighbor(pos, physicalSide)
    getCanonicalNeighbor(pos, localSide): actual physical target + target port
    getTraversalStep(chart, tangent): path-dependent chart and direction
    getConnectionGeometry: physical port pair + transported signed orientation
    Actual world six-neighbor notifications remain vanilla physical.

Layer C: Semantic dispatch per provider
    PHYSICAL (unknown default); CANONICAL_LOCAL (declared);
    TRAVERSAL_RELATIVE (opt-in graph); FOREIGN_LOCAL (explicit adapter);
    WORLD_Y_ABSOLUTE (purposeful physical algorithm);
    null/non-direction contexts unchanged.
    Registry keys granular Block/BlockEntityType/capability/API, with
    specificity, priorities, conflict reporting and absence fallback.

Layer D: Version-scoped optional integration SPI
    Pure Planet math -> official Minecraft/NeoForge adapter -> thin hooks.
    Integrations for Create/AE2/Mekanism/DH only when exact installed
    artifact and API are known. No hard class references to optional mod
    during base classloading. Registry finalized at setup, not tick.
    Avoid mutating registered BakedModels, BlockStates or global Direction.

Layer E: Lifecycle + persistence contract
    side changes invalidate caches; graph recalc on load/unload;
    block and BE transport separate, saved poses/ports deterministic;
    worldgen profile/biome versioned; no per-tick reflective scans.

Layer F: Diagnostics and acceptance
    log source space, expected port convention, source/target positions,
    local/physical direction and owning adapter under debug toggle.
    Every compatibility claim requires exact mod version and manual or
    complete deterministic tests. Prefer an explicit unsupported mode over
    silent misleading operation. Optimize no-mod fast path.

## 8. Prioritized acceptance matrix (NOT EXECUTED)

P0 — BlockCapability side model and regression:
1. local-authored echo provider intentionally expecting canonical UP;
2. second provider intentionally expecting PHYSICAL DOWN/UP;
3. same block family on six faces, edges, corners; null side unchanged;
4. vanilla inventory vs third-party item/fluid/energy blocks;
5. queried physical target, entry side and BlockCapabilityCache validity;
6. non-Planet world's exact vanilla behavior.

P1 — Graph and machine behavior:
1. Create shaft, small/large gear, belt, speed controller and inconsistent
   loops; signed speed parity and no inappropriate block destruction.
2. AE2 cable/part port, virtual vs in-world node, split/merge/repath,
   unload/reload, no duplicated phantom connections.
3. Mekanism transmitter pipe/energy/fluid, connection GUI, saved masks,
   capability invalidation, unload/reload.
4. Create moving contraption crossing seam and reassembling in physical
   world, actor movement, BER, collisions, particle/sound, passengers.

P2 — World-edit/worldgen/render compatibility:
1. WorldEdit paste/rotate with entity/BE data and physically correct
   block updates/scheduled ticks; scripting fake player/automation.
2. NeoForge biome modifiers, placed features, TerraBlender regions/
   surface rules, fixed-seed across face, feature-policy fallback.
3. ModelData/RenderType/connected texture vs default and alternate
   renderer, shaders, six-face skylight and breaking overlays.
4. Distant Horizons LOD with top/bottom/side exposed terrain, clipping,
   radial sky, API data source lifetime, multi-thread staging.
5. Mixed modpack and absence-of-mod pass-through, no client/server
   classloading crash and performance/regression budget.

Run pure unit checks and build early when actually coding, then one
subsystem-level in-game acceptance per AGENTS.md. No runtime pass inferred
from upstream API code or from this research-only review.

## 9. Architecture decision and reopening scope

P0 NEW CONTRACT CORRECTION: automatic side-context rewrite is not safe for
unknown third-party providers. Keep physical NeoForge semantics by default
and LOCAL only on explicitly declared receiving provider/family.
Existing Planet-owned local tests remain valid but narrow in scope.

P0 NEW INTEROPERABILITY FOUNDATION: physical/virtual space provenance,
connection signed orientation/handedness, BE data transport and graph
lifecycle are additional generic abstraction surfaces to add in Phase 1
and Phase 10 rather than class-by-class patches.

P1: exact-version source hooks and optional bridges at owner phase; third
party graph engines remain owned by their respective mods.

The previous frozen R1-R6 family map is retained. This is one bounded
compatibility-specific amendment, not an endless global research cycle.
No runtime code, build or gameplay acceptance performed during this review.
