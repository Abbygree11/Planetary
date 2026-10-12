# Phase 2.3C-2 — placement contexts and independent synthetic item authors

**2026-10-12 · branch 2.0 · Minecraft 1.21.1 / NeoForge 21.1.215 · one whole owner family, four internal checks.** Exact original 2.3B census is **1,333 ITEM IDs, 87 registered concrete classes, 925 BlockItem descendants** (original run [37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), immutable ZIP SHA256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`). This is NOT a new original-census reproduction. The previously confirmed `2.3C-1` loaded ASM/CI tests remain separate.

## Source provenance and exact test boundary

Directly inspected the comparative Mojang-named **1.21.1** source at pinned `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`:

- [UseOnContext](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/context/UseOnContext.java), [BlockPlaceContext](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/context/BlockPlaceContext.java), [DirectionalPlaceContext](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/context/DirectionalPlaceContext.java).
- [ScaffoldingBlockItem](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/ScaffoldingBlockItem.java), [PlaceOnWaterBlockItem](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/PlaceOnWaterBlockItem.java), [SolidBucketItem](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/SolidBucketItem.java).
- Later **comparative, NOT the same as 21.1.215** official [SolidBucketItem.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/item/SolidBucketItem.java.patch), [BlockItem.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/item/BlockItem.java.patch) and [BucketItem.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/item/BucketItem.java.patch). No patches found at that later SHA for the three context classes, ScaffoldingBlockItem or PlaceOnWaterBlockItem; absence from that tree does **not** prove 21.1.215 bytecode.
- New [Phase23C2PlacementContextAsmContractTest](../../src/test/java/dev/planetary/world/Phase23C2PlacementContextAsmContractTest.java) uses ASM **on the real Gradle-resolved 21.1.215 compiled classes** for descriptors, declared overrides, exact INVOKE call-site owners and count, and NeoForge context-sensitive SolidBucket sound method. No `Class.forName()` of Mixin-package classes, no change to Mixin target, no `require=0`.
- Existing Stage3B [special item writers](PHASE2_STAGE3B_SPECIAL_ITEM_USE_COMPONENT_NEOFORGE_MOD_SOURCE_AUDIT_1_21_1.md) and [non-item authors](PHASE2_STAGE3B_ALTERNATE_NONITEM_CREATORS_SOURCE_NEOFORGE_MOD_1_21_1.md) provide source context for dispenser/falling/foreign writer bypasses. Real pinned Farmer's Delight MC1.21.1 has separately overridden `RopeItem.updatePlacementContext`, `RiceItem.useOn`, `MushroomColonyItem.getPlacementState`; the investigated mod version requires NeoForge >=21.1.219, above the Planetary 21.1.215 runtime target: **source comparator only, no mod JAR loaded/compat PASS**.

## 1/4 — JVM owner / context construction contracts

| Real declaring owner | Exact or critical ABI checked by test | Coordinate/dispatch meaning |
| --- | --- | --- |
| `UseOnContext` | `getClickedPos():BlockPos`, `getClickedFace():Direction` via `BlockHitResult`; nullable player | Physical hit, direction and position. `getHorizontalDirection` defaults NORTH if player null; `getRotation` defaults 0° |
| `BlockPlaceContext` | protected `(Level,Player,InteractionHand,ItemStack,BlockHitResult)` constructor invokes physical `BlockPos.relative(Direction)` | Original `hitPos` and possible physical `relativePos`, conditional `replaceClicked` by `canBeReplaced` |
| `BlockPlaceContext.at` | static `(BlockPlaceContext,BlockPos,Direction):BlockPlaceContext`, constructs new context | Recalculate hit/replacement against NEW PHYSICAL block; never pretend original context still describes final `getClickedPos` |
| `DirectionalPlaceContext` | `(Level,BlockPos,Direction,ItemStack,Direction)` constructor calls `BlockPlaceContext` with `player=null` | Own `getClickedPos/canPlace/replacingClickedOnBlock/getNearestLookingDirections/getHorizontalDirection/getRotation` overrides; physically authored direction order, never `Direction.orderedByNearest(player)` |
| `ScaffoldingBlockItem` | `updatePlacementContext(BlockPlaceContext):BlockPlaceContext` | Overrides target by `BlockPlaceContext.at`, physical `MutableBlockPos.move`; `ScaffoldingBlock.getDistance` and 7-distance/height constraints |
| `PlaceOnWaterBlockItem` | `use(Level,Player,InteractionHand):InteractionResultHolder` vs `useOn(UseOnContext):InteractionResult` | `useOn` returns PASS; `use` raycasts `SOURCE_ONLY`, synthesizes hit one **physical above** via `BlockPos.above`/`BlockHitResult.withPosition`, then `super.useOn` |
| `SolidBucketItem` | `useOn(UseOnContext):InteractionResult` and `emptyContents(Player,Level,BlockPos,BlockHitResult):boolean` | Player `super.useOn` follows BlockItem; dispenser executes **direct** physical `Level.setBlock`, not `BlockItem.place` |
| `SolidBucketItem` NeoForge sound bridge | `getPlaceSound(BlockState,Level,BlockPos,Player):SoundEvent` | Comparative official NeoForge patch intentionally bridges to custom `getPlaceSound(BlockState)`, preserving powder-snow sound despite NeoForge's context-sensitive BlockItem hook; strict actual 21.1.215 test must prove the override |

Exact signature matching is mandatory: `canPlace` from `BlockItem`, `BlockPlaceContext`, `DirectionalPlaceContext` and `StandingAndWallBlockItem` are **different Java and/or JVM descriptors**. Same spelling is not common semantic ownership.

### Newly discovered specific NeoForge 21.1.215 risk (separate follow-up)

**`SolidBucketItem` contextual sound override is absent in the actual compiled target version.** The comparative 1.21.1 patch at a later NeoForge source commit 3cae645 is *newer/different* and has a bridge; it cannot be retroactively described as applied to 21.1.215. Since 2.3C-1 verified that the compiled `BlockItem.place` invokes the four-argument sensitive `getPlaceSound`, the SolidBucket legacy one-arg override appears to be bypassed along player BlockItem placement. Actual audible outcome and whether a different effect compensates remain **NOT TESTED**. Record compatibility bug candidate for later version-specific implementation/acceptance; do not slip a sound patch into research-only 2.3C-2. Direct dispenser `emptyContents` calls its own `placeSound` field and is a distinct path.

## 2/4 — lifecycle and alternate-author split

**Normal player:** physical player hit `UseOnContext` → `BlockItem.useOn` → `BlockPlaceContext` → `BlockItem.place` virtual `updatePlacementContext/getPlacementState/placeBlock`; once placed, real `BLOCK_STATE` component, BE data/component security and callbacks apply. `ScaffoldingBlockItem` can replace the location/context midway; never cache `getClickedPos` before that override. The scaffolding scan uses global `Direction.UP` and physical mutable XYZ while following a chain; its `ScaffoldingBlock` support/distance lifecycle is a separate block-owner algorithm and is not corrected by changing an Item Mixin.

**Non-player:** Shulker-box dispenser calls `BlockItem.place` with a player-less `DirectionalPlaceContext`. The synthetic constructor's supplied `direction` and clicked side have independent provenance. FallingBlockEntity uses that context to test replaceability, **not** to invoke BlockItem.place. `DirectionalPlaceContext.getClickedPos` ignores the base `replaceClicked` selection and returns the original physical hit position. Synthetic `getNearestLookingDirections` explicitly enumerates a world-direction order with `DOWN` first; this cannot be inferred from a player-body frame.

**Water:** source-only fluid raycast → physical `above` re-target → `BlockItem.useOn` under synthesized UseOnContext. This applies to **both** original PlaceOnWater IDs (`lily_pad`, `frogspawn`). An adapter only at the originally clicked player-side face would not see the final authored block. Replacing `BlockPos.above` globally breaks other fluid, worldgen and physical algorithms; opt-in water-placement semantic intent and properly sourced target frame is needed.

**Powder snow:** `SolidBucketItem.useOn` uses the shared BlockItem pipeline and replaces the consumed held container when successful, but `DispensibleContainerItem.emptyContents` directly writes the supplied physical dispenser target (and emits FLUID_PLACE/sound) without applying BlockItem post-placement BE/item-state logic. A one-size-fits-all item-after-place adapter misses this author; do not conflate with `BucketItem`'s independent NeoForge fluid-stack/FluidType-sensitive `emptyContents` overload.

**Neighbor/survival/updates:** `BlockState.canBeReplaced`, `ScaffoldingBlock.getDistance`, `canSurvive`, `updateShape`, `neighborChanged`, scheduled ticks, shape/rotation/mirror, fluid collision and block entities keep their own *block/engine* owners. Item context bytecode assertions do not certify those lifecycle handlers. Exact edge/corner multi-cell traversal is path-dependent; physical target identity and canonical target BlockState frame must remain independently available.

## 3/4 — strict semantic/cross-mod decision

| Owner/mechanism | Disposition | Safe boundary |
| --- | --- | --- |
| `UseOnContext.getClickedPos/getClickedFace`, `BlockPos.relative` | **PHYSICAL_DO_NOT_TOUCH** | Hit normal and target physical coordinate used by every vanilla/mod world writer |
| `BlockPlaceContext.at` | **SHARED_SAFE (typed provenance, no global mutation)** | Observation/adaptation after each virtual placement-context change; author+real physical target+frame |
| `DirectionalPlaceContext` synthetic order | **OPT_IN_ALGORITHM** | Explicit author/source intent; null-player contexts use vanilla physical behavior until individually verified |
| `ScaffoldingBlockItem` propagation | **OPT_IN_ALGORITHM** | Local support/routing policy at scaffolding algorithm owner; preserve physical build-height and replaceability |
| `PlaceOnWaterBlockItem` `above` | **OPT_IN_ALGORITHM** | Water surface placement policy with real fluid hit and next physical cell; neither forced global-Y nor global BlockPos rewrite |
| `SolidBucketItem.emptyContents` | **OPT_IN_ALGORITHM** | Dispenser author at supplied physical target, retain direct event/sound/permissions and FluidType behavior of other bucket owner families |
| Arbitrary mod overrides, virtual worlds, contraptions and physical capability graphs | **MOD_INDEPENDENT / PHYSICAL_DO_NOT_TOUCH** | Real loaded-mod owner check, typed opt-in or mod integration; no inherited-Mixin claims for override methods |

**Deepest safe boundary:** a stable `PlanetPlacementIntent`-like typed data carrier (author `PLAYER/DISPENSER/FALLING/WATER_RAYCAST/FOREIGN`, physical clicked hit, original and final physical target, target canonical BlockState frame, player BODY frame only when present, optional traversal chart when warranted); thin NeoForge adapters at **real owner dispatch**. It remains a **design only**, not a Java API introduced by this package. One level deeper (mutating generic BlockPos/Direction/Level) damages physical graph and mods. One level shallower (named water item / one scaffolding block patch) loses derived mods and synthetic authors. Unknown third-party implementations default to physical semantics.

## 4/4 — strict ASM/JUnit gate and 2.3C-2 acceptance

The new JUnit ASM suite checks all three context class hierarchies, constructor-level `relative` ownership, `BlockPlaceContext.at` owner/descriptors, synthetic overrides and lack of player lookup, `ScaffoldingBlockItem` `at` retarget, exact `PlaceOnWaterBlockItem` `above`/withPosition/`INVOKESPECIAL BlockItem.useOn` call chain, `SolidBucketItem` direct `emptyContents` and **the absence of** the later NeoForge sensitive sound bridge in actual 21.1.215. **These are bytecode invariants, NOT a running Mixin transformation.**

[First CI run 38259861596](https://github.com/Abbygree11/Planetary/actions/runs/38259861596): compiled, 322 JUnit tests executed, **1 FAIL** at precise `SolidBucketItem.getPlaceSound(BlockState,Level,BlockPos,Player)` override assertion; this is a **real 21.1.215 version delta** relative to official later comparative patch. New [commit 57b5499](https://github.com/Abbygree11/Planetary/commit/57b54994c364645df51409e18f3ccc1b70903f92) retains a strict test asserting the *actual absence* of the contextual override plus the presence of the base NeoForge four-arg call and legacy subclass override. This is not suppressing the mismatch: it pins it and records the possible sound regression for separately scoped implementation/acceptance. [Repeat CI run 38260347819](https://github.com/Abbygree11/Planetary/actions/runs/38260347819) **SUCCESS, completed**: production/test compilation, JUnit and census steps all green on the exact test revision. This verifies the asserted 21.1.215 bytecode contract, **not** runtime Mixin application, audio or six-face gameplay.

Future **NOT RUN** game cases: normal non-Planet regression; 6 canonical Planet interiors × natural scaffolding extensions, lily pad/frogspawn physical water hit, powder snow bucket manual; all 12 edge pairs, 8 exact corners with changed gravity frame; secondary click, obstruction, replaceability, World build-height, `ScaffoldingBlock` distance/support; player-less dispenser shulker and solid bucket; falling-block synthetic replacement, BE/components, stack consumption, audio/game-event; persistence and mod JAR with target-compatible NeoForge version; foreign/virtual level should remain physical. Real same-owner Minecraft 1.21.1 mod source vs runtime test are separate.

| Gate | State |
| --- | --- |
| Pinned comparative 1.21.1 sources and NeoForge extension patch reviewed | DONE |
| Exact original registry class/ID relationships | PREVIOUSLY VERIFIED in 2.3B-4 |
| Strict 21.1.215 classpath ASM test committed | DONE |
| CI compile/JUnit of exact assertions | First run 38259861596 FAIL (real ABI delta), adjusted **strict version delta** assertion 57b5499; [repeat run 38260347819](https://github.com/Abbygree11/Planetary/actions/runs/38260347819) **PASS** |
| Applied Mixin in client/server | NOT RUN |
| Real world six-face/seam/corner and mods | NOT RUN |

**Next owner family after 2.3C-2 is 2.3C-3:** direct secondary placement via BedItem/DoubleHighBlockItem, final Block#setPlacedBy and block lifecycle/multiblock author interactions; preserve source/target physical cells, mod overrides, NeoForge hooks, block entity and component repair before code implementation.
