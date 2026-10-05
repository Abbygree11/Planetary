package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Narrow local-frame adaptations for generic vanilla Particle motion.
 *
 * <p>Only semantics proven to represent gravity-local UP/DOWN are adapted
 * here. Particle's internal collision short-circuit and velocity clipping stay
 * vanilla/physical.</p>
 */
public final class PlanetParticleMotion {
    public static final double VANILLA_UP_BIAS = 0.1D;
    private static final double VANILLA_GROUND_FRICTION =
            (double) 0.7F;
    private static final double VANILLA_STOP_EPSILON =
            1.0E-5D;

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
     * Exact local-frame equivalent of vanilla:
     *
     * <pre>
     * onGround = requestedY != actualY && requestedY < 0
     * </pre>
     *
     * <p>The collision solver itself remains completely vanilla and physical.
     * We only reinterpret its requested/actual displacement after the move.</p>
     */
    public static boolean isLocalGroundCollision(
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

        PlanetFrameVector requested =
                toLocal(frame, requestedWorldMovement);
        PlanetFrameVector actual =
                toLocal(frame, actualWorldMovement);

        return Double.compare(
                        requested.y(),
                        actual.y()
                ) != 0
                && requested.y() < 0.0D;
    }

    /**
     * Local equivalent of vanilla's sticky vertical collision stop, restricted
     * to a confirmed LOCAL-DOWN landing.
     *
     * <p>Vanilla sets stoppedByCollision when requested world-Y movement is
     * non-trivial and the actual world-Y movement becomes ~0. On +Y ground
     * contact this makes particles stop moving entirely on later ticks.
     *
     * <p>For rotated gravity we only reproduce that condition after the same
     * movement has already been classified as a local ground collision. This
     * deliberately excludes local-UP/head collisions and all tangent
     * collisions.</p>
     */
    public static boolean shouldStopAfterLocalGroundCollision(
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

        PlanetFrameVector requested =
                toLocal(frame, requestedWorldMovement);
        PlanetFrameVector actual =
                toLocal(frame, actualWorldMovement);

        return requested.y() < 0.0D
                && Math.abs(requested.y())
                >= VANILLA_STOP_EPSILON
                && Math.abs(actual.y())
                < VANILLA_STOP_EPSILON;
    }

    /**
     * Particle.tick has already applied vanilla ground friction to physical
     * world X/Z. For side gravity those are not both local ground tangents.
     *
     * <p>Undo only that physical-X/Z multiplier and reapply the exact vanilla
     * 0.7F multiplier to local X/Z. For +/-Y gravity the vanilla tangent plane
     * already is world X/Z, so the velocity is returned unchanged.</p>
     */
    public static Vec3 correctGroundFriction(
            Vec3 vanillaVelocityAfterGroundFriction,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                vanillaVelocityAfterGroundFriction,
                "vanillaVelocityAfterGroundFriction"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetVector down = frame.worldDown();

        if (down.y() != 0) {
            return vanillaVelocityAfterGroundFriction;
        }

        Vec3 beforeVanillaGroundFriction =
                new Vec3(
                        vanillaVelocityAfterGroundFriction.x
                                / VANILLA_GROUND_FRICTION,
                        vanillaVelocityAfterGroundFriction.y,
                        vanillaVelocityAfterGroundFriction.z
                                / VANILLA_GROUND_FRICTION
                );

        PlanetFrameVector local =
                toLocal(
                        frame,
                        beforeVanillaGroundFriction
                );

        return toWorld(
                frame,
                new PlanetFrameVector(
                        local.x()
                                * VANILLA_GROUND_FRICTION,
                        local.y(),
                        local.z()
                                * VANILLA_GROUND_FRICTION
                )
        );
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
}
