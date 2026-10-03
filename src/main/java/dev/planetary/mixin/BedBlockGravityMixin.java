package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetPlacementRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BedBlock.class)
public abstract class BedBlockGravityMixin {
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
        BlockPos foot =
                context.getClickedPos();

        if (PlanetBlockRuntime.stateFrameAt(
                level,
                foot
        ).isEmpty()) {
            return;
        }

        Optional<Direction> facingOptional =
                PlanetFrameApi.localHorizontalDirection(
                        context
                );
        if (facingOptional.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        Direction facing =
                facingOptional.get();

        Optional<PlanetBlockNeighborQuery> headOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        foot,
                        facing
                );

        if (headOptional.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        BlockPos head =
                headOptional.get()
                        .targetPos();

        if (level.isOutsideBuildHeight(
                head.getY()
        ) || !level.getBlockState(head)
                .canBeReplaced(context)
                || !level.getWorldBorder()
                .isWithinBounds(head)) {
            cir.setReturnValue(null);
            return;
        }

        BedBlock self =
                (BedBlock) (Object) this;
        cir.setReturnValue(
                self.defaultBlockState()
                        .setValue(
                                BedBlock.FACING,
                                facing
                        )
                        .setValue(
                                BedBlock.PART,
                                BedPart.FOOT
                        )
        );
    }

    @Inject(
            method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$placeHead(
            Level level,
            BlockPos footPos,
            BlockState footState,
            @Nullable LivingEntity placer,
            ItemStack stack,
            CallbackInfo ci
    ) {
        Optional<PlanetBlockNeighborQuery> headOptional =
                PlanetBlockRuntime.neighbor(
                        level,
                        footPos,
                        footState.getValue(
                                BedBlock.FACING
                        )
                );

        if (headOptional.isEmpty()) {
            return;
        }

        PlanetBlockNeighborQuery head =
                headOptional.get();
        Direction headFacing =
                PlanetPlacementRuntime.reframeToTarget(
                        head,
                        footState.getValue(
                                BedBlock.FACING
                        )
                );

        if (!level.isClientSide) {
            level.setBlock(
                    head.targetPos(),
                    footState
                            .setValue(
                                    BedBlock.PART,
                                    BedPart.HEAD
                            )
                            .setValue(
                                    BedBlock.FACING,
                                    headFacing
                            ),
                    3
            );
            level.blockUpdated(
                    footPos,
                    Blocks.AIR
            );
            footState.updateNeighbourShapes(
                    level,
                    footPos,
                    3
            );
        }

        ci.cancel();
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

        BedPart part =
                state.getValue(
                        BedBlock.PART
                );
        Direction pairDirection =
                part == BedPart.FOOT
                        ? state.getValue(
                                BedBlock.FACING
                        )
                        : state.getValue(
                                BedBlock.FACING
                        ).getOpposite();

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
            BedBlock self =
                    (BedBlock) (Object) this;

            if (neighborState.is(self)
                    && neighborState.getValue(
                    BedBlock.PART
            ) != part) {
                cir.setReturnValue(
                        state.setValue(
                                BedBlock.OCCUPIED,
                                neighborState.getValue(
                                        BedBlock.OCCUPIED
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

        cir.setReturnValue(state);
    }
}
