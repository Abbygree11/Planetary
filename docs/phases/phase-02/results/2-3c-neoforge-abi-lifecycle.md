# Phase 2.3C — результаты selected NeoForge21.1.215 JVM и lifecycle audit

**Область:** **19 bounded source + selected exact ASM/JUnit CI packages (C1–C19)**, а не полный исходный метод-body аудит всех 241+87 классов и всех модовых обходов. [Итоговый selected ABI gate](../../../research/PHASE2_STAGE3C_1_TO_19_FINAL_SELECTED_ABI_SCOPE_GATE_1_21_1.md).

Исследованы исходные и NeoForge-контракты: `BlockItem`/placement contexts, multiblock/BE, physical neighbor updates, scheduled/random ticks, growth and fluid interaction, piston, redstone ports, occlusion/support, lighting/AO, shaders/entity/item/model, model reload; C16 front/top/rotation16, C17 rails, C18 multiface/vine, C19 GrowingPlant directional head/body. [C19 CI #38311748785 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/38311748785) (full source/test package end), другие CI/сигнатуры в каждом C1–C19 исходном отчёте.

**Построчный scope P01–P40 из 3C final gate:** 9 `SELECTED_SPECIFIC_ABI`; 11 `SELECTED_SHARED_ABI`; 16 `SHARED_ABI_ONLY`; 4 `PARTIAL_SHARED_ABI`; **0 известных SPECIFIC_TARGET_GAP среди специально отобранных шести прежних пробелов**. Это **не 40/40 полный охват per-owner ABI**. [P01–P40 disposition TSV](../../../research/PHASE2_STAGE3C_P01_P40_ABI_COVERAGE_DISPOSITION_1_21_1.tsv), [final gate](../../../research/PHASE2_STAGE3C_1_TO_19_FINAL_SELECTED_ABI_SCOPE_GATE_1_21_1.md).

**До implementation/game PASS:** точный Mixin weaving и реальная логика, overrides модов, естественная установка/падение/рост/мирогенерация, изменения BlockState во времени, client/server, six faces/edges/corners, baseline noPlanet, современная конкретная модовая среда. Каждый механизм — 4 части owner/source, dispatch/lifecycle, mods/bypass, exact JVM+CI с раздельной игровой приёмкой.
