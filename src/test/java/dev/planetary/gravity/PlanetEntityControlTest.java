package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetEntityControlTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void localUpInputOnPositiveXFaceBecomesWorldPositiveX() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        Vec3 world = PlanetEntityControl.relativeInputToWorld(
                new Vec3(0.0, 1.0, 0.0),
                1.0F,
                0.0F,
                frame
        );

        assertVec(world, 1.0, 0.0, 0.0);
    }

    @Test
    void forwardAndStrafeStayInTheLocalFloorPlane() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        Vec3 forward = PlanetEntityControl.relativeInputToWorld(
                new Vec3(0.0, 0.0, 1.0),
                1.0F,
                0.0F,
                frame
        );
        assertVec(forward, 0.0, 0.0, 1.0);

        Vec3 strafe = PlanetEntityControl.relativeInputToWorld(
                new Vec3(1.0, 0.0, 0.0),
                1.0F,
                0.0F,
                frame
        );
        assertVec(strafe, 0.0, -1.0, 0.0);
    }

    @Test
    void jumpReplacesOnlyLocalVerticalVelocity() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        Vec3 result = PlanetEntityControl.jumpVelocity(
                new Vec3(0.1, 0.3, 0.4),
                0.42F,
                0.0F,
                false,
                frame
        );

        assertVec(result, 0.42, 0.3, 0.4);
    }

    @Test
    void sprintJumpAddsImpulseAlongLocalForward() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        Vec3 result = PlanetEntityControl.jumpVelocity(
                Vec3.ZERO,
                0.42F,
                0.0F,
                true,
                frame
        );

        assertVec(result, 0.42, 0.0, 0.2);
    }

    @Test
    void normalTopFaceMatchesVanillaAxes() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_Y);

        Vec3 result = PlanetEntityControl.relativeInputToWorld(
                new Vec3(0.25, 0.5, 1.0),
                1.0F,
                0.0F,
                frame
        );

        double length = Math.sqrt(
                0.25 * 0.25
                        + 0.5 * 0.5
                        + 1.0
        );

        assertVec(
                result,
                0.25 / length,
                0.5 / length,
                1.0 / length
        );
    }

    private static void assertVec(
            Vec3 actual,
            double x,
            double y,
            double z
    ) {
        assertEquals(x, actual.x, EPSILON);
        assertEquals(y, actual.y, EPSILON);
        assertEquals(z, actual.z, EPSILON);
    }
}
