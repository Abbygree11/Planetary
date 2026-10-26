# Planetary — `3R-2i`: support strategy, moving-piston collision, CollisionGetter and NeoForge face hooks

**2026-10-26 · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · branch `2.0`. RESEARCH ONLY — complete bounded packet 4/4, original ASM CI SUCCESS.** No production Java, Mixins, world data, V8 lab or `main` changes. **3A and full Phase3 3A–3E research gates remain OPEN.**

## 1/4 — Original NeoForge 21.1.215 compiled owner and method-invocation proofs

Previous [3R-2h](PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md) proved **144** selected original NeoForge method-name invocation candidates but its 3A bucket still left class-subtype/overload methods unresolved. To improve this, committed [research-only original ASM/JUnit `Phase3R2iContextualSupportPistonAndExtensionAsmTest`](../../src/test/java/dev/planetary/world/Phase3R2iContextualSupportPistonAndExtensionAsmTest.java).

The test reads **18 exact target compiled classfiles** from the real NeoForge 21.1.215 JUnit classpath. It writes deterministic owner/supertype/interface (`context-owner-inheritance.tsv`), all owner method declarations and access flags (`context-owner-methods.tsv`), selected exact source caller/name/JVM descriptor → target owner/name/JVM descriptor/opcode (`context-owner-invokes.tsv`) and summary under `build/reports/planetary`. It asserts all three FULL/CENTER/RIGID enum implementations, their distinct calls, moving piston collision paths and NeoForge `IBlockStateExtension` hook forwards.

These were the previously identified **original version source bytecode** signatures (from CI 3R-2g/3R-2h), not inferred from GitHub search matches:

| Declaring source | Direct original JVM callsites |
| --- | --- |
| `SupportType$1.isSupporting` | `BlockState.getBlockSupportShape`, `Block.isFaceFull` |
| `SupportType$2.isSupporting` | `BlockState.getBlockSupportShape`, `VoxelShape.getFaceShape`, `Shapes.joinIsNotEmpty` |
| `SupportType$3.isSupporting` | `BlockState.getBlockSupportShape`, `VoxelShape.getFaceShape`, `Shapes.joinIsNotEmpty` |
| `PistonMovingBlockEntity.getCollisionShape` | Two `BlockState.getCollisionShape` callsites |
| `PistonMovingBlockEntity.moveCollidedEntities` | `BlockState.getCollisionShape` |
| `PistonMovingBlockEntity.moveStuckEntities` | `BlockState.getCollisionShape` |
| `CollisionGetter.isUnobstructed` | `BlockState.getCollisionShape(BlockGetter, BlockPos, CollisionContext)` |
| `IBlockStateExtension.hidesNeighborFace` | `Block.hidesNeighborFace` |
| `IBlockStateExtension.supportsExternalFaceHiding` | `Block.supportsExternalFaceHiding` |

**Scope limitation:** static caller → invoked declared receiver owner is not necessarily the concrete runtime receiver or mod subclass. `INVOKEVIRTUAL` and `INVOKEINTERFACE` need object type and level-context provenance; the new test does not instrument applied Mixins, active gameplay, all mods, general reflection or classloading.


