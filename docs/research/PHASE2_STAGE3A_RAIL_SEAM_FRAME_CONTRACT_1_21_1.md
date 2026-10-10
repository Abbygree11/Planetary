# Phase 2 Stage 3A-5.3 — rail geometry, local slope, seam chart and minecart boundary

**2026-10-10** · `2.0` · Minecraft **1.21.1** /
NeoForge **21.1.215** / Java **21** ·
**one bounded research-only step**, no Java patches or
runtime/CI/gameplay acceptance.

**Input corpus:** the 3 source+runtime-declaration reviewed
rail classes (4 registered IDs) from
[3A-5.1](PHASE2_STAGE3A_RAIL_SOURCE_OWNER_AUDIT_1_21_1.md)
and [4 precise ITEM and off-item author paths,
3A-5.2](PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md).
Pinned comparative 1.21.1 Java revision
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1).
Actual patched NeoForge method bytecode/INVOKEs and
applied Mixin target matches are **not** established by
the source-level chart; all such gates remain pending.

## 1. Semantic axes and coordinates — five notions must NOT collapse

| Kind | Example | Invariant / ownership |
|---|---|---|
| Source-local rail shape port | `RailShape.ASCENDING_EAST`, straight/quarter-turn | Rail BlockState property; `NORTH`, `SOUTH`, `EAST`, `WEST` interpreted in a stable canonical *source-local* chart |
| Physical adjacent cell | `source.pos.relative(physicalDirection)` | A real one-block XYZ displacement, required for `Level.setBlock`, notifications, loaded sections |
| Target-local inbound port | `targetFrame.worldToLocal(physicalDirection.getOpposite())` | Target BlockState frame may differ, especially edge or shared corner; never substitute `sourcePort.getOpposite()` |
| Local ascending/downward offset | `local UP` (and DOWN for searching nearby rails) | Independently resolved local radial direction; **not** world `BlockPos.above/below` at side faces |
| Traversed path continuation | `PlanetBlockStep.transportedDirection` | Necessary when one connection/rail path walks across a seam; different from a fixed XYZ vector |

Existing Planetary infrastructure checked at the parent
revision `97c7c78ff2df3f12290cc57485e7054309d3b53c`:
[`PlanetBlockStateFrame`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockStateFrame.java)
resolves a **canonical frame by physical position**;
[`PlanetBlockNeighborQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockNeighborQuery.java)
records source-local direction, physical displacement,
target-local inward face and boundary-crossing;
[`PlanetBlockFrameContext`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockFrameContext.java)
implements physical local-step resolution and
`walk(...)` with transported directions;
[`PlanetBlockStep`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockStep.java)
explicitly warns its target **traversal** chart is
not guaranteed to equal the target's canonical
BlockState frame at a corner.
[`PlanetBlockSupportQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockSupportQuery.java)
supplies the support's own local inward face.

**Important limitation:** these generic primitives have
**not** been verified as a full replacement for
`RailState`'s rail-specific connection graph; `walk()`
does not validate track block types, slope height,
reciprocal shape ports, turn choice, cart curvature or
signal graph behavior. A single `walk(EAST, N)` is
not a substitute for a junction-specific decision
or an ascending endpoint lookup.

## 2. RailShape ports: document the actual state vocabulary

Comparative [`RailShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/properties/RailShape.java#L5)
contains **ten** values, of which **four** are ascending:

| `RailShape` | Source-local connectivity (before converting positions) |
|---|---|
| `NORTH_SOUTH` | NORTH at grade / SOUTH at grade |
| `EAST_WEST` | EAST at grade / WEST at grade |
| `ASCENDING_NORTH` | NORTH **one local UP** / SOUTH at grade |
| `ASCENDING_SOUTH` | SOUTH **one local UP** / NORTH at grade |
| `ASCENDING_EAST` | EAST **one local UP** / WEST at grade |
| `ASCENDING_WEST` | WEST **one local UP** / EAST at grade |
| `NORTH_EAST` | NORTH at grade / EAST at grade |
| `NORTH_WEST` | NORTH at grade / WEST at grade |
| `SOUTH_EAST` | SOUTH at grade / EAST at grade |
| `SOUTH_WEST` | SOUTH at grade / WEST at grade |

Actual [`RailState.updateConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L37)
constructs that exact set as **physical Cartesian positions**
via `pos.north/east/...` and `pos.east().above()`;
[`RailState.getRail`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L119)
checks candidate grade then world above/below. For
rising tracks, resolving **tangent followed by
local UP** and verifying its reciprocal endpoint
must be defined in the target frame. At a seam,
these two operations can have different physical
results depending on which chart owns the intermediate
cell; do not assume they commute.

