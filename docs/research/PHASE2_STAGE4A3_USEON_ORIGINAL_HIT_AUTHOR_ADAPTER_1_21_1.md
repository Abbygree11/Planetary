# Planetary 2.4A-3 — original UseOnContext hit and typed author-context bridge

Target: Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21. Branch `2.0`.
Code commit: [f51bb9b](https://github.com/Abbygree11/Planetary/commit/f51bb9b48a26ed40af6fe654e40ef97ce0c5e72b). **Compile/CI and applied Minecraft Mixin are separate proof levels.**

## Actual source ownership

`UseOnContext.getHitResult():BlockHitResult` is the protected original hit getter. `BlockPlaceContext.getClickedPos()` can instead return the final physical target after the replaceability rule. `BlockPlaceContext.at` creates a new synthetic hit/context; its hit is original **for that new context**, not automatically the first player's earlier raycast. Arbitrary `Item.useOn`, `BlockItem.getPlacementState`, mod-owned context overrides and synthetic `DirectionalPlaceContext` can author different targets and properties.

Added `PlanetOriginalHitSource` as an ordinary non-Mixin read-only interface and `UseOnContextOriginalHitInvoker` as the narrowly scoped actual NeoForge 1.21.1 `@Invoker("getHitResult")`. `PlanetPlacementContextAdapter` now offers `fromPhysicalBlockPlaceContext(context, explicitAuthor)` returning `Optional.empty()` if the woven interface is absent, as opposed to fabricating original physical hit via an inverse target step. It reads the actual context's physical `BlockHitResult` (position, side, click Vec3), keeps independently computed final target and replace flag, and does not infer player BODY frame unless author is explicitly supplied as player. Existing explicit-original-hit adapter remains supported.

`PlanetFrameApi.placementFrame(BlockPlaceContext)` now uses `PlanetPlacementResolver` on woven contexts, so pre-existing (already working) specialized family Mixins consuming `PlanetFrameApi` use the typed source/target semantic contract. When an unwoven plain JUnit test provides no accessor, old target-only semantics are preserved for regression compatibility; no false original-hit provenance is claimed.

## Deepest safe owner contract

| Category | Handling | Limit |
| --- | --- | --- |
| SHARED_SAFE | Read true original hit from inherited `UseOnContext`, final target from active context and canonical target from Planet field | Implemented bridge, exact classpath ASM/JUnit gate |
| OPT_IN_ALGORITHM | Known block families already calling `PlanetFrameApi.placementFrame` (pillar/hopper/slab/end rod/etc) | Their owner-specific states remain original; no universal 241 block adaptation |
| MOD_INDEPENDENT | Mod custom `Item.useOn`, overridden placement, Create synthetic/virtual chart, worldgen and structure writers | No implicit state rewrite; source of a NEW retargeted context is that context's hit |
| PHYSICAL_DO_NOT_TOUCH | `Level.setBlock`, physical `BlockPos.relative`, raw context getters, `BlockItem.place`, components, NeoForge callbacks | Unchanged; no cancellable HEAD hook or global synthetic Level |

The next deeper global hit-getter rewrite would break physical code and modded neighbor calculations. The shallower per-torch/per-log patch would lose authors and modded implementations. The stable semantic intent with one thin source accessor avoids both.

## Automated verification

`PlanetPlacementContextAuthorBridgeTest` covers original vs final cell at canonical seam, true physical original side/location against mocked nonphysical getter return, independent source/target canonical faces, vanilla/unbound pass-through, conservative unwoven bridge, legacy pure-test behavior.

`Phase24A3OriginalHitAsmContractTest` reads the actual Gradle-loaded NeoForge 21.1.215 `UseOnContext` and compiled Mixin class to require protected getter, subclass inheritance, external bridge interface, matching `@Invoker` signature and registered config. This is **not** evidence of a successful applied Mixin in Minecraft client/server.

**Manual acceptance still OPEN:** client and dedicated server startup with applied accessor; ordinary Overworld passthrough; player place/replacement and synthetic/fake player; six face interiors, 12 edge transitions, 8 corners; loaded exact-compatible third-party mods; preserve all preexisting working slab/hopper/pillar/standing-wall and particle interactions. Modded `Level` that uses foreign virtual coordinates must NOT be classified as physical without an explicit integration adapter. NeoForge item post-place components/events and multi-cell lifecycle remain independent.

Next: owner-specific oriented-state author adapters (not another generic intercept); include exact 21.1.215 source+ABI per owner, opt-in mod bridge, tests and then actual in-game acceptance. Not a complete 2.4A phase PASS.
