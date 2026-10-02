package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stores hopper FACING in canonical LOCAL block semantics.
 */
@Mixin(HopperBlock.class)
public abstract class HopperBlockPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localPlacementFacing(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetFrameApi.placementFrame(context)
                .ifPresent(frame -> {
                    Direction facing =
                            frame.localClickedFace()
                                    .getOpposite();

                    if (facing.getAxis()
                            == Direction.Axis.Y) {
                        facing = Direction.DOWN;
                    }

                    HopperBlock self =
                            (HopperBlock) (Object) this;

                    cir.setReturnValue(
                            self.defaultBlockState()
                                    .setValue(
                                            HopperBlock.FACING,
                                            facing
                                    )
                                    .setValue(
                                            HopperBlock.ENABLED,
                                            true
                                    )
                    );
                });
    }
}
