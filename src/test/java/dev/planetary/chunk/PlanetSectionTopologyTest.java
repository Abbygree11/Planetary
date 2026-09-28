package dev.planetary.chunk;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionTopologyTest {
    private static final int FACE_SIZE = 256;
    private static final List<PlanetDirection> EDGES = List.of(
            PlanetDirection.NORTH,
            PlanetDirection.SOUTH,
            PlanetDirection.WEST,
            PlanetDirection.EAST
    );

    @Test
    void ordinarySectionNeighborsStayOnTheSameFace() {
        PlanetSectionAddress section = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                100,
                -12,
                120
        );

        assertEquals(
                new PlanetSectionAddress(PlanetFace.POS_Y, 101, -12, 120),
                PlanetSectionTopology.step(section, PlanetDirection.EAST, FACE_SIZE)
        );
        assertEquals(
                new PlanetSectionAddress(PlanetFace.POS_Y, 100, -13, 120),
                PlanetSectionTopology.step(section, PlanetDirection.DOWN, FACE_SIZE)
        );
    }

    @Test
    void topFaceEastEdgeEntersPositiveXFaceWithoutChangingDepth() {
        PlanetSectionAddress source = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                -37,
                80
        );

        PlanetSectionAddress target = PlanetSectionTopology.step(
                source,
                PlanetDirection.EAST,
                FACE_SIZE
        );

        assertEquals(PlanetFace.POS_X, target.face());
        assertEquals(-37, target.y());
    }

    @Test
    void everyDirectedFaceEdgeIsReciprocalAtSectionGranularity() {
        int[] seamSamples = {0, 1, FACE_SIZE / 2, FACE_SIZE - 2, FACE_SIZE - 1};

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : EDGES) {
                for (int seam : seamSamples) {
                    PlanetSectionAddress source = boundarySection(face, edge, seam, -19);
                    FaceTransform transform = PlanetSectionTopology.edgeTransform(
                            source,
                            edge,
                            FACE_SIZE
                    );

                    PlanetSectionAddress target = PlanetSectionTopology.step(
                            source,
                            edge,
                            FACE_SIZE
                    );

                    PlanetSectionAddress roundTrip = PlanetSectionTopology.step(
                            target,
                            transform.targetEdge(),
                            FACE_SIZE
                    );

                    assertEquals(source, roundTrip, transform.toString());
                }
            }
        }
    }

    @Test
    void localUpAndDownNeverChangeTheGravityFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetSectionAddress source = new PlanetSectionAddress(face, 50, -100, 70);

            PlanetSectionAddress up = PlanetSectionTopology.step(
                    source,
                    PlanetDirection.UP,
                    FACE_SIZE
            );
            PlanetSectionAddress down = PlanetSectionTopology.step(
                    source,
                    PlanetDirection.DOWN,
                    FACE_SIZE
            );

            assertEquals(face, up.face());
            assertEquals(-99, up.y());
            assertEquals(face, down.face());
            assertEquals(-101, down.y());
        }
    }

    @Test
    void edgeDetectionOnlyMatchesTheRequestedBoundary() {
        PlanetSectionAddress west = new PlanetSectionAddress(PlanetFace.POS_Z, 0, 7, 90);
        PlanetSectionAddress interior = new PlanetSectionAddress(PlanetFace.POS_Z, 1, 7, 90);

        assertTrue(PlanetSectionTopology.isOnEdge(west, PlanetDirection.WEST, FACE_SIZE));
        assertFalse(PlanetSectionTopology.isOnEdge(west, PlanetDirection.EAST, FACE_SIZE));
        assertFalse(PlanetSectionTopology.isOnEdge(interior, PlanetDirection.WEST, FACE_SIZE));
    }

    @Test
    void blockCoordinatesConvertToFaceAwareSectionAddressUsingFloorDivision() {
        PlanetSectionAddress address = PlanetSectionAddress.fromLocalBlock(
                PlanetFace.NEG_X,
                -1,
                -17,
                32
        );

        assertEquals(
                new PlanetSectionAddress(PlanetFace.NEG_X, -1, -2, 2),
                address
        );
    }

    private static PlanetSectionAddress boundarySection(
            PlanetFace face,
            PlanetDirection edge,
            int seam,
            int y
    ) {
        return switch (edge) {
            case WEST -> new PlanetSectionAddress(face, 0, y, seam);
            case EAST -> new PlanetSectionAddress(face, FACE_SIZE - 1, y, seam);
            case NORTH -> new PlanetSectionAddress(face, seam, y, 0);
            case SOUTH -> new PlanetSectionAddress(face, seam, y, FACE_SIZE - 1);
            default -> throw new IllegalArgumentException("Not a horizontal edge: " + edge);
        };
    }
}
