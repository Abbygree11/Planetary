package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Corrects WaterDropParticle's explicit on-ground X/Z damping.
 *
 * <p>Fluid/block-surface removal remains a Phase-5 integration gate.</p>
 */
@Mixin(WaterDropParticle.class)
public abstract class WaterDropParticleGravityMixin {
    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$rotateGroundDamping(
            CallbackInfo ci
    ) {
        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        if (!particle.planetary$isOnGround()) {
            return;
        }

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        particle.planetary$getLevel(),
                        particle.planetary$getX(),
                        particle.planetary$getY(),
                        particle.planetary$getZ()
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        double groundScale =
                (double) 0.7F;

        Vec3 corrected =
                PlanetParticleMotion.remapComponentScales(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frameOptional.get(),
                        groundScale,
                        1.0D,
                        groundScale,
                        groundScale,
                        1.0D,
                        groundScale
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
