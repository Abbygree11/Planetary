# Фаза 10 — Совместимость с другими модами

**Статус из роадмапа:** PARTIAL / CONTINUOUS GATE. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **10.1** Категория automatic: external consumers standard Vanilla/NeoForge hooks.
- [ ] **10.2** Категория mostly automatic: subclasses/overrides наших базовых механизмов — диспетчеризация и custom BlockState.
- [ ] **10.3** Категория explicit integration: нестандартные placement, BE, fluids, vehicle, coordinate-space mods. **Rail integration C17** [pinned actual Create mod independent TrackShape/TrackBlockItem/Bezier graph and NeoForge IBaseRailBlockExtension](../research/PHASE2_STAGE3C17_RAILSTATE_PHYSICAL_GRAPH_NEOFORGE_MOD_ASM_1_21_1.md). Create TrackBlock is NOT BaseRailBlock and cannot automatically inherit RailState fixes; only opt-in track graph adapter with real loaded mod JAR; no synthetic `Direction` / physical Level global remap.
- [ ] **10.3R** Explicit source-owned `Level`/section context for third-party `AddSectionGeometryEvent` and guidebook fake-world meshes, Create/Flywheel VirtualRenderWorld and Iris pipeline generation; do not bind render-region `BlockAndTintGetter` implicitly to `Minecraft.level`, and do not capture mutable ModelData on background worker. [C15](../research/PHASE2_STAGE3C15_RESOURCE_RELOAD_MODEL_CACHE_SECTION_GEOMETRY_WEATHER_SHADER_LIFECYCLE_ASM_1_21_1.md).
- [ ] **10.4** Public PlanetFrameApi, frame capabilities and contract tests; port compatibility and release notes.
- [ ] **10.5** Финальная кросс-модовая приёмка нескольких классов модов, но обязательный gate действует в каждой фазе.

## Критерии и зависимости

Это постоянная задача и критерий каждой стадии, не только финальная уборка.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.


### P26 third-party six-port mod graph vs MultifaceSpreader (2026-10-12 C18)

[Full P26 NeoForge/vanilla owner + pinned real Create FluidPipeBlock/AxisPipeBlock source audit](../research/PHASE2_STAGE3C18_MULTIFACE_VINE_SPREAD_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md): Create extends `PipeBlock` and shares six `PROPERTY_BY_DIRECTION` state flags but **does not** inherit `MultifaceBlock` and does not call `MultifaceSpreader`. Create `canConnectTo/updateShape/onWrenched/FluidTransportBehaviour` is a different physical connector/fluid algorithm. A universal `PipeBlock` or `Direction` mixin would break other mods/fake Levels; offer opt-in typed port adapter and independently verify truly compatible Create JAR. Real mod runtime tests remain NOT RUN.
