package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients CampfireSmokeParticle's constructor-added random world-UP rise.
 *
 * <p>The supplied velocity is already physical emitter velocity; only the
 * subclass-authored delta is local semantic velocity.</p>
 */
@Mixin(CampfireSmokeParticle.class)
public abstract class CampfireSmokeParticleGravityMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateConstructorRise(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            boolean signal,
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

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Vec3 corrected =
                PlanetParticleMotion.reinterpretTickDeltaAsLocal(
                        new Vec3(
                                xSpeed,
                                ySpeed,
                                zSpeed
                        ),
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frameOptional.get()
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
