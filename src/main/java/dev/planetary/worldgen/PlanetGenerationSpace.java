package dev.planetary.worldgen;

import dev.planetary.topology.PlanetCore;
import net.minecraft.core.BlockPos;

import java.util.Objects;

/**
 * Seamless, face-independent coordinate space used by Planet worldgen.
 *
 * <p>The physical world remains one ordinary Minecraft XYZ block grid. For
 * procedural generation only, every cube shell around the core is mapped onto
 * a sphere of the same radius:</p>
 *
 * <pre>
 * physical p
 * shell = max(|px|, |py|, |pz|)
 * generation = normalize(p) * shell
 * </pre>
 *
 * <p>This transform never selects a PlanetFace. Consequently terrain noise,
 * caves, lakes, biome climate and other WRAP generation fields are continuous
 * at gravity boundaries. The inverse transform is exact (apart from floating
 * point error), allowing generation-space features to be mapped back to real
 * BlockPos coordinates later.</p>
 */
public final class PlanetGenerationSpace {
    private static final double EPSILON = 1.0E-12D;
    private static final double INV_SQRT_TWO =
            1.0D / Math.sqrt(2.0D);

    private final PlanetCore core;

    public PlanetGenerationSpace(PlanetCore core) {
        this.core = Objects.requireNonNull(core, "core");
    }

    public PlanetCore core() {
        return core;
    }

    public PlanetGenerationPoint toGeneration(
            BlockPos worldPos
    ) {
        Objects.requireNonNull(worldPos, "worldPos");

        return toGeneration(
                worldPos.getX() + 0.5D,
                worldPos.getY() + 0.5D,
                worldPos.getZ() + 0.5D
        );
    }

    public PlanetGenerationPoint toGeneration(
            double worldX,
            double worldY,
            double worldZ
    ) {
        double dx = worldX - core.centerX();
        double dy = worldY - core.centerY();
        double dz = worldZ - core.centerZ();

        double shell = maxAbs(dx, dy, dz);
        if (shell <= EPSILON) {
            return new PlanetGenerationPoint(
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        double euclideanRadius =
                Math.sqrt(
                        dx * dx
                                + dy * dy
                                + dz * dz
                );

        double scale = shell / euclideanRadius;

        return new PlanetGenerationPoint(
                dx * scale,
                dy * scale,
                dz * scale
        );
    }

    /**
     * Exact inverse of {@link #toGeneration(double, double, double)}.
     */
    public PlanetWorldGenPoint toWorld(
            PlanetGenerationPoint point
    ) {
        Objects.requireNonNull(point, "point");

        double shell = point.radius();
        if (shell <= EPSILON) {
            return new PlanetWorldGenPoint(
                    core.centerX(),
                    core.centerY(),
                    core.centerZ()
            );
        }

        double generationMax =
                maxAbs(
                        point.x(),
                        point.y(),
                        point.z()
                );

        double scale = shell / generationMax;

        return new PlanetWorldGenPoint(
                core.centerX() + point.x() * scale,
                core.centerY() + point.y() * scale,
                core.centerZ() + point.z() * scale
        );
    }

    /**
     * Physical L-infinity radius around the core. This is also exactly the
     * Euclidean radius of the corresponding generation-space point.
     */
    public double shellRadius(
            double worldX,
            double worldY,
            double worldZ
    ) {
        return maxAbs(
                worldX - core.centerX(),
                worldY - core.centerY(),
                worldZ - core.centerZ()
        );
    }

    public double signedSurfaceOffset(
            double worldX,
            double worldY,
            double worldZ
    ) {
        return shellRadius(
                worldX,
                worldY,
                worldZ
        ) - core.radius();
    }

    /**
     * Returns exact distance to the nearest gravity-boundary plane.
     *
     * <p>No gravity face is selected. At an edge the largest two absolute
     * coordinates are equal, so the distance is zero. At a corner all three
     * are equal and the same rule naturally yields zero.</p>
     */
    public PlanetEdgeDistance edgeDistance(
            double worldX,
            double worldY,
            double worldZ
    ) {
        double ax = Math.abs(
                worldX - core.centerX()
        );
        double ay = Math.abs(
                worldY - core.centerY()
        );
        double az = Math.abs(
                worldZ - core.centerZ()
        );

        double largest;
        double second;

        if (ax >= ay) {
            if (ay >= az) {
                largest = ax;
                second = ay;
            } else if (ax >= az) {
                largest = ax;
                second = az;
            } else {
                largest = az;
                second = ax;
            }
        } else {
            if (ax >= az) {
                largest = ay;
                second = ax;
            } else if (ay >= az) {
                largest = ay;
                second = az;
            } else {
                largest = az;
                second = ay;
            }
        }

        double margin = largest - second;

        return new PlanetEdgeDistance(
                margin,
                margin * INV_SQRT_TWO
        );
    }

    public boolean allowsPlacement(
            PlanetGenerationPlacementPolicy policy,
            double worldX,
            double worldY,
            double worldZ,
            double requiredEdgeClearance
    ) {
        Objects.requireNonNull(policy, "policy");

        if (requiredEdgeClearance < 0.0D) {
            throw new IllegalArgumentException(
                    "requiredEdgeClearance must be >= 0"
            );
        }

        return switch (policy) {
            case WRAP -> true;
            case AVOID_EDGE ->
                    edgeDistance(
                            worldX,
                            worldY,
                            worldZ
                    ).hasClearance(
                            requiredEdgeClearance
                    );
        };
    }

    private static double maxAbs(
            double x,
            double y,
            double z
    ) {
        return Math.max(
                Math.abs(x),
                Math.max(
                        Math.abs(y),
                        Math.abs(z)
                )
        );
    }
}
