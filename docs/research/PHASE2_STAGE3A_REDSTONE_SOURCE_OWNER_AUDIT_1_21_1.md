# Phase 2 Stage 3A-4.1 — selected redstone, tension and signal owner paths

**Checkpoint:** 2026-10-10; Minecraft 1.21.1 / NeoForge
21.1.215 / Java 21; Planetary branch `2.0`.

**One bounded research package:** **8** actual previously
`REVIEW_PENDING` concrete BLOCK classes / **8**
registered BLOCK IDs of the original 11-class signal
candidate cohort (12 IDs). These have DIFFERENT
algorithms and distinct Phase 2 vs Phase 7A
responsibilities, even when one signal circuit connects them.
`DetectorRailBlock` (1 ID), `PoweredRailBlock`
(**2** IDs: `activator_rail` and `powered_rail`) and
`DaylightDetectorBlock` (1 ID) are **explicitly NOT
source reviewed in this package** and remain
`REVIEW_PENDING`. RailState and rail-neighbor graph
(P28) are separate from wire graph (P25); daylight sensor
has world/BE sample lifecycle.

## Evidence provenance (exact owner vs approximate source)

**Original compiled runtime registry:** GitHub Actions
[run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact ID `11643813158`, original source commit
`aa39572950a15403ea0a9003eefccf3bf6675ff7`.
Original ZIP SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Decoded the original `phase2-neo1211-block-registry.tsv`
(1060 IDs, 241 concrete Java implementation classes)
and checked all eight exact class names, individual
`minecraft:` IDs, inherited hierarchies and
**five exact method signatures**, not bare-name guesses.
Also checked meaningful `neighborChanged`, `tick`,
`onPlace`/`onRemove` and `useWithoutItem` declaring
owners where present.

**Method body semantics** compared against pinned
Minecraft 1.21.1 source revision
[`hackersense/OptiFine-Source` `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block).
These comparative sources are **NOT identical evidence**
to NeoForge patched 21.1.215 class bytes, Mixin injection
match counts or gameplay. All of those remain PENDING.
Reviewed Planetary Mixin directory/config by name: **no
dedicated registered TripWire/RedStoneWire/Diode/Observer/
Target/Lamp source Mixin** in the current roster. This
does not prove no generic hook or external integration.

## Exact five-signature nearest declaration owners (NeoForge)

Headers shorthand:
- `place` = `getStateForPlacement(BlockPlaceContext)`
- `survive` = `canSurvive(BlockState,LevelReader,BlockPos)`
- `shapeUpdate` = `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`
- `random` = `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`
- `placedBy` = `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`

| Concrete class | Registered BLOCK ID | place / survive / shapeUpdate / random declaring owners | placedBy owner | P mechanisms | Comparative class evidence |
|---|---|---|---|---|---|
| `TripWireBlock` | `minecraft:tripwire` | `TripWireBlock / BlockBehaviour / TripWireBlock / BlockBehaviour` | `Block` | P25,P34,P35,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L78) |
| `TripWireHookBlock` | `minecraft:tripwire_hook` | `TripWireHookBlock / TripWireHookBlock / TripWireHookBlock / BlockBehaviour` | `TripWireHookBlock` | P23,P24,P25,P35,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L80) |
| `RedStoneWireBlock` | `minecraft:redstone_wire` | `RedStoneWireBlock / RedStoneWireBlock / RedStoneWireBlock / BlockBehaviour` | `Block` | P22,P24,P25,P35,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L154) |
| `RepeaterBlock` | `minecraft:repeater` | `RepeaterBlock / DiodeBlock / RepeaterBlock / BlockBehaviour` | `DiodeBlock` | P08,P22,P24,P35,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L63) |
| `ComparatorBlock` | `minecraft:comparator` | `DiodeBlock / DiodeBlock / ComparatorBlock / BlockBehaviour` | `DiodeBlock` | P08,P22,P24,P35,P36,P38 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L56) |
| `ObserverBlock` | `minecraft:observer` | `ObserverBlock / BlockBehaviour / ObserverBlock / BlockBehaviour` | `Block` | P07,P24,P35,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L70) |
| `TargetBlock` | `minecraft:target` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour` | `Block` | P34,P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TargetBlock.java#L46) |
| `RedstoneLampBlock` | `minecraft:redstone_lamp` | `RedstoneLampBlock / BlockBehaviour / BlockBehaviour / BlockBehaviour` | `Block` | P36 | [source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneLampBlock.java#L34) |

**Additional owners not in the five basic fields**:
- `TripWireBlock`: scheduled tick TripWireBlock; onPlace/onRemove TripWireBlock; neighborChanged BlockBehaviour.
- `TripWireHookBlock`: scheduled tick TripWireHookBlock; onRemove TripWireHookBlock; onPlace BlockBehaviour; neighborChanged BlockBehaviour.
- `RedStoneWireBlock`: neighborChanged/onPlace/onRemove RedStoneWireBlock; scheduled tick BlockBehaviour; useWithoutItem RedStoneWireBlock.
- `RepeaterBlock`: neighborChanged/actual scheduled tick/onPlace/setPlacedBy DiodeBlock; useWithoutItem RepeaterBlock.
- `ComparatorBlock`: neighborChanged/onPlace/setPlacedBy DiodeBlock; scheduled tick ComparatorBlock; useWithoutItem ComparatorBlock.
- `ObserverBlock`: scheduled tick/onPlace/onRemove ObserverBlock; neighborChanged BlockBehaviour.
- `TargetBlock`: scheduled tick/onPlace TargetBlock; onProjectileHit TargetBlock; neighborChanged BlockBehaviour.
- `RedstoneLampBlock`: neighborChanged + scheduled tick RedstoneLampBlock; onPlace BlockBehaviour.

**Scheduled tick ≠ `randomTick`:** all eight
`randomTick` declaring owners resolve to
`BlockBehaviour`, yet TripWire, Hook, Repeater
(inherited `DiodeBlock`), Comparator, Observer,
Target and Lamp use class- or ancestor-authored
**scheduled** ticks; RedStoneWire instead has
synchronous power propagation in neighbor/change
and onPlace/onRemove. Never infer absence of signal
behavior from `randomTick=BlockBehaviour`.

## Algorithm author paths and physical/local split

### 1. TripWireBlock + TripWireHookBlock: coupled cable with maximum span

[`TripWireBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L78)
authors four horizontal adjacency flags using
`shouldConnectTo`: a same-wire segment or a
`TripWireHookBlock` whose `FACING` points back.
[`TripWireBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L90)
handles world-horizontal callbacks and a single state
property. However
[`TripWireBlock.updateSource`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L127)
scans **SOUTH and WEST**, up to **41 cells** toward
a hook and invokes **static**
`TripWireHookBlock.calculateState`; this is an
alternate author path, not one adjacent-cell query.
[`entityInside` / `checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L155)
reads entity collision in the block's shape and
writes `POWERED`; delayed tick later rechecks.
[`playerWillDestroy`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L116)
can mark `DISARMED` before breaking when shears
are used. All are distinct from simple four-bit
orientation.

[`TripWireHookBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L80)
requires horizontal `FACING` and a sturdy target
side behind the hook.
[`getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L98)
tries nearest-looking horizontal candidates and
calls `canSurvive`.
[`setPlacedBy`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L123)
triggers full `calculateState`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L128)),
which scans along the hook's `FACING` for up to 41
positions, updates the **opposite hook** and
every participating **tripwire segment's ATTACHED**
state, then notifies physical neighbours.
**Local topology requirement:** physical positions
are authoritative; when crossing a cube edge,
a persistent cable axis cannot be simulated by
rotating only its *first* direction enum or
by a one-step tangent graph lookup. An explicit
walk/path and segment orientation policy is required.
Signal output ports and tick causal ordering are
Phase **7A**, not accepted here.

### 2. RedStoneWireBlock: RedstoneSide is not a fence Boolean

[`getConnectionState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L159)
computes redstone wire **`NONE`/`SIDE`/`UP`**
for each of NORTH/EAST/SOUTH/WEST, with DOT vs
CROSS special cases.
[`getMissingConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L201)
tests physical `above`, then four physical
`Direction.Plane.HORIZONTAL` positions.
[`getConnectingSide`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L286)
inspects neighbour on same, **above** or **below**
level, redstone conductor/support shape and
target sturdy side; support
[`canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L318)
tests a physical block below and source's local
surface support.
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L218)
has three separate branches for physical DOWN,
UP and tangents. The `shouldConnectTo`
family ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L512))
discriminates Diode facing, observer facing,
wire and other signal sources.

