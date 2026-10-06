package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Pure local-frame math for CherryParticle.
 */
public final class PlanetCherryParticleMotion {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;

    private PlanetCherryParticleMotion() {
    }

    public static Vec3 windAcceleration(
            PlanetGravityFrame frame,
            float particleRandom,
            int lifetimeAfterDecrement
    ) {
        Objects.requireNonNull(frame, "frame");

        float elapsed =
                (float) (
                        INITIAL_LIFETIME
                                - lifetimeAfterDecrement
                );
        float normalized =
                Math.min(
                        elapsed / (float) INITIAL_LIFETIME,
                        1.0F
                );

        double localX =
                Math.cos(
                        Math.toRadians(
                                (double) (
                                        particleRandom * 60.0F
                                )
                        )
                )
                        * 2.0D
                        * Math.pow(
                                (double) normalized,
                                1.25D
                        )
                        * (double) ACCELERATION_SCALE;

        double localZ =
                Math.sin(
                        Math.toRadians(
                                (double) (
                                        particleRandom * 60.0F
                                )
                        )
                )
                        * 2.0D
                        * Math.pow(
                                (double) normalized,
                                1.25D
                        )
                        * (double) ACCELERATION_SCALE;

        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localX,
                                0.0D,
                                localZ
                        )
                );

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
    }

    public static boolean shouldRemove(
            PlanetGravityFrame frame,
            boolean onGround,
            int lifetimeAfterDecrement,
            Vec3 worldVelocity
    ) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(
                worldVelocity,
                "worldVelocity"
        );

        if (onGround) {
            return true;
        }

        if (lifetimeAfterDecrement >= 299) {
            return false;
        }

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                worldVelocity.x,
                                worldVelocity.y,
                                worldVelocity.z
                        )
                );

        return local.x() == 0.0D
                || local.z() == 0.0D;
    }
}
