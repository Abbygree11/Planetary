# Particle subsystem audit matrix — Minecraft 1.21.1

Status: Phase 4 implementation inventory.  
Acceptance policy: BATCH only; no class-by-class gameplay acceptance.

Source basis:
- all 74 Java files under net/minecraft/client/particle in the audited 1.21.1
  source tree;
- ParticleEngine.registerProviders;
- targeted emitter/source audits outside the client particle package.

Legend:
- BASE — standard Particle.tick / Particle.move shared adapters own the behavior.
- CUSTOM — class has semantic axis behavior outside the base engine.
- EMITTER — source/origin/velocity creation path needs local-frame adaptation.
- F5 — final correctness depends on Phase 5 fluid topology/height/flow.
- F7 — final correctness depends on entity/body semantics.
- F9 — final correctness depends on runtime portal topology.
- FRAME-INDEPENDENT — no gravity-relative axis semantics found in the particle
  implementation itself.
- PASS — manually accepted runtime behavior already exists.

## Shared engine

| Mechanism | Current Planet owner | Status |
|---|---|---|
| Particle seven-arg +0.1 UP bias | ParticleGravityMixin + PlanetParticleMotion | implemented |
| Particle.setPower +0.1 preserved baseline | ParticleGravityMixin | implemented |
| Particle.tick gravity | ParticleGravityMixin | implemented |
| Particle.tick blocked-vertical XZ boost | ParticleGravityMixin + local post correction | implemented |
| Particle.tick onGround XZ friction | ParticleGravityMixin + local post correction | implemented |
| base Particle.move collision ordering | LocalGravityParticleMoveMixin -> PlanetParticleMoveRuntime | implemented; generalized from accepted Terrain fix |
| local stoppedByCollision/onGround/tangent clipping | PlanetParticleCollisionResponse | implemented |
| custom tick additive semantic delta before move | SemanticTickDeltaParticleMixin | implemented for audited allowlist |
| component-wise world-axis damping -> local axes | PlanetParticleMotion.remapComponentScales | implemented |
| fixed subclass world-Y launch term | FixedVerticalLaunchParticleMixin/helper | implemented for audited allowlist |
| block-local emitter coordinates | PlanetParticleEmitter | implemented foundation |

## Registered particle-class matrix

### Already accepted / dedicated implemented

| Class/family | Classification | Notes |
|---|---|---|
| TerrainParticle | BASE move + destroy emitter | PASS: radial burst/local fall/no rotated floor crawl |
| FallingDustParticle | CUSTOM | dedicated local DOWN acceleration + terminal clamp; phase acceptance pending |
| CherryParticle | CUSTOM + EMITTER | dedicated local wind/gravity/removal; CherryLeavesBlock local emitter |
| torch/flame/smoke origins | EMITTER | ordinary/soul/redstone torch emitters PASS away from exact edge |
| CampfireSmokeParticle | CUSTOM + EMITTER | constructor random rise + pre-move semantic delta; CampfireBlock emitter implemented |
| DragonBreathParticle | CUSTOM | local pre-move vertical semantics + post-move component-scale correction |
| SnowflakeParticle | BASE + CUSTOM post-scale | local vertical/tangent damping adapter |
| SquidInkParticle | BASE + CUSTOM air fall | local in-air DOWN acceleration |
| WaterDropParticle/Splash | CUSTOM + F5 | direct gravity + local ground damping; launch adapted; surface-height removal blocked by F5 |
| WaterCurrentDownParticle | CUSTOM + F5 | initial local DOWN + local tangent spiral; fluid-membership final gate F5 |
| DripParticle family | CUSTOM + F5 | pre-move gravity delta + base local move; fluid-height removal final gate F5 |
| BubbleParticle | CUSTOM + F5 | local rise via semantic tick delta; water membership F5 |
| BubblePopParticle | CUSTOM + F5 | local gravity via semantic tick delta; final integration F5 |
| WakeParticle | CUSTOM + F5 | custom gravity path covered; source/fluid integration F5 |

