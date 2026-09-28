package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetEntityGeometryTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void playerLikeBoxRotatesOntoEveryGravityAxis() {
        Vec3 anchor = new Vec3(10.0, 20.0, 30.0);
        AABB vanilla = new AABB(
                9.7,
                20.0,
                29.7,
                10.3,
                21.8,
                30.3
        );

        AABB positiveX = PlanetEntityGeometry
                .rotateVanillaBoundingBox(
                        vanilla,
                        anchor,
                        new PlanetGravityFrame(PlanetFace.POS_X)
                );

        assertBox(
                positiveX,
                10.0,
                19.7,
                29.7,
                11.8,
                20.3,
                30.3
        );

        AABB negativeZ = PlanetEntityGeometry
                .rotateVanillaBoundingBox(
                        vanilla,
                        anchor,
                        new PlanetGravityFrame(PlanetFace.NEG_Z)
                );

        assertBox(
                negativeZ,
                9.7,
                19.7,
                28.2,
                10.3,
                20.3,
                30.0
        );
    }

    @Test
    void eyeAndBlockBelowFollowLocalUpAndDown() {
        Vec3 anchor = new Vec3(10.25, 20.5, 30.75);
        PlanetGravityFrame frame =
                new PlanetGravityFrame(PlanetFace.POS_X);

        Vec3 eye = PlanetEntityGeometry.eyePosition(
                anchor,
                1.62,
                frame
        );

        assertEquals(11.87, eye.x, EPSILON);
        assertEquals(20.5, eye.y, EPSILON);
        assertEquals(30.75, eye.z, EPSILON);

        BlockPos below = PlanetEntityGeometry.blockBelow(
                anchor,
                frame
        );

        assertEquals(BlockPos.containing(
                9.7499999,
                20.5,
                30.75
        ), below);
    }

    private static void assertBox(
            AABB box,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        assertEquals(minX, box.minX, EPSILON);
        assertEquals(minY, box.minY, EPSILON);
        assertEquals(minZ, box.minZ, EPSILON);
        assertEquals(maxX, box.maxX, EPSILON);
        assertEquals(maxY, box.maxY, EPSILON);
        assertEquals(maxZ, box.maxZ, EPSILON);
    }
}
