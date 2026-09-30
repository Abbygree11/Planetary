package dev.planetary.mixin;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomStrollGoal.class)
public interface RandomStrollGoalAccessor {
    @Accessor("mob")
    PathfinderMob planetary$getMob();
}
