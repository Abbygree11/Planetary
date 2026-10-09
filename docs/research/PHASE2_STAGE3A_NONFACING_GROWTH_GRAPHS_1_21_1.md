# Phase 2 Stage 3A-2.3 — non-FACING growth graphs (12 actual registered classes)

**Research-only checkpoint**, 2026-10-10, Planetary `2.0`.
Target: **Minecraft 1.21.1** / **NeoForge 21.1.215**.

**Evidence:** compiled NeoForge registry classes/owners VERIFIED by
original [CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact `11643813158` (source revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`,
ZIP SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`).
Comparative Minecraft 1.21.1 method body source pinned to
[hackersense/OptiFine-Source `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block).

**STRICT BOUNDARY:** original NeoForge Java-21 reflection identifies exact
nearest **method declarations**, not compiled INVOKEs or Mixin application.
Comparative source is NOT proof of exact NeoForge-modified method bodies.
No game client, Java runtime or test change in this packet.
No bytecode-path or gameplay PASS.

## Registry selection and why property heuristics miss it

Exactly **12 new concrete classes** / **12 registered block IDs**,
all originally `REVIEW_PENDING`, and all `has_orientation_candidate=false`
in the **original** NeoForge artifact. They are not classes already
counted among the previous 35. Their actual registered property sets
are either `age`, `age,berries`, `berries`, `age,leaves,stage`,
or empty; **none** has `facing`.

8 classes are four head/body pairs sharing **nonregistered base**
`GrowingPlantBlock` -> `GrowingPlantHeadBlock` /
`GrowingPlantBodyBlock`:
Kelp (UP), Cave Vines (DOWN), Weeping Vines (DOWN),
Twisting Vines (UP). Also 4 growth-column classes:
Sugar Cane, Cactus, Bamboo Stalk, Bamboo Sapling.

## Verified exact-signature owners

Owner triple below: `getStateForPlacement(BlockPlaceContext)` /
`canSurvive(BlockState,LevelReader,BlockPos)` /
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)` /
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`.
The separate **scheduled** `tick(BlockState,ServerLevel,BlockPos,RandomSource)`
owner is displayed in another column to prevent conflation.

| Registered concrete class | Registered ID | Exact owner quadruple (placement / survive / shape / randomTick) | Scheduled tick owner | P groups | Comparative evidence |
|---|---|---|---|---|---|
| `KelpBlock` | `minecraft:kelp` | `KelpBlock / GrowingPlantBlock / GrowingPlantHeadBlock / GrowingPlantHeadBlock` | `GrowingPlantBlock` | P22,P24,P29,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/KelpBlock.java#L75) |
| `KelpPlantBlock` | `minecraft:kelp_plant` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantBodyBlock / BlockBehaviour` | `GrowingPlantBlock` | P22,P24,P29,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/KelpPlantBlock.java#L29) |
| `CaveVinesBlock` | `minecraft:cave_vines` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantHeadBlock / GrowingPlantHeadBlock` | `GrowingPlantBlock` | P22,P24,P29,P34 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CaveVinesBlock.java#L51) |
| `CaveVinesPlantBlock` | `minecraft:cave_vines_plant` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantBodyBlock / BlockBehaviour` | `GrowingPlantBlock` | P22,P24,P29,P34 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CaveVinesPlantBlock.java#L38) |
| `WeepingVinesBlock` | `minecraft:weeping_vines` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantHeadBlock / GrowingPlantHeadBlock` | `GrowingPlantBlock` | P22,P24,P29 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeepingVinesBlock.java#L23) |
| `WeepingVinesPlantBlock` | `minecraft:weeping_vines_plant` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantBodyBlock / BlockBehaviour` | `GrowingPlantBlock` | P22,P24,P29 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeepingVinesPlantBlock.java#L21) |
| `TwistingVinesBlock` | `minecraft:twisting_vines` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantHeadBlock / GrowingPlantHeadBlock` | `GrowingPlantBlock` | P22,P24,P29 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TwistingVinesBlock.java#L23) |
| `TwistingVinesPlantBlock` | `minecraft:twisting_vines_plant` | `GrowingPlantBlock / GrowingPlantBlock / GrowingPlantBodyBlock / BlockBehaviour` | `GrowingPlantBlock` | P22,P24,P29 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TwistingVinesPlantBlock.java#L21) |
| `SugarCaneBlock` | `minecraft:sugar_cane` | `Block / SugarCaneBlock / SugarCaneBlock / SugarCaneBlock` | `SugarCaneBlock` | P22,P24,P30,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SugarCaneBlock.java#L97) |
| `CactusBlock` | `minecraft:cactus` | `Block / CactusBlock / CactusBlock / CactusBlock` | `CactusBlock` | P22,P24,P30,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CactusBlock.java#L111) |
| `BambooStalkBlock` | `minecraft:bamboo` | `BambooStalkBlock / BambooStalkBlock / BambooStalkBlock / BambooStalkBlock` | `BambooStalkBlock` | P20,P22,P24,P30 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooStalkBlock.java#L103) |
| `BambooSaplingBlock` | `minecraft:bamboo_sapling` | `Block / BambooSaplingBlock / BambooSaplingBlock / BambooSaplingBlock` | `BlockBehaviour` | P20,P22,P24,P30 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooSaplingBlock.java#L49) |

For all 12, `setPlacedBy` resolves to `Block`, and
`neighborChanged` resolves to `BlockBehaviour` in the
original reflection scan. CaveVines head and body each declare
their own `useWithoutItem`; all 10 other classes resolve
that exact signature to `BlockBehaviour`.
`KelpBlock` has **two overloads**:
`getStateForPlacement(BlockPlaceContext)` is KelpBlock,
while `getStateForPlacement(LevelAccessor)` is
`GrowingPlantHeadBlock`. This is a critical method-signature
trap and must not be silently flattened.

## Full family algorithm pathways

### P29: directional head/body chain is not BlockState FACING

[`GrowingPlantBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GrowingPlantBlock.java#L37)
stores `growthDirection` as an **instance field**, not as
`BlockState.FACING`. Vanilla values are UP for kelp/twisting
and DOWN for cave/weeping. Its placement checks
`pos.relative(growthDirection)` to decide if the placed
segment should be a **head** or a **body**.
Its inherited `canSurvive` instead checks the cell
`pos.relative(growthDirection.opposite)` with
`canAttachTo` plus same-species head/body or face-sturdy
support; non-directional BlockState is no evidence of
spatial independence.

[`GrowingPlantHeadBlock.randomTick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GrowingPlantHeadBlock.java#L48)
checks AGE, probability and `canGrowInto` at the
growth-direction successor, then writes another head.
Its `updateShape` ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GrowingPlantHeadBlock.java#L82))
reacts to both **support** and **growing end**; growth-end
contact with another head/body converts this head to a body,
optionally preserving subclass payload.
[`GrowingPlantBodyBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GrowingPlantBodyBlock.java#L37)
converts an exposed body back into a head and can schedule
fluid ticks. **Do not** replace neighbor direction callbacks
with fabricated physical coordinates. Use the source-local
chart at the source cell for the role of the edge, but
preserve physical `BlockPos` for target access and
actual update event directions.

[`GrowingPlantBodyBlock.performBonemeal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GrowingPlantBodyBlock.java#L81)
uses `BlockUtil.getTopConnectedBlock` to locate the
actual head in a **directional graph** and delegates to
head `performBonemeal`; the head can directly create
multiple cells ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GrowingPlantHeadBlock.java#L123)).
A single local-direction redirect of `canSurvive` does
not cover traversal, head/body conversion, bonemeal,
new growth writes, or neighbor notifications.

### Four pair-specific modifiers

- **Kelp**: full water-source placement gate in
  [`KelpBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/KelpBlock.java#L75),
  grow into WATER, forbid attaching to MAGMA, both
  head/body fluid states are source-water. `scheduleFluidTicks`
  is true; fluid evolution belongs to **Phase 5**, while
  the source block graph belongs to Phase 2.
- **Cave vines**: `BERRIES` payload must survive
  head -> body ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CaveVinesBlock.java#L55))
  and body -> head ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CaveVinesPlantBlock.java#L40)).
  Growth applies distinct berry RNG; picking berries
  invokes `CaveVines.use` from **both** classes,
  and direct bonemeal can set BERRIES rather than extend
  the vine. This is also P34 interaction, not only P29.
- **Weeping vines**: DOWN growth direction, `NetherVines`
  successor predicate
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeepingVinesBlock.java#L23)).
  No direct item is registered for the body.
- **Twisting vines**: UP growth direction, same
  `NetherVines` successor family
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TwistingVinesBlock.java#L23)),
  different local support/growth axis role.

### P30: columns with four-way adjacency and multi-cell writers

- **Sugar cane**: [`canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SugarCaneBlock.java#L97)
  accepts a lower cane, otherwise requires permitted
  substrate with **adjacent fluid water or frosted ice**
  checked in `Direction.Plane.HORIZONTAL` around the
  **supporting cell**, not indiscriminately around current
  cell. [`randomTick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SugarCaneBlock.java#L57)
  checks above vacancy, scans below column length and
  grows above; `updateShape` queues a later survival
  `tick`. Both vertical scan and source-local tangent
  adjacency must be adapted coherently.
- **Cactus**: [`canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CactusBlock.java#L111)
  rejects nearby solid blocks or lava in **four tangent
  directions**, requires CACTUS or SAND below and checks
  whether the **above** block is liquid. A column growth
  rewrite must preserve that special **negative** adjacency
  predicate; don't naively delegate to `BushBlock`.
  Growth scans height and creates blocks above
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CactusBlock.java#L55));
  failures trigger a scheduled delayed break, not necessarily
  an immediate block replacement.
- **Bamboo stalk**: [`getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooStalkBlock.java#L103)
  can return **`Blocks.BAMBOO_SAPLING` state** even though
  the method belongs to `BambooStalkBlock`. Branches read
  local below and above, neighboring AGE, and refuse
  nonempty fluid. Random growth checks light above,
  scans up/down with a max length **16**, updates AGE,
  LEAVES and STAGE; its `updateShape` responds to
  physical UP neighbor with age copying
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooStalkBlock.java#L180)).
  Bonemeal independently authors multiple new cells.
- **Bamboo sapling**: checks below `BAMBOO_PLANTABLE_ON`
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooSaplingBlock.java#L58));
  natural random growth and bonemeal use a **direct**
  `growBamboo` writing BAMBOO above
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooSaplingBlock.java#L111)).
  `updateShape` can **replace the sapling with BAMBOO**
  when it detects bamboo above
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BambooSaplingBlock.java#L64)).
  This is a separate alternate state author and not
  reducible to `getStateForPlacement` after placement.

## Registered item producers: actual item census join

Original `phase2-neo1211-item-registry.tsv` confirms:

| Block-state target | Registered item author |
|---|---|
| `minecraft:kelp` | `minecraft:kelp` → `BlockItem` |
| `minecraft:cave_vines` | `minecraft:glow_berries` → `ItemNameBlockItem` |
| `minecraft:weeping_vines` | `minecraft:weeping_vines` → `BlockItem` |
| `minecraft:twisting_vines` | `minecraft:twisting_vines` → `BlockItem` |
| `minecraft:sugar_cane` | `minecraft:sugar_cane` → `BlockItem` |
| `minecraft:cactus` | `minecraft:cactus` → `BlockItem` |
| `minecraft:bamboo` | `minecraft:bamboo` → `BlockItem` |
| `kelp_plant`, `cave_vines_plant`, `weeping_vines_plant`, `twisting_vines_plant`, `bamboo_sapling` | **No directly registered BlockItem** |

Count = **7 directly registered plant-placing items**,
five runtime variants with no direct item. In particular,
`minecraft:bamboo` may create `minecraft:bamboo_sapling`
and `glow_berries` creates `cave_vines`. The five
variant blocks can be authored by lifecycle changes
and generation even without registered items.
Item-name lookups do not replace actual
`BlockItem.getPlacementState` or bonemeal writers.
Separate Stage 3B still must verify runtime dispatch
and NeoForge modifications.

## Adapter and cross-phase disposition

Inspection of existing Planetary `src/main/java/dev/planetary/mixin`
showed **no dedicated mixin whose name targets this GrowingPlant/
Kelp/CaveVines/Weeping/Twisting/Cactus/SugarCane/Bamboo family**.
That is a **source inventory result**, NOT proof that no
generic or external mod hook reaches any of its functions.
Source-candidate family adaptation must later cover:
`GrowingPlantBlock`, Head/Body and `BlockUtil` traversal,
canSurvive/neighbor-update/scheduled tick, alternative
bonemeal and growth authors, per-block spatial predicates,
and source-vs-target frame at seam. Avoid scattered
per-class Mixin patches without a coherent mechanism map.

P29 and P30 are **Phase-2 growth topology/state**;
P22/P24 support and invalidation are Phase 2;
P34 berries interactions Phase 2;
P37 Kelp water state and scheduling bridge to
**Phase 5** physics; worldgen of plants bridges to
**Phase 8**. Physical hit and BlockPos must remain
physical in non-Planet worlds.

## Research gates / next task

**Cumulative ledger:** 47/241 class-level source+reflection
reviewed (**93** registered block IDs), **194/241**
REVIEW_PENDING (**967** block IDs). Original class
denominator **241**, ID denominator **1060** unchanged.

For these 12, source reviewed + nearest declaring owner
in NeoForge's original reflection ZIP VERIFIED. Their
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`, and
`gameplay_acceptance` stay `REVIEW_PENDING`.
No new Java code, no compile/test or user in-game PASS.

**NEXT** (another answer only): 2.3A-2.4, final
registry/census invariant and disposition verification,
then move to later source families under the next
specifically scoped checkpoint; do not declare 241/241
semantically reviewed.
