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
 * Reorients generic vanilla Particle motion semantics into the local Planet
 * gravity frame while keeping physical collision geometry in ordinary XYZ.
 */
@Mixin(Particle.class)
public abstract class ParticleGravityMixin {
    @Shadow
    @Final
    protected ClientLevel level;

    @Shadow
    protected double xo;

    @Shadow
    protected double yo;

    @Shadow
    protected double zo;

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
    protected boolean speedUpWhenYMotionIsBlocked;

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
    private double planetary$moveStartXd;

    @Unique
    private double planetary$moveStartYd;

    @Unique
    private double planetary$moveStartZd;

    @Unique
    private PlanetGravityFrame planetary$tickFrame;

    @Unique
    private boolean planetary$insideBaseTick;

    @Unique
    private boolean planetary$tickMoveObserved;

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
    private void planetary$beginBaseTick(
            CallbackInfo ci
    ) {
        this.planetary$insideBaseTick = true;
        this.planetary$tickMoveObserved = false;
        this.planetary$tickFrame = null;

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
        this.planetary$tickFrame = frame;

        if (this.gravity == 0.0F) {
            return;
        }

        double acceleration =
                0.04D * (double) this.gravity;

        // Cancel the hard-coded vanilla world -Y acceleration that tick() is
        // about to apply, then apply the same magnitude along local DOWN.
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

        if (this.planetary$insideBaseTick
                && this.planetary$tickFrame != null) {
            this.planetary$tickMoveObserved = true;
        }

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

        this.planetary$moveStartXd = this.xd;
        this.planetary$moveStartYd = this.yd;
        this.planetary$moveStartZd = this.zd;
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

        PlanetParticleMotion.CollisionSemantics semantics =
                PlanetParticleMotion.collisionSemantics(
                        requested,
                        actual,
                        new Vec3(
                                this.planetary$moveStartXd,
                                this.planetary$moveStartYd,
                                this.planetary$moveStartZd
                        ),
                        frame
                );

        Vec3 correctedVelocity =
                semantics.correctedWorldVelocity();

        this.xd = correctedVelocity.x;
        this.yd = correctedVelocity.y;
        this.zd = correctedVelocity.z;

        this.onGround = semantics.onGround();

        // Do not reinterpret stoppedByCollision. Unlike onGround it is a
        // sticky internal short-circuit whose vanilla behavior is relied on by
        // particles spawned inside block shapes (notably destroy TerrainParticle).
        this.planetary$moveFrame = null;
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$finishBaseTick(
            CallbackInfo ci
    ) {
        try {
            PlanetGravityFrame frame =
                    this.planetary$tickFrame;

            if (frame == null
                    || !this.planetary$tickMoveObserved) {
                return;
            }

            boolean vanillaBlockedSpeedUpApplied =
                    this.speedUpWhenYMotionIsBlocked
                            && this.y == this.yo;

            Vec3 worldDisplacement =
                    new Vec3(
                            this.x - this.xo,
                            this.y - this.yo,
                            this.z - this.zo
                    );

            boolean localBlockedSpeedUpRequired =
                    this.speedUpWhenYMotionIsBlocked
                            && PlanetParticleMotion
                            .hasNoLocalVerticalDisplacement(
                                    worldDisplacement,
                                    frame
                            );

            if (!vanillaBlockedSpeedUpApplied
                    && !localBlockedSpeedUpRequired
                    && !this.onGround) {
                return;
            }

            Vec3 corrected =
                    PlanetParticleMotion.correctBaseTickAxisEffects(
                            new Vec3(
                                    this.xd,
                                    this.yd,
                                    this.zd
                            ),
                            frame,
                            vanillaBlockedSpeedUpApplied,
                            localBlockedSpeedUpRequired,
                            this.onGround
                    );

            this.xd = corrected.x;
            this.yd = corrected.y;
            this.zd = corrected.z;
        } finally {
            this.planetary$insideBaseTick = false;
            this.planetary$tickMoveObserved = false;
            this.planetary$tickFrame = null;
        }
    }
}
