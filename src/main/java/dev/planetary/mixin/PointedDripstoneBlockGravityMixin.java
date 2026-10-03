package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetDripstonePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockGravityMixin {
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
        BlockPos pos =
                context.getClickedPos();

        if (PlanetBlockRuntime.stateFrameAt(
                level,
                pos
        ).isEmpty()) {
            return;
        }

        Optional<Direction> tipOptional =
                PlanetDripstonePlacement
                        .initialTipDirection(
                                context
                        );

        if (tipOptional.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        Direction tip =
                tipOptional.get();
        boolean mergeTips =
                !context.isSecondaryUseActive();

        DripstoneThickness thickness =
                PlanetDripstonePlacement
                        .calculateThickness(
                                level,
                                pos,
                                tip,
                                mergeTips
                        );

        PointedDripstoneBlock self =
                (PointedDripstoneBlock)
                        (Object) this;

        cir.setReturnValue(
                self.defaultBlockState()
                        .setValue(
                                PointedDripstoneBlock.TIP_DIRECTION,
                                tip
                        )
                        .setValue(
                                PointedDripstoneBlock.THICKNESS,
                                thickness
                        )
                        .setValue(
                                PointedDripstoneBlock.WATERLOGGED,
                                level.getFluidState(pos)
                                        .getType()
                                        == Fluids.WATER
                        )
        );
    }

    @Inject(
            method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localSurvival(
            BlockState state,
            LevelReader reader,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(reader instanceof Level level)
                || PlanetBlockRuntime.stateFrameAt(
                        level,
                        pos
                ).isEmpty()) {
            return;
        }

        cir.setReturnValue(
                PlanetDripstonePlacement
                        .isValidPlacement(
                                level,
                                pos,
                                state.getValue(
                                        PointedDripstoneBlock.TIP_DIRECTION
                                )
                        )
        );
    }

    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localVerticalUpdate(
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

        if (state.getValue(
                PointedDripstoneBlock.WATERLOGGED
        )) {
            accessor.scheduleTick(
                    pos,
                    Fluids.WATER,
                    Fluids.WATER.getTickDelay(
                            accessor
                    )
            );
        }

        Optional<PlanetBlockNeighborQuery> up =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        Direction.UP
                );
        Optional<PlanetBlockNeighborQuery> down =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        Direction.DOWN
                );

        boolean verticalNeighbor =
                up.map(query ->
                        query.targetPos()
                                .equals(neighborPos)
                ).orElse(false)
                        || down.map(query ->
                        query.targetPos()
                                .equals(neighborPos)
                ).orElse(false);

        if (!verticalNeighbor) {
            cir.setReturnValue(state);
            return;
        }

        Direction tip =
                state.getValue(
                        PointedDripstoneBlock.TIP_DIRECTION
                );
        Direction supportDirection =
                tip.getOpposite();

        Optional<PlanetBlockNeighborQuery> support =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        supportDirection
                );

        PointedDripstoneBlock self =
                (PointedDripstoneBlock)
                        (Object) this;

        if (support.isPresent()
                && support.get()
                        .targetPos()
                        .equals(neighborPos)
                && !PlanetDripstonePlacement
                .isValidPlacement(
                        level,
                        pos,
                        tip
                )) {
            if (tip != Direction.DOWN
                    || !accessor.getBlockTicks()
                    .hasScheduledTick(
                            pos,
                            self
                    )) {
                accessor.scheduleTick(
                        pos,
                        self,
                        tip == Direction.DOWN
                                ? 2
                                : 1
                );
            }

            cir.setReturnValue(state);
            return;
        }

        boolean merged =
                state.getValue(
                        PointedDripstoneBlock.THICKNESS
                ) == DripstoneThickness.TIP_MERGE;

        cir.setReturnValue(
                state.setValue(
                        PointedDripstoneBlock.THICKNESS,
                        PlanetDripstonePlacement
                                .calculateThickness(
                                        level,
                                        pos,
                                        tip,
                                        merged
                                )
                )
        );
    }
}
