# Planetary 2.0 implementation roadmap

> Development process is defined by `/AGENTS.md` and is mandatory.
> This file owns scope, phase ordering, dependencies, status and acceptance
> gates; it does not replace the process rules.

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

## Ultimate cross-mod compatibility architecture gate [2026-10-08, RESEARCH COMPLETE]

Canonical audit:
docs/research/ULTIMATE_CROSS_MOD_COMPATIBILITY_REVIEW_1_21_1.md

Found a materially unsafe universal assumption in current runtime:
BlockCapabilityMixin unconditionally rewrites every NeoForge sided Direction
context to target canonical LOCAL direction. Current verified STONE provider
tests were intentionally designed to EXPECT canonical LOCAL, so they only
validate that contract. Ordinary third-party providers may instead expect
PHYSICAL sides (Mekanism persists port state by Direction ordinal; AE2 uses
Direction keyed in-world grid connections). The issue is RESEARCH-CONFIRMED
AS A GENERAL CONTRACT RISK, not an in-game reproduced failure.

**NEW P0 capability contract before broad compatibility claims:**
- default unknown NeoForge capability provider sides to PHYSICAL;
- rewrite to canonical LOCAL only with a per-provider NeoForge registration
  wrapper; unlike a capability/block-wide registry, this allows multiple
  providers on one block to retain their independent physical/local meaning;
  preserve old local echo as an opt-in regression case; preserve null/cache;
- calculate correct PHYSICAL adjacent target before selecting receiving side;
- verify exact NeoForge 21.1.215 integration and a genuine modded machine
  before claiming cross-mod runtime compatibility.
Owner: Phase 1 + Phase 7A.5 + Phase 10. First P0 implementation is now
committed with acceptance PENDING; see checkpoint below.

**NEW universal compatibility foundation:**
- explicit coordinate provenance: physical Level vs virtual Create
  ContraptionWorld, WorldEdit clipboard, generation sample, entity frame,
  traversing chart, render frame; never silently reinterpret fake Level;
- separate position/direction/normal/velocity/rotation/AABB and typed
  foreign transforms; no global primitive replacements;
- ConnectionGeometry for physically adjacent endpoint port pairs plus
  signed rotational axis/handedness when a graph declares it, without
  implementing a second Create/AE2/Mekanism graph;
- frame-aware BlockState + BlockEntity/NBT side-settings transport and
  lifecycle/invalidation for moved blocks, with opt-in and rigid fallback;
- per-provider opt-in semantic adapters at NeoForge registration (implemented
  for block and BlockEntity); other mechanism SPI is future work, with no
  per-tick reflection or optional-mod hard dependency;
- exact-version integration matrix for Create, AE2, Mekanism, WorldEdit,
  TerraBlender, custom fluid/entity, Sodium/Embeddium/Iris, Distant Horizons,
  scripted/fake-player automation and vanilla pass-through.
Owners: Phase 1 foundation; Phase 2/7A graph and block transport;
Phase 8 biome/LOD; Phase 3 render; Phase 10 optional bridges.

This is a TARGETED amendment to the frozen R1-R6 map, not another open-ended
discovery pass. Batch 0 must inventory this contract before code changes;
implementation proceeds in coherent owning batches with one acceptance
matrix each.

### Capability provider-direction implementation checkpoint [IMPLEMENTED / ACCEPTANCE PENDING]

Detailed exact NeoForge source and tests:
docs/research/CAPABILITY_PROVIDER_CONTEXT_1_21_1.md.

Implemented now:
- public PlanetCapabilityAdapters.canonicalLocalBlock and
  canonicalLocalBlockEntity register per-provider LOCAL semantics;
- unknown/unwrapped NeoForge providers retain physical direction and target
  BlockPos; BlockCapabilityMixin does not rewrite a physical Planet Level,
  while old virtual-atlas fallback is retained for its separate prototype;
- diagnostic STONE local echo and standard handlers use explicit opt-in;
  new independent PHYSICAL_SIDE_ECHO tests 36 more physical dispatches;
- PlanetCoordinateContext.PhysicalBlock vs ForeignBlock carries declared
  coordinate provenance, and PlanetFrameApi overloads refuse unresolved
  foreign BlockPos (no implicit virtual/physical transform);
- deterministic tests for all faces, a corner, null/unbound side behavior,
  unattached BlockEntity, physical-vs-foreign separation.

**STATUS: implemented, tests not executed in this session and gameplay
acceptance pending.** This does NOT establish third-party machines working.
The old global conversion had been tested only with LOCAL-aware diagnostic
providers; any existing vanilla/machine-side regression must be researched
at its owning provider/port semantic layer, not reverted to blanket patch.

Still needed:
- first user build and startup + single subsystem acceptance;
- ensure real NeoForge 21.1.215 target compiles/runs; exact upstream
  branch 1.21.1 call path has been reviewed;
- real side-specific vanilla hopper/furnace/dispenser + genuine external
  item/fluid/energy machine on ±X/±Y/±Z and edge/corner;
- connection signed axis, moving BE data, physically sourced foreign
  transforms, optional Create/AE2/Mekanism adapters and unrelated modpacks.
Those are future coherent subsystem gates, not claims for this checkpoint.

## Final research sweep: frozen cross-phase handoff [2026-10-08]

R1–R6 source/mechanism research has completed, plus a separate public mod
coordinate-system precedent study. This means *research architecture* is
ready for dependency-ordered implementation; not one Phase 2–11 runtime
subsystem has been newly accepted by this documentation-only pass.

Canonical cross-phase decisions and batch ordering:
- docs/research/FINAL_COMPLETENESS_SWEEP_1_21_1.md
- docs/research/EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md

Status:
- R1/R2/R3/R4/R5/R6: RESEARCH COMPLETE.
- Final completeness sweep: COMPLETE (with explicit future hook/version gates).
- Runtime: remains at previous Phase-4 experimental/baseline checkpoint;
  DO NOT mark earlier partial implementations PASS retroactively.
- Each batch must respect AGENTS.md and complete its one acceptance matrix.

**Implementation order is dependency-based, not naive phase-number order:**
0. Build/startup baseline, last accepted gameplay state, and finite-world/
   save-version/light-engine architecture gates.
A. Phase 1: pure canonical state/traversal/typed direction-vector/normal/AABB
   foundation and public frame API.
B. Phase 2 + Phase 3: block semantics, state/graph ownership, shapes/static/
   accelerated renderer adapter.
C. Phase 7.1/7.2 and 7B.1/7B.2: physical deltaMovement migration and
   entity/body collision, locomotion, view/raycast/client-server authority.
D. Phase 5 + Phase 4: fluid topology/render/NeoForge extensions and
   particle class/emitter completion; weather emitters depend on 7B.4.
E. Phase 7A then Phase 6: signals, piston, rail, logistics; afterward
   chart-aware navigation/AI depending on Phase 2/5/7.
F. Phase 8 and Phase 7B.3–7B.4: PlanetSurfaceQuery, continuous climate/
   biome generator, terrain/sea/hydrology, features, spawn candidates,
   real weather/sky-light-engine integration.
G. Phase 9 + 10 + 11: rigid structures and portals, targeted mod integration,
   saved-world compatibility, smooth gravity transition/polish.

External precedents and added acceptance:
- GravityChanger / Immersive Portals demonstrate explicit physical velocity,
  eye-offset, quaternion and direction adapters. R4 physical deltaMovement
  is ALREADY the chosen future design; current runtime still stores LOCAL.
- Valkyrien Skies highlights split position/vector/raycast/particle transforms,
  NaN/huge transformed bounds, unloaded-space teleports, rider-save mismatch.
- Cubic Chunks demonstrates why truly unlimited physical Y is a storage/engine
  rewrite; first implementation envelope MUST be finite and documented.
- Immersive Portals virtual wrapping cannot provide real across-seam
  redstone/fluid/AI; Planet must preserve one physical voxel neighborhood.
- Starminer and Up And Down are historical comparison/test coverage only.
- All external examples span different Minecraft/loader versions; none
  automatically validate NeoForge 1.21.1 integration.

**P0 gates before claiming production-quality survival planet:**
1. Six-face skylight needs a true sky-light-source/exposure mechanism, not
   rotated render or globally redefined Heightmap.
2. Finite vertical world envelope + versioned serialized worldgen profile
   BEFORE replacing the R48 fixed test cube on existing saves.
