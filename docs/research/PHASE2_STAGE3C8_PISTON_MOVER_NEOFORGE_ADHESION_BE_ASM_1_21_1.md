# 2.3C-8 — Piston / moving block transactions, NeoForge mod extensions and moving BlockEntity (4/4)

**2026-10-12 · Planetary 2.0 · Minecraft1.21.1/NeoForge21.1.215 · bounded actual classpath ASM research, not game acceptance.**

## Sources and classification

Original independent [Stage3A complete piston source audit](PHASE2_STAGE3A_PISTON_MOVEMENT_SLIME_HONEY_DEEP_SOURCE_NEOFORGE_CREATE_1_21_1.md) and [72 unrun game scenarios](PHASE2_STAGE3A_PISTON_SIX_FACE_SEAM_CORNER_MOD_ACCEPTANCE_1_21_1.md) cover **6 registered vanilla BLOCK IDs / 5 concrete classes**: PistonBaseBlock (piston, sticky_piston), PistonHeadBlock, MovingPistonBlock, SlimeBlock, HoneyBlock. Two synthetic (no-item) IDs. Original registry source census 241 vanilla concrete classes/1060 blocks does not certify patched ASM or gameplay.

Pinned readable 1.21.1 comparator: [PistonBaseBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/piston/PistonBaseBlock.java), [PistonStructureResolver](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/piston/PistonStructureResolver.java), [PistonMovingBlockEntity](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/piston/PistonMovingBlockEntity.java), [MovingPistonBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/piston/MovingPistonBlock.java), [PistonHeadBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/piston/PistonHeadBlock.java), pinned SHA b77c5c6995874f6cf2755bc5234428906b337b75.

