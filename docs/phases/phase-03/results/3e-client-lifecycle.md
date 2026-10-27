# Phase 3E — итог предварительного исследования: ресурсный и клиентский lifecycle

**Состояние:** 3R-2K/2L/2M исследовали выбранные оригинальные места источников/перезагрузки/воркеров; **полный 3R-6 ещё не выполнен**. Phase3 R = OPEN; реальные долгоживущие клиентские тесты и Phase2 V = OPEN.

## Подтверждённые исходные пути

1. **RenderRegion не всегда ClientLevel.** [3R-2K](../../../research/PHASE3_R2K_RENDER_REGION_MODELDATA_WORLD_PROVENANCE_1_21_1.md), [CI #40339884955 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40339884955): RenderRegionCache создаёт RenderChunkRegion и сохраняет исходный Level/срезы; SectionCompiler использует getBlockState/getBlockEntity/getModelData и BakedModel.getModelData/getRenderTypes. Нельзя в worker frame восстанавливать гравитацию по глобальному Minecraft.getInstance().level без доказанной provenance.
2. **NeoForge ModelData snapshots имеют собственную точку записи/обновления.** [3R-2L](../../../research/PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md), [CI #40356263029 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40356263029): ModelDataManager.snapshotSectionRegion проверяет thread и собирает карты по SectionPos; отображения копируются, **не доказано глубокое копирование изменяемых ModelData**. requestRefresh/refreshAt используют соответствующий исходный Level и BE; RenderSection.RebuildTask захватывает render region, затем освобождает ссылку. Фактическая race-семантика в игре не доказана.
3. **Смена мира — не просто замена одного указателя.** [3R-2M](../../../research/PHASE3_R2M_CLIENT_LEVEL_SWITCH_DISPATCHER_CANCEL_AND_PROVENANCE_1_21_1.md), [CI #40407267093 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40407267093): исследованы оригинальные Minecraft.setLevel, LevelRenderer.setLevel, ViewArea и SectionRenderDispatcher; **16 оригинальных классов, 801 объявленный метод, 88 выбранных метод-body, 2182 original instructions, 429 invokes, 25 invokedynamic**. Обновление мира вызывает unload/reset, снятие ресурсов и отмену некоторых задач. Cancellation — кооперативная; callbacks, future completion, буферы и uploadAllPendingUploads не гарантированно исчезают мгновенно.
4. **BER factories различают регистрацию и instance creation.** [3R-4D](../../../research/PHASE3_R4D_NEOFORGE_BER_PROVIDER_REGISTRATION_MOD_OWNER_1_21_1.md), [CI #40553809343 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40553809343): RegisterRenderers.event регистрирует фабрики в BlockEntityRenderers Map.put, а Dispatcher.onResourceManagerReload создаёт новый renderer map, которым пользуются getRenderer/render/renderItem. Нужно различать **зарегистрированный provider**, **созданный renderer**, **старую instance ссылку** и **render call**.

## Архитектурные инварианты

- **Истинный owner мира/региона/контекста обязателен.** Разные ClientLevel/dimension/worldgen/Ponder/contraption GUI и ModelDataManager нельзя склеить по последнему globally active клиентскому миру.
- **Epoch/cancellation важны для адаптеров Planet.** После world unload, disconnect, смены dimension/resource reload, отмены section task, закрытия renderer, старые orientation caches/model snapshots/texture references не должны тихо переноситься в другой мир.
- **Граница render threading:** чтение состояния/BE/ModelData и sample physical neighbors может происходить в worker с snapshot; правила должны следовать оригинальным host objects без гонок, UI and fake-Level без принудительного world frame.
- **Эти инварианты — план безопасности реализации, не утверждение, что текущий Planet runtime уже безопасен.**

## Остаётся в 3R-6 — пока OPEN

Оригинальные и модовые reload listeners, pipeline event registration ordering, model/atlas/texture re-bake и renderer recreation, asynchronous section worker/thread callbacks, buffer GPU upload/fences, ModelData invalidation, произвольные live-mod callbacks, ClientLevel/feature flag, GUI/noLevel preview, foreign virtual worlds; контроль изоляции нескольких миров и mod interop. Обязательно связать R5 findings с каждой старой формой 3R-2K/L/M и последующей 3R-7 gate.

**Визуальная проверка отдельно:** снаряжённый клиент, реальный world switch/dimension return, chunk unload/reload и rebuild, shader/model resource pack reload, +Y/noPlanet and 6/12/8, модовый runtime; ни один из перечисленных исходных CI это не подменяет.

**Источники и открытые строки:** [2K](../../../research/PHASE3_R2K_RENDER_REGION_MODELDATA_WORLD_PROVENANCE_1_21_1.md), [2L](../../../research/PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md), [2M](../../../research/PHASE3_R2M_CLIENT_LEVEL_SWITCH_DISPATCHER_CANCEL_AND_PROVENANCE_1_21_1.md), [4D](../../../research/PHASE3_R4D_NEOFORGE_BER_PROVIDER_REGISTRATION_MOD_OWNER_1_21_1.md), [Owner ledger](../../../research/PHASE3_OWNER_COVERAGE_1_21_1.tsv). Следующий этап исследований: 3R-5 (3D), затем 3R-6 (3E), затем 3R-7 (вся Phase3).
