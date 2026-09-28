package dev.planetary.topology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetGravityFrameTest {
    private static final double EPSILON = 1.0e-12;

    @Test
    void arbitraryVectorsRoundTripThroughEveryGravityFrame() {
        PlanetFrameVector[] samples = {
                new PlanetFrameVector(0.0, 0.0, 0.0),
                new PlanetFrameVector(1.0, 0.0, 0.0),
                new PlanetFrameVector(0.0, 1.0, 0.0),
                new PlanetFrameVector(0.0, 0.0, 1.0),
                new PlanetFrameVector(2.5, -7.25, 0.125)
        };

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            for (PlanetFrameVector local : samples) {
                PlanetFrameVector world =
                        frame.localToWorld(local);
                PlanetFrameVector roundTrip =
                        frame.worldToLocal(world);

                assertVectorEquals(
                        local,
                        roundTrip,
                        face + " / " + local
                );
            }
        }
    }

    @Test
    void allSixLocalDirectionsRoundTripOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            for (PlanetDirection local
                    : PlanetDirection.values()) {
                PlanetVector world = frame.worldAxis(local);
                assertEquals(
                        local,
                        frame.localDirectionOf(world),
                        face + " / " + local
                );
            }
        }
    }

    @Test
    void changingFramePreservesPhysicalWorldVector() {
        PlanetFrameVector localMotion =
                new PlanetFrameVector(
                        0.37,
                        0.81,
                        -1.42
                );

        for (PlanetFace sourceFace : PlanetFace.values()) {
            PlanetGravityFrame source =
                    new PlanetGravityFrame(sourceFace);

            for (PlanetFace targetFace : PlanetFace.values()) {
                PlanetGravityFrame target =
                        new PlanetGravityFrame(targetFace);

                PlanetFrameVector targetLocal =
                        source.transformLocalTo(
                                target,
                                localMotion
                        );

                assertVectorEquals(
                        source.localToWorld(localMotion),
                        target.localToWorld(targetLocal),
                        sourceFace + " -> " + targetFace
                );
            }
        }
    }

    @Test
    void frameDownAlwaysMatchesGravityField() {
        PlanetGravityField field =
                new PlanetGravityField(
                        new PlanetCore(0, 0, 0, 500)
                );

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            assertEquals(
                    field.gravityDirection(face),
                    frame.worldDown()
            );
            assertEquals(
                    field.localUp(face),
                    frame.worldUp()
            );
        }
    }

    private static void assertVectorEquals(
            PlanetFrameVector expected,
            PlanetFrameVector actual,
            String message
    ) {
        assertEquals(
                expected.x(),
                actual.x(),
                EPSILON,
                message + " x"
        );
        assertEquals(
                expected.y(),
                actual.y(),
                EPSILON,
                message + " y"
        );
        assertEquals(
                expected.z(),
                actual.z(),
                EPSILON,
                message + " z"
        );
    }
}
