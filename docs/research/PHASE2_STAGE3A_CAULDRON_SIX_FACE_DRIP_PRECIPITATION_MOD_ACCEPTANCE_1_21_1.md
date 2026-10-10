# Stage 3A-18.3 — 45 future six-face/edge/corner, rain, drip, content, item and mod acceptance fixtures

**2026-10-11 · ALL CA18-01..CA18-45 NOT RUN.** SOURCE analysis/compiled original declaring owners does not establish NeoForge patched bytecode correctness or Planetary runtime behavior. [Mechanism and mod research](PHASE2_STAGE3A_CAULDRON_ORIGINAL_OWNER_ITEM_RAIN_DRIP_MOD_1_21_1.md) · [Original ZIP / live ledger reconciliation](PHASE2_STAGE3A_CAULDRON_ORIGINAL_100_CLASS_RECONCILIATION_1_21_1.md).

## Instrumentation/evidence requirements

For every future case capture actual original NeoForge/Planet binary versions, source and target physical BlockPos/chunk/dimension, active Planet gravity face and source/target canonical frames, chart provenance when crossing edges/corners, exact cauldron ID and LEVEL, actual block/cauldron FluidState, rainfall/precipitation type and WORLD-Y vs local-UP heightmap policy, event and item callback order, real class/method owner, item stack before/after, scheduled tick and random seed, entity AABB vs contents plane, comparator output, each active mod and mixin audit. Record independent PASS/FAIL/SKIP for: physical world path, Planet frame translation, patched ASM applied, actual server logic and client rendering. NEVER infer client render correctness from server content or vice versa. No tests were executed as part of this source-only phase.

## All 45 proposed tests — NOT RUN

