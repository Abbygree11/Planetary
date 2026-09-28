package dev.planetary.gravity;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;

/**
 * The low-level collision kernel used when vanilla's assumption that world Y
 * is the vertical axis is not true.
 *
 * <p>The supplied AABB and VoxelShapes remain in ordinary world coordinates.
 * Only the order and meaning of the three movement components are interpreted
 * in the entity's local gravity frame.</p>
 */
public final class PlanetEntityCollision {
    private PlanetEntityCollision() {
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
            localY = collideLocal(
                    up,
                    box,
                    shapes,
                    localY
            );
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
            localZ = collideLocal(
                    south,
                    box,
                    shapes,
                    localZ
            );
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
            localX = collideLocal(
                    east,
                    box,
                    shapes,
                    localX
            );
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
            localZ = collideLocal(
                    south,
                    box,
                    shapes,
                    localZ
            );
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

        Direction direction = null;
        for (Direction candidate : Direction.values()) {
            if (candidate.getStepX() == world.x()
                    && candidate.getStepY() == world.y()
                    && candidate.getStepZ() == world.z()) {
                direction = candidate;
                break;
            }
        }

        if (direction == null) {
            throw new IllegalStateException(
                    "Frame axis is not a Minecraft Direction: " + world
            );
        }

        return new AxisStep(
                direction.getAxis(),
                direction.getAxisDirection().getStep()
        );
    }

    private record AxisStep(
            Direction.Axis axis,
            int axisSign
    ) {
    }
}
