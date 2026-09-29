package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetPlayerPoseGeometryTest {
    private static final double EPSILON = 1.0E-6D;

    @Test
    void bottomFaceStandingBoxExtendsAwayFromPlanet() {
        Vec3 anchor = new Vec3(
                24.5D,
                273.999999D,
                24.5D
        );
        EntityDimensions standing =
                EntityDimensions.scalable(
                        0.6F,
                        1.8F
                );

        AABB box =
                PlanetPlayerPoseGeometry.poseBoundingBox(
                        standing,
                        anchor,
                        new PlanetGravityFrame(
                                PlanetFace.NEG_Y
                        )
                );

        assertTrue(
                box.minY < anchor.y - 1.79D
        );
        assertEquals(
                anchor.y + 1.0E-6D,
                box.maxY,
                EPSILON
        );
    }

    @Test
    void sideFaceStandingBoxExtendsAlongLocalUp() {
        Vec3 anchor = new Vec3(
                31.0D,
                280.5D,
                24.5D
        );
        EntityDimensions standing =
                EntityDimensions.scalable(
                        0.6F,
                        1.8F
                );

        AABB box =
                PlanetPlayerPoseGeometry.poseBoundingBox(
                        standing,
                        anchor,
                        new PlanetGravityFrame(
                                PlanetFace.POS_X
                        )
                );

        assertEquals(
                anchor.x,
                box.minX,
                EPSILON
        );
        assertTrue(
                box.maxX > anchor.x + 1.79D
        );
    }

    @Test
    void topFaceMatchesVanillaPoseBox() {
        Vec3 anchor = new Vec3(
                24.5D,
                287.0D,
                24.5D
        );
        EntityDimensions standing =
                EntityDimensions.scalable(
                        0.6F,
                        1.8F
                );

        AABB expected =
                standing.makeBoundingBox(anchor);
        AABB actual =
                PlanetPlayerPoseGeometry.poseBoundingBox(
                        standing,
                        anchor,
                        new PlanetGravityFrame(
                                PlanetFace.POS_Y
                        )
                );

        assertEquals(expected.minX, actual.minX, EPSILON);
        assertEquals(expected.minY, actual.minY, EPSILON);
        assertEquals(expected.minZ, actual.minZ, EPSILON);
        assertEquals(expected.maxX, actual.maxX, EPSILON);
        assertEquals(expected.maxY, actual.maxY, EPSILON);
        assertEquals(expected.maxZ, actual.maxZ, EPSILON);
    }
}
