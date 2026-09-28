package dev.planetary.mixin;

import dev.planetary.world.PlanetLevelBridge;
import dev.planetary.world.PlanetVanillaUpdateFrame;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    @Inject(method = "updateNeighborsAt", at = @At("HEAD"), cancellable = true)
    private void planetary$updateNeighborsAt(
            BlockPos pos,
            Block sourceBlock,
            CallbackInfo ci
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved -> {
            resolved.world().updateNeighborsAt(
                    resolved.position(),
                    sourceBlock
            );
            ci.cancel();
        });
    }

    @Inject(
            method = "updateNeighborsAtExceptFromFacing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$updateNeighborsAtExceptFromFacing(
            BlockPos pos,
            Block sourceBlock,
            Direction exceptDirection,
            CallbackInfo ci
    ) {
        Level level = (Level) (Object) this;

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved -> {
            resolved.world().updateNeighborsAtExceptFromFacing(
                    resolved.position(),
                    sourceBlock,
                    exceptDirection
            );
            ci.cancel();
        });
    }

    @Inject(
            method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$neighborChanged(
            BlockPos pos,
            Block sourceBlock,
            BlockPos sourcePos,
            CallbackInfo ci
    ) {
        if (PlanetVanillaUpdateFrame.consumeReentryPermit()) {
            return;
        }

        Level level = (Level) (Object) this;

        PlanetVanillaUpdateFrame.resolve(level, pos, sourcePos)
                .ifPresent(frame -> {
                    if (frame.targetPos().equals(pos)
                            && frame.sourceAliasPos().equals(sourcePos)) {
                        return;
                    }

                    PlanetVanillaUpdateFrame.runWithReentryPermit(
                            () -> level.neighborChanged(
                                    frame.targetPos(),
                                    sourceBlock,
                                    frame.sourceAliasPos()
                            )
                    );
                    ci.cancel();
                });
    }

    @Inject(
            method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$neighborChangedWithState(
            BlockState state,
            BlockPos pos,
            Block sourceBlock,
            BlockPos sourcePos,
            boolean isMoving,
            CallbackInfo ci
    ) {
        if (PlanetVanillaUpdateFrame.consumeReentryPermit()) {
            return;
        }

        Level level = (Level) (Object) this;

        PlanetVanillaUpdateFrame.resolve(level, pos, sourcePos)
                .ifPresent(frame -> {
                    if (frame.targetPos().equals(pos)
                            && frame.sourceAliasPos().equals(sourcePos)) {
                        return;
                    }

                    PlanetVanillaUpdateFrame.runWithReentryPermit(
                            () -> level.neighborChanged(
                                    state,
                                    frame.targetPos(),
                                    sourceBlock,
                                    frame.sourceAliasPos(),
                                    isMoving
                            )
                    );
                    ci.cancel();
                });
    }

    @Inject(
            method = "neighborShapeChanged(Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$neighborShapeChanged(
            Direction direction,
            BlockState queried,
            BlockPos pos,
            BlockPos sourcePos,
            int flags,
            int recursionLevel,
            CallbackInfo ci
    ) {
        if (PlanetVanillaUpdateFrame.consumeReentryPermit()) {
            return;
        }

        Level level = (Level) (Object) this;

        PlanetVanillaUpdateFrame.resolve(level, pos, sourcePos)
                .ifPresent(frame -> {
                    if (frame.targetPos().equals(pos)
                            && frame.sourceAliasPos().equals(sourcePos)
                            && frame.directionToSource() == direction) {
                        return;
                    }

                    PlanetVanillaUpdateFrame.runWithReentryPermit(
                            () -> level.neighborShapeChanged(
                                    frame.directionToSource(),
                                    queried,
                                    frame.targetPos(),
                                    frame.sourceAliasPos(),
                                    flags,
                                    recursionLevel
                            )
                    );
                    ci.cancel();
                });
    }

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

        PlanetLevelBridge.resolve(level, pos).ifPresent(resolved ->
                cir.setReturnValue(
                        resolved.world().setBlock(
                                resolved.position(),
                                state,
                                flags,
                                recursionLeft
                        )
                )
        );
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
