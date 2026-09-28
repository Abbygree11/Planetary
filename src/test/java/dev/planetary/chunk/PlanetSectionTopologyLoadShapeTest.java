package dev.planetary.chunk;

import dev.planetary.topology.PlanetFace;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionTopologyLoadShapeTest {
    private static final int FACE_SIZE = 256;

    @Test
    void interiorRenderDistanceTwentyKeepsTheFlatSphereCount() {
        PlanetSectionAddress center = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE / 2,
                -20,
                FACE_SIZE / 2
        );

        Set<PlanetSectionAddress> sections = PlanetSectionTopologyLoadShape.sphere(
                center,
                20,
                FACE_SIZE
        );

        assertEquals(33_401, sections.size());
        assertEquals(
                136_810_496L,
                PlanetSectionTopologyLoadShape.logicalBlockCapacity(sections.size())
        );
    }

    @Test
    void edgeSphereContinuesOntoTheNeighboringGravityFace() {
        PlanetSectionAddress center = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                0,
                FACE_SIZE / 2
        );

        Set<PlanetSectionAddress> sections = PlanetSectionTopologyLoadShape.sphere(
                center,
                6,
                FACE_SIZE
        );

        assertTrue(sections.stream().anyMatch(p -> p.face() == PlanetFace.POS_Y));
        assertTrue(sections.stream().anyMatch(p -> p.face() == PlanetFace.POS_X));
        assertAllCanonical(sections);
    }

    @Test
    void cornerSphereCanReachBothAdjacentFacesWithoutInvalidCoordinates() {
        PlanetSectionAddress center = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                0,
                FACE_SIZE - 1
        );

        Set<PlanetSectionAddress> sections = PlanetSectionTopologyLoadShape.sphere(
                center,
                6,
                FACE_SIZE
        );

        assertTrue(sections.stream().anyMatch(p -> p.face() == PlanetFace.POS_Y));
        assertTrue(sections.stream().anyMatch(p -> p.face() == PlanetFace.POS_X));
        assertTrue(sections.stream().anyMatch(p -> p.face() == PlanetFace.POS_Z));
        assertAllCanonical(sections);
    }

    private static void assertAllCanonical(Set<PlanetSectionAddress> sections) {
        for (PlanetSectionAddress section : sections) {
            assertTrue(section.x() >= 0 && section.x() < FACE_SIZE, section.toString());
            assertTrue(section.z() >= 0 && section.z() < FACE_SIZE, section.toString());
        }
    }
}
