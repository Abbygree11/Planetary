# Stage 2.3B-3 future acceptance — non-item creation paths

**2026-10-12: design only. ALL NOT RUN.** Run with actual natural dispenser/falling/worldgen/structure command/BE actions; never count forced BlockState setup as real author execution. Every applicable case checks physical BlockPos, stored state legal values, actual writer, BE/fluids/neighbor updates/events, client/server+restart. Six Planet face interiors + 12 face-pair edges + eight corners, plus non-Planet and foreign/schematic world regression. Cross-phase mechanisms retain separate owner/status.

| ID | Author | Scenario | Status |
| --- | --- | --- | --- |
| B3-01 | D | shulker dispenser six faces | NOT RUN |
| B3-02 | D | shulker synthetic orientation 12 seams and 8 corners | NOT RUN |
| B3-03 | D | wither skull direct block placement | NOT RUN |
| B3-04 | D | pumpkin golem via dispenser | NOT RUN |
| B3-05 | D | bonemeal dispenser and NeoForge cancellation | NOT RUN |
| B3-06 | D | honeycomb via dispenser state swap | NOT RUN |
| B3-07 | D | firestart via dispenser ItemAbility | NOT RUN |
| B3-08 | D | filled bucket stack-aware emptyContents | NOT RUN |
| B3-09 | D | empty bucket pickup | NOT RUN |
| B3-10 | D | solid powder snow bucket through dispenser | NOT RUN |
| B3-11 | D | water potion to mud | NOT RUN |
| B3-12 | D | dispenser handles unsupported item by ejection | NOT RUN |
| B3-13 | D | Dropper ejection vs inventory transfer | NOT RUN |
| B3-14 | F | sand source->entity->landing on six faces | NOT RUN |
| B3-15 | F | fall across seam and adjacent corner | NOT RUN |
| B3-16 | F | landing replacement with DirectionalPlaceContext no player | NOT RUN |
| B3-17 | F | fall atop half support | NOT RUN |
| B3-18 | F | fall on moving piston obstruction | NOT RUN |
| B3-19 | F | concrete powder hydrated with canBeHydrated | NOT RUN |
| B3-20 | F | fluid source preservation with waterlogged state | NOT RUN |
| B3-21 | F | falling BE NBT restored via loadWithComponents | NOT RUN |
| B3-22 | F | drop failure and onBrokenAfterFall | NOT RUN |
| B3-23 | F | non-Planet default fall regression | NOT RUN |
| B3-24 | S | setblock literal FACING/AXIS/rotation values | NOT RUN |
| B3-25 | S | fill multiple cells including faces | NOT RUN |
| B3-26 | S | clone source properties and BE | NOT RUN |
| B3-27 | S | template original mirror and rotation +Y baseline | NOT RUN |
| B3-28 | S | template transformed onto side face (opt-in) | NOT RUN |
| B3-29 | S | template across 12 edges and eight corners | NOT RUN |
| B3-30 | S | StructureProcessor cancels or replaces block | NOT RUN |
| B3-31 | S | NeoForge template-aware processor invocation | NOT RUN |
| B3-32 | S | template BE NBT/loot and component state | NOT RUN |
| B3-33 | S | template waterlogged state/fluids | NOT RUN |
| B3-34 | S | template entities and updateShape | NOT RUN |
| B3-35 | S | StructurePiece direct placement | NOT RUN |
| B3-36 | S | JigsawBlockEntity connector position | NOT RUN |
| B3-37 | S | worldgen tree/chorus/dripstone/special features | NOT RUN |
| B3-38 | S | WorldGenLevel/terrain sampling vs real chunks | NOT RUN |
| B3-39 | M | FlowerPotBlock.useItemOn and useWithoutItem | NOT RUN |
| B3-40 | M | CandleCakeBlock.useItemOn composite | NOT RUN |
| B3-41 | M | randomTick ChangeOverTimeBlock and BE tic | NOT RUN |
| B3-42 | M | sculk catalyst/Spreader growth callback | NOT RUN |
| B3-43 | M | Create virtual SchematicLevel template preview | NOT RUN |
| B3-44 | M | Create Schematicannon world placement | NOT RUN |
| B3-45 | M | Create ContraptionWorld virtual map | NOT RUN |
| B3-46 | M | AE2 swapRegions and BE state | NOT RUN |
| B3-47 | M | AE2 from Planet to non-Planet target | NOT RUN |
| B3-48 | M | third party independent writer unsupported default | NOT RUN |

D = dispenser/Dropper; F = FallingBlockEntity; S = structure/commands/worldgen; M = block interaction/BE/mod.

**Mandatory gates:** comparative source != actual NeoForge21.1.215 patched ASM != implemented Planet code != green Gradle/CI != real gameplay != loaded mod proof. Create pinned source requires NeoForge21.1.219 (Planet targets21.1.215); AE2 source min 21.1.169 technically includes target but tested JAR compatibility remains UNKNOWN. Unknown mod calls retain original physical behavior unless explicit safe semantic opt-in. No PASS claims.
