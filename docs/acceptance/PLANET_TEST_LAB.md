# Planetary test lab — automatic six-face development fixtures

Target: Minecraft 1.21.1 / NeoForge 21.1.215, `2.0`.

**Status: Java 21 compile + all JUnit PASSED on GitHub CI (including updated natural support stations); client fixture and Phase-2 gameplay acceptance pending.** Passing JUnit
or smoke startup alone does not prove individual placement/render cases.

## Quick use (development `runClient` only)

1. `git pull && .\run-client.ps1` AFTER GitHub Actions CI is green.
2. Enter the **dedicated Planet** dimension/world. Local
   `runClient` has the JVM-only flag
   `-Dplanetary.debug.fixtures.auto=true` and builds identical
   fixture pads on the six face interiors only if the reserved
   regions contain no player blocks.
3. The brand-new test world player starts on the +Y lab pad;
   already-playing/saved-world players are **not teleported** if
   they are away from their original spawn.
4. Observe the already built CYAN reference samples; use the
   adjacent EMPTY area with a LIME floor cell to place the same
   objects naturally with the mouse. The reference was built by a
   server fixture and must not be mistaken for proof of BlockItem
   placement/survival.
5. Switch faces using in-game creative/cheats commands:
   `/planetary test go pos_y`
   `/planetary test go neg_y`
   `/planetary test go pos_x`
   `/planetary test go neg_x`
   `/planetary test go pos_z`
   `/planetary test go neg_z`.
   The command teleports to a safe local-up point above that face
   and prints the station legend.

Commands require permission level 2 (enable cheats in local world).
They only work in the world powered by `PlanetChunkGenerator`.

## Test builder and safety

`/planetary test build`: safely build missing labs if the reserved
regions are empty; skip already marked or occupied face regions.
Do not remove existing blocks.

`/planetary test legend`: print row order; no mutation.

`/planetary test rebuild <face>`: explicitly DESTROY/replace the
reserved 41x33 pad plus six local-up layers on ONE selected face.
This erases anything a player built within that area; it never
touches other faces or ordinary Overworld. Use only when you want
to reset that exact pad. Never run destructively automatically.

A gold floor marker is written after successful generation; it is
persistent and prevents redundant rebuilds on later login. On
existing dev saves, any non-air cell in the reserved volume
prevents automatic generation on that face. Development auto mode
is configured only on the Gradle client run model. Distributed
mod jars never auto-generate test structures unless explicitly
launched with this JVM property.

Geometry per face:
- core (0,128,0), radius 48;
- pad floor local UP radial distance 51;
- references radial distance 52; 41x33 platform;
- five columns of stations (local east -16,-8,0,8,16), four
  rows (local south -12,-4,4,12);
- each station has CYAN = direct reference (x-1),
  LIME = player-placement spot (x+1);
- Portal frame uses local SOUTH/UP plane so its structure does
  not block adjacent user placement space.
- One bounded build per face, no per-tick generator and no global
  changes to worldgen/BlockPos/particle algorithms.

## Station rows

| Row (south) | Column 1 | Column 2 | Column 3 | Column 4 | Column 5 |
|---|---|---|---|---|---|
| 1 | End Rod | Ender Chest | Enchanting Table | Hanging Spore Blossom | Dripstone/water-drip scaffold |
| 2 | 4 candles | Candle cake | Torch | Persistent Cherry Leaves | Redstone Torch |
| 3 | Campfire | Soul Campfire | Fueled Furnace | Fueled Blast Furnace | Fueled Smoker |
| 4 | Brewing Stand | Charged Respawn Anchor | Enchanting Table + Bookshelves | Unlit Obsidian Portal Frame | Obsidian Flint/Steel interaction |

Flint-and-steel station intentionally stays unlit to test the
real `useOn` ignition path. Furnace block entities receive input
and coal to avoid extinguishing immediately after setBlock.
Spore blossom is placed below a real physical local-UP supporting
stone. The empty GREEN placement location also has a physical
LOCAL-UP stone ceiling so players can use normal placement there.

The Cherry Leaves station has a preinstalled leaf block on CYAN.
Its GREEN adjacent placement spot uses natural **grass block soil**
instead of lime concrete so that a cherry sapling can be planted
through the actual `BushBlock.mayPlaceOn` path. Tree growth/light
behavior remains a separate Phase-2/10 acceptance item.

Discrepancy between a visible reference and a failed adjacent
player placement is a **Phase-2 mechanism** failure, not evidence
that particles are broken.

The water-drip slot is currently a scaffold; the fluid-source
and drip emitter graph remains Phase 4/5 integration, so a
drip source at that stand is **not yet acceptance evidence**.
The portal's ignition/shape is Phase 9, not a particle emitter fix.

## Phase 2.4A orientation-family acceptance — REQUIRED use of this lab

