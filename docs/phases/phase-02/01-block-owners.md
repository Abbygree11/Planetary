# 2.3A-1: реестр block effective owners — начало

**Статус:** NEXT / IN PROGRESS (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Stage 3A ACTIVE: 22 owner clusters описаны, остальные REVIEW_PENDING. Не считать 241/241 принятыми.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Сверить фактические источники PHASE2_EFFECTIVE_OWNER_FINDINGS и PHASE2_STAGE3A_OWNER_FINDINGS с NeoForge registry-derived типами.
- [ ] **2.** Выбрать 8–15 ещё не разобранных effective-owner классов **одного семейства** и проверить каждый call-path: placement, canSurvive, updateShape, tick, interaction (применимость), overrides.
- [ ] **3.** Сохранить в PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv первые evidence-backed строки и добавить ссылку на source/owner; неизвестное оставить REVIEW_PENDING.
- [ ] **4.** Повторить такой маленький набор в СЛЕДУЮЩЕЙ итерации, не в одном гигантском ответе.

## Условия завершения этой карточки

8–15 подтверждённых owner dispositions с проверяемыми ссылками/примечаниями; честный счёт выполнено/осталось; обновлённый checkpoint.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
