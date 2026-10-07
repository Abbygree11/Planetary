# Entity / body / interaction / network matrix — Minecraft 1.21.1 / NeoForge 21.1.215

Status: R4 COMPLETE research result for Planetary 2.0.

This document is the concrete Phase-7 plus interaction/network half of Phase-7B
produced by global research batch R4.

R4 is research only. No runtime implementation is performed here.

Primary source families audited:
- vanilla Entity movement/collision/body/pose/view;
- LivingEntity travel/jump/climb/fluid/elytra;
- ItemEntity, ExperienceOrb, PrimedTnt, FallingBlockEntity;
- Projectile / ThrowableProjectile / AbstractArrow /
  AbstractHurtingProjectile / FireworkRocketEntity / FishingHook;
- AbstractMinecart and Boat;
- riding/passengers/attachments/dismount/leash/sleeping;
- knockback / entity push / explosion / piston move interaction;
- GameRenderer picking / Item POV ray / ProjectileUtil;
- LocalPlayer movement packets;
- ServerGamePacketListenerImpl player + vehicle validation;
- ClientPacketListener teleport/movement correction;
- NeoForge 1.21.1 Entity/LivingEntity/Player/projectile/vehicle/network patches
  and relevant events/extensions.

---

## 1. R4 fundamental coordinate contract

Planetary must separate three spaces.

### 1.1 Physical world space

The following Minecraft contracts must remain PHYSICAL world XYZ:

- Entity.position;
- Entity bounding box;
- Entity.deltaMovement;
- Entity.move(MoverType, Vec3) argument/result displacement;
- Entity.push(Vec3) and external force vectors;
- explosion impulse vectors;
- leash elastic vectors;
- packet X/Y/Z positions;
- teleport targets;
- vehicle packet positions;
- ray start/end;
- HitResult / BlockHitResult location and physical side;
- projectile flight displacement;
- collision shapes.

This is required for vanilla/NeoForge interoperability and mod compatibility.

### 1.2 Body-local semantic space

The following concepts are local to the entity gravity/body frame:

- UP/DOWN;
- tangent X/Z;
- step height;
- local vertical collision;
- local ground / floor;
- jump;
- gravity acceleration;
- climb vertical;
- fluid sink/rise;
- eye/seat/leash authored offsets;
- local yaw/pitch vocabulary;
- fall distance;
- local horizontal movement metrics.

### 1.3 Render/camera space

Camera/body render orientation composes:
- body-local yaw/pitch;
- gravity-frame local->world rotation;
- camera-specific transforms.

The physical view ray produced by that composition is then ordinary world-space.

---

## 2. Critical R4 architecture correction: deltaMovement must stay physical

The current partial EntityGravityMixin converts deltaMovement into LOCAL
coordinates while inside a Planet field.

R4 rejects that as the long-term architecture.

This is not a theoretical compatibility preference. Exact 1.21.1 paths prove
that vanilla treats deltaMovement as a physical world vector.

Examples:

### Explosion

Explosion computes a normalized WORLD vector:
    entity position/eye - explosion world position

then directly:
    entity.setDeltaMovement(entity.getDeltaMovement().add(worldImpulse))

### Leash

Leashable computes:
    holder.worldPosition - entity.worldPosition

for X/Y/Z, then directly adds that world vector to deltaMovement.

### Entity.push

Entity.push(double x, double y, double z) directly adds its arguments to
deltaMovement. Callers commonly derive those arguments from world positions.

### Client teleport correction

ClientPacketListener.handleMovePlayer interprets RelativeMovement.X/Y/Z by
copying the corresponding deltaMovement x/y/z component.

Those relative flags are explicitly PHYSICAL packet axes.

### ProjectileUtil

ProjectileUtil.getHitResultOnMoveVector uses getDeltaMovement directly as the
world ray displacement.

### Vehicle/server validation

ServerGamePacketListenerImpl compares physical packet displacement length against
entity.getDeltaMovement().lengthSqr().

Third-party mods naturally use the same vanilla API contract.

Therefore keeping local velocity hidden inside Entity.deltaMovement requires
intercepting an unbounded set of vanilla/mod callers and is not portable.

### Required future migration

Long-term invariant:

    Entity.deltaMovement == physical world velocity

