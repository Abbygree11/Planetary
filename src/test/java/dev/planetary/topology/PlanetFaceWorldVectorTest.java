package dev.planetary.topology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetFaceWorldVectorTest {

    @Test
    void exposedWorldBasisMatchesTheCubeFaceFrame() {
        assertBasis(
                PlanetFace.POS_Y,
                new PlanetVector(1, 0, 0),
                new PlanetVector(0, 1, 0),
                new PlanetVector(0, 0, 1)
        );
        assertBasis(
                PlanetFace.NEG_Y,
                new PlanetVector(1, 0, 0),
                new PlanetVector(0, -1, 0),
                new PlanetVector(0, 0, -1)
        );
        assertBasis(
                PlanetFace.POS_X,
                new PlanetVector(0, -1, 0),
                new PlanetVector(1, 0, 0),
                new PlanetVector(0, 0, 1)
        );
        assertBasis(
                PlanetFace.NEG_X,
                new PlanetVector(0, 1, 0),
                new PlanetVector(-1, 0, 0),
                new PlanetVector(0, 0, 1)
        );
        assertBasis(
                PlanetFace.POS_Z,
                new PlanetVector(1, 0, 0),
                new PlanetVector(0, 0, 1),
                new PlanetVector(0, -1, 0)
        );
        assertBasis(
                PlanetFace.NEG_Z,
                new PlanetVector(-1, 0, 0),
                new PlanetVector(0, 0, -1),
                new PlanetVector(0, -1, 0)
        );
    }

    @Test
    void everyExposedBasisRemainsRightHanded() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetVector east =
                    face.worldVector(PlanetDirection.EAST);
            PlanetVector up =
                    face.worldVector(PlanetDirection.UP);
            PlanetVector south =
                    face.worldVector(PlanetDirection.SOUTH);

            assertEquals(south, east.cross(up), face.name());
            assertEquals(0, east.dot(up), face.name());
            assertEquals(0, east.dot(south), face.name());
            assertEquals(0, up.dot(south), face.name());
        }
    }

    private static void assertBasis(
            PlanetFace face,
            PlanetVector east,
            PlanetVector up,
            PlanetVector south
    ) {
        assertEquals(
                east,
                face.worldVector(PlanetDirection.EAST)
        );
        assertEquals(
                up,
                face.worldVector(PlanetDirection.UP)
        );
        assertEquals(
                south,
                face.worldVector(PlanetDirection.SOUTH)
        );
    }
}
