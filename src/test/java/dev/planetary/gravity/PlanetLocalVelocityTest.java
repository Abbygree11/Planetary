package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetLocalVelocityTest {
    private static final double EPSILON = 1.0E-12D;

    @Test
    void reexpressingLocalVelocityPreservesWorldMomentumAcrossEveryFacePair() {
        PlanetFrameVector sourceLocal =
                new PlanetFrameVector(
                        0.31D,
                        0.42D,
                        -0.77D
                );

        for (PlanetFace sourceFace : PlanetFace.values()) {
            PlanetGravityFrame source =
                    new PlanetGravityFrame(sourceFace);

            PlanetFrameVector expectedWorld =
                    source.localToWorld(sourceLocal);

            for (PlanetFace targetFace : PlanetFace.values()) {
                PlanetGravityFrame target =
                        new PlanetGravityFrame(targetFace);

                PlanetFrameVector targetLocal =
                        source.transformLocalTo(
                                target,
                                sourceLocal
                        );

                PlanetFrameVector actualWorld =
                        target.localToWorld(targetLocal);

                assertEquals(
                        expectedWorld.x(),
                        actualWorld.x(),
                        EPSILON,
                        sourceFace + " -> " + targetFace + " x"
                );
                assertEquals(
                        expectedWorld.y(),
                        actualWorld.y(),
                        EPSILON,
                        sourceFace + " -> " + targetFace + " y"
                );
                assertEquals(
                        expectedWorld.z(),
                        actualWorld.z(),
                        EPSILON,
                        sourceFace + " -> " + targetFace + " z"
                );
            }
        }
    }
}