Planet locomotion algorithms should use a temporary local projection:

    local = frame.worldToLocal(worldDelta)
    run vanilla-semantic local-Y/tangent algorithm
    world = frame.localToWorld(localResult)
    entity.setDeltaMovement(world)

Likewise Entity.move receives physical movement.

This migration is required before broad Phase-7 expansion.

Current manually accepted player behavior remains valid evidence about desired
semantics, but not proof that the local-storage implementation boundary should
remain.

---

## 3. Stable Planet entity APIs

Preferred reusable core:

### PlanetEntityBodyFrame

Owns:
- PlanetGravityFrame;
- physical anchor position;
- physical AABB;
- local dimensions;
- local eye offset;
- local attachment offsets;
- world<->local vector conversion;
- local body UP/DOWN/tangents.

### PlanetEntityMotion

Owns pure transformations/classification:
- requested physical motion -> local components;
- actual physical collision motion -> local collision flags;
- local vertical/tangent classification;
- world velocity projection and recomposition.

### PlanetEntityCollision

Existing architecture is a strong foundation:
- physical AABB;
- physical colliders;
- solve collision in local Y then local X/Z;
- return physical displacement;
- local step-up semantics.

Do not duplicate collision in LivingEntity subclasses.

### PlanetEntitySupport

Own:
- local floor-facing AABB slice;
- supporting physical BlockPos;
- local ground fallback rewind.

---

## 4. Phase 7.1 — generic Entity movement/body core

### 4.1 Collision

Vanilla Entity.collide resolves:
- world Y first;
- X/Z after;
- world-Y step geometry.

Existing PlanetEntityCollision correctly replaces the solve ordering with:
- local Y first;
- local tangent axes.

Physical collision geometry remains unchanged.

### 4.2 Collision flags

Vanilla derives:
- horizontalCollision from X/Z clipping;
- verticalCollision from Y clipping;
- verticalCollisionBelow from clipped negative Y.

Planet derives these from local projected requested/actual motion.

The public booleans remain semantic:
- horizontal = local tangent collision;
- vertical = local vertical collision;
- below = local DOWN collision.

### 4.3 onGround

onGround means supported against LOCAL DOWN.

It is not tied to physical -Y.

Packet onGround therefore remains a semantic bool.

### 4.4 Supporting block

Vanilla checkSupportingBlock:
- creates a thin slab below AABB.minY;
- fallback rewinds only X/Z.

Planet:
- floor slab is physical face corresponding to local DOWN;
- fallback rewind uses local tangent movement converted to physical.

Existing PlanetEntityGeometry.supportSlice is the correct type of primitive.

### 4.5 getOnPos / movement-affecting block

Vanilla getOnPos assumes:
    position.y - epsilon

Planet must resolve from the local-down foot anchor.

Fence/wall special handling in NeoForge additionally uses
collisionExtendsVertically and must remain reachable.

### 4.6 Fall distance

checkFallDamage receives local vertical displacement.

Fall accumulation:
- negative local Y increases fall distance;
- local ground resets/applies damage.

Physical build-height/world-border rules remain physical.

### 4.7 Step sounds / movement distances

Vanilla uses:
- vec.horizontalDistance() for walkDist;
- movement.y zeroed unless climbing;
- crouch step checks movement.y == 0.

These must use local tangent/local vertical components.

Physical world distance for flyDist can remain vector length.

### 4.8 Block speed/jump factors

Queries for "block below affecting movement" use local DOWN.

Once the physical target BlockPos is resolved, ordinary block/NeoForge
getFriction/getSpeedFactor/getJumpFactor hooks run unchanged.

### 4.9 Stuck/suffocation/push-out

Inside-block collision iteration over physical AABB remains physical.

But escape search authored as:
- four XZ directions;
- optional world UP

is body-local semantics.

Existing LocalPlayer push-out fix covers one path.
Generic Entity.moveTowardsClosestSpace and sibling paths need the same family
classification.

---

## 5. Pose, dimensions and physical AABB

EntityDimensions is authored in vanilla local Y-up convention:
- width = tangent width;
- height = body-local vertical;
- eyeHeight = local UP offset;
- attachments = body-local offsets.

