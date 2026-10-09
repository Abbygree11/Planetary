# 2.4B: опоры, выживание, соседние события

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Не терять уже реализованные адаптеры Bush/Candle/Cake/Spore; проверить их dispatch и reuse.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Завершить общий query физического supportPos и корректную сторону support-state; не делать глобальный BlockPos подменой.
- [ ] **2.** По одному семейству за пакет: floor/wall/ceiling; кусты/саженцы; непрямые supports и special placement.
- [ ] **3.** Для каждого family audit обновить canSurvive + updateShape/neighborChanged + support removal + water tick preservation.
- [ ] **4.** Проверить нерелевантное событие от соседа, +Y equivalence, seams, synthetic/LevelReader fallbacks.

## Условия завершения этой карточки

Нет ложного разрушения от не-опорного соседа и неверной опоры на боках; green CI и игровая приёмка отдельно.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
