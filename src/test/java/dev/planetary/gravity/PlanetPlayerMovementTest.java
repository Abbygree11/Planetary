package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetPlayerMovementTest {

    @Test
    void outwardMovementIsUpOnEveryGravityFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            var up = frame.worldUp();
            Vec3 outward = new Vec3(
                    up.x() * 0.2D,
                    up.y() * 0.2D,
                    up.z() * 0.2D
            );

            assertTrue(
                    PlanetPlayerMovement.isMovingUp(
                            outward,
                            frame,
                            1.0E-5D
                    ),
                    face.toString()
            );
        }
    }

    @Test
    void tangentialAndDownwardMovementAreNotJumpUp() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        assertFalse(
                PlanetPlayerMovement.isMovingUp(
                        new Vec3(0.0D, 0.2D, 0.0D),
                        frame,
                        1.0E-5D
                )
        );
        assertFalse(
                PlanetPlayerMovement.isMovingUp(
                        new Vec3(-0.2D, 0.0D, 0.0D),
                        frame,
                        1.0E-5D
                )
        );
    }
}
