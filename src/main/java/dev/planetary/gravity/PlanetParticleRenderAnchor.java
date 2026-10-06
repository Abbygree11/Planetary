package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Render-anchor helpers for particles whose vanilla position is the center of
 * the X/Z footprint and the minimum Y face of the particle AABB.
 */
public final class PlanetParticleRenderAnchor {
    private PlanetParticleRenderAnchor() {
    }

    /**
     * Returns the world-space offset that moves vanilla's render anchor
     * (center X/Z, min Y) to the center of the LOCAL-DOWN face of the same
     * physical AABB.
     *
     * <p>For +Y gravity local DOWN is world -Y, so the result is exactly zero.
     * For the other five faces this is the signed half-extent needed to make
     * the visual anchor rotationally equivalent to vanilla.</p>
     */
    public static Vec3 localDownFaceOffset(
            AABB box,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(frame, "frame");

        double centerX =
                (box.minX + box.maxX) * 0.5D;
        double centerY =
                (box.minY + box.maxY) * 0.5D;
        double centerZ =
                (box.minZ + box.maxZ) * 0.5D;

        double halfX =
                (box.maxX - box.minX) * 0.5D;
        double halfY =
                (box.maxY - box.minY) * 0.5D;
        double halfZ =
                (box.maxZ - box.minZ) * 0.5D;

        PlanetVector down =
                frame.worldDown();

        Vec3 localDownAnchor =
                new Vec3(
                        centerX + down.x() * halfX,
                        centerY + down.y() * halfY,
                        centerZ + down.z() * halfZ
                );

        Vec3 vanillaAnchor =
                new Vec3(
                        centerX,
                        box.minY,
                        centerZ
                );

        return localDownAnchor.subtract(vanillaAnchor);
    }
}
