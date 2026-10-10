# Stage 3A-16.3 — chorus source-local branching, physical six-connection pipe and seam acceptance contract

**2026-10-11 | Planetary branch 2.0 | Minecraft 1.21.1 NeoForge 21.1.215 | ALL 36 CH16 future fixtures NOT RUN.** No Java/Mixin, patched NeoForge ASM or actual gameplay acceptance. [Full chorus original owner/item/feature audit](PHASE2_STAGE3A_CHORUS_GROWTH_PIPEBLOCK_ITEM_WORLDGEN_ORIGINAL_AUDIT_1_21_1.md) · [Permanent deepest SAFE mechanism cross-mod compatibility policy](PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md).

## Physical, semantic and third-party contracts

- One physical `BlockPos` at seam/corner, physical XZ chunk storage, `PlanetBlockStateFrame` canonical per source/target block and `PlanetBlockFrameContext` transient transported traversal chart. Planet semantic six canonical UP faces: POS_Y=+Y, NEG_Y=−Y, POS_X=+X, NEG_X=−X, POS_Z=+Z, NEG_Z=−Z.
- A Chorus **branch-growing** source uses vanilla `above()` and four `Direction.Plane.HORIZONTAL` directions with `below()`/End Stone/radius in XZ. To support a cubic Planet surface coherently, the *specific growth algorithm* may opt in to source-local UP and tangent branching with seam-aware chart. But **a physical graph callback** from NeighborUpdater to `ChorusPlantBlock.updateShape` already denotes a physical adjacent BlockPos and must NOT be blindly remapped on all modded blocks. Generator's world XZ bounds + above offsets are a different feature-space algorithm: adapting just placement won't fix `growTreeRecursive`.
- `ChorusPlantBlock` inherits `PipeBlock` six boolean property→64 cached VoxelShape geometry, **overrides** placement/survival/updateShape. PipeBlock's physical bitmask is safe to reuse for literal physically connected arms, but survival requires END_STONE/one-horizontal-plant and up/below exclusion conditions. For modded PipeBlock descendants, unknown method semantic policy defaults PHYSICAL, not local.
- `ChorusFlowerBlock` is a separate non-PipeBlock with AGE0..5, delayed tick support invalidation, projectile permissions, server client event 1033/1034 and recursive growth, all independent from a generic pipe 6-flag graph.
- Source/target actual physical legal positions and states must be recorded at each mutation. No hypothetical 'one virtual block per face'. No global rewrite `Level.setBlock`, `BlockPos.relative`, `Direction.UP`, `Direction.Plane.HORIZONTAL`, `Level.getFluidState`, all `NeighborUpdater` callback directions or third-party network side contexts.
- Deepest safe adaptation hierarchy: **engine dispatcher physical provenance (preserve args) → stable typed `PlanetMechanismContext` helper with real source/target, canonical frames and declared operation intent → opt-in growth/attachment/graph algorithm family → independently dispatched mod override or confirmed optional mod adapter if needed**. Geometry/render Phase3; block+graph Phase2; tick/events Phase7A; feature/terrain Phase8/9; modded integration Phase10. Runtime design only, no code.

## Mandatory future fixture metadata

Exact git commit, Minecraft/NeoForge/mod build and loaded class, applied Mixin/bytecode evidence separately, vanilla/no-Planet or Planet field; physical source/target/support BlockPos and chunk, `PlanetFace` canonical both, entry traversal chart if any, actual callback physical direction, requested local semantic direction IF explicitly opted in, original owner method, BlockState AGE and six properties, random seed and tick order, reader/writer origin (item/feature/tick/command/mod), before/after geometry, actual held item stack, event ID and sound, worldgen radius/height, client server observation and expected vs actual. **No fixture below has been executed.**

## All 36 proposed deterministic future tests — NOT RUN

