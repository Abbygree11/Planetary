package dev.planetary.chunk;

import dev.planetary.topology.PlanetFace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionLoadManagerTest {
    private static final int FACE_SIZE = 256;

    @Test
    void renderAndSimulationUseIndependentThreeDimensionalRadii() {
        PlanetSectionLoadManager manager = new PlanetSectionLoadManager(FACE_SIZE, 20, 8);
        PlanetSectionAddress center = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE / 2,
                0,
                FACE_SIZE / 2
        );

        manager.movePlayerTo(center);

        assertEquals(33_401, manager.renderedSections().size());
        assertEquals(2_109, manager.simulatedSections().size());
        assertTrue(manager.renderedSections().containsAll(manager.simulatedSections()));
    }

    @Test
    void renderSphereWrapsOntoNeighborFaceAtAnEdge() {
        PlanetSectionLoadManager manager = new PlanetSectionLoadManager(FACE_SIZE, 8, 4);
        PlanetSectionAddress center = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                0,
                FACE_SIZE / 2
        );

        manager.movePlayerTo(center);

        assertTrue(
                manager.renderedSections().stream()
                        .anyMatch(section -> section.face() == PlanetFace.POS_X)
        );
        assertTrue(
                manager.renderedSections().stream()
                        .anyMatch(section -> section.face() == PlanetFace.POS_Y)
        );
    }

    @Test
    void movingAcrossAnEdgeProducesOnlyCanonicalLoadAndUnloadDeltas() {
        PlanetSectionLoadManager manager = new PlanetSectionLoadManager(FACE_SIZE, 8, 4);
        PlanetSectionAddress onTopEdge = new PlanetSectionAddress(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                0,
                FACE_SIZE / 2
        );

        manager.movePlayerTo(onTopEdge);

        PlanetSectionAddress onRightFace = PlanetSectionTopology.step(
                onTopEdge,
                dev.planetary.topology.PlanetDirection.EAST,
                FACE_SIZE
        );

        PlanetSectionAddressActivityUpdate update = manager.movePlayerTo(onRightFace);

        assertTrue(!update.render().isEmpty());
        assertTrue(manager.renderedSections().containsAll(update.render().toLoad()));
        assertTrue(update.render().toUnload().stream().noneMatch(manager.renderedSections()::contains));
        assertTrue(manager.renderedSections().contains(onRightFace));
    }

    @Test
    void distanceChangesDoNotRequireMovingThePlayer() {
        PlanetSectionLoadManager manager = new PlanetSectionLoadManager(FACE_SIZE, 8, 4);
        PlanetSectionAddress center = new PlanetSectionAddress(
                PlanetFace.NEG_Z,
                100,
                -20,
                120
        );
        manager.movePlayerTo(center);

        PlanetSectionAddressActivityUpdate update = manager.setDistances(10, 6);

        assertTrue(!update.render().toLoad().isEmpty());
        assertTrue(!update.simulation().toLoad().isEmpty());
        assertEquals(
                PlanetSectionTopologyLoadShape.sphere(center, 10, FACE_SIZE),
                manager.renderedSections()
        );
        assertEquals(
                PlanetSectionTopologyLoadShape.sphere(center, 6, FACE_SIZE),
                manager.simulatedSections()
        );
    }

    @Test
    void simulationDistanceCannotExceedRenderDistance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlanetSectionLoadManager(FACE_SIZE, 8, 9)
        );
    }
}
