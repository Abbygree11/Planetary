package dev.planetary.topology;

import java.util.Objects;

/**
 * Integer position in one face-local coordinate space.
 *
 * <p>X and Z run along the face. Y is local vertical: increasing Y moves
 * away from the core and decreasing Y moves toward it.</p>
 */
public record PlanetPos(PlanetFace face, int x, int y, int z) {
    public PlanetPos {
        Objects.requireNonNull(face, "face");
    }

    public PlanetPos offsetLocal(PlanetDirection direction) {
        return new PlanetPos(
                face,
                x + direction.dx(),
                y + direction.dy(),
                z + direction.dz()
        );
    }
}
