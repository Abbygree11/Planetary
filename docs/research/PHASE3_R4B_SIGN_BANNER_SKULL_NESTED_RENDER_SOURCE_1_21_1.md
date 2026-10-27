# Planetary — `3R-4B`: original Sign/HangingSign/Banner/Skull BER nested render and local-frame owners

**Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21 · branch `2.0` · RESEARCH ONLY.** Four bounded research parts: source and exact JVM class owners, inherited/multistage render call paths, Planet/NeoForge/mod alternative route matrix, CI evidence and phase-2 readiness handoff. Neither destination Phase3 research gate **R** nor real visual game gate **V** is closed by these tests. No production Java, Mixins, V8, world or `main` edits.

## 1/4 — Reuse exact registry `3R-4A` and trace declaring original method bodies

The previous [`3R-4A` complete original registry audit](PHASE3_R4A_ORIGINAL_VANILLA_BER_REGISTRY_AND_OWNER_1_21_1.md) [CI #40436274761](https://github.com/Abbygree11/Planetary/actions/runs/40436274761) proved **24 registered BE types / 22 distinct original vanilla BER factory classes / 28 classfiles** from original `BlockEntityRenderers.<clinit>` GETSTATIC/INVOKEDYNAMIC/reg calls. `SIGN` and `HANGING_SIGN` map respectively to `SignRenderer` and `HangingSignRenderer extends SignRenderer`, `BANNER` to `BannerRenderer` and `SKULL` to `SkullBlockRenderer`. The 22-owner registry is NOT a runtime assertion that NeoForge mods cannot replace any provider.

New [`Phase3R4BOriginalSignBannerSkullMethodBodyAsmTest`](../../src/test/java/dev/planetary/world/Phase3R4BOriginalSignBannerSkullMethodBodyAsmTest.java) captures SHA256 of actual target original compiled classfiles, direct superclasses/interfaces, all declared JVM methods/fields, and selected **ordered JVM opcodes** (method/field call owners, labels, loaded constants, branches, invokedynamic) from four BER, nested `SignModel`/`HangingSignModel`, their related BlockEntities/State blocks and model/buffer owners. It also verifies the inherited contract for front/back text, hanging chains, banner patterns and skull profile delegation. Selected methods only, not a complete BER/NeoForge or mod loader scan and not an applied Mixin. Original output reports are produced in `build/reports/planetary/phase3-r4b-original-ber-*` for independent artifact verification.

CI evidentiary history: [#40496409146](https://github.com/Abbygree11/Planetary/actions/runs/40496409146) **FAIL at research test compilation**, due typo accessor `Instruction.operand()` for a record defining `target()`; the game/runtime didn't run and was not a cause. Corrected in [`e57e985`](https://github.com/Abbygree11/Planetary/commit/e57e9859f83564e7029f64c57b5ac6b36c1d6307). **Corrected CI and output counts must be verified before replacing PENDING below**.

## 2/4 — Rendering ownership: correct BlockState != correct nested local model frame

Based on pinned original 1.21.1 Java source comparator and previously verified `3R-4A` target registry/inheritance, with exact JVM source-body test above verifying declared paths:

| Actual vanilla owner | Independent original responsibility | What Phase2 must not conflate |
| --- | --- | --- |
| `SignRenderer.render` | reads state/wood model and calls `renderSignWithText` | `BlockState` yaw/attachment is written by Phase2 placement, renderer owns visual pose |
| `SignRenderer.renderSignWithText` | calls `translateSign`, `renderSign`, and twice `renderSignText` for front/back SignText | a single rigid outer frame should be applied once to both sign board and both text sides; independent text transforms must stay inside |
| `HangingSignRenderer.render` | calls inherited `renderSignWithText` | subclass is a separate factory, not a new independent text-coordinate system |
| `HangingSignRenderer$HangingSignModel.evaluateVisibleParts` | uses `BlockStateProperties.ATTACHED` to show chain/hinge model parts | ceiling/wall hanging sign supports and visually present chain pieces are related but not the same state/consumer |
| `BannerRenderer.render` | transforms banner state and calls `renderPatterns` with cloth and pattern model | Banner `ROTATION_16` vs wall `FACING`, layers, texture and cloth animation are separate nested owners |
| `SkullBlockRenderer.render` | obtains `SkullBlockEntity.getOwnerProfile`, selects render type and invokes `renderSkull` | wall/standing `Direction`, player profile and skin are not all determined by block-state rotation |
| `SkullBlockRenderer.renderSkull` | static helper receives wall direction, yaw, buffer, model and texture RenderType | GUI/preview calling static helper must not inherit a world gravity frame from a global BER wrapper |

`PlanetLocalBlockEntityRender` uses a selected world-attached `PoseStack` LIFO scope at BER `HEAD` / `RETURN` for the four original BER types only, separate from the existing `BedRendererGravityMixin`. This is *provisional source code*, with selected original ABI/JUnit compilation established in the prior `3C-1`, **not** a measured installed client pass. A render callback that throws before `RETURN` could theoretically leave a stale scope; that is a source-level exception-safety gap and not proof of an actual crashing or visibly crooked game state. Global `BlockEntityRenderDispatcher` wrapping is deliberately rejected because `3R-4A` found world/beam and camera effects (e.g. portal/gateway and beacon).

**Mod/NeoForge bypass:** actual `EntityRenderersEvent.RegisterRenderers.registerBlockEntityRenderer` can replace a factory for an existing BlockEntityType. A Mixin woven into the vanilla `SignRenderer` class says nothing conclusive about an independently supplied modded replacement BER or FakeLevel render path. We need downstream `3R-4D` and `3R-5` ownership/loader tests. Don't promise universal skins, banners and modded signs from vanilla test coverage.

## 3/4 — Family-owner and real-game acceptance matrix

[35 classified routes](PHASE3_R4B_SIGN_HANGING_BANNER_SKULL_BER_OWNER_MATRIX_1_21_1.tsv) enumerate: state author vs renderer, text front/back separately, original wall vs ceiling hanging sign `ATTACHED`/chains, banner layers, skull `ResolvableProfile`/RenderType and nested render, selected four Planet Mixins and separate Bed owner, exception scope, NeoForge registration override, owner-of-Level and client resource reload, +Y/noPlanet plus six-face/12-seam/8-corner game gates.

**Mandatory visual differentiation**, before claiming a Phase2 fix: (a) log real naturally player-placed LIME and worldgen `BlockState` including `ROTATION_16`/`FACING`/`ATTACHED` vs reference CYAN forced state; (b) test ordinary +Y/noPlanet without special transforms; (c) compare board/cloth/head, front and back text, chain visibility and attachments with the physically expected face; (d) repeat on six face interiors and seam/edge/corner contexts where the family can actually be placed; (e) verify Mixin weaving and client live game rather than interpreting static JUnit success as proof. Check representative real loaded mod BE replacement separately or mark as untested.

## 4/4 — Actual CI results / handoff, open gates

**PENDING** — insert exact corrected Java21 CI run status, original report counts and SHA256 of downloaded artifact after the rerun completes; never copy values from previous 3R-4A registry or an untested source comparator.

This `3R-4B` is a selected *family source-body* audit, not all registered 22 BER method bodies. Continue bounded **`3R-4C`** all remaining vanilla BER owner/animation/physical-space splits (chest/ender/trapped, bed/shulker/bell/lectern/books, dynamic piston/spawner, beacon/conduit/portal/gateway, decorated pot/brushable/trial/vault etc.), then **`3R-4D`** NeoForge actual registered replacement and mod renderer ownership. Only then `3R-5` alternate mod renderers, `3R-6` source lifecycle, `3R-7` whole Phase3 research gate **R** and minimum shared Phase2 runtime fixes / actual game **V**. Stalled `2.4A-10` chain, sign and seam gameplay are still NOT accepted. No runtime rendering change was authorized or made in this packet.
