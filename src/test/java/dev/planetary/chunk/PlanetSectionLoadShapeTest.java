package dev.planetary.chunk;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionLoadShapeTest {
    @Test
    void radiusZeroContainsOnlyThePlayerSection() {
        PlanetSectionPos center = new PlanetSectionPos(4, -7, 11);
        assertEquals(Set.of(center), PlanetSectionLoadShape.sphere(center, 0));
    }

    @Test
    void renderDistanceTwentyContains33401Sections() {
        Set<PlanetSectionPos> sections = PlanetSectionLoadShape.sphere(
                new PlanetSectionPos(0, 0, 0),
                20
        );

        assertEquals(33_401, sections.size());
        assertEquals(136_810_496L, PlanetSectionLoadShape.logicalBlockCapacity(sections.size()));
    }

    @Test
    void sphereIncludesBoundaryButExcludesAnythingBeyondIt() {
        Set<PlanetSectionPos> sections = PlanetSectionLoadShape.sphere(
                new PlanetSectionPos(0, 0, 0),
                20
        );

        assertTrue(sections.contains(new PlanetSectionPos(20, 0, 0)));
        assertTrue(sections.contains(new PlanetSectionPos(-20, 0, 0)));
        assertTrue(sections.contains(new PlanetSectionPos(0, 0, 20)));
        assertFalse(sections.contains(new PlanetSectionPos(20, 1, 0)));
        assertFalse(sections.contains(new PlanetSectionPos(21, 0, 0)));
    }

    @Test
    void everyGeneratedSectionIsInsideTheEuclideanRadius() {
        PlanetSectionPos center = new PlanetSectionPos(100, -50, 7);
        int radius = 12;

        for (PlanetSectionPos pos : PlanetSectionLoadShape.sphere(center, radius)) {
            int dx = pos.x() - center.x();
            int dy = pos.y() - center.y();
            int dz = pos.z() - center.z();
            assertTrue(PlanetSectionLoadShape.containsOffset(dx, dy, dz, radius));
        }
    }

    @Test
    void shapeIsSymmetricAroundThePlayer() {
        Set<PlanetSectionPos> sections = PlanetSectionLoadShape.sphere(
                new PlanetSectionPos(0, 0, 0),
                8
        );

        for (PlanetSectionPos pos : sections) {
            assertTrue(sections.contains(new PlanetSectionPos(-pos.x(), -pos.y(), -pos.z())));
        }
    }
}
