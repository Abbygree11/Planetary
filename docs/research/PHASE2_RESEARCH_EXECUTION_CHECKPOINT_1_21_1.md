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
