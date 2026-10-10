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
| 2.3A-9 | [02g-sculk-spread-growth-owners.md](phase-02/02g-sculk-spread-growth-owners.md) | **DONE 4/4 research only** | SculkBlock original source and five declaring owners, exact ITEM/alternate authors, six-face 28 future tests, final original ZIP 74-reviewed/195 BLOCK-ID crosscheck; all ASM/Planet/gameplay gates pending |
| 2.3A-10 | [02h-lightning-rod-weather-owners.md](phase-02/02h-lightning-rod-weather-owners.md) | **ACTIVE 0/4** | Next source-pending original `minecraft:lightning_rod` / LightningRodBlock, six-direction FACING and WATERLOGGED/POWERED, LightningBolt strike and copper/redstone/particle independent owners |
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

**После Stage 3A-9.4 (2026-10-10):** исследовательский пакет `SculkBlock` / распространения скалка закрыт **4/4 как документационное исследование**, НЕ как реализация или приёмка. [Полная независимая сверка исходного NeoForge ZIP с живым 241-строчным реестром и 74 исследованными классами](../research/PHASE2_STAGE3A_SCULK_GROWTH_ORIGINAL_74_CLASS_RECONCILIATION_1_21_1.md): ZIP SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` совпал, разобраны 1060 оригинальных BLOCK IDs/241 classes, 1333 ITEM и 1712 state-property records. По всем **74 классам и их 195 точным BLOCK ID** сверены пять original full-qualified-signature владельцев; все 4 исходных vs GitHub контрольных отпечатка в **FNV-1a32 И FNV-1a64 MATCH**. **167 классов/865 BLOCK IDs** остаются source pending. **ВСЕ 241** NeoForge patched-bytecode, Planet adapter и gameplay review gates `REVIEW_PENDING`. Все 28 тестов SG9-01…28 только предложены, не запускались; Java-код не изменялся.

**Следующий приоритет** отобран по реально незавершённым конкретным классам: **`LightningRodBlock`** (зарегистрированный `minecraft:lightning_rod`, **1 BLOCK ID**, `facing,powered,waterlogged`, исходная цепь наследования `LightningRodBlock>RodBlock>DirectionalBlock>Block>BlockBehaviour`, пять original effective declaring owners `LightningRodBlock/BlockBehaviour/LightningRodBlock/BlockBehaviour/Block`, текущий ledger `REVIEW_PENDING`). Он связывает Phase 2 orientation/support и Phase 3 particle/heightmap, Phase 5 waterlogging, Phase 7A redstone 8-tick POWERED/LightningBolt, Phase 8/9 world-surface strike и медь. `EndRodBlock` уже рассмотрен, не добавляется повторно; медные классы остаются собственными отдельными непройденными owner audit.

**NEXT первая незавершённая — [Stage 3A-10.1](phase-02/02h-lightning-rod-weather-owners.md)**: полный исходный compiled/declaration и сравнительный source audit `LightningRodBlock` с соседними owners. Один документационный GitHub коммит; STOP. Phase 2 и Stage 3A всё ещё OPEN.
