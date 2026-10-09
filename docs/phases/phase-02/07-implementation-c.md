# 2.4C: рост, мультблоки, локальные графы

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Требует kernel Phase1 и families из 2A/2B, учитывает Phase 5 для water plants.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Разобрать рост по ванильным альтернативным growth owners: sapling, crops, vines, growing plant, multiface attachment.
- [ ] **2.** Отдельными итерациями door/bed/chest pairing, сборка/разборка, UPDATE и сериализация.
- [ ] **3.** Отдельно rails/face attachments/fences/walls local tangent graph и seam connection.
- [ ] **4.** Для каждого подвида маленький deterministic test + CI; заранее пометить перекрестные Phase 7A и Phase 9 контракты.

## Условия завершения этой карточки

Все текущие владельцы графа и пар имеют dispositions, не сделан ложный вывод по одному примеру.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
