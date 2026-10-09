# Фаза 6 — Навигация и AI

**Статус из роадмапа:** PARTIAL. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **6.1** 6A: chart-aware path node graph, walkability, local floor and passable support from Phase2.
- [ ] **6.2** 6B: MoveControl + body steering on rotated faces, отдельно path-target conversion.
- [ ] **6.3** 6C: target generation для RandomStroll и других common goals, не ломать RNG и vanilla target weights.
- [ ] **6.4** 6D: pathfinder mobility families: walkers, swimmers, flyers and special navigation; aquatic after Phase5.
- [ ] **6.5** Согласованная acceptance матрица: mobs, seam, target tracking, support changes, no tick regression.

## Критерии и зависимости

Исправленная походка отдельного моба не доказывает корректную AI navigation.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
