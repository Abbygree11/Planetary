package dev.planetary.topology;

import java.util.Objects;

/**
 * One local Minecraft coordinate frame selected by the cubic gravity field.
 *
 * <p>Local coordinates deliberately keep vanilla semantics:
 * x=EAST, y=UP, z=SOUTH. This class only rotates that local frame into the
 * planet's ordinary world XYZ grid.</p>
 *
 * <p>Planet entities keep deltaMovement in this local frame so vanilla
 * movement code can continue treating Y as vertical. Physical collision is
 * transformed to world XYZ at the Entity.move boundary. When the selected
 * face changes, local velocity is re-expressed through world space so physical
 * momentum remains continuous.</p>
 */
public final class PlanetGravityFrame {
    private final PlanetFace face;
    private final PlanetVector east;
    private final PlanetVector up;
    private final PlanetVector south;

    public PlanetGravityFrame(PlanetFace face) {
        this.face = Objects.requireNonNull(face, "face");
        this.east = face.worldVector(PlanetDirection.EAST);
        this.up = face.worldVector(PlanetDirection.UP);
        this.south = face.worldVector(PlanetDirection.SOUTH);
    }

    public PlanetFace face() {
        return face;
    }

    public PlanetVector worldAxis(
            PlanetDirection localDirection
    ) {
        return face.worldVector(
                Objects.requireNonNull(
                        localDirection,
                        "localDirection"
                )
        );
    }

    public PlanetDirection localDirectionOf(
            PlanetVector worldAxis
    ) {
        return face.localDirectionOf(
                Objects.requireNonNull(worldAxis, "worldAxis")
        );
    }

    public PlanetFrameVector localToWorld(
            PlanetFrameVector local
    ) {
        Objects.requireNonNull(local, "local");

        return new PlanetFrameVector(
                east.x() * local.x()
                        + up.x() * local.y()
                        + south.x() * local.z(),
                east.y() * local.x()
                        + up.y() * local.y()
                        + south.y() * local.z(),
                east.z() * local.x()
                        + up.z() * local.y()
                        + south.z() * local.z()
        );
    }

    public PlanetFrameVector worldToLocal(
            PlanetFrameVector world
    ) {
        Objects.requireNonNull(world, "world");

        return new PlanetFrameVector(
                dot(world, east),
                dot(world, up),
                dot(world, south)
        );
    }

    /**
     * Re-expresses a local vector in another gravity frame while preserving
     * the exact physical world-space vector.
     */
    public PlanetFrameVector transformLocalTo(
            PlanetGravityFrame target,
            PlanetFrameVector local
    ) {
        Objects.requireNonNull(target, "target");
        return target.worldToLocal(localToWorld(local));
    }

    public PlanetVector worldUp() {
        return up;
    }

    public PlanetVector worldDown() {
        return face.worldVector(PlanetDirection.DOWN);
    }

    public PlanetVector worldEast() {
        return east;
    }

    public PlanetVector worldSouth() {
        return south;
    }

    private static double dot(
            PlanetFrameVector vector,
            PlanetVector axis
    ) {
        return vector.x() * axis.x()
                + vector.y() * axis.y()
                + vector.z() * axis.z();
    }

    @Override
    public String toString() {
        return "PlanetGravityFrame[" + face + "]";
    }
}
