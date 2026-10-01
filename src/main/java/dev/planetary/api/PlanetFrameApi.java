package dev.planetary.api;

import dev.planetary.topology.PlanetFace;
import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

/**
 * Stable integration surface for Planet-local block directions.
 */
public final class PlanetFrameApi {
    private PlanetFrameApi() {
    }

    public static Optional<PlanetFace> canonicalBlockFace(
            Level level,
            BlockPos pos
    ) {
        return PlanetBlockRuntime.stateFrameAt(level, pos)
                .map(frame -> frame.face());
    }

    public static Optional<Direction> localSideToPhysical(
            Level level,
            BlockPos pos,
            Direction localSide
    ) {
        return PlanetBlockRuntime.localSideToPhysical(
                level,
                pos,
                localSide
        );
    }

    public static Optional<Direction> physicalSideToLocal(
            Level level,
            BlockPos pos,
            Direction physicalSide
    ) {
        return PlanetBlockRuntime.physicalSideToLocal(
                level,
                pos,
                physicalSide
        );
    }

    public static Optional<BlockNeighbor> localNeighbor(
            Level level,
            BlockPos sourcePos,
            Direction sourceLocalDirection
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(sourceLocalDirection, "sourceLocalDirection");

        return PlanetBlockRuntime.neighbor(
                level,
                sourcePos,
                sourceLocalDirection
        ).map(PlanetFrameApi::toApiNeighbor);
    }

    private static BlockNeighbor toApiNeighbor(
            PlanetBlockNeighborQuery query
    ) {
        return new BlockNeighbor(
                query.sourcePos(),
                query.sourceStateFrame().face(),
                query.sourceLocalDirection(),
                query.physicalDirection(),
                query.targetPos(),
                query.targetStateFrame().face(),
                query.targetLocalSideTowardSource(),
                query.crossedTraversalBoundary()
        );
    }

    public record BlockNeighbor(
            BlockPos sourcePos,
            PlanetFace sourceFace,
            Direction sourceLocalDirection,
            Direction physicalDirection,
            BlockPos targetPos,
            PlanetFace targetFace,
            Direction targetLocalSideTowardSource,
            boolean crossedGravityBoundary
    ) {
        public BlockNeighbor {
            Objects.requireNonNull(sourcePos, "sourcePos");
            Objects.requireNonNull(sourceFace, "sourceFace");
            Objects.requireNonNull(sourceLocalDirection, "sourceLocalDirection");
            Objects.requireNonNull(physicalDirection, "physicalDirection");
            Objects.requireNonNull(targetPos, "targetPos");
            Objects.requireNonNull(targetFace, "targetFace");
            Objects.requireNonNull(
                    targetLocalSideTowardSource,
                    "targetLocalSideTowardSource"
            );
        }
    }
}
