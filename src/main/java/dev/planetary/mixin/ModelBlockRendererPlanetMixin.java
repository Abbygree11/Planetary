package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.planetary.client.render.PlanetBakedModelRotation;
import dev.planetary.client.render.PlanetClientRenderFrame;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Supplies a position-oriented baked-model view to the ordinary NeoForge
 * ModelBlockRenderer pipeline.
 */
@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererPlanetMixin {
    @ModifyVariable(
            method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private BakedModel planetary$orientModel(
            BakedModel model,
            BlockAndTintGetter level,
            BakedModel argModel,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack,
            VertexConsumer consumer,
            boolean checkSides,
            RandomSource random,
            long seed,
            int overlay,
            ModelData modelData,
            RenderType renderType
    ) {
        return PlanetClientRenderFrame
                .stateFrameAt(
                        level,
                        pos
                )
                .map(frame ->
                        PlanetBakedModelRotation.orientForBlock(
                                model,
                                state,
                                frame.face(),
                                PlanetClientRenderFrame.candidateFacesAt(
                                        level,
                                        pos
                                )
                        )
                )
                .orElse(model);
    }
}
