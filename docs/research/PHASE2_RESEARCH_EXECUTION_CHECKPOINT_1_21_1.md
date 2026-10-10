# Phase 2 staged mechanism audit — durable execution checkpoint

Target: Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.
Branch: `2.0` ONLY. Status: STAGED RESEARCH IN PROGRESS; entire
Phase-2 orientation/survival/interactions NOT accepted.

## Purpose and non-negotiable rule

User asks to complete *all* Phase-2 orientation-sensitive engine
mechanisms without one giant model response or speculative one-block
fixes. Persist every independently completed substage to GitHub.
A conversation disconnection must not destroy the state or require
repeating past audits. Implement by FULL algorithm owner families,
including inherited and overridden implementations, after source/
compiled owner review; never treat one named block as a complete
family without proving why it is a unique algorithm owner.

## Existing verified research recovered from GitHub (DO NOT REDO)

1. `ORIENTATION_MECHANISMS_ATLAS_1_21_1.md`: P01-P40 orientation
   mechanisms, cross-phase dependencies, lifecycle and creation
   bypasses; frozen research taxonomy, not acceptance.
2. `PHASE2_VANILLA_CLASS_CENSUS_1_21_1.tsv`: 293 top-level
   source block Java files, with 53 representative method
   spot-checks and 240 pattern-only initial scans, NOT 293
   registered blocks or 293 fully reviewed methods.
3. `PHASE2_ITEM_CREATION_CENSUS_1_21_1.tsv`: 121 source item
   Java files, likewise first-pass inventory.
4. `PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md`: BlockItem /
   UseOnContext / BlockPlaceContext / alternate authors /
   post-placement BlockState components.
5. `PHASE2_SOURCE_OWNER_AUDIT_WAVE_B_1_21_1.md`: polymorphic
   support, survival, growth, neighbor updates, graph overrides.
6. `PHASE2_SOURCE_OWNER_AUDIT_WAVE_C_1_21_1.md`: multiblocks,
   state composition, use/interaction, fire and structure authors.
7. `PHASE2_EFFECTIVE_OWNER_FINDINGS_1_21_1.md`,
   `PHASE2_NEOFORGE_REGISTRY_FINDINGS_1_21_1.md`,
   `PHASE2_REGISTERED_FAMILY_PRIORITY_MAP_1_21_1.md`:
   Java 21 bootstrapped 21.1.215 registry census and
   effective method owner inventory.
   GitHub Actions run 37988064055 (commit aa395729) green.
   Counts: 1060 BLOCK IDs, 241 concrete block implementation
   classes, 1333 ITEM IDs, 87 concrete item implementation
   classes, 925 BlockItem IDs, 1712 BlockState property instances,
   528 initial name-heuristic orientation property candidates.
   Heuristic property candidates are NOT the universe of affected
   blocks (many state-independent geometry/support algorithms).
8. `PHASE2_ORIENTATION_ACCEPTANCE_1_21_1.md`: strict final
   natural-player-placement, six-face, seams, compiled Mixin,
   NeoForge, client/server, alternate-author and BER integration
   acceptance. Existing 20-station lab does not cover all groups.

## NEXT: bounded independently committable stages

### Stage 3A — compiled block method-owner disposition [ACTIVE; initial reviewed owner clusters committed]
- Use the actual generated NeoForge registry/owner TSV and
  effective-owner findings. Classify EVERY one of the 241
  concrete block classes and effective lifecycle owners,
  including classes with NO FACING property (scaffolding,
  sea pickles, cactus, sugar cane, liquids, graph nodes).
- Each row must have a P01-P40 owner group or explicit
  NOT_APPLICABLE/CROSS_PHASE with evidence, inherited/override
  dispatch owner, Planetary integration, and `REVIEW_PENDING`
  until source/compiled owner semantics are justified.
- Unlike a string-pattern census, check actual effective owner
  and all relevant method bypass/alternate authors.
- Initial scoped review committed in
  `PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md`: 22 researched
  algorithm-owner clusters, with explicit PARTIAL_ADAPTER /
  RESEARCHED_GAP dispositions; this is NOT 241 individual
  registered-class reviews.
- Remaining: deliver `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`
  mapping every actual registry class, then expand/close the
  narrative `PHASE2_STAGE3A_OWNER_FINDINGS_1_21_1.md`.
- Unknown ownership must remain visible. Do NOT fabricate a
  "241/241 reviewed" outcome from automatic names alone.

### Stage 3B — item+creation override disposition
- Review all 87 concrete item implementation classes, 925
  BlockItem registrations, custom `useOn` / `use`,
  placement-context rewriting, standing/wall variants,
  fake/dispenser and post-placement block-state components.
- Produce explicit evidence-backed dispositions; Stage 3A
  alone cannot prove the alternate creation paths.

### Stage 3C — lifecycle/NeoForge patch and source matrix
- Confirm full override/bytecode ownership of placement,
  survival, support predicate, physical neighbor update,
  scheduled tick, growth, shape-authoring, rotate/mirror,
  BE and structure creation, interaction/pairing for every
  P01-P40 applicable owner (plus cross-phase handoffs).
- Compare exact ModDevGradle 21.1.215 bytecode and NeoForge
  patches with Mojang-mapped comparison sources.
- Add deterministic CI fail-unknown-owner gate ONLY once
  evidence-reviewed owner dispositions cover the actual
  registry. No `require=0`/silent unknown passing.

### Stage 4 — implementation mechanism waves
- Wave A: context/physical hit/body/canonical authorship,
  all directional domains (six-direction, horizontal,
  AXIS, FrontAndTop, rotation16), replacement + alternate items.
- Wave B: floor/wall/ceiling supports, inherited predicate
  chains, custom override survival, neighbor update.
- Wave C: single/multicell growth, multiblock pairs, blocks'
  local graphs, rail/spatial attachments.
- Wave D: orientation-sensitive uses/interactions/fire,
  state transformation/structure/copy/rotation, extension points.
- Wave E: reconcile cross-phase contracts with Phase 3
  static/BE renderer, Phase 5 waterlogging, Phase 7A signaling,
  Phase 9 portal/structure geometry; fix owning phase only.
- Each wave: research contract -> smallest shared semantic
  APIs -> thin versioned integrations -> ASM and semantic
  tests -> green GitHub Actions -> one coherent in-game
  acceptance; do not require a user retest for every class.

### Stage 5 — Phase-2 release acceptance
- Expand automatic lab by mechanism (not one station per ID).
- Run full +Y/-Y/+X/-X/+Z/-Z, edge and corner matrix,
  true BlockItem/useOn and synthetic/dispenser/structure tests,
  server/client persistence, support removal and unrelated
  neighbor updates, vanilla/foreign-world regression.
- Only mark P01-P40 rows accepted with actual evidence.
  Cross-phase-owned rendering/simulation may be BLOCKED with
  explicit owner; do not misrepresent that as Phase-2 PASS.
- Produce `PHASE2_FINAL_COVERAGE_MATRIX_1_21_1.md`
  identifying each owner and acceptance proof.

### 2026-10-10 micro-checkpoint: Stage 3A / 2.3A-1 task 1 complete

- Original CI artifact `11643813158` from successful run
  `37988064055` downloaded and raw BLOCK/ITEM/PROPERTY TSV
  schema and uniqueness/row counts verified (1060 / 1333 / 1712);
  241 registered concrete block classes, all audit rows still
  explicitly pending.
- Cross-reconciled `PHASE2_EFFECTIVE_OWNER_FINDINGS` and all
  22 existing `PHASE2_STAGE3A_OWNER_FINDINGS` algorithm-family
  entries against real `java_class`, `class_hierarchy` and
  `effective_method_owners` fields.
- Clarified **103** effective
  `getStateForPlacement(BlockPlaceContext)` declared owners vs
  **104** across every same-name overload; 11 block IDs have
  multiple `getStateForPlacement` overload signatures.
- Detected **59** actual `BushBlock` hierarchy IDs across **29**
  registered concrete classes, but only **40** still dispatch
  `canSurvive` to `BushBlock` (19 bypass via override).
- **No new semantic owner dispositions, runtime changes or game
  acceptance** in this checkpoint. Documentation-only commit.
- Next FIRST incomplete micro-task: 2.3A-1 task 2 — deep source
  review of 12 named concrete `BushBlock` descendants defined
  in `docs/phases/phase-02/01-block-owners.md`. Do not advance
  directly to implementation.

### 2026-10-10 micro-checkpoint: Stage 3A / 2.3A-1 task 2 complete

- Reviewed 12 concrete `BushBlock` descendants, 18 registered
  Block IDs; independently joined exact-method owners with raw
  registry TSV and checked comparative 1.21.1 Java class methods.
- All 18 have non-Bush effective `canSurvive`, leaving exactly
  one sibling outside this set (`SeaPickleBlock`; previously
  researched but not gameplay-accepted).
