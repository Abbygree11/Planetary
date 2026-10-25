# Phase 3 — Whole-phase original-source / NeoForge / mod-compatibility audit gate

**Date:** 2026-10-18. **Status: NOT RESEARCHED TO PHASE-2 COMPLETENESS.**
Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · `Abbygree11/Planetary` branch `2.0`.

## Why this gate exists

The user explicitly approves interrupting the active phase (currently 2.4A) to fix an issue in a later owning phase, **but only after the entire later phase has been comprehensively researched**. Phase 2's source/ABI owner atlas, block/item census, override/bypass classification and cross-mod matrix set the bar. The existing Phase 3 PARTIAL card, `RENDERING_1_21_1.md`, 28-BER selective R2 note and Phase-2 C11–C15 are preparatory/selected sources, NOT frozen full Phase-3 coverage. The earlier `3C-1` renderer code already exists (CI SUCCESS), but remains provisional and unaccepted in gameplay. Do not interpret its four-owner research as a full Phase 3 pass.

## 3R-2d dynamic shape/cache/foreign-world checkpoint (2026-10-25)

[Source research](PHASE3_R2D_DYNAMIC_SHAPE_CACHE_OCCLUSION_SUPPORT_1_21_1.md), [28-row mechanisms+bypass ledger](PHASE3_R2D_DYNAMIC_SHAPE_CONTEXT_OWNER_MATRIX_1_21_1.tsv), [selected original NeoForge compiled callsite JUnit](../../src/test/java/dev/planetary/world/Phase3R2dShapeCacheAndContextAsmResearchTest.java). **CI completed SUCCESS:** [#40145655409](https://github.com/Abbygree11/Planetary/actions/runs/40145655409), head [f9fec10](https://github.com/Abbygree11/Planetary/commit/f9fec104412a17552defb920660a49de730dd52b) (research-only ASM/JUnit). Published GitHub Actions artifact **12580212320** (`phase2-neo1211-registry-census`, includes `phase3-neo1211-shape-cache-context-callsite.tsv`), manifest SHA-256 **`a4ef20bf9ffb5ba932cef83ade51c1372f1ad1c1246d7fa163275f39875f3b9b`**, seven selected compiled method rows plus header, artifact re-downloaded and verified. This proves selected target JVM calls, **not** applied Minecraft Mixins, game, or mod compatibility.

**3A still NOT closed:** `dynamicShape` cached vs dynamic registered state list, full original NeoForge patched INVOKEs/consumer domain, real mod subtype/foreign BlockGetter and BE-dependent culling cache invalidation need research. `3R-2e` is the next bounded **research-only** packet; the original six declaring method proof 241/241 remains intact from 3R-2c. Do not implement client culling/cache fixes or begin 3B until correct research gate.

## 3R-2c six-method original JVM census (2026-10-25)

[Stage](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.md) / [241 exact original JVM class/owner atlas](PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.tsv) / [two independent ZIP verifier](../../scripts/research/phase3_shape_six_owner_verify.py). Six selected original Block shape API method declaring-owner signatures are now fully covered 241 classes/1060 IDs, including all intermediate inherited owners. Four newly verified methods via CI #40136919659 original NeoForge21.1.215 bootstrapped runtime. **This is a sub-gate only:** 3A dynamic context/BlockStateBase face cache/overloads/mod bypass and all of 3B–3E remain OPEN. `3R-2d` research-only next; keep all later phase checkboxes unchecked.

## 3R-2b checkpoint (2026-10-25) — two exact APIs completed, phase gate still OPEN

All 241 original NeoForge21.1.215 registered class hierarchy and two effective original JVM method-declaring owners `getShape/getCollisionShape` now checksum-verifed using original historical immutable ZIP. [Research 4/4](PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.md) / [241-row exact owner atlas](PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.tsv) / [reproducer](../../scripts/research/phase3_shape_effective_owner_verify.py). The four other original shape API owners are **source candidates only**. **NEXT 3R-2c**: exact target compiled/patch/callsite evidence and remaining 3A subsystem/extension owners, not Phase3 implementation. No checkbox below is closed by two methods alone.

## 3R-2 source-only registry census checkpoint (2026-10-24)

[Stage research](PHASE3_R2_REGISTERED_SHAPE_DECLARATIONS_1_21_1.md) + [241 registered class/1060 block direct-declaration matrix](PHASE3_R2_REGISTERED_DIRECT_SHAPE_DECLARATIONS_1_21_1.tsv) are now committed, with recorded six VoxelShape and five related rendering/light API source-search candidate flags. Intermediate inherited owners **remain unresolved in 241 rows**; full exact NeoForge21.1.215 ABI and real mod comparisons are still OPEN. [30-row whole-phase ledger](PHASE3_OWNER_COVERAGE_1_21_1.tsv) now tracks this as a selected census, not phase completion. Next named packet `3R-2b` must finish effective superclass owner resolution before considering 3A or the whole phase researched.

## Research resumed 2026-10-24

`3R-1` completed a **selected original source owner/call-flow subset** for 3A (shape/cache/raycast/collision/support/render occlusion), with [source evidence](PHASE3_R1_3A_SHAPE_CALLPATH_AND_COVERAGE_1_21_1.md) and [phase-wide owner coverage ledger](PHASE3_OWNER_COVERAGE_1_21_1.tsv). This is **NOT** a completed 3A registered-block override inventory and **NOT** the phase gate. `3R-2` must continue full concrete registered block/override census before any Phase3 implementation.

## Research-only buckets (each separately bounded, source-first; NO implementation while auditing)

| Bucket | Inventory + mechanism contracts to freeze | Coverage status |
| --- | --- | --- |
| 3A static shape & geometry | `VoxelShape`/collision/outline/interaction, occlusion caches, section/chunk seams, face provenance, transformed hit shape, mod override families | OPEN |
| 3B standard blocks | `BakedModel`, ModelData, RenderType, quad normals/UV/tint, culling/hide hooks, AO, light/shadow, chunk rebuild and resource reload, transparent/multipart/rand variants, seam and corner policy | OPEN |
| 3C Block Entities | **All registered vanilla BER declaring owners and overridden inheritors** (not just four); normal world-attached, absolute/world/camera geometry, text, skins, flexible models, BEWLR/item+foreign world, registration/replacement | OPEN |
| 3D modded/accelerated | NeoForge custom render/section geometry and porting APIs, custom BE renderers, instanced Create/Flywheel pipeline, moving piston and fluid block renderers, shader/Iris, shadow/g-buffer/PBR, caches and fallback paths | OPEN |
| 3E resource+entities+client lifecycle | Resource/texture/model reload; chunk worker/dispatcher provenance, render epoch, LOD/cache, player/entity/item/vehicle render effects, debug/UI/world-preview, teleport and world switch | OPEN |

For **each bucket** enumerate original real owners and registered instances, precise method overrides and dispatch, client/server entry boundaries, `PHYSICAL` vs `LOCAL` vs `CAMERA` coordinate spaces, alternative drawing/baking/animation owners, target NeoForge compiled ABI, loaded-mod extension points plus explicit independent paths, unknowns, and six-face/12-edge/8-corner plus no-Planet validation. Distinguish documentation, source samples, compiled coverage, runtime Mixin, gameplay and loaded-mod proofs; none substitutes for another.

## Gate completion (only after all research batches)

- [ ] All relevant registered vanilla render owner classes traced, including inherited/overridden and bypass cases; no implicit "it is just a block" exclusions.
- [ ] End-to-end pipeline inventories for 3A/3B/3C/3D/3E with **per-owner** evidence and selected concrete source+exact NeoForge ABI.
- [ ] Real NeoForge mod extension / custom renderer override matrix; identify Create/Flywheel/Iris and data-driven model paths as supported/independent/unknown, never speculative.
- [ ] One reconciled phase-wide owner and bypass matrix linked from `docs/phases/phase-03.md`, with explicit OPEN owner rows not silently ignored.
- [ ] Frozen generic, deepest safe rendering and geometry transformation contracts before runtime fixes; preserve physical world lighting and effects.
- [ ] Planned regression and game matrix for all six faces, 12 edges, 8 corners, +Y vanilla, no-Planet, chunks/resource reload/shaders/loaded mods.

Until all boxes are evidenced, mark **PHASE 3 RESEARCH INCOMPLETE**. Existing `3C-1` code [ea04238](https://github.com/Abbygree11/Planetary/commit/ea0423894e2c1e92a5d74e81c87543be48bd9453) and [CI #39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) remain a **provisional bounded intervention, GAMEPLAY PENDING**, not whole-phase completion. Research-only step next, or continue already-audited Phase 2. Do not silently start Chest/other BER fixes.
