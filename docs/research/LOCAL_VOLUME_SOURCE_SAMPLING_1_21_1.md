# Phase 4 / Phase 1: local volumetric emitter queries (Minecraft 1.21.1)

Status: SOURCE AUDIT COMPLETE. Ordered traversal pure tests confirmed
BUILD SUCCESSFUL by user. Enchanting-table and spore-blossom
integration IMPLEMENTED / JUNIT + CLIENT STARTUP + GAMEPLAY PENDING.

## Source paths (readable vanilla 1.21.1)

Reference: `hackersense/OptiFine-Source` branch main,
`1.21.1/net/minecraft`:
- `world/level/block/EnchantingTableBlock.java`
- `world/inventory/EnchantmentMenu.java`
- `world/level/block/SporeBlossomBlock.java`

### Enchantment table: functional and visual coupling

`EnchantingTableBlock.BOOKSHELF_OFFSETS` is a
5x5 ring at local Y=0 or 1 with |local X| or |local Z| = 2.
`EnchantingTableBlock.isValidBookShelf(Level, tablePos, offset)`
checks **two positions**:
1. provider at `tablePos.offset(offset)` tagged
   ENCHANTMENT_POWER_PROVIDER;
2. air/transmitter at `tablePos.offset(offset.x/2,
   offset.y, offset.z/2)` tagged ENCHANTMENT_POWER_TRANSMITTER.

`EnchantmentMenu` uses this SAME static `isValidBookShelf` on
the SERVER to calculate enchantment level. `EnchantingTableBlock.animateTick`
uses it on CLIENT after `random.nextInt(16)==0` to spawn ENCHANT
particles, with THREE subsequent nextFloat samples and local
particle source `(tableX+0.5, tableY+2.0, tableZ+0.5)`; the
particle 'velocity' is a sampled local bookshelf-offset vector.

**Therefore a particle-only change would make visuals and real enchant
power disagree.** The authoritative bookshelf membership check must be
adapted in one common Level+BlockPos+local-offset layer used by both
callers, BEFORE touching emission coordinates. Scope is owned jointly
by Phase 1 local multi-cell block topology, Phase 2 block
interaction mechanics, and Phase 4 emitter visuals.
It is NOT sufficient to rotate `Level.addParticle` arguments.

### Spore blossom: conditional candidate volume

`SporeBlossomBlock.animateTick(state, level, source, random)`:
1. `random.nextDouble()` X, fixed LOCAL y=0.7,
   `random.nextDouble()` Z; one FALLING_SPORE_BLOSSOM at
   source local block unit position, zero velocity.
2. **Exactly 14 iterations.** Each draws
   `Mth.nextInt(random,-10,10)` local X offset,
   `-random.nextInt(10)` local Y offset,
   `Mth.nextInt(random,-10,10)` local Z offset,
   **in this order**. Candidate MutableBlockPos set to
   source + these 3 integer offsets.
3. `level.getBlockState(candidate)` then checks
   `!blockstate.isCollisionShapeFullBlock(level,candidate)`.
4. Only if unblocked, draws THREE `nextDouble()`
   local unit-cell coordinates and emits SPORE_BLOSSOM_AIR
   particle at candidate + these coordinates, zero velocity.

Rotating the visual spawn without rotating the CANDIDATE block
and collision/occlusion query is wrong: emits through walls and
selects global-world-Y blocks on rotated faces.
Changing RNG order or doing extra samples is wrong.
The particle's sampled per-cell displacement must follow
the **candidate's transported local frame**, not the source
frame, if the candidate lies past a gravity seam.
`SporeBlossomBlock.canSurvive` and `updateShape` use physical
support / gravity axes and are **Phase 2 block support**, separate
from this animateTick visual query; do not repair those by
canceling particle effects.

## Shared missing engine mechanism: ordered multi-cell local displacement

Current `PlanetBlockFrameContext.step(Direction)` correctly handles
one local step across a gravity seam, and `walk(Direction,steps)`
transports ONE direction. Both remain correct. There is no API
to interpret an entire `BlockPos` displacement `(dx,dy,dz)`
such as bookshelf `(2,1,2)` or 14 spore candidate offsets
`[-10,10] x [-9,0] x [-10,10]` with transport of the **whole
local coordinate basis** after an edge transition.

