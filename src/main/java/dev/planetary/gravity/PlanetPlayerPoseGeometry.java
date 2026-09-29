package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Builds player pose-fit boxes in the current local gravity frame.
 *
 * <p>Vanilla EntityDimensions always extends a pose box from the anchor
 * toward world +Y. On the bottom face local UP is world -Y, so vanilla thinks
 * a perfectly valid standing pose intersects the planet and falls back to
 * Pose.SWIMMING. Rotating the ordinary vanilla box preserves all vanilla pose
 * dimensions while using the correct local UP.</p>
 */
public final class PlanetPlayerPoseGeometry {
    private PlanetPlayerPoseGeometry() {
    }

    public static AABB poseBoundingBox(
            EntityDimensions dimensions,
            Vec3 anchor,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(frame, "frame");

        AABB vanilla = dimensions.makeBoundingBox(anchor);

        return PlanetEntityGeometry.rotateVanillaBoundingBox(
                vanilla,
                anchor,
                frame
        );
    }
}
