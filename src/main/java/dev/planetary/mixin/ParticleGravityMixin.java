package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Reorients only gravity-local Particle semantics while preserving vanilla
 * physical collision internals.
 */
@Mixin(Particle.class)
public abstract class ParticleGravityMixin {
    @Shadow
    @Final
    protected ClientLevel level;

    @Shadow
    protected double x;

    @Shadow
    protected double y;

    @Shadow
    protected double z;

    @Shadow
    protected double xd;

    @Shadow
    protected double yd;

    @Shadow
    protected double zd;

    @Shadow
    protected boolean onGround;

    @Shadow
    private boolean stoppedByCollision;

    @Shadow
    protected float gravity;

    @Unique
    private PlanetGravityFrame planetary$moveFrame;

    @Unique
    private double planetary$moveStartX;

    @Unique
    private double planetary$moveStartY;

    @Unique
    private double planetary$moveStartZ;

    @Unique
    private PlanetGravityFrame planetary$groundFrameThisTick;

    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDD)V",
            at = @At("RETURN")
    )
    private void planetary$rotateInitialUpBias(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
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

        Vec3 corrected =
                PlanetParticleMotion.rotateVanillaUpBias(
                        new Vec3(
                                this.xd,
                                this.yd,
                                this.zd
                        ),
                        frameOptional.get()
                );

        this.xd = corrected.x;
        this.yd = corrected.y;
        this.zd = corrected.z;
    }

    @Inject(
            method = "setPower(F)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$scaleAroundLocalUpBias(
            float power,
            CallbackInfoReturnable<Particle> cir
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        this.level,
                        this.x,
                        this.y,
                        this.z
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        Vec3 scaled =
                PlanetParticleMotion.scaleAroundLocalUpBias(
                        new Vec3(
                                this.xd,
                                this.yd,
                                this.zd
                        ),
                        power,
                        frameOptional.get()
                );

        this.xd = scaled.x;
        this.yd = scaled.y;
        this.zd = scaled.z;

        cir.setReturnValue(
                (Particle) (Object) this
        );
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$beginTick(
            CallbackInfo ci
    ) {
        this.planetary$groundFrameThisTick = null;

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        this.level,
                        this.x,
                        this.y,
                        this.z
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        PlanetGravityFrame frame =
                frameOptional.get();

        if (this.gravity == 0.0F) {
            return;
        }

        double acceleration =
                0.04D * (double) this.gravity;

        // Cancel the hard-coded vanilla -Y acceleration that tick() is about
        // to apply, then add the same acceleration along local DOWN.
        this.yd += acceleration;

        PlanetVector down =
                frame.worldDown();

        this.xd += down.x() * acceleration;
        this.yd += down.y() * acceleration;
        this.zd += down.z() * acceleration;
    }

    @Inject(
            method = "move(DDD)V",
            at = @At("HEAD")
    )
    private void planetary$beginMove(
            double requestedX,
            double requestedY,
            double requestedZ,
            CallbackInfo ci
    ) {
        this.planetary$moveFrame = null;

        // Preserve vanilla's sticky collision short-circuit exactly.
        if (this.stoppedByCollision) {
            return;
        }

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        this.level,
                        this.x,
                        this.y,
                        this.z
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        this.planetary$moveFrame =
                frameOptional.get();
        this.planetary$moveStartX = this.x;
        this.planetary$moveStartY = this.y;
        this.planetary$moveStartZ = this.z;
    }

    @Inject(
            method = "move(DDD)V",
            at = @At("RETURN")
    )
    private void planetary$finishMove(
            double requestedX,
            double requestedY,
            double requestedZ,
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$moveFrame;

        if (frame == null) {
            return;
        }

        Vec3 requested =
                new Vec3(
                        requestedX,
                        requestedY,
                        requestedZ
                );

        Vec3 actual =
                new Vec3(
                        this.x - this.planetary$moveStartX,
                        this.y - this.planetary$moveStartY,
                        this.z - this.planetary$moveStartZ
                );

        this.onGround =
                PlanetParticleMotion.isLocalGroundCollision(
                        requested,
                        actual,
                        frame
                );

        if (this.onGround) {
            this.planetary$groundFrameThisTick = frame;

            // Reproduce only vanilla's sticky stop on a confirmed LOCAL-DOWN
            // landing. Do not generalize stoppedByCollision to head/tangent
            // collisions and do not alter collision velocity components.
            if (PlanetParticleMotion
                    .shouldStopAfterLocalGroundCollision(
                            requested,
                            actual,
                            frame
                    )) {
                this.stoppedByCollision = true;
            }
        }

        this.planetary$moveFrame = null;
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$finishTick(
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$groundFrameThisTick;

        try {
            if (frame == null
                    || !this.onGround) {
                return;
            }

            Vec3 corrected =
                    PlanetParticleMotion.correctGroundFriction(
                            new Vec3(
                                    this.xd,
                                    this.yd,
                                    this.zd
                            ),
                            frame
                    );

            this.xd = corrected.x;
            this.yd = corrected.y;
            this.zd = corrected.z;
        } finally {
            this.planetary$groundFrameThisTick = null;
        }
    }
}
