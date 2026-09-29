package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Re-expresses vanilla living-entity animation calculations in local gravity
 * coordinates. Vanilla assumes world XZ is always the floor plane.
 */
public final class PlanetLivingAnimation {
    private static final double MOVEMENT_THRESHOLD =
            0.0025000002D;

    private PlanetLivingAnimation() {
    }

    public static State state(
            PlanetGravityFrame frame,
            Vec3 worldDisplacement,
            float entityYaw,
            float currentBodyYaw,
            float attackAnim,
            boolean onGround
    ) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(
                worldDisplacement,
                "worldDisplacement"
        );

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                worldDisplacement.x,
                                worldDisplacement.y,
                                worldDisplacement.z
                        )
                );

        double horizontalSquared =
                local.x() * local.x()
                        + local.z() * local.z();

        float bodyTarget = currentBodyYaw;
        float animationStep = 0.0F;
        boolean moving =
                horizontalSquared > MOVEMENT_THRESHOLD;

        if (moving) {
            animationStep =
                    (float) Math.sqrt(horizontalSquared)
                            * 3.0F;

            float movementYaw =
                    (float) Mth.atan2(
                            local.z(),
                            local.x()
                    ) * (180.0F / (float) Math.PI)
                            - 90.0F;

            float difference = Mth.abs(
                    Mth.wrapDegrees(
                            entityYaw - movementYaw
                    )
            );

            bodyTarget =
                    95.0F < difference
                                    && difference < 265.0F
                            ? movementYaw - 180.0F
                            : movementYaw;
        }

        if (attackAnim > 0.0F) {
            bodyTarget = entityYaw;
        }

        return new State(
                bodyTarget,
                animationStep,
                moving && onGround ? 1.0F : 0.0F,
                local
        );
    }

    public static float walkDistance(
            PlanetGravityFrame frame,
            Vec3 worldDisplacement,
            boolean includeLocalHeight
    ) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(
                worldDisplacement,
                "worldDisplacement"
        );

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                worldDisplacement.x,
                                worldDisplacement.y,
                                worldDisplacement.z
                        )
                );

        return (float) Mth.length(
                local.x(),
                includeLocalHeight ? local.y() : 0.0D,
                local.z()
        );
    }

    public record State(
            float bodyTargetYaw,
            float animationStep,
            float runTarget,
            PlanetFrameVector localDisplacement
    ) {
    }
}
