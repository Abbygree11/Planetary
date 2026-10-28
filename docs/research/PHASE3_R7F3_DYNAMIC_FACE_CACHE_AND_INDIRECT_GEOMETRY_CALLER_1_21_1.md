# Planetary — 3R-7F-3: dynamic face occlusion and indirect geometry caller roots

## 2026-10-29 — Verified original research CI #40778087503 and independently checked artifact

**Final bounded research unit 3R-7F-3: 4/4 source/ABI PASS, NOT gameplay.** [Test source commit ecf81e3](https://github.com/Abbygree11/Planetary/commit/ecf81e3d9f56e4624064c3ecfc6820702fb67b30) was checked by [CI #40778087503 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40778087503) on Java 21. Original artifact **12854974327** was independently downloaded; complete ZIP SHA256 **da08d03b27b55cf877932116a0d98665ed8b7257eda5ff8c94f9b9f680289de1**; 95 files in the combined workflow archive, six new 3R-7F-3 files independently hashed.

| New original artifact entry | SHA256 |
| --- | --- |
| phase3-r7f3-original-owner-sha.tsv | 46ab9e5dfc1a1bc42a25c86702f38db7e3a836ea147cfa20981f1e8f8daabfc2 |
| phase3-r7f3-dynamic-face-stale-cache.tsv | e27f623e29b16833716c865b74aaa4e76061de394a0c2de6a578105c7d568d8e |
| phase3-r7f3-original-543-root-classification.tsv | 23437531af0c160677e3f1a1ed2b36b29cb1c033198ccb66a1bc364b5cbd1de8 |
| phase3-r7f3-all-9430-invokedynamic-handles.tsv | 02808e6e7f91c51e446484c9e62b9851e97e6ffe8eb6c57ded2ead6acc0d5fda |
| phase3-r7f3-all-9430-potential-reflection-literals.tsv | 8c19404488038bde937cffdbf7dddd7a5d696908dba0aa91561d5a0b48fb6cdc |
| phase3-r7f3-summary.txt | 465079a1125944fd23caf8f9704c79c8c288d338429091d8df4dd7207ed70729 |

### G03 original dynamic face source versus the currently mocked Planet cache

The actual original Block.shouldRenderFace calls two contextual BlockState.getFaceOcclusionShape paths, and Planet currently memoizes a contextual Boolean by only the two BlockStates and source/target local sides. In a controlled state-changing mock, its first actual output is false/hidden (full faces), while after the face owner changes to empty its second actual cached output remains false even though a direct fresh query is empty and the correct answer would be true. This **does reproduce current Planet cache staleness on mocked dynamic face geometry**; unlike earlier piston tests, the mocked method here is getFaceOcclusionShape itself. No particular loaded vanilla/third-party mod has been proven to trigger it.

Source-owned safe future correction **resolved at research/design level**: avoid applying the Planet ThreadLocal pair-key cache to contextual faces; conservative first implementation removes it entirely in Planet mode and recomputes each original source/neighbor occlusion query using the true getter, positions, local frame sides and physical contact. Do not globally rewrite vanilla BlockStateBase.Cache, Level/getBlockState or Direction. When the getter provenance is a foreign/virtual/worker region, fail safe without inferring a Planet frame from an unrelated current ClientLevel; add validated producer-specific adapters only for proved source worlds. Mod-loaded visual acceptance and actual production correction remain separate future stages.

### G04 exact original root closure scope

Original complete *discovered static* **9430 classfile** scan: **543 selected direct named method calls from 247 calling source classes**, 1:1 classification into provenance/phase/ambiguous-name buckets, plus a new **whole-9430 invokedynamic and literal sweep**. **Four exact method handles** appeared in invokedynamic: 3 ClipContext$Block static initialization references to BlockStateBase.getShape/getCollisionShape/getVisualShape (Phase3A query/collision/raycast owner, should not blanket-map physical actor world), and one StructureBlockEntity.getRelatedCorners handle to Level.getBlockEntity (Phase9 structure BE lookup, not a direct geometry transform). **Zero selected exact suspicious reflection string literals** were present across all 9430 classfiles. This is a bounded original source census: dynamic computed strings, arbitrary polymorphic dispatch, transform-generated code and installed mod JARs remain outside proof; they cannot be interpreted as loaded mod success.

The 543 exact named calls were not all real shape consumers: 218 getBlockEntity context-only name hits, 23 getOffset ambiguous-descriptor name hits, plus 164 block geometry, 43 cross-phase JVM review, 26 rendering, 11 client/effects, 37 actor, 18 worldgen/chunk and 3 item authors. This classification is an ownership/phase routing inventory, **not** proof that every grouped call has the same physical/local coordinate semantics.

### Gate disposition following verified evidence

Historical 3R-7, 3R-7F-1 and 3R-7F-2 151-row matrices **remain immutable**. The new [3R-7F-3 151-row effective source gate](PHASE3_R7F3_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) and [four-row audited delta](PHASE3_R7F3_SOURCE_GATE_DELTA_1_21_1.tsv) promote **G03 A17/A35/B11 and G04 A25** only to **BOUNDED_R_SOURCE_PASS_V_OPEN** (with explicit practical fallback design), NOT fixed-code/game/client/mod proof. Remaining source blockers **6 → 2**: G06 **E03, E10** client resource reload and late-work. All **46 final source-guard** rows still need 3R-7G per-ID review; Iris G07 optional claims remain unproved. **Whole source gate R OPEN; visual gate V OPEN.** NEXT **3R-7F-4** original G06 full research, then possible 7F-5 shader scope, 7G final 151, minimal Phase3-aware patch and real game V, only then 2.4A continuation.


**2026-10-29 · branch 2.0 · Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · RESEARCH ONLY.**
**Evidence at creation:** research test commit [ecf81e3](https://github.com/Abbygree11/Planetary/commit/ecf81e3d9f56e4624064c3ecfc6820702fb67b30), [CI #40778087503](https://github.com/Abbygree11/Planetary/actions/runs/40778087503) **IN PROGRESS at report creation**. Full research gate R and visual gate V remain OPEN. No production Java/Mixins/lab/main changes.

## 1/4 — Original original 21.1.215 method/source owners

Original Minecraft Block.shouldRenderFace calls BlockState.getFaceOcclusionShape twice for source and adjacent faces. Original BlockBehaviour.BlockStateBase.getFaceOcclusionShape contains cache branch and delegates to getOcclusionShape/face extraction. BlockStateBase.initCache respects hasDynamicShape; earlier 26,684 original registered states included 179 uncached states. A foreign mod must respect its own original block registration/dynamic-shape contract. The new [original-target JUnit](../../src/test/java/dev/planetary/world/Phase3R7F3DynamicFaceAndIndirectRootsResearchTest.java) records exact compiled SHA and selected call sites. Prior selected [3R-2d](PHASE3_R2D_DYNAMIC_SHAPE_CACHE_OCCLUSION_SUPPORT_1_21_1.md), [3R-2e](PHASE3_R2E_REGISTERED_DYNAMIC_CACHE_MOD_PROVENANCE_1_21_1.md), [3R-3B-2](PHASE3_R3B2_ORIGINAL_AO_QUAD_SEAM_MOD_OWNER_1_21_1.md) provide complementary original evidence.

## 2/4 — Context-dependent mock face regression in actual current Planetary code

[PlanetBlockRenderCulling](../../src/main/java/dev/planetary/client/render/PlanetBlockRenderCulling.java) caches a Boolean with key **(source BlockState, neighboring BlockState, source local side, target local side)** and omits getter/world/position/BlockEntity/time/epoch, even though the contextual original face owners are called with the real original supplied getter and both physical positions on a cache miss. Crucially skipRendering and hidesNeighborFace execute freshly before this cache, so they are not directly memoized in the Planet cache.

The new controlled JUnit uses Mockito BlockState face results and real PlanetGravityField binding: same source/neighbor states, positions and local sides; initially full source and target faces yield hidden false. After the mock original source changes its contextual face to empty (fresh correct result visible true), a second Planetary culling invocation returns **cached hidden false**, without querying the source again. A fresh direct face query is empty. This is a source-level reproducible **mocked dynamic face stale-cache failure in the actual Planetary code**; it does NOT prove any particular original vanilla or installed Create/AE2 block changes face occlusion in this manner. Previous moving-piston collision reproduction is independent and cannot be cited as this face proof.

**Research design decision, not implementation:** Never memoize a Planet contextual face Boolean solely by BlockState pair and directions across arbitrary getters/positions/BE epochs. First safe production policy can skip the Planet-specific occlusion Boolean cache entirely until per-owner static eligibility can be proved; retain vanilla cache contracts independently. Do not globally replace BlockGetter, original directions or physical neighbors. Provenance must come from the true supplied producer; non-Level fake/region getters must not simply be substituted with a possibly unrelated current ClientLevel. Foreign mod lifecycle, 6/12/8 seams, dynamic BE, no-Planet and +Y require real game acceptance.

## 3/4 — Original 543 direct callers and broadened original 9430 indirect candidates

The research test regenerates the original 543 direct matched JVM calls across 9430 original 21.1.215 Minecraft/NeoForge classfiles through the existing [3R-2g original census](PHASE3_R2G_ENGINE_GEOMETRY_CONSUMER_AND_COMPAT_1_21_1.md), preserving each exact 9-field source/target descriptor, opcode, source area and registered ancestry and adding a named provisional phase/semantic bucket. Context-only getBlockEntity and unrelated-name getOffset are *not* automatically shape callers. Actor/AI (Phase7), worldgen (Phase9), items (Phase2), rendering/AO (3B/3E) and world shape/support (3A) have different coordinate contracts.

Unlike the prior 543 direct INVOKE census, the new test independently re-discovers all original **9430 classfiles**, examines each method's invokedynamic bootstrap handle arguments for exact geometry API names and collects suspicious string literals that could indicate reflection. The presence of a string is NOT evidence of actual reflection. A finite original static check cannot prove all virtual dispatch/overrides, dynamically computed reflection, injected transformer code or loaded mod call paths. Both the 543 edge dispositions and all-9430 indirect/literal candidates are research artifacts that require actual CI and review before changing the A25 gate.

Previously pinned release ABIs: [3R-2h](PHASE3_R2H_RELEASE_BINARY_ABI_AND_ENGINE_CALLPATH_DISPOSITION_1_21_1.md) Create6.0.10/Ponder1.0.82 virtual/wrapped worlds and AE2 19.2.18 FacadeBlockAccess; [3R-2j](PHASE3_R2J_DYNAMIC_SHAPE_AND_WORLD_PROVENANCE_1_21_1.md) getter/BE collision and [3R-2k](PHASE3_R2K_RENDER_REGION_MODELDATA_WORLD_PROVENANCE_1_21_1.md) region ModelData producer. A Level subclass is not necessarily live physical ClientLevel, and a non-Level render getter does not itself authorize a Planet frame from current ClientLevel.

## 4/4 — Research CI and gate decision

**At creation CI is pending:** do not label the 4/4 source evidence as CI PASS until this research-only Java21 run succeeds and the original artifact is independently inspected. The [3R-7F-2 effective 151 source gate](PHASE3_R7F2_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) remains current pending evidence review. Historical 3R-7, 7F-1, 7F-2 matrices are immutable. G03 = A17/A35/B11 and G04 = A25 are the targeted dispositions; G06 E03/E10 remains separate. Research R OPEN, real game V OPEN. Real installed Create/AE2 or Mixins, modded dynamic states, complete virtual dispatch, all 6 faces/12 seams/8 corners and graphical output are not asserted.

**Next:** verified and bounded G03/G04 decisions followed by 3R-7F-4 G06, optional 7F-5 Iris, 3R-7G review of all 151 IDs and 46 remaining source guards. Only then build the minimal shared Planet rendering fix and execute real loaded client V before returning to 2.4A.
