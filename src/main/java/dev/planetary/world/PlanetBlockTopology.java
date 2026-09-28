package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetPos;
import dev.planetary.topology.PlanetTopology;

import java.util.Objects;

/**
 * Block-granularity navigation across the six cube-planet faces.
 */
public final class PlanetBlockTopology {
    private PlanetBlockTopology() {
    }

    public static PlanetBlockPos step(
            PlanetBlockPos pos,
            PlanetDirection direction,
            int faceSizeBlocks
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(direction, "direction");
        validateFaceSize(faceSizeBlocks);

        PlanetPos target = PlanetTopology.step(
                new PlanetPos(pos.face(), pos.x(), pos.y(), pos.z()),
                direction,
                faceSizeBlocks
        );

        return new PlanetBlockPos(
                target.face(),
                target.x(),
                target.y(),
                target.z()
        );
    }

    /**
     * Applies a local offset while carrying the local X/Z frame through every
     * crossed cube edge.
     */
    public static PlanetBlockPos offset(
            PlanetBlockPos origin,
            int dx,
            int dy,
            int dz,
            int faceSizeBlocks
    ) {
        Objects.requireNonNull(origin, "origin");
        validateFaceSize(faceSizeBlocks);

        PlanetBlockPos current = origin;
        PlanetDirection xDirection = dx >= 0 ? PlanetDirection.EAST : PlanetDirection.WEST;
        PlanetDirection zDirection = dz >= 0 ? PlanetDirection.SOUTH : PlanetDirection.NORTH;

        int xSteps = Math.abs(dx);
        int zSteps = Math.abs(dz);
        int doneX = 0;
        int doneZ = 0;

        while (doneX < xSteps || doneZ < zSteps) {
            boolean takeX;
            if (doneX >= xSteps) {
                takeX = false;
            } else if (doneZ >= zSteps) {
                takeX = true;
            } else {
                long nextX = (long) (2 * doneX + 1) * zSteps;
                long nextZ = (long) (2 * doneZ + 1) * xSteps;
                takeX = nextX <= nextZ;
            }

            PlanetDirection moveDirection = takeX ? xDirection : zDirection;
            FaceTransform crossed = isOnEdge(current, moveDirection, faceSizeBlocks)
                    ? PlanetTopology.edgeTransform(current.face(), moveDirection)
                    : null;

            current = step(current, moveDirection, faceSizeBlocks);

            if (crossed != null) {
                xDirection = crossed.transformDirection(xDirection);
                zDirection = crossed.transformDirection(zDirection);
            }

            if (takeX) {
                doneX++;
            } else {
                doneZ++;
            }
        }

        return new PlanetBlockPos(
                current.face(),
                current.x(),
                current.y() + dy,
                current.z()
        );
    }

    public static boolean isOnEdge(
            PlanetBlockPos pos,
            PlanetDirection edge,
            int faceSizeBlocks
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(edge, "edge");
        validateFaceSize(faceSizeBlocks);

        return switch (edge) {
            case WEST -> pos.x() == 0;
            case EAST -> pos.x() == faceSizeBlocks - 1;
            case NORTH -> pos.z() == 0;
            case SOUTH -> pos.z() == faceSizeBlocks - 1;
            case UP, DOWN -> false;
        };
    }

    private static void validateFaceSize(int faceSizeBlocks) {
        if (faceSizeBlocks <= 0) {
            throw new IllegalArgumentException("faceSizeBlocks must be > 0");
        }
    }
}
