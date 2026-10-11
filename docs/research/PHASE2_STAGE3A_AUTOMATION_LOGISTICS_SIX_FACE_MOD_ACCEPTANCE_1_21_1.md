# Stage3A-23.3 — 68 future NOT RUN vanilla/modded automation and logistics acceptance fixtures

**2026-10-11 · AU23-01..AU23-68 ALL NOT RUN.** Source-only research, not running a test client, dedicated server, transformed NeoForge21.1.215 ASM inspection, applied Planetary Mixin, or any third-party mod jar. [Deep owners/real pinned mod evidence](PHASE2_STAGE3A_AUTOMATION_LOGISTICS_ORIGINAL_BE_ITEM_MOD_1_21_1.md), [physically independent 118-class source ledger](PHASE2_STAGE3A_AUTOMATION_LOGISTICS_ORIGINAL_118_CLASS_RECONCILIATION_1_21_1.md).

Each future acceptance must retain: real physical target `BlockPos`, true outgoing source/destination side, exact original registered class BLOCK ID and ITEM and legal property values, source+target Planet face/chart/tangent, source and target BE identity, mod jar/version/NeoForge dependency resolution (BotanyPots21.1.209 dev baseline plausible but not tested, SophisticatedStorage21.1.229 min > project21.1.215 not compatible), all worldgen/fake-Level provenance, server tick number and transfer ItemStack deltas, actual rendered model/BE/shape vs physical collision, vanilla no-Planet baseline and complete transformed ASM method owner/injector proof. Seven separate eventual gates `SOURCE/ASM/PLANET_ADAPTER/SERVER/CLIENT/ACTUAL_MOD/GAMEPLAY`; none granted automatically by these designs.

