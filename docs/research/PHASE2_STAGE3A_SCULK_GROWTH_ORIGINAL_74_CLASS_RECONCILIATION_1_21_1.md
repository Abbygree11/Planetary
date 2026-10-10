# Stage 3A-9.4 — independent NeoForge 21.1.215 source/compiled declaring-owner reconciliation after SculkBlock

**2026-10-10 · Planetary branch `2.0` · Minecraft 1.21.1 / NeoForge 21.1.215 / Java 21.**
**Research-only packet closure, NOT completed Phase 2 implementation or game testing.** This task is the **fourth and final** independently committable SculkBlock research substep (3A-9.1–9.4); previously prepared 28 six-face fixtures SG9-01…SG9-28 are proposed **NOT RUN**. All original runtime/ASM/Planet gameplay acceptance gates remain pending. No Java changed.

## 1. Independently reparse original CI artifact, not handwritten ledger numbers

Actual immutable NeoForge [Actions run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), source commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`, artifact **11643813158**. Reopened local original archive `phase2-neo1211-registry-census.zip`; recomputed file `hashlib.sha256` over **actual ZIP bytes** and extracted members with Python `zipfile.ZipFile` + `csv.DictReader(delimiter='\t')`. Exact ZIP checksum **MATCH**:

`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`

| Extracted original member | Independently counted records |
|---|---:|
| `phase2-neo1211-block-registry.tsv` | **1060** exact registered BLOCK IDs |
| Distinct `BLOCK.java_class` (full qualified, concrete runtime) | **241** classes |
| `phase2-neo1211-item-registry.tsv` | **1333** exact ITEM IDs — do **not** treat as BLOCK IDs |
| `phase2-neo1211-state-properties.tsv` | **1712** original state property records |

Original ZIP bytes never modified; historical original `audit_disposition` fields remain original and are not conflated with the separately maintained GitHub source-reviewed disposition.

**Target GitHub ledger:** actual branch head before current work `145b55ba3f30e645cafb465c4fec3b21340282b4`, [`PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv`](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv), blob SHA `ca1dfdb21806fccd0dfbe86440b126ac366559d6`, **241 data rows × 16 header columns**. Actual current original-class count roster and live ledger match; unique fully qualified classes (`241/241`); all `registered_block_ids_count` add to **1060**. No invented class or ID.

## 2. Verify exact 74 source-reviewed classes, 195 original IDs, five owner declarations each

Original-side Python source independently parses `effective_method_owners` for each *full method argument signature*, including `;`-separated overload declarations. It groups registered IDs **by exact full class**, verifies all IDs in each class resolve identical nearest declaring owners, and emits canonical strings sorted lexically by full class name. Current GitHub ledger side independently parsed using JavaScript with 16 header-named columns (no reusing original Python arrays) and produced the same canonical strings. The two independent implementations calculate diagnostic **FNV-1a 32-bit and FNV-1a 64-bit** checksums.

The five signature slots, **not merely fuzzy method names**, are:
1. `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`;
2. `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`;
3. `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`;
4. `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`;
5. `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

Canonical strings (all UTF-8, exactly one `\n` per sorted full-class row):
- **241 class roster**: `fullClass|registeredIdCount\n`.
- **74 reviewed class exact IDs**: `fullClass|id1,id2,...\n`, each ID sorted.
- **74 reviewed class method owners**: `fullClass|owner1|owner2|owner3|owner4|owner5\n`.
- **74 combined IDs+owners**: `fullClass|id1,id2,...|owner1|...|owner5\n`.

| Canonical string | ORIGINAL ZIP FNV32 | LIVE GITHUB FNV32 | ORIGINAL ZIP FNV64 | LIVE GITHUB FNV64 | Result |
|---|---|---|---|---|---|
| All 241 `class/count` | `0xf188a064` | `0xf188a064` | `0x9ba3c115841e18e4` | `0x9ba3c115841e18e4` | **MATCH** |
| 74 reviewed `class/exact IDs` | `0xc9faaa4d` | `0xc9faaa4d` | `0x5abff9b62db6e7ad` | `0x5abff9b62db6e7ad` | **MATCH** |
| 74 reviewed `class/five owners` | `0x834ec024` | `0x834ec024` | `0xd48ab9eb3f763ae4` | `0xd48ab9eb3f763ae4` | **MATCH** |
| 74 reviewed `class/IDs/five owners` | `0xe5f165da` | `0xe5f165da` | `0x2f427d21b52dee7a` | `0x2f427d21b52dee7a` | **MATCH** |

