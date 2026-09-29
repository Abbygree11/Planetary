package dev.planetary.mixin;

import dev.planetary.gravity.PlanetEntityCollision;
import dev.planetary.gravity.PlanetEntityControl;
import dev.planetary.gravity.PlanetEntityGeometry;
import dev.planetary.gravity.PlanetEntityMotion;
import dev.planetary.gravity.PlanetEntityOrientation;
import dev.planetary.gravity.PlanetEntitySupport;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Gravity-aware Entity geometry and movement semantics.
 *
 * <p>Physical position and deltaMovement remain in ordinary world XYZ. Only
 * the parts of vanilla movement that interpret Y as "vertical" are temporarily
 * expressed in the selected local gravity frame.</p>
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
    public abstract float getYRot();

    @Shadow
    public abstract void setYRot(float yRot);

    @Shadow
    public float yRotO;

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract float maxUpStep();

    @Shadow
    public abstract boolean onGround();

    @Shadow
    public boolean horizontalCollision;

    @Shadow
    public boolean verticalCollision;

    @Shadow
    public boolean verticalCollisionBelow;

    @Shadow
    public boolean minorHorizontalCollision;

    @Shadow
    private float eyeHeight;

    @Shadow
    private Optional<BlockPos> mainSupportingBlockPos;

    @Shadow
    private boolean onGroundNoBlocks;

    @Unique
    private PlanetFace planetary$preferredGravityFace;

    @Unique
    private PlanetEntityMotion.CollisionResult
            planetary$lastCollisionResult;

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
                    PlanetFace previousFace =
                            planetary$preferredGravityFace;
                    PlanetFace nextFace = frame.face();

                    if (previousFace != null
                            && previousFace != nextFace) {
                        PlanetEntityOrientation.transportYaw(
                                previousFace,
                                nextFace,
                                getYRot()
                        ).ifPresent(targetYaw -> {
                            setYRot(targetYaw);
                            yRotO = targetYaw;
                        });
                    }

                    planetary$preferredGravityFace = nextFace;
                    return frame;
                });
    }

    @Override
    public Optional<PlanetFace> planetary$gravityFace() {
        return planetary$gravityFrame()
                .map(PlanetGravityFrame::face);
    }

    @Inject(
            method = "onGround()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$onGround(
            CallbackInfoReturnable<Boolean> cir
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        Entity self = (Entity) (Object) this;
        cir.setReturnValue(
                PlanetEntitySupport.isGrounded(
                        self,
                        frameOptional.get()
                )
        );
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
            method = "calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$calculateViewVector(
            float xRot,
            float yRot,
            CallbackInfoReturnable<Vec3> cir
    ) {
        planetary$gravityFrame().ifPresent(frame -> {
            if (frame.face() == PlanetFace.POS_Y) {
                return;
            }

            Vec3 local = cir.getReturnValue();
            PlanetFrameVector world =
                    frame.localToWorld(
                            new PlanetFrameVector(
                                    local.x,
                                    local.y,
                                    local.z
                            )
                    );

            cir.setReturnValue(
                    new Vec3(
                            world.x(),
                            world.y(),
                            world.z()
                    )
            );
        });
    }

    @Inject(
            method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$getInterpolatedEyePosition(
            float partialTicks,
            CallbackInfoReturnable<Vec3> cir
    ) {
        planetary$gravityFrame().ifPresent(frame -> {
            if (frame.face() == PlanetFace.POS_Y) {
                return;
            }

            Entity self = (Entity) (Object) this;
            cir.setReturnValue(
                    PlanetEntityGeometry.eyePosition(
                            self.getPosition(partialTicks),
                            eyeHeight,
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

    @Inject(
            method = "getOnPos(F)Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$getOnPos(
            float localDownOffset,
            CallbackInfoReturnable<BlockPos> cir
    ) {
        planetary$gravityFrame().ifPresent(frame -> {
            if (frame.face() == PlanetFace.POS_Y) {
                return;
            }

            if (mainSupportingBlockPos.isPresent()) {
                cir.setReturnValue(mainSupportingBlockPos.get());
                return;
            }

            Vec3 offset =
                    PlanetEntityGeometry.localOffsetToWorld(
                            frame,
                            0.0,
                            -localDownOffset,
                            0.0
                    );
            cir.setReturnValue(
                    BlockPos.containing(position().add(offset))
            );
        });
    }

    @Inject(
            method = "checkSupportingBlock(ZLnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$checkSupportingBlock(
            boolean onGround,
            Vec3 localMovement,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$gravityFrame();
        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        ci.cancel();
        PlanetGravityFrame frame = frameOptional.get();

        if (!onGround) {
            onGroundNoBlocks = false;
            mainSupportingBlockPos = Optional.empty();
            return;
        }

        AABB support = PlanetEntityGeometry.supportSlice(
                getBoundingBox(),
                frame,
                1.0E-6D
        );

        Entity self = (Entity) (Object) this;
        Optional<BlockPos> supporting =
                level().findSupportingBlock(self, support);

        if (supporting.isEmpty()
                && !onGroundNoBlocks
                && localMovement != null) {
            Vec3 rewind =
                    PlanetEntityGeometry.localOffsetToWorld(
                            frame,
                            -localMovement.x,
                            0.0,
                            -localMovement.z
                    );
            supporting = level().findSupportingBlock(
                    self,
                    support.move(rewind)
            );
        }

        mainSupportingBlockPos = supporting;
        onGroundNoBlocks = supporting.isEmpty();
    }

    @Inject(
            method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$collide(
            Vec3 worldMovement,
            CallbackInfoReturnable<Vec3> cir
    ) {
        planetary$lastCollisionResult = null;

        Optional<PlanetGravityFrame> frameOptional =
                planetary$gravityFrame();
        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        PlanetGravityFrame frame = frameOptional.get();
        Entity self = (Entity) (Object) this;
        Vec3 actualMovement =
                PlanetEntityCollision.collide(
                        self,
                        worldMovement,
                        getBoundingBox(),
                        level(),
                        frame,
                        maxUpStep(),
                        onGround()
                );

        planetary$lastCollisionResult =
                PlanetEntityMotion.classify(
                        worldMovement,
                        actualMovement,
                        frame
                );

        cir.setReturnValue(actualMovement);
    }

    @Redirect(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZLnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void planetary$setOnGroundFromLocalCollision(
            Entity entity,
            boolean vanillaOnGround,
            Vec3 movement
    ) {
        PlanetEntityMotion.CollisionResult result =
                planetary$lastCollisionResult;

        if (result == null) {
            entity.setOnGroundWithMovement(
                    vanillaOnGround,
                    movement
            );
            return;
        }

        this.horizontalCollision =
                result.horizontalCollision();
        this.verticalCollision =
                result.verticalCollision();
        this.verticalCollisionBelow =
                result.verticalCollisionBelow();

        if (!this.horizontalCollision) {
            this.minorHorizontalCollision = false;
        }

        PlanetFrameVector actualLocal =
                result.actualLocal();

        entity.setOnGroundWithMovement(
                result.verticalCollisionBelow(),
                new Vec3(
                        actualLocal.x(),
                        actualLocal.y(),
                        actualLocal.z()
                )
        );
    }

    @ModifyVariable(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 0
            ),
            ordinal = 0,
            argsOnly = true
    )
    private Vec3 planetary$requestedMovementToLocal(
            Vec3 worldMovement
    ) {
        return planetary$toLocalWhenActive(worldMovement);
    }

    @ModifyVariable(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private Vec3 planetary$actualMovementToLocal(
            Vec3 worldMovement
    ) {
        return planetary$toLocalWhenActive(worldMovement);
    }

    @Redirect(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 planetary$getDeltaMovementInLocalFrame(
            Entity entity
    ) {
        return planetary$toLocalWhenActive(
                entity.getDeltaMovement()
        );
    }

    @Redirect(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void planetary$setLocalDeltaMovementVector(
            Entity entity,
            Vec3 localMovement
    ) {
        entity.setDeltaMovement(
                planetary$toWorldWhenActive(localMovement)
        );
    }

    @Redirect(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(DDD)V"
            )
    )
    private void planetary$setLocalDeltaMovementComponents(
            Entity entity,
            double x,
            double y,
            double z
    ) {
        Vec3 world = planetary$toWorldWhenActive(
                new Vec3(x, y, z)
        );
        entity.setDeltaMovement(world);
    }

    @Inject(
            method = "moveRelative(FLnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$moveRelative(
            float amount,
            Vec3 localInput,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$gravityFrame();
        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        Entity self = (Entity) (Object) this;
        Vec3 worldInput =
                PlanetEntityControl.relativeInputToWorld(
                        localInput,
                        amount,
                        self.getYRot(),
                        frameOptional.get()
                );

        self.setDeltaMovement(
                self.getDeltaMovement().add(worldInput)
        );
        ci.cancel();
    }

    @Unique
    private Vec3 planetary$toLocalWhenActive(Vec3 world) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$gravityFrame();
        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return world;
        }

        PlanetFrameVector local =
                frameOptional.get().worldToLocal(
                        new PlanetFrameVector(
                                world.x,
                                world.y,
                                world.z
                        )
                );
        return new Vec3(local.x(), local.y(), local.z());
    }

    @Unique
    private Vec3 planetary$toWorldWhenActive(Vec3 local) {
        Optional<PlanetGravityFrame> frameOptional =
                planetary$gravityFrame();
        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return local;
        }

        PlanetFrameVector world =
                frameOptional.get().localToWorld(
                        new PlanetFrameVector(
                                local.x,
                                local.y,
                                local.z
                        )
                );
        return new Vec3(world.x(), world.y(), world.z());
    }
}
