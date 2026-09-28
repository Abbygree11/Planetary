package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.topology.PlanetTopology;
import net.minecraft.core.Direction;

import java.util.Objects;

/**
 * Adapter between Minecraft's six Direction values and Planetary's face-local
 * direction frame.
 *
 * <p>Inside a face, vanilla Direction is interpreted locally: UP is away from
 * the core, DOWN is toward the core, and the four horizontal directions lie
 * on the current face. When an operation crosses a cube edge, this adapter
 * rotates that local Direction into the target face frame.</p>
 */
public final class PlanetVanillaDirection {
    private PlanetVanillaDirection() {
    }

    public static PlanetDirection fromVanilla(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return switch (direction) {
            case DOWN -> PlanetDirection.DOWN;
            case UP -> PlanetDirection.UP;
            case NORTH -> PlanetDirection.NORTH;
            case SOUTH -> PlanetDirection.SOUTH;
            case WEST -> PlanetDirection.WEST;
            case EAST -> PlanetDirection.EAST;
        };
    }

    public static Direction toVanilla(PlanetDirection direction) {
        Objects.requireNonNull(direction, "direction");
        return switch (direction) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
        };
    }

    /**
     * Converts a vanilla Direction interpreted in local gravity coordinates
     * into the corresponding ordinary world Direction.
     */
    public static Direction localToWorld(
            PlanetGravityFrame frame,
            Direction localDirection
    ) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(localDirection, "localDirection");

        PlanetVector axis = frame.worldAxis(
                fromVanilla(localDirection)
        );
        return worldDirection(axis);
    }

    /**
     * Converts an ordinary world Direction into the vanilla Direction a block
     * or entity should observe inside the supplied local gravity frame.
     */
    public static Direction worldToLocal(
            PlanetGravityFrame frame,
            Direction worldDirection
    ) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(worldDirection, "worldDirection");

        PlanetVector axis = new PlanetVector(
                worldDirection.getStepX(),
                worldDirection.getStepY(),
                worldDirection.getStepZ()
        );

        return toVanilla(frame.localDirectionOf(axis));
    }

    private static Direction worldDirection(
            PlanetVector axis
    ) {
        for (Direction direction : Direction.values()) {
            if (direction.getStepX() == axis.x()
                    && direction.getStepY() == axis.y()
                    && direction.getStepZ() == axis.z()) {
                return direction;
            }
        }

        throw new IllegalArgumentException(
                "Not a vanilla unit direction: " + axis
        );
    }

    public static Direction transformAcrossEdge(
            PlanetFace sourceFace,
            Direction sourceEdge,
            Direction direction
    ) {
        PlanetDirection edge = fromVanilla(sourceEdge);
        if (!edge.isHorizontal()) {
            throw new IllegalArgumentException("Only horizontal directions identify a cube edge: " + sourceEdge);
        }

        FaceTransform transform = PlanetTopology.edgeTransform(sourceFace, edge);
        return toVanilla(transform.transformDirection(fromVanilla(direction)));
    }
}
