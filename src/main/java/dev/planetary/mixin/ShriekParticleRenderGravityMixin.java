package dev.planetary.mixin;

import dev.planetary.client.render.PlanetRenderTransform;
import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.ShriekParticle;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

/**
 * Composes ShriekParticle's two vanilla local quad rotations with the active
 * Planet local->world frame.
 */
@Mixin(ShriekParticle.class)
public abstract class ShriekParticleRenderGravityMixin {
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ShriekParticle;renderRotatedQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;Lorg/joml/Quaternionf;F)V"
            ),
            index = 2
    )
    private Quaternionf planetary$rotateShriekQuad(
            Quaternionf localRotation
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
            return localRotation;
        }

        return PlanetRenderTransform
                .localToWorldQuaternion(
                        frameOptional.get().face()
                )
                .mul(
                        localRotation
                );
    }
}
