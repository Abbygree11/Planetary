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
| 2.3A-10 | [02h-lightning-rod-weather-owners.md](phase-02/02h-lightning-rod-weather-owners.md) | **DONE 4/4 source research** | Original LightningRodBlock one ID/five compiled declaring owners + BlockItem; LightningBolt/coppoer event and alternate states; 28 UNRUN six-face redstone tests; original ZIP 241-class/75-reviewed fingerprint MATCH; all runtime gates pending |
| 2.3A-11 | [02i-egg-hatching-substrate-owners.md](phase-02/02i-egg-hatching-substrate-owners.md) | **DONE 4/4 source research** | Original exact TurtleEggBlock/SnifferEggBlock/FrogspawnBlock 3 BLOCK IDs, 3 ITEM joins incl. PlaceOnWaterBlockItem, source Turtle/Frog natural direct authors, 30 UNRUN future six-face tests, 241/78-class reconciliation; all ASM/gameplay pending |
| 2.3A-12 | [02j-cold-surfaces-snow-ice-owners.md](phase-02/02j-cold-surfaces-snow-ice-owners.md) | **DONE 4/4 source research only** | SnowLayerBlock/PowderSnowBlock/IceBlock/FrostedIceBlock: four full compiled declaring owner tuples, registered ITEM vs SolidBucket and no frosted_ice item; 32 UNRUN six-face thermal/water tests; original ZIP 241/82 owners MATCH; all ASM/gameplay gates pending |
| 2.3A-13 | [02k-coral-live-dead-water-owners.md](phase-02/02k-coral-live-dead-water-owners.md) | **ACTIVE 0/4 internal, entire family per «кк»** | Seven concrete coral live/dead full/plant/floor/wall classes, 35 exact BLOCK IDs; StandingAndWallBlockItem dual-target, fluid/waterlogged and death ticks pending |
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

**После Stage 3A-12.4 (2026-10-10):** весь cold surfaces source-research пакет за одно «кк» выполнен **4/4**: [полный оригинальный зарегистрированный BLOCK/ITEM и source-owner аудит снега, рыхлого снега и двух видов льда](../research/PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_BLOCK_ITEM_ALTERNATE_AUTHOR_AUDIT_1_21_1.md), [32 БУДУЩИХ / НЕВЫПОЛНЕННЫХ six-face, ребро, угол, collisions, fluid и thermal приемочных теста](../research/PHASE2_STAGE3A_COLD_SURFACE_SIX_FACE_THERMAL_FLUID_ENTITY_CONTRACT_1_21_1.md), [независимая полная оригинальная ZIP vs GitHub 241-классовая/82-reviewed сверка всех пяти original nearest declaring owners и точных BLOCK IDs](../research/PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_82_CLASS_RECONCILIATION_1_21_1.md). Предварительный docs+ledger checkpoint: [`cd66b4422198093cef8bcd474867a29af8b278c4`](https://github.com/Abbygree11/Planetary/commit/cd66b4422198093cef8bcd474867a29af8b278c4).

SnowLayerBlock источник: LAYERS1..8 на одном настоящем BlockPos, canBeReplaced проверяет clicked Direction.UP при определённых контекстах, canSurvive — отдельные SNOW_LAYER_CAN/CANNOT теги и физическую UP collision опоры, randomTick удаляет при BLOCK light >11; форма столкновения отличается от формы выделения. PowderSnowBlock не FallingBlock и не жидкость: entityInside/динамическая collision + boots/fallDistance/stuck world Vec3, BucketPickup обратный путь, предмет `SolidBucketItem` — и BlockItem.useOn, и самостоятельный DispensibleContainerItem.emptyContents direct setBlock POWDER_SNOW, без WATERLOGGED/FACING. IceBlock.randomTick яркость + melted WATER или AIR в ultraWarm, playerDestroy отдельно смотрит физическую опору `pos.below()` и энчант. FrostedIceBlock объявляет AGE0..3, планирует tick, обходит **шесть ФИЗИЧЕСКИХ соседей**; inherited randomTick=IceBlock в оригинальном реестре не означает, что блок зарегистрирован с randomTicks. Оригинальный прямой ITEM для FROSTED_ICE **отсутствует**. SnowAndFreezeFeature пишет SNOW+ICE по глобальному XZ/Heightmap, источник FrostWalker disk-data не доказан только просмотром ReplaceDisk.

**Текущие проверенные численности:** оригинальный неизменённый ZIP NeoForge21.1.215 artifact11643813158 SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`, зарегистрировано 241 класса/1060 BLOCK IDs/1333 ITEM/1712 properties. **82/241** классов и **203/1060** exact BLOCK IDs source+original 5 declaring owners REVIEWED; **159/241** классов и **857/1060** BLOCK IDs остаются source REVIEW_PENDING; все **241×3** NeoForge patched ASM / Planet adapter / gameplay acceptance значения `REVIEW_PENDING` (723). Все четыре independent original Python vs GitHub JS canonical FNV32+FNV64 пары MATCH. Не было Java/CI/gameplay тестов или PASS.

**NEXT одно «кк» → [ВЕСЬ Stage 3A-13](phase-02/02k-coral-live-dead-water-owners.md)**: семь оригинальных реальных классов живых/мёртвых кораллов (по 5 зарегистрированных BLOCK ID = 35), 25 original ITEM join с 10 StandingAndWallBlockItem floor→wall писателями и no-direct-WallFan item, fluid WAT­ERLOGGED, поддержка и запланированная смерть. Все четыре внутренние части за один запрос без уточнений. Phase2/Stage3A остаются OPEN.
