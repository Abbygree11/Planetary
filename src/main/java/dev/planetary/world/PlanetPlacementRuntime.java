package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared runtime helpers for local-frame placement families.
 */
public final class PlanetPlacementRuntime {
    private PlanetPlacementRuntime() {
    }

    public static Optional<BlockPos> neighborPos(
            Level level,
            BlockPos source,
            Direction localDirection
    ) {
        return PlanetBlockRuntime.neighbor(
                level,
                source,
                localDirection
        ).map(PlanetBlockNeighborQuery::targetPos);
    }

    public static Direction reframeToTarget(
            PlanetBlockNeighborQuery query,
            Direction sourceLocalDirection
    ) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(
                sourceLocalDirection,
                "sourceLocalDirection"
        );

        Direction physical =
                query.sourceStateFrame()
                        .localToWorld(
                                sourceLocalDirection
                        );

        return query.targetStateFrame()
                .worldToLocal(
                        physical
                );
    }

    public static Optional<Direction> localVerticalDirection(
            BlockPlaceContext context
    ) {
        return PlanetFrameApi.localNearestLookingDirections(
                context
        ).flatMap(directions ->
                directions.stream()
                        .filter(direction ->
                                direction.getAxis()
                                        == Direction.Axis.Y
                        )
                        .findFirst()
        );
    }

    public static Optional<DoorHingeSide> doorHinge(
            DoorBlock door,
            BlockPlaceContext context,
            Direction localFacing
    ) {
        Objects.requireNonNull(door, "door");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(localFacing, "localFacing");

        Level level =
                context.getLevel();
        BlockPos lower =
                context.getClickedPos();

        Optional<BlockPos> upperOptional =
                neighborPos(
                        level,
                        lower,
                        Direction.UP
                );
        Optional<PlanetFrameApi.PlacementFrame> placementOptional =
                PlanetFrameApi.placementFrame(
                        context
                );

        if (upperOptional.isEmpty()
                || placementOptional.isEmpty()) {
            return Optional.empty();
        }

        BlockPos upper =
                upperOptional.get();
        Direction left =
                localFacing.getCounterClockWise();
        Direction right =
                localFacing.getClockWise();

        PlanetBlockNeighborQuery upperQuery =
                PlanetBlockRuntime.neighbor(
                        level,
                        lower,
                        Direction.UP
                ).orElseThrow();
        Direction upperLeftDirection =
                reframeToTarget(
                        upperQuery,
                        left
                );
        Direction upperRightDirection =
                reframeToTarget(
                        upperQuery,
                        right
                );

        BlockPos lowerLeft =
                neighborPos(
                        level,
                        lower,
                        left
                ).orElse(null);
        BlockPos upperLeft =
                neighborPos(
                        level,
                        upper,
                        upperLeftDirection
                ).orElse(null);
        BlockPos lowerRight =
                neighborPos(
                        level,
                        lower,
                        right
                ).orElse(null);
        BlockPos upperRight =
                neighborPos(
                        level,
                        upper,
                        upperRightDirection
                ).orElse(null);

        if (lowerLeft == null
                || upperLeft == null
                || lowerRight == null
                || upperRight == null) {
            return Optional.empty();
        }

        BlockState lowerLeftState =
                level.getBlockState(lowerLeft);
        BlockState upperLeftState =
                level.getBlockState(upperLeft);
        BlockState lowerRightState =
                level.getBlockState(lowerRight);
        BlockState upperRightState =
                level.getBlockState(upperRight);

        int score =
                (lowerLeftState.isCollisionShapeFullBlock(
                        level,
                        lowerLeft
                ) ? -1 : 0)
                        + (upperLeftState.isCollisionShapeFullBlock(
                        level,
                        upperLeft
                ) ? -1 : 0)
                        + (lowerRightState.isCollisionShapeFullBlock(
                        level,
                        lowerRight
                ) ? 1 : 0)
                        + (upperRightState.isCollisionShapeFullBlock(
                        level,
                        upperRight
                ) ? 1 : 0);

        boolean lowerLeftDoor =
                lowerLeftState.getBlock()
                        instanceof DoorBlock
                        && lowerLeftState.getValue(
                        DoorBlock.HALF
                ) == DoubleBlockHalf.LOWER;
        boolean lowerRightDoor =
                lowerRightState.getBlock()
                        instanceof DoorBlock
                        && lowerRightState.getValue(
                        DoorBlock.HALF
                ) == DoubleBlockHalf.LOWER;

        if ((!lowerLeftDoor || lowerRightDoor)
                && score <= 0) {
            if ((!lowerRightDoor || lowerLeftDoor)
                    && score >= 0) {
                Vec3 localHit =
                        placementOptional.get()
                                .localHitOffset();
                int stepX =
                        localFacing.getStepX();
                int stepZ =
                        localFacing.getStepZ();

                boolean leftHinge =
                        (stepX >= 0 || !(localHit.z < 0.5D))
                                && (stepX <= 0 || !(localHit.z > 0.5D))
                                && (stepZ >= 0 || !(localHit.x > 0.5D))
                                && (stepZ <= 0 || !(localHit.x < 0.5D));

                return Optional.of(
                        leftHinge
                                ? DoorHingeSide.LEFT
                                : DoorHingeSide.RIGHT
                );
            }

            return Optional.of(
                    DoorHingeSide.LEFT
            );
        }

        return Optional.of(
                DoorHingeSide.RIGHT
        );
    }

    public static Optional<AABB> localBoxToWorld(
            Level level,
            BlockPos pos,
            AABB localBox
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(localBox, "localBox");

        return PlanetBlockRuntime.stateFrameAt(
                level,
                pos
        ).map(frame ->
                PlanetVoxelShapeRotation.localToWorld(
                        Shapes.create(localBox),
                        frame.face()
                ).bounds().move(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()
                )
        );
    }
}
