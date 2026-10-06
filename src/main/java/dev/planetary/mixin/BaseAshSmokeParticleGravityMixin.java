package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Moves BaseAshSmokeParticle's constructor component scaling into local axes.
 *
 * <p>Explicit speed parameters are treated as already-physical emitter input;
 * only the base Particle-generated velocity is interpreted as local XYZ.</p>
 */
@Mixin(BaseAshSmokeParticle.class)
public abstract class BaseAshSmokeParticleGravityMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateBaseGeneratedVelocity(
            ClientLevel level,
            double x,
            double y,
            double z,
            float scaleX,
            float scaleY,
            float scaleZ,
            double addX,
            double addY,
            double addZ,
            float quadScale,
            SpriteSet sprites,
            float lifetimeScale,
            int baseLifetime,
            float gravity,
            boolean hasPhysics,
            CallbackInfo ci
    ) {
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

        PlanetGravityFrame frame =
                frameOptional.get();
        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Vec3 vanillaFinal =
                PlanetParticleMotion.recoverVanillaAfterBaseBiasScales(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frame,
                        (double) scaleX,
                        (double) scaleY,
                        (double) scaleZ
                );

        Vec3 additions =
                new Vec3(
                        addX,
                        addY,
                        addZ
                );
        Vec3 vanillaScaledBase =
                vanillaFinal.subtract(
                        additions
                );

        Vec3 corrected =
                PlanetParticleMotion.localVelocityToWorld(
                        vanillaScaledBase,
                        frame
                ).add(
                        additions
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
