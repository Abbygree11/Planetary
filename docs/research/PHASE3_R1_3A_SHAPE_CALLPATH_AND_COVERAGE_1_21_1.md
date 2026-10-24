# Planetary — Phase 3 research `3R-1`: 3A shape API/call-graph and phase-wide evidence ledger

**2026-10-24 | Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 | branch `2.0`**

**Result: RESEARCH-ONLY SOURCE-TRACED SUBSET. Whole Phase-3 owner audit: OPEN. No game or runtime code changed; no new CI.** This packet establishes a reproducible 3A entrypoint/caching/consumer contract and opens a separately tracked phase-wide coverage ledger. This is not an inventory of every registered 3A block override. The previously completed general R2 architecture survey and Phase2 C11–C15 source/selected ABI checks are reused, not falsely promoted to a complete phase.

## 1/4 — Pinned original source: original call graph and owners

Readable Mojang-mapped comparator pinned to `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75`. It may contain OptiFine changes; it is **not** proof of *exact* NeoForge 21.1.215 binaries. Older [C11](PHASE2_STAGE3C11_SHAPES_OCCLUSION_SUPPORT_RENDER_CULLING_NEOFORGE_ASM_1_21_1.md) tests selected exact 21.1.215 classpath signatures and selected Planet mixin/owner ABI; a **whole-source-registry subclass/override inventory remains missing**.

| Original owner in pinned source | Verified path and relevant semantics | Owner/space, and essential bypass |
| --- | --- | --- |
| [BlockBehaviour.BlockStateBase](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java) | `getShape` virtual Block.getShape; `getCollisionShape(context)` virtual Block.getCollisionShape; `getCollisionShape(simple)` may return `Cache.collisionShape`; `getVisualShape`, `getInteractionShape`, `getBlockSupportShape`, `getOcclusionShape` independently dispatch to block owners | Local shape authored by Block subclass, **physical** shape expected by raycast/collision consumers; avoid double transform if nested original calls occur |
| [BlockStateBase.Cache](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java) | `Cache` constructed from `EmptyBlockGetter.INSTANCE, BlockPos.ZERO`; caches collision, face-occlusion VoxelShape[] indexed by `Direction.ordinal`, support/rigid/full flags, skylight and light-block results | **No Planet BlockPos/Level** at construction; changing cache globally poisons every world; a dynamic mod shape cannot be assumed state-invariant |
| [BlockGetter.clip / clipWithInteractionOverride](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/BlockGetter.java) + [ClipContext.Block](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/ClipContext.java) | `COLLIDER` / `OUTLINE` / `VISUAL` choose different state shape APIs; `FALLDAMAGE_RESETTING` can supply `Shapes.block`; `clip` traverses physical block cells, calls block shape, then separate interaction override and fluid clip | Physical ray start/end/hit/side must remain physical; **shape selection ≠ shape rotation ≠ hit-result semantic face** |
| [BlockCollisions](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/BlockCollisions.java) | Scans physical cells; uses context-dependent `getCollisionShape`, has special `voxelshape == Shapes.block()` fast path, calls `Shapes.joinIsNotEmpty` for partial collision | Must preserve singleton/full-cube identity and AABB geometry; no blanket shape-object wrapping, no synthetic change to cursor |
| [SupportType](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SupportType.java) + [BlockStateBase.isFaceSturdy](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java) | FULL/CENTER/RIGID each consumes `getBlockSupportShape` differently, with cached support flags when state cache exists | Support SOURCE block's local side toward target, but **target neighbor physical BlockPos**; local UP ≠ global world UP |
| [Block.shouldRenderFace](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/Block.java) + [Shapes](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/phys/shapes/Shapes.java) | `skipRendering`, canOcclude, face masks from original and neighbor, `joinIsNotEmpty(ONLY_FIRST)`; original state-pair direction cache keys only one `Direction`. NeoForge adds independent external face hiding hooks (see C11) | This is **3B occlusion/quad visibility consumer**, not a justification to rewrite all 3A shape queries; source and neighbor may belong to different Planet faces |
| [VoxelShape](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/phys/shapes/VoxelShape.java) + [Shapes](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/phys/shapes/Shapes.java) | `clip`, `collide`, face shape extraction, boolean join, full-block and empty geometry | Generic shape math is PHYSICAL after owner/source adaptation; keep physical axes and fast-path identities intact |
| [BlockBehaviour](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.java) defaults | `getOcclusionShape` may call `state.getShape`; `getBlockSupportShape` may call collision; `getVisualShape` may call collision | Nested call graph must not rotate twice; full overrides/calls still need census |

