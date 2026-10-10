# Phase 2 Stage 3A-8 — sculk shrieker and catalyst block/BE owners

**Status: COMPLETE — 4/4 independently bounded source research tasks completed; ASM/Planet/gameplay STILL pending.**
Branch `2.0`; Minecraft **1.21.1** /
NeoForge **21.1.215**, Java 21.

[Phase 2 roadmap](../phase-02.md) ·
[241-class exact BLOCK registry disposition](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) ·
[Stage 3A-7.4 original 71-class/192-ID reconciliation](../../research/PHASE2_STAGE3A_SCULK_COHORT_RECONCILIATION_1_21_1.md)

## Exactly TWO original registered concrete classes / TWO exact BLOCK IDs — source+compiled declarations reviewed, runtime pending

| Exact Java class | Exact original registered BLOCK ID | Original state property names | Original compiled class hierarchy |
|---|---|---|---|
| `net.minecraft.world.level.block.SculkShriekerBlock` | `minecraft:sculk_shrieker` | `can_summon,shrieking,waterlogged` | `SculkShriekerBlock > BaseEntityBlock > Block > BlockBehaviour` |
| `net.minecraft.world.level.block.SculkCatalystBlock` | `minecraft:sculk_catalyst` | `bloom` | `SculkCatalystBlock > BaseEntityBlock > Block > BlockBehaviour` |

Source **original immutable** NeoForge 21.1.215
runtime registry ZIP GitHub Actions artifact
**11643813158**, run **37988064055**,
SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Both class+original five-signature effective
method declaring owners have now been reviewed
in **Stage 3A-8.1** and promoted to
`SOURCE_REVIEWED_INTEGRATION_PENDING` with
`REFLECTION_OWNER_VERIFIED` only.
All patched ASM, Planet adapter and gameplay
acceptance fields remain `REVIEW_PENDING`.

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

- [x] **1. Stage 3A-8.1.** Read exact original
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
  **DONE 2026-10-10 / 3A-8.1:** [original 2 registered BLOCK IDs + 5 exact compiled declaration owners each; comparative 1.21.1 BE VibrationSystem/90-tick shriek/WardenSpawnTracker and independent GameEvent.ENTITY_DIE/8-tick BLOOM/SculkSpreader cursor+SculkBehaviour/Vein/Block writer graph](../../research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_SOURCE_OWNER_AUDIT_1_21_1.md). Two source-only rows promoted; **73/241 classes (194/1060 IDs)** reviewed, 168/241 pending; all 241 ASM/Planet/gameplay fields still pending.
- [x] **2. Stage 3A-8.2.** Independently
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
  **DONE 2026-10-10 / 3A-8.2:** [exact original 1333-ITEM placed_block/BlockItem owners and explicit SculkPatchFeature sculk worldgen writers](../../research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_ITEM_WORLDGEN_ALTERNATE_AUTHORS_1_21_1.md). Two item matches, 7/7 placing declarations per item; optional BLOCK_STATE/BE data, sculk growth and direct catalyst/shrieker features verified. **No new BLOCK class promoted:** 73/241 reviewed (194/1060 IDs); all 241 ASM/Planet/gameplay gates pending, no code or game test.
- [x] **3. Stage 3A-8.3.** Write source
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
  **DONE 2026-10-10 / 3A-8.3:** [source/physical/local gravity contract, six-face basis, exact SpawnUtil world-Y support, 18-neighbor cursor movement, item/BE/worldgen alternate writers, and 26 proposed unrun acceptance fixtures](../../research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_SIX_FACE_SEAM_ACCEPTANCE_CONTRACT_1_21_1.md). **No class promoted** (73/241, 194/1060 IDs); 241 ASM/Planet/gameplay gates remain REVIEW_PENDING. No Java or gameplay change.
- [x] **4. Stage 3A-8.4.** Independently
  reparse original NeoForge 21.1.215
  ZIP, verify entire roster, all
  current source reviewed classes/
  exact registered BLOCK IDs/five
  original method owner declarations,
  241 ASM/runtime/gameplay gates and
  all pending rows. Prepare next
  owner family card; ONE GitHub commit,
  Stage 3A/Phase 2 remains open.

  **DONE 2026-10-10 / 3A-8.4:** [original NeoForge ZIP reparse and entire 241/73-class source/compiled declaring-owner reconciliation](../../research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_ORIGINAL_73_CLASS_RECONCILIATION_1_21_1.md). Four independent FNV32 + FNV64 fingerprints MATCH for complete class roster, exact 73-reviewed/194 BLOCK IDs, all five owners per reviewed class and combined data. **Important correction:** `SculkVeinBlock` already belonged to 73 reviewed classes, while `SculkBlock` remains source pending. New [3A-9 growth-owner card](02g-sculk-spread-growth-owners.md) created; 73/241 reviewed, 168 pending; all 241 ASM/Planet/gameplay pending. Docs only; no Java/build/game tests.

## Durable restart checkpoint

**LAST COMPLETE research microtask: Stage 3A-8.4 (2026-10-10)**. [Full source/compiled 241-class reconciliation](../../research/PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_ORIGINAL_73_CLASS_RECONCILIATION_1_21_1.md). Original exact NeoForge 21.1.215 ZIP artifact 11643813158 SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Re-read source 1060 BLOCK IDs, 241 class names, 1333 ITEM IDs, 1712 properties; all reviewed original ID lists and five fully qualified dispatch declaration owners checked and matched current live 16-column ledger with both FNV32 and FNV64 diagnostic fingerprints. 73/241 classes, 194/1060 IDs source+original owner reviewed; 168/241, 866/1060 pending; 241 NeoForge patched ASM/Planet adapter/gameplay rows still REVIEW_PENDING.

**Erratum:** `SculkVeinBlock` (`minecraft:sculk_vein`) was already SOURCE_REVIEWED_INTEGRATION_PENDING in the earlier graph cohort and is INCLUDED in the existing 73 classes. The related `SculkBlock` (`minecraft:sculk`) alone remains REVIEW_PENDING. Neither was promoted by Stage 3A-8. Its next full source-owner research is a separate card.

The next ACTIVE bounded packet is [Stage 3A-9 SculkBlock charge-growth](02g-sculk-spread-growth-owners.md), **0/4 research tasks completed**. **NEXT “кк” -> 3A-9.1 checkbox 1**, not another 3A-8 task. Stage 3A and all of Phase 2 still OPEN; no Java changes, CI, patched NeoForge bytecode tests or in-game PASS.
