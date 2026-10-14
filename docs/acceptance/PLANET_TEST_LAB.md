# Planet Test Lab — V6 catch-up acceptance (2.4A-6, -7, -8)

Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21, branch `2.0`.

## Why V6 includes three families

Normally the lab is **replaceable and current-only**; JUnit/ASM preserve regression coverage and historical scenes are not retained. The game-client checks for `2.4A-6` and `2.4A-7` were accidentally skipped when previous long chats lost connection. **Temporary exception requested by the user:** restore these two whole-owner families beside current `2.4A-8`, then resume the usual active-only replacement policy after manual acceptance.

This is still ONE lab pad per face, within the original 41×33 floor; no expanding wings, no old unrelated museum. The lab is populated with reference **blocks** rather than dropping item entities. Natural item placement must be done by the player.

## Commands and surface convention

1. After green CI, pull `2.0` and run `.\run-client.ps1`. Only the dev client enables `planetary.debug.fixtures.auto`.
2. Enter the dedicated Planet world with permission level 2. Use `/planetary test build`, `/planetary test legend`, then `/planetary test go pos_y` (or `neg_y`, `pos_x`, `neg_x`, `pos_z`, `neg_z`).
3. **CYAN**: preinstalled target-canonical BlockState reference. **LIME**: adjacent empty natural-placement lane. Take a corresponding item from the creative inventory and place it via actual mouse click. Never interpret a reference fixture as a successful BlockItem test.
4. Use `/planetary test rebuild <face>` only for a deliberate destructive reset of this one face's bounded development pad.

Coordinates are in local EAST / SOUTH of each face; the compact V6 grid has four EAST columns (`-15, -5, +5, +15`) and six SOUTH rows (`-12, -7, -2, +3, +8, +13`). Each 5×5 reference/placement/support footprint is isolated.

## Active V6 station grid

| SOUTH | EAST -15 | EAST -5 | EAST +5 | EAST +15 |
| --- | --- | --- | --- | --- |
| -12 | Oak gate plain | Spruce gate + 1 wall | Cherry gate + 2 walls | Bamboo gate open |
| -7 | Crimson gate wall | Warped gate wall | Mangrove gate plain | Dark oak gate open |
| -2 | Furnace north | Smoker east | Blast furnace south | Furnace west |
| +3 | Smoker LIT | Blast furnace LIT | Furnace LIT | Smoker north |
| +8 | Oak log local Y | Oak log local X | Stripped oak log local Z | Waterlogged chain |
| +13 | Basalt local X | Hay bale local Y | Infested deepslate local Z | Crimson stem local X |

## Manual checklist — run independently on all six faces

**`2.4A-6` FenceGateBlock (SOUTH rows -12, -7):** place actual gate items facing different player headings into LIME lanes; open/close from both sides, check FACING and OPEN; check IN_WALL with adjacent cobblestone wall, after adding/removing a wall, and with redstone (OPEN/POWERED). For cherry, walls flank both reference and natural lanes. Test vanilla materials plus a truly loaded version-compatible inheriting mod gate and, separately, an independently authored gate. Mod source inheritance alone != mod gameplay PASS.

**`2.4A-7` AbstractFurnaceBlock (SOUTH rows -2, +3):** place furnace, smoker and blast furnace from all four actor headings; compare FACING and identity, interact with GUI/inventory, process appropriate fuel/recipes and observe LIT transitions and particle/sound orientation. Preinstalled LIT references are static state demonstrations, **not proof** that a recipe ran. Repeat in an ordinary no-Planet world. Independent mod machines/overrides are separate, not assumed fixed.

**`2.4A-8` RotatedPillar/Chain/Infested (SOUTH rows +8, +13):** place actual X/Y/Z axis blocks. For X click the supplied support at station-local EAST+2; for Z click station-local SOUTH+1 support; for Y click the LIME floor. Confirm AXIS and Chain WATERLOGGED after actual water placement; verify infested deepslate and alternate materials. A mod log inheriting RotatedPillarBlock needs a real loaded compatible version for gameplay evidence; mod worldgen remains independent.

**Across all three:** also check the 12 physical cube edges, eight corners, and ordinary non-Planet Overworld. The lab is on face interiors, so edges/corners must be tested separately. Confirm client startup/Mixin application and dedicated-server use separately from JUnit. Generic missing side-quads on cube seams are tracked in Phase 3B, not resolved by this lab.

## Automatic migration and protection

**V6 marker = OBSIDIAN**, located at local (0, floor, 0). All known predecessor markers auto-migrate: V5 AMETHYST; V4 NETHERITE; V3 EMERALD; V2 DIAMOND; V1 GOLD. An already-current V6 pad is untouched on login. An entirely empty reserved volume can be built. Any *unmarked* occupied volume is preserved, unless the player explicitly requests `rebuild <face>`.

Migration clears **only** the marker-owned existing lab inside EAST -20..+20, SOUTH -16..+16, radial floor `FLOOR_RADIUS` through `FLOOR_RADIUS+6`. The old V1 secondary wing (EAST 21..40, SOUTH -12..12) is cleared **only** if its historical LAPIS marker exists. Structures built inside a previously marker-owned dev pad may be replaced; outside that pad stay unchanged. Keep important builds elsewhere.

CI PASS means compile/JUnit/ASM, not applied client Mixins or real gameplay. Prior relevant CI: gate V3 [#38478594846](https://github.com/Abbygree11/Planetary/actions/runs/38478594846), furnace V4 [#38622053271](https://github.com/Abbygree11/Planetary/actions/runs/38622053271), axis V5 [#38643250260](https://github.com/Abbygree11/Planetary/actions/runs/38643250260). The new V6 CI/game statuses must be tracked separately.
