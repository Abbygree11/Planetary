# Stage 3A-7.3 — calibrated sculk six-face FACING, physical vibration and signal acceptance contract

**2026-10-10** · Planetary branch `2.0` · Minecraft
**1.21.1**, NeoForge **21.1.215**, Java **21**.

**One research/test-contract microtask, not an implementation.**
This document reads the real Planetary frame/neighbor/
physical shape source at GitHub HEAD
`010fb15f0255453aa9f498ed79e25e4c2744f0fc` and pinned comparative vanilla
Minecraft 1.21.1 Java revision `b77c5c6995874f6cf2755bc5234428906b337b75`.
The 2 registered classes/2 BLOCK IDs were
already source+original compiled-declaration
reviewed in
[3A-7.1](PHASE2_STAGE3A_SCULK_VIBRATION_SOURCE_OWNER_AUDIT_1_21_1.md);
both exact ITEM creators were reviewed in
[3A-7.2](PHASE2_STAGE3A_SCULK_ITEM_ALTERNATE_AUTHORS_1_21_1.md).
No patched NeoForge ASM/mixin call-site,
client/server test, Java patch or gameplay
acceptance was performed by 3A-7.3.

## 1. Six independent domains; use the correct coordinate for each

| Domain | Required meaning | Source/proposed adapter boundary |
|---|---|---|
| **Canonical BlockState source direction** | Calibrated `FACING` remains one of vanilla local NORTH/EAST/SOUTH/WEST | Per-physical-block `PlanetBlockStateFrame.resolve`, stable even if reached via two traversal paths at a seam |
| **Physical neighbor coordinate** | Exact ordinary Minecraft `BlockPos` containing the redstone input/receiving block | Source-local `FACING.getOpposite()` → `PlanetBlockNeighborQuery`; validate `pos.relative(physicalDirection)==targetPos` |
| **Target's canonical query side** | Which physical face of the *neighbor's* BlockState corresponds to the query | Resolve target frame and `targetLocalSideTowardSource`; actual `Level.getSignal` API direction semantics must be audited before using local or physical side in a runtime mixin |
| **Output `getSignal(...,Direction)`** | Direction is an API **queried side**, not an offset used to locate the neighbor | Calibrated block suppresses query equal to stored FACING; `getDirectSignal` inherited from base only responds to queried local UP |
| **Vibration world position** | Original physical `Vec3` source and BlockPositionSource target; Euclidean distance/ray occlusion and real 3×3 XZ chunks | `VibrationSystem.Listener`, `Ticker`, not transported block tangent direction at cube edges |
| **Shape, fluid, render and ticks** | Local half-block VoxelShape, WATERLOGGED fluid, BlockEntity travel ticker, scheduled block phase tick, client particles | Distinct Phase 3 / Phase 5 / Phase 7A ownership; do not replace with one generic direction rotation |

Actual project code:

