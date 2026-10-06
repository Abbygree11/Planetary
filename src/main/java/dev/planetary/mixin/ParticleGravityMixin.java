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
 * Reorients vanilla particle gravity into the local Planet gravity frame.
 *
 * <p>Vanilla Particle.tick always applies {@code -Y} acceleration. At HEAD we
 * add the opposite +Y acceleration to cancel that upcoming term, then add the
 * same acceleration magnitude along Planet local DOWN. The original tick is
 * otherwise left intact.</p>
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
    protected float gravity;

    @Shadow
    protected boolean onGround;

    @Shadow
    protected boolean speedUpWhenYMotionIsBlocked;

    @Shadow
    protected boolean removed;

    @Unique
    private PlanetGravityFrame planetary$baseTickFrame;

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
    private void planetary$beginLocalBaseTick(
            CallbackInfo ci
    ) {
        this.planetary$baseTickFrame = null;

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
        this.planetary$baseTickFrame = frame;

        if (this.gravity == 0.0F) {
            return;
        }

        double acceleration =
                0.04D * (double) this.gravity;

        // Cancel the hard-coded vanilla world -Y acceleration that tick() is
        // about to apply, then add the same magnitude along local DOWN.
        this.yd += acceleration;

        PlanetVector down =
                frame.worldDown();

        this.xd += down.x() * acceleration;
        this.yd += down.y() * acceleration;
        this.zd += down.z() * acceleration;
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$finishLocalBaseTick(
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$baseTickFrame;
        this.planetary$baseTickFrame = null;

        if (frame == null
                || this.removed) {
            return;
        }

        boolean vanillaBlockedSpeedUpApplied =
                this.speedUpWhenYMotionIsBlocked
                        && this.y == this.yo;

        boolean localBlockedSpeedUpRequired =
                this.speedUpWhenYMotionIsBlocked
                        && PlanetParticleMotion
                        .hasNoLocalVerticalDisplacement(
                                new Vec3(
                                        this.x - this.xo,
                                        this.y - this.yo,
                                        this.z - this.zo
                                ),
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
    }
}
