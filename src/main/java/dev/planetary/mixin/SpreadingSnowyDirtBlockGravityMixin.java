package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetGrowthTopology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-expresses SpreadingSnowyDirtBlock natural tick semantics in canonical
 * local block coordinates.
 */
@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadingSnowyDirtBlockGravityMixin {
    @Inject(
            method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localGrassTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        if (PlanetBlockRuntime.stateFrameAt(
                level,
                pos
        ).isEmpty()) {
            return;
        }

        PlanetBlockNeighborQuery localUp =
                PlanetBlockRuntime.neighbor(
                        level,
                        pos,
                        Direction.UP
                ).orElse(null);
        if (localUp == null) {
            return;
        }

        if (!planetary$canBeGrass(
                state,
                level,
                pos,
                localUp
        )) {
            level.setBlockAndUpdate(
                    pos,
                    Blocks.DIRT.defaultBlockState()
            );
            ci.cancel();
            return;
        }

        if (level.getMaxLocalRawBrightness(
                localUp.targetPos()
        ) >= 9) {
            SpreadingSnowyDirtBlock self =
                    (SpreadingSnowyDirtBlock)
                            (Object) this;
            BlockState spreadState =
                    self.defaultBlockState();

            for (int i = 0; i < 4; i++) {
                int localX =
                        random.nextInt(3) - 1;
                int localY =
                        random.nextInt(5) - 3;
                int localZ =
                        random.nextInt(3) - 1;

                BlockPos target =
                        PlanetGrowthTopology.spreadTarget(
                                level,
                                pos,
                                localX,
                                localY,
                                localZ
                        ).orElse(null);

                if (target == null
                        || !level.getBlockState(target)
                                .is(Blocks.DIRT)) {
                    continue;
                }

                PlanetBlockNeighborQuery targetUp =
                        PlanetBlockRuntime.neighbor(
                                level,
                                target,
                                Direction.UP
                        ).orElse(null);

                if (targetUp == null
                        || !planetary$canPropagate(
                                spreadState,
                                level,
                                target,
                                targetUp
                        )) {
                    continue;
                }

                boolean snowy =
                        level.getBlockState(
                                targetUp.targetPos()
                        ).is(Blocks.SNOW);

                level.setBlockAndUpdate(
                        target,
                        spreadState.setValue(
                                SnowyDirtBlock.SNOWY,
                                snowy
                        )
                );
            }
        }

        ci.cancel();
    }

    private static boolean planetary$canPropagate(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            PlanetBlockNeighborQuery localUp
    ) {
        return planetary$canBeGrass(
                state,
                level,
                pos,
                localUp
        ) && !level.getFluidState(
                localUp.targetPos()
        ).is(FluidTags.WATER);
    }

    private static boolean planetary$canBeGrass(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            PlanetBlockNeighborQuery localUp
    ) {
        BlockPos upPos =
                localUp.targetPos();
        BlockState upState =
                level.getBlockState(
                        upPos
                );

        if (upState.is(Blocks.SNOW)
                && upState.getValue(
                        SnowLayerBlock.LAYERS
                ) == 1) {
            return true;
        }

        if (upState.getFluidState()
                .getAmount() == 8) {
            return false;
        }

        int lightBlock =
                LightEngine.getLightBlockInto(
                        level,
                        state,
                        pos,
                        upState,
                        upPos,
                        localUp.physicalDirection(),
                        upState.getLightBlock(
                                level,
                                upPos
                        )
                );

        return lightBlock
                < level.getMaxLightLevel();
    }
}
