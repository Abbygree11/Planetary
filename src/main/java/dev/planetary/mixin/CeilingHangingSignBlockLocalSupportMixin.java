package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Local ceiling UP support; preserves original water-tick update. */
@Mixin(CeilingHangingSignBlock.class)
public abstract class CeilingHangingSignBlockLocalSupportMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void planetary$localCeiling(
            BlockState state, LevelReader level, BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PlanetBlockSupportRuntime.query(level, pos, Direction.UP)
                .ifPresent(query -> {
                    BlockState support = level.getBlockState(query.supportPos());
                    cir.setReturnValue(support.isFaceSturdy(
                            level, query.supportPos(),
                            query.supportLocalSideTowardSource(),
                            SupportType.CENTER));
                });
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void planetary$physicalCeilingInvalidation(
            BlockState state, Direction physicalDirection, BlockState other,
            LevelAccessor level, BlockPos position, BlockPos neighbor,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetBlockSupportRuntime.query(level, position, Direction.UP)
                .filter(query -> PlanetBlockSupportRuntime.isSupportNeighbor(
                        query, neighbor))
                .ifPresent(query -> {
                    BlockState support = level.getBlockState(query.supportPos());
                    if (!support.isFaceSturdy(
                            level, query.supportPos(),
                            query.supportLocalSideTowardSource(),
                            SupportType.CENTER)) {
                        cir.setReturnValue(Blocks.AIR.defaultBlockState());
                    }
                });
    }
}
