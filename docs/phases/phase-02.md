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
| 2.3A-8 | [02f-sculk-shrieker-catalyst-owners.md](phase-02/02f-sculk-shrieker-catalyst-owners.md) | **ACTIVE 3/4** | Six-face/canonical-vs-physical sculk contract; Warden SpawnUtil world-Y collider assumption, catalyst charge and 18-offset SculkSpreader, item/BE/generation writers, 26 future test fixtures. 73/241 reviewed; ASM/runtime/gameplay pending |
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

**После Stage 3A-8.3 (2026-10-10):** составлен [контракт для шести граней, рёбер и углов, с 26 сценариями будущих проверок](../research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_SIX_FACE_SEAM_ACCEPTANCE_CONTRACT_1_21_1.md). Ванильный `SpawnUtil` для Warden использует 20 попыток, world XZ ±5 и вертикальный диапазон 6, сканирует world DOWN и требует world-UP collider. Это отдельная задача адаптации поиска спавна сущностей на локальных поверхностях, не основание менять `Direction.UP` везде. `SculkSpreader.ChargeCursor` рассматривает **18 физических смещений** (6 осевых + 12 двухосевых), а не 6 и не только четыре локальные горизонтальные стороны. Катализатор использует отдельный direct GameEvent.ENTITY_DIE listener, сохраняет физический source event Vec3, создаёт cursor от world-UP+0.5, обновляет BE каждый серверный тик, BLOOM сбрасывает отдельным block tick через 8 тиков. Шрикер использует `VibrationSystem` и 90-tick reset, с отдельным `onRemove`.

Контракт фиксирует **единственный физический BlockPos/BlockState/BE на ребре или углу**, детерминированный canonical frame, разделение локального support и реальных world XYZ событий, физических X/Z чанков, соседних графов, worldgen и NBT. План проверок: vanilla-контроль, шесть граней, рёбра/углы, чанки, загрузка, жидкости, частицы, генерация, предупреждение и Warden. **Все 26 тестов только описаны, не запускались.** Новых классов не повышено: **73/241** reviewed (**194/1060 BLOCK IDs**), 168/241 pending (866 IDs); все 241 NeoForge ASM/Planet adapter/gameplay статусы PENDING. Java-код не менялся.

**NEXT первая незавершённая — [3A-8.4](phase-02/02f-sculk-shrieker-catalyst-owners.md):** независимая полная сверка оригинального NeoForge ZIP и всех статусов, подготовка следующего семейства, один исследовательский коммит.

Обновление документа означает завершение **исследовательского подпакета**, не всей фазы 2.
