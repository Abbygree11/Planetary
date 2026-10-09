# Stage 3A-3.2 — exact registered item authors and alternative state writers for connected graphs

**Checkpoint:** 2026-10-10. Minecraft 1.21.1 / NeoForge
21.1.215, Java 21, Planetary branch `2.0`.

**Evidence scope: ITEM registry and alternative author source audit
for the same eight classes from Stage 3A-3.1.**
No newly classified classes, no code modification, no NeoForge
patch-body or Mixin-invocation analysis, **no gameplay PASS**.

## 1. Exact compiled item registry join (not a guess by item name)

Original unmodified GitHub Actions [registry artifact
11643813158](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
original source SHA `aa39572950a15403ea0a9003eefccf3bf6675ff7`,
ZIP SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.

Joined exact `phase2-neo1211-block-registry.tsv` entries (1060
block IDs) to all `phase2-neo1211-item-registry.tsv`
entries (1333 item IDs) by the **actual `placed_block`
field**, never guessed `id + suffix`. Results:

| Registered concrete BLOCK class | Exact BLOCK IDs | Direct registered ITEM instances | ITEM implementation |
|---|---:|---:|---|
| `FenceBlock` | 12 | 12 | `BlockItem` |
| `FenceGateBlock` | 11 | 11 | `BlockItem` |
| `WallBlock` | 25 | 25 | `BlockItem` |
| `IronBarsBlock` | 2 | 2 | `BlockItem` |
| `StainedGlassPaneBlock` | 16 | 16 | `BlockItem` |
| `VineBlock` | 1 | 1 | `BlockItem` |
| `GlowLichenBlock` | 1 | 1 | `BlockItem` |
| `SculkVeinBlock` | 1 | 1 | `BlockItem` |
| **Total** | **69** | **69** | **69 `BlockItem`** |

All 69 ITEM records satisfy:
`block_item=true`,
`java_class=net.minecraft.world.item.BlockItem`,
`registry_id=placed_block`, unique exact `placed_block`
per ITEM row, and exact coverage of all 69 target block IDs.
**No** `StandingAndWallBlockItem` or `ItemNameBlockItem`
for these eight concrete classes. This differs from earlier
torch/bamboo cases and cannot be inferred for unreviewed classes.

The **compiled nearest item method-declaration owners** are
`BlockItem` for all **69** of each of:
`useOn(UseOnContext)`, `place(BlockPlaceContext)`,
`updatePlacementContext(BlockPlaceContext)`,
`getPlacementState(BlockPlaceContext)`,
`placeBlock(BlockPlaceContext,BlockState)`,
`canPlace(BlockPlaceContext,BlockState)`,
`registerBlocks(Map,Item)`.
This confirms *registered instance implementation and
method-declaring owner*; it is not a call trace, bytecode
transform check, or observed gameplay behavior.
The full list of 69 **individual block IDs** stays in the
[verified class ledger](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).

## 2. Normal placement calls, then post-placement mutation

Comparative 1.21.1 source
[`BlockItem.useOn`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L46)
builds a `BlockPlaceContext`; `place`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L61))
runs `updatePlacementContext`,
`getPlacementState`, `canPlace`, `placeBlock` and
then re-reads the *placed* state from the physical world.
[`getPlacementState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L152)
dispatches to the concrete block's
`getStateForPlacement(BlockPlaceContext)`, or to the
inherited declaring owner, with a survival/unobstructed check.

**Secondary author after initial placement:**
[`BlockItem.updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158)
reads the `DataComponents.BLOCK_STATE`
`BlockItemStateProperties` component, applies property
overrides to the already-written block state and may
write it again with `level.setBlock(..., 2)`.
It can therefore overwrite authored `NORTH/EAST/SOUTH/WEST`,
`UP`, `FACING`, `IN_WALL`, `WATERLOGGED` or
multi-face bits **when they exist and are provided in
the item component**. This is a *potential bypass*, not
proof that ordinary survival items use those overrides.
Source/target orientation policy for components, commands,
structure transport and fake players belongs to Stage 3B
and P35; do not invent an automatic world-side rotation
for these arbitrary serialized components.

The normal path also invokes
`Block.setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`
(the reflecting owner for all eight) after component updates,
subject to the source's block identity check.
This does not imply `setPlacedBy` re-evaluates every
connected property. Both method dispatch and active
NeoForge patch/apply sites remain separate future gates.

## 3. Other state writers and side effects per algorithm

| Family | Evidence-backed alternative author | Why `BlockItem.getPlacementState` alone misses it |
|---|---|---|
| Fence / stained panes | `FenceBlock.updateShape`, `IronBarsBlock.updateShape` | Neighbour `Direction` callback toggles one local tangent; water ticks/shape-cache render effects are distinct |
| Gate | `FenceGateBlock.useWithoutItem`, `neighborChanged`, `onExplosionHit`, `updateShape` | Writes `OPEN`, sometimes changes `FACING`, sets `POWERED`, recomputes `IN_WALL`, including power/explosion triggers |
| Wall | `WallBlock.updateShape` / helper `updateSides`, `shouldRaisePost` | Independently authors `WallSide.NONE/LOW/TALL` and `UP` via above collision face, even with no item |
| Vine | `VineBlock.randomTick`, `getUpdatedState` / `updateShape` | Grows extra faces or positions; prunes unsupported faces; vertical support can be supplied by vine above |
| Glow lichen | `GlowLichenBlock.performBonemeal` -> `MultifaceSpreader` | Chooses source face/target and **directly places** new coverage states; no `BlockPlaceContext` |
| Sculk vein | `SculkVeinBlock.regrow`, `attemptUseCharge` -> `attemptPlaceSculk`, `onDischarged`; `MultifaceSpreader` | Direct writes, charges, neighbour conversion, removed faces and water replacement; growth/configuration differs from glow lichen |
| Both `MultifaceBlock` subclasses | `MultifaceGrowthFeature.placeGrowthIfPossible` when configured for that target type; `MultifaceSpreader.SpreadConfig.placeBlock` | Calls **overloaded** `MultifaceBlock.getStateForPlacement(BlockState,BlockGetter,BlockPos,Direction)`, not the player's `BlockPlaceContext` overload |