**Functional signal boundary:**
[`calculateTargetStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L356)
temporarily disables `shouldSignal` to avoid
self-feedback while reading neighbours, then scans
wire input strengths.
[`updatePowerStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L330)
writes `POWER` and issues notifications to the
physical neighborhood; `onPlace`/`onRemove`/
`neighborChanged` also participate. An
orientation-only one-block `updateShape` rewrite
cannot preserve this network without a Phase 7A
signal-port and update-order plan.
Use source-local tangent for `RedstoneSide` property
and physical target coordinates for the actual graph;
do not naïvely reclassify ABOVE/BELOW on a seam.

### 3. DiodeBlock / RepeaterBlock / ComparatorBlock: directed ports

[`DiodeBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L44)
checks physical `below`; its
[`getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L188)
orients `FACING` from the player's horizontal
direction, reads front signal with
[`getInputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L153)
and side signal at
[`getAlternateSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L170).
[`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L56)
and [`neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L99)
schedule transitions with priorities; onPlace
and setPlacedBy trigger other source/port
notifications.

[`RepeaterBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L63)
delegates to DiodeBlock then calculates `LOCKED`.
Its [`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L71)
handles support and lateral locking.
[`useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L48)
cycles `DELAY` independently of placement.
The P36 locking input is a **lateral signal port**,
not a generic rotation or neighbor-strength sum.

[`ComparatorBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L56)
handles downward support while inherited DiodeBlock
owns placement/survival.
Its [`getInputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L110)
can read adjacent analog-emitting blocks and a
two-cells-ahead `ItemFrame` behind a conductor.
[`calculateOutputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L70)
and [`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L224)
compute subtract/compare mode, but the numeric
output is backed by
`ComparatorBlockEntity`, not only the
`POWERED` BlockState flag.
[`useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L159)
toggles `MODE`. P38 block entity and P36 causal
signal graph are cross-phase evidence, not
present acceptance.

