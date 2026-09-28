package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

final class PlanetVanillaNeighborUpdatesTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void edgeUpdatePresentsEveryTargetInItsOwnCanonicalFrame() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level level = mock(Level.class);
        world.bindLevel(level);

        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                -20,
                FACE_SIZE / 2
        );

        PlanetVanillaNeighborUpdates.updateAll(
                level,
                world,
                source,
                Blocks.STONE
        );

        ArgumentCaptor<BlockPos> targetCaptor =
                ArgumentCaptor.forClass(BlockPos.class);
        ArgumentCaptor<BlockPos> sourceCaptor =
                ArgumentCaptor.forClass(BlockPos.class);

        verify(level, times(6)).neighborChanged(
                targetCaptor.capture(),
                eq(Blocks.STONE),
                sourceCaptor.capture()
        );

        List<BlockPos> targetPositions = targetCaptor.getAllValues();
        List<BlockPos> sourceAliases = sourceCaptor.getAllValues();

        Set<PlanetBlockPos> decodedTargets = new HashSet<>();

        for (int i = 0; i < targetPositions.size(); i++) {
            BlockPos targetVanilla = targetPositions.get(i);
            BlockPos sourceAlias = sourceAliases.get(i);

            PlanetBlockPos target = world.vanillaPosCodec().decode(
                    targetVanilla
            );
            decodedTargets.add(target);

            assertEquals(
                    targetVanilla,
                    world.vanillaPosCodec().encode(target)
            );
            assertEquals(
                    source,
                    world.vanillaPosCodec().decode(sourceAlias)
            );

            int manhattan =
                    Math.abs(targetVanilla.getX() - sourceAlias.getX())
                            + Math.abs(targetVanilla.getY() - sourceAlias.getY())
                            + Math.abs(targetVanilla.getZ() - sourceAlias.getZ());
            assertEquals(1, manhattan);
        }

        Set<PlanetBlockPos> expected = new HashSet<>();
        for (Direction direction : Direction.values()) {
            expected.add(world.relative(source, direction));
        }

        assertEquals(expected, decodedTargets);
        assertTrue(
                decodedTargets.stream().anyMatch(
                        pos -> pos.face() == PlanetFace.POS_X
                )
        );
    }

    @Test
    void updateExceptSkipsExactlyOneSourceLocalDirection() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level level = mock(Level.class);
        world.bindLevel(level);

        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                100,
                0,
                200
        );
        Block sourceBlock = Blocks.DIRT;

        PlanetVanillaNeighborUpdates.updateExcept(
                level,
                world,
                source,
                sourceBlock,
                Direction.DOWN
        );

        ArgumentCaptor<BlockPos> targetCaptor =
                ArgumentCaptor.forClass(BlockPos.class);

        verify(level, times(5)).neighborChanged(
                targetCaptor.capture(),
                eq(sourceBlock),
                org.mockito.ArgumentMatchers.any(BlockPos.class)
        );

        Set<PlanetBlockPos> decodedTargets = new HashSet<>();
        for (BlockPos target : targetCaptor.getAllValues()) {
            decodedTargets.add(world.vanillaPosCodec().decode(target));
        }

        assertTrue(!decodedTargets.contains(
                world.relative(source, Direction.DOWN)
        ));
        assertEquals(5, decodedTargets.size());
    }
}
