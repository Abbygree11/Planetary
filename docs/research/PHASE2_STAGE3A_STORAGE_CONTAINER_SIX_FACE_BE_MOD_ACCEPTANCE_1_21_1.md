# Stage3A-22.3 — 55 future UNRUN storage/container/BlockEntity/actual mod six-face acceptance contracts

**2026-10-11 · ST22-01…ST22-55 all FUTURE NOT RUN.** These are test designs and no claim of fixed runtime behavior, transformed ASM, loaded game/client/server or mod. [Full source + pinned mod](PHASE2_STAGE3A_STORAGE_CONTAINER_DEEP_OWNER_ITEM_BE_MOD_1_21_1.md); [independent 112-class physical ZIP-vs-live ledger](PHASE2_STAGE3A_STORAGE_CONTAINER_ORIGINAL_112_CLASS_RECONCILIATION_1_21_1.md).

**Future evidence mandatory for each fixture:** original unique ID/declaring owner and BlockState legal domains, actual exact NeoForge21.1.215 patched target owner + method descriptor/invoke/handler ASM, runtime Planet adapter source, world mode/real XYZ support and neighboring physical six-graph, source+target canonical face/chart, per-action BlockHitResult with hit physical direction+point, actual physical source/target BE IDs and NBT/components, client renderer and actual collision, vanilla no-Planet baseline, mod jar/metadata/deps and full IItemHandler side call chain, server/client outcome and performance. Mark separate fields `ORIGINAL_SOURCE`, `PATCH_ASM`, `PLANET_ADAPTER`, `DEDICATED_SERVER`, `CLIENT_RENDER`, `ACTUAL_MOD_LOAD`, `GAMEPLAY` as PASS/FAIL/SKIP, not green from source doc.

| Test ID | Future physical/Planet/mod action | Expected observation | Owner group |
| --- | --- | --- | --- |
| ST22-01 | Vanilla non-Planet world normal BlockItem place all five | Exact legal state properties and five concrete BEs per block | BASELINE |
| ST22-02 | Five original registry BlockItem joins | decorated_pot/bookshelf/barrel/chest/ender_chest; 5 direct items and 5 block IDs | ARCHIVE |
| ST22-03 | All five canSurvive inherited owner, external support changes | Only original BlockBehaviour semantics, not transplanted candle support | SUPPORT |
| ST22-04 | Barrel facing UP/DOWN and four horizontal using player view | Only BarrelBlock supports full six FACING; OPEN legal bool | PLACE |
| ST22-05 | Chest/Ender/Bookshelf/Decorated facing 4 legal horizontal directions | Never set illegal physical UP/DOWN into 4-way properties | PLACE |
| ST22-06 | Decorated pot WATERLOGGED beside/inside water | Real FluidState and water tick, CRACKED independent | FLUID |
| ST22-07 | Chest/Ender WATERLOGGED placement/updateShape | Real water tick, no fake fluid lookup | FLUID |
| ST22-08 | Barrel/Bookshelf no waterlogged original property | No synthetic WATERLOGGED/invalid blockstate assignment | FLUID |
| ST22-09 | Decorated Pot insert 1 matching ItemStack | Single BE stack, one consume, correct component equality and sounds | TRANSACTION |
| ST22-10 | Decorated Pot insert mismatched/full stack | No cross-item merge, no oversize, correct pass to alternate use | TRANSACTION |
| ST22-11 | Decorated Pot empty-hand interaction | Wobble and fail sound, no FlowerPot-style plant extraction | TRANSACTION |
| ST22-12 | Decorated Pot pick/clone and item placement with sherds | PotDecorations component persists through BE and ItemStack | COMPONENT |
| ST22-13 | Decorated Pot tool shatter / enchanted protection | Cracked state, correct dynamic sherd loot, no duplicate contents | DROPS |
| ST22-14 | Decorated Pot projectile allowed/disallowed break | mayInteract/mayBreak and cracked/drop BE exact once | DROPS |
| ST22-15 | Decorated Pot CRACKED render & particle world-Y source | Correct locally authored animated/client visual, true physical drops | RENDER |
| ST22-16 | Bookshelf six book slots clicked front N/S/E/W | Hit face correct, rows Y≥0.5 vs lower, columns thresholds0.375/0.6875 | HIT |
| ST22-17 | Bookshelf from back/top/bottom physical click | Nonfront returns no slot; no phantom book insertion | HIT |
| ST22-18 | Bookshelf place only BOOKSHELF_BOOKS tag item | One-item transaction and slot_0..5 bool mirrors BE | BOOKS |
| ST22-19 | Bookshelf empty-slot removal / full-slot insertion | No double spend or corruption, proper pass/consume behavior | BOOKS |
| ST22-20 | Bookshelf last-interacted comparator 0..6 | Compares last slot+1, NOT fullness signal | REDSTONE |
| ST22-21 | Bookshelf 6 slot component save/load and break drops | Exact item NBT/components, no duplicate drops, slot states sync | PERSIST |
| ST22-22 | Barrel open/close multiple simultaneous players | ContainerOpenersCounter OPEN state, server tick and client sound | BE |
| ST22-23 | Barrel comparator and loot-table delayed initialization | True inventory & loot/openers stat, no double initialization | BE |
| ST22-24 | Barrel open piglin alert event | Server-only piglin behavior and stat remain original | ENTITY |
| ST22-25 | Chest single placement and physical nearby candidate partner | SINGLE or LEFT/RIGHT correct from actual BlockPlaceContext | PAIR |
| ST22-26 | Chest sneak-click on side of single chest | Placement respects clicked physical face, side and same FACING | PAIR |
| ST22-27 | Chest placement with two candidate neighbors | Pair only legal same type and no wrong triple chest | PAIR |
| ST22-28 | Chest left/right partner removed | Both TYPE changes valid with physical neighbor and one BE identity each | PAIR |
| ST22-29 | Chest DoubleBlockCombiner 54-slot menu | Two distinct BEs, each 27 inventory, correct order, no alias | BE |
| ST22-30 | Chest blocked by solid conductor above | Original physical above blocker path and menu denial | BLOCKER |
| ST22-31 | Chest blocked by sitting cat above | Actual world AABB + entity pose, no global displaced box | BLOCKER |
| ST22-32 | Chest clear or cat exits | Menu opens, loot table and both halves once | BLOCKER |
| ST22-33 | Chest double-slot comparator and contents updates | Correct combined inventory and neighbor notification | REDSTONE |
| ST22-34 | Chest client lid renderer & server open recheck | Consistent physical lid pivot and original per-BE opener count | RENDER |
| ST22-35 | Ender Chest 2 adjacent blocks | No ChestBlock pairing; always separate BE lid/openers | ENDER |
| ST22-36 | Ender Chest per-player inventory two players | Two independent PlayerEnderChestContainer datasets, no shared BE item storage | ENDER |
| ST22-37 | Ender Chest physical above blocker | Only menu/lid blocked according to vanilla predicate, no item loss | ENDER |
| ST22-38 | Ender Chest move/remove/re-place across dimensions | Player items remain in player ender storage, BE resets lid only | ENDER |
| ST22-39 | Ender Chest portal particles on six faces | Client motion local render transform only; no false server item effect | RENDER |
| ST22-40 | Six Planet face interiors all five placements | Actual player placement, legitimate states, correct BE, local visual normal | FACES |
| ST22-41 | All 12 Planet edge transitions both directions | No ghost partner/BEs, physical coords and frame transport clear | EDGE |
| ST22-42 | All 8 Planet three-face corners all approaches | Deterministic one real block/BE identity, no item duplication | CORNER |
| ST22-43 | One chest half on each side of a Planet edge | Explicit policy for crossing physical adjacent charts, pairing stable | EDGE |
| ST22-44 | Chest single-to-double and double-to-single at corner | No broken topology, no triple connection, preserve existing items | CORNER |
| ST22-45 | Bookshelf all six face local-front pixel clicks | Local hit coordinate transform selects intended one-of-six slots | FACES |
| ST22-46 | Pot sherd particle/item drops and BER on bottom Planet face | Client particle and true dropped item entity physics independently accepted | FACES |
| ST22-47 | World chunk seam adjacent double chest, reopen and restart | Two saved BEs, no chunk ghost partner and no lost inventory | PERSIST |
| ST22-48 | Save/reload all five BE inventories/components/open counters | NBT/DataComponents/loot after version-safe reload | PERSIST |
| ST22-49 | Mod Sophisticated Storage1.21.1 source pinned SHA | Source comparator baseline NeoForge21.1.229 too new for21.1.215; actual mod BINARY TEST SKIP | MOD |
| ST22-50 | Mod Sophisticated Storage chest pairing and controller | Independent ChestBlock/BE + ItemStack upgrade and paired physical graph | MOD |
| ST22-51 | Mod Sophisticated Storage barrel 6 FACING and custom components | Own state domain + BE material retained, no vanilla Mixin coverage claim | MOD |
| ST22-52 | Mod IItemHandler capability physical side | Unknown third party gets actual physical Direction even in Planet | SIDED |
| ST22-53 | Mod opt-in physical-to-local item side negotiated both parties | No accidental double rotation, no invalid call to unknown provider | SIDED |
| ST22-54 | Foreign clipboard/WorldGenLevel direct BlockState/BE placement | No wrong item placement hooks, preserve foreign world-provenance | FOREIGN |
| ST22-55 | Large network of 100+ chests, hoppers and mod storage | Bounded checks, no duplicate tick/menu/double BE per physical cell | PERF |

