# Phase 2 research, wave C: multiblock, compound orientation, interaction and structure bypass

Target Minecraft 1.21.1 / NeoForge 21.1.215.
Status: source-level mechanism research checkpoint ONLY.
No production patch in this wave; actual NeoForge runtime/registry
owner audit remains pending the executable census.

Compare with authoritative `ORIENTATION_MECHANISMS_ATLAS_1_21_1.md`
P09/P16–P21/P34–P40 and waves A/B.

## 1. Pairs and multi-cell placement (P17–P21)

### DoorBlock

`DoorBlock.getStateForPlacement` calculates FACING from player
horizontal, hinge from left/right neighboring occupancy and
hit offset; checks physical `pos.above()` and power on
both lower/upper cells. `setPlacedBy` then sets the upper HALF
at another physical cell. `updateShape` compares received
physical neighbor Direction/Y with HALF and copies/deletes the
paired part; `useWithoutItem` and neighbor redstone adjust OPEN
and POWERED.

**One correct lower FACING does not imply paired upper-cell
placement/update correctness.** Need an atomic source-local
upper-neighbor selection plus neighbor's own chart, hinge
left/right handedness near seams and powered/open interaction
preservation. Phase-7A redstone query remains cross-phase.

### BedBlock

`getStateForPlacement` obtains horizontal direction then
uses `blockPos.relative(direction)` to test head cell;
`updateShape` uses `getNeighbourDirection(PART,FACING)`
to find counterpart; user interaction with FOOT redirects to
HEAD. These source checks and actual two-cell placement may
cross a seam and are not solved by simply rotating the stored
FACING. `BedRenderer` is a separate Phase-3 confirmation.

### DoublePlantBlock

Own `getStateForPlacement` tests `pos.above()` and build-height
limit; `setPlacedBy` authors the UPPER half independently
(including waterlogged copy), and `updateShape` checks the
physical Y-axis neighbor. This is not automatically repaired
by `BushBlock.canSurvive`; require source-local upper-cell
placement, support invalidation, double-height waterlogging
and chunk-edge/collision tests.

### ChestBlock

Own `getStateForPlacement` with player direction, sneaking/
secondary-use branch and nearby chest detection, generates
ChestType.SINGLE/LEFT/RIGHT. `updateShape` re-pairs when
neighbor states change and schedules water ticks.
`getConnectedDirection(TYPE,FACING)` computes the physical
neighbor from canonical state values, and a double chest
uses DoubleBlockCombiner / two inventory owners.

EnderChest-specific FACING fix covers **none** of the double
chest pairing or inventory consistency requirements.
The Chest BlockEntityRenderer is owned by Phase 3, not
proof of Phase-2 pair topology.

## 2. Composite state and non-equivalent 2D direction (P09/P16/P18)

### TrapDoorBlock

`getStateForPlacement` branches on whether clicked face axis
is horizontal, whether the block is replacing the hit cell
and local hit height; writes FACING, HALF, OPEN and POWERED.
`neighborChanged` modifies powered/open state and
`updateShape` schedules water ticks. A universal
`HORIZONTAL_FACING` transform alone cannot preserve the
clicked-wall HALF rule or redstone/waterlogging coupling.

### FenceGateBlock

Independent `getStateForPlacement` creates horizontal FACING,
OPEN and POWERED and scans nearby wall neighbors to determine
IN_WALL. `updateShape` depends on neighbor-direction axis
and recomputes IN_WALL from both physical side cells;
`useWithoutItem` uses player facing to open/close, and
`neighborChanged` handles redstone.

This is an interaction + neighbor graph, NOT FenceBlock's
tangent connection implementation.

### CrafterBlock and JigsawBlock

Both write an `EnumProperty<FrontAndTop>` orientation;
Crafter combines nearest-looking vertical and horizontal
directions; Jigsaw uses clicked face and player horizontal
as a second orthogonal axis. Legal `FrontAndTop` pairs
MUST stay orthogonal after body->physical->target conversion.
`rotate`/`mirror` persist composite orientation and
can't be implemented as a single horizontal FACING transform.
Crafter redstone and Jigsaw structural connectors are
separate Phase-7A/9 consumers.