- Source findings and narrowly scoped implementation contracts:
  `PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md`.
- **No TSV owner dispositions yet**: that is the next separate
  micro-task 3/4. **No NeoForge ASM/Mixin runtime or game acceptance**,
  no code changes; doc-only checkpoint, no CI rerun.
- Next: create first evidence-backed 12-class
  `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`,
  without claiming 241/241 semantic proof.

### 2026-10-10 micro-checkpoint: Stage 3A / 2.3A-1 task 3 complete

- New `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` and
  `PHASE2_BLOCK_OWNER_DISPOSITION_GUIDE_1_21_1.md`.
- Exactly 241 registry-derived concrete-class roster rows and 1060
  represented registered block IDs; **12** source-reviewed classes
  / **18** IDs (the Bush descendant research); **229** classes /
  **1042** IDs REVIEW_PENDING. Source-reviewed is not runtime PASS.
- Five fixed-signature actual nearest effective owner classes
  entered only for 12 reviewed classes; code owner INVOKE and
  NeoForge-specific patched-bytecode review remain pending.
- No Java changes, no new CI/gameplay acceptance.
- Next FIRST incomplete micro-task: card 2.3A-1 task 4
  (next 8–15 concrete owner family; source + registry trace,
  then update TSV disposition in a separate commit).

### 2026-10-10 micro-checkpoint: Stage 3A / 2.3A-1 task 4 complete

- Source-reviewed an **additional 12 real BushBlock
  inherited-canSurvive concrete classes** and **34 registered
  block IDs**, with pinned Minecraft 1.21.1 source method paths,
  actual NeoForge 21.1.215 registry owner-signature candidates,
  mechanism groups and cross-phase boundary cautions:
  `PHASE2_STAGE3A_BUSH_INHERITED_AUDIT_1_21_1.md`.
- Updated the same 241-class ledger:
  **24 source-reviewed / 217 REVIEW_PENDING** (52/1060
  registered IDs vs 1008 pending). Original class roster and
  1060 registry count unchanged.
- Distinguish correctness of `BushBlock.canSurvive`
  from independent growth, direct bonemeal placement,
  structure feature placement, fluid/interaction and
  entity-motion consumers. P20/P22/P24/P30/P34 and cross-phase
  links documented without premature runtime fixes.
- **Only source comparative plus reflection owner evidence;
  no exact NeoForge ASM, tests or gameplay validation.**
  No runtime code changed; doc-only checkpoint.
- Stage **2.3A-1 four small tasks complete.**
  Next FIRST unfinished: `docs/phases/phase-02/02-block-owners-rest.md`,
  Stage 2.3A-2 task 1 (one new 8–15 owner-class family).

### 2026-10-10 micro-checkpoint: Stage 3A-2 / attachment source owners

- Card: `docs/phases/phase-02/02-block-owners-rest.md`,
  micro-task **1 DONE** for first 11-class attachment group.
  Research in `PHASE2_STAGE3A_FACE_ATTACHMENT_AUDIT_1_21_1.md`.
- Actual runtime registry: 11 concrete classes / **29 IDs by
  class count**, combined evidence-based source-reviewed 35/241
  classes / 81/1060 IDs; pending 206 classes / 979 IDs.
- Source owner paths: wall/standing torch and redstone signal
  bypasses, ladder, lantern ceiling/floor, spore ceiling,
  six-way amethyst, EndRod FACING adjacency rule, lever/button
  shared FACE attachment plus separate interaction/ticks.
  Alternate item creation `StandingAndWallBlockItem` is
  source-identified, not independently runtime accepted.
- **Important evidence distinction:** new 11 have comparative
  Java source nearest-declaration owners only; exact NeoForge
  21.1.215 reflection/ASM remains explicitly unverified in TSV.
  Original prior 24 retain verified reflection evidence.
- NO Java changes, no new CI or gameplay validation.
- Next FIRST incomplete micro-task **2.3A-2.2 / checkbox 2**:
  verify the new 11 source-owner paths against exact NeoForge
  patched runtime bytecode and item/interaction alternative
  authors, then update TSV evidence levels in a separate commit.
  Other 206 classes not auto-promoted.

### 2026-10-10 micro-checkpoint: Stage 3A-2.2 attachment exact reflection owners

- Original CI ZIP artifact 11643813158 at commit aa395729
  downloaded and SHA256 verified (7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e).
- For all 11 previously source-reviewed face-attachment
  concrete classes, verified class hierarchy and exact
  `getStateForPlacement`, `canSurvive`, `updateShape`,
  `randomTick`, `setPlacedBy` method declaration owners;
  `neighborChanged`, scheduled tick and `useWithoutItem`
  relevant owners checked independently.
- Reconciled 29 exact registered block IDs and 26 registered
  BlockItems (3 StandingAndWallBlockItem; 23 simple BlockItem).
  Wall torch (2 IDs) and redstone wall torch (1 ID) have
  no direct BlockItem registration; variants selected by
  standing torch items.
- Inspected 8 actually registered Planetary Mixin adapter
  classes; no dedicated LanternBlock or AmethystCluster
  support Mixin found. Existence != correctness/game pass.
- 11 `registry_dispatch_evidence` now
  REFLECTION_OWNER_VERIFIED; all 35 source-reviewed
  classes covered by reflection. Class/ID totals unchanged:
  35/241 reviewed (81 IDs) and 206/241 pending (979 IDs).
- **No patch-body / INVOKE-site ASM checks or gameplay pass**;
  all those ledger fields remain REVIEW_PENDING.
  No production code changed; no new CI required.
- Next FIRST pending card item:
  `docs/phases/phase-02/02-block-owners-rest.md`,
  task 3 (2.3A-2.3), small non-FACING class family.

### 2026-10-10 micro-checkpoint: Stage 3A-2.3 non-FACING growth graphs

- Card `docs/phases/phase-02/02-block-owners-rest.md`
  task **3/4 DONE**. Committed research
  `PHASE2_STAGE3A_NONFACING_GROWTH_GRAPHS_1_21_1.md`.
