package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
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
    private double planetary$actualMoveX;

    @Unique
    private double planetary$actualMoveY;

    @Unique
    private double planetary$actualMoveZ;

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

        // Default to the requested movement. If vanilla actually invokes its
        // collision solver, the Redirect below replaces these with the exact
        // clipped Vec3 returned by Entity.collideBoundingBox.
        this.planetary$actualMoveX = requestedX;
        this.planetary$actualMoveY = requestedY;
        this.planetary$actualMoveZ = requestedZ;
    }

    @Redirect(
            method = "move(DDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 planetary$captureExactCollisionResult(
            Entity entity,
            Vec3 requestedMovement,
            AABB box,
            Level level,
            List<VoxelShape> collisions
    ) {
        Vec3 actualMovement =
                Entity.collideBoundingBox(
                        entity,
                        requestedMovement,
                        box,
                        level,
                        collisions
                );

        if (this.planetary$moveFrame != null) {
            this.planetary$actualMoveX =
                    actualMovement.x;
            this.planetary$actualMoveY =
                    actualMovement.y;
            this.planetary$actualMoveZ =
                    actualMovement.z;
        }

        return actualMovement;
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
                        this.planetary$actualMoveX,
                        this.planetary$actualMoveY,
                        this.planetary$actualMoveZ
                );

        this.onGround =
                PlanetParticleMotion.isLocalGroundCollision(
                        requested,
                        actual,
                        frame
                );

        if (this.onGround) {
            this.planetary$groundFrameThisTick = frame;

            // The exact vanilla collision result proves that LOCAL-DOWN
            // movement was clipped by real geometry. Stop future movement on
            // this first landing tick so rotated faces do not visibly acquire
            // an extra surface-sliding phase.
            this.stoppedByCollision = true;
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
