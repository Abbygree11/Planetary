# Stage 3A-5.1 — original NeoForge rail class owners and complete rail-graph source pathways

**2026-10-10** · Planetary branch `2.0`, target
Minecraft **1.21.1** / NeoForge **21.1.215** / Java 21.

**Scope:** one first-incomplete bounded source-owner research task,
**3** original `REVIEW_PENDING` concrete rail classes,
**4** registered BLOCK IDs. Exact class declaration
owners verified by original compiled NeoForge runtime
registry artifact; semantics read in pinned comparative
Minecraft 1.21.1 Java. **No implementation / patched ASM /
compiled Mixin weave / CI build / gameplay PASS.**

## 1. Original executable registry evidence, distinct from source semantics

Original ZIP: [GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055)
artifact **11643813158**, original source revision
`aa39572950a15403ea0a9003eefccf3bf6675ff7`,
unmodified ZIP SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Primary `phase2-neo1211-block-registry.tsv` contains
**1060 registered BLOCK ID rows / 241 concrete Java classes**.
Original `class_hierarchy`, `effective_method_owners`,
`declared_property_names` and exact `registry_id`
verified for the THREE classes with complete five-method
signatures (no bare-name matching).

| Concrete class / hierarchy | Exact actual block registry ID(s) | State properties | Five compiled nearest declaring owners |
|---|---|---|---|
| `RailBlock > BaseRailBlock > Block > BlockBehaviour` | `minecraft:rail` (1) | `shape,waterlogged` | placement **BaseRailBlock**; survive **BaseRailBlock**; updateShape **BaseRailBlock**; randomTick **BlockBehaviour**; setPlacedBy **Block** |
| `DetectorRailBlock > BaseRailBlock > Block > BlockBehaviour` | `minecraft:detector_rail` (1) | `powered,shape,waterlogged` | placement **BaseRailBlock**; survive **BaseRailBlock**; updateShape **BaseRailBlock**; randomTick **BlockBehaviour**; setPlacedBy **Block** |
| `PoweredRailBlock > BaseRailBlock > Block > BlockBehaviour` | `minecraft:activator_rail`, `minecraft:powered_rail` (2) | `powered,shape,waterlogged` | placement **BaseRailBlock**; survive **BaseRailBlock**; updateShape **BaseRailBlock**; randomTick **BlockBehaviour**; setPlacedBy **Block** |
| **Total** | **4 unique BLOCK IDs** | | **3 reviewed concrete classes** |

Exact five method signatures:
`getStateForPlacement(BlockPlaceContext)`,
`canSurvive(BlockState,LevelReader,BlockPos)`,
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`,
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`,
`setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`.

**Additional runtime declaration owners**, also recorded by
original compiled reflection (not guessed from class names):

| Behavior | RailBlock | DetectorRailBlock | PoweredRailBlock |
|---|---|---|---|
| `neighborChanged` | BaseRailBlock | BaseRailBlock | BaseRailBlock |
| `onPlace` | BaseRailBlock | **DetectorRailBlock** | BaseRailBlock |
| `rotate/mirror` | RailBlock | DetectorRailBlock | PoweredRailBlock |
| `getShape`, `getFluidState` | BaseRailBlock | BaseRailBlock | BaseRailBlock |
| `tick(ServerLevel,...)` (scheduled, **not** randomTick) | BlockBehaviour | **DetectorRailBlock** | BlockBehaviour |
| `useWithoutItem` | BlockBehaviour | BlockBehaviour | BlockBehaviour |

`BaseRailBlock` is an **abstract, nonregistered
declaring owner**. `RailState` is a **nonregistered
graph state/connection algorithm**. A registry-class-only
patch would miss most of the actual behavior.

## 2. Pinned comparative source evidence

