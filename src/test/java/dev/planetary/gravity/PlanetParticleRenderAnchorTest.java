package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetParticleRenderAnchorTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void localDownFaceOffsetMatchesVanillaAnchorOnAllFaces() {
        AABB box =
                new AABB(
                        9.9D,
                        20.0D,
                        29.9D,
                        10.1D,
                        20.2D,
                        30.1D
                );

        Map<PlanetFace, Vec3> expected =
                Map.of(
                        PlanetFace.POS_Y,
                        Vec3.ZERO,
                        PlanetFace.NEG_Y,
                        new Vec3(0.0D, 0.2D, 0.0D),
                        PlanetFace.POS_X,
                        new Vec3(-0.1D, 0.1D, 0.0D),
                        PlanetFace.NEG_X,
                        new Vec3(0.1D, 0.1D, 0.0D),
                        PlanetFace.POS_Z,
                        new Vec3(0.0D, 0.1D, -0.1D),
                        PlanetFace.NEG_Z,
                        new Vec3(0.0D, 0.1D, 0.1D)
                );

        for (PlanetFace face : PlanetFace.values()) {
            Vec3 actual =
                    PlanetParticleRenderAnchor.localDownFaceOffset(
                            box,
                            new PlanetGravityFrame(face)
                    );

            assertVec(
                    expected.get(face),
                    actual,
                    face.name()
            );
        }
    }

    @Test
    void usesActualAabbHalfExtentAlongLocalDownAxis() {
        AABB box =
                new AABB(
                        1.0D,
                        2.0D,
                        3.0D,
                        1.4D,
                        2.6D,
                        3.8D
                );

        assertVec(
                new Vec3(0.2D, 0.3D, 0.0D),
                PlanetParticleRenderAnchor.localDownFaceOffset(
                        box,
                        new PlanetGravityFrame(
                                PlanetFace.NEG_X
                        )
                ),
                "NEG_X"
        );

        assertVec(
                new Vec3(0.0D, 0.6D, 0.0D),
                PlanetParticleRenderAnchor.localDownFaceOffset(
                        box,
                        new PlanetGravityFrame(
                                PlanetFace.NEG_Y
                        )
                ),
                "NEG_Y"
        );

        assertVec(
                new Vec3(0.0D, 0.3D, -0.4D),
                PlanetParticleRenderAnchor.localDownFaceOffset(
                        box,
                        new PlanetGravityFrame(
                                PlanetFace.POS_Z
                        )
                ),
                "POS_Z"
        );
    }

    private static void assertVec(
            Vec3 expected,
            Vec3 actual,
            String message
    ) {
        assertEquals(expected.x, actual.x, EPSILON, message + " x");
        assertEquals(expected.y, actual.y, EPSILON, message + " y");
        assertEquals(expected.z, actual.z, EPSILON, message + " z");
    }
}
