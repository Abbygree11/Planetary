package dev.planetary.chunk;

import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionStoreTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void missingSectionsAreImplicitAirAndAllocateNothing() {
        PlanetSectionStore store = new PlanetSectionStore();
        PlanetSectionAddress address = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                100,
                -4,
                120
        );

        assertTrue(store.getBlockState(address, 3, 4, 5).isAir());
        assertEquals(0, store.allocatedSectionCount());
    }

    @Test
    void nonAirWriteAllocatesExactlyOneSparseSection() {
        PlanetSectionStore store = new PlanetSectionStore();
        PlanetSectionAddress address = new PlanetSectionAddress(
                PlanetFace.POS_X,
                20,
                -10,
                30
        );

        store.setBlockState(address, 1, 2, 3, Blocks.STONE.defaultBlockState());

        assertEquals(1, store.allocatedSectionCount());
        assertSame(
                Blocks.STONE.defaultBlockState(),
                store.getBlockState(address, 1, 2, 3)
        );
        assertTrue(store.findSection(address).isPresent());
    }

    @Test
    void writingAirToMissingSpaceDoesNotAllocateASection() {
        PlanetSectionStore store = new PlanetSectionStore();
        PlanetSectionAddress address = new PlanetSectionAddress(
                PlanetFace.NEG_Z,
                1,
                2,
                3
        );

        store.setBlockState(address, 7, 7, 7, Blocks.AIR.defaultBlockState());

        assertEquals(0, store.allocatedSectionCount());
    }

    @Test
    void clearingLastNonAirBlockReleasesTheSparseSection() {
        PlanetSectionStore store = new PlanetSectionStore();
        PlanetSectionAddress address = new PlanetSectionAddress(
                PlanetFace.NEG_Y,
                5,
                -30,
                8
        );

        store.setBlockState(address, 2, 2, 2, Blocks.DIRT.defaultBlockState());
        assertFalse(store.findSection(address).orElseThrow().hasOnlyAir());

        store.setBlockState(address, 2, 2, 2, Blocks.AIR.defaultBlockState());

        assertEquals(0, store.allocatedSectionCount());
        assertTrue(store.findSection(address).isEmpty());
    }
}
