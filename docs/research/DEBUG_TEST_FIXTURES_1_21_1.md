# Planetary debug test fixtures — reproducible in-world acceptance

Target: Minecraft 1.21.1 / NeoForge 21.1.215, branch 2.0.
Status: Phase-2/3 defect inventory confirmed from user report;
fixture implementation committed; GitHub CI green, client/runtime acceptance pending.

## Why this is a Phase-0/1 infrastructure prerequisite

2026-10-09 user manual Phase-4 acceptance reported:
- PASS: base particle gravity; destruction/collision; torches/redstone;
  cherry-leaf particles *when leaves manually placed*; seam/load.
- Mixed/blocked: End Rod and Ender Chest placed/rotated wrongly on
  rotated/negative-Y faces; cake/candle/spore blossom not placeable;
  enchanting-table model broken on rotated faces although particle
  behavior appears okay; flint/steel cannot ignite there; water-drip
  motion correct but initial origin wrong.
- There are screenshots of ender chest (wrong rendered/stacked
  appearance), sideways End Rod, enchantment table book/render
  abnormality, plus dirt blocks arranged near the side/bottom with
  other test objects in the background. Screens alone do not prove
  the complete root cause; use actual vanilla ownership research.

These are NOT evidence that Phase 0–3 as whole are done. Phase 4
cannot be closed until its tests are unblocked, and blocked cases
must be tracked in Phase 2/3/9/5 families rather than particle hacks.

## Development acceptance policy

- A runtime/visual symptom is not equivalent to a working implementation:
  separate (A) natural player interaction placement, (B) valid
  canonical BlockState and support/survival, (C) block entity renderer,
  (D) particle behavior.
- Debug fixtures MUST NOT masquerade as natural player placement.
  A prebuilt reference block proves only render/emitter ability;
  adjacent empty placement lane tests vanilla BlockItem/useOn path.
- Make fixtures repeatable and low-friction, including on all six
  faces. Do not require the user to gather materials or physically
  build elaborate structures between patches.
- Keep debug setup in `dev.planetary.debug`, not Mixins. Use real
  physical world BlockPos and canonical-local BlockStates, transport
  via `PlanetGravityFrame`. Do not rewrite BlockPos behavior.
- Place on remote face interiors of dedicated `PlanetChunkGenerator`
  world, radius 48, core (0,128,0), away from edge/corner. Only use
  actual worldgen generator gate, never modify ordinary Overworld.
- Prefer explicit Brigadier `/planetary test build` and
  `/planetary test tp <face>` commands. Fixture builder should be
  safe to rerun, with a reserved and bounded region, not a global
  terrain clear. For a genuinely enter-and-check flow, allow one-time
  automatic setup only in a deliberately identified DEV context,
  never silently overwrite users' builds in existing save files.
- Test fixtures should include passive emitters and a distinct
  adjacent **empty** natural-placement station for each problematic
  block. Do not brute-force impossible survival states and then claim
  placement fixed.
- No global high-frequency tick event: each scene is a one-off
  server construction or registered worldgen feature/preset.
- Consider 1.21.1 server chunk availability, range, worldgen
  height, local UP and directional block model rotation. No
  packet-heavy rebuild each login.

## Initial Phase-4 fixture suite (six faces)

Include:
- 1x4 candles and candle cake, with local support, lit + extinguish;
- End Rod along canonical local UP, Ender Chest with canonical local
  FACING, enchanting table (BlockEntityRenderer/book);
- brewing stand, campfires regular/soul, lit furnace/blast furnace/
  smoker, Respawn Anchor, portal particles;
- cherry leaves PERSISTENT and valid supporting logs;
- hanging spore blossom with support in physical local UP, occupied
  and free air candidates; falling dripstone/drip sources;
- partial bookshelf ring with both emitter and numeric power,
  while leaving slots for natural user placement;
- a physical ignition station for flint and steel; do not use a
  pre-ignited portal as evidence that ignition works.

