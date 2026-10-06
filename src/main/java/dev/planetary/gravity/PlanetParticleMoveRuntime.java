package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Minecraft-facing local-frame implementation of vanilla Particle.move.
 *
 * <p>The semantic algorithm is kept outside the Mixin so future Minecraft
 * ports only need to rewire field/hook access when vanilla signatures move.</p>
 *
 * <p>Version-sensitive mirrored vanilla details:
 * - movement length gate: 10000.0;
 * - the hasNearBlocks optimization;
 * - setLocationFromBoundingbox convention: center X/Z + minY.
 *
 * Re-audit these details on every Minecraft version port.</p>
 */
public final class PlanetParticleMoveRuntime {
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED =
            10000.0D;

    private PlanetParticleMoveRuntime() {
    }

    public static void move(
            Particle particle,
            PlanetParticleMoveAccess state,
            PlanetGravityFrame frame,
            double requestedX,
            double requestedY,
            double requestedZ,
            BlockPos.MutableBlockPos nearBlockPos
    ) {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(
                nearBlockPos,
                "nearBlockPos"
        );

        if (state.planetary$isStoppedByCollision()) {
            return;
        }

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

        AABB box =
                particle.getBoundingBox();

        if (state.planetary$hasPhysics()
                && movementLengthSquared != 0.0D
                && movementLengthSquared
                < MAXIMUM_COLLISION_VELOCITY_SQUARED
                && hasNearBlocks(
                        state,
                        requestedX,
                        requestedY,
                        requestedZ,
                        box,
                        nearBlockPos
                )) {
            List<VoxelShape> colliders =
                    new ArrayList<>();

            for (VoxelShape shape
                    : state.planetary$getLevel()
                    .getBlockCollisions(
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

            state.planetary$setX(
                    (moved.minX + moved.maxX) * 0.5D
            );
            state.planetary$setY(
                    moved.minY
            );
            state.planetary$setZ(
                    (moved.minZ + moved.maxZ) * 0.5D
            );
        }

        PlanetParticleCollisionResponse.Result response =
                PlanetParticleCollisionResponse.apply(
                        requestedMovement,
                        actualMovement,
                        new Vec3(
                                state.planetary$getXd(),
                                state.planetary$getYd(),
                                state.planetary$getZd()
                        ),
                        frame
                );

        state.planetary$setStoppedByCollision(
                response.stoppedByCollision()
        );
        state.planetary$setOnGround(
                response.onGround()
        );

        Vec3 correctedVelocity =
                response.correctedWorldVelocity();

        state.planetary$setXd(
                correctedVelocity.x
        );
        state.planetary$setYd(
                correctedVelocity.y
        );
        state.planetary$setZd(
                correctedVelocity.z
        );
    }

    private static boolean hasNearBlocks(
            PlanetParticleMoveAccess state,
            double dx,
            double dy,
            double dz,
            AABB box,
            BlockPos.MutableBlockPos mutablePos
    ) {
        if (state.planetary$getBbWidth() > 1.0F
                || state.planetary$getBbHeight() > 1.0F) {
            return true;
        }

        int x =
                Mth.floor(
                        state.planetary$getX()
                );
        int y =
                Mth.floor(
                        state.planetary$getY()
                );
        int z =
                Mth.floor(
                        state.planetary$getZ()
                );

        mutablePos.set(
                x,
                y,
                z
        );

        if (!state.planetary$getLevel()
                .getBlockState(
                        mutablePos
                )
                .isAir()) {
            return true;
        }

        double edgeX =
                dx > 0.0D
                        ? box.maxX
                        : dx < 0.0D
                        ? box.minX
                        : state.planetary$getX();
        double edgeY =
                dy > 0.0D
                        ? box.maxY
                        : dy < 0.0D
                        ? box.minY
                        : state.planetary$getY();
        double edgeZ =
                dz > 0.0D
                        ? box.maxZ
                        : dz < 0.0D
                        ? box.minZ
                        : state.planetary$getZ();

        int targetX =
                Mth.floor(
                        edgeX + dx
                );
        int targetY =
                Mth.floor(
                        edgeY + dy
                );
        int targetZ =
                Mth.floor(
                        edgeZ + dz
                );

        if (targetX == x
                && targetY == y
                && targetZ == z) {
            return false;
        }

        mutablePos.set(
                targetX,
                targetY,
                targetZ
        );

        return !state.planetary$getLevel()
                .getBlockState(
                        mutablePos
                )
                .isAir();
    }
}
