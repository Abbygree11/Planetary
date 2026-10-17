package dev.planetary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;
import java.util.Optional;

/**
 * Opt-in, world-attached BER pose boundary for the rigid block-local
 * sign/banner/skull renderers. This is deliberately NOT hooked into the
 * global BlockEntityRenderDispatcher: many other BERs own physical/world,
 * camera or instanced geometry and must not be rotated.
 *
 * Original renderer owns the model's local yaw, attachment chains,
 * wall offsets, text, patterns, skin, buffers, normals and light values.
 * The only Planet change is the outer LOCAL block frame -> physical XYZ.
 */
public final class PlanetLocalBlockEntityRender {
    private PlanetLocalBlockEntityRender() {
    }

    /**
     * No Level means inventory/GUI preview. No Planet means unchanged vanilla.
     * Standard +Y has the identity frame; do not push it needlessly.
     */
    public static Optional<PlanetFace> worldAttachedFrame(BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        if (blockEntity.getLevel() == null) {
            return Optional.empty();
        }
        return PlanetClientRenderFrame
                .stateFrameAt(blockEntity.getLevel(), blockEntity.getBlockPos())
                .map(frame -> frame.face())
                .filter(face -> face != PlanetFace.POS_Y);
    }

    /**
     * Must be paired with finish() at the same renderer method's RETURN.
     * These concrete renderers never mutate the block's field/frame during
     * synchronous rendering. A different physical renderer cannot silently
     * opt in because this helper is called from only four narrow Mixins.
     */
    public static void begin(BlockEntity blockEntity, PoseStack poseStack) {
        Objects.requireNonNull(poseStack, "poseStack");
        worldAttachedFrame(blockEntity).ifPresent(face -> {
            poseStack.pushPose();
            PlanetRenderTransform.applyAroundBlockCenter(poseStack, face);
        });
    }

    public static void finish(BlockEntity blockEntity, PoseStack poseStack) {
        Objects.requireNonNull(poseStack, "poseStack");
        if (worldAttachedFrame(blockEntity).isPresent()) {
            poseStack.popPose();
        }
    }
}