Do NOT implement this as `sourcePos.offset(localToWorld(dx,dy,dz))`
globally. That is a flat source tangent-plane projection and can
read blocks outside the shell when the range crosses an edge.
Likewise do not call raw `BlockPos.relative()` at the destination
with a source-frame direction after a seam.

Chosen typed traversal policy for simultaneous multi-axis
displacements: **ordered path X, then Z, then Y**, one cell at a
time, with full local basis remapped by `FaceTransform` whenever
a *horizontal* walk crosses to a new gravity face.
- X/EAST moves first (or WEST if dx is negative),
- Z/SOUTH moves next (or NORTH if dz is negative),
- Y/UP moves last (or DOWN if dy is negative).
This defines a deterministic route near cube vertices where
multi-axis displacements can be non-commutative; do not silently
assert that X-then-Z equals Z-then-X at a triple corner.
Direction transport preserves semantic UP/DOWN across surface
edges. A vertical crossing near the *planet core* is a distinct
unsupported frame ownership problem; do not infer body transport
there without evidence. These source samplers live on surface,
not inside core.

Semantic API location: `dev.planetary.world`, not mixin package;
initial implementation should be a pure coordinate projection
from an existing `PlanetBlockFrameContext`, with tests on all
six faces, edges, corners, signed offsets and one-axis equivalence
to existing `walk`. Keep source frame/target frame paired,
not just naked physical positions.

This is a foundation for future client/server adapters;
it must NOT globally modify vanilla `BlockPos`, globally alter
bookcase validation, or install a partial SporeBlossom particle
hook before both complete call paths have been tested.

## Implemented typed traversal foundation (2026-10-08)

`src/main/java/dev/planetary/world/PlanetLocalBlockOffset.java`
exposes `traverse(PlanetBlockFrameContext source, int localX,
int localY, int localZ)` returning the target **physical cell plus
transported traversal chart** in another
`PlanetBlockFrameContext`. The helper changes no Level/BlockPos
behavior by itself and is outside reserved Mixin packages.

It implements the explicitly documented axis order X, Z, Y.
Each cell is resolved through the existing
`PlanetBlockFrameContext.step` engine, and on every horizontal
gravity seam, `FaceTransform.transformDirection` remaps
the **whole remaining EAST/SOUTH local basis**, not only the
direction of the step currently being completed.
Local UP remains relative to the current gravity face.

A radial-frame transition inside the core is rejected as
unsupported instead of guessing an edge transform; this API is
for shell-local source volumes. Integer.MIN_VALUE offsets are
rejected to avoid overflow/unbounded traversal.

`src/test/java/dev/planetary/world/PlanetLocalBlockOffsetTest.java`:
- 450 small signed (dx,dy,dz) samples across all 6 interior faces,
  comparing to direct same-face localToWorld integer projection;
- 24 directed face-edge examples (4 x 6), verifying equivalence
  of one-axis moves with existing `PlanetBlockFrameContext.walk`;
- six 2D X-then-Z seam-crossing samples verifying the SOUTH axis
  is explicitly parallel transported via `FaceTransform`;
- 24 triple-face corner examples (4 corners x 6 faces), with
  deterministic X-then-Z physical route and transported axis;
- zero offset retains the original traversal frame even on a seam;
- rejects unbounded integer offset.

User explicitly confirmed BUILD SUCCESSFUL for the initial pure
PlanetLocalBlockOffsetTest checkpoint. Multi-axis offsets near corners
remain path-defined, not invariant under permuting axes. Runtime
source integration is a NEW unaccepted change after this checkpoint.

## Performance / acceptances

Spore volume might require up to 14*30 steps each tick.
PlanetLocalBlockOffset now has a conservative proven same-face fast path:
if the local radial score stays strictly greater than the absolute
endpoints of both tangent scores throughout the X -> Z -> Y path,
project the offset in one constant-time arithmetic step, resolving
only the final frame. All ties, seam/corner paths and unsupported
radial cases fall back to the existing transported cell-by-cell
algorithm. No persistent maps/caches. This optimization and all
new runtime adapters still require updated Gradle/JUnit validation.

