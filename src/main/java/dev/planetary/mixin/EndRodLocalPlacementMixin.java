package dev.planetary.mixin;

import dev.planetary.world.PlanetDirectionalPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reframe authored rod FACING without changing vanilla physical adjacency. */
@Mixin(EndRodBlock.class)
public abstract class EndRodLocalPlacementMixin {
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void planetary$localRodDirection(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetDirectionalPlacement.endRod(
                context, (EndRodBlock) (Object) this
        ).ifPresent(cir::setReturnValue);
    }
}
