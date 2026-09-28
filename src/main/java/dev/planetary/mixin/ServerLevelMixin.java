package dev.planetary.mixin;

import dev.planetary.world.PlanetLevelBridge;
import dev.planetary.world.PlanetVanillaUpdateFrame;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * ServerLevel overrides several Level neighbor-update entry points, so the
 * server needs the same Planetary routing and update-frame canonicalization.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "updateNeighborsAt", at = @At("HEAD"), cancellable = true)
    private void planetary$updateNeighborsAt(
            BlockPos pos,
            Block sourceBlock,
            CallbackInfo ci
    ) {
        ServerLevel level = (ServerLevel) (Object) this;

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
        ServerLevel level = (ServerLevel) (Object) this;

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

        ServerLevel level = (ServerLevel) (Object) this;

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

        ServerLevel level = (ServerLevel) (Object) this;

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
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/ticks/LevelTicks;tick(JILjava/util/function/BiConsumer;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void planetary$runScheduledBlockTicks(
            BooleanSupplier hasTimeLeft,
            CallbackInfo ci
    ) {
        ServerLevel level = (ServerLevel) (Object) this;
        PlanetWorldAccess world = PlanetLevelBridge.get(level);
        if (world != null) {
            world.runScheduledBlockTicks(
                    level,
                    level.getGameTime(),
                    65536
            );
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/ticks/LevelTicks;tick(JILjava/util/function/BiConsumer;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void planetary$runScheduledFluidTicks(
            BooleanSupplier hasTimeLeft,
            CallbackInfo ci
    ) {
        ServerLevel level = (ServerLevel) (Object) this;
        PlanetWorldAccess world = PlanetLevelBridge.get(level);
        if (world != null) {
            world.runScheduledFluidTicks(
                    level,
                    level.getGameTime(),
                    65536
            );
        }
    }
}
