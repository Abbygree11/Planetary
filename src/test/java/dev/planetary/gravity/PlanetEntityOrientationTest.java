package dev.planetary.gravity;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetEntityOrientationTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void everyDirectedCubeEdgeHasAnOrientationTransition() {
        int count = 0;

        for (PlanetFace source : PlanetFace.values()) {
            for (PlanetDirection edge
                    : PlanetDirection.values()) {
                if (!edge.isHorizontal()) {
                    continue;
                }

                PlanetFace target =
                        source.neighborAcross(edge);

                var transition =
                        PlanetEntityOrientation.transition(
                                source,
                                target
                        );

                assertTrue(
                        transition.isPresent(),
                        source + " -> " + target
                );
                assertEquals(
                        source,
                        transition.orElseThrow().sourceFace()
                );
                assertEquals(
                        target,
                        transition.orElseThrow().targetFace()
                );
                count++;
            }
        }

        assertEquals(24, count);
    }

    @Test
    void yawTransportMatchesFaceTransformOnAllEdges() {
        float[] yaws = {
                0.0F,
                90.0F,
                -90.0F,
                180.0F,
                37.5F,
                -123.25F
        };

        for (PlanetFace source : PlanetFace.values()) {
            for (PlanetDirection edge
                    : PlanetDirection.values()) {
                if (!edge.isHorizontal()) {
                    continue;
                }

                FaceTransform transform =
                        FaceTransform.across(source, edge);
                PlanetFace target =
                        transform.targetFace();

                for (float yaw : yaws) {
                    float transportedYaw =
                            PlanetEntityOrientation
                                    .transportYaw(
                                            source,
                                            target,
                                            yaw
                                    )
                                    .orElseThrow();

                    PlanetFrameVector expected =
                            transform.transformVector(
                                    heading(yaw)
                            );
                    PlanetFrameVector actual =
                            heading(transportedYaw);

                    assertEquals(
                            expected.x(),
                            actual.x(),
                            EPSILON,
                            source + ":" + edge
                                    + " yaw=" + yaw + " x"
                    );
                    assertEquals(
                            expected.z(),
                            actual.z(),
                            EPSILON,
                            source + ":" + edge
                                    + " yaw=" + yaw + " z"
                    );
                    assertEquals(
                            0.0D,
                            actual.y(),
                            EPSILON
                    );
                }
            }
        }
    }

    @Test
    void knownPreviouslyInvertedTransitionsAreTransported() {
        float topNorthYaw = 180.0F;
        float onNegativeZ =
                PlanetEntityOrientation.transportYaw(
                        PlanetFace.POS_Y,
                        PlanetFace.NEG_Z,
                        topNorthYaw
                ).orElseThrow();

        assertHeadingEquals(
                FaceTransform.across(
                        PlanetFace.POS_Y,
                        PlanetDirection.NORTH
                ).transformVector(
                        heading(topNorthYaw)
                ),
                heading(onNegativeZ)
        );

        FaceTransform toBottom =
                FaceTransform.across(
                        PlanetFace.POS_X,
                        PlanetDirection.EAST
                );

        float sideEastYaw = -90.0F;
        float onBottom =
                PlanetEntityOrientation.transportYaw(
                        PlanetFace.POS_X,
                        PlanetFace.NEG_Y,
                        sideEastYaw
                ).orElseThrow();

        assertHeadingEquals(
                toBottom.transformVector(
                        heading(sideEastYaw)
                ),
                heading(onBottom)
        );
    }

    private static PlanetFrameVector heading(float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        return new PlanetFrameVector(
                -Math.sin(yaw),
                0.0D,
                Math.cos(yaw)
        );
    }

    private static void assertHeadingEquals(
            PlanetFrameVector expected,
            PlanetFrameVector actual
    ) {
        assertEquals(
                expected.x(),
                actual.x(),
                EPSILON
        );
        assertEquals(
                expected.y(),
                actual.y(),
                EPSILON
        );
        assertEquals(
                expected.z(),
                actual.z(),
                EPSILON
        );
    }
}