Milestones:
1. [PREVIOUS PURE TEST ACCEPTED; NEW FAST PATH TEST PENDING] typed multi-axis displacement
   with basis transport and exact one-axis equivalence;
2. [IMPLEMENTED / RUNTIME UNACCEPTED] one server+client EnchantingTable
   `isValidBookShelf` membership boundary preserving bookshelf
   valid shape, unique counted providers and menu outcomes;
3. [IMPLEMENTED / RUNTIME UNACCEPTED] client enchantment particle origin+velocity
   adapter while preserving vanilla RNG;
4. [IMPLEMENTED / RUNTIME UNACCEPTED] client SporeBlossom candidate/occlusion
   and emission unit-offset adapter preserving all draws;
5. [PENDING] single combined integration/multi-face/edge
   acceptance batch, with +Y vanilla baseline.

No runtime-related PASS inferred from pure helper code or unit tests.

## 2026-10-08 implementation batch: enchantment and spores

### Shared physical neighbor query

`dev.planetary.world.PlanetLocalBlockProjection.physicalOffset`
resolves `Level + source BlockPos + signed local(dx,dy,dz)`.
On a Planet world it delegates to the tested
`PlanetLocalBlockOffset.traverse` (including +Y near seams);
outside Planet it preserves vanilla `BlockPos.offset`.

### EnchantingTableBlock: common SERVER + CLIENT membership

Common Mixin `EnchantingTableBookshelfGravityMixin` intercepts only
two existing `BlockPos.offset` invocations in static
`EnchantingTableBlock.isValidBookShelf`:
- `offset(Vec3i)` for bookshelf provider;
- `offset(int,int,int)` for half-offset power transmitter.

On Planet, both become physical neighbor locations through the shared
projection. Vanilla `Level.getBlockState`, ENCHANTMENT_POWER_PROVIDER,
ENCHANTMENT_POWER_TRANSMITTER and short-circuit predicate stay unchanged;
server `EnchantmentMenu` and client visual samples both consume this
same static vanilla predicate. No global BlockPos modification, no
global replacement of enchanting power predicate.

Client-only `EnchantingTableParticleGravityMixin` uses one
`@ModifyArgs` on the existing `Level.addParticle` INVOKE in
`animateTick`, rotating sampled local source position and
ENCHANT particle 'velocity' relative to the same source block frame.
RNG draw count/order and particle count/vanilla metadata remain
unmodified.

**Remaining policy gate:** at a 3-face corner multiple authored bookshelf
offsets might converge to one physical provider. The common predicate
preserves vanilla offset iteration and uses the chosen X->Z->Y path;
physical-provider deduplication is a separate enchanting-menu game-rule
decision, not a particle-specific patch. No claim of gameplay acceptance
at corners until the complete Phase-2/4 test matrix confirms intent.

### SporeBlossomBlock: atomic source sampler

Client-only `SporeBlossomParticleGravityMixin` injects at
`animateTick` HEAD and cancels the vanilla method ONLY on rotated
Planet faces. +Y/ordinary worlds execute vanilla unchanged.
`PlanetSporeBlossomSourceRuntime.emit` mirrors the bounded Minecraft
1.21.1 source sampler as a deliberately marked version-sensitive
integration hotspot:
1. two random doubles, one FALLING_SPORE_BLOSSOM particle at local y=0.7;
2. 14 candidate attempts in vanilla order:
   `Mth.nextInt(-10,10)` for X,
   `-random.nextInt(10)` for Y,
   `Mth.nextInt(-10,10)` for Z;
3. determine physical candidate AND its transported chart via the
   ordered local volumetric projection;
4. call the exact vanilla `getBlockState(candidate)` and
   `isCollisionShapeFullBlock(level,candidate)` at that physical cell;
5. only on non-full-block candidate sample three `nextDouble` values
   for local unit sub-cell coordinates, rotate in the destination
   traversal chart, emit one SPORE_BLOSSOM_AIR.

