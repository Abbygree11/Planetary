package dev.planetary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class PlanetLocalBlockEntityRenderTest {
    private static final int R = 24;
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(new PlanetCore(0, 0, 0, R));

    @Test
    void physicalBlockModelAndTextStayWithinSameCanonicalSixFaceFrame() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            for (PlanetFace face : PlanetFace.values()) {
                PlanetGravityFrame orientation = new PlanetGravityFrame(face);
                BlockPos position = new BlockPos(
                        (int) orientation.worldUp().x() * R,
                        (int) orientation.worldUp().y() * R,
                        (int) orientation.worldUp().z() * R);
                BlockEntity entity = mock(BlockEntity.class);
                when(entity.getLevel()).thenReturn(level);
                when(entity.getBlockPos()).thenReturn(position);

                PoseStack poses = new PoseStack();
                Matrix4f before = new Matrix4f(poses.last().pose());
                var frame = PlanetLocalBlockEntityRender.worldAttachedFrame(entity);
                if (face == PlanetFace.POS_Y) {
                    assertTrue(frame.isEmpty(), "Vanilla +Y should not push pose");
                } else {
                    assertEquals(face, frame.orElseThrow());
                }
                PlanetLocalBlockEntityRender.begin(entity, poses);
                Matrix4f during = new Matrix4f(poses.last().pose());

                var center = new org.joml.Vector3f(.5f, .5f, .5f);
                during.transformPosition(center);
                assertEquals(.5f, center.x(), 1e-5f, face + " x center");
                assertEquals(.5f, center.y(), 1e-5f, face + " y center");
                assertEquals(.5f, center.z(), 1e-5f, face + " z center");
                var top = new org.joml.Vector3f(.5f, 1f, .5f);
                during.transformPosition(top);
                assertEquals(.5 + .5 * orientation.worldUp().x(),
                        top.x(), 1e-5, face + " local UP x");
                assertEquals(.5 + .5 * orientation.worldUp().y(),
                        top.y(), 1e-5, face + " local UP y");
                assertEquals(.5 + .5 * orientation.worldUp().z(),
                        top.z(), 1e-5, face + " local UP z");

                PlanetLocalBlockEntityRender.finish(entity, poses);
                assertTrue(before.equals(poses.last().pose(), 1e-6f),
                        face + " must not leak a render pose");
            }
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void selectionIsSnapshottedEvenIfOtherModChangesLevelDuringRender() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            BlockEntity entity = mock(BlockEntity.class);
            when(entity.getLevel()).thenReturn(level);
            when(entity.getBlockPos()).thenReturn(new BlockPos(R, 0, 0));
            PoseStack pose = new PoseStack();
            Matrix4f baseline = new Matrix4f(pose.last().pose());
            PlanetLocalBlockEntityRender.begin(entity, pose);

            // Simulates an independent extension detaching the BE mid-render.
            when(entity.getLevel()).thenReturn(null);
            PlanetLocalBlockEntityRender.finish(entity, pose);
            assertTrue(baseline.equals(pose.last().pose(), 1e-6f));
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void nestedCallsKeepBothPoseStacksInCorrectLifoOrder() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            BlockEntity outer = mock(BlockEntity.class);
            when(outer.getLevel()).thenReturn(level);
            when(outer.getBlockPos()).thenReturn(new BlockPos(R, 0, 0));
            BlockEntity inner = mock(BlockEntity.class); // item/no level
            PoseStack pose = new PoseStack();
            Matrix4f baseline = new Matrix4f(pose.last().pose());
            PlanetLocalBlockEntityRender.begin(outer, pose);
            Matrix4f rotated = new Matrix4f(pose.last().pose());
            PlanetLocalBlockEntityRender.begin(inner, pose);
            PlanetLocalBlockEntityRender.finish(inner, pose);
            assertTrue(rotated.equals(pose.last().pose(), 1e-6f));
            PlanetLocalBlockEntityRender.finish(outer, pose);
            assertTrue(baseline.equals(pose.last().pose(), 1e-6f));
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void noPlanetAndInventoryPreviewMustNotModifyThePose() {
        Level world = mock(Level.class);
        BlockEntity entity = mock(BlockEntity.class);
        when(entity.getBlockPos()).thenReturn(new BlockPos(R, 0, 0));
        PoseStack poses = new PoseStack();
        Matrix4f before = new Matrix4f(poses.last().pose());

        assertTrue(PlanetLocalBlockEntityRender.worldAttachedFrame(entity).isEmpty());
        PlanetLocalBlockEntityRender.begin(entity, poses);
        PlanetLocalBlockEntityRender.finish(entity, poses);
        assertTrue(before.equals(poses.last().pose(), 1e-6f));

        when(entity.getLevel()).thenReturn(world);
        assertTrue(PlanetLocalBlockEntityRender.worldAttachedFrame(entity).isEmpty());
        PlanetLocalBlockEntityRender.begin(entity, poses);
        PlanetLocalBlockEntityRender.finish(entity, poses);
        assertTrue(before.equals(poses.last().pose(), 1e-6f));
    }
}