Planet rule:
- keep dimensions canonical/local;
- produce physical AABB by rotating the local box about the entity anchor.

Because Planet gravity is axis-aligned, the rotated box is still a world AABB.

NeoForge EntityEvent.Size must remain the authority that can modify dimensions
before Planet converts them to physical geometry.

Do not expose rotated physical dimensions as modified EntityDimensions values.

---

## 6. Eye/view/body orientation

### Eye position

Vanilla:
    position + (0, eyeHeight, 0)

Planet:
    position + frame(local UP * eyeHeight)

Existing PlanetEntityGeometry.eyePosition is the right boundary.

### View vector

Vanilla calculateViewVector interprets yaw/pitch in Y-up coordinates.

Planet preferred contract:
- yaw/pitch remain BODY-LOCAL angular vocabulary;
- calculate local view vector with vanilla math;
- transform once to physical world through gravity frame.

This preserves ordinary control semantics.

### Raw yaw/pitch compatibility warning

Mods that call getYRot/getXRot and independently assume world Y-up can still be
wrong on rotated gravity.

Preferred compatibility API is the physical getViewVector / body-frame helper,
not global reinterpretation of Direction or Vec3.

---

## 7. Gravity-frame transition and orientation state

Changing gravity face requires:
- re-expressing body-local orientation;
- preserving the same physical facing direction where product policy requires;
- camera transition;
- bounding box refresh.

With physical deltaMovement storage, velocity does NOT need re-expression when
the gravity frame changes; its physical vector remains the same.

This removes a major source of transition bugs.

### Network consistency gate

Planet entity face selection currently uses preferred face + hysteresis state.

Body-local yaw/pitch packet values are only meaningful if client and server
interpret them in the same gravity frame.

Therefore R4 requires one of:

A. prove frame transition selection is fully deterministic from synchronized
physical trajectory and cannot diverge; or

B. synchronize the authoritative gravity face/body-frame state.

Because hysteresis contains state and client prediction can differ from server
timing, B is the safer long-term design.

Do not encode local velocity into vanilla movement packets.

A synchronized body-frame identifier should be a thin Planet state channel;
packet XYZ remains vanilla physical.

---

## 8. Phase 7.2 — LivingEntity locomotion families

LivingEntity.travel is largely authored in local Y-up coordinates.

With physical deltaMovement storage, preferred architecture is:

    project physical velocity to local
    execute one local locomotion family
    recompose physical velocity before Entity.move / storage

### 8.1 Ground/air locomotion

Local semantics:
- Y gravity;
- tangent friction;
- onGround friction selection;
- levitation;
- slow falling;
- step/climb.

World build-height fallback remains physical policy and should not be inferred
from local Y.

### 8.2 Jump

jumpFromGround:
- local +Y impulse;
- sprint tangent impulse derived from body-local yaw.

NeoForge jump attributes/effects remain unchanged.

### 8.3 Climbing

handleOnClimbable clamps:
- local tangent X/Z;
- local vertical minimum;
- ladder/sneak downward suppression.

Climbable block lookup depends on local body/support topology.

### 8.4 Swimming/fluid

Depends on Phase 5 geometry and NeoForge FluidType.

Local locomotion:
- vertical sink/jump;
- local tangent drag;
- fluid escape step.

Physical FluidState.getFlow vector is applied through entity/body integration.

Preserve:
- FluidType.move;
- swim speed;
- motion scale;
- custom fluid movement hooks.

### 8.5 Elytra / fall flying

Vanilla elytra mixes:
- view vector;
- horizontalDistance;
- velocity.y;
- pitch.

This is a body-local flight algorithm.

Algorithm should run in local body coordinates:
- physical velocity -> local;
- physical view -> body-local or directly use local yaw/pitch;
- vanilla elytra math;
- result -> physical.

Fly-into-wall collision remains local tangent collision semantics.
Damage ray/collision geometry remains physical.

### 8.6 Riptide / spin attack

Body-directed impulse is local view/body semantics.

Entity hit/collision scan is physical.

### 8.7 Powder snow / honey / slime

Classify per mechanism:
- vertical bounce/stick -> local vertical;
- tangent slowdown -> local tangent;
- overlap/collision geometry -> physical.

Do not patch only named blocks without following their shared entity callbacks.

