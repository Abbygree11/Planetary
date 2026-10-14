package dev.planetary.mixin;

import dev.planetary.world.PlanetPillarPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shared inherited axis-pillar owner: run vanilla/NeoForge first, then
 * reframe only AXIS. Never reconstruct a defaultBlockState at HEAD.
 */
@Mixin(RotatedPillarBlock.class)
public abstract class RotatedPillarBlockPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true
    )
    private void planetary$localPlacementAxis(
            BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetPillarPlacement.afterPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
