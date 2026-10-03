package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the SNOWY state tied to canonical local UP instead of physical +Y.
 */
@Mixin(SnowyDirtBlock.class)
public abstract class SnowyDirtBlockGravityMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSnowPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Level level =
                context.getLevel();
        BlockPos pos =
                context.getClickedPos();

        PlanetBlockNeighborQuery localUp =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        Direction.UP
                ).orElse(null);
        if (localUp == null) {
            return;
        }

        SnowyDirtBlock self =
                (SnowyDirtBlock) (Object) this;

        cir.setReturnValue(
                self.defaultBlockState()
                        .setValue(
                                SnowyDirtBlock.SNOWY,
                                level.getBlockState(
                                        localUp.targetPos()
                                ).is(BlockTags.SNOW)
                        )
        );
    }

    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSnowUpdate(
            BlockState state,
            Direction physicalDirection,
            BlockState neighborState,
            LevelAccessor accessor,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (!(accessor instanceof Level level)) {
            return;
        }

        PlanetBlockNeighborQuery localUp =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        Direction.UP
                ).orElse(null);
        if (localUp == null) {
            return;
        }

        if (localUp.targetPos()
                .equals(neighborPos)) {
            cir.setReturnValue(
                    state.setValue(
                            SnowyDirtBlock.SNOWY,
                            neighborState.is(
                                    BlockTags.SNOW
                            )
                    )
            );
        } else {
            cir.setReturnValue(state);
        }
    }
}
