package dev.planetary.mixin;

import dev.planetary.world.PlanetPillarPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.InfestedRotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Opt-in for the independent, non-RotatedPillarBlock superclass family.
 * Its original clicked-face AXIS algorithm has the same local semantics.
 * Preserve infested spawning, host-state mapping, BlockItem and worldgen.
 */
@Mixin(InfestedRotatedPillarBlock.class)
public abstract class InfestedRotatedPillarPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true
    )
    private void planetary$infestedCanonicalAxis(
            BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetPillarPlacement.afterPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
