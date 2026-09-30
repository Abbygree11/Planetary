package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.particle.DripParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * DripParticle overrides Particle.tick(), so the generic particle-gravity
 * mixin never sees water/lava/honey/dripstone drops. Reorient its own
 * hard-coded world -Y acceleration into Planet local DOWN.
 */
@Mixin(DripParticle.class)
public abstract class DripParticleGravityMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$rotateDripGravity(
            CallbackInfo ci
    ) {
        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        float gravity = particle.planetary$getGravity();
        if (gravity == 0.0F) {
            return;
        }

        Optional<PlanetGravityFrame> frame =
                PlanetBlockGravity.frameAt(
                        particle.planetary$getLevel(),
                        particle.planetary$getX(),
                        particle.planetary$getY(),
                        particle.planetary$getZ()
                );

        if (frame.isEmpty()) {
            return;
        }

        double acceleration = (double) gravity;

        // DripParticle.tick() is about to do: yd -= gravity.
        // Cancel that term, then apply the same acceleration along local DOWN.
        particle.planetary$setYd(
                particle.planetary$getYd()
                        + acceleration
        );

        PlanetVector down = frame.get().worldDown();

        particle.planetary$setXd(
                particle.planetary$getXd()
                        + down.x() * acceleration
        );
        particle.planetary$setYd(
                particle.planetary$getYd()
                        + down.y() * acceleration
        );
        particle.planetary$setZd(
                particle.planetary$getZd()
                        + down.z() * acceleration
        );
    }
}