**2026-10-14 process correction:** The test lab already exists and must be the default in-game acceptance environment for every new placement/orientation family, rather than repeatedly asking for arbitrary ad-hoc world builds. The user called out the omission after manual 2.4A-4 TrapDoorBlock acceptance. Code-level JUnit/ASM remain separate; 'looks OK' in one tested location does not prove all six faces and edges.

- Before asking for gameplay testing: point to this lab and give `/planetary test go pos_y` / `neg_y` / `pos_x` / `neg_x` / `pos_z` / `neg_z` (requires cheats and the dedicated Planet world); use `/planetary test legend` for current station layout.
- The original 20 CYAN/LIME stations do not represent trapdoor/stair acceptance. **2.4A-5 adds a separately marked placement wing** on each face; use its eight named trapdoor/stair stations for these families without rebuilding the original pad. Both direct reference and empty natural-placement lanes are present. Code/CI and actual loaded-client acceptance are separate.
- Future extension must be **non-destructive and versioned**, with independent reserved slots/markers/checks so a saved 41x33 face lab and player builds stay untouched. Do not change `STATIONS` count, occupied marker semantics, or silently invoke destructive `/planetary test rebuild <face>` on previously built worlds.
- Use CYAN=direct canonical reference only for render/state contrast; LIME=empty natural BlockItem/useOn lane. Compare both plus original hit versus final target, local FACING/HALF and game behavior; log deviations, not repeated generic test instructions.
- Existing pads cover the **six face interiors**, not all **12 actual gravity edges and eight corners**. Edge/corner acceptance needs separate non-destructive additional fixtures or navigation and must not be silently counted as PASSED from a face pad.
- Keep loaded-mod inherited and independent owner coverage, classic Overworld no-Planet, vanilla item components/BlockItem state overrides, dispenser/no-player authors and dedicated server runtime as distinct acceptance gates.

**For now:** actual 2.4A-4 gameplay user response 'всё ок' = **no reported trapdoor regressions in inspected play**, not 20-material × 6/12/8 or real-mod release acceptance. Next placement implementation packet must first make its lab usage explicit and add matching dedicated fixtures without deleting the existing six-face lab.

## Independent placement wing — 2.4A-5 (gameplay pending)

Created by `PlanetTestFixtures.buildPlacementWing` **only when the entire independent region is empty**. Same dev-only auto mode; `/planetary test build` also builds missing wings safely, and `/planetary test go <face>` builds/points to the wing on that face. The original 41x33 face-pad is **untouched**. A different LAPIS marker records successful construction. If any wing cell is occupied and there is no valid marker, the wing is skipped; existing player builds are preserved.

**Geometry:** local EAST x=21..40 (connected to old pad at x=20), local SOUTH z=-12..12, floor radial distance 51, references radial 52. Stations have CYAN direct reference at local east center-1 and LIME floor below the empty player BlockItem/useOn slot at east center+1. The wing is outside the old pad footprint and has its own per-face non-overlap geometry tests.

| local SOUTH | EAST 27: trapdoors | EAST 35: stairs |
| --- | --- | --- |
| -9 | Oak bottom | Oak straight |
| -3 | Iron top | Quartz outer-left with supporting front neighbor |
| 3 | Copper trapdoor | Cut-copper inner-right with supporting rear neighbor |
| 9 | Waxed copper trapdoor | Waxed cut-copper top |

**How to verify after green CI:** launch `run-client.ps1`, enter the dedicated Planet world, issue `/planetary test go pos_y`, look to the **east/right** of the existing lab, or use `/planetary test legend`. Repeat on `neg_y`, `pos_x`, `neg_x`, `pos_z`, `neg_z`. At each station compare preset CANONICAL reference with freshly placed same-material block on adjacent LIME floor cell; for quartz/cut copper stairs, preset neighboring stairs at both reference and natural lanes allow testing real `SHAPE`. Then test removal/addition of neighbor to trigger `updateShape`. Preserve material identity and WATERLOGGED state. Redstone, weathering, custom mod authors and actual fluids require additional distinct scenarios; the wing does not automatically claim coverage.

**Critical:** Neither the original face-pad nor this wing reaches true 12 seams / 8 corners; separate *non-destructive* edge/corner fixtures and loaded mod tests remain PENDING. `/planetary test rebuild <face>` is destructive for the OLD lab only; it does not reset this wing. Never issue destructive rebuild to regenerate the wing. Real game acceptance is NOT proved by reference installation or JUnit alone.

## Acceptance procedure

Use one pass through the six pads, report only anomalies:
- direct reference orientation and render;
- adjacent natural placement + support/survival;
- idle particles' origin and gravity;
- ignition and enchanting power when applicable;
- +/-X, +/-Z, -Y versus +Y baseline;
- short seam/performance smoke and regression.

Document passed mechanics without requiring redundant replays.
Current state and blocked cases:
`docs/research/DEBUG_TEST_FIXTURES_1_21_1.md`,
`docs/acceptance/PHASE_4_PARTICLES.md`,
`docs/IMPLEMENTATION_PLAN.md`.
