# Planetary

**Экспериментальный мод Minecraft 1.21.1 / NeoForge 21.1.215** с кубической планетой и локальной гравитацией. Разработка ведётся в ветке [`2.0`](https://github.com/Abbygree11/Planetary/tree/2.0). Проект **не является завершённым модом для обычного survival**: отдельные механизмы уже работают, но совместимость всего игрового мира и сторонних модов ещё проходит исследование и доработку.

## Идея

Мир использует обычную физическую XYZ-сетку Minecraft: блок на ребре куба существует **в единственном экземпляре**, а не дублируется на соседних гранях. Поле гравитации разбито на шесть областей, в которых локальный `DOWN` направлен к ядру планеты. При переходе между областями поворачиваются локальная система координат игрока и семантика ориентированных блоков.

Архитектурное правило: физические `BlockPos`, `Direction`, `VoxelShape` и стандартные API мира не переопределяются глобально. Planetary адаптирует **только контекстно-зависимые** запросы, сохраняя ванильные и NeoForge-вызовы, важные для совместимости с модами.

## Текущее состояние ветки 2.0

| Область | Что есть | Что ещё не принято |
| --- | --- | --- |
| Топология и гравитация | Шесть граней, преобразования систем координат, переходы через рёбра, серверные/клиентские адаптеры движения | Полная проверка на всех углах, в чанках и со всеми видами сущностей |
| Блоки и столкновения | Адаптеры размещения, опоры, `VoxelShape`, соседних обновлений и части ориентированных блоков | Все владельцы механик, динамические/модовые формы, повторная игровая приёмка |
| Рендеринг | Поворот стандартных `BakedModel` и квадов, frame-aware culling, некоторые эффекты | Сложные модели, AO/свет/тени, custom render engines, BER и fluid renderer |
| Генерация мира | `PlanetChunkGenerator`, исходный объём куба и записи в обычные чанки | Полная генерация биомов, структур, декораций, поверхностей, пещер и интеграция модов |
| Обновления и тики | Маршрутизация части `Level`-операций, block/fluid ticks и BlockEntity-хранилище | Полноценные виртуальные BE-тикеры, сохранение данных, все события NeoForge |
| Редстоун, поршни, жидкости, автоматизация | Исследованы реальные владельцы механизмов и версии их байткода | Общая адаптация локальной гравитации, игровые испытания и загруженные сторонние моды |

**Важно:** успешный `Gradle/JUnit/ASM` означает проверку выбранных методов и сигнатур, **а не** прохождение тестов в запущенном Minecraft. Результаты поэтапных проверок фиксируются отдельно от игровой приёмки.

## Сборка и запуск для разработчиков

Нужны **Java 21**, Gradle (скрипт `bootstrap-gradle.ps1` подготавливает локальный запуск) и окружение, совместимое с NeoForge 21.1.215. Основная автоматическая проверка выполняется в [GitHub Actions](https://github.com/Abbygree11/Planetary/actions/workflows/test.yml).

На Windows PowerShell:

```powershell
.\test.ps1
.\run-client.ps1
```

Скрипт `run-client.ps1` при запуске dev-клиента отключает музыку Minecraft в локальном `run/options.txt`. Для самостоятельной работы Gradle доступны задачи `compileJava`, `compileTestJava`, `test`, `runClient`.

Сборка и успешные unit-тесты **не гарантируют**, что конкретная экспериментальная игровая функция включена или исправна. Для проверки поведения внутри игры используйте [матрицу приёмки](docs/acceptance/PHASE2_ORIENTATION_ACCEPTANCE_1_21_1.md) и соответствующий этап roadmap.

## Архитектура и документация

- [Протокол разработки — `AGENTS.md`](AGENTS.md) — обязательные правила исследования, разработки и статусов.
- [План реализации — `docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md) — фактическая очередь работ и зависимости.
- [Непрерывный контекст — `docs/AI_CONTEXT.md`](docs/AI_CONTEXT.md) — последние проверенные решения и открытые проблемы.
- [Карты и карточки фаз](docs/phases/README.md) — короткие пакеты с восстановлением после обрыва чата.
- [Механизмы гравитации](docs/research/GRAVITY_MECHANISM_MAP_1_21_1.md), [переносимость версий](docs/research/PORTABILITY_STRATEGY.md), [матрица форм](docs/research/SHAPES_1_21_1.md), [жидкости](docs/research/FLUID_MECHANISM_MATRIX_1_21_1.md).
- [Актуальная фаза 2.3C: точный NeoForge ABI/lifecycle](docs/phases/phase-02/04-lifecycle-neoforge.md) — ограниченные семейства механизмов и ссылки на доказательства; [исходная сверка 15 пакетов с P01–P40](docs/research/PHASE2_STAGE3C_1_TO_15_SCOPE_RECONCILIATION_AND_NEXT_OWNER_GATES_1_21_1.md) и [живая матрица 40 механизмов](docs/research/PHASE2_STAGE3C_P01_P40_ABI_COVERAGE_DISPOSITION_1_21_1.tsv) отделяют проверенные выбранные ABI от ещё не проверенных конкретных алгоритмов. C16 закрыл P09/P10/P35 по выбранному ABI; [C17 исследует рельсовый граф](docs/research/PHASE2_STAGE3C17_RAILSTATE_PHYSICAL_GRAPH_NEOFORGE_MOD_ASM_1_21_1.md).

## Направление развития

Цель — survival-совместимый мир с локальной гравитацией, полноценным строительством, жидкостями, существами, сигналами, генерацией и модами. Для этого Planetary исследует **общие владельцы механизмов**, а не вводит отдельный патч для каждого ванильного или модового блока.

Неподтверждённые механизмы остаются открытыми в roadmap. Файлы исследований фиксируют необходимые точки интеграции; они не являются утверждением, что соответствующая функция уже реализована.
\n\n**2.3C-18:** [P26 Multiface/Vine/three spread operators research](docs/research/PHASE2_STAGE3C18_MULTIFACE_VINE_SPREAD_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md), [CI38307680178](https://github.com/Abbygree11/Planetary/actions/runs/38307680178) selected NeoForge21.1.215 classpath ASM/JUnit SUCCESS; six faces/12 seams/8 corners and actual mod game acceptance remain OPEN. Next 2.3C-19 P29 GrowingPlant.\n

**2026-10-13: 2.3C source/selected ABI research scoped COMPLETE** — [19 research packets with verified selected 21.1.215 ASM/JUnit CI and 40-row evidence limits](docs/research/PHASE2_STAGE3C_1_TO_19_FINAL_SELECTED_ABI_SCOPE_GATE_1_21_1.md); last [C19 GrowingPlant/Kelp/Vines/NeoForge crop event and real BOP1.21.1 source](docs/research/PHASE2_STAGE3C19_GROWINGPLANT_HEAD_BODY_DIRECTED_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md), [CI38311748785](https://github.com/Abbygree11/Planetary/actions/runs/38311748785) SUCCESS. 9 selected-specific, 11 selected-shared, 16 shared-only, 4 partial, zero specifically identified still-uncovered algorithm family, **not** all 241/87 patched JVM method owner verified and **not** real gameplay/loaded mod compatibility. **2.4A-1 typed placement intent/resolver implementation:** [scope, code and independent acceptance gates](docs/research/PHASE2_STAGE4A1_TYPED_PLACEMENT_INTENT_1_21_1.md); CI/gameplay status must be checked separately. **2.4A-2:** [typed property-domain helper and tests](docs/research/PHASE2_STAGE4A2_TYPED_ORIENTATION_PROPERTIES_1_21_1.md) (CI/gameplay separately gated). Read-only [2.4A-3 UseOn original-hit integration](docs/research/PHASE2_STAGE4A3_USEON_ORIGINAL_HIT_AUTHOR_ADAPTER_1_21_1.md) follows; owner-specific runtime algorithms and gameplay still open., then engineering phases; no more indefinite 2.3C research family numbering without a named uncovered owner.
