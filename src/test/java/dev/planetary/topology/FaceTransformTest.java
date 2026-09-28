package dev.planetary.topology;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FaceTransformTest {
    private static final int SIZE = 64;
    private static final List<PlanetDirection> EDGES = List.of(
            PlanetDirection.NORTH,
            PlanetDirection.SOUTH,
            PlanetDirection.WEST,
            PlanetDirection.EAST
    );

    @Test
    void allTwentyFourDirectedEdgesAreDefinedAndReciprocal() {
        int count = 0;
        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : EDGES) {
                FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
                FaceTransform inverse = transform.inverse();

                count++;
                assertEquals(face, transform.sourceFace());
                assertEquals(edge, transform.sourceEdge());
                assertNotEquals(face, transform.targetFace());
                assertEquals(face, inverse.targetFace());
                assertEquals(edge, inverse.targetEdge());
                assertEquals(transform.targetFace(), inverse.sourceFace());
                assertEquals(transform.targetEdge(), inverse.sourceEdge());
            }
        }
        assertEquals(24, count);
    }

    @Test
    void crossingAnEdgeAndCrossingBackReturnsTheSameCell() {
        int[] seamSamples = {0, 1, SIZE / 2, SIZE - 2, SIZE - 1};
        int[] ySamples = {-128, -1, 0, 17, 512};

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : EDGES) {
                FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
                FaceTransform inverse = transform.inverse();

                for (int seam : seamSamples) {
                    for (int y : ySamples) {
                        PlanetPos source = boundaryCell(face, edge, seam, y);
                        PlanetPos target = transform.crossBoundaryCell(source, SIZE);
                        PlanetPos roundTrip = inverse.crossBoundaryCell(target, SIZE);
                        assertEquals(source, roundTrip, transform.toString());
                    }
                }
            }
        }
    }

    @Test
    void directionTransformIsExactlyInvertibleForAllSixDirections() {
        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : EDGES) {
                FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
                FaceTransform inverse = transform.inverse();

                for (PlanetDirection direction : PlanetDirection.values()) {
                    PlanetDirection onTarget = transform.transformDirection(direction);
                    PlanetDirection roundTrip = inverse.transformDirection(onTarget);
                    assertEquals(direction, roundTrip, transform + " / " + direction);
                }
            }
        }
    }

    @Test
    void gravityDirectionsRemainLocalAcrossEveryEdge() {
        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : EDGES) {
                FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
                assertEquals(PlanetDirection.UP, transform.transformDirection(PlanetDirection.UP));
                assertEquals(PlanetDirection.DOWN, transform.transformDirection(PlanetDirection.DOWN));
            }
        }
    }

    @Test
    void topFaceHasExpectedHumanReadableNeighbors() {
        assertEquals(PlanetFace.POS_X, PlanetFace.POS_Y.neighborAcross(PlanetDirection.EAST));
        assertEquals(PlanetFace.NEG_X, PlanetFace.POS_Y.neighborAcross(PlanetDirection.WEST));
        assertEquals(PlanetFace.POS_Z, PlanetFace.POS_Y.neighborAcross(PlanetDirection.SOUTH));
        assertEquals(PlanetFace.NEG_Z, PlanetFace.POS_Y.neighborAcross(PlanetDirection.NORTH));
    }

    @Test
    void seamReversalExistsButNeverBreaksReciprocity() {
        boolean sawPreserved = false;
        boolean sawReversed = false;

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : EDGES) {
                FaceTransform transform = PlanetTopology.edgeTransform(face, edge);
                sawPreserved |= !transform.reversesSeamCoordinate();
                sawReversed |= transform.reversesSeamCoordinate();
                assertEquals(
                        transform.reversesSeamCoordinate(),
                        transform.inverse().reversesSeamCoordinate()
                );
            }
        }

        assertTrue(sawPreserved);
        assertTrue(sawReversed);
        assertFalse(EDGES.isEmpty());
    }

    private static PlanetPos boundaryCell(PlanetFace face, PlanetDirection edge, int seam, int y) {
        return switch (edge) {
            case WEST -> new PlanetPos(face, 0, y, seam);
            case EAST -> new PlanetPos(face, SIZE - 1, y, seam);
            case NORTH -> new PlanetPos(face, seam, y, 0);
            case SOUTH -> new PlanetPos(face, seam, y, SIZE - 1);
            default -> throw new IllegalArgumentException("Not an edge: " + edge);
        };
    }
}