- 12 actual runtime concrete classes / 12 registered block
  IDs with NO candidate orientation property audited:
  8 GrowingPlant head/body variants, SugarCaneBlock,
  CactusBlock, BambooStalkBlock, BambooSaplingBlock.
  Exact-signature method declaring owners and item alias
  mappings checked from original 21.1.215 ZIP
  (SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`);
  body comparisons from pinned source.
- Registered item census: 7 directly registered BlockItems,
  five block states without own BlockItem; important
  `minecraft:bamboo` item can author
  `minecraft:bamboo_sapling` via BambooStalkBlock placement.
- Source semantics: P29 head/body directional graph,
  P30 vertical columns and horizontal substrate/support
  adjacency, P37 kelp/water and Phase5, berries
  `useWithoutItem` P34, direct bonemeal writers.
  No dedicated plant/growth Mixin found by filename in
  current Planetary mixin list, not proof of unhooked behavior.
- Ledger after commit: **47** class-level source +
  NeoForge reflection owners reviewed (93 block IDs);
  **194** class-level REVIEW_PENDING (967 IDs).
  241 classes / 1060 IDs unchanged; no ASM,
  NeoForge patched-body, gameplay pass.
- **NEXT first unfinished microtask 2.3A-2.4**:
  final exact registry census/disposition invariants and
  evidence formatting check, one separate response.
  No production Java code changed.

### 2026-10-10 micro-checkpoint: Stage 3A-2.4 exact ledger reconciliation

- Card `docs/phases/phase-02/02-block-owners-rest.md`:
  all four **bounded** packages completed. **Stage 3A
  incomplete**, with 194 pending actual concrete classes.
- Reconciled original ZIP `11643813158`, SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`, against
  `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`.
  All 241 fully qualified classes and exact per-class registered
  ID counts match; all 47 reviewed ID sets and five
  full exact declaring owners match. Four independent
  FNV-1a fingerprints: 0xf188a064, 0x6c35dd77,
  0xacd03109, 0x9fc5dbf5. See
  `PHASE2_STAGE3A_LEDGER_RECONCILIATION_1_21_1.md`.
- Current: 47/241 source+reflection reviewed
  (93/1060 BLOCK IDs); 194/241 still `REVIEW_PENDING`
  (967/1060 IDs). All ASM/Planet integration/gameplay
  acceptance flags remain REVIEW_PENDING. No Java code change.
- Old per-package numbers in guide are now explicitly
  HISTORICAL; current counts lead the guide.
- New separate card
  `docs/phases/phase-02/02a-block-graph-owners.md`
  names **10 real still-pending candidate graph classes /
  71 block IDs**, source/research NOT started or claimed.
- **NEXT FIRST uncompleted micro-task:** card 2.3A-3
  step 1, one source+compiled owner investigation of
  a bounded P25/P26 connectivity subset. Do not
  skip directly to Stage 3B or implementation.

### 2026-10-10 micro-checkpoint — Stage 3A-3.1 connected graph owners

- Current card: `docs/phases/phase-02/02a-block-graph-owners.md`,
  task **1/4 DONE**, remaining 2/3/4 unchecked.
- New class-level source research:
  `docs/research/PHASE2_STAGE3A_CONNECTIVITY_GRAPHS_1_21_1.md`.
  Original NeoForge CI artifact 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`
  used for exact 8 class IDs and nearest method
  declaration owners, source compared against pinned
  Minecraft 1.21.1 Java.
- Eight reviewed / 69 registered IDs:
  FenceBlock 12, FenceGateBlock 11, WallBlock 25,
  IronBarsBlock 2, StainedGlassPaneBlock 16, VineBlock 1,
  GlowLichenBlock 1, SculkVeinBlock 1.
  Distinct algorithms: local 4 tangents P25,
  perpendicular gate/wall P27, five-face vine P26
  support fallback + spread, six-face MultifaceBlock
  full-face support and spreading.
  Source `FenceBlockGravityMixin` exists, no
  runtime handler evidence; all ASM/gameplay pending.
- **TripWireBlock** and **TripWireHookBlock**
  *not reviewed*, remain in the 186 `REVIEW_PENDING`;
  P36 redstone interaction needed separately.
- New ledger: **55/241** source+reflection reviewed
  (162/1060 associated block IDs), **186/241**
  REVIEW_PENDING (898/1060 IDs).
- **NEXT FIRST incomplete task**: 2.3A-3.2,
  real ITEM/alternative creation and interaction
  author paths for the already selected eight.
  One standalone research commit, no runtime Java
  changes in this task.

### 2026-10-10 micro-checkpoint — Stage 3A-3.2 item and alternate-state authors

- Card `docs/phases/phase-02/02a-block-graph-owners.md`, task **2/4 DONE**; **3 and 4 remain unchecked**.
- Source and original NeoForge CI item registry join:
  [`PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md`](PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md).
  **69/69** BLOCK IDs represented by eight reviewed
  graph classes have **69 distinct BlockItem** entries with
  exact `placed_block`=ITEM ID, 7 common item method
  declaring owners all BlockItem.
- Important non-item writers: `BlockItem` post-placement
  `DataComponents.BLOCK_STATE` component, fence-gate
  useWithoutItem/neighborChanged/onExplosionHit, fence
  LeadItem (leash entity interaction), VineBlock randomTick,
  GlowLichenBlock performBonemeal -> MultifaceSpreader,
  SculkVeinBlock regrow/onDischarged/attemptUseCharge,
  conditional MultifaceGrowthFeature nonplayer placement.
- **No change to class source-review status**:
  55/241 reviewed, 186 REVIEW_PENDING, 162/898 IDs;
  TripWireBlock and TripWireHookBlock remain pending.
- All NeoForge patched bytecode / actual Mixin weaves,
  Planet adaptation and gameplay acceptance remain PENDING.
  Docs-only, no new CI or game run.
- **NEXT first unchecked task**: Stage 3A-3.3 item 3,
  no-property multi-face + BlockStateBase cached shape/face
  and real physical neighbor callback mapping at seams,
  Phase 2 vs 3/5/7A scope. One separate bounded commit.

### 2026-10-10 micro-checkpoint — Stage 3A-3.3 graph shape/cache and callback contract

- Card `docs/phases/phase-02/02a-block-graph-owners.md`:
  task **3/4 DONE**; task 4 remains unchecked.
- Dedicated source-only audit
  `docs/research/PHASE2_STAGE3A_GRAPH_SHAPE_CALLBACK_CHART_1_21_1.md`
  uses pinned 1.21.1 BlockBehaviour.BlockStateBase, Block,
  CrossCollisionBlock, WallBlock, VineBlock, MultifaceBlock,
  and existing PlanetBlockStateFrame/NeighborQuery/SupportQuery,
  BlockStateShapeMixin/PlanetBlockShapeRuntime/BlockRenderCulling.
- Canonical source-local graph property direction,
  PHYSICAL neighborShapeChanged/updateShape callback
  direction and BlockPos, and target-local inward
  cached SupportType face remain distinct. Cached state
  faces and per-concrete-class shapes are not
  planet-position-dependent. Existing wrapper rotates
  physical shape outermost but preserves canonical
  support/occlusion; mixed-frame MultifaceBlock
  OR predicate and WallBlock above/down comparison
  need explicit later integration tests.
- All identified gaps are **potential** until ASM
  INVOKE/Mixin apply + gameplay all faces/seams/corners;
  runtime code NOT modified or accepted.
- Ledger unchanged **55/241** source+reflection reviewed
  (162 IDs), **186/241** REVIEW_PENDING (898 IDs).
  TripWireBlock/TripWireHookBlock remain pending.
- **NEXT first unchecked task 3A-3.4:** exact CI ZIP
  census, reviewed ID/owner and acceptance-status
  reconciliation; write next small owner family
  card after that distinct microtask is done.
  No tests or game client claimed.

### 2026-10-10 micro-checkpoint — Stage 3A-3.4 registry integrity

- Card `docs/phases/phase-02/02a-block-graph-owners.md`:
  **all four BOUNDED research steps DONE**, but **entire
  Stage 3A and Phase 2 are incomplete**.
- Original NeoForge CI ZIP 11643813158 SHA256:
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Independently compared all **241**
  exact class names and count of **1060** registered IDs,
  plus each of **55** source-reviewed classes'
  **162 exact block ID strings** and five declaring
  method owners by complete signatures.
- All four independent FNV-1a 32-bit diagnostic digests
  MATCH between source ZIP and committed branch TSV:
  0xf188a064 roster, 0x5e998532 reviewed IDs,
  0x98207d0 owners, 0x1f4133ac combined.
- Eight graph classes (69 IDs) retain reviewed/reflection
  tier; **69/69 corresponding ordinary BlockItems**
  independently verified in original item registry.
  TripWireBlock, TripWireHookBlock and 184 additional
  classes stay `REVIEW_PENDING`. All acceptance fields
  remain REVIEW_PENDING.
- Cumulative **55 source+reflection-reviewed / 186 pending**
  classes, 162 vs 898 registered BLOCK IDs.
- **New card:** `docs/phases/phase-02/02b-redstone-signal-owners.md`
  11 real *still pending* signal/rail/tension classes,
  12 block IDs. **NEXT FIRST unchecked task: Stage
  3A-4.1 item 1**, separate small source/refl owner
  audit of a coherent 8–11-class subset.
- No Java code change, CI build, applied-Mixin ASM
  or in-game PASS. Research only.

### 2026-10-10 micro-checkpoint — Stage 3A-4.1 signal owner audit

- Card `docs/phases/phase-02/02b-redstone-signal-owners.md`,
  checkbox 1/4 **DONE**. One documented package:
  `docs/research/PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md`.
- Original NeoForge 21.1.215 CI ZIP 11643813158
  (SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`)
  for class/8 ID roster and five exact method declaring
  owner signatures, pinned comparative Minecraft
  1.21.1 source for full spatial and signal algorithms.
- 8 newly source+reflection reviewed classes:
  TripWireBlock, TripWireHookBlock, RedStoneWireBlock,
  RepeaterBlock, ComparatorBlock, ObserverBlock,
  TargetBlock, RedstoneLampBlock. 8 BLOCK IDs.
  Nonregistered DiodeBlock is inherited authority
  for repeater and comparator placement/support/tick.
- Three originally suggested candidates **NOT REVIEWED**:
  DetectorRailBlock (1 ID), PoweredRailBlock (2 IDs),
  DaylightDetectorBlock (1 ID). Remain pending, as
  does every other unreviewed class.
- Ledger after commit: **63/241 source+reflection-reviewed**
  classes (170/1060 IDs), **178/241 REVIEW_PENDING**
  (890/1060 IDs). All ASM/Planet/gameplay statuses
  `REVIEW_PENDING`. No code/CI/gameplay tests.
- NEXT FIRST unchecked task: **3A-4.2 card checkbox 2**,
  independent true ITEM class and placed_block
  join for eight just reviewed signal classes,
  plus off-item writers (entity pressure/hit, comparator
  BE, scheduled pulse, cable scanning, redstone wire
  propagation). Keep Stage 7A acceptance separate.

### 2026-10-10 micro-checkpoint: Stage 3A-4.2 ITEM owners and signal alternate writers

- Card `docs/phases/phase-02/02b-redstone-signal-owners.md`,
  **2/4 DONE**. Research:
  `PHASE2_STAGE3A_REDSTONE_ITEM_ALTERNATE_AUTHORS_1_21_1.md`.