### 4. ObserverBlock: watched and output faces oppose

[`ObserverBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L141)
supports all six `FACING` directions.
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L70)
compares the changed physical neighbour
direction to the **watched** FACING property and
schedules a delayed pulse.
[`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L54)
toggles `POWERED` and
[`updateNeighborsInFront`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L87)
notifies the **opposite physical** output face.
[`getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L109)
restricts output to a particular port. A
side-face cube implementation must preserve
the source's local front/back semantics while
using true world neighbor positions and signal
Direction parameters, not reverse FACING
twice globally.

### 5. TargetBlock + RedstoneLampBlock: no direction-named property

[`TargetBlock.onProjectileHit`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TargetBlock.java#L46)
reads **physical** `BlockHitResult.getDirection()`
and the fractional hit position to score
`OUTPUT_POWER` without any `FACING` property.
[`setOutputPower`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TargetBlock.java#L92)
directly writes the state and schedules reset.
Local gravity should **not** rotate a projectile's
real hit vector or use a fabricated target FACING;
physical hit plane/scoring remains well-defined.
Port-facing signal integration remains P36.

[`RedstoneLampBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneLampBlock.java#L34)
samples `Level.hasNeighborSignal` to write
`LIT`.
[`neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneLampBlock.java#L40)
turns it on immediately or schedules turn-off;
[`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneLampBlock.java#L61)
validates signal before clearing `LIT`.
No orientation property does not make it
redstone-network-independent; it simply has
no directional placement state of its own.

## Cross-phase contract and remaining evidence gates

**Phase 2:** actual source-local placing/support,
`updateShape`, mapping 4-way wire/segment properties,
six-way observer, physical `BlockPos` traversal,
per-block target-local support; collision and
attachment semantics. Handle source `LevelAccessor`
vs `Level` and fake/no-player context explicitly.

**Phase 7A:** signal input/output port identity,
`shouldSignal` recursion, DiodeBlock front/side
inputs, comparator BE output, tripwire scan/timing,
observer pulse order, neighboring hooks and blocks
notification, lamp/target signal sources. **No
functioning redstone network claimed in this phase.**

**Phase 3:** wire and tripwire shape/collision visual
correctness at side/bottom face.
**Phase 8:** structure/worldgen and third-party
redstone block authors where relevant.
**P35:** rotate/mirror/clone/state component
port states preserve authored local semantics.

### Future tests (specified, not run)

1. Across all six planet faces, test standalone
tripwire, hook pair at 2–41 distance, removal and
shears; repeat with cable crossing a face seam
and corners. Verify attachment and entity
trigger boxes.
2. Wire DOT/CROSS, upward/downward step, solid
neighbor, stair/slab support, two-sided wire
corners and edge crossings; measure power
without recursion or oscillation and compare
identical geometry in vanilla world.
3. Repeater delays 1–4, side locking from both
directions; comparator compare/subtract with
analog inventory and item frame, scheduled tick
priority, and cross-face signal output port.
4. Observer watches all six physical orientations,
fires 2-tick pulses to opposite face, including
world edge; verify absence of self-trigger.
5. Target projectiles impact all six world sides,
hit-center/edge score; lamp on/off delayed
response and non-directional block behavior.
6. Audit real NeoForge method body patches,
Mixin handler match counts and every
alternative writer before any runtime PASS.

## Scope and next first unfinished card item

Before Stage 3A-4.1: **55/241** class-level
source+reflection-reviewed (162/1060 IDs) and
**186/241** REVIEW_PENDING (898 IDs).

This package: **8 classes / 8 actual registered IDs**,
all source+runtime-reflection declaration reviewed.
After: **63/241** source+reflection-reviewed
(170/1060 IDs); **178/241 REVIEW_PENDING**
(890/1060 IDs).
Three original cohort classes deferred:
`DetectorRailBlock` (1), `PoweredRailBlock` (2),
`DaylightDetectorBlock` (1); 4 IDs and no
semantic/disposition upgrade.

**All** `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and
`gameplay_acceptance` rows remain
`REVIEW_PENDING`. No production Java changed,
no build/client/actual NeoForge ASM test claimed.

**NEXT independent packet:** Stage **3A-4.2**
(card checkbox 2): independently join original ITEM
registry for these eight, including wire
redstone dust item, tripwire string, comparator
and repeater; verify non-item signal writers and
full producer/consumer interactions.
