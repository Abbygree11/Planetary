package dev.planetary.chunk;

/**
 * Independent deltas for visible/loaded sections and fully simulated sections.
 */
public record PlanetSectionActivityUpdate(
        PlanetSectionLoadDelta render,
        PlanetSectionLoadDelta simulation
) {
    public boolean isEmpty() {
        return render.isEmpty() && simulation.isEmpty();
    }
}
