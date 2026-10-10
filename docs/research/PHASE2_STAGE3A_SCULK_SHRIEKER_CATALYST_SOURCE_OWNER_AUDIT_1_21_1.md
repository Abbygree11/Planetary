# Phase 2 Stage 3A-8.1 — SculkShrieker and SculkCatalyst exact compiled BLOCK owners plus source writer graph

**2026-10-10** · branch `2.0` · Minecraft
**1.21.1** / NeoForge **21.1.215** / Java 21.

**Scope:** exactly **two formerly source-pending
registered Java BLOCK implementation classes /
two exact BLOCK IDs**. This source+original
compiled declaration audit is distinct from
NeoForge modified method bytecode, Minecraft
runtime/gameplay or Planetary integration.
Two **different algorithms** must not be combined
into a generic sculk “listener” adapter:
shrieker's per-BE `VibrationSystem.Listener`
and warnings versus catalyst's direct
`CatalystListener` on entity death and
`SculkSpreader` block mutation.

## 1. Original immutable NeoForge compiled registry

Primary immutable input
`/mnt/data/phase2-neo1211-registry-census.zip`,
[GitHub Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
artifact **11643813158**, built at commit
`aa39572950a15403ea0a9003eefccf3bf6675ff7`.
Archive SHA-256 independently verified:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
ZIP original members: BLOCK registry **1060
rows / 241 concrete classes**; ITEM registry
**1333 rows**; BlockState properties **1712 rows**.
This task reads original BLOCK registry,
**not** the 1333 ITEM→BLOCK placed_block
join, deferred to Stage 3A-8.2.

| Original `BLOCK.java_class` | Exact `registry_id` | Original `declared_property_names` | `class_hierarchy` |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkShriekerBlock` | `minecraft:sculk_shrieker` | `can_summon,shrieking,waterlogged` | `SculkShriekerBlock>BaseEntityBlock>Block>BlockBehaviour` |
| `net.minecraft.world.level.block.SculkCatalystBlock` | `minecraft:sculk_catalyst` | `bloom` | `SculkCatalystBlock>BaseEntityBlock>Block>BlockBehaviour` |

**Neither original class is an orientation-property
candidate** (`has_orientation_candidate=false`);
that does NOT mean their physical/block-entity
algorithms are gravity-independent. No FACING
property exists on these two classes.
SculkShriekerBlock has WATERLOGGED, but
SculkCatalystBlock does **not**.

Exactly five original compiled
`effective_method_owners`, matched by
**full qualified argument signatures**,
not method names or nearest guessed source
class. Each row has exactly one original
BLOCK ID; compiled class owner from that
original ID:

| Exact method signature (short types; original full qualified) | SculkShriekerBlock | SculkCatalystBlock |
|---|---|---|
| `getStateForPlacement(BlockPlaceContext)` | **SculkShriekerBlock** | **Block** |
| `canSurvive(BlockState,LevelReader,BlockPos)` | BlockBehaviour | BlockBehaviour |
| `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)` | **SculkShriekerBlock** | BlockBehaviour |
| `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)` | BlockBehaviour | BlockBehaviour |
| `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)` | Block | Block |

Other original effective declaring owners
from the same compiled registry (not to confuse
with `randomTick`):
`tick(BlockState,ServerLevel,BlockPos,RandomSource)`
**SculkShriekerBlock / SculkCatalystBlock**;
`getCollisionShape` **SculkShriekerBlock /
BlockBehaviour**;
`getFluidState` **SculkShriekerBlock /
BlockBehaviour**;
`neighborChanged` **BlockBehaviour** both;
`rotate` and `mirror` **BlockBehaviour**
both. `getTicker` lives in the concrete
blocks (not one of the five original census
method slots); each uses a DIFFERENT
BlockEntity ticker and callback.

Original method-owner reflection declares
what the class would dispatch to, not that
NeoForge 21.1.215 patched body, mixin and
Planetary execution paths are correct.

## 2. Pinned comparable Java 1.21.1 source set

Comparative Java source revision
[`b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1)
(**not** compiled NeoForge patched method code):
[`SculkShriekerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L32),
[`SculkShriekerBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L46),
[`SculkCatalystBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java#L22),
[`SculkCatalystBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java#L29),
[`SculkSpreader`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java#L40),
[`SculkBehaviour`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBehaviour.java#L12),
[`SculkBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L15),
[`SculkVeinBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L25),
[`MultifaceSpreader`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/MultifaceSpreader.java#L13)
and shared [`VibrationSystem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/gameevent/vibrations/VibrationSystem.java#L36).

Class-owned methods are different from
their BE-owned algorithms and event sources.
The original 241 class census is a census
of **registered concrete block classes**,
not a count of all methods and BlockEntity,
listener, SculkBehaviour or GameEvent owners.

## 3. Sculk SHRIEKER — state, BE vibration, warning and Warden

### 3.1 Real BlockState and physical geometry

[`SculkShriekerBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L150)
reads water at the actual clicked cell;
default `SHRIEKING=false`,
`WATERLOGGED=false`,
`CAN_SUMMON=false`.
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L136)
schedules WATER fluid tick if waterlogged;
[`getFluidState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L157)
returns source WATER when set.
Shape is **8/16-high canonical-local
COLLIDER**, used by
[`getCollisionShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L109)
and `getOcclusionShape`, and
`useShapeForLightOcclusion=true`.
`getShape` not directly overridden
by this class. Rendering/model and
actual entity `stepOn` invocation
on side/underside faces remain open.

### 3.2 Two distinct activation entrypoints and a BE-owned warning gate

[`SculkShriekerBlock.stepOn`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L68)
on the server resolves player identity,
including a controlling rider or
projectile/item owner through
[`SculkShriekerBlockEntity.tryGetPlayer`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L121),
and calls BE `tryShriek`.
This is independent of a normal world
game event.

The BE is itself
`GameEventListener.Provider<VibrationSystem.Listener>`
and `VibrationSystem`. Its nested
[`VibrationUser`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L241)
listens within radius **8**, has
`GameEventTags.SHRIEKER_CAN_LISTEN`,
requires source attributable to a player,
and demands `SHRIEKING=false`.
The shared `VibrationSystem.Ticker`
delivers the event after physical
Vec3 distance/occlusion and chunk tick
checks. **The listener, physical entity
stepOn dispatcher and two trigger paths
are separate runtime gates**, neither
established by placement alone.

BE `tryShriek`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L160))
resets warningLevel when starting a new
shriek, considers
`canRespond` and `tryToWarn`.
[`tryToWarn`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L177)
calls `WardenSpawnTracker.tryWarn`,
which is NOT equivalent to immediate
warden summoning.

The BE's
[`shriek`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L185)
**writes `BlockState.SHRIEKING=true`**,
schedules **90-tick** block reset,
emits a level event and
`GameEvent.SHRIEK`.
The actual scheduled
[`SculkShriekerBlock.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L95)
writes `SHRIEKING=false` and asks BE
`tryRespond`. Removing the shrieker
while SHRIEKING also invokes
[`onRemove`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java#L84)
BE `tryRespond` before removal.
These are **independent state writers
and response callback paths**.

[`canRespond`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L196)
requires `CAN_SUMMON=true`,
difficulty **not PEACEFUL** and
`RULE_DO_WARDEN_SPAWNING=true`.
A sensor might shriek without satisfying
these conditions; **do not identify
any shriek with an actual Warden spawn**.

[`tryRespond`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L203)
may call `trySummonWarden` when warning
level above threshold, otherwise plays
warning audio. Its
[`trySummonWarden`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L229)
calls `SpawnUtil.trySpawnMob` around
the real world BlockPos, with
**20 attempts, world XZ range 5,
Y range 6 and ON_TOP_OF_COLLIDER**.
Those are actual physical-world
spawn ranges; they do NOT automatically
use six local-gravity up directions.
The BE may apply darkness around its
world-physical position in radius 40.
Warning and vibration data save/load
(`warning_level`, `listener`)
in [BE NBT methods](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java#L83).
Warden entity gravity/AI/spawn acceptance
remains a separate entity/Phase 7A issue;
no claim that the spawn happens in Planet
worlds is made.

### 3.3 Non-item state creator identified now

[`SculkBlock.getRandomGrowthState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L85)
chooses a default `SCULK_SHRIEKER`
state with `CAN_SUMMON=isWorldGeneration`
or an ordinary `SCULK_SENSOR` state,
then [`SculkBlock.attemptUseCharge`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L29)
writes that state with `LevelAccessor.setBlock`
to **world `blockpos.above()`**, outside
`BlockItem.place`.
The code also checks nearby sensor/shrieker
density in a **physical X/Z and Y volume**.
This source proves a real **programmatic
creation of sculk shrieker/sensor states**
by growth, with a world-vs-worldgen
CAN_SUMMON distinction; it does not prove
that any tested Planet gravity face
already implements that local-UP policy.

**Future task 3A-8.2** must still
independently enumerate original ITEM
creators and direct/template/generation
entrypoints, so do not mark Item/structure
coverage complete here.

## 4. Sculk CATALYST — direct death event, BLOOM, charge cursor and spread

### 4.1 Not the VibrationSystem listener / not WATERLOGGED

[`SculkCatalystBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java#L22)
inherits `Block.getStateForPlacement`,
has only `PULSE=BlockStateProperties.BLOOM`,
default **BLOOM=false**.
No orientation property or WATERLOGGED.
[`getTicker`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java#L64)
returns server-only
`SculkCatalystBlockEntity::serverTick`
for `SCULK_CATALYST` BE, **NOT**
`VibrationSystem.Ticker`.
[`serverTick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java#L39)
advances `SculkSpreader.updateCursors`
every server BE tick.

The actual BE's
[`CatalystListener`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java#L63)
implements `GameEventListener` directly,
uses delivery `BY_DISTANCE` and
listening **radius 8**. It is **not**
`VibrationSystem.Listener`, does not
inherit the shrieker's vibration
travel/occlusion path, and its
world-XYZ event location is a separate
physical event input.

### 4.2 Exact death-to-spread writer chain

[`handleGameEvent`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java#L96)
accepts `GameEvent.ENTITY_DIE` only with
a `LivingEntity` source, avoids
double-consuming experience, reads
death damage/reward and, if eligible,
[`addCursors`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java#L145)
at
`BlockPos.containing(eventVec3.relative(Direction.UP,0.5))`.
This is **global/world UP 0.5**
offset on the event position, NOT
the dead entity's gravity-local UP.
Calls `skipDropExperience` so the same
experience isn't dropped normally.
Other dependent actions include the
advancement and event listener.

[`CatalystListener.bloom`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java#L131)
writes `BLOOM=true`,
schedules a **relative 8-tick**
block tick, sends `SCULK_SOUL`
particles at **world `Y+1.15`**,
plays sound. Scheduled
[`SculkCatalystBlock.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java#L47)
writes `BLOOM=false`.
This is different from the
per-server-BE-tick cursor progression,
and neither is `randomTick`.

The BE persists
SculkSpreader cursor data using
[`loadAdditional/saveAdditional`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java#L45).
`SculkSpreader` has up to **32
cursors**, uses actual physical
BlockPos keys to coalesce/track charge,
and has different
[`createLevelSpreader/createWorldGenSpreader`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java#L66)
modes with different replaceable tags,
growth costs and charge decay.
The BE creates the *level* spreader;
worldgen uses a different creation path.

### 4.3 Full separate mutable-world writer graph, not just catalyst BlockState

[`SculkSpreader.ChargeCursor.update`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java#L309)
calls block-specific
`SculkBehaviour.attemptSpreadVein`,
`attemptUseCharge`, `onDischarged`,
then moves to valid physical neighbors.
The motion
[`getValidMovementPos`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java#L388)
uses **world-axis non-corner neighbor
offsets**, checks physical face
`isFaceSturdy` and other sculk
behavior. No global blanket direction
patch or assumption that a world
corner means a cube-face seam is valid.

[`SculkBehaviour.DEFAULT`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBehaviour.java#L14)
can call `SculkVeinBlock` or
`MultifaceSpreader` to write face-attached
states. [`SculkVeinBlock.attemptPlaceSculk`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L129)
uses actual world-neighbor directions,
`setBlock` to replace a tagged block
with sculk, then `Block.pushEntitiesUp`
and more spreading/water-state effects.
[`SculkVeinBlock.onDischarged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkVeinBlock.java#L88)
may remove faces or revert to AIR/WATER.

[`SculkBlock.attemptUseCharge`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L29)
can create a new sensor/shrieker
**world-above** a sculk block, and
[`SculkBlock.getRandomGrowthState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkBlock.java#L85)
is a state author of `CAN_SUMMON` and
`WATERLOGGED` on the new block.
Important: `SculkBlock`, `SculkVeinBlock`,
`MultifaceSpreader` are graph owners;
their registered class review status
in the ledger is independent of
this source inspection and MUST NOT
be changed here. A full direct
worldgen/ITEM creation audit is
separate Stage 3A-8.2 and phase 8/9.

## 5. Cross-phase boundaries and acceptance candidates (not run)

| Owner / mechanism | Required future Phase-boundary acceptance |
|---|---|
| `SculkShriekerBlock` water & 8/16 collider | Phase 2 WATERLOGGED state, Phase 5 water tick and flows, Phase 3 local collision/occlusion & actual `stepOn` callback on all six faces |
| Shrieker BE VibrationSystem and 90 scheduled ticks | Phase 7A listener delivery/loaded chunk, per-BE ticker and relative block tick state; no duplicate event |
| WardenSpawnTracker / SpawnUtil physical XZ/Y | Phase 7A/world entity spawn and AI; local-gravity surface policy is **not** inherited by vanilla SpawnUtil |
| Catalyst direct GameEvent.ENTITY_DIE listener | Phase 7A actual physical event source, XP consumption once and BE cursor pipeline |
| Catalyst BLOOM + SculkSpreader | Phase 2 direct BlockState writes/cursor targets, Phase 3 SCULK_SOUL world-Y particles, Phase 7A BE+scheduled tick, Phase 8/9 direct worldgen/writer and six-face physical growth |
| SculkVeinBlock/MultifaceSpreader/SCULK growth | Phase 2/8/9 multi-face graph, world/target canonical support and local-UP, no duplicate seam BlockPos, waterlogged propagation |
| Source/API 3-way separation | Preserve physical world BlockPos/Vec3 where appropriate, map only block-local support/shape and render semantics; don't rotate all event and world-spawn offsets globally |

**Next acceptance fixtures to specify fully
in Stage 3A-8.3 (NONE run now):** both
exact BLOCK IDs, item vs growth placement,
CAN_SUMMON false hand-placed / worldgen
true origin, six-face local 8/16
collision for shrieker, player stepOn
and actual BE event filtering, 90-tick
schedule, PEACEFUL and
RULE_DO_WARDEN_SPAWNING gating,
WardenSpawnTracker vs world-physical
spawn range and gravity; catalyst
living-entity death and XP once,
direct listener radius8, 8-tick bloom,
distinct cursor BE persistence,
charge spread across seam/corner,
SculkVein WATERLOGGED, physical world
Y +0.5 / +1.15 particle offsets,
vanilla non-Planet control. These
are **requirements**, not game PASS.

## 6. Durable status and next first task

Promoted **only**:
`SculkShriekerBlock`
(`minecraft:sculk_shrieker`) and
`SculkCatalystBlock`
(`minecraft:sculk_catalyst`),
2 original registered classes /2 BLOCK IDs,
from source `REVIEW_PENDING` to
`SOURCE_REVIEWED_INTEGRATION_PENDING`
with `REFLECTION_OWNER_VERIFIED`.
Full 16-column ledger now:
**73/241** source+compiled declaration
reviewed (**194/1060 BLOCK IDs**);
**168/241** source `REVIEW_PENDING`
(**866/1060 BLOCK IDs**).
`LightningRodBlock` remains source pending.
All 241 `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance`,
`gameplay_acceptance` columns still
`REVIEW_PENDING`.

**No Java or NeoForge ASM patch,
CI/client/server build, or actual game
tests.** Source inspection of additional
nonregistered BE/Spreader and registered
SculkBlock / SculkVeinBlock graph authors
does not promote those rows.

**NEXT FIRST unchecked microtask 3A-8.2**,
checkbox 2 in
[`02f-sculk-shrieker-catalyst-owners.md`](../phases/phase-02/02f-sculk-shrieker-catalyst-owners.md):
independently join original ITEM registry
`placed_block` and exact special vs
BlockItem authors for two BLOCK IDs,
complete direct/growth/worldgen/structure
writer audit and source state-component
bypasses. One bounded research commit
then stop.
