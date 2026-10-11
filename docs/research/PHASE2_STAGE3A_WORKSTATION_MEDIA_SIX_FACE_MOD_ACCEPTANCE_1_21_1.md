# Stage3A-25.3 — 68 future **NOT RUN** vanilla/multiface/modded workstation, music, redstone, GUI, sound tests

**2026-10-11 · ALL WM25-01…WM25-68 FUTURE NOT RUN.** This packet **does NOT** compile Java/Mixin, run Minecraft client/server/dedicated server or true NeoForge21.1.215 patched bytecode compatibility/injector tests, load actual mod jars, or mark integration PASS. [Exact seven original owners/source/pinned mod inspections](PHASE2_STAGE3A_WORKSTATION_MEDIA_ORIGINAL_BE_ITEM_MOD_1_21_1.md), [physical immutable 129-class four-stream audit](PHASE2_STAGE3A_WORKSTATION_MEDIA_ORIGINAL_129_CLASS_RECONCILIATION_1_21_1.md).

Each future fixture record: original source class/BLOCK+ITEM ID; original BlockState legal values; actual full declaring owner and compiled NeoForge patched runtime descriptor; real Level/BlockPos/chunk/dimension/source BE, any ephemeral GUI inventory vs persistent BE; original GameEvent/blockEvent and redstone direct/weak/analog side; source+target PlanetFace/chart/tangent; actual ItemStack/XP/audio packet source; world fake/foreign flag; independent mod pinned source SHA+JAR metadata; client hit/BER/sound particle vs server authoritative state. Explicit original non-Planet baseline before asserting Planet local behavior.

