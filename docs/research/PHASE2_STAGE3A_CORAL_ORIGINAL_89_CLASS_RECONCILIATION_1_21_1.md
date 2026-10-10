# Stage 3A-13.4 — independent original NeoForge 241/89-class coral reconciliation and next aquatic support family

**2026-10-10 · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21 · Planetary branch 2.0.** Completion of all **4/4** *SOURCE-RESEARCH* substages of living/dead coral family from **ONE user “кк”**; not acceptance of patch bytecode, applied Planetary mixins, tests or gameplay. [Original coral 7-class/35 BLOCK-ID five-declaring-owner + item/reef audit](PHASE2_STAGE3A_CORAL_BLOCK_SOURCE_ITEM_ALTERNATE_WRITERS_1_21_1.md), [32 proposed UNRUN future six-face/seam/water/death fixtures](PHASE2_STAGE3A_CORAL_SIX_FACE_WATER_SUPPORT_DEATH_ACCEPTANCE_CONTRACT_1_21_1.md).

## 1. Rehash and independently parse original Minecraft 1.21.1 / NeoForge immutable census

Physical original ZIP `/mnt/data/phase2-neo1211-registry-census.zip` (Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, originating commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`) actually reopened and sha256sum recalculated, SHA256 **MATCH**:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`.
Original archive `zipfile.ZipFile`/`csv.DictReader(delimiter="\t")` independently enumerated **1060 exact registered BLOCK ID rows / 241 fully qualified runtime concrete classes**, **1333 ITEM entries** and **1712 state-property entries**; historical original audit status retained unchanged.

Python original owner ledger computed by independently adapting pre-existing immutable-audit script `/mnt/data/_audit_phase2_12_4.py` to use **prior 82 exact source-reviewed class names + precisely seven newly audited coral classes**; no reliance on GitHub ledger strings for those class memberships. Script `/mnt/data/_audit_phase2_13_4.py`, output `/mnt/data/_audit_phase2_13_4_stdout.json`, independent canonical original fingerprints `/mnt/data/_phase2_original_fingerprints_13_4.json` are local reproducibility outputs (NOT uploaded as part of this docs commit). Original parser verifies every **one of 35** registered Coral* class entries has same 5-tuple for each original **FULL qualified signature**, joining exact unique class/IDs, not only class counts:
- `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`;
- `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`;
- `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`;
- `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`;
- `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

Separate LIVE GitHub JavaScript parser read **actual** `PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` blob `85a3973967c5dfd197645b0214bc57882ea47595` as 241 rows × 16 header-named columns; selected 89 *live status* `SOURCE_REVIEWED_INTEGRATION_PENDING` classes, 238 exact BLOCK IDs and actual five declaring-owner cells. Original Python and live JS constructed four **identical** canonical strings sorted by raw case-sensitive class name (not locale-sensitive `localeCompare`): every class/ID/value row ends exactly one newline, IDs lexicographically comma joined, owners five ordered.

### Four original ZIP vs independently parsed LIVE GitHub fingerprint matches

| Canonical line format | Python original ZIP FNV-1a32 = JS LIVE FNV32 | Python original FNV-1a64 = JS LIVE FNV64 | Python original-side SHA256 only | Result |
|---|---|---|---|---|
| `class|registeredIdCount\n` for all 241 | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | MATCH |
| `class|exactId1,exactId2,...\n` for 89 reviewed classes | `0x19ddd1cc` | `0x744d3c85441c874c` | `65bf26e2a66e4f4fb0c86fdc7ccf24064f70e2cd9a727e72d9a9f7b895585264` | MATCH |
| `class|owner1|owner2|owner3|owner4|owner5\n` for 89 reviewed | `0xcf1b95b9` | `0x0dbc7d3f2f9228f9` | `3b65ecd728cee7b2e19d1ad475a1bb291d84cac98b16c3090122f7bcbf68ea5a` | MATCH |
| `class|exactIds|owner1|...|owner5\n` combined | `0x90c7ae10` | `0x460aea51c8464c10` | `8648fa62c371885071ee7373e7b294dc174e879839e99ea44f8c970f28a75f96` | MATCH |

FNV32/FNV64 are diagnostic consistency checks, not cryptographic authenticity proofs; shown SHA256 of canonical strings was computed **only by original Python side**, not independently duplicated in JS. The ZIP SHA256 authenticates original ZIP bytes; no claim this proves same NeoForge-patched method bytecode or actual adapter execution.

## 2. Exactly seven reviewed original concrete coral classes, 35 physical exact registered BLOCK IDs

| Concrete class (each 5 BLOCK IDs) | Five nearest original compiled declaring method owners |
|---|---|
| `CoralBlock` | `CoralBlock / BlockBehaviour / CoralBlock / BlockBehaviour / Block` |
| `CoralPlantBlock` | `BaseCoralPlantTypeBlock / BaseCoralPlantTypeBlock / CoralPlantBlock / BlockBehaviour / Block` |
| `CoralFanBlock` | `BaseCoralPlantTypeBlock / BaseCoralPlantTypeBlock / CoralFanBlock / BlockBehaviour / Block` |
| `CoralWallFanBlock` | `BaseCoralWallFanBlock / BaseCoralWallFanBlock / CoralWallFanBlock / BlockBehaviour / Block` |
| `BaseCoralPlantBlock` | `BaseCoralPlantTypeBlock / BaseCoralPlantTypeBlock / BaseCoralPlantTypeBlock / BlockBehaviour / Block` |
| `BaseCoralFanBlock` | `BaseCoralPlantTypeBlock / BaseCoralPlantTypeBlock / BaseCoralPlantTypeBlock / BlockBehaviour / Block` |
| `BaseCoralWallFanBlock` | `BaseCoralWallFanBlock / BaseCoralWallFanBlock / BaseCoralWallFanBlock / BlockBehaviour / Block` |

Each class contains five original species IDs with exact `brain,bubble,fire,horn,tube` prefixed either live or dead `coral_block`/`coral`/`coral_fan`/`coral_wall_fan`; exact ID lists stored in live ledger and sourced from independent original ZIP. **Abstract `BaseCoralPlantTypeBlock` is a source algorithm owner but NOT another registered concrete class.** Prior source-reviewed 82 classes unchanged. Coral ITEM exact join **25 actual registered items**: 15 ordinary BlockItem + 10 StandingAndWallBlockItem, with **zero direct ITEM.placed_block matches for the ten wall-fan IDs**; all ten wall variants nevertheless can be authored by corresponding dual-registration specialized Items. `CoralFeature` worldgen is a separate direct wall-fan author.

## 3. Negative acceptance gates and totals

| Actual live 241-row disposition | Concrete classes | Exact registered BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **89** | **238** |
| `REVIEW_PENDING` (only original registered class and ID count) | **152** | **822** |
| **Total** | **241** | **1060** |

All reviewed rows have exact original ID strings, five original declaring owner cells, `REFLECTION_OWNER_VERIFIED`, comparative source evidence. All pending rows remain `SEE_REGISTRY_ARTIFACT` / `REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY`, no false full ID audit. For **ALL 241 classes**, each of `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, `gameplay_acceptance` remains `REVIEW_PENDING` — **723 outstanding field values**. No Java/NeoForge transformed ASM, build/CI, game client/server or acceptance happened; 32 future fixtures **NOT RUN**. Phase 2 / Stage 3A remain OPEN.

## 4. Next original source-pending aquarium water/ecology owner family: sponge absorption + sea pickles

Proposed [new Stage 3A-14 task card](../phases/phase-02/02l-aquatic-absorption-sea-pickle-owners.md) groups exactly **three** actual source-pending registered concrete original classes, one registered ID each:

| Original concrete class / exact registered BLOCK ID | Original state fields and role | Known source algorithm risk (triage only, NOT promoted) |
|---|---|---|
| `SpongeBlock` / `minecraft:sponge` | no registered state property | OnPlace/neighborChanged breadth-first 6-physical-direction water removal depth6 up to ~65 visited nodes; BucketPickup and kelp/seagrass removal, becomes WET_SPONGE if successful |
| `WetSpongeBlock` / `minecraft:wet_sponge` | no properties | UltraWarm onPlace converts back SPONGE with event/sound; `animateTick` physically world-Y-biased DRIPPING_WATER particles |
| `SeaPickleBlock` / `minecraft:sea_pickle` | `pickles,waterlogged` | Same-cell stacking 1..4, support `pos.below()` and UP face shape/sturdiness, WATERLOGGED, bonemeal propagation scanning source physical XZ+world-Y (checks `BlockTags.CORAL_BLOCKS`); separate from CoralFeature reef writers |

These **three** were confirmed original registered classes but are NOT source-reviewed/patched/accepted in 13.4; their next audit must verify exact original 5 signatures, all 1333 ITEM creator paths and water/bonemeal/direct worldgen/sponge absorption authors. Including SeaPickleBlock is motivated by the confirmed CoralFeature`placeCoralBlock` sea-pickle alternate state writer and SeaPickleBlock checks of coral block tags. Sponge and wet sponge separate Phase5 mass-water absorption and Phase3 particle/Phase7 habitat mechanisms; no blanket physical-neighbor rewrite. New Stage14 internal plan 14.1–14.4, **ONE future “кк” = whole packet** with small GitHub checkpoints and final concise result.

**Stage 3A-13 coral research DONE 4/4; next “кк” runs whole Stage 3A-14.**
