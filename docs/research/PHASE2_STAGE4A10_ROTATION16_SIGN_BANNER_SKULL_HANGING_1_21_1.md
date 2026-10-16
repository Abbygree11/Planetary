# Planetary — 2.4A-10: full four-owner ROTATION_16 player placement + support

Date: 2026-10-17. Branch: `2.0`. Target: Minecraft 1.21.1, NeoForge 21.1.215, Java 21.

**Scope contract: one whole distinct ROTATION_16 placement family**, 4 original declaring methods and inherited registered sibling variants. Exact loaded NeoForge target owner/compiled Mixin checks + complete six-chart pure yaw test are separate from applied client, loaded mods and Phase 3 BER. Never confuse a state-reference station with naturally placed items.

## 1/4 — original vanilla owner callsites (selected source + target ABI)

Pinned Mojang-mapped comparative source at `hackersense/OptiFine-Source@b77c5c6995874f6cf2755bc5234428906b337b75` (may contain OptiFine modifications, actual classfile ABI checked in Gradle target tests).

| Declaring owner | Author and zero | Inherited families and independent behavior |
| --- | --- | --- |
| [StandingSignBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/StandingSignBlock.java) | `RotationSegment.convertToSegment(context.getRotation()+180)` (NORTH_0 in target local basis) | Standing wood/bamboo/cherry/nether sign wood varieties; WATERLOGGED from physical target fluid; `canSurvive` original physical `pos.below().isSolid`, `updateShape` physical DOWN. Item `SignItem extends StandingAndWallBlockItem` chooses standing vs WallSignBlock independently. |
| [BannerBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/BannerBlock.java) | Same `yaw+180` zero (NORTH_0) | All 16 colored banners, BannerItem standing vs WallBannerBlock. Patterns/colors/BE not touched; `canSurvive` = original block below `isSolid`. |
| [SkullBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/SkullBlock.java) | `RotationSegment.convertToSegment(context.getRotation())` (SOUTH_0 in actor-heading mapping) | Different heads incl inherited PlayerHeadBlock, powered from AbstractSkullBlock/physical redstone; WallSkullBlock is independent 4-way `FACING`, not ROTATION_16. |
| [CeilingHangingSignBlock](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/level/block/CeilingHangingSignBlock.java) | Full yaw+180 when `ATTACHED=true`; otherwise `RotationSegment.convertToSegment(Direction.fromYRot(yaw).getOpposite())` snapping to one of 4 axes | All ceiling hanging signs, inherited SignItem/HangingSignItem variant selection, local **UP** sturdy ceiling, 4-way `ATTACHED=false` support alignment with adjacent hanging signs, Shift override, waterlogging and attached chain. WallHangingSignBlock 4-way variant remains independent. |

