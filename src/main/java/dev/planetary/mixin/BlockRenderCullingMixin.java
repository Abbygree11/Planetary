package dev.planetary.mixin;

import dev.planetary.client.render.PlanetBlockRenderCulling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reframes only semantic face/culling directions while preserving physical
 * neighbor positions.
 */
@Mixin(Block.class)
public abstract class BlockRenderCullingMixin {
    @Inject(
            method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void planetary$frameAwareCulling(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction physicalDirection,
            BlockPos neighborPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetBlockRenderCulling.shouldRenderFace(
                state,
                level,
                pos,
                physicalDirection,
                neighborPos
        ).ifPresent(
                cir::setReturnValue
        );
    }
}
