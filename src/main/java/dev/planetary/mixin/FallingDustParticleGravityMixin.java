package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.particle.FallingDustParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * FallingDustParticle has its own fixed world-Y acceleration and does not use
 * Particle.gravity. Rotate that acceleration into local DOWN and cap terminal
 * speed along the local gravity axis instead of world Y.
 */
@Mixin(FallingDustParticle.class)
public abstract class FallingDustParticleGravityMixin {
    private static final double ACCELERATION = 0.003D;
    private static final double MAX_DOWN_SPEED = 0.14D;

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$rotateDustGravity(
            CallbackInfo ci
    ) {
        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        particle.planetary$getLevel(),
                        particle.planetary$getX(),
                        particle.planetary$getY(),
                        particle.planetary$getZ()
                );

        if (frameOptional.isEmpty()) {
            return;
        }

        PlanetVector down =
                frameOptional.get().worldDown();

        // Vanilla just applied yd -= 0.003. Undo that world-Y term.
        double xd = particle.planetary$getXd();
        double yd = particle.planetary$getYd()
                + ACCELERATION;
        double zd = particle.planetary$getZd();

        // Apply the same acceleration in local DOWN.
        xd += down.x() * ACCELERATION;
        yd += down.y() * ACCELERATION;
        zd += down.z() * ACCELERATION;

        double downSpeed =
                xd * down.x()
                        + yd * down.y()
                        + zd * down.z();

        if (downSpeed > MAX_DOWN_SPEED) {
            double excess =
                    downSpeed - MAX_DOWN_SPEED;
            xd -= down.x() * excess;
            yd -= down.y() * excess;
            zd -= down.z() * excess;
        }

        particle.planetary$setXd(xd);
        particle.planetary$setYd(yd);
        particle.planetary$setZd(zd);
    }
}
