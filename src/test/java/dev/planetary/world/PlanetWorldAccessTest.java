package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetWorldAccessTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void vanillaDirectionNeighborAccessWorksInsideAFace() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos origin = new PlanetBlockPos(
                PlanetFace.POS_Y,
                100,
                20,
                100
        );
        PlanetBlockPos east = world.relative(origin, Direction.EAST);

        world.setBlockState(east, Blocks.STONE.defaultBlockState());

        assertSame(
                Blocks.STONE.defaultBlockState(),
                world.getNeighborState(origin, Direction.EAST)
        );
    }

    @Test
    void vanillaDirectionNeighborAccessCrossesPlanetEdge() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos edge = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                -5,
                FACE_SIZE / 2
        );

        PlanetBlockPos across = world.relative(edge, Direction.EAST);
        assertEquals(PlanetFace.POS_X, across.face());

        world.setBlockState(across, Blocks.DIRT.defaultBlockState());

        assertSame(
                Blocks.DIRT.defaultBlockState(),
                world.getNeighborState(edge, Direction.EAST)
        );
    }

    @Test
    void localDownAlwaysMovesTowardTheCoreWithoutChangingFace() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);

        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockPos pos = new PlanetBlockPos(face, 500, 100, 500);
            PlanetBlockPos down = world.relative(pos, Direction.DOWN);

            assertEquals(face, down.face());
            assertEquals(99, down.y());
        }
    }

    @Test
    void fluidsRemainRealVanillaFluidStatesThroughWorldAccess() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_Z,
                200,
                10,
                300
        );

        world.setBlockState(pos, Blocks.WATER.defaultBlockState());

        assertTrue(!world.getFluidState(pos).isEmpty());
        assertSame(
                Blocks.WATER.defaultBlockState(),
                world.getBlockState(pos)
        );
    }

    @Test
    void setBlockStateReturnsThePreviousVanillaState() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_X,
                10,
                -20,
                30
        );

        assertTrue(world.setBlockState(pos, Blocks.STONE.defaultBlockState()).isAir());
        assertSame(
                Blocks.STONE.defaultBlockState(),
                world.setBlockState(pos, Blocks.GRASS_BLOCK.defaultBlockState())
        );
    }
}
