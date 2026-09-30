package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.gravity.PlanetWalkNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(GroundPathNavigation.class)
public abstract class GroundPathNavigationGravityMixin
        extends PathNavigation {
    protected GroundPathNavigationGravityMixin(
            Mob mob,
            Level level
    ) {
        super(mob, level);
    }

    @Inject(
            method = "createPathFinder",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$createLocalPathFinder(
            int maxVisitedNodes,
            CallbackInfoReturnable<PathFinder> cir
    ) {
        if (PlanetGravityRuntime.findFor(this.mob).isEmpty()) {
            return;
        }

        PlanetWalkNodeEvaluator evaluator =
                new PlanetWalkNodeEvaluator();
        evaluator.setCanPassDoors(true);

        this.nodeEvaluator = evaluator;
        cir.setReturnValue(
                new PathFinder(
                        evaluator,
                        maxVisitedNodes
                )
        );
    }

    @Inject(
            method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$skipWorldYTargetAdjustment(
            BlockPos pos,
            int accuracy,
            CallbackInfoReturnable<Path> cir
    ) {
        if (PlanetGravityRuntime.findFor(this.mob).isEmpty()) {
            return;
        }

        cir.setReturnValue(
                super.createPath(
                        Stream.of(pos),
                        accuracy
                )
        );
    }

    @Inject(
            method = "getTempMobPos",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$usePhysicalAnchor(
            CallbackInfoReturnable<net.minecraft.world.phys.Vec3> cir
    ) {
        if (PlanetGravityRuntime.findFor(this.mob).isPresent()) {
            cir.setReturnValue(this.mob.position());
        }
    }
}