3. Entity physical-velocity migration before cross-frame force/network/AI.
4. Exact-corner traversal chart remains independent of physical BlockPos.
5. Exact NeoForge 21.1.215 hooks must be verified per implementation batch.

The final sweep documents 15 residual engineering gates and how they
map to existing phases, not an invitation to endless global re-research.
No build/runtime tests required for the research-only commits.

## Cross-cutting gravity/local-frame audit [MANDATORY]

Master audit: `docs/research/GRAVITY_IMPACT_AUDIT_1_21_1.md`

Authoritative mechanism/phase map:
`docs/research/GRAVITY_MECHANISM_MAP_1_21_1.md`

Roadmap ownership rule:
- phases own internal engine mechanisms, not whole named blocks/entities;
- one Minecraft object may appear in several phases for different behaviors;
- before a class-specific patch, inspect its base family + sibling classes;
- prefer engine boundary -> base-family adapter -> algorithm-family adapter ->
  class-specific adapter only when the behavior is genuinely unique.

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

## Cross-version portability gate [MANDATORY]

Canonical strategy: `docs/research/PORTABILITY_STRATEGY.md`

Every phase must minimize the amount of code that must change when moving from
Minecraft 1.21.1 / NeoForge 21.1.215 to a newer runtime.

Phase requirements:
- version-stable topology/frame/semantic logic lives in Planet-owned helpers;
- Minecraft/NeoForge-specific code is a thin adaptation layer;
- Mixins contain minimal hook plumbing, not duplicated subsystem algorithms;
- shared mechanism/base-family boundaries are preferred over concrete-class
  patches;
- version-specific private members, ordinals and copied vanilla fragments are
  documented as explicit port hotspots;
- pure semantic tests are separated from per-version integration/runtime tests;
- standard NeoForge/Minecraft extension points remain reachable;
- any Planet-owned persistent format has an explicit migration/version policy.

Phase closure must include a short portability review:
"Could a newer Minecraft version change the vanilla hook while leaving the
Planet helper/algorithm intact?"

If not, the boundary should be improved before multiplying it across more
classes.

## Global research completion gate [COMPLETE — IMPLEMENTATION MAY RESUME]

Current project mode:
**R1–R6 and the final global cross-sweep are COMPLETE.** The extra targeted
third-party compatibility audit is also complete; implement its P0 provider
contract at its owning mechanism and resume baseline + coherent batches.

Canonical batch plan:
`docs/research/GLOBAL_RESEARCH_BATCH_PLAN_1_21_1.md`.

Research batches:
- R1 block/world semantic topology — COMPLETE;
- R2 client geometry/render/particles — COMPLETE;
- R3 fluids — COMPLETE;
- R4 entity/body/interaction/network — COMPLETE;
- R5 AI/navigation + signals/automation/logistics — COMPLETE;
- R6 environment/worldgen/structures/compatibility — COMPLETE;
- final completeness sweep — COMPLETE;
- third-party integration/API semantic review — COMPLETE.

Runtime remains at the current partial Phase-4 checkpoint. No runtime
changes were made by research; this is not a rollback. The next changes
must go through Batch 0 baseline and P0 capability-direction contract.

After the completed research sweep:
1. preserve the dependency-based implementation order already recorded;
2. verify baseline and local-vs-physical capability provider risk;
3. resume coherent mechanism implementation batches;
4. request build/startup at the first runtime checkpoint;
5. use subsystem/phase batch gameplay acceptance rather than per-class tests.

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
- physical Planet levels no longer have capability sides silently rewritten
  by the generic mixin; only explicitly wrapped receiving providers consume
  canonical local sides. Tests added; in-game acceptance still pending;
- dedicated physical capability queries keep queried BlockPos authoritative
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
- opted-in NeoForge provider receives canonical local side with target
  physical BlockPos unchanged; unwrapped providers receive PHYSICAL side
- diagnostic local provider: 36 six-face side mappings, plus 36 independent
  physical echo mappings and BlockCapabilityCache invalidation
- standard item/fluid/energy providers: local-UP accept + local-DOWN reject on all six faces
- public PlanetFrameApi local-neighbor primitive
- Create-like raw BlockPos.relative(local FACING) stress harness across all six faces

## Phase 2 — block semantic subsystem [R1 RESEARCH COMPLETE; IMPLEMENTATION PARTIAL, BATCH ACCEPTANCE]

Detailed mechanism ownership:
`docs/research/GRAVITY_MECHANISM_MAP_1_21_1.md`

Completed R1 family matrix:
`docs/research/BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md`

Detailed existing research:
- `docs/research/PLACEMENT_1_21_1.md`
- `docs/research/SUPPORT_UPDATES_1_21_1.md`
- `docs/research/GROWTH_CONNECTIONS_1_21_1.md`
- `docs/research/MULTIBLOCK_PLACEMENT_1_21_1.md`

### Phase policy

This phase owns BLOCK-LOCAL SEMANTICS:

- placement interpretation;
- canonical BlockState orientation;
- support/survival;
- semantic neighbor/update interpretation;
- multiblock/pair relationships;
- local tangent connection graphs;
- runtime growth/support graphs;
- the block-side rail graph;
- support-triggered block behavior.

It does NOT own:
- static/BER rendering -> Phase 3;
- particles -> Phase 4;
- fluid simulation -> Phase 5;
- entity/minecart physics -> Phase 7;
- signal propagation/pistons -> Phase 7A.

A block can therefore be partially complete here and still require work in a
later owning phase.

Do NOT request one runtime test per block. Finish coherent family adapters and
run one Phase-2 family acceptance matrix.

### 2A — placement input and canonical state orientation

Vanilla mechanisms:
- UseOnContext / BlockPlaceContext;
- BlockItem;
- StandingAndWallBlockItem;
- DirectionalPlaceContext;
- DirectionalBlock / HorizontalDirectionalBlock;
- RotatedPillarBlock;
- click-offset HALF/TOP/BOTTOM logic.

Canonical rule:
- hit position and physical hit side stay physical;
- state properties store canonical LOCAL semantics.

Implemented:
- physical hit-side -> canonical local hit-side;
- physical click point -> local hit offset;
- local nearest-looking ordering;
- RotatedPillarBlock AXIS;
- Hopper FACING;
- slab TOP/BOTTOM;
- standing/wall variant selection;
- first placement diagnostic across all six faces.

Family audit/coverage still required:
- full DirectionalBlock family;
- full HorizontalDirectionalBlock family;
- standing/wall signs, banners and skulls;
- lantern/hanging placement;
- dispenser/dropper/observer/piston placement aspect only;
- stair/trapdoor specialized click-half behavior;
- FrontAndTop ORIENTATION family (Crafter and Jigsaw): both front and top are
  canonical local directions; Crafter runtime output belongs Phase 7A and Jigsaw
  structure semantics belong R6.

Do not create a giant HorizontalDirectionalBlock behavior mixin: only the
property/orientation vocabulary is shared; survival, signals and pairing belong
to different algorithms.

### 2B — support, survival and semantic update boundary

Vanilla mechanisms:
- canSurvive;
- sturdy/support-face queries;
- updateShape;
- neighborChanged when it interprets support direction;
- Block.canSupport* helpers.

Physical NeighborUpdater fan-out remains physical.
The target block must reframe the physical source-target relation into its
canonical local semantics.

Implemented:
- PlanetBlockSupportRuntime / PlanetBlockSupportQuery;
- standing torch local-DOWN support;
- wall torch support/update;
- redstone wall torch support/update only;
- ladder placement/support/update;
- FaceAttachedHorizontalDirectionalBlock placement/support/update;
- pressure plate local-DOWN support/update;
- support-removal runtime acceptance for torch/wall torch/ladder/lever;
- pressure plate support manual PASS.

Family audit still required:
- BushBlock support family;
- BaseRailBlock support;
- lantern/hanging support;
- signs/banners/skulls support families;
- coral/fan attachments;
- scaffolding support/distance;
- diode/repeater/comparator support aspect only;
- any block family doing raw below()/above()/relative(localDirection).

Important:
remapping only updateShape(Direction) is insufficient when the implementation
then performs raw BlockPos.relative(direction). Adapt both semantic direction and
physical traversal at the owning family boundary.

### 2C — multiblock and pair topology

Owns state relationships such as:
- upper/lower;
- head/foot;
- left/right paired block;
- directed pair neighbor.

Implemented:
- door local FACING/hinge/upper-half/survival/pair update;
- bed FOOT->HEAD topology and target-frame FACING;
- pointed dripstone local vertical chain support/thickness/update;
- unsupported stalactite chain scan local DOWN.

