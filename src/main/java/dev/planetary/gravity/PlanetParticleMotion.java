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
     * Moves a world-Y position term already applied by vanilla into local Y.
     *
     * <p>This is position math, not velocity math. The untouched part of the
     * trajectory remains in physical world coordinates.</p>
     */
    public static Vec3 rotateAddedWorldYPositionTermToLocalY(
            Vec3 vanillaWorldPosition,
            PlanetGravityFrame frame,
            double vanillaWorldYTerm
    ) {
        Objects.requireNonNull(
                vanillaWorldPosition,
                "vanillaWorldPosition"
        );
        Objects.requireNonNull(frame, "frame");

        Vec3 withoutTerm =
                vanillaWorldPosition.subtract(
                        0.0D,
                        vanillaWorldYTerm,
                        0.0D
                );

        PlanetFrameVector worldTerm =
                frame.localToWorld(
                        new PlanetFrameVector(
                                0.0D,
                                vanillaWorldYTerm,
                                0.0D
                        )
                );

        return withoutTerm.add(
                worldTerm.x(),
                worldTerm.y(),
                worldTerm.z()
        );
    }

    public static Vec3 rotateAddedWorldYTermToLocalY(
            Vec3 currentVelocity,
            PlanetGravityFrame frame,
            double vanillaWorldYTerm
    ) {
        Objects.requireNonNull(
                currentVelocity,
                "currentVelocity"
        );
        Objects.requireNonNull(frame, "frame");

        Vec3 withoutVanillaTerm =
                currentVelocity.subtract(
                        0.0D,
                        vanillaWorldYTerm,
                        0.0D
                );

        return addLocalVelocity(
                withoutVanillaTerm,
                frame,
                0.0D,
                vanillaWorldYTerm,
                0.0D
        );
    }

    public static Vec3 localVelocityToWorld(
            Vec3 localVelocity,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                localVelocity,
                "localVelocity"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localVelocity.x,
                                localVelocity.y,
                                localVelocity.z
                        )
                );

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
    }

    public static Vec3 addLocalVelocity(
            Vec3 physicalVelocity,
            PlanetGravityFrame frame,
            double localX,
            double localY,
            double localZ
    ) {
        Objects.requireNonNull(
                physicalVelocity,
                "physicalVelocity"
        );

        return physicalVelocity.add(
                localVelocityToWorld(
                        new Vec3(
                                localX,
                                localY,
                                localZ
                        ),
                        frame
                )
        );
    }

    /**
     * Difference introduced by rotateVanillaUpBias compared with untouched
     * vanilla Particle constructor output.
     */
    public static Vec3 baseUpBiasCorrection(
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(frame, "frame");

        PlanetVector up =
                frame.worldUp();

        return new Vec3(
                up.x() * VANILLA_UP_BIAS,
                up.y() * VANILLA_UP_BIAS
                        - VANILLA_UP_BIAS,
                up.z() * VANILLA_UP_BIAS
        );
    }

    /**
     * Recovers the velocity vanilla would have had after a subclass applied
     * component-wise scales to Particle's constructor velocity.
     *
     * <p>Use scale 0 for a component overwritten by the subclass.</p>
     */
    public static Vec3 recoverVanillaAfterBaseBiasScales(
            Vec3 currentVelocity,
            PlanetGravityFrame frame,
            double scaleX,
            double scaleY,
            double scaleZ
    ) {
        Objects.requireNonNull(
                currentVelocity,
                "currentVelocity"
        );

        Vec3 correction =
                baseUpBiasCorrection(frame);

        return currentVelocity.subtract(
                correction.x * scaleX,
                correction.y * scaleY,
                correction.z * scaleZ
        );
    }

    /**
     * Reinterprets the velocity delta produced by a custom vanilla tick before
     * Particle.move as LOCAL X/Y/Z semantics.
     *
     * <p>The starting velocity is already physical world velocity. Only the
     * delta authored by the vanilla custom tick is rotated.</p>
     */
    public static Vec3 reinterpretTickDeltaAsLocal(
            Vec3 velocityAtTickStart,
            Vec3 vanillaVelocityBeforeMove,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(
                velocityAtTickStart,
                "velocityAtTickStart"
        );
        Objects.requireNonNull(
                vanillaVelocityBeforeMove,
                "vanillaVelocityBeforeMove"
        );
        Objects.requireNonNull(frame, "frame");

        Vec3 vanillaDelta =
                vanillaVelocityBeforeMove.subtract(
                        velocityAtTickStart
                );

        PlanetFrameVector physicalDelta =
                frame.localToWorld(
                        new PlanetFrameVector(
                                vanillaDelta.x,
                                vanillaDelta.y,
                                vanillaDelta.z
                        )
                );

        return velocityAtTickStart.add(
                physicalDelta.x(),
                physicalDelta.y(),
                physicalDelta.z()
        );
    }

    /**
     * Removes component-wise scales already applied in physical world axes and
     * reapplies equivalent scales in local particle axes.
     */
    public static Vec3 remapComponentScales(
            Vec3 vanillaScaledWorldVelocity,
            PlanetGravityFrame frame,
            double vanillaWorldScaleX,
            double vanillaWorldScaleY,
            double vanillaWorldScaleZ,
            double desiredLocalScaleX,
            double desiredLocalScaleY,
            double desiredLocalScaleZ
    ) {
        Objects.requireNonNull(
                vanillaScaledWorldVelocity,
                "vanillaScaledWorldVelocity"
        );
        Objects.requireNonNull(frame, "frame");

        if (vanillaWorldScaleX == 0.0D
                || vanillaWorldScaleY == 0.0D
                || vanillaWorldScaleZ == 0.0D) {
            throw new IllegalArgumentException(
                    "Cannot undo a zero component scale"
            );
        }

        Vec3 neutralWorld =
                new Vec3(
                        vanillaScaledWorldVelocity.x
                                / vanillaWorldScaleX,
                        vanillaScaledWorldVelocity.y
                                / vanillaWorldScaleY,
                        vanillaScaledWorldVelocity.z
                                / vanillaWorldScaleZ
                );

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                neutralWorld.x,
                                neutralWorld.y,
                                neutralWorld.z
                        )
                );

        PlanetFrameVector correctedWorld =
                frame.localToWorld(
                        new PlanetFrameVector(
                                local.x() * desiredLocalScaleX,
                                local.y() * desiredLocalScaleY,
                                local.z() * desiredLocalScaleZ
                        )
                );

        return new Vec3(
                correctedWorld.x(),
                correctedWorld.y(),
                correctedWorld.z()
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
