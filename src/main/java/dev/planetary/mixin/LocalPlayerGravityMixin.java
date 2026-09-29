package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetLocalPlayerGeometry;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.world.PlanetVanillaDirection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Fixes LocalPlayer's vanilla push-out-of-blocks logic for arbitrary local UP.
 *
 * <p>Vanilla samples four XZ corners and may write a constant 0.1 velocity
 * toward the nearest free side. On a side gravity face that XZ assumption can
 * falsely classify the player as suffocating and continuously "pull" them
 * along the surface. The implementation below performs the same algorithm in
 * the player's local XZ plane.</p>
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerGravityMixin {

    private static final Direction[] PLANETARY_HORIZONTAL_DIRECTIONS = {
            Direction.WEST,
            Direction.EAST,
            Direction.NORTH,
            Direction.SOUTH
    };

    @Inject(
            method = "moveTowardsClosestSpace(DD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$moveTowardsClosestSpace(
            double x,
            double z,
            CallbackInfo ci
    ) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        ci.cancel();
        PlanetGravityFrame frame = frameOptional.get();

        // Vanilla passes offsets in its XZ plane. Reinterpret those same
        // offsets as the player's local XZ plane and rotate them into world.
        Vec3 sample =
                PlanetLocalPlayerGeometry.samplePoint(
                        self.position(),
                        x - self.getX(),
                        z - self.getZ(),
                        frame
                );

        BlockPos blockPos = BlockPos.containing(sample);
        if (!planetary$suffocatesAt(self, blockPos, frame)) {
            return;
        }

        double dx = sample.x - blockPos.getX();
        double dy = sample.y - blockPos.getY();
        double dz = sample.z - blockPos.getZ();

        Direction bestLocalDirection = null;
        double bestDistance = Double.MAX_VALUE;

        for (Direction localDirection
                : PLANETARY_HORIZONTAL_DIRECTIONS) {
            Direction worldDirection =
                    PlanetVanillaDirection.localToWorld(
                            frame,
                            localDirection
                    );

            double coordinate =
                    worldDirection.getAxis()
                            .choose(dx, dy, dz);

            double distance =
                    worldDirection.getAxisDirection()
                            == Direction.AxisDirection.POSITIVE
                            ? 1.0D - coordinate
                            : coordinate;

            if (distance < bestDistance
                    && !planetary$suffocatesAt(
                            self,
                            blockPos.relative(worldDirection),
                            frame
                    )) {
                bestDistance = distance;
                bestLocalDirection = localDirection;
            }
        }

        if (bestLocalDirection == null) {
            return;
        }

        Vec3 localVelocity = self.getDeltaMovement();

        if (bestLocalDirection.getAxis()
                == Direction.Axis.X) {
            self.setDeltaMovement(
                    0.1D * bestLocalDirection.getStepX(),
                    localVelocity.y,
                    localVelocity.z
            );
        } else {
            self.setDeltaMovement(
                    localVelocity.x,
                    localVelocity.y,
                    0.1D * bestLocalDirection.getStepZ()
            );
        }
    }

    private static boolean planetary$suffocatesAt(
            LocalPlayer player,
            BlockPos pos,
            PlanetGravityFrame frame
    ) {
        AABB column =
                PlanetLocalPlayerGeometry.suffocationColumn(
                        player.getBoundingBox(),
                        pos,
                        frame
                ).deflate(1.0E-7D);

        return player.level()
                .collidesWithSuffocatingBlock(
                        player,
                        column
                );
    }
}
