package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetVanillaDirectionTest {
    @Test
    void vanillaDirectionsRoundTripThroughLocalPlanetFrame() {
        for (Direction direction : Direction.values()) {
            assertEquals(
                    direction,
                    PlanetVanillaDirection.toVanilla(
                            PlanetVanillaDirection.fromVanilla(direction)
                    )
            );
        }
    }

    @Test
    void crossingTopEastEdgeRotatesTheHorizontalFrameOntoPositiveXFace() {
        assertEquals(
                Direction.EAST,
                PlanetVanillaDirection.transformAcrossEdge(
                        PlanetFace.POS_Y,
                        Direction.EAST,
                        Direction.EAST
                )
        );

        assertEquals(
                Direction.WEST,
                PlanetVanillaDirection.transformAcrossEdge(
                        PlanetFace.POS_Y,
                        Direction.EAST,
                        Direction.WEST
                )
        );

        assertEquals(
                Direction.SOUTH,
                PlanetVanillaDirection.transformAcrossEdge(
                        PlanetFace.POS_Y,
                        Direction.EAST,
                        Direction.SOUTH
                )
        );
    }

    @Test
    void localUpAndDownRemainLocalUpAndDownAcrossEveryDirectedEdge() {
        for (PlanetFace face : PlanetFace.values()) {
            for (Direction edge : new Direction[]{
                    Direction.NORTH,
                    Direction.SOUTH,
                    Direction.WEST,
                    Direction.EAST
            }) {
                assertEquals(
                        Direction.UP,
                        PlanetVanillaDirection.transformAcrossEdge(face, edge, Direction.UP)
                );
                assertEquals(
                        Direction.DOWN,
                        PlanetVanillaDirection.transformAcrossEdge(face, edge, Direction.DOWN)
                );
            }
        }
    }
}
