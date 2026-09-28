package dev.planetary.gravity;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gravity-frame collision and step-up support.
 */
public final class PlanetEntityCollision {
    private static final double STEP_EPSILON = 1.0E-5D;

    private PlanetEntityCollision() {
    }

    public static Vec3 collide(
            Entity entity,
            Vec3 worldMovement,
            AABB entityBox,
            Level level,
            PlanetGravityFrame frame,
            float maxUpStep,
            boolean onGround
    ) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(worldMovement, "worldMovement");
        Objects.requireNonNull(entityBox, "entityBox");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(frame, "frame");

        List<VoxelShape> initialEntityCollisions =
                level.getEntityCollisions(
                        entity,
                        entityBox.expandTowards(worldMovement)
                );
        List<VoxelShape> colliders = collectColliders(
                entity,
                level,
                initialEntityCollisions,
                entityBox.expandTowards(worldMovement)
        );

        Vec3 actual = worldMovement.lengthSqr() == 0.0D
                ? worldMovement
                : collideWithShapes(
                        worldMovement,
                        entityBox,
                        colliders,
                        frame
                );

        PlanetEntityMotion.CollisionResult result =
                PlanetEntityMotion.classify(
                        worldMovement,
                        actual,
                        frame
                );

        if (maxUpStep <= 0.0F
                || !(result.verticalCollisionBelow() || onGround)
                || !result.horizontalCollision()) {
            return actual;
        }

        PlanetFrameVector requestedLocal =
                result.requestedLocal();
        PlanetFrameVector actualLocal =
                result.actualLocal();

        AABB stepBase = result.verticalCollisionBelow()
                ? entityBox.move(
                        PlanetEntityGeometry.localOffsetToWorld(
                                frame,
                                0.0,
                                actualLocal.y(),
                                0.0
                        )
                )
                : entityBox;

        Vec3 stepSearchVector =
                PlanetEntityGeometry.localOffsetToWorld(
                        frame,
                        requestedLocal.x(),
                        maxUpStep,
                        requestedLocal.z()
                );

        AABB stepSearch = stepBase.expandTowards(stepSearchVector);
        if (!result.verticalCollisionBelow()) {
            stepSearch = stepSearch.expandTowards(
                    PlanetEntityGeometry.localOffsetToWorld(
                            frame,
                            0.0,
                            -STEP_EPSILON,
                            0.0
                    )
            );
        }

        List<VoxelShape> stepColliders = collectColliders(
                entity,
                level,
                initialEntityCollisions,
                stepSearch
        );

        float[] heights = collectCandidateStepUpHeights(
                stepBase,
                stepColliders,
                frame,
                maxUpStep,
                (float) actualLocal.y()
        );

        double baseHorizontal =
                actualLocal.x() * actualLocal.x()
                        + actualLocal.z() * actualLocal.z();

        for (float height : heights) {
            Vec3 candidateRequest =
                    PlanetEntityGeometry.localOffsetToWorld(
                            frame,
                            requestedLocal.x(),
                            height,
                            requestedLocal.z()
                    );

            Vec3 candidate = collideWithShapes(
                    candidateRequest,
                    stepBase,
                    stepColliders,
                    frame
            );
            PlanetFrameVector candidateLocal =
                    frame.worldToLocal(
                            new PlanetFrameVector(
                                    candidate.x,
                                    candidate.y,
                                    candidate.z
                            )
                    );

            double candidateHorizontal =
                    candidateLocal.x() * candidateLocal.x()
                            + candidateLocal.z() * candidateLocal.z();

            if (candidateHorizontal > baseHorizontal) {
                if (result.verticalCollisionBelow()) {
                    candidate = candidate.add(
                            PlanetEntityGeometry.localOffsetToWorld(
                                    frame,
                                    0.0,
                                    actualLocal.y(),
                                    0.0
                            )
                    );
                }
                return candidate;
            }
        }

