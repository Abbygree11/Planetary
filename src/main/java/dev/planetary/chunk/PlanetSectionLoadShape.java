package dev.planetary.chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Computes the 3D render/simulation neighborhood around a player section.
 *
 * <p>Render distance keeps Minecraft's familiar meaning: distance N means
 * roughly N*16 blocks. Unlike vanilla column loading, the neighborhood is a
 * true sphere in section space.</p>
 */
public final class PlanetSectionLoadShape {
    private PlanetSectionLoadShape() {
    }

    public static Set<PlanetSectionPos> sphere(PlanetSectionPos center, int radiusSections) {
        if (center == null) {
            throw new NullPointerException("center");
        }
        if (radiusSections < 0) {
            throw new IllegalArgumentException("radiusSections must be >= 0");
        }

        long radiusSquared = (long) radiusSections * radiusSections;
        HashSet<PlanetSectionPos> result = new HashSet<>();

        for (int dx = -radiusSections; dx <= radiusSections; dx++) {
            long dxSquared = (long) dx * dx;
            for (int dy = -radiusSections; dy <= radiusSections; dy++) {
                long remaining = radiusSquared - dxSquared - (long) dy * dy;
                if (remaining < 0) {
                    continue;
                }

                int maxDz = (int) Math.floor(Math.sqrt(remaining));
                for (int dz = -maxDz; dz <= maxDz; dz++) {
                    result.add(center.offset(dx, dy, dz));
                }
            }
        }

        return Set.copyOf(result);
    }

    public static boolean containsOffset(int dx, int dy, int dz, int radiusSections) {
        if (radiusSections < 0) {
            return false;
        }
        long distanceSquared = (long) dx * dx + (long) dy * dy + (long) dz * dz;
        long radiusSquared = (long) radiusSections * radiusSections;
        return distanceSquared <= radiusSquared;
    }

    public static long logicalBlockCapacity(int sectionCount) {
        if (sectionCount < 0) {
            throw new IllegalArgumentException("sectionCount must be >= 0");
        }
        return (long) sectionCount * PlanetSection.BLOCK_COUNT;
    }
}
