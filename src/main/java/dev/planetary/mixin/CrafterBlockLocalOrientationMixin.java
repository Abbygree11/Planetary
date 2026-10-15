package dev.planetary.mixin;

import dev.planetary.world.PlanetFrontAndTopPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve original Crafter placement, redstone and BlockEntity. */
@Mixin(CrafterBlock.class)
public abstract class CrafterBlockLocalOrientationMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$canonicalCrafterPlacement(
            BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetFrontAndTopPlacement.afterCrafterPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
