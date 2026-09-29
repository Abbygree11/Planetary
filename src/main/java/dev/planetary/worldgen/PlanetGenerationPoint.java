package dev.planetary.worldgen;

/**
 * One point in the seamless procedural-generation space of a cubic planet.
 *
 * <p>Generation space is spherical: its Euclidean radius equals the physical
 * cube shell radius. A physical cube shell therefore becomes one continuous
 * spherical shell without gravity-face seams.</p>
 */
public record PlanetGenerationPoint(
        double x,
        double y,
        double z
) {
    public double radiusSquared() {
        return x * x + y * y + z * z;
    }

    public double radius() {
        return Math.sqrt(radiusSquared());
    }

    public PlanetGenerationPoint add(
            PlanetGenerationPoint other
    ) {
        return new PlanetGenerationPoint(
                x + other.x,
                y + other.y,
                z + other.z
        );
    }

    public PlanetGenerationPoint scale(double factor) {
        return new PlanetGenerationPoint(
                x * factor,
                y * factor,
                z * factor
        );
    }
}