- Original 21.1.215 CI ZIP 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Eight registered BLOCK IDs map exactly to eight ITEM
  records using `placed_block`: **6 BlockItem**,
  **2 ItemNameBlockItem** (string->tripwire,
  redstone->redstone_wire). 7 exact item method
  declaration owners all `BlockItem`; ItemNameBlockItem
  itself only provides distinct getDescriptionId.
- Source-only other writers: TripWireBlock
  entityInside/checkPressed/playerWillDestroy,
  TripWireHookBlock.calculateState across two hooks
  and up to 41 cells, RedStoneWireBlock
  updatePowerStrength/calculateTargetStrength,
  DiodeBlock scheduled priorities/repeater lock,
  ComparatorBlockEntity OutputSignal persistence,
  ObserverBlock two-tick, TargetBlock projectile hit,
  RedstoneLampBlock LIT/timer.
  BlockItem `DataComponents.BLOCK_STATE` override
  occurs before `setPlacedBy`. Do NOT conflate
  item authors with power graph.
- Ledger unchanged **63/241 source+reflection reviewed**
  (170/1060 BLOCK IDs), **178/241 REVIEW_PENDING**
  (890/1060 IDs). DetectorRail, PoweredRail,
  DaylightDetector deferred. All ASM/Planet/gameplay
  acceptance PENDING. No Java/code/CI/game changes.
- **NEXT FIRST unchecked task:** 3A-4.3 (card
  checkbox 3) exact physical-vs-local signal
  direction, port, cable and notification topology,
  recursion and scheduled priority, Phase 2/7A
  contract. Single independent docs commit.

### 2026-10-10 micro-checkpoint — Stage 3A-4.3 redstone port/topology source evidence

- Card `docs/phases/phase-02/02b-redstone-signal-owners.md`
  checkbox **3/4 DONE**, only checkbox 4 remains.
- Detailed research
  `docs/research/PHASE2_STAGE3A_REDSTONE_PORT_TOPOLOGY_1_21_1.md`
  uses pinned comparative 1.21.1 source and existing
  PlanetBlockFrameContext.walk, StateFrame, NeighborQuery,
  ShapeRuntime. **No runtime/JVM bytecode/gameplay tests**.
- Five distinct directions: source-local state
  `FACING`/tangent, physical neighbor change/BlockPos,
  target-local inward face, signal-getter query-side
  contract, seam `transportedDirection`; no global
  Direction rewrite.
- TripWire/Hook write up to 41-cell ordered cable with
  both hooks, physical notifications, trigger AABB;
  wire `RedstoneSide` (NONE/SIDE/UP) and cached
  geometry separate from POWER recursive callback path,
  `shouldSignal` mutable instance field; DiodeBlock
  input FACING/side-lock versus opposite notification
  cell; Comparator BE output; Observer watched input
  and opposite output+2-tick pulse; target hit
  physical and lamp delayed off. Scheduled timing and
  priorities should remain vanilla-compatible.
- Rail/daylight sources read only for cross-phase
  handoff (P28/P36): **DetectorRailBlock,
  PoweredRailBlock, DaylightDetectorBlock continue
  to be `REVIEW_PENDING`**, not source-reviewed.
- Ledger unchanged **63/241 reviewed (170 registered IDs),
  178 pending (890 IDs), 1060 registered IDs**;
  all ASM/Planet/gameplay acceptances REVIEW_PENDING.
- **NEXT first incomplete task:** Stage 3A-4.4
  checkbox 4, exact original CI ZIP class
  source-review counts, ID sets, signature owner
  and per-gate status reconciliation, queue
  next limited owner family in separate card.
  ONE bounded commit, then stop.

### 2026-10-10 micro-checkpoint — Stage 3A-4.4 original CI census reconciliation

- Card `docs/phases/phase-02/02b-redstone-signal-owners.md`
  **all four research checkboxes DONE**. Not full
  Stage 3A, not Phase 2 acceptance.
- Original NeoForge 21.1.215 CI ZIP artifact
  11643813158 SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Compare all 241 original class names+counts, and
  **63 reviewed** classes' **170 exact IDs** plus
  five effective declaration owners by **full exact
  signature**. All four canonical independent digests
  matching GitHub TSV: roster `0xf188a064`,
  IDs `0xdb8afbd5`, owners `0x6052a7cf`,
  combined `0x66be3dcb`.
- Full source/class ledger unchanged:
  63 `SOURCE_REVIEWED_INTEGRATION_PENDING`
  (170/1060 registered BLOCK IDs), 178
  `REVIEW_PENDING` (890 IDs). All 241
  patched ASM, Planet integration and in-game
  acceptance `REVIEW_PENDING`.
- Eight source-reviewed redstone/tension signal
  owners reconciled, exact eight item placement records
  from prior 3A-4.2: 6 BlockItem,
  2 ItemNameBlockItem (string and redstone).
- Next **P28 rail** candidate class group (still
  unreviewed): RailBlock minecraft:rail;
  DetectorRailBlock minecraft:detector_rail;
  PoweredRailBlock minecraft:activator_rail and
  minecraft:powered_rail. 3 concrete classes,
  **4 actual IDs**, plus nonregistered
  BaseRailBlock/RailState. DaylightDetectorBlock
  pending **separately** (minecraft:daylight_detector).
- Full evidence:
  `docs/research/PHASE2_STAGE3A_REDSTONE_COHORT_RECONCILIATION_1_21_1.md`.
- **NEXT FIRST unchecked task**:
  `docs/phases/phase-02/02c-rail-owners.md`
  checkbox 1 / Stage 3A-5.1; one source/NeoForge
  declaration family audit, one commit.
- No code, CI build, ASM INVOKE or game client
  PASS. User shorthand "кк" means: execute this
  single next bounded microtask, commit, checkpoint,
  answer; do NOT launch an unbounded series.

### 2026-10-10 micro-checkpoint — Stage 3A-5.1 rail owner/graph source audit

- Card `docs/phases/phase-02/02c-rail-owners.md`,
  checkbox **1/4 DONE**; checkboxes 2,3,4 open.
  Audited 3 original pending concrete rail classes /
  4 BLOCK IDs: RailBlock/minecraft:rail,
  DetectorRailBlock/minecraft:detector_rail,
  PoweredRailBlock/minecraft:activator_rail +
  minecraft:powered_rail. Full record
  `docs/research/PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md`.
- Original NeoForge 21.1.215 CI ZIP artifact 11643813158
  SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Compiled nearest exact core method signatures
  ALL three owners: place/survive/updateShape =
  BaseRailBlock, randomTick BlockBehaviour,
  setPlacedBy Block. Extra onPlace detector-specific,
  BaseRailBlock neighborChanged and onPlace other
  families, scheduled tick DetectorRailBlock only,
  rotate/mirror per subclass.
- Comparative pinned 1.21.1 source `BaseRailBlock`,
  `RailState`, `RailBlock`, `DetectorRailBlock`,
  `PoweredRailBlock`, `RailShape` audited.
  RailState `place/connectTo` changes this and
  multiple neighboring rail BlockStates, uses
  physical XZ-only comparison and physical world-Y
  above/below assumptions, separate source-local
  chart/slope policy required. RailBlock curve
  signal junction, detector entity 20-tick,
  powered rail up to 8 recursed segments distinct.
  `updateShape` waterlog fluid tick ≠ rail graph.
- Full ledger **66/241** class source+reflection
  reviewed (174 BLOCK IDs), **175/241**
  REVIEW_PENDING (886 IDs), still **1060** total.
  DaylightDetectorBlock remains pending, all
  241 NeoForge ASM/Planet runtime/gameplay
  acceptance statuses REVIEW_PENDING.
- **NEXT FIRST unchecked task**: Stage 3A-5.2,
  card checkbox 2: independent actual original
  ITEM `placed_block` join for 4 rail IDs,
  minecart/off-item state authors and
  power/detector network causal write paths.
  One bounded commit; no Java game patch.

### 2026-10-10 micro-checkpoint — Stage 3A-5.2 exact rail ITEM + alternate authors

- Card `docs/phases/phase-02/02c-rail-owners.md`
  checkbox **2/4 DONE**. Research:
  `docs/research/PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md`.
