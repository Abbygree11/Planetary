package dev.planetary.chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks one spherical 3D neighborhood (render or simulation distance).
 *
 * <p>The tracker only changes when the player's section changes. It keeps the
 * current set in place and computes only the entering/leaving shell. It never
 * rebuilds the loaded set every game tick.</p>
 */
public final class PlanetSectionRadiusTracker {
    private int radiusSections;
    private PlanetSectionPos center;
    private final HashSet<PlanetSectionPos> active = new HashSet<>();

    public PlanetSectionRadiusTracker(int radiusSections) {
        validateRadius(radiusSections);
        this.radiusSections = radiusSections;
    }

    public int radiusSections() {
        return radiusSections;
    }

    public PlanetSectionPos center() {
        return center;
    }

    public Set<PlanetSectionPos> activeSections() {
        return Set.copyOf(active);
    }

    public PlanetSectionLoadDelta moveTo(PlanetSectionPos newCenter) {
        if (newCenter == null) {
            throw new NullPointerException("newCenter");
        }
        if (newCenter.equals(center)) {
            return PlanetSectionLoadDelta.empty();
        }

        if (center == null) {
            HashSet<PlanetSectionPos> toLoad = new HashSet<>();
            PlanetSectionLoadShape.forEachSphere(newCenter, radiusSections, pos -> {
                active.add(pos);
                toLoad.add(pos);
            });
            center = newCenter;
            return new PlanetSectionLoadDelta(toLoad, Set.of());
        }

        PlanetSectionPos oldCenter = center;
        HashSet<PlanetSectionPos> toLoad = new HashSet<>();
        HashSet<PlanetSectionPos> toUnload = new HashSet<>();

        PlanetSectionLoadShape.forEachSphere(newCenter, radiusSections, pos -> {
            if (!PlanetSectionLoadShape.contains(oldCenter, pos, radiusSections)) {
                toLoad.add(pos);
            }
        });

        PlanetSectionLoadShape.forEachSphere(oldCenter, radiusSections, pos -> {
            if (!PlanetSectionLoadShape.contains(newCenter, pos, radiusSections)) {
                toUnload.add(pos);
            }
        });

        active.removeAll(toUnload);
        active.addAll(toLoad);
        center = newCenter;

        return new PlanetSectionLoadDelta(toLoad, toUnload);
    }

    public PlanetSectionLoadDelta resize(int newRadiusSections) {
        validateRadius(newRadiusSections);
        if (newRadiusSections == radiusSections) {
            return PlanetSectionLoadDelta.empty();
        }

        int oldRadius = radiusSections;
        radiusSections = newRadiusSections;

        if (center == null) {
            return PlanetSectionLoadDelta.empty();
        }

        HashSet<PlanetSectionPos> toLoad = new HashSet<>();
        HashSet<PlanetSectionPos> toUnload = new HashSet<>();

        if (newRadiusSections > oldRadius) {
            PlanetSectionLoadShape.forEachSphere(center, newRadiusSections, pos -> {
                if (!PlanetSectionLoadShape.contains(center, pos, oldRadius)) {
                    toLoad.add(pos);
                }
            });
            active.addAll(toLoad);
        } else {
            PlanetSectionLoadShape.forEachSphere(center, oldRadius, pos -> {
                if (!PlanetSectionLoadShape.contains(center, pos, newRadiusSections)) {
                    toUnload.add(pos);
                }
            });
            active.removeAll(toUnload);
        }

        return new PlanetSectionLoadDelta(toLoad, toUnload);
    }

    public PlanetSectionLoadDelta clear() {
        if (active.isEmpty()) {
            center = null;
            return PlanetSectionLoadDelta.empty();
        }

        Set<PlanetSectionPos> toUnload = Set.copyOf(active);
        active.clear();
        center = null;
        return new PlanetSectionLoadDelta(Set.of(), toUnload);
    }

    private static void validateRadius(int radiusSections) {
        if (radiusSections < 0) {
            throw new IllegalArgumentException("radiusSections must be >= 0");
        }
    }
}
