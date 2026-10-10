# Stage 3A-6.3 — exact canonical local support, contact AABB, SKY and signal-query chart

**2026-10-10** · Planetary `2.0`, Minecraft **1.21.1** /
NeoForge **21.1.215** / Java 21.

**This is one research / future acceptance contract, not a
new implementation.** Inputs were already audited in
[3A-6.1 exact 3-class/16-ID BLOCK owners](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_SOURCE_OWNER_AUDIT_1_21_1.md)
and [3A-6.2 16 exact ITEM creators and off-item writers](PHASE2_STAGE3A_ENVIRONMENT_PRESSURE_SENSOR_ITEM_ALTERNATE_AUTHORS_1_21_1.md).
This turn also read actual Planetary frame/support/
shape runtime source from branch `2a7edf841555f668e77ca9d46aab5a7c942fcf8c`,
and pinned comparative Minecraft 1.21.1 Java
at source commit `b77c5c6995874f6cf2755bc5234428906b337b75`.
**No NeoForge patched ASM/INVOKE evidence,
compiled Mixin binding, Java changes, CI tests,
client/server execution or gameplay PASS.**

## 1. Five coordinate / callback domains cannot share one Direction

| Domain | Source value | Required invariant |
|---|---|---|
| **Canonical source BlockState frame** | Plate source-local `DOWN`; detector's local geometry | Same physical `BlockPos` always has one canonical local orientation, independent of how a seam traversal arrived |
| **Physical world neighbor** | Actual ordinary XYZ one-step to support, or actual target of neighbor notification | `sourcePos.relative(physicalDirection)==targetPos` must hold; do not notify fixed global `pos.below()` on side face |
| **Canonical support/target frame** | Support's *own* local face facing the plate | Interpret physical back-direction through **support's canonical frame**, not assume opposite source-local `DOWN` across a seam |
| **Signal API query-side** | `getDirectSignal(...,Direction.UP)` | This is a **queried port argument**, not necessarily `sourcePos.relative(world UP)` or physical notification vector; determine caller contract before patch |
| **Environmental/entity sample** | `TOUCH_AABB.move(pos)` or `LightLayer.SKY` at physical `pos` | Physical query box and sky light algorithm are independent of BlockState FACING, visual shape, and simple gravity axis rotation |

The real project helpers reviewed:

- [`PlanetBlockStateFrame`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockStateFrame.java)
  uses `selectCanonicalBlockFace` for a stable
  `localToWorld/worldToLocal` map; canonical
  state frame is **not** the traversal frame.
- [`PlanetBlockFrameContext.step/walk`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockFrameContext.java)
  resolves seam-aware physical one-step and
  transported traversal direction.