Still required:
- DoublePlantBlock family;
- tall seagrass / pitcher / small-dripleaf pair paths;
- chest LEFT/RIGHT pair + DoubleBlockCombiner physical traversal;
- standing/hanging multi-part block families discovered by the source sweep.

Redstone behavior of doors/chests is not owned here.
Block-entity rendering is not owned here.

### 2D — local tangent and multiface connection graphs

Shared primitive:
four seam-aware LOCAL tangent directions, not world
Direction.Plane.HORIZONTAL.

Implemented:
- FenceBlock N/E/S/W placement/update through local tangent neighbors;
- grass/surface seam work that consumes canonical local UP.

Still required:
- WallBlock sides + UP post rule;
- IronBarsBlock;
- TripWireBlock connection topology;
- CrossCollisionBlock family;
- MultifaceBlock / GlowLichen / SculkVein;
- Vine attachment topology;
- Chorus/PipeBlock-style connectivity where local tangent/vertical meaning is
  semantic.

Share traversal primitives, not one universal state mutator: WallSide,
booleans, RedstoneSide and multiface properties have different state rules.

### 2E — runtime growth and ecology

Already implemented:
- SpreadingSnowyDirtBlock survival/spread local UP + seam-aware growth;
- SnowyDirtBlock SNOWY placement/update local UP.

Audit/implement by family:
- BushBlock descendants and crops;
- cactus / sugar cane / bamboo;
- saplings/mushrooms/flowers where support or spread is local;
- GrowingPlantBlock head/body family;
- kelp / cave/weeping/twisting vines;
- DoublePlant growth side after 2C topology;
- vine random spread;
- scaffolding distance graph;
- snow/nylium/farm/path runtime rules;
- leaves/plant emitters only for their NON-particle semantic behavior
  (particle emission stays Phase 4).

Worldgen feature growth is Phase 8, not this runtime phase.

### 2F — rail BLOCK topology

This is the owner of the user's "rails do not place on rotated gravity" class of
bug.

Vanilla owners:
- BaseRailBlock;
- RailState;
- RailShape.

Required as ONE graph implementation:
- local-DOWN support;
- canonical local RailShape;
- initial orientation from local tangent player direction;
- four local tangent rail neighbors;
- same/local-UP/local-DOWN neighbor search;
- ascending rail using local UP;
- curve selection;
- reconnect propagation after add/remove;
- seam-aware graph traversal;
- rail connection identity in local/traversal topology rather than vanilla's
  physical-X/Z column assumption;
- waterlogging scheduling hook (fluid correctness remains Phase 5).

R1 architecture decision:
do not patch RailState method-by-method as the long-term design. Extract a
Planet-owned rail graph helper/service and keep RailState/Mixin wiring thin for
cross-version portability.

Explicitly NOT owned here:
- powered/detector/activator signal behavior -> Phase 7A;
- minecart movement on RailShape -> Phase 7.4;
- rail model/shape rendering -> Phase 3.

Do not patch RailBlock individually. BaseRailBlock + RailState are the shared
algorithm boundary.

### 2G — falling/support-triggered block semantics

Owns block-side trigger/support decisions for:
- FallingBlock family;
- brushable/falling-like blocks;
- pointed dripstone support/fall trigger;
- scaffolding collapse/support;
- analogous modded support-triggered blocks.

Already implemented/accepted pieces:
- falling-block local spawn anchor;
- dripstone local chain fall trigger;
- block-side falling particle origin work where applicable.

The spawned entity's physics/damage belongs to Phase 7.3.
Its particles belong to Phase 4.
Its renderer belongs to Phase 3/entity rendering integration.

### 2H — waterlogged block hooks

Phase 2 only ensures local semantic block update paths do not break:
- WATERLOGGED state;
- scheduled fluid tick preservation;
- LiquidBlockContainer/SimpleWaterloggedBlock hook reachability.

Actual fluid topology/height/flow/render belongs entirely to Phase 5.

### Phase-2 acceptance matrix

Run once the non-fluid block semantic families above are implemented.

Must include representative families, not every individual block:

- +Y vanilla baseline and all five rotated faces;
- one rotated-pillar/axis block;
- one face-attached block + one standing/wall family;
- slab/stair/trapdoor placement;
- door + DoublePlant + bed + chest pair;
- fence + wall + iron bars/multiface representative;
- crop/Bush + directed growing plant + vine/scaffolding;
- ordinary rail: straight, curve, ascending, reconnect and seam crossing;
- falling/support-trigger representative;
- remove actual LOCAL support and verify survival/update;
- exact edge/corner only for mechanisms whose topology owns the seam;
- no duplicate scheduled updates / runaway neighbor recursion.

Phase 2 does not close merely because a list of common blocks can be placed.

## Phase 3 — block geometry and rendering subsystem [R2 RESEARCH COMPLETE; IMPLEMENTATION PARTIAL, BATCH ACCEPTANCE]

### 3A — position-aware shape boundary

Implemented:
- outline/collision/visual/interaction shapes rotate canonical local -> physical
  on outermost bound-Level query;
- nested shape scope prevents double/triple rotation;
- cached six-frame VoxelShape transforms;
- full-block/empty singleton fast paths;
- exact seam shape tests.

Support/occlusion cache policy must remain explicit:
canonical cached state data is never mutated into a position-specific frame.

Completed R2 matrix:
`docs/research/CLIENT_RENDER_PARTICLE_MATRIX_1_21_1.md`

### 3B — static baked models, culling, AO and light

Implemented:
- cached BakedModel wrapper per original model x PlanetFace;
- cached transformed BakedQuad;
- physical renderer side -> canonical local getQuads side;
- rotated vertex positions + packed normals;
- physical final BakedQuad.direction;
- ModelData/RenderType/render-pass delegation;
- frame-aware Block.shouldRenderFace;
- dedicated occlusion cache;
- grass/mycelium local-UP surface seam handling.

R2 decisions:
- AO/light adjacency is already driven by transformed PHYSICAL
  BakedQuad.direction; do not add a second frame rotation;
- breaking overlay uses the same ModelBlockRenderer.tesselateBlock path;
- directional shade remains an R6 environment-policy decision;
- standard OffsetType XZ/XYZ should be authored in canonical local coordinates;
  arbitrary custom OffsetFunction is a compatibility boundary;
- special/custom model paths that bypass standard BakedModel remain Phase-10
  integration work.

### 3C — block entity / custom renderer frame

Static BakedModel rotation does NOT cover BERs.

Existing:
- first BedRenderer local-frame adapter.

Audit renderer families:
- chest;
- standing/wall/hanging sign;
- banner;
- skull/head;
- shulker box;
- bell;
- lectern/book;
- end portal/gateway;
- moving piston;
- other directional BERs.

R2 decision:
do NOT globally transform BlockEntityRenderDispatcher.

Use a shared Planet render-frame API selectively:
- rigid block-local BER family -> root local->world transform is preferred;
- runtime-direction BER -> classify direction data before transforming;
- world/camera-space BER -> dedicated policy/adapter only.

This keeps vanilla renderer internals intact while avoiding double-rotation of
physical hit/camera/world data.

### 3D — moving/accelerated rendering integration

Owners include:
- FallingBlockRenderer alignment;
- MovingPistonBlock/PistonMovingBlockEntity visual geometry;
- custom entity/block moving renderers;
- Flywheel/Create-style accelerated/instanced render paths.

Physics ownership remains in the relevant block/entity/automation phase; this
subphase owns visual frame equivalence.

Fluid renderer is Phase 5C, though it reuses render-frame primitives.

### Phase-3 acceptance matrix

One batch:
- full asymmetric static model all six faces;
- slab/stair/torch/dripstone shapes match visuals;
- no full-cube culling holes at seams;
- AO/light/shade samples correct physical neighbors;
- breaking overlay follows rotated model;
- bed + chest + sign/banner/skull representative BERs;
- falling block + moving piston representative moving renderer;
- ModelData/RenderType compatibility;
- performance/cache smoke test.

## Phase 4 — particle subsystem [R2 RESEARCH COMPLETE; IMPLEMENTATION PARTIAL, BATCH ACCEPTANCE]


### Latest startup evidence — 2026-10-08 (third user attempt)

User's chained test.ps1/run-client.ps1 reached runClient after the reserved
Mixin-package relocation, implying the preceding test process exited
successfully. Client still crashed at ParticleEngine.registerProviders:114:
`WaterCurrentDownParticleGravityMixin.planetary$rotateCurrentSpiral`
had 0/1 matching `@At(INVOKE)` calls when targeting
`Particle.move(DDD)V`.

