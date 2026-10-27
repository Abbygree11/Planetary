# Phase 1.3–1.4 — результаты: support sides, capabilities, foreign contexts

**Статус Phase1 PARTIAL; source-design constraints, без нового runtime/game теста.**

### Установленные границы

- **Семантический BlockState**: ванильные значения `UP`, `DOWN`, `FACING`, `AXIS` интерпретируются в canonical block-local frame Planet. Домены Vanilla BlockState не расширять и не переделывать для совместимости с original Model/BakedModel/mod state. Точка преобразования — между потребителем и автором алгоритма, а не всем `Direction` глобально.
- **Support query и Capability side**: context может означать локальную сторону механизма, но физические запросы `BlockPos` и соседям остаются в мировой системе. `PlanetSidedQueryFrame` задаёт аналогию для NeoForge-механизмов сторон без требования знать каждый модовый блок. Следует явно разделять **physical query side**, **target-local side** и source frame объекта.
- **Foreign contexts**: Create `ContraptionWorld` / `VirtualRenderWorld`, Ponder `WrappedLevel` и AE2 `FacadeBlockAccess` показаны в [released binary audit](../../../research/PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md), но источник координат чужого `Level` не всегда совпадает с активным `Minecraft.getInstance().level`. `BlockAndTintGetter` не обязательно `Level`.
- **Безопасная стратегия**: единая чистая Java топология/семантические преобразования, узкие точки входа для vanilla/NeoForge/модов. Не ломать noPlanet, не ротировать vanilla state caching по глобальному положению, не переиспользовать world-specific cache между измерениями.

### Первоисточники

[BLOCK_FRAME](../../../research/BLOCK_FRAME_1_21_1.md) · [EXTERNAL_COORDINATE_MOD_CASE_STUDIES](../../../research/EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md) · [PORTABILITY_STRATEGY](../../../research/PORTABILITY_STRATEGY.md) · [PHASE3 3R-2H](../../../research/PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md). Подробные выбранные CI по 3R-2H доказывают проверенные релизные классы, **не** реальный модовый интеграционный клиент или всю Phase1.

### Обязательные будущие доказательства

1. Контракты effective support side / NeoForge capability для всех ориентаций, наследников/переопределений и разных adjacent faces.
2. Реальная foreign context provenance и сочетание модов, не допускающее fallback на не тот client world.
3. На +Y/noPlanet всё ванильное поведение, в Planet — 6 граней/12 рёбер/8 углов и world-switch/worker release; PASS только после юнит + Minecraft клиент/сервер доказательств.

[План Phase1](../../phase-01.md) и [SESSION_HANDOFF](../../../SESSION_HANDOFF.md) определяют следующую реализацию. Phase1 **PARTIAL**, не GAME PASS.
