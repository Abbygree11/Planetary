# Phase 2 Stage 3A — BushBlock inherited support bypasses (micro-task 2)

Target: **Minecraft 1.21.1**, **NeoForge 21.1.215**, Planetary `2.0`.
**Status:** comparative-source methods REVIEWED; actual registry/effective
reflection owners VERIFIED from compiled NeoForge census; NeoForge-specific
patches/bytecode call-site behavior and all-face gameplay **NOT VERIFIED**.
This is a research artifact, **not** an implementation or in-game PASS.

## Provenance and scope

- Compiled registry baseline: [Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
  artifact `11643813158` (code `aa395729`); parsed original
  `phase2-neo1211-block-registry.tsv` and
  `phase2-neo1211-item-registry.tsv` (not derived from guessed class names).
- Comparative Mojang-named reference:
  [hackersense/OptiFine-Source at b77c5c6995874f6cf2755bc5234428906b337b75](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block),
  **not** assumed to be exact transformed NeoForge implementation.
- Parent paths source-checked: [BushBlock.java:24](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BushBlock.java#L24)–39,
  [CropBlock.java:61](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L61)–200, [DoublePlantBlock.java:42](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L42)–95,
  [SaplingBlock.java:52](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SaplingBlock.java#L52)–85, [MushroomBlock.java:52](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MushroomBlock.java#L52)–101.
- Exact owner columns parsed with the first owner for each **method
  signature** (placement = `BlockPlaceContext`; survival = `LevelReader`;
  shape = `LevelAccessor`; randomTick = `ServerLevel`).
  Reflection owner is *not* per-call ASM INVOKE verification.

**Actual counts:** 59 registry IDs have `BushBlock` in `class_hierarchy`.
40 inherit its effective `canSurvive`; 19 bypass that declaration.
The **12 concrete types / 18 block IDs below account for 18 of
those 19 overrides**; the remaining one is `minecraft:sea_pickle`
(`SeaPickleBlock`), already described in the earlier Stage 3A owner
cluster, but NOT end-to-end accepted.

## Registered concrete classes and effective owners

Format in owner triple: **getStateForPlacement / canSurvive / updateShape**
(exact parameter signatures); tick is `randomTick` declaring owner.

| Concrete registered class | Registry block IDs (minecraft:) | Effective owner triple | Tick owner | Source-backed mechanism / risk | P groups | Source evidence |
|---|---|---|---|---|---|---|
| `BeetrootBlock` | `beetroots` | `Block / CropBlock / BushBlock` | `BeetrootBlock` | Crop: farmland+light; beetroot age 0..3, random gate 2/3 and adjusted bonemeal | P22/P30/P34 | [CropBlock.java:193](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L193), [BeetrootBlock.java:62](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BeetrootBlock.java#L62) |
| `CarrotBlock` | `carrots` | `Block / CropBlock / BushBlock` | `CropBlock` | Crop farmland/light, shared growthSpeed; own seed/age payload; no lifecycle override | P22/P30/P34 | [CropBlock.java:134](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L134), [CarrotBlock.java:1](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CarrotBlock.java#L1) |
| `CropBlock` | `wheat` | `Block / CropBlock / BushBlock` | `CropBlock` | Farmland only mayPlaceOn; sufficient light; growthSpeed reads physical below and 3x3 physical XZ + cardinal neighbors | P22/P30/P34 | [CropBlock.java:61](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L61), [CropBlock.java:134](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L134), [CropBlock.java:193](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L193) |
| `PotatoBlock` | `potatoes` | `Block / CropBlock / BushBlock` | `CropBlock` | Same inherited farmland/light/growth; own seed and item-state behavior; no lifecycle override | P22/P30/P34 | [CropBlock.java:193](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L193), [PotatoBlock.java:1](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PotatoBlock.java#L1) |
| `TorchflowerCropBlock` | `torchflower_crop` | `Block / CropBlock / BushBlock` | `TorchflowerCropBlock` | Age 2 becomes *different block* (torchflower); random gate + bonemeal age increment; preserve RNG | P22/P30/P34 | [TorchflowerCropBlock.java:73](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TorchflowerCropBlock.java#L73), [TorchflowerCropBlock.java:79](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TorchflowerCropBlock.java#L79) |
| `DoublePlantBlock` | `large_fern,pitcher_plant,tall_grass` | `DoublePlantBlock / DoublePlantBlock / DoublePlantBlock` | `BlockBehaviour` | HALF upper/lower; placement checks physical above and physical build-Y limit; upper creation/setPlacedBy; pair survival/update on world-axis Y | P20/P22 | [DoublePlantBlock.java:42](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L42), [DoublePlantBlock.java:62](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L62), [DoublePlantBlock.java:70](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L70), [DoublePlantBlock.java:90](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L90) |
| `TallFlowerBlock` | `lilac,peony,rose_bush,sunflower` | `DoublePlantBlock / DoublePlantBlock / DoublePlantBlock` | `BlockBehaviour` | Inherits all DoublePlant pairs; bone meal gives item, no orientation override | P20/P22/P34 | [TallFlowerBlock.java:28](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TallFlowerBlock.java#L28), [DoublePlantBlock.java:77](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L77) |
| `PitcherCropBlock` | `pitcher_crop` | `PitcherCropBlock / PitcherCropBlock / PitcherCropBlock` | `PitcherCropBlock` | Overrides initial placement to single default state + NO-OP setPlacedBy; age 3+ extends upward via growth; growthSpeed and lower/upper lookup use world Y | P20/P22/P30 | [PitcherCropBlock.java:58](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PitcherCropBlock.java#L58), [PitcherCropBlock.java:85](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PitcherCropBlock.java#L85), [PitcherCropBlock.java:134](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PitcherCropBlock.java#L134), [PitcherCropBlock.java:156](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PitcherCropBlock.java#L156), [PitcherCropBlock.java:203](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PitcherCropBlock.java#L203) |
| `SmallDripleafBlock` | `small_dripleaf` | `SmallDripleafBlock / SmallDripleafBlock / SmallDripleafBlock` | `BlockBehaviour` | Overrides support for underwater/soil; FACING from player, two-cell placement with same FACING; water tick and bonemeal BigDripleaf substitution | P08/P20/P22/P23/P30 | [SmallDripleafBlock.java:58](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SmallDripleafBlock.java#L58), [SmallDripleafBlock.java:66](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SmallDripleafBlock.java#L66), [SmallDripleafBlock.java:75](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SmallDripleafBlock.java#L75), [SmallDripleafBlock.java:94](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SmallDripleafBlock.java#L94), [SmallDripleafBlock.java:140](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SmallDripleafBlock.java#L140) |
| `TallSeagrassBlock` | `tall_seagrass` | `TallSeagrassBlock / TallSeagrassBlock / DoublePlantBlock` | `BlockBehaviour` | Support face sturdy UP and !magma; upper placement must be full WATER source; lower survival needs source water; no standalone BlockItem registration found | P20/P22/P24 | [TallSeagrassBlock.java:49](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TallSeagrassBlock.java#L49), [TallSeagrassBlock.java:62](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TallSeagrassBlock.java#L62), [TallSeagrassBlock.java:80](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TallSeagrassBlock.java#L80) |
| `MangrovePropaguleBlock` | `mangrove_propagule` | `MangrovePropaguleBlock / MangrovePropaguleBlock / MangrovePropaguleBlock` | `MangrovePropaguleBlock` | HANGING: support local UP against mangrove leaves; standing: inherited Bush soil; updateShape checks global UP, waterlogged tick; separate growth/age branch | P22/P23/P30 | [MangrovePropaguleBlock.java:81](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MangrovePropaguleBlock.java#L81), [MangrovePropaguleBlock.java:107](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MangrovePropaguleBlock.java#L107), [MangrovePropaguleBlock.java:113](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MangrovePropaguleBlock.java#L113), [MangrovePropaguleBlock.java:134](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MangrovePropaguleBlock.java#L134), [MangrovePropaguleBlock.java:187](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MangrovePropaguleBlock.java#L187) |
| `MushroomBlock` | `brown_mushroom,red_mushroom` | `Block / MushroomBlock / BushBlock` | `MushroomBlock` | Soil special tag OR darkness + solid-render support; spreading 3D physical area and neighbor random offsets; biome/worldgen consumer independent | P22/P30 | [MushroomBlock.java:52](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MushroomBlock.java#L52), [MushroomBlock.java:89](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MushroomBlock.java#L89), [MushroomBlock.java:96](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MushroomBlock.java#L96) |

**Every class above:** `neighborChanged` resolves to
`BlockBehaviour`, while `useItemOn` and `useWithoutItem`
also resolve to `BlockBehaviour` in the effective-owner TSV. This
does **not** establish all interactions safe: fertilization works via
`BonemealableBlock` and external items, and `BlockItem.placeBlock`
or other authoring paths run independently. For `BeetrootBlock`,
`CarrotBlock`, `CropBlock`, `PotatoBlock`, and
`TorchflowerCropBlock`, `updateShape` resolves to `BushBlock`
but **survival** goes through `CropBlock` then calls
`super.canSurvive` (the existing Bush Mixin is reachable in this
delegation). Do not count these as completely unadapted or as fully
covered: the higher-level light and growth paths remain distinct.

## Verified dispatch chains / algorithm owners

### A. Five CropBlock classes — shared support, separate growth

[CropBlock.java:193](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CropBlock.java#L193):
`CropBlock.canSurvive` evaluates the crop brightness predicate and
delegates to `BushBlock.canSurvive`, whose `below()` may be adapted
by `BushBlockLocalSupportMixin` for inherited fallback.
`CropBlock.mayPlaceOn` uses **FARMLAND**, not generic full-block
sturdiness. The shared `randomTick` checks brightness, age, and
`CropBlock.getGrowthSpeed`. The latter traverses the supporting
world-Y layer and XZ 3×3 farmland, plus N/S/E/W crop adjacency.
Those are local-geometry semantic candidates, not physical hit normals.

`BeetrootBlock` and `TorchflowerCropBlock` override
`randomTick` to conditionally call the parent with an additional
RNG draw; preserve vanilla random draw order. Torchflower age 2 is a
**block type transition**, not just another AGE property.
`CarrotBlock`/`PotatoBlock` inherit the full Crop lifecycle.
Do not patch per crop ID. Growth into adjacent terrain belongs to
a shared vegetation growth graph (P30), and bonemeal is an independent
item interaction author (P34).

### B. Two-cell plants — physical Y has multiple owners

[DoublePlantBlock.java:62](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L62) checks the physical upper vacancy
and build-height bound; [DoublePlantBlock.java:70](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L70) writes the
upper cell; [DoublePlantBlock.java:77](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L77) links UPPER to LOWER
through `below()`. [DoublePlantBlock.java:42](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DoublePlantBlock.java#L42) uses
`Direction.Axis.Y` and `Direction.UP/DOWN` to unlink a pair.
`placeAt`, break/drop handling and
`DoubleHighBlockItem.placeBlock` are **alternate creation/removal
paths**, not covered by `getStateForPlacement`.
Keep the actual physical height cap independent of **local**
upper-cell topology; blindly replacing `pos.getY()` with rotated
local altitude breaks build boundaries.

`TallFlowerBlock` inherits all of that lifecycle.
`PitcherCropBlock` does NOT: initial placement returns a single
default state and its `setPlacedBy` is intentionally empty.
Only later `grow` transitions age 3+ into a two-cell plant by
`above()`; `getLowerHalf`, survival and bonemeal need the same
paired-cell chart. Treat these as distinct algorithms sharing a
two-cell topology primitive, not one forced DoublePlant mixin.

`SmallDripleafBlock` authors FACING and both HALF states, overrides
support to query the fluid `above()` **the physical supporting
block**, schedules water ticks, and bonemeal converts to BigDripleaf.
`TallSeagrassBlock` requires strong **full source water** checks,
checks the supporting block's UP face, and its upper half validation
is independent. Cross-phase Phase 5 owns fluid simulation; Phase 2
must preserve the water predicate and source-cell selection.

### C. Mangrove and mushroom — distinct physical paths

`MangrovePropaguleBlock.canSurvive` switches from
`SaplingBlock`/Bush soil to **hanging support above** if
`HANGING=true`. Its `updateShape` examines `Direction.UP`,
schedules water ticks and delegates to `BushBlock.updateShape`.
Do not reframe the callback direction without ensuring physical
neighbor identity. A hanging propagule can be authored directly by
`createNewHangingPropagule`, not only the user's normal
`BlockItem` placement. Growth and water handling are separate.

`MushroomBlock.canSurvive` directly reads `pos.below()` and
tests `MUSHROOM_GROW_BLOCK` OR low brightness plus
`isSolidRender`; no `BushBlock.canSurvive` call exists.
`randomTick` searches a **3D physical bounding area** and applies
random offsets before `canSurvive` and setBlock; local vertical
meaning, density scope and RNG must be investigated before deciding
whether the area is transformed or remains world-physical.
Never replace `isSolidRender` with a generic face-support predicate.

## Creation paths cross-check (actual registered ITEM census)

- `ItemNameBlockItem` for beetroot seeds, carrot, potato,
  torchflower seeds, wheat seeds, and pitcher pod.
- `DoubleHighBlockItem` for large fern, lilac, peony,
  rose bush, sunflower, tall grass and small dripleaf.
  Reference: [DoubleHighBlockItem.placeBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/DoubleHighBlockItem.java#L17-L25)
  writes the **physical above** cell before delegating to base.
- `BlockItem` for mushrooms, mangrove propagule and pitcher
  plant. The actual item census has **no direct BlockItem
  registration** targeting `minecraft:tall_seagrass`:
  worldgen/bonemeal/water creation paths remain to audit.
- Item-side method/override **semantic review is deferred to Stage
  3B**; these census joins prevent an incorrect claim that the
  ordinary BlockItem path is the only creation route.

## Required semantic contracts for implementation (future Stage 4)

1. Resolve local source support/ceiling and neighbor through
   `PlanetBlockFrameContext` /`PlanetBlockSupportRuntime`, keep
   physical `BlockPos` and collision writes unmodified, preserve
   subclass virtual `mayPlaceOn` and exact vanilla predicates.
2. Implement explicit **local** upper/lower pair topology and
   paired invalidation with source-vs-target chart and seam
   handling. Physically enforce height bounds and obstruction.
   Separate initial placement, `setPlacedBy`, direct `placeAt`,
   age-triggered growth, and `DoubleHighBlockItem`.
3. For crops, support local tangent sampling preserving 3×3
   farmland weights, surrounding crop adjacency and RNG order.
   Maintain local growth position for plant extension without
   silently altering unrelated illumination semantics.
4. Preserve waterlogged tick scheduling, fluid source predicates,
   vanilla/NeoForge hooks, and original block-type transitions.
5. Tests: +Y vanilla-equivalence, five rotated gravity directions,
   support removal, pair placement/removal, bonemeal/growth,
   water source/edge, seam and corner, fake item placement, normal
   non-Planet world. Exact-version source/ASM and real client
   startup must be checked **before** gameplay PASS.

## Outstanding / explicit nonclaims

- **No** compiled JVM INVOKE-site/Mixin handler semantic verification,
  no proof that comparative source is identical to NeoForge patches,
  no `BlockStateBase` state-cache examination, no live game tests.
- No claim that the 18 block IDs work. Only source-owned call paths
  and reflection dispatch *candidates* were reviewed.
- Existing [BushBlockLocalSupportMixin](../../src/main/java/dev/planetary/mixin/BushBlockLocalSupportMixin.java)
  intercepts `BushBlock.canSurvive`'s `BlockPos.below()`; it
  does **not** intercept `MushroomBlock.canSurvive`,
  the hanging branch of `MangrovePropaguleBlock`, or
  `DoublePlantBlock`'s paired Y-axis lifecycle.
- Remaining one override ID is `SeaPickleBlock`, already included
  in the original owner cluster with RESEARCHED_GAP but not accepted.
- Next **separate response**: Stage 3A micro-task 3 — create the
  first 12 class-level evidence-backed TSV dispositions using
  this audit and actual registry rows, with explicit source/compiled
  evidence levels and `GAMEPLAY_PENDING`; all other registered
  concrete classes should remain visibly `REVIEW_PENDING`.
