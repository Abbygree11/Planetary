package dev.planetary.chunk;

/**
 * Layout constants and coordinate conversion for a Minecraft-sized
 * 16x16x16 planet section.
 *
 * <p>The class intentionally does not own BlockState storage yet. The next
 * storage layer can reuse Minecraft's palette/container machinery instead
 * of inventing another block representation.</p>
 */
public final class PlanetSection {
    public static final int SIZE = 16;
    public static final int BLOCK_COUNT = SIZE * SIZE * SIZE;

    private PlanetSection() {
    }

    public static PlanetSectionPos sectionOfBlock(int blockX, int blockY, int blockZ) {
        return new PlanetSectionPos(
                Math.floorDiv(blockX, SIZE),
                Math.floorDiv(blockY, SIZE),
                Math.floorDiv(blockZ, SIZE)
        );
    }

    public static int localCoordinate(int blockCoordinate) {
        return Math.floorMod(blockCoordinate, SIZE);
    }

    public static int localIndex(int localX, int localY, int localZ) {
        requireLocal(localX);
        requireLocal(localY);
        requireLocal(localZ);
        return localX + SIZE * (localZ + SIZE * localY);
    }

    public static int renderRadiusBlocks(int renderDistanceSections) {
        if (renderDistanceSections < 0) {
            throw new IllegalArgumentException("renderDistanceSections must be >= 0");
        }
        return renderDistanceSections * SIZE;
    }

    private static void requireLocal(int value) {
        if (value < 0 || value >= SIZE) {
            throw new IllegalArgumentException("Local coordinate must be in [0, 15]: " + value);
        }
    }
}
