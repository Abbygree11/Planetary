# Phase 2 Stage 3A-3.1 — connected block graphs, 8 actual concrete classes

2026-10-10; target **Minecraft 1.21.1**, **NeoForge 21.1.215**,
Planetary branch **`2.0`**.

**Status:** `SOURCE_REVIEWED_INTEGRATION_PENDING` /
`REFLECTION_OWNER_VERIFIED`; **NeoForge patched code/ASM INVOKEs,
Planet adaptation correctness, gameplay all REVIEW_PENDING**.
This is an eight-class bounded **research** package, not a code patch.

## Exact sources and registry scope

- **Real runtime registry / exact declarations:** original
  [CI run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
  artifact `11643813158`, repository revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`.
  The artifact `phase2-neo1211-registry-census.zip` SHA-256 is
  `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
  Read actual `phase2-neo1211-block-registry.tsv`, 1060 block
  ID rows / 241 unique concrete classes, matching original per-class
  counts and exact `effective_method_owners` signatures.
- **Comparative source semantics**, not exact NeoForge patched body:
  [pinned 1.21.1 `hackersense/OptiFine-Source` revision
  `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block).
  The eight concrete classes and their relevant
  nonregistered `CrossCollisionBlock`,
  `MultifaceBlock`, and `IronBarsBlock` ancestors
  were source-read.
- **Selection:** 8 of the 10 candidates in
  [the Stage 3A-3 card](../phases/phase-02/02a-block-graph-owners.md)
  covering **69 actual block IDs**. The two excluded
  `TripWireBlock` and `TripWireHookBlock` are
  **still `REVIEW_PENDING`** (2 IDs). Their
  redstone signal/tension/hook recalculation is a
  distinct mechanism, so not falsely treated as
  a fence-or-vine adjacency algorithm.

## Source+NeoForge exact-signature owner matrix

Five rows/owners are identified by **full signature**:
`getStateForPlacement(BlockPlaceContext)` /
`canSurvive(BlockState,LevelReader,BlockPos)` /
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)` /
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)` /
`setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`.
The table includes the first four; the **fifth is `Block`
for all eight classes**. `neighborChanged` is
`FenceGateBlock` for gates, and `BlockBehaviour` for
the other seven. All owner names below were checked in
the original NeoForge reflection artifact.

| Concrete class | Registered block IDs | Declaring owners: placement / survive / updateShape / randomTick | Mechanisms | Source |
|---|---:|---|---|---|
| `FenceBlock` | 12 | `FenceBlock / BlockBehaviour / FenceBlock / BlockBehaviour` | P25,P34,P35,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java#L107) |
| `FenceGateBlock` | 11 | `FenceGateBlock / BlockBehaviour / FenceGateBlock / BlockBehaviour` | P08,P25,P34,P35,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L162) |
| `WallBlock` | 25 | `WallBlock / BlockBehaviour / WallBlock / BlockBehaviour` | P25,P27,P35,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L169) |
| `IronBarsBlock` | 2 | `IronBarsBlock / BlockBehaviour / IronBarsBlock / BlockBehaviour` | P25,P35,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java#L44) |
| `StainedGlassPaneBlock` | 16 | `IronBarsBlock / BlockBehaviour / IronBarsBlock / BlockBehaviour` | P25,P35,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/StainedGlassPaneBlock.java#L24) |
| `VineBlock` | 1 | `VineBlock / VineBlock / VineBlock / VineBlock` | P23,P26,P30,P35 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L115) |
| `GlowLichenBlock` | 1 | `MultifaceBlock / MultifaceBlock / GlowLichenBlock / BlockBehaviour` | P23,P26,P34,P35,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GlowLichenBlock.java#L54) |
| `SculkVeinBlock` | 1 | `MultifaceBlock / MultifaceBlock / SculkVeinBlock / BlockBehaviour` | P23,P26,P35,P37 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L192) |

The [ledger TSV](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv)
contains **each of the 69 exact `minecraft:` registered
block IDs**, not guessed ID prefixes. The 16 stained
panes share inherited `IronBarsBlock` placement,
`updateShape`, shape and rotation methods.
Reflection declaration != actual method invocation/patch
or game integration.

## P25: different 4-tangent graph authorities

### FenceBlock — four neighbor flags + predicate and item interactions

[`FenceBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java#L107)
reads the four world-horizontal neighbors, their **inward
physical sturdy face**, and applies its own virtual
`connectsTo` predicate. That predicate
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java#L72)) accepts:
- compatible same-fence group (and respects the
  `WOODEN_FENCES` vs other `FENCES` tag split),
- compatible gate connected to that *direction*,
- or nonexceptional, face-sturdy adjacent blocks.

[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceBlock.java#L129)
updates only the corresponding property for a
**physical neighbor Direction** callback, plus
schedules water ticks. Existing Planetary
[`FenceBlockGravityMixin`](../../src/main/java/dev/planetary/mixin/FenceBlockGravityMixin.java)
implements a candidate local tangent query on placement/update
and preserves water tick, but this is **source-only adapter
existence**, not runtime correctness or a PASS for fences
at edge/corner transitions. `FenceBlock` also owns
lead-related `useItemOn` / `useWithoutItem`;
these are item/interaction paths for 3A-3.2/P34,
not solved by a graph property update.

### FenceGateBlock — orientation, perpendicular WALLS, redstone

[`FenceGateBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L162)
authors **`FACING`, `OPEN`, `POWERED`, `IN_WALL`**.
`IN_WALL` is derived from **two neighbor cells
perpendicular to facing**, testing `BlockTags.WALLS`,
not the fence `connectsTo` predicate.
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L88)
is a dedicated perpendicular neighbor update and checks
the *opposite* side as well. `neighborChanged`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FenceGateBlock.java#L240))
reads powered state and changes opening; `useWithoutItem`
toggles the gate. State authoring belongs to Phase 2,
signal propagation to Phase 7A (P36).
The gate is **not a `FenceBlock` subclass**, nor
is it a replaceable fence algorithm.

### WallBlock — tangent edges, local above collision, central post

[`WallBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L169)
queries 4 neighbors **and the block above**.
`WallBlock` has **two** method-name
`updateShape` overloads:
1. lifecycle `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`
   ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L193));
