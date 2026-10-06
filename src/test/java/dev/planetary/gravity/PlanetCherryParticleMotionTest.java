package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetCherryParticleMotionTest {
    private static final double EPSILON = 1.0E-12D;

    @Test
    void windLivesOnlyInLocalTangentPlaneOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 world =
                    PlanetCherryParticleMotion.windAcceleration(
                            frame,
                            0.37F,
                            240
                    );

            PlanetFrameVector local =
                    frame.worldToLocal(
                            new PlanetFrameVector(
                                    world.x,
                                    world.y,
                                    world.z
                            )
                    );

            assertEquals(
                    0.0D,
                    local.y(),
                    EPSILON,
                    face.name()
            );
        }
    }

    @Test
    void positiveYWindMatchesVanillaXZExactly() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_Y
                );
        float random = 0.37F;
        int lifetime = 240;

        float elapsed = 300.0F - lifetime;
        float normalized =
                Math.min(
                        elapsed / 300.0F,
                        1.0F
                );

        double expectedX =
                Math.cos(
                        Math.toRadians(
                                (double) (
                                        random * 60.0F
                                )
                        )
                )
                        * 2.0D
                        * Math.pow(
                                (double) normalized,
                                1.25D
                        )
                        * (double) 0.0025F;

        double expectedZ =
                Math.sin(
                        Math.toRadians(
                                (double) (
                                        random * 60.0F
                                )
                        )
                )
                        * 2.0D
                        * Math.pow(
                                (double) normalized,
                                1.25D
                        )
                        * (double) 0.0025F;

        Vec3 actual =
                PlanetCherryParticleMotion.windAcceleration(
                        frame,
                        random,
                        lifetime
                );

        assertEquals(expectedX, actual.x, EPSILON);
        assertEquals(0.0D, actual.y, EPSILON);
        assertEquals(expectedZ, actual.z, EPSILON);
    }

    @Test
    void stopCheckUsesLocalTangentsOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 localXBlocked =
                    worldVector(
                            frame,
                            0.0D,
                            -0.1D,
                            0.2D
                    );
            Vec3 localZBlocked =
                    worldVector(
                            frame,
                            0.2D,
                            -0.1D,
                            0.0D
                    );
            Vec3 free =
                    worldVector(
                            frame,
                            0.2D,
                            -0.1D,
                            0.3D
                    );

            assertTrue(
                    PlanetCherryParticleMotion.shouldRemove(
                            frame,
                            false,
                            250,
                            localXBlocked
                    ),
                    face.name() + " local X"
            );
            assertTrue(
                    PlanetCherryParticleMotion.shouldRemove(
                            frame,
                            false,
                            250,
                            localZBlocked
                    ),
                    face.name() + " local Z"
            );
            assertFalse(
                    PlanetCherryParticleMotion.shouldRemove(
                            frame,
                            false,
                            250,
                            free
                    ),
                    face.name() + " free"
            );
        }
    }

    @Test
    void groundAlwaysRemovesAndFirstTickIgnoresZeroTangents() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.NEG_X
                );

        assertTrue(
                PlanetCherryParticleMotion.shouldRemove(
                        frame,
                        true,
                        299,
                        Vec3.ZERO
                )
        );

        assertFalse(
                PlanetCherryParticleMotion.shouldRemove(
                        frame,
                        false,
                        299,
                        Vec3.ZERO
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
}