---

## 9. Phase 7.3 — non-living entity families

Base Entity collision coverage is insufficient because subclasses author
velocity components directly.

### 9.1 ItemEntity

Local semantics:
- gravity;
- tangent friction;
- local-ground bounce;
- fluid buoyancy/float.

NeoForge FluidType.setItemMovement must be preserved.

### 9.2 ExperienceOrb

Local semantics:
- gravity;
- ground bounce;
- fluid buoyancy.

Attraction to player:
- source/target positions are PHYSICAL;
- attraction vector is a physical world vector.

Do not feed that physical vector into local-stored velocity.

### 9.3 PrimedTnt

Local semantics:
- spawn upward impulse;
- gravity;
- local-ground bounce/friction.

Explosion center remains physical world position.

Smoke emitter source offset uses body-local UP if visual behavior should follow
the entity body.

### 9.4 FallingBlockEntity

Existing accepted work is partial coverage.

Still classify:
- gravity;
- local-ground landing;
- bounce/friction;
- fluid ray interactions;
- physical source/placement BlockPos;
- renderer anchor in Phase 3D.

### 9.5 Other non-living entities

Armor stands/displays/decorations may have:
- base physical collision;
- body-local pose/render orientation;
- attachment semantics.

Do not assume no work merely because they do not walk.

---

## 10. Phase 7.3B — projectiles

Projectile subsystem has four distinct owners.

### 10.1 Launch

Projectile.shootFromRotation uses shooter yaw/pitch and shooter velocity.

Required:
- body-local view -> physical launch vector;
- add shooter PHYSICAL velocity;
- onGround only suppresses the shooter's local vertical contribution according
  to vanilla semantics.

Do not rotate a physical shooter velocity twice.

### 10.2 Flight physics

Per family:
- ThrowableProjectile gravity/drag;
- AbstractArrow gravity/water drag/in-ground;
- firework authored acceleration;
- FishingHook gravity/fluid bobbing;
- hurting projectile acceleration.

Gravity-relative scalar terms use local DOWN.

Physical externally authored acceleration/target vectors remain physical.

### 10.3 Collision

ProjectileUtil movement rays and Level.clip are physical.

BlockHitResult direction/location remains physical.

EntityHitResult geometry remains physical.

No gravity transform is applied to the finished HitResult.

### 10.4 Projectile orientation

ProjectileUtil.rotateTowardsMovement computes yaw/pitch using:
- world horizontalDistance;
- world Y.

On rotated gravity this is not body-local orientation.

Use physical flight vector projected into the projectile body/gravity frame,
then derive local yaw/pitch.

Renderer then applies gravity/body frame.

### 10.5 Special projectile families

Firework attached flight:
- attached entity physical position;
- body-local look direction transformed physical;
- rocket acceleration semantics.

FishingHook:
- water-surface math depends Phase 5;
- open-water scan is a fluid/environment graph;
- bob vertical impulses are local;
- hooked-entity pull is physical source-to-target vector.

Wind charges/fireballs:
- externally supplied physical direction/acceleration must not be treated as
  gravity automatically.

NeoForge ProjectileImpactEvent and projectile selection/hooks remain in the
standard collision/launch paths.

---

## 11. Phase 7.4 — minecart

AbstractMinecart is a dedicated rail-coordinate engine.

Vanilla hard-codes:
- RailShape EXITS as world XZ vectors;
- ascending exits with world Y offsets;
- getPos/getPosOffs using physical X/Z columns;
- horizontalDistance speed;
- world-Y rail elevation;
- X/Z-only tangent forces.

Phase 2F provides canonical local RailShape graph.

Phase 7.4 must consume that graph as local rail geometry:

    rail node + canonical RailShape
        -> physical tangent/ascending curve
        -> physical minecart motion

Do not independently reinterpret vanilla RailShape tables in multiple methods.

NeoForge minecart extension points to preserve:
- IMinecartCollisionHandler;
- configurable air speed/drag/max speed;
- custom rail hooks introduced by platform patches.

Powered/detector rail signal remains Phase 7A.

---

## 12. Phase 7.4 — boat

Boat is a dedicated fluid/body-surface engine.

