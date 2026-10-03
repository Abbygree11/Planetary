package dev.planetary.mixin;

import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Debug/runtime acceptance bridge to invoke the real registered
 * StandingAndWallBlockItem placement implementation without constructing a
 * synthetic Item after registries are frozen.
 */
@Mixin(StandingAndWallBlockItem.class)
public interface StandingAndWallBlockItemAccessor {
    @Invoker("getPlacementState")
    BlockState planetary$invokeGetPlacementState(
            BlockPlaceContext context
    );
}
