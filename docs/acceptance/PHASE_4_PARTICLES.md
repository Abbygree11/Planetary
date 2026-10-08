# Phase 4 — one coherent particle subsystem acceptance

Minecraft 1.21.1 · NeoForge 21.1.215 · Planetary branch 2.0

**Status: CI AND PLANET-WORLD STARTUP CHECKPOINT PASSED / PARTICLE GAMEPLAY ACCEPTANCE PENDING.**
This is ONE acceptance run for the implemented non-blocked Phase-4
families, not a series of per-class manual retests.

## Before gameplay: hard startup gate

- CI for branch 2.0 must be green: `compileJava`,
  `compileTestJava` and JUnit `test`.
- On the user's machine: `git pull && .\run-client.ps1`.
- Start ordinary Minecraft, enter the dedicated Planet world, confirm
  that the world and compatibility diagnostics finish without a Mixin
  application error. Capture first deepest `Caused by` stacktrace if
  a crash occurs; do not run gameplay acceptance on an unstable client.
- In-game probes for block support, placement, Frame API and
  local/physical capability providers must remain green.
  `22 raw BlockPos.relative mismatches detected` is an EXPECTED
  successful diagnostic, not 22 failing tests.

CI green does **not** substitute for this one client-transformer check.

User evidence 2026-10-09:
- GitHub Actions `37846622714` succeeded for the last
  code/workflow HEAD `a9c5368e` (compile + JUnit).
- In-game Planet screenshot shows the world running with the support,
  placement, Frame API and capabilities checks passed; 22 raw relative
  differences are intentionally detected.
- Standing/shape diagnostics are not visible in this latest crop.
- No claim yet that the new particle and numerical enchantment behavior
  has passed on all six faces.

**Do not rerun Gradle or the same client-startup gate merely to
begin this matrix.** Continue in the open Planet world if available.

## Setup for the ONE gameplay acceptance matrix

- Use creative mode for source blocks and repeatable observation.
- Observe +Y first as vanilla baseline, then -Y, +X, -X, +Z and -Z,
  where possible on sufficiently distant face interiors.
- Repeat only mechanisms owning a surface transition at a cube edge;
  avoid treating generic block placement exactly on a seam as a
  particle failure when Phase-2 generic edge placement is open.
- Compare visible motion with **local UP/DOWN**, not global XYZ.
- Avoid using `/particle` as proof for block-owned sources:
  commands inject physical world coordinates/velocity and bypass
  the original block emitter semantics.
- Test over a safe empty local-floor area so particles are visible
  against background; avoid heavy simultaneous effects obscuring
  emitter direction.

## Checklist — source families and physics

1. **Baseline and shared gravity.** On +Y observe ordinary fall/rise
   of particles: vanilla behavior should be unchanged. On rotated
   faces, gravity-sensitive particles descend toward the local floor;
   rise-oriented ones move local UP. No consistent +Y sideways drift.
2. **Destroy / collision / falling blocks.** Break full blocks at
   multiple face interiors, including rotated +/-X,+/-Z and -Y.
   Destruction pieces should burst from the block and land/collide
   with the local floor (not slide along it indefinitely).
   Sand/gravel/anvil fall and their physical damage are Phase-7/2
   integration gates; here check visual debris and no sprite drift.
3. **Torches and redstone torches.** Place ordinary, soul and redstone
   variants (standing and wall where block placement permits).
   Emitter flames/smoke should start near the real tip and rise local
   UP, not around world +Y. +Y baseline unchanged.
4. **Cherry leaves and ParticleUtils.** Observe natural falling
   leaf particles under cherry leaves. Emitter belongs to the block
   and motion is local gravity. No reversed/suspended leaves.
5. **Campfire / furnaces / block-local emitters.** Observe campfire
   regular/soul smoke and sparks, lit furnace/blast furnace/smoker,
   brewing stand, End Rod, Respawn Anchor, Ender Chest. Each effect
   should appear at the same LOCAL block-relative origin as vanilla.
   Zero/isotropic velocities must not get unintended direction changes.
   Do not require every one on all six faces separately: cover each
   source at least once on a rotated interior, and representative
   shared families on all six faces.
6. **Candles and cake candles.** Observe lit one through four candles
   and candle on cake on a rotated face; flame/smoke origin must be
   attached to the candle, local UP must be respected, with no extra
   particle/sound counts. Extinguish a candle: smoke puff should
   move toward LOCAL UP. On +Y it stays vanilla.
7. **Enchanting table.** Place eligible bookshelf providers and a
   vanilla enchanting table on face interior, including rotated
   +X/-X or +/-Z. Check ENCHANT particles visually travel toward
   the table in its local frame and that ACTUAL enchanting choices
   reflect the bookshelves (not just particles). Repeat an edge-
   spanning setup if structure placement works. Modded provider
   float-power equivalence and duplicated providers at triple
   corners are distinct Phase-2 compatibility/policy gates.
8. **Spore blossom.** Place/observe blossom on a suitable local
   supporting surface. Falling spores should move toward the LOCAL
   floor, airborne spores should sample surrounding free volume
   rather than a vertical physical-Y-only column, and should not
   appear inside a collidable full block. Verify on a side face
   and at least one edge candidate volume.
9. **Custom tick and special renderer families.** Observe samples of
   drip/water drop/wake/bubbles, dragon breath, spell/ash/smoke,
   END_PORTAL/REVERSE_PORTAL, shriek/sculk visual orientation,
   trial-spawner particles, and representative generic gravity
   particles. Check for correct local rise/fall, no premature
   removal, no freezing or duplicate emission. Some fluid-coupled
   types are only provisional until Phase 5.
10. **Regression, seams, and load.** Representative emitters should
    work on +/-X,+/-Z,-Y and remain vanilla on +Y. At a navigable
    seam, particles emitted on each side use the owning source's
    frame, without jumping to world +Y or creating a second copy.
    Observe multiple simultaneously active sources for a short
    smoke test: no drastic frame spikes or console flood.

## Deferred cross-phase integration, NOT grounds to falsely fail Phase 4

- **Phase 5 fluids:** water/lava surface detection, flowing-current
  transport, bubble columns, waterlogging and fluid-particle
  collision depend on the new fluid topology. Record the fluid
  family as provisional; finish at the Phase-5 integration gate.
- **Phase 7 entity/body-authored sources:** Glow Squid ink, firework
  rocket/star bursts and body-driven splash/attack particles require
  the entity-body frame. Base Spark shared gravity is covered here.
- **Phase 7B weather:** rain/snow sources and particle weather volume
  depend on environment / local sky and precipitation logic.
- **Phase 9 portal geometry:** Nether portal plane and topology;
  particle orbit/motion is already covered in Phase 4.
- **Phase 2 triple-face layout:** bookcase physical-provider dedup,
  exact-corner generic block placement, spore blossom hanging
  support/shape changes belong to block semantics.

These are **explicit pending dependencies**, not silent acceptance.

## Evidence and closure

A single user gameplay session can report issues by checklist number
and observed face, with screenshot or log only for failures.
Do not repeat all passing checks after a targeted regression fix
unless that fix could affect those families.

Phase 4 can be marked PASS for non-blocked scope only after the
runtime startup gate and checklist have been explicitly confirmed,
with each cross-phase item marked pending in the roadmap.
CI by itself is not gameplay acceptance.
