# Planetary — `3R-2c`: exact NeoForge 21.1.215 SIX-shape owner audit (241 registered block classes)

**2026-10-25 · `2.0` · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21**

**Outcome: complete exact original JVM declaring-owner census for SIX specific vanilla Block VoxelShape API signatures, 241/241 registered concrete classes and 1060/1060 IDs. Stage research 4/4 complete.** The 3A dynamic/overload/Cache/NeoForge-mod caller inventory and 3B–3E phase-wide review remain **OPEN**. No production/Mixin/lab behavior changed; the new Java is a **research-only JUnit test** producing an immutable GitHub CI artifact.

## 1/4 — Source owners, exact signatures and original NeoForge JVM proof

[Original Phase2 reflection ZIP #37988064055 / artifact 11643813158](https://github.com/Abbygree11/Planetary/actions/runs/37988064055) had only the first two original declaring-owner signatures (`getShape`, `getCollisionShape`). Source-index candidates for the other four came from pinned Mojang-mapped comparator `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`. Previous `3R-2b` correctly labelled those four as **SOURCE CANDIDATE**.

New [`Phase3R2cOriginalShapeOwnerCensusTest`](../../src/test/java/dev/planetary/world/Phase3R2cOriginalShapeOwnerCensusTest.java) calls `Bootstrap.bootStrap()`, traverses actual `BuiltInRegistries.BLOCK`, inspects `getDeclaredMethods` up the real Java `Class#getSuperclass` chain, excludes synthetic/bridge methods, verifies **exact argument/return types**, requires **241 concrete classes / 1060 IDs**, emits all four original nearest declaring owner FQNs and class hierarchy as `build/reports/planetary/phase3-neo1211-four-shape-effective-owners.tsv` (CI artifact, not fabricated output).

[CI #40136919659](https://github.com/Abbygree11/Planetary/actions/runs/40136919659) **SUCCESS** on exact source commit [`1740d9b`](https://github.com/Abbygree11/Planetary/commit/1740d9bc076832fd980c116b8aca45ee3a913117). Published `phase2-neo1211-registry-census` archive ID **12577831169**, also containing the NEW `phase3-neo1211-four-shape-effective-owners.tsv`. Original first ZIP archive ID **11643813158** remains distinct.

Exact selected Block **declaring method descriptors**, verified by this JVM-reflection test:

| Method | Return | Actual JVM parameter types |
| --- | --- | --- |
| `getShape` | `VoxelShape` | `BlockState, BlockGetter, BlockPos, CollisionContext` (earlier original reflection) |
| `getCollisionShape` | `VoxelShape` | same four parameters (earlier original reflection) |
| `getVisualShape` | `VoxelShape` | `BlockState, BlockGetter, BlockPos, CollisionContext` |
| `getInteractionShape` | `VoxelShape` | `BlockState, BlockGetter, BlockPos` |
| `getOcclusionShape` | `VoxelShape` | `BlockState, BlockGetter, BlockPos` |
| `getBlockSupportShape` | `VoxelShape` | `BlockState, BlockGetter, BlockPos` |

The **first two** owner columns were restored from the independent original Phase2 archive; the **last four** from exact original CI #40136919659. [Final SIX-owner TSV atlas](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.tsv) keeps explicit per-row evidence types. [Dual-archive independent verifier](../../scripts/research/phase3_shape_six_owner_verify.py) verifies both originals, original first-two census, the exact other-four signatures and all 241 FQN/count/owner rows. It is NOT a generic NeoForge shape engine or gameplay test.

## 2/4 — Fully verified original inherited family atlas

| Four NEW verified methods | Distinct effective declaring Java classes | Registered classes whose effective owner is `BlockBehaviour` |
| --- | ---: | ---: |
| `getVisualShape` | **8** | **229 / 241** |
| `getInteractionShape` | **5** | **235 / 241** |
| `getOcclusionShape` | **8** | **232 / 241** |
| `getBlockSupportShape` | **10** | **230 / 241** |

Source-index candidate atlas `3R-2b` was verified **row-by-row 241/241** against the new original JVM reflection (class FQN/count/hierarchy and all four owners). Reconciliation proof: canonical normalized record `FQN|count|hierarchy|visualOwner|interactionOwner|occlusionOwner|supportOwner\n`, sorted by case-sensitive full Java FQN; **new original TSV SHA-256** `6b789f089b97cdb20c9b11ebc6d2eeca80e4f73076673d807cb29651946524fa`, **canonical 241-row SHA-256** `5c3d8692963daa32bc4394811a744e395801892f8bf753c5223992be956a12a9`, **independent FNV-1a** `ca50e69a59206014` **MATCH**. First two `3R-2b` hashes remain original SHA `2998ea5f3eb04745aa12cdedd7a1ea2128987e8d7c6b43da56f47cc1b1e9068b`, FNV `c04f6d13ecd62939`. These checksums are **not** a game PASS.

Confirmed original-family behavior (pinned readable vanilla source; compiled declaring owner proven separately):

- [`BlockBehaviour`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java): base `getVisualShape` invokes collision; `getInteractionShape` returns `Shapes.empty()`; `getOcclusionShape` invokes state's `getShape`; `getBlockSupportShape` invokes `getCollisionShape(..., CollisionContext.empty())`. These **nested calls** forbid naïve per-API unconditional wrapping (double rotate).
- [`TransparentBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TransparentBlock.java) / [`IronBarsBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java): visually empty form may differ from actual collision/interaction; inherited stains and mod glasses do not become full cubes simply because the subclass has no direct VoxelShape method.
- [`AbstractCauldronBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AbstractCauldronBlock.java), [`ComposterBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComposterBlock.java), [`HopperBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/HopperBlock.java) and [`ScaffoldingBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ScaffoldingBlock.java): independent selection/raycast overrides. Scaffolding's `getShape` and `getCollisionShape` also depend on `CollisionContext` (held item, above/descending state) — output is not a static material AABB.
- [`SkullBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SkullBlock.java), [`FenceGateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java) and [`LecternBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LecternBlock.java): explicit `getOcclusionShape` controls visual face masking, not interchangeable with `getCollisionShape`.
- [`ShulkerBoxBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ShulkerBoxBlock.java): `getBlockSupportShape` depends on live `BlockEntity.isClosed()` and the opened direction; [`CeilingHangingSignBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CeilingHangingSignBlock.java) delegates its support shape to `getShape`. These must preserve caller context and safe cache provenance.

## 3/4 — Original callsites/overloads, NeoForge and mod bypass

Previous exact NeoForge 21.1.215 [`Phase23C11ShapesOcclusionCullingAsmContractTest`](../../src/test/java/dev/planetary/world/Phase23C11ShapesOcclusionCullingAsmContractTest.java) [CI #38289949215](https://github.com/Abbygree11/Planetary/actions/runs/38289949215) verifies the **BlockStateBase** overloads `getShape(BlockGetter,BlockPos)` vs contextual, collision simple vs contextual, contextual visual, simple interaction/occlusion/support, physical/typed `getFaceOcclusionShape`, support FULL/CENTER/RIGID, original `Block.shouldRenderFace` and NeoForge neighbor `hidesNeighborFace/supportsExternalFaceHiding`. This is **selected compiled owner/callsite proof**, not a full exhaustive NeoForge 3A source patch/hook census.

Source callflow separation still mandatory:
```text
BlockGetter.clip -> ClipContext OUTLINE/COLLIDER/VISUAL
  -> physical VoxelShape.clip + interaction-ray override
BlockCollisions -> state.getCollisionShape(context) -> Shapes/AABB (physical)
BlockStateBase.getFaceOcclusionShape -> cached face mask OR Shapes.getFaceShape
  -> actual source/neighbor face occlusion in Block.shouldRenderFace
BlockStateBase.isFaceSturdy -> cached flags OR SupportType FULL/CENTER/RIGID
  -> getBlockSupportShape -> maybe live BlockEntity
```
The original `BlockStateBase.Cache` is created with `EmptyBlockGetter/BlockPos.ZERO` in a *global state cache*, not world-scoped; dynamic mod shape outcomes cannot be globally memoized by a `BlockState` alone. `Shapes.block()` identity and physical ray/hit/collision positions must remain physical. **At six-face edges and corners, independently derive source and neighbor local face directions** (do not use one canonical face across both).

**NeoForge/Mod:** earlier selected comparator NeoForge patch `neoforged/NeoForge@3cae64530de368936d4c07497d3f0a25e2f9ffec` changes `Block.shouldRenderFace` to call `hidesNeighborFace` and `supportsExternalFaceHiding` (C11 already pins exact target ABI selected). Create ConnectedGlass and AE2 QuartzGlass own independent transparent culling; original pinned source comparisons in C11 are **NOT** loaded mod runtime. Flywheel/contraptions may bypass vanilla shape/BakedModel assumptions. **Not covered** by six vanilla declaring-owner class reflection and not automatically safe for every custom mod.

## Second confirmed green CI and reproducibility distinction

**Final 3R-2c source/test verification:** second full [CI #40137491107](https://github.com/Abbygree11/Planetary/actions/runs/40137491107) on [30937e1](https://github.com/Abbygree11/Planetary/commit/30937e1ea8309edc74ac80580f3c2e39ad11549a) **COMPLETED SUCCESS** after committing the 241-row atlas and dual-archive verifier. This second CI ran the existing research JUnit and normal Java21 test suite; the standalone Python verifier is committed and independently reproducible using both workflow ZIPs but is **not automatically executed by CI**. No production Java or V8 change; Phase3 research gate still OPEN. 

## 4/4 — strict acceptance, no implementation, next research gate

| Gate | Verified | Still pending |
| --- | --- | --- |
| 241 exact registered class/1060 ID source + 6 original JVM method declarers | **PASS**; both original archived workflows, independent 241-row checksums and dual verifier | None for these six signatures in original vanilla registry |
| NeoForge BlockStateBase and selected consumer INVOKEs | **SELECTED ABI PASS** via C11 original compiled classpath test | Full dynamic/overload/face cache/foreign getter hook matrix |
| Runtime test source & CI | **[CI #40136919659](https://github.com/Abbygree11/Planetary/actions/runs/40136919659) SUCCESS** for new research-only test | Does not mean applied Mixin or Minecraft gameplay pass |
| Full 3A source/override + mod bypass owner audit | **SIX exact vanilla declaring owners covered** | Live BlockEntity/CollisionContext, per-world memoization, 12 seams/8 corners, true custom mod class and render bypasses |
| Whole Phase3 (3A–3E) source/mod complete | **NOT DONE** | 3B/3C/3D/3E untouched in this stage |
| Production code and V8 lab | **UNCHANGED** | Provisional 3C-1 client/game/mod still pending |

**Next stage `3R-2d` (research only):** finish all 3A contextual/dynamic callers, `getFaceOcclusionShape` cache variants and `SupportType` external hooks, document full mod bypass and physical-vs-local edge chart boundary, make 3A final completeness decision (only if all 3A owners are inventoried); **then**, and only then, switch to `3R-3` standard BakedModel/render AO pipeline. No production Java or premature Phase3-ready claim. Original 2.4A-10 visual bug remains tracked until the complete Phase3 research gate.