| ID | Future fixture | Required observation |
| --- | --- | --- |
| CA18-01 | Plain non-Planet empty cauldron placed via minecraft:cauldron BlockItem | One physical block, no facing property; item useBlock and block shape unchanged |
| CA18-02 | Original item stack late BLOCK_STATE components on empty cauldron | Illegal LEVEL/facing rejected or ignored by engine; no fabricated orientation value |
| CA18-03 | Use command to insert water_cauldron level 1,2,3 | All legal levels decode and survive; no ordinary separate filled ITEM required |
| CA18-04 | Use /setblock of powder_snow_cauldron level 1,2,3 | Distinct registry ID but same LayeredCauldronBlock class, no mistaken water behavior |
| CA18-05 | Use /fill to create lava cauldron | LavaCauldronBlock true full, no assigned LEVEL property |
| CA18-06 | Place cauldron normal-world side-hit | Source inherited Block.getStateForPlacement; no phantom local facing |
| CA18-07 | World non-Planet no rain vs RAIN exposed empty cauldron | Only qualifying RAIN callback may fill; per callback probability 0.05 |
| CA18-08 | SNOW precipitation exposed empty cauldron | Only qualifying SNOW callback may produce powder_snow_cauldron L1; probability 0.1 |
| CA18-09 | RAIN on water_cauldron L1 and L2 | RAIN-type layered instance increments one level on qualifying successful roll |
| CA18-10 | RAIN on water_cauldron L3 | No increment or wraparound; full stays LEVEL3 |
| CA18-11 | SNOW on powder_snow_cauldron L1 and L2 | SNOW-type instance increments only its own LEVEL |
| CA18-12 | Opposite precipitation type for both layered cauldrons | Water instance rejects SNOW; snow instance rejects RAIN |
| CA18-13 | RAIN/SNOW on lava cauldron | No vanilla handlePrecipitation override and no false fluid fill |
| CA18-14 | Sky obstruction, roof and varied heightmap | ServerLevel tickPrecipitation reaches correct top/below physical cell, no through-roof filling |
| CA18-15 | Ordinary Block.randomTick vs weather tick | Cauldron BlockBehaviour.randomTick is not precipitation source; no invented random-tick behavior |
| CA18-16 | Wet dripstone source above empty cauldron, delayed tick | Valid WATER source sends delayed event; receiver rechecks tip and becomes water L1 |
| CA18-17 | Lava stalactite above empty cauldron | Valid LAVA source leads to lava_cauldron after scheduled tick, not water |
| CA18-18 | WATER stalactite above water_cauldron levels 1 and 2 | Exact matching source increments level by one after recheck |
| CA18-19 | WATER stalactite above water_cauldron level 3 | No overfill; unchanged |
| CA18-20 | WATER stalactite above powder_snow_cauldron | Receiver rejects water; no unlawful snow/water mixing |
| CA18-21 | LAVA stalactite above lava_cauldron | Full lava cauldron rejects drip; no false level or new block |
| CA18-22 | Remove source tip or source fluid before delayed cauldron tick | AbstractCauldronBlock.tick re-evaluates and does not fill stale source |
| CA18-23 | Block drip line with solid physical obstruction / gap | Real collision aperture and scan cap gate receiver; not visual particle-based |
| CA18-24 | Remove/replace target cauldron before delayed tick | Current target state/source determines result; no ghost container filling |
| CA18-25 | MUD source and stalactite without cauldron | MUD→CLAY original PointedDripstone route kept separate from cauldron |
| CA18-26 | Player water bucket on empty and water-layered cauldron | Default CauldronInteraction makes water LEVEL3; actual inventory, stat and sound |
| CA18-27 | Player lava/powder snow bucket on empty and matching cauldrons | Content transforms by interaction map, no BlockItem-inserted phantom fluid |
| CA18-28 | Water bucket taking water level 3 vs 1 or 2 | At L3 extract and empty; less than L3 no extract via cauldron map |
| CA18-29 | Lava bucket taking lava and powder snow bucket taking full snow | Map-specific outputs and empty block replacement |
| CA18-30 | Glass bottle water cauldron L1, L2 and L3 | Extract water potion and decrement, L1→empty |
| CA18-31 | Water potion inserted into empty/partial/full water cauldron | Requires water PotionContents, increments if not full, otherwise PASS |
| CA18-32 | Wash leather armor, banner, shulker box in water | CauldronInteraction lowers exact one level and preserves item semantics |
| CA18-33 | NeoForge right-click cancel / skip / main-hand off-hand cases | Interaction pipeline result semantics and mod priority preserved |
| CA18-34 | Burning entity contacts layered water interior | Extinguish and consume 1 level, gated by actual content plane and mayInteract |
| CA18-35 | Burning entity contacts powder snow cauldron interior | Source swaps snow to water-equivalent then lowers one level, no false snow survival |
| CA18-36 | Entity intersects lava cauldron interior and outside | lavaHurt only when actually inside; contentHeight 0.9375 and physical AABB evidence |
| CA18-37 | Comparator on empty, water L1/L2/L3, snow L1/L2/L3, lava | Analog outputs 0/1/2/3 and lava 3 with original comparator semantics |
| CA18-38 | Each six Planet canonical face interior with exposed local top | Content plane and interaction hit resolved explicitly; no illegal property writes |
| CA18-39 | Rain and snow on ±X, ±Z, +Y, −Y Planet exterior | Document per-face sky exposure policy; engine world-Y and local-UP not conflated |
| CA18-40 | All 12 face-pair cube edges from both travel charts | One real cauldron block and target frame; drip/water/rain not doubled |
| CA18-41 | All 8 triple-face corners, different travel histories | Deterministic single owner for receiver and weather without chart flicker |
| CA18-42 | Physical XZ chunk seam with unloaded chunk and reload | Scheduled drip tick and cauldron state persist or follow vanilla loading semantics |
| CA18-43 | Real physical WATER/LAVA FluidState adjacent to cauldron | No fake FluidState for cauldron contents, no corruption of waterlogged mod blocks |
| CA18-44 | Actual compatible BareBonesBrews (1.21.1) mixin ordering trial | Mod custom BE prevents precipitation/drip/extinguish when it cancels; event intercept preserved; 21.1.251 requirement flagged |
| CA18-45 | Independent subclass/custom mod receiver + no-Planet/foreign Level baseline | Opt-in semantic context only, physical default for unknown mod, bytecode/ASM and game test evidence separately collected |

## Target mechanism scopes, never blanket transforms

- Phase2: per-cell source/target canonical semantics, physical BlockPos, edge/corner chart and original BlockItem state legality.
- Phase3: 3D content shape, rendering and interaction collision.
- Phase5: true fluid state, pointed dripstone and delayed receiver update, independent CauldronInteraction maps and NeoForge item events.
- Phase6: comparator analog outputs.
- Phase7: Entity AABB/cauldron interior contact, extinguish, lava heat and fire effects.
- Phase8/9: rainfall worldgen/exposure/heightmap and direct structure/command writers.
- Phase10: optional mod semantics; foreign/fake Level and no-Planet world PHYSICAL untouched.

Status: CA18-01..45 design only. All original 241×3 NeoForge patched ASM / Planet adapter / gameplay fields remain REVIEW_PENDING; all 45 tests NOT RUN. Earlier partial falling-stalactite tests are historical and remain unchanged.
