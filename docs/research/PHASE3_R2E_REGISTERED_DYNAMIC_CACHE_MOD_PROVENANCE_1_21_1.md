# Planetary — `3R-2e`: original BlockState dynamicShape/cache census and real mod override provenance

**2026-10-25 · research-only 4/4 · `2.0` · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21**

**Scope:** original registered **all BlockState variants**, vanilla cache eligibility and observed cache population; representative **pinned mod** override/fake-world source routes; verify caller provenance. **No production Java, Mixins, debug laboratory V8, world data or game behavior changed.** One research-only JUnit source added; normal CI tests ran. **Phase3A is NOT fully researched and whole Phase3 remains PARTIAL.**

## 1/4 — Original runtime registry and exact caching evidence

Original target runtime test [`Phase3R2eRegisteredDynamicShapeCacheCensusTest`](../../src/test/java/dev/planetary/world/Phase3R2eRegisteredDynamicShapeCacheCensusTest.java) bootstraps actual 21.1.215 original `BuiltInRegistries.BLOCK`, iterates every block's `getStateDefinition().getPossibleStates()`, reads `BlockBehaviour.BlockStateBase.cache` via exact-target JVM reflection, checks `Block.hasDynamicShape()` and `BlockState.hasOffsetFunction()`, asserts original ID/class counts and no mismatch between dynamic and cached state eligibility.