- Original unmodified NeoForge 21.1.215
  CI ZIP 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`:
  all 4 BLOCK IDs independently joined to
  **4 original ITEM rows** via `placed_block`;
  all 4 `BlockItem`, no renamed aliases;
  all seven item placement exact-signature nearest
  declaring owners `BlockItem` each.
- Distinct nonitem graph/signal/structure paths:
  BaseRailBlock onPlace/neighborChanged ->
  RailState.place/connectTo multi-block rail
  state writes; RailBlock 3-way redstone junction;
  DetectorRailBlock entityInside/checkPressed,
  20-tick powered recheck, analog
  MinecartCommandBlock/Container minecart;
  PoweredRailBlock findPoweredRailSignal at
  up to 8 graph steps, separate activator and
  powered rail block-instance pathways.
  AbstractMinecart.tick explicitly handles
  activator activateMinecart vs powered-rail
  moveAlongTrack boost/brake (full movement
  intentionally deferred).
  **MineshaftPieces** direct Blocks.RAIL default
  state + RailShape worldgen placement proven,
  generic StructureTemplate direct BlockState
  write path identified (not claims of specific
  rail template contents).
  `BlockItem` may optionally apply
  `DataComponents.BLOCK_STATE` between initial
  placeBlock and setPlacedBy.
- **No class promotion this task**: full ledger
  66/241 source+reflection reviewed (174/1060
  IDs), 175 REVIEW_PENDING (886 IDs).
  DaylightDetectorBlock remains separate pending.
  All ASM/Planetary runtime/gameplay verdicts
  for 241 classes REVIEW_PENDING, Java unchanged.
- **NEXT FIRST unchecked task**: 3A-5.3
  checkbox **3**: exact local rail direction/shape
  versus world BlockPos/slope coordinates at
  edges and corners, dynamic power and minecart
  movement cross-phase boundary; define tests
  but claim none executed. ONE research commit.

### 2026-10-10 micro-checkpoint — Stage 3A-5.3 RailShape seam source chart

- Card `docs/phases/phase-02/02c-rail-owners.md`
  **3/4 bounded research tasks DONE**. Research file
  `docs/research/PHASE2_STAGE3A_RAIL_SEAM_FRAME_CONTRACT_1_21_1.md`.
- Pinned comparative Minecraft 1.21.1 `RailShape`,
  `RailState`, `BaseRailBlock`, `PoweredRailBlock`,
  `DetectorRailBlock` and `AbstractMinecart`.
  Planetary StateFrame, NeighborQuery,
  SupportQuery, FrameContext.walk and
  ShapeRuntime reviewed as infrastructure only.
  No actual NeoForge patched INVOKEs verified.
- Ten RailShape values: 2 straight, 4 ASCENDING,
  4 quarter-turn (only ordinary RailBlock).
  Source canonical local ports vs actual world
  physical adjacent cell vs target-local inward
  side; local radial grade UP/DOWN and
  seam transported continuation distinct.
- Critical `RailState` assumptions:
  world N/S/E/W, `getRail` candidate then world
  above/below, `hasConnection` X/Z equality
  ignoring Y; `place/connectTo` multi-cell
  writes and power-based turn priorities.
  Cannot globally remap Directions or merely
  rotate RailShape on source BlockState.
- BaseRailBlock ascending requires extra
  support neighbor and waterlogged fluid tick;
  PoweredRail up-to-8 same-block-instance chain
  separate from DetectorRail minecart AABB,
  20-tick recheck, direct-signal query side
  and Comparator analog signal.
  AbstractMinecart seeks track at world Y-1,
  calculates motion on global XZ and slope
  with world Y; movement not solved by
  BlockState-only rail topology.
  Phase3 render/physical collision, Phase5 water,
  Phase7A signal causality, Phase8 direct
  Mineshaft worldgen need explicit evidence.
- 11 future test fixtures covering six faces,
  4 slopes, 4 curves, seam/corner, mixed
  grade, power, cart movement/detection,
  waterlogging, direct structure creators.
  **NO fixtures run.**
- Status unchanged: **66/241** source+reflection
  reviewed (174/1060 IDs), **175/241**
  REVIEW_PENDING (886 IDs), DaylightDetector
  still pending, all three ASM/Planet/gameplay
  gates pending on all 241 classes.
- **NEXT FIRST unfinished task** Stage 3A-5.4,
  card checkbox 4: compare all original
  NeoForge 21.1.215 CI ZIP exact registry,
  class status+method owner signatures and
  set next bounded pending family.
  ONE independent commit and stop.

### 2026-10-10 micro-checkpoint — Stage 3A-5.4 FULL original CI / reviewed rail owner census

- Card `docs/phases/phase-02/02c-rail-owners.md`
  **all 4 of 4 bounded research checkboxes COMPLETE**;
  full Phase2 and Stage3A still incomplete.
- Original NeoForge 21.1.215 unmodified CI
  artifact 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Reopened ZIP directly: **1060 original BLOCK
  ID rows**, **1333 ITEM rows**,
  **1712 state property rows**, **241 unique
  registered Java concrete classes**.
- 66 source+reflection reviewed classes' **all
  174 exact IDs** and **five declaring nearest
  owners** reconstructed using full parameter
  signatures from `effective_method_owners`,
  including overload disambiguation; exact
  full original-vs-github canonical FNV-1a:
  `0xf188a064` 241-class roster,
  `0x6c5c67d7` reviewed ID strings,
  `0x57a01200` reviewed 5-owners,
  `0xb3945a6e` all combined — **MATCH all 4**.
  Report `docs/research/PHASE2_STAGE3A_RAIL_COHORT_RECONCILIATION_1_21_1.md`.
- Three rail owners/class IDs specifically
  included: RailBlock minecraft:rail (1),
  DetectorRailBlock minecraft:detector_rail
  (1), PoweredRailBlock minecraft:activator_rail
  and minecraft:powered_rail (2); five owners
  BaseRailBlock/BaseRailBlock/BaseRailBlock/
  BlockBehaviour/Block per class.
- Current class-status counts unchanged:
  **66/241 SOURCE_REVIEWED_INTEGRATION_PENDING
  (174/1060 IDs)**; **175/241 REVIEW_PENDING
  (886/1060 IDs)**. All 241 patched bytecode,
  Planet adapter, game acceptance `REVIEW_PENDING`.
  No Java/build/client/gameplay tests.
- New card `docs/phases/phase-02/02d-environment-pressure-sensor-owners.md`
  created for three still pending source owner
  classes / 16 registered IDs:
  DaylightDetectorBlock 1; PressurePlateBlock
  13; WeightedPressurePlateBlock 2.
  Sky/BE ticker and BasePressurePlateBlock
  entity collision/tick are separate concerns.
  Sculk/Lightning are deferred independent
  source family groups, not auto-reviewed.
- **NEXT FIRST unchecked task:** **Stage 3A-6.1**,
  new 02d card checkbox **1**, original
  NeoForge 21.1.215 registry & five-owner
  compiled declarations and pinned comparative
  source behavior for these 3 classes.
  One package, independent GitHub commit; stop.

### 2026-10-10 micro-checkpoint — Stage 3A-6.1 sensor exact runtime owner + source

- Active `docs/phases/phase-02/02d-environment-pressure-sensor-owners.md`
  card **1/4 DONE**, next first unchecked 2.
  Complete research:
  `docs/research/PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md`.
- Original unmodified NeoForge 21.1.215 CI ZIP
  artifact 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Original exact 3 registered Java class rows /
  **16 actual BLOCK IDs**, 5 nearest
  declaring method owners by full signature.
  DaylightDetectorBlock owners
  Block/BlockBehaviour/BlockBehaviour/
  BlockBehaviour/Block; PressurePlateBlock
  and WeightedPressurePlateBlock owners
  Block/BasePressurePlateBlock/
  BasePressurePlateBlock/BlockBehaviour/Block.
- Pinned source 1.21.1:
  DaylightDetectorBlock.getTicker only server +
  hasSkyLight with BlockEntity type, tickEntity
  samples SKY light / skyDarken + sunAngle on
  gameTime%20==0, INVERTED interaction mode
  immediate POWER author (not randomTick).
  BasePressurePlateBlock canSurvive below rigid
  OR support-center UP, updateShape DOWN
  removes on missing support; entityInside/
  TOUCH_AABB physically in world XYZ, entity
  filter excludes spectators/ignoring triggers;
  checkPressed writes POWERED/POWER, notifies
  block pos and world below, scheduled tick.
  PressurePlateBlock Boolean 0/15 20 ticks,
  `BlockSetType` sensitivity EVERYTHING vs MOBS.
  WeightedPressurePlateBlock analog ceil(15 *
  min(count,maxWeight)/maxWeight), 10-tick recheck.
  Direct plate getDirectSignal only queried UP,
  daylight getSignal all queried sides.
- New source status: **69/241**
  SOURCE_REVIEWED_INTEGRATION_PENDING
  (**190/1060 registered BLOCK IDs**),
  **172/241 REVIEW_PENDING (870/1060 IDs)**.
  Excluded Sculk/Lightning classes remain
  source pending. All 241 patched ASM,
  Planet adapter, gameplay acceptance
  `REVIEW_PENDING`; no Java, CI, game tests.
- **NEXT FIRST unchecked microtask 3A-6.2**,
  card checkbox 2: independent exact original
  1333 ITEM `placed_block` join for 16
  actual sensor BLOCK IDs and alternate
  world/BE/entity authors. One bounded commit,
  stop; no runtime PASS.

### 2026-10-10 micro-checkpoint — Stage 3A-6.2 sensor ITEM owners and non-item source writes

- Active card `docs/phases/phase-02/02d-environment-pressure-sensor-owners.md`:
  checkboxes **1 and 2 DONE**, 3 and 4 remain open.
  New research doc:
  `docs/research/PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md`.
- Original unmodified NeoForge 21.1.215
  1333-ITEM/1060-BLOCK registry census ZIP
  artifact 11643813158 SHA-256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Actual `ITEM.placed_block == BLOCK.registry_id`
  16/16 exact unique mapping (daylight 1,
  ordinary plates 13, weighted plates 2);
  all concrete regular BlockItem class,
  exactly seven item creation/placement
  effective method declaration owners BlockItem
  for every item; `use(Level,Player,InteractionHand)`
  inherited Item. No ItemNameBlockItem alias.
  Canonical 16 original joined registry row
  SHA256 `49c88b217ca7b0dd560634c2c8cb01560b265b29a85030ebda92b66f7596ebe9`.
- `BlockItem.place` initial context/BlockState
  then optional `DataComponents.BLOCK_STATE`
  rewrite before `setPlacedBy`.
  DaylightDetectorBlock server BE ticker
  gameTime%20 skylight/sun/inverted and
  `useWithoutItem` immediate POWER updates;
  BasePressurePlateBlock entityInside/getEntityCount
  physical world-XYZ TOUCH_AABB and scheduled
  20/10 tick analog/bool POWER writes, support
  loss and onRemove physical below notifications;
  StructureTemplate generic setBlock + neighbor
  shape recalculation bypasses BlockItem.
  **No verified specific sensor worldgen structure**.
- No new class promoted:
  **69/241 SOURCE_REVIEWED_INTEGRATION_PENDING
  (190/1060 BLOCK IDs)**, **172/241
  REVIEW_PENDING (870 IDs)**. Sculk/Lightning
  remain separate source pending. All ASM,
  Planet runtime and gameplay gate fields
  REVIEW_PENDING, no production Java or CI/client test.
- **NEXT FIRST unchecked Stage 3A-6.3**:
  local/world BlockState support and dynamic
  press hitbox, signal queried UP, direct
  sky sampling and BE vs block scheduled tick
  at all faces/seams. Define future runnable
  tests, no PASS; ONE independent commit then stop.

### 2026-10-10 micro-checkpoint — Stage 3A-6.3 six-face sensor local-physical chart

- Sensor family card `docs/phases/phase-02/02d-environment-pressure-sensor-owners.md`:
  **3 of 4 bounded research tasks DONE**,
  task 4 still open.
  New report:
  `docs/research/PHASE2_STAGE3A_SENSOR_FACE_FRAME_AND_SIGNAL_CONTRACT_1_21_1.md`.
- Source: actual Planetary POS/NEG X/Y/Z
  `PlanetFace` basis, stable `PlanetBlockStateFrame`,
  `PlanetBlockFrameContext.step/walk`,
  `PlanetBlockNeighborQuery`,
  `PlanetBlockSupportQuery`,
  `PlanetVoxelShapeRotation`,
  `PlanetBlockShapeRuntime` (outermost
  physical `VoxelShape` when BlockGetter Level).
  Pinned Minecraft 1.21.1 PressurePlateBlock,
  WeightedPressurePlateBlock,
  BasePressurePlateBlock, DaylightDetectorBlock.
  **No new ASM/Mixin runtime tracing.**
- Local-source DOWN support must map to
  actual physical block, then support target's
  canonical local inward face, maintain rigid
  OR center support. Underlying vanilla
  `updateShape(Direction.DOWN)` and
  `pos.below()` are global-Y assumptions
  and don't automatically work on side faces.
  Six exact PlanetFace basis direction rows in
  report; seam and 3-face corner canonical
  BlockState vs path-dependent traversal distinct.
- Plate `TOUCH_AABB` X/Z [1/16,15/16],
  Y [0,4/16] in canonical local coords
  is raw `Level.getEntitiesOfClass` input,
  not the 0.5/16 or 1/16 visual VoxelShape.
  Rotate around block center using canonical
  gravity frame and translate once; **also
  verify actual entityInside dispatch**, which
  is not guaranteed by shape rotation.
  Entity filters exclude spectators and
  isIgnoringBlockTriggers and ordinary
  `BlockSetType` sensitivity vs analog counts.
- `getDirectSignal(Direction.UP)` is an API
  queried direction, not physical callback,
  and should remain separate from
  `updateNeighboursAt(pos.below())`.
  Daylight world LightLayer.SKY/skyDarken/
  sunAngle not a gravity-local sky;
  server daylight BlockEntityTicker when
  hasSkyLight and gameTime%20, INVERTED
  interaction immediate; plates instead
  use scheduled 20/10-tick relative rechecks
  after entity contact. No randomTick.
- 15 future physical six-face, 2-face seam,
  3-face corner, sky/day-night, scheduler,
  power and vanilla control tests specified,
  **NONE RUN**.
- Ledger unchanged **69/241 source+reflection
  reviewed (190/1060 BLOCK IDs)**;
  **172/241 REVIEW_PENDING (870/1060 IDs)**.
  Sculk/lightning pending; all 241 ASM,
  Planet adapter and gameplay statuses
  REVIEW_PENDING, Java unchanged.
- **NEXT FIRST unchecked microtask 3A-6.4**
  card checkbox 4: independently reconcile
  original NeoForge 21.1.215 compiled
  ZIP with 69 reviewed exact class/190 IDs
  and five method-owner signature tuples
  and all 172 source-unreviewed rows; queue
  next independent owner family, commit once.

### 2026-10-10 micro-checkpoint — Stage 3A-6.4 all 69 exact class/ID/owner declarations reconciled

- Card `docs/phases/phase-02/02d-environment-pressure-sensor-owners.md`
  **4 of 4 bounded research subtasks DONE**;
  Phase 2 and Stage 3A still incomplete.
- Original **unmodified** NeoForge 21.1.215
  CI artifact 11643813158 ZIP SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Current original runtime census 1060 BLOCK
  ID rows /241 distinct registered classes,
  1333 ITEM, 1712 property records.
  Original `effective_method_owners` parsed
  by exact five full qualified argument
  signatures for all 69 current source-reviewed
  classes, checked all 190 registered exact IDs
  from `registry_id` against GitHub class ledger.
  Four original vs GitHub canonical FNV digests
  all MATCH: roster `0xf188a064`,
  IDs `0x6ae8047b`,
  owners `0xba6ef72f`,
  combined `0x328b390e`.
  Full evidence file:
  `docs/research/PHASE2_STAGE3A_SENSOR_COHORT_RECONCILIATION_1_21_1.md`.
- Source+reflection completed for actual
  DaylightDetectorBlock (1 ID),
  PressurePlateBlock (13), WeightedPressurePlateBlock
  (2) in previous 3A-6.1/6.2/6.3 tasks;
  this 3A-6.4 did NOT change source dispositions,
  Bytecode/Mixin/application/gameplay statuses.
- Complete ledger **69/241 source+reflection
  reviewed (190/1060 exact BLOCK IDs)**,
  **172/241 source REVIEW_PENDING (870 IDs)**.
  All patched NeoForge bytecode, Planet runtime/
  adapter and gameplay acceptance gates for all
  241 entries remain REVIEW_PENDING.
  No Java patch/build/game client tests this turn.
- Created new independent
  `docs/phases/phase-02/02e-sculk-vibration-sensor-owners.md`.
  Exactly **SculkSensorBlock** (1 ID,
  `minecraft:sculk_sensor`) and
  **CalibratedSculkSensorBlock** (1 ID,
  `minecraft:calibrated_sculk_sensor`)
  are next two still-pending classes/2 IDs.
  `VibrationSystem`, SculkSensorBlockEntity,
  CalibratedSculkSensorBlockEntity essential
  nonregistered source owners.
  SculkShriekerBlock and SculkCatalystBlock,
  LightningRodBlock all still pending separate
  owner families.
- **NEXT FIRST unfinished microtask Stage 3A-7.1**,
  new 02e card checkbox 1, original compiled
  NeoForge 21.1.215 five method declaration
  owners / exact IDs and full pinned 1.21.1
  game-event vibration/BlockEntity signal
  pathways for 2 new concrete classes.
  One bounded research-only GitHub commit,
  checkpoint then stop.

### 2026-10-10 micro-checkpoint — Stage 3A-7.1 sculk vibration sensor source+compiled owners

- Active card `docs/phases/phase-02/02e-sculk-vibration-sensor-owners.md`
  checkbox **1 DONE**, checkboxes 2–4 open.
  New report:
  `docs/research/PHASE2_STAGE3A_SCULK_VIBRATION_SOURCE_OWNER_AUDIT_1_21_1.md`.
- Original NeoForge 21.1.215 unmodified CI ZIP
  artifact 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Exact BLOCK registry IDs:
  `minecraft:sculk_sensor` and
  `minecraft:calibrated_sculk_sensor`.
  SculkSensorBlock 5 nearest declaration
  owners SculkSensorBlock, BlockBehaviour,
  SculkSensorBlock, BlockBehaviour, Block;
  calibrated CalibratedSculkSensorBlock,
  BlockBehaviour, SculkSensorBlock,
  BlockBehaviour, Block. Plain sensor
  has no FACING, calibrated 4 horizontal FACING.
- Pinned Minecraft 1.21.1 comparative
  SculkSensorBlock/CalibratedSculkSensorBlock,
  SculkSensorBlockEntity/
  CalibratedSculkSensorBlockEntity,
  VibrationSystem (Listener/Data/User/Ticker)
  and BaseEntityBlock sources reviewed.
  BE event listener physical radius plain 8,
  calibrated 16; plain ACTIVE 30 ticks,
  calibrated ACTIVE 10, cooldown both 10.
  Separate event vibration BE ticker
  and scheduled phase-advancing BLOCK tick;
  comparator analog returns BE last
  vibration frequency only when ACTIVE.
  Calibrated block `getSignal` queried
  Direction==FACING output suppression,
  BE `getBackSignal` physical opposite
  neighbor plus Level.getSignal argument
  frequency filter. Physical raw Euclidean
  event travel/occlusion, 3×3 world XZ
  adjacent-chunk gate, six physical
  resonator checks and world-below
  neighbor notifications identified.
  `stepOn` forced vibration and
  WATERLOGGED water tick paths found.
- Two classes newly promoted source+reflection
  only: **71/241 SOURCE_REVIEWED_INTEGRATION_PENDING
  (192/1060 registered BLOCK IDs)**,
  **170/241 REVIEW_PENDING (868 IDs)**.
  SculkShriekerBlock, SculkCatalystBlock
  and LightningRodBlock still pending.
  All 241 patched ASM/Planetary integration
  and gameplay acceptance fields REVIEW_PENDING.
  No production Java/CI/client/gameplay
  run in this research package.
- **NEXT FIRST unchecked microtask 3A-7.2**:
  independently check actual original ITEM
  `placed_block` rows for both sensor ID,
  optional item state components, BE/game
  event and direct block writer bypasses.
  One separate bounded GitHub commit then stop.

### 2026-10-10 micro-checkpoint — Stage 3A-7.2 two exact ITEM BlockItem creators and BE/phase event writers

- Active card `docs/phases/phase-02/02e-sculk-vibration-sensor-owners.md`
  **2/4 bounded tasks DONE**, 3–4 still open.
  Source report:
  `docs/research/PHASE2_STAGE3A_SCULK_ITEM_ALTERNATE_AUTHORS_1_21_1.md`.
- Reopened untouched original NeoForge
  21.1.215 CI ZIP artifact 11643813158
  SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`: 1060 BLOCK registry
  rows, 1333 ITEM rows, 1712 properties.
  Exact ITEM.placed_block join for
  minecraft:sculk_sensor and
  minecraft:calibrated_sculk_sensor finds
  precisely **one same-named ITEM per BLOCK**,
  `BlockItem>Item` concrete BlockItem class
  both cases, seven placing lifecycle
  compiled owners `BlockItem` both;
  separate `Item.use` owner Item.
  Original two-row canonical SHA256
  `6225d8898725346b8f37d34d97d64a4a7a86a17a5cf39e02d339f2bc0972fee5`.
