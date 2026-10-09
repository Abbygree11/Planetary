# Stage 3A-4.3 — redstone signal ports, physical notifications, seam traversal and tick order

**2026-10-10** · branch `2.0` · Minecraft **1.21.1** /
NeoForge **21.1.215** / Java 21.
**Scope: the SAME 8 concrete BLOCK classes / 8 IDs**
previously marked `SOURCE_REVIEWED_INTEGRATION_PENDING` in
[3A-4.1 source+reflection](PHASE2_STAGE3A_REDSTONE_SOURCE_OWNER_AUDIT_1_21_1.md).
ITEM + non-item writers were audited
[in 3A-4.2](PHASE2_STAGE3A_REDSTONE_ITEM_ALTERNATE_AUTHORS_1_21_1.md).
**No newly reviewed class or verified gameplay result in this task.**

## Evidence boundaries

Comparative Minecraft 1.21.1 source:
[`hackersense/OptiFine-Source` revision `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).
Original NeoForge runtime reflection for class/declaring owner names,
but **not method bytes**, originates from
[CI artifact 11643813158](https://github.com/Abbygree11/Planetary/actions/runs/37988064055)
(original ZIP SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`).
Planetary local-frame, traversal and shape source was inspected on
parent commit `74119f482a4d1e82ea3af280eeca9df68e3be714`.
The observations below **describe source behavior and propose acceptance
contracts**; no NeoForge patched-bytecode/mixin injection
matching, unit tests, client run, signals or gameplay have
been validated in this packet.

## 1. Distinct direction identities for a single signal edge

| Notion | Typed name used here | Meaning |
|---|---|---|
| Local BLOCK_STATE direction | `sourceLocal` | Source block's `NORTH/EAST/SOUTH/WEST/FACING`, UP or DOWN semantic port |
| Physical adjacent displacement | `physicalStep` | `BlockPos` world XYZ neighboring cell: required by vanilla notification functions |
| Neighbor's local inward side | `targetLocalTowardSource` | Opposite of `physicalStep` transformed by **target's** canonical face, not source's |
| API signal query face | `queriedSignalDirection` | Argument supplied to `BlockState.getSignal/getDirectSignal` by `SignalGetter` callers: verify caller-side meaning before converting; not automatically `sourceLocal` |
| Traversal continuation | `transportedDirection` | Direction carried to next `PlanetBlockFrameContext.step` when walking an edge-spanning cable, not merely current block's property |
| Scheduled tick | `(pos, block, delay, priority)` | Tick scheduler identity/ordering in real server world; **not a geometric Direction** |

The first three can differ *at a single cube-edge adjacent pair*.
For example source-local EAST can be physically world UP,
yet neighbor-local face toward source can be SOUTH.
The invariants are:
`sourcePos.relative(physicalStep)==targetPos` and
`targetFrame.localToWorld(targetLocalTowardSource)==physicalStep.getOpposite()`.
Do **not** assert that local inward face is
`sourceLocal.getOpposite()` at seams.

