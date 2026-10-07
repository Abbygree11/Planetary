# NeoForge BlockCapability Direction-provider contract — 1.21.1

Status: IMPLEMENTED IN 2.0, AUTOMATED BUILD/UNIT + GAME ACCEPTANCE PENDING.
Source revision: NeoForged/NeoForge branch 1.21.1, BlockCapability.java and
RegisterCapabilitiesEvent.java; Planetary's own BlockCapabilityMixin and
PlanetCapabilityDiagnostics. This is a bounded Phase 1 + 7A.5 + 10 change.

## Root cause

Planet's old @Mixin(BlockCapability.class) intercepts the internal
getCapability(Level, BlockPos, BlockState?, BlockEntity?, context) method.
For every Direction in a Planet region it passed
PlanetBlockRuntime.physicalSideToLocal(...) to ALL registered providers.

NeoForge's actual BlockCapability.getCapability implementation:
1. resolves BlockState and BlockEntity when they are null;
2. chooses the provider LIST using state.getBlock();
3. runs each registered IBlockCapabilityProvider in order;
4. returns the first non-null result.

The public RegisterCapabilitiesEvent.registerBlock and
registerBlockEntity methods register providers, not a universal side
interpretation. BlockCapabilityCache owns the physical query and stores its
context for later cache lookups/invalidation.

Thus a blanket argument rewrite before the provider LIST changes the context
of unrelated mods. Prior Stone tests registered locally authored providers
that all expected local UP, so they did not establish compatibility for
foreign providers expecting physical sides (e.g., direction-indexed ports).
The risk was known from the cross-mod audit; no in-game mod failure was
independently reproduced.

Exact relevant source:
https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/capabilities/BlockCapability.java
https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent.java
https://docs.neoforged.net/docs/1.21.1/inventories/capabilities/

## Implemented seam

- For bound PHYSICAL Planet Level, BlockCapabilityMixin returns without
  changing ANY Direction, BlockPos, state, block entity or cache context.
- Old virtual-atlas-only routing stays guarded in BlockCapabilityMixin for
  legacy test/prototype Levels (not on a PlanetGravityRuntime-bound Level).
- Public PlanetCapabilityAdapters.canonicalLocalBlock(provider) is a
  per-PROVIDER wrapper used in RegisterCapabilitiesEvent.registerBlock.
  It converts physical entry Direction to the canonical BLOCK frame only
  in an active Planet region and otherwise preserves original physical
  context, including null.
- Public PlanetCapabilityAdapters.canonicalLocalBlockEntity(provider) is
  the equivalent for registerBlockEntity; unattached BE -> passthrough.
- Multiple providers registered for the SAME capability/block can now
  independently choose their side conventions. No global per-mod heuristic,
  mutable capability registry, hidden thread-local coordinate mode, or
  automatic third-party semantic guessing is required.
- Queried physical target selection is a separate upstream problem.
  These adapters never change target BlockPos or neighbor graph.
- The wrapped provider itself still participates in NeoForge's normal
  provider ordering, cache invalidation and Block/BE registration.

The old Stone local-side echo plus local-UP-only item/fluid/energy providers
are now individually wrapped at registration. A separate unwrapped
PHYSICAL_SIDE_ECHO provider is registered on the same Stone block, proving
the old blanket behavior is gone. Existing local regression coverage is
preserved, not silently deleted.

## Extra coordinate provenance boundary

PlanetCoordinateContext.PhysicalBlock asserts a real Level/BlockPos pair;
PlanetCoordinateContext.ForeignBlock requires a named external space ID.
PlanetFrameApi overloads accept only PhysicalBlock and fail closed for
ForeignBlock. Numeric coordinate equality never implicitly transforms a
schematic/contraption into physical world position. Adapter verification
of real physical Level provenance and conversion for Create/WorldEdit/Valkyrien
Skies remains a later explicit integration task; do not claim automatic
support for wrapping Levels or any foreign CoordinateSpace.

## Deterministic tests added

- PlanetCapabilityAdaptersTest: six faces × six physical sides,
  opted-in local vs unwrapped physical provider, exact three-face corner,
  null side, unbound Level passthrough, unattached vs attached BlockEntity.
- PlanetCoordinateContextTest: same numeric physical and foreign BlockPos;
  only explicit physical coordinate context accesses PlanetFrameApi.
- Runtime login PlanetCapabilityDiagnostics: 36 local echo mappings,
  36 independent physical echo mappings, 36 local-UP standard
  item/fluid/energy probes, BlockCapabilityCache invalidation and reread.
- No assertion that real Mekanism/AE2/Create or arbitrary mod-provider
  hookups passed yet. No claim about exact no-mod multiplayer behavior
  until tests/build and in-game acceptance.

## Limitations and next steps

- BlockCapability#getCapability is an internal NeoForge method and the
  legacy mixin is still a version-dependent portability hotspot. A future
  cleanup could remove the virtual-atlas fallback once old tests do not
  need it; DO NOT remove now without baseline.
- Unlike universal implicit rewriting, explicit opt-in requires authors of
  canonical-local providers to use the wrapper. Unknown modded providers
  retain vanilla PHYSICAL context and will NOT automatically be made
  gravity-local by this change. This intentional conservatism reduces
  conflicts at the price of eventual per-family adaptation.
- A future mod-specific adapter may need custom orientation/port policy
  for machine state and its NBT; do not blindly wrap a mod's provider when
  it expects physical saved Direction indices.
- The foreign-space API currently refuses unresolved virtual positions;
  it does not contain a magic projection for arbitrarily moving worlds.
- Check input state or captured BlockEntity only as needed by NeoForge;
  no extra chunk/block fetch is performed by the wrapper.
- Source Level queries on non-Planet worlds and vanilla +Y must remain
  byte-for-byte side-equivalent to the passed physical Direction.

## Manual acceptance matrix after build/startup (pending)

1. Start dedicated Planet and log in without crash/mixin registration errors.
   Expect no BlockCapabilityMixin reentry recursion.
2. Confirm chat probe reports 36 explicit local, 36 physical passthrough,
   36 standard item/fluid/energy checks and exactly one invalidation.
3. On +Y, side probes are vanilla-equivalent.
4. On ±X/±Z/-Y, wrapped local provider interprets local UP as expected.
5. On seams and corners, wrapped local provider uses TARGET canonical
   block frame, not traversal chart from another block.
6. Test side-dependent third-party machine/pipe with unwrapped provider:
   physical ports must not unexpectedly rotate.
7. Test vanilla inventory/hopper/furnace/dispenser IO on rotated faces
   and old supported cases. Any regression gets its own owning family
   adapter; DO NOT restore global rewrite.
8. Test normal Overworld unaffected and BlockCapabilityCache listener
   invalidation/relookup unchanged.
9. Check after world reload and when crossing activation radius.
10. No gameplay PASS before user's confirmation or complete deterministic
    integration coverage.

For runtime verification after pull: git pull && .\test.ps1 && .\run-client.ps1