- Pinned Minecraft 1.21.1 source:
  `BlockItem.place` initial context
  `getStateForPlacement` (`WATERLOGGED`
  both, plus calibrated `FACING`) then
  `placeBlock`, optional post-place
  `DataComponents.BLOCK_STATE` legal
  property updates, `BLOCK_ENTITY_DATA`
  conditional and BE item components.
  Non-item sources:
  `VibrationSystem.Listener` event route
  and `Ticker` vibration arrival,
  `SculkSensorBlockEntity.VibrationUser`
  last-frequency author, sensor.activate
  ACTIVE/POWER writer, scheduled block
  tick/deactivate PHASE writer, onPlace
  POWER reset, stepOn forced vibration,
  six physical resonator GameEvents,
  BE vibration save/load, calibrated
  BE backside signal read and comparator
  output read, generic StructureTemplate
  direct state/BE write. No confirmed
  sensor-containing shipped worldgen template.
- **No new class source promotion:**
  71/241 SOURCE_REVIEWED_INTEGRATION_PENDING
  (192/1060 BLOCK IDs);
  170/241 REVIEW_PENDING (868 IDs).
  SculkShrieker/SculkCatalyst/LightningRod
  still pending. All patched ASM,
  Planet adapter/runtime and gameplay
  gates for 241 entries REVIEW_PENDING,
  no Java, CI, server/client, game tests.
