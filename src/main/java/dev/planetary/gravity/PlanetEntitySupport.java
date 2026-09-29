package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Ground/support queries that work for any axis-aligned planet gravity.
 *
 * <p>Vanilla support helpers are heavily coupled to world -Y. For gameplay
 * decisions such as jumping we instead probe a short distance in local DOWN
 * and ask the level collision system whether a real block occupies that
 * space.</p>
 */
public final class PlanetEntitySupport {
    public static final double DEFAULT_PROBE_DISTANCE = 0.05D;

    private PlanetEntitySupport() {
    }

    public static boolean isGrounded(
            Entity entity,
            PlanetGravityFrame frame
    ) {
        return isGrounded(
                entity,
                frame,
                DEFAULT_PROBE_DISTANCE
        );
    }

    public static boolean isGrounded(
            Entity entity,
            PlanetGravityFrame frame,
            double probeDistance
    ) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(frame, "frame");

        AABB probe = groundProbe(
                entity.getBoundingBox(),
                frame,
                probeDistance
        );

        return entity.level()
                .getBlockCollisions(entity, probe)
                .iterator()
                .hasNext();
    }

    public static AABB groundProbe(
            AABB box,
            PlanetGravityFrame frame,
            double probeDistance
    ) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(frame, "frame");

        if (probeDistance <= 0.0D) {
            throw new IllegalArgumentException(
                    "probeDistance must be > 0"
            );
        }

        Vec3 down =
                PlanetEntityGeometry.localOffsetToWorld(
                        frame,
                        0.0D,
                        -probeDistance,
                        0.0D
                );

        return box.move(down);
    }
}