No extra RNG draws, emission attempts, state changes, or sounds. No
global Level.addParticle or getBlockState interception. This is a
deliberate small vanilla source-method copy, isolated for port audits
because no equally narrow call-site hook guarantees the candidate
position and sampled local jitter stay coupled without mutable
thread/global state. The non-particle SporeBlossom canSurvive/updateShape
paths remain owned by Phase 2.

### Regression coverage

New `VolumeParticleMixinContractTest` checks:
- two exact `BlockPos.offset` INVOKEs in isValidBookShelf;
- one enchanting / two spore `Level.addParticle` INVOKEs;
- exact callback JVM descriptors, staticness and `@At` targets for
  two `@Redirect`, one `@ModifyArgs` and one cancellable `@Inject`;
- JSON registration and `defaultRequire=1`.
New `VolumeParticleFrameTest` checks physical ENCHANT source/vector
and subcell spore source across all six gravity faces and over a seam.

BUILD/CLIENT STATUS AFTER THIS BATCH: PENDING USER RUN.
Do not mark this feature or entire Phase 4 gameplay accepted until
source/consumer tests and visual gameplay acceptance complete.


## 2026-10-08 user JUnit failure: NeoForge bookshelf patch / annotation encoding

The user ran `test.ps1` on the preceding enchanting/spore
implementation and reported **TWO failing methods** in
`VolumeParticleMixinContractTest`; Java compilation succeeded and
other failures were not reported. **No runClient from that chained
command was evidenced**, so new Mixins have NOT yet passed startup.

1. `vanillaBookshelfPredicateAndParticleBodiesPreserveTargets`
   expected one `BlockPos.offset(Vec3i)` and one
   `BlockPos.offset(III)` in `isValidBookShelf`. Actual
   NeoForge-transformed test class contains **two**
   `offset(Vec3i)` calls and one `offset(III)`, in that order.
   Exact cause confirmed in the official NeoForge source patch:
   `patches/net/minecraft/world/level/block/EnchantingTableBlock.java.patch`.
   NeoForge replaces vanilla provider tag test with:
   `level.getBlockState(pos.offset(offset)).getEnchantPowerBonus(level,
   pos.offset(offset)) != 0`. Thus the two identical Vec3i offsets
   are **intentional mod-extensibility support**, not a rogue JVM
   call or a reason to suppress one redirect. Existing
   `EnchantingTableBookshelfGravityMixin` provider @Redirect
   targets both calls and maps both to the same physical location.
   The test's exact expected invocation list was corrected to
   `[OFFSET_BLOCK, OFFSET_BLOCK, OFFSET_COORDS]`.

2. `allFourVolumeMixinHandlersHaveExactSignatureAndAtContract`
   read a scalar annotation `method` field via ASM
   `AnnotationVisitor.visit`. Mixin's `@Redirect.method` is
   actually declared `String[]`, so even one method name is
   encoded as an ARRAY visited through `visitArray("method")`.
   The test therefore read `targetMethod=null`, although exact
   `@At` targets, handler descriptors and staticness already
   matched. The test parser now handles the method array and
   requires exactly one expected method name; do NOT modify
   correct production callback signatures to work around this
   test-only parsing error.

**Important newly uncovered GAMEPLAY integration gap, NOT fixed by
the two test corrections:**
`patches/net/minecraft/world/inventory/EnchantmentMenu.java.patch`
in NeoForge adds separate SERVER-side bonus lookups within its
`slotsChanged` calculation:
`level.getBlockState(pos.offset(offset)).getEnchantPowerBonus(level,
pos.offset(offset))` after a successful isValidBookShelf predicate.
These raw `pos.offset(offset)` expressions DO NOT automatically
follow the remapped physical provider positions established by
`EnchantingTableBookshelfGravityMixin`. Therefore
CLIENT + SERVER bookshelf VALIDITY is shared, but SERVER *numerical
enchantment power* may still use the wrong physical location on
rotated faces. This is **Phase-2/4 integration PENDING**, not a
test regression and must be resolved at the actual
NeoForge `EnchantmentMenu` extension boundary, retaining
`getEnchantPowerBonus` and any `EventHooks.onEnchantmentLevelSet`
behavior. Do not replace NeoForge bonus with vanilla fixed +1,
and do not globally intercept `BlockPos.offset`.

