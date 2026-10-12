# 2.3B-1 — future acceptance: item authoring and target mutation

**2026-10-12; DESIGN ONLY / ALL NOT RUN.** These are TEST INTENT, not completed manual, JUnit, ASM, CI or modded runtime checks. Baselines 1.21.1 / NeoForge21.1.215 / Planetary 2.0. Source: [first item-family audit](PHASE2_STAGE3B_ITEM_PLACEMENT_FIRST_FAMILY_SOURCE_AUDIT_1_21_1.md), prior [Phase-2 acceptance](../acceptance/PHASE2_ORIENTATION_ACCEPTANCE_1_21_1.md).

## Dimensions (every applicable family, not every BLOCK ID)

Each canonical face: POS_Y/NEG_Y/POS_X/NEG_X/POS_Z/NEG_Z. Boundary: all 12 face-pair edges + all eight three-face corners; if a corner offers two chart paths to one physical cell, assert chart-specific intent but **one real physical BlockPos**. Include +Y vanilla-equivalence and worlds without Planet gravity. Each must check explicit physical clicked/target `BlockPos`, author provenance, actual selected registered BLOCK ID, final canonical property legality, physical collision/support, final BE (where applicable), and mod-visible events. Client screenshots alone cannot PASS server semantics.

| Case | Natural action + input | Assert | Status |
|---|---|---|---|
| I01 | plain ordinary `BlockItem` on all faces | final hit/relative target correct; orientation no accidental change; events/item consume once | NOT RUN |
| I02 | click replaceable water/grass vs solid block; each face | `replaceClicked` and physical target choice match vanilla rules | NOT RUN |
| I03 | slab/stair repeat-placement / replacement in top/bottom and wall hit | target state and correct legal HALF/TYPE with no unexpected second cell | NOT RUN |
| I04 | standing vs wall torch/coral-like dual item on all faces | correct physical registered variant and matching `canSurvive` | NOT RUN |
| I05 | standing sign + wall sign, each wood material sample, 6 faces | same item chooses correct registered BLOCK, text BE persists | NOT RUN |
| I06 | hanging sign ceiling vs wall, wall support edge cases | `HangingSignItem.canPlace` and separate WallHangingSignBlock check preserved | NOT RUN |
| I07 | all 16 banner colors representative rotation values incl 0/4/8/12 | correct registered standing/wall variant and BE patterns | NOT RUN |
| I08 | skull/head standing/wall item and player head NBT | variant registration, support, BE/profile remain correct | NOT RUN |
| I09 | scaffolding normal placement + sneaking/inside/side on all faces | physical scan and target rewritten correctly; no >7 unsupported placements | NOT RUN |
| I10 | scaffolding build bound, blocked 1..7 candidate and seam | no out-of-range physical writes or malformed context | NOT RUN |
| I11 | dispenser placing shulker/other synthetic context without Player | no NPE, exact original synthetic order or documented local opt-in | NOT RUN |
| I12 | falling-block replacement using `DirectionalPlaceContext` | no NPE; original physics and local landing intent not conflated | NOT RUN |
| I13 | `BLOCK_STATE` component with valid FACING6/AXIS3/ROTATION16 | component after placement preserved and legal; author conversion explicit only | NOT RUN |
| I14 | `BLOCK_STATE` invalid value / unknown property / unexpected selected wall variant | ignored per final block StateDefinition; no state corruption | NOT RUN |
| I15 | block entity custom data on sign, chest and modded independent block | BE component and permission gates, `setPlacedBy`, updates/events remain correct | NOT RUN |
| I16 | bed + double-high item placement on 6 faces and edges | paired source+target physical positions and final state survive | NOT RUN |
| I17 | `PlaceOnWaterBlockItem.use()`, NOT `useOn()` | fluid raycast and target selection handed to Phase5 contract | NOT RUN |
| I18 | flint/fire charge/bonemeal/bucket as alternate authors | actual subclass hook/NeoForge tool ability and proper physical side | NOT RUN |
| I19 | Create-style fake player + direct mod BlockItem subclass override | no assumption of ordinary Player; preserve its override and physical props | NOT RUN |
| I20 | pinned ClickSigns RoadSignBlock in actual loaded 1.21.1 runtime | independent mod getStateForPlacement/use/BE works without vanilla SignBlock shortcut | NOT RUN |
| I21 | vanilla non-Planet Level and foreign/schematic Level | physical `Direction`/`BlockPos` and original callbacks untouched | NOT RUN |
| I22 | exact NeoForge21.1.215 ASM method owner and @Redirect call target | descriptor and invocation matching; no `require=0` masking | NOT RUN |
| I23 | 12 seams + 8 corners for I02/I04/I05/I09/I13 | source-hit/body-frame/target-frame mismatch explicitly exercised | NOT RUN |
| I24 | server restart + neighbor removal + client/server interaction | state and BE persistence, late neighbor survival, correct sync | NOT RUN |

**Strict PASS gate**: natural player placement, actual registered item, real chosen BlockState, and populated server behavior; force-setting a fixture block state is **not** item test success. For mod claims require actual loaded mod and method evidence, not only source comparison. If a test spans another subsystem (render Phase3, liquids Phase5, automation Phase7A, portal Phase9) record independent owner and BLOCKED/PENDING, not Phase2 success. No CI or gameplay run on documentation-only packet.
