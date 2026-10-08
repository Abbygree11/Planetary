package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetTopology;
import net.minecraft.core.Direction;

import java.util.Objects;

/**
 * Transports a three-dimensional LOCAL block-cell displacement through the
 * real physical cube topology without inventing alias world positions.
 *
 * <p>Unlike a single `frame.localToWorld(dx,dy,dz)` projection, this preserves
 * both current physical BlockPos AND the remaining tangent-coordinate basis
 * at every gravity edge. The documented path order is X, then Z, then Y.
 * Near a three-face vertex this is deliberately ORDERED rather than claiming
 * two axis orders commute.</p>
 *
 * <p>The source and result retain traversal-frame identity; consumers doing
 * canonical BlockState queries must still use the state frame at targetPos.</p>
 */
public final class PlanetLocalBlockOffset {
    private PlanetLocalBlockOffset() {
    }

    /**
     * @return target physical cell and its transported traversal chart
     */
    public static PlanetBlockFrameContext traverse(
            PlanetBlockFrameContext source,
            int localX,
            int localY,
            int localZ
    ) {
        Objects.requireNonNull(source, "source");

        Basis basis = new Basis(
                Direction.EAST,
                Direction.SOUTH
        );
        PlanetBlockFrameContext current = source;

        current = traverseAxis(current, basis, localX, Axis.EAST);
        current = traverseAxis(current, basis, localZ, Axis.SOUTH);
        current = traverseAxis(current, basis, localY, Axis.UP);

        return current;
    }

    private static PlanetBlockFrameContext traverseAxis(
            PlanetBlockFrameContext initial,
            Basis basis,
            int delta,
            Axis axis
    ) {
        PlanetBlockFrameContext current = initial;
        Direction canonicalAxis = switch (axis) {
            case EAST -> basis.east;
            case SOUTH -> basis.south;
            case UP -> Direction.UP;
        };

        // Integer.MIN_VALUE cannot be negated; a particle/source offset this
        // large is invalid and would never be a sensible bounded block query.
        if (delta == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unbounded local offset");
        }

        Direction currentDirection =
                delta >= 0
                        ? canonicalAxis
                        : canonicalAxis.getOpposite();

        for (int n = 0; n < Math.abs(delta); n++) {
            PlanetBlockStep step = current.step(currentDirection);

            if (step.crossedGravityBoundary()) {
                if (currentDirection.getAxis().isVertical()) {
                    // A radial frame change near the core is not a surface
                    // edge. No FaceTransform between tangent charts can be
                    // inferred safely at this step.
                    throw new IllegalStateException(
                            "Radial frame transition during local "
                                    + axis + " displacement at "
                                    + current.pos()
                    );
                }

                FaceTransform transition = PlanetTopology.edgeTransform(
                        current.face(),
                        PlanetVanillaDirection.fromVanilla(currentDirection)
                );

                if (transition.targetFace() != step.target().face()) {
                    throw new IllegalStateException(
                            "Unexpected face transition "
                                    + current.face()
                                    + " -> "
                                    + step.target().face()
                                    + " while moving "
                                    + currentDirection
                    );
                }

                basis.east = remap(transition, basis.east);
                basis.south = remap(transition, basis.south);
                currentDirection = remap(transition, currentDirection);
            }

            current = step.target();
        }

        return current;
    }

    private static Direction remap(
            FaceTransform transition,
            Direction localDirection
    ) {
        PlanetDirection source =
                PlanetVanillaDirection.fromVanilla(localDirection);

        return PlanetVanillaDirection.toVanilla(
                transition.transformDirection(source)
        );
    }

    private enum Axis {
        EAST,
        SOUTH,
        UP
    }

    private static final class Basis {
        private Direction east;
        private Direction south;

        private Basis(Direction east, Direction south) {
            this.east = east;
            this.south = south;
        }
    }
}
