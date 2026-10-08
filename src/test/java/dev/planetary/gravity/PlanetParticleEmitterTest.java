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
    void belowBlockRotatesVanillaBelowEmitterOnAllFaces() {
        double sampleX = 0.2D;
        double sampleZ = 0.8D;

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            assertVec(
                    PlanetParticleEmitter.belowBlock(
                            POS,
                            frame,
                            sampleX,
                            sampleZ
                    ),
                    expectedFromLocal(
                            frame,
                            sampleX - 0.5D,
                            -0.55D,
                            sampleZ - 0.5D
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

        assertVec(
                PlanetParticleEmitter.belowBlock(
                        POS,
                        frame,
                        0.2D,
                        0.8D
                ),
                new Vec3(
                        10.2D,
                        19.95D,
                        30.8D
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

    @Test
    void candleAndCandleCakeUnitOffsetsRotateAroundBlockCenter() {
        Vec3[] authoredUnitOffsets = {
                // Vanilla CandleBlock offsets for 1-4 candles.
                new Vec3(0.5D, 0.5D, 0.5D),
                new Vec3(0.375D, 0.44D, 0.5D),
                new Vec3(0.625D, 0.5D, 0.44D),
                new Vec3(0.5D, 0.313D, 0.625D),
                new Vec3(0.375D, 0.44D, 0.5D),
                new Vec3(0.56D, 0.5D, 0.44D),
                new Vec3(0.44D, 0.313D, 0.56D),
                new Vec3(0.625D, 0.44D, 0.56D),
                new Vec3(0.375D, 0.44D, 0.375D),
                new Vec3(0.56D, 0.5D, 0.375D),
                // CandleCakeBlock uses local top, exactly v=1.0.
                new Vec3(0.5D, 1.0D, 0.5D)
        };

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            for (Vec3 unitOffset : authoredUnitOffsets) {
                Vec3 converted =
                        PlanetParticleEmitter.rotateUnitBlockEmitterOffset(
                                unitOffset, frame
                        );

                // Vanilla lambda adds the integer block corner afterward.
                Vec3 world = new Vec3(
                        POS.getX() + converted.x,
                        POS.getY() + converted.y,
                        POS.getZ() + converted.z
                );
                assertVec(
                        world,
                        expectedFromLocal(
                                frame,
                                unitOffset.x - 0.5D,
                                unitOffset.y - 0.5D,
                                unitOffset.z - 0.5D
                        )
                );

                if (face == PlanetFace.POS_Y) {
                    assertVec(converted, unitOffset);
                }
            }
        }
    }

    @Test
    void candleExtinguishPuffUsesExactFloatDerivedLocalUp() {
        Vec3 vanillaPuff = new Vec3(
                0.0D,
                (double) 0.1F,
                0.0D
        );

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            Vec3 physical =
                    PlanetParticleMotion.localVelocityToWorld(
                            vanillaPuff, frame
                    );

            PlanetFrameVector recovered =
                    frame.worldToLocal(new PlanetFrameVector(
                            physical.x, physical.y, physical.z
                    ));

            assertVec(
                    new Vec3(
                            recovered.x(),
                            recovered.y(),
                            recovered.z()
                    ),
                    vanillaPuff
            );
        }
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
