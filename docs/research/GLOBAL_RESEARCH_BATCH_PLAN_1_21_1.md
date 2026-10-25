# Global gravity research batch plan — Minecraft 1.21.1

Status: active planning/research stage before further runtime implementation.

Purpose:
finish the architecture/mechanism map for the whole mod in bounded, substantial
research batches. Do not interleave runtime implementation while a research
batch is still discovering ownership/mechanisms.

This document controls HOW the remaining global audit is completed.

## Rule

For each research batch:

1. inventory the relevant vanilla/NeoForge classes and registries;
2. run broad source searches for gravity-axis assumptions;
3. manually inspect representative complete call flows;
4. classify physical vs canonical-local vs traversal-chart vs body-local vs
   generation/environment semantics;
5. identify stable engine/base-family/algorithm-family boundaries;
6. identify cross-phase dependencies;
7. identify version-sensitive port hotspots;
8. define deterministic test coverage and one future batch acceptance matrix;
9. update GRAVITY_MECHANISM_MAP_1_21_1.md and IMPLEMENTATION_PLAN.md;
10. record unresolved product-policy questions explicitly.

NO runtime code changes inside a research batch unless the user explicitly
changes this policy.

After all batches complete:
- perform a final completeness sweep;
- freeze the architecture map for the first implementation pass;
- resume implementation phase-by-phase/mechanism-by-mechanism;
- use batch acceptance, not per-class gameplay testing.

## Batch R1 — block/world semantic topology [COMPLETE]

Result:
`docs/research/BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md`

Completion findings are merged into the mechanism map and Phase-2 roadmap.

Scope:
- UseOnContext / BlockPlaceContext / BlockItem;
- Directional/HorizontalDirectional/RotatedPillar families;
- FaceAttached and standing/wall families;
- support/survival/sturdy-face semantics;
- updateShape / neighborChanged interpretation;
- NeighborUpdater physical fan-out boundary;
- multiblock and pair topology;
- CrossCollision / Wall / Multiface / Vine connection graphs;
- growth/ecology families;
- scaffolding;
- BaseRailBlock / RailState / RailShape;
- support-trigger/falling block-side logic;
- waterlogged block hooks (without fluid simulation itself).

Output:
complete Phase-2 ownership map, family list, dependencies to render/redstone/
entity/fluid phases, and a Phase-2 representative acceptance matrix.

## 2026-10-25 Phase3 `3R-2d`: static/dynamic cache/occlusion and foreign getter research

