package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure semantic coordinate/motion checks for the Phase-4 block-local emitter
 * family. These represent already-sampled values taken from the seven
 * 1.21.1 Block.animateTick algorithms. None of the adapters calls RNG.
 */
final class PlanetBlockLocalEmitterBatchTest {
    private static final BlockPos POS = new BlockPos(16, 129, -24);
    private static final Vec3 CENTER = Vec3.atCenterOf(POS);
    private static final double EPS = 1.0E-9D;

    private record Sample(String source, Vec3 localOffset) {
    }

    private static final List<Sample> SAMPLES = List.of(
            new Sample("furnace front",
                    new Vec3(0.52D, -0.20D, -0.14D)),
            new Sample("blast furnace front",
                    new Vec3(0.17D, -0.05D, -0.52D)),
            new Sample("smoker top",
                    new Vec3(0.0D, 0.60D, 0.0D)),
            new Sample("brewing stand smoke",
                    new Vec3(-0.06D, 0.35D, 0.08D)),
            new Sample("end rod facing and center jitter",
                    new Vec3(-0.02D, 0.34D, 0.01D)),
            new Sample("respawn anchor top",
                    new Vec3(0.22D, 0.50D, -0.24D)),
            new Sample("ender chest portal",
                    new Vec3(0.25D, -0.11D, -0.25D))
    );

    @Test
    void sevenBlockEmitterPositionsRoundTripExactlyInAllSixFrames() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);

            for (Sample sample : SAMPLES) {
                Vec3 vanilla = CENTER.add(sample.localOffset);

                Vec3 adapted =
                        PlanetParticleEmitter.transformVanillaLocalEmitter(
                                POS, frame,
                                vanilla.x, vanilla.y, vanilla.z
                        );

                PlanetFrameVector decoded = frame.worldToLocal(
                        new PlanetFrameVector(
                                adapted.x - CENTER.x,
                                adapted.y - CENTER.y,
                                adapted.z - CENTER.z
                        )
                );

                assertVec(
                        new Vec3(decoded.x(), decoded.y(), decoded.z()),
                        sample.localOffset
                );

                if (face == PlanetFace.POS_Y) {
                    assertVec(adapted, vanilla);
                }
            }
        }
    }

    @Test
    void upwardAndSwirlMomentumAreLocalOnlyForAuthoredSources() {
        Vec3 anchorUp = new Vec3(0.0D, 0.025D, 0.0D);
        Vec3 chestSwirl = new Vec3(0.62D, -0.015D, -0.27D);

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            assertLocalMomentum(anchorUp, frame);
            assertLocalMomentum(chestSwirl, frame);

            if (face == PlanetFace.POS_Y) {
                assertVec(
                        PlanetParticleMotion.localVelocityToWorld(anchorUp, frame),
                        anchorUp
                );
                assertVec(
                        PlanetParticleMotion.localVelocityToWorld(chestSwirl, frame),
                        chestSwirl
                );
            }
        }
    }

    private static void assertLocalMomentum(
            Vec3 local,
            PlanetGravityFrame frame
    ) {
        Vec3 physical = PlanetParticleMotion.localVelocityToWorld(local, frame);
        PlanetFrameVector decoded = frame.worldToLocal(
                new PlanetFrameVector(
                        physical.x, physical.y, physical.z
                )
        );
        assertVec(
                new Vec3(decoded.x(), decoded.y(), decoded.z()),
                local
        );
    }

    private static void assertVec(Vec3 actual, Vec3 expected) {
        assertEquals(expected.x, actual.x, EPS);
        assertEquals(expected.y, actual.y, EPS);
        assertEquals(expected.z, actual.z, EPS);
    }
}
