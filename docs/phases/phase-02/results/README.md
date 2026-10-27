# Planetary — постоянные результаты Phase 2: 2.3A/B/C, 2.4 и приёмка

**Ветка 2.0 · Minecraft1.21.1 / NeoForge21.1.215 / Java21.** Исследовательские пакеты **2.3A, 2.3B, 2.3C закрыты в ограниченном source/selected ABI смысле**. **Реализация и игровая приёмка Phase2 НЕ завершены**; 2.4A-10 временно приостановлен из-за общего визуального blocker. Нельзя смешивать RESEARCH PASS и GAMEPLAY PASS.

| Раздел | Результат и доказательства |
| --- | --- |
| [2.3A](2-3a-registered-block-owners.md) | 241/241 оригинальных зарегистрированных block class, 1060/1060 Block IDs, P01–P40 механизмы |
| [2.3B](2-3b-item-and-alternate-writers.md) | 87/87 item class, 1333 items, 925 BlockItem и независимые авторы |
| [2.3C](2-3c-neoforge-abi-lifecycle.md) | 19 исходных NeoForge/source/ASM/CI семейства, 40 P-ID scope matrix |
| [2.4](2-4-implementation-and-visual-blocker.md) | Ориентация/placement/support частично реализованы, не закрыты sign/chain/BER/seam и общая V |
| [2.5](2-5-gameplay-acceptance.md) | Клиент, сервер, 6/12/8, +Y/noPlanet, реальные моды — итоговая игровая приёмка TODO |

Существующие планы и десятки семейных карточек **не удалялись**: [план Phase2](../../phase-02.md), [полный каталог 2.3A–2.5](../), [исследовательский checkpoint](../../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [orientation atlas P01–P40](../../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [scope matrix 2.3C](../../../research/PHASE2_STAGE3C_P01_P40_ABI_COVERAGE_DISPOSITION_1_21_1.tsv). Ссылки на оригинальные CI и тесты в файлах результатов и детальных исследованиях.

**Текущий следующий `кк` — 3R-5, затем 3R-6/7, только после R PASS минимальные shared Phase2 visual fixes и реальный V, затем 2.4A.** [SESSION_HANDOFF](../../../SESSION_HANDOFF.md) — текущая точка продолжения. Нет автоматической связи «2.3C source complete» → «2.4A game complete».