[4/4 research](PHASE3_R2D_DYNAMIC_SHAPE_CACHE_OCCLUSION_SUPPORT_1_21_1.md), [28-row source/ABI/mod bypass matrix](PHASE3_R2D_DYNAMIC_SHAPE_CONTEXT_OWNER_MATRIX_1_21_1.tsv), selected target JVM ASM [CI #40145655409](https://github.com/Abbygree11/Planetary/actions/runs/40145655409) SUCCESS. `BlockStateBase.Cache` canonical global state, conditional `dynamicShape`, face/support and `CollisionContext` callers source-traced, selected exact target ABI captured. **3A STILL OPEN**: dynamic state registry census and mod/foreign world real JAR/source and invalidation, next `3R-2e` RESEARCH ONLY before 3R-3, no runtime code changes.

## 2026-10-25 Phase3 `3R-2c` six original shape API JVM owner checkpoint

[Full report](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.md), [six-shape registered class atlas](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.tsv), [dual-archive verified script](../../scripts/research/phase3_shape_six_owner_verify.py). Original NeoForge21.1.215 Java21 class reflection `getVisualShape/getInteractionShape/getOcclusionShape/getBlockSupportShape` from [CI #40136919659](https://github.com/Abbygree11/Planetary/actions/runs/40136919659) green (test-only code), all 241/1060 real registry items; original preceding two method owners from phase2 artifact. This resolves six **declaring-owner** APIs, NOT dynamic BlockStateBase.Cache, different overload/callers, mod bypass or 3B–3E. **Next `3R-2d` research-only**. Do not move to runtime implementation or claim full Phase3 acceptance.

## 2026-10-25 Phase3 `3R-2b` exact owner checkpoint

Original immutable NeoForge21.1.215 ZIP re-downloaded and SHA verified. [3R-2b stage](PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.md), [all 241 classes original nearest getShape/getCollisionShape owners](PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.tsv), [independent reproduce/verify script](../../scripts/research/phase3_shape_effective_owner_verify.py) are exact original JVM reflection evidence, unlike previous direct-source search. Four additional shape APIs and full Phase3 3A–3E still OPEN. Next `3R-2c` before any new Phase3 runtime implementation. No new CI required for research-only commit.

## Batch R2 — client geometry/render/particles [COMPLETE FOR ORIGINAL R2 SCOPE, NOT WHOLE PHASE 3]

**2026-10-24 latest `3R-2` research-only checkpoint:** [241-class direct-geometry source-search matrix](PHASE3_R2_REGISTERED_DIRECT_SHAPE_DECLARATIONS_1_21_1.tsv) and [4/4 research](PHASE3_R2_REGISTERED_SHAPE_DECLARATIONS_1_21_1.md) now recorded, but true nearest effective superclass owners and complete NeoForge21.1.215 ABI are NOT audited. `3R-2b` is next, before `3R-3`. No runtime CI required.

**2026-10-24 scope reconciliation:** R2 was a selected architectural family/particle inventory. It did not produce the later demanded exhaustive Phase3 registered owner/override census, NeoForge exact-version per-owner ABI inventory or full mod render bypass map. Phase3 research gate is **OPEN**. New research-only 3R-1 through 3R-7 use [phase-wide ledger](PHASE3_OWNER_COVERAGE_1_21_1.tsv), the [3R-1 source audit](PHASE3_R1_3A_SHAPE_CALLPATH_AND_COVERAGE_1_21_1.md), and [Phase3 gate](PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md). No runtime changes until completion.

Result:
`docs/research/CLIENT_RENDER_PARTICLE_MATRIX_1_21_1.md`

Completion findings are merged into the mechanism map and Phase-3/4 roadmap.

Scope:
- VoxelShape/query/collision/visual/interaction shapes;
- occlusion/culling;
- ModelBlockRenderer / BakedModel / BakedQuad;
- AO/light/shade/random offsets;
- breaking overlays;
- BER/custom renderers;
- moving block/entity rendering;
- complete ParticleEngine registry/class/emitter audit;
- nested particle emitters;
- render-oriented particle subclasses;
- accelerated renderer compatibility boundaries.

Output:
complete Phase-3 + Phase-4 mechanism map and port hotspots.

Important:
existing particle runtime work remains frozen during this research batch; use it
as evidence, not as a reason to continue coding before the global audit is done.

## Batch R3 — fluids [COMPLETE]

Result:
`docs/research/FLUID_MECHANISM_MATRIX_1_21_1.md`

Completion findings are merged into the mechanism map and Phase-5 roadmap.

Scope:
- FlowingFluid;
- FluidState;
- LiquidBlock;
- waterlogging/container hooks;
- source creation;
- horizontal/local-tangent spread;
- local-DOWN recursion and slope search;
- fluid shapes/heights;
- entity flow/current;
- buckets;
- drip/bubble/current emitters;
- LiquidBlockRenderer;
- lava/water interactions;
- modded fluid extension points.

Output:
complete Phase-5 algorithm map and integration gates with Phase 2/3/4/7.

## Batch R4 — entity/body/interaction/network [COMPLETE]

Result:
`docs/research/ENTITY_BODY_NETWORK_MATRIX_1_21_1.md`

Completion findings are merged into the mechanism map and Phase-7/7B roadmap.

Scope:
- Entity.move/collision/step/support/fall;
- LivingEntity travel/jump/swim/climb/elytra;
- non-living entities;
- projectiles;
- minecarts and boats;
- riding/passengers/dismount/leash;
- forces/knockback/explosion/piston interaction;
- eye/view/body frame;
- raycast/pick/hit result;
- LocalPlayer/ServerPlayer prediction;
- movement packets/floating/teleports/vehicle packets.

Output:
complete Phase-7 + interaction/network half of Phase-7B.

## Batch R5 — AI/navigation + signals/automation/logistics [COMPLETE]

Result:
`docs/research/AI_AUTOMATION_MATRIX_1_21_1.md`

Completion findings are merged into the mechanism map and Phase-6/7A roadmap.

Scope:
- NodeEvaluator / WalkNodeEvaluator / PathFinder;
- GroundPathNavigation and other navigation modes;
- Move/Look/Body controls;
- RandomPos/target generation;
- common ground/flying/aquatic goals;
- doors/fences/rails/hazard integration;
- SignalGetter and signal-side conventions;
- RedStoneWire;
- repeater/comparator/observer/torch/button/lever/plate;
- powered/detector/activator rails;
- pistons/push resolver/moving piston;
- slime/honey;
- NeoForge sided capabilities;
- hopper/dispenser/dropper/machine logistics.

Output:
complete Phase-6 + Phase-7A map and cross-dependencies.

## Batch R6 — environment/worldgen/structures/compatibility [COMPLETE]

Result:
`docs/research/ENVIRONMENT_WORLDGEN_STRUCTURE_COMPAT_MATRIX_1_21_1.md`

Research covers Phase 7B.3-4 + Phase 8/9/10, source call paths,
frame ownership, unresolved product gates, persistence, portability,
performance and one batch acceptance matrix per mechanism.

Major corrections: vanilla XZ heightmap is not a six-face surface;
PlanetChunkGenerator currently disables decoration/carvers/initial mobs;
continuous scalar fields do not transparently transport arbitrary integer-grid
feature geometry; daylight requires a separate radial skylight source decision;
sea shell cannot be represented by ChunkGenerator.getSeaLevel global Y;
rigid structures need whole StructureStart validation before registration.


Scope:
- spawn placement and NaturalSpawner;
- heightmaps;
- precipitation/weather/snow/ice/lightning;
- sky/skylight/environment policy;
- biome temperature/elevation semantics;
- PlanetGenerationSpace coverage;
- chunk generation/surface/caves/features;
- structures/structure processors;
- runtime portals / rigid topology;
- mod compatibility/public APIs;
- performance hot paths;
- persistence/data-versioning;
- cross-version portability review.

Output:
complete Phase-7B environment + Phase-8/9/10/11 map.

## Final completeness sweep [COMPLETE — ARCHITECTURE HANDOFF, RUNTIME UNCHANGED]

Results:
- `docs/research/FINAL_COMPLETENESS_SWEEP_1_21_1.md`
- `docs/research/EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md`

The full R1–R6 mechanism ownership/cross-dependencies have been reconciled;
critical implementation gates, physical-vs-local contract conflicts, external
coordinate-system precedents and dependency-ordered implementation batches
are now recorded. The following steps in this section describe the completed
sweep criteria, not work to repeat before starting Batch 0.


After R1-R6:

- compare all findings against the complete 1.21.1 source/package inventory;
- sweep dangerous patterns again and ensure every result maps to a mechanism or
  an intentional physical/global policy;
- verify no named block/entity is treated as "done" across unrelated mechanisms;
- verify every phase has:
  - stable owner boundary;
  - dependencies;
  - portability notes;
  - deterministic test strategy;
  - one batch runtime acceptance matrix;
- produce a short prioritized implementation order based on dependencies.

Only then resume runtime implementation.

## Current runtime freeze point

Current branch already contains partial Phase-4 work through the
base/custom-particle-engine checkpoint.

Do not roll it back merely because planning continues.
Do not expand it further until the global research batches and final sweep are
complete.

The next runtime checkpoint after research completion should first verify
build/startup before asking for the consolidated subsystem gameplay acceptance.