Additional **original-ZIP-side ONLY** SHA-256 of these four canonical strings (not falsely claimed to have separately SHA-hashed the JS GitHub side):

1. Roster: `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d`.
2. Reviewed exact IDs: `1cee71522bfed0fd941d10ea4e4230f6c6dc78c9fe951b02d28c479a3d87ce66`.
3. Five owner tuples: `266f32136f4851ccc0bb0e1682e1bff21910156918d6230089b9218c5c93a5cb`.
4. Combined: `3ba1bb0cf29557155c69a1afc98028ef1026a9aea7600c10d9b1c6ce62725f53`.

Original side rebuilt its 74-class reviewed set from the previous original-audited 73 exact class names **plus `SculkBlock`**. GitHub side selects reviewed classes from ACTUAL `SOURCE_REVIEWED_INTEGRATION_PENDING` ledger values; the two sides yield **identical** complete class/ID/owner checksums. FNV hashes are diagnostic reconciliation, not collision-resistant proofs; the independent source ZIP digest SHA-256 is a cryptographic artifact identity check. See previous [3A-8.4 73-class reconciliation](PHASE2_STAGE3A_SCULK_SHRIEKER_CATALYST_ORIGINAL_73_CLASS_RECONCILIATION_1_21_1.md) for previous checkpoint.

**Difference since 3A-8.4 is exactly the one intended previously pending class**:
`net.minecraft.world.level.block.SculkBlock` → `minecraft:sculk`, five owner tuple `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block`. It was separately source+a priori original declaration audited in [3A-9.1](PHASE2_STAGE3A_SCULK_BLOCK_SOURCE_OWNER_AUDIT_1_21_1.md); registered ITEM and all known non-item source writers in [3A-9.2](PHASE2_STAGE3A_SCULK_BLOCK_ITEM_ALTERNATE_WRITERS_1_21_1.md), six-face non-code test requirements in [3A-9.3](PHASE2_STAGE3A_SCULK_GROWTH_SIX_FACE_SEAM_CHARGE_ACCEPTANCE_CONTRACT_1_21_1.md). `SculkVeinBlock` had ALREADY been source-reviewed before packet 3A-8 and is only counted **once**.

## 3. Negative/incomplete gates are part of the verified outcome

| Actual GitHub disposition | Class count | Exact original registered BLOCK IDs | Meaning |
|---|---:|---:|---|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **74** | **195** | pinned comparative source + original nearest method-declaring owners examined for **five signatures**; runtime integration not accepted |
| `REVIEW_PENDING` | **167** | **865** | not yet full class-source and original owner independently promoted |
| **Total** | **241** | **1060** | full original registered roster |

All **74 reviewed rows** have actual exact ID lists, `REFLECTION_OWNER_VERIFIED` and five populated source method declaration owners. All **167 pending rows** retain the exact `registry_dispatch_evidence=REGISTRY_CLASS_AND_COUNT_VERIFIED_ONLY` and `registered_block_ids_reviewed_only=SEE_REGISTRY_ARTIFACT`; do not fabricate full reviewed ID lists for these pending classes. No guessed ID list in source-pending rows.

ALL **241 rows** have **each** of `neoforge_patch_bytecode_review`, `planet_adapter_acceptance` and `gameplay_acceptance` still `REVIEW_PENDING` (**723 outstanding field values**). None of these checked owners proves which NeoForge-transformed bytecode body was invoked at runtime, whether an adapter targets it, six-face Minecraft behavior, survival, worldgen, or a manual gameplay acceptance. No Java change, game tests, Mixin validation, CI execution or acceptance PASS happened in Stage 9.4.

## 4. NEXT specific pending registered owner — Stage 3A-10 LightningRodBlock

Selected from the **original ZIP class roster and exact live pending ledger**, not guessed from material names:

