# Research: local fall damage in Minecraft 1.21.1

Status: player path fixed and manually verified; falling-block spawn-anchor fix pending manual acceptance.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Why this is a separate subsystem

"Fall damage" is not one call path in 1.21.1.

There are at least two relevant paths:

1. ordinary Entity / FallingBlockEntity movement;
2. ServerPlayer network-authoritative fall accounting.

Treating only Entity.move cannot fix both.

## 2. Ordinary Entity path

Vanilla Entity.move performs physical collision and then:

    setOnGroundWithMovement(...)
    blockPos = getOnPosLegacy()
    blockState = level.getBlockState(blockPos)
    checkFallDamage(actualMovement.y, onGround, blockState, blockPos)

Entity.checkFallDamage then:
- when grounded:
  - if fallDistance > 0, calls supportBlock.fallOn(..., fallDistance);
  - resets fallDistance;
- while airborne and vertical movement < 0:
  - fallDistance -= verticalMovement.

Therefore the semantic value passed to checkFallDamage is LOCAL vertical
displacement, not necessarily physical world Y.

Planet Entity.move already classifies requested/actual movement in
PlanetEntityMotion.CollisionResult. The intended local value is
actualLocal.y().

## 3. LivingEntity path

LivingEntity overrides checkFallDamage for server-side particles / block-change
bookkeeping, then calls super.checkFallDamage.

LivingEntity.causeFallDamage:
- computes damage from fall distance minus SAFE_FALL_DISTANCE;
- applies FALL_DAMAGE_MULTIPLIER;
- hurts the entity when the result is positive.

The scalar damage formula itself is frame-independent. The inputs and landing
geometry are the frame-sensitive parts.

## 4. ServerPlayer is different

ServerPlayer overrides checkFallDamage with an EMPTY method.

Actual server player fall accounting is performed by:

    ServerGamePacketListenerImpl.handleMovePlayer
        -> ServerPlayer.doCheckFallDamage(dx, dy, dz, packetOnGround)

The dx/dy/dz passed there are physical WORLD packet deltas.

ServerPlayer.doCheckFallDamage:
- passes the full delta to checkSupportingBlock;
- obtains getOnPosLegacy / support state;
- calls super.checkFallDamage(dy, onGround, supportState, supportPos).

Therefore physical dy is wrong for +/-X and +/-Z gravity and reversed for -Y.

Planet policy:
at the ServerGamePacketListener -> ServerPlayer.doCheckFallDamage boundary,
convert the complete physical world delta into the player's current local frame.
Then ServerPlayer receives:
    local dx, local dy, local dz, local onGround

This also makes Planet's existing checkSupportingBlock hook receive the movement
vector in the local convention it expects.

Reference check:
the 1.21 GravityChanger fork independently has a dedicated
ServerPlayer fall-distance mixin that converts world movement to player-local Y
before the parent checkFallDamage call. Its generic Entity move transform does
not claim to cover ServerPlayer by itself.

## 5. FallingBlockEntity / falling stalactite path

FallingBlockEntity does NOT override Entity.checkFallDamage, so it should follow
the ordinary Entity path.

FallingBlockEntity.causeFallDamage has three independent gates:
1. hurtEntities must be true;
2. the supplied fallDistance must produce a non-negative distance scalar;
3. eligible living entities must be inside the FallingBlockEntity's CURRENT
   bounding box.

The target predicate explicitly excludes creative and spectator entities.

For pointed dripstone, PointedDripstoneBlock.spawnFallingStalactite in vanilla:
- converts every DOWN-directed chain block to FallingBlockEntity;
- only the terminal TIP/TIP_MERGE entity gets setHurtsEntities(size, 40).

PlanetDripstoneFalling already reproduces that arming step through local DOWN.

## 6. NeoForge 1.21.1 patches

NeoForge's 1.21.1 FallingBlockEntity patch changes concrete-powder hydration
checks but does not replace the fall-distance / causeFallDamage path.

The ServerPlayer patch does not replace doCheckFallDamage semantics.

Therefore vanilla call-flow above remains the correct integration boundary.

## 7. Runtime trace required for dripstone

Static source inspection is no longer enough because generic Entity local
movement should already make FallingBlockEntity fallDistance work.

Temporary trace points are intentionally narrow to POINTED_DRIPSTONE:

A. PlanetDripstoneFalling
- prints when the terminal tip is armed;
- id, root, tip, chain distance, damage parameters.

B. Entity.checkFallDamage
- local vertical movement;
- grounded;
- fallDistance before vanilla update/reset;
- local velocity;
- requested/actual local collision movement;
- support position/state.

C. FallingBlockEntity.causeFallDamage
- fallDistance argument;
- hurtEntities;
- damage-per-distance/max;
- current AABB;
- exact eligible target count;
- living target count within AABB inflated by 1 block.

