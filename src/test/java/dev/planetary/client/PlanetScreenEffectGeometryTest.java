package dev.planetary.client;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetScreenEffectGeometryTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void sideFaceEyeSamplesStayAroundLocalEyePlane() {
        Vec3 eye = new Vec3(
                10.0D,
                20.0D,
                30.0D
        );

        Vec3 sample =
                PlanetScreenEffectGeometry.sampleEyeCorner(
                        eye,
                        0.6D,
                        1.0D,
                        0,
                        new PlanetGravityFrame(
                                PlanetFace.POS_X
                        )
                );

        // POS_X local basis:
        // EAST=-Y, UP=+X, SOUTH=+Z.
        assertEquals(9.95D, sample.x, EPSILON);
        assertEquals(20.24D, sample.y, EPSILON);
        assertEquals(29.76D, sample.z, EPSILON);
    }

    @Test
    void bottomFaceEyeSamplesUseWorldYAsLocalVertical() {
        Vec3 eye = new Vec3(
                10.0D,
                20.0D,
                30.0D
        );

        Vec3 lower =
                PlanetScreenEffectGeometry.sampleEyeCorner(
                        eye,
                        0.6D,
                        1.0D,
                        0,
                        new PlanetGravityFrame(
                                PlanetFace.NEG_Y
                        )
                );

        // NEG_Y: local UP=-Y. A negative local-Y sample therefore moves
        // slightly toward world +Y, while X/Z remain local floor offsets.
        assertEquals(9.76D, lower.x, EPSILON);
        assertEquals(20.05D, lower.y, EPSILON);
        assertEquals(30.24D, lower.z, EPSILON);
    }
}
