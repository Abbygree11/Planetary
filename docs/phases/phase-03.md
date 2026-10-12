# Фаза 3 — Геометрия и рендеринг блоков

**Статус из роадмапа:** PARTIAL. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

## Короткие самостоятельные пакеты

- [ ] **3.1** 3A: аудит формы outline/collision/visual/interact и seam/occlusion caches; короткая отдельная матрица.
- [ ] **3.2** 3B: BakedModel quad orientation, culling, AO, lighting, grass edge; отдельная группа доказательств.
- [ ] **3.3** 3C: BlockEntityRenderer general frame policy и отдельный специальный случай Chest/EnderChest/Enchanting book — без обмана с BlockState FACING.
- [ ] **3.4** 3D: moving block/accelerated render engines (Flywheel/Create), GPU instance/normal and current/previous-frame pose matrices; shaderpack shadow pass compatibility.
- [ ] **3.4S** Iris (NeoForge 1.21.1) shaderpack integration as a separate acceptance branch: GBuffer normals, shadow-map matrices, day/night/world-light vs gravity, PBR normal maps, transparent fluids, optional Flywheel-Iris compatibility addon, vanilla/no-Planet regression. Source audit: [C13](../research/PHASE2_STAGE3C13_ALTERNATE_BER_FLUID_INSTANCING_SHADERS_ASM_1_21_1.md).
- [ ] **3.4E** Player/Entity render, physical ground shadow/flame/hitboxes and nonliving vehicles/item entities: explicit render owner and local vs world camera frame, hand/item-frame RenderItemInFrameEvent, moving piston NeoForge ClientHooks, item GUI remains unrotated by player's world gravity. Source [C14](../research/PHASE2_STAGE3C14_CAMERA_ENTITY_ITEM_MOVING_WORLD_EFFECTS_ASM_1_21_1.md); Phase7 handles vehicle motion and interpolation.
- [ ] **3.4R** Resource/model lifecycle, typed `Level` and section-render provenance for worker callbacks, AO/culling model-data invalidation and epoch; native NeoForge `AddSectionGeometryEvent` renderer, shaderpack/texture atlas reload and dynamic BE geometry in independent mod + foreign preview world. [C15 source audit](../research/PHASE2_STAGE3C15_RESOURCE_RELOAD_MODEL_CACHE_SECTION_GEOMETRY_WEATHER_SHADER_LIFECYCLE_ASM_1_21_1.md).
- [ ] **3.5** Финальная Phase3 acceptance отдельно для static vs BER; не просить повторно тестировать принятые частицы.

## Критерии и зависимости

Phase 2 исправляет состояние, Phase 3 — визуальную/BE ориентацию; одна фаза не подменяет другую.

Каждый пакет выполняется в **отдельной короткой итерации**: source owner → механизм целиком → маленький artifact/commit → contract tests/CI (если изменён код) → отдельная игровая приёмка. Состояние проверяется по реальным evidence в `docs/research` и `docs/AI_CONTEXT.md`. Пункты выше являются маршрутной **декомпозицией**, а не заявлением, что все они не начинались: при первом заходе сверить с фактами и отметить только действительно закрытое. Перед началом нового пакета читать `AGENTS.md`.

## Resume checkpoint

**Следующий шаг:** первый пункт `[ ]`, который ещё не подтверждён исходниками или игровым результатом. По завершении добавить конкретный артефакт, SHA коммита, CI run (если применимо), следующий чекбокс; закончить ответ, не переходить к следующей фазе автоматически.


### P28 RailShape geometry/visualization handoff (2026-10-12 C17)

[RailState, NeoForge custom slope and source-owned Create TrackShape audit](../research/PHASE2_STAGE3C17_RAILSTATE_PHYSICAL_GRAPH_NEOFORGE_MOD_ASM_1_21_1.md): Phase2 owns local graph endpoints/shape selection, but Phase3 must separately rotate vanilla rail render quads/voxel hitboxes at all local faces/edges; custom rail `isValidRailShape` shapes may not match vanilla 10-value rendering assumptions. Create TrackBlock/TrackShape/Bezier model is independent; GPU/BER/rail-curve render must preserve physical coordinate provenance. Selected ASM contract is not visual acceptance. Check texture orientation, rise height, culling/light and no-Planet pass-through with real mod JAR only in later Phase3/10 tests.


### P26 multiface/vine render and shape handoff (2026-10-12 C18)

[Six-face MultifaceBlock vs five-face VineBlock orientation, support shapes and mod Create pipe comparison](../research/PHASE2_STAGE3C18_MULTIFACE_VINE_SPREAD_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md): source `MultifaceBlock` caches union of six face plates, VineBlock caches five face plates, GlowLichen has light and waterlog, SculkVein uses custom spread; even correctly transported BlockState flags do NOT automatically transform physical `VoxelShape`/AO/model or client-chunk materialization on Planet side faces. Phase3 must test face-culling/light/water/BER and fallback no-Planet world independently; avoid global render-space remap or applying Vine DOWN face that cannot exist.


### P29 head/body chain and source-specific model/shape handoff (2026-10-13 C19)

[C19 concrete 8 vanilla GrowingPlant subclasses and real BOP1.21.1 hanging-strand/moss variants](../research/PHASE2_STAGE3C19_GROWINGPLANT_HEAD_BODY_DIRECTED_GROWTH_NEOFORGE_MOD_ASM_1_21_1.md): local growth and head/body state conversion are NOT equivalent to physically reoriented Kelp/CaveVines/WeepingVines textures, voxel shapes, water transparency, hanging cobweb web/collision or worldgen visual; Phase3 separately must verify quads/culling/lighting and real mod models on six faces/seams. No rendered client or mod Jar tested by source/ASM.
