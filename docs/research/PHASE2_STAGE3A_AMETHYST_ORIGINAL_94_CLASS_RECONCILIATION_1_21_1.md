# Stage 3A-15.4 — independent original NeoForge 241/94-class amethyst audit and cross-mod compatibility handoff

**2026-10-11 · Minecraft 1.21.1 / NeoForge 21.1.215 · Planetary branch 2.0.** Whole amethyst family Stage 15 completed **4/4 SOURCE-ONLY** from one user “кк” including a separately requested **AE2 and other modded crystal** case study. All new test cases are future proposed and NOT RUN. No Java/Mixin, NeoForge patched bytecode inspection, CI build or gameplay acceptance.

## Rehashed original 1.21.1 physical NeoForge ZIP and independent two-parser audit

Actual immutable `/mnt/data/phase2-neo1211-registry-census.zip` (GitHub Actions [run 37988064055](https://github.com/Abbygree11/Planetary/actions/runs/37988064055), artifact **11643813158**, source `aa39572950a15403ea0a9003eefccf3bf6675ff7`). Byte-for-byte SHA256 recalculated and **MATCH**:
`7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e`. Fresh Python `zipfile.ZipFile` / `csv.DictReader(delimiter='\t')` enumerated original 1060 exact registered BLOCK IDs, **241** distinct full class names, 1333 ITEM registrations and 1712 BlockState properties.

Original independent Python audit script: `/mnt/data/_audit_phase2_15_4.py`; results `/mnt/data/_phase2_original_fingerprints_15_4.json`, produced in this session (not pushed). It uses 94 exact previously recognized or newly audited class names, **without selecting the reviewed set from live GitHub status**. For each of 94, it joins original fully qualified concrete BLOCK class and **every exact original registered ID** and checks all five nearest declaring method owners by COMPLETE QUALIFIED ARGUMENT SIGNATURE, not name-only. It explicitly parses extra `;` overload entries such as `GrowingPlantHeadBlock.getStateForPlacement(LevelAccessor)` versus `KelpBlock.getStateForPlacement(BlockPlaceContext)` correctly; previous Stage14 verified that existing KelpBlock ledger was right. Five required owner slots in fixed order:
`getStateForPlacement(BlockPlaceContext)`, `canSurvive(BlockState,LevelReader,BlockPos)`, `updateShape(BlockState,Direction,BlockState,LevelAccessor,BlockPos,BlockPos)`, `randomTick(BlockState,ServerLevel,BlockPos,RandomSource)`, `setPlacedBy(Level,BlockPos,BlockState,LivingEntity,ItemStack)`, with actual FQCN parameter equality.

Separate live GitHub JavaScript audit after intermediate Stage15 checkpoint [`568a42368fef2f7c1d8750a174d29d1907653f52`](https://github.com/Abbygree11/Planetary/commit/568a42368fef2f7c1d8750a174d29d1907653f52) read CURRENT [241-row×16-column owner TSV](PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) Git blob `da6df8eb1b51c7dbd99bcd3813389beca7798459`, independently selected 94 `SOURCE_REVIEWED_INTEGRATION_PENDING` source classes, exact registered IDs and five declaring owner values. Both parsers sort fully qualified Java class strings with raw lexical/codepoint order (NOT localeCompare), each line ends exactly one newline, exact ID values sort comma-joined and five owners preserve required signature order. Recomputed FNV-1a32/FNV-1a64 from Python original ZIP AND separately in JavaScript **ALL FOUR MATCH**:

| Canonical stream | ORIGINAL ZIP FNV32 = LIVE JS FNV32 | ORIGINAL ZIP FNV64 = LIVE JS FNV64 | Python original-side SHA-256 only | Result |
|---|---|---|---|---|
| All 241 `class|count\n` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | **MATCH** |
| 94 reviewed `class|id1,...\n` (243 exact IDs) | `0x8fdd91da` | `0xe1293d388d69a13a` | `c6286e4007b3bd7ecf0747818826c423b4e6babdbcce4dc65b98bbc1bbb701f3` | **MATCH** |
| 94 reviewed `class|owner1|...|owner5\n` | `0xfd43f039` | `0x6809305ec04bc919` | `771fc80eeac39211018a02ec04bf97ab6e80fc063ae7435367a5168960718d36` | **MATCH** |
| 94 combined `class|ids|owner1|...|owner5\n` | `0x44ffce30` | `0x3aa5993d7e463650` | `c19988cabf76178bba137224a12b34fa71fe47daddfc2f2f722260a70efb2bb7` | **MATCH** |

Original-side SHA-256 canonical strings were computed ONLY with Python, NOT duplicated as JS cryptographic proof; FNV32/64 are noncryptographic consistency cross-checks. Immutable source ZIP SHA-256 validates original archive, but original compiled declaring owner reflection is **not** NeoForge patched method bytecode, loaded Mixin or gameplay proof.

## Exactly two new concrete original classes source+declaring-owner reviewed, modded IDs excluded

| New class / original one registered BLOCK ID | Five nearest full-argument declaring owners |
|---|---|
| `AmethystBlock` / `minecraft:amethyst_block` | `Block / BlockBehaviour / BlockBehaviour / BlockBehaviour / Block` |
| `BuddingAmethystBlock` / `minecraft:budding_amethyst` | `Block / BlockBehaviour / BlockBehaviour / BuddingAmethystBlock / Block` |

Original 1333 ITEM join one standard BlockItem for each. Existing `AmethystClusterBlock` **4 original registered IDs** (small, medium, large, cluster) already source-reviewed in earlier Stage3A; unchanged and not counted twice. [Vanilla original and pinned source owner, item and geode writers](PHASE2_STAGE3A_AMETHYST_BUDDING_ORIGINAL_BLOCK_ITEM_GEODE_AUDIT_1_21_1.md); [32 future six-face, geode and modded acceptance tests (ALL NOT RUN)](PHASE2_STAGE3A_AMETHYST_SIX_FACE_GROWTH_MODDED_ACCEPTANCE_CONTRACT_1_21_1.md); [cross-mod actual AE2 1.21.1 and BuddingCrystals 1.21.1 code comparison](PHASE2_STAGE3A_MODDED_CRYSTAL_AE2_BUDDINGCRYSTALS_1_21_1.md).

AE2 **BuddingCertusQuartzBlock** and **CertusQuartzClusterBlock** independently implement physical six-direction growth and 6-FACING/WATERLOGGED via AEBaseBlock, *not subclassing vanilla*, while adding 1/12 parent source wear after successful growth, except flawless. BuddingCrystals **BuddingCrystalBlock extends BuddingAmethystBlock and overrides randomTick**, while **CrystalSet.Builder registers literal vanilla AmethystClusterBlock for four bud sizes**; its custom growthChance can differ from 1/5. That is **two distinct cross-mod dispatch categories** with separate compat gates, even though gameplay algorithms are close. Neither third-party registered class or ID belongs to original 241-class/1060-ID vanilla census, and no modded patched ASM/game client verification occurred.

## Final live source/runtime gate accounting

| Current original NeoForge class disposition | Concrete classes | Exact registered BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **94** | **243** |
| `REVIEW_PENDING` | **147** | **817** |
| **TOTAL** | **241** | **1060** |

All 241 GitHub class names unique, registered count = 1060, all 94 reviewed have exact ID list and five full original owner declarations + `REFLECTION_OWNER_VERIFIED`; other 147 remain original count-only. For ALL **241×3=723** `neoforge_patch_bytecode_review`, `planet_adapter_acceptance` and `gameplay_acceptance` fields, result **REVIEW_PENDING**. No source mutation or tests; original Stage15 4/4 research done, Phase 2 and Stage 3A OPEN.

## Next verified real pending family — Chorus growth/connection

Next [Stage3A-16 chorus growth + 6-connection graph source owner card](../phases/phase-02/02n-chorus-branch-growth-owners.md), exactly **two original source-pending concrete classes/2 BLOCK IDs**:
- `ChorusFlowerBlock` / `minecraft:chorus_flower`; original hierarchy `ChorusFlowerBlock>Block>BlockBehaviour`, legal `age=0..5`. Its source `randomTick` vertical/side branching, stage aging and static `generatePlant/growTreeRecursive` worldgen are independent true physical \`LevelAccessor.setBlock\` creators.
- `ChorusPlantBlock` / `minecraft:chorus_plant`; original hierarchy `ChorusPlantBlock>PipeBlock>Block>BlockBehaviour`, six boolean `down,east,north,south,up,west` connection properties; owner graph and \`canSurvive\` support cannot be inferred from vanilla amethyst attachment logic.

Both still `REVIEW_PENDING`; no original 94 source coverage claimed for them here. Source triage confirmed exact two original registry IDs and comparative files but not yet all five compiled owners/ITEM author graphs. Next **one “кк” → entire Stage3A-16 internal 16.1–16.4**, source research, checkpoint commits, six face/12 edge/8 corner tests, final original ZIP independent reconcile and a new packet, then STOP.
