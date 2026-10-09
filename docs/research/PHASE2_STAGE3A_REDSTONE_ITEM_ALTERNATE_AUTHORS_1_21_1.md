# Stage 3A-4.2 — original ITEM registry and non-item signal authors

**2026-10-10**, Planetary branch `2.0`, Minecraft **1.21.1**,
NeoForge target **21.1.215** / Java 21. One bounded
docs-only research package; **no newly promoted concrete block
classes**. The eight classes were source+NeoForge reflection
reviewed in [3A-4.1](PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md).

## 1. Independent original NeoForge ITEM census (exact `placed_block`)

Primary data: **unmodified** ZIP from
[GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact ID `11643813158`, original repo revision
`aa39572950a15403ea0a9003eefccf3bf6675ff7`,
SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Explicitly joined **1333 original ITEM records** from
`phase2-neo1211-item-registry.tsv` by real `placed_block`
to eight reviewed block IDs from the **1060-row BLOCK registry**.
No ID-prefix inference.

| Exact registered block ID | Exact registered ITEM ID | Compiled concrete ITEM Java class | True item creator |
|---|---|---|---|
| `minecraft:tripwire` | `minecraft:string` | `ItemNameBlockItem` | string item placement |
| `minecraft:tripwire_hook` | `minecraft:tripwire_hook` | `BlockItem` | hook item placement |
| `minecraft:redstone_wire` | `minecraft:redstone` | `ItemNameBlockItem` | redstone dust placement |
| `minecraft:repeater` | `minecraft:repeater` | `BlockItem` | repeater item |
| `minecraft:comparator` | `minecraft:comparator` | `BlockItem` | comparator item |
| `minecraft:observer` | `minecraft:observer` | `BlockItem` | observer item |
| `minecraft:target` | `minecraft:target` | `BlockItem` | target item |
| `minecraft:redstone_lamp` | `minecraft:redstone_lamp` | `BlockItem` | lamp item |
| **Exact total** | **8 unique items** | **6 `BlockItem` + 2 `ItemNameBlockItem`** | **8 matched targets** |

For **all eight** original compiled records,
`block_item=true` and
`placed_block` equals the exact target
BLOCK ID. All seven distinct exact method declarations
resolve to **`BlockItem`**, including the two
`ItemNameBlockItem` instances:
`useOn(UseOnContext)`,
`place(BlockPlaceContext)`,
`updatePlacementContext(BlockPlaceContext)`,
`getPlacementState(BlockPlaceContext)`,
`placeBlock(BlockPlaceContext,BlockState)`,
`canPlace(BlockPlaceContext,BlockState)`, and
`registerBlocks(Map,Item)`.
These are **nearest declaring owners**, not proof
of patched NeoForge method bodies or actual callback
invocations. There is NO separately registered
`minecraft:tripwire` ITEM and NO
`minecraft:redstone_wire` ITEM in the census.

Comparative [`ItemNameBlockItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/ItemNameBlockItem.java#L4)
extends `BlockItem` and overrides
`getDescriptionId()`, not placement lifecycle.
Actual [`BlockItem.useOn`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L46)
dispatches through `place` then
`getPlacementState` to the **placed block's**
`getStateForPlacement` even if item and block IDs
have different names. Raw item-registration name
is NOT the target block state class.

**Non-vanilla alternate serialized author:**
[`BlockItem.updateBlockStateFromTag`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/BlockItem.java#L158)
may read an item's `DataComponents.BLOCK_STATE` and
replace properties of a **just placed** state
(only when a component is present). Its work
precedes `setPlacedBy`, so a hook's
`calculateState` may be called on the component-modified
state. `updateBlockEntityComponents` and
`updateCustomBlockEntityTag` may also run for comparator
block entities. This is an **available code path**,
not a claim normal survival item stacks carry
custom `BLOCK_STATE` or BE components.
No generic autorotation of serialized directions
has been proven.

## 2. Non-item state writers: trigger to cell effects

| Concrete mechanism | Trigger and true state writer | Multi-cell and Phase 7A consequence |
|---|---|---|
| `TripWireBlock` | `entityInside` / scheduled `tick` -> `checkPressed` -> `setBlock(POWERED)` -> `updateSource`; `onPlace`/`onRemove`; `playerWillDestroy` with shears writes `DISARMED` | Entity collision box detection and cable hook recalculation, not ordinary item placement |
| `TripWireHookBlock` | `setPlacedBy`, scheduled `tick`, `onRemove` and direct call from TripWire -> **static `calculateState`** | Walk up to 41 cells, write both endpoint hooks `ATTACHED/POWERED` and intermediate tripwire `ATTACHED`, notify neighbors |
| `RedStoneWireBlock` | `onPlace`, `onRemove`, `neighborChanged` -> `updatePowerStrength`; `updateShape` mutates `RedstoneSide` graph properties | Writes `POWER`, physical neighbor notifications, corner/up/down rechecks; `shouldSignal` temporary self-suppression during power calculation |
| `RepeaterBlock` | `useWithoutItem` cycles `DELAY`; lateral `updateShape` changes `LOCKED`; inherited `DiodeBlock.setPlacedBy`, `neighborChanged`, scheduled `tick` | Directed front/back/side signal ports, `TickPriority` and deferred POWERED changes |
| `ComparatorBlock` | `useWithoutItem` cycles `MODE` -> `refreshOutputState`; inherited `DiodeBlock.neighborChanged` -> `checkTickOnNeighbor`; comparator scheduled `tick` -> `refreshOutputState` | Analog input, item-frame and container, COMPARE/SUBTRACT, `ComparatorBlockEntity.output` stored separately from `BlockState.POWERED` |
| `ObserverBlock` | `updateShape` on watched FACING -> `startSignal`; scheduled `tick` flips `POWERED`; `onPlace`/`onRemove` notification | Physical opposite output-side notifications, 2-tick pulse, avoid broad global direction rewrite |
| `TargetBlock` | `onProjectileHit` -> `updateRedstoneOutput` -> `setOutputPower`; scheduled `tick` clears power | Hit face and local fractions scored **physically**, duration 20 ticks for arrows versus 8 otherwise; signal output P36 |
| `RedstoneLampBlock` | initial `getStateForPlacement` samples `hasNeighborSignal`; `neighborChanged` writes ON or schedules OFF; scheduled `tick` validates loss of signal | Has NO FACING property, but signal consumer logic still depends on neighbor graph |

### Precise author and side-effect source paths

**Tripwire string + hooks:** [`TripWireBlock.checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L175)
queries entities overlapping the current state shape,
writes `POWERED`, calls
[`updateSource`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L127)
on lines scanned toward hooks and schedules its
own tick (10).
[`TripWireHookBlock.calculateState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L128)
is **static and receives both an optional replacing segment
and a hook position**. It rewrites both hooked endpoints and
zero or more intermediate segments with physical neighbour
updates. [`playerWillDestroy`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L116)
can mark `DISARMED` before removal. These
authors bypass the item placement algorithm entirely.
Hook `setPlacedBy` ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L123))
is the item-placed entrypoint, but later changes can
start from any segment.

**Redstone dust:** [`updatePowerStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L330)
computes a new target power, writes it only if
the observed state still matches, and issues
notifications around six physical neighbors;
[`calculateTargetStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L356)
suppresses its own outgoing `shouldSignal` when
reading incoming sources, then reads
side/up/down potential wire strengths.
[`updateNeighborsOfNeighboringWires`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L440)
handles neighboring corner cells. These do
not behave like the boolean
`FenceBlock.updateShape` connection model;
`RedstoneSide.NONE/SIDE/UP` is THREE-valued,
not merely connected vs disconnected.
[`neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L463)
also drops wire when it loses support; this
is independent of placement item class.

**Repeater and comparator inheritance:**
[`DiodeBlock.setPlacedBy`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L194)
may schedule a tick after placement;
[`DiodeBlock.neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L99)
and [`checkTickOnNeighbor`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L118)
compute turn-on transitions with tick priorities;
[`DiodeBlock.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L56)
owns repeater scheduled execution, except comparator
overrides its tick to refresh analog output.

[`RepeaterBlock.useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L48)
cycles delay, and [`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L71)
recalculates side-LOCKED state. Its signal logic
must not be simplified to an unconditional repeat
of wire's `POWER`.

[`ComparatorBlock.useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L159)
switches COMPARE/SUBTRACT mode and immediately
calls [`refreshOutputState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L193)
which checks `ComparatorBlockEntity.getOutputSignal`,
mutates `output` and notifies downstream.
[`ComparatorBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/ComparatorBlockEntity.java#L8)
persists that integer under NBT
`OutputSignal`; no `POWER` int in the
comparator BlockState itself. Generic block-entity
application from BlockItem and redstone scheduling
remain distinct paths.

**Observer:**
[`ObserverBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L70)
calls `startSignal` for FACING-side updates;
[`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L54)
alternates POWERED on/off and calls
[`updateNeighborsInFront`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L87)
on the opposite output cell. `onPlace` and
`onRemove` have additional pending-tick conditions.
This is a directed event and notification pipeline,
not an item-authored block graph.

**Target / lamp:**
[`TargetBlock.onProjectileHit`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TargetBlock.java#L46)
and [`getRedstoneStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TargetBlock.java#L71)
score the **actual physical projectile impact plane**
and apply output with a scheduled reset at
[`setOutputPower`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TargetBlock.java#L92).
[`RedstoneLampBlock.neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneLampBlock.java#L40)
turns `LIT` on or schedules turn-off; the
[`scheduled tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneLampBlock.java#L61)
checks the current signal before clearing the light.

## 3. Distinguish class/BE/ITEM ownership before adaptation

1. The eight ITEMS are **not** eight identical placement
   IDs: `minecraft:string` and `minecraft:redstone`
   are `ItemNameBlockItem` owners whose `placed_block`
   differs. Registries are direct runtime source evidence;
   no matching by item suffix or assumption of block-only
   `BlockItem` instance is safe.
2. All eight Item placement signatures resolve to
   `BlockItem` at the **declaration** level, but the
   underlying Block `getStateForPlacement` implementations
   differ (from 3A-4.1). `setPlacedBy` can have an
   entirely different owner and authors later source/BE
   effects. Item-state component mutation can precede it.
3. No `randomTick` on these eight does **not**
   mean no ticks: these families use distinct **scheduled**
   ticks and `neighborChanged` callbacks, including
   RedStoneWire's synchronous causal propagation.
4. Distinguish physical **hit location**, entity
   collision, `LevelAccessor` world callbacks,
   canonical local signal ports and source-vs-target
   block frame before any future Phase 7A integration.
   Never globally remap a vanilla signal Direction.
5. Ensure a comparator's analog output is checked
   in both the BlockState and its BlockEntity, and
   ensure physical world updates never bypass its
   required `refreshOutputState`. Keep BE data
   unchanged by orientation patches unless a specific
   transformation policy is proven.
6. Preserve vanilla behavior in non-Planet worlds and
   in mod/fake levels; actual NeoForge patch bytecode,
   Mixin weave and gameplay comparisons remain REQUIRED.

**Not yet investigated in this card:** actual ITEM
creators and off-item authors of
`DetectorRailBlock`, `PoweredRailBlock` and
`DaylightDetectorBlock`, still class-level
`REVIEW_PENDING`. The set of all redstone-related
producers in the game is larger than the present
eight; this is NOT a 7A-complete review.

## 4. Quantitative checkpoint and next task

- Registered concrete BLOCK classes **241**, BLOCK IDs
  **1060**, original ITEM records **1333**.
- Stage 3A-4.2: exact **8/8** reviewed BLOCK IDs joined
  to **8** registered ITEM records, split **6 ordinary
  `BlockItem`** and **2 `ItemNameBlockItem`**.
  **All seven shared item placement/effective owner signatures
  resolve to `BlockItem`**.
- **No new source class review this turn**: cumulative
  class statuses **63/241 source+reflection reviewed**
  (170 registered BLOCK IDs) and
  **178/241 REVIEW_PENDING** (890 IDs).
- Each of **241** ledger rows remains
  `REVIEW_PENDING` for
  `neoforge_patch_bytecode_review`,
  `planet_adapter_acceptance`,
  `gameplay_acceptance`.
- No Java code changed; no build, original NeoForge
  method-body ASM INVOKE verification, CI run or
  gameplay tests were executed for this packet.

**NEXT first unchecked task:** Stage **3A-4.3**,
card checkbox **3**, source-vs-target frame contracts,
redstone signal port/notification topology at seams,
wire recursion and causal tick order, tripwire
41-cell cable and rail-graph cross-phase handoff.
Do not take a new owner family and do not mark
gameplay PASS in that next research packet.
