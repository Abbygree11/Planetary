# Stage3A-24.3 — 56 future NOT RUN machine/recipe/BE/port/Planet/mod acceptance contracts

**2026-10-11 · all PR24-01…PR24-56 FUTURE NOT RUN.** No Minecraft client/server/gameplay, patched NeoForge21.1.215 ASM, actual Java/Mixin build or mod binary loaded. [Deep verified original 4 class/BlockEntity/Minecraft1.21.1 Iron Furnaces source audit](PHASE2_STAGE3A_FURNACE_BREWING_ORIGINAL_BE_ITEM_MOD_1_21_1.md) and [immutable Python-vs-live GitHub 122-class owner hash audit](PHASE2_STAGE3A_FURNACE_BREWING_ORIGINAL_122_CLASS_RECONCILIATION_1_21_1.md).

Every future test must record `sourceOriginalBlockID`, original legal state property values, original method FULL declaring owner + exact actual compiled NeoForge21.1.215 callsite and injected handler, actual BE class/source/target XYZ, serialized data, true physical provider side vs explicit canonical local source+target frame/chart, original foreign WorldGenLevel/provenance, ItemStack and XP deltas, real GUI/player action (not artificially force-setting blockstates), server/collision, client particle/animation and loaded mod jar version/deps. Separate gates `ORIGINAL_SOURCE/PATCHED_ASM/PLANET_ADAPTER/DEDICATED_SERVER/CLIENT/ACTUAL_MOD/GAMEPLAY`, don't infer any PASS from a research card.

| ID | Future manual/automated fixture | Expected observable result |
| --- | --- | --- |
| PR24-01 | Vanilla normal world original four BlockItems | Exact original BlockState legal keys and BE type for furnace/blast/smoker/brewing |
| PR24-02 | Four exact BLOCK IDs and four ITEM BlockItem joins | Original immutable registry and Item placement match each one |
| PR24-03 | Furnace ordinary four horizontal FACING placement | Actual player view projection, no illegal UP/DOWN |
| PR24-04 | Blast Furnace four horizontal FACING placement | Own BE, correct recipe type BLASTING |
| PR24-05 | Smoker four horizontal FACING placement | Own BE, correct recipe type SMOKING |
| PR24-06 | BrewingStand placement no FACING state | Only HAS_BOTTLE_0..2, no invented orientation |
| PR24-07 | Furnace BE litTime/LIT server tick | Fuel consumes once and actual BlockState.LIT updated by BE direct setBlock |
| PR24-08 | Blast Furnace LIT and recipe time | Actual BLASTING recipe time, not hardcoded furnace 200 for all |
| PR24-09 | Smoker smoking recipe input and output | SMOKING type correctly matches, no cross-smelting |
| PR24-10 | Furnace invalid recipe with fuel | No phantom output and no unjustified ingredient loss |
| PR24-11 | Furnace recipe result components mismatch | No incompatible output stack merging / duplication |
| PR24-12 | Furnace fuel remaining bucket container | Correct consumed fuel and crafting remainder ItemStack |
| PR24-13 | Furnace interrupted burn/progress and replacement | Correct pending progress reset or decay and NBT consistency |
| PR24-14 | Furnace XP and recipesUsed on extraction | Server awards XP/stats once, valid recipe ID counts |
| PR24-15 | Furnace break on server with cooked contents | Drops BE exactly once, XP handling and comparator notification |
| PR24-16 | Furnace 3 slots actual UP side WorldlyContainer | Only input slot [0] exposed, no opt-in side rotation |
| PR24-17 | Furnace 3 slots actual DOWN side WorldlyContainer | Output and fuel slots [2,1] with bucket extraction restriction |
| PR24-18 | Furnace four physical lateral sides | Only fuel slot [1] on native WorldlyContainer |
| PR24-19 | Furnace hopper pulls from physical top | Actual Hopper getSourceContainer above and face DOWN alignment |
| PR24-20 | Furnace hopper output physical below | Provider DOWN and caller UP physical, inventory item conservation |
| PR24-21 | Furnace redstone comparator reacts to inventory | Actual BE stack fullness signal, unchanged world physical side |
| PR24-22 | Furnace NBT chunk reload fuel and recipesUsed | BurnTime/CookTime/CookTimeTotal/RecipesUsed preserved, no instant duplication |
| PR24-23 | Furnace client flame/smoke at FACING relative front | Visual client particle offset matches local chart without world Level mutation |
| PR24-24 | Blast furnace client effects top/side on Planet | Server unchanged, CLIENT visuals canonical face/tangent |
| PR24-25 | Smoker client smoke animation and sound | Per-owner CLIENT effects, not copied furnace particle blindly |
| PR24-26 | BrewingStand slots 0..2 potion containers | All bottle properties track real BE and server occupancy |
| PR24-27 | BrewingStand slot3 potion ingredient | Actual Level.potionBrewing isIngredient rejects invalid component |
| PR24-28 | BrewingStand slot4 fuel powder consumption | Independent fuel=20 counter and proper burn only when ready |
| PR24-29 | BrewingStand 400 tick brewing cycle | serverTick and potionBrewing.mix, one ingredient consumed |
| PR24-30 | BrewingStand ingredient removal mid-brew | Cancels or resets brewTime per original BE |
| PR24-31 | BrewingStand potion mixing nonmatching ingredient | No wrong recipe or double use, 3 valid bottle actions |
| PR24-32 | BrewingStand crafting remainder on ingredient use | Remainder added or physical dropped once, count preserved |
| PR24-33 | BrewingStand UP WorldlyContainer side | Ingredient slot [3] only, provider physical side unchanged |
| PR24-34 | BrewingStand DOWN WorldlyContainer side | Slots [0,1,2,3] with ingredient extraction restrictions |
| PR24-35 | BrewingStand horizontal WorldlyContainer sides | Bottle 0,1,2 and fuel slot4, not furnace fuel slot1 |
| PR24-36 | BrewingStand GUI and comparator/click | Server BE open, physical hit shape, analog contents |
| PR24-37 | BrewingStand 3 booleans updated by BE setBlock | Change only HAS_BOTTLE_* no invalid FACING/LIT |
| PR24-38 | BrewingStand shape + smoke on upside-down Planet face | CLIENT shape/particles match local geometry; physical BE unchanged |
| PR24-39 | BrewingStand break/drop and NBT save/reload | Items/stand config and BrewTime/Fuel exact; no duplicates |
| PR24-40 | No-Planet vanilla parity across all four | Original placement/recipe/cook/loot/port unchanged |
| PR24-41 | All four machines on six Planet face interiors | Exact original state keys & one real BE per physical cell |
| PR24-42 | Six-face on-Planet furnace emission physical vs local | Local display transform only; server physical no global direction rewrite |
| PR24-43 | All 12 Planet edges both traverse directions | No ghost BE, no side double-rotation, item conservation |
| PR24-44 | All 8 Planet corners three face approaches | Deterministic one BlockPos/BE/recipe; correct chart context |
| PR24-45 | Furnace hopper front/back crossing Planet gravity chart | Physical container sides by default, optional bilateral typed local semantics |
| PR24-46 | BrewingStand hopper at boundary chart | Actual UP/DOWN slot contract, no vanilla-mod side mixing |
| PR24-47 | Server unload/reload during smelting and brewing | Server tick and NBT true, no duplicate fuel/items/XP |
| PR24-48 | Iron Furnaces 4.3.2 Minecraft1.21.1 load with deps | Check actual jar 21.1.215 and metadata; SHA41fde... source alone no PASS |
| PR24-49 | IronFurnaces independent BlockIronFurnaceBase placement | Own 4 way FACING and TYPE/JOVIAL/LIT, vanilla Mixin not applied |
| PR24-50 | IronFurnaces TileBase recipes smelt/smoke/blast | Actual mod own recipes/cache+augment/speed/fuel and BE direct LIT writes |
| PR24-51 | IronFurnaces capability SidedInvWrapper IItemHandler | Physical side in RegisterCapabilitiesEvent unless bilateral explicit opt-in |
| PR24-52 | IronFurnaces neighbor energy/output Direction.values | Independent FE energy and physical transfer graph no global rewriting |
| PR24-53 | Unknown third-party furnace custom BE independent hierarchy | No vanilla callback assumed, PASS only after actual runtime test |
| PR24-54 | SophisticatedStorage mod version 21.1.229 required | Explicit incompatible with Planet target21.1.215, SKIP binary |
| PR24-55 | Foreign schematic/structure/WorldGenLevel block+BE writer | No BlockItem hook; original physical Level/BE persists without aliases |
| PR24-56 | Concurrent 200+ furnace/brewing + hopper machines | Server tick cooldown, save/reload and memory/CPU bounded; no regression |

