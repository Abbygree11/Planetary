package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies slab TOP/BOTTOM semantics in the target block's canonical local frame.
 */
@Mixin(SlabBlock.class)
public abstract class SlabBlockPlacementMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSlabPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetFrameApi.placementFrame(context)
                .ifPresent(frame -> {
                    SlabBlock self =
                            (SlabBlock) (Object) this;
                    BlockState current =
                            context.getLevel()
                                    .getBlockState(
                                            context.getClickedPos()
                                    );

                    if (current.is(self)) {
                        cir.setReturnValue(
                                current.setValue(
                                                SlabBlock.TYPE,
                                                SlabType.DOUBLE
                                        )
                                        .setValue(
                                                SlabBlock.WATERLOGGED,
                                                false
                                        )
                        );
                        return;
                    }

                    FluidState fluidState =
                            context.getLevel()
                                    .getFluidState(
                                            context.getClickedPos()
                                    );

                    BlockState base =
                            self.defaultBlockState()
                                    .setValue(
                                            SlabBlock.TYPE,
                                            SlabType.BOTTOM
                                    )
                                    .setValue(
                                            SlabBlock.WATERLOGGED,
                                            fluidState.getType()
                                                    == Fluids.WATER
                                    );

                    Direction localFace =
                            frame.localClickedFace();

                    boolean bottom =
                            localFace != Direction.DOWN
                                    && (localFace == Direction.UP
                                    || !frame.localHitUpperHalf());

                    cir.setReturnValue(
                            bottom
                                    ? base
                                    : base.setValue(
                                            SlabBlock.TYPE,
                                            SlabType.TOP
                                    )
                    );
                });
    }

    @Inject(
            method = "canBeReplaced(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/context/BlockPlaceContext;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSlabReplacement(
            BlockState state,
            BlockPlaceContext context,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetFrameApi.placementFrame(context)
                .ifPresent(frame -> {
                    SlabBlock self =
                            (SlabBlock) (Object) this;
                    ItemStack item =
                            context.getItemInHand();
                    SlabType type =
                            state.getValue(
                                    SlabBlock.TYPE
                            );

                    if (type == SlabType.DOUBLE
                            || !item.is(self.asItem())) {
                        cir.setReturnValue(false);
                        return;
                    }

                    if (!context.replacingClickedOnBlock()) {
                        cir.setReturnValue(true);
                        return;
                    }

                    Direction localFace =
                            frame.localClickedFace();
                    boolean upper =
                            frame.localHitUpperHalf();
                    boolean horizontal =
                            localFace.getAxis()
                                    .isHorizontal();

                    boolean replace =
                            type == SlabType.BOTTOM
                                    ? localFace == Direction.UP
                                    || upper && horizontal
                                    : localFace == Direction.DOWN
                                    || !upper && horizontal;

                    cir.setReturnValue(replace);
                });
    }
}
