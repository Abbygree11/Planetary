package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Server/client movement helpers that must use local gravity instead of
 * vanilla's hard-coded world-Y vertical component.
 */
public final class PlanetPlayerMovement {
    private PlanetPlayerMovement() {
    }

    public static double localVerticalComponent(
            Vec3 worldMovement,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(worldMovement, "worldMovement");
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector local = frame.worldToLocal(
                new PlanetFrameVector(
                        worldMovement.x,
                        worldMovement.y,
                        worldMovement.z
                )
        );
        return local.y();
    }

    public static boolean isMovingUp(
            Vec3 worldMovement,
            PlanetGravityFrame frame,
            double epsilon
    ) {
        if (epsilon < 0.0D) {
            throw new IllegalArgumentException(
                    "epsilon must be >= 0"
            );
        }

        return localVerticalComponent(
                worldMovement,
                frame
        ) > epsilon;
    }
}