- [`PlanetBlockNeighborQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockNeighborQuery.java)
  separately carries `sourceLocalDirection`,
  `physicalDirection`, `targetLocalSideTowardSource`
  and boundary-crossing.
- [`PlanetBlockSupportQuery`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockSupportQuery.java)
  resolves source-local support direction to
  physical support BlockPos and canonical
  target-local inbound face.
- [`PlanetBlockShapeRuntime`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetBlockShapeRuntime.java)
  rotates outermost **physical VoxelShape**
  queries when `BlockGetter instanceof Level`,
  and intentionally keeps support/occlusion
  queries canonical; its control flow does **not**
  automatically transform arbitrary raw
  `net.minecraft.world.phys.AABB` entity searches
  or sky light direction, and may not apply
  through a wrapped non-Level `BlockGetter`.
- [`PlanetVoxelShapeRotation`](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/world/PlanetVoxelShapeRotation.java)
  uses the cube-centered transform and identity
  on POS_Y; coordinate helper for a possible
  **new** separate raw-AABB box transform,
  NOT proof such a sensor integration exists.

**Design rule:** preserve actual world `BlockPos`
as storage identity; no edge alias/duplicate blocks.
At an exact seam or cube corner, traversal
frame may differ from target block's
canonical `PlanetBlockStateFrame`.
Use canonical source frame for block state
and canonical target frame for sturdy-face
query. Keep normal non-Planet `Level`
behavior identical to vanilla.

## 2. Six concrete face bases from checked PlanetFace.java

The **real** project `PlanetFace` local axes
([source](https://github.com/Abbygree11/Planetary/blob/2.0/src/main/java/dev/planetary/topology/PlanetFace.java)) are
not a guessed XYZ orientation:

| Canonical face | Local UP physically | Local DOWN (support step) physically | Local EAST physically | Local SOUTH physically |
|---|---|---|---|---|
| `POS_Y` | World `UP` (+Y) | World `DOWN` (-Y) | +X | +Z |
| `NEG_Y` | World `DOWN` (-Y) | World `UP` (+Y) | +X | -Z |
| `POS_X` | World `EAST` (+X) | World `WEST` (-X) | -Y | +Z |
| `NEG_X` | World `WEST` (-X) | World `EAST` (+X) | +Y | +Z |
| `POS_Z` | World `SOUTH` (+Z) | World `NORTH` (-Z) | +X | -Y |
| `NEG_Z` | World `NORTH` (-Z) | World `SOUTH` (+Z) | -X | -Y |

These are the **interior-face expectations**,
not hardcoded global offsets for every seam/corner.
At edges, the appropriate support target and
its own stable BlockState orientation come
from `PlanetBlockSupportQuery.resolve`, and
the physical direction must be verified using
the actual `PlanetBlockStep`.

## 3. PressurePlate: support and activation are distinct source owners

Comparative [`BasePressurePlateBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L66)
looks at vanilla `pos.below()` and tests
`canSupportRigidBlock` OR
`canSupportCenter(level,supportPos,Direction.UP)`.
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L58)
only removes the plate when the incoming neighbor
direction is `Direction.DOWN` **and**
`canSurvive` fails.
[`onRemove`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L126)
notifies neighbors when powered.

Planet contract (not yet a changed source method):

1. Resolve plate's **canonical** frame and
   request **source-local DOWN** support.
2. Obtain exact physical support `BlockPos`,
   and target's canonical
   `supportLocalSideTowardSource`.
   Query the correct **physical side** of that
   support, converted to canonical target-local
   face for local-state sturdiness logic.
3. Preserve rigid-block OR center support,
   including half blocks and other canonical
   `VoxelShape` support mechanics.
4. When actual physical support changes, invoke
   the **source-local DOWN** invalidation
   path. The original `updateShape` signature's
   direction might be passed as physical
   direction by its caller: verify dispatch
   against actual NeoForge patched call site
   before translating.
5. On removal, requery support and removal
   notifications via physical target positions,
   and avoid double neighbor notifications
   or extra toggles at a shared edge.

[`BasePressurePlateBlock.updateNeighbours`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L142)
calls `updateNeighborsAt(pos)` and
`updateNeighborsAt(pos.below())` in world XYZ.
These are **two physically addressed** callback
targets, not two signal query-side values;
a side-face plate's support is not necessarily
world-below. The notification's changed-block
identity and invocation order should remain
vanilla-compatible.

## 4. PressurePlate raw contact AABB vs VoxelShape — different pipelines

The vanilla pressed and released
[`getShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L41)
is a slightly inset **VoxelShape**, heights
respectively **0.5/16** and **1/16**,
which can be rotated via project
`PlanetBlockShapeRuntime` *when the wrapper
conditions really hold*.

But the detection box in
[`BasePressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L28)
is a raw `TOUCH_AABB` in block-local
Minecraft coordinates:

```text
local X min..max =  1/16 .. 15/16
local Y min..max =  0    ..  4/16
local Z min..max =  1/16 .. 15/16
```

