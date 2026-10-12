# 2.3B-4: exact original ITEM registry and effective method owners (SOURCE, 4/4)

**2026-10-12, branch 2.0, MC1.21.1 / NeoForge21.1.215. RESEARCH ONLY.** Real unchanged workflow [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158** `phase2-neo1211-registry-census` re-downloaded and SHA256 **7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e** verified. Original 1333 distinct ITEM IDs, **87 registered concrete classes**, **925 BlockItem descendants** and **408 non-BlockItems**. All 925 primary placed_block references resolve to one of the original **1060 BLOCK IDs**; zero non-BlockItems have placed_block. 1712 exact property records. Compiled original ITEM registry includes each exact effective method FQN signature, class_hierarchy and all declaring owners; **87/87 class groups have internally consistent method owners**. No arbitrary name-driven registry inference.

## 1/4 Actual row joins and deterministic deliverables

Three deterministic TSVs generated and validated by Python zipfile/csv from the original ZIP, supplied alongside this research:
- **1333 registered IDs**: `phase2-item-1333-exact-owner-reconciliation.tsv`, SHA256 `bdd32d354a16a2518633d7261924abedfcedb7be61c2fac0c0fb648610f6fe3e`.
- **87 registered concrete class groups**: `phase2-item-87-concrete-class-owner-reconciliation.tsv`, SHA256 `53174472dd147fea1a59a468429f4b3a93c84812ebb5c9e69ed77512c9dc96c5`.
- **14 registered item IDs whose primary placed_block differs**: `phase2-item-14-nonidentical-block-linkage.tsv`, SHA256 `3bab016feb54fe765f1cfd7636a45272a26a344fded664bcaa9edce547521d`.

Each 1333-row record includes real item ID, Java concrete FQN, hierarchy, block_item flag, primary target block ID, target Block class/properties, original *full signature* effective owners of `use/useOn/place/updatePlacementContext/getPlacementState/placeBlock/canPlace/registerBlocks`, original all declaring owners and explicit ASM/gameplay pending status. Reproducibility script is `scripts/research/phase2_item_registry_reconcile.py`. Original artifact ZIP itself remains immutable on GitHub Actions; derived TSV file attachments are not asserted to be committed inside the Git tree.

## 2/4 Exact effective owner method counts (original compiled evidence)

| Owner path across actual ITEM ID rows | Count |
| --- | ---: |
| `BlockItem.place` for BlockItem descendants | 925 |
| `BlockItem.useOn` | 922; other 2 `PlaceOnWaterBlockItem`, 1 `SolidBucketItem` |
| `BlockItem.updatePlacementContext` | 924; 1 `ScaffoldingBlockItem` |
| `BlockItem.getPlacementState` | 862; `StandingAndWallBlockItem` 58, `GameMasterBlockItem` 5 |
| `BlockItem.placeBlock` | 882; `DoubleHighBlockItem` 27, `BedItem` 16 |
| `BlockItem.canPlace` | 925 |
| `BlockItem.registerBlocks` | 865; `StandingAndWallBlockItem` 58, anonymous `Items$1` 2 |
| Non-BlockItem `Item.useOn` inherited | 284; non-inherited 124 |
| Non-BlockItem `use` non-inherited from `Item` | 166 |

58 standing/wall variants = 19 direct +16 Banner +11 Sign +11 HangingSign +1 PlayerHead. `PlaceOnWaterBlockItem` has **lily_pad and frogspawn** (not only lily pad); `SolidBucketItem` is powder_snow_bucket and has a second direct `DispensibleContainerItem.emptyContents` author. `HangingSignItem.canPlace(LevelReader,BlockState,BlockPos)` is a **different overload** from `BlockItem.canPlace(BlockPlaceContext,BlockState)`; do not mix by method name.

Anonymous actual original compiled `Items$1` is **big_dripleaf and cauldron**; effective `registerBlocks=(Map,Item)` anonymous method short owner is empty in raw register census. Later comparative official [NeoForge 1.21.1 Items.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/item/Items.java.patch) changes vanilla BlockItem into anonymous subclass with overridden `registerBlocks` and `removeFromBlockToItemMap` to map secondary **big_dripleaf_stem** and water/lava/powder_snow cauldrons to original item. These are **extra block-to-item mappings, not four more ITEM IDs and not extra BlockItem.place output candidates**. Exact applied 21.1.215 bytecode still 2.3C PENDING.

## 3/4 Source declarations and semantic bypass disposition

Fetched & inspected all **86 named** corresponding comparative 1.21.1 Java class files at pinned [b77c5c6995](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item); anonymous class confirmed original census + comparative official NeoForge patch. Checked `use/useOn/place/updatePlacementContext/getPlacementState/placeBlock/registerBlocks/onUseTick/finishUsingItem/interactLivingEntity` declarations, plus B-1/B-2 selected method bodies and B-3 alternative creator owners. This proves 87/87 **effective declaration-owner source classification**, **NOT** every unrelated method or actual transformed ASM.

Non-BlockItem world writers include bone meal, tools, honeycomb, fire, debug stick and Ender Eye; `SpawnEggItem` **80 registered IDs** can mutate *existing* Spawner BE `setEntityId` or spawn entity, **not 80 block-placement classes**. `MinecartItem` 6 and `BoatItem` 18 spawn entities. `CompassItem.useOn` writes ItemStack LODESTONE_TRACKER not Block; `MapItem.useOn` updates banner marker saved map data; `LeadItem.useOn` leashes entities. `BrushItem.onUseTick` is delayed BE writer. Vanilla inherited `Item.useOn` alone is no guarantee no block change through Block.useItemOn/other callbacks. Real mod subclass overrides and foreign Levels from B-1/B-2/B-3 cannot be declared supported based only on vanilla source.

## 4/4 Status and next

Source item roster **87/87**, IDs **1333/1333**, BlockItem links **925/925** plus original method owner signatures verified; 2.3B-1/2/3 alternative owner families documented. All exact NeoForge21.1.215 ASM, Planet integration, mod-JAR/runtime, six faces/12 seams/8 corners, CI and gameplay **REVIEW_PENDING / NOT RUN**. No Java/Mixin/Gradle modifications. Deepest safe boundary is typed author provenance + physical clicked/target BlockPos + target canonical frame, preserving explicit properties, BE/components, NeoForge mod extension hooks and foreign/worldgen coordinates. Never globally remap Level.setBlock/BlockPos/Direction. Next **2.3C exact patched target bytecode**, then Phase2.4 implementations and tests. [Future cases ALL NOT RUN](PHASE2_STAGE3B_ITEM_REGISTRY_ORIGINAL_87_SOURCE_ACCEPTANCE_1_21_1.md).
