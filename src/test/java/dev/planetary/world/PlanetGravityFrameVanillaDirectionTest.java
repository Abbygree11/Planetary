package dev.planetary.world;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetGravityFrameVanillaDirectionTest {

    @Test
    void vanillaDirectionsRoundTripThroughEveryGravityFrame() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            for (Direction local : Direction.values()) {
                Direction world =
                        PlanetVanillaDirection.localToWorld(
                                frame,
                                local
                        );
                Direction roundTrip =
                        PlanetVanillaDirection.worldToLocal(
                                frame,
                                world
                        );

                assertEquals(
                        local,
                        roundTrip,
                        face + " / " + local
                );
            }
        }
    }

    @Test
    void sideFaceTreatsItsOutwardAxisAsVanillaUp() {
        PlanetGravityFrame positiveX =
                new PlanetGravityFrame(PlanetFace.POS_X);

        assertEquals(
                Direction.EAST,
                PlanetVanillaDirection.localToWorld(
                        positiveX,
                        Direction.UP
                )
        );
        assertEquals(
                Direction.DOWN,
                PlanetVanillaDirection.worldToLocal(
                        positiveX,
                        Direction.WEST
                )
        );
        assertEquals(
                PlanetDirection.DOWN,
                positiveX.localDirectionOf(
                        PlanetFace.POS_X.worldVector(
                                PlanetDirection.DOWN
                        )
                )
        );
    }
}