        return actual;
    }

    public static Vec3 collideWithShapes(
            Vec3 worldMovement,
            AABB entityBox,
            List<VoxelShape> shapes,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(worldMovement, "worldMovement");
        Objects.requireNonNull(entityBox, "entityBox");
        Objects.requireNonNull(shapes, "shapes");
        Objects.requireNonNull(frame, "frame");

        if (shapes.isEmpty()) {
            return worldMovement;
        }

        PlanetFrameVector local = frame.worldToLocal(
                new PlanetFrameVector(
                        worldMovement.x,
                        worldMovement.y,
                        worldMovement.z
                )
        );

        double localX = local.x();
        double localY = local.y();
        double localZ = local.z();
        AABB box = entityBox;

        AxisStep up = axisStep(frame, PlanetDirection.UP);
        AxisStep east = axisStep(frame, PlanetDirection.EAST);
        AxisStep south = axisStep(frame, PlanetDirection.SOUTH);

        if (localY != 0.0D) {
            localY = collideLocal(up, box, shapes, localY);
            if (localY != 0.0D) {
                box = box.move(
                        PlanetEntityGeometry.localOffsetToWorld(
                                frame,
                                0.0,
                                localY,
                                0.0
                        )
                );
            }
        }

        boolean zFirst = Math.abs(localX) < Math.abs(localZ);

        if (zFirst && localZ != 0.0D) {
            localZ = collideLocal(south, box, shapes, localZ);
            if (localZ != 0.0D) {
                box = box.move(
                        PlanetEntityGeometry.localOffsetToWorld(
                                frame,
                                0.0,
                                0.0,
                                localZ
                        )
                );
            }
        }

        if (localX != 0.0D) {
            localX = collideLocal(east, box, shapes, localX);
            if (!zFirst && localX != 0.0D) {
                box = box.move(
                        PlanetEntityGeometry.localOffsetToWorld(
                                frame,
                                localX,
                                0.0,
                                0.0
                        )
                );
            }
        }

        if (!zFirst && localZ != 0.0D) {
            localZ = collideLocal(south, box, shapes, localZ);
        }

        PlanetFrameVector result = frame.localToWorld(
                new PlanetFrameVector(
                        localX,
                        localY,
                        localZ
                )
        );

        return new Vec3(result.x(), result.y(), result.z());
    }

    private static List<VoxelShape> collectColliders(
            Entity entity,
            Level level,
            List<VoxelShape> entityCollisions,
            AABB box
    ) {
        List<VoxelShape> result =
                new ArrayList<>(entityCollisions.size() + 8);
        result.addAll(entityCollisions);

        WorldBorder worldBorder = level.getWorldBorder();
        if (worldBorder.isInsideCloseToBorder(entity, box)) {
            result.add(worldBorder.getCollisionShape());
        }

        for (VoxelShape shape : level.getBlockCollisions(entity, box)) {
            result.add(shape);
        }

        return result;
    }

    private static float[] collectCandidateStepUpHeights(
            AABB box,
            List<VoxelShape> colliders,
            PlanetGravityFrame frame,
            float maxStep,
            float currentVerticalMovement
    ) {
        AxisStep up = axisStep(frame, PlanetDirection.UP);
        List<Float> values = new ArrayList<>(4);

        double base = localMinimum(box, up);

        for (VoxelShape shape : colliders) {
            for (double coordinate : shape.getCoords(up.axis())) {
                float height = (float) (
                        coordinate * up.axisSign() - base
                );

                if (height < 0.0F
                        || height == currentVerticalMovement
                        || height > maxStep) {
                    continue;
                }

                if (!values.contains(height)) {
                    values.add(height);
                }
            }
        }

        values.sort(Float::compare);
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private static double localMinimum(
            AABB box,
            AxisStep up
    ) {
        double worldCoordinate = switch (up.axis()) {
            case X -> up.axisSign() > 0 ? box.minX : box.maxX;
            case Y -> up.axisSign() > 0 ? box.minY : box.maxY;
            case Z -> up.axisSign() > 0 ? box.minZ : box.maxZ;
        };

        return worldCoordinate * up.axisSign();
    }

    private static double collideLocal(
            AxisStep step,
            AABB box,
            List<VoxelShape> shapes,
            double localDistance
    ) {
        double worldDistance =
                localDistance * step.axisSign();

        double clipped = Shapes.collide(
                step.axis(),
                box,
                shapes,
                worldDistance
        );

        return clipped * step.axisSign();
    }

    private static AxisStep axisStep(
            PlanetGravityFrame frame,
            PlanetDirection localDirection
    ) {
        PlanetVector world = frame.worldAxis(localDirection);

        for (Direction direction : Direction.values()) {
            if (direction.getStepX() == world.x()
                    && direction.getStepY() == world.y()
                    && direction.getStepZ() == world.z()) {
                return new AxisStep(
                        direction.getAxis(),
                        direction.getAxisDirection().getStep()
                );
            }
        }

        throw new IllegalStateException(
                "Frame axis is not a Minecraft Direction: " + world
        );
    }

    private record AxisStep(
            Direction.Axis axis,
            int axisSign
    ) {
    }
}
