package dev.planetary.chunk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class PlanetSectionTest {
    @Test
    void sectionCoordinatesUseFloorDivisionForNegativeBlocks() {
        assertEquals(new PlanetSectionPos(0, 0, 0), PlanetSection.sectionOfBlock(0, 0, 0));
        assertEquals(new PlanetSectionPos(0, 0, 0), PlanetSection.sectionOfBlock(15, 15, 15));
        assertEquals(new PlanetSectionPos(1, 1, 1), PlanetSection.sectionOfBlock(16, 16, 16));
        assertEquals(new PlanetSectionPos(-1, -1, -1), PlanetSection.sectionOfBlock(-1, -1, -1));
        assertEquals(new PlanetSectionPos(-2, -2, -2), PlanetSection.sectionOfBlock(-17, -17, -17));
    }

    @Test
    void localCoordinateWrapsNegativeBlocksLikeMinecraftSectionCoordinates() {
        assertEquals(0, PlanetSection.localCoordinate(0));
        assertEquals(15, PlanetSection.localCoordinate(15));
        assertEquals(0, PlanetSection.localCoordinate(16));
        assertEquals(15, PlanetSection.localCoordinate(-1));
        assertEquals(15, PlanetSection.localCoordinate(-17));
    }

    @Test
    void localIndexCoversExactly4096Slots() {
        boolean[] seen = new boolean[PlanetSection.BLOCK_COUNT];

        for (int y = 0; y < PlanetSection.SIZE; y++) {
            for (int z = 0; z < PlanetSection.SIZE; z++) {
                for (int x = 0; x < PlanetSection.SIZE; x++) {
                    int index = PlanetSection.localIndex(x, y, z);
                    seen[index] = true;
                }
            }
        }

        int count = 0;
        for (boolean value : seen) {
            if (value) {
                count++;
            }
        }
        assertEquals(4096, count);
    }

    @Test
    void renderDistanceKeepsVanillaSixteenBlockMeaning() {
        assertEquals(320, PlanetSection.renderRadiusBlocks(20));
    }

    @Test
    void invalidLocalCoordinateIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PlanetSection.localIndex(16, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> PlanetSection.localIndex(0, -1, 0));
    }
}
