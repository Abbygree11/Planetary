# Stage 3A-13 — living and dead coral state/support/water and fan alternate-item algorithm owners

**Status: ACTIVE — 0/4 INTERNAL research tasks; all source-pending registered classes, all NeoForge patched ASM, Planet runtime adapter and gameplay gates PENDING.** Minecraft **1.21.1**, NeoForge **21.1.215**, branch **2.0**, Java 21. **ONE user “кк” → finish all four internal 13.1–13.4 tasks in one continuous assistant turn**, durable GitHub checkpoint commits, short progress messages, one final answer; on interruption resume first unchecked substep of THIS same packet, never mark partial results unearned. No Java edits; source/compiled method-owner declarations alone do not imply ASM/game PASS.

[Stage 3A-12 complete original 241/82-class reconciliation](../../research/PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_82_CLASS_RECONCILIATION_1_21_1.md) · [actual full registered class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) · [Phase 2 roadmap](../phase-02.md).

## Exact original NeoForge registered cohesive family: SEVEN source-pending concrete classes, 35 BLOCK IDs

Original physical immutable NeoForge 21.1.215 ZIP artifact **11643813158** SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`, 1060 exact BLOCK IDs/241 classes, 1333 ITEM entries. Independently original registry grouped by Java full class name; **exactly 5 registered IDs per class** (brain, bubble, fire, horn, tube coral variants), all seven live GitHub ledger rows are `REVIEW_PENDING`:

| Original concrete runtime class | Exact total ID count | Actual hierarchy distinction | Original state property names |
|---|---:|---|---|
| `CoralBlock` | 5 | `CoralBlock > Block > BlockBehaviour` | none |
| `CoralPlantBlock` | 5 | `CoralPlantBlock > BaseCoralPlantTypeBlock > Block > BlockBehaviour` | waterlogged |
| `CoralFanBlock` | 5 | `CoralFanBlock > BaseCoralFanBlock > BaseCoralPlantTypeBlock > Block > BlockBehaviour` | waterlogged |
| `CoralWallFanBlock` | 5 | `CoralWallFanBlock > BaseCoralWallFanBlock > BaseCoralFanBlock > BaseCoralPlantTypeBlock > Block > BlockBehaviour` | facing,waterlogged |
| `BaseCoralPlantBlock` | 5 | `BaseCoralPlantBlock > BaseCoralPlantTypeBlock > Block > BlockBehaviour` | waterlogged |
| `BaseCoralFanBlock` | 5 | `BaseCoralFanBlock > BaseCoralPlantTypeBlock > Block > BlockBehaviour` | waterlogged |
| `BaseCoralWallFanBlock` | 5 | `BaseCoralWallFanBlock > BaseCoralFanBlock > BaseCoralPlantTypeBlock > Block > BlockBehaviour` | facing,waterlogged |

`BaseCoralPlantTypeBlock` is an **abstract source superclass**, NOT an additional registered concrete class. The 3 BaseCoral* concrete classes correspond to dead variants in original registry; 4 Coral* classes live. The original exact 1333-ITEM `placed_block` join for these 35 registered BLOCK IDs yields **25 registered ITEM creators**: 15 ordinary `BlockItem` and 10 `StandingAndWallBlockItem` for floor fans; **ZERO exact ITEM.placed_block matches for 10 wall-fan BLOCK IDs**. Wall placement can still occur via StandingAndWallBlockItem's alternate registered wall BlockState — absence of an exact ITEM join does **not** mean impossible player creation.

Cross-phase boundaries: Phase 2 support, floor-vs-wall attachment and FACING, canonical source/target frame, actual 3D water-neighbor scan and updateShape; Phase 5 WATERLOGGED and real fluid flow; Phase 7 fluid-dependent entity habitat and AI; Phase 8/9 coral underwater natural/worldgen writers; Phase 3 voxel shapes/face culling and dead/living render. Actual physical BlockPos, world chunks and fluid states remain physical, not six chart aliases. Never universally rotate `Direction.DOWN`, `Direction.Plane.HORIZONTAL` or `BlockPos` primitives.

## Four INTERNAL tasks — entire family per a SINGLE “кк”

- [ ] **13.1 — seven exact original BLOCK rows grouped across all 35 IDs and five fully qualified nearest declaring method owners, full comparative subclass/inherited source dispatch.** Physically rehash original ZIP; inspect pinned 1.21.1 `CoralBlock`, `CoralPlantBlock`, `CoralFanBlock`, `CoralWallFanBlock`, `BaseCoralPlantTypeBlock`, `BaseCoralPlantBlock`, `BaseCoralFanBlock`, `BaseCoralWallFanBlock`, real death/survive/tick/attachment methods and original 5 signature owners. Promote only fully sourced exact original registered classes; all ASM/Planet/gameplay remain PENDING. One GitHub checkpoint commit, continue internally.
- [ ] **13.2 — original 1333-ITEM exact placed_block join + 10 dual-author fan items.** Verify 15 ordinary BlockItem, 10 StandingAndWallBlockItem, no direct wall fan ITEM joined but wall fallback; trace BlockItem, `StandingAndWallBlockItem.getPlacementState`, clicked-face/context fallback and late BlockState, dead/live conversion writes and all known underwater reef worldgen sources, commands/templates and fluid neighbor updates. Don't invent shipped NBT or type conversions. One checkpoint, continue.
- [ ] **13.3 — six-face, 12 edges, 8 triple-corner live/dead coral water/attachment/seam and server tick tests, 25+ future cases ALL NOT RUN.** Explicit source-local wall support direction vs target BlockState canonical, source/target water scan six physical neighbors, WATERLOGGED vs neighboring WATER, tick death and exact dead-state properties, normal/no-Planet control, original item dual-target behavior, fluid and chunk unload/save. One checkpoint, continue.
- [ ] **13.4 — independent rehash/full 241-class original registered exact ID and 5-owner reconciliation vs latest live GitHub, all pending ASM/Planet/gameplay gates and next cohesive family card.** Update phase roadmap and AI_CONTEXT/checkpoint, do one final docs-only commit, STOP after whole family 4/4. Do not change Java or claim gameplay acceptance.

## Current inherited checkpoint from Stage 3A-12

[Cold-surface original four BLOCK/ITEM/alternate writer audit](../../research/PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_BLOCK_ITEM_ALTERNATE_AUTHOR_AUDIT_1_21_1.md), [32 future CS12 acceptance scenarios NOT RUN](../../research/PHASE2_STAGE3A_COLD_SURFACE_SIX_FACE_THERMAL_FLUID_ENTITY_CONTRACT_1_21_1.md), [original 241/82-class reconciled report](../../research/PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_82_CLASS_RECONCILIATION_1_21_1.md). **82/241 source+original effective-owner reviewed classes and 203/1060 exact registered BLOCK IDs**, **159/241 / 857/1060 pending**. All 241 patched NeoForge ASM, Planet adapter, gameplay acceptance gates `REVIEW_PENDING` (**723 status fields**), no code, CI or game tests. Stage 3A and Phase2 OPEN; **NEXT “кк” → whole Stage 3A-13, all four steps**, not one checkbox.