[`PressurePlateBlock.getSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PressurePlateBlock.java#L50)
and [`WeightedPressurePlateBlock.getSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java#L45)
pass **`TOUCH_AABB.move(pos)`** into a world
entity query via `getEntityCount`.
The raw AABB is **not** the pressed/released
visual VoxelShape, and the entity query does
**not** go through the existing
`BlockState.getShape` rotation hook.
Proposal: transform the **eight raw local AABB
corners around the block center (0.5,0.5,0.5)**
using that cell's canonical `PlanetGravityFrame`,
collect new world-aligned min/max, then add
physical block `BlockPos` exactly once.
The resulting **physical** AABB is passed
to `Level.getEntitiesOfClass`.
No need for a globally rotated `AABB` class.

Example expected contact AABB in block-local
physical coordinates (NOT `getShape`):

| Face | Physical thickness 0..4/16 is along | Inset 1/16..15/16 is on |
|---|---|---|
| `POS_Y` | +Y | X and Z |
| `NEG_Y` | -Y | X and Z |
| `POS_X` | +X | Y and Z |
| `NEG_X` | -X | Y and Z |
| `POS_Z` | +Z | X and Y |
| `NEG_Z` | -Z | X and Y |

**Caveat important enough for an independent
test:** even a perfectly transformed
`TOUCH_AABB` is **not sufficient** if the
vanilla entity collision dispatcher never
calls the plate's `entityInside` callback
on a side-facing physical plate. Actual
entity/block contact dispatch or a
bounded sensor-specific server query must
be verified separately. Rotating the
pressure plate visual model cannot establish
its physical trigger.

[`getEntityCount`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L166)
keeps `EntitySelector.NO_SPECTATORS` and
`!isIgnoringBlockTriggers()`.
The ordinary subclass chooses a
`BlockSetType.pressurePlateSensitivity`
filter of `EVERYTHING` (all eligible
Entity) or `MOBS` (eligible LivingEntity);
the weighted subclass counts eligible
Entity with its variant-specific
`maxWeight`. Don't rewrite these filters
or assume entities are counted identically
across plate types.

## 5. Signal query, neighbor callback and power scaling

| Behavior | Read/write owner | Distinct required semantics |
|---|---|---|
| Ordinary plates POWERED | [`PressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/PressurePlateBlock.java#L37) | Boolean value; **0 or 15**, 20-tick pressed recheck; `BlockSetType` sensitivity |
| Weighted plates POWER | [`WeightedPressurePlateBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WeightedPressurePlateBlock.java#L45) | Integer **0–15** via `ceil(15*min(count,maxWeight)/maxWeight)`; 10-tick pressed recheck |
| Plate weak signal | [`BasePressurePlateBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L149) | Returns stored strength; any query side in source |
| Plate direct signal | [`BasePressurePlateBlock.getDirectSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L155) | Only when **API argument `Direction.UP`**; verify queried port direction transformation, NOT `updateNeighboursAt(pos.below())` |
| Daylight sensor weak signal | [`DaylightDetectorBlock.getSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L60) | Returns stored `POWER`; source has no directional override; do not invent direct signal where none exists |
| Notification on plate change | [`BasePressurePlateBlock.updateNeighbours`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L142) | Actual `pos` and physical local-DOWN support cell; preserve update causality and limit |
| Entity plate source | [`BasePressurePlateBlock.checkPressed`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BasePressurePlateBlock.java#L97) | Conditional `setBlock`, entity count, sounds, GameEvent, while pressed scheduled recheck |
| Daylight sensor state | [`DaylightDetectorBlock.updateSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L65) | Conditional POWER write from sky and sun angle or inverted mode |

**Direction warning:** Minecraft's `getDirectSignal`
`Direction` is a *queried direction*. Converting
a world `Direction` into local coordinates requires
knowledge of the **source signal block frame**,
querying block/receiver and actual NeoForge caller.
Do NOT pass `targetLocalSideTowardSource` or
`physicalDirectionToSupport` verbatim as the
queried argument; those are different domains.
A future Phase 7A audit must trace actual query
call sites/receiver semantics before marking
support-cell strong power or a side-face consumer
as accepted.

## 6. Daylight: WORLD light policy separate from gravity chart

[`DaylightDetectorBlock.updateSignalStrength`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L65)
uses physical `BlockPos` at
`getBrightness(LightLayer.SKY,pos)`,
`getSkyDarken()`, and world `getSunAngle(1.0F)`.
There is **no orientation property** defining
a local upward sky-facing normal. A local
rotation of the model or output port does
not rotate the world's sky light engine.

[`DaylightDetectorBlock.getTicker`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L134)
returns an actual BE ticker **only server-side**
and when `dimensionType().hasSkyLight()`.
[`tickEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L140)
updates on global game ticks
`gameTime % 20L == 0`; this must be
distinguished from random block tick or
pressure-plate scheduled block tick.

[`useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/DaylightDetectorBlock.java#L91)
cycles `INVERTED` and recalculates POWER
**immediately**, not waiting for the next
20th game tick. These two write triggers
should produce consistent numerical
POWER and notification behavior.

**Unresolved design choice (do not silently
claim one as implemented):** on a cubic
planet, should sensors follow ordinary global
vanilla SKY samples, or a physically meaningful
per-face sunlight/occlusion policy? Source shows
existing vanilla API but **does not authorize**
inventing six independent local skies,
rotating the world's `LightLayer.SKY`, or
claiming side and underside have correct
lighting. Recommended initial regression
baseline is to preserve the literal
vanilla `LightLayer.SKY` and sun-angle
calculation unless a dedicated planet
lighting engine policy is explicitly designed;
separate functional request and tests
for planet-wide local skylight.

**BE lifecycle vs block tick:**
`DaylightDetectorBlockEntity` is typed
minimal storage; logic lives in
`DaylightDetectorBlock.tickEntity`.
Both pressure plate types use
`BasePressurePlateBlock.tick` as an
**actively scheduled BlockState recheck**
and use `entityInside` only to initially
trigger an unpressed plate. Their 20-
and 10-tick cadence measures time *since
scheduling*, unlike daylight's clock
`gameTime%20==0`. Do not share a single
periodic polling scheduler for these.

## 7. Future runnable acceptance matrix — not executed

Start with a **vanilla, non-Planet control
world**, and a `POS_Y` flat surface as a
second control; verify these match vanilla
before side/bottom regression testing.
Proposed test fixtures (none were run):

| ID / setup | Exact source / target state assertion | Runtime / cross-phase assertion |
|---|---|---|
| **S1** all six faces, stone/wooden ordinary plate with support | Canonical source local DOWN maps to correct support physical cell in face chart, target sturdy local back face; `canSurvive` positive | Place without ghost support; query valid blockstate `POWERED` |
| **S2** six faces, center-only support and nonrigid blocks | Rigid OR `canSupportCenter` UP evaluated on target's canonical contact face | Invalid supports rejected; valid centers retained, removed only for real local-DOWN changes |
| **S3** remove support on six faces, including `NEG_Y` | Plate turns AIR following **support** update, neighboring unrelated face blocks unchanged | No duplicate drops/updates, state changes correct; real world position logged |
| **S4** physical AABB on all six faces | Transform `TOUCH_AABB` around block center exactly once; slab thickness along physical outward normal, not fixed world Y | Eligible player/mob/item crossing triggers, excluded entities don't; verify `entityInside` dispatch |
| **S5** press and release / leave contact region | Only relevant `BlockState` changes `POWERED` or `POWER`, POWER direction local semantics stable | Normal plate 20-tick delayed recheck vs weighted plate 10 ticks, no missed release |
| **S6** ordinary material sensitivity and weighted analog values | Check `BlockSetType` EVERYTHING vs MOBS, maxWeight per actual registered block instance | Pressure ordinary 0/15; weighted ceil quantization against 1, 2, maxWeight, overflow eligible entities |
| **S7** 1/16 edge and corner of contact box | Tiny/inset physical hitbox correctly rotates, not same as `getShape` | Boundary-inclusive collision behavior matches vanilla control; no phantom presses across next block |
| **S8** edge between `POS_Y` and `POS_X` (and all adjacent pairs) | Source and target both use stable **canonical** frame despite path-dependent traversal; phys target not duplicated | Plate connected across gravity seam retains support, power, entity trigger and notifications |
| **S9** exact three-face cube corner, two traversal routes | Identical physical BlockPos and canonical source orientation; support/target back-face consistent | No duplicate state, no wrong-side power or double callbacks; cross-edge standing/contact |
| **S10** plate direct weak vs strong signal | `getSignal` all sides, `getDirectSignal` from correct **queried local UP**; separately assert support-cell physical updates | Phase 7A receiver/query input matches source semantics, no accidental strong power of wrong block |
| **S11** daylight normal/inverted on all six faces, sunrise/noon/night | Preserve consistent SKY/skyDarken/sun-angle formula and clamping, interaction flips INVERTED | No assumption about local radial SKY unless explicitly designed, compare with vanilla at same physical block brightness |
| **S12** daylight in dimension `hasSkyLight=false` | `getTicker` null; no periodic light recalculation; interaction still follows source logic as allowed | Confirm server/client BE creation and no invalid ticking |
| **S13** BE ticker clock vs plate scheduler | Daylight POWER only changes on `gameTime%20==0` except immediate interaction; ordinary and weighted timers are **relative** | Check onLoad/unload, chunk boundary, callback count; no randomTick conflation |
| **S14** direct structure template with known sensor fixture | `StructureTemplate.setBlock` preserved legal `INVERTED/POWER/POWERED` and support; no BlockItem call | No claim that vanilla shipped a sensor-containing structure; Phase 8 explicit fixture |
| **S15** vanilla and non-Planet world regression | Same input entity filter, sky and time, state and callback directions as unpatched Minecraft | No global Direction behavior changes or modifications outside Planetary gravity field |

For each case log physical `BlockPos`,
canonical `PlanetFace`, requested local
direction, actual physical direction,
support target `BlockPos` and target
local inbound face, shape and raw box bounds,
entity IDs/categories at query time,
`POWERED/POWER/INVERTED` state before/after,
scheduled tick and gameTime, `getSignal` /
`getDirectSignal` **queried** directions,
neighbor callback target, and entityInside
invocation. At a seam or corner also record
the distinct traversal-face preference so
a canonical state is not inadvertently
transformed twice.

**Pass gates (UNMET):** exact NeoForge 21.1.215
compiled ASM/Mixin target reachability, server/
client behavior, six-face seam/corner support
and entity query tests, daylight environment
policy, Phase 7A signal output, no vanilla/
other-mod regressions and Phase 8 structure
placement. The table is a **test specification,
not results**.

## 8. Durable counts and next first open task

No new concrete Java classes were reviewed
this research step: same three sensor classes
already source+original reflection reviewed
from 3A-6.1 (DaylightDetectorBlock,
PressurePlateBlock, WeightedPressurePlateBlock,
**16 actual registered BLOCK IDs**).
Full ledger remains **69/241**
`SOURCE_REVIEWED_INTEGRATION_PENDING`
(**190/1060 BLOCK IDs**),
**172/241 `REVIEW_PENDING`**
(**870/1060 BLOCK IDs**); Sculk and
LightningRod classes are still source-pending.
**All 241** class ASM-patched bytecode,
Planetary adapter and gameplay-acceptance
gates remain `REVIEW_PENDING`.
No changes to Java, assets or Minecraft
runtime; no build/client/server run.

**NEXT FIRST microtask 3A-6.4 (card task 4):**
exact original unmodified NeoForge 21.1.215
CI ZIP vs all **69** reviewed classes and
their **190** complete registered IDs and
five nearest method declaration owners,
preserve all 172 pending classes / acceptance
gates, select and create the next small
independent owner-family card. One GitHub
commit and stop; do NOT claim full Phase 2.
