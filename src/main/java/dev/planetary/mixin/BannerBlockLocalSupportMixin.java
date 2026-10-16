package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Local DOWN support for all BannerBlock color variants and inheritors. */
@Mixin(BannerBlock.class)
public abstract class BannerBlockLocalSupportMixin {
    @Redirect(method = "canSurvive",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"))
    private BlockPos planetary$localSupportBelow(
            BlockPos receiver, BlockState state,
            LevelReader level, BlockPos position
    ) {
        return PlanetBlockSupportRuntime.query(level, receiver, Direction.DOWN)
                .map(query -> query.supportPos())
                .orElseGet(receiver::below);
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void planetary$physicalSupportInvalidation(
            BlockState state, Direction physicalDirection, BlockState other,
            LevelAccessor level, BlockPos position, BlockPos neighbor,
            CallbackInfoReturnable<BlockState> cir
    ) {
        PlanetBlockSupportRuntime.query(level, position, Direction.DOWN)
                .filter(query -> PlanetBlockSupportRuntime.isSupportNeighbor(
                        query, neighbor))
                .filter(query -> !level.getBlockState(query.supportPos()).isSolid())
                .ifPresent(query -> cir.setReturnValue(Blocks.AIR.defaultBlockState()));
    }
}
