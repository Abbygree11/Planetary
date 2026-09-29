package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetEntityCollisionTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void localVerticalCollisionCanUseWorldX() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        AABB entity = new AABB(
                0.0, 0.0, 0.0,
                1.0, 1.0, 1.0
        );

        Vec3 requested = new Vec3(2.0, 0.0, 0.0);
        Vec3 actual = PlanetEntityCollision.collideWithShapes(
                requested,
                entity,
                List.of(
                        Shapes.create(
                                new AABB(
                                        2.0, 0.0, 0.0,
                                        3.0, 1.0, 1.0
                                )
                        )
                ),
                frame
        );

        assertEquals(1.0, actual.x, EPSILON);
        assertEquals(0.0, actual.y, EPSILON);
        assertEquals(0.0, actual.z, EPSILON);

        PlanetEntityMotion.CollisionResult result =
                PlanetEntityMotion.classify(
                        requested,
                        actual,
                        frame
                );

        assertFalse(result.horizontalCollision());
        assertTrue(result.verticalCollision());
        assertFalse(result.verticalCollisionBelow());
    }

    @Test
    void collisionTowardCoreCountsAsGround() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        PlanetEntityMotion.CollisionResult result =
                PlanetEntityMotion.classify(
                        new Vec3(-0.3, 0.0, 0.0),
                        Vec3.ZERO,
                        frame
                );

        assertTrue(result.verticalCollision());
        assertTrue(result.verticalCollisionBelow());
        assertFalse(result.horizontalCollision());
    }

    @Test
    void collisionTowardCoreCountsAsGroundOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);
            var down = frame.worldDown();

            Vec3 requested = new Vec3(
                    down.x() * 0.3D,
                    down.y() * 0.3D,
                    down.z() * 0.3D
            );

            PlanetEntityMotion.CollisionResult result =
                    PlanetEntityMotion.classify(
                            requested,
                            Vec3.ZERO,
                            frame
                    );

            assertTrue(
                    result.verticalCollision(),
                    face.toString()
            );
            assertTrue(
                    result.verticalCollisionBelow(),
                    face.toString()
            );
            assertFalse(
                    result.horizontalCollision(),
                    face.toString()
            );
        }
    }

    @Test
    void tangentialCollisionStaysHorizontalInLocalFrame() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        PlanetEntityMotion.CollisionResult result =
                PlanetEntityMotion.classify(
                        new Vec3(0.0, -0.4, 0.0),
                        Vec3.ZERO,
                        frame
                );

        assertTrue(result.horizontalCollision());
        assertFalse(result.verticalCollision());
        assertFalse(result.verticalCollisionBelow());
    }
}
