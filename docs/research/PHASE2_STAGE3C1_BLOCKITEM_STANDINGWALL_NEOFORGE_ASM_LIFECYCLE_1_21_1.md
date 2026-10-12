# Stage 2.3C-1 — BlockItem / StandingAndWall placement: bytecode, lifecycle and Mixin contract

**2026-10-12 · branch `2.0` · MC 1.21.1 / NeoForge 21.1.215 · bounded owner-family package 4/4.**

## Evidence boundary

- **Original compiled item registry (Stage 2.3B-4):** original unchanged Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), ZIP SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. 1,333 ITEM IDs, 87 actual concrete ITEM Java classes, 925 BlockItem descendants; 58 registered StandingAndWall descendants (19 direct, 16 Banner, 11 Sign, 11 HangingSign, 1 PlayerHead). **This records effective declaration owners, NOT proof that an `@Redirect` applies.**
- **Readable comparative Mojang source:** [BlockItem.java](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java), [StandingAndWallBlockItem.java](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/StandingAndWallBlockItem.java).
- **Comparative, NOT proved exact at 21.1.215:** official [BlockItem.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/item/BlockItem.java.patch), [Items.java.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/item/Items.java.patch). The repository does not expose a ref literally named `21.1.215`; a later 1.21.1 patch snapshot cannot substitute for target bytecode.
- **Actual project hooks:** `StandingAndWallBlockItemMixin`, `StandingAndWallBlockItemAccessor`, `PlanetFrameApi.localNearestLookingDirections`; no production Java/Mixin/Gradle edits in this packet.
- **Real Gradle-resolved target ASM verification:** new [Phase23C1ItemPlacementAsmContractTest](../../src/test/java/dev/planetary/world/Phase23C1ItemPlacementAsmContractTest.java) checks loaded `.class` files from target dependency and compiled Planetary Mixin. Source review and test *authorship* are done; record test outcome separately from committed code. GitHub Actions [run 38257655791](https://github.com/Abbygree11/Planetary/actions/runs/38257655791) initiated by test commit; 315 JUnit tests reached execution and one test assertion failed because the test mistakenly expected an annotation method *descriptor without its method name*. Corrected in test commit [56721e4](https://github.com/Abbygree11/Planetary/commit/56721e4470521beb391ac58e1b91c3960084717e), now checking full `getPlacementState` selector. [Repeat CI run 38258262302](https://github.com/Abbygree11/Planetary/actions/runs/38258262302) launched; do not claim the second run succeeded before checking.

## 1/4 — precise patch + effective loaded bytecode owner

Test expectations (the automated test, rather than this document, is authoritative when actually run):

| Owner | Full method descriptor / bytecode target | Scope |
| --- | --- | --- |
| `BlockItem` | `place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;` | public common virtual placement pipeline |
| `BlockItem` | `useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;` | dispatch entry; specialized Item variants may bypass |
| `BlockItem` | `updatePlacementContext(BlockPlaceContext):BlockPlaceContext` | virtual; ScaffoldingBlockItem overrides |
| `BlockItem` | `getPlacementState(BlockPlaceContext):BlockState` | virtual; StandingAndWall 58 IDs, GameMaster 5 IDs override |
| `BlockItem` | `placeBlock(BlockPlaceContext,BlockState):boolean` | virtual; DoubleHighBlockItem 27 IDs and BedItem 16 IDs override |
| `StandingAndWallBlockItem` | `getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;` | declares its OWN method with a `BlockPlaceContext.getNearestLookingDirections()[Direction]` call |
| `Items$1` | `registerBlocks(Ljava/util/Map;Lnet/minecraft/world/item/Item;)V` | anonymous big_dripleaf/cauldron secondary registration, NOT new placement candidates |

Expected NeoForge-specific `BlockItem.getPlaceSound(BlockState,Level,BlockPos,Player):SoundEvent` is pinned with a *failing* ASM test; this is more precise than assuming the comparative patch is exactly the Gradle-resolved ABI. A changed signature must fail the test and be explicitly re-reviewed, not suppressed.

## 2/4 — full lifecycle and bypass domains

Comparative `BlockItem.place` follows virtual `updatePlacementContext → getPlacementState → placeBlock`; **the physical target may change at the first step**. After placing, the method re-reads the *actual* world state, applies `DataComponents.BLOCK_STATE` state-properties, guarded `DataComponents.BLOCK_ENTITY_DATA`, BE component transfer, `Block.setPlacedBy`, criteria, sound, `GameEvent.BLOCK_PLACE`, stack consumption. The test asserts the compiled base method continues to invoke crucial post-placement owners instead of blindly copying the vanilla source method.

`StandingAndWallBlockItem.getPlacementState` calls both its standing and wall target block's `getStateForPlacement` and polymorphic `canPlace(LevelReader,BlockState,BlockPos)`, which in turn delegates to `BlockState.canSurvive`. **This `canPlace` overload is separate** from `BlockItem.canPlace(BlockPlaceContext,BlockState)`. `BlockState.canSurvive/updateShape/neighborChanged/scheduledTick/growth` are independent block/world owner families, NOT proven by the item-family Mixin. No attempt to rewrite world-wide neighbor direction or block properties.

Special `BedItem` and `DoubleHighBlockItem` second-half placement can cross seams; `ScaffoldingBlockItem` can substitute the target placement context; `PlaceOnWaterBlockItem` (2 IDs) and `SolidBucketItem` (powder_snow_bucket) may use a different `use/useOn` route; dispensers/falling/worldgen/commands/foreign Levels may bypass BlockItem entirely. Separate Stage 3C packages must check them. All actual gameplay scenarios remain **NOT RUN**.

## 3/4 — semantic front/top/axis and NeoForge extension conservation

`StandingAndWallBlockItem` chooses a standing vs wall **candidate order**, it does not establish all block FACING/AXIS properties. On a Planet interior, `PlanetFrameApi.localNearestLookingDirections` converts the player's local viewing directions via physical space into the *target canonical BlockState frame*; at an exact edge player/body and target frames can differ. The `@Redirect` changes only order returned to this one item owner; `BlockHitResult`, physical clicked face, target BlockPos, block-supported sides and WorldGen/foreign worlds stay physical. Null-player synthetic `DirectionalPlaceContext` retains vanilla order until specifically designed/tested; this is safe fallback, not success on all faces.

Later NeoForge patch adds context-aware `getPlaceSound` and `BlockState.getSoundType(level,pos,player)` dispatch. All future adapters MUST keep this method dispatch, BE permission checks, `setPlacedBy`, components and lifecycle callbacks. Extending vanilla `StandingAndWallBlockItem` may inherit this candidate-order Mixin; **overridden** `getPlacementState` in a mod class does not. Native `BlockItem` default physically placed block is `getBlock()`, but 14 item IDs have different primary `placed_block` IDs and the anonymous `Items$1` entries additionally map secondary block-to-item IDs. Registry IDs alone never determine runtime placement semantics.

**Proposed later implementation boundary, NOT implemented:** stable typed author/source/target physical context and target canonical BlockState orientation + opted-in semantic candidates; thin versioned owner adapter, no blanket `Level.setBlock/BlockPos.relative/Direction` replacement.

## 4/4 — ASM test and acceptance split

`Phase23C1ItemPlacementAsmContractTest` independently reads *the classpath classes* with ASM (no reflection-based load of Mixin package): asserts base member descriptors, virtual pipeline call owners/count, post-placement lifecycle call names, sensitive NeoForge sound hook descriptor, one exact `INVOKEVIRTUAL BlockPlaceContext.getNearestLookingDirections()[Direction]` inside the **declaring subclass**, `@Redirect.method[]` + `@At(value,target)`, handler descriptor/staticness, matching `@Invoker`, and anonymous `Items$1` registration declaration. Any changed compiled ABI/callsite fails loudly; never change to `require=0` to hide it.

**Acceptance layers kept separate:**

| Gate | 2.3C-1 status |
| --- | --- |
| Source and existing 2.3B original effective-owner evidence | REVIEWED |
| Strict ASM test authored and committed | DONE |
| Actual NeoForge21.1.215 classpath ASM/JUnit run | Original run 38257655791 FAIL (test assertion typo); corrected run 38258262302 PENDING until its conclusion is independently inspected |
| Mixin transformer actually applied in client/server | NOT RUN |
| Planetary item semantics on 6 canonical faces/12 seams/8 corners | NOT RUN |
| Actual loaded third-party mod subclasses | NOT RUN |
| Final Phase 2 placement/lifecycle coverage across all item & nonitem owner families | OPEN |

**Next Stage 2.3C-2 family:** `BlockPlaceContext / UseOnContext / DirectionalPlaceContext / ScaffoldingBlockItem / PlaceOnWaterBlockItem / SolidBucketItem` target/author dispatch and no-player synthetic paths, with applied 21.1.215 patch/ASM and strict bytecode tests. Do not promote all 87 ITEM classes or 241 BLOCK classes from this one family test.
