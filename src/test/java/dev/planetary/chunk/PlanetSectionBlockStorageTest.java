package dev.planetary.chunk;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionBlockStorageTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void newSectionIsVanillaAirWithout4096BlockObjects() {
        PlanetSectionBlockStorage section = new PlanetSectionBlockStorage();

        assertTrue(section.hasOnlyAir());
        assertEquals(0, section.nonAirBlockCount());
        assertTrue(section.getBlockState(0, 0, 0).isAir());
        assertTrue(section.getBlockState(15, 15, 15).isAir());
    }

    @Test
    void storesTheExactVanillaBlockStateIncludingProperties() {
        PlanetSectionBlockStorage section = new PlanetSectionBlockStorage();
        BlockState water = Blocks.WATER.defaultBlockState();

        section.setBlockState(7, 8, 9, water);

        assertSame(water, section.getBlockState(7, 8, 9));
        assertFalse(section.getFluidState(7, 8, 9).isEmpty());
        assertEquals(1, section.nonAirBlockCount());
    }

    @Test
    void replacingAStateReturnsThePreviousVanillaState() {
        PlanetSectionBlockStorage section = new PlanetSectionBlockStorage();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();

        assertTrue(section.setBlockState(1, 2, 3, stone).isAir());
        assertSame(stone, section.setBlockState(1, 2, 3, dirt));
        assertSame(dirt, section.getBlockState(1, 2, 3));
        assertEquals(1, section.nonAirBlockCount());
    }

    @Test
    void removingTheLastBlockReturnsSectionToEmptyState() {
        PlanetSectionBlockStorage section = new PlanetSectionBlockStorage();

        section.setBlockState(4, 5, 6, Blocks.STONE.defaultBlockState());
        section.setBlockState(4, 5, 6, Blocks.AIR.defaultBlockState());

        assertTrue(section.hasOnlyAir());
        assertEquals(0, section.nonAirBlockCount());
    }
}
