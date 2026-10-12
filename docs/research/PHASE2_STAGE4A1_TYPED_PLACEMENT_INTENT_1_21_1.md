# Planetary 2.4A-1 — typed placement intent / physical geometry / target canonical semantics

Target: Minecraft 1.21.1, NeoForge 21.1.215, Java 21; branch `2.0`.

## What is implemented

- `PlanetPlacementIntent` captures **author provenance**, the original clicked block, final placement target, unchanged hit normal and hit point, replacement flag and **optional, explicit** player body frame.
- Both positions use `PlanetCoordinateContext`: either the **same physical Level** or the **same foreign coordinate system**. Mixed types, mismatched Levels and mismatched virtual space IDs are rejected.
- `PlanetPlacementResolver` reads source and target **independently**, using existing `PlanetBlockStateFrame` and `PlanetBlockPlacementFrame`. Target state semantics never reuse a source chart (especially on edges).
- Unknown/non-Planet/foreign environments produce **no Planet semantic frame**. This is a missing opt-in, not a synthetic world-UP rotation.
- `PlanetPlacementContextAdapter.fromPhysicalBlockPlaceContext` is an explicit **version-specific** adapter. Caller must retain original hit `BlockPos` before `BlockPlaceContext` conversion: `UseOnContext.getHitResult()` is protected; `BlockPlaceContext.getClickedPos()` may be the final target. No unreliable inverse `relative()` calculation.
- Pure JUnit tests exercise 6 faces × 6 directions, exact edge source/target mismatch, foreign no-lookup, vanilla absence, Level/space validation, explicit author and player-frame handling, plus retargeted `BlockPlaceContext`.

## Owner matrix and explicit limits

| Owner boundary | Contract | 2.4A-1 status |
| --- | --- | --- |
| `SHARED_SAFE` Planet API | Record original/final geometry, provenance, explicit frame, optional semantic resolution | Implemented, [CI #38313939066](https://github.com/Abbygree11/Planetary/actions/runs/38313939066) SUCCESS |
| `OPT_IN_ALGORITHM` BlockItem/Block family properties | Pass canonical values **only** at semantic author sites; preserve vanilla calls | Next packets; no runtime rewrite in 2.4A-1 |
| `MOD_INDEPENDENT` alternate Item/modded overrides | Carry explicit provenance; mod integrations choose their own adapter | Source evidence from 2.3B/C; loaded mods not verified |
| `PHYSICAL_DO_NOT_TOUCH` hit ray, adjacent BlockPos, BlockItem lifecycle, NeoForge events | Never globally rewrite Level/Direction/BlockPos or intercept writes | Intentionally unchanged |

Reason for API location: deeper generic `Level.setBlock`/`Direction` rewrites cannot distinguish semantic state from physical topology and break mods; shallower named-block Mixins fail to cover inherited or independent authors. The typed API lets later version-specific adapters use a common meaning while leaving unknown callers physical by default.

### Acceptance gates

This packet is an **API foundation, not gameplay implementation**. First [CI #38313592960](https://github.com/Abbygree11/Planetary/actions/runs/38313592960) compiled successfully but 1 of 430 JUnit tests failed: the new null-guard test expected IllegalArgumentException although the documented null contract throws NullPointerException. Fixed the **test assertion only** in [893a093](https://github.com/Abbygree11/Planetary/commit/893a0938fcedb858fe1d7f93de6e306148767147). Repeat [CI #38313939066](https://github.com/Abbygree11/Planetary/actions/runs/38313939066) **COMPLETED SUCCESS**: production+test Java compile, JUnit and original registry census. This is unit-level evidence only. No changes to Mixins, Java block placement behaviors, NeoForge hooks, persistence or mod interoperability are claimed.

Future 2.4A families must verify compiled 21.1.215 ABI, actual injected runtime and no-Planet baseline, +Y and all five rotated faces, 12 edges, 8 corners, replaceable and non-replaceable hit, player vs null-player, alternate item targets, structures, physical and foreign mod callers; loaded third-party JAR tests stay pending. Relevant prior source evidence: [2.3C final gate](PHASE2_STAGE3C_1_TO_19_FINAL_SELECTED_ABI_SCOPE_GATE_1_21_1.md), [C1](PHASE2_STAGE3C1_BLOCKITEM_STANDINGWALL_NEOFORGE_ASM_LIFECYCLE_1_21_1.md), [C2](PHASE2_STAGE3C2_PLACEMENT_CONTEXTS_SYNTHETIC_WATER_SCAFFOLD_BUCKET_ASM_1_21_1.md), [C3](PHASE2_STAGE3C3_MULTIBLOCK_ITEM_BLOCK_SETPLACEDBY_ASM_LIFECYCLE_1_21_1.md), [C16](PHASE2_STAGE3C16_FRONT_AND_TOP_ROTATION16_STATE_TRANSFORMS_NEOFORGE_ASM_1_21_1.md).

Next: `2.4A-2` canonical property families (6/4-way FACING, AXIS, HALF, ATTACH, ROTATION_16, FrontAndTop), separate semantic adaptation from physical support/adjacency, then item-author paths and acceptance.
