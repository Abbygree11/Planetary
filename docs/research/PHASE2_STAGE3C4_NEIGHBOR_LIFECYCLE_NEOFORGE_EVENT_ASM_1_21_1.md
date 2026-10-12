# Stage 2.3C-4 — BlockStateBase / NeighborUpdater / updateOrDestroy: shared lifecycle and NeoForge contracts (4/4)

**2026-10-12 · Planetary branch `2.0` · Minecraft 1.21.1 · NeoForge 21.1.215 · bounded classpath-ASM research, NOT gameplay implementation.**

## Evidence and scope

- Source-of-truth original registry Stage2.3A/2.3B: **241/241 vanilla registered concrete Block classes, 1060/1060 BLOCK IDs; 87/87 concrete Item classes and 1333/1333 ITEM IDs** source/registered-owner audited. This **does not** mean all 241 Block owner-class patched ASM/Mixin/gameplay acceptance complete.
- Direct comparative source inspected for [BlockBehaviour.BlockStateBase](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java), [Block](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/Block.java), [NeighborUpdater](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/redstone/NeighborUpdater.java), [CollectingNeighborUpdater](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/redstone/CollectingNeighborUpdater.java), [InstantNeighborUpdater](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/redstone/InstantNeighborUpdater.java), [Level](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/Level.java), [BushBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BushBlock.java).
- **Later official 1.21.1 comparative patch, not exact 21.1.215:** [Level.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/level/Level.java.patch) and [ServerLevel.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/server/level/ServerLevel.java.patch) contain `EventHooks.onNeighborNotify` and additional `BlockState.onNeighborChange` paths. No `NeighborUpdater` / `CollectingNeighborUpdater` / `InstantNeighborUpdater` patch found at **that comparative SHA**. Non-existence there is not proof about the target release.
- **Actual target test:** [Phase23C4NeighborLifecycleAsmContractTest](../../src/test/java/dev/planetary/world/Phase23C4NeighborLifecycleAsmContractTest.java), whose assertions read real Gradle-loaded **NeoForge 21.1.215 compiled classfiles** and compiled Planetary Mixin annotation/handler descriptors. [CI run 38269226491](https://github.com/Abbygree11/Planetary/actions/runs/38269226491) **completed SUCCESS** (Java compile, JUnit including the new strict ASM test, registry census). This proves the asserted ABI for Gradle-loaded 21.1.215 only, not the runtime behavior of notifications or weaving.
- Existing production Planetary owners inspected: [LevelMixin](../../src/main/java/dev/planetary/mixin/LevelMixin.java), [ServerLevelMixin](../../src/main/java/dev/planetary/mixin/ServerLevelMixin.java), [PlanetVanillaNeighborUpdates](../../src/main/java/dev/planetary/world/PlanetVanillaNeighborUpdates.java), [PlanetNeighborUpdateQueue](../../src/main/java/dev/planetary/world/PlanetNeighborUpdateQueue.java), [BushBlockLocalSupportMixin](../../src/main/java/dev/planetary/mixin/BushBlockLocalSupportMixin.java), [PlanetBlockSupportRuntime](../../src/main/java/dev/planetary/world/PlanetBlockSupportRuntime.java). **No production Java/Mixin/Gradle edits** in this stage.

## 1/4 — real declaring owners and two independent callback pipelines

The common vanilla dispatch is **polymorphic but not an orientation algorithm**:

| Owner | Mechanism | Domain and compatibility |
| --- | --- | --- |
| `BlockBehaviour.BlockStateBase.updateShape(Direction,BlockState,LevelAccessor,BlockPos,BlockPos)` | Resolves concrete `getBlock().updateShape(state, ...)`; mod subclass override still receives callback | Engine dispatch shared; direction describes **physical neighbor** by default |
| `BlockStateBase.canSurvive(LevelReader,BlockPos)` | Resolves `getBlock().canSurvive(state, ...)` | Base default true; real survival rules belong to subclass/algorithm |
| `BlockStateBase.handleNeighborChanged(Level,BlockPos,Block,BlockPos,boolean)` | Resolves `getBlock().neighborChanged(state,...)` | **Non-state-returning** redstone/signaling/general callback, distinct from updateShape |
| `BlockStateBase.updateNeighbourShapes(LevelAccessor,BlockPos,flags,depth)` | Iterates engine `BlockBehaviour.UPDATE_SHAPE_ORDER`, dispatches `LevelAccessor.neighborShapeChanged` with opposite direction and coordinates | Physical 3D neighbor fan-out; recursion depth and flag propagation matter |
| `NeighborUpdater.executeShapeUpdate` | Reads actual target state, asks `BlockState.updateShape`, then `Block.updateOrDestroy(old,new,...,flags,depth)` | State-changing survival and connection updates, can destroy a block |
| `NeighborUpdater.executeUpdate` | Runs `BlockState.handleNeighborChanged` with crash-report context | No automatic `updateOrDestroy` state-return path |
| `InstantNeighborUpdater` | Executes shape/neighbor callbacks immediately, decrements update depth | Physical event order; not equivalent to queued dispatch |
| `CollectingNeighborUpdater` | Ordered/limited `ShapeUpdate / FullNeighborUpdate / SimpleNeighborUpdate / MultiNeighborUpdate` queue | Prevent infinite neighbor chains; queued positions need to remain immutable and real |
| `Block.updateOrDestroy` | If state changed: `new.isAir()` → `LevelAccessor.destroyBlock` with suppress-drops flag; else `LevelAccessor.setBlock` with flag adjustments; recursion guard | Preserve drops, BlockEntities, neighbor events, permissions, mod extensions and client notifications |
| `Block.updateFromNeighbourShapes` | Iterates engine physical `UPDATE_SHAPE_ORDER`, invoking `BlockState.updateShape` | Independent consumer used by commands/templates and state transformations |

`NeighborUpdater.UPDATE_ORDER` and `BlockBehaviour.UPDATE_SHAPE_ORDER` have separate declarations/semantics. One generalized direction reorder would also affect redstone/logistics, fluids, worldgen and modded physical graphs and is unsafe.

## 2/4 — lifecycle, survival, scheduled updates and separation of concerns

`Level.setBlock` may call `onRemove/onPlace`, update neighbor signals, `updateIndirectNeighbourShapes`, `updateNeighbourShapes`, and `updateIndirectNeighbourShapes` for the new state, conditioned by flags such as `UPDATE_KNOWN_SHAPE` and recursion depth. `Block.updateOrDestroy` is a **result/commit** stage; it does **not** decide a block's legal local support orientation. Its Air case must trigger the correct destruction path and NeoForge drop callbacks.

`BushBlock.updateShape` recomputes `state.canSurvive(level,pos)`; `BushBlock.canSurvive` checks `pos.below()` and dispatches virtual `mayPlaceOn` (some mods/classes override `canSurvive` independently). Planetary currently has `BushBlockLocalSupportMixin` targeting just the physical `BlockPos.below()` call, preserving the polymorphic soil predicate, but a separate implementation-stage audit must assert its exact compiled Mixin `@Redirect` owner and full handler descriptor. `BedBlock`, `DoorBlock`, `ScaffoldingBlock`, `RedstoneWireBlock`, fluids and modded blocks implement their own independent semantics. Stage2.3C-3 flagged cancellable BED/DOOR `updateShape` interceptors that can suppress unrelated callback processing and need in-game acceptance.

`BlockStateBase.tick(ServerLevel,BlockPos,RandomSource)` and `randomTick(...)` call independent concrete `Block.tick/randomTick` implementations. **Scheduled ticks, random ticks, fluid ticks, and temporal block changes do not share `NeighborUpdater` execution** and need distinct owner-family stage(s). Direct `Level.setBlock`, `StructureTemplate`, commands, bonemeal, block entities and virtual/fake Levels bypass the ordinary Item placement pipeline, and not all of them use this exact updater.

## 3/4 — NeoForge and mod compatibility with Planetary

**High-risk cross-mod boundary: cancellable Planetary `@Inject(HEAD)` before NeoForge events.** `LevelMixin` and `ServerLevelMixin` intercept `updateNeighborsAt` and `updateNeighborsAtExceptFromFacing`. When `PlanetLevelBridge.resolve(level,pos)` returns a Planet world, the handlers delegate directly to `resolved.world().updateNeighborsAt*` and **cancel** the vanilla method. The exact loaded 21.1.215 **does** invoke `EventHooks.onNeighborNotify` in those original methods, as verified by successful ASM/JUnit test; HEAD cancellation on Planet paths could bypass notifications to third-party NeoForge listeners. The passing ASM test proves **both the actual target-version event INVOKE and compiled Mixin HEAD/selector/handler/cancellable contract**. It does not run mod event listeners. This is an integration risk, not proof of a real missing mod event until instrumented runtime validation. Later NeoForge also adds `BlockState.onNeighborChange` path for comparator updates, which has separate ownership and should not be bypassed or flattened into ordinary shape updates.

Other LevelMixin/ServerLevelMixin `neighborChanged` and `neighborShapeChanged` HEAD interceptions use `PlanetVanillaUpdateFrame` and a reentry permit to canonicalize update source/target alias across faces; `PlanetVanillaNeighborUpdates.updateExcept` maps six local directions to physical/canonical target positions and calls the actual `Level.neighborChanged`. That routing may be useful for planet-shaped geometry, but **alias topology and physical mod side-effects are separate domains**. Unknown mods must receive true physical positions/sides unless they explicitly opted into Planet semantic chart coordinates.

The shared safe seam is a Planet-owned typed `NeighborEventProvenance` / `SupportQuery` (design sketch, not new runtime code) with real physical target+source, canonical source+target frames, local chart edge transport only when requested, update type (`SHAPE` vs `SIGNAL`), flags/recursion and source author. A thin version adapter must preserve NeoForge `EventHooks`, virtual block override, original queue semantics and modded hooks. Deeper **global** intercept at `Level.setBlock`, `BlockPos.relative`, `Direction`, `BlockStateBase.updateShape`, `NeighborUpdater` that changes meaning by default is unsafe; shallower only-Bush/Bed/Door patches miss connected modded owners.

| Candidate | Policy | Why |
| --- | --- | --- |
| Physical `BlockPos`/Level, six-direction neighbor graph, flags and queues | **PHYSICAL_DO_NOT_TOUCH** | Mods, capabilities, structure/worldgen/foreign levels expect real identity |
| Instrumentation at `BlockStateBase` and `NeighborUpdater` | **SHARED_SAFE observation ONLY** | Keep polymorphic dispatch, preserve NeoForge event calls and actual world writes |
| Local support/attachment, two-part seam linking and special updateShape semantics | **OPT_IN_ALGORITHM** | Use typed source/target frames, preserve `canSurvive` + mod state/property legality |
| Independent modded methods/reimplemented graph or virtual worlds | **MOD_INDEPENDENT / PHYSICAL** | Inherited vanilla Mixin does not cover an overridden mod method; opt-in extension or a proven narrow adapter |

**Real 1.21.1 mod source comparators already pinned in earlier audits:** [AE2](PHASE2_STAGE3A_MODDED_CRYSTAL_AE2_BUDDINGCRYSTALS_1_21_1.md) overrides crystal growth/neighbor semantics independently, and [Botany Pots](PHASE2_STAGE3A_AUTOMATION_LOGISTICS_ORIGINAL_BE_ITEM_MOD_1_21_1.md) uses its own physical sided inventory/BE propagation. This is a source-level demonstration of *different owners*, not proof these exact jars were run with Planetary. Compatible loaded-mod acceptance remains **NOT RUN**.

## 4/4 — strict ASM and separate acceptance

[Phase23C4NeighborLifecycleAsmContractTest](../../src/test/java/dev/planetary/world/Phase23C4NeighborLifecycleAsmContractTest.java) asserts exact classpath descriptors and call-site owners/count, `BlockStateBase` polymorphic routing, `NeighborUpdater` shape vs signaling split, instant/queued implementations, destruction/write preservation, and compiled **Level/ServerLevel Mixin** target selectors, `@At(HEAD)`, handler args/staticness, cancellable flags. A changed original NeoForge event hook must explicitly fail an assertion and be documented as a target-version difference, not be concealed by `require=0` or a name-only test.

| Gate | Status at test authorship |
| --- | --- |
| Source/patch comparison and shared semantic owner matrix | RESEARCHED 4/4 |
| Actual 21.1.215 compiled ASM + Mixin test committed | DONE (test authored) |
| GitHub Actions compile/JUnit | [Run 38269226491](https://github.com/Abbygree11/Planetary/actions/runs/38269226491) **PASS / completed SUCCESS**, tested compiled selected-method ABI and Mixin annotations |
| Real Mixin transformer application client+server | NOT RUN |
| NeoForge neighbor event observer and mod listener delivery | NOT RUN |
| Six canonical faces, 12 seams, 8 corners, no-Planet regression | NOT RUN |
| Loading real compatible third-party mod jar, virtual world and redstone/fluids | NOT RUN |
| All 241 registered vanilla concrete block owners patched ASM/gameplay | OPEN |

**Future acceptance:** normal vanilla no-Planet neighbor/drop/comparator/redstone behavior; support removal and reconnection on each face, 12 edge pairs/8 triple corners; loaded and unloaded chunk borders, recursion-limits and event ordering; `UPDATE_SUPPRESS_DROPS` and `UPDATE_KNOWN_SHAPE`, BlockEntity invalidation, fluid updates, `neighborChanged` and `neighborShapeChanged` parity; confirm NeoForge `NeighborNotifyEvent` with actual subscribed mod listener; loaded 1.21.1 mods with their overridden `updateShape` and custom `canSurvive`, independent fake Levels.

**Next 2.3C-5:** scheduled vs random block ticks + fluid scheduler, temporal block mutation, independent dispatch and NeoForge hooks. Four internal checks together, do not invent finished coverage of all vanilla classes.
