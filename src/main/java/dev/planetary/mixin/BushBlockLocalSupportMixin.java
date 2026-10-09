package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * BushBlock has a virtual mayPlaceOn support test. Reframe only its
 * BlockPos.below() query, leaving the existing polymorphic soil predicate
 * and generic updateShape survival dispatch intact for vanilla and mods.
 */
@Mixin(BushBlock.class)
public abstract class BushBlockLocalSupportMixin {
    @Redirect(
            method = "canSurvive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos planetary$localSoilBelow(
            BlockPos physicalSource,
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        return PlanetBlockSupportRuntime.query(
                level, physicalSource, Direction.DOWN
        ).map(query -> query.supportPos())
                .orElseGet(physicalSource::below);
    }
}
