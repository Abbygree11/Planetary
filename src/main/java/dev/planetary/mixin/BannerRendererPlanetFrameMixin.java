package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.client.render.PlanetLocalBlockEntityRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phase 3C-1: complete vanilla BER model/text/chain/pattern/skin rendering
 * in its own canonical block frame, then project geometry to physical XYZ.
 * Inventory BE previews and non-Planet worlds remain unchanged.
 */
@Mixin(BannerRenderer.class)
public abstract class BannerRendererPlanetFrameMixin {
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BannerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"))
    private void planetary$beginBlockFrame(
            BannerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        PlanetLocalBlockEntityRender.begin(blockEntity, poseStack);
    }

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BannerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("RETURN"))
    private void planetary$finishBlockFrame(
            BannerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        PlanetLocalBlockEntityRender.finish(blockEntity, poseStack);
    }
}
