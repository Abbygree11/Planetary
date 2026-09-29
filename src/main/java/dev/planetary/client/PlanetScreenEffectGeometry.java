package dev.planetary.client;

import dev.planetary.gravity.PlanetEntityGeometry;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Local-frame sampling used by first-person screen effects.
 *
 * <p>Vanilla samples eight points around the eye in world X/Y/Z. On a side
 * gravity face that reaches into the floor and falsely triggers the
 * inside-block overlay. These helpers keep the exact vanilla offsets but
 * interpret them in the player's local EAST/UP/SOUTH frame.</p>
 */
public final class PlanetScreenEffectGeometry {
    private PlanetScreenEffectGeometry() {
    }

    public static Vec3 sampleEyeCorner(
            Vec3 eyePosition,
            double width,
            double scale,
            int cornerIndex,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                eyePosition,
                "eyePosition"
        );
        Objects.requireNonNull(frame, "frame");

        if (cornerIndex < 0 || cornerIndex >= 8) {
            throw new IllegalArgumentException(
                    "cornerIndex must be in [0, 7]"
            );
        }

        double localX =
                (((cornerIndex >> 0) & 1) - 0.5D)
                        * width
                        * 0.8D;
        double localY =
                (((cornerIndex >> 1) & 1) - 0.5D)
                        * 0.1D
                        * scale;
        double localZ =
                (((cornerIndex >> 2) & 1) - 0.5D)
                        * width
                        * 0.8D;

        return eyePosition.add(
                PlanetEntityGeometry.localOffsetToWorld(
                        frame,
                        localX,
                        localY,
                        localZ
                )
        );
    }
}
