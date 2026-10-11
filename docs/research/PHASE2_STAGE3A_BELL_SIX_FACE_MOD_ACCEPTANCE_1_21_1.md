# Stage3A-32.3 — Bell family acceptance matrix (NOT RUN)

MC1.21.1 / NeoForge21.1.215. **Every case below: PLANNED / NOT RUN**, no fixture proves source reviewed = runtime accepted. Distinguish native client/server/Mixin, vanilla no-Planet, mod binary load, 6 world faces, 12 seams, 8 corners. Actual loaded mod checks are conditional on matching mod versions.

| ID | Planned deterministic/loaded scenario | Required behavior and owner |
| --- | --- | --- |
| BELL32-01..06 | Natural player BlockItem placement on each of six gravity faces | Local FLOOR orientation, chosen target BlockPos and canSurvive support; valid four-state FACING, no world-Y clamp |
| BELL32-07..12 | Natural wall/ceiling attachment on each face (wall on at least two local tangents) | Correct local support mapping; CLOCKWISE directions transported; no invalid FACING state |
| BELL32-13..24 | One actual player placement/cross-boundary support case at each 12 edge seam | Source and target chart support correctly transported; no off-by-one/half-cell placement; PHYSICAL neighbor always same true world-cell |
| BELL32-25..32 | Eight 3-face corner traversal samples using a real support block | Deterministic original source→target chart; no unexpected wall-axis remap |
| BELL32-33 | SIDE support removed from SINGLE_WALL | Bell removed as in vanilla for correct support side; no phantom BE |
| BELL32-34 | DOUBLE_WALL support lost on one side then re-added | State transitions DOUBLE→SINGLE→DOUBLE; FACING change matches physical survivor and local intended facing |
| BELL32-35 | Above support removed from CEILING, below from FLOOR | Each correctly breaks; test six gravity faces and no-Planet baseline |
| BELL32-36 | Player side hit, upper/lower body hit, physical top/bottom, rotated local top/bottom | Only valid localized hit causes ring; height threshold 0.8124F preserved within correctly transformed hit frame |
| BELL32-37 | Projectile and item-free interaction in same four attachment variants | One event and correct direction; no double sound, stats, or phantom client ring |
| BELL32-38 | Redstone rising/falling and explosion | Correct one-shot semantics and POWERED state; Phase7A signal-side gate separate |
| BELL32-39 | `Level.blockEvent(...,1,Direction.get3DDataValue())` client/server sync | Physical wire direction decode intact and canonical visual swing in Phase3, no packet rewrite |
| BELL32-40 | Vanilla BellRenderer static support model and dynamic bell in six faces | Body swings along local tangent, does not rotate in wrong world axis, static support model and culling valid |
| BELL32-41 | BellBlockEntity raider hearing/glow, resonance, particles | Raider timing/range (server) unchanged, local client particle emitter requires Phase3; no XZ-only offset leak |
| BELL32-42 | Non-Planet vanilla control plus repeated tick/performance | Exact original placement, signals, 50-tick shake, search caching; no frame allocs when Planet disabled |
| BELL32-43 | Create PeculiarBell + HauntedBell on each face | Inherited Bell survive, independent override redstone/ring, correct conversion above local soul fire underfoot; preserved FACING/ATTACHMENT/POWERED |
| BELL32-44 | Create independent DeskBell FACING/POWERED/WATERLOGGED | Six-way placement, independent shape and signal semantics, powered pulse/reset and fluid scheduling |
| BELL32-45 | Create AbstractBellBlockEntity + BellRenderer, simulated redraw/client packet | Direction-correct 74-tick swing and player-triggered animation without vanilla BellBlockEntity cast |
| BELL32-46 | Create contraption moving Haunted Bell | Foreign/virtual Level, movement position event, no Planet chart accidentally applied to contraption internal coordinates |
| BELL32-47 | Village/worldgen structure-set bell, commands, BlockItem BLOCK_STATE components | Alternate writers and legacy persisted orientation state inspected; no assumptions of player-only placement |
| BELL32-48 | NeoForge patched bytecode and exact Mixin dispatch plus client/server with installed mods | Prove source/Bell/AbstractBellBlock override dispatch, transformer applies; no ASM claim before execution |

Full pass requires Phase2 + Phase3 + Phase7A + Phase5 + Phase7 and mod handoffs as relevant, NOT merely passing this note. Test all applicable materials/variants including future mod registrations without hardcoding individual IDs. Evidence status: 48 case labels represent planned groups; **0 executed**.
