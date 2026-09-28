package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTickAccess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class PlanetVanillaBlockLifecycleTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @SuppressWarnings("unchecked")
    @Test
    void setBlockRunsVanillaRemoveAndPlaceCallbacksAtCanonicalPosition() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );

        Level level = mock(Level.class);
        LevelTickAccess<Block> blockTicks = mock(LevelTickAccess.class);
        when(level.getBlockTicks()).thenReturn(blockTicks);
        when(blockTicks.hasScheduledTick(
                org.mockito.ArgumentMatchers.any(BlockPos.class),
                org.mockito.ArgumentMatchers.any(Block.class)
        )).thenReturn(false);

        world.bindLevel(level);

        try {
            PlanetBlockPos pos = new PlanetBlockPos(
                    PlanetFace.POS_X,
                    0,
                    -20,
                    FACE_SIZE / 2
            );
            BlockPos canonicalPos = world.vanillaPosCodec().encode(pos);

            world.setBlockState(
                    pos,
                    Blocks.CHEST.defaultBlockState()
            );
            assertTrue(world.getBlockEntity(pos) != null);

            BlockState poweredObserver = Blocks.OBSERVER
                    .defaultBlockState()
                    .setValue(ObserverBlock.POWERED, true);
            BlockState unpoweredObserver = poweredObserver.setValue(
                    ObserverBlock.POWERED,
                    false
            );

            assertTrue(world.setBlock(
                    pos,
                    poweredObserver,
                    Block.UPDATE_KNOWN_SHAPE,
                    Block.UPDATE_LIMIT
            ));

            // ChestBlock#onRemove delegates BE removal through the real Level.
            verify(level).removeBlockEntity(canonicalPos);

            // ObserverBlock#onPlace clears POWERED through Level#setBlock.
            verify(level).setBlock(
                    canonicalPos,
                    unpoweredObserver,
                    Block.UPDATE_CLIENTS
                            | Block.UPDATE_KNOWN_SHAPE
            );

            assertSame(
                    poweredObserver,
                    world.getBlockState(pos)
            );
            assertTrue(world.getBlockEntity(pos) == null);
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void assigningTheExactSameStateDoesNotRunLifecycleAgain() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            PlanetBlockPos pos = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    100,
                    10,
                    200
            );
            BlockState state = Blocks.STONE.defaultBlockState();

            world.setBlockState(pos, state);

            assertFalse(world.setBlock(
                    pos,
                    state,
                    Block.UPDATE_KNOWN_SHAPE,
                    Block.UPDATE_LIMIT
            ));
            assertSame(state, world.getBlockState(pos));
        } finally {
            world.unbindLevel();
        }
    }
}
