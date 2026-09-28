package dev.planetary.chunk;

import java.util.Set;

/**
 * Canonical section changes after moving through the cube-planet topology.
 */
public record PlanetSectionAddressLoadDelta(
        Set<PlanetSectionAddress> toLoad,
        Set<PlanetSectionAddress> toUnload
) {
    public PlanetSectionAddressLoadDelta {
        toLoad = Set.copyOf(toLoad);
        toUnload = Set.copyOf(toUnload);
    }

    public static PlanetSectionAddressLoadDelta empty() {
        return new PlanetSectionAddressLoadDelta(Set.of(), Set.of());
    }

    public boolean isEmpty() {
        return toLoad.isEmpty() && toUnload.isEmpty();
    }
}
