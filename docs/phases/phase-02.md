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
| 2.3A-8 | [02f-sculk-shrieker-catalyst-owners.md](phase-02/02f-sculk-shrieker-catalyst-owners.md) | **DONE 4/4 research only** | Original full 241-class roster and 73 source-reviewed class/194 BLOCK ID/five-method-owner tuples independently reconcile; two sculk block creators, 26 future tests; no ASM/Planet/gameplay PASS |
| 2.3A-9 | [02g-sculk-spread-growth-owners.md](phase-02/02g-sculk-spread-growth-owners.md) | **ACTIVE 2/4** | Exact original sculk ordinary BlockItem (7/7 placement declaring owners); no block properties/BE component targets; SculkVeinBlock conditional direct SCULK writer, SculkPatchFeature indirect worldgen route, generic template/command bypasses; 74/241 reviewed and all ASM/gameplay pending |
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

**После Stage 3A-9.2 (2026-10-10):** [исходный ITEM creator и альтернативные direct writers для `minecraft:sculk`](../research/PHASE2_STAGE3A_SCULK_BLOCK_ITEM_ALTERNATE_WRITERS_1_21_1.md). Проверен оригинальный zip NeoForge 21.1.215: **ровно один** ITEM `minecraft:sculk` типа `BlockItem` связывается полем `placed_block` с `SculkBlock`. Семь сигнатур placing/registration принадлежат `BlockItem`, `Item.use` отдельно; полный SHA-256 joined row `e3e5e3822245209bfb6923e4cf3226e7649a24af9481d8cf1d3d46b7fde246e4`. У этого BlockState нет ни одного свойства, у SculkBlock нет BE: `DataComponents.BLOCK_STATE` не способен применить несуществующие FACING/WATERLOGGED; `BLOCK_ENTITY_DATA` не создаёт BE.

Прямой автор **нового Blocks.SCULK — `SculkVeinBlock.attemptPlaceSculk`**, который при курсоре с зарядом и соответствующей `SCULK_REPLACEABLE(_WORLD_GEN)` метке заменяет физическую соседнюю ячейку по направлению существующей грани жилы. Отдельно `SculkBlock.attemptUseCharge` при успешном росте создаёт **SENSOR/SHRIEKER**, а не новый SCULK. `SculkPatchFeature` создаёт SCULK **косвенно** через `SculkSpreader` и vein writer; сам напрямую создаёт катализатор и шрикер. `CaveFeatures.SCULK_VEIN` создаёт vein через `MULTIFACE_GROWTH`, не SCULK напрямую. `StructureTemplate`, `/setblock`, `/fill` способны записывать SCULK через прямое состояние без `BlockItem`; наличие конкретного vanilla NBT шаблона с SCULK не проверялось. Замена вызывает `Block.pushEntitiesUp` — дополнительная будущая интеграционная проверка entity/local-up фазы 7.

**Исследовательский реестр не изменён:** **74/241** классов reviewed (**195/1060 BLOCK IDs**); **167/241** pending (**865/1060 IDs**), все 241 NeoForge ASM, Planet adapter и gameplay gates PENDING. Java-код не изменялся, CI и игровые тесты не выполнялись.

**NEXT первая незавершённая — [Stage 3A-9.3](phase-02/02g-sculk-spread-growth-owners.md):** контракт 18 physical offsets, six-face source/target local positions, seam/corner growth и воспроизводимые будущие тесты. ONE docs-only commit. Phase 2 OPEN.
