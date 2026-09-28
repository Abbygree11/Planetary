package dev.planetary.mixin;

import dev.planetary.world.PlanetLevelBridge;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects vanilla Level access only for explicitly bound Planetary Levels
 * and only for BlockPos values that resolve inside Planetary's virtual atlas.
 */
@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void planetary$getBlockState(
            BlockPos pos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved ->
                cir.setReturnValue(
                        resolved.world().getBlockState(resolved.position())
                )
        );
    }

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void planetary$getFluidState(
            BlockPos pos,
            CallbackInfoReturnable<FluidState> cir
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved ->
                cir.setReturnValue(
                        resolved.world().getFluidState(resolved.position())
                )
        );
    }

    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private void planetary$getBlockEntity(
            BlockPos pos,
            CallbackInfoReturnable<BlockEntity> cir
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved ->
                cir.setReturnValue(
                        resolved.world().getBlockEntity(resolved.position())
                )
        );
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$setBlock(
            BlockPos pos,
            BlockState state,
            int flags,
            int recursionLeft,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved -> {
            PlanetWorldAccess world = resolved.world();

            BlockState previous = (flags & Block.UPDATE_NEIGHBORS) != 0
                    ? world.setBlockStateAndUpdateNeighbors(
                            resolved.position(),
                            state
                    )
                    : world.setBlockState(
                            resolved.position(),
                            state
                    );

            cir.setReturnValue(previous != state);
        });
    }

    @Inject(method = "setBlockEntity", at = @At("HEAD"), cancellable = true)
    private void planetary$setBlockEntity(
            BlockEntity blockEntity,
            CallbackInfo ci
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, blockEntity.getBlockPos())
                .ifPresent(resolved -> {
                    blockEntity.setLevel(level);
                    resolved.world().blockEntities().put(
                            resolved.position(),
                            blockEntity
                    );
                    ci.cancel();
                });
    }

    @Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true)
    private void planetary$removeBlockEntity(
            BlockPos pos,
            CallbackInfo ci
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved -> {
            resolved.world().blockEntities().remove(resolved.position());
            ci.cancel();
        });
    }

    @Inject(method = "isLoaded", at = @At("HEAD"), cancellable = true)
    private void planetary$isLoaded(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Level level = (Level) (Object) this;

        if (PlanetLevelBridge.resolve(level, pos).isPresent()) {
            cir.setReturnValue(true);
        }
    }
}
