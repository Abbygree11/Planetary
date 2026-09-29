package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.client.PlanetCameraRotation;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.model.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Rotates only the living-entity model pose after LivingEntityRenderer has
 * pushed its own PoseStack frame. This avoids shared push/pop state in the
 * global EntityRenderDispatcher and keeps nested renders safe.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererGravityMixin<
        T extends LivingEntity,
        M extends EntityModel<T>> {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void planetary$rotateLivingBody(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) entity)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        poseStack.mulPose(
                PlanetCameraRotation.entityQuaternion(
                        frameOptional.get()
                )
        );
    }
}
