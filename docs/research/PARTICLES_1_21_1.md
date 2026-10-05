# Research: generic particles and local-frame emitters in Minecraft 1.21.1

Status: active Phase 4 audit.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## Scope

Particles have two independent gravity-frame problems:

1. particle MOTION may interpret world Y as semantic UP/DOWN;
2. particle EMITTERS may construct their initial world position using hard-coded
   world-Y/world-XZ offsets.

Do not solve an emitter bug by rotating arbitrary particle velocity, and do not
solve motion by moving the emitter. Those are separate boundaries.

## Generic Particle path

Vanilla Particle has these world-axis assumptions:

- velocity constructor adds a fixed +0.1 to world Y after randomization;
- setPower preserves that +0.1 world-Y baseline;
- tick subtracts 0.04 * gravity from world Y;
- move classifies onGround from a blocked negative world-Y displacement;
- move marks stoppedByCollision from a blocked world-Y displacement;
- move zeros X/Z velocity when those physical axes collide;
- base tick applies ground friction to world X/Z;
- speedUpWhenYMotionIsBlocked observes whether world Y changed.

Current Planet adapters already cover:
- constructor +0.1 semantic UP -> local UP;
- setPower baseline -> local UP;
- base gravity -> local DOWN;
- selected direct-gravity subclasses whose tick bypasses Particle.tick.

Still open after the torch-emitter patch:
- the full local collision/friction semantics in Particle.move/tick.

That remaining item must be solved generically, not per particle type.

## Falling blocks / falling dust

FallingBlock.animateTick uses a semantic "below" test and emits FALLING_DUST from
the bottom face. Planet already replaces that with local DOWN and
PlanetParticleSpawn.spawnOnLocalDownFace.

FallingDustParticle owns a separate fixed downward acceleration and terminal
speed clamp. Planet already rotates those terms onto the local gravity axis.

## TorchBlock.animateTick

Vanilla standing torch computes:

    x = pos.x + 0.5
    y = pos.y + 0.7
    z = pos.z + 0.5

Relative to block center this is exactly:

    local offset = (0, +0.2, 0)

The +0.2 is semantic LOCAL UP, not physical world +Y.

Both SMOKE and flame particle calls use the same origin. Their particle types and
zero initial velocity are otherwise already correct.

## WallTorchBlock.animateTick

Vanilla wall torch computes:

    d0 = pos.x + 0.5
    d1 = pos.y + 0.7
    d2 = pos.z + 0.5
    emitter = (d0,d1,d2)
              + world UP * 0.22
              + FACING.opposite() * 0.27

The first implementation incorrectly treated +0.22 as the whole center-relative
vertical offset. That dropped the existing +0.20 from y+0.7.

Relative to block center the real semantic offset is therefore:

    local UP * 0.42
    + local FACING.opposite() * 0.27

Planet block-state FACING is canonical LOCAL orientation.

No block position, BlockState property or registered model is mutated.

## Chosen boundary

PlanetParticleEmitter is a pure geometry helper. It converts vanilla local
emitter offsets around a block center into physical Vec3 positions.

TorchParticleGravityMixin intercepts only the XYZ arguments of the two
Level.addParticle calls in TorchBlock/WallTorchBlock.animateTick.

The corrected implementation no longer reconstructs torch constants at all.
It takes the final coordinates vanilla has already computed, subtracts the block
center, interprets that complete delta as LOCAL coordinates, and rotates the
delta into physical world XYZ. This preserves y+0.7, the wall +0.22 term and any
future vanilla arithmetic without duplicating it.

Why this boundary:
- keeps vanilla particle option/type unchanged;
- keeps vanilla particle count unchanged;
- keeps vanilla velocity unchanged;
- avoids duplicating TorchBlock implementation;
- preserves exact POS_Y behavior by not modifying the invocation there;
- automatically covers normal and soul standing/wall torches because both use
  the same TorchBlock/WallTorchBlock code path.

Redstone signal behavior remains Phase 7A, but particle emission is independent
of signal propagation and is now adapted here.

RedstoneTorchBlock.animateTick uses center + local UP*0.2 plus independent
random +/-0.1 offsets. RedstoneWallTorchBlock additionally uses the same wall
+0.22 and FACING.opposite()*0.27 terms as the ordinary wall torch.

RedstoneTorchParticleGravityMixin uses the same final-coordinate transform as
ordinary torches. Because it transforms the already sampled vanilla coordinates,
it consumes no extra random numbers and preserves the exact random jitter.

## Tests

PlanetParticleEmitterTest covers:
- standing torch local-UP offset on all six faces;
- wall torch full center-relative +0.42 local-UP offset on all six faces;
- wall torch four horizontal FACING values on all six faces;
- exact POS_Y coordinates equal vanilla;
- a representative sampled redstone-wall emitter with jitter;
- invalid vertical wall FACING rejected.

