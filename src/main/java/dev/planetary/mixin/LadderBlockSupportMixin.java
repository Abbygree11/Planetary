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
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Canonical-local placement/support/update semantics for ladders.
 */
@Mixin(LadderBlock.class)
public abstract class LadderBlockSupportMixin {
    @Inject(
            method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSupport(
            BlockState state,
            LevelReader level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Direction localSupport =
                state.getValue(
                                LadderBlock.FACING
                        )
                        .getOpposite();

        PlanetBlockSupportRuntime.query(
                level,
                pos,
                localSupport
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

        LadderBlock self =
                (LadderBlock) (Object) this;
        BlockPos target =
                context.getClickedPos();

        if (!context.replacingClickedOnBlock()) {
            BlockPos physicalClickedBlock =
                    target.relative(
                            context.getClickedFace()
                                    .getOpposite()
                    );
            BlockState clickedState =
                    context.getLevel()
                            .getBlockState(
                                    physicalClickedBlock
                            );

            Optional<Direction> clickedLocalFace =
                    PlanetFrameApi.physicalSideToLocal(
                            context.getLevel(),
                            physicalClickedBlock,
                            context.getClickedFace()
                    );

            if (clickedState.is(self)
                    && clickedLocalFace.isPresent()
                    && clickedState.getValue(
                            LadderBlock.FACING
                    ) == clickedLocalFace.get()) {
                cir.setReturnValue(null);
                return;
            }
        }

        FluidState fluid =
                context.getLevel()
                        .getFluidState(target);

        for (Direction direction :
                directionsOptional.get()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }

            BlockState candidate =
                    self.defaultBlockState()
                            .setValue(
                                    LadderBlock.FACING,
                                    direction.getOpposite()
                            );

            if (candidate.canSurvive(
                    context.getLevel(),
                    target
            )) {
                cir.setReturnValue(
                        candidate.setValue(
                                LadderBlock.WATERLOGGED,
                                fluid.getType()
                                        == Fluids.WATER
                        )
                );
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
                                LadderBlock.FACING
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

        if (state.getValue(
                LadderBlock.WATERLOGGED
        )) {
            level.scheduleTick(
                    pos,
                    Fluids.WATER,
                    Fluids.WATER.getTickDelay(level)
            );
        }

        cir.setReturnValue(state);
    }
}
