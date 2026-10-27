# Phase 2.3B — результаты ITEM registry и альтернативных авторов BlockState

**Статус:** bounded source-item audit завершён; реальная игра/моды/непосредственные PostPlacedBy paths НЕ полностью приняты.

- Оригинальный реестр: **1333 Item ID**, **87 concrete Item classes**, **925 BlockItem**. Stage 2.3B-4 документирует 87/87 классов и исходные `use`/`useOn`, `BlockItem.place`, `StandingAndWallBlockItem`, `DirectionalPlaceContext`, `WaterBucket`/специализированные item creators; сохранена связь ITEM↔BLOCK и альтернативы без BlockItem. [Stage3B registry audit](../../../research/PHASE2_STAGE3B_ITEM_ORIGINAL_87_SOURCE_OWNER_RECONCILIATION_1_21_1.md), [full 87 source acceptance](../../../research/PHASE2_STAGE3B_ITEM_REGISTRY_ORIGINAL_87_SOURCE_ACCEPTANCE_1_21_1.md).
- Важный принцип: не всё создание BlockState происходит через игрока и BlockItem: worldgen/structure, dispenser, fluids, replace/move, temporal updates, entity/item use, post-placement BE NBT и сторонние моды могут обходить стандартный path. [Alternate non-item creators](../../../research/PHASE2_STAGE3B_ALTERNATE_NONITEM_CREATORS_SOURCE_NEOFORGE_MOD_1_21_1.md), [writer dispositions TSV](../../../research/PHASE2_STAGE3B_ALTERNATE_NONITEM_AUTHOR_DISPOSITION_1_21_1.tsv).
- Физическая позиция нажатия/соседей остаётся Minecraft XYZ; локальный результат BlockState выбирается через canonical Planet frame, не через глобальное подменённое `getClickedFace`.

**Не закрыто:** реальная проверка отличий мирогенерации и player-placed цепей/знаков/стеблей на всех гранях, модовые event/placement callbacks, ITEM GUI/BER визуал, полноценный 2.4+ game V. Исходники Item classes ≠ полная runtime совместимость. [Полный Phase2 план](../../phase-02.md).
