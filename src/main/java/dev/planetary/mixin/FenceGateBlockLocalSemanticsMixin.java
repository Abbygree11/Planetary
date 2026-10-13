package dev.planetary.mixin;

import dev.planetary.world.PlanetFenceGatePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Thin Minecraft/NeoForge 1.21.1 adapter for all inherited FenceGateBlock
 * placement, wall adjacency and hand opening logic. No HEAD cancellation,
 * no rewrite of physical Level neighbors or redstone and sound behavior.
 */
@Mixin(FenceGateBlock.class)
public abstract class FenceGateBlockLocalSemanticsMixin {
    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true
    )
    private void planetary$gatePlacement(BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir) {
        PlanetFenceGatePlacement.afterPlacement(context, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }

    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true
    )
    private void planetary$gateWallUpdate(
            BlockState state, Direction physicalSide, BlockState neighbor,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir) {
        PlanetFenceGatePlacement.afterNeighborUpdate(level, pos, cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }

    /** Redirect only the owner's one player-heading read; original body, sounds,
     * events, signal updates and block writes all remain in vanilla. */
    @Redirect(
            method = "useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getDirection()Lnet/minecraft/core/Direction;")
    )
    private Direction planetary$gateInteractionHeading(
            Player player, BlockState state, Level level, BlockPos pos,
            Player actor, BlockHitResult hit) {
        return PlanetFenceGatePlacement.interactingPlayerDirection(player, level, pos);
    }
}
