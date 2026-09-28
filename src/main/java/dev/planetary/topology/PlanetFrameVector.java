package dev.planetary.topology;

/**
 * Continuous vector used by a gravity frame.
 *
 * <p>When interpreted as local coordinates, x=EAST, y=UP and z=SOUTH.
 * When interpreted as world coordinates, x/y/z are ordinary Minecraft world
 * axes. The same value type is used deliberately so frame conversion is an
 * explicit operation rather than a hidden coordinate-space flag.</p>
 */
public record PlanetFrameVector(
        double x,
        double y,
        double z
) {
    public PlanetFrameVector add(PlanetFrameVector other) {
        return new PlanetFrameVector(
                x + other.x,
                y + other.y,
                z + other.z
        );
    }

    public PlanetFrameVector scale(double factor) {
        return new PlanetFrameVector(
                x * factor,
                y * factor,
                z * factor
        );
    }

    public double dot(PlanetFrameVector other) {
        return x * other.x
                + y * other.y
                + z * other.z;
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }
}
