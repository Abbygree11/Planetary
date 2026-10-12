# 2.3B: item authors и альтернативная установка

**Статус:** ACTIVE / **2.3B-1 SOURCE RESEARCH COMPLETE (четыре внутренние проверки 4/4)**; full Stage 2.3B **NOT DONE**, compiled/Planet/gameplay/actual mod JAR gates **REVIEW_PENDING**. [Индекс фазы](../phase-02.md).

**Оригинальный baseline:** Minecraft 1.21.1 / NeoForge 21.1.215, immutable registry artifact11643813158/run37988064055 with 1333 ITEM IDs, 87 concrete registered ITEM classes, 925 BlockItem subclass IDs, 121 candidate item source files. The census is not proof 87 audited methods or mod compatibility.

## Пакеты / очередной checkpoint (каждый следующий пакет — все четыре внутренние проверки за один ответ)

- [x] **2.3B-1 — normal/variant/rewriting item placement**: comparative 1.21.1 source dispatch of `UseOnContext`/`BlockPlaceContext`/`BlockItem`, `StandingAndWallBlockItem`/`SignItem`/`HangingSignItem`, `ScaffoldingBlockItem`, `DirectionalPlaceContext`, `BedItem`/`DoubleHighBlockItem`, late state component, NeoForge comparative patch, pinned independent mod boundary; [all 4/4 internal sections](../../research/PHASE2_STAGE3B_ITEM_PLACEMENT_FIRST_FAMILY_SOURCE_AUDIT_1_21_1.md); [24 future cases ALL NOT RUN](../../research/PHASE2_STAGE3B_ITEM_PLACEMENT_FIRST_FAMILY_ACCEPTANCE_1_21_1.md). Source reviewed selected family only, no original ITEM class-disposition count or exact applied 21.1.215 ASM claim.
- [ ] **2.3B-2 — remaining item useOn/use/components & effective subclasses**: 4/4 source dispatch, registration inheritance, type/property contracts, independent mods. Include `PlaceOnWaterBlockItem`, fire/tool/growth/containers/creative, all remaining identified effective item overrides; preserve post-place data.
- [ ] **2.3B-3 — alternate non-item creators**: dispenser, `FallingBlockEntity` and direct state replacement, structure/features/commands/`Level.setBlock`, BE/interaction/temporal updates with real source and physical/canonical/foreign classification. Four internal checks.
- [ ] **2.3B-4 — immutable exact original item-registry reconciliation**: crossjoin all 1333 exact ITEM IDs / 87 registered classes / 925 BlockItem subclass rows, record per-class inherited and overridden `use`, `useOn`, `place`, `getPlacementState`, `updatePlacementContext`, `placeBlock`, `BLOCK_STATE`, actual `placed_block`/wall variants and provenance. Explicit REVIEW_PENDING for each uncertain class/call; do not mark SOURCE reviewed based on names. Four internal checks.

## Conditions for closing the ENTIRE 2.3B card

Evidence-backed disposition for **every registered concrete item class** and relevant non-item authors; clear subtype override/bypass classifications, full linkage to original BLOCK IDs, effect of late properties, and future acceptance fixtures. Only transition to 2.3C exact patched NeoForge 21.1.215 ASM once source owners/dispositions have been reconciled. No runtime implementation PASS is implied.

### Checkpoint for next `кк`

Last completed: **2.3B-1** source packet. Current expected branch `2.0`, obtain actual HEAD from GitHub; don't guess commit. Next first unfinished: **2.3B-2 four internal tasks in one response**. If a write did not commit, it is not complete. Java/Mixin/Gradle/CI/client/server/loaded-mod gameplay untouched in 2.3B-1.
