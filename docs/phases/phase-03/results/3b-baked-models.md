# Phase 3B — итог исследований: модели, квады, AO/освещение и грани

**Статус:** ограниченные исходные пакеты **3R-3B-1, 3R-3B-2, 3R-3B-3** завершены по выбранному original ABI и CI; **вся Phase3 R = OPEN, реальные визуальные тесты V = OPEN**. Пакеты 3B не доказывают устранение дефекта, увиденного в игре.

## Доказанные механизмы

- **Слои авторов:** BlockState/владельцы формы ≠ BakedModel.getQuads (обычный и NeoForge с ModelData/RenderType) ≠ BakedQuad направление/нормаль/UV/tint ≠ ModelBlockRenderer AO/flat ≠ SectionCompiler и chunk rebuild. [3R-3B-1 original target bytecode](../../../research/PHASE3_R3B1_BAKED_MODEL_QUAD_CULLING_TARGET_JVM_1_21_1.md), [CI #40418106623 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40418106623): **17/17 оригинальных классов, 539 объявленных методов, 183 поля, 135 отобранных JVM call sites**.
- **Отсечение граней:** Block.shouldRenderFace и соседние BlockState.getFaceOcclusionShape; внешние NeoForge hooks hidesNeighborFace / supportsExternalFaceHiding нельзя обходить глобальным замещением. Особо проверяются несовпадения physical cull side и canonical source-model side.
- **Два механизма освещения:** vanilla ModelBlockRenderer с AO и flat, а также NeoForge SmoothQuadLighter / FlatQuadLighter с оригинальными нормалями, соседями и ModelData. [3R-3B-2](../../../research/PHASE3_R3B2_ORIGINAL_AO_QUAD_SEAM_MOD_OWNER_1_21_1.md), с исходной матрицей [AO/culling/mod](../../../research/PHASE3_R3B2_QUAD_AO_CULLING_LIGHT_MOD_OWNER_MATRIX_1_21_1.tsv). Не изменять физические LevelLightEngine, BlockPos, skylight lookup ради локального «up».
- **Развёртка и вложенные модели:** [3R-3B-3](../../../research/PHASE3_R3B3_ORIGINAL_MODEL_UV_TINT_RENDERPASS_OWNER_1_21_1.md), [CI #40430136142 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40430136142): **26 оригинальных классов, 342 метода, 1296 вызовов, 78 выбранных тел / 1964 инструкций**. FaceBakery/UV lock, sprite, BlockFaceUV, MultiPartBakedModel и WeightedBakedModel, BakedModelWrapper и NeoForge delegate getQuads/getModelData/getRenderTypes/getRenderPasses требуют сохранения текстурной семантики и порядка проходов.
- **Выбранные regression tests:** synthetic 4-vertex baked quads на 6 гранях проверяют сохранение UV0/COLOR/UV2, tintIndex, sprite, shade/AO и модели с ModelData/RenderType. Это JUnit, **не настоящая отрисовка в Minecraft**. Подробный тест [Phase3R3B3PackedQuadUvTintContractTest.java](../../../../src/test/java/dev/planetary/client/render/Phase3R3B3PackedQuadUvTintContractTest.java).

## Реальный блокирующий дефект

Пользователь обнаружил **неполную/асимметричную боковую текстуру на рёбрах кубической планеты**; пример — трава, но дефект относится к произвольным полным/неполным и модовым блокам. У него **не доказана** первопричина; отличать отсутствие квадов, неверную сторону, неверную UV/normal, culling, свет/AO/шейдер, stale chunk rebuild. Исправление должно быть **общим** для владельцев геометрии; категорически не GrassBlock-only и не хаотическая инъекция каждому блоку.

## Что всё ещё OPEN

Реальная отрисовка на 6 гранях, обоих концах всех 12 рёбер и 8 углах, соседние и несовпадающие блоки, chunk boundaries и rebuild, в обычном мире +Y/noPlanet, ModelData updates, AE2 facades/glass/cables и другие **загруженные** модовые модели. Shaderpack/Iris/Flywheel — отдельно 3D; LIME/CYAN знаки и цепь — 3C/Phase2 V. Ни один исходный CI не закрывает игровой visual gate V.

**Ссылки:** [3B-1](../../../research/PHASE3_R3B1_BAKED_MODEL_QUAD_CULLING_TARGET_JVM_1_21_1.md), [3B-2](../../../research/PHASE3_R3B2_ORIGINAL_AO_QUAD_SEAM_MOD_OWNER_1_21_1.md), [3B-3](../../../research/PHASE3_R3B3_ORIGINAL_MODEL_UV_TINT_RENDERPASS_OWNER_1_21_1.md), [Phase2 visual gate](../../../research/PHASE2_VISUAL_READINESS_GATE_1_21_1.md). Следующий пакет **3R-5**, затем 3R-6/7 — не считать research R закрытым сейчас.
