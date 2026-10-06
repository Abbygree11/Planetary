package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Local-UP semantics for the hard-coded +0.1 vertical bias in Particle.
 */
public final class PlanetParticleMotion {
    public static final double VANILLA_UP_BIAS = 0.1D;
    private static final double VANILLA_BLOCKED_SPEED_UP = 1.1D;
    private static final double VANILLA_GROUND_FRICTION = (double) 0.7F;

    private PlanetParticleMotion() {
    }

    /**
     * Particle(ClientLevel,...velocity...) has already added +0.1 to world Y.
     * Remove that world-UP term and add the same magnitude along local UP.
     */
    public static Vec3 rotateVanillaUpBias(
            Vec3 vanillaVelocity,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(vanillaVelocity, "vanillaVelocity");
        Objects.requireNonNull(frame, "frame");

        PlanetVector up = frame.worldUp();

        return new Vec3(
                vanillaVelocity.x
                        + up.x() * VANILLA_UP_BIAS,
                vanillaVelocity.y
                        - VANILLA_UP_BIAS
                        + up.y() * VANILLA_UP_BIAS,
                vanillaVelocity.z
                        + up.z() * VANILLA_UP_BIAS
        );
    }

    /**
     * Corrects vanilla Particle.tick axis-specific post-move multipliers.
     *
     * <p>Vanilla applies speedUpWhenYMotionIsBlocked and onGround friction to
     * physical X/Z. On a rotated Planet face those are not necessarily the
     * local tangent axes. Scalar friction is intentionally untouched.</p>
     */
    public static Vec3 correctBaseTickAxisEffects(
            Vec3 vanillaVelocityAfterTick,
            PlanetGravityFrame frame,
            boolean vanillaBlockedSpeedUpApplied,
            boolean localBlockedSpeedUpRequired,
            boolean onGround
    ) {
        Objects.requireNonNull(
                vanillaVelocityAfterTick,
                "vanillaVelocityAfterTick"
        );
        Objects.requireNonNull(frame, "frame");

        Vec3 neutral =
                vanillaVelocityAfterTick;

        if (vanillaBlockedSpeedUpApplied) {
            neutral =
                    new Vec3(
                            neutral.x
                                    / VANILLA_BLOCKED_SPEED_UP,
                            neutral.y,
                            neutral.z
                                    / VANILLA_BLOCKED_SPEED_UP
                    );
        }

        if (onGround) {
            neutral =
                    new Vec3(
                            neutral.x
                                    / VANILLA_GROUND_FRICTION,
                            neutral.y,
                            neutral.z
                                    / VANILLA_GROUND_FRICTION
                    );
        }

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                neutral.x,
                                neutral.y,
                                neutral.z
                        )
                );

        double localX = local.x();
        double localY = local.y();
        double localZ = local.z();

        if (localBlockedSpeedUpRequired) {
            localX *= VANILLA_BLOCKED_SPEED_UP;
            localZ *= VANILLA_BLOCKED_SPEED_UP;
        }

        if (onGround) {
            localX *= VANILLA_GROUND_FRICTION;
            localZ *= VANILLA_GROUND_FRICTION;
        }

        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localX,
                                localY,
                                localZ
                        )
                );

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
    }

    public static boolean hasNoLocalVerticalDisplacement(
            Vec3 worldDisplacement,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                worldDisplacement,
                "worldDisplacement"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                worldDisplacement.x,
                                worldDisplacement.y,
                                worldDisplacement.z
                        )
                );

        return local.y() == 0.0D;
    }

    /**
     * Generalization of vanilla Particle.setPower:
     *   x *= p
     *   y = (y - 0.1) * p + 0.1
     *   z *= p
     *
     * The preserved 0.1 baseline is local UP instead of world +Y.
     */
    public static Vec3 scaleAroundLocalUpBias(
            Vec3 velocity,
            float power,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(velocity, "velocity");
        Objects.requireNonNull(frame, "frame");

        PlanetVector up = frame.worldUp();
        Vec3 bias = new Vec3(
                up.x() * VANILLA_UP_BIAS,
                up.y() * VANILLA_UP_BIAS,
                up.z() * VANILLA_UP_BIAS
        );

        return bias.add(
                velocity.subtract(bias)
                        .scale(power)
        );
    }
}
