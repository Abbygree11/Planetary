package dev.planetary.topology;

/**
 * A direction in a face-local Minecraft coordinate frame.
 *
 * <p>X points east, Y points away from the planet core, and Z points south.
 * The important invariant is that DOWN always means "toward the core" for
 * the current face, regardless of the face's physical orientation.</p>
 */
public enum PlanetDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int dx;
    private final int dy;
    private final int dz;

    PlanetDirection(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int dz() {
        return dz;
    }

    public boolean isHorizontal() {
        return dy == 0;
    }

    public PlanetDirection opposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }
}
