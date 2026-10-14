package dev.planetary.mixin;

import dev.planetary.world.PlanetFurnacePlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 1.21.1 owner adapter: no HEAD cancel, no BlockEntity or recipe rewrite. */
@Mixin(AbstractFurnaceBlock.class)
public abstract class AbstractFurnaceBlockLocalPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$canonicalFurnacePlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetFurnacePlacement.afterPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
