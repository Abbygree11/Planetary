package dev.planetary.world;

import dev.planetary.chunk.PlanetSection;
import dev.planetary.chunk.PlanetSectionAddress;
import dev.planetary.topology.PlanetFace;

import java.util.Objects;

/**
 * Canonical block address in one face-local cube-planet coordinate frame.
 *
 * <p>X/Z lie on the face. Y is local radial height: increasing Y points
 * away from the core, decreasing Y points inward.</p>
 */
public record PlanetBlockPos(
        PlanetFace face,
        int x,
        int y,
        int z
) {
    public PlanetBlockPos {
        Objects.requireNonNull(face, "face");
    }

    public PlanetSectionAddress sectionAddress() {
        return new PlanetSectionAddress(
                face,
                Math.floorDiv(x, PlanetSection.SIZE),
                Math.floorDiv(y, PlanetSection.SIZE),
                Math.floorDiv(z, PlanetSection.SIZE)
        );
    }

    public int localX() {
        return Math.floorMod(x, PlanetSection.SIZE);
    }

    public int localY() {
        return Math.floorMod(y, PlanetSection.SIZE);
    }

    public int localZ() {
        return Math.floorMod(z, PlanetSection.SIZE);
    }
}
