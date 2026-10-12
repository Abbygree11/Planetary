# 2.4A: установка и свойства ориентации

**Статус:** TODO (не `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** После evidence-backed owner audit по соответствующим семьям; стадия не требует ждать *всю* вселенную классов, но не допускает слепых блоковых костылей.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [ ] **1.** За отдельный commit реализовать стабильный semantic API для physical hit vs canonical target orientation, оставив координаты мира физическими.
- [ ] **2.** В отдельных коротких итерациях прикрыть 6-way FACING, horizontal FACING, AXIS, half/attach/rotation16, front/top и replacement. **P09/P10 source + selected exact ABI evidence:** [C16 research](../../research/PHASE2_STAGE3C16_FRONT_AND_TOP_ROTATION16_STATE_TRANSFORMS_NEOFORGE_ASM_1_21_1.md). `FrontAndTop` 12 valid orthogonal pairs (not six), ROTATION_16 tangent segment (not direction enum), mod Create custom `POINTING` remains opt-in; preserve vanilla/NeoForge callbacks. CI alone != runtime implementation.
- [ ] **3.** Отдельный маленький пакет: item-authored state, альтернативная установка и override owners.
- [ ] **4.** Для каждого законченного family cluster: позитивный +Y, пять иных граней, seam tests, vanilla passthrough, ASM, затем CI.

## Условия завершения этой карточки

Каждая законченная family требует тесты+green CI; отметка GAMEPLAY PENDING до пользовательской проверки.

### Checkpoint для следующего захода

- Последний подтверждённый подпакет: **не зафиксирован в этой карточке**.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- Следующее действие: первый `[ ]` сверху. После успешного подпакета отметить `[x]`, ссылку на исходники/тест/CI, commit SHA и следующий шаг.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
