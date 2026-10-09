package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Vanilla CakeBlock and CandleCakeBlock share the same physical-below
 * isSolid() support predicate and physical-DOWN update comparison.
 * Keep original isSolid semantics; do not replace with canSupportCenter.
 */
@Mixin({CakeBlock.class, CandleCakeBlock.class})
public abstract class CakeFamilyLocalSupportMixin {
    @Redirect(
            method = "canSurvive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos planetary$localBelow(
            BlockPos physicalSource,
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        return PlanetBlockSupportRuntime.query(
                level, physicalSource, Direction.DOWN
        ).map(PlanetBlockSupportQuery::supportPos)
                .orElseGet(physicalSource::below);
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void planetary$physicalSupportNeighbor(
            BlockState state,
            Direction physicalDirection,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Optional<PlanetBlockSupportQuery> query =
                PlanetBlockSupportRuntime.query(level, pos, Direction.DOWN);
        if (query.isEmpty()) {
            return;
        }

        // On Planet, vanilla's (physicalDirection == DOWN) check is NOT
        // equivalent to source-local support DOWN. A non-support physical
        // neighbor must never cause a valid cake to drop.
        if (PlanetBlockSupportRuntime.isSupportNeighbor(
                query.get(), neighborPos)
                && !state.canSurvive(level, pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        } else {
            cir.setReturnValue(state);
        }
    }
}
