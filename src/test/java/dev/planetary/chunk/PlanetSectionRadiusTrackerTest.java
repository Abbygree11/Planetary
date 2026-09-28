package dev.planetary.chunk;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetSectionRadiusTrackerTest {
    @Test
    void firstMoveLoadsTheWholeSphere() {
        PlanetSectionRadiusTracker tracker = new PlanetSectionRadiusTracker(20);

        PlanetSectionLoadDelta delta = tracker.moveTo(new PlanetSectionPos(0, 0, 0));

        assertEquals(33_401, delta.toLoad().size());
        assertTrue(delta.toUnload().isEmpty());
        assertEquals(33_401, tracker.activeSections().size());
    }

    @Test
    void stayingInsideTheSameSectionDoesNoWork() {
        PlanetSectionRadiusTracker tracker = new PlanetSectionRadiusTracker(8);
        PlanetSectionPos center = new PlanetSectionPos(2, 3, 4);

        tracker.moveTo(center);
        PlanetSectionLoadDelta second = tracker.moveTo(center);

        assertTrue(second.isEmpty());
    }

    @Test
    void movingOneSectionOnlyChangesTheEnteringAndLeavingShell() {
        int radius = 20;
        PlanetSectionRadiusTracker tracker = new PlanetSectionRadiusTracker(radius);
        PlanetSectionPos first = new PlanetSectionPos(0, 0, 0);
        PlanetSectionPos second = new PlanetSectionPos(1, 0, 0);

        tracker.moveTo(first);
        PlanetSectionLoadDelta delta = tracker.moveTo(second);

        assertFalse(delta.isEmpty());
        assertEquals(delta.toLoad().size(), delta.toUnload().size());
        assertTrue(delta.toLoad().size() < 33_401 / 10);
        assertEquals(PlanetSectionLoadShape.sphere(second, radius), tracker.activeSections());
    }

    @Test
    void resizingChangesOnlyTheOuterShell() {
        PlanetSectionRadiusTracker tracker = new PlanetSectionRadiusTracker(6);
        PlanetSectionPos center = new PlanetSectionPos(-3, 9, 5);
        tracker.moveTo(center);

        PlanetSectionLoadDelta grow = tracker.resize(8);
        assertFalse(grow.toLoad().isEmpty());
        assertTrue(grow.toUnload().isEmpty());
        assertEquals(PlanetSectionLoadShape.sphere(center, 8), tracker.activeSections());

        PlanetSectionLoadDelta shrink = tracker.resize(5);
        assertTrue(shrink.toLoad().isEmpty());
        assertFalse(shrink.toUnload().isEmpty());
        assertEquals(PlanetSectionLoadShape.sphere(center, 5), tracker.activeSections());
    }

    @Test
    void clearUnloadsEverything() {
        PlanetSectionRadiusTracker tracker = new PlanetSectionRadiusTracker(4);
        tracker.moveTo(new PlanetSectionPos(0, 0, 0));
        Set<PlanetSectionPos> before = tracker.activeSections();

        PlanetSectionLoadDelta delta = tracker.clear();

        assertEquals(before, delta.toUnload());
        assertTrue(delta.toLoad().isEmpty());
        assertTrue(tracker.activeSections().isEmpty());
    }
}