Interpretation:
- no armedDripstoneTip -> chain scan/state bug;
- armed, but no checkFallDamage -> movement/collision call-flow bug;
- checkFallDamage never accumulates -> local vertical conversion bug;
- grounded with positive fallDistance but no causeFallDamage -> landing/support
  dispatch bug;
- causeFallDamage with hurtEntities=false -> tip arming lost;
- causeFallDamage with eligibleTargets=0 and nearbyLiving1>0 -> AABB/contact or
  creative/spectator eligibility issue;
- eligibleTargets>0 and positive scalar but no health loss -> damage event /
  immunity path must be audited next.

## 8. Acceptance

Player:
- survival, normal fall damage enabled;
- +Y vanilla control;
- +/-X, +/-Z, -Y falls from >= 6 local blocks;
- damage scales with local fall distance;
- no damage from ordinary short drops.

Pointed dripstone:
- terminal tip is armed once;
- local chain falls together;
- trace shows increasing positive fallDistance while falling;
- landing trace is grounded with fallDistance > 0;
- causeFallDamage receives hurtEntities=true;
- survival player occupying the landing AABB appears in eligibleTargets;
- health decreases with falling-stalactite damage source.

Diagnostics must be removed/reduced after the exact dripstone failure is found.


## 9. Runtime result after server-player fix

Manual verification after 8f73253:
- ordinary player fall damage on rotated gravity: PASS;
- falling pointed-dripstone damage against the player: PASS.

This confirms:
- ServerPlayer local packet-delta adaptation is correct;
- FallingBlockEntity fallDistance accumulation is correct;
- pointed-dripstone terminal tip is armed with hurtEntities=true;
- FallingBlockEntity.causeFallDamage is reached with a positive fall distance.

A remaining case failed:
a creeper standing centered in a one-block hole was not damaged.

Trace:
    causeFallDamage
    arg=8.834728
    hurtEntities=true
    perDistance=6.0
    max=40
    onGround=true
    eligibleTargets=0
    nearbyLiving1=1

Therefore the damage scalar/predicate are not the problem. The mob is alive and
nearby, but its AABB does not intersect the falling-tip AABB at the exact damage
moment.

## 10. FallingBlockEntity spawn anchor root cause

Vanilla FallingBlockEntity.fall constructs the entity at:

    (blockX + 0.5, blockY, blockZ + 0.5)

This is not the block center. It is the center of the source block's WORLD-DOWN
face, which is exactly the correct "feet"/local-origin anchor under normal
gravity.

Planet rotates the entity AABB so its vanilla local Y becomes Planet local UP.
On a side face, physical world Y is then a tangent axis. Keeping constructor Y
at the integer block boundary leaves the falling entity displaced by 0.5 block
along that tangent.

The captured failing AABB exposed this directly:
    y = 158.51 .. 159.49
    center y = 159.0

For a falling block whose local vertical is physical X/Z, physical Y should be a
centered tangent coordinate N + 0.5, not N.

General invariant:

    fallingBlockAnchor =
        sourceBlockCenter
        + 0.5 * physical(local DOWN)

Examples:
- local DOWN = world DOWN -> vanilla (x+0.5, y, z+0.5)
- local DOWN = WEST       -> (x, y+0.5, z+0.5)
- local DOWN = EAST       -> (x+1, y+0.5, z+0.5)
- local DOWN = NORTH      -> (x+0.5, y+0.5, z)
- local DOWN = SOUTH      -> (x+0.5, y+0.5, z+1)
- local DOWN = world UP   -> (x+0.5, y+1, z+0.5)

This is a generic FallingBlockEntity spawn-boundary issue, not a dripstone
special case. Sand, gravel, anvils and modded FallingBlock users benefit too.

Commit 4cfff7c:
- adds PlanetFallingBlockSpawn;
- rewrites FallingBlockEntity constructor coordinates inside static fall(...)
  before the entity is added to the level;
- keeps vanilla exactly unchanged for world DOWN;
- removes temporary pointed-dripstone FallTrace diagnostics;
- adds six-direction unit coverage for the anchor formula.

Manual acceptance is still required before closing this issue.


## 11. Renderer follow-up after physical anchor fix

Manual acceptance of 4cfff7c:
- falling-block / pointed-dripstone damage: PASS;
- mobs are damaged correctly;
- physical falling behavior works;
- sand and anvils visually shift by half a block when converting from block to
  FallingBlockEntity and then appear to snap while travelling through a hole.

This is renderer-only. FallingBlockRenderer still assumes the entity anchor is
the center of a WORLD-DOWN face and uses world-Y-specific BlockPos/translation.

Client fix:
- keep the correct physical entity anchor unchanged;
- derive render cell center from anchor - 0.5 * localDown;
- derive model translation from
  -0.5 * localDown - (0.5,0.5,0.5);
- preserve exact vanilla values on +Y/world-DOWN.
