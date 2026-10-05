package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static void assertVec(
            Vec3 actual,
            Vec3 expected
    ) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