- [`PlanetFace`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/topology/PlanetFace.java) defines **six distinct right-handed local bases**.
- [`PlanetBlockStateFrame`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockStateFrame.java) selects `selectCanonicalBlockFace` for each physical BlockPos and maps `localToWorld/worldToLocal`.
- [`PlanetBlockFrameContext`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockFrameContext.java) is a traversal chart, with `step` possibly switching frames at an edge; **do not store its path-dependent face in BlockState**.
- [`PlanetBlockNeighborQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockNeighborQuery.java) maps source-local direction to actual target `BlockPos`, world-physical direction and target's own local inward face. On an exact seam, `crossedTraversalBoundary` is not a license to allocate an alias block.
- [`PlanetBlockRuntime`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockRuntime.java) provides `stateFrameAt`, `neighbor`, `localSideToPhysical` and `physicalSideToLocal` only when an active Planet field exists; ordinary vanilla worlds must pass through unchanged.
- [`PlanetBlockShapeRuntime`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockShapeRuntime.java) rotates **outermost VoxelShape** only for a physical Level query. No evidence it rewrites arbitrary `GameEvent`, `Vec3`, raycasts, chunk indexing or BlockEntity ticker code.
- [`PlanetVoxelShapeRotation`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetVoxelShapeRotation.java) rotates local AABB components of a VoxelShape around block center, not absolute world vibration vectors.

**Primary rule:** a physical-world `BlockPos` has exactly one
canonical block orientation, independent of which side of the
cube was used to approach it. Keep Minecraft's
physical XYZ world storage authoritative; adapt
source/target *block-semantic* directions at
the appropriate boundary, not all directions globally.

## 2. Actual six-face local basis and calibrated back-port chart

Derived **from the actual committed `PlanetFace` enum**
(not copied from Minecraft's global XZ “horizontal”):

| Canonical face | Local UP → world | Local EAST → world | Local SOUTH → world | Local NORTH → world | Local WEST → world |
|---|---|---|---|---|---|
| `POS_Y` | +Y | +X | +Z | −Z | −X |
| `NEG_Y` | −Y | +X | −Z | +Z | −X |
| `POS_X` | +X | −Y | +Z | −Z | +Y |
| `NEG_X` | −X | +Y | +Z | −Z | −Y |
| `POS_Z` | +Z | +X | −Y | +Y | −X |
| `NEG_Z` | −Z | −X | −Y | +Y | +X |

The calibrated sensor's
[`getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L60)
sets `FACING` from vanilla placement
`getHorizontalDirection()`, and
[`rotate/mirror`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L79)
mutate that property. Planet compatibility
must preserve exactly four **source-local tangent
directions** for FACING on all six faces,
including cases where a local horizontal
tangent is **world UP or DOWN**. It must **not**
add `Direction.UP/DOWN` to this vanilla
`HORIZONTAL_FACING` StateDefinition.

**Example on `POS_X`:** FACING `EAST`
is physical **world −Y**; the signal
*input/back* at local `WEST` is physical
**world +Y**. The stored BlockState FACING
is still `EAST` (canonical local), not
world `DOWN`, because `Direction.DOWN`
is not a legal FACING value for the calibrated
block. At an exact face boundary/corner,
use `PlanetBlockNeighborQuery` to resolve
the **actual** physical input target,
then determine its proper receiver query-side
convention. Avoid reading
`pos.relative(FACING.getOpposite())`
with the literal local enum interpreted as
world coordinates.

## 3. Calibrated port input, weak output and strong output are DIFFERENT

