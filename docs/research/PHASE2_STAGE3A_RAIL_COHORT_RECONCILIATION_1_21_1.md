# Stage 3A-5.4 — independently reconcile all 66 reviewed owner classes and rail group with original NeoForge runtime ZIP

**2026-10-10** · Planetary `2.0` · Minecraft **1.21.1**,
NeoForge **21.1.215**, Java 21. One independent
**registry and research-status** microtask, with **no source
disposition changes** and **no Java changes**.

## Source authenticity and comparison method

This checkpoint freshly opened the ORIGINAL compiled CI
runtime census (not an excerpt from earlier notes):
[GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055)
artifact **11643813158** ZIP, original source revision
`aa39572950a15403ea0a9003eefccf3bf6675ff7`,
verified ZIP **SHA-256**:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.

Its exact source tables:

| Original member | Physical records |
|---|---:|
| `phase2-neo1211-block-registry.tsv` | **1060** registered `minecraft:` BLOCK IDs |
| `phase2-neo1211-item-registry.tsv` | **1333** registered ITEM IDs |
| `phase2-neo1211-state-properties.tsv` | **1712** property records |
| Unique `java_class` in BLOCK registry | **241** |

The original ZIP's **241 unique class names and actual
registered BLOCK ID counts** were reconstructed directly
from its `registry_id` / `java_class` cells.
For **all current 66** source+reflection-reviewed classes,
the independent check reconstructed **all 174 exact BLOCK
ID strings** and five nearest method-declaring owners
per reviewed concrete class from `effective_method_owners`.
The parser required **complete fully qualified argument
signatures**, correctly selecting the appropriate
overload (the first bare method name is not sufficient;
e.g. vanilla plant owner signatures can differ):

- `getStateForPlacement(BlockPlaceContext)`
- `canSurvive(BlockState,LevelReader,BlockPos)`
- `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`
- `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`
- `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`

The actual ZIP data were processed independently of
the GitHub ledger. The same canonical FNV-1a
checksums were then calculated on the current
branch ledger `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`.
Class strings and comma-separated exact BLOCK ID
lists were **ASCII/lexicographically sorted**, and the
nearest declaring owner signatures were ordered
`getStateForPlacement`, `canSurvive`,
`updateShape`, `randomTick`, `setPlacedBy`.
Concatenation format includes the trailing `\n`
for each row. FNV-1a 32-bit is a **diagnostic
fingerprint**, not collision-resistant or a runtime test.

## Exact original ZIP-vs-current ledger results

| Canonical check | Fresh original 21.1.215 ZIP | GitHub 2.0 ledger | Result |
|---|---|---|---|
| 241 unique class names + registered ID count | `0xf188a064` | `0xf188a064` | **MATCH** |
| 66 reviewed classes + their 174 individually exact sorted IDs | `0x6c5c67d7` | `0x6c5c67d7` | **MATCH** |
| 66 reviewed classes + 5 exact-signature declaring owners | `0x57a01200` | `0x57a01200` | **MATCH** |
| 66 reviewed classes + IDs + all 5 declaring owners | `0xb3945a6e` | `0xb3945a6e` | **MATCH** |

Additional strict current ledger checks:
**241** class rows, unique `java_class` keys,
**1060** summed registered IDs; all exactly
**16 columns** per class row;
**66/241** `SOURCE_REVIEWED_INTEGRATION_PENDING`,
**175/241** `REVIEW_PENDING`;
**174** reviewed registered BLOCK IDs /
**886** source-unreviewed registered IDs;
all reviewed rows show
`registry_dispatch_evidence=REFLECTION_OWNER_VERIFIED`,
complete distinct exact ID sets per row and
5 nonpending method declaring owners;
all 175 unreviewed rows remain
`registry_dispatch_evidence=REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY`.

