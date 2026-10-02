package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stores RotatedPillarBlock.AXIS in canonical LOCAL block semantics.
 */
@Mixin(RotatedPillarBlock.class)
public abstract class RotatedPillarBlockPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localPlacementAxis(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetFrameApi.placementFrame(context)
                .ifPresent(frame -> {
                    RotatedPillarBlock self =
                            (RotatedPillarBlock) (Object) this;

                    cir.setReturnValue(
                            self.defaultBlockState()
                                    .setValue(
                                            RotatedPillarBlock.AXIS,
                                            frame.localClickedFace()
                                                    .getAxis()
                                    )
                    );
                });
    }
}
