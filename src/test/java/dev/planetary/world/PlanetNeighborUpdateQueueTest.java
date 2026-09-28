package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetNeighborUpdateQueueTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void updateNeighborsQueuesExactlySixVanillaDirections() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                100,
                5,
                100
        );

        world.updateNeighborsAt(source, Blocks.STONE);
        List<PlanetNeighborUpdate> updates = world.neighborUpdates().drain();

        assertEquals(6, updates.size());
        assertTrue(updates.stream().allMatch(update -> update.source().equals(source)));
        assertTrue(updates.stream().allMatch(update -> update.sourceBlock() == Blocks.STONE));
    }

    @Test
    void edgeNeighborKeepsTargetLocalDirectionBackToSource() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE / 2,
                0,
                0
        );

        PlanetNeighborRef neighbor = world.neighbor(source, Direction.NORTH);

        assertEquals(PlanetFace.NEG_Z, neighbor.position().face());
        assertEquals(Direction.NORTH, neighbor.directionBackToSource());
    }

    @Test
    void changingBlockCanQueueTopologyAwareNeighborUpdates() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                0,
                FACE_SIZE / 2
        );

        world.setBlockStateAndUpdateNeighbors(
                source,
                Blocks.DIRT.defaultBlockState()
        );

        List<PlanetNeighborUpdate> updates = world.neighborUpdates().drain();
        assertEquals(6, updates.size());
        assertTrue(
                updates.stream().allMatch(
                        update -> update.sourceBlock() == Blocks.AIR
                )
        );
        assertTrue(
                updates.stream().anyMatch(
                        update -> update.target().face() == PlanetFace.POS_X
                )
        );
    }

    @Test
    void updateNeighborsExceptQueuesFiveDirections() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                100,
                0,
                200
        );

        world.updateNeighborsAtExceptFromFacing(
                source,
                Blocks.STONE,
                Direction.DOWN
        );

        List<PlanetNeighborUpdate> updates = world.neighborUpdates().drain();

        assertEquals(5, updates.size());
        assertTrue(
                updates.stream().noneMatch(
                        update -> update.target().equals(
                                world.relative(source, Direction.DOWN)
                        )
                )
        );
    }
}
