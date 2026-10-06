package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.particle.BubblePopParticle;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.client.particle.WaterDropParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Vanilla has several particle classes that override Particle.tick() and
 * directly execute {@code yd -= gravity}. They bypass ParticleGravityMixin,
 * so rotate that direct gravity term here for all such gravity-backed classes.
 */
@Mixin({
        DripParticle.class,
        WaterDropParticle.class,
        BubblePopParticle.class,
        WakeParticle.class,
        CampfireSmokeParticle.class
})
public abstract class DirectGravityParticleMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$rotateDirectGravity(
            CallbackInfo ci
    ) {
        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        double acceleration =
                (double) particle.planetary$getGravity();

        if (acceleration == 0.0D) {
            return;
        }

        Optional<PlanetGravityFrame> frame =
                PlanetBlockGravity.frameAt(
                        particle.planetary$getLevel(),
                        particle.planetary$getX(),
                        particle.planetary$getY(),
                        particle.planetary$getZ()
                );

        if (frame.isEmpty()) {
            return;
        }

        // Target tick() is about to subtract exactly gravity from world Y.
        // Cancel that and apply the same vector magnitude along local DOWN.
        particle.planetary$setYd(
                particle.planetary$getYd()
                        + acceleration
        );

        PlanetVector down = frame.get().worldDown();

        particle.planetary$setXd(
                particle.planetary$getXd()
                        + down.x() * acceleration
        );
        particle.planetary$setYd(
                particle.planetary$getYd()
                        + down.y() * acceleration
        );
        particle.planetary$setZd(
                particle.planetary$getZd()
                        + down.z() * acceleration
        );
    }
}
