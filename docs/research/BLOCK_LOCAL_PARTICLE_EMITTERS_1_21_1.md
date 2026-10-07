# Phase 4: block-local particle emitter family — Minecraft 1.21.1

Status: **IMPLEMENTED / BUILD+CLIENT+GAMEPLAY ACCEPTANCE PENDING**, no gameplay PASS.

## Scope and ownership

This bounded implementation batch covers a common stable boundary:
`Block.animateTick(BlockState, Level, BlockPos, RandomSource)` computes
block-local emission coordinates (and sometimes local momentum), then
calls `Level.addParticle(ParticleOptions, double x,y,z,vx,vy,vz)`.

Minecraft physical `Level.addParticle`, particles' physical XYZ storage,
sound sources, RNG and emission count stay unchanged. The block constructs
its normal vanilla arguments and Planet adapts them **at the call site**,
before the Level API receives them. This matches the already accepted
torch/redstone-torch strategy in Planetary.

Primary comparative source references:
`hackersense/OptiFine-Source`, branch `main`, folder
`1.21.1/net/minecraft/world/level/block/`.
Read individual 1.21.1 `animateTick` methods in FurnaceBlock,
BlastFurnaceBlock, SmokerBlock, BrewingStandBlock, EndRodBlock,
RespawnAnchorBlock, EnderChestBlock, plus AbstractCandleBlock,
CandleBlock, CandleCakeBlock, EnchantingTableBlock,
SporeBlossomBlock and NetherPortalBlock for boundary/ownership
comparison. These are comparative decompiled sources, NOT an exact
NeoForge 21.1.215 transformed binary. Test exact bytecode signatures
during Gradle execution.

## Completed family inventory and adaptation decisions

| Block animateTick | Vanilla particles | Local position semantics | Momentum contract | Vanilla RNG count |
|---|---|---|---|---|
| FurnaceBlock | SMOKE + FLAME, same origin | FACING front offset ±0.52 plus random local tangent/height | zero, stay zero | unchanged |
| BlastFurnaceBlock | SMOKE | FACING front offset ±0.52, random tangent/height | zero | unchanged |
| SmokerBlock | SMOKE | fixed top position local Y+1.1 block-relative | zero | unchanged |
| BrewingStandBlock | SMOKE | sampled XYZ above brewing stand | zero | unchanged |
| EndRodBlock | END_ROD | local FACING displacement plus sampled 3D center jitter | isotropic Gaussian physical; do not rotate | unchanged |
| RespawnAnchorBlock | REVERSE_PORTAL | sampled local X/Z on top surface | local Y positive upward speed | unchanged |
| EnderChestBlock | PORTAL (three each animateTick) | random local block volume | authored local tangential X/Z and small local vertical term | unchanged |

All seven **declare their own animateTick** and the surveyed code
uses `Level.addParticle(ParticleOptions;DDDDDD)V`, so one narrow
multi-target `@ModifyArgs` adapter is a shared mechanism, not
seven copies of vanilla emitters.

Transform: subtract center `(pos.x+.5,pos.y+.5,pos.z+.5)` from
vanilla sampled position to get a canonical LOCAL block offset; apply
`PlanetGravityFrame.localToWorld`, add the SAME physical block center.
This works with canonical LOCAL `BlockState.FACING` for EndRod and
furnaces without double-transforming their direction inputs.

For RespawnAnchor and EnderChest only, the vanilla emitted velocity is
also canonical local, so rotate it once to physical; all other family
members' supplied zeros/Gaussian vector remain as originally authored.

Implemented in `BlockLocalParticleEmitterGravityMixin` (registered in
`planetary.mixins.json`). All seven emitters use one `@ModifyArgs` at the
existing Level.addParticle INVOKE; no redirects or duplicate emission.
Pure numerical tests: `PlanetBlockLocalEmitterBatchTest` (seven sampled
coordinates across six frames and two local velocity contracts).
Runtime bytecode/registration test:
`BlockLocalParticleEmitterInvocationTest` (the exact seven animateTick
methods, expected calls, compiled @Mixin list, compiled @ModifyArgs
anchor, and registered mixin entry). Test classes are in `dev.planetary.gravity`,
not in NeoForge's reserved Mixin package.

Properties:
- `+Y` and outside Planet must be exact pass-through, including
  original args object and RNG behavior;
- no second `addParticle` call, no new random call, no sound changes;
- sampling is done by vanilla before the hook, no re-simulation;
- frame lookup once per emission; no per-particle retained state/cache;
- do not reinterpret world-space velocities from external callers.

## Explicit deferred/not blindly adapted

- CandleBlock/CandleCakeBlock: `AbstractCandleBlock.animateTick`
  uses `getParticleOffsets`, a lambda and private static
  `addParticlesAndSound`; `extinguish` is a separate server/world
  emitter path. Needs a separate safe API-aware adapter to rotate
  candle offsets without rotating world-space sound locations twice.
  DO NOT hook `CandleBlock.animateTick` (method inherited, not present).
- EnchantingTableBlock: `BOOKSHELF_OFFSETS` must traverse **physical
  transformed neighbor topology**, not just rotate final effect; this
  source belongs to a combined block-neighbor/Phase-4 adaptation and
  must preserve bookshelf predicate, target and RNG.
- SporeBlossomBlock: distributed air-spore sampling iterates candidate
  block cells (`j - nextInt(10)`); rotating particle coordinates
  alone would leave world-Y-only occupancy/sampling. Needs block-neighbor
  topology and source + samples as a coherent family.
- NetherPortalBlock: portal-plane neighbor / AXIS ownership belongs
  to Phase 9; visual particle part Phase 4 but source topology is gated.
- World-physical/isotropic generic events are NOT globally converted.
- Block placement FACING correctness belongs to Phase 2; this batch
  consumes its existing canonical state contract.

## Bytecode safety

`@ModifyArgs` must match actual `Level.addParticle` call-site owner
and signature, with `defaultRequire=1`. Add unit tests reading
Minecraft 1.21.1 class files via ASM and count invocations
(Furnace=2; other six=1 per `animateTick` method) plus verify
compiled Mixin `@ModifyArgs/@At` target and mixin registration.
Never load mixin class via reflection; its package is reserved.

## Acceptance gate

One Phase-4 whole-subsystem acceptance pass later includes these
emitter effects on +Y, -Y, +/-X, +/-Z, near seam and corner when
the source owns it. For this implementation batch no manual
per-block requests. A game launch is an intermediate checkpoint
only if required by integration failures.

Build/test command, when necessary:
`git pull && .\test.ps1 && .\run-client.ps1`.