Source revision [`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1)
was read for `BaseRailBlock`, `RailBlock`,
`DetectorRailBlock`, `PoweredRailBlock`,
`RailState`, `RailShape`. These are comparative
Minecraft 1.21.1 Java methods, not confirmation that
NeoForge has identical patched `INVOKE` instructions.

### 2.1 BaseRailBlock: placement, support, update, fluids

[`BaseRailBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L173)
starts a `RailShape.NORTH_SOUTH` or `EAST_WEST` from
the player's **horizontal world direction**, and sets
`WATERLOGGED` using the physical clicked cell's fluid.
Its constructor `isStraight` differentiates the
shape-value vocabulary:
[`RailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailBlock.java#L22) has `isStraight=false`,
whereas [`DetectorRailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L40)
and [`PoweredRailBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L22)
have `true`, and `RAIL_SHAPE_STRAIGHT` does not
allow corner values.

[`BaseRailBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L62)
uses `canSupportRigidBlock(level,pos.below())`.
[`neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L89)
can remove the track when support is absent.
[`shouldBeRemoved`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L107)
checks *both* bottom support and **additional adjacent
support for an ascending rail** in the shape's
cardinal tangent direction. A bottom-only hook
does not preserve slope survival on cube faces.

[`BaseRailBlock.onPlace`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L68)
calls its state `updateState` which invokes
[`updateDir`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L139)
→ **`new RailState(...).place(...).getState()`**.
The direct-item placement state is just an initial
straight guess; **later graph placement can replace
the shape for this and adjacent cells**.

[`BaseRailBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L186)
does **NOT** calculate the new track network; it
schedules a fluid tick for `WATERLOGGED` then
delegates. Shape topology is authored by the
`RailState` pathway in placement and neighbor
callbacks, not by just overriding `updateShape`.
[`getFluidState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L199)
returns water on waterlogged rail.

[`BaseRailBlock.getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseRailBlock.java#L55)
returns different voxel geometry for flat vs
ascending rails. Correct local `SHAPE` property
without world-physical shape rotation is insufficient
for proper interaction/collision (Phase 3).

### 2.2 RailState: critical owner of multi-rail writes

[`RailState.updateConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L37)
creates two **world BlockPos** endpoints from every
`RailShape`: N/S, E/W, 4 rising orientations,
and 4 quarter-turns if permitted. Each
`ASCENDING_*` uses a positional **`.above()`**
relative to a world-horizontal neighbor.

[`getRail`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L119) tries a rail at
a candidate cell, then **world above** and **world below**;
[`hasRail`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L111) checks those
three heights as well. The graph is NOT a single
flat 4-neighbor search.

[`hasConnection`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L150) compares
`BlockPos.X` and `BlockPos.Z`, intentionally
ignoring **world Y** for vanilla rail slope matching.
This is a **critical axis-dependent assumption**:
simply rotating shape states won't make this match a
gravity-local vertical axis on ±X/±Z/-Y faces.
At face seams, chart mappings and physical target
positions change independently.

[`countPotentialConnections`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L165)
visits the world HORIZONTAL plane;
[`canConnectTo`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L180)
allows a new connection if the existing ones
are compatible or fewer than two. This is
not a generic fence bit graph.

[`connectTo`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L185)
chooses the resulting `RailShape` based on
connection pattern, can choose an ascending
shape after checking the elevated cardinal
neighbor and **writes `this.level.setBlock(pos,state,3)`**.
[`place`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailState.java#L281)
selects straight or corners based on neighboring
rail graph and whether incoming redstone signal
prefers a particular turn; it also checks
neighbors at higher elevations to choose rising shapes,
writes the new state and iterates the current
endpoints to call neighbors'
`removeSoftConnections` and `connectTo`.
Thus `RailState` can author/overwrite states in
**several physical rail positions**, including a
neighbor's shape, *after* `BlockItem.placeBlock`
initial placement. Modding only `BaseRailBlock`
getStateForPlacement leaves this author untouched.

[`RailBlock.updateState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RailBlock.java#L32)
adds a special re-evaluation for rail intersections:
only when incoming block is a signal source and
`countPotentialConnections()==3`. In a
three-way rail junction, a changed redstone signal
can change the turn choice. Functional switch
causality is Phase 7A, but local `RailShape`
authorship is Phase 2.

### 2.3 DetectorRailBlock: entity detection and connected updates

[`DetectorRailBlock.entityInside`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L60)
and [scheduled `tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L72)
call [`checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L99):
check minecart entities, write `POWERED`,
notify neighbors (including world **below**) and
schedule **20-tick** recheck while occupied.
[`updatePowerToConnected`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L141)
constructs a `RailState` and notifies each
rail connection directly with `neighborChanged`.
Its [`onPlace`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L153)
**overrides BaseRailBlock** and calls both
`updateState` (which places/relinks rail graph)
and `checkPressed`. Skipping this override risks
missing the detector's initial signal.

[`getDirectSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DetectorRailBlock.java#L84)
is direction-gated to vanilla world UP, unlike
`getSignal` (power output 15 on any query direction).
An actual chart-aware runtime needs to distinguish
API signal query direction from property
orientation and physical notification position;
Phase 7A owns validated signal output semantics.

### 2.4 PoweredRailBlock: same class, two different registered blocks

[`PoweredRailBlock.updateState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L185)
compares `POWERED` with immediate
`Level.hasNeighborSignal` and a **bounded recursive
8-segment** scan in both along-rail directions,
writes a new `POWERED` state, notifies actual
world support below, and additionally above for
ascending rails.

[`findPoweredRailSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L39)
does explicit **X/Y/Z integer arithmetic** per
`RailShape`, and
[`isSameRailWithPower`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PoweredRailBlock.java#L145)
requires compatible orientations and `blockstate.is(this)`.
This last condition means the two registered block
instances (powered vs activator) do not become one
unrestricted propagation network by sharing
`PoweredRailBlock` Java class.
No generic continuous 4-way wire connection algorithm
can replace this depth/shape/type-sensitive walk.

`PoweredRailBlock` still inherits
`BaseRailBlock.onPlace` and
`BaseRailBlock.neighborChanged` for graph/survival
entrypoints, even though its *own* updateState
authors POWERED. Crucially, its
`tick` nearest declared owner is
`BlockBehaviour` (no own scheduled tick),
whereas detector's is its own class.

## 3. Exact responsibility table / open integration gates

| Owner | Source method(s) | Writes/reads | Concern and phase |
|---|---|---|---|
| `BlockItem` through `BaseRailBlock` | `getStateForPlacement` | initial straight `RailShape` + `WATERLOGGED` | P01/P03/P28/P37, source-local horizontal versus physical hit |
| `BaseRailBlock` | `canSurvive`, `shouldBeRemoved`, `neighborChanged` | support below AND one ascending-tangent support | P22/P24/P28; target local back-to-source sturdiness |
| `RailState` | `getRail`, `updateConnections`, `place`, `connectTo` | reads and writes **multiple physical cells**; world Y-only slopes | P28; requires per-cell frame and edge/corner traversal |
| `RailBlock` | `updateState`, rotate/mirror | turn at 3-neighbor powered junction | P28/P35 / Phase 7A switch update ordering |
| `DetectorRailBlock` | `entityInside`, `tick`, `checkPressed`, `onPlace` | writes POWERED, connected rail notification | P36, Phase 7A; minecart detection geometry separate |
| `PoweredRailBlock` | `updateState`, `findPoweredRailSignal` | up to 8-rail recursive POWERED circuit | P28+P36, Phase 7A; block instance type check |
| `BaseRailBlock` | `updateShape`, `getFluidState` | WATERLOGGED tick/water | P37 / Phase 5; not track connection author |
| `BaseRailBlock` | `getShape` | flat versus rising voxel support silhouette | Phase 3; physical geometry and collision/interaction |

**Planetary runtime architecture presently supplies**
position-stable `PlanetBlockStateFrame` for local
property meaning and `PlanetBlockFrameContext.walk`
for seam-aware physical step, plus
`PlanetBlockNeighborQuery` source-vs-target face
mapping. **None of these alone implements**
`RailState` neighbor graph or cart-trajectory physics.
Before changing it, validate exact call sites and
NeoForge patch-body transformations, preserve
normal vanilla behavior and avoid a blanket
`Direction` rewrite.

**Deliberately NOT claimed in this packet:**
complete `Minecart` movement dynamics, detector
precise entity-AABB acceptance, structural schematic
placement or worldgen item aliases, NeoForge ASM,
Mixin binding, gameplay or CI PASS.
Those require separate microtasks.

## 4. Bookkeeping, status, and next independent work

Prior to this work: **63** of **241** source+reflection
reviewed classes (**170/1060 registered BLOCK IDs**),
**178** class reviews pending (890 BLOCK IDs).
This task newly source+reflection reviews:
`RailBlock` (1), `DetectorRailBlock` (1),
`PoweredRailBlock` (2) = **3 classes / 4 IDs**.

After commit: **66/241** classes
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(**174/1060 BLOCK IDs**); **175/241**
`REVIEW_PENDING` (**886/1060 BLOCK IDs**).
`DaylightDetectorBlock` stays `REVIEW_PENDING`.
For ALL 241 original classes, every
`neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and
`gameplay_acceptance` remains `REVIEW_PENDING`.

**NEXT FIRST task: Stage `3A-5.2`** —
independently join original 1333-item NeoForge ITEM
registry for these 4 rail blocks via actual
`placed_block` (not guessed names), and analyze
minecart/detector/power network/structure
alternative authors with distinct
`PoweredRailBlock` instances. Single
separately committed research packet.


## 2026-10-10 Stage 3A-5.2 ITEM and additional rail authors

[Original 1333-ITEM registry 1:1 rail placed_block join and
non-item state author evidence](PHASE2_STAGE3A_RAIL_ITEM_ALTERNATE_AUTHORS_1_21_1.md):
all four rail registered block IDs correspond to
four separate, ordinary `BlockItem` registered
item records with same-named registry IDs;
all seven item placement signatures declare on
`BlockItem` (distinct from `Item.use`).
`RailState` writes several rail cells after
item placement and on neighbor updates;
DetectorRailBlock searches minecart AABB,
writes POWERED and notifies graph and below
with 20-tick check and separate analog comparator
query; PoweredRailBlock has 8-segment recursive
powered network and protects per-block-instance
rail subtype. AbstractMinecart reads
POWERED_RAIL for movement and ACTIVATOR_RAIL
for virtual activateMinecart hook; cart subclass
responses NOT yet accepted.
MineshaftPieces provably creates `Blocks.RAIL`
and writes via worldgen block placement, bypassing
BlockItem; StructureTemplate has a generic
BlockState write and neighbor-shape update path.
No new class disposition: 66/241 reviewed
(174 IDs), 175 pending (886 IDs); all patched
ASM/Planet/gameplay acceptance pending.
NEXT 3A-5.3 seam topology, movement cross-phase
and full test matrix.
