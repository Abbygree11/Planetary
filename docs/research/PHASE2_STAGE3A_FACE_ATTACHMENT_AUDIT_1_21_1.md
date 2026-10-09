# Stage 3A-2.1 — physical attachment and support owners

Date: 2026-10-10. Scope: Minecraft **1.21.1**, NeoForge target
**21.1.215**, Planetary branch `2.0`.

**Status: 11 class-level comparative-source reviews, 29 runtime
block IDs by count; NOT runtime/ASM or gameplay accepted.**

## Evidence hierarchy (do not promote evidence silently)

- Registry class roster and registered-ID counts from actual
  [NeoForge census run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
  original artifact `11643813158`: **11 real concrete
  classes; 29 registered IDs**. The individual ID strings
  were not independently re-extracted in this package:
  the TSV deliberately retains `SEE_REGISTRY_ARTIFACT_ID_JOIN_PENDING`
  instead of presenting guessed ID mappings.
- Read the **method bodies** in comparative
  [Mojang-named Minecraft 1.21.1 source snapshot `b77c5c6995874f6cf2755bc5234428906b337b75`](https://github.com/hackersense/OptiFine-Source/tree/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block),
  including inherited nonregistered
  [`BaseTorchBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseTorchBlock.java#L34) and
  [`FaceAttachedHorizontalDirectionalBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FaceAttachedHorizontalDirectionalBlock.java#L29).
- The following table reports **source inheritance/declaration
  ownership candidates**, NOT newly verified reflection
  results against the NeoForge-patched class files.
  The ledger tags this batch
  `COMPARATIVE_SOURCE_OWNER_ONLY_NEOFORGE_REFLECTION_RECHECK_PENDING`
  rather than `REFLECTION_OWNER_VERIFIED`. Never infer Mixin
  matching or actual `INVOKE` sites from this table.
- Previously audited 24 classes retain their separately
  verified `REFLECTION_OWNER_VERIFIED` status; this new
  batch did not modify their owner evidence.

## Eleven actual registered classes in the bounded owner cluster

The owner triple is **getStateForPlacement(BlockPlaceContext)** /
**canSurvive(BlockState,LevelReader,BlockPos)** /
**updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)**.
The last method owner is **randomTick(BlockState,ServerLevel,BlockPos,RandomSource)**,
NOT `tick` (scheduled tick is separate).

| Runtime concrete class | Registered IDs (count) | Source declaration owner triple | randomTick owner | Mechanisms | Evidence |
|---|---:|---|---|---|---|
| `TorchBlock` | 2 | `Block` / `BaseTorchBlock` / `BaseTorchBlock` | `BlockBehaviour` | P22,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/TorchBlock.java#L35) |
| `WallTorchBlock` | 2 | `WallTorchBlock` / `WallTorchBlock` / `WallTorchBlock` | `BlockBehaviour` | P07,P22,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallTorchBlock.java#L77) |
| `RedstoneTorchBlock` | 1 | `Block` / `BaseTorchBlock` / `BaseTorchBlock` | `BlockBehaviour` | P22,P23,P36 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneTorchBlock.java#L67) |
| `RedstoneWallTorchBlock` | 1 | `RedstoneWallTorchBlock` / `RedstoneWallTorchBlock` / `RedstoneWallTorchBlock` | `BlockBehaviour` | P07,P22,P23,P36 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneWallTorchBlock.java#L53) |
| `LadderBlock` | 1 | `LadderBlock` / `LadderBlock` / `LadderBlock` | `BlockBehaviour` | P07,P22,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LadderBlock.java#L72) |
| `LanternBlock` | 2 | `LanternBlock` / `LanternBlock` / `LanternBlock` | `BlockBehaviour` | P22,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LanternBlock.java#L49) |
| `SporeBlossomBlock` | 1 | `Block` / `SporeBlossomBlock` / `SporeBlossomBlock` | `BlockBehaviour` | P22,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SporeBlossomBlock.java#L38) |
| `AmethystClusterBlock` | 4 | `AmethystClusterBlock` / `AmethystClusterBlock` / `AmethystClusterBlock` | `BlockBehaviour` | P07,P22,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AmethystClusterBlock.java#L101) |
| `EndRodBlock` | 1 | `EndRodBlock` / `BlockBehaviour` / `BlockBehaviour` | `BlockBehaviour` | P07,P23 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/EndRodBlock.java#L31) |
| `LeverBlock` | 1 | `FaceAttachedHorizontalDirectionalBlock` / `FaceAttachedHorizontalDirectionalBlock` / `FaceAttachedHorizontalDirectionalBlock` | `BlockBehaviour` | P08,P22,P23,P34,P36 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LeverBlock.java#L108) |
| `ButtonBlock` | 13 | `FaceAttachedHorizontalDirectionalBlock` / `FaceAttachedHorizontalDirectionalBlock` / `FaceAttachedHorizontalDirectionalBlock` | `BlockBehaviour` | P08,P22,P23,P34,P36 | [comparative source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ButtonBlock.java#L132) |

Most listed `randomTick` paths inherit `BlockBehaviour`
but this does NOT mean there is no tick behavior.
`RedstoneTorchBlock` and `ButtonBlock` have their own
**scheduled** `tick`; `RedstoneWallTorchBlock` inherits
the scheduled redstone torch tick. `EndRodBlock` has no
survival/support predicate of its own: its six-way placement
is an orientation author, not necessarily a supported-object
attachment. Keep its `canSurvive` marked as generic source
inherited, NeoForge status pending.

## Mechanism-level source call paths and boundary conditions

### Standing torches vs wall torches: not one algorithm

[`BaseTorchBlock.canSurvive`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseTorchBlock.java#L44)
performs `canSupportCenter(level, pos.below(), UP)`;
[`BaseTorchBlock.updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BaseTorchBlock.java#L34)
invalidates when the **physical DOWN callback** loses
support. `TorchBlock` additionally emits client particles,
which belong to previously accepted particle work/Phase 3.

[`WallTorchBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallTorchBlock.java#L77)
instead stores horizontal `FACING`, inspects
`pos.relative(FACING.opposite)`, and checks the **support
block's FACING face sturdiness**. Its own
[`getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallTorchBlock.java#L91)
enumerates *horizontal* nearest-looking directions;
[`updateShape`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/WallTorchBlock.java#L116)
matches neighbor callback against `FACING.opposite`.
**Both** chosen block variant and actual support must be
reframed; merely adapting standing torch `pos.below()`
cannot fix wall torches.

[`StandingAndWallBlockItem.getPlacementState`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/StandingAndWallBlockItem.java#L32)
is an **item-side alternate author** which asks the wall
block for placement and chooses between standing/wall states.
Natural torch item use therefore cannot be proven by
directly constructing either block and invoking a
`getStateForPlacement` unit test. Stage 3B must audit
its exact target registrations and NeoForge semantics.

### Redstone torch: attachment, input, output, time are separate

[`RedstoneTorchBlock.hasNeighborSignal`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneTorchBlock.java#L70)
reads the input at `pos.below()` / `Direction.DOWN`,
distinct from the standing torch **support predicate**.
[`RedstoneTorchBlock.tick`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneTorchBlock.java#L76)
maintains burn-out state, and
[`neighborChanged`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneTorchBlock.java#L106)
schedules the update. Its `onPlace`/`onRemove`
notify six physical neighbors. Those are **Phase 7A**
redstone consumers with a Phase-2 source/target chart
handoff, not permission to globally rewrite directions.

[`RedstoneWallTorchBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneWallTorchBlock.java#L53)
does not inherit `WallTorchBlock` as a Java superclass:
it subclasses **RedstoneTorchBlock**. It separately calls
`WallTorchBlock.canSurvive` and delegates placement to
`Blocks.WALL_TORCH.getStateForPlacement(context)`
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RedstoneWallTorchBlock.java#L66)).
It **overrides** `hasNeighborSignal` to sample
`pos.relative(FACING.opposite)`, and its own output
`getSignal` excludes `FACING`.
The shared base torch placement/survival and wall redstone
signal pathways are NOT one single shared JVM hook.
Verify item selected block and virtual-dispatch targets.

### Ladder, lantern, blossom and amethyst: distinct predicates

- [`LadderBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LadderBlock.java#L72):
  wall FACING and `canAttachTo` at the supporting cell,
  special `replacingClickedOnBlock` branch
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LadderBlock.java#L98));
  `updateShape` invalidates on matching physical
  supporting-neighbor callback, schedules water ticks.
  Ladder FACING can be rotated/mirrored; local horizontal
  chart and actual support face must be distinguished.
- [`LanternBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LanternBlock.java#L49):
  `HANGING` is authored from **Axis.Y** nearest directions,
  support through `getConnectedDirection`, water state
  preservation, and local ceiling/floor
  `canSupportCenter` neighbor
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LanternBlock.java#L82)).
  This is neither torch's single `DOWN` nor wall torch's
  horizontal `FACING`. Water tick is Phase 5 handoff.
- [`SporeBlossomBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SporeBlossomBlock.java#L38):
  tests physical **above** / `DOWN` center support,
  forbids water in self cell; update invalidation is
  triggered by **UP** callbacks
  ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SporeBlossomBlock.java#L44)).
  Standalone ceiling predicate plus source authoring.
  Client-only `animateTick` is separate from geometry
  support and from redstone tick.
- [`AmethystClusterBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AmethystClusterBlock.java#L101):
  `FACING` is **six-direction** from the clicked face,
  opposite support position with sturdy
  state-local facing face, and `updateShape` compares
  the physical opposite-FACING callback and schedules
  water ticks ([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/AmethystClusterBlock.java#L109)).
  Keep six-way support, rotation/mirroring and voxel-shape
  ownership separately; water physics Phase 5, render Phase 3.

### End rod and FACE-attach devices: don't conflate free state with support

[`EndRodBlock.getStateForPlacement`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/EndRodBlock.java#L31)
derives six-direction FACING from the clicked physical face
but **reverses it** if an existing adjacent EndRod points
toward the new rod. The static six-way `RodBlock`
axis-shape and item state composition are distinct.
The source does not declare a custom `canSurvive`:
do **not** invent a required sturdy neighbor for it.

[`FaceAttachedHorizontalDirectionalBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/FaceAttachedHorizontalDirectionalBlock.java#L29)
is a **non-concrete/non-registry ancestor**, shared by
`LeverBlock` and `ButtonBlock`; it owns
`getStateForPlacement`, `canSurvive`,
`updateShape`, and `getConnectedDirection`.
It transforms source physical direction into
`AttachFace.FLOOR/WALL/CEILING` + horizontal `FACING`,
then uses the target supporting block's
`isFaceSturdy` face.
[`LeverBlock.useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LeverBlock.java#L108)
and [`ButtonBlock.useWithoutItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ButtonBlock.java#L132)
both change `POWERED` but with distinct semantics:
lever is a toggle; button schedules a reset
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ButtonBlock.java#L208)) and may be held
by an arrow entity
([source](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/ButtonBlock.java#L225)).
Both update the attached neighbor and emit directional
redstone signals. Preserve `getConnectedDirection`
and signal ports as **Phase 7A** contract; block
placement/support authoring remains Phase 2.

## Coverage, hazards and acceptance

Source-reviewed: **11 newly classified classes / 29 IDs**;
running total **35 of 241** classes / **81 of 1060**
block IDs. Remaining **206 classes / 979 IDs** remain
`REVIEW_PENDING`.

The source metadata **does not yet** establish
whether any Planetary placement adapter reaches these
different owner bodies after NeoForge/ModDevGradle
transforms. Do not duplicate Mixins per concrete class
before Stage 3C exact-version evidence. This research
does not change runtime code, test or gameplay PASS.

Required later validation: actual Java 21 NeoForge
21.1.215 method declaring owners and JVM INVOKE sites, injection handlers and construction paths; in particular:
the distinction between **local semantic attachment face**
and **physical BlockPos/Direction and neighbor callback**;
six faces and seam, real player
`StandingAndWallBlockItem`, underside/side
`HANGING`, self-support state remap, water tick,
redstone signal and scheduled behavior (Phase 7A),
source-vs-target charts, rotate/mirror, non-Planet world
vanilla equivalence, +Y/±X/±Z/-Y seams/corners.
Unknown classes, modded registrations and game behavior
remain pending.

**NEXT 2.3A-2.2:** corroborate these 11 exact NeoForge
patched-class method owners and creation/interaction
authors, update provenance levels in the same TSV,
then proceed to a new 8–15-class group. One answer,
one committed checkpoint.
