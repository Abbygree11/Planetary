package dev.planetary.mixin;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Reframes only the semantic direction ordering used to choose standing vs
 * wall variants. Physical hit/target geometry remains vanilla.
 */
@Mixin(StandingAndWallBlockItem.class)
public abstract class StandingAndWallBlockItemMixin {
    @Redirect(
            method = "getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/context/BlockPlaceContext;getNearestLookingDirections()[Lnet/minecraft/core/Direction;"
            )
    )
    private Direction[] planetary$localVariantOrder(
            BlockPlaceContext context
    ) {
        return PlanetFrameApi.localNearestLookingDirections(
                        context
                )
                .map(list ->
                        list.toArray(
                                Direction[]::new
                        )
                )
                .orElseGet(
                        context::getNearestLookingDirections
                );
    }
}
