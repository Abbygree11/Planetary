package dev.planetary.client.render;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetRenderTransformTest {
    private static final float EPSILON = 1.0E-6F;

    @Test
    void quaternionMapsCanonicalBasisToFrameBasis() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);
            Quaternionf rotation =
                    PlanetRenderTransform
                            .localToWorldQuaternion(face);

            assertVector(
                    frame.worldEast(),
                    rotation.transform(
                            new Vector3f(1.0F, 0.0F, 0.0F)
                    ),
                    face + " east"
            );
            assertVector(
                    frame.worldUp(),
                    rotation.transform(
                            new Vector3f(0.0F, 1.0F, 0.0F)
                    ),
                    face + " up"
            );
            assertVector(
                    frame.worldSouth(),
                    rotation.transform(
                            new Vector3f(0.0F, 0.0F, 1.0F)
                    ),
                    face + " south"
            );
        }
    }

    private static void assertVector(
            PlanetVector expected,
            Vector3f actual,
            String message
    ) {
        assertEquals(
                (float) expected.x(),
                actual.x,
                EPSILON,
                message + " x"
        );
        assertEquals(
                (float) expected.y(),
                actual.y,
                EPSILON,
                message + " y"
        );
        assertEquals(
                (float) expected.z(),
                actual.z,
                EPSILON,
                message + " z"
        );
    }
}
