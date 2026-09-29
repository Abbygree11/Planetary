package dev.planetary.worldgen;

/**
 * Distance information for the nearest gravity-region boundary.
 *
 * <p>The gravity boundaries are diagonal planes such as |x|=|y|. The raw
 * axis margin is the difference between the largest and second-largest
 * absolute core-relative coordinates. Dividing that margin by sqrt(2) gives
 * the exact Euclidean distance to the nearest such plane while inside one
 * gravity pyramid.</p>
 */
public record PlanetEdgeDistance(
        double axisMargin,
        double euclideanDistance
) {
    public boolean isOnEdge(double epsilon) {
        if (epsilon < 0.0D) {
            throw new IllegalArgumentException(
                    "epsilon must be >= 0"
            );
        }

        return euclideanDistance <= epsilon;
    }

    public boolean hasClearance(double clearance) {
        if (clearance < 0.0D) {
            throw new IllegalArgumentException(
                    "clearance must be >= 0"
            );
        }

        return euclideanDistance >= clearance;
    }
}