| Registered exact original fact | Verified value |
|---|---|
| `BLOCK.registry_id` | `minecraft:lightning_rod` |
| `BLOCK.java_class` | `net.minecraft.world.level.block.LightningRodBlock` |
| Count | **1** BLOCK ID |
| `class_hierarchy` | `LightningRodBlock > RodBlock > DirectionalBlock > Block > BlockBehaviour` |
| `declared_property_names` | `facing,powered,waterlogged` |
| Original five full signature declaring owners | `LightningRodBlock / BlockBehaviour / LightningRodBlock / BlockBehaviour / Block` |
| Current live GitHub ledger disposition | **REVIEW_PENDING**, no new promotion in 9.4 |
| Original historical `audit_disposition` | **REVIEW_PENDING** |

A bounded source **triage** (NOT the future full registered-class audit, and no promotion) read pinned [`LightningRodBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/LightningRodBlock.java), [`RodBlock`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/RodBlock.java), and [`LightningBolt`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/entity/LightningBolt.java). Important mechanism boundaries for the NEXT packet (nothing accepted yet):

- **Placement owner** `LightningRodBlock.getStateForPlacement` reads actual clicked physical face and water fluid; registered `FACING` is full six-direction. `RodBlock.getShape` selects physical XYZ rod shaft by `FACING.getAxis()`; its rotate/mirror and pathfinding are inherited. Both need canonical target BlockState frame vs source physical hit input; Phase 2 owns state and physical shape consistency.
- **Neighbor + fluid** `LightningRodBlock.updateShape` enqueues WATER tick for WATERLOGGED source, physical vanilla `updateShape`; Phase 2 preserves semantic local contact and Phase 5 fluids. No assumption that \`RodBlock\` handles the subclass's POWERED or WATERLOGGED properties.
- **Lightning event + redstone** `LightningRodBlock.onLightningStrike` writes POWERED true, updates neighbors at `pos.relative(FACING.getOpposite())` and schedules an **8 tick** POWERED false reset. `getSignal` gives 15 when powered; `getDirectSignal` only 15 when the queried direction matches FACING. Redstone neighbor-side interpretation and tick event belong Phase 7A, not just FACING placement.
- **LightningBolt** calls lightning-rod strike from its physical `getStrikePosition`, and `clearCopperOnLightningStrike` checks beneath rod using `FACING.getOpposite()`. Lightning/weather and copper oxidation state changes have physical strike position, vertical surface, geometric search, and tag responsibilities distinct from rod's Item creator, and may have cross-owner consumers.
- **Client-only visual** `LightningRodBlock.animateTick` relies on world-Y vs WORLD_SURFACE heightmap and direction-axis ELECTRIC_SPARK; Phase 3 weather/particle and Phase 7A event must be distinguished. A physically sideways rod is legal even if current vanilla heightmap only drives world-top spark events; no source-only proof of intended Planet local surface lightning targeting.

**Next new task card:** [`docs/phases/phase-02/02h-lightning-rod-weather-owners.md`](../phases/phase-02/02h-lightning-rod-weather-owners.md) starts **Stage 3A-10 ACTIVE 0/4**. `10.1` one exact registered class (original ZIP five declarations, all source method hierarchy + redstone/weather callers); `10.2` exact original ITEM and alternate lightning/copper/template authors; `10.3` six-face physical strike/local FACING/side and 8-tick redstone/spark seam contract, future tests explicitly not run; `10.4` full 241-class reconciliation and next class family selection. Do **not** automatically promote adjacent CopperBulb/WeatheringCopper classes, which remain separate registry owners pending their own audits. \`EndRodBlock\` is already reviewed and is not a substitute for lightning rod behavior.

## 5. Durable status/handoff

**SculkBlock packet Stage 3A-9: 4/4 documentation/source-research microsteps DONE.** Stage 3A and Phase 2 remain **OPEN**, 167 concrete classes not yet reviewed and all 241 ASM/Planet/gameplay gates pending. No Java runtime or Minecraft acceptances changed. Next user message `кк` → **Stage 3A-10.1**, first unchecked checkbox of the new `02h` research card; do only that bounded audit in one GitHub commit and STOP.
