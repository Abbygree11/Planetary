package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetWorldBlockEntityTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void placingVanillaChestCreatesRealChestBlockEntityAutomatically() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Z,
                200,
                -2500,
                300
        );

        world.setBlockState(pos, Blocks.CHEST.defaultBlockState());

        ChestBlockEntity chest = assertInstanceOf(
                ChestBlockEntity.class,
                world.getBlockEntity(pos)
        );

        assertEquals(
                world.vanillaPosCodec().encode(pos),
                chest.getBlockPos()
        );
        assertSame(
                Blocks.CHEST.defaultBlockState(),
                world.getBlockState(pos)
        );
    }

    @Test
    void chestInventorySurvivesOrdinaryWorldStateReads() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_Y,
                100,
                -100,
                100
        );

        world.setBlockState(pos, Blocks.CHEST.defaultBlockState());

        ChestBlockEntity chest = assertInstanceOf(
                ChestBlockEntity.class,
                world.getBlockEntity(pos)
        );
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 32));

        assertSame(chest, world.getBlockEntity(pos));
        assertEquals(32, chest.getItem(0).getCount());
        assertTrue(world.getBlockState(pos).is(Blocks.CHEST));
    }

    @Test
    void replacingChestWithNormalBlockRemovesItsBlockEntity() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_X,
                50,
                -200,
                70
        );

        world.setBlockState(pos, Blocks.CHEST.defaultBlockState());
        BlockEntity old = world.getBlockEntity(pos);

        world.setBlockState(pos, Blocks.STONE.defaultBlockState());

        assertNull(world.getBlockEntity(pos));
        assertTrue(old.isRemoved());
        assertTrue(world.getBlockState(pos).is(Blocks.STONE));
    }

    @Test
    void sameLocalCoordinatesOnDifferentFacesOwnDifferentBlockEntities() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );

        PlanetBlockPos top = new PlanetBlockPos(
                PlanetFace.POS_Y,
                500,
                -10,
                600
        );
        PlanetBlockPos side = new PlanetBlockPos(
                PlanetFace.POS_X,
                500,
                -10,
                600
        );

        world.setBlockState(top, Blocks.CHEST.defaultBlockState());
        world.setBlockState(side, Blocks.CHEST.defaultBlockState());

        BlockEntity topEntity = world.getBlockEntity(top);
        BlockEntity sideEntity = world.getBlockEntity(side);

        assertEquals(2, world.blockEntities().size());
        assertTrue(topEntity != sideEntity);
        assertTrue(!topEntity.getBlockPos().equals(sideEntity.getBlockPos()));
    }
}
