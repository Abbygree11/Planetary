package dev.planetary.world;

import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Non-rendering geometric contracts for the shared enchanting-table/Spore
 * source chart. Vanilla +Y positions and motion must be unchanged.
 */
final class VolumeParticleFrameTest {
    private static final PlanetCore CORE =
            new PlanetCore(3, 100, -7, 20);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);
    private static final double EPS = 1.0e-9D;

    @Test
    void bookshelfSourceAndAuthoredVelocityAreLocalOnAllSixFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            BlockPos table = physical(face, 20, 2, -3);
            Vec3 samplePosition = new Vec3(
                    table.getX() + 0.5D,
                    table.getY() + 2.0D,
                    table.getZ() + 0.5D
            );
            Vec3 localVelocity = new Vec3(1.17D, -1.45D, -2.26D);

            Vec3 origin = PlanetParticleEmitter.transformVanillaLocalEmitter(
                    table, frame,
                    samplePosition.x, samplePosition.y, samplePosition.z
            );
            Vec3 motion = PlanetParticleMotion.localVelocityToWorld(
                    localVelocity, frame
            );

            // Source is local Y +1.5 relative to block center.
            Vec3 physicalUp = vector(frame.localToWorld(
                    new PlanetFrameVector(0.0D, 1.5D, 0.0D)
            ));
            assertVec(origin, Vec3.atCenterOf(table).add(physicalUp));
            assertVec(localVelocity, vector(frame.worldToLocal(
                    new PlanetFrameVector(motion.x, motion.y, motion.z)
            )));

            if (face == PlanetFace.POS_Y) {
                assertVec(origin, samplePosition);
                assertVec(motion, localVelocity);
            }
        }
    }

    @Test
    void sporeSampleUsesDestinationTraversalChartWhenCrossingEdge() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockFrameContext flower = PlanetBlockFrameContext.resolve(
                    FIELD,
                    physical(face, 20, 19, 0),
                    face
            ).orElseThrow();

            // One segment exceeds the face edge; the second tangent and
            // negative LOCAL vertical offset use the transported chart.
            PlanetBlockFrameContext sample =
                    PlanetLocalBlockOffset.traverse(
                            flower, 3, -2, 2
                    );
            BlockPos candidate = sample.pos();
            Vec3 localCell = new Vec3(0.15D, 0.75D, 0.33D);

            Vec3 physicalPosition =
                    PlanetParticleEmitter.transformVanillaLocalEmitter(
                            candidate,
                            sample.frame(),
                            candidate.getX() + localCell.x,
                            candidate.getY() + localCell.y,
                            candidate.getZ() + localCell.z
                    );

            Vec3 expected = Vec3.atCenterOf(candidate).add(
                    vector(sample.frame().localToWorld(
                            new PlanetFrameVector(
                                    localCell.x - 0.5D,
                                    localCell.y - 0.5D,
                                    localCell.z - 0.5D
                            )
                    ))
            );
            assertVec(expected, physicalPosition);
        }
    }

    @Test
    void sporeSampleWithinTopFacePreservesExactVanillaSubcellPosition() {
        PlanetGravityFrame up = new PlanetGravityFrame(PlanetFace.POS_Y);
        BlockPos sample = physical(PlanetFace.POS_Y, 20, 2, -3);
        Vec3 physicalPosition =
                PlanetParticleEmitter.transformVanillaLocalEmitter(
                        sample, up,
                        sample.getX() + 0.01D,
                        sample.getY() + 0.7D,
                        sample.getZ() + 0.99D
                );
        assertVec(
                new Vec3(
                        sample.getX() + 0.01D,
                        sample.getY() + 0.7D,
                        sample.getZ() + 0.99D
                ),
                physicalPosition
        );
    }

    private static Vec3 vector(PlanetFrameVector coordinate) {
        return new Vec3(coordinate.x(), coordinate.y(), coordinate.z());
    }

    private static BlockPos physical(
            PlanetFace face, int up, int east, int south
    ) {
        PlanetGravityFrame frame = new PlanetGravityFrame(face);
        PlanetVector u = frame.worldUp();
        PlanetVector e = frame.worldEast();
        PlanetVector s = frame.worldSouth();

        return new BlockPos(
                CORE.x() + u.x() * up + e.x() * east + s.x() * south,
                CORE.y() + u.y() * up + e.y() * east + s.y() * south,
                CORE.z() + u.z() * up + e.z() * east + s.z() * south
        );
    }

    private static void assertVec(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPS);
        assertEquals(expected.y, actual.y, EPS);
        assertEquals(expected.z, actual.z, EPS);
    }
}
