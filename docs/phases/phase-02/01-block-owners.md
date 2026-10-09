# 2.3A-1: реестр block effective owners — начало

**Статус:** NEXT / IN PROGRESS (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Stage 3A ACTIVE: 22 owner clusters описаны, остальные REVIEW_PENDING. Не считать 241/241 принятыми.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [x] **1.** Сверить фактические источники PHASE2_EFFECTIVE_OWNER_FINDINGS и PHASE2_STAGE3A_OWNER_FINDINGS с NeoForge registry-derived типами. **DONE (2026-10-10)**: оригинальный CI ZIP `11643813158`, run `37988064055`, 1060 block IDs / 241 concrete classes; проверены 22 опубликованных owner-кластера (НЕ 241 семантический аудит), 103 `BlockPlaceContext` owners vs 104 owners при включении перегрузок. Подробности: [Stage 3A owner findings](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md#2026-10-10-verified-reconciliation-of-original-ci-artifact).
- [x] **2.** Выбрать 8–15 ещё не разобранных effective-owner классов **одного семейства** и проверить каждый call-path: placement, canSurvive, updateShape, tick, interaction (применимость), overrides. **DONE (2026-10-10):** 12 concrete BushBlock descendants, 18 block IDs, comparative Java source + exact-signature NeoForge registry-owner join; [source research](../../research/PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md). **ASM/NeoForge patch and gameplay still pending; no runtime change.**
- [x] **3.** Сохранить в PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv первые evidence-backed строки и добавить ссылку на source/owner; неизвестное оставить REVIEW_PENDING. **DONE (2026-10-10):** [241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) + [evidence/status guide](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_GUIDE_1_21_1.md): 12 reviewed / 229 pending; 18/1060 IDs attached to reviewed classes; bytecode and gameplay still pending.
- [ ] **4.** Повторить такой маленький набор в СЛЕДУЮЩЕЙ итерации, не в одном гигантском ответе.

## Условия завершения этой карточки

8–15 подтверждённых owner dispositions с проверяемыми ссылками/примечаниями; честный счёт выполнено/осталось; обновлённый checkpoint.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **3/4 — committed first owner-disposition ledger** (2026-10-10), 12/241 classes SOURCE_REVIEWED_INTEGRATION_PENDING, 229/241 REVIEW_PENDING, exact 1060 registry ID counts. Source research and actual registry dispatch reconciled; bytecode/gameplay still pending.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: **подпакет 4/4** — выбрать и глубоко исследовать следующую небольшую группу из 8–15 concrete classes одного owner-семейства (например, SaplingBlock/derived vegetation, но сначала проверить реальную registry owner группу). После изучения исходников и путей установки/выживания/роста/обновления обновить TSV-диспозиции и evidence notes. 229 ещё не проверенных классов не считать DONE. Каждый новый пакет — отдельный коммит, переход к следующей карточке только после сохранённого checkpoint.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
