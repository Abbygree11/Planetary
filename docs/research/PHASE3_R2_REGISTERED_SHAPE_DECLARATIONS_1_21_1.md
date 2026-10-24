# Planetary — Phase3 research `3R-2`: original 241-class geometry declaration census

**2026-10-24 · branch `2.0` · Minecraft 1.21.1 · NeoForge 21.1.215 · Java21.**
**Result: REGISTER-KEYED SOURCE-SEARCH MATRIX COMPLETED (241/241 class keys; 1060/1060 registry IDs), but declaring-owner *inheritance/overload/compiled-ABI* closure remains OPEN.** No Java, Mixin, test, lab, build or gameplay change. **Next bounded packet = `3R-2b` inherited effective owners, full per-signature proof and override/bypass reconciliation.** Do not jump to 3B until 3A research gate is satisfied.

## 1/4 — Immutable registry provenance + search method

A precise census must start with original runtime classes, not an arbitrary directory listing. Baseline [`PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) contains **241 distinct original block FQNs** and **1060 original registered BLOCK IDs**; immutable NeoForge21.1.215 registry ZIP provenance artifact 11643813158 / run 37988064055 / SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` and prior reconciliation [241/1060](PHASE2_STAGE3A_SOURCE_241_CLASS_ORIGINAL_1060_BLOCK_RECONCILIATION_1_21_1.md). This packet **did not re-download or re-bootstrap** that original ZIP. It reused its published per-class count as registration evidence.

For each of the 241 keys, search against pinned *readable* source `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`, path `1.21.1/net/minecraft/world/level/block`, using GitHub code search restricted to each method signature prefix `methodName(BlockState`. The source contains OptiFine adaptations; its source matches are NOT exact compiled NeoForge21.1.215 bytecode. The research table is [`PHASE3_R2_REGISTERED_DIRECT_SHAPE_DECLARATIONS_1_21_1.tsv`](PHASE3_R2_REGISTERED_DIRECT_SHAPE_DECLARATIONS_1_21_1.tsv), **241 rows exactly**, with six shape-specific method columns and five cross-owner light/render method columns. Row `registered_block_ids` is the exact original registry count; counts of matched IDs overlap and must never be summed as unique block coverage.

**Proof levels of TSV:** `SEARCH_MATCHED_SOURCE_SIGNATURE` means code search returned a matching source file for the candidate method signature, not a complete overload-/declaring-owner disassembly. **`NOT_RETURNED_BY_CODE_SEARCH` is NOT evidence that the class lacks a method or inherited geometry**. GitHub code search reported changing total estimates for `getShape(BlockState` (131 and 124 on separate calls); an independent page query returned 100+24 with `incomplete_results=false`. We therefore conservatively keep negative rows *unverified*, and `inheritance_status=BASE_PARENT_RESOLUTION_PENDING` for all 241. No source search heuristic is promoted to immutable binary ABI.

| Exact searched prefix | Matched *registered concrete classes* | Registered IDs belonging to those matched classes |
| --- | ---: | ---: |
| `getShape(BlockState` | 110 | 509 |
| `getCollisionShape(BlockState` | 20 | 65 |
| `getVisualShape(BlockState` | 7 | 19 |
| `getInteractionShape(BlockState` | 3 | 3 |
| `getOcclusionShape(BlockState` | 7 | 32 |
| `getBlockSupportShape(BlockState` | 9 | 62 |
| `getLightBlock(BlockState` (3B link) | 2 | 9 |
| `propagatesSkylightDown(BlockState` (3B link) | 10 | 51 |
| `isSolidRender(BlockState` (3B link) | 0 search matches | 0 search matches; **NOT proof no overrides** |
| `getShadeBrightness(BlockState` (3B link) | 7 | 7 |
| `getRenderShape(BlockState` (3B/BER routing) | 36 | 75 |

The final five methods belong mostly to later rendering/lighting review; collecting their declaration search hits here prevents mistakenly treating all geometry-relevant block rendering as just `VoxelShape`. `BlockStateBase.isFaceSturdy` is **not a Block subclass declaration**; it is owned by the BlockState method/cache and delegates via `SupportType`. Special `Block#getCollisionShape` variants/overloads and NeoForge interfaces require source+ASM beyond this search prefix.

## 2/4 — Concrete inherited mechanism families verified in source

Pinned full source for eight representative 1.21.1 classes was read directly, rather than inferred from search absence:

| Concrete or abstract source chain | Direct owner findings | Consequence for inherited registered IDs |
| --- | --- | --- |
| [`CrossCollisionBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CrossCollisionBlock.java) (abstract/intermediate) → [`FenceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java) | Abstract base declares `getShape` and `getCollisionShape`; registered `FenceBlock` declares its own `getVisualShape` and `getOcclusionShape` | Fence IDs inherit cross-collision geometry; a zero on FenceBlock's direct getShape column does **not** mean a generic full block |
| `CrossCollisionBlock` → [`IronBarsBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java) → [`StainedGlassPaneBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/StainedGlassPaneBlock.java) | IronBarsBlock owns `getVisualShape`; StainedGlassPaneBlock declares none of the six queried VoxelShape methods directly | 16 stained glass pane IDs use **inherited pane/bar connection shape** rather than `BlockBehaviour` default |
| [`MultifaceBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java) → [`GlowLichenBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GlowLichenBlock.java) | MultifaceBlock directly declares `getShape`; registered GlowLichenBlock does not | Local six-face plates, light and spreading semantics require base-family owner |
| [`ShulkerBoxBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ShulkerBoxBlock.java) → BaseEntityBlock | Declares `getShape` and `getBlockSupportShape`; source references world/block entity-dependent geometry | 17 ID family; BE-driven collision/support and dynamic cache policy not certified by a state-only shape flag |
| [`ScaffoldingBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ScaffoldingBlock.java) → Block | Distinct `getShape`, `getCollisionShape`, `getInteractionShape` | Same registered ID has multiple *noninterchangeable* shape APIs; interaction/raycast vs entity collision differ |

Additional direct original representative owners identified in earlier P2/C11 and rechecked in the matrix: `DoorBlock` (16 IDs), `StairBlock` (52), `SlabBlock` (56), `WallBlock` (25), `LiquidBlock` (2), plus the 17 shulker IDs. These are **mechanism examples**, not a loaded mod compatibility test and not a claim of all 241 inherited-owner chains resolved.

**Canonical preservation boundaries:** `BlockStateBase.Cache` may store `getCollisionShape`, `getFaceOcclusionShape` and FULL/CENTER/RIGID support bits on `EmptyBlockGetter, BlockPos.ZERO`; keep original `Shapes.block()` identity, original virtual mod dispatch and physical Level/BlockPos. A `getShape` override may be called nested inside collision, support, visual or occlusion; never transform every method independently. `BlockGetter.clip` physical ray, `BlockCollisions` AABB and target physical `VoxelShape` remain separate from canonical local State/owner shape. Two different cube charts meeting at an edge can demand different source/neighbor canonical occlusion sides (see [3R-1](PHASE3_R1_3A_SHAPE_CALLPATH_AND_COVERAGE_1_21_1.md)).

## 3/4 — NeoForge and mod override/bypass classification (no runtime implementation)

| Category | Current source evidence | Status / next exact evidence gate |
| --- | --- | --- |
| Original base virtual dispatch | Pinned source declaration-search matrix + eight direct source superclass checks | 241 registered keys accounted, but 241 per-signature **nearest effective inherited declaring owners OPEN** |
| NeoForge 21.1.215 class signatures and patch behavior | Prior [C11 selected NeoForge 21.1.215 ASM/JUnit](PHASE2_STAGE3C11_SHAPES_OCCLUSION_SUPPORT_RENDER_CULLING_NEOFORGE_ASM_1_21_1.md) from [successful CI #38289949215](https://github.com/Abbygree11/Planetary/actions/runs/38289949215), covering selected BlockStateBase, Block.shouldRenderFace, shape/cache and ModelBlockRenderer hooks | **SELECTED compiled ABI only**. No full 241-class ASM owner/callsite matrix yet; later comparative NeoForge patch SHA must not be assumed exact |
| Create1.21.1 connected glass | Pinned Create comparative `Creators-of-Create/Create@7ff2760e4924d35d4bc2e22c4b19923f0178cd16` `ConnectedGlassBlock` overrides face-skip/visibility and inherits from TransparentBlock | `skipRendering`/NeoForge face hiding is a **3B visibility author**, not one of six 3A VoxelShape declarations; mod artifact/JAR/game acceptance OPEN |
| AE2 QuartzGlassBlock | Pinned AE2 source `AppliedEnergistics/Applied-Energistics-2@db17504a86128fdf3dae31f5fb7a112a646e0b93` with independent type-based face-skip semantics (prior C11 evidence) | Separate culling/geometry bypass; **SOURCE COMPARATOR ONLY**; actual NeoForge/version-compatible loaded JAR PENDING |
| Dynamic shape: BE, Level, CollisionContext, foreign BlockGetter | Shulker Box, Scaffolding, connected fences/walls and mod-created fake contraption worlds | Position/context dependency and per-BlockState cached face occlusion may diverge; **no blanket cache rewrite**, no proof from direct-method search; enumerate overrides and provenance |
| Shader/custom BakedModel/BER | Source R2, C13–C15, provisional 3C-1 | Separate whole-Phase3 buckets; do not move 3A geometry into shader path |

No block IDs were "fixed"; the exact next task is **SOURCE/CLASSFILE OWNER RESOLUTION**, not runtime changes. True safety boundary remains typed original caller/world/target frame and distinct canonical-vs-physical output, never global `Level`, `Direction`, `VoxelShape` or `BlockStateBase.Cache` rewriting.

## 4/4 — Evidence, strict completeness, next packet

| Proof gate | Status |
| --- | --- |
| Original registered concrete class/ID key integrity | **241 / 241 CLASS KEYS and 1060 / 1060 original ID counts CROSS-JOINED** against committed immutable P2 TSV |
| Six VoxelShape + five lighting/render search-signature candidate columns | **241-row matrix created**, source search nonmatch is NOT source-method absence |
| Exact 241 nearest effective inherited owners | **NOT YET**, must resolve intermediate unregistered parents, overload signatures, default BlockBehaviour and inherited virtual override |
| Every NeoForge exact 21.1.215 per-owner patched shape method/invoke | **NOT YET**; prior C11 is selected compiled target proof only |
| Complete 3B–3E whole-phase census | **OPEN** |
| Real mod JAR, all six faces/12 edges/8 corners, no-Planet acceptance | **NOT TESTED** |
| New CI | **NOT REQUIRED**, docs-only research/TSV; pre-existing provisional 3C-1 [CI #39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) SUCCESS is NOT a 3R-2 test |

**Next `3R-2b` — RESEARCH ONLY:** take each of the **241 registered classes**, resolve full original class hierarchy (including all unregistered base classes) and each precise VoxelShape overload's nearest declaring Java owner, not simply files found by code search. Verify exact selected NeoForge21.1.215 ABI and branchy original callsites for dynamic-/shape-offset families; reconcile the 241-row matrix with original runtime registry snapshot/hash; map mod override/bypass owners and explicitly separate `getRenderShape` and VoxelShape. Update [phase-wide coverage](PHASE3_OWNER_COVERAGE_1_21_1.tsv), gate, phase card, roadmap and handoff. **Do not start 3B as `3R-3` until 3A's effective-owner gap is closed; do not run client or alter runtime during research.**
