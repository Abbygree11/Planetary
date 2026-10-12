# 2.3C: NeoForge и полный lifecycle

**Статус:** ACTIVE: 2.3C-1 owner-family research/ASM test COMMITTED; exact loaded-classpath ASM/JUnit CI PASS, applied Mixin/gameplay NOT RUN. Остальные owner families не завершены. [Индекс фазы](../phase-02.md).

**Контекст:** Stage2.3A и Stage2.3B original source registry/class/effective-owner исследования завершены; actual NeoForge21.1.215 patched ASM и runtime acceptance ещё не проверены. [2.3B-4 exact 1333/87/925 registry audit](../../research/PHASE2_STAGE3B_ITEM_ORIGINAL_87_SOURCE_OWNER_RECONCILIATION_1_21_1.md).

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## 2.3C owner-family пакеты (в каждом делать все четыре внутренние проверки сразу на один набор механизмов)

- [ ] **1.** Для одного ограниченного семейства/набора механизмов сверить patch с настоящим преобразованным байткодом NeoForge 21.1.215, точные метод-дескрипторы и loaded owner. Не отмечать actual ASM PASS до фактической проверки.
- [ ] **2.** Проверить canSurvive, updateShape, neighborChanged, scheduleTick, growth, rotation/mirror и BlockEntity/structure authoring.
- [ ] **3.** Отдельно документировать front/top/axis, изменённые Mojang/NeoForge сигнатуры, альтернативные вызовы, extension hooks.
- [ ] **4.** Добавить строгие ASM/contract tests для завершённых call-site owner families; никакого require=0 в качестве маскировки.

## Завершённые ограниченные owner-family пакеты

- [x] **2.3C-1 — BlockItem → StandingAndWallBlockItem (4/4 исследование и ASM-контракт добавлены, не runtime PASS):** [bytecode/lifecycle research](../../research/PHASE2_STAGE3C1_BLOCKITEM_STANDINGWALL_NEOFORGE_ASM_LIFECYCLE_1_21_1.md), [strict classfile test](../../../src/test/java/dev/planetary/world/Phase23C1ItemPlacementAsmContractTest.java), [CI run 38257655791](https://github.com/Abbygree11/Planetary/actions/runs/38257655791). First JUnit run failed in test selector assertion; fixed in 56721e4; [repeat CI 38258262302](https://github.com/Abbygree11/Planetary/actions/runs/38258262302) **SUCCESS** (compiled ASM tests/JUnit/census); transformer/6-face/12-edge/8-corner/modded gameplay NOT RUN. Commit with test `5668ba7`; research `641d4c0`.
- [ ] **2.3C-2 (NEXT):** placement context / synthetic author family: `BlockPlaceContext`, `UseOnContext`, `DirectionalPlaceContext`, `ScaffoldingBlockItem`, `PlaceOnWaterBlockItem`, `SolidBucketItem`. Four inner checks together, bytecode + lifecycle + extension/semantics + ASM test.

The four generic tasks above are **phase-wide**, and remain open until all relevant owner families are covered; one completed first-family packet does not close the phase.

## Условия завершения этой карточки

Таблица verified owner lifecycle и конкретные integration hooks; тесты не утверждают больше проверенного.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **2.3C-1: документация и тест закоммичены, первый CI FAIL из-за ошибочной ожидаемой строки, исправлено в 56721e4; повторный CI 38258262302 PASS. Игровая приёмка NOT RUN; реальный Mixin/gameplay не тестировались.**
- Последний доказанный checkpoint: 2.3C-2 research/strict exact-version contract committed; repeat CI 38260347819 pending at time of writing. All runtime Mixin/client/server/mod/game checks NOT RUN.\n- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: **2.3C-3** полный owner-family пакет, четыре внутренние проверки (patch/bytecode, lifecycle, signature/mod hooks, ASM/contract tests/plan). Не растягивать четыре пункта на четыре сообщения. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
