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
| 2.3A-5 | [02c-rail-owners.md](phase-02/02c-rail-owners.md) | **NEXT** | Rails: 3 still-pending registered concrete classes / 4 IDs, with nonregistered BaseRailBlock/RailState; daylight separately pending |
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

**Актуальный статус после 2.3A-4.4 (2026-10-10):**
в оригинальном NeoForge 21.1.215 CI артефакте
**241 класс / 1060 зарегистрированных BLOCK IDs**;
**63/241** `SOURCE_REVIEWED_INTEGRATION_PENDING`
(170 IDs) и **178/241** `REVIEW_PENDING` (890 IDs).
Пять владельцев методов для 63 классов и точные ID
повторно сравнены с оригинальным ZIP: четыре хеша
**совпали**. Завершены только четыре
исследовательских подпакета 3A-4; фаза 2 ещё
не реализована/не принята. Все ASM/Planet/gameplay
статусы `REVIEW_PENDING`. Старые числа в таблице
выше — исторические snapshots.

**Следующий независимый пакет:** [3A-5.1
rail-owner census/source graph](phase-02/02c-rail-owners.md),
три реальные pending класса рельсов: RailBlock,
DetectorRailBlock, PoweredRailBlock (4 block IDs),
с анализом не-регистровых BaseRailBlock/RailState
и локальных уклонов. DaylightDetectorBlock остаётся
pending и будет исследован отдельно. Один пакет,
один коммит, никаких заявлений о runtime PASS.

Обновление этого файла означает только, что появился маршрут и checkpoint, **не что Phase 2 исправлена**.
