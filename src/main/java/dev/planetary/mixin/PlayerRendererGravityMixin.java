package dev.planetary.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * PlayerRenderer executes inside an already gravity-rotated PoseStack. Feed
 * setupRotations a local-frame view vector so it does not rotate the head/body
 * a second time using the world-space vector.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererGravityMixin {

    @Redirect(
            method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 planetary$viewVectorInLocalFrame(
            AbstractClientPlayer player,
            float partialTick
    ) {
        Vec3 world = player.getViewVector(partialTick);

        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) player)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return world;
        }

        PlanetFrameVector local =
                frameOptional.get().worldToLocal(
                        new PlanetFrameVector(
                                world.x,
                                world.y,
                                world.z
                        )
                );

        return new Vec3(
                local.x(),
                local.y(),
                local.z()
        );
    }
}
