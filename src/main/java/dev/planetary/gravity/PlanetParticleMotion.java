package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Local-UP semantics for the hard-coded +0.1 vertical bias in Particle.
 */
public final class PlanetParticleMotion {
    public static final double VANILLA_UP_BIAS = 0.1D;

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
