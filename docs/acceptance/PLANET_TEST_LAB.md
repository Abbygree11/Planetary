# Planet Test Lab — active, replaceable development acceptance

Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21, branch `2.0`.

## Purpose

The six-face in-game lab is a **temporary, replaceable test scene for the current mechanism families**, NOT a museum or a persistent regression gallery. After successful Java/ASM/JUnit CI, use the scene for real BlockItem / player interactions and visual/movement observations. Older families remain covered by automated regression tests; failures not reproducible by those tests should result in new tests.

**One pad per face; no expanding wings.** The V1 twenty-station particle/placement museum and the separate V1 trapdoor/stair wing are retired. V2 puts current `TrapDoorBlock` and `StairBlock` reference/real-placement stations into the ORIGINAL 41×33 pad. Future feature packets replace the active list and bump the scene marker; do not accumulate old exhibits.

## Start and travel

1. Confirm GitHub Actions passed for the code being tested; then `git pull` and `.\run-client.ps1` (PowerShell).
2. Open the dedicated Planet world, with cheats / permission level 2. Only dev `runClient` sets `-Dplanetary.debug.fixtures.auto=true`; distributed jars do not auto-build this scene.
3. Use `/planetary test go pos_y` / `neg_y` / `pos_x` / `neg_x` / `pos_z` / `neg_z` to teleport between six face interiors, or `/planetary test legend` to see the ACTIVE station list.
4. CYAN beneath a preinstalled `BlockState` is **reference only**. Use the nearby LIME floor spot to place the actual item with the mouse. A preinstalled state never proves `BlockItem`, survival or overrides work.

## V2 current scenario: placement and neighbor SHAPE

All station positions are within local EAST x=-20..20, local SOUTH z=-16..16, at radial floor distance 51 (core 0,128,0; radius 48). Each has a direct reference at station east-1 and an empty natural-placement cell at east+1. This is the only active layout:

| Row | EAST -8: trapdoors | EAST +8: stairs |
| --- | --- | --- |
| 1, SOUTH -12 | Oak bottom | Oak straight |
| 2, SOUTH -4 | Iron top | Quartz OUTER_LEFT (+ reference and natural neighbor) |
| 3, SOUTH +4 | Copper trapdoor | Cut-copper INNER_RIGHT (+ reference and natural neighbor) |
| 4, SOUTH +12 | Waxed copper trapdoor | Waxed cut-copper top |

Natural lane starts EMPTY, aside from intentional neighbor scaffolds for the two stair-corner cases. Verify FACING/HALF from real clicked side/height, add and remove neighboring stairs and verify `SHAPE` changes, check different materials and waterlogging as separate interactions.

Six face interiors **do not** prove behavior at the planet's 12 true edges / 8 corners, or with independent third-party mod item/BlockState overrides, custom model pipelines, virtual worlds, or a dedicated server. Those remain separate explicit acceptance gates. The global any-block seam rendering issue belongs to Phase 3B, not the test scene.

## Versioned replacement and safety boundary

The original lab had a GOLD marker at local (0, floor, 0), and a historical wing used a LAPIS marker. **V2 has a DIAMOND marker at the original pad center.** Build decision:

| State | Automatic action |
| --- | --- |
| DIAMOND marker (current V2) | Leave as-is; no rebuilding on every login |
| GOLD marker (recognized old lab) | Replace the entire old pad with current V2; if present, also clear the marker-owned old wing |
| Completely empty original reserved volume | Build V2 |
| Unmarked occupied volume | Refuse automatically; leave it untouched |
| Explicit `/planetary test rebuild <face>` | Destroy and regenerate the bounded lab volume on that one face |

Only within the dedicated Planet world. The **automatic migration is confined to former marker-owned dev-lab areas**: original pad EAST -20..20, SOUTH -16..16, radial layers 51..57; retired wing EAST 21..40, SOUTH -12..12, same radial layers, only if its old lapis marker exists. **Player-made blocks INSIDE a marker-owned dev scene can be lost on version upgrade**. Work outside those regions stays untouched. Take a backup if you deliberately build important structures inside this disposable lab.

Available commands: `/planetary test build` safely migrates/builds missing scenes, `/planetary test go <face>` builds/migrates the destination face when needed, and `/planetary test rebuild <face>` explicitly forces a destructive reset of one face's active scene. Bump the current scene marker when changing active station content later (and recognize the previous marker); do not silently modify a current-marked scene in place.

**Build verification (2026-10-14):** [GitHub Actions #38475081760](https://github.com/Abbygree11/Planetary/actions/runs/38475081760) SUCCESS on code SHA 35e8910: Java21 production/test compilation, complete JUnit and NeoForge 21.1.215 registry. Automatic V1-to-V2 migration in an actual running client and all six real natural-placement lanes **not yet verified**.

## Independent evidence gates

- CI / JUnit / ASM checks: block-frame conversions, stair shape semantics, owner descriptors and test-lab coordinates/marker policy.
- Dev client: successful Mixin application and real placement in LIME cells on all six face interiors.
- Extra: 12 edges, eight corners, no-Planet Overworld bypass, material behavior, actual loaded third-party mods and dedicated server.

Owner implementation status is in [Phase 2.4A card](../phases/phase-02/05-implementation-a.md); original historical fixture audit remains in [DEBUG_TEST_FIXTURES](../research/DEBUG_TEST_FIXTURES_1_21_1.md), not as a requirement to retain those exhibits in the game.
