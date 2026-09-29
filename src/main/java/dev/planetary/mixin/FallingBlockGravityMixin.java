package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes sand, gravel, anvils and other FallingBlock implementations test
 * support in local DOWN instead of hard-coded world -Y.
 */
@Mixin(FallingBlock.class)
public abstract class FallingBlockGravityMixin {
    @Shadow
    protected abstract void falling(
            FallingBlockEntity entity
    );

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$useLocalDown(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        Direction down =
                PlanetBlockGravity.localDown(level, pos);

        if (down == Direction.DOWN) {
            return;
        }

        BlockPos below = pos.relative(down);

        if (below.getY() >= level.getMinBuildHeight()
                && below.getY() < level.getMaxBuildHeight()
                && FallingBlock.isFree(
                        level.getBlockState(below)
                )) {
            FallingBlockEntity entity =
                    FallingBlockEntity.fall(
                            level,
                            pos,
                            state
                    );
            this.falling(entity);
        }

        ci.cancel();
    }
}
