package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Axis-aligned world geometry for an entity whose local Y axis is supplied by
 * a PlanetGravityFrame.
 *
 * <p>Because cubic-planet gravity is always aligned to one of the six world
 * axes, rotating a vanilla AABB still produces a world-axis-aligned AABB. No
 * oriented-box collision system is required.</p>
 */
public final class PlanetEntityGeometry {
    private PlanetEntityGeometry() {
    }

    public static AABB rotateVanillaBoundingBox(
            AABB vanillaBox,
            Vec3 anchor,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(vanillaBox, "vanillaBox");
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(frame, "frame");

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        double[] xs = {
                vanillaBox.minX - anchor.x,
                vanillaBox.maxX - anchor.x
        };
        double[] ys = {
                vanillaBox.minY - anchor.y,
                vanillaBox.maxY - anchor.y
        };
        double[] zs = {
                vanillaBox.minZ - anchor.z,
                vanillaBox.maxZ - anchor.z
        };

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    PlanetFrameVector world =
                            frame.localToWorld(
                                    new PlanetFrameVector(x, y, z)
                            );

                    double wx = anchor.x + world.x();
                    double wy = anchor.y + world.y();
                    double wz = anchor.z + world.z();

                    minX = Math.min(minX, wx);
                    minY = Math.min(minY, wy);
                    minZ = Math.min(minZ, wz);
                    maxX = Math.max(maxX, wx);
                    maxY = Math.max(maxY, wy);
                    maxZ = Math.max(maxZ, wz);
                }
            }
        }

        return new AABB(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

    public static Vec3 localOffsetToWorld(
            PlanetGravityFrame frame,
            double x,
            double y,
            double z
    ) {
        PlanetFrameVector world = frame.localToWorld(
                new PlanetFrameVector(x, y, z)
        );
        return new Vec3(world.x(), world.y(), world.z());
    }

    public static Vec3 eyePosition(
            Vec3 anchor,
            double eyeHeight,
            PlanetGravityFrame frame
    ) {
        return anchor.add(
                localOffsetToWorld(
                        frame,
                        0.0,
                        eyeHeight,
                        0.0
                )
        );
    }

    public static BlockPos blockBelow(
            Vec3 anchor,
            PlanetGravityFrame frame
    ) {
        Vec3 down = localOffsetToWorld(
                frame,
                0.0,
                -0.5000001D,
                0.0
        );
        return BlockPos.containing(anchor.add(down));
    }
}
