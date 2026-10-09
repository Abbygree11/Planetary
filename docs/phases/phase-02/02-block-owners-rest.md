# 2.3A-2: реестр остальных block effective owners

**Статус:** DONE for the four bounded 2.3A-2 research tasks; **Stage 3A remains ACTIVE with 194 classes pending**, no gameplay PASS. [Индекс фазы](../phase-02.md).

**Контекст:** Предыдущая карточка 01 должна быть хотя бы начата, включать evidence-backed формат TSV.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [x] **1.** Обрабатывать оставшиеся concrete block implementation classes порциями 8–15 алгоритмически связанных owners. **Первая порция DONE (2026-10-10):** 11 concrete attachment classes, 29 registered IDs; [source audit](../../research/PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md) + [241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv). Суммарно **35/241 source-reviewed**, **206 pending**. Ещё НЕ NeoForge bytecode/gameplay PASS; следующая порция должна оставаться отдельной микрозадачей.
- [x] **2.** Для каждого доказать применимость P01–P40, inherited vs override, собственный target/code path, альтернативных авторов и состояние Planet adapter. **2.3A-2.2 DONE (2026-10-10):** для 11 ранее выбранных classes сверены точные NeoForge reflection declaring owners, 29 block IDs, 26 registered BlockItems, 3 item-side StandingAndWall variants, 8 существующих Mixins и два source integration gap. [Audit addendum](../../research/PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md#2026-10-10-addendum--ci-artifact-exact-owner-and-item-reconciliation). **ASM INVOKE, Mixin runtime, gameplay — PENDING.**
- [x] **3.** Отдельно обрабатывать растения без FACING, жидкости, нестандартные графы и блоки без directional BlockState — имя свойства не критерий. **2.3A-2.3 DONE (2026-10-10):** 12 registered concrete classes (4 GrowingPlant head/body pairs, sugar cane, cactus, bamboo stalk and sapling), exactly 12 actual registry IDs; [research](../../research/PHASE2_STAGE3A_NONFACING_GROWTH_GRAPHS_1_21_1.md) and [updated TSV](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv). **47/241** source + runtime reflection owner reviewed, **194/241** pending, no ASM/gameplay PASS.
- [x] **4.** Сверить итоговое число строк с реальным registry census; НЕ повышать статус незнакомой строки до REVIEWED. **2.3A-2.4 DONE (2026-10-10):** [independent original ZIP reconciliation](../../research/PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md); **241/241 exact class+registered-count**, **47/47 exact reviewed ID lists + 5 exact method-declaring-owner signatures**. All 194 unknown classes remain `REVIEW_PENDING`, no NeoForge ASM/Mixin gameplay PASS.

## Условия завершения этой карточки

Все классы имеют доказуемую классификацию или явный REVIEW_PENDING; ни одного скрытого пропуска; нет выдуманного полного покрытия.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **2.3A-2.4 — exact original CI ZIP vs committed 241-class ledger**, 2026-10-10: 241/241 matching class/count, 47/47 matching reviewed exact ID sets and five method-declaration owners; 194 pending untouched, all 241 bytecode/runtime/gameplay acceptance fields PENDING. [Reconciliation](../../research/PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md).
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: [**2.3A-3, microtask 1**](02a-block-graph-owners.md), в следующем отдельном ответе взять 8–10 из 10 реально зарегистрированных ещё pending connectivity owners (71 IDs total candidate cohort), разобраться с P25/P26 method/graph call paths; не завершать 194 классов автоматически.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