[CI **#40206261371**](https://github.com/Abbygree11/Planetary/actions/runs/40206261371) **COMPLETED SUCCESS** on test [`a7f1daf`](https://github.com/Abbygree11/Planetary/commit/a7f1dafe7d5c04d97f2fd6c3252c2f8eda898d1e). Published original registry artifact **12603169554** (name `phase2-neo1211-registry-census`; contains new reports). The artifact was re-downloaded and parsed; ZIP SHA-256 `bf3f4f677591d3584fdad8d294e21d51f0975ef4ca529229fecb0f44bf4ff1c3`.

| Complete exact original 21.1.215 registry | Verified value |
| --- | ---: |
| Registered Block IDs | **1060 / 1060** |
| Registered Java block classes | **241 / 241** |
| Total legal BlockState variants | **26684** |
| States with actual non-null `BlockStateBase.cache` | **26505** |
| States with actual null cache | **179** |
| Registered block IDs with `hasDynamicShape=true` | **22** |
| Registered Java classes containing those IDs | **6** |
| State variants with `hasOffsetFunction=true` | **126**, of which **32** dynamic/uncached and **94** static/cached |
| Contradictions to vanilla `dynamicShape` cache eligibility in every tested state | **0** |

The new artifact reports (exact bytes) and SHA-256:
- `phase3-neo1211-block-dynamic-cache.tsv`: **1060 block rows**, SHA `621a1704b597dfdca2833e62a4b341586af0d98f4579a1d6bdb7d284d3c30f2d`.
- `phase3-neo1211-class-dynamic-cache.tsv`: **241 class rows**, SHA `35696687d3212650efdb4e694f672a28dd7381acaa7e9fe2de5eef455d3a9f56`.
- `phase3-neo1211-dynamic-cache-summary.txt`: SHA `57a3da689f6a3c4bef84118efab50addbc57eca6dd7a00ab6f434918222b37dd`.

**Dynamic class aggregate (original target runtime):**

| Java declaring block class | IDs | States | Uncached states | Offset states |
| --- | ---: | ---: | ---: | ---: |
| `BambooStalkBlock` | 1 | 12 | 12 | 12 |
| `PointedDripstoneBlock` | 1 | 20 | 20 | 20 |
| `PowderSnowBlock` | 1 | 1 | 1 | 0 |
| `ScaffoldingBlock` | 1 | 32 | 32 | 0 |
| `ShulkerBoxBlock` | 17 | 102 | 102 | 0 |
| `MovingPistonBlock` | 1 | 12 | 12 | 0 |
| **Total** | **22** | **179** | **179** | **32** |

[Permanent 22-ID lookup](PHASE3_R2E_ORIGINAL_DYNAMIC_BLOCK_IDS_1_21_1.tsv) is a quick query convenience; the **entire 1060 IDs and 241 classes are in the CI artifact and regeneratable via committed JUnit**, NOT falsely claimed to be present in that 22-ID summary. This makes the status auditable even after this chat.

**Interpretation:** `dynamicShape=true` identifies original vanilla states not eligible for `BlockStateBase.Cache`; it does NOT assert that every other face-occlusion result of a mod is strictly immutable, that an offset block has dynamic collision, or that a custom `BlockGetter` carries true Planet coordinates. There are **94 offset states that still cache**, which is valid given original cache's guards for nonempty collision with offsets (not evidence of bad cached collision).

## 2/4 — Source owner inheritance, per-position context and dispatch

- [`BlockBehaviour.BlockStateBase.initCache`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L562) checks `!getBlock().hasDynamicShape()`; on static shapes constructs a **global per-BlockState** cache, using `EmptyBlockGetter.INSTANCE/BlockPos.ZERO` (previous 3R-2d exact target ASM [CI #40145655409](https://github.com/Abbygree11/Planetary/actions/runs/40145655409)).
- [`BlockStateBase.getFaceOcclusionShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java#L613) indexes six **canonical `Direction.ordinal()`** masks if cached, otherwise delegates to position-aware `getOcclusionShape/getFaceShape`. Support uses cached three-`SupportType` boolean lookup when available.
- Original six VoxelShape declaring-owner signatures have complete 241/241 JVM evidence from [3R-2c atlas](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.tsv). This stage adds a **different axis** of proof: exact *state variants and cache eligibility*, not a claim that other caller methods have all been inventoried.
- `ScaffoldingBlock` is dynamic with `CollisionContext.isAbove/isDescending`, `ShulkerBoxBlock` with moving live BlockEntity bounding/support, `MovingPistonBlock` with swept motion semantics; bamboo/dripstone/loose snow have their own placement/collision callers. **Register flags do not certify correct actor-local Y or support across Planet seams.**
- `getShape`, `getCollisionShape`, `getVisualShape`, `getInteractionShape`, `getBlockSupportShape`, `getOcclusionShape` may be nested. `PlanetBlockShapeRuntime` rotates only on an outer physical query when a real `Level` is available and bound. Preserving original virtual dispatch and static cache orientation remains mandatory; do not unconditionally rotate every return, every `VoxelShape` or global `BlockStateBase.Cache`.

## 3/4 — Actual mod source comparisons: custom visibility, virtual worlds and renderer bypasses

[**11-row pinned source/Planet integration matrix**](PHASE3_R2E_MOD_VIRTUAL_WORLD_AND_CULLING_OWNER_MATRIX_1_21_1.tsv) separates source and runtime proof levels. Most important verified comparator chains:

- Create [`ConnectedGlassBlock`](https://github.com/Creators-of-Create/Create/blob/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create/content/decoration/palettes/ConnectedGlassBlock.java) (`extends TransparentBlock`) overrides `skipRendering` to hide faces next to other connected glass; [`WindowBlock`](https://github.com/Creators-of-Create/Create/blob/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create/content/decoration/palettes/WindowBlock.java) subclasses and overrides it independently. Both belong to **visibility 3B**, not the six vanilla 3A shape declaring methods. No loaded compatible mod jar proof.
- Create [`VirtualRenderWorld`](https://github.com/Creators-of-Create/Create/blob/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create/foundation/virtualWorld/VirtualRenderWorld.java) **extends `Level`** but maintains its **own `blockStates`/`blockEntities` maps and `ModelData`**, physically distinct virtual-model positions. [`ContraptionWorld`](https://github.com/Creators-of-Create/Create/blob/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create/content/contraptions/ContraptionWorld.java) extends Catnip `WrappedLevel` and returns contraption-state blocks; its exact wrapper hierarchy/coordinates need separate ABI verification. **`instanceof Level` does not prove this is the *same authoritative physical* bound world.**
- Planet's [`PlanetGravityRuntime`](../../src/main/java/dev/planetary/gravity/PlanetGravityRuntime.java) uses **`IdentityHashMap<Level,Binding>`** and checks the actual Level object in `findAt`; an unbound virtual `Level` is NOT automatically treated as bound Planet, which is a protective boundary. However [`PlanetClientRenderFrame`](../../src/main/java/dev/planetary/client/render/PlanetClientRenderFrame.java) **falls back to `Minecraft.getInstance().level` for non-`Level` BlockGetter**, risking false frame provenance if that getter holds virtual/preview blocks at corresponding positions. Both are **code-verified compatibility risks, not a proven client failure**.
- AE2 [`QuartzGlassBlock`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/db17504a86128fdf3dae31f5fb7a112a646e0b93/src/main/java/appeng/decorative/solid/QuartzGlassBlock.java) defines independent `skipRendering`. A **different-version source comparator**, [`QuartzGlassModel` at `b141e917...`](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/b141e91764897d3844c9debd4210396b5f9b5238/src/client/java/appeng/client/render/model/QuartzGlassModel.java), generates quads from neighbor `getAppearance` via `BlockAndTintGetter` and `DynamicBlockStateModel.collectParts`; its API is **NOT verified to correspond to NeoForge21.1.215**. It proves a separate model-render path *exists*, not compatibility with Planet today.
- AE2 [`CertusQuartzClusterBlock` at same different-version comparator](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/b141e91764897d3844c9debd4210396b5f9b5238/src/main/java/appeng/decorative/solid/CertusQuartzClusterBlock.java) overrides `getShape` and checks neighbor `isFaceSturdy` for survival. Dynamic shape/attachment is a separate source and owner family; **same comparator-version warning applies**.

**Classification:** VANILLA_TARGET_REFLECTION (241 classes / state cache), PINNED_THIRD_PARTY_SOURCE_COMPARATOR (Create / AE2 at specified SHAs), PLANET_SOURCE_CODE_INSPECTED (Level and render fallback), and LOADED_MOD_GAMEPLAY_PENDING are **not interchangeable statuses**. Any later fix must operate on deepest safe owner while preserving external NeoForge/render/model overrides and physical coordinates.

## 4/4 — Completeness decision, remaining gaps and next fixed packet

| Gate | Evidence | Status |
| --- | --- | --- |
| All original registered blocks, class families, and possible BlockState cache eligibility | [CI #40206261371](https://github.com/Abbygree11/Planetary/actions/runs/40206261371), artifact 12603169554, two TSV hashes above | **PASS 1060/241/26684, dynamic 22 IDs and 179 states, zero cache eligibility mismatch** |
| All original six block-shape method declaring owners | 3R-2c original exact reflection atlas + CI | **PASS for six named vanilla methods** |
| Cache/face/support selected ABI and physical/contextual caller path | 3R-2d exact selected ASM, pinned source, mod matrix | **SELECTED PASS**, not all callers |
| Exact modded 21.1.215-compatible loaded class/JAR and fake-world transforms | Pinned Create/AE2 source comparators, Create virtual Level code, Planet fallback read | **SOURCE ONLY / RUNTIME PENDING** |
| Planet client applied Mixins, all six faces, 12 edges, 8 corners, regular non-Planet world | No new Minecraft client or loaded mod in this research | **GAMEPLAY PENDING** |
| Full 3A owner/override and dynamic cache invalidation research | Dynamic registry and selected consumers done; alternate render/fake getter owner census and no stale-cache proof open | **INCOMPLETE** |
| Full Phase3 3A–3E source/ABI/mod audit | 3B/3C/3D/3E all still OPEN | **INCOMPLETE** |

**Next `3R-2f` — RESEARCH ONLY:** finish explicit vanilla/NeoForge *consumer ownership* for cached vs dynamic `getFaceOcclusionShape` and `SupportType`; create exact compatible-version mod/fake `BlockGetter` and alternative render/geometry override matrix (Create `VirtualRenderWorld`, Catnip `WrappedLevel`, AE2 connected-model etc, including version-specific originals), classification of `PlanetClientRenderFrame` fallback and `PlanetBlockRenderCulling` stale cache. Freeze 3A source/ABI/caller/extension inventory when evidence is complete; do not mark gate closed merely because 22 vanilla dynamic IDs are now enumerated. **Do NOT implement cache fixes yet**; user requires full Phase3 (3A–3E) researched before next Phase3 runtime work. `main` untouched; V8 and provisional 3C-1 gameplay status remain PENDING.
