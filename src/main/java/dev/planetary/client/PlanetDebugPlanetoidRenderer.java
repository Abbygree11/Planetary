package dev.planetary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.debug.PlanetDebugPlanetoid;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetVector;
import dev.planetary.world.PlanetBlockPos;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * First visual bridge from the face-local Planetary world into ordinary
 * Minecraft rendering.
 *
 * <p>Every face is kept as a rigid orthonormal block grid. No per-block
 * shearing or spherical deformation is performed, so cubes and modded block
 * models keep their native shape right up to a cube edge.</p>
 */
final class PlanetDebugPlanetoidRenderer {

    private PlanetDebugPlanetoidRenderer() {
    }

    static void render(
            RenderLevelStageEvent event,
            PlanetWorldAccess world,
            BlockPos center
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers =
                minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(
                center.getX() - camera.x,
                center.getY() - camera.y,
                center.getZ() - camera.z
        );

        for (PlanetFace face : PlanetFace.values()) {
            renderFace(
                    minecraft,
                    buffers,
                    poseStack,
                    world,
                    face
            );
        }

        poseStack.popPose();
        buffers.endBatch();
    }

    private static void renderFace(
            Minecraft minecraft,
            MultiBufferSource.BufferSource buffers,
            PoseStack poseStack,
            PlanetWorldAccess world,
            PlanetFace face
    ) {
        int half = PlanetDebugPlanetoid.FACE_SIZE / 2;

        poseStack.pushPose();
        poseStack.mulPose(faceMatrix(face));
        poseStack.translate(-half, half, -half);

        for (int x = 0; x < PlanetDebugPlanetoid.FACE_SIZE; x++) {
            for (int z = 0; z < PlanetDebugPlanetoid.FACE_SIZE; z++) {
                PlanetBlockPos planetPos = new PlanetBlockPos(
                        face,
                        x,
                        PlanetDebugPlanetoid.SURFACE_Y,
                        z
                );
                BlockState state = world.getBlockState(planetPos);

                poseStack.pushPose();
                poseStack.translate(
                        x,
                        PlanetDebugPlanetoid.SURFACE_Y,
                        z
                );
                minecraft.getBlockRenderer().renderSingleBlock(
                        state,
                        poseStack,
                        buffers,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY
                );
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    private static Matrix4f faceMatrix(PlanetFace face) {
        PlanetVector east =
                face.worldVector(PlanetDirection.EAST);
        PlanetVector up =
                face.worldVector(PlanetDirection.UP);
        PlanetVector south =
                face.worldVector(PlanetDirection.SOUTH);

        return new Matrix4f()
                .identity()
                .m00(east.x())
                .m01(east.y())
                .m02(east.z())
                .m10(up.x())
                .m11(up.y())
                .m12(up.z())
                .m20(south.x())
                .m21(south.y())
                .m22(south.z());
    }
}
