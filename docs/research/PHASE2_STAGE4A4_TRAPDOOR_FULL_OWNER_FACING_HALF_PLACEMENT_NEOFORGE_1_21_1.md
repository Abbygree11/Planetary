# Planetary — 2.4A-4: TrapDoorBlock whole-owner placement (FACING / HALF)

Date: 2026-10-13. Branch: `2.0`. Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.
Code: [843e768](https://github.com/Abbygree11/Planetary/commit/843e76848e6aa7a6f7e0bc52a4d3e3b161722526); strict ABI test [4c9dc20](https://github.com/Abbygree11/Planetary/commit/4c9dc20a1f92ad3b54bbe3c7bded0575e28eb0e0); corrected ASM array visitor [efbbc57](https://github.com/Abbygree11/Planetary/commit/efbbc57d4f78831b45af32c77413f7f368cc5a05). [CI #38319624909](https://github.com/Abbygree11/Planetary/actions/runs/38319624909) COMPLETED SUCCESS: Java 21 production+test compile, complete JUnit and exact NeoForge classpath ASM assertions, registered BLOCK/ITEM/STATE census.

## Declaring owner and material coverage

Pinned original 1.21.1 comparative source: [TrapDoorBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TrapDoorBlock.java). Registered original [Stage3A 28 full 40-ID audit](PHASE2_STAGE3A_DOOR_TRAPDOOR_FULL_OWNER_ITEM_REDSTONE_COPPER_MOD_1_21_1.md) verifies 20 trapdoors / 20 related doors separately. Trapdoors: 11 wood, iron, 4 waxed copper and 4 unwaxed copper states; 16 direct TrapDoorBlock registered, 4 WeatheringCopperTrapDoorBlock subclasses inheriting placement. Real common owner is `TrapDoorBlock.getStateForPlacement(BlockPlaceContext)`, not HorizontalDirectionalBlock (only FACING vocabulary). NeoForge exact bytecode owner and inherited copper behavior checked by `Phase24A4TrapdoorPlacementAsmContractTest`, with BlockItem virtual item lifecycle intact.

Pinned mod source comparator: Biomes O' Plenty 1.21.1 BlockSetTypes and matching door/trapdoor registrations in [Stage3A audit](PHASE2_STAGE3A_DOOR_TRAPDOOR_FULL_OWNER_ITEM_REDSTONE_COPPER_MOD_1_21_1.md). Every Compat [MacawTrapdoorsModule](PHASE2_STAGE3A_DOOR_TRAPDOOR_FULL_MATERIAL_MOD_SCOPE_1_21_1.md) creates anonymous descendants of TrapDoorBlock; source comparator requires newer NeoForge 21.1.248, and loaded JAR is NOT tested. Inheriting a method may share this fix; overriding mod classes or independent multi-part/trapdoor algorithms are NOT automatically covered. No claim of all mods compatible.

## Exact algorithm and safety

Vanilla has **two** getStateForPlacement orientation cases:
1. When not replacing the clicked block and clicked normal is HORIZONTAL: FACING = clicked normal, HALF = clicked Y fractional offset > 0.5.
2. Otherwise: FACING = actor horizontal direction OPPOSITE, HALF = BOTTOM only for clicked UP; TOP otherwise.
   
Under Planet each branch uses **TARGET canonical local** clicked normal and target-local hit offset. `PlanetTrapDoorPlacement.orient` owns a pure semantic selection with an explicit optional actor-horizontal direction and rejects a vertical heading instead of guessing. `reframe` is Minecraft adapter: on a Planet target, re-express player body-local heading through existing `PlanetFrameApi.localHorizontalDirection`, or convert only an explicit null-player `DirectionalPlaceContext` physical synthetic heading. Unknown other null-player/mod writers default to original vanilla output when no valid semantic heading is given. No rewrite of player physical hit, Level, block neighbor, fluids, BlockPos or foreign coordinate chart.

**Thin `TrapDoorBlockLocalPlacementMixin` injects at RETURN** and only replaces `TrapDoorBlock.FACING` and `TrapDoorBlock.HALF` on the ACTUAL returned state. It leaves vanilla/NeoForge `getStateForPlacement` invocation, original concrete block/material, OPEN/POWERED/WATERLOGGED, BlockSetType, redstone, sound, wind charge, item state components, fluid ticks, neighborChanged, useWithoutItem and weathering algorithms intact. It does not patch DoorBlock (two-cell and DoubleHighBlockItem owner), StairsShape (physical neighbor algorithm), culling or graphics. New Mixin is required=true in existing config.

### Deepest-safe-owner matrix

| Classification | Decision |
| --- | --- |
| SHARED_SAFE | Typed target canonical clicked normal + offset + optional known actor heading; whole inherited TrapDoorBlock placement algorithm (20 vanilla IDs, mod inheritors possible) |
| OPT_IN_ALGORITHM | Two concrete TrapDoorBlock orientation branches rewritten as target-local FACING/HALF, no unconditional HorizontalDirectionalBlock override |
| MOD_INDEPENDENT | Third-party override/custom trapdoors/synthetic mod placement must opt in; do not infer foreign Level or custom BlockSetType behavior |
| PHYSICAL_DO_NOT_TOUCH | BlockItem pipeline, actual BlockPos, FluidState and water ticks, redstone neighbor signal, copper aging, wax/scrape item state mutation, special BER/ModelData |

Deeper global BlockItem/Level/Direction rewrites destroy physical third-party topology; shallower iron-only/copper-only/wood-only edits miss inherited mod materials and separate source authors.

## Tests and acceptance

`PlanetTrapDoorPlacementTest` runs six faces × local six clicked normals × two upper/lower hit fractions, both replacing/non-replacing, actor missing vs supplied; 12 edges and 8 corners target-frame invariants, concrete wood/iron/copper/waxed-copper preservation of OPEN/POWERED/WATERLOGGED and exact block identity, no-Planet and unknown null-player pass-through. `Phase24A4TrapdoorPlacementAsmContractTest` checks 21.1.215 target method owner, BlockItem source path, WeatheringCopperTrapDoorBlock inherited placement, method descriptor, RETURN-only injected handler/signature/cancellable=true and Mixin registration.

**Game/server loaded-Mod evidence OPEN** despite green [CI #38319624909](https://github.com/Abbygree11/Planetary/actions/runs/38319624909). In-game: check +Y and all five rotated faces; placing trapdoors by clicking side and ceiling/floor, with/without replaceable target, both click heights, wood + iron + waxed/unwaxed copper; OPEN/POWERED changes from redstone, hand access differences, WATERLOGGED and tick, weathering/waxing and axes; target and neighbor frame differences at 12 edges and 8 corners; null-player dispenser and independent mod Item; no-Planet normal world and modded (BOP vs independent) and startup weaving. Reported Phase3B generic edge-face visual culling defect is **tracked elsewhere and NOT fixed here**. After actual user test record only checked portions, not all cases.

**Next 2.4A:** separate complete Stairs placement/shape owner (placement and physical neighbor shape graph must be adapted coherently), other audited property families, specialist Item overrides and post-component writers, CI and gameplay independently. Phase2A checklist 2/3/4 not globally DONE.