## Manual acceptance

Check standing and wall torch on:
- +Y, -Y;
- +X, -X;
- +Z, -Z;
- one block immediately next to a gravity edge.

Expected:
- standing flame/smoke exits from the visible top of the torch;
- wall flame/smoke sits above the tip and toward its supporting wall exactly like
  vanilla orientation;
- no duplicate smoke/flame;
- +Y remains visually vanilla;
- particle motion after spawn still follows existing local-particle behavior.


## Generic Particle.move local collision semantics

Exact vanilla Particle.move flow:
1. save requested physical dx/dy/dz;
2. clip the physical movement with Entity.collideBoundingBox;
3. move the particle bounding box by the clipped physical vector;
4. set stoppedByCollision when requested WORLD Y was non-trivial but clipped
   WORLD Y became ~0;
5. set onGround when WORLD Y was clipped while requested WORLD Y was negative;
6. zero xd on physical-X collision;
7. zero zd on physical-Z collision.

Only step 2-3 are genuinely physical geometry. Steps 4-7 are semantic-axis
decisions and must follow local gravity.

Planet now lets vanilla perform collision clipping unchanged, then reconstructs
requested and actual movement in the active PlanetGravityFrame:
- local Y collision while requested local Y < 0 -> onGround;
- local X collision -> zero local X velocity;
- local Z collision -> zero local Z velocity;
- local Y collision does NOT zero local-Y velocity, matching vanilla world-Y
  behavior.

Because each Planet face basis is an axis-aligned signed permutation, converting
requested/actual vectors between physical and local frames does not introduce a
second collision solver or diagonal approximation.

Particle.stoppedByCollision is deliberately left vanilla/physical. Source and
runtime regression analysis showed it is not equivalent to a semantic gravity
axis: it is a sticky short-circuit that prevents every later move() call. Destroy
TerrainParticles are created inside the destroyed VoxelShape, so rotating this
sticky condition to local Y can freeze the burst inside the source block.

This generic move correction is also inherited by custom tick implementations
that still call Particle.move, including vanilla falling dust, drip/water-drop,
cherry and several smoke/water particles.

## Particle.tick axis-dependent post-move behavior

Base Particle.tick has two more world-axis assumptions after move:

    if (speedUpWhenYMotionIsBlocked && y == yo)
        xd *= 1.1;
        zd *= 1.1;

    if (onGround)
        xd *= 0.7;
        zd *= 0.7;

The ordinary friction multiplier is scalar across all three axes and is already
frame-independent.

For rotated Planet faces the adapter:
1. lets vanilla finish the base tick;
2. detects whether vanilla applied its physical-X/Z speed-up/ground multipliers;
3. algebraically removes only those physical-axis multipliers;
4. transforms velocity into the local frame;
5. applies the same 1.1 / 0.7 multipliers to local X/Z;
6. transforms velocity back to physical XYZ.

The local equivalent of y == yo is zero local-Y displacement during that tick.

This avoids cancelling/reimplementing Particle.tick and preserves age, lifetime,
friction, removal, subclass super.tick behavior and all non-axis-specific
vanilla logic.

## Known remaining class-specific particle audit

Generic Particle.move/onGround is now adapted, but class-specific tick logic can
still contain its own world-axis assumptions.

Known examples from the 1.21.1 source sweep:
- DragonBreathParticle has its own y == yo branch;
- BubbleParticle directly accelerates +world Y;
- some water/current particles derive horizontal swirl in world X/Z;
- weather/rain particle spawning remains environment/world-axis work;
- fluid-specific particles are ultimately tied to Phase 5 fluid topology;
- exact gravity-edge frame selection remains the common edge-policy problem,
  not a particle-specific offset fix.

Do not mark Phase 4 closed until these remaining direct subclasses and emitter
helpers are either adapted or explicitly assigned to their owning later phase.


## Destroy TerrainParticle regression after local move pass

Manual acceptance immediately caught a regression: block-destroy particles no
longer dispersed around the broken block and remained confined to its original
volume.

ParticleEngine.destroy explicitly samples particle origins from INSIDE the
destroyed BlockState VoxelShape. TerrainParticle then relies on normal Particle
motion to escape that volume.

The first local-move implementation incorrectly reclassified
Particle.stoppedByCollision using local Y. That field is sticky: once true,
future Particle.move calls return immediately. For particles born inside a
shape, a clipped local-Y component can therefore freeze the entire remaining
motion.

Correction:
- local onGround remains frame-aware;
- local tangent collision velocity response remains frame-aware;
- local ground friction remains frame-aware;
- stoppedByCollision is NOT rewritten and remains vanilla internal behavior.

Architectural lesson: not every vanilla world-Y branch is semantic gravity.
Classify the purpose of the field/control flow before rotating it.
