# 2.4A: установка и свойства ориентации

**Статус:** 2.4A-1/2 typed APIs CI PASS; 2.4A-3 original-hit runtime bridge COMMITTED / CI PENDING / GAMEPLAY PENDING (not phase `PASS`). [Индекс фазы](../phase-02.md).

**Контекст:** После evidence-backed owner audit по соответствующим семьям; стадия не требует ждать *всю* вселенную классов, но не допускает слепых блоковых костылей. [Final 2.3C-1…19 scope gate](../../research/PHASE2_STAGE3C_1_TO_19_FINAL_SELECTED_ABI_SCOPE_GATE_1_21_1.md) — 19/19 selected exact ABI PASS, original 40 mechanisms accounted with 16 shared-only / 4 partial; source survey sufficiently scoped, implementation sequence 2.4A-1 onward. Runtime/UI/real mod and six-face acceptance OPEN.

**Источники:** [точка восстановления](../../research/PHASE2_RESEARCH_EXECUTION_CHECKPOINT_1_21_1.md), [P01–P40](../../research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md), [владельцы Stage3A](../../research/PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md), [roadmap](../../IMPLEMENTATION_PLAN.md).

## Независимые микропакеты (по ОДНОМУ на отдельный ответ)

- [x] **1.** За отдельный commit реализовать стабильный semantic API для physical hit vs canonical target orientation, оставив координаты мира физическими.
- [ ] **2.** В отдельных коротких итерациях прикрыть 6-way FACING, horizontal FACING, AXIS, half/attach/rotation16, front/top и replacement. **P09/P10 source + selected exact ABI evidence:** [C16 research](../../research/PHASE2_STAGE3C16_FRONT_AND_TOP_ROTATION16_STATE_TRANSFORMS_NEOFORGE_ASM_1_21_1.md). `FrontAndTop` 12 valid orthogonal pairs (not six), ROTATION_16 tangent segment (not direction enum), mod Create custom `POINTING` remains opt-in; preserve vanilla/NeoForge callbacks. CI alone != runtime implementation.
- [ ] **3.** Отдельный маленький пакет: item-authored state, альтернативная установка и override owners.
- [ ] **4.** Для каждого законченного family cluster: позитивный +Y, пять иных граней, seam tests, vanilla passthrough, ASM, затем CI.

## Условия завершения этой карточки

Каждая законченная family требует тесты+green CI; отметка GAMEPLAY PENDING до пользовательской проверки.

**2.4A-1 foundation:** [typed intent, target resolver and test contract](../../research/PHASE2_STAGE4A1_TYPED_PLACEMENT_INTENT_1_21_1.md). Code: `PlanetPlacementIntent`, `PlanetPlacementResolver`, `PlanetPlacementContextAdapter`, `PlanetPlacementIntentTest`. No gameplay/NeoForge Mixin behavior claimed; first CI had one mistaken null exception assertion, fixed in 893a093; repeat [CI #38313939066](https://github.com/Abbygree11/Planetary/actions/runs/38313939066) COMPLETED SUCCESS (compile, JUnit, registry).

**2.4A-2 typed API:** [property semantics and limits](../../research/PHASE2_STAGE4A2_TYPED_ORIENTATION_PROPERTIES_1_21_1.md), `PlanetOrientationProperties` and `PlanetOrientationPropertiesTest`. This does NOT close checklist 2: runtime author adapters remain open. 2.4A-1 manual user response: “Все ок”; record only no reported regression, not comprehensive 6/12/8 acceptance. New [CI #38315358373](https://github.com/Abbygree11/Planetary/actions/runs/38315358373) completed SUCCESS, new code compiled and full JUnit and registry checks green; runtime author adapters still OPEN.

**2.4A-3 runtime author bridge:** [new research/implementation packet](../../research/PHASE2_STAGE4A3_USEON_ORIGINAL_HIT_AUTHOR_ADAPTER_1_21_1.md), `PlanetOriginalHitSource`, `UseOnContextOriginalHitInvoker`, original-hit overload in `PlanetPlacementContextAdapter`, typed integration in `PlanetFrameApi.placementFrame(context)`, new JUnit/ASM tests. **CI PENDING; gameplay PENDING; item overriding authors not solved.**

### Checkpoint для следующего захода

- Last research prerequisite: `2.3C-19` [CI38311748785](https://github.com/Abbygree11/Planetary/actions/runs/38311748785) COMPLETED SUCCESS plus [final 19/19 report/ASM/P01–P40 scope gate](../../research/PHASE2_STAGE3C_1_TO_19_FINAL_SELECTED_ABI_SCOPE_GATE_1_21_1.md). **2.4A-1 implementation committed in this stage; CI [CI #38313939066](https://github.com/Abbygree11/Planetary/actions/runs/38313939066) PASS; runtime remains separately gated.** First implementation checkbox covers semantic API only.
- Последний рабочий commit: смотреть HEAD ветки `2.0`, не выдумывать SHA.
- **NEXT `кк` → audited property author implementations:** after checking 2.4A-3 CI, adapt coherent real declaring-owner getStateForPlacement/item override families without guessing mod algorithms; keep runtime, 6/12/8, original/retarget and loaded-mod proof gates separate.
- Если соединение оборвалось до коммита — эта работа считается незавершённой.
