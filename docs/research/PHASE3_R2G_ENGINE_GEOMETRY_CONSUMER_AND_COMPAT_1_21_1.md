# Planetary — `3R-2g`: engine-wide discoverable JVM caller census and version-compatible virtual/model owners

**2026-10-26 · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · `2.0`**
**Research-only**. No production Java, Mixins, generated world, V8 or gameplay modifications. Full 3A and all Phase3 **OPEN**.

## 1/4 — New original NeoForge runtime census outside registered block ancestry

Added [Phase3R2gEngineShapeCallerCensusTest](../../src/test/java/dev/planetary/world/Phase3R2gEngineShapeCallerCensusTest.java). Unlike 3R-2f, it discovers `.class` entries from the *actual runtime resource locations* for Minecraft `Block` and optional NeoForge `IBlockExtension`/`IDynamicBakedModel`. It scans every discovered Minecraft/NeoForge classfile (not merely 241 registered vanilla block classes) with ASM and captures the exact caller+callee JVM method descriptor and opcode for the same explicit 26-name shape/support/culling/context selector.

The generated `build/reports/planetary/phase3-neo1211-engine-shape-consumer-edges.tsv` has a scope flag `registered_block_ancestry` versus `outside_block_ancestry` and an owner-area classification, plus `phase3-neo1211-engine-shape-consumer-summary.txt`. Both belong to the existing `phase2-neo1211-registry-census` GitHub Actions artifact on runs that complete the test.

**Important coverage semantics:** this is an exhaustive *discovered class-file/name-matched INVOKE* census within the listed resource roots, **not** a proof of all Minecraft runtime callsites, bytecode from arbitrary loaded mods, method-handle/invokedynamic/reflection, dynamic virtual dispatch, applied Mixin transformation or client gameplay. Different NeoForge classpath layouts may expose different roots; the report lists which roots were found. `getBlockEntity` is deliberately broad and must never be counted as proof that all such calls consume shape.

**CI result:** pending verification; do not present code commit alone as ASM PASS. Do not hardcode newly measured counts before the output is inspected.

## 2/4 — Exact geometry boundaries and newly discoverable owner families

Audit categories deliberately span `world.level` (collision/block access/chunk), `world.entity` (actor collision), `world.item` (placement), `client` (model/chunk/render), `server`, `world.phys`, and `net.neoforged` where physically co-located. Unlike a blanket Mixin on `BlockGetter` or `Level`, the correct contract depends on the author:

- Shape query: sender's source `BlockPos`, physical vs local side, current getter and `CollisionContext` are independent inputs. Use the deepest safe translation boundary; never double-rotate cached/global `VoxelShape`.
- Support query and neighbor culling: retain **both** block-local frames across a chart seam, original `skipRendering`/NeoForge hide hooks, and per-position data where geometry is contextual.
- Physical actor/raycast collisions: do not reinterpret every vanilla physical-Y or cached full-cube comparison as local block geometry. Entity/actor gravity is a separate Phase7 owner.
- Dynamic `BlockEntity`, model data, moving world and custom model-generated quads do not reduce to state-pair `VoxelShape` and cannot be made correct by indiscriminately rotating `getShape`.

## 3/4 — Real 1.21.1 Create/Catnip and AE2 source paths, not new API assumptions

