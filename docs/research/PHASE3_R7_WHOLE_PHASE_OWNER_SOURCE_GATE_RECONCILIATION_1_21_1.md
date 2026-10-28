# Planetary — `3R-7`: полнофазный аудит результатов 3A–3E и решение по source gate R

## 2026-10-29 — 3R-7F-3 G03/G04 original research source 4/4 PASS, 6 -> 2 R blockers

[Original source and controlled Planet cache report](PHASE3_R7F3_DYNAMIC_FACE_CACHE_AND_INDIRECT_GEOMETRY_CALLER_1_21_1.md) · [effective 151-row source gate](PHASE3_R7F3_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) · [four-row delta](PHASE3_R7F3_SOURCE_GATE_DELTA_1_21_1.tsv) · [Java21 CI #40778087503 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40778087503), 9430 original classfiles, 543 direct calls/247 owners, 4 invokedynamic handles (3 ClipContext$Block shape method references + 1 StructureBlockEntity.getBlockEntity handle), zero selected exact reflection name literals. Actual Planet state-only face cache returns stale false on a **mocked** context-changing getFaceOcclusionShape; future conservative no-context-Boolean-cache policy documented, **not implemented**. A17/A35/B11/A25 are BOUNDED_R_SOURCE_PASS_V_OPEN only, not gameplay/mod-loaded PASS. Remaining **2 R source blockers E03/E10 in G06**, 46 source guard rows and optional shader claims; V OPEN for dynamic mod blocks and 6/12/8 +Y/noPlanet. **NEXT 3R-7F-4**. Historical 151-row gate snapshots unchanged.


## 2026-10-29 — Subsequent 3R-7F-2 Flywheel release supersession

The historical 3R-7 and 3R-7F-1 matrices remain immutable. [Exact official Flywheel 1.0.6 released compiled/source/shader audit](PHASE3_R7F2_FLYWHEEL_106_RELEASE_GPU_NORMAL_LIGHT_LIFECYCLE_1_21_1.md) with [CI #40776433970 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40776433970) and [effective 151-row gate](PHASE3_R7F2_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) changes only D10/D11/E20 in G05 to **BOUNDED_R_SOURCE_PASS_V_OPEN**. Current total **6 R blockers (G03/G04/G06)**; 46 guard rows and V still OPEN. Next 3R-7F-3.


## 2026-10-29 — Subsequent 3R-7F-1 evidence supersedes two SOURCE blocker decisions

**This report remains the immutable 3R-7 historical stage verdict (11 blockers at that time), not the latest gate.** [Original selected 12-class G01/G02 NeoForge JVM CI #40774469160 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40774469160) plus [new original/mod fluid+BEWLR report](PHASE3_R7F1_ORIGINAL_FLUID_TESSELLATION_ITEM_BEWLR_MOD_RENDER_OWNER_1_21_1.md) resolve **B05/G01** and **C05/G02** as bounded source owner research, real mod/GUI/world/game V still OPEN. The latest [151-row effective source gate](PHASE3_R7F1_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) has **9 R-blocking rows across G03–G06**; [two-row evidence delta](PHASE3_R7F1_SOURCE_GATE_DELTA_1_21_1.tsv) proves what changed, original `3R-7` 151-row matrix and its 151-owner integrity CI remain valid historical snapshot. 46 source guard rows and Iris optional claims await explicit research/disposition. Next `3R-7F-2` exact Flywheel1.0.6 released GPU source/ABI; **R OPEN/V OPEN**.

**Дата:** 2026-10-29 · **ветка:** `2.0` · **цель:** Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21. **Это не реализация.** Цель пакета — проверить, достаточно ли исходных исследований для безопасного возвращения к Phase 2, **не объявляя новые source/game PASS без оригинального подтверждения**.

**Решение:** `3R-7` может завершить **аудит R**, но **full Phase3 research gate R = OPEN** из-за недоисследованных независимых путей рендеринга, точного Flywheel backend, динамической геометрии и очередей reload. **Phase2 visual gate V = OPEN. Возврат в `2.4A` пока запрещён.** Это отличает «исследовательская перепроверка завершена» от «весь этап 3 исследован полностью».

## 1/4 — Проверен полный исходный реестр 151 owner rows

Связанная [матрица решений для КАЖДОЙ записи](PHASE3_R7_ALL_151_OWNER_SOURCE_GATE_DISPOSITION_1_21_1.tsv) сохраняет исходные `phase/id/owner_family/status`, evidential grade и дословно оставшийся риск, а также новую `audit_disposition` и ответственный packet. Ничего не удалено/не переименовано из [исторического live ledger](PHASE3_OWNER_COVERAGE_1_21_1.tsv), чтобы не стирать прежние тесты и ссылки.

| Секция | Исходные записи |
| --- | ---: |
| 3A VoxelShape/support/physical world | **41** |
| 3B BakedModel/quads/light/fluid | **25** |
| 3C BER/provider/BE item | **48** |
| 3D independent render/accelerated | **15** |
| 3E reload/workers/client lifetime | **22** |
| **Итого** | **151** |

**Доказательность исходных строк** (эвристика классификации самих значений status, не новое доказательство байткода): **91** с ссылкой на selected original/compiled CI, **22** pinned release-source или опубликованная metadata, **26** предыдущих source/open, **12** остальных выбранных исходных/тестовых контрактов. В **18 строках** указана ранняя история либо частично устаревшая формулировка ("open census" при более позднем bounded exact ASM); это не 18 незакрытых новых механизмов, но и не 18 автоматических PASS. Следующая сверка должна ссылаться на более поздний CI, не заменяя раннюю историю.

**Диспозиции по 151 записям (не являются оценкой runtime coverage):** `BLOCKS_R_SOURCE_GATE` **11**, `OPTIONAL_SHADER_SOURCE_SCOPE_UNVERIFIED` **2**, `SUPERSEDED_OR_STALE_LEDGER_RECONCILE` **18**, `SELECTED_SOURCE_EVIDENCE_RUNTIME_GAP` **74**, `SELECTED_SOURCE_NEEDS_FINAL_GUARD` **46**. Эти классы **взаимоисключающие для удобства учёта, но степень доказанности source/ABI vs gameplay берётся из отдельной колонки**. В частности, 74 game-gaps не означают, что все 74 уже будут приняты в клиенте или что source-research по ним полностью закрыт.

**Первичные доказательства по фазам:**
- **3A** [3R-1](PHASE3_R1_3A_SHAPE_CALLPATH_AND_COVERAGE_1_21_1.md) и [2B..2M](PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md): **241/241 зарегистрированных vanilla Block classes, 1060 Block IDs, 26684 отражённых BlockStates, 26505 state-cache eligible, 179 dynamic**; original 9430 JVM classfiles / 543 consumer edges исследованы в отдельном bounded source discovery, но это **не автоматический virtual dispatch/mod JAR graph**. A24 уже частично superseded A25: раньше «полный non-Block census OPEN», позже original scan проведён; unselected/indirect/mod roots всё ещё требуют решения.
- **3B** [3R-3B-1](PHASE3_R3B1_BAKED_MODEL_QUAD_CULLING_TARGET_JVM_1_21_1.md), [3B-2](PHASE3_R3B2_ORIGINAL_AO_QUAD_SEAM_MOD_OWNER_1_21_1.md), [3B-3](PHASE3_R3B3_ORIGINAL_MODEL_UV_TINT_RENDERPASS_OWNER_1_21_1.md): original baked quad / ModelData / RenderType / AO+flat / BakedModelWrapper, UV/tint/multipart and NeoForge source owners proved on selected original JVM; **B05 fluid/transparent bypass root still distinct**; actual seam UV vs culling bug not accepted.
- **3C** [3R-4A](PHASE3_R4A_ORIGINAL_VANILLA_BER_REGISTRY_AND_OWNER_1_21_1.md), [4B](PHASE3_R4B_SIGN_BANNER_SKULL_NESTED_RENDER_SOURCE_1_21_1.md), [4C](PHASE3_R4C_REMAINING_BER_ORIGINAL_NESTED_SPACE_AND_MOD_1_21_1.md), [4D](PHASE3_R4D_NEOFORGE_BER_PROVIDER_REGISTRATION_MOD_OWNER_1_21_1.md): **24 vanilla BE type registrations / 22 BER class owners, original 22-class bounded method research, NeoForge provider map/reload and pinned AE2/Create own BER**; **C05 BEWLR/GUI/noLevel/item alternative owner** remains independent.
- **3D** [3R-5](PHASE3_R5_INDEPENDENT_SECTION_GEOMETRY_CREATE_FLYWHEEL_SHADER_SOURCE_1_21_1.md) original **10 classfiles/337 methods/3228 calls, CI #40594978070 SUCCESS**, 46 source/mod/space routes; section event worker and Create released-source `ROTATING`/`TRANSFORMED` instances/ContraptionVisual proved, but exact Flywheel1.0.6 backend/generation not verified.
- **3E** [3R-2L](PHASE3_R2L_MODELDATA_SNAPSHOT_THREAD_AND_SECTION_WORKER_1_21_1.md), [2M](PHASE3_R2M_CLIENT_LEVEL_SWITCH_DISPATCHER_CANCEL_AND_PROVENANCE_1_21_1.md), [3R-6](PHASE3_R6_CLIENT_RESOURCE_RELOAD_WORKER_EPOCH_MOD_1_21_1.md) original **15 classfiles/656 methods/5126 calls, CI #40596882360 SUCCESS**, 44 life-cycle/mod routes. `blockUntilClear` only clears queued work, `dispose` calls queued uploads, ModelData has owner Level and thread. **E03 all reload listeners/mod owners and E10 deeper SectionOcclusionGraph/late callback owner** still require specific source disposition.

**Scope preservation:** historical 2.3A/B/C Phase2 registered source audits and Phase1 canonical-vs-traversal chart, physical XYZ and mod side hooks are *prerequisites*, not substitutes for Phase3 full owner research. [Phase1 results](../phases/phase-01/results/README.md), [Phase2 results](../phases/phase-02/results/README.md), [Phase3 results](../phases/phase-03/results/README.md) must remain permanently linked when implementation resumes.

## 2/4 — Source research blockers vs acceptance-only vs optional shader claims

The source gate is **not blocked by every future gameplay test**, but it **is blocked by a previously unclassified rendering/geometry owner that could require a future Phase3 architecture change**. Six precisely bounded source clusters account for 11 current high-priority owner rows; detailed per-ID mapping is in [151-row audit](PHASE3_R7_ALL_151_OWNER_SOURCE_GATE_DISPOSITION_1_21_1.tsv).

| Cluster | Owner IDs | Missing fact / minimal proof to close | Next packet |
| --- | --- | --- | --- |
| **G01 — fluids/transparent** | `B05` | Original 21.1.215 FluidRenderer, liquid tessellation, camera/section and NeoForge fluid extension sources, shadow/translucency normal routing vs canonical block frame and installed mod entry hooks. Selected original method/jump plus inheritance/registrations; not fluid visual gameplay PASS | `3R-7F-1` |
| **G02 — BEWLR/item GUI/foreign render** | `C05` | Exact item-side `BlockEntityWithoutLevelRenderer`/ItemRenderer and NeoForge mod/custom BE item hook, noLevel fallback; verify actual class owner vs BER factory and GUI previews | `3R-7F-1` |
| **G03 — dynamic culling caches** | `A17,A35,B11` | Original dynamic `getFaceOcclusionShape` owner/consumer/cache eligibility vs Planet pair-key lacking world/pos/BE; do not claim piston collision proves stale face occlusion; establish safe invalidation/canonical-side contract or tested guard | `3R-7F-3` |
| **G04 — indirect non-block roots** | `A25` | Original 9430-class/543-edge complete *discovered* consumer scan has unselected/virtual/reflection roots; finite classify only roots that can mutate/read Planet block-facing geometry or support, record explicit out-of-scope source owners | `3R-7F-3` |
| **G05 — Flywheel1.0.6 exact backend** | `D10,D11,E20` | Create 6.0.10 source uses `VisualizationManager`, `ROTATING`, `TRANSFORMED`; need **exact published** Flywheel 1.0.6 binary/source version, `VisualizationManagerImpl`/VisualStorage, shader matrix/normals and resource/instance lifecycle or defensible versioned fallback contract. Source 1.21.1/dev branch is only comparator | `3R-7F-2` |
| **G06 — reload/occlusion graph late work** | `E03,E10` | Full **relevant** original ModelManager/ResourceManager listener and SectionOcclusionGraph.waitAndReset/late RebuildTask callback/mesh upload source sequence; custom mod reload hooks classified. No claim global race solved just because selected method exists | `3R-7F-4` |

**Optional but explicit separate claim gate G07** `D14,E22`: Iris 1.8.12 + Sodium0.6.13 NeoForge1.21.1 is published version metadata, not original source/shader render-pass ABI; source-selected shader normal/GBuffer/light/consumer acceptance is **UNVERIFIED**. Decide `3R-7F-5` if Planet is going to advertise supported Iris/shaderpack integration. It must not silently inherit vanilla render PASS or be reported as supported. If supporting non-shaderpack core client first, record **optional shader integration unsupported/not accepted** rather than blocking every Phase2 baseline feature indefinitely.

**Other 18 historical rows:** update or link to later original-source proof; do not re-run identical 2B/2L/3B/4C/5/6 packets solely because old status says `PRIOR`/`OPEN`. **46 additional rows** still tagged `SELECTED_SOURCE_NEEDS_FINAL_GUARD`: the final `3R-7G` must classify each as genuinely R-satisfied by a cited later source **or** move it to a narrow blocker; no bulk status promotion based on regex.

## 3/4 — Source lineage of accelerated mod and consequences for architecture

**Create release pin:** `mc1.21.1-6.0.10` @ `ac0c444d9828da3453ae8cc65338e8de063286fb`, built against NeoForge21.1.219, Planet uses NeoForge21.1.215; Flywheel1.0.6 and Ponder1.0.82 declared, actually loaded Create/AE2 mod client **OPEN**. The exact Flywheel release JAR, source JAR and SHA sidecars exist at [official Create Maven Flywheel1.0.6 index](https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/1.0.6/), but its compiled **bytes have not been downloaded/hashed**, so `mod visual API -> backend shader` remains **R source blocker** (not game test).

**Useful Flywheel original *branch comparator*, NOT released 1.0.6:** [Engine-Room/Flywheel `1.21.1/dev` revision `9189ad50d1b7bf59873043ebf06c21894b551573`](https://github.com/Engine-Room/Flywheel/tree/9189ad50d1b7bf59873043ebf06c21894b551573) contains:
- `VisualizationManager` API and `VisualizationManagerImpl.supportsVisualization`, `reset`/`resetAll`, `beginFrame`/`render`/`delete`, plus `VisualManagerImpl` internal ConcurrentLinkedQueue transaction drain and storage invalidate.
- `InstancedDrawManager` and `IndirectDrawManager` own separate GPU drawing/upload paths; indirect manager `delete` also deletes staging buffers, light/matrix buffers, meshPool and OIT framebuffer. These are **specific potential source owners**, not accepted 1.0.6 ABI.
- `ShaderLightVisual` and `VisualizationLevel` distinguish their own visuals/foreign levels. Do NOT attach a universal Planet `PoseStack` or world `Level` wrapper as a blind fix.

**Original NeoForge bypass:** AddSectionGeometryEvent client producer `Level` can be foreign; worker callback `SectionRenderingContext.getRegion` BlockAndTintGetter and chunk-origin pose are separate. Original BER provider replacement occurs per BlockEntityType on resource reload. AE2 19.2.18 owns independent BER and BakedModel/Facade, not inherited vanilla Sign/Chest mixins. This is why the Phase2 architectural fix must be shared at correct author/consumer seam with graceful noPlanet/noLevel behavior, not “global all BER”.

**Do not mix gates:** R requires the relevant **source/method owner/dispatch/coordinate classification** and a reasoned extension contract; V later requires **real loaded modules** and applied Minecraft 6 faces/12 seams/8 corners, naturally generated LIME vs forced CYAN signs/chains, +Y/noPlanet, GUI/foreign world, model reload, shader on/off when advertised.

## 4/4 — Verified gate verdict, regression guardrail and persistence

### Verified original CI for the research **audit**, not original Minecraft method-body research

The strict ledger integrity JUnit source commit [`d131949`](https://github.com/Abbygree11/Planetary/commit/d13194959a7a61f599368121e2061256f731a327) completed **[GitHub Actions #40771099541 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40771099541)**: Java21 production and test compilation and full JUnit PASS. The published original Actions archive **artifact ID `12853056270`** was downloaded and independently SHA-256 verified: complete ZIP SHA256 **`62cd8db21f3292732a6ae8e388e5a7d06de7d1800ed02e0dc2a2beb8542b4a7d`**. The two real `3R-7` stage members were verified independently:

| Report member | SHA256 |
| --- | --- |
| `phase3-r7-whole-phase-owner-gate-summary.txt` | `102608ea9239b28e3761fb3d30d4b20447625b09a0d9358e488b48eed191e240` |
| `phase3-r7-blocking-source-groups.tsv` | `8bd9eee72477b9e32ee3643e4b414788702703da6276a9da2bbf7e7d77fa8ef0` |

The actual CI result includes the **151 original owners and 151 matching dispositions**, phase counts **41/25/48/15/22**, six blocker clusters with **11 exact owner IDs**, **2 optional shader-source**, **18 historical states**, **74 accepted source-but-runtime-pending dispositions** and **46 final source guard reviews**. JUnit additionally records original ledger SHA256 **`b6ea163a9b996f5873112f9a80a433db2cf18ae354996baa9cda8367849124aa`** and 151-line new disposition matrix SHA256 **`696ab69681278c832b8b9840f6ecd68d197de766b1b7c083cec2440e180a10a9`**.

[Pinned stand-alone archive verifier](../../scripts/research/phase3_r7_owner_gate_verify.py) committed *after* the source test CI in [`1161279`](https://github.com/Abbygree11/Planetary/commit/11612798d4614cca3e34bfcbb3e44b17813013d7). The archive itself was independently downloaded and checked in this packet; **do not claim** the separately committed checker executed as a step inside the previous original workflow. This JUnit verifies **doc/ledger evidence integrity**, not original target JNI/NeoForge classfiles, actual Minecraft client, loaded Create/Flywheel or shader compatibility. Previous 3R-5/6 original target classfile proof remains distinct. **R OPEN and V OPEN.**



**Gate verdict from this audit: `R_OPEN = 11 named source blockers across G01–G06 + 2 conditional shader/optional claim entries + 46 source guard rows still individually requiring 3R-7G disposition`; `V_OPEN`.** This audit **does not itself modify a rendering behavior**; it protects Phase2 against a repeat of the earlier premature sign/chain/seam patch. No fake claim of all mod classes loaded or virtual dispatch being fully enumerated. Existing original 2L/2M/3B1–3/4A–D/5/6 CI links and published hashes are retained in individual research reports; numbers of classfiles/methods across stages **overlap**, cannot sum to a unique whole-minecraft class count.

**Research integrity test** [`Phase3R7WholePhaseOwnerGateAuditTest.java`](../../src/test/java/dev/planetary/world/Phase3R7WholePhaseOwnerGateAuditTest.java) asserted exact old/new ledger integrity, one-to-one 151 rows, unique per-stage IDs, columns/non-empty evidence, 41/25/48/15/22 counts, six expected blocker clusters, optional G07 separate, archived detailed research source files present, and generate a deterministic research-gate snapshot under `build/reports/planetary` for GitHub artifact. **A successful audit test means matrix integrity only, NOT original Minecraft ABI/game PASS.** Do not present JUnit integrity test as testing Flywheel1.0.6/GPU.

**Verified CI is pinned above. Research source disposition completion also requires updating [R gate](PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md), [Phase2 visual gate](PHASE2_VISUAL_READINESS_GATE_1_21_1.md), [Phase3 results index](../phases/phase-03/results/README.md) and 3A–3E outcome cards as needed, Phase3 plan, AI_CONTEXT, IMPLEMENTATION_PLAN and `SESSION_HANDOFF` with **next `3R-7F-1`**. At each source packet, eliminate named G0x blockers and **revalidate** statuses in 151-row matrix, then **3R-7G** decision gate; do not advance to Phase2 source modifications until R genuinely PASS.

**No production Java, Mixin, V8 lab, Minecraft save/world, or main branch modified in `3R-7`.** Bounded **3R-7 4/4 audit PASS** on JUnit/archived ledger integrity, but whole **source gate R OPEN**, game visual **V OPEN**. Do not mislabel this as original mod ABI or gameplay proof.
