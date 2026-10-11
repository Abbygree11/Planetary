# Stage3A-22.4 — original physical NeoForge ZIP against independently LIVE 241/112 GitHub original full owner/ID ledger

**2026-10-11, SOURCE ONLY.** Actually read physical original GitHub Actions `phase2-neo1211-registry-census.zip` artifact **11643813158**, run **37988064055**, local `/mnt/data/phase2-neo1211-registry-census.zip` with Python `zipfile.ZipFile` + `csv.DictReader`; SHA256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**. 241 original concrete registered vanilla classes /1060 BLOCK IDs/1333 ITEM IDs/1712 property entries. Script `/mnt/data/_audit_phase2_22.py` (executed; results `/mnt/data/_audit_phase2_22_result.json`) used explicit frozen **107 prior original class names** (the Stage21 original roster), added only five Stage22 class names, and independently matched original rows for all 112 classes, exact ID+full FIVE method declaring owner signatures for each original registered ID. It rejected mismatched overload/method arguments and ensured **five and only five** original direct `BlockItem` joins for the five new IDs.

Fixed exact FULL method signatures (five columns in order):
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

| Concrete class | Exact original BLOCK ID | Original five fully-qualified-resolved declaring owner SHORT names |
| --- | --- | --- |
| BarrelBlock | minecraft:barrel | **BarrelBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block** |
| ChestBlock | minecraft:chest | **ChestBlock / BlockBehaviour / ChestBlock / BlockBehaviour / Block** |
| ChiseledBookShelfBlock | minecraft:chiseled_bookshelf | **ChiseledBookShelfBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block** |
| DecoratedPotBlock | minecraft:decorated_pot | **DecoratedPotBlock / BlockBehaviour / DecoratedPotBlock / BlockBehaviour / Block** |
| EnderChestBlock | minecraft:ender_chest | **EnderChestBlock / BlockBehaviour / EnderChestBlock / BlockBehaviour / Block** |

A separate JavaScript execution fetched actually unchanged LIVE GitHub `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` old blob SHA **64234adc461b7d46128325f3843f437396e27c61** (241 rows x16 columns) and promoted only five pending original-verified rows **in memory** to produce sorted ASCII canonical class/ID/full-five-owner streams. Original ZIP Python and live JS independent FNV-1a32/FNV-1a64 **FOUR PAIRS ALL MATCH**:

| Canonical sorted ASCII newline-terminated records | Original ZIP Python FNV32 = LIVE GitHub JS FNV32 | Original Python FNV64 = LIVE GitHub JS FNV64 | Python-only original SHA256 |
| --- | --- | --- | --- |
| All 241 `class\|count` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| Source 112 `class\|sortedExactBlockIDs` | `0x13b4c018` | `0x1c3c5ba03c7a3498` | `9492d06198b6ceab44d660b0084f7eb1b2069e44c2d732bad7009b776a1874e1` |
| Source 112 `class\|fiveOwners` | `0x305af051` | `0x1486dab73e5d29d1` | `8fcd4e42455024b21af1d4996a3eb35992bc798fdbffdacf67ebf5a393cbe118` |
| Source 112 `class\|sortedIDs\|fiveOwners` | `0x5aecd211` | `0x04804864e0640f91` | `d3d87b4fafd8f2b82a6ff2c1890fad7bc3260b152af7e9b762b6ca2fa66c2cc8` |

FNV non-cryptographic diagnostics, SHA256 canonical streams computed **original Python only** (NOT independently JS-rehashed). ZIP SHA256 verifies actual archived bytes, NOT exact transformed target NeoForge21.1.215 patched method bodies or client/server. Original source [whole five-class/BE/mod analysis](PHASE2_STAGE3A_STORAGE_CONTAINER_DEEP_OWNER_ITEM_BE_MOD_1_21_1.md) pinned vanilla comparable source b77c5c… and Sophisticated Storage MC1.21.1 commit 58a5d04… **requires NeoForge>=21.1.229** above target21.1.215; no actual loaded mod acceptance.

| Original class group status | Concrete classes | Exact original BLOCK IDs |
| --- | ---: | ---: |
| SOURCE_REVIEWED_INTEGRATION_PENDING | **112** | **329** |
| REVIEW_PENDING | **129** | **731** |
| TOTAL | **241** | **1060** |

ALL **241×3=723** original NeoForge patched bytecode/Planet adapter/gameplay flags `REVIEW_PENDING`. [All 55 ST22 acceptance test contracts NOT RUN](PHASE2_STAGE3A_STORAGE_CONTAINER_SIX_FACE_BE_MOD_ACCEPTANCE_1_21_1.md). No Java/Mixin/build/CI/gameplay/dedicated client/server/loaded-mod testing occurred.

Next «кк» whole [Stage3A-23 physical inventory neighbor logistics and automation](../phases/phase-02/02u-inventory-neighbor-logistics-automation-owners.md): ShulkerBoxBlock17 BLOCK IDs + TrappedChest/Hopper/Dispenser/Dropper/Crafter1 each = **six pending classes/22 BLOCK IDs**.
