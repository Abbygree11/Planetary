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
