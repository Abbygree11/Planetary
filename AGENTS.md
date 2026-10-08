# Planetary development protocol

This file is the mandatory working agreement for every AI/coding session on
Planetary.

**Read this file before planning, researching, editing code, or proposing the
next implementation step. Do not rely on chat memory as the source of truth.**

Repository: `Abbygree11/Planetary`  
Working branch: **`2.0` only**  
Target: Minecraft **1.21.1**, NeoForge **21.1.215**, Java 21.

Never write to `main`.

## 1. Canonical project documents

Each document has one job:

- `AGENTS.md` — HOW development must be performed. This file is canonical for
  process.
- `docs/IMPLEMENTATION_PLAN.md` — WHAT remains to be built, phase ordering,
  dependencies, status, acceptance gates and newly discovered work.
- `docs/AI_CONTEXT.md` — continuity log: current architecture, verified
  behavior, active bugs, important decisions, failed approaches and manual
  acceptance results.
- `docs/research/*.md` — detailed technical research for individual Minecraft
  mechanisms/subsystems.

If a new idea appears, an old idea is rejected, scope changes, a dependency is
discovered, or a bug is reassigned to another subsystem/phase, update the
roadmap in the relevant place. Do not leave plan changes only in chat or only in
`AI_CONTEXT.md`.

## 2. Mandatory start-of-session procedure

At the beginning of every new chat/session:

1. Read this file.
2. Read the active/relevant sections of `docs/IMPLEMENTATION_PLAN.md`.
3. Read the latest relevant parts of `docs/AI_CONTEXT.md`.
4. Read `docs/research/GRAVITY_MECHANISM_MAP_1_21_1.md` when the work can be
   affected by local gravity/frame semantics.
5. Read `docs/research/PORTABILITY_STRATEGY.md` before designing a new runtime
   mechanism or changing an integration boundary.
6. Read the research document(s) for the subsystem being touched.
7. Inspect the actual current `2.0` branch and recent relevant commits/files.
8. Identify the last manually accepted/known-good behavior before changing
   runtime code.

GitHub/repository state wins over remembered chat context.

Do not ask the user to repeat process rules that are already documented here.

## 3. Roadmap discipline

The roadmap is a living execution plan, not a wishlist.

Before implementing anything, identify which phase/subsystem OWNS the behavior.

When new information appears:

- add newly discovered work to the correct phase;
- update existing items when the intended architecture changes;
- remove or rewrite rejected ideas instead of leaving contradictory plans;
- record dependencies between phases;
- distinguish implemented, pending acceptance, manually verified and rolled-back
  work;
- do not mark a runtime item DONE/PASS until acceptance actually happened.

Do not fix a generic cross-cutting problem with a one-off special case in an
unrelated subsystem merely because that is where it was first noticed. Record
and defer it to the owning common mechanism when appropriate.

Example: exact-edge block positioning is a generic block-frame/edge-policy
problem, not a torch-specific offset problem.

## 3A. Roadmap phases are organized by engine mechanisms

Roadmap ownership is based on the INTERNAL VANILLA/NEOFORGE MECHANISM being
adapted, not on a flat list of block/entity names.

Canonical classification:
`docs/research/GRAVITY_MECHANISM_MAP_1_21_1.md`.

A concrete Minecraft object may legitimately appear in multiple phases because
different behaviors have different owners.

Examples:
- rail placement/support + RailState graph -> block-semantics phase;
- powered/detector rail signaling -> signals/automation phase;
- minecart-on-rail movement -> entity/vehicle phase;
- rail rendering -> render phase;
- door placement/pairing -> block-semantics phase;
- door redstone -> signals phase;
- door pathfinding -> AI phase.

Before adding a class-specific runtime patch:

1. identify the visible failing behavior, not merely the class name;
2. identify the exact vanilla call path that owns that behavior;
3. inspect the superclass/base family;
4. inspect sibling classes using the same family/mechanism;
5. check whether an existing shared Planet boundary should cover the family;
6. update the mechanism map and roadmap when ownership/scope changes.

Prefer this hierarchy:

    stable engine boundary
        -> base-family adapter
            -> explicit algorithm-family adapter
                -> class-specific adapter only when behavior is genuinely unique

