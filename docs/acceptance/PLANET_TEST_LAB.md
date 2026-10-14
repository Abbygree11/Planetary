# Planet Test Lab — active, replaceable development acceptance

Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21, branch `2.0`.

## Purpose

The six-face in-game lab is a **temporary, replaceable test scene for the current mechanism families**, NOT a museum or a persistent regression gallery. After successful Java/ASM/JUnit CI, use the scene for real BlockItem / player interactions and visual/movement observations. Older families remain covered by automated regression tests; failures not reproducible by those tests should result in new tests.

**One pad per face; no expanding wings.** The V1 twenty-station particle/placement museum and the separate V1 trapdoor/stair wing are retired. V4 puts current `AbstractFurnaceBlock` furnace/smoker/blast-furnace reference/real-placement stations into the ORIGINAL 41×33 pad. Future feature packets replace the active list and bump the scene marker; do not accumulate old exhibits.

## Start and travel

1. Confirm GitHub Actions passed for the code being tested; then `git pull` and `.\run-client.ps1` (PowerShell).
2. Open the dedicated Planet world, with cheats / permission level 2. Only dev `runClient` sets `-Dplanetary.debug.fixtures.auto=true`; distributed jars do not auto-build this scene.
3. Use `/planetary test go pos_y` / `neg_y` / `pos_x` / `neg_x` / `pos_z` / `neg_z` to teleport between six face interiors, or `/planetary test legend` to see the ACTIVE station list.
4. CYAN beneath a preinstalled `BlockState` is **reference only**. Use the nearby LIME floor spot to place the actual item with the mouse. A preinstalled state never proves `BlockItem`, survival or overrides work.

## V4 current scenario: AbstractFurnaceBlock horizontal placement

The current mechanism is the inherited placement of `FurnaceBlock`, `SmokerBlock` and `BlastFurnaceBlock`. All eight stations lie on the same original 41×33 face pads. CYAN is a directly installed block-state reference, LIME is an **empty actual player placement** lane; do not mistake a forced state for correct BlockItem behavior. Previous V3 fence-gate fixtures are retired, not preserved as a museum.

| SOUTH row | EAST -8 | EAST +8 |
| --- | --- | --- |
| -12 | Furnace FACING NORTH | Smoker FACING EAST |
| -4 | Blast furnace FACING SOUTH | Furnace FACING WEST |
| +4 | Smoker LIT reference | Blast furnace LIT reference |
| +12 | Furnace LIT reference | Smoker FACING NORTH |

The LIT=true reference is a static visual/state fixture, **not proof of actual burning**. Verify natural item placement with player facing four local tangent directions and each of the six gravity faces, FACING opposite actor local horizontal, successful GUI opening and normal cooking, independent LIT behavior, and visible modded/subclass behavior only when real compatible mods are loaded. Do not globally reorient world coordinates or recipe/block entity state.

## Versioned replacement and safety boundary

The original lab had a GOLD marker at local (0, floor, 0), and a historical wing used a LAPIS marker. **V4 has a NETHERITE marker at the original pad center.** Build decision:

| State | Automatic action |
| --- | --- |
| NETHERITE marker (current V4) | Leave as-is; no rebuilding on every login |
| EMERALD (V3), DIAMOND (V2) or GOLD (V1) marker | Replace the owned pad with V4; if present, also retire the marker-owned old wing |
| Completely empty original reserved volume | Build V4 |
| Unmarked occupied volume | Refuse automatically; leave it untouched |
| Explicit `/planetary test rebuild <face>` | Destroy and regenerate the bounded lab volume on that one face |

Only within the dedicated Planet world. The **automatic migration is confined to former marker-owned dev-lab areas**: original pad EAST -20..20, SOUTH -16..16, radial layers 51..57; retired wing EAST 21..40, SOUTH -12..12, same radial layers, only if its old lapis marker exists. **Player-made blocks INSIDE a marker-owned dev scene can be lost on version upgrade**. Work outside those regions stays untouched. Take a backup if you deliberately build important structures inside this disposable lab.

Available commands: `/planetary test build` safely migrates/builds missing scenes, `/planetary test go <face>` builds/migrates the destination face when needed, and `/planetary test rebuild <face>` explicitly forces a destructive reset of one face's active scene. Bump the current scene marker when changing active station content later (and recognize the previous marker); do not silently modify a current-marked scene in place.

**Previous V2** code CI [#38475081760](https://github.com/Abbygree11/Planetary/actions/runs/38475081760) SUCCESS and user smoke reported «все ок». **Previous V3 gate lab:** [CI #38478594846](https://github.com/Abbygree11/Planetary/actions/runs/38478594846) SUCCESS, real gate gameplay/mod JAR acceptance still OPEN. **Current V4 furnace-family [CI #38622053271](https://github.com/Abbygree11/Planetary/actions/runs/38622053271) SUCCESS / Minecraft gameplay PENDING**.

## Independent evidence gates

- CI / JUnit / ASM checks: block-frame conversions, furnace placement owner and FACING/LIT state, migration marker and lab coordinates.
- Dev client: successful furnace Mixin application, real facing/GUI/cooking and independent LIT checks in LIME cells on all six face interiors.
- Extra: 12 edges, eight corners, no-Planet Overworld bypass, material behavior, actual loaded third-party mods and dedicated server.

Owner implementation status is in [Phase 2.4A card](../phases/phase-02/05-implementation-a.md); original historical fixture audit remains in [DEBUG_TEST_FIXTURES](../research/DEBUG_TEST_FIXTURES_1_21_1.md), not as a requirement to retain those exhibits in the game.
