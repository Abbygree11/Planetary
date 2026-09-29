package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetPlayerPoseGeometry;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * Makes Player.updatePlayerPose() test STANDING/CROUCHING/SWIMMING clearance
 * in the player's local gravity frame instead of assuming world +Y is UP.
 */
@Mixin(Player.class)
public abstract class PlayerGravityMixin {

    @Redirect(
            method = "canPlayerFitWithinBlocksAndEntitiesWhen(Lnet/minecraft/world/entity/Pose;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityDimensions;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
            )
    )
    private AABB planetary$poseBoundingBox(
            EntityDimensions dimensions,
            Vec3 position
    ) {
        Player self = (Player) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return dimensions.makeBoundingBox(position);
        }

        return PlanetPlayerPoseGeometry.poseBoundingBox(
                dimensions,
                position,
                frameOptional.get()
        );
    }
}