## 3. Interaction-only orientation without block placement (P34)

### ChiseledBookShelfBlock

`getStateForPlacement` uses player horizontal FACING,
but `useItemOn`/`useWithoutItem` select a shelf slot via
`getHitSlot(BlockHitResult,BlockState)` (physical hit
position + state facing). The item-slot hit fraction
must be reinterpreted into the **placed block's** chart;
rotating visible model without hit coordinates would
cause incorrect item insertion. Existing NeoForge
`EnchantmentMenu` bookshelf power patch is a DIFFERENT
algorithm and cannot repair these slots.

### Tool interactions

NeoForge's FlintAndSteel/FireCharge getToolModifiedState
`ItemAbilities.FIRESTARTER_LIGHT` is a mod extension contract,
but target fire placement still uses the PHYSICAL clicked
adjacent cell, with direction parameters which may be
canonical local for fire state/portal orientation.
Campfire/candle lighting, FIRE placement, portal
formation, and redstone interactions must be checked
as distinct *consumers* sharing geometric input
without copying/extinguishing NeoForge extension hooks.

## 4. Non-player world authoring/persistence (P35/P39/P40)

### StructureTemplate

`StructureTemplate.placeInWorld` transforms saved positions
by `StructurePlaceSettings`, then applies
`BlockState.mirror(...).rotate(...)`, writes physical cells,
restores BlockEntity NBT and invokes block neighbor/updateShape
logic. No `BlockPlaceContext` exists. A block orientation
implementation limited to the player's BlockItem path will
never intercept structure placement correctly.

Vanilla `Rotation` is typically horizontal/world-Y;
for 3D six-face planet insertion, a rigid structure
transform must be defined as source-local -> target
physical basis with handedness, not blindly compose
old `Rotation` on every state. This is a joint Phase-2
state vocabulary / Phase-9 structure geometry contract.

### Alternative authors

- `/setblock`, `/fill`, clones and datapack structure placement
  can write serialized properties directly in a physical cell.
- `BlockItemStateProperties` is applied AFTER the vanilla
  placement state's orientation was calculated; explicit
  user-authored BLOCK_STATE overrides need a typed policy.
- Fake-player, dispenser and `DirectionalPlaceContext` do not
  necessarily have a player-body frame; defer only to an explicit
  authoring chart/provenance, not a guessed physical yaw.
- Foreign/virtual levels and mod contraptions can have their own
  source charts; the original physical-coordinate API must not
  rewrite their directions opportunistically.

## 5. Cross-phase integration checklist

| Phase-2 authored state / interaction | Next consumer | Owner |
|---|---|---|
| Canonical chest FACING and LEFT/RIGHT pair | Chest BER, lid hinge, inventory multiblock | Phase 3, 2 |
| Ender Chest FACING | Ender Chest BER orientation | Phase 3 |
| Door/Half/Trapdoor/FenceGate state | Models, powered open/close, navigation | Phase 3, 7A, 6 |
| Bed HEAD/FOOT, placement | BedRenderer, respawn and sleep body frame | Phase 3, 7 |
| Crafter FrontAndTop | Redstone input / powered crafting | Phase 7A |
| Jigsaw FrontAndTop, templates | rigid generated structure transforms | Phase 9 |
| Chiseled bookshelf hit slot | inventory BE and client/server picking | Phase 2, 7B |
| fire tool useOn, clicked face | fire, portal plane, flame origin | Phase 2, 9, 4 |
| structure/clone BlockState | renderer, physics and neighbor updates | Phase 3, 2, 9 |
| waterlogged paired states | fluid tick & flow and fluid rendering | Phase 5 |

## 6. Disposition / next step

Stage 3 MUST use the actual NeoForge registry + implementation owner
census to enumerate all sibling IDs, override paths and modded
extension points of these algorithms. Map P09/P16–P21/P34–P40
to shared frame helpers and current Mixins without assuming
that correct basic player-facing behavior proves interaction
or structure correctness.

No code is changed in this research stage; no extra
manual gameplay testing requested. The initial 20-station
fixture covers only a subset of these gates and must be
expanded by *mechanism*, not arbitrary block ID.
