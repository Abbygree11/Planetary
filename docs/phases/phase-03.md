# Фаза 3 — Геометрия и рендеринг блоков

**Статус из роадмапа:** PARTIAL. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **3.1** 3A: аудит формы outline/collision/visual/interact и seam/occlusion caches; короткая отдельная матрица.
- [ ] **3.2** 3B: BakedModel quad orientation, culling, AO, lighting, grass edge; отдельная группа доказательств.
- [ ] **3.3** 3C: BlockEntityRenderer general frame policy и отдельный специальный случай Chest/EnderChest/Enchanting book — без обмана с BlockState FACING.
- [ ] **3.4** 3D: moving block/accelerated render engines (Flywheel/Create), GPU instance/normal and current/previous-frame pose matrices; shaderpack shadow pass compatibility.
- [ ] **3.4S** Iris (NeoForge 1.21.1) shaderpack integration as a separate acceptance branch: GBuffer normals, shadow-map matrices, day/night/world-light vs gravity, PBR normal maps, transparent fluids, optional Flywheel-Iris compatibility addon, vanilla/no-Planet regression. Source audit: [C13](../research/PHASE2_STAGE3C13_ALTERNATE_BER_FLUID_INSTANCING_SHADERS_ASM_1_21_1.md).
- [ ] **3.4E** Player/Entity render, physical ground shadow/flame/hitboxes and nonliving vehicles/item entities: explicit render owner and local vs world camera frame, hand/item-frame RenderItemInFrameEvent, moving piston NeoForge ClientHooks, item GUI remains unrotated by player's world gravity. Source [C14](../research/PHASE2_STAGE3C14_CAMERA_ENTITY_ITEM_MOVING_WORLD_EFFECTS_ASM_1_21_1.md); Phase7 handles vehicle motion and interpolation.
- [ ] **3.5** Финальная Phase3 acceptance отдельно для static vs BER; не просить повторно тестировать принятые частицы.

## Критерии и зависимости

Phase 2 исправляет состояние, Phase 3 — визуальную/BE ориентацию; одна фаза не подменяет другую.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.
