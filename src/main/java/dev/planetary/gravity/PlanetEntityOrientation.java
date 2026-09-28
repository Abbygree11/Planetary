package dev.planetary.gravity;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;

import java.util.Objects;
import java.util.Optional;

/**
 * Parallel-transport for entity view/control orientation across cube edges.
 *
 * <p>Yaw is stored in the entity's current local gravity frame. When the
 * gravity face changes, keeping the same numeric yaw is generally wrong:
 * entering the same target face from different edges requires different yaw
 * remaps. This class derives the exact edge transform from the old/new faces.</p>
 */
public final class PlanetEntityOrientation {
    private static final double EPSILON = 1.0E-12D;

    private PlanetEntityOrientation() {
    }

    public static Optional<Float> transportYaw(
            PlanetFace sourceFace,
            PlanetFace targetFace,
            float sourceYawDegrees
    ) {
        Objects.requireNonNull(sourceFace, "sourceFace");
        Objects.requireNonNull(targetFace, "targetFace");

        if (sourceFace == targetFace) {
            return Optional.of(sourceYawDegrees);
        }

        Optional<FaceTransform> transition =
                transition(sourceFace, targetFace);
        if (transition.isEmpty()) {
            return Optional.empty();
        }

        double yaw = Math.toRadians(sourceYawDegrees);

        PlanetFrameVector sourceHeading =
                new PlanetFrameVector(
                        -Math.sin(yaw),
                        0.0D,
                        Math.cos(yaw)
                );

        PlanetFrameVector targetHeading =
                transition.get().transformVector(sourceHeading);

        double horizontalLengthSquared =
                targetHeading.x() * targetHeading.x()
                        + targetHeading.z() * targetHeading.z();

        if (horizontalLengthSquared <= EPSILON) {
            return Optional.of(sourceYawDegrees);
        }

        float targetYaw = (float) Math.toDegrees(
                Math.atan2(
                        -targetHeading.x(),
                        targetHeading.z()
                )
        );

        return Optional.of(targetYaw);
    }

    public static Optional<FaceTransform> transition(
            PlanetFace sourceFace,
            PlanetFace targetFace
    ) {
        Objects.requireNonNull(sourceFace, "sourceFace");
        Objects.requireNonNull(targetFace, "targetFace");

        if (sourceFace == targetFace) {
            return Optional.empty();
        }

        PlanetDirection targetNormalInSource =
                sourceFace.localDirectionOf(
                        targetFace.worldVector(
                                PlanetDirection.UP
                        )
                );

        if (!targetNormalInSource.isHorizontal()) {
            return Optional.empty();
        }

        FaceTransform transform =
                FaceTransform.across(
                        sourceFace,
                        targetNormalInSource
                );

        return transform.targetFace() == targetFace
                ? Optional.of(transform)
                : Optional.empty();
    }
}
