package dev.planetary.topology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetEmbeddingTest {
    private static final int SIZE = 12;
    private static final double EPSILON = 1.0e-9;

    @Test
    void localWorldProjectionRoundTripsOnEveryFace() {
        PlanetEmbedding embedding = new PlanetEmbedding(SIZE);

        double[][] samples = {
                {0.0, 0.0, 0.0},
                {SIZE, 0.0, SIZE},
                {SIZE / 2.0, 1.75, SIZE / 3.0},
                {2.25, -3.5, 9.5}
        };

        for (PlanetFace face : PlanetFace.values()) {
            for (double[] sample : samples) {
                PlanetEmbedding.PlanetWorldPoint world =
                        embedding.toWorld(
                                face,
                                sample[0],
                                sample[1],
                                sample[2]
                        );

                PlanetEmbedding.LocalPoint local =
                        embedding.toLocal(face, world);

                assertEquals(face, local.face());
                assertEquals(sample[0], local.x(), EPSILON);
                assertEquals(sample[1], local.y(), EPSILON);
                assertEquals(sample[2], local.z(), EPSILON);
            }
        }
    }

    @Test
    void everyDirectedTopologyEdgeSharesOnePhysicalSeamLine() {
        PlanetEmbedding embedding = new PlanetEmbedding(SIZE);
        double[] seamSamples = {
                0.0,
                0.25,
                1.0,
                SIZE / 2.0,
                SIZE - 0.25,
                SIZE
        };

        PlanetDirection[] edges = {
                PlanetDirection.NORTH,
                PlanetDirection.SOUTH,
                PlanetDirection.WEST,
                PlanetDirection.EAST
        };

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : edges) {
                FaceTransform transform =
                        PlanetTopology.edgeTransform(face, edge);

                for (double seam : seamSamples) {
                    PlanetEmbedding.PlanetWorldPoint source =
                            embedding.surfaceEdgePoint(
                                    face,
                                    edge,
                                    seam
                            );

                    double targetSeam =
                            embedding.transformSeamCoordinate(
                                    transform,
                                    seam
                            );

                    PlanetEmbedding.PlanetWorldPoint target =
                            embedding.surfaceEdgePoint(
                                    transform.targetFace(),
                                    transform.targetEdge(),
                                    targetSeam
                            );

                    assertPointEquals(
                            source,
                            target,
                            transform + " seam=" + seam
                    );
                }
            }
        }
    }

    @Test
    void surfacePlanesRemainRigidAndOrthogonal() {
        PlanetEmbedding embedding = new PlanetEmbedding(SIZE);

        for (PlanetFace face : PlanetFace.values()) {
            PlanetEmbedding.PlanetWorldPoint origin =
                    embedding.toWorld(face, 0.0, 0.0, 0.0);
            PlanetEmbedding.PlanetWorldPoint east =
                    embedding.toWorld(face, 1.0, 0.0, 0.0);
            PlanetEmbedding.PlanetWorldPoint up =
                    embedding.toWorld(face, 0.0, 1.0, 0.0);
            PlanetEmbedding.PlanetWorldPoint south =
                    embedding.toWorld(face, 0.0, 0.0, 1.0);

            double ex = east.x() - origin.x();
            double ey = east.y() - origin.y();
            double ez = east.z() - origin.z();

            double ux = up.x() - origin.x();
            double uy = up.y() - origin.y();
            double uz = up.z() - origin.z();

            double sx = south.x() - origin.x();
            double sy = south.y() - origin.y();
            double sz = south.z() - origin.z();

            assertEquals(1.0, length(ex, ey, ez), EPSILON);
            assertEquals(1.0, length(ux, uy, uz), EPSILON);
            assertEquals(1.0, length(sx, sy, sz), EPSILON);

            assertEquals(0.0, dot(ex, ey, ez, ux, uy, uz), EPSILON);
            assertEquals(0.0, dot(ex, ey, ez, sx, sy, sz), EPSILON);
            assertEquals(0.0, dot(ux, uy, uz, sx, sy, sz), EPSILON);
        }
    }

    private static void assertPointEquals(
            PlanetEmbedding.PlanetWorldPoint expected,
            PlanetEmbedding.PlanetWorldPoint actual,
            String message
    ) {
        assertEquals(expected.x(), actual.x(), EPSILON, message + " x");
        assertEquals(expected.y(), actual.y(), EPSILON, message + " y");
        assertEquals(expected.z(), actual.z(), EPSILON, message + " z");
    }

    private static double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static double dot(
            double ax,
            double ay,
            double az,
            double bx,
            double by,
            double bz
    ) {
        return ax * bx + ay * by + az * bz;
    }
}
