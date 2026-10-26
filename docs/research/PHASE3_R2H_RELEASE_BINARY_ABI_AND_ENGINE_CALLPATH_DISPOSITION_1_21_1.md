# Planetary — `3R-2h`: released mod ABI and 144 original caller dispositions

**2026-10-26. Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21 / branch `2.0`. Research-only, 4/4.** No production Java/Mixins, world data, V8 test lab or `main` changes. **Whole 3A and whole 3A–3E phase research remain OPEN.**

## 1/4 — Compatible releases, not development branch versions

Create release [`mc1.21.1-6.0.10`](https://github.com/Creators-of-Create/Create/releases/tag/mc1.21.1-6.0.10) at tag `ac0c444d9828da3453ae8cc65338e8de063286fb` has `gradle.properties` showing MC 1.21.1, NeoForge build version `21.1.219`, Ponder dependency `1.0.82`, Flywheel `1.0.6`. **Prior Create source `7ff2760` is a newer development state advertising `6.0.11` and `1.0.85`; do not mistake it for the inspected release.** In the released Create source both `ContraptionWorld` and `VirtualRenderWorld` retain the already described separate storage/wrapper semantics.

The official developer documentation associates release 6.0.10 with Maven version `6.0.10-280`. The research workflow downloaded this *release-equivalent* official slim build (not asserted byte-identical to a user-installed release JAR), Ponder 1.0.82, and the exact original AE2 [19.2.18 GitHub release](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/tag/neoforge/v19.2.18) @`db17504`. Maven `.sha256` and GitHub release asset digest were independently verified before JDK21 `javap -s -p` checks.

| Binary | SHA-256 | Confirmed JVM classes |
| --- | --- | ---: |
| `create-1.21.1-6.0.10-280-slim.jar` | `f0652bee27460f2d26a748f537cb5d687981441378d3a526d17ffaab5a1072bb` | 2 |
| `ponder-neoforge-1.0.82+mc1.21.1.jar` | `0cf4611ad853042b689ac386184c5bbe02950efcffddb49e5f604e82baddb0dc` | 1 |
| `appliedenergistics2-19.2.18.jar` | `df15a07f86ca1ca93aa66373f9d5d84dd3d4586631fceb38f4fbc85dad591e39` | 5 |

Verified declared type/method owners: Create `ContraptionWorld extends WrappedLevel` with `getBlockState`; `VirtualRenderWorld extends Level` with `getBlockState`; Ponder/Catnip `WrappedLevel extends Level` and declares `getLevel/getBlockState`. AE2 `GlassBakedModel implements IDynamicBakedModel` with `getModelData/getQuads`; `FacadeBlockAccess implements BlockAndTintGetter` with `getBlockState`; `CableBusBakedModel.getModelData/getQuads`, `QuartzGlassBlock.skipRendering`, `CertusQuartzClusterBlock.getShape/canSurvive`.

**Limits:** this validates released class declarations, not method-body dynamic behavior, real loaded mods, frame transformations or Mixins. Create's compile-against NeoForge 21.1.219 neither proves nor disproves its loader compatibility with Planetary 21.1.215; actual mod metadata and loaded-client acceptance remain separate.

## 2/4 — All additional selected original JVM calls classified, not conflated

The committed [reproducer](../../scripts/research/phase3_r2h_release_binary_and_callpath_audit.py) re-reads the immutable [3R-2g original CI artifact](https://github.com/Abbygree11/Planetary/actions/runs/40242279888) from NeoForge 21.1.215. Byte SHA of exact original 543-row invocation corpus is `94aa7f868f428400b9f5539fba9af8107d69ba0aea9dcc56f9a8cd0eaf4bf161`. It compares all 7 caller/callee JVM fields with 3R-2f's 276 rows: **247 overlap, 273 new, 6 Planet-only old invocations omitted by design**. Among new matches: 114 generic `getBlockEntity` queries, 15 semantically unrelated `getOffset` overload/name collisions, and **144 remaining relevant method-name candidates**.

All 144 have individual source owner, source/target method descriptors, opcode, provisional owning phase and coordinate-dispatch contract in the generated [CI TSV artifact](https://github.com/Abbygree11/Planetary/actions/runs/40315164089/artifacts/12646527522), **not merely the aggregate below**.

| Provisional owner groups | Count |
| --- | ---: |
| 3A world geometry + NeoForge extensions | 25 |
| 3B ModelData, light, AO, baked/fluid rendering | 26 |
| 3E client effects, particles, outline/debug/shadow | 17 |
| Phase 7 entity movement, floor, AI, respawn | 43 |
| Phase 9 worldgen structures | 18 |
| Phase 2 fluids and item authors | 10 |
| Cross-phase utilities awaiting source resolution | 5 |
| **Total** | **144** |

The original `SupportType$1/$2/$3` anonymous implementations, `PistonMovingBlockEntity` context-dependent collision, `CollisionGetter`, `WorldBorder` and NeoForge `IBlockStateExtension` visibility hooks are especially important *new or previously unenumerated exact compiled callers*. They cannot be ignored merely because the previous 3R-2f scan included the outer `SupportType` enum class. Direct INVOKE detection is not exhaustive virtual dispatch/reflective execution; evidence level remains **ORIGINAL_COMPILED_SELECTED**, not gameplay.

Light propagation `LightEngine` and NeoForge `SmoothQuadLighter` consume face occlusion on original compiled paths; model `SectionCompiler` and dynamic `ModelData` are independent. These are **3B owners** awaiting the full Phase3B pass, not license to globally mutate physics/lighting. Entity and AI path collision consumers are Phase7; generation `JigsawPlacement`/Mineshaft are Phase9. All 144 are classified as producer candidates without silently extending a Phase3A owner gate to those external mechanisms.

## 3/4 — Mod-world/fake BlockGetter/frame and cache boundaries

[25-row source/ABI/dispatch/bypass matrix](PHASE3_R2H_RELEASE_BINARY_AND_DISPATCH_OWNER_MATRIX_1_21_1.tsv) captures individual statuses and missing evidence.

Create `ContraptionWorld` stores contraption states in own position space despite extending the Catnip `WrappedLevel` chain; `VirtualRenderWorld` has separate blocks, BlockEntities, light and chunks. The released Ponder `WrappedLevel` delegates to another `Level`. Therefore **`instanceof Level` is not Planet binding authorization**, and wrapper/block coordinates are not inherently those of the wrapped physical world. This is a contract for future provenance/adapters; no mod game bug is claimed.

AE2 `FacadeBlockAccess` is a non-Level `BlockAndTintGetter` that can provide substituted facade state. The legitimate target-version AE2 `GlassBakedModel` owns neighbor-dependent `ModelData` and conditional quads, `CableBusBakedModel` has a separate quad/cache path, and `QuartzGlassBlock` has its own `skipRendering` override. They require correct two-block frames and model/world provenance, not a blanket `VoxelShape` or `BlockGetter` rewrite.

Planet's current non-Level getter fallback to active client Level and state-pair-only occlusion cache lacking context position/world/time/BE remain **source-verified risks only**, not reproductions. No cache/mixin or game-world patch was made in this packet.

## 4/4 — Original CI result, immutable proof and research gate

New [research-only CI workflow](../../.github/workflows/phase3-r2h.yml) reuses original target-JVM census, classifies all 144 selected calls and downloads independent official release/mod Maven JARs; hashes and `javap` check precise external compiled type and method declarations. It uploads **only generated metadata and JVM declarations; not the third-party JAR files**.

**[CI #40315164089](https://github.com/Abbygree11/Planetary/actions/runs/40315164089) — COMPLETED SUCCESS.** Artifact **12646527522** independently re-downloaded; ZIP SHA256 **`f9b3edc4a42b1cd8eb7bcc7c941f09826228f091b9fd3bcf02be9c3268286c97`**. Twelve files: full 144-call disposition TSV, 8 checked `javap` class listings, 8-row ABI TSV, two summary JSON files. The official built-in original project test and new research-only workflow have distinct evidence scopes.

**3A incomplete:** investigate concrete effective dispatch and exact source signature/position semantics for `SupportType$1/$2/$3`, `PistonMovingBlockEntity`, `CollisionGetter`, `IBlockStateExtension` plus custom dynamic/BE geometry, lifecycle of fake worlds, cache invalidation, exact 21.1.215 loader compatibility, and real installed-mod tests. 3B–3E need their own Phase2-depth source/ABI/renderer/bypass census. Code CI green cannot replace the missing gameplay and loaded mod gates.

**NEXT `3R-2i` — RESEARCH ONLY:** close selected remaining 3A dynamic-support and moving-collision dispatch callers with original compiled source, full override/context ownership and explicit frame provenance acceptance plan; freeze 3A only when evidence supports it. Do not move into 3B or implement fixes until the user-required full-phase research gate is satisfied.
