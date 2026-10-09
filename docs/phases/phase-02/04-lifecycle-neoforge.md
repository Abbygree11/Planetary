# 2.3C: NeoForge и полный lifecycle

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Зависимость от подтверждённых block/item owner-кластеров и уже выполненного сравнительного source research.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Сверить patch и реальный байткод NeoForge 21.1.215 для по 1–2 owner-family методов за итерацию.
- [ ] **2.** Проверить canSurvive, updateShape, neighborChanged, scheduleTick, growth, rotation/mirror и BlockEntity/structure authoring.
- [ ] **3.** Отдельно документировать front/top/axis, изменённые Mojang/NeoForge сигнатуры, альтернативные вызовы, extension hooks.
- [ ] **4.** Добавить строгие ASM/contract tests для завершённых call-site owner families; никакого require=0 в качестве маскировки.

## Условия завершения этой карточки

Таблица verified owner lifecycle и конкретные integration hooks; тесты не утверждают больше проверенного.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
