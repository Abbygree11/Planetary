package dev.planetary.mixin;

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
 * Makes every Direction-context NeoForge BlockCapability seam-aware.
 *
 * <p>This covers item/fluid/energy handlers and mod-defined sided capabilities
 * without depending on the individual mod that owns the pipe, cable, machine
 * or inventory.</p>
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

        PlanetSidedQueryFrame.resolve(level, pos, side)
                .ifPresent(frame -> {
                    if (frame.targetPos().equals(pos)
                            && frame.targetSide() == side) {
                        return;
                    }

                    BlockCapability capability =
                            (BlockCapability) (Object) this;

                    Object result =
                            PlanetSidedQueryFrame.callWithReentryPermit(
                                    () -> capability.getCapability(
                                            level,
                                            frame.targetPos(),
                                            state,
                                            blockEntity,
                                            frame.targetSide()
                                    )
                            );

                    cir.setReturnValue(result);
                });
    }
}