On each face, provide a clearly placed arrival/waypoint and concise
station layout legend. Must avoid custom block/item registries to
retain vanilla baselines. Turn off startup spam of per-block probes
if it obscures the test; do not disable diagnostic assertions.

## CI acceptance

- GitHub Actions Java21 Gradle compileJava, compileTestJava and
  JUnit test MUST pass before a user is asked for a new client run.
- Add pure tests for fixture block positions and face orientation,
  nonintersecting stations, boundary/height safety, and idempotency
  contract when practical.
- Runtime smoke: fixture generation must not crash client, spawn
  hazards, damage the save outside bounded reserved area, or run
  repeatedly on every login. Existing user worlds should never
  be rebuilt without explicit command.
- Full gameplay acceptance is separate and uses
  `docs/acceptance/PHASE_4_PARTICLES.md`.

## Backlog ownership extracted from manual report

| Defect | Owner | State |
|---|---|---|
| End Rod rotates/places wrong | Phase 2 placement FACING + Phase 3 static model | OPEN: family call path audit |
| Ender Chest wrong appearance/orientation | Phase 2 horizontal FACING + Phase 3 BE renderer | OPEN |
| Candles, cake-candles cannot place | Phase 2 local support/BlockItem/useOn | OPEN |
| Spore blossom cannot hang on other faces | Phase 2 BushBlock ceiling support | OPEN |
| Enchanting table book/model wrong | Phase 3 block-entity renderer/book animation | OPEN |
| Saplings fail on side/bottom | Phase 2E growth/survival and Phase 10 generation | OPEN |
| Flint and steel cannot ignite on side/bottom | Phase 2 interaction/ignition and Phase 9 portal-geometry | OPEN |
| Water drip originates wrong, falls local | Phase 4 emitter + Phase 5 fluid/drip source integration | OPEN |
| Other custom particle families too difficult to reproduce manually | Debug fixture prerequisite + later Phase 4 acceptance | BLOCKED BY TEST SETUP |

Build automated reproducible fixtures FIRST, then fix ownership
families coherently in 2/3/9/4/5 and resume Phase 4 acceptance.

## 2026-10-09 first debug-lab implementation

Created:
- `src/main/java/dev/planetary/debug/PlanetTestFixtures.java`,
  bounded six-face layout builder with non-destructive auto-setup,
  20 paired stations per face, canonical-local reference BlockStates
  and adjacent vacant natural-placement cells.
- `src/main/java/dev/planetary/debug/PlanetTestCommands.java`,
  `/planetary test build`, `go <face>`, `legend`,
  `rebuild <face>` (the latter is DESTRUCTIVE and opt-in).
- `PlanetDebugServerEvents.onPlayerLogin`: in the dedicated
  PlanetChunkGenerator ONLY, when JVM property
  `planetary.debug.fixtures.auto` is true, attempt first-time
  no-overwrite six-face install. Only players near the original
  spawn may be relocated to new +Y lab pad. Other login positions
  are not changed.
- `build.gradle` local `runClient` profile supplies that
  JVM property. Distributed jars don't automatically set it.
- `PlanetTestFixturesTest`: checks all six frames, distinct pad
  footprints, 20 named stations and adjacent natural-placement
  locations, and arrival build heights.
- `docs/acceptance/PLANET_TEST_LAB.md`: exact 5x4 station map,
  case list, navigational commands, safety and acceptance contract.

Original per-block bugs are UNCHANGED. In particular a preset
spore blossom can exist where natural BlockItem placement fails;
that outcome confirms a Phase-2 gap rather than an emitter PASS.
An unlit portal frame is a deliberate ignition test and
does not assert portal geometry is fixed.
No global Mixin, gravity/particle runtime or world-generation
codec/biome logic was modified by this infrastructure batch.

The first CI green gate is required for this new code. The first
user runtime check after green CI should confirm fixture creation
does not stall/crash, and that the CYAN/LIME pairing really
separates reference model/emission from natural placement.
