package dev.planetary.mixin;

import dev.planetary.api.PlanetOriginalHitSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Version-specific read-only bridge; leaves vanilla placement lifecycle intact. */
@Mixin(UseOnContext.class)
public interface UseOnContextOriginalHitInvoker extends PlanetOriginalHitSource {
    @Override
    @Invoker("getHitResult")
    BlockHitResult planetary$originalHit();
}
