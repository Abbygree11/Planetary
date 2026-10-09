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
