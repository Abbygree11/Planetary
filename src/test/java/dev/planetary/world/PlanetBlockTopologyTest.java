package dev.planetary.world;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetBlockTopologyTest {
    private static final int FACE_SIZE = 4096;

    @Test
    void sectionAddressAndLocalCoordinatesHandleNegativeRadialY() {
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                31,
                -17,
                32
        );

        assertEquals(1, pos.sectionAddress().x());
        assertEquals(-2, pos.sectionAddress().y());
        assertEquals(2, pos.sectionAddress().z());
        assertEquals(15, pos.localX());
        assertEquals(15, pos.localY());
        assertEquals(0, pos.localZ());
    }

    @Test
    void steppingAcrossOrdinarySectionBoundaryDoesNotChangeFace() {
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                15,
                10,
                20
        );

        assertEquals(
                new PlanetBlockPos(PlanetFace.POS_Y, 16, 10, 20),
                PlanetBlockTopology.step(pos, PlanetDirection.EAST, FACE_SIZE)
        );
    }

    @Test
    void steppingAcrossPlanetEdgeChangesGravityFace() {
        PlanetBlockPos pos = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                -30,
                FACE_SIZE / 2
        );

        PlanetBlockPos target = PlanetBlockTopology.step(
                pos,
                PlanetDirection.EAST,
                FACE_SIZE
        );

        assertEquals(PlanetFace.POS_X, target.face());
        assertEquals(-30, target.y());
    }

    @Test
    void edgeRoundTripReturnsToTheSameBlock() {
        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                7,
                1234
        );

        PlanetBlockPos target = PlanetBlockTopology.step(
                source,
                PlanetDirection.EAST,
                FACE_SIZE
        );

        PlanetBlockPos roundTrip = PlanetBlockTopology.step(
                target,
                PlanetDirection.WEST,
                FACE_SIZE
        );

        assertEquals(source, roundTrip);
    }
}
