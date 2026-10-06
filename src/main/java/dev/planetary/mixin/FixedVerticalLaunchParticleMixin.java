package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.DustPlumeParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.NoteParticle;
import net.minecraft.client.particle.ShriekParticle;
import net.minecraft.client.particle.WaterCurrentDownParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients fixed subclass-authored world-Y launch terms after construction.
 */
@Mixin({
        HeartParticle.class,
        NoteParticle.class,
        DustPlumeParticle.class,
        ShriekParticle.class,
        WaterCurrentDownParticle.class
})
public abstract class FixedVerticalLaunchParticleMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateFixedVerticalLaunch(
            CallbackInfo ci
    ) {
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

        double verticalTerm;

        if ((Object) this instanceof HeartParticle) {
            verticalTerm = 0.1D;
        }
        else if ((Object) this instanceof NoteParticle) {
            verticalTerm = 0.2D;
        }
        else if ((Object) this instanceof ShriekParticle) {
            verticalTerm = 0.1D;
        }
        else if ((Object) this instanceof DustPlumeParticle) {
            verticalTerm = 0.15D;
        }
        else if ((Object) this instanceof WaterCurrentDownParticle) {
            verticalTerm = -0.05D;
        }
        else {
            return;
        }

        Vec3 corrected =
                PlanetParticleMotion.rotateAddedWorldYTermToLocalY(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frameOptional.get(),
                        verticalTerm
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
