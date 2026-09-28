package dev.planetary.topology;

import java.util.Arrays;

/**
 * One of the six gravity faces of the cube planet.
 *
 * <p>Each face owns a right-handed local Minecraft frame:</p>
 * <ul>
 *     <li>local +Y / UP points away from the core;</li>
 *     <li>local +X / EAST lies along the face;</li>
 *     <li>local +Z / SOUTH lies along the face.</li>
 * </ul>
 */
public enum PlanetFace {
    POS_X(AxisVector.POS_X, AxisVector.NEG_Y, AxisVector.POS_Z),
    NEG_X(AxisVector.NEG_X, AxisVector.POS_Y, AxisVector.POS_Z),
    POS_Y(AxisVector.POS_Y, AxisVector.POS_X, AxisVector.POS_Z),
    NEG_Y(AxisVector.NEG_Y, AxisVector.POS_X, AxisVector.NEG_Z),
    POS_Z(AxisVector.POS_Z, AxisVector.POS_X, AxisVector.NEG_Y),
    NEG_Z(AxisVector.NEG_Z, AxisVector.NEG_X, AxisVector.NEG_Y);

    private final AxisVector up;
    private final AxisVector east;
    private final AxisVector south;

    PlanetFace(AxisVector up, AxisVector east, AxisVector south) {
        if (!east.cross(up).equals(south)) {
            throw new IllegalArgumentException("Face basis must satisfy EAST x UP = SOUTH");
        }
        this.up = up;
        this.east = east;
        this.south = south;
    }

    AxisVector upAxis() {
        return up;
    }

    AxisVector axisFor(PlanetDirection direction) {
        return switch (direction) {
            case UP -> up;
            case DOWN -> up.negate();
            case EAST -> east;
            case WEST -> east.negate();
            case SOUTH -> south;
            case NORTH -> south.negate();
        };
    }

    PlanetDirection localDirectionOf(AxisVector axis) {
        return Arrays.stream(PlanetDirection.values())
                .filter(direction -> axisFor(direction).equals(axis))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Axis is not part of face frame: " + axis));
    }

    public PlanetFace neighborAcross(PlanetDirection edge) {
        if (!edge.isHorizontal()) {
            throw new IllegalArgumentException("Only horizontal directions identify a face edge: " + edge);
        }
        return fromNormal(axisFor(edge));
    }

    static PlanetFace fromNormal(AxisVector normal) {
        return Arrays.stream(values())
                .filter(face -> face.up.equals(normal))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No cube face for normal " + normal));
    }
}

record AxisVector(int x, int y, int z) {
    static final AxisVector POS_X = new AxisVector(1, 0, 0);
    static final AxisVector NEG_X = new AxisVector(-1, 0, 0);
    static final AxisVector POS_Y = new AxisVector(0, 1, 0);
    static final AxisVector NEG_Y = new AxisVector(0, -1, 0);
    static final AxisVector POS_Z = new AxisVector(0, 0, 1);
    static final AxisVector NEG_Z = new AxisVector(0, 0, -1);

    AxisVector negate() {
        return new AxisVector(-x, -y, -z);
    }

    int dot(AxisVector other) {
        return x * other.x + y * other.y + z * other.z;
    }

    AxisVector cross(AxisVector other) {
        return new AxisVector(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }
}