| ID | Future fixture | What to verify |
| CH16-01 | No-Planet chorus flower AGE0 placed on END_STONE; normal BlockItem | Initial AGE, physical support, original BlockItem placement and survival |
| CH16-02 | Flower supported directly by CHORUS_PLANT below | canSurvive independent of adjacency and AGE; physical below |
| CH16-03 | Flower below AIR with exactly one horizontal neighboring CHORUS_PLANT | canSurvive true only with all three remaining physical horizontal cells AIR |
| CH16-04 | Flower below AIR, two horizontal CHORUS_PLANT neighbors | canSurvive false (not generic sturdy-face acceptance) |
| CH16-05 | Flower below AIR, one CHORUS_PLANT plus unrelated non-AIR horizontal | canSurvive false |
| CH16-06 | Flower updateShape from UP vs non-UP with invalid support | Original delayed tick 1 only for non-UP; tick rechecks and destroys if invalid |
| CH16-07 | Flower AGE 4 vs AGE5 isRandomlyTicking | AGE5 no random grow; AGE4 can random grow/dead |
| CH16-08 | Flower projectile on server with mayInteract/mayBreak permitted vs denied | Destroy only with both permissions true, real hit BlockPos |
| CH16-09 | RandomTick vertical candidate empty above, above2 free and 4 horizontals free | Turns source to connected plant, creates flower at physical above, event 1033 |
| CH16-10 | RandomTick vertical above occupied or neighboring obstruction | No illegal vertical transition, lateral/dead path per AGE and RNG |
| CH16-11 | RandomTick with four physical horizontal choices + nearest back-direction exception | Lateral creates flower AGE+1 only if actual target and target below AIR and others free |
| CH16-12 | RandomTick no admissible lateral branch or AGE threshold | Source dead AGE5, event 1034; no distinct dead flower block |
| CH16-13 | Chorus plant BlockItem on target with 6 specific neighbors | getStateWithConnections actual neighbor states and END_STONE special DOWN only |
| CH16-14 | Plant six Boolean connection flags and 6-bits shape mask | Exact 64 PipeBlock cached VoxelShapes indexed by stored six physical connections |
| CH16-15 | Plant callback one physical direction changed | updateShape updates ONLY corresponding PROPERTY_BY_DIRECTION on surviving state |
| CH16-16 | Plant callback from DOWN with END_STONE vs any other side END_STONE | Only DOWN may connect END_STONE in physical vanilla |
| CH16-17 | Plant invalid support in updateShape then tick after one tick | Schedules delayed destroy, doesn't necessarily AIR instantly |
| CH16-18 | Plant below END_STONE vs below plant vs below unrelated non-air | Exact canSurvive branch and support, independent of boolean connection flags |
| CH16-19 | Plant with above and below nonair plus horizontal neighbor support | Original flag may invalidate lateral path; avoid simplistic 'any neighbor' rule |
| CH16-20 | Plant horizontal neighbor with its own below END_STONE / plant | Specific 2-hop survival, not generic face-sturdy |
| CH16-21 | ChorusPlantFeature with origin AIR and below END_STONE | Direct generatePlant/recursive worldgen path, bypass BlockItem |
| CH16-22 | Feature origin not AIR or below not END_STONE | Placement refuses without mutation |
| CH16-23 | ChorusFlowerBlock.growTreeRecursive first trunk and subsequent branch | World above offsets, rng 1..4 and extra 1 on root, 4-branch recursion bound, terminal flower AGE5 |
| CH16-24 | Recursive worldgen XZ radius bound x,z and blocked neighbor | Physical x/z absolute bound, empty target/below + allNeighborsEmpty, preserve deterministic seed |
| CH16-25 | Direct commands or synthetic structure template chorus states | Writer bypasses BlockItem; only legal AGE0..5, six booleans; no invented shipped NBT |
| CH16-26 | Vanilla no Planet plus POS_Y Planet parity | Real physical XYZ behavior unchanged unless explicitly declared local-growth policy |
| CH16-27 | Six Planet face interiors source local vertical plant branch | Optional typed source local-UP algorithm projects into physical target; XZ source policy explicit |
| CH16-28 | All 12 true cube face-pair edges, two source traversal entries | One real target and canonical BlockState, no duplicate branch/connection flags |
| CH16-29 | All 8 true triple corners, three traversal entries | One physical graph node and one source/target canonical frame each, no alias plants |
| CH16-30 | Chorus branch crosses physical XZ chunk boundary and reload | Visited/state/AGE/ticks and sources persist, no growth time double scheduling |
| CH16-31 | Compare PHYSICAL six neighbor pipe graph vs local tangent growth plane | Never rotate six physical callback Directions for unknown mod PipeBlock family |
| CH16-32 | Generic BlockBehaviour.BlockStateBase.updateShape and NeighborUpdater instrumentation | Provenance captured while physical incoming Direction remains unchanged for unregistered mod |
| CH16-33 | Deepest helper candidate on independent mod PipeBlock descendant | 64-mask shape and owner-specific survival differentiated; no unsupported Mixin claim |
| CH16-34 | Non-PipeBlock mod implements six boolean connection states itself | Generic typed physical adjacency API possible, inherited PipeBlock Mixin insufficient |
| CH16-35 | Worldgen fake Level/structure/clipboard and non-Planet control | Do not reinterpret synthetic coordinates as Planet physical without provenance |
| CH16-36 | NeoForge 21.1.215 actual client/server with optional modded pipe/network | Require applied ASM, mod's real method dispatch, shapes, graph, fluids, biomes and acceptance, not source-only pass |

## Mod and runtime acceptance blockers

Mod compatibility implications are **mechanical hypotheses** here, not an assertion a given mod actually extends PipeBlock. A future mod registry/call-site census MUST supply loaded class, source SHA, owner and side semantics before claiming compatibility. Vanilla original 241 registered concrete class census explicitly excludes every modded registered BLOCK ID. We retain all 241 original patched ASM, Planet adapter, gameplay gates REVIEW_PENDING even after 2 new chorus classes source review (96/241 classes, 245/1060 IDs). Six-face+12-edge+8-corner unit/actual client server tests, synthetic feature and optional third-party mod graph fixture still NOT RUN.