Existing Planetary contracts already encode this distinction:
[`PlanetBlockStateFrame`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockStateFrame.java)
owns a **stable position-only** canonical chart;
[`PlanetBlockNeighborQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockNeighborQuery.java)
exposes `sourceLocalDirection`, `physicalDirection`,
`targetLocalSideTowardSource` and actual `targetPos`.
[`PlanetBlockFrameContext.walk`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockFrameContext.java)
iterates `step(...)` then transports direction on each step,
which supplies a potential physical path primitive for cables;
**it does not check tripwire/hook identity, local axes,
signal ports, remote hook orientation or time ordering**.
This is an infrastructure inventory, not a claim that
a redstone-specific adapted walker already exists.

## 2. TripWire + Hook is an ordered multi-cell state transaction

Source `TripWireBlock.updateSource` ([line 127](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L127))
walks up to **41** cells in the raw world SOUTH/WEST directions.
`TripWireHookBlock.calculateState`
([line 128](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L128))
walks from one hook's `FACING` for at most 41 cells,
collects `TripWireBlock` states and optionally the
replaced/removed segment, then updates:
1. near and far hooks' `ATTACHED` + `POWERED`;
2. all surviving segments' `ATTACHED`;
3. scheduler state / neighbors at **both** hook positions;
4. physical notifications adjacent to the hook backs,
   through [`notifyNeighbors`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireHookBlock.java#L260);
5. `GameEvent`/sound and entity trigger changes.

The key source `TripWireBlock.checkPressed`
([line 175](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L175)) checks
entities in `blockstate.getShape(...).bounds()`;
because `ATTACHED` toggles the shape from tall to low
([source line 72](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TripWireBlock.java#L72)),
the **physical hitbox rotation (Phase 3)** can affect
which entities are considered in contact and so must
be tested independently from the signal calculation.

**Design requirement, not implemented:** treat cable as a
bounded **ordered set of actual physical cell positions**
whose direction is transported at seams. At each step,
verify block type, opposite hook identity and its
**own local `FACING` mapped to the actual physical
reciprocal**. Never compute remote hook as
`start.relative(originalPhysicalDirection, i)` once
the path crossed a seam. Never merely rotate the
first `Direction` and leave the intervening segment
writes in vanilla linear coordinates.

**Risk to test:** a *bent-around-a-corner* Planet cable
needs an explicit policy for whether vanilla straight
cable alignment is preserved in local charts and whether
`ATTACHED` and hook signal should propagate across
a seam. Corner adjacency can carry two possible chart
paths: agree on canonical block chart and deterministic
traversal rather than selecting based on last player/view
face. Beware update recursion when hook rewrites wire,
wire calls `updateSource`, and remote hook notifies back.

**Not claimed:** that canonical Planet `walk()` already
guarantees this full semantic contract, or that a
41-cell cable physically spanning any corner works today.

## 3. RedStoneWireBlock: 3-valued connections vs causal POWER

### Connection graph is NOT network-power graph

[`RedStoneWireBlock.getConnectionState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L159)
computes four local tangent `RedstoneSide`
(`NONE`, `SIDE`, `UP`) and retains DOT/CROSS
presentation. [`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L218)
has **three** classes of physical callback:
DOWN checks support, UP recomputes whole connection
graph, tangent updates one port or triggers full
recomputation.

[`getConnectingSide`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L286)
inspects the neighboring cell on that physical side,
and its ABOVE/BELOW continuations. A source-local tangent
may correspond to global physical UP at a side planet
face; **its local-UP rise path is a separate edge** and
cannot be obtained by globally treating every vanilla
`Direction.UP` as world UP. The support check uses
target-local sturdiness in a way analogous to P22/P24;
both placement and update must preserve special allowed
support blocks.

**State shape:** `RedStoneWireBlock`
[`SHAPES_FLOOR` / `SHAPES_UP` / `SHAPES_CACHE`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L52)
is precomputed from local connection properties;
`getShape` normalizes POWER to zero before lookup
([source line 148](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L148)).
The shape is not automatically world-rotated by
changing `RedstoneSide` alone. Existing registered
[`BlockStateShapeMixin`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/mixin/BlockStateShapeMixin.java)
and [`PlanetBlockShapeRuntime`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockShapeRuntime.java)
are shared shape-boundary infrastructure, **not
tested redstone face geometry acceptance**.

### Signal strength and neighbor callbacks form a separate loop

[`calculateTargetStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L356)
temporarily flips the block **instance field**
`shouldSignal=false`, samples
`Level.getBestNeighborSignal`, sets it true, then
reads neighboring wire strengths including conditional
above/below offsets. [`getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L486)
checks `shouldSignal`, excludes `Direction.DOWN`
and gates some outputs on the physically queried
side and recomputed state connections.
**Danger:** a frame rewrite of `getSignal`,
`shouldConnectTo` or `getBestNeighborSignal`
must preserve the exact **caller-side meaning**
of `queriedSignalDirection`, and not reverse it
as if it were the target-local inward support side.

[`updatePowerStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L330)
writes the POWER `BlockState` when stale-state
identity still matches and sends notifications to
the block and its six physical neighboring cells.
[`updateNeighborsOfNeighboringWires`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L440)
adds corner neighbors including up/down.
[`updateIndirectNeighbourShapes`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedStoneWireBlock.java#L255)
explicitly issues `neighborShapeChanged` to wire
positions in an upward/downward diagonal.

**No global shortcut:** naively remap all
`Level.updateNeighborsAt`, `neighborShapeChanged`
or all `Direction.values()` would change other
block behaviors and possibly create duplicate,
missing or cyclic notifications. Physical notifications
must remain real BlockPos updates. Any Plane-local
semantics have to be resolved **at the algorithm
owner** and tested for update-order equivalence.

**Recursion/ordering risk:** `shouldSignal`
is a mutable member shared by block class instance
(as shown in comparative source); do not introduce
extra recursive signal queries around it without
a bytecode-informed reentrancy contract. The present
audit does not assert a confirmed race, multithreaded
bug or that NeoForge's patched wire evaluator has
identical method bodies. Phase 7A must independently
check transformed call sites and actual ordering.

## 4. DiodeBlock, Comparator and Observer are directed ports

**`DiodeBlock` common authority:** `RepeaterBlock`
and `ComparatorBlock` inherit a physical support check
through [`DiodeBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L44)
on the source's floor and
[`getInputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L153)
on the **state's** `FACING` side.
[`getAlternateSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L170)
uses `FACING.getClockWise()` and
`getCounterClockWise()` for side inputs.
[`getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L82)
returns output only if the API query side matches
`FACING`. But
[`updateNeighborsInFront`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L217)
notifies the world cell at
`pos.relative(FACING.getOpposite())`.
**Do not assume signal query Direction is the physical
displacement vector to the output cell**:
vanilla's signal-query side convention and its
BlockPos notification vector differ.
Reconstruct physical output cell using its authored
local port and preserve the query contract when
passing `Direction` to signal getters.
Both values may require separate conversion at
a face seam.

**Repeater:** `LOCKED` depends on two lateral
ports; `DELAY` is a user-controlled tick parameter
not a geometric axis. Its own
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RepeaterBlock.java#L71)
writes `LOCKED` when lateral callbacks arrive;
`DiodeBlock.tick` ([line 56](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DiodeBlock.java#L56))
owns scheduled POWERED writes.
**Comparator:** the `MODE` writer and analog
two-cells-ahead / ItemFrame input
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L110))
can cross a seam, yet output strength persists as
`ComparatorBlockEntity.OutputSignal`; a correct
state FACING alone cannot guarantee correct
analog front/back and lateral policy.
`ComparatorBlock.useWithoutItem` recalculates
BE output synchronously;
[`checkTickOnNeighbor`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L177)
schedules a delay before calling
[`refreshOutputState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ComparatorBlock.java#L193).

**Observer:** [`ObserverBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L70)
listens to neighbor changes on the *watched*
`FACING` side, while
[`updateNeighborsInFront`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L87)
notifies at the opposite side.
[`getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L109)
returns 15 only when `POWERED` and
`queriedSignalDirection == FACING`.
That source-level convention is consistent with an
opposite-side output cell being queried from its
inward direction; it MUST be tested with the
NeoForge actual caller, not guessed from word
'front'. [`tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ObserverBlock.java#L54)
toggles a two-tick pulse and notifies downstream.
Global signal-direction rotation would
mistakenly couple watched side with emitted side.

### Scheduled tick causality must survive geometry adaptation

| Mechanism | Source-known tick rule | What must remain invariant |
|---|---|---|
| TripWire press | Recheck 10 ticks while pressed | Shape -> entity hit, segment POWERED, hook recomputation ordering |
| TripWireHook | `calculateState` tick after segment changes | Physical endpoint/segment writes + notifications |
| Repeater | `DiodeBlock` `getDelay=DELAY*2`; HIGH/VERY_HIGH/EXTREMELY_HIGH priority cases | Per-position queued tasks, priority and `LOCKED` check |
| Comparator | Own delay **2 ticks**, `ComparatorBlockEntity` refresh on mode/input | Distinguish BE output and state POWERED, notify after result |
| Observer | 2-tick on/off pulse and tick de-duplication | FACING watched vs opposite output port |
| Target | 8 ticks default, **20** for arrows | Physical hit face/fractions + scheduled OUTPUT_POWER reset |
| Lamp | Immediate on and delayed **4 ticks** off | `hasNeighborSignal` before clearing LIT |

These are original comparative 1.21.1 rules, not
verified patched NeoForge timing or new game tests.
Existing Planetary `LevelTicksMixin` is registered
in mixin config, but its presence **does not prove**
these redstone author behaviors are adapted or
signal-tick priorities preserved.

## 5. Rails and daylight: inspect handoff only, still NOT classified

Three actual original registered concrete classes
remain source/disposition `REVIEW_PENDING`:
`DetectorRailBlock` (1 ID), `PoweredRailBlock` (2)
and `DaylightDetectorBlock` (1).
Contextual comparative source highlights **why
their ownership must not be merged into wire work**:

- [`BaseRailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L62)
  uses a `RailShape`, floor support,
  `updateState/updateDir` and
  `RailState` adjacency resolution, with slope
  geometry `RailShape.isAscending`; **P28** is a
  rail topology/track-shape problem beyond P25.
- [`PoweredRailBlock.findPoweredRailSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L39)
  traverses up to 8 connected rail segments, branches
  on `RailShape` and physical X/Y/Z increment,
  which does **not** automatically match a
  source-local 4-way wire graph or TripWire's 41-step
  cable. Signal semantics remain Phase 7A.
- [`DetectorRailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L72)
  updates power and connected rails based on
  entity detection / scheduled tick rather than
  copying a wire's `getSignal`.
- [`DaylightDetectorBlock.updateSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L65)
  samples sky brightness / sun angle and
  `INVERTED` state; this is environmental sensing
  and world/BE scheduling, not directional
  `FACING` placement. Its full alternate author
  chain has **NOT** been audited.

Those glimpses are **cross-phase boundary evidence only**.
Do NOT promote any of the three classes without
a *separate complete source+NeoForge owner task*.

## 6. Phase boundaries and future acceptance matrix (NOT EXECUTED)

**Phase 2 owns** source-local placement/state,
topology-aware property port mapping, support
and neighbor callbacks, tracking real actual
block positions, reversible source/target
face conversions. **Phase 3 owns**
render, entity ray/collision shape orientation,
TripWire entity-press physical AABB and wire
local/physical voxel geometry. **Phase 7A owns**
full signal API side convention, redstone
causal propagation/recursion, scheduled tick
priority and de-duplication, multi-cell
tripwire and powered rail logical networks,
observer output and comparator BE signal state.
**Phase 8** owns generation/structure writes
when relevant.

**Explicit future tests (none run):**

1. On all six cube faces, each studied block:
   placement, physical support, visible/local
   orientation and the actual `getSignal` /
   `getDirectSignal` **query direction** measured
   at every output side. Include a vanilla
   non-Planet control world.
2. On four cube **edges and representative
   corners**, wire DOT/CROSS, NONE/SIDE/UP and
   above/below climb, input/output on either
   side with no missed/double notifications.
3. Redstone chain ring/feedback, at least
   15-segment attenuation, repeated toggling
   and high-frequency neighbor updates:
   verify stable power with matching priority
   and update order; no recursive loops,
   extra signals or missed transitions.
4. Repeater forward/lateral side lock, DELAY
   1..4; comparator COMPARE/SUBTRACT + BE
   OutputSignal/item-frame analog input;
   observer watched-side-only pulses for all
   six orientations (and seam-crossing output).
5. Two hook tripwire 2..41-cell chains,
   crossing one face edge and a corner,
   segment removal/disarm, entity collision
   at both low/raised AABB; verify both
   hooks and all segment states without
   falsely assuming original straight XYZ ray.
6. Target block physical arrows/non-arrows
   on every face (center/edge),
   lamp delayed release and daylight sensor
   *later* only after owner classification.
7. After source review, inspect **actual
   NeoForge 21.1.215 patched ASM/INVOKE**,
   Mixin targets and any alternate signal
   evaluator, then verify client + dedicated
   server/ticks. Source-only check does not
   establish application or gameplay PASS.

**Status unchanged:** 241 concrete BLOCK
classes / 1060 registry IDs; **63/241**
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(170 IDs) and **178/241 `REVIEW_PENDING`**
(890 IDs). All **241** `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance` remain `REVIEW_PENDING`.
No production Java modified; no tests or game
client executed.

**NEXT FIRST separately committable task:
Stage 3A-4.4**, exact original NeoForge ZIP
reviewed class/ID/owner + status reconciliation;
prepare next small-family card without silently
promoting rails/daylight or claiming
Phase 2/7A complete.