Root cause belongs to the Phase-4 mixin integration boundary: inherited
method call owners. Source shows `WaterCurrentDownParticle.tick` and
`DragonBreathParticle.tick` both call `this.move`, which encodes the
subclass as the JVM invocation owner. Independent `ShriekParticle.render`
calls inherited `renderRotatedQuad` twice via its subclass name; the
existing `@ModifyArg` incorrectly targeted `SingleQuadParticle`.

This bounded corrective batch:
- [IMPLEMENTED / STARTUP PENDING] retarget WaterCurrentDown and DragonBreath
  tick injected method INVOKEs to their concrete subclass owners;
- [IMPLEMENTED / STARTUP PENDING] retarget Shriek render `@ModifyArg` to
  two Shriek-owned `renderRotatedQuad` calls;
- [IMPLEMENTED / TEST PENDING] extend `ParticleTickInvocationTargetTest`
  to all eight subclass tick owners and compiled `@At` annotation targets;
- [IMPLEMENTED / TEST PENDING] add `ParticleRenderInvocationTargetTest`
  for both Shriek calls and exact `@ModifyArg` annotation;
- [DOCUMENTED] source/callsite research in
  `docs/research/PARTICLE_MIXIN_INVOKE_ANCHORS_1_21_1.md`;
- [UNCHANGED] particle world motion math, accepted Terrain collision, fluids,
  RNG/emitters, capability-provider and topology implementation.

Acceptance gate remains a SINGLE combined build+client smoke checkpoint:
`git pull && .\\test.ps1 && .\\run-client.ps1`.
First verify all bytecode + pure tests, then main menu and Planet login.
No Phase-4 batch gameplay PASS or capability provider runtime acceptance
until that succeeds.



### Follow-up 2026-10-08: JUnit test loader failure — FIX IMPLEMENTED / PENDING RECHECK

User's second log after 0ce172f:
- compileTestJava: completed;
- task :test: FAILED while trying to load
  dev.planetary.mixin.ParticleTickInvocationTargetTest;
- exact root cause: Mixin IllegalClassLoadError, because
  dev.planetary.mixin.* is reserved by planetary.mixins.json.
  No ASM assertion ran; no evidence yet that the actual Java move
  invocation targets are correct or incorrect.

Phase-4 corrective checkpoint:
- move ParticleTickInvocationTargetTest from reserved mixin package to
  dev.planetary.gravity;
- ALSO move PlanetSemanticTickDeltaState runtime helper from reserved mixin
  package to dev.planetary.gravity; use PlanetParticleMoveAccess boundary;
- six hook Mixins import the same shared implementation; no tick math
  or hook descriptors changed;
- enforce reserved package rule in AGENTS.md.

Current gate: new Gradle :test result and client startup are still PENDING.
Do not treat last failed :test as a runtime/gameplay failure, and do not
treat successful compileTestJava as enough evidence of startup correctness.

### Phase-4 startup blocker 2026-10-08 — IMPLEMENTED / STARTUP ACCEPTANCE PENDING

User report after previous a9c683f pull:
- unit tests/compile: BUILD SUCCESSFUL (8 tasks);
- runClient: FAIL at ParticleEngine.registerProviders during Mixin application;
- precise failing injection: SemanticTickDeltaParticleMixin
  planetary$reinterpretTickDeltaBeforeMove; 0/1 successful INVOKEs,
  scanned 0 target(s) for Particle.move(DDD)V.

Root: Java compiles inherited this.move(DDD) from a concrete subclass tick
into INVOKEVIRTUAL owner=the concrete subclass, while generic multi-target
mixin looked for declaring base Particle owner. Six subclasses each have an
actual tick-owned this.move call. See
docs/research/PARTICLE_MATRIX_1_21_1.md for evidence and ownership.

Fix now IMPLEMENTED (not yet startup/manual accepted):
- remove multi-target SemanticTickDeltaParticleMixin;
- six exact 1.21.1 subclass *TickDeltaMixin adapters, each HEAD capture and
  pre-move target bound to its concrete INVOKEVIRTUAL owner;
- shared PlanetSemanticTickDeltaState tracks tick-authored velocity only;
  preserved physical base velocity, +Y/no-Planet pass-through and RNG;
- deterministic ASM test checks actual Minecraft bytecode invocation owners
  plus mixin JSON registration, and fails loudly if hooks drift;
- Particle.move runtime, TerrainParticle accepted collision, fluid/worldgen,
  capabilities and all other subsystems not changed by this fix.

Acceptance gate: first test.ps1 must pass (including new bytecode test),
run-client.ps1 must complete past ParticleEngine init into main menu and
world; only then resume single Phase-4 gameplay matrix. Do NOT mark PASS
before user confirms startup. If the bytecode test shows different owner,
use its reported class/owner to revise only that exact hook.



### Phase policy

All particle work is owned by this phase, even when the concrete emitter lives
in a block/entity/world helper.

Do NOT ask for class-by-class manual testing while this phase is being built.
Research and implement the complete particle subsystem first, then run one
manual acceptance matrix.

Exception: an intermediate checkpoint is allowed only for startup/mixin failure
or when a new shared core boundary cannot be validated deterministically and
substantial later work would depend on it.

Fluid-coupled particles are also audited here. If a final behavior depends on
fluid topology/flow that does not exist until Phase 5, implement the
frame-independent particle part here and mark only the integration gate blocked
by Phase 5. Do not create temporary fake fluid behavior.

### 4.0 Shared particle foundations

Required common mechanisms:

- emitter coordinates:
  - distinguish block-local semantic offsets from physical world coordinates;
  - rotate local UP/DOWN/tangent offsets at the emitter boundary;
  - preserve vanilla RNG count/order;
- acceleration/motion:
  - generic Particle.tick gravity;
  - custom tick gravity/rise terms;
  - local tangent-plane accelerations/drift;
- collision:
  - preserve physical AABB/world storage;
  - when a particle's vanilla semantics depend on vertical-vs-tangent ordering,
    rotate the whole Particle.move semantic unit rather than patching flags
    afterward;
  - local onGround/stoppedByCollision/tangent clipping where the owning class
    actually requires them;
- lifecycle:
  - world-Y / world-XZ stop/remove conditions must be classified as semantic or
    physical before adaptation;
- rendering:
  - only adapt render/anchor logic when source research proves it owns the
    asymmetry; do not use render offsets as a substitute for broken physics;
- environment queries:
  - block/fluid/weather neighbor checks must use the owning local semantic
    direction when appropriate;
- performance:
  - no unbounded per-particle caches;
  - avoid allocations in hot ticks where practical;
  - no duplicate vanilla + Planet emission.

Shared implementation should be generic only when multiple audited particle
families genuinely share the same semantics. Otherwise use explicit allowlists;
do not generalize from one working particle class by assumption.

### 4.1 Accepted foundations / behavior

Already manually verified:

- ordinary block-breaking particles follow local gravity;
- block-destroy particle local-UP launch bias;
- TerrainParticle radial destroy burst;
- TerrainParticle rotated collision behavior:
  - no extra local-floor crawl on +/-X, +/-Z or -Y;
  - accepted boundary is the whole local Particle.move semantic unit;
- standing/wall normal+soul torch flame/smoke origins away from exact edges;
- standing/wall redstone torch visual particle origins away from exact edges.

TerrainParticle accepted architecture evolved into the audited base-engine
boundary:

- LocalGravityParticleMoveMixin targets the BASE Particle.move method;
- subclasses overriding move() bypass it naturally and remain explicit custom
  families;
- the reusable move algorithm lives in PlanetParticleMoveRuntime behind a
  Planet-owned access contract;
- physical AABB/world storage remains vanilla;
- collision shapes are resolved with PlanetEntityCollision in LOCAL
  Y -> LOCAL X/Z order;
- stoppedByCollision/onGround/tangent clipping use local axes;
- version-sensitive vanilla details such as hasNearBlocks are isolated rather
  than spread through particle subclasses.

Rejected approaches that must not be revived:

- post-processing onGround/stoppedByCollision after a world-axis collision solve;
- reconstructing collision movement from newPos-oldPos;
- TerrainParticle render-anchor shifting as a crawl fix.

### 4.2 Implemented, final phase acceptance pending

