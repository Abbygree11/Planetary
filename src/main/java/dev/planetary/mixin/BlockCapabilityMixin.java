package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetSidedQueryFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adapts Direction-context NeoForge BlockCapabilities to Planet block frames.
 *
 * <p>In the physical Planet world the queried BlockPos remains ordinary world
 * XYZ exactly as NeoForge specifies. Only a non-null physical Direction
 * context is converted to the target block's canonical local BlockState side
 * before provider dispatch. This covers standard item/fluid/energy and custom
 * sided capabilities without knowing the owning mod.</p>
 *
 * <p>The older virtual-atlas canonicalization remains only as a legacy
 * fallback while that storage prototype still exists in the codebase.</p>
 */
@Mixin(value = BlockCapability.class, remap = false)
public abstract class BlockCapabilityMixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private void planetary$getCapability(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity,
            Object context,
            CallbackInfoReturnable<Object> cir
    ) {
        if (PlanetSidedQueryFrame.consumeReentryPermit()) {
            return;
        }
        if (!(context instanceof Direction side)) {
            return;
        }

        /*
         * Dedicated Planet worlds use ordinary physical BlockPos storage.
         * NeoForge's queried pos is therefore authoritative and MUST NOT be
         * redirected. Only the sided Direction context is reframed into the
         * target block's canonical local BlockState frame.
         */
        var localSide =
                PlanetBlockRuntime.physicalSideToLocal(
                        level,
                        pos,
                        side
                );
        if (localSide.isPresent()) {
            Direction canonicalSide =
                    localSide.get();

            if (canonicalSide == side) {
                return;
            }

            BlockCapability capability =
                    (BlockCapability) (Object) this;

            Object result =
                    PlanetSidedQueryFrame.callWithReentryPermit(
                            () -> capability.getCapability(
                                    level,
                                    pos,
                                    state,
                                    blockEntity,
                                    canonicalSide
                            )
                    );

            cir.setReturnValue(result);
            return;
        }

        /*
         * Legacy virtual-atlas worlds can still expose a guard-space alias for
         * the same logical target block. Preserve that compatibility path for
         * the old world-access tests/runtime while the physical Planet world
         * migration is completed.
         */
        PlanetSidedQueryFrame.resolve(level, pos, side)
                .ifPresent(frame -> {
                    if (frame.targetPos().equals(pos)
                            && frame.targetSide() == side) {
                        return;
                    }

                    BlockCapability capability =
                            (BlockCapability) (Object) this;

                    BlockPos targetPos =
                            frame.targetPos();
                    BlockState targetState =
                            level.getBlockState(
                                    targetPos
                            );
                    BlockEntity targetBlockEntity =
                            level.getBlockEntity(
                                    targetPos
                            );

                    Object result =
                            PlanetSidedQueryFrame.callWithReentryPermit(
                                    () -> capability.getCapability(
                                            level,
                                            targetPos,
                                            targetState,
                                            targetBlockEntity,
                                            frame.targetSide()
                                    )
                            );

                    cir.setReturnValue(result);
                });
    }
}
