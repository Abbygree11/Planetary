package dev.planetary.world;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class PlanetFrontAndTopPlacementTest {
    @Test
    void crafterCoversExactlyTwelveLegalFrontTopStates() {
        Set<FrontAndTop> results = EnumSet.noneOf(FrontAndTop.class);
        for (Direction front : Direction.values()) {
            for (Direction heading : Direction.Plane.HORIZONTAL) {
                Optional<FrontAndTop> value = PlanetFrontAndTopPlacement
                        .crafterOrientation(front, Optional.of(heading));
                assertTrue(value.isPresent(), "front=" + front + " heading=" + heading);
                assertEquals(front, value.get().front());
                assertEquals(front == Direction.DOWN ? heading.getOpposite()
                                : front == Direction.UP ? heading : Direction.UP,
                        value.get().top());
                results.add(value.get());
            }
        }
        assertEquals(12, results.size());
        assertTrue(PlanetFrontAndTopPlacement
                .crafterOrientation(Direction.NORTH, Optional.empty()).isPresent());
        assertTrue(PlanetFrontAndTopPlacement
                .crafterOrientation(Direction.UP, Optional.empty()).isEmpty());
        assertTrue(PlanetFrontAndTopPlacement
                .crafterOrientation(Direction.UP, Optional.of(Direction.UP)).isEmpty());
    }

    @Test
    void jigsawHasIndependentClickedFaceRuleAndTwelveStates() {
        Set<FrontAndTop> results = EnumSet.noneOf(FrontAndTop.class);
        for (Direction clicked : Direction.values()) {
            for (Direction heading : Direction.Plane.HORIZONTAL) {
                FrontAndTop orientation = PlanetFrontAndTopPlacement
                        .jigsawOrientation(clicked, Optional.of(heading)).orElseThrow();
                assertEquals(clicked, orientation.front());
                assertEquals(clicked.getAxis().isVertical()
                        ? heading.getOpposite() : Direction.UP, orientation.top());
                results.add(orientation);
            }
        }
        assertEquals(12, results.size());
        assertTrue(PlanetFrontAndTopPlacement
                .jigsawOrientation(Direction.EAST, Optional.empty()).isPresent());
        assertTrue(PlanetFrontAndTopPlacement
                .jigsawOrientation(Direction.DOWN, Optional.empty()).isEmpty());
        assertTrue(PlanetFrontAndTopPlacement
                .jigsawOrientation(Direction.DOWN, Optional.of(Direction.DOWN)).isEmpty());
    }

    @Test
    void originalPlusYBaselineKeepsDistinctAuthorAlgorithms() {
        assertEquals(FrontAndTop.NORTH_UP, PlanetFrontAndTopPlacement
                .crafterOrientation(Direction.NORTH, Optional.of(Direction.SOUTH))
                .orElseThrow());
        assertEquals(FrontAndTop.NORTH_UP, PlanetFrontAndTopPlacement
                .jigsawOrientation(Direction.NORTH, Optional.of(Direction.WEST))
                .orElseThrow());
        assertEquals(FrontAndTop.UP_EAST, PlanetFrontAndTopPlacement
                .crafterOrientation(Direction.UP, Optional.of(Direction.EAST))
                .orElseThrow());
    }
}
