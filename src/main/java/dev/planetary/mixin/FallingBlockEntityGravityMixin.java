package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the remaining landing-time world-Y support lookup of a falling
 * block entity with the planet-local DOWN direction.
 */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityGravityMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos planetary$localBelow(
            BlockPos pos
    ) {
        FallingBlockEntity self =
                (FallingBlockEntity) (Object) this;

        return pos.relative(
                PlanetBlockGravity.localDown(
                        self.level(),
                        pos
                )
        );
    }
}
