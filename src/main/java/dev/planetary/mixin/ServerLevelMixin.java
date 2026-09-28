package dev.planetary.mixin;

import dev.planetary.world.PlanetLevelBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ServerLevel overrides the Level neighbor-update entry points, so the server
 * needs the same Planetary routing as the base Level implementation.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "updateNeighborsAt", at = @At("HEAD"), cancellable = true)
    private void planetary$updateNeighborsAt(
            BlockPos pos,
            Block sourceBlock,
            CallbackInfo ci
    ) {
        ServerLevel level = (ServerLevel) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved -> {
            resolved.world().updateNeighborsAt(
                    resolved.position(),
                    sourceBlock
            );
            ci.cancel();
        });
    }

    @Inject(
            method = "updateNeighborsAtExceptFromFacing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$updateNeighborsAtExceptFromFacing(
            BlockPos pos,
            Block sourceBlock,
            Direction exceptDirection,
            CallbackInfo ci
    ) {
        ServerLevel level = (ServerLevel) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved -> {
            resolved.world().updateNeighborsAtExceptFromFacing(
                    resolved.position(),
                    sourceBlock,
                    exceptDirection
            );
            ci.cancel();
        });
    }
}
