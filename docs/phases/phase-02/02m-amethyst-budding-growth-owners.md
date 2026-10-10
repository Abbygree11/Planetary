# Stage 3A-15 — amethyst parent, budding-amethyst six-direction growth and pre-reviewed crystal-cluster mechanics

**Status: ACTIVE — 0/4 INTERNAL source-research steps, all ASM/Planet/gameplay PENDING.** Branch `2.0`, Minecraft 1.21.1 / NeoForge 21.1.215, Java 21.

**ONE user “кк” → complete ALL four internal tasks 15.1→15.2→15.3→15.4 in one continuous assistant turn**, with small durable GitHub docs/ledger commits and concise user progress; no request for four further messages. If interrupted, resume first unchecked internal step of SAME family, do not skip source owner verification. Never claim patch bytecode or game acceptance from source-only evidence; do not change Java.

[Prior 3A-14.4 independent 241/92 original registry reconciliation](../../research/PHASE2_STAGE3A_AQUATIC_ORIGINAL_92_CLASS_RECONCILIATION_1_21_1.md) · [241 registered concrete class owner ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) · [Phase2 roadmap](../phase-02.md).

## Exact immutable original Minecraft 1.21.1 / NeoForge 21.1.215 registered scope

Physical original ZIP artifact 11643813158, SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`, 1060 exact BLOCK IDs / 241 concrete classes, 1333 ITEM, 1712 properties.

| Exact registered BLOCK class | Original exact BLOCK IDs | State properties | Historical current source-reviewed status |
|---|---|---|---|
| `net.minecraft.world.level.block.AmethystBlock` | **1**: `minecraft:amethyst_block` | none | **REVIEW_PENDING** |
| `net.minecraft.world.level.block.BuddingAmethystBlock` | **1**: `minecraft:budding_amethyst` | none | **REVIEW_PENDING** |
| `net.minecraft.world.level.block.AmethystClusterBlock` (dependency only) | **4**: `minecraft:small_amethyst_bud`, `medium_amethyst_bud`, `large_amethyst_bud`, `amethyst_cluster` | `facing,waterlogged` | **ALREADY SOURCE_REVIEWED_INTEGRATION_PENDING**, not newly promoted |

Actual compiled hierarchy (from untouched original archive): `AmethystBlock>Block>BlockBehaviour`; `BuddingAmethystBlock>AmethystBlock>Block>BlockBehaviour`; `AmethystClusterBlock>AmethystBlock>Block>BlockBehaviour`. Two new source-pending concrete classes, two exact registered BLOCK IDs, plus already-reviewed four-bud registered IDs. Original 1333 ITEM exact join one standard BlockItem for `minecraft:amethyst_block` and one BlockItem for `minecraft:budding_amethyst`, and existing four cluster items separately; verify full ITEM owners in 15.2. No imaginary 7th registered class.

Confirmed preliminary pinned Minecraft 1.21.1 [`BuddingAmethystBlock.randomTick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BuddingAmethystBlock.java) chooses one **physical Direction.values()** of six and ages bud chain based on matching `AmethystClusterBlock.FACING`, writes new bud with FACING and target waterlogged state; the already reviewed `AmethystClusterBlock` owns full 6-direction placement/survival/shape and waterlogged mechanisms. Generic Minecraft coordinate rewrite would modify physical 6-neighbor growth graph; Planet source/target canonical frames and physical target FluidState are distinct. Parent block `AmethystBlock` is not a bud and need not own growth tick just because it is superclass.

Cross phase: Phase2 six-face directional bud facing/target support, Phase5 waterlogging, Phase3 cluster voxel/render/culling, Phase8/9 geode feature and natural growth, Phase7 special harvest/entity interaction if source proves; preserve no-Planet vanilla tests. Stage15 is source research, not Java implementation.

## Four internal tasks for ONE next “кк”

- [ ] **15.1 — original 241 concrete BLOCK class source/nearest full signature owner audit for exactly TWO pending classes plus inherited AmethystClusterBlock context.** Rehash original immutable ZIP, check 2 original ID/hierarchy/property rows and all five exact full argument nearest method declaring owners and critical `BuddingAmethystBlock.randomTick`, `AmethystClusterBlock` placement/support/waterlog, existing owner crosschecks. Promote ONLY two new fully source+compiled-owner-reviewed classes, never repeat an already reviewed cluster; leave all patched ASM/Planet/gameplay gates pending. Commit bounded checkpoint then continue.
- [ ] **15.2 — exact original 1333 ITEM join and geode/worldgen/direct creator audit.** Separate two ordinary BlockItems (AmethystBlock, BuddingAmethystBlock) from four already registered bud Item creators; follow `BuddingAmethystBlock.randomTick` direct creation/upgrade along physical 6-axis, `AmethystClusterBlock` physical FACING support, geode feature and source predicates, commands/templates/loot/dead growth bypasses. Never invent shipped structure .nbt or claims from one source file. Commit and continue.
- [ ] **15.3 — six Planet faces, all 12 edges/8 corners, six-neighbor bud chain and waterlogged acceptance contract with ≥25 future deterministic tests (ALL NOT RUN).** Distinguish physical six-neighbor growth from source canonical local-up, target physically facing neighbor, missing support, charged growth odds/timing, geode volume, client shape/water/FluidState and vanilla fallback. Commit and continue without user message.
- [ ] **15.4 — independently compare immutable original ZIP exact 241-class/ID roster and reviewed all five full-qualified method owners versus actually LIVE GitHub 16-column ledger; verify all 241×3 NeoForge ASM/Planet/gameplay gates pending, update roadmap/checkpoint/AI_CONTEXT and prepare next real pending owner-family card.** One final docs-only GitHub commit, STOP entire Stage15 and give concise user result. Retain **KelpBlock getStateForPlacement overload** full-signature matching hard gate from Stage14 audit; never select first overloaded declaration by name.

## Durable inherited Stage14 checkpoint

[Original Sponge/WetSponge/SeaPickle block+item/alternate writers](../../research/PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_ORIGINAL_BLOCK_ITEM_WRITERS_1_21_1.md), [32 future UNRUN sponge water BFS/SeaPickle six-face tests](../../research/PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_SIX_FACE_WATER_CONTRACT_1_21_1.md), [independent full original 241/92-class census reconciliation with correct exact overloads](../../research/PHASE2_STAGE3A_AQUATIC_ORIGINAL_92_CLASS_RECONCILIATION_1_21_1.md). Prior source-reviewed **92/241 classes, 241/1060 exact BLOCK IDs**; pending **149/241 / 819/1060**. All **241×3** NeoForge patched ASM, Planet adapter and gameplay statuses **REVIEW_PENDING**. No Java/Mixin/build/CI/real client/server/gameplay acceptance. **NEXT one “кк” → whole Stage 3A-15**, not only 15.1.
