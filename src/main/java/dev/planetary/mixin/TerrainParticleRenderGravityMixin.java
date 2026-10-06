package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleRenderAnchor;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Makes TerrainParticle render position rotationally equivalent to vanilla.
 *
 * <p>Vanilla Particle coordinates represent center X/Z but minY of the
 * particle AABB. That is implicitly the local-DOWN face only for +Y gravity.
 * Destroy particles on rotated faces therefore remain visibly offset from the
 * local floor after collision and expose a surface-crawling phase that is
 * hidden by the floor on vanilla +Y.</p>
 *
 * <p>This mixin changes rendering only. Particle physics, AABB, lifetime and
 * collision remain vanilla/Planet gravity behavior.</p>
 */
@Mixin(SingleQuadParticle.class)
public abstract class TerrainParticleRenderGravityMixin {
    private static final String RENDER_ROTATED_QUAD =
            "renderRotatedQuad("
                    + "Lcom/mojang/blaze3d/vertex/VertexConsumer;"
                    + "Lnet/minecraft/client/Camera;"
                    + "Lorg/joml/Quaternionf;"
                    + "F)V";

    @Unique
    private double planetary$terrainRenderOffsetX;

    @Unique
    private double planetary$terrainRenderOffsetY;

    @Unique
    private double planetary$terrainRenderOffsetZ;

    @Inject(
            method = RENDER_ROTATED_QUAD,
            at = @At("HEAD")
    )
    private void planetary$prepareTerrainRenderAnchor(
            VertexConsumer consumer,
            Camera camera,
            Quaternionf rotation,
            float partialTick,
            CallbackInfo ci
    ) {
        this.planetary$terrainRenderOffsetX = 0.0D;
        this.planetary$terrainRenderOffsetY = 0.0D;
        this.planetary$terrainRenderOffsetZ = 0.0D;

        if (!((Object) this instanceof TerrainParticle)) {
            return;
        }

        Particle particle =
                (Particle) (Object) this;
        ParticleGravityAccessor accessor =
                (ParticleGravityAccessor) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        accessor.planetary$getLevel(),
                        accessor.planetary$getX(),
                        accessor.planetary$getY(),
                        accessor.planetary$getZ()
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        Vec3 offset =
                PlanetParticleRenderAnchor.localDownFaceOffset(
                        particle.getBoundingBox(),
                        frameOptional.get()
                );

        this.planetary$terrainRenderOffsetX = offset.x;
        this.planetary$terrainRenderOffsetY = offset.y;
        this.planetary$terrainRenderOffsetZ = offset.z;
    }

    @Redirect(
            method = RENDER_ROTATED_QUAD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 0
            )
    )
    private double planetary$shiftTerrainRenderX(
            double partialTick,
            double previous,
            double current
    ) {
        return Mth.lerp(
                partialTick,
                previous,
                current
        ) + this.planetary$terrainRenderOffsetX;
    }

    @Redirect(
            method = RENDER_ROTATED_QUAD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 1
            )
    )
    private double planetary$shiftTerrainRenderY(
            double partialTick,
            double previous,
            double current
    ) {
        return Mth.lerp(
                partialTick,
                previous,
                current
        ) + this.planetary$terrainRenderOffsetY;
    }

    @Redirect(
            method = RENDER_ROTATED_QUAD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 2
            )
    )
    private double planetary$shiftTerrainRenderZ(
            double partialTick,
            double previous,
            double current
    ) {
        return Mth.lerp(
                partialTick,
                previous,
                current
        ) + this.planetary$terrainRenderOffsetZ;
    }
}
