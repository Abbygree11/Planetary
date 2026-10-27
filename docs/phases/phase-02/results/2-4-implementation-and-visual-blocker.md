# Phase 2.4 — результаты реализации и текущий блокер визуальной приёмки

**ACTIVE / PARTIAL**, текущая реализация Phase2 в ветке 2.0, исходный пакет `2.4A-10` **ПРИОСТАНОВЛЕН** до Phase3 исследовательского R и минимального общего runtime визуального исправления с реальным gate V.

### Что уже есть

- В коде существуют placement/state/neighbor/canonical frame адаптеры и contract JUnit/ASM для выбранных owner families. **CI PASS на отдельных коммитах не означает, что вся Phase2 визуально или игровым клиентом принята.**
- Независимый **проблемный случай табличек и цепей**: естественно сгенерированные vs вручную установленные объекты, LIME natural vs CYAN forced, attachment/rotation16, грани -Y/X/Z. Правильный BlockState FACING не равен правильной BER геометрии, тексту и цепям.
- Отдельная **общая ошибка боковых квадов на рёбрах/углах** (трава — показательный пример, не первопричина). Не чинить GrassBlock, Sign или один железный/деревянный вариант вместо общего кода. Проведены необходимые original-source пакеты 3R-3B и 3R-4A…D, **визуальный баг ещё не признан исправленным**.
- Предыдущие отдельные уже принятые пользовательские тесты частиц сохранять, не переделывать V8 test lab ради новых задач.

### Где кодовый статус и доказательства

[2.4A active packet](../../phase-02/05-implementation-a.md), [Phase2 original research checkpoint](../../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [Phase2 visual readiness gate](../../../research/PHASE2_VISUAL_READINESS_GATE_1_21_1.md), [Phase3 3B result](../../phase-03/results/3b-baked-models.md), [3C BER result](../../phase-03/results/3c-block-entity-renderers.md), [SESSION_HANDOFF](../../../SESSION_HANDOFF.md). Значения pass/fail строго проверять по живой ветке/actual Actions.

### Условия возврата к 2.4A

1. **3R-5** независимые mod/render/GPU owners → **3R-6** world/worker/reload → **3R-7** whole Phase3 owner research R (не дробить каждый будущий 2.4A blocker на новое неизученное исследование).
2. После R PASS создать **общий** Phase2-facing рендеринг для реальных недочётов — без глобального BER/PoseStack/Direction.
3. Проверить Minecraft live 6 faces/12 edges/8 corners, vanilla +Y/noPlanet, natural vs placed, texture/shader/resource reload, действительно загруженные совместимые моды. **V OPEN до этого**.
4. Только после этого возобновлять последовательность 2.4A, 2.4B/C/D и Phase2.5.

**Игровой pass не присваивать по source/JUnit.**
