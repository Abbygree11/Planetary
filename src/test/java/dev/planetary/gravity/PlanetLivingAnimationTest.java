package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetLivingAnimationTest {
    private static final double EPSILON = 1.0E-6D;

    @Test
    void worldVerticalMotionOnSideFaceBecomesHorizontalWalking() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        PlanetLivingAnimation.State state =
                PlanetLivingAnimation.state(
                        frame,
                        new Vec3(0.0D, -0.2D, 0.0D),
                        -90.0F,
                        -90.0F,
                        0.0F,
                        true
                );

        assertEquals(0.6F, state.animationStep(), EPSILON);
        assertEquals(1.0F, state.runTarget(), EPSILON);
        assertEquals(0.2D, state.localDisplacement().x(), EPSILON);
        assertEquals(0.0D, state.localDisplacement().y(), EPSILON);
    }

    @Test
    void walkDistanceUsesLocalFloorPlane() {
        float sideDistance =
                PlanetLivingAnimation.walkDistance(
                        new PlanetGravityFrame(
                                PlanetFace.POS_Z
                        ),
                        new Vec3(
                                0.0D,
                                -0.25D,
                                0.0D
                        ),
                        false
                );

        assertEquals(0.25F, sideDistance, EPSILON);
    }
}
