package dev.planetary.chunk;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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
        HashSet<PlanetSectionPos> result = new HashSet<>();
        forEachSphere(center, radiusSections, result::add);
        return Set.copyOf(result);
    }

    /**
     * Iterates a sphere without first allocating a complete temporary set.
     *
     * <p>This is the primitive used by the incremental tracker: crossing a
     * section boundary only creates the entering/leaving shell, not another
     * copy of the whole loaded neighborhood.</p>
     */
    public static void forEachSphere(
            PlanetSectionPos center,
            int radiusSections,
            Consumer<PlanetSectionPos> consumer
    ) {
        if (center == null) {
            throw new NullPointerException("center");
        }
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        validateRadius(radiusSections);

        long radiusSquared = (long) radiusSections * radiusSections;

        for (int dx = -radiusSections; dx <= radiusSections; dx++) {
            long dxSquared = (long) dx * dx;
            for (int dy = -radiusSections; dy <= radiusSections; dy++) {
                long remaining = radiusSquared - dxSquared - (long) dy * dy;
                if (remaining < 0) {
                    continue;
                }

                int maxDz = (int) Math.floor(Math.sqrt(remaining));
                for (int dz = -maxDz; dz <= maxDz; dz++) {
                    consumer.accept(center.offset(dx, dy, dz));
                }
            }
        }
    }

    public static boolean contains(
            PlanetSectionPos center,
            PlanetSectionPos candidate,
            int radiusSections
    ) {
        if (center == null) {
            throw new NullPointerException("center");
        }
        if (candidate == null) {
            throw new NullPointerException("candidate");
        }
        if (radiusSections < 0) {
            return false;
        }

        long dx = (long) candidate.x() - center.x();
        long dy = (long) candidate.y() - center.y();
        long dz = (long) candidate.z() - center.z();
        long distanceSquared = dx * dx + dy * dy + dz * dz;
        long radiusSquared = (long) radiusSections * radiusSections;
        return distanceSquared <= radiusSquared;
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

    private static void validateRadius(int radiusSections) {
        if (radiusSections < 0) {
            throw new IllegalArgumentException("radiusSections must be >= 0");
        }
    }
}
