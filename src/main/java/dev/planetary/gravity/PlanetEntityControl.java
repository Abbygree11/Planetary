package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Converts vanilla-style local movement controls into world-space motion.
 *
 * <p>Yaw remains local to the current gravity frame. The returned velocity is
 * always ordinary world XYZ so physical momentum can stay continuous when the
 * gravity face changes.</p>
 */
public final class PlanetEntityControl {
    private static final double EPSILON = 1.0E-7D;

    private PlanetEntityControl() {
    }

    public static Vec3 relativeInputToWorld(
            Vec3 relative,
            float amount,
            float yawDegrees,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(relative, "relative");
        Objects.requireNonNull(frame, "frame");

        double lengthSquared = relative.lengthSqr();
        if (lengthSquared < EPSILON) {
            return Vec3.ZERO;
        }

        Vec3 scaled = (lengthSquared > 1.0D
                ? relative.normalize()
                : relative).scale(amount);

        double yaw = Math.toRadians(yawDegrees);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);

        PlanetFrameVector local = new PlanetFrameVector(
                scaled.x * cos - scaled.z * sin,
                scaled.y,
                scaled.z * cos + scaled.x * sin
        );

        PlanetFrameVector world = frame.localToWorld(local);
        return new Vec3(world.x(), world.y(), world.z());
    }

    public static Vec3 jumpVelocity(
            Vec3 currentWorldVelocity,
            float jumpPower,
            float yawDegrees,
            boolean sprinting,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                currentWorldVelocity,
                "currentWorldVelocity"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector local = frame.worldToLocal(
                new PlanetFrameVector(
                        currentWorldVelocity.x,
                        currentWorldVelocity.y,
                        currentWorldVelocity.z
                )
        );

        double x = local.x();
        double y = jumpPower;
        double z = local.z();

        if (sprinting) {
            double yaw = Math.toRadians(yawDegrees);
            x += -Math.sin(yaw) * 0.2D;
            z += Math.cos(yaw) * 0.2D;
        }

        PlanetFrameVector world = frame.localToWorld(
                new PlanetFrameVector(x, y, z)
        );

        return new Vec3(world.x(), world.y(), world.z());
    }
}
