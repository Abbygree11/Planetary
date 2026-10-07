package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityRuntime;
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
 * Legacy virtual-atlas capability alias bridge only.
 *
 * <p>For a physical Planet world, NeoForge gets the exact physical queried
 * BlockPos and Direction with no hidden rewrite. Providers that intentionally
 * consume canonical Planet-local sides explicitly opt in via
 * dev.planetary.api.PlanetCapabilityAdapters at registration time.</p>
 *
 * <p>The old virtual-atlas test/prototype still uses guarded alias routing,
 * but is never applied to a Level bound to the physical Planet runtime.</p>
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

        // Keep NeoForge's original provider dispatch and physical side.
        // A Level may be bound to Planet even when this particular position
        // lies outside a debug activation region. Do not activate legacy
        // alias routing anywhere on such a Level.
        if (PlanetGravityRuntime.find(level).isPresent()) {
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
