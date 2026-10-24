# Phase 3 — Whole-phase original-source / NeoForge / mod-compatibility audit gate

**Date:** 2026-10-18. **Status: NOT RESEARCHED TO PHASE-2 COMPLETENESS.**
Minecraft 1.21.1 · NeoForge 21.1.215 · Java 21 · `Abbygree11/Planetary` branch `2.0`.

## Why this gate exists

The user explicitly approves interrupting the active phase (currently 2.4A) to fix an issue in a later owning phase, **but only after the entire later phase has been comprehensively researched**. Phase 2's source/ABI owner atlas, block/item census, override/bypass classification and cross-mod matrix set the bar. The existing Phase 3 PARTIAL card, `RENDERING_1_21_1.md`, 28-BER selective R2 note and Phase-2 C11–C15 are preparatory/selected sources, NOT frozen full Phase-3 coverage. The earlier `3C-1` renderer code already exists (CI SUCCESS), but remains provisional and unaccepted in gameplay. Do not interpret its four-owner research as a full Phase 3 pass.

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
