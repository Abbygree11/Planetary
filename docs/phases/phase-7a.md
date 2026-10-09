# Фаза 7A — Сигналы, редстоун, механизмы и logistics

**Статус из роадмапа:** RESEARCH COMPLETE / IMPLEMENTATION PLANNED. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **7A.1** 7A.1: physical-neighbor source selection vs source-local redstone signal Direction.
- [ ] **7A.2** 7A.2: redstone directed graph owners (wires, repeaters, comparators, observers, torches, sensors).
- [ ] **7A.3** 7A.3: rail signals + graph semantics поверх Phase2 rail topology.
- [ ] **7A.4** 7A.4: pistons/moving structures/neighbor updates; отдельный Phase3 moving-render contract.
- [ ] **7A.5** 7A.5: NeoForge sided item/fluid/energy capability contracts + side cache invalidation.
- [ ] **7A.6** Подтвердить работу на шести гранях и seam; не терять сигнал из-за двойного поворота.

## Критерии и зависимости

Phase 2 orientation не включает redstone propagation: границу фиксировать явно.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
