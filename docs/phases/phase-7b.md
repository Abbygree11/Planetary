# Фаза 7B — Взаимодействия, сеть и окружение

**Статус из роадмапа:** RESEARCH COMPLETE / ACCEPTANCE PENDING. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **7B.1** 7B.1: physical eye/ray/hit geometry from body-local view, placement hit contract for Phase2.
- [ ] **7B.2** 7B.2: server-client prediction/packet validation/body pose and anti-desync; cases per packet owner.
- [ ] **7B.3** 7B.3: spawn placement & local ground/ceiling eligibility with Phase2/8 integration.
- [ ] **7B.4** 7B.4: rain/snow/weather, global heightmap vs local shell, sky/light/precipitation policy.
- [ ] **7B.5** Проверка sync/foreign-world behavior на шести гранях и seams.

## Критерии и зависимости

Не превращать глобальные хитбоксы и network coordinates в скрытую локальную систему.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