- FallingBlock client animateTick emits from local DOWN;
- FallingDustParticle acceleration and terminal-speed clamp use local DOWN;
- shared custom-tick semantic-delta adapter plus dedicated audited custom
  families;
- cherry-leaf particle path:
  - local-DOWN emission from CherryLeavesBlock;
  - local support-face test;
  - vanilla RNG order preserved;
  - local tangent wind;
  - local gravity;
  - allowlisted local Particle.move collision;
  - local tangent blocked/removal semantics;
- accepted TerrainParticle collision design generalized to the base
  Particle.move engine boundary; custom move overrides remain separately
  audited.

Exact-edge torch emitter mismatch remains deferred to the generic
player/body-vs-canonical BlockState edge policy. Do not special-case torch
geometry.

### 4.3 Full source audit before any more manual testing

Audit every vanilla 1.21.1 particle class and every vanilla emitter/helper that
contains any of these assumptions:

- direct x/y/z or xd/yd/zd semantic manipulation;
- gravity/rise hard-coded to world Y;
- horizontal/tangent behavior hard-coded to world X/Z;
- onGround or y==yo style vertical checks;
- pos.above()/below(), Direction.UP/DOWN, or y +/- constant emitter placement;
- ParticleUtils helpers with world-axis meaning;
- block/fluid/environment queries coupled to the particle;
- custom move() or custom tick() that bypasses Particle.tick;
- weather/rain/splash origins;
- nested particle emission from another particle;
- renderer logic whose meaning depends on vertical orientation.

For each audited class/path, classify it as one of:

1. frame-independent: no Planet change;
2. base Particle.tick compatible: inherited shared fix is sufficient;
3. shared local-move compatible: add to an explicit allowlist only after tests;
4. custom tick: dedicated local-frame adapter;
5. emitter-only adaptation;
6. fluid-coupled: particle-side implementation here, final integration gate
   after Phase 5;
7. intentionally world-physical: document why no rotation is correct.

### 4.4 Known families to cover in this audit

At minimum include:

- TerrainParticle / block destroy;
- FallingDustParticle;
- torch / wall torch / soul torch;
- redstone torch;
- CherryParticle + CherryLeavesBlock/ParticleUtils.spawnParticleBelow;
- CampfireSmokeParticle + CampfireBlock emitter;
- DragonBreathParticle and all emitters that supply its initial velocity;
- DripParticle family;
- WaterDropParticle;
- WakeParticle;
- BubbleParticle / BubblePopParticle / BubbleColumnUpParticle;
- WaterCurrentDownParticle;
- SplashParticle and fluid splash emitters;
- lava-related particles;
- smoke/ash families that use speedUpWhenYMotionIsBlocked;
- spell/trial-spawner particles with negative gravity/rise;
- crit/breaking-item/snowflake/explode and other base-gravity users;
- weather/rain particles;
- firework nested emissions;
- portal/reverse-portal/fly-towards-position classes with custom movement;
- any remaining ParticleEngine-registered vanilla particle class found by the
  full registry/source sweep.

This list is a minimum audit matrix, not a whitelist. The source sweep decides
the final set.

### 4.5 Fluid dependency rule

Particle classes that query FluidState or model bubble/current/drip behavior are
still owned by Phase 4 for:

- local-frame acceleration;
- emitter orientation;
- custom tick axis semantics;
- collision/lifecycle semantics independent of fluid topology.

However, final correctness of:

- flow/current direction;
- fluid surface/inside tests under Planet topology;
- fluid-render coupling;
- waterlogging-dependent emission;
- lava/water topology interactions

is blocked by Phase 5 and will receive an integration re-check there.

### 4.6 Single final particle acceptance matrix

Do not request manual testing until the non-blocked Phase 4 implementation is
complete.

The final pass must cover, in one run:

- +Y vanilla baseline;
- +/-X, +/-Z and -Y;
- block destroy;
- falling dust / falling-block visual particles;
- all torch variants;
- cherry leaves;
- campfire smoke;
- dragon breath;
- representative generic gravity particles;
- representative custom-tick rise/fall particles;
- weather/rain;
- representative fluid-coupled particles for the portions not blocked by
  Phase 5;
- no physical +Y drift;
- no duplicate vanilla/custom emission;
- no premature removal/freeze;
- no extra surface crawl where +Y does not have it;
- performance smoke check with many particles;
- exact-edge cases only where the particle mechanism itself owns the edge
  behavior; generic block edge-policy remains deferred.

Phase 4 closes only when this matrix passes, except for explicitly documented
Phase-5-blocked fluid integration gates.

## Phase 5 — fluid subsystem [R3 RESEARCH COMPLETE; PLANNED IMPLEMENTATION, BATCH ACCEPTANCE]

Detailed research:
- `docs/research/FLUIDS_1_21_1.md`
- `docs/research/FLUID_MECHANISM_MATRIX_1_21_1.md`

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

Hard requirements from R3:
- build one Planet-owned fluid graph/context on top of PlanetBlockStep;
- carry semantic local direction separately from physical edge direction;
- replace vanilla slope-cache identity that assumes physical X/Z;
- use frame-complete wall-occlusion caching;
- preserve NeoForge CreateFluidSourceEvent / FluidType / BaseFlowingFluid /
  FluidInteractionRegistry hooks;
- keep bucket hit-direction physical;
- keep waterlogging/container same-position behavior intact;
- render fluids through a dedicated local mesh algorithm, not BakedModel rotation;
- defer entity immersion/body response to Phase 7 while keeping FluidState.getFlow
  as a physical world vector.

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

## Phase 6 — navigation and AI subsystem [R5 RESEARCH COMPLETE; IMPLEMENTATION PARTIAL, BATCH ACCEPTANCE]

Completed R5 matrix:
`docs/research/AI_AUTOMATION_MATRIX_1_21_1.md`

This phase owns navigation graphs, steering and AI target-generation semantics,
not generic entity physics.

Dependencies:
- Phase 1 traversal charts;
- Phase 2 support/door/fence/rail semantic graphs;
- Phase 7.1 entity body/collision semantics;
- Phase 5 for aquatic navigation closure.

Existing:
- PlanetWalkNodeEvaluator first pass;
- local MoveControl target interpretation;
- path node local anchor;
- local random-stroll experiment.

R5 architecture decisions:
- navigation node identity is (physical BlockPos + traversal chart), not xyz-only;
- retain vanilla PathFinder A* where possible, but use a Planet-owned node cache
  and chart-aware Path/navigation metadata;
- edge waypoint overshoot and ordinal MoveControl redirects are experimental and
  must not become the final boundary;
- WalkNodeEvaluator floor/step/drop/body volume/diagonals are one local-ground
  graph family;
- steering projects physical targets into the entity body frame;
- RandomPos and MoveToBlockGoal are shared target-generation boundaries;
- preserve NeoForge BlockState/FluidState path-type callbacks;
- aquatic/amphibious closure still depends on Phase 5.

### 6A — chart-aware node graph and floor semantics

Implement together:
- PlanetNavigationNodeKey = physical BlockPos + traversal chart;
- Planet navigation Node subclass/cache;
- WalkNodeEvaluator-equivalent local tangent/floor/step/drop semantics;
- seam-aware diagonals;
- body-frame candidate AABB/clearance;
- chart-aware Path equality/waypoint/timeout metadata;
- vanilla PathFinder retained where compatible.

### 6B — steering/body controls

- replace ordinal MoveControl coordinate redirects with stable Planet steering;
- physical target delta -> body-local tangent/vertical;
- MoveControl / LookControl / BodyRotationControl / FlyingMoveControl;
- output physical movement/body orientation under the R4 entity contract.

### 6C — target generation and common ground goals

Shared families:
- RandomPos local tangent/local vertical generation with RNG-order preservation;
- MoveToBlockGoal local-volume search;
- chase/flee/melee inherit PathNavigation.

Explicit bypass adapters:
- DoorInteractGoal;
- EatBlockGoal;
- RemoveBlockGoal;
- local-UP bed/powder-snow cases;
- BreathAir/TryFindWater after Phase 5.

Do not patch every Goal independently.

### 6D — navigation modes

Separate gates:
- ground;
- wall climber;
- flying;
- amphibious;
- aquatic after Phase 5.

### Phase-6 acceptance matrix

- random stroll;
- chase/flee;
- step up/down;
- door/fence/rail hazards;
- every representative seam direction both ways;
- stop on edge without spinning;
- multiple target generators;
- controlled many-mob path-search performance;
- maxVisitedNodes/no search explosion.

