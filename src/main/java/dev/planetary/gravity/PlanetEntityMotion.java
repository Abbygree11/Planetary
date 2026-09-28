package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Converts vanilla's world-Y collision concepts into local gravity concepts.
 */
public final class PlanetEntityMotion {
    private static final double EPSILON = 1.0E-7D;

    private PlanetEntityMotion() {
    }

    public static CollisionResult classify(
            Vec3 requestedWorldMovement,
            Vec3 actualWorldMovement,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                requestedWorldMovement,
                "requestedWorldMovement"
        );
        Objects.requireNonNull(
                actualWorldMovement,
                "actualWorldMovement"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector requested = frame.worldToLocal(
                new PlanetFrameVector(
                        requestedWorldMovement.x,
                        requestedWorldMovement.y,
                        requestedWorldMovement.z
                )
        );
        PlanetFrameVector actual = frame.worldToLocal(
                new PlanetFrameVector(
                        actualWorldMovement.x,
                        actualWorldMovement.y,
                        actualWorldMovement.z
                )
        );

        boolean xCollision = differs(requested.x(), actual.x());
        boolean yCollision = differs(requested.y(), actual.y());
        boolean zCollision = differs(requested.z(), actual.z());

        return new CollisionResult(
                xCollision || zCollision,
                yCollision,
                yCollision && requested.y() < 0.0D,
                requested,
                actual
        );
    }

    private static boolean differs(double a, double b) {
        return Math.abs(a - b) > EPSILON;
    }

    public record CollisionResult(
            boolean horizontalCollision,
            boolean verticalCollision,
            boolean verticalCollisionBelow,
            PlanetFrameVector requestedLocal,
            PlanetFrameVector actualLocal
    ) {
    }
}
