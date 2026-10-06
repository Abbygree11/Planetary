package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients SplashParticle's explicit +0.1 launch when it overrides the
 * WaterDropParticle constructor velocity.
 */
@Mixin(SplashParticle.class)
public abstract class SplashParticleGravityMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateSplashLaunch(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            CallbackInfo ci
    ) {
        if (ySpeed != 0.0D
                || (xSpeed == 0.0D
                && zSpeed == 0.0D)) {
            return;
        }

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        level,
                        x,
                        y,
                        z
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Vec3 corrected =
                PlanetParticleMotion.rotateAddedWorldYTermToLocalY(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frameOptional.get(),
                        0.1D
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
