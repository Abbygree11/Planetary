# Phase 2.3A — результаты оригинального аудита Block owner classes

**Область проверки: source/registered original; НЕ 241 независимых gameplay PASS.**

- Исследован полный набор **241/241 конкретных классов зарегистрированных ванильных блоков**, **1060/1060 Block ID**, с учётом наследуемых владельцев методов и семейств, а не только названий блоков. Итоговый checkpoint **[2.3A-39](../../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md)** и [Phase 2 plan](../../phase-02.md). Работа зафиксирована в карточках 2.3A-1…39 и original census; исходный конец пакета в GitHub commit [fe970c5](https://github.com/Abbygree11/Planetary/commit/fe970c5b599b28d9b79fb065abb880d969adff7e).
- Базовые owner families: placement, FACING/AXIS, supports, neighbor update, redstone, rails, sculk, temporal growth/oxidation, crops, waterlogged blocks, doors/beds/multiblocks, hanging/worldgenerated vegetation, rotated patterns, natural structures and non-item authors. Это карта **общих алгоритмов**: все дочерние варианты дерева/материала/железа/модовые аналоги не должны чиниться исключениями по Block ID.
- [P01–P40 Mechanism Atlas](../../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [source waves A/B/C](../../../research/PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md), [full block-owner disposition](../../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) и [долгий resumable checkpoint](../../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md) — первоисточники.

**Не закрыто:** все оригинальные method body branches 241 классов, реальные overrides модовых блоков, внедрённые Mixins, six-face/12 edge/8 corner game behavior, фото/визуал приёмка и Phase2 runtime. Исследовательский roster DONE, всё остальное не выдумывать.
