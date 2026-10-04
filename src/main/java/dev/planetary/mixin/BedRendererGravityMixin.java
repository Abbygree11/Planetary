package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.client.render.PlanetClientRenderFrame;
import dev.planetary.client.render.PlanetRenderTransform;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BedRenderer bypasses the ordinary baked-model orientation adapter.
 */
@Mixin(BedRenderer.class)
public abstract class BedRendererGravityMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BedBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD")
    )
    private void planetary$pushLocalFrame(
            BedBlockEntity bed,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            CallbackInfo ci
    ) {
        Level level =
                bed.getLevel();
        if (level == null) {
            return;
        }

        PlanetClientRenderFrame
                .stateFrameAt(
                        level,
                        bed.getBlockPos()
                )
                .ifPresent(frame -> {
                    poseStack.pushPose();
                    PlanetRenderTransform
                            .applyAroundBlockCenter(
                                    poseStack,
                                    frame.face()
                            );
                });
    }

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BedBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("RETURN")
    )
    private void planetary$popLocalFrame(
            BedBlockEntity bed,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            CallbackInfo ci
    ) {
        Level level =
                bed.getLevel();
        if (level == null) {
            return;
        }

        if (PlanetClientRenderFrame
                .stateFrameAt(
                        level,
                        bed.getBlockPos()
                )
                .isPresent()) {
            poseStack.popPose();
        }
    }
}
