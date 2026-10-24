# Фаза 3 — Геометрия и рендеринг блоков

**Статус из роадмапа:** PARTIAL. Это не отметка об игровой приёмке. [Индекс](README.md) · [Основной роадмап](../IMPLEMENTATION_PLAN.md) · [Правила](../../AGENTS.md).

**Temporary visibility aid, not a renderer fix (2026-10-15):** In dev `runClient`, `PlanetDevVisibility` uses vanilla NIGHT_VISION on all six Planet faces; `/planetary test light off` reveals the underlying real lighting. This does **not** implement sky direction adaptation, local skylight, AO, shadow or mod shader pipelines. Real Phase 3B lighting acceptance must be tested with this helper OFF as well.

**3.3 / 3C-1 — BER sign/banner/skull rendering (2026-10-18): CODE+CI PASS / GAMEPLAY PENDING.** Implemented shared `PlanetLocalBlockEntityRender` block-center canonical->physical pose and four thin exact-owner Mixins for SignRenderer, HangingSignRenderer, BannerRenderer and SkullBlockRenderer (including standing/wall variants on their original renderer paths). Source/NeoForge/mod bypass 4/4: [stage packet](../research/PHASE3_STAGE3C1_BER_SIGN_BANNER_SKULL_1_21_1.md). [CI #39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) **SUCCESS**, code [ea04238](https://github.com/Abbygree11/Planetary/commit/ea0423894e2c1e92a5d74e81c87543be48bd9453). Client applied Mixin, 6 faces/12 edges/8 corners and loaded mod JAR **PENDING**. V8 42-station lab remains unchanged; +Y rendering is identity and any defect there needs separate reproduction. Global BER dispatcher, BEWLR item, custom Flywheel/world/camera effects NOT rotated.

## Research track 3R — whole-phase audit BEFORE further Phase3 runtime

**Active first checkpoint `3R-1` (2026-10-24): SELECTED 3A SOURCE PATHS TRACED / ALL-PHASE RESEARCH STILL OPEN.** [3R-1 call-flow and boundary research](../research/PHASE3_R1_3A_SHAPE_CALLPATH_AND_COVERAGE_1_21_1.md) inventories `BlockStateBase` cached/dynamic shape queries; `ClipContext/BlockGetter` raycasts; `BlockCollisions` and full-cube identity; support FULL/CENTER/RIGID; physical-versus-local occlusion; Shape math and adjacent 3B renderer consumer. It **does not** enumerate all registered shape overrides nor every rendering owner. Source comparator pinned, selected historical C11 NeoForge 21.1.215 ABI referenced honestly. [Phase-wide coverage TSV](../research/PHASE3_OWNER_COVERAGE_1_21_1.tsv) distinguishes SOURCE_TRACE_SELECTED, PRIOR_R2_ONLY, OPEN_CENSUS and provisional 3C-1 CI, never blindly promotes the old R2 COMPLETE marker.

**Named research batches:** `3R-2` — exhaustively enumerate registered 3A class/ID override/virtual dispatch; `3R-3` — BakedModel, culling, AO/light/render pass/cache/connected mod owners; `3R-4` — complete registered BER and alternate owner census (including provisional 3C-1); `3R-5` — accelerated/custom/NeoForge/Flywheel/Iris render pipelines; `3R-6` — resource/client worker, entity and preview lifecycle; `3R-7` — close coverage gaps, exact selected target-version ABI, unified mod/owner/bypass matrix and ACCEPTANCE contract. Each stage is a bounded **research-only 4/4 commit**, no Minecraft runtime change and no unnecessary new CI; `3R-7` can only mark research gate complete when *every* row has sufficient source and ABI evidence and all bypasses are inventoried. All 6/12/8 and loaded-mod gameplay acceptance remain separately pending.

## Mandatory Phase-3 whole-phase research gate — NOT YET PASSED (2026-10-18)

**Process correction:** user explicitly permits jumping between roadmap phases to fix a blocker, but requires the **entire destination phase to have first been researched as completely as Phase 2**. An audit of four Sign/HangingSign/Banner/Skull BER declaring owners (`3C-1`) and Phase-2's selected C11–C15 renderer notes are **not** a full source/NeoForge/mod bypass inventory of Phase 3. Do not continue Phase-3 runtime implementation (Chest/EnderChest/etc) yet; instead finish Phase-3 **research-only** preparation following [whole-phase source coverage gate](../research/PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md), or resume already-researched 2.4A work until that gate is met.

The already written `3C-1` renderer change remains a **PROVISIONAL code checkpoint** [ea04238](https://github.com/Abbygree11/Planetary/commit/ea0423894e2c1e92a5d74e81c87543be48bd9453), [CI #39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) SUCCESS, **actual client/Mixin/mod/visual acceptance PENDING**. Its green CI does not retrospectively authorize a jump, demonstrate all of Phase 3 is analyzed or close original `2.4A-10` gameplay. No automatic revert or additional fixes without evidence.

## Короткие самостоятельные пакеты

- [ ] **3.1** 3A: аудит формы outline/collision/visual/interact и seam/occlusion caches; короткая отдельная матрица.
- [ ] **3.2** 3B: BakedModel quad orientation, culling, AO, lighting, grass edge; отдельная группа доказательств.
- [ ] **3.2S — ОБНАРУЖЕННЫЙ GAMEPLAY BUG, ОБЩИЙ МЕХАНИЗМ:** неполная/асимметричная отрисовка боковых граней **произвольных блоков** на рёбрах и стыках гравитационных граней кубической планеты. Скриншот пользователя 2026-10-13 иллюстрирует дефект на блоках с травой: боковая визуальная текстура/окантовка проявляется на одной стороне, но отсутствует на другой. Это **не GrassBlock-only проблема** и не основание для блокового костыля.
  **Владелец: Phase 3B**, а не 2.4A-4. Исследовать весь исходный NeoForge render pipeline: ModelBlockRenderer, BakedModel.getQuads (обычный/ModelData/RenderType/случайные квады), BakedQuad.direction/normal/UV/tint/overlay, source/neighbor canonical frames, Block.shouldRenderFace + NeoForge external face hiding + occlusion shapes, AO/light/shade, client chunk rebuild/cache. Различать отсутствие квада, ошибочную его сторону, прозрачность/UV, tint/освещение и culling: причина ПОКА НЕ ДОКАЗАНА.
  **Объём:** любые полные/неполные, асимметричные, многотекстурные, прозрачные, направленные блоки и разные соседние состояния на обоих концах seam; стандартные модели модов по общему BakedModel API. Независимые custom/Flywheel/shader/foreign-world пути — отдельные явно отслеживаемые интеграции Phase 3D/10, не выдуманная поддержка. Сохранить физические BlockPos/Direction и NeoForge hooks; никаких GrassBlock-only исключений.
  **Приёмка:** доказательства оригинального declaring-owner и точной версии NeoForge 21.1.215, общие тесты/ASM/CI для изменённых границ; игровая матрица 6 граней, 12 рёбер, 8 углов, обе видимые боковины одного и разных соседних блоков, обычный +Y/no-Planet, чанковые границы/rebuild, реальный модовый набор. Статус REPORTED / ROOT_CAUSE_UNCONFIRMED / FIX_PENDING / GAMEPLAY_PENDING.
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
