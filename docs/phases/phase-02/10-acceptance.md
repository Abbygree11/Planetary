# 2.5: выпускная приёмка механизмов фазы 2

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Реестр владельцев и реализация должны быть достаточно завершены. CI + игровые тесты — разные статусы.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Расширять debug-fixtures по механизмам; отдельно direct setBlock reference и natural BlockItem useOn.
- [ ] **2.** Группировать +Y/-Y/+X/-X/+Z/-Z, seam/corner, player/dispenser/structure/replace, creative/survival, support removal.
- [ ] **3.** Проверять client/server, save/load, unrelated neighbor update и обычный non-Planet мир; сохранились ли ранее принятые частицы.
- [ ] **4.** Сформировать PHASE2_FINAL_COVERAGE_MATRIX_1_21_1.md с ID всех owners и ссылками на реальные evidence/acceptance. Открытые Phase3/5/7A/9 обязательно BLOCKED.

## Условия завершения этой карточки

Только подтверждённые механизмы получают GAMEPLAY PASS; Phase2 закрывается по матрице, не по ощущениям.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
