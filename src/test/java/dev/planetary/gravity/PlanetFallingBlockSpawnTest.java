package dev.planetary.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetFallingBlockSpawnTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void anchorIsCenterOfRequestedDownFace() {
        BlockPos pos =
                new BlockPos(
                        10,
                        20,
                        30
                );

        assertAnchor(
                pos,
                Direction.DOWN,
                10.5D,
                20.0D,
                30.5D
        );
        assertAnchor(
                pos,
                Direction.UP,
                10.5D,
                21.0D,
                30.5D
        );
        assertAnchor(
                pos,
                Direction.WEST,
                10.0D,
                20.5D,
                30.5D
        );
        assertAnchor(
                pos,
                Direction.EAST,
                11.0D,
                20.5D,
                30.5D
        );
        assertAnchor(
                pos,
                Direction.NORTH,
                10.5D,
                20.5D,
                30.0D
        );
        assertAnchor(
                pos,
                Direction.SOUTH,
                10.5D,
                20.5D,
                31.0D
        );
    }

    private static void assertAnchor(
            BlockPos pos,
            Direction down,
            double x,
            double y,
            double z
    ) {
        Vec3 actual =
                PlanetFallingBlockSpawn.anchor(
                        pos,
                        down
                );

        assertEquals(
                x,
                actual.x,
                EPSILON
        );
        assertEquals(
                y,
                actual.y,
                EPSILON
        );
        assertEquals(
                z,
                actual.z,
                EPSILON
        );
    }
}
