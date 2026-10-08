# Phase 4: candle particle emitters (1.21.1 / NeoForge 21.1.215)

Status: **IMPLEMENTED / BUILD, CLIENT AND GAMEPLAY ACCEPTANCE PENDING**.

## Vanilla call flow (comparative 1.21.1 source)

Reference: `hackersense/OptiFine-Source`, 1.21.1
`net/minecraft/world/level/block/AbstractCandleBlock.java`,
`CandleBlock.java`, `CandleCakeBlock.java`.

### Lit `AbstractCandleBlock.animateTick(state, Level, pos, RandomSource)`

- Checks `LIT`, otherwise does nothing.
- Calls polymorphic protected `getParticleOffsets(state)` on the
  actual candle subtype; DO NOT hook `CandleBlock.animateTick` because
  subclasses inherit the implementation.
- Invokes `Iterable.forEach(Consumer)`; vanilla closure adds
  (pos.x, pos.y, pos.z) to each returned LOCAL unit-block offset.
- Private `addParticlesAndSound` calls `random.nextFloat()` once per
  candle: with probability <0.3 SMOKE, with probability <0.17
  CANDLE_AMBIENT sound + two additional floats, always SMALL_FLAME.
  Sound XYZ is computed from the emitted position +0.5 each.
- **Correct minimal injection (after runtime correction):** scoped
  `@Redirect` for `Iterable.forEach` in `animateTick`.
  The redirect receives the invoked Iterable + Consumer followed by
  enclosing `animateTick(state,level,pos,random)` arguments. On rotated
  Planet blocks it calls `offsets.forEach(offset -> originalConsumer.accept(
  rotateUnitBlockEmitterOffset(offset, frame)))`; vanilla original
  consumer still generates every particle and sound in the same order.
  Outside Planet/+Y, call `offsets.forEach(originalConsumer)` unchanged.
  `@ModifyArg` is INVALID with captured enclosing arguments.

### `AbstractCandleBlock.extinguish(Player?, state, LevelAccessor, pos)`

- Vanilla sets `LIT=false` first and checks the actual block subtype;
  then invokes another `Iterable.forEach` with a separate lambda.
- Each offset causes one SMOKE at `pos + offset`, with hardcoded
  `(0, (double)0.1F, 0)` velocity.
- Outside that iterable, vanilla plays `CANDLE_EXTINGUISH` sound and
  sends `GameEvent.BLOCK_CHANGE` exactly once.
- **Correct minimal injection:** scoped `@Redirect` of this single
  `Iterable.forEach(Consumer)` inside `extinguish`.
  On rotated physical `Level`, iterate same supplied offsets in
  same order and emit smoke at converted position with the exact
  float-originated 0.1F term rotated into local UP; on vanilla,
  +Y or virtual `LevelAccessor` call the original
  `offsets.forEach(originalConsumer)` untouched.
- This reproduces only the tiny 1.21.1 extinguish smoke lambda:
  one particle per original offset, same order, no RNG. Sound/state/
  game event remain in original vanilla method. Do NOT cancel
  `extinguish` or rerun those events.
- `PlanetBlockGravity.frameAt` requires a concrete Level and must
  never attempt to reframe foreign worldgen/LevelAccessor spaces.

## Shared semantic transform

Every candle offset is authored as a position in the LOCAL
block-unit cube, not already a physical absolute XYZ. For offset
`(u,v,w)`, compute `(0.5,0.5,0.5) +
frame.localToWorld(u-0.5,v-0.5,w-0.5)`.
The vanilla consumer later adds block integer `pos`, yielding
exactly the existing PlanetParticleEmitter physical position.
This must support v=1.0 for candles on cakes and not rely on
`BlockPos.containing(absolutePosition)` (would pick wrong cell
near block top/edges).

Use a semantic helper in `dev.planetary.gravity` and only thin
Mixin callbacks in reserved `dev.planetary.mixin`.

## Mixin/bytecode risks and contracts

Both `animateTick` and `extinguish` invoke the Java default
interface `java.lang.Iterable.forEach(Consumer)`.
Expected bytecode owner is `java/lang/Iterable`, method
`forEach(Ljava/util/function/Consumer;)V`, `INVOKEINTERFACE`,
exactly one call per target method. The lambda's own synthetic
body must not be targeted by name.

Add contract tests reading the **actual** Minecraft
`AbstractCandleBlock.class` and both Mixin `@Redirect`
compiled annotations, PLUS each redirect handler's full JVM descriptor
and required staticness, without loading Mixin via reflection. Respect
`defaultRequire=1`; do not mute mismatch errors.

Pure tests: transformed offset round trip on all six faces,
vanilla +Y equivalence for the 1–4 candle offsets and candle-cake
offset (0.5,1.0,0.5), extinguish exact 0.1F UP velocity.

## Risks and acceptance

- `forEach` callback is a Java method with one `Consumer` argument:
  scoped `@Redirect` can capture the receiver Iterable, its Consumer
  and original animateTick arguments; do not use `@ModifyArg` with
  source method arguments. Preserve vanilla lambda invocation,
  with no extra random samples or particle calls.
