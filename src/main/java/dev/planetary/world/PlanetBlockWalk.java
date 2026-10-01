package dev.planetary.world;

import net.minecraft.core.Direction;

import java.util.Objects;

/**
 * Result of continuing one local block direction for a fixed number of
 * physical one-block steps.
 *
 * <p>The direction is parallel-transported after every gravity-boundary
 * crossing. This deliberately models an ordered path, not an unordered
 * Cartesian dx/dy/dz displacement.</p>
 *
 * @param source starting block/frame context
 * @param initialDirection local direction requested at the source
 * @param steps number of physical one-block steps taken
 * @param target final block/frame context
 * @param transportedDirection local direction to use for continuing the same
 *                             path from the target
 * @param gravityBoundaryCrossings number of frame changes along the path
 */
public record PlanetBlockWalk(
        PlanetBlockFrameContext source,
        Direction initialDirection,
        int steps,
        PlanetBlockFrameContext target,
        Direction transportedDirection,
        int gravityBoundaryCrossings
) {
    public PlanetBlockWalk {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(
                initialDirection,
                "initialDirection"
        );
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(
                transportedDirection,
                "transportedDirection"
        );

        if (steps < 0) {
            throw new IllegalArgumentException(
                    "steps must be >= 0"
            );
        }
        if (gravityBoundaryCrossings < 0
                || gravityBoundaryCrossings > steps) {
            throw new IllegalArgumentException(
                    "gravityBoundaryCrossings must be between 0 and steps"
            );
        }

        if (steps == 0) {
            if (!source.pos().equals(target.pos())
                    || source.face() != target.face()) {
                throw new IllegalArgumentException(
                        "zero-step walk must remain in source context"
                );
            }
            if (transportedDirection != initialDirection) {
                throw new IllegalArgumentException(
                        "zero-step walk must preserve direction"
                );
            }
            if (gravityBoundaryCrossings != 0) {
                throw new IllegalArgumentException(
                        "zero-step walk cannot cross a boundary"
                );
            }
        }
    }

    public boolean crossedGravityBoundary() {
        return gravityBoundaryCrossings > 0;
    }
}
