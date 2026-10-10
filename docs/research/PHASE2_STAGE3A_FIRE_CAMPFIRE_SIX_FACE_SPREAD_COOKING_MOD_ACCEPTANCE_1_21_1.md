# Stage 3A-20.3 — 50 FUTURE unrun fire / campfire six-face, seam, cooking, ITEM, portal and mod acceptance fixtures

**2026-10-11 · ALL F20-01…F20-50 = NOT RUN.** [Deep original source/mod/owner mechanisms](PHASE2_STAGE3A_FIRE_CAMPFIRE_DEEP_ORIGINAL_ITEM_SPREAD_COOKING_MOD_1_21_1.md) · [independent original 241/106 class audit](PHASE2_STAGE3A_FIRE_CAMPFIRE_ORIGINAL_106_CLASS_RECONCILIATION_1_21_1.md). This design document is NOT an applied patch or positive client/server test.

Required instrumented fixture record: original NeoForge actual version/compiled effective full signature and future loaded patched ASM hash, Planet code SHA, Mod/version/loader minimum, source physical block+support+target chunk and legal state properties, transported target canonical face and edge/corner traversal chart, true physical fluid, FIRE `age` and 5 bits vs campfire 4-way facing, random seed and doFireTick, scheduled tick queue vs randomTick, portal source axis, direct ITEM/Dispenser path, heat consumer tag/BlockState matcher, campfire BE recipe/item slots/save, player damage and entity hitbox, output and renderer/smoke actual client, snapshot tests no-Planet. Each fixture must report independent original bytecode, patched-ASM-applied, Planet adapter, server behavior, client visuals and actual third-party mod acceptance **PASS/FAIL/SKIP**; without execution all remain NOT RUN.