**Concrete chains traced:**

```text
player view / selection:
BlockGetter.clip(ClipContext)
  -> ClipContext.Block.OUTLINE / COLLIDER / VISUAL
  -> BlockStateBase.getShape / getCollisionShape / getVisualShape
  -> virtual Block subclass (including dynamic Position/CollisionContext)
  -> physical VoxelShape.clip; interaction override queried separately
  -> physical BlockHitResult
entity/world collision:
BlockCollisions.scan(physical BlockGetter, position)
  -> BlockStateBase.getCollisionShape(world,pos,context)
  -> cached or dynamic shape owner
  -> Shapes.block() identity OR Shapes.joinIsNotEmpty physical intersection
support and survival:
BlockStateBase.isFaceSturdy(world,supportPos, localDirection, FULL/CENTER/RIGID)
  -> Cache.faceSturdy OR SupportType.isSupporting
  -> getBlockSupportShape -> possibly collision/getShape nested
render occlusion (separate 3B consumer):
Block.shouldRenderFace(sourceState, world, sourcePhysicalPos, physicalSide, neighborPhysicalPos)
  -> vanilla skipRendering + NeoForge external hide hooks
  -> source/neighbor own face-occlusion side -> Shapes.ONLY_FIRST
  -> ModelBlockRenderer / BakedModel quads and chunk renderer (3B)
```

These are **owner/consumer paths**, not a claim that every overload or custom mod override has been validated. C11 already documented a risk in Planet's state+two-local-direction culling cache: dynamic mod face geometry can vary with BlockEntity/position without the same `BlockState`; **no live reproduction or cache-repair code** in this research packet.

## 2/4 — Coordinate provenance, invariants, dependency graph

- **PHYSICAL_DO_NOT_TOUCH:** source and neighbor world `BlockPos`, `BlockGetter` traversal, `VoxelShape.clip/collide`, hit-result coordinates, `Shapes.block` singleton/empty, world lighting, `BlockStateBase.Cache` generated against `EmptyBlockGetter`.
- **CANONICAL LOCAL:** block's authored directional `VoxelShape` before the outer physical representation conversion; support block's own local face; original `FACING`/rotation state domain; mod virtual shape callbacks.
- **TRAVERSAL/EDGE:** physical neighbor across a 12-edge/8-corner chart seam; source and target local `Direction` must be derived *independently*. A single state-pair/direction cache key is insufficient across divergent charts.
- **CAMERA/ENTITY:** selection ray end points, bounding boxes, collision entity context are physical; actor local view ray creation belongs to Phase 7B, not this shape owner.
- **CLIENT RENDER:** shape occlusion and BakedModel AO/light/culling may reuse shape data but are not identical to physical collision; Phase3B owns the rendering decision, Phase3C BER independent, Phase3D custom shaders/geometry independent.
- **CROSS-PHASE:** Phase2 placement/support, Phase5 fluids/face permeability, Phase7 collision and player motion, Phase7B raycast/network, Phase3B visibility all consume Phase3A-derived geometry differently. Record contract, never globally rewrite world access.

## 3/4 — Mod compatibility, unsupported by this packet

