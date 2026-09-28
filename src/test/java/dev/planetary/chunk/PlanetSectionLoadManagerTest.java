package dev.planetary.chunk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionLoadManagerTest {
    @Test
    void renderAndSimulationUseIndependentThreeDimensionalRadii() {
        PlanetSectionLoadManager manager = new PlanetSectionLoadManager(20, 8);

        manager.movePlayerTo(new PlanetSectionPos(0, 0, 0));

        assertEquals(33_401, manager.renderedSections().size());
        assertEquals(2_109, manager.simulatedSections().size());
        assertTrue(manager.renderedSections().containsAll(manager.simulatedSections()));
    }

    @Test
    void distanceChangesDoNotRequireMovingThePlayer() {
        PlanetSectionLoadManager manager = new PlanetSectionLoadManager(8, 4);
        PlanetSectionPos center = new PlanetSectionPos(10, -20, 30);
        manager.movePlayerTo(center);

        PlanetSectionActivityUpdate update = manager.setDistances(10, 6);

        assertTrue(!update.render().toLoad().isEmpty());
        assertTrue(!update.simulation().toLoad().isEmpty());
        assertEquals(PlanetSectionLoadShape.sphere(center, 10), manager.renderedSections());
        assertEquals(PlanetSectionLoadShape.sphere(center, 6), manager.simulatedSections());
    }

    @Test
    void simulationDistanceCannotExceedRenderDistance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlanetSectionLoadManager(8, 9)
        );
    }
}