Do not call an object "done" because one of its mechanisms works.

## 3B. Cross-version portability is a hard architecture gate

Canonical strategy: `docs/research/PORTABILITY_STRATEGY.md`.

Planetary is currently implemented for Minecraft 1.21.1 / NeoForge 21.1.215,
but new work must be structured so newer Minecraft ports primarily replace thin
integration adapters instead of rewriting core Planet semantics.

Required dependency direction:

    stable Planet semantic/core logic
        <- Minecraft/NeoForge adapters
            <- thin Mixins / hooks / entrypoints

Rules:
- keep topology/frame/graph/shape/entity semantic algorithms out of mixins when
  practical;
- mixins should collect vanilla context, call stable Planet helpers, adapt the
  result and return to vanilla;
- localize version-specific Minecraft implementation details;
- prefer public/protected/NeoForge boundaries over private accessors and
  ordinal/local-variable-dependent injections;
- do not copy large vanilla methods when a narrow argument/result adapter can
  preserve the original implementation;
- when copying/reimplementing vanilla logic is unavoidable, isolate it as an
  explicit version-sensitive hotspot and add vanilla-equivalence tests;
- preserve codecs, capabilities, model extensions and other standard
  Minecraft/NeoForge extension points;
- Planet-owned persistent formats must be versioned/migratable;
- pure semantic tests should survive a Minecraft version port with little or no
  change.

Before expanding a new runtime mechanism across many classes, verify that its
core logic can remain unchanged if the exact vanilla hook/signature moves in a
future Minecraft version.

### Reserved Mixin package boundary (NeoForge 1.21.1)

Any package declared as `package` in a Mixin config (currently
`dev.planetary.mixin` in `planetary.mixins.json`) is reserved by the
Mixin transformer. **Never place ordinary helper classes, unit-test classes,
records or utility classes in that package.** Put semantic helpers in
`dev.planetary.gravity`/`world`/`api` as appropriate, and place tests in a
matching non-mixin source package. The reserved package is for actual Mixin
and accessor classes only.

This is not merely a style preference: the NeoForge Gradle test runner can
throw `IllegalClassLoadError: ... is in a defined mixin package ... and cannot
be referenced directly` before executing even one JUnit method. A runtime
helper in the same reserved namespace can fail later while applying Mixins.
Check both `src/main` and `src/test` before committing new helpers/tests.

### Bytecode-verified Mixin INVOKE sites

For every `@At(value = "INVOKE", target = "...")` or
`@ModifyArg(s)`/redirect that hooks a potentially inherited method,
**verify the actual JVM call-site owner in the exact target-version
bytecode**, not the class declaring the invoked method.
`this.inheritedMethod(...)` generally emits a concrete
subclass-owned `INVOKEVIRTUAL`, while `super.method(...)` emits
`INVOKESPECIAL`. The owner and descriptor are both part of the
Mixin matching contract.

When introducing or repairing such an anchor, add an ASM test that
reads target-class bytecode AND compiled Mixin annotations to check
the exact owner, signature and expected call count. Tests live outside
the reserved Mixin package and must not load mixin classes via
reflection. If a Mixin fails to apply during client startup, audit
other shared-family inherited INVOKE hooks before requesting another
round of user startup tests. Never use `require = 0` just to conceal
a mismatch.

### Injection handler signatures, not just @At targets

A valid bytecode `@At(target=...)` and a passing JVM instruction-owner
test do NOT guarantee an injector can apply. On Minecraft 1.21.1 we
saw `@ModifyArg` target `Iterable.forEach(Consumer)` match correctly,
but client startup FAIL because its callback declared
`(Consumer, BlockState, Level, BlockPos, RandomSource)`.

**Handler contracts differ by injector type:**
- `@ModifyArg` accepts only the single invoked argument being changed.
  It cannot append enclosing target-method arguments.
- `@ModifyArgs` accepts `Args` plus optional enclosing method arguments;
  it incurs argument-bundle/boxing overhead.
- `@Redirect` for instance INVOKE accepts the invoked **receiver**
  followed by invoked call args, then optional enclosing method args.
  The handler's staticness must match the enclosing target method.
- `@Inject` has its own CallbackInfo/context conventions.

