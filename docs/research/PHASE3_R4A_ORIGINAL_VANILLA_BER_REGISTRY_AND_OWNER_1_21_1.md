# Planetary — `3R-4A`: all vanilla BlockEntityRenderer registrations and inheritance/NeoForge mod override research

**Minecraft 1.21.1 / NeoForge 21.1.215 / Java21, branch `2.0`; RESEARCH ONLY (bounded 4/4).** This packet covers the *full vanilla registry factory map* and all original corresponding compiled renderer classfiles, not the entire behavioral bytecode of each family, applied Mixins, dynamically registered mod renderers or live client game acceptance. **Phase3 destination research gate R and Phase2 visual game gate V remain OPEN.**

## 1/4 — original target registry owner, all vanilla factories and method inventory

[`Phase3R4AVanillaBerRegistryAndOwnersAsmTest`](../../src/test/java/dev/planetary/world/Phase3R4AVanillaBerRegistryAndOwnersAsmTest.java) directly reads the **original** compiled `BlockEntityRenderers.<clinit>` from the exact Java21 NeoForge21.1.215 dependency. It records:
- Every `GETSTATIC BlockEntityType.*` in the original static initializer, every Java `invokedynamic` constructor method-reference bootstrap (`H_NEWINVOKESPECIAL`) and every `INVOKESTATIC BlockEntityRenderers.register` in original lexical sequence. It requires each registration to pair **one BlockEntityType constant** with **one concrete renderer constructor** (instead of guessing by class name).
- The expected 24 vanilla registrations in original order, to **22 distinct vanilla renderer implementations** (CHEST/ENDER_CHEST/TRAPPED_CHEST all instantiate the common `ChestRenderer`). This is an exact-target assertion; a changed original registry fails the Java21 unit test.
- The actual compiled bytes and SHA256 of all 22 concrete renderer owners, the factory/dispatcher `BlockEntityRenderer` and `BlockEntityRendererProvider/Context`, and `SignRenderer$SignModel`, capturing class inheritance/interfaces, all methods/descriptors/access flags, fields and JVM direct INVOKEs. All concrete original renderer owners must actually declare a `render` and a factory constructor. Includes exact original SignRenderer and HangingSignRenderer shared `renderSignWithText` dispatch.

The pinned but *non-authoritative comparator* 1.21.1 `BlockEntityRenderers.java` from an OptiFine-source tree independently lists the same 24 `BlockEntityType` names. Original compiled target JVM test above is the actual acceptance source, and its CI/ZIP must complete before declaring compiled evidence.

**Original full registered map:**

| BlockEntityType / count | Factory owner |
| --- | --- |
| SIGN, HANGING_SIGN | `SignRenderer`, `HangingSignRenderer` |
| MOB_SPAWNER, PISTON | `SpawnerRenderer`, `PistonHeadRenderer` |
| CHEST, ENDER_CHEST, TRAPPED_CHEST | `ChestRenderer` (shared) |
| ENCHANTING_TABLE, LECTERN | `EnchantTableRenderer`, `LecternRenderer` |
| END_PORTAL, END_GATEWAY, BEACON | `TheEndPortalRenderer`, `TheEndGatewayRenderer`, `BeaconRenderer` |
| SKULL, BANNER, STRUCTURE_BLOCK | `SkullBlockRenderer`, `BannerRenderer`, `StructureBlockRenderer` |
| SHULKER_BOX, BED, CONDUIT, BELL | `ShulkerBoxRenderer`, `BedRenderer`, `ConduitRenderer`, `BellRenderer` |
| CAMPFIRE, BRUSHABLE_BLOCK, DECORATED_POT | `CampfireRenderer`, `BrushableBlockRenderer`, `DecoratedPotRenderer` |
| TRIAL_SPAWNER, VAULT | `TrialSpawnerRenderer`, `VaultRenderer` |

Canonical [full 24-registration / 32-route family-owner matrix](PHASE3_R4A_VANILLA_BER_REGISTRY_OWNER_MATRIX_1_21_1.tsv) distinguishes **preliminary space policy** from established runtime semantics. Count/route inventory is not a claim that all 22 have an identical local frame requirement.

