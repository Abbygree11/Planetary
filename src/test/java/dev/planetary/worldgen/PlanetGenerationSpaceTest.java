package dev.planetary.worldgen;

import dev.planetary.topology.PlanetCore;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetGenerationSpaceTest {
    private static final double EPSILON = 1.0E-9D;

    private static final PlanetCore CORE =
            new PlanetCore(10, -20, 30, 500);
    private static final PlanetGenerationSpace SPACE =
            new PlanetGenerationSpace(CORE);

    @Test
    void coreMapsToGenerationOrigin() {
        PlanetGenerationPoint point =
                SPACE.toGeneration(
                        new BlockPos(
                                CORE.x(),
                                CORE.y(),
                                CORE.z()
                        )
                );

        assertEquals(0.0D, point.x(), EPSILON);
        assertEquals(0.0D, point.y(), EPSILON);
        assertEquals(0.0D, point.z(), EPSILON);
    }

    @Test
    void everyPhysicalCubeShellBecomesOneSphere() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        double[][] samples = {
                {100.0D, 0.0D, 0.0D},
                {100.0D, 50.0D, 0.0D},
                {100.0D, 100.0D, 0.0D},
                {100.0D, 100.0D, 100.0D},
                {-100.0D, 37.0D, -91.0D}
        };

        for (double[] sample : samples) {
            PlanetGenerationPoint point =
                    SPACE.toGeneration(
                            cx + sample[0],
                            cy + sample[1],
                            cz + sample[2]
                    );

            assertEquals(
                    100.0D,
                    point.radius(),
                    EPSILON
            );
        }
    }

    @Test
    void generationTransformRoundTripsAcrossFacesEdgesAndCorners() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        double[][] samples = {
                {100.0D, 20.0D, -30.0D},
                {-100.0D, 20.0D, 30.0D},
                {100.0D, 100.0D, 2.0D},
                {100.0D, -100.0D, 100.0D},
                {-100.0D, -100.0D, -100.0D},
                {0.001D, 0.001D, 0.001D}
        };

        for (double[] sample : samples) {
            double wx = cx + sample[0];
            double wy = cy + sample[1];
            double wz = cz + sample[2];

            PlanetWorldGenPoint roundTrip =
                    SPACE.toWorld(
                            SPACE.toGeneration(
                                    wx,
                                    wy,
                                    wz
                            )
                    );

            assertEquals(wx, roundTrip.x(), EPSILON);
            assertEquals(wy, roundTrip.y(), EPSILON);
            assertEquals(wz, roundTrip.z(), EPSILON);
        }
    }

    @Test
    void generationCoordinatesAreContinuousAcrossGravityEdge() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        double epsilon = 1.0E-5D;

        PlanetGenerationPoint left =
                SPACE.toGeneration(
                        cx + 100.0D,
                        cy + 100.0D - epsilon,
                        cz + 17.0D
                );

        PlanetGenerationPoint right =
                SPACE.toGeneration(
                        cx + 100.0D - epsilon,
                        cy + 100.0D,
                        cz + 17.0D
                );

        double distance = Math.sqrt(
                square(left.x() - right.x())
                        + square(left.y() - right.y())
                        + square(left.z() - right.z())
        );

        assertTrue(
                distance < 5.0E-5D,
                "generation space must not jump at x=y gravity boundary"
        );
    }

    @Test
    void edgeDistanceIsZeroAtEdgesAndCorners() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        assertEquals(
                0.0D,
                SPACE.edgeDistance(
                        cx + 100.0D,
                        cy + 100.0D,
                        cz
                ).euclideanDistance(),
                EPSILON
        );

        assertEquals(
                0.0D,
                SPACE.edgeDistance(
                        cx - 100.0D,
                        cy + 100.0D,
                        cz - 100.0D
                ).euclideanDistance(),
                EPSILON
        );
    }

    @Test
    void edgeDistanceUsesNearestDiagonalPlane() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        PlanetEdgeDistance distance =
                SPACE.edgeDistance(
                        cx + 100.0D,
                        cy + 80.0D,
                        cz + 10.0D
                );

        assertEquals(
                20.0D,
                distance.axisMargin(),
                EPSILON
        );
        assertEquals(
                20.0D / Math.sqrt(2.0D),
                distance.euclideanDistance(),
                EPSILON
        );
    }

    @Test
    void wrapIgnoresEdgesButRigidPlacementRequiresClearance() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        double edgeX = cx + 100.0D;
        double edgeY = cy + 100.0D;

        assertTrue(
                SPACE.allowsPlacement(
                        PlanetGenerationPlacementPolicy.WRAP,
                        edgeX,
                        edgeY,
                        cz,
                        1000.0D
                )
        );

        assertFalse(
                SPACE.allowsPlacement(
                        PlanetGenerationPlacementPolicy.AVOID_EDGE,
                        edgeX,
                        edgeY,
                        cz,
                        1.0D
                )
        );

        assertTrue(
                SPACE.allowsPlacement(
                        PlanetGenerationPlacementPolicy.AVOID_EDGE,
                        cx + 100.0D,
                        cy + 20.0D,
                        cz,
                        50.0D
                )
        );
    }

    @Test
    void signedSurfaceOffsetUsesSameCubicShellAsGravity() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        assertEquals(
                0.0D,
                SPACE.signedSurfaceOffset(
                        cx + 500.0D,
                        cy + 20.0D,
                        cz
                ),
                EPSILON
        );
        assertEquals(
                -400.0D,
                SPACE.signedSurfaceOffset(
                        cx + 100.0D,
                        cy,
                        cz
                ),
                EPSILON
        );
        assertEquals(
                250.0D,
                SPACE.signedSurfaceOffset(
                        cx,
                        cy - 750.0D,
                        cz
                ),
                EPSILON
        );
    }

    private static double square(double value) {
        return value * value;
    }
}