- **NEXT FIRST unchecked microtask 3A-7.3**:
  source canonical calibrated FACING,
  physical backside target + API signal
  query-side, vibration Vec3 listener
  world-coordinate transport, loaded chunks,
  six-face seam/corner, water and shape
  fixture set; ONE docs checkpoint commit,
  then stop.

### 2026-10-10 micro-checkpoint — Stage 3A-7.3 sculk six-face calibrated direction, physical VibrationSystem and 19 tests

- Active card `docs/phases/phase-02/02e-sculk-vibration-sensor-owners.md`
  **3/4 bounded research tasks DONE**,
  task 4 still unchecked.
  Report:
  `docs/research/PHASE2_STAGE3A_SCULK_SIX_FACE_VIBRATION_SIGNAL_CONTRACT_1_21_1.md`.
- Read actual Planetary branch `2.0`
  `PlanetFace` six true local tangent
  basis vectors, stable per-physical-cell
  `PlanetBlockStateFrame`,
  `PlanetBlockFrameContext` crossing
  traversal charts, `PlanetBlockNeighborQuery`
  target physical cell and target local
  inbound face, `PlanetBlockRuntime`,
  `PlanetBlockShapeRuntime` outermost
  VoxelShape. Pinned comparative 1.21.1
  sculk/calibrated class and BE sources
  and complete `VibrationSystem` receiver.
  No true ASM/Mixin/dispatch audit.
- Four-value calibrated `FACING` remains
  source-local tangential NORTH/EAST/SOUTH/
  WEST; on POS_X local EAST means physical
  world -Y and backside WEST physical +Y.
  Separate physical backside BlockPos from
  `Level.getSignal(neighborPos, direction)`
  queried-side and target canonical port.
  `getSignal` suppresses queried FACING,
  inherited direct signal requires queried UP,
  analog comparator reads BE last frequency
  only while ACTIVE. Phase 7A must confirm
  actual NeoForge caller API convention.
- `VibrationSystem.Listener` and ticker
  preserve physical world Vec3 distance,
  world-ray occlusion and actual world-XZ
  3×3 chunk loaded/ticking gate; don't
  bend vibration ray/topology at seams.
  Base radius8, calibrated16; `stepOn`
  forced event path may require physical
  collision dispatch audit. Physical six
  resonator positions preserved, separate
  potential world-below neighbor callback
  local semantic discrepancy.
- Canonical local sensor VoxelShape 8/16-high;
  model, stepOn and animateTick world-Y
  particles Phase3 pending. WATERLOGGED
  schedules fluid tick (Phase5) and silences
  audio; vibrations/POWER still processed.
  BE server Ticker vibration travel distinct
  from scheduled block ACTIVE plain30/
  calibrated10 then COOLDOWN10 tick
  and from fluid tick. All crossphase
  tests (19 vanilla controls, all faces,
  seams and corners, chunk tick gates,
  vibrations, queried redstone, water,
  analog and BE save/load) specified,
  **NONE EXECUTED**.
- **NO change to original 16-col disposition:
  71/241 source+reflection reviewed classes
  (192/1060 registered BLOCK IDs),
  170/241 source pending (868/1060 IDs)**.
  All 241 patched NeoForge ASM, Planet
  adapter and gameplay acceptance fields
  REVIEW_PENDING. No Java/build/game tests.
- **NEXT FIRST unchecked task 3A-7.4**,
  card task 4: independent original
  NeoForge 21.1.215 CI ZIP roster+71
  reviewed classes/192 actual IDs + five
  nearest full-signature declaring owners,
  all 170 class pending statuses and
  ASM/gameplay gates, queue next owner
  family, ONE research-only GitHub commit.

### 2026-10-10 micro-checkpoint — Stage 3A-7.4 original immutable NeoForge 71-class/192 ID/five-owner reconciliation

- `docs/phases/phase-02/02e-sculk-vibration-sensor-owners.md`
  **all 4 of 4** sculk vibration/calibrated
  sensor research subtasks DONE, **not**
  Stage 3A/Phase 2 or game acceptance.
  Reconciliation report:
  `docs/research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md`.