## Deepest SAFE adapter/phase contract for the 55 future cases

- **SHARED_SAFE:** immutable real source/target BlockPos, physical incoming NeighborUpdater side, original item/BE author, typed canonical source+target frame/tangents, chart and foreign WorldGenLevel flag. It **never automatically rewrites physical Direction**, and never equates neighbor world positions across chart boundaries.
- **OPT_IN_ALGORITHM:** legal FACING domain by original class (Barrel 6; other four horizontal 4); Chest double pairing/combiners/BE, bookshelf hit local 3×2, pot one-stack/shards, Ender per-player inventory, cat/suffocation physical obstruction vs semantic local top, each a distinct algorithm with source-appropriate versioned adapter.
- **MOD_INDEPENDENT:** SophisticatedStorage custom hierarchy, controller double chest ItemStack component migrations, barrellike custom FACING and sided IItemHandler (declared required NeoForge21.1.229; cannot load target21.1.215 without compatible version), unknown custom storage requires real installed compatible jar and typed explicit provider/caller opt-in. Do not infer any automatic compatibility of copied algorithm.
- **PHYSICAL_DO_NOT_TOUCH:** Level/BlockPos/FluidState, item capability side without opt-in, actual BlockEntity world address, direct `Level.setBlock`/structure writes, literal BlockHitResult physical hit, chunk identity; never patch global BlockStateBase, Direction or all sided ItemHandlers indiscriminately.
- Phase2: placement/neighbor topology and interactions; Phase3: VoxelShapes, BER, lid and hit; Phase5 true water; Phase6 comparator/scheduled tick; Phase7 BE contents/drop/piglin/cat; Phase8/9 direct worldgen/clipboard; Phase10 mod capability/foreign transport. All physical/mod and local semantic layers require separate independent acceptance.

**ALL 55 cases UNRUN and all original 241×3=723 patched-ASM/Planet-adapter/gameplay columns REVIEW_PENDING.**
