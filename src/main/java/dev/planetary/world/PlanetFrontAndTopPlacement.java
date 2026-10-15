package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * Target-canonical FRONT+TOP author algorithms. Two independent vanilla
 * placement writers (CrafterBlock, JigsawBlock) share the legal 12-state
 * domain, but not their rules. Neither raw world directions nor the original
 * BlockState's other properties are changed.
 */
public final class PlanetFrontAndTopPlacement {
    private PlanetFrontAndTopPlacement() {
    }

    /**
     * Crafter original author uses nearest looking direction OPPOSITE.
     * The front is vertical when the player looks vertically; its top must
     * then be a tangent horizontal heading (reversed for front DOWN).
     */
    public static Optional<FrontAndTop> crafterOrientation(
            Direction localFront, Optional<Direction> localHorizontalHeading
    ) {
        Objects.requireNonNull(localFront, "localFront");
        Objects.requireNonNull(localHorizontalHeading, "localHorizontalHeading");
        Direction top;
        if (localFront == Direction.DOWN) {
            if (localHorizontalHeading.isEmpty()) return Optional.empty();
            top = localHorizontalHeading.get().getOpposite();
        } else if (localFront == Direction.UP) {
            if (localHorizontalHeading.isEmpty()) return Optional.empty();
            top = localHorizontalHeading.get();
        } else {
            top = Direction.UP;
        }
        return Optional.ofNullable(FrontAndTop.fromFrontAndTop(localFront, top));
    }

    /** Jigsaw original author uses the clicked normal as FRONT. */
    public static Optional<FrontAndTop> jigsawOrientation(
            Direction localClickedNormal, Optional<Direction> localHorizontalHeading
    ) {
        Objects.requireNonNull(localClickedNormal, "localClickedNormal");
        Objects.requireNonNull(localHorizontalHeading, "localHorizontalHeading");
        Direction top;
        if (localClickedNormal.getAxis().isVertical()) {
            if (localHorizontalHeading.isEmpty()) return Optional.empty();
            top = localHorizontalHeading.get().getOpposite();
        } else {
            top = Direction.UP;
        }
        return Optional.ofNullable(
                FrontAndTop.fromFrontAndTop(localClickedNormal, top));
    }

    /** Preserve original NeoForge state and TRIGGERED/CRAFTING, only ORIENTATION. */
    public static Optional<BlockState> afterCrafterPlacement(
            BlockPlaceContext context, BlockState original
    ) {
        Objects.requireNonNull(context, "context");
        if (original == null || !original.hasProperty(BlockStateProperties.ORIENTATION)) {
            return Optional.empty();
        }

        Optional<Direction> front = Optional.empty();
        Optional<Direction> heading = Optional.empty();
        if (context.getPlayer() != null) {
            front = PlanetFrameApi.localNearestLookingDirections(context)
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0).getOpposite());
            heading = PlanetFrameApi.localHorizontalDirection(context);
        } else if (context instanceof DirectionalPlaceContext) {
            // Synthetic contexts have EXPLICIT physical directions, not
            // a player's body-local yaw/pitch; resolve at the target chart.
            var target = PlanetBlockRuntime.stateFrameAt(
                    context.getLevel(), context.getClickedPos());
            if (target.isPresent()) {
                front = Optional.of(target.get().worldToLocal(
                        context.getNearestLookingDirection().getOpposite()));
                Direction candidate = target.get().worldToLocal(
                        context.getHorizontalDirection());
                if (candidate.getAxis().isHorizontal()) {
                    heading = Optional.of(candidate);
                }
            }
        }
        final Optional<Direction> resolvedHeading = heading;
        return front.flatMap(dir -> crafterOrientation(dir, resolvedHeading))
                .map(orientation -> original.setValue(
                        BlockStateProperties.ORIENTATION, orientation));
    }

    /** Preserve Jigsaw BlockEntity/structure metadata and original state. */
    public static Optional<BlockState> afterJigsawPlacement(
            BlockPlaceContext context, BlockState original
    ) {
        Objects.requireNonNull(context, "context");
        if (original == null || !original.hasProperty(BlockStateProperties.ORIENTATION)) {
            return Optional.empty();
        }
        return PlanetFrameApi.placementFrame(context)
                .flatMap(frame -> {
                    Optional<Direction> horizontal = Optional.empty();
                    if (context.getPlayer() != null) {
                        horizontal = PlanetFrameApi.localHorizontalDirection(context);
                    } else if (context instanceof DirectionalPlaceContext) {
                        Direction candidate = PlanetBlockRuntime.stateFrameAt(
                                context.getLevel(), context.getClickedPos())
                                .map(target -> target.worldToLocal(
                                        context.getHorizontalDirection()))
                                .orElse(null);
                        if (candidate != null && candidate.getAxis().isHorizontal()) {
                            horizontal = Optional.of(candidate);
                        }
                    }
                    return jigsawOrientation(frame.localClickedFace(), horizontal);
                })
                .map(orientation -> original.setValue(
                        BlockStateProperties.ORIENTATION, orientation));
    }
}
