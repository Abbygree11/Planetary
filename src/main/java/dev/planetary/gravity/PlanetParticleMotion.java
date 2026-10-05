package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Local-frame semantics for generic vanilla Particle motion.
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

    /**
     * Reinterprets Particle.move collision response in the gravity-local frame.
     *
     * <p>Collision clipping itself remains ordinary physical XYZ. Only the
     * semantic consequences are local:
     * - local Y collision while moving DOWN means onGround;
     * - collisions on local X/Z zero those tangent velocity components;
     * - a collision on local Y does not zero gravity-axis velocity, matching
     *   vanilla's treatment of world Y.</p>
     *
     * <p>Particle.stoppedByCollision is intentionally NOT generalized here.
     * It is a sticky internal movement short-circuit, not merely a gravity
     * classification flag. In particular, block-destroy TerrainParticles are
     * spawned inside the destroyed VoxelShape; converting that sticky flag to
     * local Y can freeze their entire burst inside the source block.</p>
     */
    public static CollisionSemantics collisionSemantics(
            Vec3 requestedWorldMovement,
            Vec3 actualWorldMovement,
            Vec3 velocityBeforeMove,
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
        Objects.requireNonNull(
                velocityBeforeMove,
                "velocityBeforeMove"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector requested =
                toLocal(frame, requestedWorldMovement);
        PlanetFrameVector actual =
                toLocal(frame, actualWorldMovement);
        PlanetFrameVector velocity =
                toLocal(frame, velocityBeforeMove);

        boolean xCollision =
                differs(requested.x(), actual.x());
        boolean yCollision =
                differs(requested.y(), actual.y());
        boolean zCollision =
                differs(requested.z(), actual.z());

        PlanetFrameVector correctedLocalVelocity =
                new PlanetFrameVector(
                        xCollision ? 0.0D : velocity.x(),
                        velocity.y(),
                        zCollision ? 0.0D : velocity.z()
                );

        Vec3 correctedWorldVelocity =
                toWorld(frame, correctedLocalVelocity);

        return new CollisionSemantics(
                yCollision && requested.y() < 0.0D,
                correctedWorldVelocity,
                requested,
                actual
        );
    }

    /**
     * Corrects the two axis-dependent effects performed by Particle.tick after
     * move(): speedUpWhenYMotionIsBlocked and onGround friction.
     *
     * <p>Vanilla has already multiplied physical X/Z when this method is called.
     * Undo those physical-axis multipliers, then apply the same multipliers to
     * local X/Z. The ordinary scalar friction applied to all three axes is not
     * touched.</p>
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

        Vec3 neutral = vanillaVelocityAfterTick;

        if (vanillaBlockedSpeedUpApplied) {
            neutral = new Vec3(
                    neutral.x / VANILLA_BLOCKED_SPEED_UP,
                    neutral.y,
                    neutral.z / VANILLA_BLOCKED_SPEED_UP
            );
        }

        if (onGround) {
            neutral = new Vec3(
                    neutral.x / VANILLA_GROUND_FRICTION,
                    neutral.y,
                    neutral.z / VANILLA_GROUND_FRICTION
            );
        }

        PlanetFrameVector local =
                toLocal(frame, neutral);

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

        return toWorld(
                frame,
                new PlanetFrameVector(
                        localX,
                        localY,
                        localZ
                )
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

        return toLocal(frame, worldDisplacement)
                .y() == 0.0D;
    }

    private static PlanetFrameVector toLocal(
            PlanetGravityFrame frame,
            Vec3 world
    ) {
        return frame.worldToLocal(
                new PlanetFrameVector(
                        world.x,
                        world.y,
                        world.z
                )
        );
    }

    private static Vec3 toWorld(
            PlanetGravityFrame frame,
            PlanetFrameVector local
    ) {
        PlanetFrameVector world =
                frame.localToWorld(local);

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
    }

    private static boolean differs(
            double expected,
            double actual
    ) {
        return Double.compare(expected, actual) != 0;
    }

    public record CollisionSemantics(
            boolean onGround,
            Vec3 correctedWorldVelocity,
            PlanetFrameVector requestedLocal,
            PlanetFrameVector actualLocal
    ) {
    }
}
