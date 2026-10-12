package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

import java.util.Objects;
import java.util.Optional;

/**
 * Single-cell trapdoor placement state semantics for the entire inheriting
 * TrapDoorBlock family (wood, iron, waxed/unwaxed copper and mod subclasses).
 *
 * <p>Only FACING and HALF are canonical-target orientation properties. This
 * helper leaves original BlockItem dispatch, fluid state, BlockSetType,
 * POWERED/OPEN, physical neighbors and mod-authored overrides untouched.
 * Unrecognized authors remain vanilla rather than receiving guessed headings.</p>
 */
public final class PlanetTrapDoorPlacement {
    private PlanetTrapDoorPlacement() {
    }

    public record Orientation(Direction facing, Half half) {
        public Orientation {
            Objects.requireNonNull(facing, "facing");
            Objects.requireNonNull(half, "half");
            if (!facing.getAxis().isHorizontal()) {
                throw new IllegalArgumentException(
                        "TrapDoorBlock FACING must be target-local horizontal");
            }
        }
    }

    /**
     * Target-local equivalent of the two branches in vanilla
     * TrapDoorBlock#getStateForPlacement. This is also reusable for future
     * clicked-wall/actor-facing placement owners, but only on explicit opt-in.
     *
     * <p>localActorHorizontal is present only when the author genuinely
     * supplies a horizontal direction. A physical look direction that
     * transforms to target-local UP/DOWN is NOT silently projected or guessed.</p>
     */
    public static Optional<Orientation> orient(
            PlanetBlockPlacementFrame target,
            boolean replacingClickedOnBlock,
            Optional<Direction> localActorHorizontal
    ) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(localActorHorizontal, "localActorHorizontal");
        Direction clicked = target.localClickedFace();

        if (!replacingClickedOnBlock && clicked.getAxis().isHorizontal()) {
            return Optional.of(new Orientation(
                    clicked,
                    target.localHitUpperHalf() ? Half.TOP : Half.BOTTOM
            ));
        }

        return localActorHorizontal
                .filter(direction -> direction.getAxis().isHorizontal())
                .map(direction -> new Orientation(
                        direction.getOpposite(),
                        clicked == Direction.UP ? Half.BOTTOM : Half.TOP
                ));
    }

    /**
     * Thin Minecraft 1.21.1 owner adapter, called only after vanilla has
     * computed its original state (including waterlogging, redstone and mods).
     * An absent actual canonical target leaves that exact state unchanged.
     */
    public static Optional<BlockState> reframe(
            BlockPlaceContext context,
            BlockState vanilla
    ) {
        Objects.requireNonNull(context, "context");
        if (vanilla == null
                || !vanilla.hasProperty(TrapDoorBlock.FACING)
                || !vanilla.hasProperty(TrapDoorBlock.HALF)) {
            return Optional.empty();
        }

        return PlanetFrameApi.placementFrame(context)
                .flatMap(placement -> {
                    // The version adapter resolves the real current-context
                    // hit and separately the final target position.
                    var target = PlanetBlockRuntime.placementFrame(
                            context.getLevel(),
                            context.getClickedPos(),
                            placement.physicalClickedFace(),
                            placement.worldClickLocation()
                    );
                    if (target.isEmpty()) {
                        return Optional.empty();
                    }

                    Optional<Direction> authorHorizontal = Optional.empty();
                    if (context.getPlayer() != null) {
                        authorHorizontal = PlanetFrameApi.localHorizontalDirection(context);
                    } else if (context instanceof DirectionalPlaceContext) {
                        // DirectionalPlaceContext's explicit synthetic heading
                        // is PHYSICAL, unlike player body-local heading.
                        Direction physicalHeading = context.getHorizontalDirection();
                        if (physicalHeading != null) {
                            Direction local = target.get().targetStateFrame()
                                    .worldToLocal(physicalHeading);
                            if (local.getAxis().isHorizontal()) {
                                authorHorizontal = Optional.of(local);
                            }
                        }
                    }

                    return orient(target.get(), context.replacingClickedOnBlock(),
                            authorHorizontal)
                            .map(orientation -> vanilla
                                    .setValue(TrapDoorBlock.FACING, orientation.facing())
                                    .setValue(TrapDoorBlock.HALF, orientation.half()));
                });
    }
}
