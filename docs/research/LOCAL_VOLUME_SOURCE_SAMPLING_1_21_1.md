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
