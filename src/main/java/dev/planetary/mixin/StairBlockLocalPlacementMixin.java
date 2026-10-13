package dev.planetary.mixin;

import dev.planetary.world.PlanetStairPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Thin exact-version owner hooks; core semantics are outside Mixin package. */
@Mixin(StairBlock.class)
public abstract class StairBlockLocalPlacementMixin {
    @Inject(method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true)
    private void planetary$canonicalStairPlacement(
            BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        PlanetStairPlacement.afterPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }

    @Inject(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true)
    private void planetary$canonicalStairNeighbor(
            BlockState state, Direction physicalDirection,
            BlockState physicalNeighbor, LevelAccessor level,
            BlockPos pos, BlockPos physicalNeighborPos,
            CallbackInfoReturnable<BlockState> cir) {
        PlanetStairPlacement.afterNeighborUpdate(level, pos, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
