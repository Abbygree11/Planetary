# Research: local multi-block placement families in Minecraft 1.21.1

Status: first pressure-plate / door / bed / pointed-dripstone placement adapters
implemented; runtime acceptance pending.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Pressure plates

BasePressurePlateBlock has three independent global-Y assumptions:
- canSurvive uses pos.below();
- updateShape only reacts to physical Direction.DOWN;
- pressure activation uses a thin TOUCH_AABB at local y=[0,0.25] but expressed
  directly in world coordinates.

Planet policy:
- support is canonical local DOWN through PlanetBlockSupportQuery;
- rigid and center support queries use the support block's canonical local side;
- updateShape identifies the actual physical support by supportPos;
- the local trigger AABB is rotated about block center into physical world
  coordinates before the entity query.

Both normal and weighted plates use the rotated trigger box.

Still deferred to Phase 7A:
- direct redstone signal side currently compares Direction.UP;
- updateNeighbours still explicitly notifies physical below.

## 2. DoorBlock

Vanilla placement assumes:
- upper half = pos.above();
- FACING = context.getHorizontalDirection();
- hinge neighbors use physical relative(left/right);
- hinge click halves use world click X/Z;
- survival uses below();
- updateShape interprets physical Y as the half-pair axis.

Planet policy:
- lower FACING uses PlanetFrameApi.localHorizontalDirection;
- upper half is local UP through PlanetBlockNeighborQuery;
- upper FACING is reframed source-local -> physical -> target-canonical-local;
- hinge lower/upper left/right positions use local topology;
- hinge click coordinates use PlanetBlockPlacementFrame.localHitOffset X/Z;
- lower support is local DOWN;
- pair update compares physical neighborPos with resolved local UP/DOWN pair.

This preserves the physical door axis even if the upper cell selects a different
canonical frame.

Door redstone neighborChanged remains Phase 7A because signal-side semantics
must be solved globally, not per block.

## 3. BedBlock

Vanilla assumes:
- FACING comes from global horizontal direction;
- head = foot.relative(FACING);
- updateShape compares a physical Direction directly with local FACING;
- setPlacedBy copies the same FACING enum into the head.

Planet policy:
- foot FACING is target-canonical local horizontal;
- head position is a seam-aware local FACING neighbor;
- head FACING is reframed into the head block's own canonical frame so the
  physical bed axis remains continuous;
- pair update finds the counterpart by local FACING/opposite and physical
  neighborPos.

Not solved in this pass:
- BedBlockEntityRenderer local-frame transform;
- use/sleep head lookup;
- stand-up/dismount offsets;
- bounce world-Y velocity;
- creative pair-destruction helper.

Therefore this patch fixes placement topology/state pairing, not the complete
bed gameplay/render subsystem.

## 4. PointedDripstoneBlock

Vanilla manual placement assumes:
- getNearestLookingVerticalDirection() is global Y;
- support uses pos.relative(TIP_DIRECTION.opposite);
- thickness uses raw relative(UP/DOWN);
- updateShape only reacts to physical UP/DOWN.

Planet policy:
- initial vertical look is taken from local nearest-looking order;
- TIP_DIRECTION remains canonical local UP/DOWN;
- support and both vertical neighbors use PlanetBlockNeighborQuery;
- when a neighbor is in another canonical frame, a source local direction is
  reframed through physical world into the target frame before comparing
  TIP_DIRECTION;
- thickness TIP/TIP_MERGE/FRUSTUM/MIDDLE/BASE follows the same vanilla decision
  tree over those local neighbors;
- updateShape matches actual physical neighborPos against local UP/DOWN and
  preserves vanilla falling-tick delays.

This pass deliberately does NOT touch the larger dripstone vertical scanning
system:
- natural growth;
- root/tip scans;
- cauldron search;
- fluid transfer;
- drip particle origin;
- stalactite-chain falling scan.

Those raw world-Y loops must be converted together later.

## 5. Shared helper

PlanetPlacementRuntime now provides:
- seam-aware local neighbor BlockPos lookup;
- source-local direction -> target-canonical direction reframing;
- local vertical look selection;
- local door hinge computation;
- local AABB -> physical AABB rotation.

The direction-reframe operation is the key multi-block invariant:
the enum may change across a seam, but its physical world direction must not.

## 6. Build-height/world-border policy

Unlike vanilla horizontal bed placement or world-UP door placement, a local
tangent/UP direction on side faces can change physical Y.

Second-half placement therefore validates the actual target physical Y against
build height. Bed head additionally preserves vanilla world-border validation.

## 7. Tests

PlanetPlacementRuntimeTest verifies an exact canonical-frame seam:
- source and target canonical faces differ;
- a source-local direction is reframed to the target;
- both local enums map to the exact same physical Direction.

Existing block-neighbor/support tests cover physical adjacency and target-side
resolution.

## 8. Acceptance

Pressure plate:
- place on local floor on all six faces;
- standing on it changes its pressed state;
- removing local support removes it.

Door:
- place on side/bottom faces;
- upper half extends local UP;
- FACING/hinge look coherent;
- removing local floor or either half tears down the pair through updates.

Bed:
- FOOT/HEAD occupy consecutive local-FACING cells;
- F3 FACING can differ across a seam while physical axis stays continuous;
- visual BER may still be wrong until the BER phase.

Pointed dripstone:
- place stalactite from local ceiling and stalagmite from local floor;
- extend a chain;
- thickness updates after adding/removing adjacent local-vertical pieces;
- support loss schedules the existing local falling behavior.