| Route | Classification | Why this still needs a complete Phase-3 audit |
| --- | --- | --- |
| Registered vanilla Block subclasses with positional/contextual shapes | SOURCE-TRACED BASE ONLY; **full registry/override census OPEN** | Base dispatch may be inherited, but each overridden `getShape/getCollisionShape/getBlockSupportShape/getOcclusionShape/getVisualShape` and multi-block dynamic override must be enumerated and linked to actual registered IDs |
| NeoForge `IForgeBlockState`, external face hiding, `ModelData/RenderType` | SELECTED NEOfORGE ABI in prior C11, **not full registry/mod acceptance** | Shape/client/render extension can be overridden outside original base method; verify exact 21.1.215 patches + source and owners for every relevant branch |
| Create/AE2 connected glass, moving contraptions, custom rendered mesh | MOD_INDEPENDENT pending per real supported artifact | Separate mesh/face hide/custom BlockEntity renderers may not call vanilla BakedModel or world BlockGetter; no loaded mod JAR validation |
| Dynamic shape depending on BlockEntity/Level or foreign `BlockGetter` | CACHE-PROVENANCE RISK / no production fix | State-only cached occlusion may be stale; test real source/target positions, fake preview worlds and cache invalidation before choosing a policy |

**Deepest safe candidate from *this subset*, NOT frozen Phase3 architecture:** preserve vanilla/NeoForge BlockState virtual dispatch, adapt only position-aware outer shape-return contract in a typed Planet frame scope; keep support/occlusion canonical where appropriate and apply source/neighbor chart conversion in rendering consumers. Global `VoxelShape` or `Direction` mutation unsafe.

## 4/4 — Evidentiary matrix, reproducibility and next research-only batch

| Gate | Evidence | Result |
| --- | --- | --- |
| Readable 1.21.1 original source owners | Pinned source links above + selected complete call flows | **SOURCE VERIFIED (selected 3A entry paths)** |
| NeoForge 21.1.215 precise ABI | Existing [C11 ASM/JUnit](PHASE2_STAGE3C11_SHAPES_OCCLUSION_SUPPORT_RENDER_CULLING_NEOFORGE_ASM_1_21_1.md) selected owners only | **PRIOR SELECTED ABI**, not a fresh/full census |
| All registered blocks / 3A dynamic overrides | Phase2 241-class/1060-ID census may serve as input, but no 3A per-owner override mapping | **NOT DONE** |
| Phase3 all-owner audit 3A…3E | [Phase-wide ledger](PHASE3_OWNER_COVERAGE_1_21_1.tsv) is a task inventory, not proof of coverage | **OPEN** |
| Applied Mixins/client (this packet) | No Java change, no client execution | **NOT APPLICABLE / NOT TESTED** |
| Game six faces, 12 edges, eight corners | No new user acceptance | **PENDING** |
| Loaded mod rendering + dynamic shape | No compatible JAR source/runtime matrix in this packet | **PENDING** |
| CI | **Docs-only commit**, no Java/source/test change | **NO NEW CI EXPECTED**; most recent pre-existing code CI [#39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) SUCCESS belongs to provisional 3C-1, not this research |

**Next EXACT bounded packet `3R-2`:** start with the *actual Phase2 registered block/class census* and map ALL registered owners overriding 3A `getShape/getCollisionShape/getBlockSupportShape/getOcclusionShape/getVisualShape/getInteractionShape` and `isFaceSturdy`/shape cache branches; record class/ID counts separately, superclass inheritance and mod/NeoForge override hooks. Complete selected source+ABI traces for representative dynamic shape families and reconcile gaps. **No runtime code during `3R-2`.** After all of 3A, proceed to 3B/3C/3D/3E with full source and exact target ABI inventories and then freeze the whole-phase map; user may jump back to the interrupted visual fix only AFTER gate completion.

**Stop here:** ONE bounded 3R-1 research checkpoint; update `docs/SESSION_HANDOFF.md`, phase card, roadmap and source R2 status without fabricating full Phase3 research PASS.
