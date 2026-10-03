package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import dev.planetary.world.PlanetPlacementRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DoorBlock.class)
public abstract class DoorBlockGravityMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Level level =
                context.getLevel();
        BlockPos lower =
                context.getClickedPos();

        if (PlanetBlockRuntime.stateFrameAt(
                level,
                lower
        ).isEmpty()) {
            return;
        }

        Optional<Direction> facingOptional =
                PlanetFrameApi.localHorizontalDirection(
                        context
                );
        Optional<PlanetBlockNeighborQuery> upperOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        lower,
                        Direction.UP
                );

        if (facingOptional.isEmpty()
                || upperOptional.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        PlanetBlockNeighborQuery upper =
                upperOptional.get();

        if (level.isOutsideBuildHeight(
                upper.targetPos().getY()
        ) || !level.getBlockState(
                upper.targetPos()
        ).canBeReplaced(context)) {
            cir.setReturnValue(null);
            return;
        }

        DoorBlock self =
                (DoorBlock) (Object) this;
        Direction facing =
                facingOptional.get();

        var hingeOptional =
                PlanetPlacementRuntime.doorHinge(
                        self,
                        context,
                        facing
                );

        if (hingeOptional.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        boolean powered =
                level.hasNeighborSignal(lower)
                        || level.hasNeighborSignal(
                        upper.targetPos()
                );

        cir.setReturnValue(
                self.defaultBlockState()
                        .setValue(
                                DoorBlock.FACING,
                                facing
                        )
                        .setValue(
                                DoorBlock.HINGE,
                                hingeOptional.get()
                        )
                        .setValue(
                                DoorBlock.POWERED,
                                powered
                        )
                        .setValue(
                                DoorBlock.OPEN,
                                powered
                        )
                        .setValue(
                                DoorBlock.HALF,
                                DoubleBlockHalf.LOWER
                        )
        );
    }

    @Inject(
            method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$placeUpperHalf(
            Level level,
            BlockPos lowerPos,
            BlockState lowerState,
            @Nullable LivingEntity placer,
            ItemStack stack,
            CallbackInfo ci
    ) {
        Optional<PlanetBlockNeighborQuery> upperOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        lowerPos,
                        Direction.UP
                );
        if (upperOptional.isEmpty()) {
            return;
        }

        PlanetBlockNeighborQuery upper =
                upperOptional.get();

        Direction upperFacing =
                PlanetPlacementRuntime.reframeToTarget(
                        upper,
                        lowerState.getValue(
                                DoorBlock.FACING
                        )
                );

        level.setBlock(
                upper.targetPos(),
                lowerState
                        .setValue(
                                DoorBlock.HALF,
                                DoubleBlockHalf.UPPER
                        )
                        .setValue(
                                DoorBlock.FACING,
                                upperFacing
                        ),
                3
        );
        ci.cancel();
    }

    @Inject(
            method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSurvival(
            BlockState state,
            LevelReader level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (state.getValue(
                DoorBlock.HALF
        ) == DoubleBlockHalf.LOWER) {
            PlanetBlockSupportRuntime.query(
                    level,
                    pos,
                    Direction.DOWN
            ).ifPresent(query ->
                    cir.setReturnValue(
                            PlanetBlockSupportRuntime.isFaceSturdy(
                                    level,
                                    query
                            )
                    )
            );
            return;
        }

        Optional<PlanetBlockSupportQuery> lowerOptional =
                PlanetBlockSupportRuntime.query(
                        level,
                        pos,
                        Direction.DOWN
                );
        if (lowerOptional.isEmpty()) {
            return;
        }

        DoorBlock self =
                (DoorBlock) (Object) this;
        BlockState lower =
                level.getBlockState(
                        lowerOptional.get()
                                .supportPos()
                );

        cir.setReturnValue(
                lower.is(self)
        );
    }

    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localPairUpdate(
            BlockState state,
            Direction physicalDirection,
            BlockState neighborState,
            LevelAccessor accessor,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (!(accessor instanceof Level level)
                || PlanetBlockRuntime.stateFrameAt(
                        level,
                        pos
                ).isEmpty()) {
            return;
        }

        DoubleBlockHalf half =
                state.getValue(
                        DoorBlock.HALF
                );
        Direction pairDirection =
                half == DoubleBlockHalf.LOWER
                        ? Direction.UP
                        : Direction.DOWN;

        Optional<PlanetBlockNeighborQuery> pairOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        pairDirection
                );

        if (pairOptional.isPresent()
                && pairOptional.get()
                        .targetPos()
                        .equals(neighborPos)) {
            DoorBlock self =
                    (DoorBlock) (Object) this;

            if (neighborState.is(self)
                    && neighborState.getValue(
                    DoorBlock.HALF
            ) != half) {
                cir.setReturnValue(
                        state
                                .setValue(
                                        DoorBlock.OPEN,
                                        neighborState.getValue(
                                                DoorBlock.OPEN
                                        )
                                )
                                .setValue(
                                        DoorBlock.POWERED,
                                        neighborState.getValue(
                                                DoorBlock.POWERED
                                        )
                                )
                );
            } else {
                cir.setReturnValue(
                        Blocks.AIR.defaultBlockState()
                );
            }
            return;
        }

        if (half == DoubleBlockHalf.LOWER) {
            Optional<PlanetBlockSupportQuery> supportOptional =
                    PlanetBlockSupportRuntime.query(
                            level,
                            pos,
                            Direction.DOWN
                    );

            if (supportOptional.isPresent()
                    && supportOptional.get()
                            .supportPos()
                            .equals(neighborPos)
                    && !state.canSurvive(
                    level,
                    pos
            )) {
                cir.setReturnValue(
                        Blocks.AIR.defaultBlockState()
                );
                return;
            }
        }

        cir.setReturnValue(state);
    }
}
