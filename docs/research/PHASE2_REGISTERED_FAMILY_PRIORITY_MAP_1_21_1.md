# Phase 2: first real-registry owner prioritization (NeoForge 21.1.215)

Status: **verified count data, implementation and gameplay pending**.
Source is the actual JUnit-generated BLOCK/ITEM/property registry
TSV artifact from GitHub Actions run 37987135612, correlated with
P01–P40 source audit and existing Planetary Mixin filenames.
The run itself had compile+JUnit+report publishing SUCCESS.

Important: the counts below are counts for EXACT concrete class
names unless otherwise stated, not sums over inherited families.
Some subclasses (e.g. weathering copper doors) have their own
class name but inherit algorithm owners. Future enhanced CI
reports actual effective method owners and hierarchy.
No one-row CLASS presence is proof of an implemented family.

## Wave A: state authoring and property vocabulary

| Mechanism group | Concrete registered evidence | Existing Planetary source | Current gap |
|---|---|---|---|
| P06 local AXIS | 52 `RotatedPillarBlock` IDs | `RotatedPillarBlockPlacementMixin` | Rod/Chain overrides, target authoring and alternative item paths pending |
| P07 full 6D direction | 17 `ShulkerBoxBlock`, 4 `AmethystClusterBlock`, observer, dispenser, dropper, End Rod, Lightning Rod, piston/piston head | `EndRodLocalPlacementMixin`, `HopperBlockPlacementMixin` | Independent implementors and BER/support consumers missing |
| P08 horizontal direction | furnace, blast furnace, smoker, chest, trapped chest, Ender Chest, barrel, vault, chiseled bookshelf, 16 glazed terracotta | `EnderChestLocalPlacementMixin` | No guaranteed generic horizontal authoring owner; many subclasses override getStateForPlacement |
| P09 two-axis FrontAndTop | 1 crafter + 1 jigsaw; each has 12 legal `orientation` values | none | Both composite algorithms unaccepted |
| P10 ROTATION_16 | 45 `rotation` property IDs, all 16 values | partial standing/wall item helper | Local yaw/attachment/BER not complete |
| P14 stair family | 52 `StairBlock` IDs, plus potential distinct concrete subclasses | no Stair-specific confirmed adapter | Placement HALF/FACING, derived corner SHAPE, physical neighbor invalidation |
| P15 slab family | 56 `SlabBlock` IDs | `SlabBlockPlacementMixin` | Replacement stacking and hit fractions across rotated faces |
| P16 trapdoor family | 16 `TrapDoorBlock` IDs plus distinct subclasses | none | FACING+HALF, nonreplace click, OPEN/POWERED |
| P17 hinged door | 16 `DoorBlock` + 4 `WeatheringCopperDoorBlock` visible IDs (additional other concrete subclasses may exist) | `DoorBlockGravityMixin` | actual two-part player placement and update/interaction |
| P19 bed pair | 16 `BedBlock` IDs | `BedBlockGravityMixin` | HEAD/FOOT interaction, per-face/edge and BER |
| P21 chest pair | `chest` and `trapped_chest` in two different runtime classes | EnderChest adapter is unrelated | LEFT/RIGHT/SINGLE and inventory pair |
| P11 face-attached | 13 `ButtonBlock` + lever, grindstone, bell | `FaceAttachedHorizontalDirectionalBlockSupportMixin` | Bell own `attachment` and distinct interaction/support |

## Wave B: semantic support and spatial graphs

| Mechanism | Concrete registered examples | Why basic FACING or BushBlock mixin is not enough |
|---|---|---|
| P22–P24 local supports | 17 `CandleBlock` + 17 `CandleCakeBlock`, bush/sapling variants | `SeaPickleBlock` overrides canSurvive; `CocoaBlock` directed support, `LanternBlock` ceiling/support/waterlogging |
| P23 wall/ceiling | 4 AmethystCluster, 2 Lantern, Cocoa | Own isFaceSturdy/canSupportCenter, physical neighborPos callback and state-local direction |
| P25 cardinal connections | 12 FenceBlock, 11 FenceGateBlock, 25 WallBlock, 2 IronBarsBlock and 16 StainedGlassPaneBlock | Different four-side graph owners even when all store north/east/south/west properties |
| P26 multi-face | 1 Vine plus special multiface blocks/variants | attachment bitmask and environmental spread/update are not equivalent to Fence graph |
| P27 wall heights | 25 WallBlock IDs | LOW/TALL/UP shape+graph has independent owner |
| P28 rail | rail 1, PoweredRailBlock 2 (powered/activator), DetectorRailBlock 1 | RailShape 6/10 states, physical adjacency and rising local UP |
| P29 growth head/body | CaveVines and Kelp variants exist WITHOUT an orientation-named state property | chain-state/topology, scheduled tick and alternate growth path |
| P30 columns | sugar cane, cactus and saplings exist WITHOUT direction property | local above/below growth, lighting and edge rules |
| P31 scaffolding | 1 ScaffoldingBlock ID (BOTTOM/DISTANCE/WATERLOGGED) | its specialized Item changes the physical target; BOTTOM was missed by initial name heuristic |

## Item authoring, entirely different from state vocabulary

Actual registry has **925** `BlockItem` IDs, including **799**
whose runtime class is exactly `BlockItem`, but another
**126** BlockItem-derived specialized registered items.
Selected counts: 27 DoubleHighBlockItem, 19
StandingAndWallBlockItem, 11 HangingSignItem,
16 BedItem, 16 BannerItem, 14 ItemNameBlockItem.
There are 408 non-BlockItem IDs; some can author
entities/world state (80 spawn egg IDs etc.).

The exact `Item.useOn`, `BlockItem.updatePlacementContext`,
`getPlacementState` and late DataComponents.BLOCK_STATE
dispatch class hierarchy is part of release completeness.
Do not simply equate one Item per Block or all BlockItems
with the base BlockItem class.

## The actual source owner, not selected class name, is the unit

A registered Block ID (e.g. `minecraft:sea_pickle`)
can have no FACING, AXIS, HALF, ORIENTATION or ROTATION property
and still depend on local DOWN. Conversely `type`
can mean sticky/normal piston rather than physical orientation.

The updated executable census `aa395729` adds
`effective_method_owners` and `class_hierarchy` for
each actual registered ID. Stage 3 must classify every
owner+override against P01–P40 and explicitly assign
`IMPLEMENTED / GAP / CROSS_PHASE / NOT_APPLICABLE /
REVIEW_PENDING`. Never mark unknown as working.

## Sequencing and gate

1. Wait for green upgraded effective-owner census CI.
2. Group registered IDs by **effective behavior owner** (not
   concrete class alone), freeze source/NeoForge patch and
   Planetary adapter gap per group.
3. Implement coherent wave A placement authoring, including
   specialized BlockItems and override bypasses; then waves B–E.
4. Publish an automated fail-unclassified report and tests
   of six faces, edges, physical neighbor cells, alternate items.
5. User manually verifies only representative mechanism stations
   in the auto-generated world after code CI and client startup
   pass; don't retest already accepted particle motion.

No new runtime Mixin was added by these registry findings.
