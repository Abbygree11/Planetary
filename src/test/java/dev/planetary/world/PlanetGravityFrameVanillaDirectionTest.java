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

    @Test
    void vanillaAxesRoundTripThroughEveryGravityFrame() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            for (Direction.Axis local : Direction.Axis.values()) {
                Direction.Axis world =
                        PlanetVanillaDirection.localAxisToWorld(
                                frame,
                                local
                        );
                Direction.Axis roundTrip =
                        PlanetVanillaDirection.worldAxisToLocal(
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
    void axisPermutationsMatchTheSixFaceFrames() {
        assertAxes(
                PlanetFace.POS_Y,
                Direction.Axis.X,
                Direction.Axis.Y,
                Direction.Axis.Z
        );
        assertAxes(
                PlanetFace.NEG_Y,
                Direction.Axis.X,
                Direction.Axis.Y,
                Direction.Axis.Z
        );
        assertAxes(
                PlanetFace.POS_X,
                Direction.Axis.Y,
                Direction.Axis.X,
                Direction.Axis.Z
        );
        assertAxes(
                PlanetFace.NEG_X,
                Direction.Axis.Y,
                Direction.Axis.X,
                Direction.Axis.Z
        );
        assertAxes(
                PlanetFace.POS_Z,
                Direction.Axis.X,
                Direction.Axis.Z,
                Direction.Axis.Y
        );
        assertAxes(
                PlanetFace.NEG_Z,
                Direction.Axis.X,
                Direction.Axis.Z,
                Direction.Axis.Y
        );
    }

    private static void assertAxes(
            PlanetFace face,
            Direction.Axis expectedWorldX,
            Direction.Axis expectedWorldY,
            Direction.Axis expectedWorldZ
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);

        assertEquals(
                expectedWorldX,
                PlanetVanillaDirection.localAxisToWorld(
                        frame,
                        Direction.Axis.X
                ),
                face + " local X"
        );
        assertEquals(
                expectedWorldY,
                PlanetVanillaDirection.localAxisToWorld(
                        frame,
                        Direction.Axis.Y
                ),
                face + " local Y"
        );
        assertEquals(
                expectedWorldZ,
                PlanetVanillaDirection.localAxisToWorld(
                        frame,
                        Direction.Axis.Z
                ),
                face + " local Z"
        );
    }
}
