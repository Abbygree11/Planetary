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


## 2026-10-10 addendum — CI artifact exact owner and item reconciliation

**Stage 2.3A-2.2 (bounded micro-task)**, original **unmodified**
ZIP `phase2-neo1211-registry-census`, artifact ID `11643813158`,
run [37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055),
source revision `aa39572950a15403ea0a9003eefccf3bf6675ff7`,
verified SHA-256
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Decoded **original** `phase2-neo1211-block-registry.tsv` and
`phase2-neo1211-item-registry.tsv` (1060/1333 rows respectively),
not just a previously written summary or inferred ID strings.

**Outcome:** for all **11 classes / 29 BLOCK IDs**, compiled NeoForge
21.1.215 **reflection exact-signature nearest declaration owners**
match the four source owner columns above
(`getStateForPlacement(BlockPlaceContext)`,
`canSurvive(BlockState,LevelReader,BlockPos)`,
`updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`,
`randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`),
plus `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`
= `Block` for all eleven. Signatures were matched independently,
not by method names alone. `neighborChanged` is inherited from
`BlockBehaviour` except **`RedstoneTorchBlock` and
`RedstoneWallTorchBlock`**, which resolve to
`RedstoneTorchBlock`. Distinguish `randomTick`
(owner `BlockBehaviour` for all eleven) from **scheduled**
`tick`: `RedstoneTorchBlock` declares the scheduled tick for
both redstone torch classes, and `ButtonBlock` has its own
scheduled tick. This corrects a source-only uncertainty, **not**
a new bytecode/ASM or Mixin runtime pass.

Real IDs and registered item creator paths:

| Concrete block class | Original CI registered BLOCK IDs (minecraft namespace omitted) | Registered block-item creation path | Source-visible Planet adapter or limitation |
|---|---|---|---|
| `TorchBlock` | `soul_torch,torch` | `StandingAndWallBlockItem:2` | `BaseTorchBlockSupportMixin;StandingAndWallBlockItemMixin` |
| `WallTorchBlock` | `soul_wall_torch,wall_torch` | `NO_DIRECT_BLOCK_ITEM` | `WallTorchBlockSupportMixin;StandingAndWallBlockItemMixin` |
| `RedstoneTorchBlock` | `redstone_torch` | `StandingAndWallBlockItem:1` | `BaseTorchBlockSupportMixin;StandingAndWallBlockItemMixin` |
| `RedstoneWallTorchBlock` | `redstone_wall_torch` | `NO_DIRECT_BLOCK_ITEM` | `WallTorchBlockSupportMixin(static helper);RedstoneWallTorchSupportMixin(update);StandingAndWallBlockItemMixin` |
| `LadderBlock` | `ladder` | `BlockItem:1` | `LadderBlockSupportMixin` |
| `LanternBlock` | `lantern,soul_lantern` | `BlockItem:2` | `NO_LANTERN_BLOCK_SUPPORT_MIXIN` |
| `SporeBlossomBlock` | `spore_blossom` | `BlockItem:1` | `SporeBlossomLocalSupportMixin` |
| `AmethystClusterBlock` | `amethyst_cluster,large_amethyst_bud,medium_amethyst_bud,small_amethyst_bud` | `BlockItem:4` | `NO_AMETHYST_CLUSTER_SUPPORT_MIXIN` |
| `EndRodBlock` | `end_rod` | `BlockItem:1` | `EndRodLocalPlacementMixin` |
| `LeverBlock` | `lever` | `BlockItem:1` | `FaceAttachedHorizontalDirectionalBlockSupportMixin` |
| `ButtonBlock` | `acacia_button,bamboo_button,birch_button,cherry_button,crimson_button,dark_oak_button,jungle_button,mangrove_button,oak_button,polished_blackstone_button,spruce_button,stone_button,warped_button` | `BlockItem:13` | `FaceAttachedHorizontalDirectionalBlockSupportMixin` |

**Independent item census check:** 29 block IDs correspond to 26
directly registered `BlockItem` subclasses (3
`StandingAndWallBlockItem` + 23 ordinary `BlockItem`);
`wall_torch`, `soul_wall_torch`, and
`redstone_wall_torch` have **no individually registered
BlockItem**. Their authoritative player-item entrypoint
is `StandingAndWallBlockItem.getPlacementState` on the
standing torch item, which calls wall block placement and
chooses the candidate by hit/direction. This is exactly
why `Block.getStateForPlacement` alone cannot prove
natural wall-torch placement.

Additional **compiled** method declaration owners:
`useWithoutItem` = `LeverBlock` / `ButtonBlock`
for those classes; `useItemOn` = `BlockBehaviour`
for all 11. All other classes' `useWithoutItem`
resolve to `BlockBehaviour`. This excludes
`RedstoneTorchBlock` signal/tick correctness, which has
other methods not included in the scanned use owners.

**Existing code evidence (source-reviewed):** the eight
mixins `BaseTorchBlockSupportMixin`,
`WallTorchBlockSupportMixin`,
`RedstoneWallTorchSupportMixin`,
`LadderBlockSupportMixin`,
`FaceAttachedHorizontalDirectionalBlockSupportMixin`,
`SporeBlossomLocalSupportMixin`,
`EndRodLocalPlacementMixin`, and
`StandingAndWallBlockItemMixin`
exist under `src/main/java/dev/planetary/mixin/` and
are registered in `src/main/resources/planetary.mixins.json`
(`mixins` array). Source hooks are only *potential*
adapters; this confirms their **existence and registration**,
not bytecode application or gameplay. No dedicated
`LanternBlockSupportMixin` or
`AmethystClusterBlockSupportMixin` was found in that
mixin directory; those paths remain **known source
integration gaps**, pending a whole-owner implementation
wave. `SporeBlossomLocalSupportMixin` exists but
user-level placement/survival is still unaccepted.

All eleven TSV rows now set
`registry_dispatch_evidence=REFLECTION_OWNER_VERIFIED`
and include the **actual ID list** from the original ZIP
instead of `SEE_REGISTRY_ARTIFACT_ID_JOIN_PENDING`.
Evidence tier improved, **disposition counts do not
change**: 35/241 classes source-reviewed (81/1060 IDs),
206/241 classes pending (979/1060 IDs).

### Critical remaining distinction: reflection is NOT ASM

Reflection locates the nearest declared implementation in
the NeoForge runtime class hierarchy. This ZIP **does not**
contain JVM class files, Mixin transformed method bodies,
`INVOKEVIRTUAL` / `INVOKESTATIC` call-site evidence,
NeoForge patches or active handler match counts.
Therefore `neoforge_patch_bytecode_review`,
`planet_adapter_acceptance` and
`gameplay_acceptance` stay `REVIEW_PENDING`
for **all eleven**. Particularly audit whether an
injected HEAD shortcut skips NeoForge extension hooks,
whether redstone input/output uses the target chart
at seams, and how physical neighbor callbacks feed
the class-specific updateShape. Those are Stage 3C /
later implementation and acceptance gates, not
claims completed in this research-only packet.

**Next** (separate turn): complete card 2.3A-2
micro-task 3 with a bounded new family that has
**no FACING** and independent local vertical/graph
semantics (e.g. sea pickles/cactus/sugar cane or
a different natural support/growth cluster). Do not
auto-approve whole registered class roster.