Before committing a new/changed Mixin injection, verify the chosen
injector kind's official API contract and add an ASM classfile test
checking BOTH the `@At` owner+descriptor and the **full handler
method descriptor and staticness**. Never assume that a successful
`test.ps1` establishes Mixin can transform the target at startup.

### NeoForge-patched methods and Mixin annotation arrays

Do not derive exact JVM call-site counts solely from vanilla/Mojang
source. NeoForge patches can deliberately introduce extra INVOKEs:
the 1.21.1 `EnchantingTableBlock.isValidBookShelf` patch adds
`getEnchantPowerBonus`, making TWO `BlockPos.offset(Vec3i)`
calls, versus vanilla's one. Check the target NeoForge patch AND the
actual Gradle-loaded target `.class` bytecode before asserting or
editing injection counts. Preserve such platform extension points.

When ASM-inspecting `@Inject`/`@Redirect`/`@ModifyArgs`
annotations, their `method` property is `String[]`, not a scalar.
Read it using `AnnotationVisitor.visitArray("method")`, assert
the intended method name/count, and do not mistake `null` from a
scalar-only visitor for an actual missing Mixin target.

The `at` member also depends on injector kind. Confirmed against
SpongePowered Mixin source: `@Inject.at()` is an `At[]` array,
whereas `@Redirect.at()` and `@ModifyArgs.at()` each return one
`At`. An ASM visitor must handle `visitArray("at")` ->
`visitAnnotation(null, At-descriptor)` for `@Inject`, as well as
`visitAnnotation("at", At-descriptor)` for the others. Require the
documented number of `@At` entries, instead of silently accepting
`atValue=null` as evidence of a missing annotation.


For server-side world-graph mechanics, trace the FULL consumer chain:
a correct shared `isValidBookShelf` predicate does not automatically
reframe the separate NeoForge `EnchantmentMenu` numerical
`getEnchantPowerBonus` block position. Record it as pending
until a compatible, client/server-consistent adapter exists.




## 3C. Separate discovery/research batches from implementation batches

Canonical global audit plan:
`docs/research/GLOBAL_RESEARCH_BATCH_PLAN_1_21_1.md`.

Do not mix large-scale mechanism discovery with runtime implementation in the
same open-ended work pass.

When the project is in a research/planning stage:

1. choose one substantial bounded mechanism batch;
2. complete its source inventory and representative call-flow analysis;
3. update mechanism ownership, dependencies, portability hotspots and acceptance
   plan;
4. stop at the batch boundary;
5. do NOT write runtime code until the research stage explicitly transitions to
   implementation.

Research batches should be large enough to expose shared mechanisms, but small
enough to finish as a bounded unit. Avoid both extremes:
- one vanilla class at a time;
- "audit all of Minecraft and implement it while discovering it".

During implementation:
- work from the frozen mechanism map;
- reopen research only when new evidence invalidates ownership or uncovers a
  missing mechanism;
- update the roadmap if new research changes phase ownership.

## 4. Research is mandatory BEFORE implementation

Before implementing a non-trivial mechanic, feature, compatibility layer, or bug
fix, perform detailed source research first.

For Minecraft/NeoForge behavior this means, as applicable:

- inspect the exact Minecraft 1.21.1 / NeoForge 21.1.215 source path;
- trace the complete call flow, not only the first method containing the visible
  symptom;
- identify who owns the state and where it changes;
- enumerate all hard-coded world-axis assumptions;
- classify each assumption as PHYSICAL world geometry or LOCAL semantic
  UP/DOWN/EAST/SOUTH/etc.;
- inspect subclasses/alternate code paths that bypass the base implementation;
- inspect client and server paths separately when both exist;
- inspect renderer/physics/emitter paths separately when they are independent;
- preserve RNG call count/order when random behavior is involved;
- preserve vanilla/NeoForge hooks and extension points;
- inspect edge/corner behavior when topology is relevant;
- inspect existing Planetary adapters so the new fix composes with them.

For a bug/regression:

1. establish the last known-good accepted state;
2. narrow the regression window;
3. reproduce the exact vanilla/Planet call flow;
4. explain why the root cause produces the observed symptom;
5. only then edit runtime code.

Do not patch the first plausible line and iterate ten times on symptoms.

