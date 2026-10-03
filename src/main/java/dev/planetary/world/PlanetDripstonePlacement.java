package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

import java.util.Optional;

/**
 * Local-frame placement/survival/thickness semantics for pointed dripstone.
 */
public final class PlanetDripstonePlacement {
    private PlanetDripstonePlacement() {
    }

    public static Optional<Direction> initialTipDirection(
            BlockPlaceContext context
    ) {
        return PlanetPlacementRuntime
                .localVerticalDirection(context)
                .map(Direction::getOpposite)
                .flatMap(candidate ->
                        calculateTipDirection(
                                context.getLevel(),
                                context.getClickedPos(),
                                candidate
                        )
                );
    }

    public static Optional<Direction> calculateTipDirection(
            Level level,
            BlockPos pos,
            Direction preferred
    ) {
        if (isValidPlacement(
                level,
                pos,
                preferred
        )) {
            return Optional.of(preferred);
        }

        Direction opposite =
                preferred.getOpposite();

        return isValidPlacement(
                level,
                pos,
                opposite
        )
                ? Optional.of(opposite)
                : Optional.empty();
    }

    public static boolean isValidPlacement(
            Level level,
            BlockPos pos,
            Direction tipDirection
    ) {
        Optional<PlanetBlockNeighborQuery> supportOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        tipDirection.getOpposite()
                );

        if (supportOptional.isEmpty()) {
            return false;
        }

        PlanetBlockNeighborQuery support =
                supportOptional.get();
        BlockState supportState =
                level.getBlockState(
                        support.targetPos()
                );

        if (supportState.isFaceSturdy(
                level,
                support.targetPos(),
                support.targetLocalSideTowardSource()
        )) {
            return true;
        }

        return isPointedWithSourceDirection(
                supportState,
                support,
                tipDirection
        );
    }

    public static DripstoneThickness calculateThickness(
            Level level,
            BlockPos pos,
            Direction tipDirection,
            boolean mergeTips
    ) {
        Optional<PlanetBlockNeighborQuery> forwardOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        tipDirection
                );

        if (forwardOptional.isEmpty()) {
            return DripstoneThickness.TIP;
        }

        PlanetBlockNeighborQuery forward =
                forwardOptional.get();
        BlockState forwardState =
                level.getBlockState(
                        forward.targetPos()
                );

        if (isPointedWithSourceDirection(
                forwardState,
                forward,
                tipDirection.getOpposite()
        )) {
            return !mergeTips
                    && forwardState.getValue(
                    PointedDripstoneBlock.THICKNESS
            ) != DripstoneThickness.TIP_MERGE
                    ? DripstoneThickness.TIP
                    : DripstoneThickness.TIP_MERGE;
        }

        if (!isPointedWithSourceDirection(
                forwardState,
                forward,
                tipDirection
        )) {
            return DripstoneThickness.TIP;
        }

        DripstoneThickness forwardThickness =
                forwardState.getValue(
                        PointedDripstoneBlock.THICKNESS
                );

        if (forwardThickness == DripstoneThickness.TIP
                || forwardThickness
                == DripstoneThickness.TIP_MERGE) {
            return DripstoneThickness.FRUSTUM;
        }

        Optional<PlanetBlockNeighborQuery> backwardOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        tipDirection.getOpposite()
                );

        if (backwardOptional.isEmpty()) {
            return DripstoneThickness.BASE;
        }

        PlanetBlockNeighborQuery backward =
                backwardOptional.get();
        BlockState backwardState =
                level.getBlockState(
                        backward.targetPos()
                );

        return isPointedWithSourceDirection(
                backwardState,
                backward,
                tipDirection
        )
                ? DripstoneThickness.MIDDLE
                : DripstoneThickness.BASE;
    }

    private static boolean isPointedWithSourceDirection(
            BlockState state,
            PlanetBlockNeighborQuery query,
            Direction sourceDirection
    ) {
        if (!state.is(
                Blocks.POINTED_DRIPSTONE
        )) {
            return false;
        }

        Direction targetDirection =
                PlanetPlacementRuntime.reframeToTarget(
                        query,
                        sourceDirection
                );

        return state.getValue(
                PointedDripstoneBlock.TIP_DIRECTION
        ) == targetDirection;
    }
}
