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

    center
    + 0.22 * world UP
    + 0.27 * FACING.opposite() in world X/Z

Planet block-state FACING is canonical LOCAL orientation. Therefore the correct
physical emitter is:

    center
    + physical(local UP) * 0.22
    + physical(local FACING.opposite()) * 0.27

No block position, BlockState property or registered model is mutated.

## Chosen boundary

PlanetParticleEmitter is a pure geometry helper. It converts vanilla local
emitter offsets around a block center into physical Vec3 positions.

TorchParticleGravityMixin intercepts only the XYZ arguments of the two
Level.addParticle calls in TorchBlock/WallTorchBlock.animateTick.

Why this boundary:
- keeps vanilla particle option/type unchanged;
- keeps vanilla particle count unchanged;
- keeps vanilla velocity unchanged;
- avoids duplicating TorchBlock implementation;
- preserves exact POS_Y behavior by not modifying the invocation there;
- automatically covers normal and soul standing/wall torches because both use
  the same TorchBlock/WallTorchBlock code path.

Redstone torches are intentionally not included here. Their signal behavior is
part of Phase 7A and their random-display particle geometry will be audited with
that subsystem rather than mixing redstone semantics into the normal torch pass.

## Tests

PlanetParticleEmitterTest covers:
- standing torch local-UP offset on all six faces;
- wall torch four horizontal FACING values on all six faces;
- exact POS_Y coordinates equal vanilla;
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
