package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetLivingAnimation;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Keeps vanilla land/air movement local-Y-up and rewrites only the animation
 * calculations that still assume the world XZ plane is the floor.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityGravityMixin {

    @Shadow
    protected float oRun;

    @Shadow
    protected float run;

    @Shadow
    protected abstract void updateWalkAnimation(
            float partialTick
    );

    @Inject(
            method = "calculateEntityAnimation(Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$calculateEntityAnimation(
            boolean includeHeight,
            CallbackInfo ci
    ) {
        LivingEntity self =
                (LivingEntity) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        Vec3 displacement = new Vec3(
                self.getX() - self.xo,
                self.getY() - self.yo,
                self.getZ() - self.zo
        );

        updateWalkAnimation(
                PlanetLivingAnimation.walkDistance(
                        frameOptional.get(),
                        displacement,
                        includeHeight
                )
        );
        ci.cancel();
    }

    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;tickHeadTurn(FF)F",
                    shift = At.Shift.BEFORE
            )
    )
    private void planetary$useLocalRunState(
            CallbackInfo ci
    ) {
        LivingEntity self =
                (LivingEntity) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        PlanetLivingAnimation.State state =
                planetary$animationState(
                        self,
                        frameOptional.get()
                );

        this.run =
                this.oRun
                        + (state.runTarget() - this.oRun)
                        * 0.3F;
    }

    @ModifyArgs(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;tickHeadTurn(FF)F"
            )
    )
    private void planetary$useLocalBodyDirection(
            Args args
    ) {
        LivingEntity self =
                (LivingEntity) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        PlanetLivingAnimation.State state =
                planetary$animationState(
                        self,
                        frameOptional.get()
                );

        args.set(0, state.bodyTargetYaw());
        args.set(1, state.animationStep());
    }

    private static PlanetLivingAnimation.State
            planetary$animationState(
                    LivingEntity entity,
                    PlanetGravityFrame frame
            ) {
        return PlanetLivingAnimation.state(
                frame,
                new Vec3(
                        entity.getX() - entity.xo,
                        entity.getY() - entity.yo,
                        entity.getZ() - entity.zo
                ),
                entity.getYRot(),
                entity.yBodyRot,
                entity.attackAnim,
                entity.onGround()
        );
    }
}
