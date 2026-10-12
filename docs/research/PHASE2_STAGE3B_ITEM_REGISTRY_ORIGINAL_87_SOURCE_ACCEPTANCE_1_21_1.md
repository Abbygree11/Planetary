# Stage 2.3B-4 original ITEM class census acceptance — ALL NOT RUN

**2026-10-12, SOURCE RECONCILIATION FINISHED / RUNTIME GATES OPEN.** SHA256 checks of unchanged original ZIP and generated TSV performed in this research packet; **these are artifact processing validations, not JUnit/client/server/world gameplay tests**. All below are designed acceptance intents NOT RUN.

| ID | Scenario | Evidence required | Runtime status |
| --- | --- | --- | --- |
| R01 | Original ZIP SHA256 and named files | hash original 147360-byte artifact, parse TSV via csv.DictReader | NOT RUN |
| R02 | 1,333 ITEM ID uniqueness | no duplicates; full row count exactly 1333 | NOT RUN |
| R03 | 87 concrete class grouping | all class group effective and all-declaring owners homogeneous | NOT RUN |
| R04 | 925 primary BLOCK links | each BlockItem primary placed_block resolves to original 1060 BLOCK IDs | NOT RUN |
| R05 | 408 non-BlockItems | every non-BlockItem has placed_block '-' | NOT RUN |
| R06 | 14 mismatch item→primary block | check all seeds/redstone/string/berries/powder snow target ID values | NOT RUN |
| R07 | Anonymous big_dripleaf and cauldron | Items$1 owner, correct secondary registerBlocks maps but no extra ITEM ID | NOT RUN |
| R08 | StandingAndWall 58 registered IDs | wall/standing variant candidates and registration maps preserved | NOT RUN |
| R09 | HangingSignItem extra canPlace overload | different LevelReader,BlockState,BlockPos signature not confused with BlockItem.canPlace | NOT RUN |
| R10 | DoubleHigh 27 door/plant IDs | post-placed second half, survival, frame and seam | NOT RUN |
| R11 | BedItem 16 IDs | head/foot + state + sound + BE across edge and corner | NOT RUN |
| R12 | Scaffolding item 1 ID | updatePlacementContext changes physical target, no null player assumption | NOT RUN |
| R13 | PlaceOnWater 2 IDs | frogspawn and lily pad use raycast, not Item.useOn player-click alone | NOT RUN |
| R14 | PowderSnow solid bucket 1 ID | block placement super path and dispenser emptyContents differ | NOT RUN |
| R15 | GameMasterBlockItem 5 IDs | permissions enforced even when synthetic context | NOT RUN |
| R16 | SpawnEgg 80 IDs | spawner BE mutation vs mob entity placement, no spurious BlockItem | NOT RUN |
| R17 | NonBlockItem useOn 124 direct/special | tools, EnderEye, fire, BE; correct mod extension call | NOT RUN |
| R18 | NonBlockItem use 166 special | bucket fluid, boat entity, projectiles, item-only and delayed hooks | NOT RUN |
| R19 | Late BLOCK_STATE/BE/components | legal properties, NBT rights, no user/mod explicit override erased | NOT RUN |
| R20 | Modded item subclass overrides | Farmer's Delight RiceItem/RopeItem/MushroomColonyItem owner dispatch when compatible JAR loaded | NOT RUN |
| R21 | Modded independent writers | Create virtual schematic and AE2 spatial writer / dimension preserved | NOT RUN |
| R22 | Synthetic dispenser on 6 faces | no player body, shulker/wither skull/copper wax states and effects | NOT RUN |
| R23 | 12 seams / eight corners | source and target canonical frames/physical hit normal / entity author | NOT RUN |
| R24 | Vanilla or foreign Level regression | no global BlockPos/Direction/Level.setBlock monkey patch | NOT RUN |
| R25 | Actual NeoForge21.1.215 ASM | method descriptors and bridge/anonymous modifiers, NeoForge event calls | NOT RUN |
| R26 | Client/server CI and persistence | all 87 classes/925 IDs linked test matrix and natural gameplay | NOT RUN |

**Cross-cutting constraints:** six Planet gravity faces, twelve edge face pairs, eight three-face corners, ordinary non-Planet vanilla, dimension/foreign/virtual levels; true item/hand and dispatcher source vs forced artificial BlockState fixture; actual NeoForge 21.1.215 transformed bytecode and version-compatible loaded mods. Test must assert effective Java method owner, primary/secondary item-block mappings, block state property legality, physical target, canonical frame intent, BE NBT/components, side effects, persistence, mod API and no spurious state conversions.

Source evidence original re-downloaded unchanged ZIP, registered owners 1333/1333 and 87/87, placed primary block links925/925. Exact runtime adapter/ASM/CI/gameplay is not accepted. Next 2.3C target bytecode/NeoForge patch review, Phase2.4 implementation/acceptance.
