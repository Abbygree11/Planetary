# Фаза 9 — Структуры и жёсткая топология

**Статус из роадмапа:** RESEARCH COMPLETE / IMPLEMENTATION PLANNED. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **9.1** 9A: generation boundaries, clearance, StructureStart bounding boxes and AVOID_EDGE policy.
- [ ] **9.2** 9A: processors/placements/high-level rule of local UP versus global chunk height; ensure modded structures.
- [ ] **9.3** 9B: portal frame orientation, geometry, ignition from Phase2 fire and dimension travel contracts.
- [ ] **9.4** 9B: real-time structure pairs/rigid state transforms and cross-face geometry; Phase3 rendering contract.
- [ ] **9.5** Acceptance: no structure inside invalid seams, reproducible seeds, and portal creation/travel tested end to end.

## Критерии и зависимости

Block ignition Phase2 and portal graph Phase9 имеют разных owners.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