Official patch references:
- `https://github.com/neoforged/NeoForge/blob/3c1fcb4f4efd6ed0cf60375e8094902426ddc973/patches/net/minecraft/world/level/block/EnchantingTableBlock.java.patch`
- `https://github.com/neoforged/NeoForge/blob/3c1fcb4f4efd6ed0cf60375e8094902426ddc973/patches/net/minecraft/world/inventory/EnchantmentMenu.java.patch`

Test-only fix implemented in branch 2.0. New Gradle output still
required; Minecraft client startup and enchanting power gameplay
remain NOT ACCEPTED.


## 2026-10-09 second user JUnit follow-up: @Inject.at uses array encoding

After the prior NeoForge offset-count and `method: String[]`
ASM visitor fixes, the user reran tests and reported ONE remaining
failure in `VolumeParticleMixinContractTest.
allFourVolumeMixinHandlersHaveExactSignatureAndAtContract`.

Observed actual compiled `SporeBlossomParticleGravityMixin` hook:
- exact handler JVM descriptor: matches expected;
- handler instance/staticness: matches expected;
- injector kind: `Inject`, correct;
- `targetMethod=animateTick`, correct after previous visitor fix;
- `cancellable=true`, correct;
- `atTarget=null`, expected for HEAD;
- `atValue=null`, **unexpected in the test**, though the source
  declares `@Inject(at = @At("HEAD"))`.

Root cause is another **ASM test parser encoding bug**, not a
runtime source/hook failure. Official SpongePowered/Mixin source
declares `Inject.at(): At[]` whereas `Redirect.at(): At` and
`ModifyArgs.at(): At`. In the class file, a single `@At("HEAD")`
inside an `@Inject` is still visited by ASM as
`visitArray("at")` followed by
`visitAnnotation(null, "Lorg/spongepowered/asm/mixin/injection/At;")`.
The previous test supported only the direct nested
`visitAnnotation("at", ...)` form. Therefore `atValue` stayed null.

Implemented the exact test-only fix:
- both array and direct At forms now delegate to one validated
  `visitAt` visitor for `value`/`target`;
- enforce **exactly one** target method and **exactly one** At
  annotation for each of the four hooks (strict contract, no
  weakening of assertions);
- preserve full handler descriptors, staticness, annotation kind,
  `cancellable`, `HEAD` and `INVOKE` target checks;
- no changes to production Mixins or particle/world behavior.

Official declaration source:
`github.com/SpongePowered/Mixin`,
`src/main/java/org/spongepowered/asm/mixin/injection/Inject.java`
and `Redirect.java`, `ModifyArgs.java`.

**Status: FIX IMPLEMENTED / USER GRADLE RECHECK PENDING**.
Do not interpret one test assertion as all tests passing, and do not
claim client/world bootstrap accepted for the recently added
EnchantingTable/SporeBlossom production Mixins.
NeoForge EnchantmentMenu numeric bookshelf bonus remains an
independent known gameplay gap (unfixed).


## 2026-10-09 user pure JUnit GREEN and NeoForge numeric enchant bonus completion

After `VolumeParticleMixinContractTest` was repaired to handle
`@Inject.at(): At[]`, user explicitly confirmed **BUILD SUCCESSFUL**.
This confirms Gradle tests for the new Phase-4 EnchantingTable / Spore
source integration, but user did NOT provide new client-world startup
evidence after these Mixins. The remaining **functional** issue
previously marked pending was separate NeoForge menu bonus queries.

### Exact NeoForge 1.21.1 menu source

From official `neoforged/NeoForge`
`patches/net/minecraft/world/inventory/EnchantmentMenu.java.patch`,
inside the `ContainerLevelAccess.execute` callback of
`EnchantmentMenu.slotsChanged`:

    float bookcases = 0;
    for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
        if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) {
            bookcases += level.getBlockState(pos.offset(offset))
                .getEnchantPowerBonus(level, pos.offset(offset));
        }
    }
    // Float -> int enchant cost, plus onEnchantmentLevelSet hooks.

