package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetTopology;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.topology.PlanetCore;
import net.minecraft.core.BlockPos;
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

        if (localX == Integer.MIN_VALUE
                || localY == Integer.MIN_VALUE
                || localZ == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unbounded local offset");
        }

        // Most samples are well within one face. A conservative dominance
        // bound proves every intermediate X -> Z -> Y step remains on that
        // face, so there is no need to resolve 1..30 frame steps per sampled
        // SporeBlossom candidate. Edge/corner cases keep the full traversal.
        PlanetBlockFrameContext interior = sameFaceInterior(
                source, localX, localY, localZ
        );
        if (interior != null) {
            return interior;
        }

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

    /**
     * Exact fast path only when one source gravity face strictly dominates
     * both tangent coordinates along the entire ordered multi-axis path.
     * At ties or near edges, use the general transported-chart walk.
     */
    private static PlanetBlockFrameContext sameFaceInterior(
            PlanetBlockFrameContext source,
            int x,
            int y,
            int z
    ) {
        PlanetGravityFrame frame = source.frame();
        PlanetCore core = source.field().core();
        BlockPos pos = source.pos();
        long dx = (long) pos.getX() - core.x();
        long dy = (long) pos.getY() - core.y();
        long dz = (long) pos.getZ() - core.z();
        PlanetVector east = frame.worldEast();
        PlanetVector south = frame.worldSouth();
        PlanetVector up = frame.worldUp();

        long tangentX = dx * east.x() + dy * east.y() + dz * east.z();
        long tangentZ = dx * south.x() + dy * south.y() + dz * south.z();
        long radial = dx * up.x() + dy * up.y() + dz * up.z();

        long smallestRadial = Math.min(radial, radial + y);
        long widestEast = Math.max(
                Math.abs(tangentX), Math.abs(tangentX + x)
        );
        long widestSouth = Math.max(
                Math.abs(tangentZ), Math.abs(tangentZ + z)
        );

        if (smallestRadial <= Math.max(widestEast, widestSouth)) {
            return null;
        }

        long targetX = (long) pos.getX()
                + (long) east.x() * x
                + (long) south.x() * z
                + (long) up.x() * y;
        long targetY = (long) pos.getY()
                + (long) east.y() * x
                + (long) south.y() * z
                + (long) up.y() * y;
        long targetZ = (long) pos.getZ()
                + (long) east.z() * x
                + (long) south.z() * z
                + (long) up.z() * y;

        if (targetX < Integer.MIN_VALUE || targetX > Integer.MAX_VALUE
                || targetY < Integer.MIN_VALUE || targetY > Integer.MAX_VALUE
                || targetZ < Integer.MIN_VALUE || targetZ > Integer.MAX_VALUE) {
            return null;
        }

        return PlanetBlockFrameContext.resolve(
                source.field(),
                new BlockPos((int) targetX, (int) targetY, (int) targetZ),
                source.face()
        ).orElseThrow();
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
