package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BasePressurePlateBlock.class)
public abstract class BasePressurePlateBlockGravityMixin {
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
        PlanetBlockSupportRuntime.query(
                level,
                pos,
                Direction.DOWN
        ).ifPresent(query ->
                cir.setReturnValue(
                        PlanetBlockSupportRuntime.canSupportRigid(
                                level,
                                query
                        ) || PlanetBlockSupportRuntime.canSupportCenter(
                                level,
                                query
                        )
                )
        );
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
        Optional<PlanetBlockSupportQuery> queryOptional =
                PlanetBlockSupportRuntime.query(
                        level,
                        pos,
                        Direction.DOWN
                );
        if (queryOptional.isEmpty()) {
            return;
        }

        PlanetBlockSupportQuery query =
                queryOptional.get();

        if (query.supportPos().equals(neighborPos)
                && !state.canSurvive(level, pos)) {
            cir.setReturnValue(
                    Blocks.AIR.defaultBlockState()
            );
            return;
        }

        cir.setReturnValue(state);
    }
}
