# Stage3A-20 — Campfire / Fire / SoulFire fire, light, cooking and mod owner family

**2026-10-11 SOURCE 4/4 COMPLETE.** Minecraft1.21.1 NeoForge21.1.215 Planetary branch 2.0. All runtime ASM/Planet/client/server GAMEPLAY gates **REVIEW_PENDING**, NO Java/Mixin code changes.

- [x] **20.1** original physical ZIP SHA256 and all four exact BLOCK IDs, legal properties, five full-parameter nearest declaring owner signatures.
- [x] **20.2** complete [original source/compiled owner, special ITEM/dispenser/portal/BE/heat/smoke and pinned 1.21.1 mod consumer audit](../../research/PHASE2_STAGE3A_FIRE_CAMPFIRE_DEEP_ORIGINAL_ITEM_SPREAD_COOKING_MOD_1_21_1.md).
- [x] **20.3** [50 future UNRUN six-face, 12 edge, 8 corner, cooking, spread, smoke, portal and mod acceptance cases](../../research/PHASE2_STAGE3A_FIRE_CAMPFIRE_SIX_FACE_SPREAD_COOKING_MOD_ACCEPTANCE_1_21_1.md).
- [x] **20.4** original independent Python ZIP vs LIVE 241×16 TSV JavaScript FNV32/FNV64 exact full-signature four-stream **MATCH**. [Full reconciliation](../../research/PHASE2_STAGE3A_FIRE_CAMPFIRE_ORIGINAL_106_CLASS_RECONCILIATION_1_21_1.md).

Original **3 classes/4 BLOCK IDs**: CampfireBlock campfire+soul_campfire, FireBlock fire, SoulFireBlock soul_fire. Abstract BaseFireBlock shared fire/soul owner, but CampfireBlock extends separate BaseEntityBlock and owns its four slot CampfireBlockEntity. FireBlock schedules fire spread on its `tick`, not `randomTick`, with 6-neighbor burnout and asymmetric physical WORLD-Y sampling; SoulFireBlock does not inherit FireBlock spreading. Smoke consumers such as `CampfireBlock.isSmokeyPos` and modded cauldron heat sources read `below` independently. Real BareBonesBrews 1.21.1 mod uses tagged/state property heat source selectors rather than extending vanilla fire; build min21.1.251 > our21.1.215 means runtime not verified. Deepest SAFE typed semantic opt-in, preserve physical unknown mods, do not globally mutate BlockPos/Direction/FluidState.

**Ledger source**: 106/241 original concrete classes, 289/1060 exact BLOCK IDs, pending135 classes/771 IDs; **all 723 ASM/Planet/gameplay gates REVIEW_PENDING, 50 future tests NOT RUN**, Phase2/3A runtime OPEN.

**NEXT one «кк» → whole Stage3A-21**: [FlowerPotBlock / 35 potted plant registered variants](02s-flower-pot-potted-plant-owners.md), all four internal steps together.
