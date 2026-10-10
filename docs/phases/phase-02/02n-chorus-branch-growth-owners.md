# Stage 3A-16 — ChorusFlowerBlock and ChorusPlantBlock branching, support and six-neighbor connection owners

**Status: ACTIVE — 2/4 INTERNAL source-research substages; NeoForge patched ASM, Planet adapter and gameplay acceptance all PENDING.** Minecraft 1.21.1 / NeoForge 21.1.215 / Java21, branch `2.0` only. **ONE user «кк» → all internal 16.1→16.2→16.3→16.4 in a single assistant turn**, bounded checkpoint GitHub commits then one concise final report. If interrupted resume first unchecked microtask, never assume source means runtime success. No Java changes.

[Stage15 final original 241/94 reconciliation](../../research/PHASE2_STAGE3A_AMETHYST_ORIGINAL_94_CLASS_RECONCILIATION_1_21_1.md) · [source reviewed original 241-class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) · [Phase 2 roadmap](../phase-02.md).

## Scope: exactly two original **registered pending** concrete classes

Original NeoForge artifact 11643813158 SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`, immutable 1060 BLOCK IDs / 241 concrete classes / 1333 ITEM / 1712 properties.

| Exact registered BLOCK class | Exact BLOCK ID | Original class hierarchy | Original legal properties |
|---|---|---|---|
| `net.minecraft.world.level.block.ChorusFlowerBlock` | `minecraft:chorus_flower` | ChorusFlowerBlock > Block > BlockBehaviour | `age` 0..5 |
| `net.minecraft.world.level.block.ChorusPlantBlock` | `minecraft:chorus_plant` | ChorusPlantBlock > PipeBlock > Block > BlockBehaviour | `down,east,north,south,up,west` boolean |

Both currently `REVIEW_PENDING` in original 241×16 mutable ledger, NOT newly source-reviewed in prior Stage15. Source preliminary triage [`ChorusFlowerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ChorusFlowerBlock.java) has `randomTick` branching, `AGE` stages, world-UP and horizontal directions, separate `generatePlant/growTreeRecursive` WORLDGEN direct writer; [`ChorusPlantBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ChorusPlantBlock.java) extends PipeBlock, computes all six physical connection booleans and survival/support graph. This is a **different algorithm** from six-physical-direction crystal random growth: tree/branch world-space growth and six-way saved pipe connections require phase-aware source/target support, not one generic `FACING` property patch. Phase2 attachment/state, Phase3 shapes, Phase5 fluids if source relevant, Phase7 growth neighbor/lifecycle, Phase8/9 end biome/natural structure authors.

## Four internal subtasks on the SAME next «кк»

- [x] **16.1 — rehash original ZIP, original two concrete IDs/state/hierarchies and all 5 full-qualified effective declaring-owner method signatures; inspect source inheritance, scheduling, survival, six-way pipe connections and all branches.** Promote ONLY verified two pending concrete classes to `SOURCE_REVIEWED_INTEGRATION_PENDING`, ASM/Planet/gameplay gates still `REVIEW_PENDING`. Small GitHub checkpoint then continue.
- [x] **16.2 — original 1333 ITEM join and ALL alternate creation/maintenance writers.** Trace BlockItem/late component, `ChorusFlowerBlock.generatePlant/growTreeRecursive`, original End city/biome-feature generation (only exact verified sources), commands/templates, growth on other blocks and kill AGE5; don't invent shipped NBT. Commit checkpoint and continue.
- [ ] **16.3 — six Planet face interiors, 12 edges, eight triple corners, physical vs source-local six-way neighbors / branched recursive growth and 25+ deterministic FUTURE UNRUN tests.** Explicit canonical physical BlockPos vs path chart, side/down worldgen, physical neighbor booleans, survival and block/structure bound, vanilla no-Planet parity, client/server ASM proof pending. Commit, continue.
- [ ] **16.4 — entire original 241-class census rehash and independent original ZIP vs live GitHub five full-qualified signature and exact reviewed ID reconciliation.** Verify all 241×3 ASM/Planet/gameplay pending, update roadmap/checkpoint/AI_CONTEXT, choose next genuine pending mechanism owner family with next task card. ONE final docs-only commit, STOP whole Stage16.

## Durable checkpoint — Stage 16.1/16.2 DONE, deepest safe cross-mod mechanism rule frozen

[Full original compiled five signature owner + pinned class, PipeBlock, BlockItem/ChorusPlantFeature and recursive growth owner audit](../../research/PHASE2_STAGE3A_CHORUS_GROWTH_PIPEBLOCK_ITEM_WORLDGEN_ORIGINAL_AUDIT_1_21_1.md) and [PERMANENT deepest safe shared mechanism / cross-mod policy](../../research/PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md), also now codified in root `AGENTS.md §3A.2`. Exact original registered ChorusFlowerBlock and ChorusPlantBlock one BLOCK ID each source+original full five signature reviewed, two registered ordinary BlockItem creators, current **96/241 classes, 245/1060 registered BLOCK IDs reviewed**, pending **145 classes/815 IDs**. No ASM/Planet/gameplay acceptance, Java tests NOT RUN. Flower independent world-UP/HORIZONTAL branch and End Stone + one horizontal support; plant PipeBlock 6-boolean source physical connections with 64 cached shape combinations, own overridden getStateForPlacement/canSurvive/updateShape; worldgen ChorusPlantFeature+growTreeRecursive bypasses Item. Deepest safe common context is typed physical provenance + source/target canonical frames + opt-in algorithm semantic projection, **NOT broad global Direction/NeighborUpdater/BlockPos rewrite**.

Next under SAME user «кк»: internal 16.3 30+ future UNRUN tests, then 16.4 original 241/96 full signature/registered ID independent reconciliation + next pending mechanism card; small git commits, user need not repeat «кк».

