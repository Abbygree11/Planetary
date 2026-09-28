package dev.planetary.chunk;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A true 3D section-radius around a player, canonicalized through cube-face
 * transitions.
 *
 * <p>The radius is measured in familiar 16-block section units. A local
 * Euclidean sphere is generated around the player's current face frame, then
 * every offset is transported through cube edges into a canonical
 * PlanetSectionAddress.</p>
 */
public final class PlanetSectionTopologyLoadShape {
    private PlanetSectionTopologyLoadShape() {
    }

    public static Set<PlanetSectionAddress> sphere(
            PlanetSectionAddress center,
            int radiusSections,
            int faceSizeSections
    ) {
        HashSet<PlanetSectionAddress> result = new HashSet<>();
        forEachSphere(center, radiusSections, faceSizeSections, result::add);
        return Set.copyOf(result);
    }

    public static void forEachSphere(
            PlanetSectionAddress center,
            int radiusSections,
            int faceSizeSections,
            Consumer<PlanetSectionAddress> consumer
    ) {
        if (center == null) {
            throw new NullPointerException("center");
        }
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        if (radiusSections < 0) {
            throw new IllegalArgumentException("radiusSections must be >= 0");
        }
        PlanetSectionTopology.validateFaceSize(faceSizeSections);

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
                    consumer.accept(
                            PlanetSectionTopology.offset(
                                    center,
                                    dx,
                                    dy,
                                    dz,
                                    faceSizeSections
                            )
                    );
                }
            }
        }
    }

    public static long logicalBlockCapacity(int canonicalSectionCount) {
        if (canonicalSectionCount < 0) {
            throw new IllegalArgumentException("canonicalSectionCount must be >= 0");
        }
        return (long) canonicalSectionCount * PlanetSection.BLOCK_COUNT;
    }
}
