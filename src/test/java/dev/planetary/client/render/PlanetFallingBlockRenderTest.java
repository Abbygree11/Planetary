package dev.planetary.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetFallingBlockRenderTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void vanillaDownKeepsVanillaTranslation() {
        assertVec(
                PlanetFallingBlockRender.modelTranslation(Direction.DOWN),
                -0.5D,
                0.0D,
                -0.5D
        );
    }

    @Test
    void modelTranslationUsesLocalDownFaceAnchor() {
        assertVec(PlanetFallingBlockRender.modelTranslation(Direction.WEST), 0.0D, -0.5D, -0.5D);
        assertVec(PlanetFallingBlockRender.modelTranslation(Direction.EAST), -1.0D, -0.5D, -0.5D);
        assertVec(PlanetFallingBlockRender.modelTranslation(Direction.NORTH), -0.5D, -0.5D, 0.0D);
        assertVec(PlanetFallingBlockRender.modelTranslation(Direction.SOUTH), -0.5D, -0.5D, -1.0D);
        assertVec(PlanetFallingBlockRender.modelTranslation(Direction.UP), -0.5D, -1.0D, -0.5D);
    }

    @Test
    void renderBlockPosUsesCellCenterBehindAnchor() {
        BlockPos pos = new BlockPos(10, 20, 30);

        for (Direction down : Direction.values()) {
            Vec3 anchor =
                    Vec3.atCenterOf(pos)
                            .add(
                                    down.getStepX() * 0.5D,
                                    down.getStepY() * 0.5D,
                                    down.getStepZ() * 0.5D
                            );

            assertEquals(
                    pos,
                    PlanetFallingBlockRender.renderBlockPos(
                            anchor,
                            down
                    ),
                    down.toString()
            );
        }
    }

    private static void assertVec(
            Vec3 actual,
            double x,
            double y,
            double z
    ) {
        assertEquals(x, actual.x, EPSILON);
        assertEquals(y, actual.y, EPSILON);
        assertEquals(z, actual.z, EPSILON);
    }
}
