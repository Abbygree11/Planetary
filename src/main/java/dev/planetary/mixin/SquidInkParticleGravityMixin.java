package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SquidInkParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients SquidInkParticle's extra in-air world -Y acceleration.
 */
@Mixin(SquidInkParticle.class)
public abstract class SquidInkParticleGravityMixin {
    private static final double AIR_FALL_ACCELERATION =
            (double) 0.0074F;

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$rotateAirFallAcceleration(
            CallbackInfo ci
    ) {
        Particle particleObject =
                (Particle) (Object) this;

        if (!particleObject.isAlive()) {
            return;
        }

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        if (!particle.planetary$getLevel()
                .getBlockState(
                        BlockPos.containing(
                                particle.planetary$getX(),
                                particle.planetary$getY(),
                                particle.planetary$getZ()
                        )
                )
                .isAir()) {
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

        Vec3 corrected =
                PlanetParticleMotion.rotateAddedWorldYTermToLocalY(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frameOptional.get(),
                        -AIR_FALL_ACCELERATION
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
