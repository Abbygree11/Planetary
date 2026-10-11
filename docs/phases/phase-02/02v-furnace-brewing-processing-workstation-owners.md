# Stage3A-24 — furnace/blast furnace/smoker/brewing stand original processing-machine owners

**2026-10-11 · SOURCE REVIEW COMPLETE 24.1–24.4 (4/4)** on Planetary branch2.0 MC1.21.1 NeoForge21.1.215 Java21. This does NOT close ASM/Mixin/client/server/gameplay acceptance. No Java/Mixin or build changes.

- [x] **24.1** Original physical archive ZIP SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` MATCH. 4 original registered BLOCK IDs, 4 exact original `BlockItem` ITEM joins, original hierarchy, legal properties and FIVE FULL-qualified method declaring owners.
- [x] **24.2** [Deep original Java/BE/fuel/recipes/capability/mod ownership](../../research/PHASE2_STAGE3A_FURNACE_BREWING_ORIGINAL_BE_ITEM_MOD_1_21_1.md). Vanilla furnace/blast/smoker 4 horizontal FACING+LIT; `AbstractFurnaceBlockEntity.serverTick` distinct smelting/blasting/smoking; physical UP input0/DOWN output2,fuel1/SIDE fuel1. BrewingStand has **NO FACING**, only three HAS_BOTTLE booleans, independent five-slot `BrewingStandBlockEntity` PotionBrewing, fuel20/brew400, physical sided UP3/DOWN0-3/SIDE0-2,4. Real MC1.21.1 Iron Furnaces 4.3.2 pinned source NeoForge21.1.209 min[21.1,) uses independent custom `Block` and `TileEntityInventory`, provider `IItemHandler` `SidedInvWrapper`; actual jar not loaded on target21.1.215.
- [x] **24.3** [56 FUTURE NOT RUN tests](../../research/PHASE2_STAGE3A_FURNACE_BREWING_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md) for vanilla, six Planet faces, 12 edges, 8 corners, recipes/fuel/BE/worldgen/modded sides/client smoke/render/performance; SHARED_SAFE/OPT_IN_ALGORITHM/MOD_INDEPENDENT/PHYSICAL_DO_NOT_TOUCH.
- [x] **24.4** [Independent original ZIP Python frozen118+four vs LIVE GitHub 241×16 owner/ID 4 FNV32+FNV64 hashes MATCH](../../research/PHASE2_STAGE3A_FURNACE_BREWING_ORIGINAL_122_CLASS_RECONCILIATION_1_21_1.md). **122/241** original classes/**355/1060** registered BLOCK IDs source reviewed, pending119 classes/705 IDs; ALL 723 ASM/Planet/gameplay flags REVIEW_PENDING.

No blanket rotation of original physical Level/BlockPos/Direction/WorldlyContainer/IItemHandler capability side. Typed bilateral provider+caller opt-in only, unknown mod physical default.

**NEXT «кк» = entire Stage3A-25** [workstation/media/noteblock seven original source classes](02w-workstation-media-interactive-owner-families.md), all four internal research stages in one packet.