Later NeoForge1.21.1 *COMPARATIVE, not actual 21.1.215 proof*, pinned SHA 3cae64530de368936d4c07497d3f0a25e2f9ffec:
[PistonBaseBlock.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/level/block/piston/PistonBaseBlock.java.patch) introduces EventHooks.onPistonMovePre (cancellable extend/retract), onPistonMovePost and BlockState.onDestroyedByPushReaction plus world-aware rotate;
[PistonStructureResolver.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/level/block/piston/PistonStructureResolver.java.patch) replaces hardcoded slime/honey with isStickyBlock and symmetric canStickTo;
[PistonMovingBlockEntity.patch](https://github.com/neoforged/NeoForge/blob/3cae64530de368936d4c07497d3f0a25e2f9ffec/patches/net/minecraft/world/level/block/piston/PistonMovingBlockEntity.java.patch) changes entity slime check to isSlimeBlock.

Actual loaded target compiled class assertions and compiled Planet LevelMixin selector in [Phase23C8PistonMovementAsmContractTest](../../src/test/java/dev/planetary/world/Phase23C8PistonMovementAsmContractTest.java). [Initial GitHub CI run 38281757960](https://github.com/Abbygree11/Planetary/actions/runs/38281757960) requires independent completion check before claiming PASS. No runtime Java, Mixin or Gradle changes.

Mod comparator: pinned real Minecraft1.21.1 [Create @7ff2760e4924d35d4bc2e22c4b19923f0178cd16](https://github.com/Creators-of-Create/Create/tree/7ff2760e4924d35d4bc2e22c4b19923f0178cd16/src/main/java/com/simibubi/create) has INDEPENDENT MechanicalPistonBlock/MechanicalPistonBlockEntity → PistonContraption/TranslatingContraption with BlockMovementChecks and fake ContraptionWorld, not a vanilla PistonBaseBlock subclass. Binary mod load NOT TESTED.

## 1/4 — declaring owners and event-driven graph

| Owner | Core sequence | Semantic domain |
| --- | --- | --- |
| PistonBaseBlock.checkIfExtend / neighborChanged / onPlace | redstone and physical quasi-connectivity, Level.blockEvent(type extend 0 / retract 1,2) | Signal graph in Phase7A, not Item placement |
| BlockStateBase.triggerEvent → PistonBaseBlock.triggerEvent | revalidate power, NeoForge pre-event cancellation, call moveBlocks, base/head writes, post-event/sound | Preserve physical Direction 0..5 event encoding |
| PistonStructureResolver.resolve/addBlockLine/addBranchingBlocks | physical directed graph, limit MAX_PUSH_DEPTH=12, world border/build height, mod isStickyBlock + symmetric canStickTo, PushReaction | Physical source and target coordinates, mod adhesion via polymorphic BlockState |
| PistonBaseBlock.moveBlocks | apply destroy push reaction, write synthetic MOVING_PISTON + explicit PistonMovingBlockEntity, source AIR, head, notify neighbors | Multiple side effects, not Level.setBlock alone |
| MovingPistonBlock.newMovingBlockEntity/getTicker/onRemove | synthetic BE construction and ticker, forced finalTick | Synthetic moving block has NO vanilla Item constructor |
| PistonMovingBlockEntity.tick/finalTick | progress, physical AABB collision, entity push, remove BE, restore movedState and notify neighbors | Depends on Stage2.3C-6 virtual BE ticker wiring (not gameplay verified) |
| PistonHeadBlock.canSurvive | behind-base support, forced head removal | Separate subclass neighbor support algorithm |

**Critical**: vanilla moved placeholder BlockEntity stores a moved BlockState, not a fully relocated original source inventory/capability BlockEntity. Some BE-bearing states cannot be pushed by vanilla. Mod movers can independently capture/restore NBT, components, ticker registration and capabilities, and must not inherit naive vanilla semantics.

## 2/4 — lifecycle, cross-face motion and failure modes

1. Check if powered and queue blockEvent; re-check during triggerEvent and allow NeoForge pre-event cancellation. Do not fire post on cancelled/failed transaction and do not double-deliver mod events.
2. Resolve entire physical list (including branching slime/honey and destroy reactions, max12 moved), preflight loaded physical source/target/world border. NeoForge onDestroyedByPushReaction receives true physical direction and fluid, not a generic setBlock(AIR) substitute.
3. Actual multi-write: placeholder states, explicit moving BE objects, source air/head changes, blockState shape and neighbor notifications with exact flags. A global Level.setBlock interceptor alone cannot preserve transactional atomicity/rollback or per-source mod BE state.
4. Advance through MovingPistonBlock.getTicker and PistonMovingBlockEntity.tick, call finalTick on completion or earlier forced cleanup, restore actual state and neighbor callbacks. If virtual Planet BE tickers are missing, **a piston can visually/temporally stay in MOVING state** (source-level risk; no live game reproduction).
5. Entity collision is separate: physics uses AABB and Entity.move(MoverType.PISTON), NeoForge isSlimeBlock dynamic extension and honey physical-horizontal traction. Planet local-gravity and client rendering are separate Phase7/3 owners.
6. **Seam policy is not frozen**: Option A straight physical push through a face boundary with canonical destination state transport; option B bending the push path via chart traversal. B requires bijective graph mapping, no duplication/loss, explicit entire transaction/rollback and possibly segment-wise collision. NEVER implement B by changing a global Direction value.

## 3/4 — mod compatibility and safest shared boundary

| Disposition | Mechanisms | Reason |
| --- | --- | --- |
| PHYSICAL_DO_NOT_TOUCH | raw BlockPos, Direction, build height/worldborder, Level, Entity.move, AABB, physical neighbor graph | Mods and fake Levels expect real physical coordinates |
| SHARED_SAFE observation | BlockStateBase state dispatch, NeoForge pre/post/push destruction hooks, isStickyBlock/canStickTo/getPistonPushReaction, Level/BE writes | Preserve polymorphic mod extensions |
| OPT_IN_ALGORITHM | canonical player-facing placement, optional seam-transport graph, state/BE relocation provenance, local entity gravity | Needs explicit chart/frame and author contract |
| MOD_INDEPENDENT | Create mechanical pistons and contraptions, modded movers/capability and NBT transfer | These bypass vanilla PistonStructureResolver and use their own motor/graph |
| CLIENT_RENDER | moving BE renderer, interpolation, collision animation particles | Separate phase/game verification |

Future design only: Planet-owned MovementPlan with typed author, source/target physical positions, canonical frame provenance, physical vs chart path policy, mod dynamic stickiness and push reaction, full toPush/toDestroy and state/BE/metadata list, flags/event stages, rollback. 1.21.1/NeoForge adapters must be thin. **Deeper global Level.setBlock/Direction/BlockPos changes break modded worlds**; shallower piston-FACING-only patch loses resolver, NeoForge events, entity physics, synthetic BE and Create.

## 4/4 — precise bytecode and acceptance gates

[Strict actual version ASM contract test](../../src/test/java/dev/planetary/world/Phase23C8PistonMovementAsmContractTest.java) asserts selected PistonBaseBlock.triggerEvent/moveBlocks/isPushable callsites, target NeoForge EventHooks.onPistonMovePre/Post and onDestroyedByPushReaction, physical PistonStructureResolver isStickyBlock/canStickTo/PushReaction, BlockStateBase dynamic reaction, MovingPistonBlock factory/getTicker/onRemove, PistonMovingBlockEntity tick/finalTick and slime/honey entity collision, compiled Planetary LevelMixin.setBlock selector/handler/@AtHEAD/cancellable. A successful JVM contract test is **not** a transformed client/server Mixin test.

| Gate | Status |
| --- | --- |
| 4/4 comprehensive source/patch/comparator/mechanism study | DONE |
| Exact target JVM ASM/JUnit compiled tests authored/committed | DONE |
| CI compile, JUnit, registry | [initial 38281757960](https://github.com/Abbygree11/Planetary/actions/runs/38281757960) failed two TEST assumptions, [repeat 38282239773](https://github.com/Abbygree11/Planetary/actions/runs/38282239773) **PASS / completed SUCCESS** |
| Live client/server Mixin weave | NOT RUN |
| 72 predesigned piston fixtures: 6 faces/12 seams/8 corners, mods, controls | NOT RUN |
| Actual NeoForge event subscriber, cancellation and custom adhesive blocks | NOT RUN |
| Full BE tick/serialization/capabilities and Create independent mover | NOT RUN |
| Vanilla no-Planet piston and redstone regression | NOT RUN |

Manual cases: genuine placement vs forced state, six-face push/pull, 12 seam paths physical vs semantic decision, 8 corners no dupes, sticky/honey modded adhesive symmetry, NeoForge pre/post counts on cancellation, destroy-push block fluid/drop hooks, entity support/bounce/sliding, moving BE progress, chunk unloading/save+restart and physical out-of-Planet/fake world behavior. The existing [72-case matrix](PHASE2_STAGE3A_PISTON_SIX_FACE_SEAM_CORNER_MOD_ACCEPTANCE_1_21_1.md) is a PLAN, not performed tests.

**Next 2.3C-9:** redstone/automation signal owner family plus NeoForge events and modded signal sideports, 4/4 package.


### CI delta 2.3C-8

[Initial CI 38281757960](https://github.com/Abbygree11/Planetary/actions/runs/38281757960) **FAILED 2/361 JUnit test assertions; production/test compilation PASS**: source-level assumption wrongly specified the return of `MovingPistonBlock.newMovingBlockEntity` as `PistonMovingBlockEntity` rather than actual declared `BlockEntity`; another assertion searched for `Entity.move` directly in `moveCollidedEntities` instead of following the owning `moveEntityByPiston` helper. Dynamic NeoForge events/isStickyBlock/canStickTo/isSlimeBlock and other 4 Stage2.3C-8 tests PASSED. [Strict correction commit 4612c77](https://github.com/Abbygree11/Planetary/commit/4612c77dfbb2fdfd5d014d1c6aa3b5589fc076e6) pins exact factory return and checks helper's Entity.move; [repeat CI 38282239773](https://github.com/Abbygree11/Planetary/actions/runs/38282239773) **needs independent conclusion**. No production code changed.


**Final CI confirmation:** [Actions 38282239773](https://github.com/Abbygree11/Planetary/actions/runs/38282239773) **completed SUCCESS**, including exact Gradle-resolved NeoForge 21.1.215 selected piston/NeoForge extension ASM and compiled Planet LevelMixin assertions. The selected method-dispatch invariants in this file are now **CI VERIFIED**. This does NOT prove client/server applied Mixin, vanilla no-Planet piston runtime, Planet faces/seams, actual BE motion after tick, dynamic NeoForge listener cancellation, 72 manual tests or loaded Create jar compatibility.
