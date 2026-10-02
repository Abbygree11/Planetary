package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Canonical-local placement/support for wall torches and redstone wall torches.
 */
@Mixin(WallTorchBlock.class)
public abstract class WallTorchBlockSupportMixin {
    @Inject(
            method = "canSurvive(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void planetary$localStaticSupport(
            LevelReader level,
            BlockPos pos,
            Direction localFacing,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetBlockSupportRuntime.query(
                level,
                pos,
                localFacing.getOpposite()
        ).ifPresent(query ->
                cir.setReturnValue(
                        PlanetBlockSupportRuntime.isFaceSturdy(
                                level,
                                query
                        )
                )
        );
    }

    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Optional<List<Direction>> directionsOptional =
                PlanetFrameApi.localNearestLookingDirections(
                        context
                );
        if (directionsOptional.isEmpty()) {
            return;
        }

        WallTorchBlock self =
                (WallTorchBlock) (Object) this;
        BlockPos pos =
                context.getClickedPos();

        for (Direction direction :
                directionsOptional.get()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }

            BlockState candidate =
                    self.defaultBlockState()
                            .setValue(
                                    WallTorchBlock.FACING,
                                    direction.getOpposite()
                            );

            if (candidate.canSurvive(
                    context.getLevel(),
                    pos
            )) {
                cir.setReturnValue(candidate);
                return;
            }
        }

        cir.setReturnValue(null);
    }

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
                                WallTorchBlock.FACING
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
