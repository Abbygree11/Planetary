package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Candle support owns local DOWN; neighbor update Direction is physical.
 * RETURN injection preserves CandleBlock's original water-tick scheduling.
 */
@Mixin(CandleBlock.class)
public abstract class CandleBlockLocalSupportMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void planetary$localCandleSupport(
            BlockState state,
            LevelReader level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetBlockSupportRuntime.query(
                level, pos, Direction.DOWN
        ).ifPresent(query -> cir.setReturnValue(
                PlanetBlockSupportRuntime.canSupportCenter(level, query)
        ));
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void planetary$dropOnPhysicalSupportRemoval(
            BlockState state,
            Direction physicalDirection,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Optional<PlanetBlockSupportQuery> support =
                PlanetBlockSupportRuntime.query(level, pos, Direction.DOWN);
        if (support.isPresent()
                && PlanetBlockSupportRuntime.isSupportNeighbor(
                        support.get(), neighborPos)
                && !PlanetBlockSupportRuntime.canSupportCenter(
                        level, support.get())) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
