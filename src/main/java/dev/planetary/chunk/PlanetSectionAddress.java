package dev.planetary.chunk;

import dev.planetary.topology.PlanetFace;

import java.util.Objects;

/**
 * Canonical address of one 16x16x16 section in a face-local planet space.
 *
 * <p>X and Z run along the current face. Y is local radial height:
 * increasing Y moves away from the core, decreasing Y moves inward.</p>
 */
public record PlanetSectionAddress(
        PlanetFace face,
        int x,
        int y,
        int z
) {
    public PlanetSectionAddress {
        Objects.requireNonNull(face, "face");
    }

    public static PlanetSectionAddress fromLocalBlock(
            PlanetFace face,
            int blockX,
            int blockY,
            int blockZ
    ) {
        PlanetSectionPos section = PlanetSection.sectionOfBlock(blockX, blockY, blockZ);
        return new PlanetSectionAddress(face, section.x(), section.y(), section.z());
    }

    public PlanetSectionPos localPos() {
        return new PlanetSectionPos(x, y, z);
    }
}
