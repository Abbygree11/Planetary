package dev.planetary.chunk;

/**
 * Independent topology-aware deltas for render and simulation neighborhoods.
 */
public record PlanetSectionAddressActivityUpdate(
        PlanetSectionAddressLoadDelta render,
        PlanetSectionAddressLoadDelta simulation
) {
    public boolean isEmpty() {
        return render.isEmpty() && simulation.isEmpty();
    }
}