| ID | Unrun test action | Future concrete acceptance contract |
| --- | --- | --- |
| AU23-01 | Original no-Planet six families actual BlockItem place | Vanilla state domain + real target BE + inventory initial state |
| AU23-02 | Original 17 shulker ID/item registry exact joins | 17 registered BLOCK and 17 ITEM one Java owner; no color property |
| AU23-03 | Shulker FACING on all six physical clicked faces | Legal FACING six, real BlockPlaceContext.getClickedFace |
| AU23-04 | Shulker occupied space prevents lid opening | Shulker.getProgressDeltaAabb and Level.noCollision physical sweep |
| AU23-05 | Shulker opening moves colliding entities | BE tick/moveCollidedEntities actual physical AABB, no clipping |
| AU23-06 | Shulker open support shape/collision shape | Physical support vs visual render separated |
| AU23-07 | Shulker survival/no fake sturdy-below predicate | Original BlockBehaviour.canSurvive remains intact |
| AU23-08 | Shulker 27 item inventory survives BlockItem break/re-place | DataComponents.CONTAINER, exact inventory no duplication |
| AU23-09 | Shulker creative nonempty break | collectComponents ItemEntity one correct container stack |
| AU23-10 | Shulker regular loot dynamic contents and silk/pickup | No item dupes or container contents lost |
| AU23-11 | Shulker WorldlyContainer sided slot restrictions | Physical Direction sides unchanged unless both endpoints opt in |
| AU23-12 | TrappedChest 4-way placement and double link | Inherits exact ChestBlock pairing, no forced illegal UP FACING |
| AU23-13 | TrappedChest opens one/two player viewer count | TrappedChestBlockEntity opener count, proper redstone output clamp15 |
| AU23-14 | TrappedChest direct redstone signal only physical UP | No global remap of redstone getDirectSignal direction |
| AU23-15 | TrappedChest cat/above conductor blockage | Exact inherited ChestBlock BlockPos.above and sitting cat AABB |
| AU23-16 | TrappedChest chunk split double block redraw | Unique two BEs and real pair state after restart |
| AU23-17 | Hopper placement clicked ceiling/up/down | Legal hopper facing excludes UP, always fallback DOWN for vertical |
| AU23-18 | Hopper 5-slot inventory and item entity attraction | Physical entityInside + intake AABB and server ticker |
| AU23-19 | Hopper push to physical horizontal output Container | REAL pos.relative(FACING), correct target opposite side |
| AU23-20 | Hopper push physical DOWN to storage below | One item/transaction correct target Direction.UP |
| AU23-21 | Hopper pull from physical above Container | REAL y+1 source, WorldlyContainer Direction.DOWN |
| AU23-22 | Hopper WorldlyContainer source denies extraction DOWN | Original refusal and no missing item; no global side rotation |
| AU23-23 | Hopper WorldlyContainer target denies insertion side | No cross-side acceptance and no invisible item loss |
| AU23-24 | Hopper redstone disables/re-enables ENABLED state | Real hasNeighborSignal, correct transfer cooldown |
| AU23-25 | Hopper same tick push/pull loop performance | No duplications or unbounded transfer in single server tick |
| AU23-26 | Hopper entityAbove vs block-level attached source | Physical AABB and pickup independent from item side |
| AU23-27 | Dispenser six-direction FACING placement | Nearest looking opposite 6 legal values |
| AU23-28 | Dispenser rising-edge powered + pos.above input | Exactly once 4 tick scheduled activation, false edge clears TRIGGERED |
| AU23-29 | Dispenser slot selection deterministic seed fixture | BE selects one nonempty slot correctly |
| AU23-30 | Dispenser empty slot/noop behavior | No falsely created item; original event/sound |
| AU23-31 | Dispenser registered special projectile item | Correct item behavior registry; muzzle and particle separate |
| AU23-32 | Dispenser registered bucket/fluid action | Real fluid/BlockSource target semantics, no global fluid rewrite |
| AU23-33 | Dispenser spawn-egg/firecharge/shear third-party behavior | Per-item owner provenance or explicit test skip if no pinned loaded mod |
| AU23-34 | Dispenser modded DispenseItemBehavior registrant loaded | Version-pinned actual mod source, if missing EVIDENCE GAP/SKIP |
| AU23-35 | Dropper inherits trigger/placement but not dispenseFrom | Own override physically targets front, no special item behavior |
| AU23-36 | Dropper inserted into sided WorldlyContainer | One item from direction opposite, no double consume |
| AU23-37 | Dropper no target container | One item physical default ejection, not dispenser special behavior |
| AU23-38 | Dropper target full/disabled slot | No item loss, source inventory unchanged |
| AU23-39 | Crafter all 12 legal FrontAndTop orientation states | Exactly correct front+top perpendicular pair, not 24 or 6 |
| AU23-40 | Crafter vertical front placement with camera horizontal tangent | Correct UP/DOWN front top chosen by horizontal view |
| AU23-41 | Crafter horizontal front placement | Top default physical UP in vanilla no-Planet; legal pair |
| AU23-42 | Crafter placed initially powered | setPlacedBy schedules own tick, no omitted initial writer |
| AU23-43 | Crafter toggled powered on/off and CRAFTING state | Rising-trigger once, scheduled craft and animation ticks independent |
| AU23-44 | Crafter 9 slots with disabled slots stored in BE | CraftingInput ignores disabled slots, no ghost ingredients |
| AU23-45 | Crafter exact recipe selection/assemble and consume | One recipe per trigger, no arbitrary downstream item mutation |
| AU23-46 | Crafter recipe remainder (bucket/container item) | Remaining outputs transfer or spawn once, never duplicated |
| AU23-47 | Crafter output inserts into physical front Hopper/Container | REAL pos.relative(orientation.front), provider opposite side |
| AU23-48 | Crafter output container full or invalid slot | Correct leftover physical item spawn and preserve recipe counts |
| AU23-49 | Crafter comparator analog signal and GUI/reload | Stored disable/slot progress maintained on server/client |
| AU23-50 | Vanilla no-Planet parity all six families | Baseline same legal states, exact item outputs/BE/openers/power |
| AU23-51 | Player place all 22 original BLOCK IDs on all 6 Planet face interiors | Unique block/BE address, legal properties; no invented UP Hopper |
| AU23-52 | Transport hopper/special output on all 6 canonical faces | Original physical target graph unless specific opted-in local semantic |
| AU23-53 | Crafter front/top on all six Planet face interiors | Valid FrontAndTop state and chart tangent, output correct real cell |
| AU23-54 | Shulker lid collision and push on all six Planet faces | Actual server 3D collision and client BER agree |
| AU23-55 | Each of 12 Planet edges both crossing approaches | No alias inventory/duplicate ItemStack, source and target chart pair explicit |
| AU23-56 | Each of 8 Planet corners three approach routes | Correct one real block/BE per physical position, orientation stable |
| AU23-57 | Two physically adjacent double trapped chests across edge | No invalid chart pairing/double opener signal, independent BEs |
| AU23-58 | Hopper/output across chunk boundary and server restart | Correct physical ItemStack count and cooldown persisted |
| AU23-59 | BotanyPots MC1.21.1 mod + dependencies actual jar load | Pinned 21.1.209 source dev baseline only, runtime version must be proven |
| AU23-60 | BotanyPots WORLDLYCONTAINER slot filtering real DOWN | Provider side gets original physical direction when opt-in absent |
| AU23-61 | BotanyPots self-export below with Direction.UP target | Independent BE physical below and explicit caller+provider opt-in |
| AU23-62 | SophisticatedStorage MC1.21.1 required NeoForge21.1.229 | Target 21.1.215 INCOMPATIBLE, skip actual binary, don't claim PASS |
| AU23-63 | Modded IItemHandler caller+provider side semantics | Never auto-transform unknown physical Directions |
| AU23-64 | Foreign fake Level, schematics, structure-generated redstone | Preserve original real BE/capabilities and typed world provenance |
| AU23-65 | Mass 200+ machine automation throughput baseline | Server tick/cooldown/performance/GC, no explosive checks |
| AU23-66 | NeoForge21.1.215 original patched Hopper/dispenser runtime ASM | Verify exact modified call-side descriptor, owners and Mixin handler contracts |
| AU23-67 | Client output particle/vibration/BER vs server physics | No displaced world-Y particles, real hit/collision consistency |
| AU23-68 | Save/reload shulker/trapped/hopper/dispenser/dropper/crafter | All BE+DataComponents+current triggered states coherent, no duplicates |

