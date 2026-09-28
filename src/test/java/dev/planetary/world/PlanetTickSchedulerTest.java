package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.TickPriority;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetTickSchedulerTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void sameTypeAndPositionCanOnlyBeQueuedOnceUntilItTicks() {
        PlanetTickScheduler<net.minecraft.world.level.block.Block> scheduler =
                new PlanetTickScheduler<>();
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                10,
                20,
                30
        );

        assertTrue(scheduler.schedule(
                Blocks.STONE,
                pos,
                100,
                TickPriority.NORMAL
        ));
        assertFalse(scheduler.schedule(
                Blocks.STONE,
                pos,
                50,
                TickPriority.NORMAL
        ));
        assertEquals(1, scheduler.size());
    }

    @Test
    void pollDueKeepsFutureTicksQueued() {
        PlanetTickScheduler<net.minecraft.world.level.block.Block> scheduler =
                new PlanetTickScheduler<>();

        PlanetBlockPos early = new PlanetBlockPos(PlanetFace.POS_Y, 1, 2, 3);
        PlanetBlockPos late = new PlanetBlockPos(PlanetFace.POS_Y, 4, 5, 6);

        scheduler.schedule(Blocks.STONE, late, 200, TickPriority.NORMAL);
        scheduler.schedule(Blocks.DIRT, early, 100, TickPriority.NORMAL);

        List<PlanetScheduledTick<net.minecraft.world.level.block.Block>> due =
                scheduler.pollDue(100, 100);

        assertEquals(1, due.size());
        assertSame(Blocks.DIRT, due.getFirst().type());
        assertEquals(early, due.getFirst().position());
        assertEquals(1, scheduler.size());
        assertTrue(scheduler.hasScheduledTick(late, Blocks.STONE));
    }

    @Test
    void worldAccessHasIndependentBlockAndFluidSchedulers() {
        PlanetWorldAccess world = new PlanetWorldAccess(4096);
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.NEG_X,
                20,
                -10,
                40
        );

        assertTrue(world.scheduleBlockTick(
                pos,
                Blocks.STONE,
                50,
                TickPriority.NORMAL
        ));
        assertTrue(world.scheduleFluidTick(
                pos,
                Fluids.WATER,
                60,
                TickPriority.NORMAL
        ));

        assertEquals(1, world.blockTicks().size());
        assertEquals(1, world.fluidTicks().size());
    }
}
