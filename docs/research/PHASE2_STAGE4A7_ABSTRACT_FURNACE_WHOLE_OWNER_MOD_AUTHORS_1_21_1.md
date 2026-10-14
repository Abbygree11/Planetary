# Planetary — 2.4A-7: inherited AbstractFurnaceBlock placement owner

Target: Minecraft 1.21.1 / NeoForge **21.1.215** / Java 21. Branch: `2.0`.

## Verified original declaring owner (P08)

[Original comparative 1.21.1 source, pinned](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AbstractFurnaceBlock.java): `AbstractFurnaceBlock.getStateForPlacement(BlockPlaceContext)` returns `defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())`. Original subclasses `FurnaceBlock`, `SmokerBlock`, `BlastFurnaceBlock` do **not** declare another getStateForPlacement; all three use base algorithm (including separate `newBlockEntity`, `openContainer`, animated particles). `HorizontalDirectionalBlock` merely defines the FACING property and generic rotate/mirror; it is **NOT the owner of placement**. Original exact NeoForge 21.1.215 classpath owner and Mixin callback ABI pinned by `Phase2PlacementFamilyMixinContractTest`.

## Whole family implementation

`PlanetFurnacePlacement.frontFromAuthor` takes optional author direction, rejects non-horizontal/unknown body heading, applies the original opposite rule. `afterPlacement` runs after vanilla return, resolves target canonical frame using shared `PlanetBlockRuntime`, obtains local actor heading from `PlanetFrameApi.localHorizontalDirection` for a real player, or transforms the explicit **physical** heading from a `DirectionalPlaceContext` synthetic placement. No player or ambiguous author -> leave original vanilla result intact. Write **only** `AbstractFurnaceBlock.FACING` into the **original** state. Preserve actual furnace/smoker/blast furnace identity, LIT and any extension properties. Mixin on base class uses cancellable `@Inject(... at=RETURN)` instead of canceling original at HEAD. No global direction replacement.

Other owner methods are **not adapted because they have independent semantics**: cooking tick/recipe/block-entity lifecycle, container menu, redstone comparator, physical item inventory, BlockState rotate/mirror, sound, particle emitter, client renderer. Their state/orientation usage may require Phase 3 (render), Phase 4 (particles), Phase 7A (redstone) or other owner-specific follow-ups; do not label those areas complete just because placement succeeded.

### Safe-depth / mod contract

| Class | Explicit boundary |
| --- | --- |
| SHARED_SAFE | One inherited AbstractFurnaceBlock placement method: base 3 registered vanilla forms, plus modded classes inheriting same method |
| OPT_IN_ALGORITHM | Real actor heading converted player-body local -> target canonical; explicit synthetic physical heading -> target canonical; only FACING write |
| MOD_INDEPENDENT | Any mod machine that **overrides** `getStateForPlacement`, defines own BlockItem, FACING property/axis domain, block entity or contraption Level does NOT automatically inherit this fix |
| PHYSICAL_DO_NOT_TOUCH | BlockItem lifecycle, BlockEntity, recipe/fluid/fuel storage, redstone, world BlockPos/Direction, structure/worldgen placement, mod fake levels, model/BER |

The same game concept "furnace" is NOT a proof of Java inheritance: modded furnaces can have independent placement authors. `create-addon` on NeoForge 1.21.1 illustrates an independent Create kinetic block `GrinderWheelBlock extends ShaftBlock` rather than AbstractFurnaceBlock; it is an **independent mod machine comparator**, not a tested compatible furnace. Specific mod loader JARs/declaring owners must be inspected and actually loaded before claiming support. More general `HorizontalDirectionalBlock` injection would break other source-authored algorithms (VaultBlock, ChestBlock, FenceGateBlock), while three subclass-specific patches needlessly miss inheriting new material/feature subclasses.

## Automated and in-game acceptance

- Semantic: each of six canonical faces × four actor horizontal directions × three concrete furnace subclasses and LIT=true; unknown/vertical/non-Planet fall-through.
- Exact NeoForge ABI/ASM: declaring base owner, no three subclass overrides, compiled RETURN handler, signature/staticness and Mixin config.
- Lab V4: current-only 6 face pads, 8 furnace/smoker/blast stations, EMERALD V3 → NETHERITE V4 and older DIAMOND/GOLD recognized. CYAN reference vs LIME actual player BlockItem placement.
- Independently OPEN until run/observed: green compile/full JUnit/registry; actual client apply, furnace GUI/cooking/LIT/particles, all six faces and seam/corner natural placement, ordinary no-Planet world, and real compatible loaded mod inheritor + independent override.

The known generic side-quad/occlusion defect on cube face seams belongs to Phase 3B, not this stage.
