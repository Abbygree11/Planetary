# Stage 3A-20.4 — independent original ZIP versus live 241/106 class source-owner reconciliation

**2026-10-11 · SOURCE RESEARCH 4/4, NO ASM, Planet integration or game acceptance.**

Actually reopened physical immutable GitHub Actions archive `/mnt/data/phase2-neo1211-registry-census.zip`, artifact **11643813158**, run **37988064055**, SHA-256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e MATCH**. Python ZipFile+csv.DictReader checked **1060 original BLOCK registry IDs/241 distinct concrete registered Java classes, 1333 ITEM and 1712 BlockState property rows**, frozen explicit **103 prior-reviewed Java names plus just CampfireBlock, FireBlock, SoulFireBlock** (not selected via LIVE amended ledger), exact per-class ID membership and the five nearest original declaring owners with FULL qualified argument types, robust to semicolon separated overload alternatives. All 4 original IDs checked, including two Campfire instances. Script locally `/mnt/data/_audit_phase2_20_original.py` (not shipped to repo).

The required five signatures are `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`, `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`, `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`, `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`, `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

| New original concrete class | Exact original BLOCK IDs | Nearest declaring owners in that five-signature order |
| --- | --- | --- |
| CampfireBlock | `minecraft:campfire`, `minecraft:soul_campfire` | CampfireBlock / BlockBehaviour / CampfireBlock / BlockBehaviour / Block |
| FireBlock | `minecraft:fire` | FireBlock / FireBlock / FireBlock / BlockBehaviour / Block |
| SoulFireBlock | `minecraft:soul_fire` | BaseFireBlock / SoulFireBlock / SoulFireBlock / BlockBehaviour / Block |

Separate LIVE GitHub JS parser actually fetched old 241×16 TSV `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` blob **b4d34472e29d82a3e0f7f7364098521160b1509e**, selected 106 reviewed after three **in-memory** row promotions, recomputed exact original ID and 5 owner cells. It independently computed FNV-1a32 and FNV-1a64 on raw Java class lexicographic ordered ASCII records, sorted comma-joined exact BLOCK IDs, fixed owner tuple, each with terminal newline. All **FOUR** original Python vs LIVE JS FNV32+64 pairs **MATCH**:

| Canonical record stream | Original Python FNV32 == LIVE JS FNV32 | Original Python FNV64 == LIVE JS FNV64 | Original Python SHA256 only |
| --- | --- | --- | --- |
| all 241 `class\|count` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` |
| reviewed 106 `class\|sortedIDs` | `0xe5866eed` | `0xd103b145d556f54d` | `87ccb9b3022c1aa8a939bf74ff35bd07e0db6fa5affea5b601f825288bf83a04` |
| reviewed 106 `class\|fiveOwners` | `0x1c9a3111` | `0x1018a8d4ebf3f611` | `9d39d931b40ab38875072263f5c5eb19d83fbfbb97e7c1e63036ace510c3473e` |
| reviewed 106 `class\|sortedIDs\|fiveOwners` | `0x724496bc` | `0x42da26638ba3f61c` | `eacbe4cc68420feb61e54287211442f32c05f3e3dc582cd754d160a9bad3e72e` |

FNV fingerprints are noncryptographic. Canonical SHA256 per record stream calculated by Python only; no false claim of independent SHA256 comparison with JS. Original ZIP SHA256 verifies archived data, **not** transformed NeoForge ASM or loaded runtime.

Original 1333 ITEM join yields ONLY two direct `placed_block` ITEM rows `minecraft:campfire` and `minecraft:soul_campfire` (ordinary BlockItem method owners), ZERO normal `fire` or `soul_fire` ITEM. `FlintAndSteelItem`, `FireChargeItem`, `DispenseItemBehavior`, `BaseFireBlock.getState/PortalShape`, `FireBlock.tick`, `CampfireBlockEntity`, commands/structures/mod direct writers are separate pathways. Legal original campfire `facing=4 horizontal;lit/signal_fire/waterlogged`, FIRE `age0..15,north/south/east/west/up` no DOWN, SOUL_FIRE no properties. BaseFireBlock is abstract, while Campfire extends BaseEntityBlock. See [deep source/ITEM/mod owner report](PHASE2_STAGE3A_FIRE_CAMPFIRE_DEEP_ORIGINAL_ITEM_SPREAD_COOKING_MOD_1_21_1.md) and [50 future tests](PHASE2_STAGE3A_FIRE_CAMPFIRE_SIX_FACE_SPREAD_COOKING_MOD_ACCEPTANCE_1_21_1.md).

| Original census ledger status | Concrete vanilla Java classes | Exact BLOCK IDs |
| --- | ---: | ---: |
| SOURCE_REVIEWED_INTEGRATION_PENDING | **106** | **289** |
| REVIEW_PENDING | **135** | **771** |
| TOTAL | **241** | **1060** |

ALL original **241×3=723** class-specific patched ASM, Planet adapter, gameplay fields remain `REVIEW_PENDING`, all **50 F20 acceptance test designs UNRUN**, no Java, Mixin, CI, unit tests, dedicated server/client/gameplay or mod runtime executed. BareBonesBrews actual 1.21.1 source heat consumer at pinned SHA **4ea706f86df360a377a971f59a2cad035b1e8bd4** requires NeoForge >=21.1.251, not compatible by declared metadata with current 21.1.215; no mod PASS claimed.

NEXT one user «кк» = whole [Stage3A-21 flower pots / potted plant holders](../phases/phase-02/02s-flower-pot-potted-plant-owners.md), **one** original source-pending FlowerPotBlock class / **35** exact BLOCK IDs, four internal research steps.