| ID | Future test | Expected observable contract |
| --- | --- | --- |
| WM25-01 | Vanilla original source 7 BlockItem->Block IDs | All 7 exact registered IDs and BE/no-BE behavior unchanged |
| WM25-02 | Vanilla lectern 4-facing placement | FACING north/east/south/west only, no illegal vertical |
| WM25-03 | Lectern direct BlockItem with book BE data | HAS_BOOK sync with component and legal powered state |
| WM25-04 | Lectern insert one valid book by item click | Consumes one and stores one exact component stack in BE |
| WM25-05 | Lectern invalid item/empty-hand no book | Original click result and menu/no menu path |
| WM25-06 | Lectern page count one vs multiple pages | BE page clamp, analog output proportional and no bad zero count |
| WM25-07 | Lectern page turned triggered redstone pulse | POWERED=15 physical signal and correct 2 scheduled ticks reset |
| WM25-08 | Lectern direct signal direction UP | Original physical callback only UP, unknown mod observer physical |
| WM25-09 | Lectern updateBelow neighbour event | Real pos.below/physical redstone neighbor update emitted once |
| WM25-10 | Lectern remove book or break with BE data | One physical ItemEntity y+1/front and BE clear, no duplication |
| WM25-11 | Lectern written book command resolution | Player permission and WrittenBookItem components preserved |
| WM25-12 | Lectern book GUI across save/reload | Page, book DataComponent NBT and comparator signal stable |
| WM25-13 | Vanilla jukebox BlockState HAS_RECORD only | No fabricated facing or sound direction property |
| WM25-14 | Jukebox insert playable disc via item interaction | One item transferred to BE; HAS_RECORD true |
| WM25-15 | Jukebox placed from custom BLOCK_ENTITY_DATA.RecordItem | setPlacedBy sets legal HAS_RECORD, no additional disc |
| WM25-16 | Jukebox empty-hand eject and onRemove | PopOutTheItem physical y+1.01, at most one item |
| WM25-17 | Jukebox no-record ticker vs active record | getTicker conditional on HAS_RECORD and NBT music position |
| WM25-18 | Jukebox playing signal and comparator | True playing 15 and custom song comparator output separate |
| WM25-19 | Jukebox one-slot JUKEBOX_PLAYABLE validation | Reject invalid item/occupied; no duplicate transfer |
| WM25-20 | Jukebox unload/reload playback tick | Original time persisted and restored, dimension/audio emitter physical |
| WM25-21 | Vanilla noteblock 23 instruments and pitch 25 | Original legal instrument enum/note 0..24, no out-of-range |
| WM25-22 | Noteblock top-only instrument original above | Actual above instrument() worksAboveNoteBlock |
| WM25-23 | Noteblock below instrument provider original | Actual below instrument() and top-only fallback HARP |
| WM25-24 | Noteblock updateShape physical Y only | No recalculation for physical X/Z non-provenanced neighbor |
| WM25-25 | Noteblock redstone rising vs falling edge | One blockEvent/note, preserved powered state |
| WM25-26 | Noteblock top-occupied sound suppression | Unless worksAboveNoteBlock, only original isAir above |
| WM25-27 | Noteblock tune by right-click, play by attack | Correct cycle NOTE and player interaction semantics |
| WM25-28 | Noteblock head/skull custom sound above | Actual SkullBlockEntity at physical above, SoundEvent correct |
| WM25-29 | Noteblock hit with top-instrument item | Physical clicked UP preserves ItemTags input block placement |
| WM25-30 | Noteblock note particle pitch/position | Client glyph normal vs physical audio emitter xyz separate |
| WM25-31 | Enchanting table no-state and BE menu | Exact original BE custom name, ephemeral EnchantmentMenu inventory |
| WM25-32 | Enchanting tabletop BOOKSHELF_OFFSETS provider ring | Original 3D radius + unobstructed transmitter + providers |
| WM25-33 | Enchanting modded tagged shelf provider | Tag-based or mod stat provider honored; not vanilla only |
| WM25-34 | Enchanting menu 2 slots + XP/lapis cost | Correct seeded clues/experience, server ownership and permission |
| WM25-35 | Enchanting animated book/particle rendered | Client facing nearest player in local frame vs real BE |
| WM25-36 | Smithing table no persistent BlockEntity | Menu uses ContainerLevelAccess real block and 3 recipe input |
| WM25-37 | Smithing consumes template/base/addition once | RecipeType.SMITHING output components and stats exact |
| WM25-38 | Smithing invalid recipe and quick-move | No unearned output or dupes; result empty |
| WM25-39 | Stonecutter 4-facing original player placement | No vertical enum, physical VoxelShape and click |
| WM25-40 | Stonecutter menu 1 input and recipe list | RecipeType.STONECUTTING selection and output accounting |
| WM25-41 | Stonecutter menu close/unload while items inside | Temporary input returned, no BE persistent ghost item |
| WM25-42 | Grindstone 3 AttachFace × 4 FACING placement | All 12 legal states with distinct hit vs collision shapes |
| WM25-43 | Grindstone parent support placement via neighbor | FaceAttachedHorizontalDirectionalBlock canAttach physically true |
| WM25-44 | Grindstone canSurvive override true vs parent | Do NOT erroneously copy parent support break predicate |
| WM25-45 | Grindstone menu two-slot repair/disenchant | Components/curses retained, result crafted once |
| WM25-46 | Grindstone XP spawned at real Vec3.center | Only after output taken, exactly one award |
| WM25-47 | Vanilla no-Planet side and menu regression | All original sound/GUI/block state behavior unchanged |
| WM25-48 | All 7 classes across each 6 Planet interiors | Registered legal state and unique physical BE/BlockPos |
| WM25-49 | 12 Planet edges crossed both directions | Menu real pos and chart-aware visual tangent, no ghost BE |
| WM25-50 | 8 Planet corners from 3 approach directions | One true XYZ state/BE and edge transport consistent |
| WM25-51 | Lectern page redstone across Planet edge | REAL callback direction and powered 2 tick cycle default |
| WM25-52 | Noteblock top/bottom across Planet chart boundary | Explicit instrument algorithm opt-in, no generic above rewrite |
| WM25-53 | Enchanting bookshelf ring crosses seam | No phantom shelf; source tag and real block count actual |
| WM25-54 | Grindstone floor/ceiling/wall cross boundary | Valid FACE/FACING with correct attachment and shape |
| WM25-55 | Jukebox sound from bottom Planet face | REAL source dimension/xyz, client orientation adjusts visual only |
| WM25-56 | Jukebox physical ItemEntity ejection on six faces | No duplicate disc, collision/physics correct real side |
| WM25-57 | Apothic Enchanting pinned MC1.21.1 mod with deps | Actual binary/version neo21.1.215 installed and loaded or explicit SKIP |
| WM25-58 | Apothic FilteringShelfBlock and custom Enchanting table | Own BE inventory/stats/offset provider menu honored, not vanilla-only |
| WM25-59 | Apothic vanilla EnchantingTableBlockEntityMixin | No injection conflicts or loss of mod data attachment |
| WM25-60 | CustomDiscs 1.1.0 compiled NeoForge21.1.218 | Metadata range includes target21.1.215 but actual runtime compatibility unproven |
| WM25-61 | CustomDiscs JukeboxTicker dimension+BlockPos cache | Physical XYZ session, track offset, late joiners sync once |
| WM25-62 | CustomDiscs disc-ejected and untracked via commands | StopDiscPacket once, no residual sound/cache from world edit |
| WM25-63 | Unknown mod instrument/sound provider outside vanilla NoteBlock | Physical default; explicit opt-in only by owner and provider |
| WM25-64 | Foreign WorldGenLevel schematic commands fake Levels | No generic Level/BlockPos/Direction mutation; proper BE source |
| WM25-65 | Menus concurrent players/close/reload durability | No ephemeral input lost or duplicated, real stillValid check |
| WM25-66 | Actual NeoForge21.1.215 patched ASM method descriptors | Compile/apply Mixin and verify full owners not just source ZIP |
| WM25-67 | 200+ music/note/menus profiler and packet limits | Bounded network/caches/collisions/GC; same normal mode performance |
| WM25-68 | Client/server/real mod dedicated multi-face tests | Compare rendered particles, audio, physical hit, authoritative output |