**CI status after test correction:** the first [CI #40435855541](https://github.com/Abbygree11/Planetary/actions/runs/40435855541) failed during `compileTestJava` from two *test-only* wrong record accessors (`Invoke.name()` was typed instead of `Invoke.method()`), NOT Minecraft or mod failure; fixed in [`e2c1ac5`](https://github.com/Abbygree11/Planetary/commit/e2c1ac50ecb92bdeabd7edfc1966c65962924b54). Corrected [CI #40436274761](https://github.com/Abbygree11/Planetary/actions/runs/40436274761) **PENDING VERIFICATION**. Original report artifact SHA/counts will be appended only after actual CI success and independent verification.

## 2/4 — actual inherited algorithm owners, local and world-space producers

Published 1.21.1 comparator original-like source (not authoritative compiled target until JUnit) establishes:

- `HangingSignRenderer extends SignRenderer`; both outer `render(SignBlockEntity,...)` implementations call shared `renderSignWithText`, while `HangingSignRenderer` overrides `translateSign` and `renderSignModel`. Sign geometry orientation, nested text (front+back), chains and wood parts **must be considered at the inherited root and overridden model transform together**. Four provisional `3C-1` BER Mixins target only outer concrete render methods. Do **not** add a second inherited transform if the outer frame already transformed the model+text.
- `TheEndGatewayRenderer extends TheEndPortalRenderer`; these visual shaders use an inherited world/portal path. A global `BlockEntityRenderDispatcher.render` local-frame transform would inappropriately rotate screen/world-space portals, vertical beacon beams and structure debug overlays.
- Three vanilla BE type IDs share `ChestRenderer` (normal/ender/trapped) but different BE subclass, blockstate and model texture; the rendering algorithm inheritance and BE data need separate cases. `PistonHeadRenderer` has dynamic moving BlockEntity progression and `SpawnerRenderer`/`TrialSpawnerRenderer` display animated entities; these routes overlap phase3A, actor rendering and Phase7. `BellRenderer`, `ShulkerBoxRenderer`, `BedRenderer`, `CampfireRenderer` and `DecoratedPotRenderer` require independent local animation/item/pattern ownership, not one generic rotation.
- All `BlockEntityRendererProvider.Context` and `BlockEntityRenderDispatcher` original declarations are included to delimit global manager boundary; method/call coverage in this packet is selected, no actual client world transition or loaded mod call ordering test.

The renderer *registered type* is different from the **BlockEntity state author**, from BakedModel quads and from nested BER child rendering. Do not assign phase2 oriented state corrections solely on an observed tilted sign until actual `ROTATION_16`/`FACING`, nesting and real player placement have been distinguished.

## 3/4 — NeoForge extension owner and concrete Planet integration

The pinned NeoForge source `EntityRenderersEvent.RegisterRenderers.registerBlockEntityRenderer(type,provider)` calls **`BlockEntityRenderers.register(type,provider)`**; `ClientHooks` posts this registration event. Thus the runtime renderer provider map may be extended or **overridden by a mod**. The original vanilla 24 registrations are not a guarantee that all real installed `BlockEntityRenderer` instances are vanilla classes. Concrete implementation must use the deepest consistent extension contract and document unsupported independently instanced renderers instead of claiming every mod is compatible because a vanilla Mixin exists.

Current Planet `3C-1` code is **provisional**:
- `PlanetLocalBlockEntityRender` opt-in local rigid BER boundary with thread-local LIFO scopes to wrap selected BER `PoseStack` around block centre, only for world-attached non-+Y Planet BlockEntity; excludes no-Level inventory/GUI and normal +Y. Not hooked globally into `BlockEntityRenderDispatcher`.
- `SignRendererPlanetFrameMixin`, `HangingSignRendererPlanetFrameMixin`, `BannerRendererPlanetFrameMixin` and `SkullBlockRendererPlanetFrameMixin` inject at concrete `render` HEAD and RETURN. Existing `BedRendererGravityMixin` is independent, not included in those four. Green earlier `Phase3C1BlockEntityRendererAsmContractTest` only proved exact Mixin callback signatures and compiled Java, not installed client application.
- The HEAD/RETURN scope may not be exception-safe if a mod render callback throws; this is a **source-level potential leakage** rather than a verified in-game regression. Do not change the production boundary until full research R is closed, and never blanket-wrap world-space portal/camera/shader/external renderers.
- The V8 42-station sign/hanging fixture CYAN forced state cannot substitute natural LIME player placement and yaw, and 2.4A generated vs placed ChainBlock axis discrepancies may belong to state writer, not BER. All 6 planet faces, 12 edges, 8 corners, +Y no-Planet and loaded mods remain gameplay **PENDING**.

The full 32-row family matrix separately records inherited owners, client dispatcher, NeoForge external registrations and explicit unsupported/unknown cases. Create6.0.10 and AE2 NeoForge 19.2.18 releases have pinned previous source/JAR ABI but **have not been loaded** in Planet client; Create6.0.10 was compiled against NeoForge 21.1.219 vs target 21.1.215, so no assumed loader compatibility.

## 4/4 — bounded completion criterion, CI provenance and next packet

`3R-4A` will be **COMPLETE in vanilla *registry census* scope only when corrected CI #40436274761 is SUCCESS and original registry/renderer TSV artifact is independently validated**. It must not mark whole `3C`, all subclass render methods, mod overrides or game visual PASS.

Remaining 3C:
1. **`3R-4B`** original exact `SignRenderer`, `HangingSignRenderer`, `BannerRenderer`, `SkullBlockRenderer` nested text/chain/pattern/player-skin and renderer superclass/virtual dispatch method-body + relevant mod override owners. All 4 selected compiled class methods, not just outer render signature.
2. **`3R-4C`** rest of the 22 registry implementation owners, including bed/chest/shulker/bell/pot/book/campfire, World beam/portal/overlay and animated actor/item bypass; original method-body grouped by actual ownership, not per arbitrary class.
3. **`3R-4D`** NeoForge `RegisterRenderers`/dispatcher and selected released-mod custom BER source/inheritance bypasses, all vanilla class/factory paths audited and coverage ledger reconciled; then transition to `3R-5` independent accelerated renderer/mod bypass, `3R-6` worker/reload ownership, `3R-7` full phase source R review.
4. Only after Research R PASS: implement the *shared* Phase2-dependent visual runtime foundation and prove game V in six-face / twelve seam / eight corner / noPlanet, natural placed vs generated, loaded compatible mod scenarios before uninterrupted `2.4A` implementation.

**No production Java, Mixins, V8 lab or `main` changes in this packet.**
