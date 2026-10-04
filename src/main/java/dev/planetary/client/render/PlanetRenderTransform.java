package dev.planetary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * Shared client-side canonical-local -> physical render transforms.
 */
public final class PlanetRenderTransform {
    private PlanetRenderTransform() {
    }

    public static Matrix4f localToWorldRotation(
            PlanetFace face
    ) {
        Objects.requireNonNull(face, "face");

        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector east =
                frame.worldEast();
        PlanetVector up =
                frame.worldUp();
        PlanetVector south =
                frame.worldSouth();

        Matrix4f rotation =
                new Matrix4f();

        rotation.m00(east.x());
        rotation.m01(east.y());
        rotation.m02(east.z());

        rotation.m10(up.x());
        rotation.m11(up.y());
        rotation.m12(up.z());

        rotation.m20(south.x());
        rotation.m21(south.y());
        rotation.m22(south.z());

        return rotation;
    }

    public static void applyAroundBlockCenter(
            PoseStack poseStack,
            PlanetFace face
    ) {
        Objects.requireNonNull(poseStack, "poseStack");
        Objects.requireNonNull(face, "face");

        if (face == PlanetFace.POS_Y) {
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(localToWorldRotation(face));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }

    public static Vec3 localToWorldOffset(
            Vec3 localOffset,
            PlanetFace face
    ) {
        Objects.requireNonNull(localOffset, "localOffset");
        Objects.requireNonNull(face, "face");

        PlanetFrameVector world =
                new PlanetGravityFrame(face)
                        .localToWorld(
                                new PlanetFrameVector(
                                        localOffset.x,
                                        localOffset.y,
                                        localOffset.z
                                )
                        );

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
    }
}
