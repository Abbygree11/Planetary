# Phase 2 research, wave B: support, growth and connection-graph override owners

Target: Minecraft 1.21.1 comparative source,
NeoForge 21.1.215 bytecode census pending.
**Status: source-owner/override samples inspected; NO runtime changes;
full actual compiled registry ownership still REVIEW_PENDING.**
Depends on wave A `PHASE2_SOURCE_OWNER_AUDIT_WAVE_A_1_21_1.md`
and master P01–P40 atlas.

This bounded stage checks whether the presence of a base-class Mixin
actually proves coverage of its descendants. It does NOT: multiple
vanilla descendants override the lifecycle method entirely.

## 1. Subclass support bypasses (P22/P23/P24/P30/P33)

| Actual vanilla source owner | Relevant source method(s) | Shared semantic boundary / bypass consequence |
|---|---|---|
| `BushBlock` | `canSurvive` reads `pos.below()` then virtual `mayPlaceOn` | Current base Mixin translates local DOWN into physical supporting cell, *only if the descendant inherits this canSurvive implementation* |
| `SaplingBlock` | `randomTick` reads lighting at `pos.above()` and calls `advanceTree` | Planting on local grass may work after base fix, but tree growth/light/topology does NOT follow automatically |
| `CropBlock` | overrides `canSurvive` for light, then calls `super.canSurvive`; `mayPlaceOn` requires farmland | Base-local soil lookup may still participate, but growth/light checks are separate; preserve NeoForge plant growth hooks |
| `SeaPickleBlock` | **overrides `canSurvive`** with its own `pos.below()`; `mayPlaceOn` checks face shape/sturdiness; placement increments existing pickles | BushBlock Mixin will NOT run in its overridden canSurvive; cannot use a universal `canSupportCenter` predicate without changing rules; waterlogged path Phase 5 |
| `CocoaBlock` | **overrides `canSurvive`** to read `pos.relative(state.FACING)`; `getStateForPlacement` enumerates look directions; `getShape` chooses a facing-indexed AABB | Source state-local FACING must be mapped to PHYSICAL neighbor before checking jungle-log tag. Plus placement orientation and shape/render ownership |
| `LanternBlock` | own `getStateForPlacement` chooses `HANGING` with nearest directions; `canSurvive` queries `getConnectedDirection(state).opposite()`; `updateShape` handles water ticks | Hanging ceiling/floor support plus property authoring are one algorithm. Preserve original predicate and water-tick timing |
| `AmethystClusterBlock` | independent six-direction `FACING`, `canSurvive` uses relative(opposite FACING) and `isFaceSturdy`; `updateShape` compares callback direction against FACING | Neither BushBlock nor candle support solves this. Use target canonical FACING -> exact PHYSICAL supporting cell/neighborPos; preserve water ticks |
| `CactusBlock` | randomTick/growth uses `pos.above()`, scans `below(i)`, scheduled tick checks canSurvive | Initial placement and later growth are distinct graph mechanisms; physical horizontal hazards must be checked in relevant source frame |
| `SugarCaneBlock` | randomTick scans physical `above()/below(i)`, neighbor-triggered survival scheduled ticks | Column length/growth needs local-UP/-DOWN graph and environmental water adjacency, not only Bush inheritance |

**Specific completeness failure that initial tests missed:**
a passing basic `BushBlock.canSurvive` unit test does NOT exercise the
overridden SeaPickleBlock implementation, the CocoaBlock
state-directed support nor all crop/light/growth checks.
This is a confirmed source-level reason to test descendants and
method implementation owner separately.

## 2. Multi-cell plant graph (P20/P29/P30)

`GrowingPlantBlock` uses the immutable `growthDirection`
for target lookups and for its support in the opposite direction.
It has an independent overload `getStateForPlacement(LevelAccessor)`.
`GrowingPlantHeadBlock` changes head -> body as the neighbor grows;
`GrowingPlantBodyBlock` can replace body -> head on removal.
Their `updateShape` methods compare **physical callback Direction**
against semantic growthDirection, schedule ticks, and handle fluid
tick scheduling. `randomTick` on the head creates the next physical
cell in the direction of growth.

A local chart transform needs to cover **all** of:
- source-local semantic growthDirection -> physical target cell;
- neighboring head/body BlockState owning canonical chart;
- physical neighbor notifications and same-state continuation;
- growing/projection into a different gravity face at an edge;
- bonemeal/environment/fluids preserving vanilla tick order.

This is NOT a surface `BushBlock` canSurvive fix. It is P29's
own graph algorithm, with head/body/corner tests.

## 3. Scaffolding and multi-face spatial graphs (P25–P28/P31)

### ScaffoldingBlock and ScaffoldingBlockItem

