package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

final class PlanetBlockEntityLevelBindingTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void blockEntityCreatedAfterBindingGetsTheRealLevel() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level level = mock(Level.class);
        world.bindLevel(level);

        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                100,
                -500,
                200
        );
        world.setBlockState(pos, Blocks.CHEST.defaultBlockState());

        ChestBlockEntity chest = assertInstanceOf(
                ChestBlockEntity.class,
                world.getBlockEntity(pos)
        );

        assertTrue(chest.hasLevel());
        assertSame(level, chest.getLevel());
        assertSame(level, world.boundLevel());
    }

    @Test
    void bindingAfterCreationUpdatesExistingBlockEntities() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_Z,
                300,
                -100,
                400
        );

        world.setBlockState(pos, Blocks.CHEST.defaultBlockState());

        ChestBlockEntity chest = assertInstanceOf(
                ChestBlockEntity.class,
                world.getBlockEntity(pos)
        );
        assertFalse(chest.hasLevel());

        Level level = mock(Level.class);
        world.bindLevel(level);

        assertTrue(chest.hasLevel());
        assertSame(level, chest.getLevel());
    }
}