Vanilla/NeoForge status calculation uses:
- AABB maxY/minY;
- blockY + fluid height;
- block below;
- Y buoyancy;
- world-Y bubble impulse.

Phase 5 provides physical local-fluid surface geometry.

Phase 7 boat adapter owns:
- immersion/status relative to boat local vertical;
- buoyancy along local UP;
- gravity local DOWN;
- land friction at local floor;
- bubble-column local vertical impulse;
- passenger seat/body placement.

Preserve NeoForge:
- supportsBoating/canBoatInFluid;
- block friction hook;
- extensible boat types.

---

## 13. Phase 7.5 — passengers and attachments

### 13.1 Passenger attachment

EntityAttachments values are authored in body-local Y-up coordinates.

Vehicle passenger attachment + passenger vehicle attachment must each be:
- evaluated in the owning entity's body frame;
- transformed to physical offsets;
- then combined in world position.

Do not add raw attachment Y directly to world Y on side gravity.

### 13.2 Riding orientation

Passenger yaw/pitch relationship is body-local policy.

Vehicle physical position remains world-space.

### 13.3 Dismount

DismountHelper is heavily world-Y/XZ:
- offsetsForDirection uses X/Z;
- below();
- Direction.UP ceiling scan;
- Axis.Y floor height;
- Vec3.upFromBottomCenterOf.

It requires a dedicated local-floor search API, not isolated replacements.

The search state is:
- physical candidate cell;
- vehicle/body frame;
- passenger pose dimensions;
- local UP clearance;
- local DOWN floor support.

### 13.4 Sleeping

LivingEntity.setPosToBed hard-codes:
    bed block center + world Y 0.6875

stopSleeping fallback uses:
    bed.above()

Stand-up search in BedBlock is a local support/body problem.

Phase 2 owns bed pair/FACING semantics.
Phase 7 owns sleeping body anchor and stand-up entity geometry.

### 13.5 Leash

Leash elastic force is source-to-target physical world vector.

Keep it physical.

Leash attachment offsets / rope hold positions are body-local authored offsets
and must be transformed once.

Leash renderer consumes physical endpoints.

### 13.6 Hanging entities

HangingEntity currently validates:
    direction axis is world-horizontal

On a rotated face, local wall directions may be physical +/-Y.

Hanging attachment direction therefore belongs to local block/body attachment
semantics, while the resulting bounding/support box is physical.

This family includes paintings/item frames and must not retain the raw
world-horizontal restriction on Planet surfaces.

---

## 14. External force policy

Not every force rotates with gravity.

### Local/body-relative forces

Rotate/author in local frame:
- gravity;
- jump;
- local vertical bounce;
- ladder climb;
- fluid buoyancy;
- bubble column lift/drag;
- body-forward sprint/riptide impulse.

### Physical world forces

Keep physical:
- explosion radial impulse;
- leash pull;
- source-to-target attraction;
- generic Entity.push(Vec3) once the caller supplies a physical vector;
- projectile impact impulse if derived from physical trajectory;
- network knockback vector packets.

### Mixed forces

LivingEntity.knockback vanilla API accepts only tangent X/Z ratios and adds a
vertical lift.

Semantic interpretation:
- ratio X/Z should represent local tangent attack direction;
- lift is local UP.

But many callers derive the ratio from world X/Z positions.

Therefore introduce a Planet-owned knockback API taking either:
- PHYSICAL source direction; or
- explicit BODY-LOCAL tangent direction.

Thin adapters classify each vanilla/NeoForge caller.

Preserve LivingKnockBackEvent before final impulse application.

---

## 15. Piston force boundary

MoverType.PISTON is physical block motion.

PistonStructureResolver / moving block defines the physical push direction after
Phase 7A local piston semantics are resolved.

Entity.limitPistonMovement clamps per physical axis and may remain physical once
Entity.move accepts physical displacement.

Do NOT reinterpret piston movement as entity local vertical/tangent merely
because the entity has gravity.

Collision result/onGround is still classified in the entity's local frame.

---

## 16. Phase 7B.1 — raycast / interaction

### 16.1 One physical ray

GameRenderer:
- gets gravity-aware physical eye position;
- gets gravity-aware physical view vector;
- Entity.pick / ProjectileUtil performs ordinary physical ray tests.

This is the correct architecture.

