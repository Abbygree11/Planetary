# Фаза 8 — Рельеф, биомы и генерация

**Статус из роадмапа:** RESEARCH COMPLETE / PARTIAL FOUNDATION. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **8.1** 8A: biome/worldgen data-driven composition — модовые биомы без закрытого списка.
- [ ] **8.2** 8B: macro elevation and continuous field with proper cube-face seams.
- [ ] **8.3** 8C: ocean continuous sea shell avoiding visible steps.
- [ ] **8.4** 8D: river/lake drainage and continuity across edges; ties into Phase5 fluids.
- [ ] **8.5** 8E: caves, features, structures, carving + correctly-owned Phase9 structure restrictions.
- [ ] **8.6** 8F: distant terrain, LOD and memory budget.
- [ ] **8.7** Проверять по отдельным seed-regression и biome-mod smoke, нельзя объявить фазы готовой по плоскому тестовому миру.

## Критерии и зависимости

PlanetChunkGenerator/fixed plains — стартовая платформа, не конечная worldgen.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.


### P26 source/growth and attachment handoff (2026-10-12 C18)

[Exact P26 Multiface/Vine/Spreader/Feature owner study](../research/PHASE2_STAGE3C18_MULTIFACE_VINE_SPREAD_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md): Phase2.4C handles attachment validity and target port transport; Phase8 owns vanilla worldgen `MultifaceGrowthFeature.placeGrowthIfPossible`, natural Vine random ticks, GlowLichen bonemeal spread and SculkVein charge-driven regrowth, including unloaded-chunk guards, postprocessing and mod-defined `SpreadConfig`. The source ASM packet **does not implement growth**, mod Biome features or gameplay. Keep `GrowingPlantBlock` P29 as separate mechanism, C19 research pending.


### P29 directed GrowingPlant head/body lifecycle / alternative worldgen (C19 2026-10-13)

[Detailed actual 1.21.1 NeoForge / 8 vanilla classes + real pinned Biomes O' Plenty source and exact selected bytecode audit](../research/PHASE2_STAGE3C19_GROWINGPLANT_HEAD_BODY_DIRECTED_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md): `GrowingPlantHeadBlock.randomTick` uses patched NeoForge `CommonHooks.canCropGrow` before writing target and `fireCropGrowPost` after success; body head lookup uses `BlockUtil.getTopConnectedBlock`, KelpFeature, TwistingVinesFeature and WeepingVinesFeature write explicit head/body without BlockItem. Phase8 must independently adapt worldgen writers/height and biome-mod growth features, preserve real WORLD GEN Level provenance, avoid double callbacks and foreign level leakage. Phase2.4C owns runtime local plant-port chain, Phase5 kelp fluid, Phase3 geometry, Phase10 loaded BOP. Source/selected ASM test **not** worldgen acceptance or complete phase 8.