**Create:** pinned [Create 6.0.11 @ 7ff2760](https://github.com/Creators-of-Create/Create/tree/7ff2760e4924d35d4bc2e22c4b19923f0178cd16), `minecraft_version=1.21.1`, `neo_version=21.1.219`. [`ContraptionWorld`](https://github.com/Creators-of-Create/Create/blob/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create/content/contraptions/ContraptionWorld.java) extends Catnip `WrappedLevel`, directly maps `getBlockState(pos)` through `contraption.getBlocks().get(pos)`; [`VirtualRenderWorld`](https://github.com/Creators-of-Create/Create/blob/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create/foundation/virtualWorld/VirtualRenderWorld.java) extends `Level` with separate `blockStates`, `blockEntities`, light and chunk sources. They do not inherit a Planet level identity binding merely because they extend `Level`.

**Catnip/Ponder:** located exact old-package [`net.createmod.catnip.levelWrappers.WrappedLevel` at Ponder @ 8981929](https://github.com/Creators-of-Create/Ponder/blob/89819291b726708d119b118dea01667eae0b64c9/common/src/main/java/net/createmod/catnip/levelWrappers/WrappedLevel.java). This source has `class WrappedLevel extends Level`, protected underlying `level`, `getLevel()`, delegated `getBlockState`, `getBlockEntity`, lighting, and a proxy `ChunkSource`. That pinned Ponder source declares Minecraft 1.21.1 and NeoForge 21.1.206, making it a **compatible source comparator**, not verification that this exact commit is the released Ponder 1.0.85 / Create 6.0.11 binary. The exact installed Catnip ABI is still open.

**AE2:** pinned [AE2 @ db17504](https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/db17504a86128fdf3dae31f5fb7a112a646e0b93), `minecraft_version=1.21.1`, `neoforge_version=21.1.169` with lower-bound range. The genuine **1.21.1 connected glass render owners** are [`GlassModel`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/client/render/model/GlassModel.java) and [`GlassBakedModel`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/client/render/model/GlassBakedModel.java), **not** MC26.3's `QuartzGlassModel`. `GlassBakedModel implements IDynamicBakedModel`: its `getModelData(BlockAndTintGetter,BlockPos,...)` constructs `GlassState` from neighboring appearances and its `getQuads(..., ModelData, RenderType)` conditionally suppresses sides/chooses frame textures. [`QuartzGlassBlock`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/decorative/solid/QuartzGlassBlock.java) independently implements `skipRendering`.

The old [`FacadeBlockAccess`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/client/render/cablebus/FacadeBlockAccess.java) is a non-`Level` `BlockAndTintGetter` wrapper with substituted facade state, while [`CableBusBakedModel`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/client/render/cablebus/CableBusBakedModel.java) owns a `ModelData`-dependent quad/cache path. [`CertusQuartzClusterBlock`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/decorative/solid/CertusQuartzClusterBlock.java) is also confirmed from this correct 1.21.1 version: directional shapes and support-facing `isFaceSturdy`. None of this establishes that these mod classes were loaded into a Planetary client or reproduced a rendering defect.

## 4/4 — Scope, reproducibility and non-passing 3A gate

[Companion matrix](PHASE3_R2G_ENGINE_MOD_CALLER_AND_BYPASS_1_21_1.tsv) specifies 24 individually classified routes, source provenance, context contracts, and remaining acceptance. This stage adds a reproducible **broader JVM audit** and restores correctly versioned AE2 1.21.1 glass model owners and pinned Ponder/Catnip source comparator. It **does not** claim finished whole-engine dynamic call graph or full mod compatibility.

**Still missing before freezing 3A:**
- CI evidence from the new full classpath scanner with actual scanned namespace/class/edge counts, resolved any scanner failures, and independently checked TSV summary.
- Release-equivalent Catnip/Create/AE2 compiled artifacts and genuine loaded 1.21.1 runtime paths; virtual-world and facade-origin delegation/side mapping.
- Qualified dispatch closures for context-dependent custom `VoxelShape`, `BlockEntity`, NeoForge extensions, raycast and shape cache invalidation; choose non-stale culling cache contract before implementation.
- Actual applied client, six faces + 12 chart edges + 8 corners, +Y/no-Planet baseline, resource reload and moving contraptions; 3B–3E remain independently OPEN.

**Next `3R-2h` (research only if original JVM census confirms outstanding gaps):** reconcile newly discovered engine owner rows against complete old shape consumer list, prove exact released Catnip/AE2/JAR ABI and extension bypass, then re-evaluate the 3A freeze gate. **Never jump to renderer fixes or 3R-3 before 3A and whole-phase research authorization.**