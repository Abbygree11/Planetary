# Stage3A-24 — furnace/smoker/blast-furnace/brewing-stand shared processing and BlockEntity recipe owner families

**ACTIVE 0/4 ORIGINAL SOURCE research, branch2.0 Minecraft1.21.1 NeoForge21.1.215 Java21.** One user «кк» covers 24.1–24.4 WHOLE together; deep-source docs/acceptance+original ZIP vs live GitHub ledger and roadmap commits. Research only, no Java/Mixin/gameplay unless explicitly separately authorized.

Original physical immutable 241-class/1060 registered BLOCK census + LIVE ledger confirm exactly four source-pending concrete Java block classes, one BLOCK ID each:
- `FurnaceBlock` → `minecraft:furnace`; fuel/smelt/tick/LIT/BE sided inventory.
- `BlastFurnaceBlock` → `minecraft:blast_furnace`; shared AbstractFurnaceBlock superclass, different recipe + fuel/time and facing.
- `SmokerBlock` → `minecraft:smoker`; shared AbstractFurnaceBlock, different recipe and gameplay comparator.
- `BrewingStandBlock` → `minecraft:brewing_stand`; not a furnace subclass; bottle slots/fuel/brewing recipe and rendered stand support, possibly different original facing/property domains.

**Family grouping is shared recipe/fuel/status/BE-machine handoff, NOT a claim all 4 share one common method owner or redstone logic.**

- [ ] **24.1** physically rehash immutable ZIP, original four registered IDs/ITEM, legal state properties and class hierarchy/five exact fully qualified original declaring method owner signatures, BE class inventory, sibling overrides.
- [ ] **24.2** full recipe/BE/fuel/time/heat/lit/output/WorldlyContainer and NeoForge sided IItemHandler mod owner DAG (including independent custom furnace and fuel providers), actual pinned Minecraft1.21.1 mod source/version. PHYSICAL vs local semantic support and front orientation, worldgen/commands/fake Level; preserve original real inventory IO/recipe data.
- [ ] **24.3** >=40 future NOT RUN no-Planet, six face interiors, 12 edges, 8 corners, BE tick/container, fuel, item recipe, automation sided ports, particles/BE/client, modded capability and fake world fixtures; deep SHARED_SAFE/OPT_IN_ALGORITHM/MOD_INDEPENDENT/PHYSICAL_DO_NOT_TOUCH.
- [ ] **24.4** independent Python original physical ZIP frozen prior 118 + 4 new source classes versus actual LIVE GitHub 241×16 full original ID/five owner FNV32+64 cross-check. Expected **122/241 classes,355/1060 BLOCK IDs IF VERIFIED**, pending119 classes/705 IDs and all 723 ASM/Planet/gameplay flags REVIEW_PENDING; update roadmap/context+next card. Docs only.

[Stage23 deep original mod/BE source](../../research/PHASE2_STAGE3A_AUTOMATION_LOGISTICS_ORIGINAL_BE_ITEM_MOD_1_21_1.md), [68 UNRUN tests](../../research/PHASE2_STAGE3A_AUTOMATION_LOGISTICS_SIX_FACE_MOD_ACCEPTANCE_1_21_1.md), [118-class physical original ZIP reconciliation](../../research/PHASE2_STAGE3A_AUTOMATION_LOGISTICS_ORIGINAL_118_CLASS_RECONCILIATION_1_21_1.md).
