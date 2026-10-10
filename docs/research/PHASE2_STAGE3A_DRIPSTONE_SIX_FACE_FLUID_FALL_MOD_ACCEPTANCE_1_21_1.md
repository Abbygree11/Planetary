# Stage 3A-17.3 — 45 future acceptance cases for pointed dripstone, fluids, falling entities and independent speleothems

**2026-10-11 · Planetary branch 2.0 · Minecraft 1.21.1 / NeoForge 21.1.215 · ALL 45 DT17-01…DT17-45 tests NOT RUN.** Evidence-based [original NeoForge five declared owners / vanilla complete source + original ITEM and independent Eternal Starlight mod](PHASE2_STAGE3A_DRIPSTONE_DEEP_ORIGINAL_OWNER_ITEM_FLUID_FALL_WORLDGEN_MOD_1_21_1.md). Deepest SAFE cross-mod semantic boundary [policy](PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md), root `AGENTS.md §3A.2`.

## Frozen physical/canonical/algorithm tests contracts

- **Exactly 2 legal vertical direction property values:** `TIP_DIRECTION = VERTICAL_DIRECTION` has vanilla `up,down`, not arbitrary 6 physical axes. Local gravity face may be POS_Y/NEG_Y/POS_X/NEG_X/POS_Z/NEG_Z; adapter must translate desired local structural vertical into valid canonical storage/shape/physics via a deliberately documented representation and measured client/server outcome. Do NOT write illegal FACING values to the vanilla property or pretend a shape rotation alone changes `canSurvive`.
- **One real physical XYZ cell** and actual chunk per block, even at 12 cube face-pair edges/8 triple corners; distinguish original clicked-hit and player look, source/target canonical `PlanetBlockStateFrame`, path-dependent `PlanetBlockFrameContext`, actual physical support face and physical six-neighbor water. No double applying transform.
- **Separate time mechanisms:** `updateShape` unsupported DOWN schedules falling **2 ticks**, UP destruction **1 tick**; `randomTick` samples cauldron transfer and growth independently with 0.17578125 water/0.05859375 lava and 0.011377778 growth; fluid tick is WATER if waterlogged; `AbstractCauldronBlock.tick` later rechecks source before receiving. Client `animateTick` dripping particles do NOT demonstrate server-side fluid filling.
- **Separate entity mechanisms:** source fallOn upward TIP stationary stalagmite damage vs `FallingBlockEntity` downward multi-segment falling stalactite and `Fallable` collision/damage sounds. Preserve earlier known partially working falling impact and previous known sprite position issues; don't revert or mark runtime PASS based on research.
- **Separate source writers:** ordinary Item.BlockItem, `PointedDripstoneBlock.grow`, `DripstoneUtils.growPointedDripstone` and worldgen `PointedDripstoneFeature/DripstoneClusterFeature`, mud→clay, cauldron receiver, commands/structures; `LargeDripstoneFeature` actually creates solid large dripstone mass, not proven direct pointed spike writer.
- **Cross-mod:** Eternal Starlight `abstract SpeleothemBlock extends Block implements Fallable,SimpleWaterloggedBlock` on **1.21.1** independently implements the same vertical/stalactite mechanism; copies original source algorithms but uses own SpeleothemThickness enum, own tagged type support, configurable growth block and does not implement vanilla cauldron transfer path. Vanilla-only Mixin will miss it. Generic engine provenance/default PHYSICAL plus opt-in typed vertical-chain mechanics is the only safe general plan until patched NeoForge mod gameplay evidence exists. Its mod metadata allows NeoForge21.1.215, but its checked source build specifies NeoForge21.1.251, and real load compatibility NOT CONFIRMED.
- **Six-face core/phase split:** Phase2 placement, canonical directions/support and thickness; Phase3 shape/particles/sounds; Phase5 real fluid/canDripThrough/cauldron; Phase7 FallingBlockEntity/fallOn/impact, Phase8/9 Feature/Column and 3D terrain and Phase10 optional mod dispatch. No global `Direction`, `BlockPos`, `FluidState`, `NeighborUpdater`, `Level.getBlockState/setBlock` rewrite.

## Future test instrumentation