## Deepest SAFE contract for acceptance

- **SHARED_SAFE:** world-mode-aware immutable actual original `BlockPos`, BE identity, original sided Direction, original full method declaring owner/caller/provider, optional source and target canonical frame/chart/tangent. No global redirection of Level, BlockPos, Direction, NeighborUpdater or provider sides.
- **OPT_IN_ALGORITHM:** three vanilla furnace block's horizontal-only four FACING states, a separate machine BE ticking and fuel/cooking/recipe type owner; brewing stand has ZERO FACING keys (3 bottle flags) and `PotionBrewing` independent 400-tick cycle/fuel. Exact BlockState law.
- **MOD_INDEPENDENT:** real Iron Furnaces 4.3.2 build metadata MC1.21.1 neo_version21.1.209 and range[21.1,), source pinned SHA41fde..., custom `BlockIronFurnaceBase/TileBase/WorldlyContainer/CapabilityIronFurnaces`; no full jar build/game test. Unknown capabilities PHYSICAL; target 21.1.215 may have differing patched bytecode and mod-dependent acceptance.
- **PHYSICAL_DO_NOT_TOUCH:** original true world-Y input/output side and neighbor for vanilla hoppers/capability, real BE world address/item count and chunk persistence, foreign fake Level, original potionBrewing recipe registry. `IItemHandler` side transformation requires both caller/provider typed agreement.
- **CLIENT_RENDER:** actual original four furnace facing front smoke/fires and brewing stand no-facing bottles/smoke separate from server BE real slot lookup and physics.
- **Coverage:** normal no-Planet baseline, all 6 face interiors, all 12 edges both crossing directions, all 8 corners, chunk boundaries/unload/reload, real actual supported modded FE and IItemHandler source, stress tests 200+ BE. All evidence still must be collected.

**All 56 scenarios NOT RUN. ALL 241×3=723 original patch ASM, Planet integration, gameplay acceptance gates REVIEW_PENDING.**
