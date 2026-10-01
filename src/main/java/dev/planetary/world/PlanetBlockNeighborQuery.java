package dev.planetary.world;

import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;
import java.util.Optional;

/**
 * Neutral canonical resolution of one local block-neighbor step.
 */
public record PlanetBlockNeighborQuery(
        PlanetBlockStateFrame sourceStateFrame,
        Direction sourceLocalDirection,
        Direction physicalDirection,
        PlanetBlockStateFrame targetStateFrame,
        Direction targetLocalSideTowardSource,
        boolean crossedTraversalBoundary
) {
    public PlanetBlockNeighborQuery {
        Objects.requireNonNull(sourceStateFrame, "sourceStateFrame");
        Objects.requireNonNull(sourceLocalDirection, "sourceLocalDirection");
        Objects.requireNonNull(physicalDirection, "physicalDirection");
        Objects.requireNonNull(targetStateFrame, "targetStateFrame");
        Objects.requireNonNull(
                targetLocalSideTowardSource,
                "targetLocalSideTowardSource"
        );

        if (sourceStateFrame.field() != targetStateFrame.field()) {
            throw new IllegalArgumentException(
                    "source/target frames use different gravity fields"
            );
        }
        if (!sourceStateFrame.pos()
                .relative(physicalDirection)
                .equals(targetStateFrame.pos())) {
            throw new IllegalArgumentException(
                    "physicalDirection does not lead to target"
            );
        }
        if (targetStateFrame.localToWorld(
                targetLocalSideTowardSource
        ) != physicalDirection.getOpposite()) {
            throw new IllegalArgumentException(
                    "target local side does not face source"
            );
        }
    }

    public static Optional<PlanetBlockNeighborQuery> resolve(
            PlanetGravityField field,
            BlockPos sourcePos,
            Direction sourceLocalDirection
    ) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(sourceLocalDirection, "sourceLocalDirection");

        Optional<PlanetBlockStateFrame> sourceOptional =
                PlanetBlockStateFrame.resolve(field, sourcePos);
        if (sourceOptional.isEmpty()) {
            return Optional.empty();
        }

        PlanetBlockStateFrame source = sourceOptional.get();
        Optional<PlanetBlockFrameContext> traversalOptional =
                PlanetBlockFrameContext.resolve(
                        field,
                        sourcePos,
                        source.face()
                );
        if (traversalOptional.isEmpty()) {
            return Optional.empty();
        }

        PlanetBlockStep step =
                traversalOptional.get().step(sourceLocalDirection);

        Optional<PlanetBlockStateFrame> targetOptional =
                PlanetBlockStateFrame.resolve(
                        field,
                        step.target().pos()
                );
        if (targetOptional.isEmpty()) {
            return Optional.empty();
        }

        PlanetBlockStateFrame target = targetOptional.get();

        return Optional.of(
                new PlanetBlockNeighborQuery(
                        source,
                        sourceLocalDirection,
                        step.physicalDirection(),
                        target,
                        target.worldToLocal(
                                step.physicalDirection().getOpposite()
                        ),
                        step.crossedGravityBoundary()
                )
        );
    }

    public BlockPos sourcePos() {
        return sourceStateFrame.pos();
    }

    public BlockPos targetPos() {
        return targetStateFrame.pos();
    }
}
