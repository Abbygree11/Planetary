package dev.planetary.mixin;

import dev.planetary.world.PlanetRotation16Placement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Narrow declaring-owner RETURN adapter. Preserve original block and metadata. */
@Mixin(SkullBlock.class)
public abstract class SkullBlockRotation16Mixin {
    @Inject(method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true)
    private void planetary$canonical16AnglePlacement(BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir) {
        PlanetRotation16Placement.afterPlacement(
                context, cir.getReturnValue(), PlanetRotation16Placement.Owner.SKULL)
                .ifPresent(cir::setReturnValue);
    }
}
