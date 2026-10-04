package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.client.render.PlanetFallingBlockRender;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.world.PlanetVanillaDirection;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Adapts FallingBlockRenderer's world-DOWN anchor assumptions to the entity's
 * current Planet gravity frame.
 */
@Mixin(FallingBlockRenderer.class)
public abstract class FallingBlockRendererGravityMixin {
    @ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private BlockPos planetary$localRenderBlockPos(
            BlockPos vanillaPos,
            FallingBlockEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        Optional<Direction> down =
                planetary$localDown(entity);

        if (down.isEmpty()
                || down.get() == Direction.DOWN) {
            return vanillaPos;
        }

        return PlanetFallingBlockRender.renderBlockPos(
                entity.position(),
                down.get()
        );
    }

    @ModifyArgs(
            method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V"
            )
    )
    private void planetary$translateFromLocalDownAnchor(
            Args args,
            FallingBlockEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        Optional<Direction> down =
                planetary$localDown(entity);

        if (down.isEmpty()
                || down.get() == Direction.DOWN) {
            return;
        }

        Vec3 translation =
                PlanetFallingBlockRender.modelTranslation(
                        down.get()
                );

        args.set(0, translation.x);
        args.set(1, translation.y);
        args.set(2, translation.z);
    }

    private static Optional<Direction> planetary$localDown(
            FallingBlockEntity entity
    ) {
        return ((PlanetGravityEntity) entity)
                .planetary$gravityFrame()
                .map(PlanetGravityFrame::face)
                .filter(face ->
                        face != PlanetFace.POS_Y
                )
                .map(face ->
                        PlanetVanillaDirection.localToWorld(
                                new PlanetGravityFrame(face),
                                Direction.DOWN
                        )
                );
    }
}
