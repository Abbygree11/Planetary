package dev.planetary.chunk;

/**
 * Integer address of a 16x16x16 section in the planet's 3D storage space.
 *
 * <p>This is deliberately independent from cube-face topology. Face-local
 * coordinates and gravity frames are a separate layer; loading only needs a
 * canonical 3D section address.</p>
 */
public record PlanetSectionPos(int x, int y, int z) {
    public PlanetSectionPos offset(int dx, int dy, int dz) {
        return new PlanetSectionPos(x + dx, y + dy, z + dz);
    }
}
