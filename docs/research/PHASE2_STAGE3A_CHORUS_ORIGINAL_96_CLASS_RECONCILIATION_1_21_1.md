# Stage 3A-16.4 — exact original NeoForge 241/96-class chorus + deepest-safe cross-mod invariant reconciliation

**2026-10-11 · Minecraft 1.21.1 / NeoForge 21.1.215 · Planetary branch 2.0. Stage 3A-16 completed 4/4 SOURCE RESEARCH ONLY, ALL runtime/ASM/gameplay acceptance PENDING.** Original source/class owner [audit](PHASE2_STAGE3A_CHORUS_GROWTH_PIPEBLOCK_ITEM_WORLDGEN_ORIGINAL_AUDIT_1_21_1.md), [36 proposed UNRUN future acceptance tests](PHASE2_STAGE3A_CHORUS_SIX_FACE_BRANCH_CONNECTION_ACCEPTANCE_CONTRACT_1_21_1.md), new permanent [deepest-safe mechanism cross-mod policy](PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md) backed by [AGENTS.md §3A.2](../../AGENTS.md).

## Independent original physical ZIP reparse vs actual current GitHub ledger

The original unchanged `/mnt/data/phase2-neo1211-registry-census.zip` (Actions run **37988064055**, artifact **11643813158**, original source commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`) was **physically reopened** in this turn, SHA256 recalculated **MATCH** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`; original Python `ZipFile`/CSV parser independently counted **1060 original BLOCK IDs, 241 concrete original registered classes, 1333 ITEM, 1712 state-property rows**. Never edited the original ZIP or original census audit dispositions.

Python independent original audit script `/mnt/data/_audit_phase2_16_4.py` (local reproducibility artifact, not automatically pushed) freezes **96 source-reviewed full class names** from old frozen 94 source owners + exactly two newly researched ChorusFlowerBlock/ChorusPlantBlock class names (not selected from GitHub reviewer statuses). For each reviewed registered class, it reads **each original exact BLOCK ID** and parses `effective_method_owners`, splitting the original `;` overload list by fully-qualified argument signature (see retained `KelpBlock` LevelAccessor vs BlockPlaceContext guard). It compares all exact ID owner tuples within class. The five compiled-original owner slots, IN ORDER, are:
- `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`;
- `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`;
- `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`;
- `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`;
- `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

Independent JS re-parser reads actual LIVE mutable GitHub `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`, blob `ed3685cd896fff8127b310c51f96235948b17527`, exactly 241 rows × 16 header-named columns, and selects 96 actual `SOURCE_REVIEWED_INTEGRATION_PENDING` reviewed classes; no use of original list for this selection. Both original Python and GitHub JS compute deterministic UTF-8 canonical strings sorted by raw Java FQCN codepoint order, one final newline per line, IDs lexicographically comma-joined, 5 nearest declaring-owner values ordered by above full signatures.

### Four exact original Python vs current GitHub JS fingerprint pairs ALL MATCH

| Canonical original/live strings | Original Python FNV32 = LIVE JS FNV32 | Original Python FNV64 = LIVE JS FNV64 | Python original-only SHA256 | Outcome |
|---|---|---|---|---|
| 241 `class|registeredIdCount\n` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | MATCH |
| Reviewed 96 `class|sortedExactId1,...\n` | `0xf2531dea` | `0x856b4baecaf0200a` | `31e57da97536d6fbf0995e88e49428c26513d552778d4b541f55ede5e3f7b527` | MATCH |
| Reviewed 96 `class|owner1|...|owner5\n` | `0xa636248e` | `0xf6cc39f7e8bb980e` | `deba69afa4c9eedc2fa144e6d87efa4ef514bd40279da7d0cdf0e08615a34b8c` | MATCH |
| Reviewed 96 `class|ids|owner1|...|owner5\n` | `0x3586d709` | `0xfc940c112ecac149` | `b8038e26296129c6298a114ed1d91d88f8d94351921be9bec36c6abf379c0139` | MATCH |

Hashes FNV32/FNV64 diagnose record equivalence, are **not** cryptographic proof. Canonical string SHA256 original-side Python only; do not invent an independent JS SHA256 check. Original archive SHA256 establishes original source bytes, NOT loaded NeoForge patch bytecode or an applied Mixin.

## New two classes, immutable previously reviewed unaffected, all independent acceptance gates still pending

| Newly source-audited exact original runtime class + ID | Original exact five declaring owners |
|---|---|
| `ChorusFlowerBlock` / `minecraft:chorus_flower` | **Block / ChorusFlowerBlock / ChorusFlowerBlock / ChorusFlowerBlock / Block** |
| `ChorusPlantBlock` / `minecraft:chorus_plant` | **ChorusPlantBlock / ChorusPlantBlock / ChorusPlantBlock / BlockBehaviour / Block** |

Both one ordinary ITEM BlockItem each out of 1333 original ITEM joins, `ChorusFlowerBlock` AGE0..5; `ChorusPlantBlock` six direction boolean properties (64 `PipeBlock` VoxelShape combinations). Source cross-phase creator and dispatch DAG: `BlockItem → ChorusFlowerBlock(Block default placement) / ChorusPlantBlock.getStateForPlacement`, then `BlockStateBase.updateShape → own override`; `ChorusPlantFeature → ChorusFlowerBlock.generatePlant → growTreeRecursive → LevelAccessor.setBlock` bypasses item; `ChorusFlowerBlock.randomTick` independently owns world-UP and world-HORIZONTAL branches, death AGE5 and events 1033/1034. Plant physical six-neighbor Boolean adjacency is a different algorithm from growth plane and from shell-local support; `PipeBlock` only owns shared geometry, not subclass survival/connection logic. Permanent cross-mod gate now codified at [AGENTS §3A.2](../../AGENTS.md): **lowest common safe semantic helper with coordinate provenance + opt-in**, unknown mods physical, no dangerous universal primitive rewrite. Nothing implemented as runtime code here.

| Actual current live mutable ledger disposition | Exact registered concrete classes | Exact BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` (source + original compiled 5 declaring owners only) | **96** | **245** |
| `REVIEW_PENDING` class/count known only | **145** | **815** |
| Total original | **241** | **1060** |

ALL **241 × 3 = 723** vanilla original `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, `gameplay_acceptance` remain **REVIEW_PENDING**. New 36 CH16 test cases and any modded downstream integration tests NOT RUN, no Java/Mixins or CI build/client/server game. Research Stage16 DONE 4/4, Stage3A/Phase2 still OPEN.

## Source-pending next algorithmic owner family handoff: pointed dripstone

[Stage3A-17 card](../phases/phase-02/02o-pointed-dripstone-fluid-falling-owners.md) covers **one** original source-pending registered `PointedDripstoneBlock` / `minecraft:pointed_dripstone` (1 original exact BLOCK ID) but many **distinct engine mechanisms and mod analog risk**: six-face directional tip/vertical segments and attachment, drip-fluid/cauldron fallback, scheduled growing tips, FallingBlockEntity/impact+damage and particles, natural dripstone worldgen and fake Levels. This is a DEEP multi-mechanism family even though one registered concrete class; need full source owner inheritance, every alternate writer and exact domain split. Previously some Planetary dripstone runtime behavior manually tested in older code, but this original owner census still SOURCE REVIEW_PENDING; never re-label previously accepted functionality as unimplemented and never mark this class fully accepted just from source research. Four internal stages one future «кк», all patched ASM/Planet/gameplay gates remain pending.
