# Stage 3A-17.4 — original NeoForge 241/97-class pointed dripstone complete independent reconciliation and deepest-safe owner handoff

**2026-10-11 · Minecraft 1.21.1 NeoForge 21.1.215 · Planetary 2.0 · Stage 17 4/4 SOURCE RESEARCH ONLY.** One user «кк» covers all internal steps. Original compiled nearest declaring owner source audit is NOT a patched NeoForge ASM/Mixin or gameplay acceptance. [Full vanilla+Eternal Starlight mechanics](PHASE2_STAGE3A_DRIPSTONE_DEEP_ORIGINAL_OWNER_ITEM_FLUID_FALL_WORLDGEN_MOD_1_21_1.md) · [45 future UNRUN tests](PHASE2_STAGE3A_DRIPSTONE_SIX_FACE_FLUID_FALL_MOD_ACCEPTANCE_1_21_1.md) · [deepest-safe mod compatibility rule](PHASE2_DEEPEST_SAFE_MECHANISM_COMPATIBILITY_CONTRACT_1_21_1.md).

## 1. Reopened immutable physical original ZIP + independent full method signature owner parser

Physically present `/mnt/data/phase2-neo1211-registry-census.zip` rehashed SHA256 **MATCH** `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` — source GitHub Actions run `37988064055`, artifact `11643813158`, original source commit `aa39572950a15403ea0a9003eefccf3bf6675ff7`. Python `ZipFile` and TSV `csv.DictReader` reopened and enumerated 1060 exact registered original BLOCK IDs / **241 distinct fully qualified concrete Java BLOCK classes**, 1333 ITEM rows / 1712 blockstate property entries. Original census rows left unchanged.

Independent Python script `/mnt/data/_audit_phase2_17_4.py` frozen **96 previously source-reviewed exact fully qualified class names**, adds **only original PointedDripstoneBlock** as 97th source-reviewed name; this reviewed-set selection is **not based on current GitHub ledger disposition**. Script checks exact original IDs per class, all class entries share five nearest original declaring owners for exact fully qualified argument signatures, and handles semicolon-delimited overloads. The required five method slots are:
- `getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext)`;
- `canSurvive(net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelReader,net.minecraft.core.BlockPos)`;
- `updateShape(net.minecraft.world.level.block.state.BlockState,net.minecraft.core.Direction,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.level.LevelAccessor,net.minecraft.core.BlockPos,net.minecraft.core.BlockPos)`;
- `randomTick(net.minecraft.world.level.block.state.BlockState,net.minecraft.server.level.ServerLevel,net.minecraft.core.BlockPos,net.minecraft.util.RandomSource)`;
- `setPlacedBy(net.minecraft.world.level.Level,net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState,net.minecraft.world.entity.LivingEntity,net.minecraft.world.item.ItemStack)`.