- Reopened original immutable 21.1.215
  CI artifact 11643813158 SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`,
  original BLOCK 1060 rows/241 distinct
  concrete Java class names, ITEM 1333
  rows and state property 1712 rows.
  Extracted exact registry BLOCK ID
  strings and five effective nearest
  declaring owners for every one of
  71 source+reflection reviewed
  classes, using full qualified
  method parameter signatures.
  Verified 192 IDs and all 71×5
  declarations against live GitHub
  16-column class ledger. Original vs
  ledger diagnostic FNV values all MATCH:
  roster `0xf188a064`,
  reviewed IDs `0x8a6840ba`,
  declaring owners `0xa81e5835`,
  combined `0x59d9c0aa`.
- **Full status unchanged: 71/241
  SOURCE_REVIEWED_INTEGRATION_PENDING**
  (**192/1060 exact BLOCK IDs**),
  **170/241 source REVIEW_PENDING
  (868/1060 IDs)**.
  All 241 class rows retain 3 patched
  NeoForge ASM, Planet adapter and
  gameplay `REVIEW_PENDING` verdicts.
  No Java code/CI/Minecraft gameplay
  or NeoForge patched method body audit.
- Created `docs/phases/phase-02/02f-sculk-shrieker-catalyst-owners.md`
  **NEXT 0/4**, exact original
  `SculkShriekerBlock` /
  `minecraft:sculk_shrieker`
  (can_summon,shrieking,waterlogged)
  and `SculkCatalystBlock` /
  `minecraft:sculk_catalyst`
  (bloom), two still source-unreviewed
  concrete block classes/2 IDs.
  Pinned source owner routing
  reveals two different branches:
  shrieker BE `VibrationSystem.Listener`,
  player shriek, 90-tick reset,
  WardenSpawnTracker/SpawnUtil and
  physical entity bounds; catalyst BE
  `CatalystListener` on ENTITY_DIE,
  `SculkSpreader.ChargeCursor`,
  BLOOM and 8-tick reset.
  This is preliminary source routing,
  **NOT** 3A-8.1 full compiled owner
  audit/source promotion.
  `LightningRodBlock` remains pending
  a separate weather/lightning family.
- **NEXT FIRST unchecked Stage 3A-8.1**,
  new 02f card checkbox 1:
  source+original compiled NeoForge
  class identity / exact 2 BLOCK IDs,
  five original declaring method
  owners and full pinned BE/vibration/
  catalyst spread alternative writer
  graph. One independent research
  GitHub commit and checkpoint then STOP.

### 2026-10-10 checkpoint Stage 3A-8.1 two actual registered sculk shrieker/catalyst owners

- Active card `docs/phases/phase-02/02f-sculk-shrieker-catalyst-owners.md`
  checkbox **1/4 DONE**, 2–4 open.
  Full research doc
  `docs/research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_SOURCE_OWNER_AUDIT_1_21_1.md`.
- Original immutable NeoForge 21.1.215
  CI artifact 11643813158 ZIP SHA256
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Original 1060 BLOCK
  rows/241 registered concrete classes,
  1333 ITEM rows and 1712 properties.
  SculkShriekerBlock registry ID
  `minecraft:sculk_shrieker`, full
  5 nearest declaring owners
  SculkShriekerBlock/BlockBehaviour/
  SculkShriekerBlock/BlockBehaviour/Block.
  SculkCatalystBlock ID
  `minecraft:sculk_catalyst`, owners
  Block/BlockBehaviour/BlockBehaviour/
  BlockBehaviour/Block. Tick overridden
  by each concrete class, randomTick
  inherited BlockBehaviour. Both
  have NO orientation property.
- Pinned comparative 1.21.1 Java
  SculkShriekerBlock + BE (VibrationSystem
  listener radius8, warning level
  and WardenSpawnTracker, player-only
  input including `stepOn`, CAN_SUMMON
  and peaceful/gamerule conditions,
  SHRIEKING BE state writer, 90-tick
  block reset, world-physical SpawnUtil
  5-XZ/6-Y ON_TOP_OF_COLLIDER);
  SculkCatalystBlock + BE (direct
  CatalystListener GameEvent.ENTITY_DIE
  radius8 not VibrationSystem, single
  XP consumption, BLOOM BE writer
  plus 8-tick block reset, per-server
  BE-tick SculkSpreader cursor updates,
  death event world UP+0.5 charge pos,
  SCULK_SOUL world Y+1.15 particle).
  Also reviewed SculkSpreader.ChargeCursor,
  SculkBehaviour, SculkVeinBlock,
  MultifaceSpreader, SculkBlock and
  VibrationSystem as extra graph owners.
  SculkBlock growth may direct-setBlock
  sensor/shrieker at global pos.above
  and set CAN_SUMMON based on worldgen
  mode. Original exact ITEM.placed_block
  and complete direct worldgen creator
  join still NEXT task.
- Promoted ONLY two verified concrete
  classes/2 IDs. Ledger **73/241
  SOURCE_REVIEWED_INTEGRATION_PENDING
  (194/1060 registered BLOCK IDs)**,
  **168/241 REVIEW_PENDING (866 IDs)**;
  all 241 patched NeoForge bytecode,
  Planet adapter and gameplay gates
  `REVIEW_PENDING`. LightningRod
  source still pending. Java/runtime/
  client/server tests NOT run.
- **NEXT FIRST unfinished Stage 3A-8.2**:
  original 1333-ITEM `placed_block`
  join for two exact classes/IDs,
  distinguish item, `DataComponents.BLOCK_STATE`,
  BE/structure/writer, growth/worldgen
  creation, separate one-commit report.
  STOP after exactly one checkbox.

### 2026-10-10 checkpoint Stage 3A-8.2 — sculk exact ITEM and alternate worldgen state writers

- Card `docs/phases/phase-02/02f-sculk-shrieker-catalyst-owners.md`: **2/4 DONE**, checkbox 3 next. Full evidence in `PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_ITEM_WORLDGEN_ALTERNATE_AUTHORS_1_21_1.md`.
- Independently re-downloaded original CI artifact 11643813158: ZIP SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`, original 1060 BLOCK/241 concrete classes; 1333 ITEM; 1712 state-property rows. Exact `ITEM.placed_block` join yields **two** ordinary `BlockItem` registry items, `minecraft:sculk_shrieker`, `minecraft:sculk_catalyst`, no subclass/alias; 7/7 placement declaring owners BlockItem, separate `Item.use`. Original joined row SHA-256 `ac59674578124bb46a566e7a22f7432d3814acc6dfba22859c43578f89adaf0b`.
- Pinned comparative 1.21.1 BlockItem `DataComponents.BLOCK_STATE` may override legal `CAN_SUMMON` or `BLOOM` after default item placement; distinct `BLOCK_ENTITY_DATA` and BE components apply before `setPlacedBy`.
- Confirmed **actual** vanilla `SculkPatchFeature.place` independent of BlockItem: worldgen SculkSpreader to SculkBlock pos.above() growth/conditional CAN_SUMMON=true, direct catalyst at origin with world-below support/chance, and direct CAN_SUMMON=true rare shrieker at ±2 world XZ / below UP sturdy support. Configured `sculk_patch_deep_dark` extraRareGrowths=0; `sculk_patch_ancient_city` 1..3 attempts; both catalystChance=0.5 with support gate. `CavePlacements` links placed-feature registrations. Generic StructureTemplate saved state/BE path exists, but no specific shipped NBT structure asserted.
- **No class promotion:** 73/241 source+reflection declaration reviewed (194/1060 IDs), 168/241 remaining (866/1060 IDs); all 241 patched NeoForge ASM, Planet adapter and gameplay fields still REVIEW_PENDING. No Java code change, build, CI or gameplay PASS.
- **NEXT FIRST Stage 3A-8.3**, checkbox 3: explicit six-face canonical-vs-physical contract and future tests for item vs worldgen, growth/support/BE, seams, corners and vanilla control. One bounded docs commit then STOP.

## Resume procedure after interrupted answer

1. Read AGENTS.md, this checkpoint, the latest roadmap
   Phase-2 subsection and tail of AI_CONTEXT.md on branch 2.0.
2. Inspect exact branch HEAD and existing stage outputs.
3. Resume the FIRST stage lacking a completed artifact.
4. Commit an independently useful artifact before starting
   the next stage. Update roadmap/AI_CONTEXT statuses.
5. Never use giant one-shot analysis/implementation in a
   response; do not rerun existing research, do not overwrite
   accepted game behavior. `git diff`/CI source verdicts
   must match the last code-changing commit, not docs commits.

## Current acceptance

Confirmed by user before new Phase-2 audit: core particle motion,
debris, torches and seam/load; block placement and orientation
on side/bottom faces remain BROKEN/incomplete. Initial family
patches exist but are NOT end-to-end accepted. Latest P01-P40
mechanism coverage research and auto-census are NOT equivalent
to completed Phase-2 implementation.
