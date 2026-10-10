# Stage 3A-18.4 — independent original NeoForge 241/100-class cauldron reconciliation

**2026-10-11 · Stage18 4/4 SOURCE research only.** Original downloaded GitHub Actions artifact **11643813158**, run **37988064055**, sha256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e** independently rehashed from physical ZIP. The original archive includes 1060 BLOCK rows, 241 concrete Java registered BLOCK classes, 1333 ITEM rows and 1712 property entries. Python ZipFile+csv.DictReader script `_audit_phase2_18.py` used a frozen explicit 100-short-class reviewed list (previous 97 + exactly CauldronBlock/LayeredCauldronBlock/LavaCauldronBlock), NOT the edited ledger to determine which original rows to hash. Python matched each of five complete, fully qualified method parameter signatures and asserted both LayeredCauldronBlock IDs share identical full signatures. Unknown class/overloaded signature or class mismatch is an error; class names sorted lexically.

Separate LIVE GitHub JS TSV parser read the actual 241-row/16-header `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` blob; applied exactly three pending→reviewed rows in memory, recomputed original class roster/IDs/five owner cell signatures, and independently generated FNV-1a32/64 on canonical ASCII lines. **All four FNV32/FNV64 comparisons between original ZIP Python and live-ledger JS MATCH**.

| Canonical newline-terminated stream | Python original FNV32 == JS ledger FNV32 | Python original FNV64 == JS ledger FNV64 | Original Python SHA256 (not cross-verified in JS) |
| --- | --- | --- | --- |
| 241 class `javaClass\|registeredCount` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| 100 reviewed class `javaClass\|sortedExactIds` | `0x11061f89` | `0x63c2e23197dfe189` | `02f7e36d2bd52fdc151f9bd6b3f20f975f66881421fbf2a80b4b0dad40f50a12` |
| 100 reviewed class `javaClass\|owner1..owner5` | `0x662301c2` | `0x27f3fcb347270282` | `91111ee23748f37b240d47a8721d90994453f56410268d3885917fd38e9f1703` |
| 100 reviewed class `javaClass\|IDs\|owner1..owner5` | `0x437eda77` | `0x6c8078fe95a9cd57` | `868266368961439f506d7ad86b4c1614516190a271efc96cbbe195abf0a71f84` |

FNV checks are noncryptographic diagnostics; original ZIP SHA256 verifies bytes but cannot verify loaded NeoForge transformed bytecode. The full original method slots are exactly `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`, `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`, `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`, `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`, `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

Newly reviewed exact original rows:
- CauldronBlock, minecraft:cauldron: Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block;
- LayeredCauldronBlock, minecraft:powder_snow_cauldron AND minecraft:water_cauldron: same five;
- LavaCauldronBlock, minecraft:lava_cauldron: same five.
- AbstractCauldronBlock is an ABSTRACT shared owner, not an additional original registered concrete class. CauldronInteraction, Items$1, BareBonesBrews BE and source hooks are distinct authors NOT added as BLOCK registered class groups.

## Final source-only disposition

| GitHub ledger status | Concrete original BLOCK classes | Exact original BLOCK IDs |
| --- | ---: | ---: |
| SOURCE_REVIEWED_INTEGRATION_PENDING | **100** | **250** |
| REVIEW_PENDING | **141** | **810** |
| TOTAL | **241** | **1060** |

Actual live TSV three acceptance columns (`neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, `gameplay_acceptance`) are **REVIEW_PENDING in all 241 rows = 241×3=723 unresolved gates**. **45 CA18 test cases NOT RUN**. No Java/Mixin/code build, CI job, client/server game startup, transformed ASM or mod compatibility test performed. Preserve historical partial dripstone/falling manual evidence without claiming cauldron or whole-stage runtime PASS. [Detailed cauldron source/item/mod research](PHASE2_STAGE3A_CAULDRON_ORIGINAL_OWNER_ITEM_RAIN_DRIP_MOD_1_21_1.md), [future 45 cases](PHASE2_STAGE3A_CAULDRON_SIX_FACE_DRIP_PRECIPITATION_MOD_ACCEPTANCE_1_21_1.md).

NEXT one «кк»: whole Stage3A-19 candle/candle-cake/cake local support, lighting, ignition, eating, 1.21.1 mod overrides and compatibility. Frozen original candidates: `CakeBlock` 1 BLOCK ID, `CandleBlock` 17, `CandleCakeBlock` 17; **3 pending classes / 35 BLOCK IDs**. See [next card](../phases/phase-02/02q-cake-candle-candlecake-light-owners.md); source/ASM still pending.
