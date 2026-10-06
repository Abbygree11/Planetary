package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WhiteAshParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * WhiteAsh provider authors its supplied XYZ speed as local tangent/vertical
 * components, unlike ordinary Smoke providers whose speed arguments are
 * emitter-physical input.
 */
@Mixin(WhiteAshParticle.class)
public abstract class WhiteAshParticleGravityMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateProviderAuthoredVelocity(
            ClientLevel level,
            double x,
            double y,
            double z,
            double localX,
            double localY,
            double localZ,
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

        Vec3 authored =
                new Vec3(
                        localX,
                        localY,
                        localZ
                );
        Vec3 physicalAuthored =
                PlanetParticleMotion.localVelocityToWorld(
                        authored,
                        frameOptional.get()
                );

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Vec3 corrected =
                new Vec3(
                        particle.planetary$getXd(),
                        particle.planetary$getYd(),
                        particle.planetary$getZd()
                ).subtract(
                        authored
                ).add(
                        physicalAuthored
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
