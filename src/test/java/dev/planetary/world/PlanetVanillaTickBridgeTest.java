package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class PlanetVanillaTickBridgeTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @SuppressWarnings("unchecked")
    @Test
    void blockAndFluidSchedulersRouteVirtualPositionsIndependently() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );

        ServerLevel level = mock(ServerLevel.class);
        LevelTicks<Block> vanillaBlockTicks = mock(LevelTicks.class);
        LevelTicks<Fluid> vanillaFluidTicks = mock(LevelTicks.class);

        when(level.getBlockTicks()).thenReturn(vanillaBlockTicks);
        when(level.getFluidTicks()).thenReturn(vanillaFluidTicks);

        world.bindLevel(level);
        try {
            PlanetBlockPos source = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    FACE_SIZE - 1,
                    -20,
                    FACE_SIZE / 2
            );
            PlanetBlockPos target = world.relative(
                    source,
                    Direction.EAST
            );

            BlockPos targetAlias = world.vanillaPosCodec()
                    .encode(source)
                    .relative(Direction.EAST);

            assertEquals(
                    target,
                    world.vanillaPosCodec().decode(targetAlias)
            );

            assertTrue(PlanetVanillaTickBridge.schedule(
                    vanillaBlockTicks,
                    new ScheduledTick<>(
                            Blocks.STONE,
                            targetAlias,
                            100L,
                            TickPriority.HIGH,
                            77L
                    )
            ));
            assertTrue(PlanetVanillaTickBridge.schedule(
                    vanillaFluidTicks,
                    new ScheduledTick<>(
                            Fluids.WATER,
                            targetAlias,
                            101L,
                            TickPriority.LOW,
                            78L
                    )
            ));

            assertTrue(
                    PlanetVanillaTickBridge.hasScheduledTick(
                            vanillaBlockTicks,
                            targetAlias,
                            Blocks.STONE
                    ).orElseThrow()
            );
            assertTrue(
                    PlanetVanillaTickBridge.hasScheduledTick(
                            vanillaFluidTicks,
                            targetAlias,
                            Fluids.WATER
                    ).orElseThrow()
            );

            var blockDue = world.blockTicks().pollDue(100L, 10);
            assertEquals(1, blockDue.size());
            assertEquals(target, blockDue.getFirst().position());
            assertEquals(77L, blockDue.getFirst().subTickOrder());

            assertTrue(world.fluidTicks().hasScheduledTick(
                    target,
                    Fluids.WATER
            ));
        } finally {
            world.unbindLevel();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void nonPlanetaryPositionsFallThroughToVanillaLevelTicks() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);

        ServerLevel level = mock(ServerLevel.class);
        LevelTicks<Block> vanillaBlockTicks = mock(LevelTicks.class);
        LevelTicks<Fluid> vanillaFluidTicks = mock(LevelTicks.class);

        when(level.getBlockTicks()).thenReturn(vanillaBlockTicks);
        when(level.getFluidTicks()).thenReturn(vanillaFluidTicks);

        world.bindLevel(level);
        try {
            BlockPos outside = new BlockPos(
                    29_999_999,
                    0,
                    29_999_999
            );

            assertFalse(PlanetVanillaTickBridge.schedule(
                    vanillaBlockTicks,
                    new ScheduledTick<>(
                            Blocks.STONE,
                            outside,
                            100L,
                            TickPriority.NORMAL,
                            1L
                    )
            ));
            assertTrue(
                    PlanetVanillaTickBridge.hasScheduledTick(
                            vanillaBlockTicks,
                            outside,
                            Blocks.STONE
                    ).isEmpty()
            );
        } finally {
            world.unbindLevel();
        }
    }
}
