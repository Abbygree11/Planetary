# 2.3A-2: реестр остальных block effective owners

**Статус:** NEXT / SOURCE AUDIT ACTIVE (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Предыдущая карточка 01 должна быть хотя бы начата, включать evidence-backed формат TSV.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [x] **1.** Обрабатывать оставшиеся concrete block implementation classes порциями 8–15 алгоритмически связанных owners. **Первая порция DONE (2026-10-10):** 11 concrete attachment classes, 29 registered IDs; [source audit](../../research/PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md) + [241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv). Суммарно **35/241 source-reviewed**, **206 pending**. Ещё НЕ NeoForge bytecode/gameplay PASS; следующая порция должна оставаться отдельной микрозадачей.
- [ ] **2.** Для каждого доказать применимость P01–P40, inherited vs override, собственный target/code path, альтернативных авторов и состояние Planet adapter.
- [ ] **3.** Отдельно обрабатывать растения без FACING, жидкости, нестандартные графы и блоки без directional BlockState — имя свойства не критерий.
- [ ] **4.** Сверить итоговое число строк с реальным registry census; НЕ повышать статус незнакомой строки до REVIEWED.

## Условия завершения этой карточки

Все классы имеют доказуемую классификацию или явный REVIEW_PENDING; ни одного скрытого пропуска; нет выдуманного полного покрытия.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **2.3A-2.1 — 11 face-attachment classes / 29 block IDs, source-level owners reviewed**, current 35/241 classes (81/1060 IDs) source-reviewed; **206/241 (979 IDs) REVIEW_PENDING**. Для новой группы сравнительные method declaring owners проверены исходниками, но NeoForge reflection owner join ещё PENDING. Исследование: [FACE_ATTACHMENT_AUDIT](../../research/PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md).
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: **2.3A-2.2 (чекбокс 2)**, в новом отдельном ответе дополнить эти 11 class-level исходных методов точной NeoForge 21.1.215 owner/ASM сверкой и item/interaction bypass, обновить их evidence level; затем продолжить прочие классы порциями 8–15. Не повышать runtime или gameplay acceptance до подтверждения.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
