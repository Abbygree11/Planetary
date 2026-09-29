package dev.planetary.mixin;

import dev.planetary.gravity.PlanetEntityControl;
import dev.planetary.gravity.PlanetEntitySupport;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Land/air movement for entities living in a Planet gravity frame.
 *
 * <p>The real deltaMovement stays in world coordinates. Vanilla-style
 * acceleration, friction, levitation and gravity are evaluated in local
 * coordinates where local Y is the selected planet UP axis.</p>
 *
 * <p>Water, lava, climbing and elytra have additional world-Y assumptions and
 * are intentionally left for dedicated follow-up hooks.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityGravityMixin {

    @Shadow
    protected abstract float getJumpPower();

    @Inject(
            method = "aiStep()V",
            at = @At("HEAD")
    )
    private void planetary$refreshLocalGroundedState(
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

        boolean grounded =
                PlanetEntitySupport.isGrounded(
                        self,
                        frameOptional.get()
                );

        if (self.onGround() != grounded) {
            self.setOnGround(grounded);
        }
    }

    @Inject(
            method = "jumpFromGround()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$jumpFromGround(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self).planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        float jumpPower = getJumpPower();
        if (jumpPower <= 1.0E-5F) {
            ci.cancel();
            return;
        }

        self.setDeltaMovement(
                PlanetEntityControl.jumpVelocity(
                        self.getDeltaMovement(),
                        jumpPower,
                        self.getYRot(),
                        self.isSprinting(),
                        frameOptional.get()
                )
        );
        self.hasImpulse = true;
        CommonHooks.onLivingJump(self);
        ci.cancel();
    }

    @Inject(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$travelOnLand(
            Vec3 travelVector,
            CallbackInfo ci
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) self).planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y
                || !self.isControlledByLocalInstance()) {
            return;
        }

        // Dedicated hooks are required for these movement modes because they
        // contain their own global-Y buoyancy / lift / climb assumptions.
        if (self.isInWater()
                || self.isInLava()
                || self.isFallFlying()) {
            return;
        }

        if (self instanceof Player player
                && player.getAbilities().flying) {
            return;
        }

        PlanetGravityFrame frame = frameOptional.get();

        PlanetFrameVector velocityBefore =
                toLocal(frame, self.getDeltaMovement());

        double gravity = self.getGravity();
        boolean falling = velocityBefore.y() <= 0.0D;
        if (falling && self.hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = Math.min(gravity, 0.01D);
        }

        BlockPos floorPos =
                self.getBlockPosBelowThatAffectsMyMovement();
        BlockState floorState =
                self.level().getBlockState(floorPos);

        float friction = floorState.getFriction(
                self.level(),
                floorPos,
                self
        );
        float horizontalDamping =
                self.onGround()
                        ? friction * 0.91F
                        : 0.91F;

        Vec3 movedWorld =
                self.handleRelativeFrictionAndCalculateMovement(
                        travelVector,
                        friction
                );

        PlanetFrameVector movedLocal =
                toLocal(frame, movedWorld);
        double vertical = movedLocal.y();

        if (self.hasEffect(MobEffects.LEVITATION)) {
            int amplifier = self.getEffect(
                    MobEffects.LEVITATION
            ).getAmplifier();

            vertical += (
                    0.05D * (amplifier + 1)
                            - movedLocal.y()
            ) * 0.2D;
        } else {
            vertical -= gravity;
        }

        PlanetFrameVector finalLocal;
        if (self.shouldDiscardFriction()) {
            finalLocal = new PlanetFrameVector(
                    movedLocal.x(),
                    vertical,
                    movedLocal.z()
            );
        } else {
            double verticalDamping =
                    self instanceof FlyingAnimal
                            ? horizontalDamping
                            : 0.98F;

            finalLocal = new PlanetFrameVector(
                    movedLocal.x() * horizontalDamping,
                    vertical * verticalDamping,
                    movedLocal.z() * horizontalDamping
            );
        }

        PlanetFrameVector finalWorld =
                frame.localToWorld(finalLocal);

        self.setDeltaMovement(
                finalWorld.x(),
                finalWorld.y(),
                finalWorld.z()
        );

        self.calculateEntityAnimation(
                self instanceof FlyingAnimal
        );
        ci.cancel();
    }

    private static PlanetFrameVector toLocal(
            PlanetGravityFrame frame,
            Vec3 world
    ) {
        return frame.worldToLocal(
                new PlanetFrameVector(
                        world.x,
                        world.y,
                        world.z
                )
        );
    }
}
