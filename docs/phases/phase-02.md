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
| 2.3A-4 | [02b-redstone-signal-owners.md](phase-02/02b-redstone-signal-owners.md) | **NEXT** | 11 *still pending* redstone/tension/signal nodes (12 IDs), 8–11 per small source audit |
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

**Актуальное состояние после 2.3A-3.4 (2026-10-10):**
**55/241** классов source+NeoForge reflection-reviewed
(**162/1060** BLOCK IDs); **186/241** `REVIEW_PENDING`
(**898/1060** IDs). Все 241 статуса ASM/Planet runtime/gameplay
по-прежнему `REVIEW_PENDING`. Это **не** завершение
исследования 241 классов и **не** игровая приёмка.
Числа в строках завершённых старых пакетов выше —
исторические точки этих подпакетов.

**Следующий отдельный пакет:** [2.3A-4, microtask 1](phase-02/02b-redstone-signal-owners.md)
— реальные pending владельцы натяжных нитей, редстоуна,
диодных схем, датчиков и рельсов (11 candidate classes /
12 BLOCK IDs). За один ответ — одно доказанное подсемейство,
отдельный GitHub commit, без преждевременного runtime PASS.

Обновление этого файла означает только, что появился маршрут и checkpoint, **не что Phase 2 исправлена**.
