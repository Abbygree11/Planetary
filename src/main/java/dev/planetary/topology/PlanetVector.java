package dev.planetary.topology;

/**
 * Small immutable integer vector in the global cube frame.
 *
 * <p>Face basis vectors returned by PlanetFace are unit axis vectors, while
 * the arithmetic helpers make the type useful for rigid embedding and gravity
 * calculations as well.</p>
 */
public record PlanetVector(int x, int y, int z) {

    public PlanetVector add(PlanetVector other) {
        return new PlanetVector(
                x + other.x,
                y + other.y,
                z + other.z
        );
    }

    public PlanetVector scale(int factor) {
        return new PlanetVector(
                x * factor,
                y * factor,
                z * factor
        );
    }

    public int dot(PlanetVector other) {
        return x * other.x
                + y * other.y
                + z * other.z;
    }

    public PlanetVector cross(PlanetVector other) {
        return new PlanetVector(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }
}