**Precise comparative method evidence:**

- [Fence gate useWithoutItem](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L185)
  toggles OPEN; opening opposite to the player's direction
  can also write `FACING`. [Gate neighborChanged](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L240)
  responds to redstone signal by mutating `POWERED/OPEN`;
  functional signal network is **Phase 7A**.
  [Gate onExplosionHit](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L219)
  toggles OPEN when explosion interaction permits
  and it is not powered. These are distinct from
  `getStateForPlacement`/neighbor-connection updates.
- [FenceBlock.useItemOn](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java#L86)
  and [`useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java#L101)
  participate in leash interactions, with
  [`LeadItem.bindPlayerMobs`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/LeadItem.java#L49).
  This is **an entity/leash interaction**, not a
  second placement author for the fence block.
  Reframing fence direction properties must not
  accidentally swallow these separate interactions.
- [VineBlock.randomTick](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L223)
  writes same-block new faces or new vines on
  other cells, gated by `RULE_DO_VINES_SPREAD`
  and density bounds; [`getUpdatedState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L163)
  / [`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L209)
  may drop one or all unsupported attachment faces.
  The physical target may be above, below or to
  the tangent side depending on source local chart.
- [GlowLichenBlock.performBonemeal](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GlowLichenBlock.java#L85)
  selects `spreader.spreadFromRandomFaceTowardRandomDirection`.
  [`MultifaceSpreader.getSpreadFromFaceTowardDirection`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceSpreader.java#L91)
  computes an actual `SpreadPos` (physical position,
  target face), while
  [`SpreadConfig.placeBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceSpreader.java#L176)
  calls the **non-player** placement overload and
  directly `setBlock` at the selected target.
  `MultifaceSpreader` is a separate *authoring service*,
  not a virtual `BlockItem.place` override.
- [`MultifaceGrowthFeature.placeGrowthIfPossible`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/MultifaceGrowthFeature.java#L72)
  independently uses the same non-player overload,
  writes a new block and can invoke its spreader.
  The applicable configured feature(s) and modded
  generator policy need **Phase 8** confirmation;
  finding this generic code does not prove every
  feature configuration targets the two reviewed
  block classes.
- [`SculkVeinBlock.regrow`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L55)
  constructs face flags and writes the placed state.
  [`onDischarged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L88)
  removes faces adjacent to sculk and changes
  fully cleared states into air/water as appropriate.
  [`attemptPlaceSculk`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L129)
  converts one adjacent replaceable block to SCULK,
  updates entities, spreads coverage and calls
  neighbour discharge. Its
  [`SculkVeinSpreaderConfig.stateCanBeReplaced`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L231)
  rejects disallowed fluids and special two-cell
  obstructions. This must not be unified with
  GlowLichen's bonemeal-only spreading policy.
- [`WallBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L193)
  has a **second unrelated same-named helper overload**
  that calculates height and UP post
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L251)).
  Changing a single method-name match would silently
  miss the helper and above-cell shape policy.

## 4. Mechanism handoff and constraints

- **P25/P26/P27, Phase 2:** local authored property/face
  slots, real physical target positions, neighbour
  update propagation, offset spanning both local
  source and target frames; no spurious effects on
  world-default gravity worlds.
- **P34/P35, Phase 2 and Stage 3B:** player
  interactions, `DataComponents.BLOCK_STATE`,
  clone/structure/NBT/commands, alternative items,
  optional fake player contexts; **69 ordinary
  BlockItems does not exclude these other writers**.
- **P37, Phase 5:** `WATERLOGGED` identity and tick
  scheduling from fence/wall/panes/lichen/sculk vein
  must survive any Phase 2 local-state rewrite.
  No hydrodynamics acceptance claimed.
- **P36, Phase 7A:** gate signal/POWERED/OPEN;
  TripWire/TripWireHook are **still REVIEW_PENDING**
  and outside this eight-class item join.
- **Phase 3:** `CrossCollisionBlock` shape cache,
  `WallBlock` collision and height logic, pane face
  culling. A correct BlockState author is necessary
  but not sufficient for correct drawn shape.

The source shows **alternative authors**, but the
compiled item census only verifies the seven
`BlockItem` effective owner signatures listed above.
It does **not** verify patched NeoForge bytecode,
active Mixins, runtime semantics of source/target
direction frames, or in-game outcomes. The original
eight TSV rows remain
`SOURCE_REVIEWED_INTEGRATION_PENDING` and
`REFLECTION_OWNER_VERIFIED`; all three acceptance
fields remain `REVIEW_PENDING`.

## Checkpoint and next task

The total ledger remains **241 concrete classes /
1060 registered IDs**; **55 reviewed (162 IDs)**,
**186 REVIEW_PENDING (898 IDs)**. This package added
**item and alternate-author evidence, not 8 new classes**.

**Next FIRST microtask:** Stage `2.3A-3.3`, original
card checkbox **3**, specifically cached BlockStateBase
and source/target physical neighbor semantics for these
graph families, plus exact Phase 2/3/5/7A handoff.
No Java patch until owner mechanisms and patch
injection gates are properly evidenced.