Ordinary `RailBlock` (constructor
[`isStraight=false`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailBlock.java#L22))
allows bends, while
[`DetectorRailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L40)
and [`PoweredRailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L22)
use `RAIL_SHAPE_STRAIGHT`, which supports
straight/rising states but **not** curved
`NORTH_EAST` etc. Even though all are rail
blocks, an adapter cannot force quarter turns
onto the latter two immutable BlockState value sets.

## 3. RailState source assumptions / exact conversion sites

| Actual comparative source site | Vanilla assumption | Required Planet integration contract |
|---|---|---|
| [`RailState.updateConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L37) | Hardcoded X/Y/Z `north/east/west/south` and positive global Y for rising endpoint | Resolve physical endpoint from canonical source-local `RailShape`, after each step verify intended target and slope grade |
| [`RailState.getRail/hasRail`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L111) | At candidate, try same cell, world Y+1, world Y−1 | Query target candidate's **canonical** local UP/DOWN, preserving a deterministic search order and avoiding an edge/corner shortcut |
| [`RailState.hasConnection`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L150) | Endpoint comparison matches physical X and Z, disregards Y | Replace the **horizontal projection rule** with a stable local-column/port compatibility policy; at seam a world-Y-ignoring test can match unrelated cells or miss a rail |
| [`countPotentialConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L165) | 4 global horizontal neighbors | Enumerate four *source-local* tangent directions; never scan fixed world HORIZONTAL on ±X/±Z faces |
| [`RailState.connectTo`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L185) | Mutates `state.SHAPE` and writes `Level.setBlock` for current rail | Preserve 2-slot logical rail-port cardinality and same physical target back-reference; no duplicate/ghost connections |
| [`RailState.place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L281) | Chooses corner/straight/ascending under neighbor topology and local signal | Preserve vanilla junction priority, allowed shape vocabulary and neighbor **multi-cell state writes**, including seam; no global `Direction` mixin |
| [`BaseRailBlock.canSurvive/shouldBeRemoved`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L62) / [`shouldBeRemoved`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L107) | Underlying rigid support at `pos.below`, additional adjacent support for ascending rail | Local DOWN support via `PlanetBlockSupportQuery` and correct additional shape-side support physical cell and its own canonical target face |
| [`BaseRailBlock.onRemove`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L153) | Sends world `.above()` callback for rising rail; world below for straight track | Notify actual physical positions in appropriate chart while preserving vanilla non-Planet world behavior |
| [`BaseRailBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L186) | Primarily schedules a water tick when `WATERLOGGED` | Do **not** mistake generic neighbor-shape callback for `RailState` placement/update network writer; water is Phase 5 |

**Canonical reciprocal-port policy to verify (not yet implemented):**

For each rail cell `A` and each allowed local port `p`,
resolve a physical endpoint `B` using its **source frame**
and, where ascending, a documented local-UP grade step.
At `B`, determine which candidate rail state / connection
refers back to **the actual physical source endpoint** after
its own canonical chart conversion. Round trips must agree
on the pair of actual world positions and logical
port matching, not just on equality of world X,Z.
The `RailShape` stored at each cell remains
canonical/position-stable even if access comes from a
different path across the corner.

**Edge ambiguity needs a design decision:** a rail can
transition from the `+Y` face to a side face along a
world-level corner while staying grade-flat in both
local charts, or can require a true local-radial
rise. An edge transition should **not automatically
be serialized as `ASCENDING_*`** merely because the
physical XYZ direction changed; `ASCENDING_*`
means one additional LOCAL-UP grade at one endpoint.
Only evaluate `RailShape` after mapping both
local ports and physical endpoints.

## 4. Rendered/physical rail geometry, signal graph and minecart motion

[`BaseRailBlock.getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L55)
returns a **2/16-high flat AABB** or
**8/16-high ascending AABB**, created in vanilla's
canonical coordinates, not a mesh matching every
quarter-rail segment. Planetary's
[`BlockStateShapeMixin`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/mixin/BlockStateShapeMixin.java)
with [`PlanetBlockShapeRuntime`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockShapeRuntime.java)
can rotate outer physical shape queries while
maintaining canonical support/occlusion. Its code
alone does not confirm the rail `VoxelShape` query
runs in a real `Level` rather than a wrapped
`BlockGetter`, or that the shape matches rendered
model geometry/ray collision on every face. Test
**visual mesh, selection/collision and hit target
separately**. Phase 3 owns rendering acceptance.

[`PoweredRailBlock.findPoweredRailSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L39)
handles **at most eight** connected segments with
manual Cartesian X/Y/Z increments and descending
fallback; [`isSameRailWithPower`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L145)
checks compatible shape and the **same concrete
block instance**, distinguishing powered and
activator rails despite shared Java class.
[`PoweredRailBlock.updateState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L185)
writes `POWERED`, notifies physical support below
and above if ascending. These are
**Phase 7A state graph/signal causal duties**:
a local-axis adaptation must preserve the
eight-segment bound, compatible connection
semantics and notification order; physical
notifications cannot be globally rotated for
every Minecraft block.

[`DetectorRailBlock.checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L99)
detects minecart occupancy via an XYZ physical AABB,
writes `POWERED`, sends callbacks to
[`RailState.getConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L141)
and physical `pos.below()` and schedules a
20-tick revisit. Its
[`getDirectSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L84)
gates on vanilla `Direction.UP`; input argument
is an API query-side, not necessarily the world
displacement to the signal receiver. Signal side
convention and `getAnalogOutputSignal` minecart
payload belong in **Phase 7A**, entity intersection
and detector search AABB need physical-orientation
tests, not only a face rotation.

**Minecart movement is an independently owned
consumer**: [`AbstractMinecart.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/vehicle/AbstractMinecart.java#L312)
samples a rail at `BlockPos(i,j-1,k)` (world-Y
bias), while
[`moveAlongTrack`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/vehicle/AbstractMinecart.java#L440)
computes flat horizontal X/Z exits,
`Vec3` velocities and special world-Y slope
adjustment.
Correct BlockState `RailShape` around a corner
does not itself prove the minecart follows it.
Likely later Minecart-specific local physics
and cart track selection are necessary,
but scope is not enough to assert exactly which
NeoForge 21.1.215 patches exist. Cart-physics
implementation must be explicitly assigned its
own phase/acceptance gate, not silently included
in a block-based P28 completion claim.

For **Phase 8**, [`MineshaftPieces`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/structure/structures/MineshaftPieces.java#L451)
directly authors `Blocks.RAIL` with preset
`RailShape` bypassing `BlockItem`; test generated
structures and structure templates as separate
writers. Phase 5 owns waterlogging fluid behavior.

## 5. Runnable future acceptance matrix — specified, NOT run

A **vanilla control world** must be compared against
the Planet world's local `+Y` face before evaluating
side-face behavior, so ordinary tracks do not
regress. Minimum proposed fixtures:

| Fixture | Block placement/graph check | Runtime and later-phase checks |
|---|---|---|
| All **six** faces: flat rail N/S and E/W | 2 connected endpoints, no unknown / wrong-world rail graph cells | Rail render + rail shape / ray + block support |
| All six: each of **4** `ASCENDING_*` | Both low/high endpoints, local DOWN support and extra grade-side support | AABB rotation, waterlogging and neighbor callback |
| Every ordinary rail quarter turn (`NE/NW/SE/SW`) | 2 endpoints and correct `RailShape` | No illegal curved rail values on Detector/Powered |
| 3-neighbor ordinary junction + signal on/off | `RailState.place/connectTo` priority and multi-cell mutations | 7A redstone turn routing, tick order, no oscillation |
| Two adjacent cube faces via edge | Both physical positions reciprocal, transported port, consistent canonical charts | Rendered track continuity, signal and cart continuity |
| Shared cube corner / exact frame tie | A single canonical BlockState orientation independent of traversal path | No duplicated rail or false XZ projection; cart path |
| Rise immediately before, during and after edge | Exact target positions for local tangent and local UP; no wrong diagonal | Cart slope physics/ray and support |
| Powered vs activator rail separate chains | Max 8 linked type-compatible blocks, no subtype cross-leak | 7A POWER and cart boost vs activation |
| Detector rail minecart entry/exit on all faces | Detector writes `POWERED`, connected rails notified, 20-tick recheck | Entity hit AABB, analog output and Phase 7A signal side |
| Waterlogged rails and removed slope support | True local floor and higher tangent support, water tick scheduled | Phase 5 fluid direction and fall/drop behavior |
| Mineshaft / structure-template rails | Final `RailShape` and physical adjacency valid after direct worldgen `setBlock` | Phase 8 orientation, worldgen/structure updates |

**For every fixture:** collect before/after
`BlockState` at source, actual endpoint world
positions, registered block identity,
`RailShape`, waterlogged/powered flags,
callback direction/sourcePos/targetPos and
exact adjacent rail states. For power/cart tests
log scheduled tick time/order, observed
`getSignal` query side, minecart position/
velocity/rail discovery and compare with normal
vanilla only when vanilla geometry equivalent.

**Hard pass criteria not yet satisfied:**
(1) original NeoForge 21.1.215 patched ASM
method/INVOKE reachability proven, (2) applicable
Planetary Mixin targets and handler count valid,
(3) zero/controlled side effects in vanilla
non-Planet dimensions, (4) physical support,
shape and multi-cell topology tests on six faces
plus edges/corners, (5) end-to-end client/server
minecart and detector/power/water tests.
A detailed test recipe is **NOT** a PASS.

## 6. Checkpoint / next first incomplete task

- Same **3 actual classes / 4 registered block IDs**
  `SOURCE_REVIEWED_INTEGRATION_PENDING`:
  `RailBlock` (1),
  `DetectorRailBlock` (1),
  `PoweredRailBlock` (2).
  `DaylightDetectorBlock` remains separate
  `REVIEW_PENDING`.
- Cumulative **66/241** source+NeoForge
  reflection-reviewed (174/1060 registered IDs),
  **175/241 REVIEW_PENDING** (886 IDs).
  All 241 `neoforge_patch_bytecode_review`,
  `planet_adapter_acceptance` and
  `gameplay_acceptance` are `REVIEW_PENDING`.
- **No Java files changed. No build, JVM patch-body
  test, server, client or gameplay performed.**
- **NEXT first unchecked stage task:
  3A-5.4**, reconcile original CI ZIP
  class/ID+five method declaring owners and
  all acceptance-status fields; queue a
  new independent still-unreviewed owner family.
  Only one bounded commit, then stop.
