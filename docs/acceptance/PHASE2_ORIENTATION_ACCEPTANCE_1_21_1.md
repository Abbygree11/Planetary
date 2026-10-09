# Phase 2 comprehensive block orientation acceptance — 1.21.1

**Status: DESIGN / NOT RUN.** This is a reproducible acceptance contract,
NOT a claim that every family already works.

Research / source owner atlas:
`docs/research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md`
and class/source census `docs/research/PHASE2_VANILLA_CLASS_CENSUS_1_21_1.tsv`.

## Coverage gate is mandatory before "Phase 2 PASS"

For each mechanism group (P01–P40 in the atlas), track:

| Column | Definition |
|---|---|
| Source owner | Actual vanilla 1.21.1 + matching NeoForge 21.1.215 call site; subclass overrides included |
| Semantic policy | Exact PHYSICAL click vs CANONICAL state vs TRAVERSAL neighbor vs BODY yaw ownership |
| Implementation | Shared Planet semantic helper + thin adaptation(s) |
| Unit + ASM CI | All 6 faces, +Y vanilla equivalence, edge/corner, no invalid injection handlers |
| Client transform | New Mixins actually apply; dedicated Planet loads and diagnostics remain green |
| Natural placement | Real player/useOn changes the correct PHYSICAL cells; no fixture `setBlock` shortcuts |
| Survival/update | Correct support removal, unrelated neighbor update, waterlogged/scheduled tick preservation |
| Render/BER integration | Correct baked geometry and any special BlockEntityRenderer; Phase 3 evidence linked |
| Alternate authors | dispenser/fake player, click replace, tag state override, structure placement, block entity metadata |
| Acceptance | User-reported check PASS/BLOCKED/FAIL; cross-phase deferral explicitly named |

Never mark a row PASS from "Mixin exists" or green Java compile alone.

## Shared coordinate test grid

For each semantic algorithm family:
- Planet faces: +Y baseline, -Y, +X, -X, +Z, -Z.
- For click: each applicable physical face, body-local orientations
  north/east/south/west relative to local UP, and nearest-look
  vertical ordering; target and player frames may differ.
- Support: local DOWN, local UP, side-directed, center/rigid/isSolid
  predicate; delete right support, delete an irrelevant physical
  neighbor, re-place source; verify no duplicate item/drop.
- At edges: source and target have different canonical frames;
  compare physical position and target state separately. Include
  three-face corners and signed multi-axis offsets only where
  meaningful, respecting deterministic traversal.
- Compare with ordinary vanilla Overworld and upper +Y on Planet.
- Verify client predictive state matches server, without
  invisible/reverted block placement after next tick.
- Reload chunks/save, verify BlockState and BE ownership unchanged.

## Family test stations to add to the automatic six-face lab

The current 20-station fixture covers only a SMALL subset. Extend
from the atlas per mechanism, not one station per arbitrary block:

| Group | Representative natural tests | Additional assertions |
|---|---|---|
| Full 6D FACING | end rod, observer, dispenser, lightning rod, shulker | six clicked sides, neighbor-owned facing, server output/attachment |
| Horizontal FACING | furnace, Ender Chest, chest, barrel, chiseled bookshelf, decorated pot, vault | body-local yaw, independent of physical world vertical, BER vs state |
| 3-axis AXIS | log, chain, rotated pillar | world-to-local axis, hit from six directions, rotated collision shape |
| 16-way rotation | standing signs, hanging sign, banners, skulls | 16 quantized yaw positions; local vertical rotation survives reload |
| FrontAndTop | crafter, jigsaw | orthogonal top/front, both 3D; no illegal FrontAndTop combinations |
| AttachFace | lever, button, grindstone, bell | floor/wall/ceiling, proper support, dynamic removal |
| Half / slab / stair / trapdoor | slab stacking, stairs inner/outer corners, trapdoor top/bottom | boundary click fractions, facing, replacement, waterlogging |
| Pair and multiblock | bed, door, tall flower/double plant, double chest | counterpart state, physical pair, hinge/left-right, seam policy |
| Local-down support | candle, cake-candle, sapling, crop, rail, pressure plate | natural place and survival; user-provided support type exactly preserved |
| Ceiling/side support | hanging spore blossom, lantern, hanging sign, wall sign, cocoa, amethyst cluster | physically correct support, remove/support events |
| Tangent 4-graph | fence, iron bars, wall, fence gate, tripwire | four directions, pillar/post, connect/disconnect near seam |
| Multi-face graph | glow lichen, sculk vein, vine | attach to all 6 supported faces, face removal, replacement |
| Growth column | sugar cane, cactus, bamboo, cave vines, twisting/weeping vines | local top/bottom, multi-tick age growth, no global-Y drift |
| Rail graph | straight, curve, powered, slope | own RailState adjacency, level changes, seam reconnect; vehicle phase later |
| Scaffold/brushable | stacking and cantilever, suspicious sand/gravel | physical support/distances, collapse, scheduled ticks |
| Directed interaction | flint-and-steel, fire charge, bone meal, chiseled bookshelf item-slot | physical hit vs local hit fraction; fire/portal result separate |
| Alternate item placement | ScaffoldingBlockItem, PlaceOnWaterBlockItem, HangingSignItem, dispenser | altered target, no-player context, physical safety |
| State mutation bypass | custom BLOCK_STATE component, `/setblock`, `/fill`, structure paste | canonical state meaning after non-player placement |
| Sign/skull/banner/BE | standing+wall sign, banners, heads, Ender Chest, enchant table | renderer orientation and correct interaction; Phase-3 linked |

A mechanism may require **several** reference blocks; multiple families
can share a tested semantic helper but still need subclass override
coverage before the corresponding row is marked complete.

## Lab ergonomics

Keep the two-lane convention:
- CYAN: preset canonical-state reference block; tests STATE/SHAPE/RENDER,
  **NOT** natural player placement.
- LIME (or appropriate actual soil): EMPTY target for normal player
  BlockItem/useOn; tests natural placement and survival.
- An independent mechanical station for interactions, multiblock
  and maintenance/regrowth; protect existing world builds.

Provide `/planetary test go <face>` navigation, one explicit
per-face reset, in-game family labels and optional inventory
provisioning (creative). Do not generate structures on ordinary
non-Planet Levels or automatically destroy occupied test areas.

## Release-oriented negative checks

- Ordinary non-Planet worlds and model geometry remain vanilla.
- Real NeoForge physical sided capability providers still receive
  physical Direction unless explicitly opted in.
- Clicking the physical side face never changes actual target cell
  to a virtual atlas alias.
- A successful local placement cannot become air after neighbor
  update due to a global-DOWN check.
- An attached fixture preset state must not be interpreted as a
  successful natural-place test.
- Passing face +Y alone cannot close a horizontal-state mechanism.
- BER animation may still be broken even if BlockState is correct:
  track separately with explicit Phase-3 acceptance.
- A modded property or BlockItem subclass unknown to coverage
  registry must be marked "unreviewed", not silently auto-rotated.

## Blocker policy

Phase-2 status may be "partially accepted with named downstream
Phase-3/5/7/9 integrations" but cannot be "complete" if a real
user cannot naturally place a common vanilla block on a valid local
support/face, or if arbitrary creation paths corrupt canonical
states. Every outstanding cross-phase integration must have an
owner and a regression gate.

**No mandatory manual per-block 293-class marathon.** CI exhaustively
groups registry paths and verifies mechanism contracts. User performs
a compact representative all-face/edge batch after each coherent
family wave, with failures identified by station/mechanism name.
