package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetEntitySupportTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void groundProbeMovesAlongLocalDownOnEveryFace() {
        AABB box = new AABB(
                10.0D, 20.0D, 30.0D,
                11.0D, 22.0D, 31.0D
        );

        assertMoved(
                box,
                PlanetFace.POS_X,
                -0.05D, 0.0D, 0.0D
        );
        assertMoved(
                box,
                PlanetFace.NEG_X,
                0.05D, 0.0D, 0.0D
        );
        assertMoved(
                box,
                PlanetFace.POS_Y,
                0.0D, -0.05D, 0.0D
        );
        assertMoved(
                box,
                PlanetFace.NEG_Y,
                0.0D, 0.05D, 0.0D
        );
        assertMoved(
                box,
                PlanetFace.POS_Z,
                0.0D, 0.0D, -0.05D
        );
        assertMoved(
                box,
                PlanetFace.NEG_Z,
                0.0D, 0.0D, 0.05D
        );
    }

    private static void assertMoved(
            AABB source,
            PlanetFace face,
            double dx,
            double dy,
            double dz
    ) {
        AABB actual = PlanetEntitySupport.groundProbe(
                source,
                new PlanetGravityFrame(face),
                0.05D
        );

        assertEquals(
                source.minX + dx,
                actual.minX,
                EPSILON,
                face + " minX"
        );
        assertEquals(
                source.minY + dy,
                actual.minY,
                EPSILON,
                face + " minY"
        );
        assertEquals(
                source.minZ + dz,
                actual.minZ,
                EPSILON,
                face + " minZ"
        );
        assertEquals(
                source.maxX + dx,
                actual.maxX,
                EPSILON,
                face + " maxX"
        );
        assertEquals(
                source.maxY + dy,
                actual.maxY,
                EPSILON,
                face + " maxY"
        );
        assertEquals(
                source.maxZ + dz,
                actual.maxZ,
                EPSILON,
                face + " maxZ"
        );
    }
}
