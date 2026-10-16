# Planetary — 2.4A-9: FrontAndTop placement authors, Crafter/Jigsaw

Target: Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21, branch `2.0`.
Date: 2026-10-15. **Only explicit original player/synthetic placement semantics are adapted.** All author, consumer, mod/renderer and gameplay gates remain independent.

## Actual original owners, not block-name heuristics

Source comparator pinned at `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`:
- [CrafterBlock.java](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CrafterBlock.java): directly declares `getStateForPlacement(BlockPlaceContext)`; chooses FRONT = `getNearestLookingDirection().getOpposite()`. For vertical FRONT, TOP depends on `getHorizontalDirection()` (opposite for DOWN); for horizontal FRONT, TOP = UP. Uses the **single `BlockStateProperties.ORIENTATION`** (`FrontAndTop`) property, 12 legal pairs. It also determines physical TRIGGERED from `hasNeighborSignal` and later runs BE, recipes, output and comparators. Those latter owners are NOT delegated to the placement helper.
- [JigsawBlock.java](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/JigsawBlock.java): different directly declared author, FRONT = actual clicked face. If clicked is vertical, TOP = actor horizontal direction opposite; otherwise UP. `newBlockEntity`, structure-template `canAttach`/front/top consumers and placement by structure/worldgen are separately authored.
- [FrontAndTop.java](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/core/FrontAndTop.java) enumerates 12 valid `(front,top)` entries; there is no 24-state unrestricted orthogonal pair domain. Existing exact selected NeoForge classpath research: [C16](PHASE2_STAGE3C16_FRONT_AND_TOP_ROTATION16_STATE_TRANSFORMS_NEOFORGE_ASM_1_21_1.md). These comparative sources may include OptiFine edits; actual declared method/handler is checked by Phase24A9 exact loaded-bytecode JUnit.

## Shared-safe level and implementation

`PlanetFrontAndTopPlacement` exposes **two owner-specific pure orientation authors** (`crafterOrientation`, `jigsawOrientation`) over the same legal 12-state enum, plus original-state-preserving post-return adaptors. Neither generic `BlockItem` nor global `Direction` is modified. Target frame resolution uses existing `PlanetFrameApi`, full player-body-to-target conversion and synthetic physical-direction opt-in when it is representable. Invalid/unknown/fake author falls back to the original state rather than guessing.

`CrafterBlockLocalOrientationMixin` and `JigsawBlockLocalOrientationMixin` each have precisely one cancellable `RETURN` injection at the concrete declaring owner. Original vanilla NeoForge placement, state identity, `TRIGGERED`, `CRAFTING`, BlockEntity, signal flags and secondary mod-added properties stay untouched.

| Contract | Evidence/limit |
| --- | --- |
| SHARED_SAFE | Legal `FrontAndTop` target-canonical pair value; pure helper + two explicitly opted-in authors |
| OPT_IN_ALGORITHM | Separate Crafter nearest-look/opposite TOP and Jigsaw clicked-normal TOP; natural player and representable synthetic contexts |
| MOD_INDEPENDENT | Mod subclass that **overrides** owner method, custom `POINTING` or different property, foreign fake Level, custom item writer, Create machine/contraption are NOT magically covered |
| PHYSICAL_DO_NOT_TOUCH | Actual world/neighbor coordinates, physical redstone signals, Crafter output location/BE capabilities, Jigsaw structure join/rotation/NBT/worldgen, raw shape and rendered normals |

Why not deeper: global `BlockStateProperties.ORIENTATION` or `Direction` rotation would change physical mod schematic, render and worldgen consumers. Why not shallower: two per-item-ID string switches duplicate geometry and miss subclasses that inherit each declaring placement method.

**Important pending independent owners:** vanilla Crafter's `dispenseItem` consumes FRONT in physical `BlockPos.relative`, hopper `addItem`, item spawn position/direction, sound/event emission. A local state on a rotated face **does not by itself** make crafting output route correctly; this is an explicit Phase 7A/automation follow-up. Likewise Jigsaw `canAttach`, joint metadata and structure-template insertion need separate Phase 9/worldgen adaptation. Do not mark either full game family DONE just because placement ABI and JUnit pass. Do not claim loaded mod JAR compatibility from source inheritance alone.

## 2026-10-16 applied-client screenshot regression and confirmed root cause

User supplied two screenshots of the V7 Crafter test: reference has red grid on a horizontal face, naturally placed block has red grid on top. Source inspection of original 1.21.1 `BlockPlaceContext` and `CrafterBlock` identified a **real implementation defect** in Planetary's owner adapter: `getNearestLookingDirection()` **singular** returns unmodified `Direction.orderedByNearest(player)[0]`; `getNearestLookingDirections()` **plural** reorders the direction array by `clickedFace.opposite` for non-replacing placement. Our `PlanetFrontAndTopPlacement.afterCrafterPlacement` previously called the latter through `PlanetFrameApi.localNearestLookingDirections(context)`. Clicking the local UP floor therefore misread raw player view as DOWN and authored FRONT UP even with horizontal player view.

FIX [04a3535](https://github.com/Abbygree11/Planetary/commit/04a35357b2126e805a1313cf17eef1563336633b): new `PlanetFrameApi.localRawNearestLookingDirection` uses actor raw `Direction.orderedByNearest()[0]`, transports actor-body semantics via physical world coordinates into canonical target frame, without clicked-face priority. Crafter opts into this precise semantic; **plural order remains unchanged** for all other mechanisms and Jigsaw still uses clicked physical normal. Regression JUnit compares singular vs plural after floor-click on all six faces, asserts Crafter horizontal FRONT, plus cross-chart actor/target transformation; [CI #38953937805](https://github.com/Abbygree11/Planetary/actions/runs/38953937805) **COMPLETED SUCCESS** (production/test compile, JUnit). Applied game retest not yet received. Even after fix, a genuinely downward-looking player can validly produce FRONT=UP; capture face, player pitch and F3 targeted `orientation` before inferring another regression. Existing V7 lab stays active.

## Automated and gameplay evidence

- JUnit property domain: each owner 6 direction × 4 heading cases => exactly 12 reachable legal pairs, missing/invalid heading fallback, original +Y baseline. Existing `PlanetOrientationPropertiesTest` separately covers physical↔canonical FRONT/TOP on six faces and invalid domain.
- ASM: `Phase24A9FrontAndTopMixinAsmContractTest` verifies exact NeoForge-loaded declaring methods, callback descriptor and staticness, `@Inject.method`, `RETURN`, cancellable and Mixin config. Actual transformed client startup is **NOT** proven by classfile inspection.
- CI for new runtime/test code: check actual latest `2.0` GitHub Actions run; no PASS until complete.
- Real game **PENDING**: startup/Mixin, Crafter and operator-given Jigsaw player placement on +Y and five other faces, all 12 `FrontAndTop` pairs where physically achievable, real LIME player lane (a reference state is not a pass); no-Planet bypass; 12 edges/8 corners with actor vs target frame crossing; manual synthetic author/structure writer; mod subclass inheritor and independent Create-style source with an actually loaded compatible JAR; dedicated server.
- **Lab policy:** V6 24-station catch-up remains to preserve pending `2.4A-6/7/8` manual evidence. Do not overwrite until the user confirms acceptance; a subsequent V7 single-pad replacement can be prepared for Crafter/Jigsaw. Use already vacant LIME lanes to smoke place real items only until then. Temporary dev `/planetary test light off` reveals the real dark side; brightness is not sky-light fixed.

Scope: selected property-owner placement implementation only; full survival automation, structure and loaded-mod behavior PENDING.
