package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetParticleMotionTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void initialBiasMovesFromWorldUpToLocalUp() {
        Vec3 vanilla =
                new Vec3(
                        0.2D,
                        0.4D,
                        -0.3D
                );

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 actual =
                    PlanetParticleMotion.rotateVanillaUpBias(
                            vanilla,
                            frame
                    );

            Vec3 expected =
                    new Vec3(
                            vanilla.x,
                            vanilla.y
                                    - PlanetParticleMotion.VANILLA_UP_BIAS,
                            vanilla.z
                    ).add(
                            frame.worldUp().x()
                                    * PlanetParticleMotion.VANILLA_UP_BIAS,
                            frame.worldUp().y()
                                    * PlanetParticleMotion.VANILLA_UP_BIAS,
                            frame.worldUp().z()
                                    * PlanetParticleMotion.VANILLA_UP_BIAS
                    );

            assertVec(actual, expected);
        }
    }

    @Test
    void powerScalesAroundLocalUpBias() {
        Vec3 velocity =
                new Vec3(
                        0.4D,
                        -0.2D,
                        0.7D
                );
        float power = 0.2F;

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 bias =
                    new Vec3(
                            frame.worldUp().x()
                                    * PlanetParticleMotion.VANILLA_UP_BIAS,
                            frame.worldUp().y()
                                    * PlanetParticleMotion.VANILLA_UP_BIAS,
                            frame.worldUp().z()
                                    * PlanetParticleMotion.VANILLA_UP_BIAS
                    );

            Vec3 expected =
                    bias.add(
                            velocity.subtract(bias)
                                    .scale(power)
                    );

            assertVec(
                    PlanetParticleMotion.scaleAroundLocalUpBias(
                            velocity,
                            power,
                            frame
                    ),
                    expected
            );
        }
    }

    @Test
    void localDownCollisionMeansGroundOnAllFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 requested =
                    worldVector(
                            frame,
                            0.0D,
                            -0.25D,
                            0.0D
                    );

            PlanetParticleMotion.CollisionSemantics actual =
                    PlanetParticleMotion.collisionSemantics(
                            requested,
                            Vec3.ZERO,
                            requested,
                            frame
                    );

            assertTrue(actual.onGround(), face.name());
            assertTrue(actual.stoppedByCollision(), face.name());

            PlanetFrameVector velocityLocal =
                    frame.worldToLocal(
                            new PlanetFrameVector(
                                    actual.correctedWorldVelocity().x,
                                    actual.correctedWorldVelocity().y,
                                    actual.correctedWorldVelocity().z
                            )
                    );

            assertEquals(
                    -0.25D,
                    velocityLocal.y(),
                    EPSILON,
                    face.name()
            );
        }
    }

    @Test
    void localUpCollisionIsVerticalButNotGround() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 requested =
                    worldVector(
                            frame,
                            0.0D,
                            0.2D,
                            0.0D
                    );

            PlanetParticleMotion.CollisionSemantics actual =
                    PlanetParticleMotion.collisionSemantics(
                            requested,
                            Vec3.ZERO,
                            requested,
                            frame
                    );

            assertFalse(actual.onGround(), face.name());
            assertTrue(actual.stoppedByCollision(), face.name());
        }
    }

    @Test
    void tangentCollisionZerosOnlyMatchingLocalVelocity() {
        Vec3 localVelocity =
                new Vec3(
                        0.4D,
                        -0.3D,
                        0.6D
                );

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 requested =
                    worldVector(
                            frame,
                            localVelocity.x,
                            localVelocity.y,
                            localVelocity.z
                    );

            Vec3 actualMovement =
                    worldVector(
                            frame,
                            0.0D,
                            localVelocity.y,
                            localVelocity.z
                    );

            PlanetParticleMotion.CollisionSemantics semantics =
                    PlanetParticleMotion.collisionSemantics(
                            requested,
                            actualMovement,
                            requested,
                            frame
                    );

            PlanetFrameVector corrected =
                    frame.worldToLocal(
                            new PlanetFrameVector(
                                    semantics.correctedWorldVelocity().x,
                                    semantics.correctedWorldVelocity().y,
                                    semantics.correctedWorldVelocity().z
                            )
                    );

            assertEquals(
                    0.0D,
                    corrected.x(),
                    EPSILON,
                    face.name()
            );
            assertEquals(
                    localVelocity.y,
                    corrected.y(),
                    EPSILON,
                    face.name()
            );
            assertEquals(
                    localVelocity.z,
                    corrected.z(),
                    EPSILON,
                    face.name()
            );
        }
    }

    @Test
    void groundFrictionAppliesToLocalTangentsOnly() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_X
                );

        Vec3 neutral =
                worldVector(
                        frame,
                        0.5D,
                        -0.4D,
                        0.25D
                );

        // Simulate vanilla's wrong physical-X/Z ground multiplier first.
        Vec3 vanillaAfterGround =
                new Vec3(
                        neutral.x * 0.7D,
                        neutral.y,
                        neutral.z * 0.7D
                );

        Vec3 corrected =
                PlanetParticleMotion.correctBaseTickAxisEffects(
                        vanillaAfterGround,
                        frame,
                        false,
                        false,
                        true
                );

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                corrected.x,
                                corrected.y,
                                corrected.z
                        )
                );

        assertEquals(0.5D * 0.7D, local.x(), EPSILON);
        assertEquals(-0.4D, local.y(), EPSILON);
        assertEquals(0.25D * 0.7D, local.z(), EPSILON);
    }

    @Test
    void blockedVerticalSpeedUpMovesFromWorldTangentsToLocalTangents() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_Z
                );

        Vec3 neutral =
                worldVector(
                        frame,
                        0.3D,
                        0.2D,
                        -0.5D
                );

        Vec3 vanillaAfterSpeedUp =
                new Vec3(
                        neutral.x * 1.1D,
                        neutral.y,
                        neutral.z * 1.1D
                );

        Vec3 corrected =
                PlanetParticleMotion.correctBaseTickAxisEffects(
                        vanillaAfterSpeedUp,
                        frame,
                        true,
                        true,
                        false
                );

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                corrected.x,
                                corrected.y,
                                corrected.z
                        )
                );

        assertEquals(0.3D * 1.1D, local.x(), EPSILON);
        assertEquals(0.2D, local.y(), EPSILON);
        assertEquals(-0.5D * 1.1D, local.z(), EPSILON);
    }

    @Test
    void localVerticalDisplacementCheckUsesFrameAxis() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_X
                );

        assertTrue(
                PlanetParticleMotion.hasNoLocalVerticalDisplacement(
                        worldVector(
                                frame,
                                0.25D,
                                0.0D,
                                -0.4D
                        ),
                        frame
                )
        );

        assertFalse(
                PlanetParticleMotion.hasNoLocalVerticalDisplacement(
                        worldVector(
                                frame,
                                0.0D,
                                0.1D,
                                0.0D
                        ),
                        frame
                )
        );
    }

    @Test
    void positiveYMatchesVanillaExactly() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_Y
                );
        Vec3 velocity =
                new Vec3(
                        0.25D,
                        0.45D,
                        -0.5D
                );

        assertVec(
                PlanetParticleMotion.rotateVanillaUpBias(
                        velocity,
                        frame
                ),
                velocity
        );

        float power = 0.2F;
        assertVec(
                PlanetParticleMotion.scaleAroundLocalUpBias(
                        velocity,
                        power,
                        frame
                ),
                new Vec3(
                        velocity.x * power,
                        (velocity.y - 0.1D) * power + 0.1D,
                        velocity.z * power
                )
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
            Vec3 actual,
            Vec3 expected
    ) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
