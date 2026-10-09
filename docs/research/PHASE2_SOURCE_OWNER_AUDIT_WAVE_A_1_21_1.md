# Phase 2 research, wave A: placement authoring / alternate creation owners

Target: Minecraft 1.21.1; NeoForge 21.1.215; branch `2.0`.
Status: **source-path audit of the common authoring pipeline; runtime
registry census CODE IMPLEMENTED / CI ACCEPTANCE PENDING**.
This report is one bounded research artifact. It does NOT mean the
whole Phase 2 source/override audit is finished, or that natural
placement is accepted in the six-face world.

Canonical master inventory:
`ORIENTATION_MECHANISMS_ATLAS_1_21_1.md` P01–P40.
Source file census: 293 block package files, 121 item package files.
Previous files were scanned for source patterns (53 block representative
methods spot-checked), not exhaustively resolved against compiled
NeoForge registries or inherited override owners.

## A. Evidence and source version provenance

Readable Mojang-named reference source:
`hackersense/OptiFine-Source` `main`,
`1.21.1/net/minecraft/world/item/...`.
These are comparative sources, NOT direct proof of what
NeoForge's 21.1.215 transformer runs.

Official NeoForge patch evidence retrieved from
`neoforged/NeoForge` historical commit
`3c1fcb4f4efd6ed0cf60375e8094902426ddc973`:
- `patches/net/minecraft/world/item/BlockItem.java.patch`;
- `patches/net/minecraft/world/item/FlintAndSteelItem.java.patch`;
- `patches/net/minecraft/world/item/FireChargeItem.java.patch`.

The repository does NOT expose Git ref `21.1.215` by that name,
so these historical patches must be treated as comparative until
the new JUnit compiled class-owner census matches the actual
21.1.215 ModDevGradle dependency. Do not claim tag-specific
evidence where no such tag was obtained.

## B. Physical source of truth versus semantic authored state

1. `UseOnContext` / `BlockPlaceContext`: the hit
   `BlockHitResult` is PHYSICAL. Its clicked `BlockPos`, hit
   normal `Direction`, and `Vec3` hit point MUST NOT be
   globally reframed. The relative target cell is selected through
   physical `relative(clickedNormal)`; replacement can keep
   the original clicked cell.
2. `BlockPlaceContext.getNearestLookingDirections` derives
   player-body order, but changes ordering if the hit cell is
   not replaceable (the physical opposite-hit face is moved
   to first priority). A globally converted Direction array
   could silently change WHICH physical adjacent cell is chosen.
   Distinguish **selection of physical target cell** from
   **authorship of a canonical-local state property**.
3. `DirectionalPlaceContext` overrides look ordering,
   horizontal direction and rotation from a synthetic direction
   (dispenser/autoplacement). It may have no real Player.
   `PlanetFrameApi.localHorizontalDirection(context)` cannot
   be universally invoked on all contexts without tracing
   target frame and provenance; avoid NPE or body-frame guess.
4. Stored `BlockState` properties are canonical LOCAL;
   e.g. FACING, AXIS, HALF, ATTACH_FACE, ROTATION_16, FrontAndTop.
   Source item automation may author a state without player
   context; these paths require explicit semantic provenance.

## C. The eleven distinct stages of the ordinary BlockItem path

From comparative `BlockItem.java`:
1. `BlockItem.useOn(new BlockPlaceContext(useOnContext))`;
2. `BlockItem.place` checks enabled features and `canPlace`;
3. `updatePlacementContext` can replace the target context
   or return null (especially `ScaffoldingBlockItem`);
4. `getPlacementState` delegates to
   `block.getStateForPlacement(context)` but can itself
   be overridden (`StandingAndWallBlockItem`);
5. `canPlace` checks survival and physical unobstructed
   collision; does NOT follow automatically from FACING;
6. `placeBlock` writes the PHYSICAL cell and triggers
   neighbor updates;
7. `updateBlockStateFromTag` applies
   `DataComponents.BLOCK_STATE` via
   `BlockItemStateProperties.apply`, AFTER authored placement;
8. block entity data/components are updated on physical pos;
9. `setPlacedBy` may create second/third cells or secondary state;
10. sound, GameEvent, item consume and player criteria remain
    original vanilla/NeoForge;
11. later `updateShape/neighborChanged/tick` can transform
    or remove the state.

**Research conclusion:** patching only
`Block.getStateForPlacement` can never cover the late
`DataComponents.BLOCK_STATE` overwrite, an overridden
`BlockItem.getPlacementState`, null-player/synthetic context,
or direct structure/command writes.

## D. Non-equivalent Item subclasses

