# 2.3A-2: реестр остальных block effective owners

**Статус:** NEXT / SOURCE AUDIT ACTIVE (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Предыдущая карточка 01 должна быть хотя бы начата, включать evidence-backed формат TSV.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Обрабатывать оставшиеся concrete block implementation classes порциями 8–15 алгоритмически связанных owners.
- [ ] **2.** Для каждого доказать применимость P01–P40, inherited vs override, собственный target/code path, альтернативных авторов и состояние Planet adapter.
- [ ] **3.** Отдельно обрабатывать растения без FACING, жидкости, нестандартные графы и блоки без directional BlockState — имя свойства не критерий.
- [ ] **4.** Сверить итоговое число строк с реальным registry census; НЕ повышать статус незнакомой строки до REVIEWED.

## Условия завершения этой карточки

Все классы имеют доказуемую классификацию или явный REVIEW_PENDING; ни одного скрытого пропуска; нет выдуманного полного покрытия.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **2.3A-1 завершён** (2026-10-10): 24/241 concrete classes, 52/1060 block IDs source-reviewed, все NeoForge ASM/gameplay проверки ожидаются. См. [первая карта](01-block-owners.md), [Bush inherited audit](../../research/PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md) и реестр.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: подпакет **2.3A-2.1**, одна самостоятельная группа 8–15 **ещё pending** concrete classes общего механизма (предпочтительно графы прикрепления/опоры). Сначала подтвердить реальные зарегистрированные типы и методовых owners, затем пройти creation/survive/update/tick/interaction с исходниками и NeoForge-specific uncertainty. После исследования обновить одни только evidence-backed строки TSV, отметить чекпоинт, закоммитить, завершить ответ.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
