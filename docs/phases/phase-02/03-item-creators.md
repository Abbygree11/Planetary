# 2.3B: item authors и альтернативная установка

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Проверки item source census 121 файлов и concrete classes 87 — предварительный сбор, не ручной аудит.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Проверить маленькими партиями BlockItem, StandingAndWallBlockItem, WallOrFloor, Hanging, DirectionalPlaceContext, обработку замены target.
- [ ] **2.** Затем item useOn/use и обновление BLOCK_STATE data component, кастомные placement context.
- [ ] **3.** Затем dispenser, falling replacement, feature/structure/runtime command, post-placement state mutation.
- [ ] **4.** В каждый commit дополнять item disposition: API owner, source/physical argument, orientation output и владелец фазы.

## Условия завершения этой карточки

Явно разобраны 87 concrete item owners и альтернативные creation paths или честно перечислены незакрытые; найдено, где BlockItem базовый путь недостаточен.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
