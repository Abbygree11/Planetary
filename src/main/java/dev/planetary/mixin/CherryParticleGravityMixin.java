package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetCherryParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.particle.CherryParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Full local-frame equivalent of CherryParticle.tick on rotated Planet faces.
 *
 * <p>Vanilla cherry petals use world X/Z as the wind plane, world -Y as
 * gravity, and xd/zd as the two horizontal collision-stop axes. Those three
 * assumptions must rotate together.</p>
 */
@Mixin(CherryParticle.class)
public abstract class CherryParticleGravityMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$tickCherryInLocalFrame(
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

        ci.cancel();

        PlanetGravityFrame frame =
                frameOptional.get();
        CherryParticleAccessor cherry =
                (CherryParticleAccessor) (Object) this;
        Particle vanillaParticle =
                (Particle) (Object) this;

        particle.planetary$setXo(
                particle.planetary$getX()
        );
        particle.planetary$setYo(
                particle.planetary$getY()
        );
        particle.planetary$setZo(
                particle.planetary$getZ()
        );

        int lifetimeBefore =
                particle.planetary$getLifetime();
        particle.planetary$setLifetime(
                lifetimeBefore - 1
        );

        if (lifetimeBefore <= 0) {
            vanillaParticle.remove();
            return;
        }

        int lifetime =
                lifetimeBefore - 1;

        Vec3 wind =
                PlanetCherryParticleMotion.windAcceleration(
                        frame,
                        cherry.planetary$getParticleRandom(),
                        lifetime
                );

        particle.planetary$setXd(
                particle.planetary$getXd()
                        + wind.x
        );
        particle.planetary$setYd(
                particle.planetary$getYd()
                        + wind.y
        );
        particle.planetary$setZd(
                particle.planetary$getZd()
                        + wind.z
        );

        double gravity =
                (double) particle.planetary$getGravity();
        PlanetVector down =
                frame.worldDown();

        particle.planetary$setXd(
                particle.planetary$getXd()
                        + down.x() * gravity
        );
        particle.planetary$setYd(
                particle.planetary$getYd()
                        + down.y() * gravity
        );
        particle.planetary$setZd(
                particle.planetary$getZd()
                        + down.z() * gravity
        );

        float rotSpeed =
                cherry.planetary$getRotSpeed()
                        + cherry.planetary$getSpinAcceleration()
                        / 20.0F;
        cherry.planetary$setRotSpeed(
                rotSpeed
        );

        particle.planetary$setORoll(
                particle.planetary$getRoll()
        );
        particle.planetary$setRoll(
                particle.planetary$getRoll()
                        + rotSpeed / 20.0F
        );

        vanillaParticle.move(
                particle.planetary$getXd(),
                particle.planetary$getYd(),
                particle.planetary$getZd()
        );

        Vec3 currentVelocity =
                new Vec3(
                        particle.planetary$getXd(),
                        particle.planetary$getYd(),
                        particle.planetary$getZd()
                );

        if (PlanetCherryParticleMotion.shouldRemove(
                frame,
                particle.planetary$isOnGround(),
                lifetime,
                currentVelocity
        )) {
            vanillaParticle.remove();
            return;
        }

        double friction =
                (double) particle.planetary$getFriction();

        particle.planetary$setXd(
                particle.planetary$getXd()
                        * friction
        );
        particle.planetary$setYd(
                particle.planetary$getYd()
                        * friction
        );
        particle.planetary$setZd(
                particle.planetary$getZd()
                        * friction
        );
    }
}
