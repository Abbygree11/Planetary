package dev.planetary.mixin;

import dev.planetary.world.PlanetRotation16Placement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Narrow declaring-owner RETURN adapter. Preserve original block and metadata. */
@Mixin(BannerBlock.class)
public abstract class BannerBlockRotation16Mixin {
    @Inject(method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true)
    private void planetary$canonical16AnglePlacement(BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir) {
        PlanetRotation16Placement.afterPlacement(
                context, cir.getReturnValue(), PlanetRotation16Placement.Owner.STANDING_SIGN_OR_BANNER)
                .ifPresent(cir::setReturnValue);
    }
}