- `@Redirect` inside static `extinguish` must capture its
  method arguments and operate only on Level-backed Planet worlds.
- No global LevelAccessor/ParticleUtils interception.
- No extra retained per-block maps or caches; wrapper Consumer
  allocation limited to actively rotated lit candle paths.
- Review possible foreign subclasses of AbstractCandleBlock: their
  offset contract is canonically block local; this adapter is
  intentionally polymorphic, not restricted to vanilla blocks.

Status remains IMPLEMENTED / gameplay acceptance pending only after
code exists and user confirms startup. One Phase-4 subsystem
manual acceptance checks candles, candle-cakes, extinguishing smoke
on +Y and rotated faces, plus previous accepted particles.

## 2026-10-08 code checkpoint (acceptance pending)

Implemented in `dev.planetary.mixin.AbstractCandleParticleEmitterGravityMixin`
and registered in `planetary.mixins.json`, with version-stable
`PlanetParticleEmitter.rotateUnitBlockEmitterOffset`:

- `@Redirect` on `AbstractCandleBlock.animateTick` redirects only
  the existing `Iterable.forEach` call and forwards sampled local
  block-unit offsets into the original Consumer. The original lambda
  creates flame/smoke and optional ambient sound, consuming the same RNG.
- `@Redirect` on static `AbstractCandleBlock.extinguish`
  leaves original iterable dispatch untouched in +Y, non-Planet or
  non-Level spaces; rotated physical Planet worlds emit exactly one
  smoke particle per original candle offset, reframe its origin,
  and rotate the exact double-cast of `0.1F` local-up velocity.
  The original method still does `setLit`, sound and game event.
- No global particle emission intercept, no candle subclass patch,
  no new world-axis fallback, no change to candle blockstate/render.

Added semantic unit tests in `PlanetParticleEmitterTest` for
1–4-candle offset samples and candle-cake v=1.0 position on all
six faces, +Y vanilla equivalence and puff velocity round trip.
`CandleParticleEmitterInvocationTest` checks exact
`INVOKEINTERFACE java/lang/Iterable.forEach(Consumer)`
for BOTH vanilla methods and inspects compiled `@Redirect`,
`@Mixin` annotations, handler JVM descriptors/staticness and JSON registration
without Mixin classloading.

No assistant Gradle build, client launch or gameplay acceptance
performed. Check on user's next batched build/startup checkpoint;
do not ask them to test lit/extinguish candles individually until
the full Phase-4 gameplay matrix is ready.

## 2026-10-08 user startup failure after first candle patch — root cause and remedy

User provided full runClient crash log:
`org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException`
while applying `AbstractCandleParticleEmitterGravityMixin`, before
Minecraft bootstrap completed. Message:

    @ModifyArg injector planetary$reframeLitCandleOffsets targets a
    method with an invalid signature (Ljava/util/function/Consumer;),
    expected (Ljava/util/function/Consumer;L.../BlockState;L.../Level;
    L.../BlockPos;L.../RandomSource;)

This points to the callback's **INCOMPATIBLE argument capture contract**
rather than a missing target or a math/RNG bug. Mixin's `@ModifyArg`
handler must take only the argument being changed; it cannot capture
the surrounding `animateTick` arguments as our first implementation
attempted. Our first ASM regression only checked injection owner/target,
not handler descriptor, and therefore failed to catch this mistake.

**Rejected:** original `@ModifyArg` on `animateTick` with appended
BlockState, Level, BlockPos, RandomSource. It compiled and JUnit tests
ran, but NeoForge runtime Mixin transformation rejected the method.

**Corrective patch (IMPLEMENTED / RE-STARTUP PENDING):**
- change lit `animateTick` callback into `@Redirect` for the exact
  same `Iterable.forEach` INVOKE, with handler parameters in the
  documented Mixin redirect order: Iterable receiver, Consumer arg,
  then enclosing BlockState, Level, BlockPos, RandomSource.
- inside the redirect, original `Iterable.forEach` executes exactly
  once. On active rotated Planet, wrap the original Consumer to
  reinterpret only block-local unit offsets. Ordinary/+Y executes
  the unmodified Consumer, no extra lambda allocation.
- leave static `extinguish` redirect, smoke puff velocity, semantic
  offset helper and original candle sound/state/RNG behavior unchanged.
- add ASM regression that asserts BOTH redirect method signatures
  verbatim and checks static-vs-instance receiver contract; update
  compiled Mixin annotation expectations from ModifyArg+Redirect
  to Redirect+Redirect.

Reference for signature rule: SpongePowered/Mixin
`org.spongepowered.asm.mixin.injection.Redirect` JavaDoc:
method redirects can append **enclosing target method arguments**
after the invoked receiver and arguments; unlike `ModifyArg`.
Official source: github.com/SpongePowered/Mixin/blob/master/
src/main/java/org/spongepowered/asm/mixin/injection/Redirect.java

**No code change to accepted non-candle emitters or gravity math.
No new client confirmation yet.** Do not mark candle implementation
PASS until Gradle + bootstrap + Planet login and later full Phase-4
gameplay matrix.
