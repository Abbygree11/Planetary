# Planetary — `3R-2f`: compiled BlockState shape consumers, NeoForge support/occlusion and mod version provenance

**2026-10-25 · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · branch `2.0`**
**Stage: research-only, bounded owner-family audit 4/4.** No production Java, Mixins, world content, test lab V8 or real gameplay changed. **Full Phase3 and Phase3A remain OPEN**; do not start runtime renderer/cache fix based on these selected original consumers alone.

## 1/4 — The actual compiled original caller census

Committed original-target research JUnit [`Phase3R2fRegisteredShapeConsumerAsmResearchTest`](../../src/test/java/dev/planetary/world/Phase3R2fRegisteredShapeConsumerAsmResearchTest.java) on exact code [`46068c88`](https://github.com/Abbygree11/Planetary/commit/46068c88cea25e9cdf4425e696d9f9d921ebdbae). It bootstraps the real NeoForge21.1.215 `BuiltInRegistries.BLOCK`, checks original 1060 IDs and 241 concrete Java classes, builds their distinct **269-class inherited ancestry**, adds selected vanilla/NeoForge support, collision, context, light and Planet renderer roots, then ASM-scans **281 classes** for a fixed method-name list. The JUnit writes **exact compiled caller owner/name/descriptor → callee owner/name/descriptor/opcode**, sorted deterministically. No caller is inferred from GitHub search index alone.

**CI [#40210234121](https://github.com/Abbygree11/Planetary/actions/runs/40210234121) COMPLETED SUCCESS** at the exact JUnit code commit. GitHub Actions archive **12605276482** `phase2-neo1211-registry-census` contains:
- `phase3-neo1211-registered-shape-consumer-edges.tsv`: **276 selected invocation edges**, SHA-256 `8275d49a24166928a7f13fbedf58eb4320afbf4ee9f826a42dcbf12a27f67e96`;
- `phase3-neo1211-registered-shape-consumer-summary.txt`: SHA-256 `c57404d4722f1bebfcd0c589139c09c2951d94d8573ebfe67ea933c2666516ce`;
- full artifact ZIP SHA-256 `aaa08f714dc09ab628606b877fee7f146b42629d26e387fea103a1f5a88eaf53`.

The archive was re-downloaded and parsed. **95 unique scanned owner classes** contain selected invokes; **59 distinct target method owner/name/descriptors**. Importantly, `getBlockEntity` is included as a deliberately **broad provenance selector** (104 calls) and may come from entirely unrelated BlockEntity operations. There are **172 other selected calls**, which also include general predicates, not necessarily rendered shapes. Neither 276 nor 172 means "all real geometry callsites in the entire engine": this is **all original registered block ancestry + an explicit limited set of engine roots**, **not** all unregistered engine/entity/client/foreign mod classes.

| Selected original target method name | ASM invoke edges | Example compiled caller/semantics |
| --- | ---: | --- |
| `isFaceSturdy` | 57 | `AmethystClusterBlock`, `DoorBlock`, `FenceBlock`, `BellBlock`, `CeilingHangingSignBlock`, original block support predicates |
| `getCollisionShape` | 26 | `BlockCollisions.computeNext`, `WallBlock.updateShape`, `MultifaceBlock.canAttachTo`, support/visual defaults |
| `getShape` | 17 | `BlockBehaviour` nested defaults and independent registered shape owners |
| `joinIsNotEmpty` | 11 | Various physical-shape unions/culling paths |
| `getFaceOcclusionShape` | 4 | `Block.shouldRenderFace` twice, `PlanetBlockRenderCulling.shouldRenderFace` twice |
| `getBlockSupportShape` | 2 | `MultifaceBlock.canAttachTo`, `BlockStateBase` delegate to Block |
| `isSupporting` | 2 | `BlockStateBase.isFaceSturdy`, `BlockStateBase.Cache` initializer |
| `getInteractionShape` | 2 | `BlockGetter.clipWithInteractionOverride`, `BlockStateBase` delegate |

Source IDs, callers, reliability and remaining verification are frozen in [32-row source/compiled/mod owner matrix](PHASE3_R2F_SHAPE_CONSUMER_OWNER_EVIDENCE_1_21_1.tsv). Full exact selected method edges live in the versioned CI artifact and **are reproducible** by running the committed research test; the 32-row file is a curated navigable index, not an incomplete copy mislabelled as the full 276-row result.

## 2/4 — Declaring-owner/caller contracts, frames and caches

The six original VoxelShape declaring method owner census for all 241 classes remains proven by [3R-2c](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.tsv); 3R-2e establishes all original **26684 legal BlockState variants**, of which 26505 are genuinely cached and 179 uncached in 22 IDs. This packet complements those with exact selected JVM **call edges**.

| Exact consumer | Original / current dependency | Correct semantic boundary |
| --- | --- | --- |
| `Block.shouldRenderFace` | Two shape-face queries, plus NeoForge `hidesNeighborFace` and `supportsExternalFaceHiding` | Preserve original mod hooks and **source/neighbor separate** canonical sides; neighbor cell positions remain PHYSICAL |
| `PlanetBlockRenderCulling.shouldRenderFace` | Mirrors source and target face calls but keys `OCCLUSION_CACHE` only by source/target `BlockState` + local directions | Static state-only cache is valid only if output invariant over world/pos/BE/time; mod-dynamic shapes can violate; **design risk source verified, game not reproduced** |
| `MultifaceBlock.canAttachTo` | Distinct `getBlockSupportShape` and `getCollisionShape` calls; their effective transformations may differ | Source/target canonical support semantics vs physical outer collision must be explicitly resolved independently |
| `BlockStateBase` | `getFaceOcclusionShape` static cached canonical `Direction.ordinal` or dynamic `getOcclusionShape`; `isFaceSturdy` uses cached bits or `SupportType.isSupporting` | Never rewrite a global cached `BlockState` based on one Planet face |
| `BlockGetter.clipWithInteractionOverride` | Independent `getInteractionShape`, physical ray | Do not mutate physical hit position or substitute collision for interaction |
| `BlockCollisions`, `WallBlock.updateShape`, `EntityCollisionContext` | Contextual collision; `World.Y` special owner for floor/descend; wall checks above collision DOWN | Local up and actual physical UP are distinct, phase7 actor-world collision needs its own contract |
| `ScaffoldingBlock`, `ShulkerBoxBlock`, `MovingPistonBlock` | Context-aware shape, live BE support, swept piston collision | State-only cache is not valid for untracked per-position outcomes; must test actual dynamic lifecycle after implementation |
| `PlanetClientRenderFrame` | Direct `Level` if present, else fallback to `Minecraft.getInstance().level` | An arbitrary foreign `BlockGetter` may not represent active world's physical blocks; **fallback provenance hazard** still untested |

No global coordinate-primitive rewriting. Nested shape calls may exist (`BlockBehaviour.getBlockSupportShape → getCollisionShape`; `BlockBehaviour.getOcclusionShape → getShape`), so a proposed adaptation must not rotate the return of every shape call independently. `BlockGetter` and `Level` can represent virtual or preview data; *type inheritance is not sufficient evidence of world coordinates*.

## 3/4 — Version-checked Create/AE2/NeoForge extension and independent bypass

Source hashes and **actual version manifests** now pinned, not merely names:

| Source comparator | Manifest check | Actual evidence and limits |
| --- | --- | --- |
| [Create@7ff2760](https://github.com/Creators-of-Create/Create/tree/7ff2760e4924d35d4bc2e22c4b19923f0178cd16) | `gradle.properties`: **Create 6.0.11; MC 1.21.1; NeoForge 21.1.219** | Comparable MC branch but builds against newer NeoForge than Planet's 21.1.215; `ConnectedGlassBlock` and subclass `WindowBlock` override `skipRendering`. `VirtualRenderWorld` **extends Level but owns independent maps for BlockState/BE/ModelData**; `ContraptionWorld` **extends Catnip WrappedLevel** and gets blocks from contraption. Catnip actual ABI/coordinate transform **UNVERIFIED**. |
| [AE2@db17504](https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/db17504a86128fdf3dae31f5fb7a112a646e0b93) | `gradle.properties`: **MC1.21.1; NeoForge 21.1.169; version range [21.1.169,)** | Range nominally includes Planet 21.1.215, **not an applied JAR proof**. `QuartzGlassBlock` declares independent `skipRendering`. Exact MC1.21.1 connected-texture BakedModel/BER owner still to inventory, not inferred from newer branches. |
| [AE2@b141e91](https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/b141e91764897d3844c9debd4210396b5f9b5238) | `gradle.properties`: **MC26.3, NeoForge26.3.0.43-beta, Java25** | Earlier research cited `QuartzGlassModel`/`CertusQuartzClusterBlock` from this SHA. **VERSION-INCOMPATIBLE comparator** with MC1.21.1 target; exclude newer `DynamicBlockStateModel.collectParts` as evidence for 1.21.1 and find correct old render author later. |
| NeoForge original 21.1.215 | Exact classpath via [3R-2f JUnit CI](https://github.com/Abbygree11/Planetary/actions/runs/40210234121), previous 3R-2d and C11 | Actual `Block.shouldRenderFace` external face hook and shape/support callers pinned by ASM; **later comparative patches are NOT exact source commits by assumption**. |
| Planet at `2.0` | `PlanetGravityRuntime` uses **identity-bound `Level`**; `PlanetBlockShapeRuntime.finishPhysical` checks `instanceof Level`; `PlanetClientRenderFrame` falls back for non-Level getters | Virtual `Level` not automatically bound to Planet; non-Level preview region can inherit wrong current-client frame. Only code-level risk, no actual mod regression claimed. |

The correct long-term approach is **frame provenance / producer authorization** at a stable renderer/BlockGetter boundary, with mod-specific compatibility adapters where necessary, not a global `Level` or `BlockGetter` behavior rewrite. Rendering and culling adapters in phase 3B must still respect NeoForge custom BakedModel/ModelData/RenderType and alternative Create instanced/Flywheel pipelines. The complete mod overrides/loaded compatible artifacts are not yet audited.

## 4/4 — Reconciled evidence, test boundaries and strict Phase-3A research gate

**Completed exactly this bounded checkpoint:** original registered block class+ancestor support/shape caller ASM pass, selected engine roots, 32-row source/mod matrix with provenance and version checks, full 276-edge generated CI artifact, save source and status. Prior six shape methods exact original owners and 26684 BlockState cache census remain PASS.

**Not completed:**
- The **whole engine** callsite closure: independent unregistered (e.g., item/entity/AI/client/non-Level wrapper/other NeoForge) consumers beyond selected roots need a categorized owner census. The 281 scanned roots do NOT prove absence of other consumers.
- NeoForge owner/extension coverage for arbitrary loaded 1.21.1 mod bytecode and custom occlusion/model data, including Catnip `WrappedLevel` and 1.21.1 AE2 connected texture model.
- Source/position-based cache invalidation in `PlanetBlockRenderCulling`, dimension/reload transitions, and real moving/virtual worlds. Current report records risks and detailed acceptance, **not a runtime fix**.
- **Actual applied Mixins**, real client/server, six faces, 12 edges, eight corners, +Y/no-Planet, loaded Create/AE2 and shaders all PENDING. Existing V8 remains unchanged.
- Full Phase3B/3C/3D/3E owner census; **whole-phase research gate cannot pass**.

**Next named research-only packet `3R-2g`:** classify additional *non-block* original/NeoForge shape consumers (entity/world/chunk, gameplay collision, neighbor updates, model/chunk rebuild) beyond 281 roots with exact compiled signatures; resolve pinned 1.21.1 Catnip `WrappedLevel` and AE2 model original sources/JAR coordinate provenance, enumerate true custom mod shape/culling dispatch paths. Freeze an explicit 3A remaining-gap disposition and phase3A gate only with evidence; otherwise subdivide research, but **do not start implementation or stage 3B prematurely**. Keep user-visible CI / applied client / game / mod distinctions and update every handoff, route matrix, roadmap.
