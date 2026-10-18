package dev.planetary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.Deque;
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
    /** A scoped LIFO of selected world-attached BER calls on this render thread.
     * Snapshot at method entry: modded BlockEntity levels/positions can change
     * during vanilla or NeoForge extension callbacks. */
    private static final ThreadLocal<Deque<Scope>> SCOPES =
            ThreadLocal.withInitial(ArrayDeque::new);

    private record Scope(PoseStack poseStack, boolean transformed) {
    }

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
     * Pair with finish() at renderer RETURN. Snapshot both the chosen
     * canonical face and the push decision before invoking the BER. Nested
     * sign/text/mod subrender passes may use the same thread; LIFO scopes
     * keep their independent model matrices balanced.
     */
    public static void begin(BlockEntity blockEntity, PoseStack poseStack) {
        Objects.requireNonNull(poseStack, "poseStack");
        Optional<PlanetFace> face = worldAttachedFrame(blockEntity);
        boolean transformed = face.isPresent();
        if (transformed) {
            poseStack.pushPose();
            try {
                PlanetRenderTransform.applyAroundBlockCenter(
                        poseStack, face.orElseThrow());
            } catch (RuntimeException | Error error) {
                poseStack.popPose();
                throw error;
            }
        }
        SCOPES.get().push(new Scope(poseStack, transformed));
    }

    public static void finish(BlockEntity blockEntity, PoseStack poseStack) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        Objects.requireNonNull(poseStack, "poseStack");
        Deque<Scope> stack = SCOPES.get();
        if (stack.isEmpty()) {
            SCOPES.remove();
            throw new IllegalStateException(
                    "Planet local BER finish without matching begin");
        }
        Scope scope = stack.pop();
        if (stack.isEmpty()) {
            SCOPES.remove();
        }
        if (scope.poseStack() != poseStack) {
            throw new IllegalStateException(
                    "Planet local BER nested PoseStack is not balanced");
        }
        if (scope.transformed()) {
            poseStack.popPose();
        }
    }
}