## Explicit deepest SAFE compatibility gate (no Java or Mixin implemented)

| Boundary | Status/policy | Required future fixture evidence |
| --- | --- | --- |
| SHARED_SAFE physical+chart typed provenance | Proposal ONLY, no automatic original Direction mutation | Physical world/BE target and optional local source+target chart available for actual algorithm owner |
| OPT_IN_ALGORITHM / Shulker OPEN collision | Proposal ONLY, six legal FACING values | Real opening swept AABB, movement and BER align on six faces |
| OPT_IN_ALGORITHM / Hopper and dropper IO | Proposal ONLY; Hopper FACING=5 and intake physical UP independent | Correct original side, item exchange, denied sided provider, no UP illegal property |
| OPT_IN_ALGORITHM / Crafter 12 front+top | Proposal ONLY; two-axis legal pair, recipe/cooldown/remainder | Twelve legal enumerated values, correct target and one recipe transaction |
| OPT_IN_ALGORITHM / Dispenser custom item | Proposal ONLY; respect actual DISPENSER_REGISTRY and mod callback | Behavior-by-item correct, projectile/fluids retain their original physical policy unless opt in |
| MOD_INDEPENDENT | BotanyPots and SophisticatedStorage owners distinct, explicit caller+provider opt-in only | Real source, jar compatible with target and target IItemHandler/WorldlyContainer proven; incompatibility SKIP |
| PHYSICAL_DO_NOT_TOUCH | Original Level/BlockPos/Direction/NeighborUpdater/FluidState/BE/chunk/provider side real | No global monkeypatch of true six-neighbor/world graph or foreign fake world |

World intents: vanilla non-Planet; **6 face interiors**, **12 paired-face edges each crossed both ways**, **8 three-face corners all approaches**; real world chunk transition/save/reload; actual compatible mod loaded; client render and server item counts. Explicitly do not count synthetic force-set state as player placement and do not mark unknown mod compatible without loaded jar.

**ALL 68 cases NOT RUN; ALL 241×3=723 original NeoForge ASM/Planet integration/gameplay gate columns REVIEW_PENDING.**
