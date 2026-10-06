package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetParticleCollisionResponseTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void fullyBlockedLocalDownMatchesVanillaFloorResponseOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 requested =
                    worldVector(
                            frame,
                            0.15D,
                            -0.08D,
                            -0.2D
                    );
            Vec3 actual =
                    worldVector(
                            frame,
                            0.15D,
                            0.0D,
                            -0.2D
                    );
            Vec3 velocity =
                    worldVector(
                            frame,
                            0.3D,
                            -0.4D,
                            0.5D
                    );

            PlanetParticleCollisionResponse.Result result =
                    PlanetParticleCollisionResponse.apply(
                            requested,
                            actual,
                            velocity,
                            frame
                    );

            assertTrue(
                    result.stoppedByCollision(),
                    face.name()
            );
            assertTrue(
                    result.onGround(),
                    face.name()
            );

            assertLocalVelocity(
                    frame,
                    result.correctedWorldVelocity(),
                    0.3D,
                    -0.4D,
                    0.5D,
                    face.name()
            );
        }
    }

    @Test
    void localHeadCollisionStopsButIsNotGround() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            PlanetParticleCollisionResponse.Result result =
                    PlanetParticleCollisionResponse.apply(
                            worldVector(
                                    frame,
                                    0.0D,
                                    0.08D,
                                    0.0D
                            ),
                            Vec3.ZERO,
                            Vec3.ZERO,
                            frame
                    );

            assertTrue(
                    result.stoppedByCollision(),
                    face.name()
            );
            assertFalse(
                    result.onGround(),
                    face.name()
            );
        }
    }

    @Test
    void tangentCollisionZerosOnlyMatchingLocalTangentVelocity() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 requested =
                    worldVector(
                            frame,
                            0.2D,
                            -0.05D,
                            0.3D
                    );
            Vec3 actual =
                    worldVector(
                            frame,
                            0.0D,
                            -0.05D,
                            0.3D
                    );
            Vec3 velocity =
                    worldVector(
                            frame,
                            0.7D,
                            -0.4D,
                            0.6D
                    );

            PlanetParticleCollisionResponse.Result result =
                    PlanetParticleCollisionResponse.apply(
                            requested,
                            actual,
                            velocity,
                            frame
                    );

            assertFalse(
                    result.stoppedByCollision(),
                    face.name()
            );
            assertFalse(
                    result.onGround(),
                    face.name()
            );

            assertLocalVelocity(
                    frame,
                    result.correctedWorldVelocity(),
                    0.0D,
                    -0.4D,
                    0.6D,
                    face.name()
            );
        }
    }

    @Test
    void unobstructedMotionLeavesFlagsAndVelocityUntouched() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 movement =
                    worldVector(
                            frame,
                            -0.1D,
                            -0.04D,
                            0.2D
                    );
            Vec3 velocity =
                    worldVector(
                            frame,
                            0.3D,
                            -0.2D,
                            -0.6D
                    );

            PlanetParticleCollisionResponse.Result result =
                    PlanetParticleCollisionResponse.apply(
                            movement,
                            movement,
                            velocity,
                            frame
                    );

            assertFalse(
                    result.stoppedByCollision(),
                    face.name()
            );
            assertFalse(
                    result.onGround(),
                    face.name()
            );
            assertVec(
                    velocity,
                    result.correctedWorldVelocity(),
                    face.name()
            );
        }
    }

    private static void assertLocalVelocity(
            PlanetGravityFrame frame,
            Vec3 world,
            double expectedX,
            double expectedY,
            double expectedZ,
            String message
    ) {
        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                world.x,
                                world.y,
                                world.z
                        )
                );

        assertEquals(
                expectedX,
                local.x(),
                EPSILON,
                message + " local x"
        );
        assertEquals(
                expectedY,
                local.y(),
                EPSILON,
                message + " local y"
        );
        assertEquals(
                expectedZ,
                local.z(),
                EPSILON,
                message + " local z"
        );
    }

    private static Vec3 worldVector(
            PlanetGravityFrame frame,
            double localX,
            double localY,
            double localZ
    ) {
        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localX,
                                localY,
                                localZ
                        )
                );

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
    }

    private static void assertVec(
            Vec3 expected,
            Vec3 actual,
            String message
    ) {
        assertEquals(
                expected.x,
                actual.x,
                EPSILON,
                message + " x"
        );
        assertEquals(
                expected.y,
                actual.y,
                EPSILON,
                message + " y"
        );
        assertEquals(
                expected.z,
                actual.z,
                EPSILON,
                message + " z"
        );
    }
}