## Mandatory no-global-patch acceptance matrix

| Domain | Safe future test criterion |
| --- | --- |
| SHARED_SAFE | Original real Level/BlockPos/Dimension/BlockEntity and physical side/event metadata retained; local chart source/target frame only typed context |
| OPT_IN_ALGORITHM | Lectern 4 FACING + page pulse, NoteBlock 23 instrument enum×25 notes×POWERED, Enchanting scanner source tags/real intervening blocks, Grindstone FACE×4 horizontal FACING and canSurvive=true; not one universal direction rewrite |
| MOD_INDEPENDENT | Apothic independent enchant shelf/table/Mixin and CustomDiscs independent JukeboxTicker real dimension+BlockPos/source network; actual mod loaded necessary before any compatibility PASS |
| PHYSICAL_DO_NOT_TOUCH | Generic BlockPos.above/below/offset, redstone physical UP signal, book/sound actual item spawn XYZ, player menu/recipes/XP, unknown mod physical provider and foreign fake Level left physically meaningful |
| CLIENT_RENDER | Lectern/book BER, enchanting animated book/shelf glyphs, note particle y+1.2, jukebox sound spatial emitter, grindstone actual collision/hit; render-only pass never server pass |

Coverage must include normal vanilla/no-Planet, **6 face interiors, all 12 edges both directions, all 8 three-face corners**, actual loaded compatible mod/dependencies, fake/schematic worlds, chunk reload, 200+ workstations/music nodes with packet/cache stress. **ALL 68 UNRUN, and ALL 241×3=723 original ASM/Planet/gameplay runtime gates REVIEW_PENDING.**
