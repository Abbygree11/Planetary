package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class PlanetParticleEmitterTest {
    private static final double EPSILON = 1.0E-9D;
    private static final BlockPos POS =
            new BlockPos(10, 20, 30);

    @Test
    void standingTorchUsesLocalUpOnAllFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Vec3 expected =
                    expectedFromLocal(
                            frame,
                            0.0D,
                            0.2D,
                            0.0D
                    );

            assertVec(
                    PlanetParticleEmitter.standingTorch(
                            POS,
                            frame
                    ),
                    expected
            );
        }
    }

    @Test
    void wallTorchKeepsFullVanillaLocalOffsetsOnAllFaces() {
        Direction[] horizontal = {
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            for (Direction localFacing : horizontal) {
                Direction support =
                        localFacing.getOpposite();

                Vec3 expected =
                        expectedFromLocal(
                                frame,
                                support.getStepX() * 0.27D,
                                0.42D,
                                support.getStepZ() * 0.27D
                        );

                assertVec(
                        PlanetParticleEmitter.wallTorch(
                                POS,
                                frame,
                                localFacing
                        ),
                        expected
                );
            }
        }
    }

    @Test
    void transformedVanillaEmitterPreservesExactSampledLocalOffsets() {
        // Representative redstone-wall-torch sample:
        // random jitter + support offset + full y+0.7+0.22 rise.
        double localX = -0.27D + 0.06D;
        double localY = 0.42D - 0.04D;
        double localZ = 0.08D;

        double vanillaX =
                POS.getX() + 0.5D + localX;
        double vanillaY =
                POS.getY() + 0.5D + localY;
        double vanillaZ =
                POS.getZ() + 0.5D + localZ;

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            assertVec(
                    PlanetParticleEmitter.transformVanillaLocalEmitter(
                            POS,
                            frame,
                            vanillaX,
                            vanillaY,
                            vanillaZ
                    ),
                    expectedFromLocal(
                            frame,
                            localX,
                            localY,
                            localZ
                    )
            );
        }
    }

    @Test
    void positiveYMatchesVanillaEmitterCoordinatesExactly() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_Y
                );

        assertVec(
                PlanetParticleEmitter.standingTorch(
                        POS,
                        frame
                ),
                new Vec3(
                        10.5D,
                        20.7D,
                        30.5D
                )
        );

        assertVec(
                PlanetParticleEmitter.wallTorch(
                        POS,
                        frame,
                        Direction.EAST
                ),
                new Vec3(
                        10.23D,
                        20.92D,
                        30.5D
                )
        );
    }

    @Test
    void wallTorchRejectsVerticalFacing() {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_X
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> PlanetParticleEmitter.wallTorch(
                        POS,
                        frame,
                        Direction.UP
                )
        );
    }

    private static Vec3 expectedFromLocal(
            PlanetGravityFrame frame,
            double x,
            double y,
            double z
    ) {
        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                x,
                                y,
                                z
                        )
                );

        return Vec3.atCenterOf(POS).add(
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
