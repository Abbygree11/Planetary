package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Lets vanilla MoveControl reason in the mob's local gravity frame.
 *
 * <p>Vanilla computes target vertical distance as wantedY - mobY and therefore
 * interprets ordinary movement along world Y on a side face as a reason to
 * jump. The redirects below make those three deltas local X/Y/Z while leaving
 * the stored AI target in ordinary world coordinates.</p>
 */
@Mixin(MoveControl.class)
public abstract class MoveControlGravityMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Shadow
    protected double wantedX;

    @Shadow
    protected double wantedY;

    @Shadow
    protected double wantedZ;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getX()D",
                    ordinal = 0
            )
    )
    private double planetary$localTargetX(
            Mob mob
    ) {
        Optional<PlanetFrameVector> local =
                planetary$localTargetDelta();

        return local
                .map(delta -> this.wantedX - delta.x())
                .orElseGet(mob::getX);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getZ()D",
                    ordinal = 0
            )
    )
    private double planetary$localTargetZ(
            Mob mob
    ) {
        Optional<PlanetFrameVector> local =
                planetary$localTargetDelta();

        return local
                .map(delta -> this.wantedZ - delta.z())
                .orElseGet(mob::getZ);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getY()D",
                    ordinal = 0
            )
    )
    private double planetary$localTargetY(
            Mob mob
    ) {
        Optional<PlanetFrameVector> local =
                planetary$localTargetDelta();

        return local
                .map(delta -> this.wantedY - delta.y())
                .orElseGet(mob::getY);
    }

    /**
     * Vanilla's second getY() belongs to a collision-shape jump heuristic that
     * compares the mob against world Axis.Y. Until path collision is fully
     * local-frame aware, suppress only that heuristic outside POS_Y. Ordinary
     * local target-height jumping above remains active.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getY()D",
                    ordinal = 1
            )
    )
    private double planetary$skipWorldYObstacleJump(
            Mob mob
    ) {
        Optional<PlanetGravityFrame> frame =
                planetary$frame();

        if (frame.isEmpty()
                || frame.get().face() == PlanetFace.POS_Y) {
            return mob.getY();
        }

        return Double.POSITIVE_INFINITY;
    }

    /**
     * isWalkable() currently projects strafe input into world X/Z and asks the
     * vanilla node evaluator. That projection is invalid on side/bottom faces.
     * Collision still prevents movement through blocks, so do not let the
     * invalid path check rewrite strafe input until node evaluation is rotated.
     */
    @Inject(
            method = "isWalkable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$skipWorldXZWalkability(
            float relativeX,
            float relativeZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Optional<PlanetGravityFrame> frame =
                planetary$frame();

        if (frame.isPresent()
                && frame.get().face() != PlanetFace.POS_Y) {
            cir.setReturnValue(true);
        }
    }

    private Optional<PlanetGravityFrame> planetary$frame() {
        return ((PlanetGravityEntity) this.mob)
                .planetary$gravityFrame();
    }

    private Optional<PlanetFrameVector>
            planetary$localTargetDelta() {
        return planetary$frame().map(
                frame -> frame.worldToLocal(
                        new PlanetFrameVector(
                                this.wantedX - this.mob.getX(),
                                this.wantedY - this.mob.getY(),
                                this.wantedZ - this.mob.getZ()
                        )
                )
        );
    }
}
