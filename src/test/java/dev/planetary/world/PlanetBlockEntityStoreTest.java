package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetBlockEntityStoreTest {
    private static final int FACE_SIZE = 4096;
    private final PlanetVanillaPosCodec codec =
            new PlanetVanillaPosCodec(FACE_SIZE, -3000, 999);

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void createsARealVanillaChestBlockEntityAtVirtualVanillaPosition() {
        PlanetBlockEntityStore store = new PlanetBlockEntityStore(codec);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_X,
                120,
                -1500,
                300
        );

        BlockEntity created = store.createForState(
                pos,
                Blocks.CHEST.defaultBlockState()
        ).orElseThrow();

        ChestBlockEntity chest = assertInstanceOf(
                ChestBlockEntity.class,
                created
        );
        assertEquals(codec.encode(pos), chest.getBlockPos());
        assertFalse(chest.hasLevel());
        assertSame(chest, store.get(pos));
        assertSame(chest, store.getByVanillaPos(codec.encode(pos)));
    }

    @Test
    void realVanillaBlockEntityKeepsItsOwnInventoryLogic() {
        PlanetBlockEntityStore store = new PlanetBlockEntityStore(codec);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_Z,
                10,
                -20,
                30
        );

        ChestBlockEntity chest = assertInstanceOf(
                ChestBlockEntity.class,
                store.createForState(
                        pos,
                        Blocks.CHEST.defaultBlockState()
                ).orElseThrow()
        );

        ItemStack stack = new ItemStack(Items.COBBLESTONE, 12);
        chest.setItem(0, stack);

        assertEquals(12, chest.getItem(0).getCount());
        assertSame(Items.COBBLESTONE, chest.getItem(0).getItem());
    }

    @Test
    void ordinaryBlocksDoNotCreateBlockEntities() {
        PlanetBlockEntityStore store = new PlanetBlockEntityStore(codec);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                1,
                2,
                3
        );

        assertTrue(
                store.createForState(
                        pos,
                        Blocks.STONE.defaultBlockState()
                ).isEmpty()
        );
        assertTrue(store.isEmpty());
    }

    @Test
    void replacingBlockEntityBlockWithStoneRemovesTheEntity() {
        PlanetBlockEntityStore store = new PlanetBlockEntityStore(codec);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_X,
                40,
                -100,
                50
        );

        BlockEntity chest = store.reconcileBlockState(
                pos,
                Blocks.CHEST.defaultBlockState()
        );
        assertFalse(chest.isRemoved());

        BlockEntity afterStone = store.reconcileBlockState(
                pos,
                Blocks.STONE.defaultBlockState()
        );

        assertNull(afterStone);
        assertTrue(chest.isRemoved());
        assertTrue(store.isEmpty());
    }

    @Test
    void blockEntityPositionAlsoSurvivesVanillaPackedLongRoundTrip() {
        PlanetBlockEntityStore store = new PlanetBlockEntityStore(codec);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_Y,
                FACE_SIZE - 1,
                -2999,
                FACE_SIZE - 1
        );

        BlockEntity chest = store.createForState(
                pos,
                Blocks.CHEST.defaultBlockState()
        ).orElseThrow();

        BlockPos packedRoundTrip = BlockPos.of(chest.getBlockPos().asLong());

        assertEquals(chest.getBlockPos(), packedRoundTrip);
        assertEquals(pos, codec.decode(packedRoundTrip));
    }
}
