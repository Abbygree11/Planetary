# Particle Mixin call-site owner contracts — 1.21.1 / NeoForge 21.1.215

Status: source-researched and code-adjusted; **client startup and in-world probes observed passing 2026-10-08; Phase-4 particle gameplay acceptance PENDING**.

## Incident chronology

- User's first full client crash after `a9c683f`: at
  `SemanticTickDeltaParticleMixin.planetary$reinterpretTickDeltaBeforeMove`,
  target `Lnet/minecraft/client/particle/Particle;move(DDD)V`, matched 0/1
  in `ParticleEngine.registerProviders`. Unit build previously succeeded.
- After splitting six subclass Mixins, a JUnit loader crash occurred because
  the bytecode test itself was incorrectly placed in reserved
  `dev.planetary.mixin`. Test and non-mixin runtime helper were moved to
  `dev.planetary.gravity`.
- User's latest log after `70a02a25`: `runClient` reached
  `ParticleEngine.registerProviders:114` but crashed on
  `WaterCurrentDownParticleGravityMixin.planetary$rotateCurrentSpiral`,
  again target `Particle.move(DDD)V`, 0/1 matched.
  Because the PowerShell command chains scripts with `&&`, reaching
  runClient means the earlier test step returned success for that attempt.

## Mechanism and exact fix

The 1.21.1 client sources for `DripParticle`, `WaterDropParticle`,
`BubblePopParticle`, `WakeParticle`, `CampfireSmokeParticle`,
`BubbleParticle`, `WaterCurrentDownParticle` and
`DragonBreathParticle` all define a subclass `tick()` that calls
`this.move(xd, yd, zd)`. Java resolves inherited methods but encodes
the **concrete subclass** as the symbolic owner for an `INVOKEVIRTUAL`
call to `this.move` inside that subclass. A Mixin `@At(INVOKE)` is
matched against a **call-site owner+name+descriptor**, not against the
method's declaring class in its superclass.

Source reference for visual class-method inventory:
`hackersense/OptiFine-Source`, directory `1.21.1/`,
`net/minecraft/client/particle`, including `WaterCurrentDownParticle`,
`DragonBreathParticle`, `ShriekParticle`. These are comparative
vanilla sources, not bytecode verification of NeoForge runtime. The
Gradle ASM tests check the actual 1.21.1 class files at test execution.

| Calling method | Count | Required exact INVOKE target |
|---|---:|---|
| DripParticle.tick | 1 | `DripParticle;move(DDD)V` |
| WaterDropParticle.tick | 1 | `WaterDropParticle;move(DDD)V` |
| BubblePopParticle.tick | 1 | `BubblePopParticle;move(DDD)V` |
| WakeParticle.tick | 1 | `WakeParticle;move(DDD)V` |
| CampfireSmokeParticle.tick | 1 | `CampfireSmokeParticle;move(DDD)V` |
| BubbleParticle.tick | 1 | `BubbleParticle;move(DDD)V` |
| WaterCurrentDownParticle.tick | 1 | `WaterCurrentDownParticle;move(DDD)V` |
| DragonBreathParticle.tick | 1 | `DragonBreathParticle;move(DDD)V` |
| ShriekParticle.render | 2 | `ShriekParticle;renderRotatedQuad(VertexConsumer,Camera,Quaternionf,F)V` |

First six are already isolated in class-specific TickDelta Mixins.
This incident fixes the remaining `WaterCurrentDownParticleGravityMixin`,
`DragonBreathParticleGravityMixin` and the independently discovered
`ShriekParticleRenderGravityMixin`. The Shriek custom `render` body
calls `this.renderRotatedQuad` twice; it also incorrectly targeted
declaring superclass `SingleQuadParticle`. The `@ModifyArg` hook
should match both subclass-owned calls.

We deliberately do not alter:
- `Particle.move` shared collision runtime;
- local semantics of spiral/dragon tick/quad rotation;
- base particle lifecycle, fluids, RNG or emitter outputs;
- default `require=1`: missing injection must still fail visibly,
  never silently skip the adapter.

## Guardrail

`ParticleTickInvocationTargetTest` verifies **eight** runtime
`tick` methods contain exactly one expected concrete-owner
`INVOKEVIRTUAL` and reads compiled Mixin `@Inject/@At` annotations
without classloading Mixins to verify their exact targets.
It also checks all eight Mixins are registered.

`ParticleRenderInvocationTargetTest` verifies the two
`ShriekParticle.render` `renderRotatedQuad` calls and
`ShriekParticleRenderGravityMixin` `@ModifyArg/@At` target.

Static manual sweep of Phase-4 injector types found the additional
dangerous inherited call `ShriekParticle.renderRotatedQuad`. Other
examined particle emitter hooks use a typed `Level.addParticle`
receiver or a direct `LeavesBlock` super call, not these inherited
particle methods. Do not generalize or rewrite those without evidence.

## Acceptance

1. `git pull && .\test.ps1 && .\run-client.ps1`
2. ASM runtime-owner AND compiled annotation tests must pass.
3. Client must pass ParticleEngine.registerProviders, load main menu,
   and enter Planet world without Mixin/IllegalClassLoadError.
4. Then validate Phase-1 capabilities diagnostics and resume the
   single Phase-4 gameplay acceptance matrix after outstanding
   particle emitter/constructor cases are implemented.

No local Gradle build, launch or manual gameplay was run by the assistant.


## 2026-10-08 fourth checkpoint: evidence after corrected owners

User-provided Minecraft screenshot shows an active dedicated Planet world
and messages: gravity attached; standing/wall, shape, support, placement,
Frame API, and new local-vs-physical capability diagnostic probes all
report `passed`. The client therefore got past ParticleEngine
initialization and the previously failing Mixin startup path.

The `22 raw BlockPos.relative mismatches detected` are expected:
`PlanetCompatibilityDiagnostics` intentionally requires a nonzero
difference to demonstrate that raw physical neighbors differ from
local-frame neighbors.

Scope of evidence: positive client-world runtime smoke; no per-particle
visual/behavior acceptances and no complete Gradle test console log
supplied for this attempt. Continue to keep the full Phase-4 particle
batch gameplay matrix and cross-mod acceptance pending.
