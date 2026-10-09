# 2.4E: обязательные перекрёстные контракты

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Нельзя закрывать Phase 2, если её результат корректен лишь как BlockState, а фактический объект в игре выглядит/ведёт себя неверно.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Phase 3: статические формы, BakedModel, BER Chest/EnderChest и Enchanting Book — раздельный владелец, но единый frame contract.
- [ ] **2.** Phase 5: waterlogging/fluid placement/state shape, scheduler, buckets.
- [ ] **3.** Phase 7A: redstone, comparator, neighbor signal, piston, capabilities.
- [ ] **4.** Phase 9: portal local plane и structure rotation/origin, synthetic block placement.
- [ ] **5.** Фиксировать на каждом стыке owner, contract tests и блокирующий статус без правки в чужой фазе.

## Условия завершения этой карточки

Есть небольшая cross-phase matrix с ID owner, потребителем и тестами; недоделки видимы, не маскируются.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
