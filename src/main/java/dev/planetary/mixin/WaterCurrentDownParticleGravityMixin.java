package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.WaterCurrentDownParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Moves WaterCurrentDownParticle's spiral in the local tangent plane.
 */
@Mixin(WaterCurrentDownParticle.class)
public abstract class WaterCurrentDownParticleGravityMixin {
    @Unique
    private PlanetGravityFrame planetary$tickFrame;

    @Unique
    private double planetary$startXd;

    @Unique
    private double planetary$startYd;

    @Unique
    private double planetary$startZd;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$captureCurrentVelocity(
            CallbackInfo ci
    ) {
        this.planetary$tickFrame = null;

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

        this.planetary$tickFrame =
                frameOptional.get();
        this.planetary$startXd =
                particle.planetary$getXd();
        this.planetary$startYd =
                particle.planetary$getYd();
        this.planetary$startZd =
                particle.planetary$getZd();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/Particle;move(DDD)V"
            )
    )
    private void planetary$rotateCurrentSpiral(
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$tickFrame;

        if (frame == null) {
            return;
        }

        PlanetFrameVector localStart =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                this.planetary$startXd,
                                this.planetary$startYd,
                                this.planetary$startZd
                        )
                );

        float angle =
                ((WaterCurrentDownParticleAccessor) (Object) this)
                        .planetary$getAngle();

        Vec3 corrected =
                PlanetParticleMotion.localVelocityToWorld(
                        new Vec3(
                                (localStart.x()
                                        + (double) (
                                        0.6F * Mth.cos(angle)
                                )) * 0.07D,
                                localStart.y(),
                                (localStart.z()
                                        + (double) (
                                        0.6F * Mth.sin(angle)
                                )) * 0.07D
                        ),
                        frame
                );

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;
        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
