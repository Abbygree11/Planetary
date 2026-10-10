# Stage 3A-12.4 — final independent original NeoForge 241-class / 82 reviewed cold-surface reconciliation and 7-class coral handoff

**2026-10-10 · Planetary branch `2.0` · Minecraft 1.21.1, NeoForge 21.1.215 / Java 21.** Full cold-surface Stage 3A-12 **4/4 research** completed in one user “кк”, with prior durable two-step source audit checkpoint. No Java/Mixin changes or game tests. All 32 `CS12-01..CS12-32` acceptance cases are future unrun fixtures; nothing in this document proves ASM patched method bodies were executed in-game.

## 1. Independent immutable original 21.1.215 artifact reparse

Original unmodified ZIP `/mnt/data/phase2-neo1211-registry-census.zip`, GitHub [Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158** (source `aa39572950a15403ea0a9003eefccf3bf6675ff7`), independently opened and cryptographic SHA-256 recomputed:
**`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`**, **MATCH**. All member records parsed as TSV via Python `zipfile.ZipFile` / `csv.DictReader`, not extracted or guessed from block names:
- **1060** original registered BLOCK ID rows, grouped by **241** distinct fully-qualified concrete Java block class names.
- **1333** original ITEM rows, independent of BLOCK count.
- **1712** original state-property rows.
- Original historical `audit_disposition` fields were never changed.

Python original archive audit `/mnt/data/_audit_phase2_12_4.py` rebuilt the source-reviewed set **from the previous 78 exact audited classes + four new independently source-reviewed classes**, 82 distinct concrete classes. It grouped original registry IDs by exact `java_class` and checked ALL five full argument-signature nearest declaring owner slots for each exact ID:
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`;
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`;
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`;
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`;
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.
Multiple original compiled method overloads were disambiguated using complete argument types, not only identical method names. Every exact original ID in each reviewed class had consistent effective owner tuple. Raw original `all_declaring_owners` was not confused with nearest original effective declaring owner.

An independent **JavaScript parser of the actually current 241-row/16-column GitHub TSV** [`PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) blob `eea8a629770bfb917a819a678eb75c9ce7f7ad13` selects **82** classes by actual `SOURCE_REVIEWED_INTEGRATION_PENDING` row status, constructs exact comma-joined ID lists and all five actual declaring owners and sorts classes by raw lexicographic code-point order (DO NOT use locale-dependent alphabetical order). Exact canonical strings are UTF-8, one final `\n` per record. JS independently computed FNV-1a 32-bit and FNV-1a 64-bit, compared with values recomputed from original Python ZIP records; all **four independent pairs MATCH**:

| Canonical string data | ORIGINAL ZIP FNV-1a32 = live GitHub FNV32 | ORIGINAL ZIP FNV-1a64 = live GitHub FNV64 | Python **original-side-only** SHA-256 | Outcome |
|---|---|---|---|---|
| Full original 241 `class|registeredCount\n` rows | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | MATCH |
| Source-reviewed 82 `class|id1,id2...\n` rows (203 exact IDs) | `0xc0a7f4ff` | `0xccc83d5c61cdc1bf` | `d9dc8d41e6ebebf389240acd5325a902570d04fce88bf66a7a2850bafde71a12` | MATCH |
| Source-reviewed 82 `class|owner1|...|owner5\n` rows | `0xdb662abf` | `0xcec5f24fb2f788ff` | `fb73fa7d97baa43340a0918a588c17fe0f37fd160f60c4ffaaae1d222b7d08a0` | MATCH |
| Reviewed 82 combined `class|ids|owner1|...|owner5\n` | `0x292d9b27` | `0x61dda5d819459487` | `9d6d046cc9c5f602468beec49fcffd171ccbf1c328f3108b82fe07919a7b1d83` | MATCH |

**Limitation:** SHA256 of canonical strings was computed original-Python-side only; do not pretend a matching independent JavaScript SHA256 was calculated. Independent JS FNV matches are a mismatch diagnostic, not cryptographic proof alone. Original ZIP full cryptographic hash and two independent parsers jointly support this *internal research consistency*, NOT a runtime Mixin/pass result.

## 2. Exactly four new class statuses, no expanded frozen-worldgen ownership claims

| New originally source-pending exact BLOCK class, 1 registered ID each | Original five full-signature declaring owners |
|---|---|
| `net.minecraft.world.level.block.SnowLayerBlock` / `minecraft:snow` | `SnowLayerBlock / SnowLayerBlock / SnowLayerBlock / SnowLayerBlock / Block` |
| `net.minecraft.world.level.block.PowderSnowBlock` / `minecraft:powder_snow` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` |
| `net.minecraft.world.level.block.FrostedIceBlock` / `minecraft:frosted_ice` | `Block / BlockBehaviour / BlockBehaviour / IceBlock / Block` |
| `net.minecraft.world.level.block.IceBlock` / `minecraft:ice` | `Block / BlockBehaviour / BlockBehaviour / IceBlock / Block` |

[Stage 12.1/12.2 source and exact registered ITEM audit](PHASE2_STAGE3A_COLD_SURFACE_ORIGINAL_BLOCK_ITEM_ALTERNATE_AUTHOR_AUDIT_1_21_1.md) documents important **creation and property distinctions**. Normal `minecraft:snow` and `minecraft:ice` ordinary BlockItem; `minecraft:powder_snow_bucket` is `SolidBucketItem` with both BlockItem `useOn` and `DispensibleContainerItem.emptyContents` actual POWDER_SNOW writer and `BucketPickup` inverse; `minecraft:frosted_ice` has **zero** original ITEM.placed_block author. A data-driven `ReplaceDisk` has a generic non-item state-writing capability but specific shipped FrostWalker enchantment config was **not verified here**. Natural [`SnowAndFreezeFeature`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/levelgen/feature/SnowAndFreezeFeature.java) directly produces ICE and SNOW in world XZ with heightmap/biome, **not** Froste­dIce or POWDER_SNOW. No invented shipped template NBT contents.

[Stage 12.3 six-face contract and 32 future tests NOT RUN](PHASE2_STAGE3A_COLD_SURFACE_SIX_FACE_THERMAL_FLUID_ENTITY_CONTRACT_1_21_1.md) covers 6 face interiors, 12 edges, 8 triple corners, random vs scheduled tick distinction, block/item writer bypasses, snow physical voxel and support, powder dynamic entity collisions and bucket source, actual water creation from IceBlock, FrostedIce AGE neighbor graph and weather/feature paths. Direct FrostedIce `randomTick` **compiled owner inherited IceBlock** does NOT prove registration enables random ticking (source registration lacks `randomTicks()`): scheduled `onPlace`+`tick` are the required main aging source. All future 32 scenarios remain UNRUN.

### Current GitHub ledger integration gates, reverified explicitly

| Actual class disposition in 241×16 TSV | Concrete classes | Original exact BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` — source+original compiled nearest signature owner only | **82** | **203** |
| `REVIEW_PENDING` — only class/count original registered | **159** | **857** |
| TOTAL | **241** | **1060** |

- All **241** Java class names unique; summed exact `registered_block_ids_count` =1060; all **82** reviewed rows have exact registered BLOCK ID list, five original owner values, `registry_dispatch_evidence=REFLECTION_OWNER_VERIFIED`; remaining pending rows keep `SEE_REGISTRY_ARTIFACT` and `REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY` (no fabricated source coverage).
- Each of ALL **241×3 = 723** independent `neoforge_patch_bytecode_review`, `planet_adapter_acceptance` and `gameplay_acceptance` fields **REVIEW_PENDING**. No Java changes, ASM bytecode transformed-body audit, applied Mixin confirmation, CI build, server/client gameplay or manual PASS; source-only research does not alter any phase implementation gate.

## 3. Next original pending family: seven concrete living/dead coral classes, 35 exact original BLOCK IDs

**New [Stage 3A-13 coral lifecycle/water/fan-placement whole-family card](../phases/phase-02/02k-coral-live-dead-water-owners.md)**. Exact original `BLOCK.java_class` groups, independently seen as source-pending in current GitHub ledger, include:

| Original fully-qualified class suffix | Number of exact registered original IDs | Mechanism distinction |
|---|---:|---|
| `CoralBlock` | **5** | Live full-size coral blocks, ticked conversion to dead coral blocks on loss of actual nearby water |
| `CoralPlantBlock` | **5** | Live planted floor coral; WATERLOGGED, survival and ticked conversion |
| `CoralFanBlock` | **5** | Live floor fans, WATERLOGGED, support, scheduled death |
| `CoralWallFanBlock` | **5** | Live wall fans, FACING/WATERLOGGED, support and orientation-preserving death |
| `BaseCoralPlantBlock` | **5** | Dead floor coral plant BlockStates with common support/waterlog lifecycle; source-pending concrete class |
| `BaseCoralFanBlock` | **5** | Dead floor fan, support and specialized item floor/wall branch |
| `BaseCoralWallFanBlock` | **5** | Dead wall fan, FACING, source-pending concrete registered class |

**Exactly 35 original ID rows across seven classes** are candidate research, NOT reviewed/promoted in this packet; all seven remain `REVIEW_PENDING` until Stage 13 source+five-owner audit. Shared `BaseCoralPlantTypeBlock` is abstract/non-registered and must be inspected as an **algorithm owner** without inventing a new 242nd registered class. Real registered `StandingAndWallBlockItem` floor→wall mapping, WATERLOGGED and actual water-neighbor scanner, world XZ/water support, death tick, local support and canonical FACING/seam semantics must all be audited. Do not presume one Item per WallFan BLOCK; join all original 1333 item records by exact `placed_block`, include alternate blocks represented by special dual-author Item classes. Use the SAME whole-family cadence: one future “кк” executes all internal 13.1–13.4 with docs checkpoint commits, not four separate requests.

**Stage 3A-12 closed 4/4 *research*; Stage 3A and Phase 2 remain OPEN. NEXT one “кк” → full Stage 3A-13 coral family.**