## Phase 7 — entity physics, locomotion and vehicles [R4 RESEARCH COMPLETE; PLANNED IMPLEMENTATION, BATCH ACCEPTANCE]

Completed R4 matrix:
`docs/research/ENTITY_BODY_NETWORK_MATRIX_1_21_1.md`

A correct player walking baseline does not close this phase.

R4 architecture invariant:
- `Entity.position`, physical AABB, `deltaMovement`, `Entity.move`
  displacement and external world forces remain PHYSICAL world-space;
- local vertical/tangent semantics are temporary projections inside Planet
  body/locomotion/collision algorithms.

Migration requirement before broad Phase-7 expansion:
the current partial runtime stores `deltaMovement` in local coordinates while
inside a Planet field. R4 rejects that as the long-term boundary because
vanilla/NeoForge networking, explosion, leash, ProjectileUtil, Entity.push and
third-party entities consume it as physical XYZ.

This does NOT revoke manually accepted player movement/camera/fall behavior.
Those behaviors remain the target; only the internal storage/adaptation
boundary must be replaced.

### 7.1 — generic entity/body collision core

Owns:
- Entity.move local vertical/tangent semantics;
- collision ordering;
- step height;
- onGround/verticalCollision/horizontalCollision;
- supporting block/floor position;
- fall distance/landing/fall damage;
- pose/bounding-box body frame;
- suffocation/push-out interactions.

Physical world AABB/collision geometry stays physical.

Existing `PlanetEntityCollision` is retained as the generic collision
foundation: physical colliders/AABB, local-axis solve, physical displacement
result.

Existing accepted foundations include player local movement/camera baseline,
packet fall-damage adaptation and several falling-entity fixes. R4 research is
complete, but implementation remains partial and must first migrate away from
local `deltaMovement` storage.

### 7.2 — living locomotion

Audit as mechanism families:
- jump;
- gravity effects/levitation/slow-fall;
- sprint/crouch/crawl;
- climbing;
- swimming and fluid drag (closure depends Phase 5);
- elytra/fall flying;
- riptide;
- powder snow;
- honey/slime;
- local tangent friction.

### 7.3 — non-living entities and projectiles

Audit:
- ItemEntity;
- ExperienceOrb;
- TNT;
- FallingBlockEntity;
- arrows/tridents/throwables;
- fireballs/wind charges;
- fishing hook;
- armor stands/displays where physical orientation matters.

Separate:
- gravity vector;
- class-specific bounce/friction/float;
- projectile launch/view boundary;
- projectile collision/deflection.

Already accepted falling-block work remains recorded here as partial coverage,
not proof for the whole family.

### 7.4 — vehicles

Minecart:
- depends on Phase-2 RailState topology;
- RailShape exit vectors and ascending offsets become local semantic geometry;
- tangent speed/horizontalDistance assumptions must rotate;
- powered/detector rail signal remains Phase 7A.

Boat:
- depends on Phase 5;
- water surface sampling;
- buoyancy;
- land friction;
- passenger placement;
- bubble/current effects.

### 7.5 — passengers, attachments and forces

Audit:
- riding/seat offsets;
- leash anchors;
- dismount floor search;
- sleeping attachment;
- shoulder entities;
- knockback;
- explosion impulses;
- piston pushes;
- mace/ram/wind-charge forces.

Do not rotate an arbitrary physical world force merely because gravity differs.
Only gravity/body-relative components rotate.

### Phase-7 acceptance matrix

One batch with dependency-marked cases:
- players/living entities;
- item + XP + TNT + falling block;
- projectile launch/fall/impact;
- climb/swim/elytra representatives;
- minecart straight/curve/slope/seam after Phase 2F;
- boat after Phase 5;
- passenger/dismount/leash;
- knockback/explosion/piston force;
- +Y vanilla baseline;
- all rotated faces;
- client/server agreement rechecked in Phase 7B.

## Phase 7A — signals, automation and sided logistics [R5 RESEARCH COMPLETE; PLANNED IMPLEMENTATION, BATCH ACCEPTANCE]

Completed R5 matrix:
`docs/research/AI_AUTOMATION_MATRIX_1_21_1.md`

Depends on canonical block semantics/topology from Phase 2.

### 7A.1 — generic signal-query boundary

- SignalGetter physical six-neighbor source selection remains PHYSICAL;
- the Direction argument passed to source-state getSignal/getDirectSignal and
  NeoForge shouldCheckWeakPower is reframed into the SOURCE canonical frame;
- physical NeighborUpdater / NeighborNotifyEvent fan-out remains physical;
- paired/local graphs choose physical positions before calling SignalGetter.

### 7A.2 — redstone graph families

Wire:
- four local tangent directions + local-UP/local-DOWN climb topology;
- target canonical side for NeoForge canRedstoneConnectTo;
- physical generic notifications remain physical.

Diode/repeater/comparator:
- local FACING forward + clockwise/counter-clockwise side inputs;
- comparator one/two-block forward traversal and hanging-item-frame integration.

Separate long-line graph:
- tripwire uses PlanetBlockWalk with transported direction.

Calibrated sculk:
- local back-input topology only; vibration propagation remains physical.

Ordinary sources (observer, torch, lever/button/plate, target, lectern, sculk,
lightning rod, etc.) inherit the generic signal boundary where possible.

### 7A.3 — rail signal behavior

Consumes the Phase-2 Planet rail graph; do not duplicate RailShape traversal.

Owns:
- PoweredRailBlock recursion through rail graph, including local slopes/seams;
- DetectorRailBlock output plus canonical-local detection AABB -> physical AABB;
- activator/powered state signal aspect.

Minecart motion remains Phase 7.4.

### 7A.4 — piston and moving automation

Implement as PlanetPistonPushGraph:
- local/quasi power probe over ordinary signal boundary;
- transported push line through PlanetBlockStep;
- slime/honey branch graph;
- max-12 and vanilla move/destroy ordering;
- preserve NeoForge canStickTo/isSlimeBlock/PistonEvent hooks.

Cross-phase prerequisite:
add PlanetBlockStateTransport for safely reframing supported oriented BlockState
properties when a moved block crosses a gravity seam.

Entity pushes integrate Phase 7.5; renderer integrates Phase 3D.

### 7A.5 — sided logistics and NeoForge capability integration

Two stages:
1. topology-aware source semantic direction -> correct PHYSICAL target;
2. physical shared face -> TARGET canonical side.

Apply to:
- vanilla WorldlyContainer;
- NeoForge ItemHandler capability;
- hopper output/local-UP suction and item collection AABB;
- dropper insertion;
- crafter FrontAndTop.front output;
- dispenser emission.

Existing capability-local adaptation is now EXPLICIT per receiving provider,
not generic/global. It cannot repair an upstream caller that already chose
the wrong physical target; physical is the default for foreign providers.

Dispenser item launch authors its vanilla upward bias in local UP, then emits a
physical entity velocity. Projectile dispenser hands physical spawn/forward to
the Phase-7 projectile contract.

### Phase-7A acceptance matrix

- wire/repeater/comparator/observer on all faces;
- lever/button/plate/torch signals;
- powered/detector/activator rail network;
- piston extend/retract/push/slime-honey/entity push;
- exact-edge signal/capability cases where topology owns the seam;
- item/fluid/energy sided provider;
- Create-like directional machine/pipe stress test;
- no duplicate neighbor-update storms.

### R6 completion and implementation handoff (research only)

Canonical matrix: docs/research/ENVIRONMENT_WORLDGEN_STRUCTURE_COMPAT_MATRIX_1_21_1.md

Mechanisms / acceptance ownership:
- 7B.3: PlanetSpawnCandidateProvider and PlanetSpawnSafetyQuery; preserve
  biome/entity registered rules and mob caps; stop selecting all-face
  candidates from vanilla physical XZ heightmap.
- 7B.4: PlanetSurfaceQuery/Index, PlanetClimateQuery and
  PlanetPrecipitationQuery; server deposition, client weather, lightning
  candidates/rods and SkyLightEngine are separate algorithms. Product target
  is natural six-face exterior daylight, not merely a rotated client sky.
- 8A: generation-space BiomeResolver/Climate.Sampler; keeping BiomeSource
  serializable is not enough to sample continuous climate on cube seams.
- 8B-8D: retain continuous macro relief, constant radial SEA_SHELL, bounded
  coast changes, post-terrain drainage and piecewise-flat river reaches.
- 8E: field noise WRAP versus local chart/rigid feature placement; preserve
  physical ChunkPos and PlacedFeature ordering, seed and biome modifiers.
