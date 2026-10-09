# 2.3A-1: реестр block effective owners — начало

**Статус:** DONE **только для микропакетов 2.3A-1** (не фаза 2 и не все 241 класса). [Индекс фазы](../phase-02.md).

**Контекст:** Stage 3A ACTIVE: 22 owner clusters описаны, остальные REVIEW_PENDING. Не считать 241/241 принятыми.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [x] **1.** Сверить фактические источники PHASE2_EFFECTIVE_OWNER_FINDINGS и PHASE2_STAGE3A_OWNER_FINDINGS с NeoForge registry-derived типами. **DONE (2026-10-10)**: оригинальный CI ZIP `11643813158`, run `37988064055`, 1060 block IDs / 241 concrete classes; проверены 22 опубликованных owner-кластера (НЕ 241 семантический аудит), 103 `BlockPlaceContext` owners vs 104 owners при включении перегрузок. Подробности: [Stage 3A owner findings](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md#2026-10-10-verified-reconciliation-of-original-ci-artifact).
- [x] **2.** Выбрать 8–15 ещё не разобранных effective-owner классов **одного семейства** и проверить каждый call-path: placement, canSurvive, updateShape, tick, interaction (применимость), overrides. **DONE (2026-10-10):** 12 concrete BushBlock descendants, 18 block IDs, comparative Java source + exact-signature NeoForge registry-owner join; [source research](../../research/PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md). **ASM/NeoForge patch and gameplay still pending; no runtime change.**
- [x] **3.** Сохранить в PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv первые evidence-backed строки и добавить ссылку на source/owner; неизвестное оставить REVIEW_PENDING. **DONE (2026-10-10):** [241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) + [evidence/status guide](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_GUIDE_1_21_1.md): 12 reviewed / 229 pending; 18/1060 IDs attached to reviewed classes; bytecode and gameplay still pending.
- [x] **4.** Повторить такой маленький набор в СЛЕДУЮЩЕЙ итерации, не в одном гигантском ответе. **DONE (2026-10-10):** 12 новых concrete наследников `BushBlock` / 34 block IDs: [исследование](../../research/PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md), [обновлённый TSV](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv). Суммарно **24/241** классов с source-reviewed диспозициями; 217 REVIEW_PENDING. Без NeoForge ASM/gameplay PASS.

## Условия завершения этой карточки

8–15 подтверждённых owner dispositions с проверяемыми ссылками/примечаниями; честный счёт выполнено/осталось; обновлённый checkpoint.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **4/4 — source review ещё 12 Bush descendants (34 block IDs)** (2026-10-10), итог **24/241** source-reviewed, **217/241** REVIEW_PENDING, **52/1060** соответствующих block IDs, остальное **1008/1060**. Только исследовательский статус, игровой PASS не подтверждён.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: перейти к [**2.3A-2, пакет 1**](02-block-owners-rest.md) в СЛЕДУЮЩЕМ ответе. Выбрать 8–15 связанных concrete owners вне уже завершённых 24, подтвердить исходники, alternate author, source/target chart, обновить TSV и checkpoint отдельным коммитом.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
