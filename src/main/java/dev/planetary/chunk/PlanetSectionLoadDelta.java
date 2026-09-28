package dev.planetary.chunk;

import java.util.Set;

/**
 * Incremental section changes produced when the player crosses a 16-block
 * section boundary or when a distance setting changes.
 */
public record PlanetSectionLoadDelta(
        Set<PlanetSectionPos> toLoad,
        Set<PlanetSectionPos> toUnload
) {
    public PlanetSectionLoadDelta {
        toLoad = Set.copyOf(toLoad);
        toUnload = Set.copyOf(toUnload);
    }

    public static PlanetSectionLoadDelta empty() {
        return new PlanetSectionLoadDelta(Set.of(), Set.of());
    }

    public boolean isEmpty() {
        return toLoad.isEmpty() && toUnload.isEmpty();
    }
}
