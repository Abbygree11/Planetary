package dev.planetary.debug;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.worldgen.PlanetWorldSettings;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Static fixture geometry contracts independent of a running Minecraft
 * server. Auto-install/runClient behavior requires separate game acceptance.
 */
final class PlanetTestFixturesTest {
    @Test
    void identicalNonOverlappingReservedFloorFootprintOnAllSixFaces() {
        Set<BlockPos> occupied = new HashSet<>();
        int area = (PlanetTestFixtures.FLOOR_MAX_X
                - PlanetTestFixtures.FLOOR_MIN_X + 1)
                * (PlanetTestFixtures.FLOOR_MAX_Z
                - PlanetTestFixtures.FLOOR_MIN_Z + 1);

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            PlanetVector up = frame.worldUp();
            BlockPos center = PlanetTestFixtures.physical(
                    face, 0, PlanetTestFixtures.FLOOR_RADIUS, 0);

            assertEquals(
                    new BlockPos(
                            PlanetWorldSettings.CORE_X
                                    + up.x() * PlanetTestFixtures.FLOOR_RADIUS,
                            PlanetWorldSettings.CORE_Y
                                    + up.y() * PlanetTestFixtures.FLOOR_RADIUS,
                            PlanetWorldSettings.CORE_Z
                                    + up.z() * PlanetTestFixtures.FLOOR_RADIUS
                    ),
                    center
            );
            assertEquals(center, PlanetTestFixtures.marker(face));

            for (int east = PlanetTestFixtures.FLOOR_MIN_X;
                    east <= PlanetTestFixtures.FLOOR_MAX_X; east++) {
                for (int south = PlanetTestFixtures.FLOOR_MIN_Z;
                        south <= PlanetTestFixtures.FLOOR_MAX_Z; south++) {
                    BlockPos pos = PlanetTestFixtures.physical(
                            face, east, PlanetTestFixtures.FLOOR_RADIUS, south);
                    assertTrue(occupied.add(pos),
                            "Fixture footprints overlap at " + pos);
                }
            }
        }

        assertEquals(area * PlanetFace.values().length, occupied.size());
    }

    @Test
    void stationGridHasUniquePositionsAndSeparateNaturalPlacementCells() {
        List<PlanetTestFixtures.Station> stations =
                PlanetTestFixtures.STATIONS;

        assertEquals(20, stations.size());
        Set<String> names = new HashSet<>();
        Set<BlockPos> slots = new HashSet<>();

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetTestFixtures.Station station : stations) {
                names.add(station.name());
                BlockPos preset = PlanetTestFixtures.physical(
                        face, station.east() - 1,
                        PlanetTestFixtures.FLOOR_RADIUS + 1,
                        station.south()
                );
                BlockPos natural = PlanetTestFixtures.physical(
                        face, station.east() + 1,
                        PlanetTestFixtures.FLOOR_RADIUS + 1,
                        station.south()
                );
                assertNotEquals(preset, natural);
                assertTrue(slots.add(natural),
                        "Natural placement lane overlaps: " + natural);
                assertTrue(
                        station.east() - 3 >= PlanetTestFixtures.FLOOR_MIN_X
                                && station.east() + 3
                                <= PlanetTestFixtures.FLOOR_MAX_X
                                && station.south() - 3
                                >= PlanetTestFixtures.FLOOR_MIN_Z
                                && station.south() + 3
                                <= PlanetTestFixtures.FLOOR_MAX_Z
                );
            }
        }

        assertEquals(20, names.size());
        assertEquals(20 * PlanetFace.values().length, slots.size());
        assertTrue(names.contains("end_rod"));
        assertTrue(names.contains("ender_chest"));
        assertTrue(names.contains("enchanting_table"));
        assertTrue(names.contains("candle_cake"));
        assertTrue(names.contains("spore_blossom"));
        assertTrue(names.contains("portal_ignition"));
    }

    @Test
    void allSixArrivalPositionsRemainWithinBuildHeightAndAboveFloor() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos arrival = PlanetTestFixtures.arrival(face);
            assertTrue(arrival.getY() >= PlanetWorldSettings.MIN_Y);
            assertTrue(arrival.getY()
                    < PlanetWorldSettings.MIN_Y + PlanetWorldSettings.HEIGHT);

            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            PlanetVector up = frame.worldUp();
            BlockPos floor = PlanetTestFixtures.physical(
                    face, 0, PlanetTestFixtures.FLOOR_RADIUS, -7);
            assertEquals(
                    floor.offset(up.x() * 3, up.y() * 3, up.z() * 3),
                    arrival
            );
        }
    }

    @Test
    void sameFaceLocalDirectionsRemainDifferentFromWorldCoordinates() {
        Set<BlockPos> faceCenters = new HashSet<>();

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos center = PlanetTestFixtures.marker(face);
            assertTrue(faceCenters.add(center));
            assertEquals(
                    PlanetTestFixtures.physical(face, 0,
                            PlanetTestFixtures.FLOOR_RADIUS, 0),
                    center
            );
        }
        assertFalse(faceCenters.isEmpty());
    }
}
