# Stage3A-37.4 — immutable original NeoForge21.1.215 9 portal/structure/special BE classes source 199→208 reconciliation

**2026-10-12**, unmodified real ZIP `/mnt/data/planetary-registry-raw.zip` (local source artifact only) artifact11643813158 run37988064055 **SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`** physically rechecked with container `sha256sum` and Python `hashlib/zipfile/csv`; original **1060 BLOCK /1333 ITEM /1712 state-property/241 class** rows. Python independently generated `/mnt/data/_audit_stage37.py` and `/mnt/data/_stage37_original_evidence.json` (local audit evidence), selected **all exact 9 pending registered Java FQN classes**, verified exactly one original registered ID each, class hierarchy, all legal original state values and five FULL effective declaring-owner method signatures; exact original 9-ID canonical SHA256 **`8d7be12235a7ab003ad2e9175db9736f547ec21e72eb6de0c7f92259b8fc5a9c`**. Not a file link/download or purported real game test.

Canonical record hash protocol (Python source): in lexical original `registry_id` order for each original compiled row, append UTF8 line `registry_id|java_class|class_hierarchy|semicolon(sorted original property_name=legal_values)|comma(sorted ITEM.registry_id|java_class|block_item|placed_block) OR NONE|five name=FULL_FQN_DECLARING_OWNER(full_args) in fixed method order + LF`; concatenate 9 lines, SHA256. No invented ID/state/method owner.

## Original 9 Java classes and creator/disposition registry, untouched comparison mode

| Exact original BLOCK registered ID | Full original Java class hierarchy | Nearest FIVE method owner classes in order | Original legal state domain | Real ITEM.placed_block exact class |
| --- | --- | --- | --- | --- |
| `minecraft:barrier` | `BarrierBlock>Block>BlockBehaviour` | `BarrierBlock / BlockBehaviour / BarrierBlock / BlockBehaviour / Block` | `waterlogged:false,true` | minecraft:barrier → net.minecraft.world.item.BlockItem |
| `minecraft:beacon` | `BeaconBlock>BaseEntityBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `NONE` | minecraft:beacon → net.minecraft.world.item.BlockItem |
| `minecraft:conduit` | `ConduitBlock>BaseEntityBlock>Block>BlockBehaviour` | `ConduitBlock / BlockBehaviour / ConduitBlock / BlockBehaviour / Block` | `waterlogged:false,true` | minecraft:conduit → net.minecraft.world.item.BlockItem |
| `minecraft:end_gateway` | `EndGatewayBlock>BaseEntityBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `NONE` | NONE |
| `minecraft:end_portal` | `EndPortalBlock>BaseEntityBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `NONE` | NONE |
| `minecraft:light` | `LightBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / LightBlock / BlockBehaviour / Block` | `level:0..15;waterlogged:false,true` | minecraft:light → net.minecraft.world.item.BlockItem |
| `minecraft:nether_portal` | `NetherPortalBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / NetherPortalBlock / NetherPortalBlock / Block` | `axis:x,z` | NONE |
| `minecraft:structure_block` | `StructureBlock>BaseEntityBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / StructureBlock` | `mode:CORNER,DATA,LOAD,SAVE` | minecraft:structure_block → net.minecraft.world.item.GameMasterBlockItem |
| `minecraft:structure_void` | `StructureVoidBlock>Block>BlockBehaviour` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `NONE` | minecraft:structure_void → net.minecraft.world.item.BlockItem |

Five full original method signature names/parameter descriptor checks selected from original compiled `effective_method_owners` (method nearest FQN is the table column; original per-ID signature was independently checked with the complete arguments, not inferred by class names):
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`

Six registered placed-block creators: `BarrierBlock`, `BeaconBlock`, `ConduitBlock`, `LightBlock`, `StructureVoidBlock` → five same-ID plain `BlockItem`; `StructureBlock` → one same-ID **GameMasterBlockItem**. NetherPortalBlock/EndPortalBlock/EndGatewayBlock → **NO original ITEM.placed_block creator**; created/maintained by portal frame/worldgen/internal commands rather than a generic item. EndPortalFrameBlock had a separate earlier source review and is not in new cohort.

## Prior frozen 199 source-reviewed class rows FNV32/FNV64 verify BEFORE mutation + new 208

Loaded original 241×16 `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` LIVE GitHub commit `42069019710d091f2412b30726de9f8efcd13538` and rehashed all four previous Stage36 frozen canonical UTF8 streams, verified exact matches before editing. Only nine rows originally `REVIEW_PENDING` source-promoted; existing 199 source rows untouched. The full 241 class ID+count roster never changed; all 241×3 runtime gates still exactly `REVIEW_PENDING`.

| FQN lexical sorted ASCII + trailing LF stream | PRE199 FNV32/FNV64 | POST208 FNV32/FNV64 |
| --- | --- | --- |
| 241 `class|count` | `0xf188a064 / 0x9ba3c115841e18e4` | `0xf188a064 / 0x9ba3c115841e18e4` |
| reviewed `class|sortedExactOriginalRegistryIDs` | `0x677fbca4 / 0xbd774337c58df484` | `0x83dda6d9 / 0x78f7e1dc96626d99` |
| reviewed `class|fiveOriginalNearestDeclaringOwners` | `0x2a3a87a6 / 0x14205e472f7a71e6` | `0xd5c2048f / 0x25d7399b880f60cf` |
| reviewed `class|sortedExactIDs|fiveOwners` | `0x880155d5 / 0x9e71be7a8a795315` | `0x0534aea8 / 0x2e79ef619d74f528` |

**After Stage37 SOURCE-only:** **208/241** registered concrete Java classes and **794/1060** vanilla BLOCK IDs source-reviewed, **33/241 classes /266 IDs** still SOURCE_PENDING. **ALL 241×3=723** original ledger runtime patched NeoForge21.1.215 bytecode/Planet adapters/client/server gameplay gates `REVIEW_PENDING`; [123 future scenarios](PHASE2_STAGE3A_PORTAL_STRUCTURE_123_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md) ALL NOT RUN. Source class owners, pinned mod build versions and comparative NeoForge patches are NOT applied 21.1.215 Mixin/runtime, loaded Create/AE2, or playable survival PASS. No Java/Mixin/Gradle/build/CI/gameplay modifications in this packet.