### 16.2 BlockHitResult stays physical

BlockHitResult:
- physical world hit location;
- physical block position;
- physical hit side.

It is used client-side and serialized to ServerboundUseItemOnPacket unchanged.

Server validation also uses the same physical location/side.

Phase 2 placement/use logic converts the physical hit side into the target
canonical local side when required.

Do not put canonical Direction into BlockHitResult.

### 16.3 Entity interactAt

Interaction point is an entity-space/world-space physical hit vector according
to vanilla networking.

Entity-specific body-local interpretation belongs at that entity's interaction
adapter.

NeoForge PlayerInteractEvent / CommonHooks must remain reachable.

---

## 17. Phase 7B.2 — player movement network contract

### 17.1 Packet positions

ServerboundMovePlayerPacket XYZ are PHYSICAL world positions.

Client LocalPlayer sends getX/getY/getZ directly.

Server compares physical packet positions against physical last-good positions.

Keep this unchanged.

### 17.2 Packet rotations

Yaw/pitch can remain Planet body-local angles if:
- both sides share the same gravity/body frame;
- physical view is derived by the same transform.

Relative teleport Y_ROT/X_ROT continues to operate on those scalar angles.

### 17.3 Packet onGround

onGround means local-floor contact.

Client computes semantic local onGround.
Server validates local collision/support.

No additional world-axis encoding is required.

### 17.4 Server movement validation world-Y assumptions

ServerGamePacketListenerImpl contains unadapted world-Y semantics:

Player:
- flag4 = packetDeltaY > 0 for jump detection;
- floating test d7 >= -0.03125;
- fall reset on positive world Y;
- doCheckFallDamage receives physical dx/dy/dz;
- setOnGroundWithMovement gets physical displacement.

Vehicle:
- subtracts 1e-6 from world-Y delta;
- floating test world-Y;
- verticalCollisionBelow interpreted through vehicle body frame.

These require a shared player/vehicle movement-validation helper that projects
physical packet displacement into the entity local frame for semantic checks.

Do not convert packet coordinates themselves.

### 17.5 Movement-too-fast check

Distance/velocity magnitude is rotation invariant.

With physical deltaMovement storage the vanilla:
    packetDistanceSq - deltaMovement.lengthSq
check remains valid.

### 17.6 Teleports

Teleport coordinates and RelativeMovement.X/Y/Z are physical axes.

ClientPacketListener explicitly preserves deltaMovement components according to
those physical relative-axis flags.

This is a hard reason deltaMovement must remain physical.

Gravity/body orientation state must be synchronized separately.

### 17.7 Vehicle movement packets

Vehicle XYZ are physical.

Vehicle local ground/floating semantics use the vehicle's own body/gravity
frame, not automatically the passenger frame.

---

## 18. Gravity face synchronization

Current Planet frame selection includes:
- preferred face;
- hysteresis;
- predicted client movement;
- server movement validation.

This state is not purely represented by packet XYZ at an exact boundary.

R4 design requirement:
server-authoritative gravity face/body-frame identity must have an explicit
client synchronization strategy unless deterministic equivalence can be proven
for all boundary/prediction cases.

Preferred portable architecture:
- Planet-owned body-frame state;
- small synchronized face identifier;
- local yaw/pitch interpreted against that face;
- physical position/velocity remain vanilla.

Do not overload vanilla position packet axes to carry frame semantics.

---

## 19. NeoForge compatibility contract

Preserve standard hooks/extensions, including:

Entity:
- EntityEvent.Size;
- Entity tick Pre/Post;
- EntityMountEvent;
- dimension travel hooks;
- fluid-type entity extensions;
- attachments/capabilities.

Living:
- LivingKnockBackEvent;
- living fall event/hook;
- jump/fluid movement hooks;
- FluidType movement;
- configurable attributes.

Projectile:
- ProjectileImpactEvent;
- projectile selection hooks;
- canRiderInteract behavior in ProjectileUtil.

Minecart:
- IMinecartCollisionHandler;
- speed/drag extensions.

Boat:
- canBoatInFluid / supportsBoating;
- fluid types;
- block friction.

Interaction:
- PlayerInteractEvent / CommonHooks entity interaction;
- server packet validation remains in normal platform path.

