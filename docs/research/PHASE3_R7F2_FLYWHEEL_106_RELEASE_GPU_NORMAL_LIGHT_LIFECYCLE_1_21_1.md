# Planetary — 3R-7F-2: exact Flywheel NeoForge 1.21.1 release 1.0.6 GPU/backend owners

**2026-10-29 · branch 2.0 · Minecraft 1.21.1 · target NeoForge 21.1.215 · Java 21 · research only.**
**Research packet: 4/4 bounded SOURCE/ABI PASS. Whole Phase 3 gate R = OPEN; live-game visual gate V = OPEN.**
Production Java, Mixins, world fixtures, branch main: **not modified**.

## 1/4 — Official exact binary and source artifact pinned, no branch-comparator substitution

[Official Maven publication](https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/1.0.6/) was downloaded by [research CI #40776433970](https://github.com/Abbygree11/Planetary/actions/runs/40776433970) at code commit [97453d8](https://github.com/Abbygree11/Planetary/commit/97453d8aa1cc41688bb7f45d9c13cb30c0ecaa98). Both complete original published JAR byte streams were checked against their separate official Maven .sha256 sidecars, not guessed from the release name. Binary SHA256: **31dda15c205eb596d3b3449ef03f6af7363a6cd35b3da4bfe916b304f9e5337e**. Source SHA256: **c251479dea729a568bedee28bb68021276f4b49be0a5b6a9603501729d5dfbfe**. Original binary JAR **726 entries**, source JAR **606 entries**, and **11 selected original classfiles matched to their released Java source owners**. Java21 javap -private -c -s captured actual original bytecode signatures and call instructions. **Three original published GLSL resources** were independently extracted. These figures come from CI runtime logs and downloaded artifact, not from the non-release Flywheel development branch.

[Archive artifact #12854533682](https://github.com/Abbygree11/Planetary/actions/runs/40776433970) contains five evidence files. Independently downloaded workflow ZIP SHA256: **15604005ca9dc7bfe72de3802020a4cf619621c2048d66d42af8942119c3dded**.

| Artifact in CI ZIP | Independent SHA256 |
| --- | --- |
| summary.json | a1f77963525160abf37e30e93325a728946f5bb2d8975b494ff01c3ba128d032 |
| selected-entries.tsv | e3895114cb586c88b349bbe7877e3c0737c18247664e745fb0d40e43e1a0228d |
| source-owner-traces.tsv | 28bb3382c408f786e44d5d1b4d3cdda6fe98db93465eefc8118041b0e685babc |
| shader-traces.tsv | f69a17cd8a511f00c58cfff7880c2ef120f105beeb83fcd776510a073ab9141a |
| compiled-javap.txt | 6d16eab18b475b85d32a97975ddc3589a09d270389e5b0f79bad08c5728e2541 |

Reproduce using [stage script](../../scripts/phase3_r7f2_flywheel_release_probe.py) or isolated [stage workflow](../../.github/workflows/phase3-r7f2.yml). The script asserts actual class/source/shader entry existence and checks archive hashes; the recorded JVM/GLSL trace was then inspected independently. No automated assertion pretends to prove **rendered output** or real OpenGL execution.

## 2/4 — Exact released GPU instance transforms and normal-space split

The original released Java sources and original compiled classfiles prove separate owners:
- InstanceTypes defines TRANSFORMED with pose 4x4; POSED with pose 4x4 **and** normal 3x3; ORIENTED with position, pivot and quaternion. In the actual compiled PosedInstance there are distinct fields pose:Matrix4f and normal:Matrix3f, and setTransform(Matrix4fc, Matrix3fc).
- The **released** shader instance/transformed.vert explicitly writes vertex position with i.pose and vertex normal via transpose(inverse(i.pose)) upper 3x3. The **released** instance/posed.vert uses separate i.normal to transform normals; instance/oriented.vert rotates both position and normal with the quaternion but handles pivot/translation only for position. Thus one blanket pose-matrix injection is insufficient and can produce incorrect lighting/normal directions.
- Source/compiled InstanceTypes selection binds corresponding type-specific shader and memory layout. InstancedDrawManager and IndirectDrawManager are separate submission owners: shader data must be consistent with the selected GPU instance type, not patched only through BakedModel quads or BlockEntityRenderer. Do not remap all physical normals or GPU shaders globally.

**Frame rule for Planetary:** original model author coordinates, Planets local gravity chart, physical XYZ mesh, camera-relative renderOrigin, and GPU normal representation remain separate. The right Planet-local orientation must become a correct physical pose/normal before the Flywheel GPU consumer, on a level/visual-specific boundary. This is an architecture constraint for future implementation, not implementation now.

## 3/4 — Real release level identity, async queues, GPU light and resource lifetime

Original 1.0.6 compiled-and-source selected owners:
- VisualizationManagerImpl is per LevelAccessor through LevelAttached, supports nonstandard VisualizationLevel, reads camera and engine renderOrigin, constructs frame relative viewProjection, and has reset/resetAll/delete as separate lifecycle operations.
- VisualManagerImpl has ConcurrentLinkedQueue transactions queueAdd/queueRemove/queueUpdate; processQueue drains them in frame/tick plans, with storage invalidate and GPU light tracking. It must not be treated as direct single-threaded BER render calls.
- InstancedDrawManager render handles mesh and instance uploads, light flush and ordinary/OIT passes and has delete teardown of instance buffers/VAO/mesh/OIT/light. IndirectDrawManager render also submits matrixBuffer.flush and lightBuffers.flush via StagingBuffer with separate indirect draw/cull resources; MatrixBuffer and StagingBuffer have independent deletion and pending transfer/fence handling.
- LightStorage holds LevelAccessor and physical section light updates, including neighboring sections; this does not automatically give its data Planet local-gravity lighting semantics.

**Lifecycle implication:** a level switch/reload or fake/virtual world must not silently carry an outdated Planet chart/renderOrigin into a Visual queued for GPU upload; epoch/invalidation and correct level source must be explicit. These are observed *risk boundaries*. Neither a static JVM inspect nor release-source trace reproduces an actual queue race, invalid GL handle, or visual artifact.

## 4/4 — Create integration, G05 disposition, runtime gate distinction

Pinned [Create 6.0.10 source commit ac0c444](https://github.com/Creators-of-Create/Create/tree/ac0c444d9828da3453ae8cc65338e8de063286fb) and [3R-5 findings](PHASE3_R5_INDEPENDENT_SECTION_GEOMETRY_CREATE_FLYWHEEL_SHADER_SOURCE_1_21_1.md) establish the caller side: SingleAxisRotatingVisual/OrientedRotatingVisual ROTATING, FlywheelVisual ROTATING/TRANSFORMED, independently moving/embedded ContraptionVisual and a conditional CPU KineticBlockEntityRenderer fallback. **Do not render both fallback and accelerated visuals simultaneously**. Original released Flywheel bytecode+source+GLSL now fill the previously missing backend-source proof for D10, D11, E20, so bounded G05 source-research can close. This does **not** establish a runtime visual success.

- **D10** Create visual instance-to-release backend ownership: bounded original binary/source checked; **V OPEN**.
- **D11** type-specific released pose/normal shader and instanced/indirect owners: bounded original binary+GLSL checked; **V OPEN**.
- **E20** original manager/queue/reload/light buffer lifetime entry owners: bounded original binary/source checked; **V OPEN**.

Important **unresolved compatibility**: Create 6.0.10 pinned source developed against NeoForge 21.1.219, whereas Planetary remains 21.1.215. No actual loader-test of this exact combined setup; no loaded mod, OpenGL, Create contraption, Ponder virtual world, real 6 faces/12 edges/8 corners, shader packs or client game acceptance. The G05 SOURCE research is bounded, but **not every concrete Flywheel/mod addon implementation is inventoried**; new independent owners can reopen the gate.

The historical [3R-7 151-row audit](PHASE3_R7_ALL_151_OWNER_SOURCE_GATE_DISPOSITION_1_21_1.tsv) and [3R-7F-1 effective 151-row snapshot](PHASE3_R7F1_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) remain immutable. The [new effective 151-row gate](PHASE3_R7F2_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv) changes only D10/D11/E20, with independent [three-row gate delta](PHASE3_R7F2_SOURCE_GATE_DELTA_1_21_1.tsv). **Source blockers: 9 -> 6**, remaining G03 A17/A35/B11, G04 A25, G06 E03/E10. **Full R OPEN** until these + 46 guard rows are reconciled and optional shader G07 decision is explicit. **Full V OPEN**, Phase2 2.4A-10 work paused. Next **3R-7F-3** (G03 dynamic face occlusion and G04 non-block virtual callers), then 7F-4 (G06), possibly 7F-5 (Iris), 7G 151-row final gate.
