# Stage 3A-10.4 — independent original NeoForge 241/75-class LightningRodBlock reconciliation and next hatch/substrate family

**2026-10-10 · Planetary `2.0` · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.** Fourth internal checkpoint and CLOSE of the *research-only* Stage 3A-10 packet. Full family conducted from a **single user “кк”**. This is **not** NeoForge patched bytecode inspection, any applied Planetary adapter or gameplay PASS.

## 1. New independent original census evidence

Actually rehashed and reparsed the unchanged local ZIP `/mnt/data/phase2-neo1211-registry-census.zip` (GitHub [Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, original source at `aa39572950a15403ea0a9003eefccf3bf6675ff7`). Its **SHA-256 is exactly** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Fresh independent Python `zipfile.ZipFile`/`csv.DictReader(delimiter="\t")`:
- 1060 actual BLOCK registry IDs / **241 distinct fully qualified registered concrete BLOCK classes**;
- 1333 separate ITEM registered rows;
- 1712 state-property rows;
- immutable historic `audit_disposition` fields not modified.

The original Python reconciliation groups all 1060 BLOCK records by exact `java_class`, sorts by fully qualified name, filters the **exact 75 currently marked reviewed full-class names** from live GitHub ledger (earlier 74 plus one LightningRodBlock), confirms **each source class ID** and verifies five **full argument-signature** `effective_method_owners` for each original registry row. Method slots: `getStateForPlacement(BlockPlaceContext)`, `canSurvive(BlockState,LevelReader,BlockPos)`, `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`, `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`, `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`. Parsed actual full qualified argument text; do not match overloaded signature names alone. Original ownership same within every registered class. Pinned source LightningRodBlock/RodBlock/DirectionalBlock/LightningBolt and exact ITEM/alternate authors in [3A-10.1+10.2](PHASE2_STAGE3A_LIGHTNING_ROD_OWNER_ITEM_ALTERNATE_AUTHORS_1_21_1.md), six-face contract [3A-10.3](PHASE2_STAGE3A_LIGHTNING_ROD_SIX_FACE_STRIKE_SIGNAL_ACCEPTANCE_CONTRACT_1_21_1.md).

**Current independent GitHub ledger** at preceding Stage 10.1/10.2 checkpoint commit [`f1de7cca8b6fa96f07d2d7f73aeebe8092d44b4d`](https://github.com/Abbygree11/Planetary/commit/f1de7cca8b6fa96f07d2d7f73aeebe8092d44b4d), exact 16-column `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` blob `3386419209017bb42019e050bf50475e260883b8`. **An independent JavaScript parser** (not the Python original arrays) read the live 241 GitHub TSV lines, sorted class names by raw code-point lexical ordering (NOT locale-sensitive `localeCompare`, which would change the canonical order), constructed same four canonical UTF-8 string forms (one final newline per row) and computed independent FNV-1a32 and FNV-1a64. All four original ZIP vs GitHub pairs **MATCH**:

| Canonical data (sorted full class) | ORIGINAL ZIP FNV-1a 32/64 | Live GitHub FNV-1a 32/64 | SHA-256 **original side only** | Verdict |
|---|---|---|---|---|
| All `class|count\n` (241) | `0xf188a064` / `0x9ba3c115841e18e4` | same | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | MATCH |
| Reviewed `class|sortedCommaJoinedExactIds\n` (75) | `0x10744d73` / `0x358f18d9a4630613` | same | `6d991dca84c0794de53cc0d991257c73e39cd1d47ea575b364a750ebfd42f1c1` | MATCH |
| Reviewed `class|5OwnerNames\n` (75) | `0xc4d4c46e` / `0xbd4cb01112604f6e` | same | `59974303d7e441ab75ac3b6d5a98ef24ec553f3decbfd5063046a43e6b607215` | MATCH |
| Reviewed `class|sortedCommaJoinedExactIds|5Owners\n` (75) | `0x2fb5ae99` / `0x4b8aea415b2bd239` | same | `d97a8f098200c0f9374a783e8f125695a7904f23f6d97da7e14fefadf9f768e8` | MATCH |

Canonical source uses actual entries `fullClass|id1,id2|owner1|owner2|owner3|owner4|owner5\n`; comma-sorted `registry_id` within each class and code-point lexical class order. Original-side SHA-256 hashes are provided to allow *collision-resistant local reproducibility*; they were **not independently SHA-256 recomputed on JavaScript side**, which is why only original side is labelled SHA-256. Independent FNV is a useful mismatch diagnostic, not cryptographic equality proof.

## 2. Result and strict outstanding gates

| Original source+compiled method declaration disposition | Classes | Exact registered BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **75 / 241** | **196 / 1060** |
| `REVIEW_PENDING` | **166 / 241** | **864 / 1060** |
| Total accounted for | **241** | **1060** |

Every class is unique in 16-column ledger, 241 row class/count roster matches original, numeric ID counts sum to 1060, all 75 source-reviewed classes include exact IDs and five filled owner values with `REFLECTION_OWNER_VERIFIED`, 166 pending rows retain `SEE_REGISTRY_ARTIFACT`/`REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY` (no fabricated details). Independent GitHub script verified **each of the 241 rows** in all three runtime columns `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, `gameplay_acceptance` is still `REVIEW_PENDING` — **723 untouched fields**. Do not mistake an original reflection signature method declaration for patch-body bytecode or applied Mixin acceptance. 28 LR10-01…LR10-28 fixtures [in 10.3](PHASE2_STAGE3A_LIGHTNING_ROD_SIX_FACE_STRIKE_SIGNAL_ACCEPTANCE_CONTRACT_1_21_1.md) **ALL NOT RUN**.

Precisely one class newly source+original declaring-owner promoted relative to [3A-9.4's prior 74-class ledger](PHASE2_STAGE3A_SCULK_GROWTH_ORIGINAL_74_CLASS_RECONCILIATION_1_21_1.md): `net.minecraft.world.level.block.LightningRodBlock`, exactly `minecraft:lightning_rod`, hierarchy `LightningRodBlock>RodBlock>DirectionalBlock>Block>BlockBehaviour`, properties `facing,powered,waterlogged`, original owner tuple **`LightningRodBlock / BlockBehaviour / LightningRodBlock / BlockBehaviour / Block`**. Original exact BLOCK row SHA256 `ffbd701b26badf030a2afb591df34474b4b421d44e81223fb58f92111437198d`. Original ITEM precisely one `minecraft:lightning_rod` `BlockItem` with 7/7 BlockItem placing owners, separate `Item.use`; ITEM row SHA256 `a6c4fdd635e83c8edba6e53f3d59674406a5ff69c597497246ba79247ba286d4`. `EndRodBlock` already reviewed earlier, **no repeat promotion**; CopperBulb/weathering copper blocks not audited/promoted here.

## 3. Next exact registered algorithm-owner packet: hatch/substrate egg blocks, not one patched example

Actual original NeoForge 21.1.215 BLOCK records, *not guessed block names* (all THREE unreviewed in ledger):

| Original concrete class / one registered ID each | Original class/state properties | Mechanism family reason |
|---|---|---|
| `TurtleEggBlock` / `minecraft:turtle_egg` | `TurtleEggBlock>Block>BlockBehaviour`; `eggs,hatch` | Stacking via BlockItem/default replacement, sand `pos.below()` hatch substrate, randomTick/time threshold, entity footstep/fall damage and baby turtle hatch creation |
| `SnifferEggBlock` / `minecraft:sniffer_egg` | `SnifferEggBlock>Block>BlockBehaviour`; `hatch` | scheduled tick-based hatch, `hatchBoost` support under egg, onPlace/ticker, sniffer entity spawn; different lifecycle than turtle egg |
| `FrogspawnBlock` / `minecraft:frogspawn` | `FrogspawnBlock>Block>BlockBehaviour`; no declared properties | water placement surface survival/updateShape/scheduled tick, frogspawn removal, tadpole spawning; original special `PlaceOnWaterBlockItem` ITEM, NOT ordinary BlockItem |

**This is source/registry triage, not a premature review of these three classes.** Exact original registered ITEM joins: `minecraft:turtle_egg` → ordinary BlockItem; `minecraft:sniffer_egg` → ordinary BlockItem; `minecraft:frogspawn` → `PlaceOnWaterBlockItem` (independent special author). Support and hatch axes are physical world-Y in vanilla, different substrate/media and different entity lifecycle; a general “all eggs use a common facing” patch would be incorrect. Phase 2 local support/survive/hatch target and stacking; Phase 5 water; Phase 7 entity spawning/contact damage, Phase 8/9 biome/spawn/worldgen; Phase 3 particles/sounds. None of these three classes is promoted in current report. The [new Stage 3A-11 card](../phases/phase-02/02i-egg-hatching-substrate-owners.md) requests four **internal** substages, executed on **one** next user “кк”, with durable checkpoint commits if needed.

## 4. End-of-family handoff

**Stage 3A-10 complete 4/4 SOURCE RESEARCH**, future acceptance UNRUN, stage 3A and Phase 2 still **OPEN**. No Java changes, patched-ASM integration proofs, builds, game client/server tests or manual gameplay acceptance. **NEXT “кк” = entire Stage 3A-11 egg/hatching/substrate three-class packet**, not merely 11.1; internally execute all four ordered substeps, commit checkpoints and final status, one compact conversation result.
