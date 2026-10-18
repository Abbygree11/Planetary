# Planet Test Lab — V8 catch-up + ROTATION_16 (2.4A-6 through 2.4A-10)

Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21, branch `2.0`.

## Phase 3C-1 — render-only V8 regression (2026-10-18)

**No fixture change / no V9.** The 42 stations per face remain exactly where they were. The renderer code [ea04238](https://github.com/Abbygree11/Planetary/commit/ea0423894e2c1e92a5d74e81c87543be48bd9453) [CI #39081259366](https://github.com/Abbygree11/Planetary/actions/runs/39081259366) SUCCESS adds the four rigid block-local BER frame adapters to vanilla SignRenderer/HangingSignRenderer/BannerRenderer/SkullBlockRenderer. Static BakedModel and physical light are unaffected.

**Real client comparison:** `git pull && .\\test.ps1 && .\\run-client.ps1`; enter Planet +Y and five rotated face labs. Compare actual F3 block IDs and full property state of CYAN and naturally placed LIME and deliberately reproduce the same `rotation` and `attached`. The CYAN preset is NOT placement acceptance. For hanging signs, `attached=true` vs `false` selects different chain models by design: don't infer defect only because a reference and a naturally placed item used different `attached`. Check (a) standing sign board AND front/back text both locally upright, (b) banner pole/cloth and pattern upright, (c) all skull/head variants and player skin locally upright, (d) hanging sign plank and all chains point toward the actual local ceiling and text is readable. Toggle `/planetary test light off` to distinguish real lighting/shadows from geometry. Also compare `+Y` with no-Planet Overworld (should be identical), ordinary item/GUI previews, modded custom BER only when actually loaded; edges and corners independently.

**If still wrong on +Y**, the new renderer adapter cannot explain it, because it is identity on `POS_Y`. Report block ID, exact F3 `rotation`/`attached`, whether block is CYAN reference or LIME player item, local face, screenshots before/after same state. Do not change the placement algorithm based on a different preset state or rewrite the global BlockEntityRenderDispatcher.

## Why V8 retains earlier families

Normally the lab is **replaceable and current-only**; JUnit/ASM preserve regression coverage and historical scenes are not retained. The game-client checks for `2.4A-6` and `2.4A-7` were accidentally skipped when previous long chats lost connection. **Temporary exception requested by the user:** restore these two whole-owner families beside `2.4A-8`, then resume the usual active-only replacement policy after manual acceptance. **V8 retains all 36 V7 stations** (including 24 V6 stations) and adds six ROTATION_16 stations for `2.4A-10`. The old items remain available for the delayed manual checks.

This is still ONE lab pad per face, within the original 41×33 floor; no expanding wings, no old unrelated museum. The lab is populated with reference **blocks** rather than dropping item entities. Natural item placement must be done by the player.

## Temporary six-face developer visibility

In the dev `runClient` only, `-Dplanetary.debug.visibility.nightVision=true` adds **vanilla night vision** to players in the dedicated Planet dimension on all six faces. This is deliberately NOT a physical skylight/AO/block-light solution, and the final lighting/shadow behavior will still require Phase 3B. It does not change light levels, spawning, crop behavior, or mod rendering pipelines. The visual result is **brighter**, not necessarily pixel-identical to the +Y sky and biome colors.

- `/planetary test light off`: disable the visual helper for the issuing player and examine actual dark/underside lighting.
- `/planetary test light on`: restore the helper for the issuing player while in the Planet world.
- The effect is only granted in the dev JVM profile. Natural potion/mod night vision is intentionally left alone; the tracked dev effect is removed after leaving Planet / disconnecting.
- Actual mod, shader, shadow, AO, cave and edge tests must also be repeated with the helper OFF. Green JUnit is not real applied client evidence.

## Commands and surface convention

1. After green CI, pull `2.0` and run `.\run-client.ps1`. Only the dev client enables `planetary.debug.fixtures.auto`.
2. Enter the dedicated Planet world with permission level 2. Use `/planetary test build`, `/planetary test legend`, then `/planetary test go pos_y` (or `neg_y`, `pos_x`, `neg_x`, `pos_z`, `neg_z`).
3. **CYAN**: preinstalled target-canonical BlockState reference. **LIME**: adjacent empty natural-placement lane. Take a corresponding item from the creative inventory and place it via actual mouse click. Never interpret a reference fixture as a successful BlockItem test.
4. Use `/planetary test rebuild <face>` only for a deliberate destructive reset of this one face's bounded development pad.

Coordinates are in local EAST / SOUTH of each face; the retained V6 grid has four EAST columns (`-15, -5, +5, +15`) and the V7/V8 FrontAndTop + ROTATION_16 stations occupy three interstitial EAST columns (`-10, 0, +10`). Both share six SOUTH rows (`-12, -7, -2, +3, +8, +13`). Each 5×5 reference/placement/support footprint is isolated.

## Retained 24 V6 stations (unchanged positions)

| SOUTH | EAST -15 | EAST -5 | EAST +5 | EAST +15 |
| --- | --- | --- | --- | --- |
| -12 | Oak gate plain | Spruce gate + 1 wall | Cherry gate + 2 walls | Bamboo gate open |
| -7 | Crimson gate wall | Warped gate wall | Mangrove gate plain | Dark oak gate open |
| -2 | Furnace north | Smoker east | Blast furnace south | Furnace west |
| +3 | Smoker LIT | Blast furnace LIT | Furnace LIT | Smoker north |
| +8 | Oak log local Y | Oak log local X | Stripped oak log local Z | Waterlogged chain |
| +13 | Basalt local X | Hay bale local Y | Infested deepslate local Z | Crimson stem local X |

## Preserved 12 V7 FrontAndTop stations

The new six-front *Crafter* and six-front *Jigsaw* stations fit between the old columns on the same 41×33 pad. They are independent 5×5 footprints and preserve every V6 test case.

| SOUTH | EAST -10 | EAST 0 | EAST +10 |
| --- | --- | --- | --- |
| -12 | Crafter NORTH_UP | Crafter SOUTH_UP | Crafter EAST_UP |
| -7 | Crafter WEST_UP | Crafter UP_NORTH | Crafter DOWN_EAST |
| -2 | Jigsaw NORTH_UP | Jigsaw SOUTH_UP | Jigsaw EAST_UP |
| +3 | Jigsaw WEST_UP | Jigsaw UP_SOUTH | Jigsaw DOWN_WEST |

**Crafter user report 2026-10-16 (2.4A-9 regression):** screenshot showed preset CYAN Crafter with the RED 3x3 TOP grid facing sideways and naturally placed LIME Crafter with the RED 3x3 TOP grid facing UP (the wooden output mouth is on its horizontal FRONT). Source audit identified a real Planet adapter bug, NOT enough evidence for a renderer bug: vanilla `CrafterBlock.getStateForPlacement` calls **singular** `BlockPlaceContext.getNearestLookingDirection()` (raw `Direction.orderedByNearest(player)[0]`, actor view only). We incorrectly used Planet `localNearestLookingDirections()` (**plural**) and reordered clicked-floor UP normal to DOWN, forcing Crafter FRONT=UP even when the player's body view was horizontal. The 2.4A-9 correction uses `PlanetFrameApi.localRawNearestLookingDirection()`, target chart-aware without clicked-face priority. Jigsaw deliberately remains clicked-normal authored; all consumers of plural order remain unchanged.

**Recheck after green CI for the Crafter correction:** place Crafter onto the LIME floor while looking approximately horizontally (yaw toward target, pitch close to 0), then verify with F3 Targeted Block `orientation`: the original state should have horizontal FRONT (wooden output mouth) and local UP as TOP (red 3x3 grid). Repeat with actor looking steeply DOWN: FRONT should become UP (wooden mouth upward), with RED 3x3 TOP grid on a lateral face — a valid distinct state. Repeat all six faces; check actor/target crossing at a physical edge and ordinary non-Planet world. If `orientation` is correct but rendered textures disagree, record a separate Phase 3 render issue. If the values disagree, supply the face, actor yaw/pitch and targeted state, not just a visual comparison of differing placement intents. **Do not force natural block orientation to equal CYAN unless you deliberately reproduced CYAN's author direction.**

**Natural placement rules:** use an actual Crafter or Jigsaw item in the **empty LIME** lane. Crafter's front is chosen from the player's viewing direction, not the clicked wall; for front UP look DOWN, for front DOWN look UP and click the supplied overhead support's underside. Jigsaw's front is taken from the clicked physical face: for local NORTH click the supplied SOUTH+1 support, for SOUTH the NORTH-1 support, for EAST the WEST/center support, for WEST the EAST+2 support, for DOWN the overhead underside, and for UP the LIME floor. Observe the resulting `orientation` state: the CYAN reference is **not** proof of a real click path. You may need operator permissions to obtain a Jigsaw block: `/give @s minecraft:jigsaw`. For vertical fronts, rotate player heading to exercise additional TOP choices; the 12 legal `FrontAndTop` states are covered by JUnit but not all represented as preset reference stations.

## 2026-10-17: Crafter top-vs-front clarification after user smoke test

**Important texture distinction:** RED 3×3 gridded panel is the **TOP** of Crafter, NOT its output FRONT. The **wooden mouth / face** is the output FRONT. A player looking roughly horizontally during placement usually gets horizontal FRONT with red grid UP; looking steeply down gives FRONT UP and the red grid on a vertical side. The exact block point clicked does not author Crafter orientation: aiming at the center vs edge from the same player position changes view **pitch** and may cross the nearest-direction threshold.

The user reports exactly this center-vs-edge change, including on +Y. **Consistent with vanilla orientation, not by itself a new defect**, but no full six-face gameplay PASS: request a screenshot of F3 targeted `orientation` and view pitch if there is a mismatch against the same ordinary Overworld placement under the same yaw/pitch. Prior source bug (singular vs plural look) was fixed in [04a3535](https://github.com/Abbygree11/Planetary/commit/04a35357b2126e805a1313cf17eef1563336633b), [CI #38953937805](https://github.com/Abbygree11/Planetary/actions/runs/38953937805) SUCCESS. This note is docs-only; NO new runtime code or CI claimed.

## Additional six V8 ROTATION_16 stations

These six new cases fill the last free positions of the existing seven-column, six-row grid, leaving **42 independent 5×5 station footprints inside each existing 41×33 pad** (six pads total). Reference states are direct *CYAN* setBlock and **not successful placement**; *LIME* remains empty for actual item use. For hanging-sign stations, a separate solid **local-UP overhead support** is installed above each CYAN and LIME lane.

| SOUTH | EAST -10 | EAST 0 | EAST +10 |
| --- | --- | --- | --- |
| +8 | Oak standing sign ROTATION=0 | Spruce standing sign ROTATION=4 | White standing banner ROTATION=8 |
| +13 | Player head ROTATION=12 | Oak ceiling hanging sign ATTACHED=true | Cherry ceiling hanging sign ATTACHED=false |

**`2.4A-10` owner acceptance:** from +Y and each rotated face, use actual sign/banner/player head/hanging sign **items** and vary actor yaw through all 16 segments (~22.5° steps). Verify the `rotation` number in F3 Targeted Block and that it describes the block's canonical **local tangent plane** (the sign/banner/player-head visual face may require independent Phase 3 BER corrections). Sign and banner yaw use `context.getRotation()+180`, SkullBlock uses original `context.getRotation()`. For the ceiling: clicking a real support underside produces hanging variant (item selects wall vs ceiling independently); ordinary rigid support gives four-quadrant `attached=false`, sneak or non-full support gives full 16-angle `attached=true`. Manually check ceiling support removal, non-Planet placement, changing sign text/wax, banner layers, powered skull variants, waterlogged sign, and a real loaded mod inheritor if available. Text, renderer and structure placement are not certified by placement orientation CI.

**Temporary catch-up policy:** retain original 24 fence-gate/furnace/AXIS and 12 Crafter/Jigsaw stations until user's deferred manual acceptance is complete; do not drop tests simply because a new family was added. New V8 marker PURPUR_BLOCK automatically migrates only previously owned V7 and older markers; manually placed blocks *inside* the old pad will be cleared during migration, but previous test stations return at their old coordinates. No separate wing.

## Manual checklist — run independently on all six faces

**`2.4A-6` FenceGateBlock (SOUTH rows -12, -7):** place actual gate items facing different player headings into LIME lanes; open/close from both sides, check FACING and OPEN; check IN_WALL with adjacent cobblestone wall, after adding/removing a wall, and with redstone (OPEN/POWERED). For cherry, walls flank both reference and natural lanes. Test vanilla materials plus a truly loaded version-compatible inheriting mod gate and, separately, an independently authored gate. Mod source inheritance alone != mod gameplay PASS.

**`2.4A-7` AbstractFurnaceBlock (SOUTH rows -2, +3):** place furnace, smoker and blast furnace from all four actor headings; compare FACING and identity, interact with GUI/inventory, process appropriate fuel/recipes and observe LIT transitions and particle/sound orientation. Preinstalled LIT references are static state demonstrations, **not proof** that a recipe ran. Repeat in an ordinary no-Planet world. Independent mod machines/overrides are separate, not assumed fixed.

**`2.4A-8` RotatedPillar/Chain/Infested (SOUTH rows +8, +13):** place actual X/Y/Z axis blocks. For X click the supplied support at station-local EAST+2; for Z click station-local SOUTH+1 support; for Y click the LIME floor. Confirm AXIS and Chain WATERLOGGED after actual water placement; verify infested deepslate and alternate materials. A mod log inheriting RotatedPillarBlock needs a real loaded compatible version for gameplay evidence; mod worldgen remains independent.

**`2.4A-9` Crafter/Jigsaw:** place each case on +Y and the five rotated interiors and compare the state domain with CYAN. Verify actual Crafter opening/inventory and block identity; do **not** count physical item ejection direction as covered by placement—it belongs to Phase 7A. Jigsaw structure attachment / worldgen belongs to Phase 9 and is not proven by this lab. A true loaded mod inheritor or independent override needs separate testing. Recheck vanilla no-Planet placement.

**Across all five stages:** also check the 12 physical cube edges, eight corners, and ordinary non-Planet Overworld. The lab is on face interiors, so edges/corners must be tested separately. Confirm client startup/Mixin application and dedicated-server use separately from JUnit. Generic missing side-quads on cube seams are tracked in Phase 3B, not resolved by this lab.

## Automatic migration and protection

**V8 marker = PURPUR_BLOCK**, located at local (0, floor, 0). All known predecessor markers auto-migrate: V7 CRYING_OBSIDIAN; V6 OBSIDIAN; V5 AMETHYST; V4 NETHERITE; V3 EMERALD; V2 DIAMOND; V1 GOLD. An already-current V8 pad is untouched on login. **A previously marker-owned V7/V6 pad is cleared inside the reserved volume and rebuilt as V8** (all original test scenarios return at the same coordinates, but placed player test items inside the pad are lost). An entirely empty reserved volume can be built. Any *unmarked* occupied volume is preserved, unless the player explicitly requests `rebuild <face>`.

Migration clears **only** the marker-owned existing lab inside EAST -20..+20, SOUTH -16..+16, radial floor `FLOOR_RADIUS` through `FLOOR_RADIUS+6`. The old V1 secondary wing (EAST 21..40, SOUTH -12..12) is cleared **only** if its historical LAPIS marker exists. Structures built inside a previously marker-owned dev pad may be replaced; outside that pad stay unchanged. Keep important builds elsewhere.

CI PASS means compile/JUnit/ASM, not applied client Mixins or real gameplay. Prior relevant CI: gate V3 [#38478594846](https://github.com/Abbygree11/Planetary/actions/runs/38478594846), furnace V4 [#38622053271](https://github.com/Abbygree11/Planetary/actions/runs/38622053271), axis V5 [#38643250260](https://github.com/Abbygree11/Planetary/actions/runs/38643250260). V6 lab [CI #38644260176](https://github.com/Abbygree11/Planetary/actions/runs/38644260176) SUCCESS and selected FrontAndTop owner code [CI #38789046409](https://github.com/Abbygree11/Planetary/actions/runs/38789046409) SUCCESS. V7 source/test [CI #38790258826](https://github.com/Abbygree11/Planetary/actions/runs/38790258826) SUCCESS; V8 owner/support/fixture corrected source [CI #38971934972](https://github.com/Abbygree11/Planetary/actions/runs/38971934972) **SUCCESS** on [7b0ad84](https://github.com/Abbygree11/Planetary/commit/7b0ad84cff7ec2646cd04b56d8fbf63ced842f61) (prior CI #38971308740 compiled but one 45° rounding JUnit failed). Actual 2.4A-10/gameplay and loaded-mod acceptance PENDING.