| Owner | Path | Full-family audit requirement |
|---|---|---|
| `StandingAndWallBlockItem` | chooses between standing and wall block types by ordered context direction; checks each candidate's `canSurvive` | preserve physical hit and priority, reframe the attachment semantics, audit sign/banner/skull subclasses |
| `HangingSignItem` | overrides `canPlace` for hanging wall sign extra support | cannot assume generic StandingAndWall support is enough |
| `ScaffoldingBlockItem` | overrides `updatePlacementContext`, physically traverses up to 7 positions; branches on `clickedFace == UP` and body direction | model local-UP authored stacking versus world-physical target walk; physical positions remain real |
| `PlaceOnWaterBlockItem` | `use` performs fluid hit then uses `hitBlockPos.above()` as physical new target | owns a Phase-5 fluid-local surface integration, must not be "fixed" by generic block placement |
| `BedItem`, `DoubleHighBlockItem` | may author paired cells after item placement | require geometry and survival tests at both target physical positions |
| `FlintAndSteelItem` | `useOn`: hit normal -> physical adjacent cell; fire-state selection and portal detection | fire/portal result depends on multiple Phase-2/9 mechanisms, not FACING only |
| `FireChargeItem` | separate `useOn` and dispenser variant | cannot assume flint-and-steel patch covers this method |
| `BoneMealItem`, `HoeItem`, `ShovelItem` | tool interactions/growth/blockstate conversion | their target/neighbor semantics must be classified before Phase-2 ecology acceptance |
| `ItemFrameItem`, `HangingEntityItem`, `ArmorStandItem`, `EndCrystalItem` | spawn entities with attachment orientation | Phase-7 body/entity rendering interface, not generally BlockItem |
| `BlockItemStateProperties` | post-placement state patch | audit explicit-authoring policy; do not blindly rotate every state-component value |

## E. NeoForge-specific extension points

Historical NeoForge `BlockItem.java.patch` adds
`BlockState.getSoundType(level,pos,player)` and
`BlockItem.getPlaceSound(state,level,pos,player)`.
Any future replacement of the placement pipeline must preserve
the exact original sound/event/criterion path; do not copy
the whole vanilla BlockItem.place without its NeoForge hooks.

Historical `FlintAndSteelItem.java.patch` and
`FireChargeItem.java.patch` replace the vanilla fixed
`Campfire/Candle/CandleCake.canLight` dispatch with
`state.getToolModifiedState(context, ItemAbilities.FIRESTARTER_LIGHT,
false)`. This enables modded blocks/tools. Changing the
tool's interaction to a copied vanilla switch would break NeoForge
compatibility. Apply local-frame adaptation strictly to the
geometric orientation/physical candidate/portal path after
correctly maintaining this ToolAction dispatch.

## F. Gap against current Planetary 2.0 source

Existing reusable foundation already includes
`PlanetFrameApi`, `PlanetBlockPlacementFrame`,
`PlanetDirectionalPlacement`, semantic `PlanetBlockSupportRuntime`
and thin runtime Mixins for rotated pillar, hopper, slabs, face-
attached support, End Rod and Ender Chest. Their existence is
**not** all-owner coverage.

Known holes before a new architecture wave:
- No proven single adaptation contract for all
  `BlockPlaceContext` plus specialized BlockItem source paths.
- Synthetic `DirectionalPlaceContext`, `updatePlacementContext`
  target moves, `getPlacementState` overrides and the after-
  placement `BLOCK_STATE` component remain unaccepted.
- No complete FACING/HORIZONTAL_FACING/AXIS/ROTATION_16/FrontAndTop
  implementation-owner count from actual registered blocks.
- No exhaustive handling of subclass overrides of
  `getStateForPlacement`, `canSurvive`, `updateShape`,
  `neighborChanged`, `rotate/mirror`, `setPlacedBy`.
- No complete family-wide alternate authoring test for
  `/setblock`, structure template, dispenser or fake player.
- Correct rendered BER orientation belongs to Phase 3 even if
  BlockState is canonical and placement works.

## G. Executable actual-registry census, stage-2 engineering artifact

Added `src/test/java/dev/planetary/world/
Phase2RegistryOrientationCensusTest.java` (JUnit)
and GitHub Actions `phase2-neo1211-registry-census` artifact.

Unlike the existing 293-class/121-item source-name TSVs, this test
boots the actual ModDevGradle dependency via
`Bootstrap.bootStrap()` and iterates
`BuiltInRegistries.BLOCK` and `BuiltInRegistries.ITEM`.
It exports:
- `phase2-neo1211-block-registry.tsv`: every registered BLOCK ID,
  real class, tentative orientation-name flag, method owner chain;
- `phase2-neo1211-state-properties.tsv`: every registered property,
  property implementation class, all legal values;
- `phase2-neo1211-item-registry.tsv`: every ITEM ID, actual
  BlockItem linkage and creation/interactions owner chain;
- `phase2-neo1211-summary.txt`: counts and review status.

**All orientation candidates are deliberately marked
`REVIEW_PENDING`.** Property-name recognition alone is not
semantic classification; inherited methods must be validated
against the owner in the actual NeoForge bytecode. No Phase-2
acceptance will be inferred from passing this census alone.

The census has minimum registry sanity assertions and emits
reports as build artifacts. This is the FIRST verification gate,
not the FINAL fail-unknown-family CI gate: that stricter enforcement
can only be enabled once every actual owner and bypass has a
reviewed disposition from atlas P01–P40. Do not mistake the
initially reported REVIEW_PENDING rows for passing coverage.

Next stage to research: support/update/connection graphs and
full owner/override semantic classification using these
actual-registry results, then a single family-wide architecture
and implementation plan. Preserve already accepted particle motion.

## H. Acceptance

For this research+diagnostics package:
1. GitHub Actions compileJava/compileTestJava and JUnit PASS;
2. Inspect actual report block/item/property counts and candidates;
3. Map every unique orientation-sensitive owner to atlas P01–P40,
   with explicit no-op/future-phase/nonapplicable dispositions;
4. Create the unknown-owner fail gate, then implement wave A whole
   families, not isolated block fixes;
5. Only after implementation CI green should a user run
   the single six-face/edge fixture gameplay check.

No user local command or client startup is required to accept a
research-only census. No new runtime Mixins were added in this stage.
