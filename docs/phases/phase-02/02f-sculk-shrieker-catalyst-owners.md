# Phase 2 Stage 3A-8 — sculk shrieker and catalyst block/BE owners

**Status: NEXT — 0/4 independently bounded research tasks completed.**
Branch `2.0`; Minecraft **1.21.1** /
NeoForge **21.1.215**, Java 21.

[Phase 2 roadmap](../phase-02.md) ·
[241-class exact BLOCK registry disposition](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[Stage 3A-7.4 original 71-class/192-ID reconciliation](../../research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md)

## Exactly TWO original registered concrete classes / TWO exact BLOCK IDs — both source REVIEW_PENDING

| Exact Java class | Exact original registered BLOCK ID | Original state property names | Original compiled class hierarchy |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkShriekerBlock` | `minecraft:sculk_shrieker` | `can_summon,shrieking,waterlogged` | `SculkShriekerBlock > BaseEntityBlock > Block > BlockBehaviour` |
| `net.minecraft.world.level.block.SculkCatalystBlock` | `minecraft:sculk_catalyst` | `bloom` | `SculkCatalystBlock > BaseEntityBlock > Block > BlockBehaviour` |

Source **original immutable** NeoForge 21.1.215
runtime registry ZIP GitHub Actions artifact
**11643813158**, run **37988064055**,
SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Both source-review statuses still
`REVIEW_PENDING`; no original five
compiled declaring owners have been promoted
in this card. All patch-ASM, Planet adapter,
gameplay acceptance fields are pending.

**Separate algorithms sharing one sculk
world-event context, not one generic
SculkBlock patch:**

- The
  [`SculkShriekerBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkShriekerBlock.java)
  + [`SculkShriekerBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkShriekerBlockEntity.java)
  family owns `SHRIEKING`/`CAN_SUMMON`/
  `WATERLOGGED`, entity-step triggered shrieks,
  VibrationSystem listener, player/warden
  warning/summon logic, gamerules, 90-tick
  shutoff, world-physical Warden spawning
  and server event effects. The BE and
  scheduled block tick are distinct owners.
- The
  [`SculkCatalystBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkCatalystBlock.java)
  + [`SculkCatalystBlockEntity`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/entity/SculkCatalystBlockEntity.java)
  + [`SculkSpreader`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SculkSpreader.java)
  family owns the `GameEvent.ENTITY_DIE`
  listener and death XP/charge routing,
  `BLOOM` 8-tick block state reset,
  `SculkSpreader.ChargeCursor` movement,
  replacement/growth of other concrete
  blocks, separate worldgen spread mode,
  and physical BlockPos vs target
  canonical state semantics.
  Its direct `CatalystListener` is **not**
  the shrieker's `VibrationSystem.Listener`.
  Sculk spread can place or mutate **other**
  concrete BLOCK classes; ownership must
  be audited at writer boundaries.

**Excluded:** SculkSensorBlock and
CalibratedSculkSensorBlock already
source+compiled reviewed in preceding
3A-7 card (runtime still pending);
`LightningRodBlock` remains source
pending for later lightning/weather family;
other world generation, warden spawn,
respiration and entity physics belong to
their respective phases.

Phase 2 controls BlockState placement,
local source frame, support, neighbor,
fluid lifecycle, ITEM creators and
direct state authors. Phase 3 governs
physical/visual shapes and particles.
Phase 5 governs local waterlogging and
fluid behavior. Phase 7A governs
GameEvents, VibrationSystem, tick and
redstone/event semantics. Phase 8/9
governs structure and worldgen spread/
replacement; entity/warden spawning
requires world-physical query and
entity behavior audit.
No global Direction rewrites. No
source-only claims of gameplay PASS.

## Four independent source research substeps — FIRST unchecked only per `кк`

- [ ] **1. Stage 3A-8.1.** Read exact original
  NeoForge 21.1.215 compiled identities/2
  registered BLOCK IDs and **five exact
  full-signature method declaration owners**
  for each; pinned comparative Minecraft
  1.21.1 `SculkShriekerBlock`,
  `SculkCatalystBlock`,
  both BlockEntity subclasses,
  `VibrationSystem`, `SculkSpreader`,
  and `SculkBehaviour` full
  source writer graph.
  Split shrieker warnings/can-summon
  and catalyst death-charge spread
  branches; check waterlogged and
  `BLOOM/SHRIEKING` schedule, physical
  query directions, alternate writes.
  Promote ONLY genuinely audited two
  rows. Evidence + ledger + checkpoint,
  ONE commit.
- [ ] **2. Stage 3A-8.2.** Independently
  join original ITEM `placed_block`
  records for exact two BLOCK IDs from
  1333-ITEM census, distinguish item
  creator aliases/optional
  `DataComponents.BLOCK_STATE` and
  BE data/component updates;
  trace game-event, death, `SculkSpreader`,
  feature/worldgen/structure state authors
  and read-only callers without claiming
  unproven vanilla structures.
  ONE research evidence commit.
- [ ] **3. Stage 3A-8.3.** Write source
  canonical vs physical world position
  contract for shriek and warden spawn
  bounds, block and BE ticks, catalyst
  bloom, charge cursor neighbor/replace
  paths, fluid/geometry, no duplicate
  corner cell, vanilla vs six-face
  edges and corners and loaded chunks.
  Specify concrete *future* tests with
  independent branch/phase gates:
  no gameplay PASS without actual run.
  ONE research commit.
- [ ] **4. Stage 3A-8.4.** Independently
  reparse original NeoForge 21.1.215
  ZIP, verify entire roster, all
  current source reviewed classes/
  exact registered BLOCK IDs/five
  original method owner declarations,
  241 ASM/runtime/gameplay gates and
  all pending rows. Prepare next
  owner family card; ONE GitHub commit,
  Stage 3A/Phase 2 remains open.

## Durable restart checkpoint

Last completed:
[Stage 3A-7.4 source registry
reconciliation](../../research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md):
ZIP sha256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`,
original vs GitHub:
roster `0xf188a064`,
71 class/192 ID digest `0x8a6840ba`,
five declaring owners `0xa81e5835`,
combined `0x59d9c0aa`.
All **71/241** source+compiled reviewed
(**192/1060 BLOCK IDs**), **170/241**
source pending (**868/1060 BLOCK IDs**).
All patched ASM/Planet/gameplay fields
REVIEW_PENDING.

**NEXT FIRST microtask: Stage 3A-8.1**,
checkbox 1, exact two still-unreviewed
classes and their two original registered
BLOCK IDs, full source/callsite/compiled
declaration research. One committed
research package and stop.
