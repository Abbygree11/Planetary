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

## Batch R2 — client geometry/render/particles [COMPLETE]

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

## Batch R6 — environment/worldgen/structures/compatibility [NEXT]

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

## Final completeness sweep

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
