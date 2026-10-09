# Stage 3A-4.4 — exact NeoForge redstone source-owner and registry reconciliation

**2026-10-10**, branch `2.0`, Minecraft **1.21.1**,
NeoForge **21.1.215**; **bounded original-census
reconciliation only**. No gameplay or code changes.

## Primary source, scope and how the check was done

The **unaltered original CI artifact ZIP** was downloaded
from [GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact `11643813158`, original build commit
`aa39572950a15403ea0a9003eefccf3bf6675ff7`,
SHA-256 **`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`**. Original
ZIP row counts were independently parsed:

| Source file | Data |
|---|---:|
| `phase2-neo1211-block-registry.tsv` | **1060 registered BLOCK IDs** |
| `phase2-neo1211-item-registry.tsv` | **1333 registered ITEM IDs** |
| `phase2-neo1211-state-properties.tsv` | **1712 recorded block-property rows** |
| Distinct registered BLOCK implementation `java_class` | **241** |

Compared original ZIP to the committed 16-column
[Planetary class ledger](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv)
at the parent revision `f34b6bcb86353780de8c02bbd3c529892a7fa81d`. All **241**
exact fully qualified class names and per-class registered
BLOCK count were compared. For each of the **63**
`SOURCE_REVIEWED_INTEGRATION_PENDING` class rows, also
compared its **170 complete registered ID strings**
and each of **five actual compiled method-declaring owners**,
resolved by **complete parameter signature**, not just by
method name:

- `getStateForPlacement(BlockPlaceContext)`
- `canSurvive(BlockState,LevelReader,BlockPos)`
- `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`
- `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`
- `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`

Method-owner reflection verifies **declaration at runtime
class-level**, not NeoForge modified method bytecode,
a call site, active Mixin binding or a gameplay result.

### Independently calculated original-vs-branch fingerprints

Canonical rows sorted by fully qualified class using
**bytewise/ASCII lexical order** (NOT language/locale
collation); registered IDs sorted likewise.
Each `class|count\n`, `class|ids\n`, `class|owners\n`,
`class|ids|owners\n` concatenated in that order.
Four FNV-1a 32-bit digests are **diagnostic fingerprints
only** (not collision-resistant cryptographic proof),
accompanied by exact key, row-width, count and
acceptance-field invariants.

| Check | Direct original ZIP | Live branch TSV | Result |
|---|---|---|---|
| All **241** class names + registered ID counts | `0xf188a064` | `0xf188a064` | **MATCH** |
| **63** reviewed class names + all **170** exact sorted IDs | `0xdb8afbd5` | `0xdb8afbd5` | **MATCH** |
| **63** reviewed class names + **5** method signature owners | `0x6052a7cf` | `0x6052a7cf` | **MATCH** |
| **63** reviewed class names + IDs + all owners combined | `0x66be3dcb` | `0x66be3dcb` | **MATCH** |

**Ledger invariant result:** 241 unique classes / 1060
BLOCK IDs, all 16 TSV columns present, 63 classes
source+reflection-reviewed and 178 still `REVIEW_PENDING`.
Reviewed IDs = 170; pending class IDs = 890.
All 63 reviewed rows have `REFLECTION_OWNER_VERIFIED`,
a complete registered ID count and nonpending
five method-declaring owners. All 178 source-unreviewed
rows retain `REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY`.
**All 241 rows** remain `REVIEW_PENDING` in each of
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`, `gameplay_acceptance`.

## The completed 3A-4 reviewed signal-family subgroup

For the following **8 actual classes / 8 exact IDs**,
original compiled class identity, ID list and complete
five-owner signature tuple match current GitHub ledger:

| Java concrete class | Exact registered BLOCK ID | Placement / survive / updateShape / randomTick / setPlacedBy declaring owners | Source-level disposition |
|---|---|---|---|
| `TripWireBlock` | `minecraft:tripwire` | `TripWireBlock / BlockBehaviour / TripWireBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `TripWireHookBlock` | `minecraft:tripwire_hook` | `TripWireHookBlock / TripWireHookBlock / TripWireHookBlock / BlockBehaviour / TripWireHookBlock` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `RedStoneWireBlock` | `minecraft:redstone_wire` | `RedStoneWireBlock / RedStoneWireBlock / RedStoneWireBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `RepeaterBlock` | `minecraft:repeater` | `RepeaterBlock / DiodeBlock / RepeaterBlock / BlockBehaviour / DiodeBlock` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `ComparatorBlock` | `minecraft:comparator` | `DiodeBlock / DiodeBlock / ComparatorBlock / BlockBehaviour / DiodeBlock` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `ObserverBlock` | `minecraft:observer` | `ObserverBlock / BlockBehaviour / ObserverBlock / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `TargetBlock` | `minecraft:target` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |
| `RedstoneLampBlock` | `minecraft:redstone_lamp` | `RedstoneLampBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` | `SOURCE_REVIEWED_INTEGRATION_PENDING` |

The semantic source investigations are separate and
were already documented:

- [3A-4.1 eight original-owner and comparative Java
  source audit](PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md):
  tension cable/hook, wire graph, diode and
  comparator BE, observer, target, lamp.
- [3A-4.2 independently joined ITEM records and
  non-item state authors](PHASE2_STAGE3A_REDSTONE_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
  **8/8** exact `placed_block` mapping, **6 ordinary
  `BlockItem` + 2 `ItemNameBlockItem`**.
  Exception aliases: `minecraft:string` ->
  `minecraft:tripwire`, `minecraft:redstone` ->
  `minecraft:redstone_wire`. Common item method
  declaring owners resolve to `BlockItem`;
  no inference that item ID equals BLOCK ID.
- [3A-4.3 source-local vs physical neighbor and
  signal API direction](PHASE2_STAGE3A_REDSTONE_PORT_TOPOLOGY_1_21_1.md):
  remote 41-cell cable/hook traversal, wire
  `NONE/SIDE/UP` shape/strength, Diode/Observer
  watched-input versus opposite physical-output
  notification, comparator BE, scheduling priority.

**This 3A-4.4 checkpoint did not newly source-review
any class and did not verify NeoForge patched bytecode.**

## Explicitly deferred candidate/graph classes

| Concrete BLOCK class | Original exact registered BLOCK ID(s) | Still unreviewed |
|---|---|---|
| `DetectorRailBlock` | `minecraft:detector_rail` | `REVIEW_PENDING` |
| `PoweredRailBlock` | `minecraft:activator_rail`, `minecraft:powered_rail` | `REVIEW_PENDING` |
| `DaylightDetectorBlock` | `minecraft:daylight_detector` | `REVIEW_PENDING` |
| `RailBlock` | `minecraft:rail` | `REVIEW_PENDING` |

A future source-level rail class review **must** include
the normal `RailBlock` in addition to detector/powered
rails and nonregistered `BaseRailBlock`, `RailState`.
These four exact rail IDs belong to **3** pending
classes and use the P28 track shape graph.
Daylight detector (1 ID) is a **different environmental
signal sampling / `BaseEntityBlock` family** and
does not belong to the rail-graph owner group.
None were promoted by this checkpoint.

## Runtime and cross-phase gates NOT passed

- NeoForge patched `INVOKE`/ASM method bodies and
  actual applied `@Inject`/`@Redirect` targets.
- Mixin reachability in compiled NeoForge 21.1.215
  for `BlockState` / `LevelAccessor` / signal getter.
- Correct side/corner world coordinates, walking
  around face edges, support check, rotated
  `VoxelShape` / entity collision; source-local
  property vs physical target-local back-face.
- Electrical network and tick-order graph (Phase 7A),
  including wire recursion, diode priorities,
  comparator `BlockEntity`, hook chain and rail
  power detection; visuals (Phase 3).
- Vanilla non-Planet regression tests, modded
  feature placement, build and actual client/server.

The research card [3A-4](../phases/phase-02/02b-redstone-signal-owners.md)
has all **four of its independent research subtasks**
done; **Stage 3A (241 class target), Phase 2
implementation and Phase 7A are still INCOMPLETE**.

**Next FIRST separate microtask:** [Stage 3A-5,
rail topology owners](../phases/phase-02/02c-rail-owners.md),
task **1**: original compiled NeoForge owner signature
and complete vanilla comparative source audits
for **3 still-pending rail classes / 4 IDs**;
do NOT auto-include DaylightDetector or label
any ASM/Planetary runtime/gameplay PASS.
