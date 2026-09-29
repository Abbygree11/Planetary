package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetBoundingBoxEpsilonTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void positiveWorldDownFacesExtendTinyAmountTowardFloor() {
        Vec3 anchor = new Vec3(
                10.0D,
                20.0D,
                30.0D
        );
        AABB vanilla = new AABB(
                9.7D, 20.0D, 29.7D,
                10.3D, 21.8D, 30.3D
        );

        AABB bottom =
                PlanetEntityGeometry.rotateVanillaBoundingBox(
                        vanilla,
                        anchor,
                        new PlanetGravityFrame(
                                PlanetFace.NEG_Y
                        )
                );

        // NEG_Y has local DOWN = world +Y. The floor-facing side must
        // protrude by 1e-6 to avoid exact-boundary grounded flicker.
        assertEquals(
                20.000001D,
                bottom.maxY,
                EPSILON
        );
        assertEquals(
                18.200001D,
                bottom.minY,
                EPSILON
        );
    }

    @Test
    void negativeWorldDownFacesKeepExactAnchor() {
        Vec3 anchor = new Vec3(
                10.0D,
                20.0D,
                30.0D
        );
        AABB vanilla = new AABB(
                9.7D, 20.0D, 29.7D,
                10.3D, 21.8D, 30.3D
        );

        AABB top =
                PlanetEntityGeometry.rotateVanillaBoundingBox(
                        vanilla,
                        anchor,
                        new PlanetGravityFrame(
                                PlanetFace.POS_Y
                        )
                );

        assertEquals(
                20.0D,
                top.minY,
                EPSILON
        );
        assertEquals(
                21.8D,
                top.maxY,
                EPSILON
        );
    }
}
