package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.client.PlanetCameraRotation;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Rotates rendered living entities so their visual body uses the same local
 * UP axis as their physical AABB and camera.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherGravityMixin {

    @Unique
    private boolean planetary$bodyPosePushed;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void planetary$rotateBodyIntoGravityFrame(
            Entity entity,
            double x,
            double y,
            double z,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        planetary$bodyPosePushed = false;

        if (!(entity instanceof LivingEntity)
                || !(entity instanceof PlanetGravityEntity gravityEntity)) {
            return;
        }

        Optional<PlanetGravityFrame> frameOptional =
                gravityEntity.planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(
                PlanetCameraRotation.frameQuaternion(
                        frameOptional.get()
                )
        );
        planetary$bodyPosePushed = true;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 1
            )
    )
    private void planetary$restoreBodyPose(
            Entity entity,
            double x,
            double y,
            double z,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (planetary$bodyPosePushed) {
            poseStack.popPose();
            planetary$bodyPosePushed = false;
        }
    }
}
