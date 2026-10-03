package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stores fence NORTH/EAST/SOUTH/WEST as canonical local tangent connections.
 */
@Mixin(FenceBlock.class)
public abstract class FenceBlockGravityMixin {
    private static final Direction[] PLANETARY$TANGENTS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };

    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localConnectionsOnPlacement(
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

        FenceBlock self =
                (FenceBlock) (Object) this;
        FluidState fluid =
                level.getFluidState(pos);

        BlockState result =
                self.defaultBlockState()
                        .setValue(
                                FenceBlock.WATERLOGGED,
                                fluid.getType()
                                        == Fluids.WATER
                        );

        for (Direction localDirection :
                PLANETARY$TANGENTS) {
            PlanetBlockNeighborQuery query =
                    PlanetBlockRuntime.neighbor(
                            level,
                            pos,
                            localDirection
                    ).orElse(null);

            if (query == null) {
                continue;
            }

            BlockState neighbor =
                    level.getBlockState(
                            query.targetPos()
                    );
            Direction neighborLocalSide =
                    query.targetLocalSideTowardSource();

            boolean sturdy =
                    neighbor.isFaceSturdy(
                            level,
                            query.targetPos(),
                            neighborLocalSide
                    );

            result =
                    result.setValue(
                            planetary$property(
                                    localDirection
                            ),
                            self.connectsTo(
                                    neighbor,
                                    sturdy,
                                    neighborLocalSide
                            )
                    );
        }

        cir.setReturnValue(result);
    }

    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localConnectionUpdate(
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
                FenceBlock.WATERLOGGED
        )) {
            accessor.scheduleTick(
                    pos,
                    Fluids.WATER,
                    Fluids.WATER.getTickDelay(
                            accessor
                    )
            );
        }

        FenceBlock self =
                (FenceBlock) (Object) this;
        BlockState result =
                state;

        for (Direction localDirection :
                PLANETARY$TANGENTS) {
            PlanetBlockNeighborQuery query =
                    PlanetBlockRuntime.neighbor(
                            level,
                            pos,
                            localDirection
                    ).orElse(null);

            if (query == null
                    || !query.targetPos()
                            .equals(neighborPos)) {
                continue;
            }

            Direction neighborLocalSide =
                    query.targetLocalSideTowardSource();
            boolean sturdy =
                    neighborState.isFaceSturdy(
                            level,
                            neighborPos,
                            neighborLocalSide
                    );

            result =
                    result.setValue(
                            planetary$property(
                                    localDirection
                            ),
                            self.connectsTo(
                                    neighborState,
                                    sturdy,
                                    neighborLocalSide
                            )
                    );
        }

        /*
         * For Planet blocks every physical update has now been classified
         * against the four logical tangent neighbors. A physical UP/DOWN axis
         * must not fall through to vanilla's world-horizontal test.
         */
        cir.setReturnValue(result);
    }

    private static BooleanProperty planetary$property(
            Direction localDirection
    ) {
        return switch (localDirection) {
            case NORTH -> FenceBlock.NORTH;
            case EAST -> FenceBlock.EAST;
            case SOUTH -> FenceBlock.SOUTH;
            case WEST -> FenceBlock.WEST;
            default -> throw new IllegalArgumentException(
                    "Fence tangent must be horizontal: "
                            + localDirection
            );
        };
    }
}
