# 2.4D: использование, огонь, преобразования состояния

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** Зависит от актуальных item/interaction owners и placement support.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** Маленькими пакетами закрыть BlockItem/useOn, replacement-state authoring, server/client interaction и placement states.
- [ ] **2.** Огниво/огонь: сначала локальная установка, затем отдельный handoff PortalShape владельцу Phase9.
- [ ] **3.** rotate/mirror/structure templates: canonical property transform vs physical structure rotation; запрет двойного поворота.
- [ ] **4.** Сохранить NeoForge hooks, gameplay packet validation and explicit cross-phase boundaries.

## Условия завершения этой карточки

Инварианты end-to-end useOn и state transformation подтверждены, portal geometry не объявлена Phase-2 PASS.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