### Base-engine families: shared coverage, emitter/constructor audit may still apply

| Class/family | Classification | Remaining issue |
|---|---|---|
| CritParticle / DamageIndicator / MagicProvider | BASE | emitter inputs only |
| ExplodeParticle / SpitParticle | BASE | emitter inputs only |
| DustParticleBase / Dust / DustColorTransition | BASE | constructor is mostly isotropic; emitter audit |
| GlowParticle | BASE | provider-specific Y/tangent scaling requires provider audit |
| HeartParticle | BASE + fixed launch | +0.1 local UP implemented; AngryVillager origin +0.5 needs emitter/body policy |
| NoteParticle | BASE + fixed launch | +0.2 local UP implemented; note-block origin audit |
| SpellParticle | BASE + constructor axis semantics | dedicated constructor correction still required |
| TrialSpawnerDetectionParticle | BASE + constructor axis semantics | dedicated constructor correction still required |
| BaseAshSmokeParticle family | BASE + constructor axis semantics | anisotropic constructor scales require family correction |
| Smoke / WhiteSmoke / LargeSmoke | BaseAshSmoke | source velocities/emitter audit |
| Ash / WhiteAsh | BaseAshSmoke | WhiteAsh provider axis-authored random velocity audit |
| DustPlumeParticle | BaseAshSmoke + explicit +Y term | constructor +0.15 local-UP correction required |
| LavaParticle | BASE + generated launch + nested EMITTER | launch implemented; lava/fluid source emitter F5; nested smoke inherits physical velocity |
| SoulParticle / RisingParticle | BASE/custom move family | no extra hard-coded axis found in Soul tick |
| TotemParticle / SimpleAnimatedParticle | BASE | emitter input audit |
| EndRodParticle | BASE tick + custom direct move | move is direct physical translation; emitter input audit |
| FlameParticle | BASE tick + custom direct move | direct move has no vertical classification; emitter origin/velocity audit |
| BreakingItemParticle | BASE | input vector physical; item-break emitter audit |
| SculkCharge/Pop | visual/custom tick | no gravity semantic found yet; source emitter audit |
| GustParticle | visual/custom tick | no gravity semantic found in particle tick |
| SonicBoom/HugeExplosion | visual | frame-independent particle body; emitter position audit |

### Custom trajectory / external-owner cases

| Class/family | Classification | Owner/gate |
|---|---|---|
| PortalParticle | CUSTOM trajectory | local-Y arc implemented; final portal emitter/orientation gate F9 |
| ReversePortalParticle | FRAME-INDEPENDENT trajectory | straight physical vector; final portal emitter F9 |
| FlyTowardsPositionParticle | CUSTOM trajectory | local-Y arc implemented; start/vector remain physical source geometry |
| FlyStraightTowardsParticle | FRAME-INDEPENDENT trajectory | straight start+vector interpolation |
| PlayerCloudParticle | CUSTOM + F7 | pulls position/velocity toward Player world Y; body-local meaning waits on entity frame policy |
| TrackingEmitter | EMITTER + F7 | samples entity AABB via getY and adds +0.2 to Y velocity; body-local origin/UP requires Phase 7 integration |
| ItemPickupParticle | FRAME-INDEPENDENT + F7 | physical interpolation between item/entity positions; no gravity-local axis in class |
| VibrationSignalParticle | FRAME-INDEPENDENT | follows physical vibration target path |
| FireworkParticles | nested EMITTER + F7 | Spark base gravity handled by shared engine; explosion/star/burst orientation is authored by Starter/rocket and deferred to entity/rocket frame integration |
| ShriekParticle | fixed launch + render orientation | +0.1 local UP + local->world render quaternion implemented |
| AttackSweepParticle | visual | no movement axis semantics in tick |
| BlockMarker | visual | static physical marker |
| MobAppearanceParticle | visual | camera/render effect |
| SuspendedParticle | visual/stationary | fluid/environment source integration only |
| SuspendedTownParticle | custom direct move | scalar physical motion, no vertical/tangent distinction in tick |
| NoRenderParticle | infrastructure | no own semantics |

