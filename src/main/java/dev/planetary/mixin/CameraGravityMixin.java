package dev.planetary.mixin;

import dev.planetary.client.PlanetCameraRotation;
import dev.planetary.gravity.PlanetEntityGeometry;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Makes Minecraft's actual Camera use the same local gravity frame as entity
 * movement. Entity view vectors alone are not sufficient because Camera.setup
 * normally rebuilds its own global-Y-up quaternion.
 */
@Mixin(Camera.class)
public abstract class CameraGravityMixin {
    private static final Vector3f PLANETARY_FORWARDS =
            new Vector3f(0.0F, 0.0F, -1.0F);
    private static final Vector3f PLANETARY_UP =
            new Vector3f(0.0F, 1.0F, 0.0F);
    private static final Vector3f PLANETARY_LEFT =
            new Vector3f(-1.0F, 0.0F, 0.0F);

    @Shadow
    private Entity entity;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Shadow
    private float roll;

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    protected abstract void setPosition(
            double x,
            double y,
            double z
    );

    @Inject(
            method = "setRotation(FFF)V",
            at = @At("TAIL")
    )
    private void planetary$applyGravityFrameToRotation(
            float yaw,
            float pitch,
            float requestedRoll,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$frame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        Quaternionf worldRotation =
                PlanetCameraRotation.cameraQuaternion(
                        this.yRot,
                        this.xRot,
                        this.roll,
                        frameOptional.get()
                );

        this.rotation.set(worldRotation);

        PLANETARY_FORWARDS.rotate(
                this.rotation,
                this.forwards
        );
        PLANETARY_UP.rotate(
                this.rotation,
                this.up
        );
        PLANETARY_LEFT.rotate(
                this.rotation,
                this.left
        );
    }

    @Inject(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void planetary$applyGravityFrameToEyePosition(
            BlockGetter level,
            Entity cameraEntity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$frame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        double entityX = Mth.lerp(
                (double) partialTick,
                cameraEntity.xo,
                cameraEntity.getX()
        );
        double entityY = Mth.lerp(
                (double) partialTick,
                cameraEntity.yo,
                cameraEntity.getY()
        );
        double entityZ = Mth.lerp(
                (double) partialTick,
                cameraEntity.zo,
                cameraEntity.getZ()
        );

        double interpolatedEyeHeight =
                Mth.lerp(
                        partialTick,
                        this.eyeHeightOld,
                        this.eyeHeight
                );

        Vec3 eyeOffset =
                PlanetEntityGeometry.localOffsetToWorld(
                        frameOptional.get(),
                        0.0D,
                        interpolatedEyeHeight,
                        0.0D
                );

        setPosition(
                entityX + eyeOffset.x,
                entityY + eyeOffset.y,
                entityZ + eyeOffset.z
        );
    }

    private Optional<PlanetGravityFrame> planetary$frame() {
        if (!(this.entity instanceof PlanetGravityEntity gravityEntity)) {
            return Optional.empty();
        }

        return gravityEntity.planetary$gravityFrame();
    }
}