Pinned comparative source:
[`CalibratedSculkSensorBlockEntity.VibrationUser.getBackSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L47)
does two things with the same vanilla
`direction=FACING.getOpposite()`:

```java
Direction direction = state.getValue(FACING).getOpposite();
return level.getSignal(pos.relative(direction), direction);
```

**Physical target addressing** and
**API getSignal query direction** are
two separate contracts in Planetary.
The physical input block is the source-local
FACING-opposite neighbor, using canonical
source frame and seam-aware physical step.
The `Level.getSignal` *caller* and
`BlockState.getSignal` implementations
must be reviewed in Phase 7A (including
NeoForge-patched paths) before deciding
whether the query direction should be
physical, the target's canonical inward/
outward port, or a mapped API contract.
**It would be unjustified to pass
`targetLocalSideTowardSource` as the
original query argument without checking
the actual caller convention.**

[`canReceiveVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/CalibratedSculkSensorBlockEntity.java#L41)
filters **event frequency**, but only when
back input is nonzero: input 0 accepts any
otherwise valid event, input N only accepts
frequency N. `VibrationSystem.getGameEventFrequency`
is an event-to-frequency map, not a world
face direction.

Independently
[`CalibratedSculkSensorBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L66)
suppresses weak output when the
queried direction **equals its stored FACING**,
and otherwise delegates to
[`SculkSensorBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L217)
returning POWER. The inherited
[`getDirectSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L223)
returns strong output only if its *queried*
side equals local UP, and dynamically calls
`state.getSignal`, which means the
calibrated weak signal filtering still
matters. The **real output/receiver**
positions and strong-power propagation
belong to Phase 7A.

Also separate
[`getAnalogOutputSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L325)
which returns `BlockEntity.lastVibrationFrequency`
only while ACTIVE; this is **not** a simple
POWER copy. A valid test must read both
`POWER` and the comparator output,
including save/reload.

## 4. Vibration uses physical world coordinates, not local gravity

[`VibrationSystem.Listener.handleGameEvent`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L225)
receives a **world `Vec3`** from the
server event dispatcher; obtains the BE
`BlockPositionSource`, validates event
tags, source entity and sensor state,
then checks occlusion and schedules
a candidate. There is no requirement
in this source to travel via a chain
of local horizontal BlockState neighbors.

[`scheduleVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L276)
records source-to-target
`Vec3.distanceTo` and time.
[`isOccluded`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L289) uses
**six world-physical** micro-offsets and
a `ClipBlockStateContext` ray with
`BlockTags.OCCLUDES_VIBRATION_SIGNALS`.
Those directions are world ray coordinates,
not `FACING` ports and should not be
remapped through a cube face.

[`Ticker.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L315)
selects the event, progresses travel time
and attempts delivery to the BE listener.
[`receiveVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L399)
checks source/target physical BlockPos and
real chunk availability before calling
the receiver's `onReceiveVibration`.
The base VibrationUser
[`requiresAdjacentChunksToBeTicking`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkSensorBlockEntity.java#L162)
returns true, so
[`areAdjacentChunksTicking`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L421)
checks a **3×3 area of real XZ chunks**
around the BE, using vanilla physical
chunk coordinates. This is **not** a
six-face “nearest 3×3 local gravity
chunk” check, and should not be globally
rotated to cube-face coordinates.

**Seam semantics:** sensor and event emitter
can lie on opposite sides of an edge/corner
yet keep their authoritative real world
`Vec3` positions, Euclidean distance,
occlusion and vanilla acoustic radius.
The source does not guarantee a vibration
will bend around the planet surface,
nor should implementation silently invent
surface geodesic distance or bend rays at
the edge. A different physical acoustics
policy would be an explicit new feature
and requires independent design plus tests.

The base user radius **8** vs calibrated
radius **16** is applied to real event
physical distances. If physically near
but separated by solid tagged occluders
at a cube edge, the ray/occlusion rule
still applies. If far apart in Euclidean
world coordinates but topologically
adjacent by cube-face “wrap”, vanilla source
does not justify delivery. No positional
aliasing permitted.

`stepOn` in
[`SculkSensorBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L114)
can call
`forceScheduleVibration(GameEvent.STEP, entity.position())`
even if a conventional world GameEvent
did not pass through the same listener
entrypoint. Future tests must compare
both paths and avoid counting one event
twice. This also requires verifying
whether physical entity stepOn dispatch
reaches side/bottom-face sculk shapes,
not merely that generic gravity works
for players.

## 5. Neighbor power callbacks, resonance and scheduled state machine

[`SculkSensorBlock.updateNeighbours`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L170)
notifies at `pos` and **world `pos.below()`**.
If the signal is intended to reach a
sensor's local support-side cell on the
`POS_X/NEG_X/POS_Z/NEG_Z/NEG_Y` gravity
faces, a physical correct target must
be resolved independently. Do not confuse
this world-below **notification target**
with a signal API `Direction.UP` query,
or indiscriminately reroute notifications
that are supposed to remain world-physical
by an actual caller contract. Neighbor
order/callback count should be compared
to vanilla.

[`tryResonateVibration`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L273)
checks **all six physically adjacent**
cells with `pos.relative(Direction.values())`
for `BlockTags.VIBRATION_RESONATORS` and
emits physical GameEvents from those cells.
This is **deliberately a six-direction
physical neighbor scan**: reinterpreting
the six directions through an arbitrary
local face is unnecessary for the **set of
physical adjacent cells**, but callback
orientation and ownership still need audit
when another block relies on local side.
Don't convert it to an XZ-only tangent
ring or duplicate cells at edges.

The authoritative state transitions are:
`INACTIVE → ACTIVE(POWER>0) →
COOLDOWN(POWER=0) → INACTIVE`.
[`activate`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L251)
writes the BlockState and schedules a
block tick; [`deactivate`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L238)
writes COOLDOWN, clears power and
schedules 10 more ticks. Base active
duration **30 ticks**, calibrated
[`getActiveTicks`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CalibratedSculkSensorBlock.java#L91)
**10 ticks**, both 10 cooldown.
These are **scheduled BlockState ticks
relative to activation**, not random ticks.
The BE `getTicker` is a separate
**server-side per-tick** vibration
delivery path; do not replace one with
the other or assume a single 20-tick
BE heartbeat (that was the *daylight*
sensor, not a sculk sensor).

**State-only placement/restore caveat:**
`BlockItem` can optionally write
`DataComponents.BLOCK_STATE`; the
`SculkSensorBlock.onPlace` source
clears POWER on a new sensor without an
existing scheduled tick. BE vibration
persistence and unusual restored PHASE/
POWER combinations need actual game tests,
not source-only acceptance.

## 6. Local half-block shape, waterlogging, GameEvent audio, visual effects

[`SculkSensorBlock.SHAPE`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L52)
is block-local 0..16 X/Z, 0..8 Y,
a **half-height canonical-local VoxelShape**.
For the six faces, the physical half-block
extends along the chosen local UP normal;
existing `PlanetBlockShapeRuntime.finishPhysical`
can rotate the **outermost** physical
VoxelShape under its Level-query conditions,
but doesn't prove interaction/collision,
stepOn dispatch or actual model/particles.
The `animateTick`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L288))
uses global `pos.y+0.25` and world XZ
random offsets: separate Phase 3/local
particle acceptance is needed, even if
generic block-breaking particles were
previously accepted.

