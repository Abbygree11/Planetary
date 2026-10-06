package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Exact local-frame equivalent of vanilla Particle.move collision response.
 */
public final class PlanetParticleCollisionResponse {
    private static final double VANILLA_STOP_EPSILON =
            1.0E-5D;

    private PlanetParticleCollisionResponse() {
    }

    public static Result apply(
            Vec3 requestedWorldMovement,
            Vec3 actualWorldMovement,
            Vec3 currentWorldVelocity,
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
                currentWorldVelocity,
                "currentWorldVelocity"
        );
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector requested =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                requestedWorldMovement.x,
                                requestedWorldMovement.y,
                                requestedWorldMovement.z
                        )
                );
        PlanetFrameVector actual =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                actualWorldMovement.x,
                                actualWorldMovement.y,
                                actualWorldMovement.z
                        )
                );
        PlanetFrameVector velocity =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                currentWorldVelocity.x,
                                currentWorldVelocity.y,
                                currentWorldVelocity.z
                        )
                );

        boolean stoppedByCollision =
                Math.abs(requested.y())
                        >= VANILLA_STOP_EPSILON
                        && Math.abs(actual.y())
                        < VANILLA_STOP_EPSILON;

        boolean onGround =
                Double.compare(
                        requested.y(),
                        actual.y()
                ) != 0
                        && requested.y() < 0.0D;

        double correctedX =
                Double.compare(
                        requested.x(),
                        actual.x()
                ) != 0
                        ? 0.0D
                        : velocity.x();

        double correctedZ =
                Double.compare(
                        requested.z(),
                        actual.z()
                ) != 0
                        ? 0.0D
                        : velocity.z();

        PlanetFrameVector correctedWorld =
                frame.localToWorld(
                        new PlanetFrameVector(
                                correctedX,
                                velocity.y(),
                                correctedZ
                        )
                );

        return new Result(
                stoppedByCollision,
                onGround,
                new Vec3(
                        correctedWorld.x(),
                        correctedWorld.y(),
                        correctedWorld.z()
                )
        );
    }

    public record Result(
            boolean stoppedByCollision,
            boolean onGround,
            Vec3 correctedWorldVelocity
    ) {
    }
}
