package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetLocalPlayerGeometryTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void sampleOffsetsStayInLocalFloorPlane() {
        Vec3 origin = new Vec3(10.5D, 20.5D, 30.5D);

        Vec3 onPositiveX =
                PlanetLocalPlayerGeometry.samplePoint(
                        origin,
                        0.35D,
                        -0.35D,
                        new PlanetGravityFrame(PlanetFace.POS_X)
                );

        // POS_X: local EAST=-Y, local SOUTH=+Z.
        assertEquals(10.5D, onPositiveX.x, EPSILON);
        assertEquals(20.15D, onPositiveX.y, EPSILON);
        assertEquals(30.15D, onPositiveX.z, EPSILON);

        Vec3 onPositiveZ =
                PlanetLocalPlayerGeometry.samplePoint(
                        origin,
                        0.35D,
                        -0.35D,
                        new PlanetGravityFrame(PlanetFace.POS_Z)
                );

        // POS_Z: local EAST=+X, local SOUTH=-Y.
        assertEquals(10.85D, onPositiveZ.x, EPSILON);
        assertEquals(20.85D, onPositiveZ.y, EPSILON);
        assertEquals(30.5D, onPositiveZ.z, EPSILON);
    }

    @Test
    void suffocationColumnUsesPlayerExtentOnlyAlongLocalUp() {
        AABB player = new AABB(
                10.0D, 20.0D, 30.0D,
                11.8D, 20.6D, 30.6D
        );
        BlockPos cell = new BlockPos(4, 5, 6);

        AABB positiveX =
                PlanetLocalPlayerGeometry.suffocationColumn(
                        player,
                        cell,
                        new PlanetGravityFrame(PlanetFace.POS_X)
                );

        assertBox(
                positiveX,
                10.0D, 5.0D, 6.0D,
                11.8D, 6.0D, 7.0D
        );

        AABB positiveZ =
                PlanetLocalPlayerGeometry.suffocationColumn(
                        player,
                        cell,
                        new PlanetGravityFrame(PlanetFace.POS_Z)
                );

        assertBox(
                positiveZ,
                4.0D, 5.0D, 30.0D,
                5.0D, 6.0D, 30.6D
        );
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
