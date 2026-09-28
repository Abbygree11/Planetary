package dev.planetary.chunk;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetPos;
import dev.planetary.topology.PlanetTopology;

import java.util.Objects;

/**
 * Applies the cube-face topology directly at 16x16x16 section granularity.
 *
 * <p>A section is still an ordinary orthogonal voxel cube. Only its address
 * changes when a horizontal neighbor crosses a face edge. The same topology
 * contract is later reused for chunks, BlockPos adapters, entities and
 * rendering.</p>
 */
public final class PlanetSectionTopology {
    private PlanetSectionTopology() {
    }

    public static PlanetSectionAddress step(
            PlanetSectionAddress address,
            PlanetDirection direction,
            int faceSizeSections
    ) {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(direction, "direction");
        validateFaceSize(faceSizeSections);

        PlanetPos source = new PlanetPos(
                address.face(),
                address.x(),
                address.y(),
                address.z()
        );

        PlanetPos target = PlanetTopology.step(source, direction, faceSizeSections);
        return new PlanetSectionAddress(
                target.face(),
                target.x(),
                target.y(),
                target.z()
        );
    }

    public static FaceTransform edgeTransform(
            PlanetSectionAddress address,
            PlanetDirection edge,
            int faceSizeSections
    ) {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(edge, "edge");
        validateFaceSize(faceSizeSections);

        if (!edge.isHorizontal()) {
            throw new IllegalArgumentException("Only horizontal directions identify a face edge: " + edge);
        }
        if (!isOnEdge(address, edge, faceSizeSections)) {
            throw new IllegalArgumentException(
                    "Section " + address + " is not on edge " + edge
            );
        }

        return PlanetTopology.edgeTransform(address.face(), edge);
    }

    public static PlanetDirection transformDirectionAcrossEdge(
            PlanetSectionAddress address,
            PlanetDirection edge,
            PlanetDirection direction,
            int faceSizeSections
    ) {
        return edgeTransform(address, edge, faceSizeSections).transformDirection(direction);
    }

    public static boolean isOnEdge(
            PlanetSectionAddress address,
            PlanetDirection edge,
            int faceSizeSections
    ) {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(edge, "edge");
        validateFaceSize(faceSizeSections);

        return switch (edge) {
            case WEST -> address.x() == 0;
            case EAST -> address.x() == faceSizeSections - 1;
            case NORTH -> address.z() == 0;
            case SOUTH -> address.z() == faceSizeSections - 1;
            case UP, DOWN -> false;
        };
    }

    private static void validateFaceSize(int faceSizeSections) {
        if (faceSizeSections <= 0) {
            throw new IllegalArgumentException("faceSizeSections must be > 0");
        }
    }
}
