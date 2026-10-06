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
    void identicalRequestedAndActualMovementIsNotGround() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 movement =
                    worldVector(
                            frame,
                            -0.08D,
                            -0.02D,
                            0.03D
                    );

            assertFalse(
                    PlanetParticleMotion.isLocalGroundCollision(
                            movement,
                            movement,
                            frame
                    ),
                    face.name()
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
                            0.15D,
                            -0.25D,
                            -0.2D
                    );
            Vec3 actual =
                    worldVector(
                            frame,
                            0.15D,
                            0.0D,
                            -0.2D
                    );

            assertTrue(
                    PlanetParticleMotion.isLocalGroundCollision(
                            requested,
                            actual,
                            frame
                    ),
                    face.name()
            );
        }
    }

    @Test
    void localUpOrTangentCollisionIsNotGround() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 upRequested =
                    worldVector(
                            frame,
                            0.0D,
                            0.2D,
                            0.0D
                    );

            assertFalse(
                    PlanetParticleMotion.isLocalGroundCollision(
                            upRequested,
                            Vec3.ZERO,
                            frame
                    ),
                    face.name() + " local UP"
            );

            Vec3 tangentRequested =
                    worldVector(
                            frame,
                            0.2D,
                            -0.1D,
                            0.3D
                    );
            Vec3 tangentActual =
                    worldVector(
                            frame,
                            0.0D,
                            -0.1D,
                            0.3D
                    );

            assertFalse(
                    PlanetParticleMotion.isLocalGroundCollision(
                            tangentRequested,
                            tangentActual,
                            frame
                    ),
                    face.name() + " tangent"
            );
        }
    }

    @Test
    void groundFrictionMovesFromPhysicalXZToLocalTangents() {
        double friction =
                (double) 0.7F;

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 beforeGround =
                    worldVector(
                            frame,
                            0.5D,
                            -0.4D,
                            0.25D
                    );

            Vec3 vanillaAfterGround =
                    new Vec3(
                            beforeGround.x * friction,
                            beforeGround.y,
                            beforeGround.z * friction
                    );

            Vec3 corrected =
                    PlanetParticleMotion.correctGroundFriction(
                            vanillaAfterGround,
                            frame
                    );

            PlanetFrameVector local =
                    frame.worldToLocal(
                            new PlanetFrameVector(
                                    corrected.x,
                                    corrected.y,
                                    corrected.z
                            )
                    );

            assertEquals(
                    0.5D * friction,
                    local.x(),
                    EPSILON,
                    face.name()
            );
            assertEquals(
                    -0.4D,
                    local.y(),
                    EPSILON,
                    face.name()
            );
            assertEquals(
                    0.25D * friction,
                    local.z(),
                    EPSILON,
                    face.name()
            );
        }
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
