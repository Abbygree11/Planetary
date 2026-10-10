# Фаза 2 — семантика блоков: ориентация, установка, опоры, обновления

**Статус: PARTIAL / ACTIVE. Нельзя считать законченным на основании отдельных рабочих блоков.** Основной источник: [глобальный роадмап](../IMPLEMENTATION_PLAN.md), Phase 2. Протокол: [README.md](README.md).

## Что уже есть (не повторять исследование)

- Исследовательский атлас [P01–P40](../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md).
- [Resumable checkpoint](../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md) со ссылками на census классов и item-путей, а также исследования waves A/B/C.
- Результат bootstrap/registry census: 1060 зарегистрированных block IDs, 241 concrete block implementation classes, 1333 item IDs, 87 concrete item implementation classes, 925 BlockItem IDs. **Это не 241 вручную доказанных механизма.**
- Уже существуют локальные адаптеры placement/support, но они не покрывают все механизмы, и игровая приёмка фазы 2 НЕ пройдена.
- Пройденные игроком проверки частиц не надо повторять без регрессии.

## Очередь МАЛЕНЬКИХ независимых пакетов

| Пакет | Файл | Статус | Выход |
|---|---|---|---|
| 2.3A-1 | [01-block-owners.md](phase-02/01-block-owners.md) | **DONE (микропакеты, только исследование)** | 24/241 class source-reviewed; 217 pending, без игрового PASS |
| 2.3A-2 | [02-block-owners-rest.md](phase-02/02-block-owners-rest.md) | **DONE (4 research packages only)** | Exact ZIP reconciliation 47/241 reviewed and 194 pending; **not** full Stage 3A |
| 2.3A-3 | [02a-block-graph-owners.md](phase-02/02a-block-graph-owners.md) | **DONE (4 research packages only)** | Exact original ZIP source owner/item/shape graph reconciliation; 55 reviewed, 186 pending |
| 2.3A-4 | [02b-redstone-signal-owners.md](phase-02/02b-redstone-signal-owners.md) | **DONE (4 research packages only)** | Exact original ZIP reconciliation for 63 source+reflection reviewed classes/170 IDs; 178 pending, all gameplay gates pending |
| 2.3A-5 | [02c-rail-owners.md](phase-02/02c-rail-owners.md) | **DONE (4 research packets only)** | All 66 class exact 5-signature owner+174 ID records match original ZIP; 175 other classes pending, no gameplay PASS |
| 2.3A-6 | [02d-environment-pressure-sensor-owners.md](phase-02/02d-environment-pressure-sensor-owners.md) | **DONE (4 research packets only)** | Original 21.1.215 ZIP all 69 exact owner tuples/190 reviewed IDs reconcile, 172 pending, no ASM or gameplay PASS |
| 2.3A-7 | [02e-sculk-vibration-sensor-owners.md](phase-02/02e-sculk-vibration-sensor-owners.md) | **DONE (4 research packets only)** | Original 21.1.215 ZIP all 71 source-reviewed exact class/192 ID/five-owner tuples reconciled; all 241 runtime/ASM/gameplay gates pending |
| 2.3A-8 | [02f-sculk-shrieker-catalyst-owners.md](phase-02/02f-sculk-shrieker-catalyst-owners.md) | **NEXT** | Exactly 2 still source-pending concrete classes/2 IDs: shrieker VibrationSystem/warden and catalyst entity-death/BLOOM/spreader; separate owners |
| 2.3B | [03-item-creators.md](phase-02/03-item-creators.md) | TODO | Item/alternative-author coverage |
| 2.3C | [04-lifecycle-neoforge.md](phase-02/04-lifecycle-neoforge.md) | TODO | Bytecode/patch/dispatch evidence and gate |
| 2.4A | [05-implementation-a.md](phase-02/05-implementation-a.md) | TODO | Context/FACING/AXIS/rotation state families |
| 2.4B | [06-implementation-b.md](phase-02/06-implementation-b.md) | TODO | Survive/support/update families |
| 2.4C | [07-implementation-c.md](phase-02/07-implementation-c.md) | TODO | Multiblock/growth/graphs/rails |
| 2.4D | [08-implementation-d.md](phase-02/08-implementation-d.md) | TODO | Interaction/fire/transform/structure |
| 2.4E | [09-crossphase.md](phase-02/09-crossphase.md) | TODO | Explicit contracts with Phases 3/5/7A/9 |
| 2.5 | [10-acceptance.md](phase-02/10-acceptance.md) | TODO | Owner-based final acceptance matrix |

**Важно:** каждый файл — не один гигантский вызов. Внутри он разбит ещё на небольшие подпакеты; за одну итерацию выбирается **один**. Переход к волне implementation не раньше завершения соответствующего evidence-backed owner audit, кроме случаев исправления критического регресса с явной причиной.

## Принцип механизма

Нельзя исправлять ориентацию на уровне одного видимого `EnderChestBlock`/свечи и объявлять семейство готовым. Нужно определить **всех владельцев алгоритма**: базовый класс, overrides, созданные/заменённые состояния после установки, item/dispenser/worldgen, физический target и локальную семантику, canSurvive/updateShape/neighborChanged, рендер/BE и NeoForge-модификации. Один механизм может иметь несколько тонких точек интеграции, но одну стабильную Planet-семантику.

## Точка продолжения

**После Stage 3A-7.4 (2026-10-10):**
завершена **4/4** исследовательская карточка
двух скалк-сенсоров, НЕ их игровая
реализация/приёмка. Оригинальный
неизменённый NeoForge **21.1.215**
ZIP artifact 11643813158 SHA256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`
прочитан заново: **1060 BLOCK IDs /
241 Java-класс, 1333 ITEM IDs, 1712
BlockState properties**. Для всех
**71 исследованного класса / 192 точных
BLOCK IDs** проверены пять
`effective_method_owners` по полным
сигнатурам методов, все значения
совпали с текущим 16-колоночным
реестром GitHub. Все четыре независимо
вычисленных контрольные суммы совпали:
roster `0xf188a064`, exact IDs
`0x8a6840ba`, five owners
`0xa81e5835`, combined
`0x59d9c0aa`.
[Подробный независимый отчёт](../research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md).

Состояние реестра **без повышения новых
классов**: **71/241**
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(192/1060 BLOCK IDs), **170/241**
`REVIEW_PENDING` (868 BLOCK IDs).
Все 241 NeoForge ASM patched-bytecode /
Planet adapter / gameplay acceptance
поля всё ещё `REVIEW_PENDING`.
Не было изменений Java, сборки или
игровых проверок. Stage 3A и Phase 2
**не завершены**.

Создана следующая маленькая карточка:
[Stage 3A-8](phase-02/02f-sculk-shrieker-catalyst-owners.md)
для **SculkShriekerBlock**
(`minecraft:sculk_shrieker`) и
**SculkCatalystBlock**
(`minecraft:sculk_catalyst`),
**двух ещё не исследованных зарегистрированных
классов / двух ID**. Это две
отдельные логики: шрикер/предупреждение
о хранителе/скалк-сигналы, катализатор/
`ENTITY_DIE`/BLOOM/`SculkSpreader`.
`LightningRodBlock` остаётся pending
отдельного погодного семейства.

**NEXT первая незавершённая задача —
[3A-8.1](phase-02/02f-sculk-shrieker-catalyst-owners.md):**
сверить точные NeoForge owners и изучить
полный исходный граф разных обработчиков
BE/вибраций/скалк-распространения,
один отдельный коммит и checkpoint.

Обновление этого файла означает только, что появился маршрут и checkpoint, **не что Phase 2 исправлена**.
