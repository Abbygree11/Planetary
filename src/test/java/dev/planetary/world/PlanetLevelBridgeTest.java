package dev.planetary.world;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetLevelBridgeTest {
    private static final int FACE_SIZE = 4096;

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void onlyExplicitlyBoundLevelsResolveVirtualPositions() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level bound = mock(Level.class);
        Level other = mock(Level.class);

        world.bindLevel(bound);

        PlanetBlockPos planetPos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                100,
                -20,
                200
        );
        BlockPos vanillaPos = world.vanillaPosCodec().encode(planetPos);

        PlanetLevelBridge.ResolvedPosition resolved =
                PlanetLevelBridge.resolve(bound, vanillaPos).orElseThrow();

        assertSame(world, resolved.world());
        assertEquals(planetPos, resolved.position());
        assertTrue(PlanetLevelBridge.resolve(other, vanillaPos).isEmpty());

        world.unbindLevel();
        assertTrue(PlanetLevelBridge.resolve(bound, vanillaPos).isEmpty());
    }

    @Test
    void vanillaNeighborAliasAcrossEdgeResolvesThroughBoundLevel() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level level = mock(Level.class);
        world.bindLevel(level);

        PlanetBlockPos edge = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                -40,
                FACE_SIZE / 2
        );

        BlockPos vanillaNeighbor = world.vanillaPosCodec()
                .encode(edge)
                .relative(Direction.EAST);

        PlanetBlockPos resolved = PlanetLevelBridge.resolve(
                level,
                vanillaNeighbor
        ).orElseThrow().position();

        assertEquals(
                PlanetBlockTopology.step(
                        edge,
                        PlanetDirection.EAST,
                        FACE_SIZE
                ),
                resolved
        );
    }
}
