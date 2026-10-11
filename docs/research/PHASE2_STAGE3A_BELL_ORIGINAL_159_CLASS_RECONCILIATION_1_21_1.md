# Stage3A-32.4 — independently reopened original Bell source/compiled-owner registry versus live 159×16 GitHub ledger

2026-10-12, branch `2.0`, Minecraft 1.21.1 NeoForge21.1.215. **Source/original reflected effective full signature owner audit DONE; all Planet, patched bytecode, mod JAR and gameplay acceptance remain PENDING.**

## Physical immutable source and exact new cohort proof

Re-downloaded **original unchanged** GitHub Actions run `37988064055` artifact **`11643813158`** `phase2-neo1211-registry-census` ZIP, physically inspected with Python `zipfile`/`csv.DictReader` and recomputed actual SHA-256 **`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`** (matches artifact metadata). Original ZIP has **1060 BLOCK** registrations, **241** concrete Java classes, **1333 ITEM**, **1712 BlockState properties**, and **1060 distinct BLOCK IDs**. This time actual ZIP bytes, not only earlier notebook/hash statements, were rechecked. The independent original source roster ASCII FNV-1a32/64 `0xf188a064 / 0x9ba3c115841e18e4` matches the original live 241-class ledger.

Exact sole original `net.minecraft.world.level.block.BellBlock` row: `minecraft:bell`, hierarchy `BellBlock>BaseEntityBlock>Block>BlockBehaviour`. Original `phase2-neo1211-state-properties.tsv` permitted values:
- `attachment=CEILING,DOUBLE_WALL,FLOOR,SINGLE_WALL`;
- `facing=east,north,south,west` (NOT six directions);
- `powered=false,true`.

One and only one exact original `ITEM.placed_block=minecraft:bell` join: `ITEM.registry_id=minecraft:bell`, concrete class `net.minecraft.world.item.BlockItem`, `block_item=true` and hierarchy `BlockItem>Item`. Original full ITEM method chain: `useOn=BlockItem(UseOnContext)`; `use=Item(Level,Player,InteractionHand)`; `place/updatePlacementContext/getPlacementState/placeBlock/canPlace/registerBlocks` all `BlockItem` effective owner. This proves the standard ITEM placement entrypoint, not that later commands, structure writers, blockstate components or mod Item instances use it.

Exact original **FULL-qualified** five reflection method declaration owners, independently selected by method name (no ambiguous overload shortcut):
1. `getStateForPlacement=BellBlock(net.minecraft.world.item.context.BlockPlaceContext)`;
2. `canSurvive=BellBlock(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`;
3. `updateShape=BellBlock(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`;
4. `randomTick=BlockBehaviour(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`;
5. `setPlacedBy=Block(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

Additional original effective owner callbacks show `BellBlock.neighborChanged`, `BellBlock.useWithoutItem`, `BellBlock.getShape/getCollisionShape/rotate/mirror`; explicit `getTicker`/BE/renderer adaptation still requires concrete runtime checks. SHA256 of deterministic ASCII single-Bell canonical record `registryId\njavaClass\nclassHierarchy\nproperties=<sorted name=legalValues separated by semicolon>\nitem=registryId|class|placedBlock\n<five name=fullSignature lines>\n` is **`2d3c0804a72a64cd6b45393fe08ee67be2bde7b0e2cb5fe231613470877b5cc6`**. Reproducibility: Python `sha256sum` raw ZIP, `zipfile.ZipFile` and `csv.DictReader(delimiter='\\t')` reading BLOCK/ITEM/property TSV, select one exact BellBlock class ID and full signature values; no generated substituting IDs.

## Independently protected previous 158 reviewed rows, then exact one-class promotion

Before modifying ledger, fetched connected **LIVE `2.0` 241×16 TSV** and recomputed all four actual ASCII JavaScript FNV-1a32/64 frozen Stage31 streams. They matched [Stage31 previous source checkpoint](PHASE2_STAGE3A_DIRECTIONAL_PATTERN_158_CLASS_RECONCILIATION_1_21_1.md) pairwise; therefore previous 158 reviewed class exact IDs and five owner tuples not accidentally changed. Only the existing `BellBlock` row is replaced; no other Java class row modified.

| Canonical stream (FQN lexical sort, LF) | BEFORE 158: FNV32 / FNV64 | AFTER 159: FNV32 / FNV64 |
| --- | --- | --- |
| All 241 `class|count` roster | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| Reviewed `class|sortedExactIDs` | `0xb89705d1 / 0x1faa132c12ee51f1` | `0x428891bc / 0x6608827a4041ae1c` |
| Reviewed `class|fiveOwners` (owners pipe-separated) | `0xa09c410b / 0x6267d65fbb702c6b` | `0xbd9cbef9 / 0x6fdb1169b42bc4d9` |
| Reviewed `class|sortedExactIDs|fiveOwners` | `0x3364c74a / 0x759c829b29a1626a` | `0x0148705a / 0x44a178b63cd79ada` |

These independent layers serve different purposes: physical original ZIP SHA256 proves input; original canonical Bell SHA256 proves new exact ID/ITEM/legal state/full owner; JavaScript FNV32/64 LIVE ledger proves old dataset unchanged and new row included; original 241-class roster hash independently matches LIVE. We do NOT claim a fresh byte-for-byte re-comparison of all 158 previously source-reviewed owners against the immutable ZIP in this step, nor treat compiled declaring owner reflection as the final NeoForge patched ASM/Mixin target verification.

Current STATUS: **159/241 vanilla source+original reflected owner reviewed**; **695/1060 exact vanilla BLOCK IDs** in reviewed source classes. Still **82/241 classes / 365/1060 BLOCK IDs SOURCE pending**. Exactly **all 241×3=723** separate `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, and `gameplay_acceptance` ledger values remain **REVIEW_PENDING**. All [48 Bell acceptance groups](PHASE2_STAGE3A_BELL_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md) **NOT RUN**; no Java/Mixin, Gradle, CI, client/server/gameplay or mod compatibility PASS.

**NEXT Stage3A-33 (whole four-substage research packet in one request):** [piston movement/push-resolver/slime-honey mechanism family](../phases/phase-02/02ae-piston-moving-slime-honey-owner-families.md) original pending five concrete classes / six IDs; preserve actual source/target physical topology and NeoForge hooks versus Planet frame semantics. Includes vanilla and pinned modded Create comparators without conflating Create contraption World/Level.