2. helper `updateShape(LevelReader,BlockState,BlockPos,BlockState,boolean,boolean,boolean,boolean)`
   ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L251)).

The helper inspects the **DOWN face of the above
block's collision shape** and computes
`WallSide.NONE/LOW/TALL` for four sides,
then computes the central **`UP` post** with
neighbor + special-tag/coverage policy
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallBlock.java#L266)).
Only changing NORTH/EAST/SOUTH/WEST bits like a fence
does not reproduce that graph or its 3D geometry.
The above neighbor is a **source-local UP** semantic,
while the physical coordinate and shape face of the
target block must be transformed using **the target's
frame**, including seams. Never call
`isFaceSturdy` on the source's local direction as
if it were a target-local face.

### IronBarsBlock + StainedGlassPaneBlock — another connection predicate

[`IronBarsBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java#L44)
uses four adjacency booleans but its
[`attachsTo` predicate](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java#L98)
accepts same `IronBarsBlock` derivatives,
`BlockTags.WALLS`, or face-sturdy nonexceptional
neighbors; this differs from FenceBlock's
WOODEN_FENCES grouping. `updateShape` is
owned by IronBarsBlock
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java#L66)).
`StainedGlassPaneBlock` simply extends
`IronBarsBlock` with a dye color and inherits
the placement/update graph for all 16 colors
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/StainedGlassPaneBlock.java#L24)).
`CrossCollisionBlock` supplies shared
`getShape`/`getCollisionShape` caches,
`rotate`/`mirror` and water fluid state
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CrossCollisionBlock.java#L102)).
`IronBarsBlock.skipRendering` has an
adjoining-pane conditional
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/IronBarsBlock.java#L82));
render/culling is Phase 3, not evidence that
connected states are correctly authored.

## P26: multi-face graph authority is not a four-way fence

### VineBlock: UP + 4 side faces; inherited vertical attachments

[`VineBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L396)
iterates nearest-looking directions, explicitly
**excludes DOWN**, and may add a face to an
already present vine (`canBeReplaced` special path).
`canSupportAtFace` / `getUpdatedState`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L133))
accept direct support from the chosen face,
or for side faces accepts a hanging support
**from a same-face vine block above**. The
`canSurvive` check
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L115))
passes if **at least one** face survives this
support pruning, while `updateShape`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L209))
can remove a particular face and becomes AIR
if no faces remain. `randomTick`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/VineBlock.java#L223))
spreads via vertical/horizontal paths with
local-density constraints. Need independent
P26 graph-wide replacement, spreading and
all-face neighbor verification, not generic
MultifaceBlock redirection.

