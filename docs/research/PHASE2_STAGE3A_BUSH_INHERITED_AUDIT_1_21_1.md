# Stage 3A: BushBlock inherited-survival family (second 12-class batch)

**Research checkpoint 2.3A-1 / task 4**, 2026-10-10.
Target: Minecraft 1.21.1 / NeoForge 21.1.215, branch `2.0`.

**Status: SOURCE_REVIEWED / REFLECTION_OWNER_VERIFIED;
NEOFORGE ASM/MIXIN and GAMEPLAY PENDING.** This document does NOT
assert correct local-gravity placement, growth or cross-edge behavior.

## Provenance

1. Actual runtime registry: [CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
   artifact ID `11643813158` generated from `aa395729`.
   The **full** `phase2-neo1211-block-registry.tsv` identifies
   12 distinct concrete classes, 34 actual block IDs; its class
   hierarchy and `effective_method_owners` identify the
   exact-signature declaration owners shown below.
2. Comparative 1.21.1 Java source pinned to
   [`hackersense/OptiFine-Source` `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block).
   This is **not** verified equivalent to the exact
   NeoForge 21.1.215 patched classfile.
3. Existing Planetary [`BushBlockLocalSupportMixin`](../../src/main/java/dev/planetary/mixin/BushBlockLocalSupportMixin.java)
   redirects **only** the `below()` call in
   `BushBlock.canSurvive`. It does not automatically
   reframe growth, world features, fluid checks, bonemeal
   or the target block selected by an Item.

## Full registered-class ownership matrix

Owner columns: `getStateForPlacement(BlockPlaceContext)` /
`canSurvive(BlockState,LevelReader,BlockPos)` /
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)` /
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`.
These are nearest **declaring classes**, not proven JVM INVOKE sites.

| Actual concrete class | Block IDs | Effective method owners (placement / survive / shape / randomTick) | Mechanisms | Comparative evidence |
|---|---:|---|---|---|
| `SaplingBlock` | 7 | `Block` / `BushBlock` / `BushBlock` / `SaplingBlock` | P22,P30 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SaplingBlock.java#L52) |
| `AzaleaBlock` | 2 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22,P30 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AzaleaBlock.java#L40) |
| `FungusBlock` | 2 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22,P30 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FungusBlock.java#L59) |
| `RootsBlock` | 2 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RootsBlock.java#L36) |
| `NetherSproutsBlock` | 1 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/NetherSproutsBlock.java#L35) |
| `TallGrassBlock` | 2 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P20,P22,P30,P34 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TallGrassBlock.java#L51) |
| `DeadBushBlock` | 1 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DeadBushBlock.java#L36) |
| `FlowerBlock` | 13 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FlowerBlock.java#L23) |
| `WitherRoseBlock` | 1 | `Block` / `BushBlock` / `BushBlock` / `BlockBehaviour` | P22 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WitherRoseBlock.java#L48) |
| `SweetBerryBushBlock` | 1 | `Block` / `BushBlock` / `BushBlock` / `SweetBerryBushBlock` | P22,P30,P34 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SweetBerryBushBlock.java#L79) |
| `SeagrassBlock` | 1 | `SeagrassBlock` / `BushBlock` / `SeagrassBlock` / `BlockBehaviour` | P20,P22,P24 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SeagrassBlock.java#L49) |
| `NetherWartBlock` | 1 | `Block` / `BushBlock` / `BushBlock` / `NetherWartBlock` | P22,P30 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/NetherWartBlock.java#L51) |

Block IDs by class, with exact IDs and source-line URLs, are
recorded in [the 241-class ledger](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).
All 12 `canSurvive` owners are `BushBlock`;
11 `updateShape` owners are `BushBlock`, while
`SeagrassBlock` overrides updateShape to schedule water ticks
after the base survival update. All 12 `setPlacedBy` owners
are `Block`; all 12 `neighborChanged` owners
are `BlockBehaviour` in the reflection census.
Only `SweetBerryBushBlock` overrides `useItemOn`
and `useWithoutItem` among these 12.

## Exact comparative source paths / escape hatches

### Base support and natural player placement

[**BushBlock.canSurvive / mayPlaceOn**](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BushBlock.java#L24)
uses `pos.below()`, then virtual `mayPlaceOn`.
The source's `updateShape` calls virtual `canSurvive` and
delegates to the superclass. The current base support Mixin
can therefore adapt the **source-local supporting cell** for
these inheriting descendants, **but only this call-path**.
`Block.getStateForPlacement` is the registered declaring
owner for 11 classes and `SeagrassBlock.getStateForPlacement`
is the exception. Physical `BlockPlaceContext.getClickedPos()`
and hit normal must remain untouched until the block-state
authorship policy actually requires local conversion.
Item creation is an independent Stage 3B owner.

Concrete virtual `mayPlaceOn` behavior:

- `SaplingBlock`, `TallGrassBlock`, `FlowerBlock`:
  inherited default dirt/farmland substrate.
- `AzaleaBlock`: additional CLAY support
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AzaleaBlock.java#L40)).
- `FungusBlock`: additional NYLIUM / MYCELIUM / SOUL_SOIL
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FungusBlock.java#L59)).
- `RootsBlock` and `NetherSproutsBlock`: additional NYLIUM /
  SOUL_SOIL ([roots](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RootsBlock.java#L36),
  [sprouts](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/NetherSproutsBlock.java#L35)).
- `DeadBushBlock`: `DEAD_BUSH_MAY_PLACE_ON` tag, not generic
  dirt ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DeadBushBlock.java#L36)).
- `WitherRoseBlock`: extends `FlowerBlock` substrate policy
  with NETHERRACK / SOUL_SAND / SOUL_SOIL
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WitherRoseBlock.java#L48)).
- `NetherWartBlock`: **SOUL_SAND only**
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/NetherWartBlock.java#L51)).
- `SeagrassBlock`: supporting target must be sturdy on its
  **local UP** side and not MAGMA; global Direction.UP in
  [the predicate](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SeagrassBlock.java#L49) must be reconciled
  with the *support block's physical side* at face seams.
- `SweetBerryBushBlock` inherits the generic Bush substrate.

Do not replace these virtual predicates with a single convenient
`canSupportCenter` predicate; that changes Minecraft's soil rules.

### Growth and authored multi-cell state (not covered by base support)

- `SaplingBlock.randomTick` reads brightness at
  `pos.above()`, applies the original RNG gate and uses
  `advanceTree` -> `TreeGrower.growTree`
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SaplingBlock.java#L52)).
  **Source-local UP** illumination sampling and
  **Phase-8** tree-generation geometry must be classified
  separately. Do not change global skylight semantics by
  mechanically rotating `getRawBrightness`.
- `AzaleaBlock` bonemeal checks `pos.above()` fluid
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AzaleaBlock.java#L46))
  before calling `TreeGrower.AZALEA`
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AzaleaBlock.java#L58)).
- `FungusBlock` bonemeal tests a required **below substrate**
  then places a configured huge-fungus feature
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FungusBlock.java#L73)).
  Generated feature geometry belongs to Phase 8; simply
  changing its source position may be insufficient.
- `TallGrassBlock.performBonemeal` checks above vacancy
  and directly calls `DoublePlantBlock.placeAt`
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TallGrassBlock.java#L51)).
  This bypasses a player's `BlockItem.place` and invokes the
  separate double-height topology already audited in
  [the previous batch](PHASE2_STAGE3A_BUSH_DESCENDANT_AUDIT_1_21_1.md).
- `SeagrassBlock` is an independent water path:
  only full-source water permits placement
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SeagrassBlock.java#L56));
  `updateShape` schedules the WATER fluid tick
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SeagrassBlock.java#L63));
  bonemeal converts it into `TallSeagrassBlock`
  by physically writing `pos.above()`
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SeagrassBlock.java#L96)).
  **Phase 5** owns fluid simulation; source-cell selection and
  created multi-cell state are Phase-2 concerns.
- `SweetBerryBushBlock.randomTick` checks brightness
  above and increments age
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SweetBerryBushBlock.java#L79)).
  It independently implements harvesting (no-item
  interaction) and a bonemeal-dependent `useItemOn`
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SweetBerryBushBlock.java#L113)).
  `entityInside` checks movement in raw XZ and applies
  stuck/damage effects ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SweetBerryBushBlock.java#L92)):
  entity body-motion semantics belong to **Phase 7**.
  Do not mark berry behavior complete based on support.
- `NetherWartBlock`: distinct age/state randomTick
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/NetherWartBlock.java#L63)),
  no player-body orientation but dependent on local-down
  supporting SOUL_SAND.
- `RootsBlock`, `NetherSproutsBlock`, `DeadBushBlock`,
  `FlowerBlock`, `WitherRoseBlock`: no independent
  `randomTick` owner. WitherRose's `entityInside`
  applies a status effect ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WitherRoseBlock.java#L82));
  that is a separate entity interaction, not a support algorithm.

## Alternate creation and lifecycle gates

Cross-check with registered ITEM TSV:
ordinary `BlockItem` creates most reviewed IDs;
`SweetBerryBushBlock`'s placed-block item is
`minecraft:sweet_berries` of type `ItemNameBlockItem`,
and `NetherWartBlock` is placed from `minecraft:nether_wart`
of type `ItemNameBlockItem`. `SeagrassBlock`
has a regular `minecraft:seagrass` block item in the
registry, **but natural creation and bonemeal bypass
BlockItem just as importantly**.

No `getStateForPlacement` override for the other 11 is
evidence of local-orientation correctness. Audit
`BlockItem.place`, component-applied `BLOCK_STATE`,
worldgen, bone-meal `performBonemeal`, direct feature writes,
and synthetic contexts in later owner stage(s).
Do not rotate physical ray hit or raw positions globally.
Also preserve original feature RNG ordering, branch choices,
survival predicates and NeoForge mod extension points.

## Status and follow-up

12 additional concrete classes / **34** registered block IDs
classified `SOURCE_REVIEWED_INTEGRATION_PENDING` in the
single [owner ledger](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv).
Cumulative total now **24/241** concrete classes and
**52/1060** block IDs source-reviewed; **217/241** classes
and **1008/1060** block IDs remain `REVIEW_PENDING`.
Original previously researched **22 mechanism clusters** have not
been converted into additional unproven class-level PASS.

**Remaining hard gates:** exact patched NeoForge bytecode,
`BlockBehaviour.BlockStateBase`, ASM Mixin INVOKEs and handlers,
true item and bonemeal creation, local support survival all faces,
seam/corner, update tick/rate, third-party world, multiplayer, fluids,
generation and entity consumers. No new Java/Mixin runtime change;
no fresh client or CI acceptance was performed.

**NEXT (separate answer):** stage 2.3A-2
(`docs/phases/phase-02/02-block-owners-rest.md`),
select a next algorithm family of 8–15 still-pending classes,
using actual registry + method owner signatures. Prefer
non-Bush family (e.g. spatial attachment graphs) to avoid
blindly assuming all blocks are same as plants.
