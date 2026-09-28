package dev.planetary.client;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetCameraRotationTest {
    private static final double EPSILON = 1.0E-5D;

    @Test
    void frameQuaternionMapsAllThreeLocalAxesOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);
            Quaternionf rotation =
                    PlanetCameraRotation.frameQuaternion(frame);

            assertVector(
                    transform(
                            rotation,
                            1.0F,
                            0.0F,
                            0.0F
                    ),
                    frame.localToWorld(
                            new PlanetFrameVector(
                                    1.0D,
                                    0.0D,
                                    0.0D
                            )
                    ),
                    face + " EAST"
            );
            assertVector(
                    transform(
                            rotation,
                            0.0F,
                            1.0F,
                            0.0F
                    ),
                    frame.localToWorld(
                            new PlanetFrameVector(
                                    0.0D,
                                    1.0D,
                                    0.0D
                            )
                    ),
                    face + " UP"
            );
            assertVector(
                    transform(
                            rotation,
                            0.0F,
                            0.0F,
                            1.0F
                    ),
                    frame.localToWorld(
                            new PlanetFrameVector(
                                    0.0D,
                                    0.0D,
                                    1.0D
                            )
                    ),
                    face + " SOUTH"
            );
        }
    }

    @Test
    void yawZeroLooksAlongLocalSouthOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Quaternionf camera =
                    PlanetCameraRotation.cameraQuaternion(
                            0.0F,
                            0.0F,
                            0.0F,
                            frame
                    );

            Vector3f forward =
                    transform(
                            camera,
                            0.0F,
                            0.0F,
                            -1.0F
                    );

            assertVector(
                    forward,
                    frame.localToWorld(
                            new PlanetFrameVector(
                                    0.0D,
                                    0.0D,
                                    1.0D
                            )
                    ),
                    face + " forward"
            );
        }
    }

    @Test
    void cameraUpAlwaysMatchesLocalUpAtZeroPitchAndRoll() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Quaternionf camera =
                    PlanetCameraRotation.cameraQuaternion(
                            37.0F,
                            0.0F,
                            0.0F,
                            frame
                    );

            Vector3f up =
                    transform(
                            camera,
                            0.0F,
                            1.0F,
                            0.0F
                    );

            assertVector(
                    up,
                    frame.localToWorld(
                            new PlanetFrameVector(
                                    0.0D,
                                    1.0D,
                                    0.0D
                            )
                    ),
                    face + " camera up"
            );
        }
    }

    private static Vector3f transform(
            Quaternionf rotation,
            float x,
            float y,
            float z
    ) {
        return rotation.transform(
                new Vector3f(x, y, z)
        );
    }

    private static void assertVector(
            Vector3f actual,
            PlanetFrameVector expected,
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