`ScaffoldingBlockItem.updatePlacementContext` searches physical
adjacent cells and chooses a different final BlockPos for the item.
`ScaffoldingBlock` separately derives `DISTANCE`, `BOTTOM`,
neighbor updates, timed collapse and local vertical support.
The two need one coordinated graph contract: rotating only
`HORIZONTAL_FACING` or editing scaffolding block shape cannot
fix target selection, support distance and falling together.

### MultifaceBlock versus VineBlock

`MultifaceBlock` owns up to six Boolean physical attachment face
properties and performs `canSurvive` over each occupied state
face, updating/deleting individual faces on neighbor removal.
`VineBlock` has a different face graph and environmental spreading
rules: `getUpdatedState`, `randomTick`, special DOWN propagation,
and per-direction attachment. They may share a stable
`resolveLocalAttachmentFace` helper but must NOT be assumed
to share identical state mutation or growth logic.

Crucial decision: a physical face normal in a raw callback is not
the local `NORTH/SOUTH/EAST/WEST/UP/DOWN` Boolean key stored in
a canonical BlockState. For cross-edge support, resolve the actual
neighbor physical cell first, then convert the side into the
target neighbor's chart. Avoid global Direction rewrites.

### BaseRailBlock / RailState

`BaseRailBlock.canSurvive` requires `canSupportRigidBlock` at
the actual local-DOWN support (not ordinary `isSolid` or
`canSupportCenter`). Its `getStateForPlacement` creates
`RailShape` from player horizontal direction and water state.
`RailState` forms a separate adjoining track graph and elevated
paths; rail shape symbols are not equivalent to world XYZ.
Increasing track slope toward local UP may mean physically sideways
world movement.

The rail system therefore needs a distinct P28/Phase-2F
graph engine and Phase-7 vehicle integration; DO NOT fold it into
the base support mixin family.

## 4. Shared contracts and anti-patterns

| Semantic operation | Correct owner/frame | Forbidden shortcut |
|---|---|---|
| Support of a local-down floor block | current canonical state DOWN -> PHYSICAL supporting BlockPos | global `pos.below()` |
| Ceiling support | current state local UP -> physical face and pos | global `pos.above()` |
| Wall/state-directed support | stored BlockState FACING -> physical supporting neighbor + neighbor-side face | raw `pos.relative(state.FACING)` on rotated face |
| Check solidity | original predicate (`isSolid` / `canSupportCenter` / `canSupportRigidBlock` / `isFaceSturdy`) | replace all with one convenient predicate |
| Support invalidation | compare callback neighborPos to exact computed physical supportPos | `physicalDirection == localFACING` |
| Extend a head/body chain | source traversal/chart-aware relative cell + target BlockState chart | direct world-Y `above()`, or rotate only head |
| Tangent connection | local EAST/SOUTH/NORTH/WEST transported to physical target and back | raw horizontal world-axis bit flags |
| Graph across corner | explicit traversal order/policy and handedness | assume X+Z steps commute at three-face vertex |
| Waterlogged blocks | preserve original water tick/state scheduling | cancel entire `updateShape` hook without reproducing fluid behavior |

## 5. Current Planetary implementation cross-check

The branch contains `BushBlockLocalSupportMixin`,
`CakeFamilyLocalSupportMixin`, `CandleBlockLocalSupportMixin`,
`SporeBlossomLocalSupportMixin`, `BaseTorchBlockSupportMixin`,
`FenceBlockGravityMixin`, `PointedDripstoneBlockGravityMixin`,
`LadderBlockSupportMixin`, `FaceAttachedHorizontalDirectionalBlockSupportMixin`.
This is **partial implementation presence**, NOT proof that every
overridden descendant mechanism is handled.

Concrete source-verified misses to classify explicitly in stage 3:
`SeaPickleBlock.canSurvive`, `CocoaBlock.canSurvive`,
`LanternBlock` hanging placement+support,
`AmethystClusterBlock` directed support+update,
`GrowingPlantBlock/Head/Body` graph,
`CactusBlock/SugarCaneBlock` growth and environmental checks,
`MultifaceBlock/VineBlock` six/tangent graph,
`ScaffoldingBlockItem/ScaffoldingBlock` placement+support,
`BaseRailBlock/RailState` support+sloped connections.
Keep actual 21.1.215 registry/ASM applicability pending until
the executable census CI returns.

## 6. Next handoff

The wave-A registry diagnostic will enumerate registered blocks
and runtime declared owner chains. Stage 3 maps all owner rows to
P01–P40 plus physical/state/traversal policy, including inherited
and overridden paths, and adds a fail-unknown-owner CI gate once
reviewed. Then implementation goes by independent complete
mechanism packages. No new named-block Mixins in this research
wave, no user Minecraft test requested, and no Phase-2 PASS claim.