- 9A: AVOID_EDGE final whole StructureStart bounding box before registration,
  structure refs/locate integrity, jigsaw/GravityProcessor local surface.
- 9B: rigid locally vertical portal plane, seam reject, separate entity
  transition/orientation. End/gateway are distinct vanilla path families.
- 10: versioned generator/profile codec and saved-world fingerprint/migration;
  modified biome registries, TerraBlender/BOP/BYG early gates; public frame
  API and opt-in feature/processor contracts, instrument chunk/spawn/light
  hotspots and keep vanilla non-Planet pass-through.

Critical newly discovered contracts:
1. Heightmap 16x16 physical XZ->Y cannot encode six Planet surfaces.
2. PlanetGenerationSpace is C0 continuous; seam gradient and voxelized
   geometry are unproven and need independent tests.
3. getSeaLevel is physical/global-Y, not the radius of a constant sea shell.
4. Arbitrary modded integer-grid Features cannot be made seamless simply by
   mapping coordinates and rounding; explicit classification is required.
5. A radial sun/skylight implementation may require a lighting engine adapter,
   not an inexpensive gravity/heightmap mixin.
6. Stock dimension build limits cannot accommodate literally infinite +Y/-Y
   exterior shells. Define a finite playable envelope/portability policy.
7. Structure selection, early bbox validation, persisted starts/references and
   chunk stage ordering must agree; do not reject after writing blocks.
8. Current generator still has no real decorations, carvers or initial mobs.

Status: research done, NO Phase-7B/8/9/10 runtime PASS claimed. Next:
FINAL GLOBAL COMPLETENESS SWEEP R1-R6, then frozen architecture and a
dependency-ordered implementation batch plan. Runtime remains frozen.

## Phase 7B — interaction, networking, spawn and environment policy [R4+R6 RESEARCH COMPLETE; RUNTIME ACCEPTANCE PENDING]

This phase contains mechanisms that must agree with the entity/body frame but are
not ordinary movement physics.

### 7B.1 — eye/view/raycast/interaction [R4 RESEARCH COMPLETE]

R4 invariant:
one physical world ray is produced from body-local eye/view semantics.
`BlockHitResult` stays physical across networking; Phase 2 converts the
physical side only when block semantics require a canonical local side.

Audited:
- eye position/eye height;
- body up/view vector;
- yaw/pitch;
- Entity.pick;
- Level.clip;
- ProjectileUtil;
- BlockHitResult physical side;
- item POV hit helpers;
- reach/crosshair;
- first/third-person interaction consistency.

Rule:
one physical world ray is produced from body-local view.
BlockHitResult stays physical; Phase-2 placement consumes its canonical-local
interpretation.

### 7B.2 — client/server prediction and validation [R4 RESEARCH COMPLETE]

R4 invariant:
packet/teleport XYZ remains physical. Jump/floating/fall/onGround checks derive
their semantic vertical component by projecting physical displacement into the
entity body frame.

Gravity-face/body-frame agreement is a network gate: current hysteresis-based
selection requires either proven deterministic client/server equivalence or an
explicit synchronized authoritative face identifier.

Audited together:
- LocalPlayer prediction;
- ServerPlayer state;
- ServerGamePacketListenerImpl floating/flying checks;
- movement packets;
- vehicle packets;
- teleport correction;
- onGround/fall state sync;
- yaw/pitch sync/interpolation.

Existing packet fall-damage fix is partial evidence only.

### 7B.3 — spawn placement

Audit:
- SpawnPlacementTypes;
- NaturalSpawner;
- mob-specific placement;
- spawn eggs;
- world/respawn/bed spawn.

Important:
vanilla heightmaps are XZ->Y and cannot automatically represent six planet
surfaces. Define a Planet surface candidate API rather than pretending vanilla
heightmaps are local-gravity maps.

### 7B.4 — environment/weather/sky/heightmap policy

Explicit product decision required for:
- precipitation direction/columns;
- snow/ice formation;
- skylight;
- sky visibility;
- lightning;
- clouds;
- heightmaps;
- build-height constraints;
- temperature/elevation rules.

Gravity-local and global environment semantics are not the same thing.

Do not rotate skylight/weather simply because player gravity rotates.

### Phase-7B acceptance matrix

- ray/camera/server hit agreement;
- no movement rubber-band/floating false positives;
- teleport/rotation consistency;
- spawn on representative local surfaces according to chosen policy;
- weather/environment behavior matches documented product policy;
- +Y vanilla-equivalent baseline where policy says it should.

## Phase 8 — real terrain/biome generation [R6 RESEARCH COMPLETE; PARTIAL RUNTIME FOUNDATION]

Existing:
- PlanetGenerationSpace continuous mapping
- WRAP / AVOID_EDGE policy
- dedicated PlanetChunkGenerator
- fixed plains test terrain

### 8A — standard biome/worldgen composition

Architectural rule:
Planetary owns planet topology and the terrain-space adapter, but must NOT own a
closed registry/list of biomes.

Target pipeline:
    standard/data-driven BiomeSource / climate selection
        -> selected vanilla or modded Biome
        -> Planet terrain density/surface
        -> ordinary biome decoration/features/spawns where safe

Keep standard Minecraft/NeoForge worldgen boundaries whenever possible:
- BiomeSource remains pluggable/data-driven;
- biome feature/decorations should continue through the ordinary biome pipeline;
- biome colors, vegetation and mob spawn lists remain properties of the biome;
- NeoForge/datapack biome additions should not require Planetary to enumerate
  their biome IDs.

Compatibility acceptance must happen EARLY, before Phase 8 is considered stable:
- vanilla biome source;
- TerraBlender-style biome composition;
- Biomes O' Plenty;
- Oh The Biomes We've Gone;
- a representative datapack that adds biome features;
- a representative modded structure/placed feature.

World-type/profile composition:
- Planet Normal
- Planet Large Biomes
- Planet Amplified

These must remain Planet generators, not swap back to a vanilla ChunkGenerator.
Large Biomes should primarily change climate/biome spatial scale.
Amplified should primarily change relief/peak/erosion amplitudes while retaining
Planet topology, macro geography and hydrology.

Do not promise transparent compatibility with a mod/datapack that replaces the
entire ChunkGenerator or performs custom global-Y terrain math. Those require a
Planet terrain-profile adapter or explicit compatibility layer.

### 8B — macro terrain / mean-elevation field

Add a smooth, global Planet macro field that can bias the MEAN elevation of land
toward face centers without creating six deterministic identical mountains.

Requirements:
- use one continuous planet-space function, not six disconnected per-face
  functions;
- no canonical-face/tie dependency in the macro field;
- face-center bias only modulates statistical terrain baseline/amplitude;
- continentalness, erosion, peaks and local noise remain capable of producing
  oceans, plains or mountains anywhere;
- exact edge/corner continuity is mandatory.

Conceptual decomposition:
    PlanetMacroField
        + continentalness
        + erosion
        + peaks/weirdness
        + local detail
        -> BASE TERRAIN

The macro dome must NOT directly determine shoreline or river height.

### 8C — oceans and constant sea shell

Decision:
do NOT make ocean level follow the macro dome.

Ocean sea level is one constant Planet elevation / cube-shell radius:
    SEA_SHELL = constant

Consequences:
- within one gravity face, ocean surface is locally flat;
- across a cube edge, the sea shell bends only at the same topological/gravity
  transition as the planet itself;
- no periodic one-block water staircase;
- no special boat auto-step/lift hack;
- no continuously sloped custom FluidState required.

Land/coast behavior:
- BASE TERRAIN is generated independently;
- shoreline is where terrain intersects the constant sea shell;
- optional coast shaping is LOCAL to a bounded coastal band;
- never define all inland elevation as distance-from-ocean;
- high-relief coasts may remain cliffs/fjords rather than being forced into a
  giant smooth ramp.

This prevents oceans from turning the whole continent into a single mound whose
entire interior slopes toward water.

### 8D — hydrology / rivers / lakes

Hydrology is derived AFTER the base macro terrain exists.

Required order:
    BASE TERRAIN
        -> slope/downhill/drainage analysis
        -> drainage network / flow accumulation
        -> river & lake selection
        -> LOCAL channel/floodplain/canyon carving

Critical rule:
river distance must NOT be a global terrain-height function.
A river locally modifies existing terrain; it does not force every block between
two rivers to become a hill.

