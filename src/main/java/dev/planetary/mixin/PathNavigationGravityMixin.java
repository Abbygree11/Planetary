package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(PathNavigation.class)
public abstract class PathNavigationGravityMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Shadow
    @Nullable
    protected Path path;

    @Shadow
    protected float maxDistanceToWaypoint;

    @Shadow
    protected abstract void doStuckDetection(
            Vec3 positionVec3
    );

    @Inject(
            method = "getGroundY",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$doNotProjectToWorldY(
            Vec3 target,
            CallbackInfoReturnable<Double> cir
    ) {
        if (planetary$frame().isPresent()) {
            cir.setReturnValue(target.y);
        }
    }

    @Inject(
            method = "followThePath",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$followInLocalFrame(
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frame =
                planetary$frame();

        if (frame.isEmpty()
                || this.path == null
                || this.path.isDone()) {
            return;
        }

        Vec3 current = this.mob.position();
        Vec3 target =
                this.path.getNextEntityPos(this.mob);

        PlanetFrameVector localDelta =
                frame.get().worldToLocal(
                        new PlanetFrameVector(
                                target.x - current.x,
                                target.y - current.y,
                                target.z - current.z
                        )
                );

        this.maxDistanceToWaypoint =
                this.mob.getBbWidth() > 0.75F
                        ? this.mob.getBbWidth() / 2.0F
                        : 0.75F
                                - this.mob.getBbWidth() / 2.0F;

        boolean reached =
                Math.abs(localDelta.x())
                                <= this.maxDistanceToWaypoint
                        && Math.abs(localDelta.z())
                                <= this.maxDistanceToWaypoint
                        && Math.abs(localDelta.y()) < 1.0D;

        if (reached) {
            this.path.advance();
        }

        this.doStuckDetection(current);
        ci.cancel();
    }

    @Inject(
            method = "isStableDestination",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localStableDestination(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Optional<PlanetGravityFrame> frame =
                planetary$frame();

        if (frame.isEmpty()) {
            return;
        }

        BlockPos support =
                pos.relative(
                        PlanetBlockGravity.localDown(
                                this.mob.level(),
                                pos,
                                frame.get().face()
                        )
                );

        cir.setReturnValue(
                this.mob.level()
                        .getBlockState(support)
                        .isSolidRender(
                                this.mob.level(),
                                support
                        )
        );
    }

    private Optional<PlanetGravityFrame> planetary$frame() {
        return ((PlanetGravityEntity) this.mob)
                .planetary$gravityFrame();
    }
}
