package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetEntityCollision;
import dev.planetary.gravity.PlanetParticleCollisionResponse;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Rotationally equivalent vanilla Particle.move for TerrainParticle.
 *
 * <p>Vanilla resolves collision in world Y -> world X/Z order and interprets
 * Y as semantic vertical. That is correct only for +Y gravity. Block-destroy
 * TerrainParticles on rotated Planet faces therefore acquire a different
 * post-contact movement phase.</p>
 *
 * <p>This adapter is intentionally TerrainParticle-only. It keeps vanilla's
 * physical AABB and near-block optimization, but resolves the collision in the
 * active local frame and applies vanilla response semantics to local axes.</p>
 */
@Mixin(Particle.class)
public abstract class TerrainParticleMoveGravityMixin {
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED =
            10000.0D;

    @Shadow
    @Final
    protected ClientLevel level;

    @Shadow
    protected double x;

    @Shadow
    protected double y;

    @Shadow
    protected double z;

    @Shadow
    protected double xd;

    @Shadow
    protected double yd;

    @Shadow
    protected double zd;

    @Shadow
    protected boolean onGround;

    @Shadow
    protected boolean hasPhysics;

    @Shadow
    private boolean stoppedByCollision;

    @Inject(
            method = "move(DDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$moveTerrainParticleInLocalFrame(
            double requestedX,
            double requestedY,
            double requestedZ,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof TerrainParticle)) {
            return;
        }

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        this.level,
                        this.x,
                        this.y,
                        this.z
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        // From this point this method replaces vanilla Particle.move exactly
        // for TerrainParticle on a rotated Planet face.
        ci.cancel();

        if (this.stoppedByCollision) {
            return;
        }

        PlanetGravityFrame frame =
                frameOptional.get();

        Vec3 requestedMovement =
                new Vec3(
                        requestedX,
                        requestedY,
                        requestedZ
                );
        Vec3 actualMovement =
                requestedMovement;

        double movementLengthSquared =
                requestedX * requestedX
                        + requestedY * requestedY
                        + requestedZ * requestedZ;

        Particle particle =
                (Particle) (Object) this;
        ParticleGravityAccessor accessor =
                (ParticleGravityAccessor) (Object) this;

        AABB box =
                particle.getBoundingBox();

        if (this.hasPhysics
                && movementLengthSquared != 0.0D
                && movementLengthSquared
                < MAXIMUM_COLLISION_VELOCITY_SQUARED
                && accessor.planetary$invokeHasNearBlocks(
                        requestedX,
                        requestedY,
                        requestedZ
                )) {
            List<VoxelShape> colliders =
                    new ArrayList<>();

            for (VoxelShape shape
                    : this.level.getBlockCollisions(
                            null,
                            box.expandTowards(
                                    requestedMovement
                            )
                    )) {
                colliders.add(shape);
            }

            actualMovement =
                    PlanetEntityCollision.collideWithShapes(
                            requestedMovement,
                            box,
                            colliders,
                            frame
                    );
        }

        if (actualMovement.x != 0.0D
                || actualMovement.y != 0.0D
                || actualMovement.z != 0.0D) {
            AABB moved =
                    box.move(actualMovement);

            particle.setBoundingBox(moved);

            // Exact vanilla Particle.setLocationFromBoundingbox convention.
            this.x =
                    (moved.minX + moved.maxX) * 0.5D;
            this.y =
                    moved.minY;
            this.z =
                    (moved.minZ + moved.maxZ) * 0.5D;
        }

        PlanetParticleCollisionResponse.Result response =
                PlanetParticleCollisionResponse.apply(
                        requestedMovement,
                        actualMovement,
                        new Vec3(
                                this.xd,
                                this.yd,
                                this.zd
                        ),
                        frame
                );

        this.stoppedByCollision =
                response.stoppedByCollision();
        this.onGround =
                response.onGround();

        Vec3 correctedVelocity =
                response.correctedWorldVelocity();

        this.xd = correctedVelocity.x;
        this.yd = correctedVelocity.y;
        this.zd = correctedVelocity.z;
    }
}
