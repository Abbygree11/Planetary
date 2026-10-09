package dev.planetary.mixin;

import dev.planetary.world.PlanetDirectionalPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keep vanilla waterlogged state and only reframe horizontal FACING
 * from player body coordinates to target canonical BlockState coordinates.
 */
@Mixin(EnderChestBlock.class)
public abstract class EnderChestLocalPlacementMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"),
            cancellable = true)
    private void planetary$localEnderChestFacing(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        BlockState vanilla = cir.getReturnValue();
        if (vanilla == null) {
            return;
        }
        PlanetDirectionalPlacement.horizontalFacing(context)
                .ifPresent(localFacing -> cir.setReturnValue(
                        vanilla.setValue(EnderChestBlock.FACING, localFacing)
                ));
    }
}