[Original `StandingAndWallBlockItem`](https://github.com/hackersense/OptiFine-Source/blob/b77c5c6995874f6cf2755bc5234428906b337b75/1.21.1/net/minecraft/world/item/StandingAndWallBlockItem.java) chooses original standing or wall block based on plural nearest-direction order (including support/survival and `isUnobstructed`), then `BlockItem` inserts blockstate components/BE data. Existing Planet `StandingAndWallBlockItemMixin` opts the semantic direction list into the target chart. We **do not replace this item writer** or the original callback ordering. Original direct structure/command/worldgen `setBlock`, `BlockState.rotate/mirror` and NeoForge world-aware contextual state transforms do not dispatch into player placement and remain separate Phase 2.4D/9 owners.

## 2/4 — implementation: deep safe semantic point

- `PlanetRotation16Placement` accepts explicit actor and target `PlanetGravityFrame`, computes a local body yaw heading (0 SOUTH, 90 WEST), transforms it through physical world space to target canonical EAST/UP/SOUTH basis, rejects vertical/no-tangent/unregistered sources. Shared `PlanetOrientationProperties.targetTangentRotation16` handles the **legal 0..15** property domain with owner-specific SOUTH_0/NORTH_0 baseline. The standing sign and Banner algorithms are sibling owners of one author rule; Skull uses the opposite baseline; Ceiling uses snapped quadrants when rigid `ATTACHED=false`.
- Original RETURN callbacks in four separately declaring owner Mixins rewrite **only ROTATION_16** and explicit ceiling `ATTACHED` to target canonical values. Retain WATERLOGGED, POWERED, registry state identity, sign double-sided text/wax, item post-components, banner pattern NBT, Skull BE/skin, block-fluid use and NeoForge callbacks.
- `StandingSignBlockLocalSupportMixin` and `BannerBlockLocalSupportMixin` narrow-redirect each concrete original `pos.below()` in `canSurvive` to the **local DOWN** support via seam-aware `PlanetBlockSupportRuntime`, retaining original `isSolid` predicate. RETURN updateShape handles physical neighbor removal without globally remapping directions; vanilla `super.updateShape` keeps original water scheduling.
- `CeilingHangingSignBlockLocalSupportMixin` adapts original `canSurvive` to local UP sturdy support of `SupportType.CENTER` and physical neighbor removal. Ceiling original `ATTACHED` computation is retested using correct local UP neighbor/physical position, `Block.isFaceFull` on support's local face, Shift and hanging-sign chain axis policy. No global physical `Level.getBlockState` replacement.

Deeper generic `Level`/Direction/RotationSegment hook would break physical mod machinery, worldgen, fake/foreign levels and structure processors. Shallower item-ID switches would miss inherited 16-color banners, all wood signs and mod subclass reuse.

## 3/4 — vanilla/NeoForge/mod bypass classification

| Boundary | Scope |
| --- | --- |
| SHARED_SAFE | Opt-in typed tangent angle; physical actor→target transport, floor/ceiling local support, original state property preservation. |
| OPT_IN_ALGORITHM | Four declaring owner RETURN adapters, support algorithms only where author explicitly chooses local floor/ceiling. Mixins may affect subclasses **inheriting** those methods, but overridden subclasses need separate evidence. |
| MOD_INDEPENDENT | Exact real pinned Create comparator `Creators-of-Create/Create@7ff2760` `MechanicalCrafterBlock.POINTING`, `RotatedPillarKineticBlock.AXIS`, tracks and contraption transformation are **not** ROTATION_16; modded wall signs or custom signs may override item/wall author; actual loaded compatible mod JAR NOT TESTED. |
| PHYSICAL_DO_NOT_TOUCH | Wall `FACING` authors; physical redstone, light/AO/model/BER, beam/Skull geometry, actual coordinates, BE serialization, StructureTemplate and NeoForge contextual rotate/mirror, create-style custom property domains. |

## 4/4 — evidence gates and six-face manual plan

- `PlanetRotation16PlacementTest` enumerates all 16 segments on all six actor/target aligned charts; asserts owner-skull opposite zero, snapped ceiling quadrant and rejected vertical/NaN/foreign sources. Existing `PlanetOrientationPropertiesTest` tests typed 16-segment and 6 faces/12 edges/8 corners through canonical target frames.
- `Phase24A10Rotation16OwnerAsmContractTest` verifies four original declaring target methods and compiled/registered adapters; bytecode/CI not equal actual woven client. NeoForge 21.1.215 exact runtime CI must complete **SUCCESS** after the final V8 source/test commit before any game run.
- V8: retain all 36 V7 and V6 stations and add six interstitial cases in two remaining rows, within original 41×33 pad/face. New marker PURPUR_BLOCK migrates only marker-owned V7 CRYING_OBSIDIAN and earlier markers. CYAN = forcibly set reference only; LIME = player BlockItem placement.
- In real client with head-turns: standing oak/spruce signs both orientations; 16 white banners incl patterned material; skull/player head; ceiling oak/cherry ATTACHED/rigid cases (support roof + sneak/chain); compare F3 rotation/attached, physically accurate floor/ceiling support, actual waterlogged, correct drop on local support removal, sign text GUI/BE, banner/skull BER. On all six face interiors plus separately 12 edges/8 corners; ordinary non-Planet world unaffected. For mod claims require actually loaded target-compatible add-on. Client Mixin apply/startup separately from compiled ASM.

**Current status:** code and tests committed, **CI outcome must be verified live**. All physical client, worldgen/structure, mod extension and shader checks PENDING. Do not mark full phase closed.
