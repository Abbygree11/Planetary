# 2.3C: NeoForge и полный lifecycle

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Stage2.3A и Stage2.3B original source registry/class/effective-owner исследования завершены; actual NeoForge21.1.215 patched ASM и runtime acceptance ещё не проверены. [2.3B-4 exact 1333/87/925 registry audit](../../research/PHASE2_STAGE3B_ITEM_ORIGINAL_87_SOURCE_OWNER_RECONCILIATION_1_21_1.md).

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## 2.3C owner-family пакеты (в каждом делать все четыре внутренние проверки сразу на один набор механизмов)

- [ ] **1.** Для одного ограниченного семейства/набора механизмов сверить patch с настоящим преобразованным байткодом NeoForge 21.1.215, точные метод-дескрипторы и loaded owner. Не отмечать actual ASM PASS до фактической проверки.
- [ ] **2.** Проверить canSurvive, updateShape, neighborChanged, scheduleTick, growth, rotation/mirror и BlockEntity/structure authoring.
- [ ] **3.** Отдельно документировать front/top/axis, изменённые Mojang/NeoForge сигнатуры, альтернативные вызовы, extension hooks.
- [ ] **4.** Добавить строгие ASM/contract tests для завершённых call-site owner families; никакого require=0 в качестве маскировки.

## Условия завершения этой карточки

Таблица verified owner lifecycle и конкретные integration hooks; тесты не утверждают больше проверенного.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: 2.3C-1 полный owner-family пакет, сразу четыре внутренние проверки (patch/bytecode, lifecycle, signature/mod hooks, ASM/contract tests/plan). Не растягивать четыре пункта на четыре сообщения. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