Avoid class allowlists for ordinary mod entities.
A mod entity inheriting standard Entity/LivingEntity physics should receive
Planet semantics through the common body/motion boundaries.

Raw custom world-axis movement code remains a Phase-10 compatibility case.

---

## 20. Cross-version portability

Strong stable Planet-owned concepts:
- PlanetEntityBodyFrame;
- physical<->local motion projection;
- local collision classification;
- support slice/floor query;
- local pose/eye/attachment transform;
- local locomotion math helpers;
- knockback force classification;
- packet-validation local semantic projection.

Version-sensitive wiring:
- Entity.move internals;
- LivingEntity.travel;
- particular subclass tick methods;
- ServerGamePacketListenerImpl validation fields/order;
- ClientPacketListener teleport handling;
- NeoForge event hook locations.

Avoid:
- storing local vectors inside vanilla deltaMovement;
- modifying packet coordinate meaning;
- globally redefining Vec3;
- raw-yaw hacks in every renderer/entity;
- duplicating server anti-cheat logic wholesale if narrow semantic projections
  can preserve it.

---

## 21. Deterministic test matrix

### Body/frame
- six faces world<->local vector round trip;
- physical velocity invariant across gravity-face transition;
- local view direction invariant under face transport;
- rotated AABB anchor;
- eye position;
- attachment offset transform.

### Collision
- local Y first all faces;
- tangent collision;
- step-up;
- support slice;
- onGround;
- fall distance;
- +Y vanilla equivalence.

### Locomotion
- jump;
- gravity;
- slow fall;
- levitation;
- climb;
- tangent friction;
- elytra local math;
- fluid integration contracts.

### Forces
- physical explosion vector unchanged by gravity frame;
- physical leash vector;
- local jump/bubble vector;
- mixed knockback adapter.

### Network
- physical packet XYZ unchanged;
- physical relative teleport axes preserve physical velocity components;
- local packet displacement projection for jump/floating/fall;
- gravity-face synchronization transition cases;
- client/server view ray equality.

### Attachments
- passenger seat;
- vehicle attachment;
- leash hold point;
- sleeping anchor;
- dismount floor/clearance.

### Projectile
- body-local launch -> physical ray;
- physical collision;
- local gravity;
- local orientation-from-motion.

---

## 22. Single Phase-7 / R4 runtime acceptance matrix

Final implementation acceptance should be batched.

1. +Y vanilla baseline.
2. player walk/run/jump/crouch.
3. step-up and local-ground support all rotated faces.
4. fall damage all faces.
5. gravity edge transition preserving physical momentum.
6. no velocity discontinuity at gravity-face change.
7. climb representative.
8. slow falling/levitation.
9. elytra representative.
10. item + XP + TNT + falling block.
11. projectile launch/flight/impact.
12. explosion knockback physical radial direction.
13. entity/entity push.
14. leash pull and rope anchor.
15. passenger seating.
16. dismount.
17. sleeping/stand-up.
18. hanging entity local wall attachment.
19. minecart straight/curve/slope/seam after Phase 2F.
20. boat/fluid/bubble after Phase 5.
21. ray/crosshair physical block hit side.
22. client/server use-item-on agreement.
23. player movement no false floating/rubber-band.
24. teleport/relative teleport preserves physical velocity.
25. vehicle movement no false floating/rubber-band.
26. exact gravity edge/corner client/server frame agreement.
27. representative NeoForge mod entity.
28. performance/network smoke test.

Dependency-marked rows may execute after their owning Phase 2/5 work, but the
R4 architecture remains fixed.

---

## 23. R4 completion decision

R4 is COMPLETE when:
- physical vs body-local entity contracts are explicit;
- deltaMovement local-storage is rejected as long-term architecture;
- Entity collision/support/fall ownership is mapped;
- Living locomotion families are mapped;
- non-living/projectile/vehicle families are mapped;
- passengers/attachments/dismount/leash/sleep ownership is mapped;
- physical vs local force policy is explicit;
- interaction ray/hit contract is explicit;
- movement packet XYZ vs local validation semantics are explicit;
- gravity-face synchronization is identified as a network gate;
- NeoForge hooks and portability boundaries are documented;
- deterministic and runtime acceptance matrices are defined.