Separate **live JavaScript** audit actually fetched current GitHub branch `2.0` 241-row×16-header named `docs/research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv` blob **`d48a67b133e221e7a368d761825356c4b210349f`** from [intermediate git commit `88f0ecb852de0c94e149703d0e673868c391fd7e`](https://github.com/Abbygree11/Planetary/commit/88f0ecb852de0c94e149703d0e673868c391fd7e); selected 97 rows by live `SOURCE_REVIEWED_INTEGRATION_PENDING` and read actual five owner cells/explicit registered IDs; 144 remaining pending rows untouched. Sorted raw Java full class lexicographic names (not localeCompare), comma-joined exact block IDs, fixed five signature tuple, one line per class with final newline. **Four independent original Python vs live JS FNV32+FNV64 fingerprint matches**:

| Canonical record stream | Original ZIP Python FNV-1a32 == live GitHub JS | Original FNV-1a64 == live GitHub JS | Original-side SHA256 only | Verdict |
|---|---|---|---|---|
| All **241** `class|registeredCount\n` | `0xf188a064` | `0x9ba3c115841e18e4` | `a19d3413788e8b3af0d567b3906d05934202bc7f312b043ea582d4db579a116d` | **MATCH** |
| Reviewed **97** `class|sortedExactId1,...\n` | `0xc13e147e` | `0x4189c54c74b162be` | `780828c7704ac02c782c224df4f8ce9c5cd57d176b58f9d1f3c9546665a103aa` | **MATCH** |
| Reviewed **97** `class|owner1|...|owner5\n` | `0x7ba9ec1a` | `0xcd034ecd33f6c3ba` | `c90a69540bfa7235a7ffc7539316248e0d9d7f1e01c20f21f7059ac3dbbeeb54` | **MATCH** |
| Reviewed **97** `class|exactIds|owner1|...|owner5\n` | `0x9f4feebc` | `0x4c64d2e7962df6dc` | `b318dd5e104c4444e49cbeed22a3723d9d2751f2849db8c077da3cdd28dc0c69` | **MATCH** |

FNV32/64 are noncryptographic diagnostic checks; shown SHA256 canonical streams computed **only by original Python** (not independently checked by JS). Original ZIP SHA256 authenticates the archived data, but no reported hash establishes transformed NeoForge patch bytecode, Planet adapter runtime or successful game tests. Exactly one original registered class newly source reviewed, `PointedDripstoneBlock` / `minecraft:pointed_dripstone`, original five nearest owners **PointedDripstoneBlock / PointedDripstoneBlock / PointedDripstoneBlock / PointedDripstoneBlock / Block**. One original BlockItem `minecraft:pointed_dripstone`, `vertical_direction=up,down`, `thickness=base,frustum,middle,tip,tip_merge`, waterlogged false/true, no BE. Independent third-party `Eternal Starlight SpeleothemBlock` (1.21.1 pinned SHA `3b74d903f07104944d58550fd97fe70c1988527e`) confirms vanilla-only Mixin cannot cover separately copied `Fallable/vertical-direction` mod algorithms, while shared typed source-target canonical vertical chain, phys FallingBlockEntity, cauldron receiver and worldgen must remain **distinct owners**.

## 2. Source-only totals, independent separate acceptance

| Disposition in **live original vanilla** 241×16 TSV | Concrete original registered classes | Exact BLOCK IDs |
|---|---:|---:|
| `SOURCE_REVIEWED_INTEGRATION_PENDING` | **97** | **246** |
| `REVIEW_PENDING` | **144** | **814** |
| TOTAL | **241** | **1060** |

All 241 classes unique, reviewed rows contain all source+original nearest original signatures and exact IDs, all **241×3=723** `neoforge_patch_bytecode_review`, `planet_adapter_acceptance`, `gameplay_acceptance` fields **REVIEW_PENDING**. No code/Mixin modifications or new CI/client/server test; 45 proposed future DT17 tests **NOT RUN**. Earlier partially successful manual falling dripstone/impact tests remain historical observations; they are not a complete 6face/edge/corner/fluid/cauldron compatibility acceptance. Stage3A-17 SOURCE RESEARCH done 4/4, Phase2/Stage3A still OPEN.

## 3. Next cohesive deep mechanism owner family: cauldron receivers (not a one-block patch)

[New Stage3A-18 cauldron receiver and rain/drip world-fluid owner card](../phases/phase-02/02p-cauldron-drip-precipitation-owners.md) exactly **three** original concrete source-pending classes with **four** exact registered IDs: `CauldronBlock` / `minecraft:cauldron` (1), `LayeredCauldronBlock` / `minecraft:water_cauldron` and `minecraft:powder_snow_cauldron` (2), `LavaCauldronBlock` / `minecraft:lava_cauldron` (1). The abstract `AbstractCauldronBlock` is a shared *algorithm* owner, NOT an extra registered concrete original class. Source preliminary triage:
- `AbstractCauldronBlock.tick` receives delayed pointed-dripstone source through `PointedDripstoneBlock.findStalactiteTipAboveCauldron` and `getCauldronFillFluidType`, checking `canReceiveStalactiteDrip` before instance `receiveStalactiteDrip`; **source and receiver are separate dispatch owners**.
- `CauldronBlock.receiveStalactiteDrip` fills into water/lava cauldron from empty; `LayeredCauldronBlock` `LEVEL=1..3` and precipitation type changes on rain/snow, receives WATER via layered increase. `LavaCauldronBlock` is another receiver and collision/hurt source distinct from fluid transport; inspect exact source before attributing inherited cauldron acceptance.
- `CauldronInteraction` is a different item interaction owner (buckets, bottles, leather armor), `Biome.Precipitation` a world weather gate and one ordinary ITEM.placed_block join may omit *special Item routes*; special cauldron commands and `Level.setBlock` possible. Need original full five signature owners, exact 1333 ITEM join and modded cauldron analog source, P0 physical vs semantic water/drip/cauldron callbacks, per-6-face/12-edge/8-corner future tests, final 241-class reconciliation. All remain PENDING; **one future «кк» runs 18.1–18.4 together** as per protocol.