Water-level policy:
- no continuously sloped/one-block-step river surface as the default;
- rivers use long piecewise-constant water-level reaches;
- elevation drops are concentrated into intentional rapids, waterfalls, gorges
  or cascades;
- large navigable rivers prefer low-gradient routes and long flat reaches;
- mountain streams are allowed to be non-navigable upstream;
- lakes use their own constant local Planet-elevation levels;
- deltas/lowland reaches converge toward the constant sea shell.

Terrain response is local and classification-based:
- small terrain/water difference -> ordinary channel / floodplain;
- medium difference -> incised valley;
- large difference -> canyon/gorge;
- sharp elevation transition -> rapids/waterfall.

River carving width/depth is bounded by river class and local context. If a
proposed water level would require an absurd continent-wide trench, choose a
different route/classification rather than dragging surrounding terrain down.

Seams:
- drainage/elevation calculations operate in PlanetGenerationSpace / Planet
  elevation, never physical global Y;
- rivers/lakes may cross gravity edges continuously;
- the water surface at an edge uses the same Planet elevation on both faces.

### 8E — caves, features and structures

Need:
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
- macro mean elevation changes smoothly toward/away from face centers
- ocean remains one constant sea shell; no artificial water staircase
- lowland major river supports long boat-friendly flat reaches
- steep river elevation loss becomes intentional rapids/waterfalls/canyons
- river carving does not create repetitive hills between every pair of rivers
- river/lake crosses representative gravity edge coherently
- cave crosses edge as one cave
- ore/feature crosses edge once, no duplicate generation
- deterministic seed/reload behavior
- BOP/BYG/TerraBlender-style biome composition acceptance before phase closure

### 8F — far terrain / LOD rendering

Goal:
allow terrain visibility on the order of ~1000 blocks and potentially farther
without loading/simulating all distant chunks as real Minecraft chunks.

Do NOT implement this as "load a normal chunk and delete everything except the
top 1-2 blocks". Avoid creating the expensive data in the first place.

Distance layers should be conceptually separated:
- near: ordinary full chunks, blocks, entities, fluids, ticks, block entities,
  collision and full lighting;
- mid: ordinary/static chunk rendering or moderately simplified terrain;
- far: render-only Planet surface LOD with no simulation.

Far representation should sample the SAME deterministic worldgen inputs but only
what is visually necessary, e.g.:
    FarSurfaceSample(
        planetElevation,
        surfaceMaterial,
        underSurfaceMaterial,
        biome,
        coarse light/color
    )

Far LOD explicitly omits:
- entities and AI;
- block entities;
- scheduled/random ticks;
- fluids simulation;
- collision;
- caves/ores that are not visible from the exterior;
- full per-block chunk storage.

Preferred renderer architecture to research:
- quadtree / geometry clipmap / concentric LOD rings;
- progressively coarser surface sampling with distance;
- mesh generation in PlanetGenerationSpace, seam-aware across cube edges;
- lightweight impostors/coarse treatment for distant trees/structures only if
  visually necessary;
- shared seed/noise path with full chunks so approaching terrain converges to the
  same surface without visible reshaping.

Illustrative target, subject to profiling:
- simulation: ~8-12 chunks;
- full block rendering: ~16-24 chunks;
- far surface LOD: ~64+ chunks (~1000+ blocks) with progressively coarser mesh.

Performance acceptance must measure CPU generation, GPU triangles, memory,
upload bandwidth and movement-induced remeshing. Far-distance targets are goals,
not guarantees.

## Phase 9 — structures and rigid runtime topology [R6 RESEARCH COMPLETE; RUNTIME PLANNED, BATCH ACCEPTANCE]

### 9A — generated structures

Default policy:
rigid structures use AVOID_EDGE unless explicitly designed for Planet topology.

Required:
- cheap origin clearance;
- final StructureStart bounding-box validation;
- data-driven per-structure safety margin;
- placed-feature vs structure classification;
- structure processors using heightmaps/global Y mapped through Planet
  generation-space policy where appropriate.

Acceptance:
- villages/temples never bend across edge;
- no half structure/duplicate start;
- representative modded structure opt/config policy.

### 9B — runtime rigid topology / portals

PortalShape and similar algorithms are not ordinary block placement.

PortalShape hard-codes:
- horizontal portal axis;
- repeated world UP;
- below search;
- rigid width/height rectangle;
- entity-height Y mapping.

Decide and implement:
- portal plane in canonical local vertical semantics;
- portal rectangle stays physically rigid;
- seam policy (normally do not bend a portal across an edge);
- portal completion/update;
- entity relative-position/orientation transition integrates Phase 7B.

Also audit analogous rigid runtime multiblock machines.

### Phase-9 acceptance matrix

- generated structure edge avoidance;
- structure processor height/elevation;
- nether portal on representative rotated faces;
- invalid seam-crossing portal policy;
- entity transition orientation through portal;
- modded rigid multiblock integration representative.

## Phase 10 — mod compatibility [R6 RESEARCH COMPLETE; PARTIAL RUNTIME FOUNDATION, CONTINUOUS GATE]

This is not an "after everything" cleanup phase. Every owning subsystem must run
its standard Minecraft/NeoForge compatibility gate while it is implemented.
Phase 10 owns public frame APIs, explicit integration modules and the final
cross-mod matrix.

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


Worldgen compatibility contract:
- Planetary owns topology, PlanetGenerationSpace mapping, macro terrain
  adaptation, sea-shell/hydrology seam policy and Planet-specific structure
  safety;
- Planetary does NOT own biome registries, biome colors, vegetation/spawn lists
  or a hard-coded set of allowed mod biomes;
- preserve standard BiomeSource + biome-decoration paths so biome mods/data packs
  can compose where they use ordinary extension points;
- test TerraBlender/Biomes O' Plenty/Oh The Biomes We've Gone during Phase 8,
  not as an afterthought in Phase 10;
- expose Normal/Large-Biomes/Amplified-like Planet terrain profiles through
  data-driven codecs/settings;
- complete ChunkGenerator replacements/custom global-Y terrain algorithms are
  explicit compatibility work, not assumed automatic.

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


## Fall-damage investigation gate
Detailed research: docs/research/FALL_DAMAGE_1_21_1.md.

Manual acceptance showed both ordinary player fall damage and falling-stalactite
damage still fail on rotated gravity after the first Entity.move fall-distance
adapter.

Research found ServerPlayer is a separate vanilla path:
ServerPlayer.checkFallDamage is empty; ServerGamePacketListenerImpl calls
ServerPlayer.doCheckFallDamage with PHYSICAL packet dx/dy/dz. The
handleMovePlayer -> doCheckFallDamage boundary now reframes the whole delta into
the player's local gravity frame.

FallingBlockEntity uses ordinary Entity.checkFallDamage, so its remaining
pointed-dripstone failure is NOT being patched speculatively. Temporary focused
FallTrace diagnostics record terminal-tip arming, local fallDistance accumulation,
landing state, hurtEntities and exact/nearby target AABB counts. Remove or reduce
these traces once the failing gate is identified.


### Falling-block spawn-anchor follow-up
Fall-damage diagnostics identified a generic spawn geometry bug after player and
player-target dripstone damage passed.

Vanilla FallingBlockEntity.fall anchors at the center of WORLD-DOWN block face.
On rotated gravity that produced a 0.5-block tangent offset. FallingBlockEntity
creation now uses the center of LOCAL-DOWN face instead:
source center + 0.5 * physical(local DOWN).

This is generic for dripstone/sand/gravel/anvils. Temporary FallTrace logging was
removed after identifying the failing gate. Manual mob-hit and falling-block
alignment acceptance remains pending.


### FallingBlockRenderer anchor alignment
Manual acceptance after the physical falling-block anchor fix confirmed damage
and mob hits, but sand/anvils still visibly shifted by 0.5 block while falling.

FallingBlockRenderer has independent world-DOWN assumptions. A client adapter now
derives its render BlockPos and PoseStack translation from the same local-DOWN
anchor invariant as FallingBlockEntity physics. Runtime acceptance pending.


### Block break particle +Y drift
Manual acceptance after falling-block closure found TerrainParticle destruction
sprites drifting toward physical +Y on +/-X and +/-Z.

Exact cause: generic Particle construction and Particle.setPower contain a
hard-coded +0.1 world-Y "upward" velocity bias. The radial block-destroy impulse
is already physical and is intentionally left unchanged.

ParticleGravityMixin now converts that +0.1 semantic UP bias to local UP and
generalizes setPower around localUP*0.1. Runtime acceptance pending.
