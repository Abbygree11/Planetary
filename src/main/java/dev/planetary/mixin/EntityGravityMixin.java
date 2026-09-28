package dev.planetary.mixin;

import dev.planetary.gravity.PlanetEntityGeometry;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * First entity-facing gravity hooks.
 *
 * <p>Movement and acceleration are intentionally not changed here yet. This
 * mixin establishes the gravity-aware entity geometry that those systems will
 * consume in the next layer.</p>
 */
@Mixin(Entity.class)
public abstract class EntityGravityMixin
        implements PlanetGravityEntity {

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    private float eyeHeight;

    @Unique
    private PlanetFace planetary$preferredGravityFace;

    @Override
    public Optional<PlanetGravityFrame> planetary$gravityFrame() {
        return PlanetGravityRuntime.find(level())
                .flatMap(field ->
                        field.selectEntityFrame(
                                getX(),
                                getY(),
                                getZ(),
                                planetary$preferredGravityFace,
                                1.0E-7D
                        )
                )
                .map(frame -> {
                    planetary$preferredGravityFace = frame.face();
                    return frame;
                });
    }

    @Override
    public Optional<PlanetFace> planetary$gravityFace() {
        return planetary$gravityFrame()
                .map(PlanetGravityFrame::face);
    }

    @Inject(
            method = "makeBoundingBox()Lnet/minecraft/world/phys/AABB;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$makeBoundingBox(
            CallbackInfoReturnable<AABB> cir
    ) {
        planetary$gravityFrame().ifPresent(frame -> {
            if (frame.face() == PlanetFace.POS_Y) {
                return;
            }

            cir.setReturnValue(
                    PlanetEntityGeometry.rotateVanillaBoundingBox(
                            cir.getReturnValue(),
                            position(),
                            frame
                    )
            );
        });
    }

    @Inject(
            method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$getEyePosition(
            CallbackInfoReturnable<Vec3> cir
    ) {
        planetary$gravityFrame().ifPresent(frame -> {
            if (frame.face() == PlanetFace.POS_Y) {
                return;
            }

            cir.setReturnValue(
                    PlanetEntityGeometry.eyePosition(
                            position(),
                            eyeHeight,
                            frame
                    )
            );
        });
    }

    @Inject(
            method = "getBlockPosBelowThatAffectsMyMovement()Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$getBlockPosBelowThatAffectsMyMovement(
            CallbackInfoReturnable<BlockPos> cir
    ) {
        planetary$gravityFrame().ifPresent(frame -> {
            if (frame.face() == PlanetFace.POS_Y) {
                return;
            }

            cir.setReturnValue(
                    PlanetEntityGeometry.blockBelow(
                            position(),
                            frame
                    )
            );
        });
    }
}
