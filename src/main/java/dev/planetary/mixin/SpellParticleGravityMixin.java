package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients SpellParticle's constructor-authored tangent/vertical velocity.
 */
@Mixin(SpellParticle.class)
public abstract class SpellParticleGravityMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void planetary$rotateSpellConstructorVelocity(
            ClientLevel level,
            double x,
            double y,
            double z,
            double inputX,
            double inputY,
            double inputZ,
            SpriteSet sprites,
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

        PlanetGravityFrame frame =
                frameOptional.get();

        boolean vanillaNoTangentInput =
                inputX == 0.0D
                        && inputZ == 0.0D;
        double vanillaTangentScale =
                vanillaNoTangentInput
                        ? (double) 0.1F
                        : 1.0D;

        ParticleGravityAccessor particle =
                (ParticleGravityAccessor) (Object) this;

        Vec3 vanillaFinal =
                PlanetParticleMotion.recoverVanillaAfterBaseBiasScales(
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        frame,
                        vanillaTangentScale,
                        (double) 0.2F,
                        vanillaTangentScale
                );

        PlanetFrameVector localInput =
                frame.worldToLocal(
                        new PlanetFrameVector(
                                inputX,
                                inputY,
                                inputZ
                        )
                );

        boolean localNoTangentInput =
                localInput.x() == 0.0D
                        && localInput.z() == 0.0D;
        double desiredTangentScale =
                localNoTangentInput
                        ? (double) 0.1F
                        : 1.0D;

        Vec3 localVanilla =
                new Vec3(
                        vanillaFinal.x
                                / vanillaTangentScale
                                * desiredTangentScale,
                        vanillaFinal.y,
                        vanillaFinal.z
                                / vanillaTangentScale
                                * desiredTangentScale
                );

        Vec3 corrected =
                PlanetParticleMotion.localVelocityToWorld(
                        localVanilla,
                        frame
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);
    }
}