The static vanilla predicate is ALREADY projected correctly by
`EnchantingTableBookshelfGravityMixin`; however these TWO explicit
menu `pos.offset(offset)` expressions still use physical XYZ.

Parchment mapping for 1.21.1 and Fabric 1.21.1 independent Mixin
implementations identify the enclosing Java synthetic method as
`EnchantmentMenu.lambda$slotsChanged$0` with exact descriptor

    (Lnet/minecraft/world/item/ItemStack;
     Lnet/minecraft/world/level/Level;
     Lnet/minecraft/core/BlockPos;)V

This synthetic lambda IS an instance method (uses menu fields).
This descriptor/name must be verified again against actual
NeoForge-transformed class bytes by the new test before runtime
startup; the name is a portability-sensitive site.

### Runtime adaptation (IMPLEMENTED / ACCEPTANCE PENDING)

`dev.planetary.mixin.EnchantmentMenuBookshelfPowerMixin` (COMMON
client/server Mixin, registered in `planetary.mixins.json`)
narrowly redirects ONLY the two
`BlockPos.offset(Vec3i)` INVOKEs in
`lambda$slotsChanged$0`.

Its nonstatic redirect handler receives the invoked physical
BlockPos receiver and local Vec3i offset, followed by enclosing
ItemStack, Level and table BlockPos. Both original expressions
are replaced with the SAME
`PlanetLocalBlockProjection.physicalOffset(level, source, dx,dy,dz)`
as the common `isValidBookShelf` validity check.

The returned position is physically authoritative and identical
for BOTH NeoForge consumers:
1. `level.getBlockState(physicalProviderPos)`;
2. `getEnchantPowerBonus(level, physicalProviderPos)`.

The original NeoForge provider's float bonus, block extension hook,
power conversion to enchant costs, `EventHooks.onEnchantmentLevelSet`,
UI random seed and enchantment result logic are untouched.
For non-Planet worlds the shared projector returns the unchanged
`BlockPos.offset(Vec3i)` coordinates. No global BlockPos
override, zero persistent cache/thread-local state, and no copied
enchantment algorithm.

### Regression contract

`src/test/java/dev/planetary/world/
EnchantmentMenuBookshelfPowerMixinContractTest.java` reads
actual NeoForge `EnchantmentMenu.class` bytecode:
- exact lambda name+descriptor and instance method;
- two (no more/no less) calls to
  `BlockPos.offset(Vec3i)` inside the lambda;
- one `getEnchantPowerBonus` call and one
  `EnchantingTableBlock.isValidBookShelf` call;
- compiled Mixin `@Mixin` target, Redirect `method[]`,
  exact `@At(INVOKE)` target, handler JVM descriptor and instance
  staticness;
- common JSON registration and `defaultRequire=1`.

This is a separate version-sensitive engine integration adapter:
when porting Minecraft/NeoForge, verify the synthetic lambda
bytecode and patch source before enabling. Any third-party
interception at the same precise callsite is a compatibility risk.

### Remaining status

- [PASS / user-confirmed] Previous `test.ps1` including the original
  four volume-particle Mixin contracts after parser fixes.
- [IMPLEMENTED / Gradle recheck pending] New menu numerical
  bonus adapter and its ASM regression.
- [STARTUP + GAMEPLAY PENDING] New EnchantingTable/Spore
  Mixins as a whole; no subsequent user gameplay confirmation.
- [PENDING PHASE-2 POLICY] Bookcase physical-provider duplication
  near three-face corners. Vanilla/NeoForge counts authored offsets;
  possible duplicate providers require explicit design decision.
- [PENDING PHASE-4 ACCEPTANCE] Full visual emitter gameplay matrix,
  including +Y, rotated faces, edge and corner.

Next gate: `git pull && .\\test.ps1 && .\\run-client.ps1`.
The menu class has a new synthetic-lambda @Redirect. Verify tests
THEN client + Planet world startup. Do NOT claim successful
runtime transformation before user evidence.