If the mechanism is substantial, create/update a focused
`docs/research/<SUBSYSTEM>_1_21_1.md` note BEFORE or alongside implementation.
The note should contain call flow, assumptions, chosen adaptation boundary,
known alternate paths, risks, tests and manual acceptance criteria.

Failed hypotheses and rejected designs are valuable research results: document
why they failed so a future session does not repeat them.

## 5. Implementation principles

Prefer the smallest stable architectural boundary that fixes the whole owned
mechanism.

General rules:

- preserve vanilla physical XYZ collision/world storage unless there is a proven
  reason not to;
- adapt semantic local-frame meaning at stable boundaries;
- prefer pure transformation/helpers with unit tests over duplicated switch
  tables;
- never globally redefine `BlockPos`, `Direction`, axes or raw XYZ based on
  hidden context;
- preserve ordinary non-Planet worlds as vanilla/pass-through;
- preserve NeoForge/vanilla extension points for mod compatibility;
- do not mutate registered/shared models or states when a wrapper/adapter is
  sufficient;
- do not duplicate vanilla RNG or consume additional random values accidentally;
- do not reimplement a large vanilla method when a narrow argument/result
  adapter can preserve the rest;
- avoid speculative changes to multiple independent mechanisms in one patch;
- keep one coherent runtime hypothesis per patch when debugging.

When a broad patch is not manually accepted, do not keep stacking fixes on top
of it indefinitely. Stop, research the new evidence, and roll back to the last
known-good state when that gives a cleaner base.

## 6. Testing before runtime acceptance

Add deterministic tests wherever the behavior can be represented without the
game client:

- frame transforms and round trips;
- direction/shape mappings;
- edge/seam math;
- vanilla-equivalence on +Y;
- all six gravity faces where relevant;
- regression tests for previously found bugs.

Tests must reproduce vanilla numeric details accurately, including float-vs-
double behavior when it matters.

A green unit test suite does NOT prove runtime behavior that depends on
Minecraft rendering, collision timing, client/server interaction, worldgen,
random ticks, etc.

### GitHub Actions must be green before asking for a local test

A GitHub Actions `.github/workflows/test.yml` build runs on each push
to branch `2.0`, using Java 21 and Gradle 8.12. It executes
`compileJava compileTestJava` and then `test`, and uploads failed
test reports.

**Mandatory when changing source code or tests:**
1. Commit changes to branch `2.0`.
2. Read the corresponding GitHub Actions run status and failures.
3. If compile or JUnit fails, fix root cause and repeat until the
   HEAD commit has green compile AND test jobs.
4. Only after green CI ask the user to run Minecraft client or
   manually check real gameplay, if needed.

Do not ask the user to find compiler errors that GitHub Actions
can find. The first workflow run (2026-10-08) caught this contract:
commit `afceb3b0` ran successfully after correcting an invalid
`public AnnotationVisitor visit(String,Object)` override to
`public void visit(String,Object)`.

A green CI validates compilation and JUnit only; it does NOT validate
Mixin class transformation in a real Minecraft client or the
behavior of Planet mechanics in gameplay. Such acceptance remains
distinct and must be reported honestly. If workflow is broken,
unavailable or not green, disclose that explicitly and do NOT
pretend the change was build-tested.


## 7. Manual acceptance is a hard gate

Never claim runtime success merely because code compiles, unit tests pass, or a
screenshot looks plausible.

For runtime behavior, PASS requires either:

- explicit user confirmation from in-game testing; or
- a truly deterministic automated test that fully covers the runtime property.

A screenshot may help diagnose a problem, but do not mark a runtime item PASS
based only on your own visual interpretation if the user has not confirmed it.

Use these status meanings consistently:

- **IMPLEMENTED / acceptance pending** — code exists, user has not yet accepted
  runtime behavior.
- **PASS / verified** — user explicitly confirmed runtime behavior or complete
  deterministic coverage exists.
- **ROLLED BACK / rejected** — implementation was not accepted and is not the
  active runtime design.
- **PLANNED** — no stable runtime implementation yet.

## 7A. Batch acceptance for shared subsystems

Do not force the user to manually test every tiny implementation step when
several pending changes belong to the same subsystem and share the same internal
mechanisms.

