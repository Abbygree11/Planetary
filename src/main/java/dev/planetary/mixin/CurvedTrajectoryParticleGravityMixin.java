package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.FlyTowardsPositionParticle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients only the explicit world-Y arc authored by custom trajectory ticks.
 *
 * <p>The base start/vector interpolation remains physical world geometry.</p>
 */
@Mixin({
        PortalParticle.class,
        FlyTowardsPositionParticle.class
})
public abstract class CurvedTrajectoryParticleGravityMixin {
    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void planetary$rotateVerticalArc(
            CallbackInfo ci
    ) {
        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        if (particle.planetary$getAge()
                > particle.planetary$getLifetime()) {
            return;
        }

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

        float ageFraction =
                (float) particle.planetary$getAge()
                        / (float) particle.planetary$getLifetime();

        double verticalTerm;

        if ((Object) this instanceof PortalParticle) {
            verticalTerm =
                    (double) (1.0F - ageFraction);
        }
        else {
            float inverse =
                    1.0F - ageFraction;
            float progress =
                    1.0F - inverse;
            progress *= progress;
            progress *= progress;
            verticalTerm =
                    -(double) (progress * 1.2F);
        }

        Vec3 corrected =
                PlanetParticleMotion
                        .rotateAddedWorldYPositionTermToLocalY(
                                new Vec3(
                                        particle.planetary$getX(),
                                        particle.planetary$getY(),
                                        particle.planetary$getZ()
                                ),
                                frameOptional.get(),
                                verticalTerm
                        );

        particle.planetary$setX(corrected.x);
        particle.planetary$setY(corrected.y);
        particle.planetary$setZ(corrected.z);
    }
}
