# Planetary — 3R-7F-5 exact Iris 1.8.12 / Sodium 0.6.13 NeoForge shader owner, resource and loader metadata study

**2026-10-29 · Minecraft 1.21.1 / NeoForge 21.1.215 / Java21 · branch \`2.0\` · RESEARCH ONLY, 4/4.** No production Java, Mixins, Minecraft client/world, V8 game lab or \`main\` modifications. **Overall Phase3 Research Gate R OPEN and real gameplay V OPEN.** In particular this study does NOT prove the published Iris JAR can load on target MC 1.21.1.

## 1/4 — Exact published versions, cryptographic source and unresolved loader contradiction

Unlike newer GitHub branch source comparators, this audit downloaded the **actual two published NeoForge release JARs** using the official Modrinth Version API, verified the API-provided SHA512 and pinned SHA1 against the downloaded bytes, and inspected the original packaged NeoForge mod metadata.

- [Iris 1.8.12+1.21.1-neoforge](https://modrinth.com/mod/iris/version/1.8.12%2B1.21.1-neoforge), version ID \`t3ruzodq\`, SHA256 **\`3e3f94d2d5cdcb8145c4385d521974aa2d8669b2cc212351f70cf06488182e83\`**, 2,438,548 bytes, SHA1 \`a3e6355915c7d3b2bc392724795113e51d289378\`.
- [Sodium 0.6.13 for NeoForge](https://www.curseforge.com/minecraft/mc-mods/sodium/files/6382651), official Modrinth version ID \`Pb3OXVqC\`, SHA256 **\`7acd7af2ef75ec883a6872c17494ead65e38025151eb08a1d60e36d34377f403\`**, 1,162,994 bytes, SHA1 \`38af70fa4dc4b2aaac636e92fdba3bedd5a025e1\`.
- The *exact* Iris Modrinth API release metadata declares direct required dependency on **Sodium project \`AANobbMI\`, version \`Pb3OXVqC\`**, not a guessed loose pairing. The first script commit \`114cbe6\` erroneously looked for the text "sodium" in this dependency object; [CI #40961335728 FAILED](https://github.com/Abbygree11/Planetary/actions/runs/40961335728) on that **research assertion only**, after successful binary download/verification. It did not demonstrate a mod/Planet build failure. Corrected in [\`d2572ec\`](https://github.com/Abbygree11/Planetary/commit/d2572ecf3fb3c49e0b8c8dc300250f66cf595a7f) to check IDs, inspect original metadata separately. Strengthened in [\`9e8c11e\`](https://github.com/Abbygree11/Planetary/commit/9e8c11e714efe255bedad7c9cb1c5184eb45fbd7).

**Critical exact-original JAR discovery, do not suppress:** the Iris JAR's \`META-INF/neoforge.mods.toml\` embeds \`version = "1.8.12-snapshot+mc1.21.1-local"\` and \`minecraft versionRange = "[1.21,1.21.1)"\`, despite the publisher's Minecraft **1.21.1** release label. The upper bound is **exclusive** under standard version-range semantics, so on its face the packaged mod metadata excludes \`1.21.1\`. The original Iris TOML does require Sodium \`[0.6,)\` and excludes Embeddium. Sodium's exact JAR requires \`minecraft versionRange = "1.21.1"\` and \`neoforge versionRange = "[21.1.82,)"\`. **This is a verified internal metadata discrepancy, NOT a verified successful or failed target mod-loader launch**. A **real NeoForge21.1.215 loader acceptance check of these exact SHA-pinned release files** and an alternate corrected Iris release selection are required before any compatibility claim. Treat official page claims as metadata, not a substitute for reading the JAR.

## 2/4 — Published original JVM, Mixins and shader-pass separation

The independently archived research probe \`scripts/research/phase3_r7f5_published_iris_sodium_probe.py\` selects **20 original binary classfiles** and executes Java21 \`javap -private -s -c\` for each. Exact published archives contain **944 Iris classes and 576 Sodium classes**. The original exact binary class list includes:

- **Iris:** \`PipelineManager\`, \`WorldRenderingPipeline\`, \`IrisRenderingPipeline\`, \`ShadowRenderer\`, \`CompositeRenderer\`, \`RenderTargets\`, \`ShaderPack\`, \`SodiumShader\`, \`SodiumPrograms\`, and \`MixinSodiumWorldRenderer\`.
- **Sodium:** \`RenderSectionManager\`, \`ChunkBuilder\`, \`ChunkRenderer\`, \`BlockRenderer\`, \`FluidRenderer\`, \`ShaderChunkRenderer\`, \`TerrainRenderPass\`, \`GlProgram\`, \`VertexSerializer\` and \`RenderDevice\`.

The exact published Iris \`PipelineManager\` JVM field \`versionCounterForSodiumShaderReload\` and \`pipelinesPerDimension\` demonstrate pipeline/cache lifecycle separately from Planet's original \`BlockState\` rendering. Exact \`MixinSodiumWorldRenderer\` references \`SodiumWorldRenderer\`/\`RenderSectionManager\` and shadow-related \`needsUpdate\` behavior; the original Sodium block/section CPU producer and shader renderer use distinct Mixin-targeted entry points from vanilla \`ModelBlockRenderer\`. This shows **real bytecode ownership**, not applied Mixin behavior or proof its injection targets match another version.

Original packaged resources: **11 Iris Mixin JSON configs, 2 Sodium Mixin JSON configs, 258 total declared Mixin list entries** (including development/integration configs: NOT a count of enabled runtime Mixins); **3 Iris shader resources and 8 Sodium shader resources**, including Sodium block opaque vertex/fragment and GLSL includes for chunk vertex, chunk matrices and material. Original resource hashes and name-only shader keyword presence are captured, **not** assertions that all third-party PBR/normal/shadow shaderpacks are compatible. No source JAR is claimed; this is exact binary/JVM + resource evidence, plus independently marked source comparator.

Relevant pinned *comparator only* [Iris 1.21.1 branch @12ea3bf](https://github.com/IrisShaders/Iris/tree/12ea3bf16b39c0a70ec31dc9de1483c77bd3ccc3) exposes \`PipelineManager.preparePipeline\`, world/dimension-oriented pipeline cache, \`MixinSodiumWorldRenderer.setupTerrain\` shadow-related forced rebuild and separate \`ShadowRenderer\`. That branch's TOML also carries the same upper-exclusive \`[1.21,1.21.1)\` shape but the branch HEAD is **not proved byte-identical** to the official release, so original published hashes/JVM take precedence.

## 3/4 — Frame/normal/resource lifecycle integration contract, not fixes

[28-route source/binary/loader/visual acceptance matrix](PHASE3_R7F5_IRIS_SODIUM_SHADER_PIPELINE_OWNER_AND_GATE_1_21_1.tsv) separates:
- Vanilla/NeoForge model compiler and \`Block.shouldRenderFace\` from **Sodium's own region \`ChunkBuilder\`, model/mesh producer \`BlockRenderer\`, independent \`ShaderChunkRenderer\` and GPU vertex format**.
- Iris dynamic dimension/pack pipeline, shaderpack \`GBuffer\`/composite/shadow pass, Iris-to-Sodium Mixin targets, normals/projection/world-to-camera matrices and resource reload generations.
- Create6.0.10/Flywheel1.0.6 original instance transform/normal and GPU lifetime from previous [3R-7F-2](PHASE3_R7F2_FLYWHEEL_106_RELEASE_GPU_NORMAL_LIGHT_LIFECYCLE_1_21_1.md). Shaderpass transformations cannot be globally inherited from vanilla \`ModelBlockRenderer\` changes.
- Wrong-lifetime world/fake Level leakage: the supplied renderer's region/virtual-world/section-source/vertex source must be authoritative. A shadow pass using the ambient \`Minecraft.level\` need not share the coordinate chart of a Create virtual render world or previous worker. **Do not globally rotate shader projection, lighting or normals by +Y**. Need explicit per-producer frame/normal policy and non-Planet fallback when source isn't proven.
- Resource/generation: Iris pipeline reset, per-dimension shader selection, Sodium section/mesh GPU lifetime, Flywheel visuals, vanilla resource reload and queued work **all have distinct invalidation owners**. General vanilla section disposal is not a GL fence for these independent renderers.

**No production correction**. Future V test (only after 3R-7G): loader startup with exact compatible, SHA-pinned Iris/Sodium pair, both shaders OFF and ON, actual shaderpack normal/PBR/shadow/composite, actual resource reload, chunk rebuild and 6 faces/12 edges/8 corners, +Y baseline, noPlanet, Create/Flywheel/AE2 virtual-world and dynamic face cases. If published Iris metadata excludes the target, select/version-fix in a separately researched scope before trying to claim V PASS; don't silently patch downloaded mod ZIP or bypass loader checks.

## 4/4 — CI, separately checked official original archive and research disposition

New [workflow \`phase3-r7f5.yml\`](../../.github/workflows/phase3-r7f5.yml) independently downloads published JARs and checks original official SHA-1/SHA-512 via GitHub Actions Java21. [**CI #40961707969 SUCCESS**](https://github.com/Abbygree11/Planetary/actions/runs/40961707969) on source [\`9e8c11e\`](https://github.com/Abbygree11/Planetary/commit/9e8c11e714efe255bedad7c9cb1c5184eb45fbd7), artifact **12934793610**, downloaded independently. Complete ZIP SHA256 **\`7a25d69513bcd921abfd142ee2ae9df85df59e63e8e1640f9ba79e302f107c7c\`**. Seven checked generated members with exact SHA256:

| Artifact member | SHA256 |
| --- | --- |
| \`release-hash-abi.tsv\` | \`02ac479302722690aaaee18be96da70faab66f0b1408c86e14d0dfe32f589fe5\` |
| \`selected-original-jvm-classfiles.tsv\` | \`6e2abdb97a3b3708f480da3a483282c5c0b9600026e369d1c947887a70611662\` |
| \`selected-original-javap.txt\` | \`fab6c1fd865f781bfbd5d51d91537702e24a15c4b7e292e49c55c3c670ee3719\` |
| \`published-neoforge-mod-metadata.tsv\` | \`e821c0f87f920365113b74cc1d3364d0264624363eab5b0e2631964161d9006c\` |
| \`published-mixin-configs.tsv\` | \`73dba894e1aef4d0ce692c05191744d8a4581f209140d3733406a9d5552b64df\` |
| \`published-shader-resources.tsv\` | \`7c15fe918a872f8d49f1bbb1c994d0dbb5feccd01a99b1af122adf8860819c08\` |
| \`summary.json\` | \`12d30bd2f28ee56e429da5998f4b317f5c6ffb62f2fa0a70826b9ae9eeb031af\` |

**Owner gate**: Optional G07 **D14/E22** are promoted from \`OPTIONAL_SHADER_SOURCE_SCOPE_UNVERIFIED\` to **\`BOUNDED_SHADER_BINARY_RESEARCH_PASS_LOADER_CONFLICT_V_OPEN\`**. This means exact JAR binary ownership/metadata, not loader or shader support. D03 remains historical reconciliation for 3R-7G. Historical 3R-7/7F-1/7F-2/7F-3/7F-4 matrices unchanged. 151 unique source-owner IDs; zero *named G01–G06* source blockers. **Whole Phase3 R remains OPEN** for strict per-ID 46 source-guard + 18 stale/superseded audits and deciding the new Iris loader metadata discrepancy, and real V remains OPEN.

**NEXT \`3R-7G\`** 151-entry source/grade/compat decision, with clear verdict on D14/E22 shader advertised support vs target JAR loader mismatch and responsible fallback; only after source guard can Phase3 shared production patch and real V begin; only then return to Phase2.4A.