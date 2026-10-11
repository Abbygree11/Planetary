# Stage3A-24.4 — actual immutable NeoForge original ZIP vs live GitHub 122 exact owner+ID audit

**2026-10-11 · original SOURCE COMPLETE, no compiled ASM/Planet/gameplay acceptance.**

Original input was **physically opened** `/mnt/data/phase2-neo1211-registry-census.zip` (GitHub Actions artifact **11643813158**, run **37988064055**) using Python `zipfile.ZipFile`/`csv.DictReader`. SHA256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**. Executed independent Python source script `/mnt/data/_audit_phase2_24.py`, wrote actual `/mnt/data/_audit_phase2_24_result.json`. Code explicitly **freezes 118 prior source-reviewed class short names**, adds only four new ones; does **not** read or derive names/IDs/owners from updated GitHub ledger. Independently checks all **241 original concrete classes/1060 IDs/1333 items/1712 state property entries**, all 122 original source-reviewed class records containing exactly **355 original BLOCK IDs**, each exact **FIVE FULL qualified method signatures**, and exactly four original BlockItem ITEM→BLOCK joins. Overloaded method signatures are matched by full signature, not method names or first overload (including FireBlock overloads). All original legal properties for new classes asserted.

Exact method signatures: `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`; `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`; `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`; `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`; `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

| Class/original BLOCK+ITEM | FULL method declaring owner short names (in signature order) | Properties |
| --- | --- | --- |
| BlastFurnaceBlock `minecraft:blast_furnace` | AbstractFurnaceBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | facing 4 horizontal, lit boolean |
| BrewingStandBlock `minecraft:brewing_stand` | Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | has_bottle_0/1/2 only, **NO FACING** |
| FurnaceBlock `minecraft:furnace` | AbstractFurnaceBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | facing 4 horizontal, lit boolean |
| SmokerBlock `minecraft:smoker` | AbstractFurnaceBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | facing 4 horizontal, lit boolean |

**Independent second input**: actual LIVE GitHub `fetch_file(ref=2.0)` prior-original `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`, blob SHA **97f4cfadcabb3c7b13b8f0b5cca0fdeaea04b8cc**, 241×16; promoted only four original-verified still-pending rows **in memory**, and independently hashed canonical ASCII streams sorted by full class, sorted exact IDs, fixed five original owner columns and trailing newline. Python **original ZIP versus JS LIVE** four FNV-1a32/FNV-1a64 pairs matched:

| Canonical record stream | Original ZIP Python FNV32 == live GitHub JS FNV32 | Original Python FNV64 == live JS FNV64 | Python-only stream SHA256 |
| --- | --- | --- | --- |
| all 241 `class\|count` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| reviewed122 `class\|exactSortedIDs` | `0x7bb82bef` | `0x879228111a7e548f` | `10a0c2ea045a9dfa51c48ea4be6ee659e5effdf7a4eb7438077291ed4beb25fd` |
| reviewed122 `class\|fiveOwners` | `0x3bb6fac8` | `0xf499cd2f0db2d828` | `78570519811ec389679b78e8d8115f86ab4f84e85a0ca60bc4c38d817c2ae80c` |
| reviewed122 `class\|sortedIDs\|fiveOwners` | `0x17107656` | `0x917d27139b957c16` | `5ab417f78289cbe9413e1cb6a95fee416f6ef9cf053e7158816fd3deb696007c` |

FNV non-cryptographic crosschecks; Python-only SHA256 canonical streams NOT independently recomputed in JS. Actual ZIP hash verifies original archived data only, **NOT patched NeoForge21.1.215 bytecode**. This is a **source-owner review**, not Mixin/java implementation or actual game acceptance. Real IronFurnaces NeoForge source MC1.21.1 SHA **41fde020636b79154e1c454a893962c639d89e1b** gradle `neo_version=21.1.209`, range `[21.1,)`: mod *source* verified; real jar on Planet21.1.215 NOT loaded. Full BE/recipe/sided/capability evidence in [deep source](PHASE2_STAGE3A_FURNACE_BREWING_ORIGINAL_BE_ITEM_MOD_1_21_1.md); [all 56 PR24 future cases NOT RUN](PHASE2_STAGE3A_FURNACE_BREWING_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md).

| Original source status | Classes | Original BLOCK IDs |
| --- | ---: | ---: |
| SOURCE_REVIEWED_INTEGRATION_PENDING | **122** | **355** |
| REVIEW_PENDING | **119** | **705** |
| ALL | **241** | **1060** |

ALL **241×3=723** original per-class NeoForge patched ASM, Planet adapter, gameplay gates remain **REVIEW_PENDING**. No Java/Mixin/build/CI/server/client/gameplay/mod-loaded work.

Next «кк» completes [Stage3A-25 seven pending workstation/media blocks](../phases/phase-02/02w-workstation-media-interactive-owner-families.md) four subtasks together.
