# Planet Test Lab — active, replaceable development acceptance

Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21, branch `2.0`.

## Purpose

The six-face in-game lab is a **temporary, replaceable test scene for the current mechanism families**, NOT a museum or a persistent regression gallery. After successful Java/ASM/JUnit CI, use the scene for real BlockItem / player interactions and visual/movement observations. Older families remain covered by automated regression tests; failures not reproducible by those tests should result in new tests.

**One pad per face; no expanding wings.** The V1 twenty-station particle/placement museum and the separate V1 trapdoor/stair wing are retired. V3 puts current `FenceGateBlock` reference/real-placement stations into the ORIGINAL 41×33 pad. Future feature packets replace the active list and bump the scene marker; do not accumulate old exhibits.

## Start and travel

1. Confirm GitHub Actions passed for the code being tested; then `git pull` and `.\run-client.ps1` (PowerShell).
2. Open the dedicated Planet world, with cheats / permission level 2. Only dev `runClient` sets `-Dplanetary.debug.fixtures.auto=true`; distributed jars do not auto-build this scene.
3. Use `/planetary test go pos_y` / `neg_y` / `pos_x` / `neg_x` / `pos_z` / `neg_z` to teleport between six face interiors, or `/planetary test legend` to see the ACTIVE station list.
4. CYAN beneath a preinstalled `BlockState` is **reference only**. Use the nearby LIME floor spot to place the actual item with the mouse. A preinstalled state never proves `BlockItem`, survival or overrides work.

## V3 current scenario: FenceGateBlock placement / lateral WALLS / OPEN

Replaces all former StairBlock/TrapDoorBlock V2 reference scenes; their regression tests remain in JUnit/ASM. Same existing six face 41×33 pads, no new wings; same CYAN direct-reference vs LIME empty player placement lanes. For any modded gate inheriting the vanilla FenceGateBlock owner, the same mechanism should run; **mod comparator source is documented, actual mod loading not yet accepted**.

| SOUTH row | EAST -8 | EAST +8 |
| --- | --- | --- |
| -12 | Oak, plain CLOSED | Spruce, one lateral wall |
| -4 | Cherry, two lateral walls | Bamboo, OPEN |
| +4 | Crimson, one lateral wall | Warped, one lateral wall |
| +12 | Mangrove, plain CLOSED | Dark oak, OPEN |

For wall stations, prebuilt cobblestone-wall scaffolding is in **physical local-EAST/WEST neighboring cells**, shared by CYAN and LIME placement lanes; both lanes allow real item placement and dynamic wall-adjacency testing. Check that `IN_WALL` changes when a lateral wall is added/removed; check both manually opening the gate from different sides and redstone OPEN/POWERED behavior; check no unrelated changes to actual physical neighboring positions. `getStateForPlacement` is a separate gate from the interaction/neighbor update methods. The references are demonstration states, **not proof of actual placement**.

## Versioned replacement and safety boundary

The original lab had a GOLD marker at local (0, floor, 0), and a historical wing used a LAPIS marker. **V3 has an EMERALD marker at the original pad center.** Build decision:

| State | Automatic action |
| --- | --- |
| EMERALD marker (current V3) | Leave as-is; no rebuilding on every login |
| DIAMOND marker (V2) or GOLD marker (V1) | Replace the owned pad with current V3; if present, also retire the marker-owned old wing |
| Completely empty original reserved volume | Build V3 |
| Unmarked occupied volume | Refuse automatically; leave it untouched |
| Explicit `/planetary test rebuild <face>` | Destroy and regenerate the bounded lab volume on that one face |

Only within the dedicated Planet world. The **automatic migration is confined to former marker-owned dev-lab areas**: original pad EAST -20..20, SOUTH -16..16, radial layers 51..57; retired wing EAST 21..40, SOUTH -12..12, same radial layers, only if its old lapis marker exists. **Player-made blocks INSIDE a marker-owned dev scene can be lost on version upgrade**. Work outside those regions stays untouched. Take a backup if you deliberately build important structures inside this disposable lab.

Available commands: `/planetary test build` safely migrates/builds missing scenes, `/planetary test go <face>` builds/migrates the destination face when needed, and `/planetary test rebuild <face>` explicitly forces a destructive reset of one face's active scene. Bump the current scene marker when changing active station content later (and recognize the previous marker); do not silently modify a current-marked scene in place.

**Previous V2** code CI [#38475081760](https://github.com/Abbygree11/Planetary/actions/runs/38475081760) SUCCESS and user smoke reported «все ок». **Current V3 gate lab CI / actual loaded-client gameplay pending.**

## Independent evidence gates

- CI / JUnit / ASM checks: block-frame conversions, gate wall graph and interaction owner descriptors, migration marker and lab coordinates.
- Dev client: successful gate Mixin application, real placement, interaction and neighbor wall changes in LIME cells on all six face interiors.
- Extra: 12 edges, eight corners, no-Planet Overworld bypass, material behavior, actual loaded third-party mods and dedicated server.

Owner implementation status is in [Phase 2.4A card](../phases/phase-02/05-implementation-a.md); original historical fixture audit remains in [DEBUG_TEST_FIXTURES](../research/DEBUG_TEST_FIXTURES_1_21_1.md), not as a requirement to retain those exhibits in the game.
