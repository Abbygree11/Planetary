package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetVanillaChunkAvailabilityTest {
    private static final int FACE_SIZE = 4096;

    @Test
    void encodedAndGuardPositionsLiveInPlanetaryChunks() {
        PlanetVanillaPosCodec codec = new PlanetVanillaPosCodec(
                FACE_SIZE,
                -3000,
                999
        );

        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockPos westEdge = new PlanetBlockPos(
                    face,
                    0,
                    0,
                    FACE_SIZE / 2
            );
            BlockPos canonical = codec.encode(westEdge);
            BlockPos guardAlias = canonical.west();

            assertTrue(codec.containsChunk(
                    SectionPos.blockToSectionCoord(canonical.getX()),
                    SectionPos.blockToSectionCoord(canonical.getZ())
            ));
            assertTrue(codec.containsChunk(
                    SectionPos.blockToSectionCoord(guardAlias.getX()),
                    SectionPos.blockToSectionCoord(guardAlias.getZ())
            ));
        }
    }

    @Test
    void chunksOutsideVirtualAtlasRemainVanillaOwned() {
        PlanetVanillaPosCodec codec = new PlanetVanillaPosCodec(
                FACE_SIZE,
                -3000,
                999
        );

        assertFalse(codec.containsChunk(
                SectionPos.blockToSectionCoord(29_999_999),
                SectionPos.blockToSectionCoord(29_999_999)
        ));
        assertFalse(codec.containsChunk(
                SectionPos.blockToSectionCoord(-29_999_999),
                SectionPos.blockToSectionCoord(-29_999_999)
        ));
    }
}
