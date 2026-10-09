# Фаза 5 — Жидкости

**Статус из роадмапа:** RESEARCH COMPLETE / IMPLEMENTATION PLANNED. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **5.1** Подтвердить source/owner matrix FlowingFluid и LiquidBlock physical/local semantics, ограничения finite-world.
- [ ] **5.2** Отдельно fluid scheduler, FluidState height/shape и flowing velocity, без двойных преобразований.
- [ ] **5.3** Отдельно waterlogging/LiquidBlockContainer и bucket placement/pickup с тестами item paths.
- [ ] **5.4** Отдельно LiquidBlockRenderer, interactions water/lava, bubble/ambient/drip origin и NeoForge fluid hooks.
- [ ] **5.5** Один coherence acceptance на шести гранях, seams, storage/save/client-server после механик Phase2.

## Критерии и зависимости

Механизмы source/level остаются независимыми от particle motion и block placement.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
