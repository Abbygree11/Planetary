# Stage3A-25.4 — independently original ZIP Python vs LIVE GitHub 129 source classes / 362 exact IDs and 5 full owner method signatures

**2026-10-11 · Stage3A-25.1–25.4 ORIGINAL-SOURCE COMPLETE only.** Physically opened archive `/mnt/data/phase2-neo1211-registry-census.zip`, exact original GitHub Actions artifact **11643813158** run **37988064055**, physically hashed **SHA256 7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**. Independent Python script `/mnt/data/_audit_phase2_25.py` **executed**, actual JSON `/mnt/data/_audit_phase2_25_result.json`, reads original `ZipFile + csv.DictReader` BLOCK/ITEM/properties. **Explicitly freezes previously reviewed 122 original Java short-names in its own source** (118 old Stage23 names + four Stage24 names), appends exactly seven Stage25 names. Python does **NOT** read the GitHub ledger as input. Original 241 concrete source classes,1060 BLOCK,1333 ITEM,1712 legal state-properties; validates all 129 class groups / **362 exact registered BLOCK IDs**, original entire five-method full-signature nearest declaring owner tuple for EVERY exact original ID, all 7 original direct BlockItem ITEM matches. Overloaded original owners matched exact FULL parameter signature, not method-name alone.

Fixed signatures:
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

| Original class/actual BLOCK and ITEM ID | EXACT original 5 nearest full-signature declaring owners | Original state property law |
| --- | --- | --- |
| LecternBlock: `minecraft:lectern` | LecternBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | facing4 horizontal, has_book bool, powered bool |
| JukeboxBlock: `minecraft:jukebox` | Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / JukeboxBlock | has_record bool ONLY, **no facing** |
| NoteBlock: `minecraft:note_block` | NoteBlock / BlockBehaviour / NoteBlock / BlockBehaviour / Block | 23 original instrument enums, note 0–24, powered bool |
| EnchantingTableBlock: `minecraft:enchanting_table` | Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | **no state properties** |
| SmithingTableBlock: `minecraft:smithing_table` | Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | **no state properties** |
| StonecutterBlock: `minecraft:stonecutter` | StonecutterBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block | facing4 horizontal ONLY |
| GrindstoneBlock: `minecraft:grindstone` | FaceAttachedHorizontalDirectionalBlock / GrindstoneBlock / FaceAttachedHorizontalDirectionalBlock / BlockBehaviour / Block | face CEILING/FLOOR/WALL, facing4 horizontal. Parent placement, Grindstone.canSurvive override TRUE |

**Independent LIVE second source:** JS `tools.mcp__GitHub__fetch_file` branch2.0 original 241×16 ledger `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`, SHA **14f879c47fe3ebe4eb7918b312da674c547ed206**, before editing any pending rows; promoted only seven source-verified original records **in memory**, retained previous 122 reviewed records, sorted all canonical ASCII full Java path and each class's original exact registered IDs, original 5 owner column order and record terminal newline. JS implementation independently computed FNV-1a 32/64 and compared against Python physical ZIP independent streams. **FOUR × FNV32+FNV64 MATCH, 0 discrepancies**:

| Physical ZIP canonical ASCII stream | Python ZIP FNV32 = LIVE GitHub JS FNV32 | Python ZIP FNV64 = LIVE JS FNV64 | Python-only stream SHA256 (not independently JS checked) |
| --- | --- | --- | --- |
| all241 `javaClass\|registeredCount` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| reviewed129 `javaClass\|sortedOriginalExactIDs` | `0xbbbfa358` | `0x7634d0250fdbcd78` | `924d20e642171ea8db4f3e6c265da20422f826f9d89978e4a8fd74fa7f6852f2` |
| reviewed129 `javaClass\|fiveFullOriginalOwners` | `0x2bf636cb` | `0xd5c921d72eb25dab` | `1614d968a53774d831c985207333045fe9d0cff498ed75ff9a14e5c05bf69f18` |
| reviewed129 `javaClass\|sortedOriginalIDs\|fiveOriginalOwners` | `0x284b5154` | `0x308e5c5fb17e2dd4` | `ea587c15f8a5aec7aeed110b95ac6016d18de0fcefec35ba918e6a7fd5ab488c` |

**Important evidence boundaries:** FNV = non-cryptographic comparison diagnostic, Python-only SHA256 canonical streams NOT independently recomputed in JS; original ZIP SHA256 authenticates physical archive bytes but **does not prove NeoForge21.1.215 transformed ASM or Mixin target descriptors**. Comparative decompiled unpatched Minecraft1.21.1 Java source, original reflection from ZIP, actual compiled patched ASM, adapter behavior, and loaded mod game acceptance are **separate gates**. No Java/Mixin/build/CI/client/server/gameplay loaded mod executed.

Actual pinned external sources: Apothic Enchanting MC1.21.1 1.6.4 SHA **3df39de4a564941b5b8856745467682ae8974e16** build NeoForge21.1.187, custom FilteringShelfBlock and ApothEnchantingTableBlock/vanilla EnchantingTableBlockEntity Mixin; and CustomDiscs MC1.21.1 1.1.0 SHA **98d5c9f8a3c48c7ada951f3476e64f2c8f03e480**, compiles NeoForge21.1.218 which is >Planet target21.1.215, declared min range includes target **but JAR load not tested**. Actual independent mod code real dimension/BlockPos physical audio/shelf owner unless explicit typed provider+caller local opt-in. [Deep source research](PHASE2_STAGE3A_WORKSTATION_MEDIA_ORIGINAL_BE_ITEM_MOD_1_21_1.md); [68 future tests ALL NOT RUN](PHASE2_STAGE3A_WORKSTATION_MEDIA_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md).

| Original source review status | Original Java classes | Original registered BLOCK IDs |
| --- | ---: | ---: |
| SOURCE_REVIEWED_INTEGRATION_PENDING | **129** | **362** |
| REVIEW_PENDING | **112** | **698** |
| Total | **241** | **1060** |

All **241×3=723** ASM/Planet/gameplay original runtime flags `REVIEW_PENDING`; all **68** WM25 tests FUTURE NOT RUN.

NEXT «кк» → [Stage3A-26 signs/hanging-signs/banners original data/block placement source family](../phases/phase-02/02x-sign-hanging-banner-text-owner-families.md), original **six pending Java concrete groups/76 BLOCK IDs**; four internal research steps in ONE turn.
