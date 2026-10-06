package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.BubblePopParticle;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.client.particle.WaterCurrentDownParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Rotates the semantic velocity delta authored by selected custom tick methods
 * before they dispatch to the base Particle.move implementation.
 *
 * <p>The vanilla lifecycle/RNG/control flow remains untouched. Only the
 * tick-authored delta is reinterpreted from vanilla local X/Y/Z into physical
 * Planet world coordinates.</p>
 */
@Mixin({
        DripParticle.class,
        WaterDropParticle.class,
        BubblePopParticle.class,
        WakeParticle.class,
        CampfireSmokeParticle.class,
        BubbleParticle.class,
        WaterCurrentDownParticle.class
})
public abstract class SemanticTickDeltaParticleMixin {
    @Unique
    private PlanetGravityFrame planetary$tickFrame;

    @Unique
    private double planetary$tickStartXd;

    @Unique
    private double planetary$tickStartYd;

    @Unique
    private double planetary$tickStartZd;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$captureTickVelocity(
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
        this.planetary$tickStartXd =
                particle.planetary$getXd();
        this.planetary$tickStartYd =
                particle.planetary$getYd();
        this.planetary$tickStartZd =
                particle.planetary$getZd();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/Particle;move(DDD)V"
            )
    )
    private void planetary$reinterpretTickDeltaBeforeMove(
            CallbackInfo ci
    ) {
        PlanetGravityFrame frame =
                this.planetary$tickFrame;

        if (frame == null) {
            return;
        }

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Vec3 corrected =
                PlanetParticleMotion.reinterpretTickDeltaAsLocal(
                        new Vec3(
                                this.planetary$tickStartXd,
                                this.planetary$tickStartYd,
                                this.planetary$tickStartZd
                        ),
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frame
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