### GlowLichenBlock and SculkVeinBlock: MultifaceBlock inherited core

Both inherit
[`MultifaceBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L191)
and
[`MultifaceBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L161):
**all present faces must have valid adjacent
support**, unlike VineBlock's salvage-any-face
semantics. Its `updateShape`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L138))
removes the unsupported **individual face** and
returns AIR only when no face remains.
Its support predicate
[`MultifaceBlock.canAttachTo`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceBlock.java#L281)
checks **full target-facing block-support
OR collision-shape face**, not merely
`BlockState.isFaceSturdy`, so blindly
substituting a fence/center-support query changes
vanilla behavior.

[`GlowLichenBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GlowLichenBlock.java#L54)
and
[`SculkVeinBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L192)
each schedule WATER ticks if waterlogged, and
then delegate to `MultifaceBlock` shape update.
Glow lichen's bonemeal
[`performBonemeal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/GlowLichenBlock.java#L85)
uses `MultifaceSpreader` to author new states.
Sculk vein has independent
[`regrow` / `onDischarged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L54)
writes, and a distinct
`SculkVeinSpreaderConfig.stateCanBeReplaced`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L231)):
the decision examines potentially **two cells**
and disallows certain fluids and solid obstructions.
Those are direct alternative state authors and
feature/spreader paths, beyond standard
`BlockItem.place`, to be joined in Stage 3A-3.2
and eventually Stage 3B.

## Cross-phase contracts and acceptance gates

For any graph owner path:
1. **Logical edge direction is source-local**:
   local tangent directions for fences/walls/panes,
   5-face vine directions, 6-face multiface bits.
2. **NeighborPos and physical callback direction
   are real world coordinates**. Resolve source
   edge to actual neighbor, then derive the
   **neighbor's own local inward support face**;
   never globally rotate `BlockPos` or
   assume a physical callback is a local state bit.
3. **Full shape-face predicates**, tag groups,
   wall above collision, waterlogged scheduling,
   post/side update order, entity/lead interaction,
   redstone input/output and spreading state authors
   must preserve vanilla semantics.
4. At seam/edge both source and target chart can differ.
   A single `Direction.Plane.HORIZONTAL` or
   `Direction.UP` replacement is insufficient.

**Phase 2:** local state authors, support/graph,
physical update reactions and paired cell writes.
**Phase 3:** connected shape/face culling.
**Phase 5:** fluid simulation (water identity/tick
scheduling still preserved in Phase 2).
**Phase 7A:** fence-gate redstone signal/network.
**Phase 8:** source worldgen & environmental spread
where relevant.

**No runtime change or game test.** No
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance` promoted. This source review
does not certify active Mixin weaving or NeoForge
patched JVM invocation sites.

## Scope accounting and next micro-task

**Before:** 47/241 source+reflection reviewed
(93/1060 block IDs), 194 pending (967 IDs).

**This package:** 8 reviewed classes (69 IDs).
**After:** **55/241 source+reflection reviewed**
(162/1060 IDs), **186/241 REVIEW_PENDING**
(898/1060 IDs). `TripWireBlock` and
`TripWireHookBlock` intentionally remain pending.

**NEXT Stage 2.3A-3.2 (one separate answer only):**
independent registered ITEM author join, component
and interaction alternative creation paths for
this 8-class batch, including lead interactions,
gate power/open, MultifaceSpreader / sculk spread,
plus graph/shape/fluids cross-phase boundaries.
Do not start new source or runtime implementation
in this same package.


## 2026-10-10 Stage 3A-3.2 item creators and non-item state writers

Completed a **separate** [69/69 original CI item-registry join and alternative-author audit](PHASE2_STAGE3A_CONNECTIVITY_ITEM_AUTHORS_1_21_1.md), validating all eight concrete classes' exact `placed_block` mapping, seven compiled `BlockItem` effective owner signatures, and independent authors. Significant paths: `BlockItem` post-placement `DataComponents.BLOCK_STATE`, gate `useWithoutItem`/`neighborChanged`/`onExplosionHit`, fence `LeadItem` (entity leash, not a new block), vine `randomTick`, GlowLichen `MultifaceSpreader`, sculk `regrow`/`onDischarged`/`attemptUseCharge`, and conditional `MultifaceGrowthFeature` generation. No class/ID status promotion: **55 reviewed / 186 pending**, 162/898 IDs; all ASM/adaptation/gameplay PENDING. NEXT 3A-3.3 exact block-state chart/shape/physical callback semantics.
