# Planetary — `3R-2m`: original client world switch, dispatcher cancellation and stale-region provenance boundary

**2026-10-27 · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · branch `2.0`. RESEARCH ONLY (bounded 4/4).** No production Java, Mixins, V8 lab, save data or `main` changes. Full Phase3 3A–3E source/ABI/owner and gameplay research gate remains **OPEN**.

## 1/4 — Original client world lifecycle, declaring owners and version-exact static ABI

Committed [`Phase3R2mClientWorldDispatcherLifecycleAsmTest`](../../src/test/java/dev/planetary/world/Phase3R2mClientWorldDispatcherLifecycleAsmTest.java) to inspect untransformed exact JVM classfiles from original NeoForge 21.1.215 / Minecraft 1.21.1 Java21 test classpath. The first [CI #40405894697](https://github.com/Abbygree11/Planetary/actions/runs/40405894697) **SUCCESS**, source commit [`c72dc29`](https://github.com/Abbygree11/Planetary/commit/c72dc29643d50da45f27ea97c088b9c9a8887c14), original artifact **12689924623**, ZIP SHA256 **`2a6890156dfe145209e39d46bed46c3662140799467b405136ecaacec18127d5`** independently downloaded; **14/14 original classes**, **794 declared methods**, **78 selected traced method bodies**, **2075 ordered instructions**, **413 direct calls** and **24 invokedynamic** sites.

The widened probe includes the concrete `SectionRenderDispatcher$RenderSection$CompileTask` and `LevelEvent$Unload` original classes, all declared method descriptors and flags, and captures `Minecraft.updateLevelInEngines`, `SectionRenderDispatcher.uploadAllPendingUploads`, `RebuildTask.cancel` and relevant methods. The initial widened [CI #40406612183](https://github.com/Abbygree11/Planetary/actions/runs/40406612183) **FAILED only from a research test initialization error**: duplicated `onChunkUnloaded` literal in `Set.of` (Java throws `IllegalArgumentException`), not an actual client/world regression. Fixed in [`27d1d82`](https://github.com/Abbygree11/Planetary/commit/27d1d82d4982e7cb92fc0d65612c070398a47372).

The corrected authoritative widened [CI #40407267093](https://github.com/Abbygree11/Planetary/actions/runs/40407267093) **SUCCESS**, JUnit source commit `27d1d82`, artifact **12691368958**, **independently downloaded** ZIP SHA256 **`73a34586bcfb0cee44ebcc040bbd29aaa96f210ad66f50cb801b74d417feb843`**. Reports and exact SHA256:

| Original artifact member | SHA-256 |
| --- | --- |
| `phase3-r2m-original-client-owners.tsv` | `a652eecc57b0b5f4ace234e3dcec97217d7f72b6afe57ed78d31281656f9ebe1` |
| `phase3-r2m-original-client-method-inventory.tsv` | `6ed47a3c756920f9e5c5c993185113b6f24d3e1958f676c57b6c82eb6326ac75` |
| `phase3-r2m-original-client-instructions.tsv` | `7c13bc1281feb2c0b1c462000bc1dad0deafaeb08ea009925e50907bb4a4dec2` |
| `phase3-r2m-original-client-summary.txt` | `4bba1f4b0090a2d779ed783ba5a1a87e7f55411c777002dbeea4e40de05edc2d` |

Exact widened **16/16 classfiles present**, **801 declared JVM methods**, **88 selected original method bodies**, **2182 ordered instructions**, **429 direct method INVOKEs**, **25 invokedynamic**. This is selected **original bytecode**, not a full control/data flow interpreter or actual client callback execution. The [independent reproducible ZIP checker](../../scripts/research/phase3_r2m_client_lifecycle_verify.py) was committed **after** the JUnit source CI, and its 16 key JVM path assertions were also independently checked against the downloaded original instruction TSV; the standalone script itself is **not claimed as a step** of that CI.

## 2/4 — Exact world transition call chain, distinct renderer ownership

Original `Minecraft.setLevel(ClientLevel)`: on the old-world condition, constructs NeoForge `LevelEvent$Unload` and posts it via `NeoForge.EVENT_BUS`; interacts with `DimensionTransitionScreenManager` for received-world UI; writes `Minecraft.level` and calls `Minecraft.updateLevelInEngines(ClientLevel)`. The order of event construct/post vs later new Level assignment is present in the instruction sequence, but runtime event observer behavior is not exercised.

Actual `Minecraft.updateLevelInEngines(ClientLevel)` calls **three independent world-binding methods**:
- `LevelRenderer.setLevel(ClientLevel)`;
- `ParticleEngine.setLevel(ClientLevel)`;
- `BlockEntityRenderDispatcher.setLevel(Level)`.

These are different owner/coordinate semantics: a chunk mesh compiler does not own all particle or BER geometry. The original `Minecraft.disconnect(Screen,boolean)` is a **separate teardown route**, not necessarily identical to `setLevel`: selected bytecode calls `Minecraft.dropAllTasks`, `ClientPacketListener.close`, posts `LevelEvent$Unload` on applicable old-world path, clears `Minecraft.level` and invokes `updateLevelInEngines(null)`. Original `ClientPacketListener.close` calls `clearLevel`; `clearLevel` resets its own fields (`level`, `levelLoadStatusManager`), not the renderer's section queues. Real user gameplay connections and side effects require applied runtime test.

Original `LevelRenderer.setLevel` stores its source `ClientLevel` and updates `EntityRenderDispatcher`; on relevant branches it invokes `allChanged`, `ViewArea.releaseAllBuffers`, `SectionRenderDispatcher.dispose`, clears `globalBlockEntities` and `visibleSections` and calls `SectionOcclusionGraph.waitAndReset`. Original `allChanged` can construct or rebind `SectionRenderDispatcher(ClientLevel,LevelRenderer,Executor,...)`, invokes `ClientLevel.clearTintCaches`, `SectionRenderDispatcher.blockUntilClear` when appropriate, updates `ViewArea` and resets graph. This proves *existence and lexical call order* per method—not that every branch is taken identically on every server disconnect, render distance change or dimension swap. Original `LevelRenderer.close` handles post-processing chains separately and must not be substituted for world-transition disposal.

## 3/4 — Dispatcher closure is not a proof of all asynchronous work being quiescent

Exact JVM method-body evidence:

- `SectionRenderDispatcher.<init>` stores its **own `level:ClientLevel`**, builds high-priority `PriorityBlockingQueue`, low-priority queue, uploads queue and `ProcessorMailbox`, and captures an `executor`. Region construction uses this dispatcher source; it does not dynamically query the active `Minecraft.level`.
- `schedule` checks dispatcher `closed` and sends the task to the mailbox; `runTask` checks `closed`/available buffer pool, takes `pollTask`, submits a task via **`CompletableFuture.supplyAsync(...,executor)`**, then `thenCompose` and `whenComplete` for follow-up and buffer-release/requeue paths. This proves asynchronous execution and deferred completion *mechanisms*, **not** that a completed task can write stale results after a world switch.
- `clearBatchQueue` drains **high and low pending compile queues**, calling the abstract `CompileTask.cancel` on polled tasks. It does not show `Future.cancel(true)`/thread interruption on already-running `supplyAsync` work. `blockUntilClear` in this exact target classfile **just invokes `clearBatchQueue`**; name must not be treated as a full executor drain/barrier.
- `dispose` sets `closed=true`, calls `clearBatchQueue` and **`uploadAllPendingUploads`**; the latter polls `toUpload` and executes `Runnable.run()` for queued uploads. Therefore disposal *processes pending uploads* as well as canceling waiting compilation, rather than simply clearing all GPU work; any actual stale buffer published after a world switch remains unproven.
- `RenderSection.cancelTasks` calls `RebuildTask.cancel` and `ResortTransparencyTask.cancel` and removes stored references. `RebuildTask.cancel` uses **`AtomicBoolean.compareAndSet`** on `isCancelled` and may mark the section dirty; this is a *cooperative cancellation flag*, not proof that a currently executing task was interrupted. `ViewArea.releaseAllBuffers` calls `RenderSection.releaseBuffers` on each section; `reset` cancels tasks and resets compiled `AtomicReference` to `UNCOMPILED`.
- `ClientLevel.<init>` creates a per-world `ModelDataManager`. The preceding verified [3R-2l](PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md) proves that manager captures an owning thread+Level and that `snapshotSectionRegion`/`requestRefresh` enforce their own thread and BE Level relations. Per-task `RenderChunkRegion` gets a **snapshot bound to the originating Level**, not a view automatically rebound to a newer `Minecraft.level`.

The [40-owner/lifecycle/bypass matrix](PHASE3_R2M_CLIENT_WORLD_TRANSITION_DISPATCHER_LIFECYCLE_MATRIX_1_21_1.tsv) separates direct original JVM proofs, existing Planet source risks and released Create/AE2 source-only. No reliable happens-before or reproduction of an actual race can be derived from this bounded selected bytecode scan.

### Planetary frame conflict and mod bypasses (source-level risk only)

Current [`PlanetClientRenderFrame`](../../src/main/java/dev/planetary/client/render/PlanetClientRenderFrame.java) falls back to **`Minecraft.getInstance().level`** for *any* non-Level `BlockGetter`. A `RenderChunkRegion` is **such a non-Level `BlockAndTintGetter`**, while carrying its genuine source `level:Level` and its own `modelDataSnapshot`. A worker using the region from an earlier client world and the ambient current `Minecraft.level` could be assigned a different planet chart: this is a valid **source-level alias possibility**, **not** a reproduced gameplay race. `PlanetBlockShapeRuntime.finishPhysical` meanwhile returns canonical VoxelShape for non-Level getters, so frame assignment differs by subsystem.

`PlanetBlockRenderCulling` caches a Boolean face-occlusion result on `(sourceState,neighborState,sourceLocalSide,targetLocalSide)` only, omitting world, position, BE, revision and snapshot epoch. The real previous `3R-2j` test proved moving piston **collision shape** dynamic across BE changes; it did **not** prove dynamic **face-occlusion** stale-gameplay regression. Keep these evidence grades separate.

Released compatible 1.21.1 source/binary signatures from 3R-2h/2k show AE2 19.2.18 `FacadeBlockAccess` as a non-Level wrapping `BlockAndTintGetter` with `this.pos == pos` reference-sensitive facade substitution, AE2 `CableBusBakedModel` retaining the original getter inside `FacadeModelData` passed into quad generation, Create6.0.10 `VirtualRenderWorld` as an independent fake `Level` with its own BlockStates/BEs/ModelData and `ContraptionWorld` with own contraption-space blocks; Ponder1.0.82 wrappers delegate through another Level. **None** was loaded in Planet's running client; Create binary compiled for NeoForge 21.1.219 vs project 21.1.215, loader compatibility must be verified rather than assumed.

## 4/4 — Status, hard research gate, next bounded unit

**Stage `3R-2m` research 4/4 closed in exact source/ABI scope**: one selected original JUnit test with two successful source versions, expanded 16-owner 88-method original JVM trace, independent downloaded SHA verification, 40 annotated owner/consumer/acceptance routes and full source/phase handoff. Failed intermediate assertion due duplicate `Set.of` literal recorded, fixed, rerun GREEN. No production code or Mixins touched. Original CI proofs are **not** applied-mixin world-change correctness; model-data objects' deep immutability, exact old/new in-flight task publication order, optional thread ownership of mod implementations, actual client disconnect/rejoin and all loaded-mod behaviors are still pending.

**Phase 3A gate remains OPEN** for actual producer-authorized frame resolution, dynamic mod context, face occlusion, 6 sides/12 seams/8 corners/noPlanet. **Whole 3B–3E source/NeoForge/loaded-mod audits and worker/BER/particles regression gates remain OPEN.** The earlier 2.4A-10 renderer issue and provisional 3C-1 gameplay are still not accepted. No leap into runtime renderer repair before full phase research.

**NEXT `3R-2n` — research-only**: original `SectionOcclusionGraph.waitAndReset` and `RenderSection.RebuildTask` late-result/upload/cancellation callbacks, owner/provenance of GPU buffer publishing, and explicit client-world epoch acceptance contract before choosing any production adapter. Confirm loaded compatible Create/AE2 client, vanilla +Y/noPlanet, all six faces/12 seams/8 corners only at correct gate.
