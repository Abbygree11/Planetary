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
| 2.3A-13 | [02k-coral-live-dead-water-owners.md](phase-02/02k-coral-live-dead-water-owners.md) | **DONE 4/4 source research** | Seven original living/dead coral floor/wall classes, 35 exact BLOCK IDs, 25 ITEM join (10 dual StandingAndWallBlockItem), CoralFeature direct creators, 32 UNRUN six-face tests, original ZIP 241/89 classes MATCH; ASM/Planet/gameplay pending |
| 2.3A-14 | [02l-aquatic-absorption-sea-pickle-owners.md](phase-02/02l-aquatic-absorption-sea-pickle-owners.md) | **DONE 4/4 source research ONLY** | Original SpongeBlock/WetSpongeBlock/SeaPickleBlock, three original BlockItems; physical six-neighbor water BFS depth6 count65 incl root, ultraWarm drying and world-Y particles, local support+bonemeal coral writer; 32 UNRUN tests, independent original ZIP full signature 241/92 classes match; all ASM/gameplay pending |
| 2.3A-15 | [02m-amethyst-budding-growth-owners.md](phase-02/02m-amethyst-budding-growth-owners.md) | **ACTIVE 0/4 internal; full family per «кк»** | Exact two source-pending AmethystBlock/BuddingAmethystBlock one original ID each; existing reviewed AmethystClusterBlock (four bud IDs) dependency; all three original ITEM/feature authors and six-neighbor growth pending |
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

**После Stage 3A-14.4 (2026-10-10):** завершён в одном «кк» пакет SpongeBlock, WetSpongeBlock, SeaPickleBlock **4/4 исследовательских подпункта, не реализация Java и не gameplay PASS**. [Исходные NeoForge registered BLOCK/ITEM, five nearest declaring owner full signatures, Sponge BFS, ultraWarm wet sponge, SeaPickle и CoralFeature alternate writers](../research/PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_ORIGINAL_BLOCK_ITEM_WRITERS_1_21_1.md); [32 БУДУЩИХ UNRUN теста на 6 граней/12 рёбер/8 углов и fluid/chunk/bucket/bonemeal](../research/PHASE2_STAGE3A_AQUATIC_SPONGE_SEA_PICKLE_SIX_FACE_WATER_CONTRACT_1_21_1.md); [полная независимая оригинальная ZIP 241 класса/92 reviewed/241 exact BLOCK ID/five full-signature method owner сверка](../research/PHASE2_STAGE3A_AQUATIC_ORIGINAL_92_CLASS_RECONCILIATION_1_21_1.md). Промежуточный коммит [`9e660e6`](https://github.com/Abbygree11/Planetary/commit/9e660e6fa5eed60fdf3c5b457b0807057704d552).

**Граница механизмов:** SpongeBlock `onPlace/neighborChanged` запускает BFS по 6 ФИЗИЧЕСКИМ соседям, глубина 6, макс 65 принятых позиций **включая исходную губку** (до 64 иных водных узлов), с индивидуальными BucketPickup/LiquidBlock/Kelp/Seagrass удалениями и переходом в WET_SPONGE если забрал хотя бы одну соседнюю воду. `WetSpongeBlock.onPlace` в ultraWarm немедленно пишет SPONGE и событие 2009, клиентские `animateTick` капли ориентированы на мировую Y, не локальную. `SeaPickleBlock` — PICKLES1..4 в одном BlockPos, WATERLOGGED, опора `pos.below()` и настоящий UP face-support target, `performBonemeal` меняет блоки только если источник wet и снизу CORAL_BLOCKS, создаёт морские огурцы через физический XZ/Y обход, `CoralFeature` отдельно пишет огурцы worldgen. Различаем source-local поддержку (Phase2), действительную fluid/BucketPickup физику (Phase5), эффект клиента (Phase3), feature-space XZ/Y (Phase8/9) и реальное управление состояниями; без глобального поворота физического BlockPos.

**Аудиторский нюанс с перегрузками:** при новой независимой Python-сверке временно выбрана первая overload `getStateForPlacement(LevelAccessor)` KelpBlock вместо обязательной `getStateForPlacement(BlockPlaceContext)`. Исходник NeoForge содержит GrowingPlantHeadBlock(LevelAccessor) и KelpBlock(BlockPlaceContext). Предыдущий ledger корректно содержит **KelpBlock**, никакая ошибочная запись не исправлялась. Скрипт затем переделан на ТОЧНОЕ сравнение ПОЛНЫХ квалифицированных типов аргументов, четыре original-vs-live FNV32+FNV64 отпечатка MATCH. В дальнейшем выбирать метод только по имени запрещено.

**Текущий статус:** оригинальный unchanged Minecraft **1.21.1**, NeoForge **21.1.215** artifact11643813158 SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` (241/1060 BLOCK, 1333 ITEM, 1712 properties). Live ledger **92/241 source+original nearest declaring-owner reviewed classes**, **241/1060 exact BLOCK IDs**; pending **149/241 classes, 819/1060 IDs**. ВСЕ `241×3=723` ASM/Planet/gameplay поля `REVIEW_PENDING`; 32 теста НЕ ЗАПУСКАЛИСЬ, Java/CI/build/client/server не запускались.

**NEXT одно «кк» → весь [Stage 3A-15](phase-02/02m-amethyst-budding-growth-owners.md)**: AmethystBlock и BuddingAmethystBlock исходные два source-pending класса/2 BLOCK IDs, ранее исследованный AmethystClusterBlock четыре ID только как dependency, не продвигать второй раз. Один запрос = 4 внутренних подпункта, коммиты по контрольным точкам. Phase2/Stage3A OPEN.
