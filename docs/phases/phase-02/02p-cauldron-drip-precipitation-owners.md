# Stage 3A-18 — shared cauldron drip, precipitation, contents and mod-interaction owners

**Status: SOURCE COMPLETE 18.1–18.4 (4/4) as of 2026-10-11; original ASM / Planet runtime / gameplay REVIEW_PENDING.** Branch 2.0, Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21. Follow root AGENTS.md §3A.2 and deepest-safe semantic rule. No runtime code changes.

**Results:**
- [18.1 and 18.2: original full-signature dispatch, source/tick/contents, 1333 ITEM mapping, all known bypasses and pinned 1.21.1 mod comparator](../../research/PHASE2_STAGE3A_CAULDRON_ORIGINAL_OWNER_ITEM_RAIN_DRIP_MOD_1_21_1.md)
- [18.3: 45 proposed UNRUN six-face/edge/corner, item, rainfall and compatible-mod cases](../../research/PHASE2_STAGE3A_CAULDRON_SIX_FACE_DRIP_PRECIPITATION_MOD_ACCEPTANCE_1_21_1.md)
- [18.4: original physical ZIP SHA256 + 100 class/250 exact ID full signature independent live ledger FNV reconciliation](../../research/PHASE2_STAGE3A_CAULDRON_ORIGINAL_100_CLASS_RECONCILIATION_1_21_1.md)
- [Stage17 prior source](../../research/PHASE2_STAGE3A_DRIPSTONE_DEEP_ORIGINAL_OWNER_ITEM_FLUID_FALL_WORLDGEN_MOD_1_21_1.md); [global mechanism contract](../../research/PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md).

Original 3 classes / 4 exact original BLOCK IDs: `CauldronBlock:minecraft:cauldron`, `LayeredCauldronBlock:minecraft:water_cauldron,minecraft:powder_snow_cauldron`, `LavaCauldronBlock:minecraft:lava_cauldron`. All four five full-signature owners `Block/BlockBehaviour/BlockBehaviour/BlockBehaviour/Block`. `AbstractCauldronBlock` abstract common dispatch/receiver, NOT fourth registered class. `CauldronInteraction` separate item map owner; precipitation originates ServerLevel world-Y heightmap, delayed source originates PointedDripstoneBlock; entity contact and client shape separate. Unknown third-party mod/foreign Level default PHYSICAL, typed algorithm opt-in only. Verified BareBonesBrews 1.21.1 source modifies base/Layered cauldrons by Mixin and event, but its declared NeoForge minimum 21.1.251 exceeds our 21.1.215: modded runtime acceptance NOT RUN.

- [x] 18.1 original registry/properties/five FULL owners rehash and inheritance.
- [x] 18.2 ITEM direct + CauldronInteraction, rain/drip/collision, independent mod source and dispatch.
- [x] 18.3 45 future acceptance cases designed, NONE executed.
- [x] 18.4 Python original ZIP vs live JS TSV FNV32/FNV64 MATCH on all four record streams, roadmap/checkpoint updated.

Original 241 classes/1060 IDs: now **100/241 classes /250/1060 IDs source reviewed**, **141 classes /810 IDs pending**. All **241×3=723** patched ASM/Planet gameplay acceptance fields PENDING. No Java/Mixin/CI/client/server tests. Stage18 research closed; Phase2/3A runtime remains OPEN.

**Next one «кк» → whole Stage3A-19 (all internal substeps)**: [CakeBlock / CandleBlock / CandleCakeBlock, 3 pending original classes /35 IDs, deep support/ignition/eating/interaction and mod owners](02q-cake-candle-candlecake-light-owners.md).
