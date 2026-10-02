package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * RedstoneWallTorchBlock has its own updateShape even though support checking
 * delegates to WallTorchBlock.canSurvive.
 */
@Mixin(RedstoneWallTorchBlock.class)
public abstract class RedstoneWallTorchSupportMixin {
    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSupportUpdate(
            BlockState state,
            Direction physicalDirection,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Direction localSupport =
                state.getValue(
                                RedstoneWallTorchBlock.FACING
                        )
                        .getOpposite();

        Optional<PlanetBlockSupportQuery> queryOptional =
                PlanetBlockSupportRuntime.query(
                        level,
                        pos,
                        localSupport
                );
        if (queryOptional.isEmpty()) {
            return;
        }

        PlanetBlockSupportQuery query =
                queryOptional.get();

        if (PlanetBlockSupportRuntime.isSupportNeighbor(
                query,
                neighborPos
        ) && !PlanetBlockSupportRuntime.isFaceSturdy(
                level,
                query
        )) {
            cir.setReturnValue(
                    Blocks.AIR.defaultBlockState()
            );
            return;
        }

        cir.setReturnValue(state);
    }
}
