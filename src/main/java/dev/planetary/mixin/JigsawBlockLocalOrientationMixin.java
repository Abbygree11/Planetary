package dev.planetary.mixin;

import dev.planetary.world.PlanetFrontAndTopPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only player/synthetic placement; structure templates own their writer. */
@Mixin(JigsawBlock.class)
public abstract class JigsawBlockLocalOrientationMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$canonicalJigsawPlacement(
            BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetFrontAndTopPlacement.afterJigsawPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