**Original JVM CI verified (two independent research tests):** [#40321142149](https://github.com/Abbygree11/Planetary/actions/runs/40321142149) **SUCCESS** on [`9d00814`](https://github.com/Abbygree11/Planetary/commit/9d00814ca81007d71bb1582117da3c9759de9bf1). GitHub artifact **12648888735**, re-downloaded ZIP SHA256 `169f7df3fe731365c1f7eff78d33b5bda03e849bb1a52887c8a083938db231ae`. Exact **18 classfiles / 432 declared methods / 90 selected method INVOKEs**, including **8 support strategy invokes** and **4 PistonMovingBlockEntity shape invokes**. Raw selected call edge TSV SHA256 `7291983b75dfd0a7080969a10f60a1c274cf40a5d748038fa706c35518862b00`; all methods, parent classes and signature/opcodes in archive.

**New deeper method-body ASM:** [`Phase3R2iOriginalContextMethodBodyInstructionTraceTest`](../../src/test/java/dev/planetary/world/Phase3R2iOriginalContextMethodBodyInstructionTraceTest.java), [CI #40321810530](https://github.com/Abbygree11/Planetary/actions/runs/40321810530) **SUCCESS** on [`381248e`](https://github.com/Abbygree11/Planetary/commit/381248ef64de9c2f8c175a6741c63aeee872ecb6). Re-downloaded 21-file artifact **12648604323**, ZIP SHA256 `989bfdc633975a9f83edad9a81d8e6de256475a5c5dd0d8db72fe0676aac8369`; **10 selected original method bodies / 6 classfiles / 498 ordered bytecode instructions**. The detailed method-body TSV SHA256 `8d6c50f518d1f59da9e5afe7515c7139bb199fd1ef82d008af5e100346e15ff1` and six-class hash TSV SHA256 `f0eccf936c7eca69b77ef53fa8f99039e6ff9bb078f3a744a98ae27f0fb91802`. Files are reproducible from committed JUnit. Method-body proof still **not** proof of runtime mod dispatch.

## 2/4 — Semantic and coordinates disposition: FULL, CENTER, RIGID are not one generic rule

The original FULL strategy queries support geometry then performs whole-face fullness; CENTER and RIGID call shape-face projection and overlap with their own support templates. Their *different chosen shape masks* and semantically distinct predicate expectations need exact constant/body checks before proclaiming equivalent transformations. The strategy evaluation is downstream of the original `BlockStateBase.isFaceSturdy` cache/dynamic branch. The static shape-support cache is built in a canonical `EmptyBlockGetter.INSTANCE, BlockPos.ZERO` world context and is **not** an automatically valid cache for position-dependent mod shapes.

Do **not** transform the global `Shapes.block()` singleton or reinterpret vanilla's physical world frame. A block edge query must retain the **source block's local side** and **target support block's local side**, the physically adjacent `BlockPos`, and each BlockState's query context. This is not the same as surface rendering or a globally rotated physical `Direction`.

**Exact original template/branch semantics now verified:** FULL's 7 instructions directly return `Block.isFaceFull(getBlockSupportShape(...), side)`. CENTER's 15 instructions compute `getBlockSupportShape(...).getFaceShape(side)`, read `SupportType$2.CENTER_SUPPORT_SHAPE` and `BooleanOp.ONLY_SECOND`, and call `Shapes.joinIsNotEmpty`. RIGID's 15 instructions use the distinct `SupportType$3.RIGID_SUPPORT_SHAPE` with the same only-second comparison. Bytecode branches invert uncovered-required-shape result: `isSupporting` is true only if the required face template is fully covered. CENTER and RIGID templates are *different*, not a single broad isFaceSturdy check; this claim is original compiled method-body evidence, not name inference.

**Moving original BE instruction paths:** `getCollisionShape` reads `extending`, `isSourcePiston`, `movedState`, `progress` and ThreadLocal `NOCLIP`; uses `Direction.getStepX/Y/Z`, `getExtendedProgress`, `VoxelShape.move` and `Shapes.or`. `moveCollidedEntities` uses `PistonMath.getMovementArea`, actual entity AABBs, push reactions, `getDeltaMovement`, `setDeltaMovement` and `moveEntityByPiston`. `moveStuckEntities` uses `Direction.Axis.isHorizontal` and physical `Direction.Axis.Y`, `VoxelShape.max(Axis.Y)`, progress, an `invokedynamic` Predicate and `moveEntityByPiston`. This is the exact original path split between block shape and actor movement (Phase7), not evidence of an already fixed local-gravity piston.

`CollisionGetter.isUnobstructed` explicitly passes the shape source getter/position/`CollisionContext`; its `noCollision` branch also consumes boolean shape intersections and a global world-border obstacle. `WorldBorder`/`Entity` uses physical coordinates and must remain outside wholesale block-local transforms. These paths intersect Phase7 entity physics; 3A must document its producer outputs without attempting to implement actor gravity.

`PistonMovingBlockEntity` has context-/progress-/BlockEntity-dependent collision and movement owners, which cannot be reduced to a state-only `VoxelShape`. Same BlockState shape pair does not prove identical moving geometry across tick/position/BE contexts. At a seam, source moving block geometry and actor displacement must be handled by *different owner contracts*. No phase-7 changes are included here.

## 3/4 — NeoForge hook / mod/fake-world boundary and regression-contract classification

[31-row owner/bypass/acceptance matrix](PHASE3_R2I_SUPPORT_MOTION_EXTENSION_OWNER_MATRIX_1_21_1.tsv) records exact direct invocation families and independent fallback/callback contracts.

`IBlockStateExtension.hidesNeighborFace` and `supportsExternalFaceHiding` forward to polymorphic `Block` methods. AE2 QuartzGlass and Create ConnectedGlass/Window block overrides remain responsible for their own culling decisions; it would be incorrect to claim full compatibility from registered vanilla 241-class ancestry. The renderer must map source/target local sides at edges separately, preserving vanilla and NeoForge hook dispatch, `ModelData` and custom baked-quad paths. The actual mod renderer owner belongs also to later 3B research.

The pinned 1.21.1 Create/Ponder and AE2 JARs at 3R-2h prove **compiled type declarations**, not that their wrapper world coordinates match the active bound Planet `Level`. Ponder `WrappedLevel` delegates to an underlying `Level`; Create `ContraptionWorld` has contraption-local blocks; `VirtualRenderWorld` stores independent world data; AE2 `FacadeBlockAccess` is a non-`Level` `BlockAndTintGetter`. `PlanetClientRenderFrame` currently falling back to active Minecraft client Level for non-Level getters and `PlanetBlockRenderCulling` state-pair-only key without world/position/time/BE remain source-level **unreproduced** risks, not gameplay failures.

Accept only separately verified cases: six planet faces, 12 chart edges, 8 corners, no-Planet and vanilla +Y control; original SupportType FULL/CENTER/RIGID support and attached blocks; moving piston during extension/retraction with nonempty BlockEntity shape; Create moving contraptions and independent preview/virtual Level; AE2 facades + connected glass and custom models, both JVM client and dedicated server where applicable. Correctness depends on explicit shape producer ownership and invalidation, not blanket Mixins.

## 4/4 — CI / evidence gate / handoff

The [target original compiled JUnit](../../src/test/java/dev/planetary/world/Phase3R2iContextualSupportPistonAndExtensionAsmTest.java) and [method-body instruction trace JUnit](../../src/test/java/dev/planetary/world/Phase3R2iOriginalContextMethodBodyInstructionTraceTest.java) are included in Java21 `gradle test` and the existing `phase2-neo1211-registry-census` artifact. **Both independent CI runs completed SUCCESS**, and their exact original ZIPs were re-downloaded and inspected. Both tests are research-only; the successful bytecode-level gate is not mod-loaded/client/visual acceptance.

**3A gate remains OPEN** even after a green selected-owner JUnit: further exact dynamic dispatch and position-context checks for original support and moving piston branches; a rigorous per-BlockGetter world/frame provenance policy; modded cache-time/BE invalidation; real loaded NeoForge-compatible Create/AE2 version tests; applied Mixins, six-face/seam/corner and no-Planet game acceptance all outstanding. This does not substitute for 3B/3C/3D/3E complete deep source/ABI audits.

**Next packet — `3R-2j` RESEARCH ONLY** if the new source/ABI check confirms the above gates remain: audit original virtual-dispatch receivers and dynamic context / cache invalidation on exact APIs, release-compatible loaded mod behavior, and freeze source-of-world provenance. No new renderer runtime fix before the full Phase3 research gate.
