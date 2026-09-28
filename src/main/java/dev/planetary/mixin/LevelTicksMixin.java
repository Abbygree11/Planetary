package dev.planetary.mixin;

import dev.planetary.world.PlanetVanillaTickBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps vanilla LevelTicks as the public API while routing virtual Planetary
 * positions into the topology-aware scheduler bound to that ServerLevel.
 */
@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin<T> {

    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    private void planetary$schedule(
            ScheduledTick<T> tick,
            CallbackInfo ci
    ) {
        LevelTicks<?> ticks = (LevelTicks<?>) (Object) this;
        if (PlanetVanillaTickBridge.schedule(ticks, tick)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "hasScheduledTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$hasScheduledTick(
            BlockPos pos,
            T type,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LevelTicks<?> ticks = (LevelTicks<?>) (Object) this;
        PlanetVanillaTickBridge.hasScheduledTick(ticks, pos, type)
                .ifPresent(cir::setReturnValue);
    }

    @Inject(
            method = "willTickThisTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$willTickThisTick(
            BlockPos pos,
            T type,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LevelTicks<?> ticks = (LevelTicks<?>) (Object) this;
        PlanetVanillaTickBridge.willTickThisTick(ticks, pos, type)
                .ifPresent(cir::setReturnValue);
    }

    @Inject(method = "count", at = @At("RETURN"), cancellable = true)
    private void planetary$count(
            CallbackInfoReturnable<Integer> cir
    ) {
        LevelTicks<?> ticks = (LevelTicks<?>) (Object) this;
        cir.setReturnValue(
                cir.getReturnValue()
                        + PlanetVanillaTickBridge.additionalCount(ticks)
        );
    }
}
