# Phase 4: candle particle emitters (1.21.1 / NeoForge 21.1.215)

Status: source architecture audited; runtime implementation/test acceptance PENDING.

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
- **Correct minimal injection:** `@ModifyArg` replacing only the
  `Consumer` argument to `Iterable.forEach` in `animateTick`.
  On rotated Planet blocks, wrap the original consumer so it receives
  transformed absolute-local unit coordinates. Vanilla forEach,
  random/sound/sprite/particle APIs and call sequence all execute.
  On ordinary levels/+Y return original consumer exactly.

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
`AbstractCandleBlock.class` and Mixin `@ModifyArg/@Redirect`
compiled annotations without loading Mixin via reflection. Respect
`defaultRequire=1`; do not mute mismatch errors.

Pure tests: transformed offset round trip on all six faces,
vanilla +Y equivalence for the 1–4 candle offsets and candle-cake
offset (0.5,1.0,0.5), extinguish exact 0.1F UP velocity.

## Risks and acceptance

- `forEach` callback is a Java method with one `Consumer` argument:
  `@ModifyArg(index=0)` must preserve vanilla lambda invocation,
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
