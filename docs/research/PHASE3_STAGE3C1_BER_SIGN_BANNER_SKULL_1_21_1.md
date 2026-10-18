# Phase 3C-1 — Rigid block-local BER family: signs, hanging signs, banners, skulls

**2026-10-18 · Planetary branch `2.0` · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.**

## 1/4 — Original declaring owner and render pipeline evidence

User V8 screenshots revealed questionable world orientation of standing signs, banners, skulls/heads and ceiling hanging signs, including visually different chain shapes between CYAN preset and naturally placed blocks. **Do not confuse F3 property snapshots of manually preset CYAN references with proof that actual LIME player placement is correct**; equal `rotation`/ `attached` across two physically equivalent states is the necessary visual comparison. The previous assistant's unconditional “state PASS, render FAIL” was too strong. Valid state selection and correct renderer orientation remain independent gates. In particular, `attached=true` and `attached=false` deliberately select different chain variants in vanilla; mismatch alone is not a bug.

Pinned comparative source (contains OptiFine modifications; method ABI is independently verified on the real NeoForge Gradle classpath): `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`. Relevant upstream owners:
- [SignRenderer](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/client/renderer/blockentity/SignRenderer.java): `render(SignBlockEntity,float,PoseStack,MultiBufferSource,int,int)` owns standing and wall variants, handles two-sided text, model offset, local Y rotation and sign text transforms.
- [HangingSignRenderer](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/client/renderer/blockentity/HangingSignRenderer.java): overrides render for its own block family, reuses SignRenderer's `renderSignWithText`, chooses visible chain/plank parts from `ATTACHED` and retains local `rotation`. Its concrete render method is separate, so wrapping the superclass `render` alone does not cover it.
- [BannerRenderer](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/client/renderer/blockentity/BannerRenderer.java): standing + wall variants, banner pole/cloth layers/pattern materials, local-yaw and flag animation.
- [SkullBlockRenderer](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/client/renderer/blockentity/SkullBlockRenderer.java): standing + wall skull variants; skin/animation and local placement offset. Shared `renderSkull` is also used in contexts with NO physical block frame; only world-attached instance rendering may rotate.
- [BlockEntityRenderDispatcher](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher.java) chooses registered `BlockEntityRenderer` per BE type and handles separate `renderItem`. It is **NOT** an appropriate global rotation owner: beacon, portal, conduit, camera/world effects and mod custom BERs can have independent physical direction.
- NeoForge source/portability background: [C13 alternate renderer and shader boundaries](PHASE2_STAGE3C13_ALTERNATE_BER_FLUID_INSTANCING_SHADERS_ASM_1_21_1.md) and [28-BER R2 inventory](CLIENT_RENDER_PARTICLE_MATRIX_1_21_1.md).

## 2/4 — Deepest safe shared adaptation (one algorithm family)

`PlanetLocalBlockEntityRender` is a **single opt-in geometric frame owner** called by four thin concrete `@Mixin` render-method HEAD/RETURN adapters:
`SignRendererPlanetFrameMixin`, `HangingSignRendererPlanetFrameMixin`, `BannerRendererPlanetFrameMixin` and `SkullBlockRendererPlanetFrameMixin`. Each retains original rendering, shader buffers, light inputs, text, pattern layers, textures, local Y yaw, BE state and render registration. Only the caller's **outer block-center pose** changes: canonical local x/y/z -> physical frame basis `PlanetRenderTransform.applyAroundBlockCenter`. Existing generic BakedModel geometry is NOT used by these BER models.

The helper reads the **actual world-attached BlockEntity Level + physical BlockPos**, resolves `PlanetClientRenderFrame.stateFrameAt`, and does nothing for absent Level/item/GUI preview, no-Planet or identity `POS_Y`. A thread-local nested LIFO scopes a single **entry-time snapshot** of whether a pose was pushed, so asynchronous/mod callback changes to the BE during rendering cannot desynchronize `pushPose/popPose`. Scope removed after final return to avoid leaked references. Do not globally rotate `BlockEntityRenderDispatcher` or `PoseStack`. Existing `BedRendererGravityMixin` remains an independently adapted owner, not wrapped twice.

**Matrix test contract:** across all six face centers, world physical block center remains unchanged and the local-up test endpoint becomes the exact physical frame UP; after renderer's finish, incoming pose is bitwise/epsilon identical. Plus nested pass-through and a mocked BE whose Level changes during rendering. Exact classfile tests verify **four separate loaded NeoForge declaring render methods**, compiled two callbacks per owner, registration and the existing independent bed callback. Applied Mixin startup is **not** proven by JUnit.

## 3/4 — Mod extension classification and bypasses

| Category | What is covered | What remains open |
|---|---|---|
| SHARED_SAFE | Reusable `PlanetRenderTransform`, canonical physical block frame, pose center, inherited vanilla sign/hanging/banner/skull class renderer logic | Arbitrary world/camera-space BER must not use this helper |
| OPT_IN_ALGORITHM | Four exact declaring vanilla renderer methods, including wall/standing variants routed through them | Mod subclass overriding method and **not calling the hooked vanilla implementation**, injected render bypasses |
| MOD_INDEPENDENT | No claim without loaded JAR: NeoForge `RegisterRenderers` replaces a BER, own buffer/matrix, custom/animated BER, BEWLR without real world, fake Level | Create/Flywheel instance rendering, Iris/other shaderpacks, mod-specific BE geometry and independent chains |
| PHYSICAL_DO_NOT_TOUCH | Level light, chunks, model-state `rotation`/`attached`, SignText/Pattern/skin/NBT, physical `BlockPos`, raw redstone, actual fluid world | Phase 3B AO/shadows and Phase 3D accelerated renderer/shader paths |

Do **not** patch specific oak/cherry/white block IDs as a substitute for these author paths. Source inheritance of a renderer is **not** a loaded-mod compatibility PASS.

## 4/4 — Acceptance evidence / what to do next

**Code commit [ea04238](https://github.com/Abbygree11/Planetary/commit/ea0423894e2c1e92a5d74e81c87543be48bd9453), [CI #39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) COMPLETE SUCCESS** (Java 21 compilation, JUnit/ASM, registry census). Earlier baseline [CI #39080992468](https://github.com/Abbygree11/Planetary/actions/runs/39080992468) SUCCESS before entry-scope snapshot enhancement. None proves actual woven client or world screenshot.

**Game / mod / renderer integration still PENDING**: re-use **unchanged V8 lab (42 stations/face)**; no rebuild version bump, no new wing. Compare identical F3 state values between preset CYAN and player LIME on +Y and normal no-Planet world (including both `attached=true` and `attached=false` for ceiling signs). For all six Planet faces compare stand-up axis, sign text front/back/readable direction, banner pole/cloth/pattern sway, skull skin orientation/animation and hanging chains+plank and actual support. Check local ceilings on +/-X,+/-Z,-Y, source seam 12 edges/corners 8, block-breaking/reload/chunk rebuilding and modded registered BER where loaded. Non-Planet worlds, inventory/held item GUI and vanilla independent renderers must remain unchanged.

**Phase 3.3 not closed:** this packet adapts four BER render paths only. Chest/EnderChest/Enchanting book and physically/world/camera-based families remain their own research+implementation packets. Any residual bad `+Y` reference display must be diagnosed separately; this adapter is identity on +Y and cannot repair that by design.