For **all 241** ledger rows, three independent
acceptance fields STILL `REVIEW_PENDING`:
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance`.
No further class was source-reviewed or accepted
by running this census reconciliation.

## Newly reviewed in Stage 3A-5.1: three rail classes / four IDs

| Exact concrete class | Registered BLOCK ID(s) | Five exact nearest declaration owners (place / survive / shape / random / placedBy) | Source review only |
|---|---|---|---|
| `RailBlock` | `minecraft:rail` | `BaseRailBlock / BaseRailBlock / BaseRailBlock / BlockBehaviour / Block` | SOURCE_REVIEWED_INTEGRATION_PENDING |
| `DetectorRailBlock` | `minecraft:detector_rail` | `BaseRailBlock / BaseRailBlock / BaseRailBlock / BlockBehaviour / Block` | SOURCE_REVIEWED_INTEGRATION_PENDING |
| `PoweredRailBlock` | `minecraft:activator_rail,minecraft:powered_rail` | `BaseRailBlock / BaseRailBlock / BaseRailBlock / BlockBehaviour / Block` | SOURCE_REVIEWED_INTEGRATION_PENDING |

Original ZIP additionally reports rail class
hierarchies `RailBlock/DetectorRailBlock/PoweredRailBlock
> BaseRailBlock > Block > BlockBehaviour`.
`BaseRailBlock` itself and `RailState` are
nonregistered internal owners; `RailState.place/connectTo`
can mutate **multiple** blocks, with world XZ-only
connection matching and world-Y rising endpoints.
All three class rows stay reviewed **for source and
compiled declaring owner only**, not real ASM weaving.
`DetectorRailBlock` owns its `onPlace` and scheduled
minecart-detection tick; others inherit
`BaseRailBlock.onPlace`.
`PoweredRailBlock` is **one class with TWO
registered IDs**: `activator_rail`, `powered_rail`.

Source audit evidence already committed, not rerun:
- [3A-5.1 compiled owners and rail graph](PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md)
- [3A-5.2 original 4 BlockItem creators and off-item writers](PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md)
- [3A-5.3 local RailShape/grade, edges/corners and cart movement acceptance chart](PHASE2_STAGE3A_RAIL_SEAM_FRAME_CONTRACT_1_21_1.md)

## Explicitly still unreviewed, and next family selection

The **three** pending sensing-source classes below
are exact original ZIP `java_class` values and
**16 registered BLOCK IDs**, selected for the
next **separate** [3A-6 source owner family](../phases/phase-02/02d-environment-pressure-sensor-owners.md).

| Pending concrete class | Exact source registry IDs | Number |
|---|---|---:|
| `DaylightDetectorBlock` | `minecraft:daylight_detector` | 1 |
| `PressurePlateBlock` | 13 plate variants: acacia, bamboo, birch, cherry, crimson, dark_oak, jungle, mangrove, oak, polished_blackstone, spruce, stone, warped (`minecraft:*_pressure_plate`) | 13 |
| `WeightedPressurePlateBlock` | `minecraft:heavy_weighted_pressure_plate`, `minecraft:light_weighted_pressure_plate` | 2 |
| **Total** | **16 exact original IDs** | **16** |

Comparative vanilla 1.21.1
[`DaylightDetectorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java)
extends `BaseEntityBlock` and samples daylight /
sky through `BlockEntityTicker`; `INVERTED` mode
and `POWER` BlockState have separate authors.
[`PressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PressurePlateBlock.java)
and [`WeightedPressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java)
inherit `BasePressurePlateBlock` entity-collision,
support and tick callbacks; Boolean vs analog plate
power and entity filters differ.
These source-owner bodies are **NOT yet accepted or
promoted in this checkpoint**. The new card performs
those audits in independently committed tasks.
Sculk-vibration and lightning-rod owners remain for
another independent family, not silently included.

## What is NOT completed

No ASM-patched Minecraft/NeoForge 21.1.215 bytecode
method body audit or instruction-level invocation
reachability; no compiled/injected Mixin matching;
no gravity-local rail implementation, client or server
runtime verification; no rail/cart gameplay tests.
The original ZIP census is declaration and registry
evidence **only**. The three completed rail
source studies and this reconciliation complete
the **3A-5 small family research card**, not all of
Stage 3A, the implementation of Phase 2,
Phase 7A signal behavior or minecart locomotion.

**Next first unfinished microtask:** Stage **3A-6.1**,
card checkbox **1** on
[environment/contact sensors](../phases/phase-02/02d-environment-pressure-sensor-owners.md):
three actual pending classes / 16 ID source
and exact NeoForge five-owner audit; one new
separate source research commit.
