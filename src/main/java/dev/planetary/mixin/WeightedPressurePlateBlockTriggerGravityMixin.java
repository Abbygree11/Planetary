package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetPlacementRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeightedPressurePlateBlock.class)
public abstract class WeightedPressurePlateBlockTriggerGravityMixin {
    private static final AABB PLANETARY$LOCAL_TOUCH =
            new AABB(
                    0.0625D,
                    0.0D,
                    0.0625D,
                    0.9375D,
                    0.25D,
                    0.9375D
            );

    @Inject(
            method = "getSignalStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localTrigger(
            Level level,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (PlanetBlockRuntime.stateFrameAt(level, pos).isEmpty()) {
            return;
        }

        AABB box =
                PlanetPlacementRuntime.localBoxToWorld(
                        level,
                        pos,
                        PLANETARY$LOCAL_TOUCH
                ).orElseThrow();

        int maxWeight =
                ((WeightedPressurePlateBlockAccessor) this)
                        .planetary$getMaxWeight();

        int count =
                Math.min(
                        level.getEntitiesOfClass(
                                Entity.class,
                                box,
                                EntitySelector.NO_SPECTATORS.and(
                                        entity ->
                                                !entity.isIgnoringBlockTriggers()
                                )
                        ).size(),
                        maxWeight
                );

        if (count <= 0) {
            cir.setReturnValue(0);
            return;
        }

        float ratio =
                (float) count
                        / (float) maxWeight;
        cir.setReturnValue(
                Mth.ceil(
                        ratio * 15.0F
                )
        );
    }
}
