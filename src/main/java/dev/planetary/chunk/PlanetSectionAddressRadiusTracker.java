package dev.planetary.chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks one canonical 3D neighborhood on the cube planet.
 *
 * <p>The neighborhood is recomputed only after the player enters another
 * 16-block section (or the configured distance changes). Storage/render work
 * still receives only the actual entering/leaving delta.</p>
 */
public final class PlanetSectionAddressRadiusTracker {
    private final int faceSizeSections;
    private int radiusSections;
    private PlanetSectionAddress center;
    private final HashSet<PlanetSectionAddress> active = new HashSet<>();

    public PlanetSectionAddressRadiusTracker(int faceSizeSections, int radiusSections) {
        PlanetSectionTopology.validateFaceSize(faceSizeSections);
        validateRadius(radiusSections);
        this.faceSizeSections = faceSizeSections;
        this.radiusSections = radiusSections;
    }

    public int radiusSections() {
        return radiusSections;
    }

    public PlanetSectionAddress center() {
        return center;
    }

    public Set<PlanetSectionAddress> activeSections() {
        return Set.copyOf(active);
    }

    public PlanetSectionAddressLoadDelta moveTo(PlanetSectionAddress newCenter) {
        if (newCenter == null) {
            throw new NullPointerException("newCenter");
        }
        if (newCenter.equals(center)) {
            return PlanetSectionAddressLoadDelta.empty();
        }

        Set<PlanetSectionAddress> next = PlanetSectionTopologyLoadShape.sphere(
                newCenter,
                radiusSections,
                faceSizeSections
        );

        HashSet<PlanetSectionAddress> toLoad = new HashSet<>(next);
        toLoad.removeAll(active);

        HashSet<PlanetSectionAddress> toUnload = new HashSet<>(active);
        toUnload.removeAll(next);

        active.clear();
        active.addAll(next);
        center = newCenter;

        return new PlanetSectionAddressLoadDelta(toLoad, toUnload);
    }

    public PlanetSectionAddressLoadDelta resize(int newRadiusSections) {
        validateRadius(newRadiusSections);
        if (newRadiusSections == radiusSections) {
            return PlanetSectionAddressLoadDelta.empty();
        }

        radiusSections = newRadiusSections;
        if (center == null) {
            return PlanetSectionAddressLoadDelta.empty();
        }

        Set<PlanetSectionAddress> next = PlanetSectionTopologyLoadShape.sphere(
                center,
                radiusSections,
                faceSizeSections
        );

        HashSet<PlanetSectionAddress> toLoad = new HashSet<>(next);
        toLoad.removeAll(active);

        HashSet<PlanetSectionAddress> toUnload = new HashSet<>(active);
        toUnload.removeAll(next);

        active.clear();
        active.addAll(next);

        return new PlanetSectionAddressLoadDelta(toLoad, toUnload);
    }

    public PlanetSectionAddressLoadDelta clear() {
        if (active.isEmpty()) {
            center = null;
            return PlanetSectionAddressLoadDelta.empty();
        }

        Set<PlanetSectionAddress> toUnload = Set.copyOf(active);
        active.clear();
        center = null;
        return new PlanetSectionAddressLoadDelta(Set.of(), toUnload);
    }

    private static void validateRadius(int radiusSections) {
        if (radiusSections < 0) {
            throw new IllegalArgumentException("radiusSections must be >= 0");
        }
    }
}
