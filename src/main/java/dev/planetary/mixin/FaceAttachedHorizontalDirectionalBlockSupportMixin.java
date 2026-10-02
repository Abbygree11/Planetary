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
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Shared local-frame support/placement boundary for vanilla and modded
 * FaceAttachedHorizontalDirectionalBlock subclasses.
 */
@Mixin(FaceAttachedHorizontalDirectionalBlock.class)
public abstract class FaceAttachedHorizontalDirectionalBlockSupportMixin {
    @Inject(
            method = "canAttach(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void planetary$localStaticAttach(
            LevelReader level,
            BlockPos pos,
            Direction localDirectionToSupport,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetBlockSupportRuntime.query(
                level,
                pos,
                localDirectionToSupport
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
        Optional<Direction> horizontalOptional =
                PlanetFrameApi.localHorizontalDirection(
                        context
                );

        if (directionsOptional.isEmpty()
                || horizontalOptional.isEmpty()) {
            return;
        }

        FaceAttachedHorizontalDirectionalBlock self =
                (FaceAttachedHorizontalDirectionalBlock)
                        (Object) this;

        for (Direction direction :
                directionsOptional.get()) {
            BlockState candidate;

            if (direction.getAxis()
                    == Direction.Axis.Y) {
                candidate =
                        self.defaultBlockState()
                                .setValue(
                                        FaceAttachedHorizontalDirectionalBlock.FACE,
                                        direction == Direction.UP
                                                ? AttachFace.CEILING
                                                : AttachFace.FLOOR
                                )
                                .setValue(
                                        FaceAttachedHorizontalDirectionalBlock.FACING,
                                        horizontalOptional.get()
                                );
            } else {
                candidate =
                        self.defaultBlockState()
                                .setValue(
                                        FaceAttachedHorizontalDirectionalBlock.FACE,
                                        AttachFace.WALL
                                )
                                .setValue(
                                        FaceAttachedHorizontalDirectionalBlock.FACING,
                                        direction.getOpposite()
                                );
            }

            if (candidate.canSurvive(
                    context.getLevel(),
                    context.getClickedPos()
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
        Direction localConnected =
                switch (state.getValue(
                        FaceAttachedHorizontalDirectionalBlock.FACE
                )) {
                    case CEILING -> Direction.DOWN;
                    case FLOOR -> Direction.UP;
                    case WALL -> state.getValue(
                            FaceAttachedHorizontalDirectionalBlock.FACING
                    );
                };

        Direction localSupport =
                localConnected.getOpposite();

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
