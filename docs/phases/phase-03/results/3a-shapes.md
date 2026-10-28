# Phase 3A — итог исследований: VoxelShape, контекст BlockGetter и кеши

## 2026-10-29 — 3R-7F-3 G03/G04 research test and report, validation pending

[Detailed stage report](../../../research/PHASE3_R7F3_DYNAMIC_FACE_CACHE_AND_INDIRECT_GEOMETRY_CALLER_1_21_1.md) · [new exact-research Java 21 source](../../../../src/test/java/dev/planetary/world/Phase3R7F3DynamicFaceAndIndirectRootsResearchTest.java) · [CI #40778087503](https://github.com/Abbygree11/Planetary/actions/runs/40778087503) pending. Scope: original Block.shouldRenderFace and BlockStateBase selected JVM owners; current Planet state-only culling cache mock-context-change reproducibility; 543 direct geometry named calls classified 1:1, entire original 9430 classfiles scanned for invokedynamic method-handle candidates and suspicious string literals (not actual reflection proof); foreign getter/virtual Level provenance still distinct. **No production changes. R OPEN/V OPEN.** Gate remains six R blockers until successful CI and reviewed artifacts; 46 source guard rows remain.


## 2026-10-29 — `3R-7` whole-phase owner audit: 3A source disposition

The [151-owner audited matrix](../../../research/PHASE3_R7_ALL_151_OWNER_SOURCE_GATE_DISPOSITION_1_21_1.tsv) contains **41 Phase3A rows**. [Full 3R-7 decision and evidence hierarchy](../../../research/PHASE3_R7_WHOLE_PHASE_OWNER_SOURCE_GATE_RECONCILIATION_1_21_1.md) establishes **G03 A17/A35** (Planet pair-key dynamic face culling: the previously reproduced moving-piston *collision* is not proof of occlusion cache staleness), **G04 A25** (finite classification of remaining original non-block/virtual geometry consumer roots after 9430 compiled classes/543 discovered edges) as source gate blockers. Earlier `A24 OPEN_FULL_ENGINE_CALLER_CENSUS` is partly superseded by later A25 original scan; do **not** rerun the complete 9430-class census blindly, nor declare virtual/modded dispatch complete. Do not expand physical XYZ globally; retain world/section source/foreign fake Level provenance. Reconcile 41 original rows individually before full R PASS. Loaded mods and 6/12/8 +Y/noPlanet remain **V pending**, not evidence that exact original source research failed. **R OPEN/V OPEN**; next focused source packet `3R-7F-1` for B05/C05, then G03/G04 `3R-7F-3`.

**Состояние:** выбранные оригинальные контракты и большой реестр 3R-1/2B…2M собраны; **целостная Phase 3A source/mod/gate ещё не принята**, вся Phase3 R = OPEN. Исследование ≠ реализация ≠ игровая приёмка.

## Установленные результаты

1. **Базовые авторы и шесть форм.** Отдельно исследованы original BlockStateBase/Block.getShape, getCollisionShape, getVisualShape, getInteractionShape, getBlockSupportShape, getOcclusionShape; исходные классы и наследуемые владельцы проверялись на Minecraft1.21.1/NeoForge21.1.215. Фактическая область vanilla: **241 зарегистрированный класс / 1060 Block ID**. Не считать «полный реестр vanilla» «полной поддержкой внешних модов». Источники: [2B](../../../research/PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.md), [2C](../../../research/PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.md).
2. **Динамический кеш — не безусловный.** [3R-2E](../../../research/PHASE3_R2E_REGISTERED_DYNAMIC_CACHE_MOD_PROVENANCE_1_21_1.md), [CI #40206261371](https://github.com/Abbygree11/Planetary/actions/runs/40206261371): **26 684 зарегистрированных ванильных BlockState**, из них **26 505** допускают исходный кеш, **179** не допускают (22 Block ID). Ключ только из состояния без Level/BlockPos/BE/времени не эквивалентен результату динамической формы.
3. **Разные потребители — разные источники координат.** BlockGetter.clip/ClipContext, collision, support FULL/CENTER/RIGID, occlusion/culling, moving piston и NeoForge IBlockStateExtension должны иметь собственные корректные physical vs local frames. Модовые BlockGetter и fake Level не обязаны совпадать с текущим Minecraft ClientLevel. [2I](../../../research/PHASE3_R2I_SUPPORT_PISTON_COLLISION_EXTENSION_DISPATCH_1_21_1.md), [2J](../../../research/PHASE3_R2J_DYNAMIC_SHAPE_AND_WORLD_PROVENANCE_1_21_1.md).
4. **Совместимые release-owners и альтернативы.** Оригинальный engine callsite census + release ABI для Create 6.0.10, Ponder 1.0.82, AE2 19.2.18 подтверждали конкретные типы/подписи, не loaded mod runtime: Create ContraptionWorld / VirtualRenderWorld и Ponder WrappedLevel имеют отдельную семантику мира; AE2 FacadeBlockAccess — BlockAndTintGetter, не Level. [2G](../../../research/PHASE3_R2G_ENGINE_GEOMETRY_CONSUMER_AND_COMPAT_1_21_1.md), [2H](../../../research/PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md) ([CI #40315164089](https://github.com/Abbygree11/Planetary/actions/runs/40315164089)).
5. **Передача контекста в рендер.** [2K](../../../research/PHASE3_R2K_RENDER_REGION_MODELDATA_WORLD_PROVENANCE_1_21_1.md) исследовал RenderRegionCache → RenderChunkRegion → SectionCompiler → ModelData и опасность неявного глобального ClientLevel fallback; [2L](../../../research/PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md) и [2M](../../../research/PHASE3_R2M_CLIENT_LEVEL_SWITCH_DISPATCHER_CANCEL_AND_PROVENANCE_1_21_1.md) показывают необходимость lifecycle/epoch и worker-границ.

## Архитектурные решения, которые нельзя потерять

- **Физический XYZ Minecraft и BlockPos не переименовывать глобально в локальную гравитацию.** Преобразовывать только семантическую сторону/геометрию в исходном владельце и перед правильным потребителем.
- Общий механизм/виртуальные вызовы/наследование вместо исключений для конкретного блока; сохранять NeoForge override и mod hooks.
- Отсутствие реального Level или неизвестный BlockGetter — причина сохранить безопасный no-rotation либо точную provenance policy, а не подменить его «последним client world».
- Кеш динамической формы не должен переживать смену world/pos/BE/time/model данных без доказанного ключа и invalidation.

## Дальнейшие требования

Нужна финальная проверка **всех** веток 3A по owner coverage, модовым virtual dispatch и фальшивым мирам в 3R-7, затем только при необходимости код владельца и реальные проверки 6 граней/12 рёбер/8 углов/+Y/noPlanet и модов. Самостоятельно закрывать 3A на основании 241/1060, отдельных тестов или 26 684 кеш-состояний нельзя.

**Сводный источник:** [whole-phase gate](../../../research/PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md) и [построчное состояние](../../../research/PHASE3_OWNER_COVERAGE_1_21_1.tsv), раздел 3A. Детали каждого пакета и оригинальные ZIP/hash/тесты находятся в ссылках выше и [индексе](README.md).
