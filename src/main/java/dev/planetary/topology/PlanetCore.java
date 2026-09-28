package dev.planetary.topology;

/**
 * Immutable definition of one cubic planet's gravity source.
 *
 * <p>The core is exactly one Minecraft block. A base planet with radius R
 * occupies the odd-sized cube from -R to +R around that core, so its diameter
 * is always {@code 2 * R + 1} blocks.</p>
 */
public record PlanetCore(
        int x,
        int y,
        int z,
        int radius
) {
    public PlanetCore {
        if (radius < 1) {
            throw new IllegalArgumentException(
                    "radius must be >= 1"
            );
        }
    }

    public int diameter() {
        return Math.addExact(
                Math.multiplyExact(radius, 2),
                1
        );
    }

    public double centerX() {
        return x + 0.5;
    }

    public double centerY() {
        return y + 0.5;
    }

    public double centerZ() {
        return z + 0.5;
    }

    public boolean isCoreBlock(
            int blockX,
            int blockY,
            int blockZ
    ) {
        return blockX == x
                && blockY == y
                && blockZ == z;
    }
}
