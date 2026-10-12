# Planetary — 2.4A-2 typed property-domain semantics (Minecraft 1.21.1 / NeoForge 21.1.215)

Date: 2026-10-13. Branch `2.0`. **Semantic API implementation only**; green compile/JUnit and actual placement gameplay are independently tracked.

## Scope / actual implementation

Added `PlanetOrientationProperties` as an **opt-in typed API** on top of `PlanetPlacementResolver.Resolution`. It never mutates generic `Level.setBlock`, `BlockState.rotate`, `BlockPlaceContext`, `UseOnContext`, physical `Direction` or a mod's own polymorphic overrides. It does not create or apply a new Mixin.

- `facing`: explicit physical directional input -> target canonical six-way local `Direction`.
- `horizontalFacing`: four-way only, rejects an unrecoverable vertical result rather than inventing an axis.
- `axis`: physical axis -> target canonical `Direction.Axis`, retaining sign-independent AXIS_3 semantics.
- `halfFromClick`: stair-like top/bottom result from *target-local clicked normal and hit offset*; does **not** stand in for `SLAB_TYPE=DOUBLE`, `DOUBLE_BLOCK_HALF`, `canBeReplaced` or special owner algorithms.
- `attachToSupport`: **physical target→support** direction -> floor/wall/ceiling, deliberately *not* a flipped hit face. `FaceAttachedHorizontalDirectionalBlock.getStateForPlacement` derives attachment from nearest-looking directions, not simply from clicked normal.
- `frontAndTop`: source-supplied physical front/top pair -> one of the actual 12 `FrontAndTop` enum states; unsupported but orthogonal pairs return empty.
- `tangentRotation16`: physical heading projected to target tangent plane, explicit `NORTH_CLOCKWISE` or `SOUTH_CLOCKWISE` zero; 16 legal discrete segments. Vertical or nonfinite headings are rejected as unrepresentable. **Owner-specific player yaw adjustments and sign/skull/ceiling-hanging-sign inheritance remain callback-owned**: no universal property rewrite.
- `withProperty`: explicitly selected `Property<T>` / `BlockState` update only for a Planet target and a legal value already supported by that **concrete** state; preserve unrelated vanilla/mod property values. No implicit third-party adaptation.

Relevant exact-version research: [C16](PHASE2_STAGE3C16_FRONT_AND_TOP_ROTATION16_STATE_TRANSFORMS_NEOFORGE_ASM_1_21_1.md) checks 12-valued `FrontAndTop`, 16-segment author classes, rotate/mirror and world-aware NeoForge alternative. Comparative pinned 1.21.1 source: `StandingSignBlock.getStateForPlacement` passes actor yaw **+180** to `RotationSegment.convertToSegment`; `SkullBlock` passes raw yaw; `CeilingHangingSignBlock` sometimes inherits orientation from neighboring signs. Source is NOT proof of universal mapping or applied Mixin. Real Create MechanicalCrafter uses independent `POINTING`, kinetic block `AXIS` can prefer neighbors, so local axis conversion alone must not stomp its algorithm.

## Ownership boundaries

| Category | Behavior | Decision |
| --- | --- | --- |
| SHARED_SAFE | Canonical target frame, direction/axis/attachment/half/12-pair/16-tangent domains, legal property writes | Stable Planet helper API implemented |
| OPT_IN_ALGORITHM | Real declaring-owner methods, actual item context, owner-specific 16-way yaw, replacement/stack/neighbor inheritance | Not auto-patched; require audited 1.21.1 adapter and exact ABI before gameplay claims |
| MOD_INDEPENDENT | Mod custom `POINTING`, `TrackShape`, Create neighbor-selected AXIS, structure writers | Never coerce automatically; intentional opt-in and loaded-mod proof later |
| PHYSICAL_DO_NOT_TOUCH | Hit and world positions, ray/BlockPos.relative, target neighbor support, world transforms, NeoForge rotate extension callbacks | Unmodified |

**Depth trade-off:** blanket state-property mutation in `Level.setBlock` is deeper but demonstrably unsafe for physical algorithms and mod metadata; patching one individual block is shallower and misses other class owners. This API accepts a chosen semantic *owner* and *domain* without rewriting unrelated consumers.

## Automated gates

New `PlanetOrientationPropertiesTest`: six faces × six directions, AXIS_3, half and attach, all 12 real FrontAndTop states per face, all 16 rotation segments per face with two zero policies, 12 cube edges and eight corners (canonical target invariant), unrelated-state preservation, legal property checks, vanilla and foreign fallback. **Runtime dispatch, applied Mixins, player item placement and real mod load are NOT covered by these pure tests**.

Required later real checks: +Y and five other faces, 12 edges, eight corners, normal hit vs replacement, actual player click and no-player dispenser/structure, full matched NeoForge 21.1.215 ABI/loaded mod, post-placement item components, neighbor survival, ROTATION_16 visuals + BER, rotate/mirror order, no-Planet baseline and save/reload. Source family [2.4A card](../phases/phase-02/05-implementation-a.md).

**Next:** audited author adapters for actual BlockItem/BlockState declaring owners; do NOT equate typed helper with gameplay implementation or mark Phase2 accepted.
