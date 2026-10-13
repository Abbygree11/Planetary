package dev.planetary.debug;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.worldgen.PlanetWorldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the CURRENT versioned dev scene geometry and migration decision.
 * Full applied game-client placement remains a separate manual gate.
 */
final class PlanetTestFixturesTest {
    @Test
    void recognizedV1V2MigrationAndCurrentV3Idempotency() {
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.GOLD_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.DIAMOND_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.ALREADY_CURRENT,
                PlanetTestFixtures.planBuild(Blocks.EMERALD_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.AIR.defaultBlockState(), true, false));
        assertEquals(PlanetTestFixtures.BuildAction.PRESERVE_OCCUPIED,
                PlanetTestFixtures.planBuild(Blocks.STONE.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.STONE.defaultBlockState(), false, true));
    }

    @Test
    void sixFacePadsHaveDistinctIdenticalBoundedFootprints() {
        Set<BlockPos> positions = new HashSet<>();
        int width = PlanetTestFixtures.FLOOR_MAX_X - PlanetTestFixtures.FLOOR_MIN_X + 1;
        int depth = PlanetTestFixtures.FLOOR_MAX_Z - PlanetTestFixtures.FLOOR_MIN_Z + 1;
        assertEquals(41, width);
        assertEquals(33, depth);
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            PlanetVector up = frame.worldUp();
            BlockPos center = PlanetTestFixtures.marker(face);
            assertEquals(new BlockPos(
                    PlanetWorldSettings.CORE_X + up.x() * PlanetTestFixtures.FLOOR_RADIUS,
                    PlanetWorldSettings.CORE_Y + up.y() * PlanetTestFixtures.FLOOR_RADIUS,
                    PlanetWorldSettings.CORE_Z + up.z() * PlanetTestFixtures.FLOOR_RADIUS), center);
            for (int x = PlanetTestFixtures.FLOOR_MIN_X;
                    x <= PlanetTestFixtures.FLOOR_MAX_X; x++) {
                for (int z = PlanetTestFixtures.FLOOR_MIN_Z;
                        z <= PlanetTestFixtures.FLOOR_MAX_Z; z++) {
                    assertTrue(positions.add(PlanetTestFixtures.physical(
                            face, x, PlanetTestFixtures.FLOOR_RADIUS, z)));
                }
            }
        }
        assertEquals(width * depth * 6, positions.size());
    }

    @Test
    void onlyEightCurrentGateFamilyStationsHaveDistinctReferenceAndNaturalLanes() {
        assertEquals(8, PlanetTestFixtures.STATIONS.size());
        Set<String> names = new HashSet<>();
        Set<BlockPos> lanes = new HashSet<>();
        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetTestFixtures.Station station : PlanetTestFixtures.STATIONS) {
                names.add(station.name());
                assertTrue(station.east() - 3 >= PlanetTestFixtures.FLOOR_MIN_X);
                assertTrue(station.east() + 3 <= PlanetTestFixtures.FLOOR_MAX_X);
                assertTrue(station.south() - 3 >= PlanetTestFixtures.FLOOR_MIN_Z);
                assertTrue(station.south() + 3 <= PlanetTestFixtures.FLOOR_MAX_Z);
                BlockPos reference = PlanetTestFixtures.physical(face,
                        station.east() - 1, PlanetTestFixtures.FLOOR_RADIUS + 1,
                        station.south());
                BlockPos natural = PlanetTestFixtures.physical(face,
                        station.east() + 1, PlanetTestFixtures.FLOOR_RADIUS + 1,
                        station.south());
                assertNotEquals(reference, natural);
                assertTrue(lanes.add(natural));
                assertNotEquals(PlanetTestFixtures.marker(face), reference);
                assertNotEquals(PlanetTestFixtures.marker(face), natural);
            }
        }
        assertEquals(48, lanes.size());
        assertTrue(names.contains("oak_gate_plain"));
        assertTrue(names.contains("spruce_gate_one_wall"));
        assertTrue(names.contains("cherry_gate_two_walls"));
        assertTrue(names.contains("bamboo_gate_open"));
        assertTrue(names.contains("crimson_gate_wall"));
        assertTrue(names.contains("warped_gate_wall"));
        assertTrue(names.contains("mangrove_gate_plain"));
        assertTrue(names.contains("dark_oak_gate_open"));
        assertFalse(names.contains("end_rod"), "Retired museum scenarios belong to regression tests");
        assertTrue(PlanetTestFixtures.gridLegend().contains("ACTIVE V3"));
    }

    @Test
    void sixArrivalsRemainInsideBuildHeightAndAboveLocalPad() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos arrival = PlanetTestFixtures.arrival(face);
            assertTrue(arrival.getY() >= PlanetWorldSettings.MIN_Y);
            assertTrue(arrival.getY()
                    < PlanetWorldSettings.MIN_Y + PlanetWorldSettings.HEIGHT);
            PlanetVector up = new PlanetGravityFrame(face).worldUp();
            BlockPos floor = PlanetTestFixtures.physical(
                    face, 0, PlanetTestFixtures.FLOOR_RADIUS, -7);
            assertEquals(floor.offset(up.x() * 3, up.y() * 3, up.z() * 3), arrival);
        }
    }

    @Test
    void noSeparateWingIsPartOfCurrentLabContract() {
        for (PlanetTestFixtures.Station station : PlanetTestFixtures.STATIONS) {
            assertTrue(station.east() <= PlanetTestFixtures.FLOOR_MAX_X);
            assertTrue(station.east() >= PlanetTestFixtures.FLOOR_MIN_X);
        }
    }
}
