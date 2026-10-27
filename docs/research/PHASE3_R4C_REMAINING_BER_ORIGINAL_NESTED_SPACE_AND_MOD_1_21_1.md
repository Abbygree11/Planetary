# Planetary — `3R-4C`: remaining 18 vanilla BER original method owners, nested frame and world-space audit

**Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21 · branch `2.0` · RESEARCH ONLY.** Four bounded parts: all remaining compiled renderer owners, actual nested/physical/camera source call paths, independent NeoForge/mod/visual gates, authoritative CI/archive verification and handoff. Not an installed Minecraft client/gameplay proof; Phase3 research R and Phase2 visual V remain **OPEN**. No production Java, Mixins, V8 lab or `main` edits.

## 1/4 — All actual vanilla factory owners, without confusing types and classes

Prior [`3R-4A`](PHASE3_R4A_ORIGINAL_VANILLA_BER_REGISTRY_AND_OWNER_1_21_1.md) original `BlockEntityRenderers.<clinit>` reflection/ASM in successful [CI #40436274761](https://github.com/Abbygree11/Planetary/actions/runs/40436274761) established **24 registered BE types / 22 distinct BER classes**. The [`3R-4B` original JVM nested trace](PHASE3_R4B_SIGN_BANNER_SKULL_NESTED_RENDER_SOURCE_1_21_1.md) independently closed selected SignRenderer/HangingSignRenderer/BannerRenderer/SkullBlockRenderer family bodies ([CI #40497581542 SUCCESS](https://github.com/Abbygree11/Planetary/actions/runs/40497581542)). Thus **22 minus 4 = 18** remaining distinct factory owners; CHEST/ENDER_CHEST/TRAPPED_CHEST share one ChestRenderer but must each have an individual runtime regression.

New [`Phase3R4CRemainingBerOriginalMethodOwnerAsmTest`](../../src/test/java/dev/planetary/world/Phase3R4CRemainingBerOriginalMethodOwnerAsmTest.java) in [commit `fc1340c`](https://github.com/Abbygree11/Planetary/commit/fc1340c47f1950eef769b7f2c276ed4327b301fe) reads classfile bytes directly from the NeoForge 21.1.215 Java21 test classpath. For **all 18** expected owners it records classfile SHA256, direct superclass/interfaces, every declared method, all direct method calls, ordered opcodes for selected `render*`/helper bodies; asserts all 18 classes exist, all declare render, and selected nested/space edges for Bed, Piston, Gateway, Campfire, Chest. Intended output in `build/reports/planetary/`: `phase3-r4c-remaining-ber-{owners,methods,invokes,instructions}.tsv` and `phase3-r4c-remaining-ber-summary.txt`. These are **selected traced method bodies**, NOT guaranteed all non-`render` lifecycle code or all mod-provided renderers.

[Research CI #40520342802](https://github.com/Abbygree11/Planetary/actions/runs/40520342802) was still **IN PROGRESS** when this research document was first authored; do not report PASS or original method counts/hashes until the run finishes and the actual archive is checked. An ABI assertion failure must be corrected from real original evidence, not by loosening it to silently pass.

## 2/4 — Nested render authors; choose frame from each actual consumer

Pinned 1.21.1 **non-authoritative comparator** source: [OptiFine-Source@b77c5c6995, original-like renderers](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/client/renderer/blockentity). Because this source is OptiFine-modified, only `3R-4C` actual target original JVM artifacts can establish the NeoForge method owners. Comparator observations here are hypotheses/selection criteria until checked against original traces.

| Original vanilla factory owner | Declared nested source/consumer path observed in pinned 1.21.1 comparator | Frame classification **candidate**, not implemented |
| --- | --- | --- |
| `SpawnerRenderer`, `TrialSpawnerRenderer` | `EntityRenderDispatcher`, preview entity/spin inside BE; common `renderEntityInSpawner` helper | mixed BE pose + entity/camera owner; do not global-wrap EntityRenderer |
| `PistonHeadRenderer` | `getMovedState`, `getXOff/getYOff/getZOff` at partial tick, `ModelBlockRenderer.tesselateBlock` under renderBlock with real `Level`/`BlockPos`; cache enable/clear | movement/physical state/ModelData and local partial translation separately; never treat as rigid static BER |
| `ChestRenderer` | BE state-facing/texture plus lid/model part; three BE types share actual factory class | selected rigid local candidate with double-chest half/texture and +Y contract |
| `EnchantTableRenderer`, `LecternRenderer` | floating/placed `BookModel`, BE book turn/open states, block FACING for lectern | animated nested book and local pose independently of book GUI |
| `TheEndPortalRenderer` | `renderCube` -> `renderFace` with `shouldRenderFace(Direction)`; special `RenderType` | mixed local cube face and shader-space visibility; do not rotate all BER dispatch |
| `TheEndGatewayRenderer` | extends portal owner; calls `BeaconRenderer.renderBeaconBeam` using Level time then `super.render` | portal face and world-height beam must not share an unexamined root transform |
| `BeaconRenderer` | time, segmented static `renderBeaconBeam`, off-screen/view-distance predicates | world-vertical beam, not automatically local-up |
| `StructureBlockRenderer` | structure bounds, `renderInvisibleBlocks`, `renderStructureVoids` physical BlockGetter queries | world debug/physical volume, not a generic rigid block |
| `ShulkerBoxRenderer` | reads Level `ShulkerBoxBlock.FACING`, `direction.getRotation`, animated lid `getProgress` | rigid local candidate, preserve six FACING values and lid animation |
| `BedRenderer` | BE state, head/foot model pieces and texture; already dedicated Planet BedRendererGravityMixin | exact double-transform veto; part seam and +Y baseline |
| `ConduitRenderer` | shell/cage/wind animated PoseStack, eye billboard uses `Camera.getYRot/getXRot` | **mixed local and camera frame in a single render**; blanket outer pose rotation could misorient the eye |
| `BellRenderer` | clickDirection and time determine xRot/zRot `ModelPart` swing | local body candidate, collision/hit side physical vs animation local |
| `CampfireRenderer` | block FACING, `getItems`, `ItemRenderer.renderStatic(FIXED,...Level,...)` | item subpass attached to block; particles/world flame separate |
| `BrushableBlockRenderer` | `DUSTED`, `getHitDirection`, hit-face position-relative physical `LevelRenderer.getLightColor`, nested `ItemRenderer.renderStatic` | **mixed physical lighting/face and local item pose**; never remap physical light lookup |
| `DecoratedPotRenderer` | pot `getDirection`, wobble tick/time, front/back/left/right sherd/material sides | rigid local candidate with animation and pattern orientation |
| `VaultRenderer` | display ItemStack, spin, static `renderItemInside` -> `ItemEntityRenderer.renderMultipleFromCount` | nested item helper can be used outside world BE; opt-in world-only if needed |

**Consequence for Phase2:** A single global `BlockEntityRenderDispatcher` gravitational frame is unsound: even a single vanilla owner (`ConduitRenderer`) mixes local animation with a camera-facing eye; the gateway mixes portal body and beam, BrushableBlock mixes item geometry with physical neighboring-block light, piston mixes static BakedModel and physical moving BE state. Renderer-local opt-in must preserve each nested source's coordinate domain. `BlockState` writer correctness does not prove BER and text/item/model correctness.

## 3/4 — Coverage, bypass and acceptance, not per-block patches

[33-route tabular ownership/space/extension matrix](PHASE3_R4C_REMAINING_BER_OWNER_AND_SPACE_MATRIX_1_21_1.tsv) covers **every registered 24 BE type** (including the shared chest factory) plus NeoForge renderer provider override, independently authored Create/Flywheel/Ponder and AE2 classes, world/GUI/BEWLR, nested entity/item renderers, fake Level, lifecycle/worker, six-face/12-seam/8-corner game verification and separate gates. These routes are classifications, not evidence of running a compatible loaded mod.

The existing `3C-1` four provisional vanilla BER Mixin HEAD/RETURN scope and independent Bed adapter have **not** been proven by live client. Renderer replacement via NeoForge `EntityRenderersEvent.RegisterRenderers.registerBlockEntityRenderer` can evade that vanilla implementation entirely. Mod-custom BER and accelerated renderers belong to **`3R-4D`** and **`3R-5`**; never infer inheritance/mod runtime compatibility from the original 22 factory types.

Real-game Phase2 witnesses that remain REQUIRED: naturally placed LIME vs forced CYAN with recorded real `FACING`/`ROTATION_16`/`ATTACHED`; generated vs player-placed chain axis; old V8 stations (not new giant fixture); all legal six gravity faces, twelve edge seams and eight corners; reference normal +Y/noPlanet world; animation and GUI/BE; actually loaded compatible mod sample with real registration and renderer Mixin applied. If a configuration is invalid for a family, document why and test the nearest legal analogue rather than claim universal placement.

## 4/4 — CI and next owner gate

**STATUS ON INITIAL DOCUMENT CREATION: `3R-4C` ORIGINAL-JVM CI PENDING** in run #40520342802. Accept only after green Java21 compile+JUnit, original archive artifact independently verified and evidence counts/selected runtime callpath checks recorded. If the run fails, keep research gate open and fix the *test evidence*, not a production Mixin. Close package 4/4 only when both proof and repository handoff are written.

Next **`3R-4D`**: original target NeoForge `RegisterRenderers`/dispatcher provider replacement, renderer extension interface and actual selected **compatible-release** mod BER inheritance/call owner bypass, then `3R-5` custom/accelerated/non-vanilla rendering, `3R-6` reload/worker lifecycle and `3R-7` whole research R gate. Only after **R PASS** fix/verify minimum shared Phase2 visual runtime foundation, then **actual client V PASS**, then resume uninterrupted `2.4A`. Do not mistake green JVM test for natural gameplay visuals.

**Edit exclusions:** No production Java, Mixins, `V8`, generated worlds or `main` in this research packet.
