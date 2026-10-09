# Phase 2: effective method-owner census — NeoForge 21.1.215

**Status:** actual registry and inherited source method implementation
owners VERIFIED by Java 21 / JUnit / GitHub CI; SEMANTIC orientation
mapping and in-game behavior remain REVIEW_PENDING.

## Evidence / what is really measured

CI run [37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
code revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`:
compileJava, compileTestJava, all JUnit, summary and registry-artifact
publication **SUCCESS**. The artifact `phase2-neo1211-registry-census`
contains registered BLOCK/ITEM IDs, concrete class hierarchy,
the *most specific declared method* per exact parameter signature,
all inherited declaring methods, and actual state properties.
This is a dynamic 21.1.215 dependency scan, not a source-name guess.

- 1060 blocks, 241 distinct concrete Java classes.
- 1333 items, 87 distinct concrete Item Java classes.
- 1712 BlockState property instances.
- 528 block IDs flagged by the expanded tentative
  orientation-property-name rules (previously 527).
- A block with NO flagged property may still require local
  support, growth, graph or interaction adaptations.
- `REVIEW_PENDING` deliberately remains the default disposition.

**Limit:** reflection locates the nearest declaration of a named
method with an exact parameter signature in the runtime class
inheritance chain. This is a candidate dispatch owner, not proof
of a specific INVOKEVIRTUAL bytecode call site or of the code
path taken; compiled NeoForge and source adaptation still need
ASM-based checks. A method declared in
`BlockBehaviour.BlockStateBase` is *not* necessarily part of
`Block`'s method-declaration ownership: this scan does NOT yet
exhaust all cache/state-level dispatch, including
`BlockStateBase.canBeReplaced`. Do not declare zero bugs simply
because the Block-level method owner is inherited.

## Per-method effective owner diversity (by registered Block ID)

| Method | Distinct named declaring owners | Largest owner groups by number of BLOCK IDs |
|---|---:|---|
| `getStateForPlacement` | **103** | Block 434, SlabBlock 60, StairBlock 56, RotatedPillarBlock 53, WallBlock 25, DoorBlock 20, TrapDoorBlock 20 |
| `canSurvive` | **60** | BlockBehaviour 750, BushBlock 40, DoorBlock 20, BaseCoralPlantTypeBlock 20, CarpetBlock 17, CandleBlock 17 |
| `updateShape` | **103** | BlockBehaviour 406, SlabBlock 60, StairBlock 56, BushBlock 44, FlowerPotBlock 35, WallBlock 25 |
| `setPlacedBy` | **15** | Block 1001, DoorBlock 20, BedBlock 16, DoublePlantBlock 8 |
| `neighborChanged` | **25** | BlockBehaviour 958, DoorBlock 20, TrapDoorBlock 20, AbstractSkullBlock 14, FenceGateBlock 11 |
| `rotate` | **61** | BlockBehaviour 608, HorizontalDirectionalBlock 85, StairBlock 56, RotatedPillarBlock 54 |
| `mirror` | **57** | BlockBehaviour 667, HorizontalDirectionalBlock 85, StairBlock 56 |
| `useItemOn` | **24** | BlockBehaviour 913, FlowerPotBlock 35, SignBlock 22, CandleBlock 17 |
| `useWithoutItem` | **52** | BlockBehaviour 806, SignBlock 44, FlowerPotBlock 35, DoorBlock 20, TrapDoorBlock 20 |

Counts sum neither to 1060 nor across columns: an ID is
represented once per method, but each ID participates in
MANY distinct methods. Class-based exact owner counts include
abstract family owners and subclass override groups; they
must be interpreted with the state-frame semantic contract.

## Item creation owners: bypasses confirmed in registered IDs

| Item method | Dominant implementation owner distribution |
|---|---|
| `useOn` | BlockItem 922, Item 284, SpawnEggItem 80, with 22+ other owners |
| `getPlacementState` | BlockItem 862, StandingAndWallBlockItem 58, GameMasterBlockItem 5 |
| `updatePlacementContext` | BlockItem 924, ScaffoldingBlockItem 1 |
| `place` | BlockItem **925** (specialized items still override components/contexts) |
| `use` | Item 1165, SpawnEggItem 80, ArmorItem 30, BoatItem 18, BucketItem 9, other owners |

This is a key reason a generic mixin in `BlockItem.place`
cannot be treated as complete coverage: specialized items
override state selection, placement context, user interaction
and post-placement component application.

## Actual source-to-Planetary gaps needing full-family patches

**State authoring P06–P16:** 103 getStateForPlacement
owners, including Stair, Trapdoor, FenceGate, Shulker,
Lantern, Amethyst, concrete Furnaces etc. Existing
Planetary's EndRod/EnderChest/Pillar/Hopper/Slab and
StandingAndWallBlockItem are only partial coverage.

**Support/neighbor P22–P33:** 60 canSurvive and 103
updateShape owners. BushBlock base Mixin does NOT
affect SeaPickle and Cocoa independent implementations;
crops/flowers have distinct environmental rules; walls,
rails, scaffolding, vine/multiface, plant head/body
require graph-specific maintenance.

**Pairs/interactions P17–P21/P34–P35:** only 15 setPlacedBy
owners, but they matter profoundly (Door/Bed/DoublePlant);
24 useItemOn and 52 useWithoutItem owners, with
Chest/ChiseledBookShelf/ToolAction and BE interactions
separate from simple placed facing.

**Creation/compat P02–P05/P39–P40:** no-player
DirectionalPlaceContext, ScaffoldingBlockItem, alternate
item use, late DataComponents.BLOCK_STATE, direct
StructureTemplate/commands/setBlock bypass regular
player placement. Must preserve physical hit selection
and foreign/virtual level provenance.

## Next classification and acceptance

- Reconcile all **241** concrete Block classes into
  effective implementation-owner clusters for every
  orientation-bearing method; classify P01–P40 or explicit
  NOT_APPLICABLE/CROSS_PHASE; do not label source name
  matches implemented.
- Reconcile all **87** concrete Item classes and 1333
  registered item IDs' BlockItem-specific creation paths
  into known mechanisms. A registry item class may not
  create a block but can still create directional entities.
- Include `BlockBehaviour.BlockStateBase` state/cache
  methods, NeoForge overrides and custom item components;
  current owner scan alone does not prove these.
- Lock a reviewed owner list and make any **new
  unclassified owner fail CI** instead of silently
  defaulting to compatible; temporarily REVIEW_PENDING
  remains honest.
- Implement whole coherent A–E waves with pure all-face
  tests and ASM invocation contracts, CI green and
  real natural-placement six-face/edge gameplay acceptance.
  No need for the user to manually test all 1060 IDs.

Reference original source owner reports:
`PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md`,
`PHASE2_SOURCE_OWNER_AUDIT_WAVE_B_1_21_1.md`,
`PHASE2_SOURCE_OWNER_AUDIT_WAVE_C_1_21_1.md`;
P01–P40 master atlas and seed:
`ORIENTATION_MECHANISMS_ATLAS_1_21_1.md` and
`PHASE2_PLANETARY_COVERAGE_SEED_1_21_1.tsv`.
