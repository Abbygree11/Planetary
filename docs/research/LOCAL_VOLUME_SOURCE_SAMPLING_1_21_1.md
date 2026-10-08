# Phase 4 / Phase 1: local volumetric emitter queries (Minecraft 1.21.1)

Status: SOURCE AUDIT COMPLETE; multi-axis traversal API in implementation;
EnchantmentTable/SporeBlossom runtime integration NOT YET IMPLEMENTED.

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

## Performance / acceptances

Spore volume might require up to 14*30 steps each tick;
prototype pure walk first. Before any runtime integration
consider an interior fast path based on proven same-face
bounds, with edge fallback. Do not cache unbounded by BlockPos.

Milestones:
1. [IMPLEMENTED/PURE-TEST-PENDING] typed multi-axis displacement
   with basis transport and exact one-axis equivalence;
2. [PENDING] one server+client EnchantingTable
   `isValidBookShelf` membership boundary preserving bookshelf
   valid shape, unique counted providers and menu outcomes;
3. [PENDING] client enchantment particle origin+velocity
   adapter while preserving vanilla RNG;
4. [PENDING] client SporeBlossom candidate/occlusion
   and emission unit-offset adapter preserving all draws;
5. [PENDING] single combined integration/multi-face/edge
   acceptance batch, with +Y vanilla baseline.

No runtime-related PASS inferred from pure helper code or unit tests.
