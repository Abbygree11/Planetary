package dev.planetary.world;

import net.minecraft.world.level.chunk.ChunkSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetChunkAvailabilityBridgeTest {

    @Test
    void onlyBoundChunkSourcesExposePlanetaryChunks() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                4096,
                -3000,
                999
        );
        ChunkSource bound = mock(ChunkSource.class);
        ChunkSource other = mock(ChunkSource.class);

        PlanetChunkAvailabilityBridge.bind(bound, world);
        try {
            var pos = world.vanillaPosCodec().encode(
                    new PlanetBlockPos(
                            dev.planetary.topology.PlanetFace.POS_Y,
                            100,
                            0,
                            100
                    )
            );

            int chunkX = net.minecraft.core.SectionPos
                    .blockToSectionCoord(pos.getX());
            int chunkZ = net.minecraft.core.SectionPos
                    .blockToSectionCoord(pos.getZ());

            assertTrue(
                    PlanetChunkAvailabilityBridge.isPlanetaryChunk(
                            bound,
                            chunkX,
                            chunkZ
                    )
            );
            assertFalse(
                    PlanetChunkAvailabilityBridge.isPlanetaryChunk(
                            other,
                            chunkX,
                            chunkZ
                    )
            );
        } finally {
            PlanetChunkAvailabilityBridge.unbind(bound, world);
        }
    }
}
