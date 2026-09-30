package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetPathGeometryTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void sameFaceUsesNormalFeetAnchor() {
        Vec3 anchor =
                PlanetPathGeometry.nodeAnchor(
                        10,
                        20,
                        30,
                        new PlanetGravityFrame(
                                PlanetFace.POS_Y
                        ),
                        PlanetFace.POS_Y
                );

        assertEquals(10.5D, anchor.x, EPSILON);
        assertEquals(20.0D, anchor.y, EPSILON);
        assertEquals(30.5D, anchor.z, EPSILON);
    }

    @Test
    void nextFaceGetsTemporaryOutwardOvershoot() {
        Vec3 anchor =
                PlanetPathGeometry.nodeAnchor(
                        49,
                        176,
                        0,
                        new PlanetGravityFrame(
                                PlanetFace.POS_X
                        ),
                        PlanetFace.POS_Y
                );

        assertEquals(
                49.0D
                        + PlanetPathGeometry
                                .EDGE_CROSSING_OVERSHOOT,
                anchor.x,
                EPSILON
        );
        assertEquals(176.5D, anchor.y, EPSILON);
        assertEquals(0.5D, anchor.z, EPSILON);
    }
}