## Fluid-coupled integration gates (Phase 5)

The particle-side frame work is Phase 4, but these exact vanilla checks cannot be
declared correct before the fluid subsystem exists:

- DripParticle:
  fluidState.getHeight + this.y < blockY + height;
- WaterDropParticle:
  collisionShape.max(Direction.Axis.Y, x, z) and FluidState.getHeight;
- Bubble/BubbleColumn:
  getFluidState(BlockPos.containing(...)).is(WATER);
- WaterCurrentDown:
  water membership and local current topology;
- splash/wake emitters from fluid/entity interactions;
- lava random-display emitters;
- dripstone water/lava source integration;
- bubble-column/current direction.

Phase 5 must re-run these integration rows, not reimplement the particle motion
already owned here.

## Entity/body integration gates (Phase 7)

- PlayerCloudParticle nearest-player vertical attraction;
- TrackingEmitter entity body sampling and +UP velocity;
- entity-status heart/totem/crit origins that use entity Y/height;
- projectile/item impact emitter velocities where the emitting entity frame is
  the semantic source;
- firework rocket trajectory/nested emitter orientation.

The particle class should consume PHYSICAL emitter coordinates/velocity after
the Phase-7 body adapter has produced them.

## Portal integration gate (Phase 9)

PortalParticle has a particle-local curved trajectory that can be adapted in
Phase 4. Final source/orientation correctness depends on the Phase-9 portal
rectangle/frame policy.

ReversePortal/FlyStraight paths are physically straight once start/vector are
correct.

## Emitter families outside net.minecraft.client.particle

Audit/implementation buckets:

- block animateTick:
  torch/redstone torch, campfire, lava/fire, leaves/spore blossom,
  dripstone, note block, portal, sculk, trial/vault;
- ParticleUtils:
  below/above/axis helpers and face sampling;
- entity status/tracking:
  TrackingEmitter and entity broadcast/status particle bursts;
- fluid random display:
  water/lava/bubble/current/splash;
- nested particle classes:
  lava -> smoke, drip -> falling/landing, firework -> trail/flash,
  explosion/gust seeds;
- weather renderer:
  rain/splash/precipitation source positions are environment policy and must
  coordinate with Phase 7B.

## Remaining Phase-4 implementation queue

Non-blocked before final batch acceptance:

1. BaseAshSmoke constructor-family local component scaling.
2. SpellParticle constructor local-axis normalization/scaling.
3. TrialSpawnerDetectionParticle constructor local vertical bias vs physical
   supplied target velocity.
4. DustPlume explicit +0.15 local-UP term.
5. [RECLASSIFIED -> Phase 7] GlowSquid ink provider: source velocity is authored
   from Squid body orientation; particle class should consume the physical
   body-produced vector after entity-frame integration.
6. [IMPLEMENTED] PortalParticle local vertical arc.
7. [IMPLEMENTED] FlyTowardsPositionParticle local vertical arc.
8. [IMPLEMENTED] Shriek local-frame render orientation.
9. [PARTIAL] Campfire emitter implemented; remaining non-fluid emitter-family
   source sweep is the next Phase-4 batch.
10. [RECLASSIFIED -> Phase 7 integration] Firework Spark base gravity is already
    covered by shared Particle.tick. Explosion/star/burst orientation is authored
    by the Firework Starter/rocket source and must follow the entity/rocket frame
    decision rather than be guessed inside SparkParticle.
11. [AUDITED] ParticleUtils is intentionally NOT globally reframed:
    - spawnParticlesAlongAxis/OnBlockFaces can represent physical block axes;
    - spawnParticleBelow is semantic only at specific callers (Cherry handled);
    - spawnSmashAttackParticles is body/ground-owned -> Phase 7;
    - caller ownership decides adaptation.
12. [POLICY GATE -> Phase 7B] weather/precipitation rows.

No gameplay acceptance is requested until these non-blocked rows are complete.
