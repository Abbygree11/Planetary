# Planetary — результаты исследования Phase 3 (3A–3E)

**Рабочая ветка:** 2.0. **Цель:** Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21. **Состояние на 2026-10-28:** исходное исследование всей Phase 3 **R = OPEN**, игровая визуальная приёмка Phase 2 **V = OPEN**. **Следующий полный пакет: 3R-6**.

Это **долговременный архив выводов**, дополняющий [план Phase 3](../../phase-03.md), а не замена подробных технических исследований. Когда 3R-7 закроет исследовательский gate, будущая реализация должна начинаться отсюда, затем следовать в оригинальные отчёты, таблицы и исходники тестов. Не восстанавливать результаты только по истории чата.

## Одна карточка результатов на каждую часть фазы

| Часть | Что хранится в итоговом файле | Состояние на указанную дату |
| --- | --- | --- |
| **3A** | [VoxelShape, поддержка, физический/локальный контекст, кеш, моды](3a-shapes.md) | Выбранные исследования 3R-1/3R-2b…m проведены; вся область и динамические обходы ещё требуют сквозной сверки |
| **3B** | [BakedModel, квады, culling, AO, освещение, текстуры, шейдерные проходы](3b-baked-models.md) | 3R-3B-1/2/3 source/selected ABI закончены, игровой seam-кейс открыт |
| **3C** | [BER, наследование, выбор провайдера, NeoForge, AE2/Create](3c-block-entity-renderers.md) | 3R-4A/B/C/D research bounded PASS, реальные модовые рендеры не приняты |
| **3D** | [GPU instancing, Create/Flywheel, нестандартные render paths, Iris](3d-alternate-pipelines.md) | `3R-5` selected NeoForge static/source 4/4 PASS CI #40594978070, точный Flywheel/Iris runtime OPEN |
| **3E** | [ClientLevel, resource reload, ModelData, section workers, эпохи](3e-client-lifecycle.md) | 3R-2k/l/m отдельные исследовательские части PASS; отдельный 3R-6 и весь lifecycle OPEN |

## Иерархия первоисточников

1. **План:** [docs/phases/phase-03.md](../../phase-03.md), где указаны требуемые исследования, допуски и зависимости.
2. **Итог по подсистеме:** эти пять результатов — краткие проверяемые выводы, архитектурные ограничения и нерешённые задачи.
3. **Детали и проверяемые доказательства:** [PHASE3_FULL_OWNER_AUDIT_GATE](../../../research/PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md), [PHASE3_OWNER_COVERAGE_1_21_1.tsv](../../../research/PHASE3_OWNER_COVERAGE_1_21_1.tsv), отдельные PHASE3_R* отчёты и таблицы; точные Java/ASM JUnit, скрипты и GitHub CI/ZIP SHA внутри отчётов.
4. **Текущее действие:** [SESSION_HANDOFF](../../../SESSION_HANDOFF.md) и фактический HEAD 2.0. Если краткая карточка и свежий код различаются, решает исходный код/оригинальный тест/CI, а не память.
5. **Игровой допуск:** отдельный [PHASE2_VISUAL_READINESS_GATE](../../../research/PHASE2_VISUAL_READINESS_GATE_1_21_1.md), не подменяемый исследованиями или JUnit.

**Статусная дисциплина:** SOURCE/RELEASE-SOURCE ≠ original compiled TARGET ABI ≠ JUnit CI ≠ applied Mixin ≠ client/game visual ≠ loaded Create/AE2/Iris. Даже «4/4 PASS» означает законченный **ограниченный исследовательский пакет**, а не всю Phase 3. Новые утверждения добавлять с этапом, точной версией, ссылкой на файл/тест, CI/артефакт и незакрытым риском.

## Реестр выполненных исследований и что не следует терять

- **3A:** [3R-1](../../../research/PHASE3_R1_3A_SHAPE_CALLPATH_AND_COVERAGE_1_21_1.md); [2B](../../../research/PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.md), [2C](../../../research/PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.md), [2D](../../../research/PHASE3_R2D_DYNAMIC_SHAPE_CACHE_OCCLUSION_SUPPORT_1_21_1.md), [2E](../../../research/PHASE3_R2E_REGISTERED_DYNAMIC_CACHE_MOD_PROVENANCE_1_21_1.md), [2F](../../../research/PHASE3_R2F_COMPILED_SHAPE_CONSUMER_AND_MOD_VERSION_AUDIT_1_21_1.md), [2G](../../../research/PHASE3_R2G_ENGINE_GEOMETRY_CONSUMER_AND_COMPAT_1_21_1.md), [2H](../../../research/PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md), [2I](../../../research/PHASE3_R2I_SUPPORT_PISTON_COLLISION_EXTENSION_DISPATCH_1_21_1.md), [2J](../../../research/PHASE3_R2J_DYNAMIC_SHAPE_AND_WORLD_PROVENANCE_1_21_1.md), [2K](../../../research/PHASE3_R2K_RENDER_REGION_MODELDATA_WORLD_PROVENANCE_1_21_1.md), [2L](../../../research/PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md), [2M](../../../research/PHASE3_R2M_CLIENT_LEVEL_SWITCH_DISPATCHER_CANCEL_AND_PROVENANCE_1_21_1.md).
- **3B:** [3B-1](../../../research/PHASE3_R3B1_BAKED_MODEL_QUAD_CULLING_TARGET_JVM_1_21_1.md), [3B-2](../../../research/PHASE3_R3B2_ORIGINAL_AO_QUAD_SEAM_MOD_OWNER_1_21_1.md), [3B-3](../../../research/PHASE3_R3B3_ORIGINAL_MODEL_UV_TINT_RENDERPASS_OWNER_1_21_1.md).
- **3C:** [4A](../../../research/PHASE3_R4A_ORIGINAL_VANILLA_BER_REGISTRY_AND_OWNER_1_21_1.md), [4B](../../../research/PHASE3_R4B_SIGN_BANNER_SKULL_NESTED_RENDER_SOURCE_1_21_1.md), [4C](../../../research/PHASE3_R4C_REMAINING_BER_ORIGINAL_NESTED_SPACE_AND_MOD_1_21_1.md), [4D](../../../research/PHASE3_R4D_NEOFORGE_BER_PROVIDER_REGISTRATION_MOD_OWNER_1_21_1.md).
- **3D/3E:** `3R-5` selected research done, `3R-6` next. Используются также [3R-2K](../../../research/PHASE3_R2K_RENDER_REGION_MODELDATA_WORLD_PROVENANCE_1_21_1.md), [3R-2L](../../../research/PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md), [3R-2M](../../../research/PHASE3_R2M_CLIENT_LEVEL_SWITCH_DISPATCHER_CANCEL_AND_PROVENANCE_1_21_1.md), но их предварительное наличие **не закрывает** 3R-6/3R-7 и весь 3D/GPU source/mod gate.

**Правило продолжения:** после каждого нового пакета 3R-5/6/7 обновлять только затронутые итоговые файлы, соответствующие PHASE3_R* detailed research + матрицу, gate R/V и SESSION_HANDOFF. При новых фактах корректировать устаревший вывод явно; не стирать прежние результаты и ссылки на первоначальные CI.
