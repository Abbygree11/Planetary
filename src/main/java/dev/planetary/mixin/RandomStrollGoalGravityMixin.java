package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.gravity.PlanetMobRandomTarget;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces vanilla XZ/Y random-walk target generation with a Planet tangent
 * target. This is especially important near cube edges, where vanilla picks a
 * point in the wrong physical plane and makes navigation chase unstable goals.
 */
@Mixin({
        RandomStrollGoal.class,
        WaterAvoidingRandomStrollGoal.class
})
public abstract class RandomStrollGoalGravityMixin {
    @Inject(
            method = "getPosition",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localRandomPosition(
            CallbackInfoReturnable<Vec3> cir
    ) {
        PathfinderMob mob =
                ((RandomStrollGoalAccessor) (Object) this)
                        .planetary$getMob();

        if (PlanetGravityRuntime.findFor(mob).isEmpty()) {
            return;
        }

        PlanetMobRandomTarget.randomStroll(
                mob,
                10
        ).ifPresent(cir::setReturnValue);
    }
}
