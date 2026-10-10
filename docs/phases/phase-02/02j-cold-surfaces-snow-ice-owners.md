# Stage 3A-12 — cold surface snow, powder-snow, ice and frosted-ice algorithm owners

**Status: ACTIVE — 0/4 INTERNAL tasks; all registered BLOCK classes and methods source-review-pending, NeoForge patched ASM / Planet adapter / gameplay all PENDING.** Branch `2.0`, Minecraft 1.21.1, NeoForge 21.1.215, Java 21.

**ONE user “кк” → complete ALL internal steps 12.1→12.2→12.3→12.4 in the SAME assistant turn**, with small durable GitHub research/ledger checkpoint commits and concise messages; no user follow-up per .1/.2/.3/.4. Resume first unfinished checkpoint of same packet on interruption; stop only after family complete or an actual blocker. Detailed reports belong in GitHub, not chat. Strict original compiled owner, NeoForge patch, Planet adapter and gameplay acceptance gates are unchanged.

[Phase 2 roadmap](../phase-02.md) · [241 registered BLOCK class ledger](../../research/PHASE2_BLOCK_OWNER_DISPOSITION_1_21_1.tsv) · [3A-11.4 original 241/78 class reconcile](../../research/PHASE2_STAGE3A_EGGS_ORIGINAL_78_CLASS_RECONCILIATION_1_21_1.md) · [gravity mechanism owner map](../../research/GRAVITY_MECHANISM_MAP_1_21_1.md).

## EXACT original registry scope — four source-pending classes, no assumed inheritance equivalence

Original unmodified NeoForge 21.1.215 registry ZIP artifact 11643813158 SHA-256 `7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e` includes 1060 BLOCK IDs/241 classes and 1333 ITEM rows:

| Exact registered BLOCK implementation class | Original exact single registered ID | Properties | ITEM owner distinction |
|---|---|---|---|
| `net.minecraft.world.level.block.SnowLayerBlock` | `minecraft:snow` | `layers` | ordinary `BlockItem` |
| `net.minecraft.world.level.block.PowderSnowBlock` | `minecraft:powder_snow` | *(none)* | **special `SolidBucketItem`** via `minecraft:powder_snow_bucket` |
| `net.minecraft.world.level.block.FrostedIceBlock` | `minecraft:frosted_ice` | `age` | **NO original ITEM with `placed_block=minecraft:frosted_ice`** |
| `net.minecraft.world.level.block.IceBlock` | `minecraft:ice` | *(none)* | ordinary `BlockItem` |

All four are CURRENTLY `REVIEW_PENDING` and NOT promoted in previous Stage 11; they have different inheritance graphs and algorithm owners. Pinned comparator 1.21.1 source showed SnowLayerBlock `canSurvive/updateShape/randomTick` and LAYERS stacking, PowderSnowBlock collision + BucketPickup, FrostedIceBlock extends IceBlock with aging/melting, IceBlock water writes. Four-phase cross-cutting ownership: Phase 2 state authoring/stacking/support and neighbor; Phase 3 render/voxel shape/particles; Phase 5 true snow/ice/water fluid and phase transitions; Phase 7 entity collision/fall/freeze; Phase 8/9 weather/snow/ice natural-worldgen/surface; vanilla/other-modded fake Levels pass through unchanged. No blanket global Direction or world Y rewrite.

## Four INTERNAL subtasks, automatically consecutive on ONE “кк”

- [ ] **12.1 original four concrete BLOCK identities + full compiled owner/source inheritance.** Independently rehash original ZIP and check all four exact BLOCK/props/hierarchies and five nearest fully qualified method declaring owners; pinned 1.21.1 four concrete sources, inherited Block/BlockBehaviour/HalfTransparentBlock/FrostedIce relationship, survival/placement/updateShape/randomTick/scheduled tick, canBeReplaced and collision/fluids. Only promote exact reviewed classes when full source+original compiled owner evidence is sufficient. All ASM/Planet/gameplay REVIEW_PENDING. One checkpoint commit, continue to 12.2 without user message.
- [ ] **12.2 exact original 1333 ITEM join and alternate state authors.** Separate ordinary BlockItem snow/ice, special SolidBucketItem powder snow and NO frosted ice direct item; trace cold surface terrain feature, FrostWalker, fluid/light/melting writes, copied structure state/commands/worldgen, entity interactions and powder snow bucket pickup. Source-based attribution by true exact writer, no invented shipped templates. One checkpoint, continue.
- [ ] **12.3 six-face/edge/corner physical support, fluid and thermal contract with 25+ future UNRUN tests.** All six interiors, 12 face edges, 8 corners, layer stacking, snow survival and face full support, powered heat/light melting, powder snow collisions/freeze/boots, bucket source/target and FrostedIce freeze/melt, real fluids/temperature/chunks, no-Planet control and third-party compatibility; keep physics/world coordinates physical and canonical local-UP only when algorithm semantics needs it. Commit, continue.
- [ ] **12.4 full original ZIP 241-class independent reconciliation and NEXT owner family handoff.** Compare exact GitHub ledger after promotions to original sorted concrete class IDs + five full signature owners for every reviewed class; all 241×3 ASM/Planet/gameplay REVIEW_PENDING; update roadmap/AI_CONTEXT; select next original pending family, create next whole-family card, one final checkpoint GitHub commit and STOP. Never mark source-only research as runtime PASS.

## From completed 3A-11 checkpoint

[Egg/frogspawn source owners, special PlaceOnWaterBlockItem, AI writers](../../research/PHASE2_STAGE3A_EGGS_BLOCK_SOURCE_ITEM_ALTERNATE_WRITERS_1_21_1.md); [30 future UNRUN local-up support/fluids/child-entity fixtures](../../research/PHASE2_STAGE3A_EGGS_SIX_FACE_HATCH_SUBSTRATE_ACCEPTANCE_CONTRACT_1_21_1.md); [original immutable 241/78 reviewed class and five owner reconciliation](../../research/PHASE2_STAGE3A_EGGS_ORIGINAL_78_CLASS_RECONCILIATION_1_21_1.md). Prior counts: **78/241 source+original owner reviewed classes, 199/1060 registered BLOCK IDs**, **163/241** and **861/1060** pending. ALL 241 ASM/Planet/gameplay acceptance REVIEW_PENDING; no Java/CI/game testing. **NEXT user “кк” → whole Stage 3A-12**, not only 12.1.
