package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetVanillaBlockLifecycleTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void setBlockRunsRemoveBeforePlaceUsingCanonicalPlanetPosition() {
        List<String> callbacks = new ArrayList<>();
        TrackingBlock oldBlock = new TrackingBlock("old", callbacks);
        TrackingBlock newBlock = new TrackingBlock("new", callbacks);

        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            PlanetBlockPos pos = new PlanetBlockPos(
                    PlanetFace.POS_X,
                    0,
                    -20,
                    FACE_SIZE / 2
            );

            world.setBlockState(pos, oldBlock.defaultBlockState());
            callbacks.clear();

            assertTrue(world.setBlock(
                    pos,
                    newBlock.defaultBlockState(),
                    Block.UPDATE_KNOWN_SHAPE,
                    Block.UPDATE_LIMIT
            ));

            assertEquals(
                    List.of("remove:old", "place:new"),
                    callbacks
            );
            assertEquals(
                    world.vanillaPosCodec().encode(pos),
                    oldBlock.lastRemovePos
            );
            assertEquals(
                    world.vanillaPosCodec().encode(pos),
                    newBlock.lastPlacePos
            );
            assertSame(
                    newBlock.defaultBlockState(),
                    world.getBlockState(pos)
            );
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void assigningTheExactSameStateDoesNotRunLifecycleAgain() {
        List<String> callbacks = new ArrayList<>();
        TrackingBlock block = new TrackingBlock("same", callbacks);

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
            BlockState state = block.defaultBlockState();

            world.setBlockState(pos, state);
            callbacks.clear();

            assertFalse(world.setBlock(
                    pos,
                    state,
                    Block.UPDATE_KNOWN_SHAPE,
                    Block.UPDATE_LIMIT
            ));
            assertTrue(callbacks.isEmpty());
        } finally {
            world.unbindLevel();
        }
    }

    private static final class TrackingBlock extends Block {
        private final String name;
        private final List<String> callbacks;
        private BlockPos lastRemovePos;
        private BlockPos lastPlacePos;

        private TrackingBlock(
                String name,
                List<String> callbacks
        ) {
            super(BlockBehaviour.Properties.of());
            this.name = name;
            this.callbacks = callbacks;
        }

        @Override
        protected void onRemove(
                BlockState state,
                Level level,
                BlockPos pos,
                BlockState newState,
                boolean movedByPiston
        ) {
            callbacks.add("remove:" + name);
            lastRemovePos = pos.immutable();
            super.onRemove(
                    state,
                    level,
                    pos,
                    newState,
                    movedByPiston
            );
        }

        @Override
        protected void onPlace(
                BlockState state,
                Level level,
                BlockPos pos,
                BlockState oldState,
                boolean movedByPiston
        ) {
            callbacks.add("place:" + name);
            lastPlacePos = pos.immutable();
            super.onPlace(
                    state,
                    level,
                    pos,
                    oldState,
                    movedByPiston
            );
        }
    }
}
