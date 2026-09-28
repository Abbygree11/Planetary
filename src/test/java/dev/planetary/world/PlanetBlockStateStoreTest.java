package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetBlockStateStoreTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void storesVanillaBlockStateByCanonicalBlockPosition() {
        PlanetBlockStateStore store = new PlanetBlockStateStore();
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Z,
                100,
                -33,
                200
        );

        store.setBlockState(pos, Blocks.DIRT.defaultBlockState());

        assertSame(Blocks.DIRT.defaultBlockState(), store.getBlockState(pos));
        assertEquals(1, store.sections().allocatedSectionCount());
    }

    @Test
    void missingBlockIsImplicitVanillaAir() {
        PlanetBlockStateStore store = new PlanetBlockStateStore();
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_X,
                10,
                10,
                10
        );

        assertTrue(store.getBlockState(pos).isAir());
        assertEquals(0, store.sections().allocatedSectionCount());
    }

    @Test
    void neighborLookupCrossesPlanetEdgeIntoAdjacentFace() {
        PlanetBlockStateStore store = new PlanetBlockStateStore();

        PlanetBlockPos edge = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                0,
                FACE_SIZE / 2
        );

        PlanetBlockPos across = PlanetBlockTopology.step(
                edge,
                PlanetDirection.EAST,
                FACE_SIZE
        );

        store.setBlockState(across, Blocks.STONE.defaultBlockState());

        assertSame(
                Blocks.STONE.defaultBlockState(),
                store.getNeighborState(edge, PlanetDirection.EAST, FACE_SIZE)
        );
    }

    @Test
    void blocksOnDifferentFacesNeverAliasInStorage() {
        PlanetBlockStateStore store = new PlanetBlockStateStore();

        PlanetBlockPos top = new PlanetBlockPos(
                PlanetFace.POS_Y,
                32,
                -1,
                48
        );
        PlanetBlockPos side = new PlanetBlockPos(
                PlanetFace.POS_X,
                32,
                -1,
                48
        );

        store.setBlockState(top, Blocks.GRASS_BLOCK.defaultBlockState());
        store.setBlockState(side, Blocks.STONE.defaultBlockState());

        assertSame(Blocks.GRASS_BLOCK.defaultBlockState(), store.getBlockState(top));
        assertSame(Blocks.STONE.defaultBlockState(), store.getBlockState(side));
        assertEquals(2, store.sections().allocatedSectionCount());
    }
}
