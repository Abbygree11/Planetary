# Stage3A-33.4 — physically independent original NeoForge six-ID + frozen live 159→164 registry reconciliation

**2026-10-12, SOURCE only**, original immutable NeoForge 21.1.215 run `37988064055`, artifact `11643813158` SHA256 **`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`**. Exact ZIP bytes reread with Python `zipfile.ZipFile`, all three TSV via `csv.DictReader(delimiter="\\t")`, SHA256 recomputed from file. Contents: **1060 BLOCK rows, 241 concrete class names, 1333 ITEM rows, 1712 original BlockState-property rows**. Independent original 241 class|count roster FNV32/64 = **`0xf188a064 / 0x9ba3c115841e18e4`**, equals live 241×16 ledger.

## The six original registered IDs, five Java owner classes (not class name guesses)

| Original exact class | Exact ORIGINAL BLOCK registry IDs | Effective five method OWNER tuples, in order below | Exact original ITEM.placed_block joins | Exact legal state values |
| --- | --- | --- | --- | --- |
| `HoneyBlock` | `minecraft:honey_block` | `Block → BlockBehaviour → BlockBehaviour → BlockBehaviour → Block` | BlockItem | NONE |
| `SlimeBlock` | `minecraft:slime_block` | `Block → BlockBehaviour → BlockBehaviour → BlockBehaviour → Block` | BlockItem | NONE |
| `MovingPistonBlock` | `minecraft:moving_piston` | `Block → BlockBehaviour → BlockBehaviour → BlockBehaviour → Block` | NO ITEM | facing 6;type normal,sticky |
| `PistonBaseBlock` | `minecraft:piston,minecraft:sticky_piston` | `PistonBaseBlock → BlockBehaviour → BlockBehaviour → BlockBehaviour → PistonBaseBlock` | 2× BlockItem | facing 6;extended false,true |
| `PistonHeadBlock` | `minecraft:piston_head` | `Block → PistonHeadBlock → PistonHeadBlock → BlockBehaviour → Block` | NO ITEM | facing 6;short false,true;type normal,sticky |

Owner tuple columns in order, with original **full-qualified** selected argument types (match by name AND entire descriptor; overloaded `rotate` does **not** masquerade as an owner):
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

Original hierarchy: `HoneyBlock/SlimeBlock > HalfTransparentBlock > Block > BlockBehaviour`; `PistonBaseBlock/PistonHeadBlock > DirectionalBlock > Block > BlockBehaviour`; `MovingPistonBlock > BaseEntityBlock > Block > BlockBehaviour`. `piston`, `sticky_piston`, `honey_block`, `slime_block` have exactly one same-name original ITEM each with concrete `BlockItem>Item` hierarchy and `block_item=true`. No original ITEM.placed_block points to `piston_head` or `moving_piston`: both are internal/synthetic authors; do not invent registry Items. Four ITEM full effective methods/owners are original `BlockItem.useOn/ place/updatePlacementContext/getPlacementState/placeBlock/canPlace/registerBlocks` and separate `Item.use` (full parameter signatures in original ITEM TSV), not independent item class names.

Full original six-row canonical cohort SHA256 computed by Python **`4d08c959b74bf3d18349c57f164911ece2ad4153f5c79761b797e862c48759b0`**. Canonical ASCII serialization for independent reproduction: each registered BLOCK ID ascending lexical, one LF: `registry_id|java_class|class_hierarchy|semicolon(sorted property_name=legal_values)|comma(sorted original ITEM registry_id|class|block_item|placed_block), or NONE|five name=fullyQualifiedOwner(signature) separated by pipe`; concatenate six lines, SHA256. This checksum independently locks exact original ID/class/hierarchy/property/ITEM/five FULL signature owner data, not only human table shorthand.

## Live previous 159 protected, only five new reviewed rows

Read connected GitHub branch `2.0` prior to modification, 241×16 TSV, all 241 classes unique. Recomputed **all four frozen Stage32 pre-change FNV32/64 streams** and matched exactly against [prior Stage32 original 159 reconciled ledger](PHASE2_STAGE3A_BELL_ORIGINAL_159_CLASS_RECONCILIATION_1_21_1.md). Rewrote ONLY five indicated original pending class rows. All six original ID exact strings, five nearest declaring owners and 4/6 vs 2/6 item creators independently confirmed against physically reopened ZIP. All 241 class roster counts unchanged.

| Canonical source ledger stream (full-class-FQN lex, trailing ASCII LF) | BEFORE 159: FNV32 / FNV64 | AFTER 164: FNV32 / FNV64 |
| --- | --- | --- |
| all 241 `class|count` | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| reviewed `class|commaSortedOriginalIDs` | `0x428891bc / 0x6608827a4041ae1c` | `0x9d2c499f / 0x525ec5011088c91f` |
| reviewed `class|fiveExactDeclaringOwnerNames` | `0xbd9cbef9 / 0x6fdb1169b42bc4d9` | `0x80dcedf7 / 0x1f99f5bbc1dcaad7` |
| reviewed `class|commaSortedOriginalIDs|fiveExactDeclaringOwnerNames` | `0x0148705a / 0x44a178b63cd79ada` | `0x7bedd6f1 / 0x1baaa834b955ff51` |

**AFTER Stage33:** 164/241 `SOURCE_REVIEWED_INTEGRATION_PENDING` concrete vanilla classes; 701/1060 corresponding original BLOCK IDs. **77/241 classes and 359/1060 IDs SOURCE_PENDING**; **all 241×3=723 patched NeoForge ASM / Planet adapter / gameplay gates still `REVIEW_PENDING`**. All [72 Stage33 future game/compat test groups](PHASE2_STAGE3A_PISTON_SIX_FACE_SEAM_CORNER_MOD_ACCEPTANCE_1_21_1.md) `NOT RUN`. No Java/Mixin/Gradle edits or CI/client/server/real mod JAR acceptance. In particular SHA `3cae64530de368936d4c07497d3f0a25e2f9ffec` represents a pinned later 1.21.1 NeoForge comparative branch, **not proof of exact 21.1.215 patch or applied ASM**; keep patch acceptance OPEN. No Java vanilla subclasses outside exact original five class roster silently promoted, and separate Create mod classes are not counted as original vanilla class/ID.
