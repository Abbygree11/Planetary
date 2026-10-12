package dev.planetary.mixin;

import dev.planetary.world.PlanetTrapDoorPlacement;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Thin RETURN adapter for the whole inherited TrapDoorBlock placement owner.
 * Preserve vanilla/NeoForge waterlogging, powered/open state and extension
 * dispatch; never cancel the original method at HEAD.
 */
@Mixin(TrapDoorBlock.class)
public abstract class TrapDoorBlockLocalPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$targetCanonicalTrapdoor(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetTrapDoorPlacement.reframe(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}
