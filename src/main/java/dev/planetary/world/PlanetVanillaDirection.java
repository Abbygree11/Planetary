package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
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