[`getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L82)
reads physical target block fluid for
`WATERLOGGED`;
[`getFluidState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L90)
returns water source if true;
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L159)
schedules a **fluid tick** when waterlogged.
[`activate`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSensorBlock.java#L251)
and scheduled `tick` suppress some
clicking sound playback if waterlogged,
but the source still runs vibration/
POWER and scheduled block transitions.
**Do not equate waterlogged with
nonfunctional sensor**, and do not assume
the fluid tick is the vibration tick.
Actual water flow/gravity is Phase 5.

## 7. Future runnable acceptance fixtures — NOT executed

Start with vanilla **non-Planet** world
control and ordinary `POS_Y` Planet
surface control. Then test the same
physical source event, sensor state
and receiver across all six canonical
gravity faces (plus every reachable
edge/corner transition). These are **test
specifications, not results**.

| ID | Setup / exact action | Required assertion and owner |
|---|---|---|
| **K1** | Vanilla control world; place both sensors with ordinary BlockItem | Same initial INACTIVE/POWER=0 and placement water state; registered IDs and BlockEntity type exact |
| **K2** | Six face interiors × both types | Local half-block VoxelShape rotated once; model stands outward from local supporting surface; no ghost/double placement |
| **K3** | All 6 faces × each 4 legal calibrated local FACING values | FACING stores NORTH/EAST/SOUTH/WEST **local** only; physical tangent axis agrees with actual PlanetFace basis, including world-vertical tangents |
| **K4** | POS_X example with calibrated FACING=EAST and rear input | Actual back cell physically +Y, not global WEST; receiver queried side resolved using actual Phase 7A Level.getSignal convention |
| **K5** | Six faces × calibrated zero redstone on back input | Valid events of different frequencies activate when otherwise eligible; no accidental output-side suppression applied to input port |
| **K6** | Six faces × calibrated back input N 1..15 | Event frequency N accepted; mismatching frequency rejected; compare 0 input and mismatching input, verify neighbor source and query direction separately |
| **K7** | Calibrated queried weak output FACING vs other sides and direct strong output local UP | FACING side returns 0, other queried tangents POWER, strong queried local UP only (with delegated weak-source result); vanilla consumer parity |
| **K8** | Both sensors at same world-space event distances near radius 8 and 16 boundaries | Actual Euclidean receiver distance and consistent activation vs nonactivation; no chart aliasing or radius expansion over edge |
| **K9** | Event emitter on POS_Y, sensor on POS_X across their edge | Same actual physical XYZ event/BE positions, no path-dependent rotation; listener gets exactly one event if vanilla radius/occlusion permits |
| **K10** | Two distinct traversal routes to exact 3-face cube corner sensor BlockPos | Identical canonical BlockState FACING, BE identity, vibration target coordinate, event outcome; no duplicate listener/BlockPos |
| **K11** | Real physical occluder and unobstructed control on same/different gravity faces | `BlockTags.OCCLUDES_VIBRATION_SIGNALS` world ray behavior matches vanilla; no gravity-local ray folding |
| **K12** | On a world X/Z physical chunk border, unload one required adjacent chunk | Base `requiresAdjacentChunksToBeTicking` gate blocks/delays delivery as source; resumes without duplicate event when chunks tick |
| **K13** | Same world event vs entity directly `stepOn` both types, all faces incl −Y | Compare listener/forced-vibration paths; actually verify `stepOn` dispatch for side/downward physical shapes, avoid duplicate candidate delivery |
| **K14** | Activate plain vs calibrated with identical valid event | PHASE ACTIVE lasts 30 vs 10 scheduled BLOCK ticks, then COOLDOWN 10 ticks and INACTIVE; no randomTick substitution or wrong phase/POWER |
| **K15** | Query comparator output during ACTIVE and afterward; save/load BE with pending vibration | BE last-event frequency only while ACTIVE; POWER is distance strength; serialized listener/frequency consistent; no invalid extra delivery |
| **K16** | Place sensors underwater at six faces, remove/reapply water; check particles/audio | WATERLOGGED carries water source and schedules FLUID tick; listener/POWER still work; clicking audio rules and local particles evaluated separately |
| **K17** | Active sensor with resonator in each of 6 physical adjacent cells, including an edge/corner; same in vanilla control | Exactly matching physical resonator GameEvent emission/frequency, no duplicate seam alias cells, unaffected neighbors untouched |
| **K18** | Direct `StructureTemplate` with known fixture and optional ITEM BlockState/BE component, vanilla/Planet | No BlockItem-only assumption; property values, BE data, scheduled phase coherence, legal FACING and fluids; no claim vanilla ships this structure |
| **K19** | Non-Planet Level / mock BlockGetter, client-only tick and server tick | No gravity transformation outside field; vibration BE ticker server only; VoxelShape wrapper Level conditions honored, no global API changes |

**Trace all of:** exact BLOCK ID, physical
`BlockPos`, `PlanetFace` canonical
state frame and any different traversal face,
local FACING and physical tangent vector,
calibrated back-port target physical BlockPos
and target's canonical inward side,
`Level.getSignal` query argument and returned
value, `getSignal/getDirectSignal` queried
directions, `GameEvent` identity/frequency,
source and target physical `Vec3`,
listener radius, candidate selection
and travel ticks, occlusion ray blocked?,
actual chunk (x,z) loaded/ticking set,
`BlockEntityType`/lastVibrationFrequency/
saved listener data, scheduled block tick,
fluid tick, before/after `PHASE/POWER/
WATERLOGGED`, neighbor callbacks, particle
spawn and `stepOn` invocation.
At a corner, log two traversal approaches
leading to the same canonical block and
compare one BE/one state/one event.

**PASS gates not met:** actual compiled NeoForge
21.1.215 ASM patched `Level.getSignal`
and event dispatcher call sites, actual
Planetary Mixin injection reachability,
physical input/output port behavior,
BE listener across all face edges, chunk
tick gating, client models/particles, fluid,
all six-face/seam/corner/vanilla controls.
No test row above was run.

## 8. Durable status / next microtask

Ledger remains **71/241** source+compiled
declaration reviewed classes
(**192/1060 exact registered BLOCK IDs**),
**170/241** source REVIEW_PENDING
(**868/1060 IDs**). Both sensor classes
(`minecraft:sculk_sensor` and
`minecraft:calibrated_sculk_sensor`)
retain `SOURCE_REVIEWED_INTEGRATION_PENDING`;
no new review promotion. `SculkShriekerBlock`,
`SculkCatalystBlock`, `LightningRodBlock`
still source REVIEW_PENDING.
**All 241** real patched NeoForge ASM,
Planet adapter and gameplay acceptance
fields remain `REVIEW_PENDING`.
No source implementation, compilation,
client/server run or player gameplay tests.

**NEXT FIRST open stage 3A-7.4**, task 4 of
[`02e-sculk-vibration-sensor-owners.md`](../phases/phase-02/02e-sculk-vibration-sensor-owners.md):
independently re-open untouched original
NeoForge 21.1.215 CI ZIP and reconcile
entire class roster, all **71** source-reviewed
classes with **192** exact BLOCK IDs and five
original method declaration owners, preserve
170 pending class rows and all 241 gameplay
gates, create next small family card,
commit ONE research package, then stop.
