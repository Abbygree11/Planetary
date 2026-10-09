# Фаза 7 — Физика сущностей и транспорт

**Статус из роадмапа:** RESEARCH COMPLETE / IMPLEMENTATION PLANNED. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **7.1** 7.1: Entity.move/physical AABB/contact and gravity vs local projection, impulse and collision separation.
- [ ] **7.2** 7.2: Living locomotion/jump/fall/motion state, foot support, effects и damage.
- [ ] **7.3** 7.3: non-living entities, projectiles, falling blocks, particles-owning entity types; не считать визуальный результат физическим.
- [ ] **7.4** 7.4: boats/minecarts/vehicles and rail-geometry dependency on Phase2.
- [ ] **7.5** 7.5: passengers, mounts, attachments, leash/tethers and forces.
- [ ] **7.6** Отдельная acceptance: physical-world velocities consistent, rotated-face controls, seams, replication.

## Критерии и зависимости

Рабочее движение игрока не является общей приёмкой Phase7.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