| ID | Future action | Required observed result |
| --- | --- | --- |
| F20-01 | Plain and soul campfire ordinary BlockItem placement no Planet | Two original IDs, legal facing N/S/E/W, true FluidState and below hay determine flags |
| F20-02 | Place campfire in real WATER | WATERLOGGED=true, LIT=false, real water tick, no fake soul fire |
| F20-03 | Plain and soul campfire below hay versus other block | SIGNAL_FIRE true only from original hay below state, independent of client smoke |
| F20-04 | Campfire support removed and neighbor update from each physical side | Use actual original inherited canSurvive and Campfire.updateShape behavior, no fabricated FireBlock survival rule |
| F20-05 | Campfire waterlogging while lit | Dowse, real FluidState, world event, BE notification and no cookingTick after unlit |
| F20-06 | Campfire water tick/neighbor update while waterlogged | Only physical WATER fluid tick scheduled, not changed by semantic support face |
| F20-07 | Dry unlit campfire flint and steel | Valid tagged firestarter lights once, original stack damage, server sound and event |
| F20-08 | Dry unlit campfire fire charge | Correct item interaction, no duplicate use callback |
| F20-09 | Burning projectile hits unlit dry campfire | CampfireBlock.onProjectileHit mayInteract and LIT/WATERLOGGED conditions respected |
| F20-10 | Burning projectile hits wet campfire | No relighting while WATERLOGGED |
| F20-11 | Lit campfire entityInside living entity; normal vs soul | Actual contact damage 1 vs 2 base raw value, source damage semantics retained |
| F20-12 | Unlit campfire entity contact | No campfire burn hit while LIT=false |
| F20-13 | Cookable item interaction and original BlockEntity slots 0..3 | Only real campfire recipe accepted, one item per slot, no phantom fifth item |
| F20-14 | Campfire 4 slots full and invalid food | No consume on invalid recipe or overfill |
| F20-15 | Server campfire cooking tick lit | Progress increases and recipe completes, output drops at real XYZ |
| F20-16 | Doused campfire after partial progress | Unlit ticker cooldownTick decreases progress by 2 each tick; preserved items |
| F20-17 | Campfire save/reload mid cook and after dowse | CookingTimes/CookingTotalTimes/items valid and unchanged on chunk reload |
| F20-18 | Campfire broken with items | onRemove drops stored items once, no duplicate phantom stacks |
| F20-19 | Campfire recipe integration from modded recipe manager | Actual recipe and output components preserved; no hard-coded vanilla foods |
| F20-20 | Smoke check 1..5 below via CampfireBlock.isSmokeyPos | Bounded source detection, lit tag and fence obstruction policy |
| F20-21 | Campfire with hay signal smoke client | Correct signal particles vs normal smoke and effect on smoke consumers |
| F20-22 | Campfire BE particleTick 4 item offsets per horizontal facing | Local tangent + four legal FACING combinations; no world-Y-only drift on Planet |
| F20-23 | FireBlock placement above sturdy nonflammable support | Default FIRE age0, support from real below physical UP |
| F20-24 | FireBlock placement adjacent combustible east/west/north/south/up | Five booleans represent physical burning surfaces; no DOWN property |
| F20-25 | Original FireBlock 5-bit shape combinations | Cached voxel variant physically matches fire state and no incorrect culling |
| F20-26 | FireBlock support removed | canSurvive checks below and 6 physical flammables; unsupported goes AIR |
| F20-27 | FireBlock scheduled tick with doFireTick disabled | No new spreading, preserve vanilla scheduled tick dispatch rules |
| F20-28 | FireBlock scheduled tick under gamerule enabled | 30..39 tick delay and AGE capped0..15; actual callback, not randomTick |
| F20-29 | Original FireBlock direct six-neighbor burnout | Physical 6 links, tag/flammability/hydration gates preserved |
| F20-30 | Original FireBlock spread volume xz[-1,1], y[-1,4] | Explicit local volume policy, bounded actual positions and seeded outcome |
| F20-31 | Fire exposed rain and humid biome | isNearRain and burnout branch on actual weather, no unrelated global heightmap rewrite |
| F20-32 | Fire on infiniburn source in designated dimension | Dimension tag and age/spread/existence semantics preserved |
| F20-33 | Soul fire on valid tag substrate | BaseFireBlock.getState chooses SOUL_FIRE with no AGE/properties |
| F20-34 | Soul fire loses substrate | SoulFireBlock.updateShape returns AIR; no FireBlock scheduled spread |
| F20-35 | FIRE and SOUL_FIRE entityInside | Correct base raw fire damage instance and source, no bogus blanket entity coordinate mutation |
| F20-36 | Flint and steel / fire charge on AIR next to supported block | Actual item useOn + BaseFireBlock.canBePlacedAt, no ordinary FIRE BlockItem |
| F20-37 | Dispenser flint and steel creates valid fire or lights campfire | Special DispenseItemBehavior dispatch, original source target and legal BlockState |
| F20-38 | Obsidian portal nearby and fire trigger | Physical PortalShape axis/rectangle valid, no rotated fake portal coordinates |
| F20-39 | BaseFireBlock client animateTick adjacent physical combustible | Client flames/smoke positions follow declared source and no server spread from particles |
| F20-40 | No-Planet vs Planet inactive ordinary vanilla world | All item, fire spread, smoke, BE, entity, game rule behavior identical |
| F20-41 | Campfire/fire/soul fire interior of each six Planet gravity face | Per block real physical support, legal HORIZONTAL_FACING, translated semantic vertical |
| F20-42 | All 12 face-pair edges both charts | No duplicate burning target, one physical fire propagation and stable support face |
| F20-43 | All 8 three-face corners multiple approaches | One real block and deterministic chart, no phantom fire/smoke/BE |
| F20-44 | Physical XZ chunk seam and un/reload with campfire BE + fire tick | Persist BE and avoid duplicate ticks or lost cooking; deterministic expected physical cells |
| F20-45 | 1.21.1 mod BareBonesBrews thermal cauldron over campfire | Real below state/tag property lit reader, source requires NeoForge>=21.1.251, cannot claim target loaded PASS |
| F20-46 | Modded tagged alternate heat source not subclass of CampfireBlock | Config HeatSourceRule matching by block ID/tag/properties, opt-in local semantics only |
| F20-47 | Foreign fake Level/structure/clipboard and mod independent fire engine | Physical coords untouched without explicit declared world space |
| F20-48 | Actual NeoForge patched bytecode and application Mixins verified | Full declaring owner and transformed target signatures checked separately |
| F20-49 | Actual dedicated server/client/modded runtime acceptance | Mod-loaded real outcomes and controlled frame, no inferred PASS from Java source |
| F20-50 | Fire spread and campfire BE performance under 100+ nodes | Bounded caches/memory/ticks, no repeated dual vanilla+custom spread |

## Correct phase ownership, explicit no-global-rewrite contract

- Phase2: real BlockPlaceContext target, fire five-direction state vs campfire 4-way facing, actual local supporting face and neighbor provenance, original special flint-and-steel/fire-charge/dispenser writers.
- Phase3: flame geometry and client particles, signal smoke, four food-slot transforms, smoke/debris and sound, canonical frame/view consistency.
- Phase5: actual physical WATERLOGGED fluid and scheduled tick, not fake fluid in campfire.
- Phase6: fire tick/game rule/flammability/block-update and potential signaling interactions; preserve base behavior as actual.
- Phase7: entityInside/fire damage, cooking output item entity drops and collision.
- Phase8/9: per-feature/structure/portal real 3D world source, rainy biome and WORLD-Y spread-volume intentional space, fake Level.
- Phase10: third-party independently authored heat consumer, alternate tagged fuel, provided cap/events; unknown mod stays PHYSICAL unless opted in.
- Performance: no global per-tick cache, no duplicated vanilla+Planet spread, bounded fire-volume traversal, BE slots and world-chunk consistency.

**50 proposed tests, ZERO executed.** No Java/Mixin/build/CI/client/server changes or gameplay acceptance during this stage; 241×3 original ASM/Planet/gameplay fields remain REVIEW_PENDING.
