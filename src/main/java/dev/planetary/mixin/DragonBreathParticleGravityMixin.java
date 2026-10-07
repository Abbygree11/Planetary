package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Adapts only DragonBreathParticle's custom vertical/tangent semantics while
 * preserving its vanilla lifecycle, sprite selection and control flow.
 */
@Mixin(DragonBreathParticle.class)
public abstract class DragonBreathParticleGravityMixin {
    @Unique
    private PlanetGravityFrame planetary$tickFrame;

    @Unique
    private double planetary$startX;

    @Unique
    private double planetary$startY;

    @Unique
    private double planetary$startZ;

    @Unique
    private double planetary$startXd;

    @Unique
    private double planetary$startYd;

    @Unique
    private double planetary$startZd;

    @Unique
    private boolean planetary$startedOnGround;

    @Unique
    private boolean planetary$moved;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$captureDragonTick(
            CallbackInfo ci
    ) {
        this.planetary$tickFrame = null;
        this.planetary$moved = false;

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
        this.planetary$startX =
                particle.planetary$getX();
        this.planetary$startY =
                particle.planetary$getY();
        this.planetary$startZ =
                particle.planetary$getZ();
        this.planetary$startXd =
                particle.planetary$getXd();
        this.planetary$startYd =
                particle.planetary$getYd();
        this.planetary$startZd =
                particle.planetary$getZd();
        this.planetary$startedOnGround =
                particle.planetary$isOnGround();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/DragonBreathParticle;move(DDD)V"
            )
    )
    private void planetary$prepareDragonVelocityBeforeMove(
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$tickFrame;

        if (frame == null) {
            return;
        }

        this.planetary$moved = true;

        PlanetFrameVector local =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                this.planetary$startXd,
                                this.planetary$startYd,
                                this.planetary$startZd
                        )
                );

        double localY =
                this.planetary$startedOnGround
                        ? 0.0D
                        : local.y();

        DragonBreathParticleAccessor dragon =
                (DragonBreathParticleAccessor) (Object) this;

        if (dragon.planetary$hasHitGround()) {
            localY += 0.002D;
        }

        PlanetFrameVector physical =
                frame.localToWorld(
                        new PlanetFrameVector(
                                local.x(),
                                localY,
                                local.z()
                        )
                );

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;
        particle.planetary$setXd(
                physical.x()
        );
        particle.planetary$setYd(
                physical.y()
        );
        particle.planetary$setZd(
                physical.z()
        );
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$correctDragonPostMoveAxes(
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$tickFrame;
        this.planetary$tickFrame = null;

        if (frame == null
                || !this.planetary$moved) {
            return;
        }

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;
        DragonBreathParticleAccessor dragon =
                (DragonBreathParticleAccessor) (Object) this;

        boolean vanillaBlocked =
                particle.planetary$getY()
                        == this.planetary$startY;

        boolean localBlocked =
                PlanetParticleMotion
                        .hasNoLocalVerticalDisplacement(
                                new Vec3(
                                        particle.planetary$getX()
                                                - this.planetary$startX,
                                        particle.planetary$getY()
                                                - this.planetary$startY,
                                        particle.planetary$getZ()
                                                - this.planetary$startZ
                                ),
                                frame
                        );

        double friction =
                (double) particle.planetary$getFriction();
        boolean hasHitGround =
                dragon.planetary$hasHitGround();

        double vanillaTangentScale =
                friction
                        * (vanillaBlocked
                        ? 1.1D
                        : 1.0D);
        double vanillaVerticalScale =
                hasHitGround
                        ? friction
                        : 1.0D;

        double desiredTangentScale =
                friction
                        * (localBlocked
                        ? 1.1D
                        : 1.0D);
        double desiredVerticalScale =
                hasHitGround
                        ? friction
                        : 1.0D;

        Vec3 corrected =
                PlanetParticleMotion.remapComponentScales(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frame,
                        vanillaTangentScale,
                        vanillaVerticalScale,
                        vanillaTangentScale,
                        desiredTangentScale,
                        desiredVerticalScale,
                        desiredTangentScale
                );

        particle.planetary$setXd(
                corrected.x
        );
        particle.planetary$setYd(
                corrected.y
        );
        particle.planetary$setZd(
                corrected.z
        );
    }
}
