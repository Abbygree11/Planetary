package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Moves SnowflakeParticle's anisotropic post-tick damping from world Y/XZ into
 * local vertical/tangent axes.
 */
@Mixin(SnowflakeParticle.class)
public abstract class SnowflakeParticleGravityMixin {
    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$rotateSnowflakeDamping(
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

        double tangentScale =
                (double) 0.95F;
        double verticalScale =
                (double) 0.9F;

        Vec3 corrected =
                PlanetParticleMotion.remapComponentScales(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frameOptional.get(),
                        tangentScale,
                        verticalScale,
                        tangentScale,
                        tangentScale,
                        verticalScale,
                        tangentScale
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
