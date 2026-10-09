# Phase 2 — real NeoForge 21.1.215 registered orientation census findings

**Status: First actual NeoForge registry scan VERIFIED in GitHub Actions,
but source-owner SEMANTIC coverage NOT complete.**
This is not a claim that 1060 blocks work with local gravity.

## Evidence

Reproducible Java 21 / ModDevGradle / NeoForge 21.1.215:
- workflow `Planetary Java 21 tests`, run **37987135612**,
  GitHub branch 2.0, code revision
  `da2d01a543f5634399412338e9126c293fefb9c0`;
- `compileJava`: success;
- `compileTestJava`: success;
- `test` (JUnit): success;
- exact registry report output: success;
- uploaded `phase2-neo1211-registry-census` artifact,
  GitHub artifact ID `11644026627`.

The first CI attempt compiled and passed JUnit but failed
to locate the report because a test-worker relative path
did not correspond to the build directory. The test now
receives an **absolute** report directory via Gradle;
the second run verified that the generated files actually
exist and are uploaded.

## Registry totals, not source-file estimates

| Quantity from bootstrapped registries | Count |
|---|---:|
| `BuiltInRegistries.BLOCK` IDs | **1060** |
| Distinct registered concrete block Java classes | **241** |
| Block IDs flagged by INITIAL orientation property-name rules | **527** |
| All registered BlockState property instances by block ID | **1712** |
| `BuiltInRegistries.ITEM` IDs | **1333** |
| Distinct registered concrete Item Java classes | **87** |
| Item IDs that are `BlockItem` subclasses | **925** |
| Other Item IDs | **408** |

The previous 293-block-file and 121-item-file *source*
censuses were first-pass discovery inventories. They are
not registry counts. Property instances are counted once
per Block ID/property, not one per concrete class or all
combinations of legal state values.

## Actual class families that magnify errors

| Concrete registered class | IDs (observed) | Risk/gate |
|---|---:|---|
| `SlabBlock` | 56 | local HALF/TYPE, stacking, physical replacement |
| `StairBlock` | 52 | FACING/HALF/corner neighbor-derived SHAPE |
| `RotatedPillarBlock` | 52 | AXIS clicked face and inherited state |
| `WallBlock` | 25 | four-side LOW/TALL and vertical post graph |
| `ShulkerBoxBlock` | 17 | full 6D FACING, animated BE render |
| `CandleBlock` | 17 | actual support plus stack/replacement |
| `CandleCakeBlock` | 17 | support plus cake/item change |
| `DoorBlock` | 16 | LOWER/UPPER pair, hinge and interaction |
| `BedBlock` | 16 | HEAD/FOOT pair and sleep interaction |
| `TrapDoorBlock` | 16 | HALF and wall click, POWERED/OPEN |
| `FenceGateBlock` | 11 | IN_WALL and neighbor power |
| `StandingSignBlock` | 11 | 16-segment player yaw and BE |
| `CeilingHangingSignBlock` | 11 | attached/rotation and dual support |
| `WallHangingSignBlock` | 11 | wall supports and item variants |

A single tested example from a class is useful for the
SHARED algorithm, but subclass overrides, different items
and real attached support remain separate coverage gates.

## Actual orientation property domains

The census includes legal values and type, not just property
field name:

- `facing` on **247** IDs has four horizontal values;
  **34** IDs allow six physical cardinal directions; **1**
  ID has five values (hopper excludes UP).
- `axis` on 55 IDs has x/y/z; one more has only x/z.
- `rotation` has 16 discrete values on **45** IDs
  (standing signs, banners, skull/other).
- `orientation` uses **12 legal FrontAndTop values**
  on **two** IDs: `crafter` and `jigsaw`.
- `half` on 76 IDs means TOP/BOTTOM; another **30**
  have LOWER/UPPER.
- `shape` on **56** stairs has 5 options,
  and on **4** rail shapes has 6 or 10 options.
- `type` on 60 slabs is BOTTOM/TOP/DOUBLE,
  on 2 chests is LEFT/RIGHT/SINGLE; but on two other
  IDs it is NORMAL/STICKY and is **not automatically
  an orientation property**.
- four cardinal `north/east/south/west` flags
  span variants: Bool, LOW/TALL wall, redstone sides.
- `face` on 15 IDs includes CEILING/FLOOR/WALL.
- `attachment` on a bell has CEILING/DOUBLE_WALL/FLOOR/
  SINGLE_WALL and was omitted by the original name list.
- `attached` on 13 hanging sign IDs, `in_wall` on
  11 fence gate IDs and `bottom` on scaffolding were
  **not marked by the initial 527-ID candidate flag**.
  This triggered a correction in the next diagnostic revision.

## Critical counterexample to property-only coverage

The real registry confirms spatially directional mechanics
with **ZERO named orientation property**:
`sea_pickle` (SeaPickleBlock overrides support),
`scaffolding` (BOTTOM/DISTANCE graph; first-name
heuristic omitted BOTTOM), `sugar_cane`, `cactus`,
`cave_vines` and `kelp`.

Therefore "all BlockStates with FACING/AXIS work" or even
"all 527 initially flagged blocks work" is NOT a phase
completion proof. Class owner methods, Item use paths,
random/scheduled tick and alternate placement must also
be classified. Further, a 6D FACING property may have
a distinct algorithm owner for every subclass.

## Specialized item creation counts

The actual 1333 item IDs include:
- 799 plain `BlockItem` IDs;
- 27 `DoubleHighBlockItem` IDs;
- 19 `StandingAndWallBlockItem` IDs;
- 11 `HangingSignItem` IDs;
- 16 `BedItem`, 16 `BannerItem`, 14
  `ItemNameBlockItem` IDs;
- 80 `SpawnEggItem` IDs that create entities, not
  placed BlockStates.

All 925 `BlockItem` IDs require a dispatch-owner
and late DataComponents.BLOCK_STATE review;
the other 408 Item IDs cannot be blindly ignored:
some place entities or interact via spatial useOn.

## Classification and release policy

The first TSV was intentionally labeled
`REVIEW_PENDING` for orientation-bearing IDs and
`NON_PROPERTY_PATH_REVIEW_PENDING` for others.
No block or Item was marked compatible solely because
the census/JUnit test passed.

The next diagnostic revision `aa395729` adds full
runtime `class_hierarchy` and **effective** first-owner
selection for each overloaded method (rather than just
alphabetically sorted all ancestor declarations), plus
the missed attachment property flags. Its CI result
must be checked separately; counts may change.

Stage 3 must map every effective owner, inherited override,
alternative item path and non-property graph to P01–P40
or explicit future-phase/nonapplicable disposition,
then enforce a fail-on-unclassified CI gate.
Stage 4 then implements COMPLETE mechanism families and
runs cross-face natural BlockItem/useOn integration tests,
not hundreds of manually constructed blocks.

## Documents and regeneration

- Master research:
  `docs/research/ORIENTATION_MECHANISMS_ATLAS_1_21_1.md`.
- Comparative source wave A/B/C:
  `docs/research/PHASE2_SOURCE_OWNER_AUDIT_WAVE_*.md`.
- Preliminary existing-adapter crosswalk:
  `docs/research/PHASE2_PLANETARY_COVERAGE_SEED_1_21_1.tsv`.
- Executable census:
  `src/test/java/dev/planetary/world/Phase2RegistryOrientationCensusTest.java`.
- Generated TSV artifact at
  https://github.com/Abbygree11/Planetary/actions/runs/37987135612
  (download `phase2-neo1211-registry-census`).
  Re-run CI when classification code changes.
