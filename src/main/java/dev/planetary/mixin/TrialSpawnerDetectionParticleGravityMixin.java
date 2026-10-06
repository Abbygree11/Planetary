package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TrialSpawnerDetectionParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Rotates TrialSpawnerDetectionParticle's retained base vertical launch into
 * local Y. Standard vanilla emitters pass zero external velocity.
 */
@Mixin(TrialSpawnerDetectionParticle.class)
public abstract class TrialSpawnerDetectionParticleGravityMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateTrialDetectionLaunch(
            ClientLevel level,
            double x,
            double y,
            double z,
            double inputX,
            double inputY,
            double inputZ,
            float scale,
            SpriteSet sprites,
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
                        0.0D,
                        (double) 0.9F,
                        0.0D
                );

        // Standard vanilla path supplies zero input. Preserve non-zero external
        // input physically, and rotate only the constructor-retained base term.
        Vec3 external =
                new Vec3(
                        inputX,
                        inputY,
                        inputZ
                );
        Vec3 internalVanilla =
                vanillaFinal.subtract(
                        external
                );
        Vec3 corrected =
                external.add(
                        PlanetParticleMotion.localVelocityToWorld(
                                internalVanilla,
                                frame
                        )
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
