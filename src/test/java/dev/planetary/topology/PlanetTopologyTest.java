package dev.planetary.topology;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetTopologyTest {
    private static final int SIZE = 32;
    private static final List<PlanetDirection> HORIZONTAL = List.of(
            PlanetDirection.NORTH,
            PlanetDirection.SOUTH,
            PlanetDirection.WEST,
            PlanetDirection.EAST
    );

    @Test
    void ordinaryNeighborsInsideAFaceRemainOrdinaryGridNeighbors() {
        PlanetPos pos = new PlanetPos(PlanetFace.POS_Y, 10, 25, 10);

        assertEquals(new PlanetPos(PlanetFace.POS_Y, 11, 25, 10), PlanetTopology.step(pos, PlanetDirection.EAST, SIZE));
        assertEquals(new PlanetPos(PlanetFace.POS_Y, 9, 25, 10), PlanetTopology.step(pos, PlanetDirection.WEST, SIZE));
        assertEquals(new PlanetPos(PlanetFace.POS_Y, 10, 25, 9), PlanetTopology.step(pos, PlanetDirection.NORTH, SIZE));
        assertEquals(new PlanetPos(PlanetFace.POS_Y, 10, 25, 11), PlanetTopology.step(pos, PlanetDirection.SOUTH, SIZE));
    }

    @Test
    void upAndDownNeverChangeFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetPos pos = new PlanetPos(face, 7, 100, 8);
            assertEquals(new PlanetPos(face, 7, 101, 8), PlanetTopology.step(pos, PlanetDirection.UP, SIZE));
            assertEquals(new PlanetPos(face, 7, 99, 8), PlanetTopology.step(pos, PlanetDirection.DOWN, SIZE));
        }
    }

    @Test
    void everyBoundaryNeighborIsSymmetric() {
        int[] samples = {0, 1, SIZE / 2, SIZE - 2, SIZE - 1};

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : HORIZONTAL) {
                FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
                for (int seam : samples) {
                    PlanetPos source = boundaryCell(face, edge, seam);
                    PlanetPos target = PlanetTopology.step(source, edge, SIZE);
                    PlanetPos back = PlanetTopology.step(target, transform.targetEdge(), SIZE);
                    assertEquals(source, back, transform.toString());
                }
            }
        }
    }

    @Test
    void closedLoopAroundFourSideFacesReturnsToTheStartingFrame() {
        PlanetFace face = PlanetFace.POS_X;
        PlanetDirection direction = PlanetDirection.EAST;

        PlanetDirection[] edges = {
                PlanetDirection.SOUTH,
                PlanetDirection.WEST,
                PlanetDirection.NORTH,
                PlanetDirection.WEST
        };

        for (PlanetDirection edge : edges) {
            FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
            direction = transform.transformDirection(direction);
            face = transform.targetFace();
        }

        assertEquals(PlanetDirection.EAST, direction);
        assertEquals(PlanetFace.POS_X, face);
    }

    private static PlanetPos boundaryCell(PlanetFace face, PlanetDirection edge, int seam) {
        return switch (edge) {
            case WEST -> new PlanetPos(face, 0, 0, seam);
            case EAST -> new PlanetPos(face, SIZE - 1, 0, seam);
            case NORTH -> new PlanetPos(face, seam, 0, 0);
            case SOUTH -> new PlanetPos(face, seam, 0, SIZE - 1);
            default -> throw new IllegalArgumentException("Not an edge: " + edge);
        };
    }
}
