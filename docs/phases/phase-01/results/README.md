# Planetary — постоянные результаты Phase 1

**Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21 · branch 2.0 · статус Phase 1: PARTIAL.** Этот индекс дополняет [план Phase 1](../../phase-01.md), а не объявляет его закрытым. Пакеты 1.1–1.4 всё ещё имеют непроверенные контракты и игровые сценарии.

| Область | Итог |
| --- | --- |
| **1.1** | [Canonical block state frame и chart на 6 гранях](1a-frames-and-topology.md) |
| **1.2** | [Physical BlockPos, walk/step и transport across edges](1a-frames-and-topology.md) |
| **1.3** | [Support-query / capability sides и shared state semantics](1b-interfaces-and-acceptance.md) |
| **1.4** | [Моды, foreign worlds, смешанные контексты и game acceptance](1b-interfaces-and-acceptance.md) |

### Оригинальные документы с подробностями

[BLOCK_FRAME](../../../research/BLOCK_FRAME_1_21_1.md) · [BLOCK_WORLD_TOPOLOGY_MATRIX](../../../research/BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md) · [GRAVITY_MECHANISM_MAP](../../../research/GRAVITY_MECHANISM_MAP_1_21_1.md) · [EXTERNAL_COORDINATE_MOD_CASE_STUDIES](../../../research/EXTERNAL_COORDINATE_MOD_CASE_STUDIES.md) · [PORTABILITY_STRATEGY](../../../research/PORTABILITY_STRATEGY.md).

Файлы результатов фиксируют изученные механизмы, архитектурные решения и оставшиеся доказательства. **Оригинальные source/CI/game gates читать в исходном подробном документе**, а не выводить новые PASS по наличию файла. Следующий активный общий пакет остаётся **3R-5** для блокировки Phase2; этот архив не переводит Phase1 в DONE. [SESSION_HANDOFF](../../../SESSION_HANDOFF.md) и live HEAD имеют приоритет.
