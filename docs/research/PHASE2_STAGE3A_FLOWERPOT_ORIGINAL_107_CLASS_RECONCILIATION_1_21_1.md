# Stage3A-21.4 — original immutable ZIP versus independent LIVE GitHub 241/107 FlowerPot full-owner reconciliation

**2026-10-11 SOURCE ONLY 4/4.** Original physical GitHub Actions ZIP artifact **11643813158** (run **37988064055**) reopened by Python `zipfile/csv` at `/mnt/data/phase2-neo1211-registry-census.zip`, original SHA256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**. Original immutable census: **241 concrete registered class groups,1060 BLOCK IDs,1333 ITEM IDs,1712 property rows**. Python audit script `/mnt/data/_audit_phase2_21.py` uses *frozen explicit prior 106 Java class names* plus exactly FlowerPotBlock, **not** a prospective amended LIVE TSV status list. It asserts all original 107 class/ID/5 method-owner consistency and exact ID sets, overload signatures, counts, state domains and ITEM join. This is not a NeoForge transformed ASM or gameplay verification.

Five fully qualified signatures, in order: `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`, `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`, `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`, `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`, `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`. For **all 35** FlowerPotBlock original BLOCK IDs tuple **Block / BlockBehaviour / FlowerPotBlock / BlockBehaviour / Block**. 35 IDs = **minecraft:flower_pot and 34 minecraft:potted_***, exact names in [source report](PHASE2_STAGE3A_FLOWERPOT_ORIGINAL_ITEM_REGISTRATION_MOD_MECHANISMS_1_21_1.md) and original 16-column TSV. Every original variant has **ZERO BlockState properties**. Exactly **ONE original ITEM row `minecraft:flower_pot`** with original BlockItem; 34 potted contents are directly authored as different block states, no ITEM. Distinguish separately registered DecoratedPotBlock/BE.

Independent JavaScript fetched actual old LIVE GitHub branch2.0 original 241-row×16-header TSV blob SHA **5d130706885e379b1d87ffeb1913ea67bd93db2b**, promoted ONLY original verified FlowerPot row **in memory**, and independently computed FNV-1a32/64 over four canonical ASCII newline-terminated lexically Java-name-sorted streams, exact IDs separately sorted and FULL five owners. All **FOUR** original Python ZIP versus LIVE GitHub JS fingerprints **MATCH**.

| Canonical stream | Original ZIP Python FNV32 = LIVE JS FNV32 | Original ZIP Python FNV64 = LIVE JS FNV64 | Python original SHA256 only |
| --- | --- | --- | --- |
| all 241 `class\|count` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| reviewed 107 `class\|IDs` | `0xf5f5926e` | `0xeab88b2a8c8894ae` | `327efae94fe1401752a2151eb08b0afccbe829054d810ff7c08725266fa4b19b` |
| reviewed 107 `class\|fiveOwners` | `0x371d28bc` | `0x23e002bb2b7492dc` | `4e94798f55fb73fe4ddb934d5c63b9c0a82e77633c3b7288a57cd192c64875c2` |
| reviewed 107 `class\|IDs\|fiveOwners` | `0x5a8c0b6c` | `0x91e4605e798c3dec` | `38bd9bab3d3ca2e9e8a1581ca1c8753d97ed1a23272652222c155badae01cac9` |

FNVs are noncryptographic diagnostic cross-checks; Python canonical-stream SHA256 not independently compared in JS, and none of this verifies the exact target **NeoForge21.1.215 patched ASM**, Planet Mixin/adapter, loaded client/server or mod gameplay. Comparable NeoForge1.21.1 **21.1.248 Javadoc** supplies `FlowerPotBlock.getEmptyPot/addPlant/Supplier` extension signature clues **but not** proof of exact target patch. [Actual BotanyPots MC1.21.1 pinned source](PHASE2_STAGE3A_FLOWERPOT_ORIGINAL_ITEM_REGISTRATION_MOD_MECHANISMS_1_21_1.md) extends its own BaseEntityBlock, version baseline21.1.209; actual binary/dependencies untested.

| Original class status | Concrete original registered classes | Exact original BLOCK IDs |
| --- | ---: | ---: |
| SOURCE_REVIEWED_INTEGRATION_PENDING | **107** | **324** |
| REVIEW_PENDING | **134** | **736** |
| TOTAL | **241** | **1060** |

All **241×3=723** original NeoForge patched-bytecode/Planet adapter/gameplay columns still **REVIEW_PENDING**. [50 FP21 future cases ALL NOT RUN](PHASE2_STAGE3A_FLOWERPOT_SIX_FACE_INTERACTION_MOD_ACCEPTANCE_1_21_1.md). No Java/Mixin/build/CI/game/client/server/mod-load work claimed.

Next one «кк» → all four parts of [Stage3A-22 storage/BE container family](../phases/phase-02/02t-storage-container-block-entity-owners.md), five original pending classes and five BLOCK IDs.
