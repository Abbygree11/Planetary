package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reconstructs subclass-authored local launch velocity after constructors that
 * anisotropically modify Particle's seven-argument constructor velocity.
 */
@Mixin({
        WaterDropParticle.class,
        LavaParticle.class
})
public abstract class LocalAuthoredLaunchParticleMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateGeneratedLaunch(
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

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        double tangentScale;

        if ((Object) this instanceof LavaParticle) {
            tangentScale = (double) 0.8F;
        }
        else {
            tangentScale = (double) 0.3F;
        }

        PlanetGravityFrame frame =
                frameOptional.get();

        Vec3 vanillaFinal =
                PlanetParticleMotion.recoverVanillaAfterBaseBiasScales(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frame,
                        tangentScale,
                        0.0D,
                        tangentScale
                );

        Vec3 corrected =
                PlanetParticleMotion.localVelocityToWorld(
                        vanillaFinal,
                        frame
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
