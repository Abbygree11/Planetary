# Research: NeoForge 1.21.1 sided capabilities in Planet frames

Status: physical-Planet runtime boundary implemented; cache runtime smoke test
still required.

## 1. NeoForge contract

BlockCapability queries identify:
- the physical Level;
- the physical BlockPos being queried;
- optional already-known BlockState / BlockEntity;
- an additional context.

For createSided capabilities the context is nullable Direction. NeoForge
documentation uses this Direction as the side from which the target block is
queried.

Providers receive the same target block identity plus context.

BlockCapabilityCache stores:
- physical Level;
- physical target BlockPos;
- query context;
and re-runs the capability query when its cached value is needed.

Capability invalidation is registered by physical BlockPos.

## 2. Planet physical-world rule

The physical queried BlockPos is authoritative.

DO NOT move/reroute a physical Planet capability query to another BlockPos just
because the Direction has local semantics. Doing so would violate NeoForge's
target-block contract for direct queries and cached queries.

Instead:
1. keep Level unchanged;
2. keep queried physical BlockPos unchanged;
3. keep supplied BlockState / BlockEntity unchanged;
4. when context is a non-null Direction, resolve the target's canonical
   PlanetBlockStateFrame;
5. convert PHYSICAL context side -> target canonical LOCAL side;
6. dispatch providers once with that canonical local side.

Null context remains null.

On POS_Y the conversion is identity.

## 3. Why this helps mod compatibility

Providers for sided item/fluid/energy/custom capabilities commonly switch on
Direction to choose input/output/storage faces.

Planet BlockState Direction values are canonical LOCAL semantics. Passing the
provider a canonical-local side therefore keeps capability side logic aligned
with FACING/AXIS/state semantics on all six gravity faces.

This applies generically to standard NeoForge capabilities without knowing the
pipe/cable/machine mod.

## 4. Important limitation: target-position math is upstream

A capability boundary cannot safely reinterpret the physical BlockPos supplied
by the caller.

If a third-party block performs:
    target = source.relative(localFacing)
and treats localFacing as Planet-local even though BlockPos.relative is physical
XYZ, it may select the wrong physical block BEFORE the capability query.

The BlockCapability boundary must not guess that a different target was
intended. That direct world-axis math belongs to compatibility class C in the
master gravity-impact audit and needs either:
- a safe higher-level neighbor adapter boundary; or
- an integration adapter for that mod/subsystem.

This is preferable to globally patching BlockPos.relative, which would corrupt
genuinely physical callers.

## 5. Legacy virtual atlas

The older PlanetLevelBridge / PlanetWorldAccess prototype represents one
logical block through canonical/guard-space virtual BlockPos aliases.

For that model only, PlanetSidedQueryFrame may canonicalize an alias to another
vanilla BlockPos representing the SAME logical block.

That fallback remains temporarily because legacy tests and storage code still
exist.

When it redirects an alias, BlockCapabilityMixin must fetch BlockState and
BlockEntity for the canonical target instead of forwarding objects supplied for
the alias.

The dedicated Planet world uses ordinary physical BlockPos and does not use
this alias path.

## 6. BlockCapabilityCache consequences

Physical Planet:
- cache position remains unchanged;
- invalidation position remains unchanged;
- cache context can stay physical because every actual provider lookup passes
  through the same capability boundary and is reframed there.

Therefore no physical-world position canonicalization is required in
ServerLevel.invalidateCapabilities/registerCapabilityListener.

The existing ServerLevel virtual-atlas canonicalization remains legacy-only.

## 7. Shared runtime resolver

PlanetBlockRuntime is the shared block-frame runtime boundary:
- fieldAt(level, pos);
- stateFrameAt(level, pos);
- traversalAt(level, pos[, preferredFace]);
- supportQuery(level, pos, localDirection);
- physicalSideToLocal;
- localSideToPhysical.

It resolves activation at the physical block CENTER via
PlanetGravityRuntime.findAt.

Future runtime mixins and compat modules should use this instead of each
selecting their own field/face policy.

## 8. Runtime acceptance probe

PlanetCapabilityDiagnostics registers a private internal sided capability on
STONE. Its provider returns the Direction context it actually receives.

On dedicated Planet player login the probe:
- checks all 6 canonical faces x all 6 physical Direction values = 36 real
  ServerLevel.getCapability -> BlockCapability mixin -> provider dispatches;
- compares every provider result with PlanetBlockRuntime.physicalSideToLocal;
- creates a real BlockCapabilityCache at a POS_X stone block;
- performs the first cached lookup;
- calls ServerLevel.invalidateCapabilities on the SAME physical target BlockPos;
- requires exactly one invalidation-listener notification;
- queries the cache again and requires the same canonical-local provider side.

Probe blocks are the six STONE cells one block outward from the core, so all
positions stay in the central loaded chunk area and do not depend on surface
render/simulation distance.

A successful login prints:
[Planetary] Capability probe passed: 36 generic side mappings,
36 standard item/fluid/energy checks, 1 cache invalidation.

The custom side-echo probe is diagnostic-only. The capability id is
planetary:internal_side_echo.

The same diagnostic class also registers providers for the three standard
NeoForge 1.21.1 block capabilities on STONE:
- Capabilities.ItemHandler.BLOCK -> ItemStackHandler;
- Capabilities.FluidHandler.BLOCK -> FluidTank;
- Capabilities.EnergyStorage.BLOCK -> EnergyStorage.

These providers deliberately expose their handler ONLY on canonical local UP.
For each of the six gravity faces runtime acceptance:
1. computes the physical world side corresponding to canonical local UP;
2. queries each standard capability from that physical side and requires the
   exact registered handler;
3. queries from the opposite physical side (canonical local DOWN) and requires
   null.

That is 6 faces x 3 capability kinds x 2 side outcomes = 36 standard capability
checks through the real NeoForge provider dispatcher.

The diagnostic handlers are isolated test objects; they do not store gameplay
data used by the Planet world.

## 9. Acceptance still required

Pure tests:
- runtime field activation uses block center;
- canonical state frame at corner;
- side round-trip on six faces;
- traversal preferred chart remains separate from state frame;
- runtime support query uses the same field.

Runtime:
- no BlockCapability mixin application error;
- diagnostic provider completes 36 physical->local side mappings;
- standard ItemHandler/FluidHandler/EnergyStorage providers each accept
  physical-local-UP and reject physical-local-DOWN on all six gravity faces;
- BlockCapabilityCache keeps the physical target and survives invalidation;
- exact edge/corner target BlockPos is not silently moved;
- one third-party pipe/machine stress case.
