package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Spore Blossom hangs from local UP. Its collision/support test and
 * water prohibition are unchanged, but the physical support direction
 * is not necessarily world +Y.
 */
@Mixin(SporeBlossomBlock.class)
public abstract class SporeBlossomLocalSupportMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void planetary$localCeilingSupport(
            BlockState state,
            LevelReader level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetBlockSupportRuntime.query(
                level, pos, Direction.UP
        ).ifPresent(query -> cir.setReturnValue(
                PlanetBlockSupportRuntime.canSupportCenter(level, query)
                        && !level.isWaterAt(pos)
        ));
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void planetary$physicalCeilingNeighbor(
            BlockState state,
            Direction physicalDirection,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Optional<PlanetBlockSupportQuery> support =
                PlanetBlockSupportRuntime.query(level, pos, Direction.UP);
        if (support.isEmpty()) {
            return;
        }

        if (PlanetBlockSupportRuntime.isSupportNeighbor(
                support.get(), neighborPos)
                && !state.canSurvive(level, pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        } else {
            cir.setReturnValue(state);
        }
    }
}