Each fixture logs exact original/patch/Planet/mod Git SHA, target MC/NeoForge + loaded mod version, applied transformed method owner signature and mixin/bytecode proof separately; vanilla no-Planet vs Planet enabled, source/target/support physical BlockPos/chunk and canonical PlanetFace/chart, actual physical direct callbacks, clicked hit/player pose, vertical_direction/THICKNESS/WATERLOGGED, original ITEM component, block/fluid/cauldron scheduled tick times and events, actor entity damage and collision, actual measured water FluidState + cauldron fill, dimension ultraWarm, seed, physical Feature outputs and vanilla/modded results. No test below is evidence of PASS.

## All 45 proposed tests — NOT RUN

| ID | Future setup/action | Required observation |
| DT17-01 | Vanilla no-Planet ordinary BlockItem pointed dripstone on sturdy ceiling | Source nearest vertical look direction determines legal TIP_DIRECTION down; thickness TIP, waterlogged actual target |
| DT17-02 | Vanilla no-Planet ordinary BlockItem on sturdy floor | Source TIP_DIRECTION up and legal thickness; target support real opposite face |
| DT17-03 | Placement on side hit with player looking up/down | Source uses nearest LOOKING vertical direction, not clickedFace; invalid orientation falls back opposite or null |
| DT17-04 | No support both vertical candidates | getStateForPlacement null, no phantom block added |
| DT17-05 | Physical block behind same-direction pointed dripstone | canSurvive true without generic sturdy face, preserve chain semantics |
| DT17-06 | Target facing opposite live tip; secondary-use toggled | THICKNESS TIP vs TIP_MERGE per source, no illegal horizontal vertical_direction |
| DT17-07 | All five THICKNESS values via setup plus neighbor changes | Recompute BASE/MIDDLE/FRUSTUM/TIP/TIP_MERGE, correct physical shape/state |
| DT17-08 | Late BlockItem BLOCK_STATE components vertical_direction,thickness,waterlogged | Only original legal values accepted after initial place; no other FACING domain |
| DT17-09 | Support lost for vertical_direction up | updateShape schedules tick1; tick destroys unsupported stalagmite |
| DT17-10 | Support lost for vertical_direction down | updateShape schedules tick2, suppresses duplicated pending falling tick; tip chain falls |
| DT17-11 | Horizontal physical neighbor updates | Fluid tick if waterlogged, otherwise no thickness/support modification from source |
| DT17-12 | Underwater source and flowing water placed spike | Waterlogged iff target FluidState TYPE Fluids.WATER; water fluid tick and source FluidState |
| DT17-13 | Down tip fall chain with 1..N segments and merged ends | One FallingBlockEntity per traversed physical segment through tip, source damage multiplier and maxDamage40 |
| DT17-14 | Stalagmite direct entity fallOn UP+TIP | Damage source stalagmite, fall distance +2, multiplier2, compares super.fallOn other stages |
| DT17-15 | FallingBlockEntity falling stalactite hits one standing mob | Fall damage and hitbox correct physical/local gravity, preserve previously observed partial fixes |
| DT17-16 | FallingBlockEntity spike crosses face-pair edge and gravity switches | One real entity trajectory, no phantom sideways half-block sliding or clone fall |
| DT17-17 | Client block debris and particles on ±X and ±Z faces | Earlier observed +Y displacement / block break graphics checked separately, not waived by collision pass |
| DT17-18 | Spike breaks on trident projectile high vs low velocity | Server mayInteract/mayBreak and ThrownTrident >0.6 gate; projectile-only source owner's semantics |
| DT17-19 | Stalactite falling last tip destruction | Fallable.onBrokenAfterFall event1045, sound and getFallDamageSource fallingStalactite |
| DT17-20 | Wet stalactite with WATERLOGGED true TIP | No canDrip despite waterlogged, own water tick still scheduled |
| DT17-21 | RandomTick dry downward stalactite, WATER source above dripstone block | RandomTick maybeTransferFluid Water0.17578125 with deterministic seeded gate, physical vertical root and source |
| DT17-22 | RandomTick lava source above root | Transfer probability 0.05859375 and receiving lava cauldron, no indiscriminate WATER |
| DT17-23 | MUD source above stalactite in normal dimension | Water transfer converts physical MUD→CLAY, Block.pushEntitiesUp and GameEvent.BLOCK_CHANGE |
| DT17-24 | MUD source above in ultraWarm | No false WATER from MUD; getDripFluid particle fluid substitution distinct from cauldron |
| DT17-25 | Cauldron physically below qualifying tip within source MAX 11 scan | findFillableCauldron... only through permitted AIR/non-solid collision aperture; scheduled delay 50+Y difference |
| DT17-26 | Cauldron recheck after scheduled tick with removed source | AbstractCauldronBlock.tick sees absent drip and does not fill; no action on mere particle |
| DT17-27 | Cauldron receiver filled exactly water/lava state | AbstractCauldronBlock vs LayeredCauldronBlock/LavaCauldronBlock owns receipt; modded cauldron compatibility gated |
| DT17-28 | Obstacle between tip and cauldron: solid, fluid, hollow collision | Physical canDripThrough collision shape aperture controls scanning; no teleport fluid |
| DT17-29 | Particle drip on six gravity faces | Client original Y+1−0.6875−0.0625 offset vs desired local physics visually, no false fluid state |
| DT17-30 | RandomTick growth requires DripstoneBlock and above water SOURCE | Both source checks, small random probability 0.011377778, cap search length 7 |
| DT17-31 | Growth chooses down extension vs stalagmite below, opposing merged tips | True physical target for each, target water test, preserved orientation+THICKNESS |
| DT17-32 | PointedDripstoneFeature direct LevelAccessor writer at cave ceiling/floor | DripstoneUtils TIP_DIRECTION/THICKNESS and source true waterlogging, no BlockItem shortcut |
| DT17-33 | DripstoneClusterFeature cave columns, wetness, radius/height | WorldGenLevel physical Column.scan, correct water and pointed vs support blocks |
| DT17-34 | LargeDripstoneFeature source author count | Creates supporting DRIPSTONE_BLOCK mass only per inspected code, not fabricated pointed spikes |
| DT17-35 | Six Planet face interiors floor/ceiling initial support | Two legal state directions in physical vanilla, local semantic vertical explicitly resolved, not illegal side direction |
| DT17-36 | All 12 Planet edges approached from both charts | One real BlockPos and canonical source/target frames, segment column follows chosen algorithm policy |
| DT17-37 | All 8 triple-face corners approached from each chart | One true physical falling chain and receiver, no duplicate hit or drip tick |
| DT17-38 | True XZ chunk seam, unloaded world and restart | Scheduled block/fluid/cauldron ticks and falling entities consistent across chunk boundaries |
| DT17-39 | Eternal Starlight actual SpeleothemBlock loaded on NeoForge21.1.215 | Original mod concrete subclass dispatch vs independent source copy, correct tag-based SpeleothemThickness/Fallable |
| DT17-40 | Eternal Starlight on each Planet face and edges/corners | Modded falling/growth/placement/survival/shape/FluidState end-to-end, original physical world graph default |
| DT17-41 | Eternal Starlight uses blockToGrowOn and no vanilla cauldron transfer | No invented vanilla mud/clay cauldron behavior in mod; separate optional compat policy |
| DT17-42 | Generic Fallable, FallingBlockEntity for another independent modded spike | Actual loaded declaring owner, no reliance on vanilla PointedDripstoneBlock Mixin |
| DT17-43 | Vanilla normal no-Planet baseline vs Planet field disabled | Every source owner path identical to unmodified Minecraft 1.21.1; unknown mod physical directions unchanged |
| DT17-44 | External structure palette /setblock /fill direct writer, fake/virtual Level | Existing legal VERTICAL_DIRECTION/THICKNESS, no unsupported frame changes or fabricated shipped NBT |
| DT17-45 | Actual patched NeoForge bytecode and loaded client/server after future implementation | Confirm applied mixins and exact declaring methods + supported custom mods, record success/failure individually |

## Source-only research status

All 45 test rows are design proposals, NOT RUN. Stage17.4 must still compare immutable original NeoForge 241 class roster, exact 97 reviewed class ID lists and five full declaring-owner signatures against live GitHub ledger using **separate original Python and live JS**, and open next actual source-pending family. Currently predicted after source-only 17.1/17.2: **97/241** reviewed classes, **246/1060** exact BLOCK IDs, pending **144/241 /814 IDs**, all 241×3 patched ASM/Planet/gameplay acceptance fields REVIEW_PENDING (723). No Java/Mixin/CI/client/server tests.