For a subsystem such as particles, fluids, rendering, redstone, worldgen, etc.:

1. audit the complete relevant vanilla/NeoForge mechanism first;
2. classify concrete classes/paths by shared behavior;
3. implement shared adapters and all coherent owned cases;
4. add deterministic tests during implementation;
5. keep roadmap status as implementation/acceptance pending;
6. perform ONE subsystem-level manual acceptance pass with a matrix covering all
   implemented cases.

Ask for an intermediate manual checkpoint only when it is necessary to avoid
building substantial work on an unverified foundation, for example:
- client/server cannot start;
- mixin/apply/load failure;
- serialization/data migration risk;
- the subsystem's core boundary itself is uncertain and cannot be validated
  deterministically;
- a previous broad implementation already caused regressions and the new base
  abstraction must be proven before reuse.

Do not turn every class-specific patch into a separate user test if it can be
covered by the final subsystem acceptance matrix.

## 8. Mandatory post-change verification checklist

After EVERY runtime-affecting change, meaningful fix/refactor, or worldgen
change, give the user a numbered verification checklist/status.

When applicable, begin with exactly:

`git pull && .\test.ps1 && .\run-client.ps1`

When a subsystem is in documented BATCH ACCEPTANCE mode, do NOT ask the user to
repeat full gameplay acceptance after every internal class/family patch.
Instead:

- request build/startup checks only when an intermediate checkpoint is actually
  needed under section 7A;
- otherwise state that gameplay acceptance is intentionally deferred to the
  subsystem matrix and continue implementation;
- provide the full gameplay checklist once the coherent subsystem batch is ready.

The final/subsystem gameplay checklist must:

1. separate build/startup from gameplay checks;
2. test the primary behavior;
3. state the expected result for every check;
4. include regression checks for behavior that previously worked;
5. include +Y as the vanilla-equivalence baseline when gravity is involved;
6. include all relevant rotated faces;
7. include edge/corner cases when the mechanism owns them;
8. include performance/smoke checks when appropriate;
9. avoid vague wording such as "check that it works".

This reconciles the permanent post-change verification rule with the permanent
batch-acceptance rule: evidence is mandatory, but redundant user gameplay
testing is not.

## 9. Regression and rollback procedure

If the user reports a regression:

1. do not declare the previous patch successful;
2. record the failed acceptance result;
3. compare against the last accepted behavior/commit;
4. investigate the exact call path before editing again;
5. prefer reverting an unaccepted broad patch over accumulating speculative
   patch-on-patch corrections;
6. preserve already accepted independent fixes;
7. document the rejected approach and root cause in `AI_CONTEXT.md` and/or the
   subsystem research note;
8. update the roadmap if the architecture/status changed.

Maintain a clear distinction between accepted baseline and experimental runtime
work.

## 10. Documentation after implementation

Whenever project state materially changes, update the appropriate sources before
ending the work:

- roadmap: plan, ownership, dependencies and status;
- AI_CONTEXT: architecture/continuity, current bug, acceptance/rejection,
  important commit/state transitions;
- research: source findings, precise mechanism, failed hypotheses, adaptation
  boundary and acceptance matrix.

Do not blindly duplicate the same prose everywhere. Keep each document focused
on its role.

## 11. Compatibility and performance gates

Every phase must consider whether the change:

- runs only when Planet behavior is active;
- adds per-tick/per-frame allocations;
- adds unbounded maps/caches;
- performs repeated frame lookup unnecessarily;
- causes duplicate vanilla + custom work;
- breaks normal NeoForge hooks or mod interoperability;
- rebuilds reusable geometry/models every frame;
- introduces client/server disagreement.

Prefer standard Minecraft/NeoForge boundaries so compatible mods inherit Planet
semantics automatically. Raw custom world-axis math in third-party mods may need
explicit integration; do not promise universal compatibility without evidence.

## 12. Communication after a code change

After committing a runtime change, tell the user concisely:

- what root cause was found;
- what architectural boundary changed;
- what was intentionally NOT changed;
- current `2.0` HEAD/commit when useful;
- the mandatory numbered verification checklist.

Do not say "fixed", "works", "build is successful" or mark a roadmap item PASS
until the corresponding evidence actually exists.
