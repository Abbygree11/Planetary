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
    void recognizedV1ThroughV7MigrationAndCurrentV8Idempotency() {
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.GOLD_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.DIAMOND_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.EMERALD_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.NETHERITE_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.AMETHYST_BLOCK.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.OBSIDIAN.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.REBUILD,
                PlanetTestFixtures.planBuild(Blocks.CRYING_OBSIDIAN.defaultBlockState(), false, false));
        assertEquals(PlanetTestFixtures.BuildAction.ALREADY_CURRENT,
                PlanetTestFixtures.planBuild(Blocks.PURPUR_BLOCK.defaultBlockState(), false, false));
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
    void v8FortyTwoStationsPreserveAllOldFamiliesAndAddRotation16AuthorLanes() {
        assertEquals(42, PlanetTestFixtures.STATIONS.size());
        Set<String> names = new HashSet<>();
        Set<BlockPos> lanes = new HashSet<>();
        Set<BlockPos> reservedSlots = new HashSet<>();
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
                // Every 5x5 station footprint must remain independent: support
                // blocks may not overlap another station's player-placement lane.
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        assertTrue(reservedSlots.add(PlanetTestFixtures.physical(
                                face, station.east() + dx,
                                PlanetTestFixtures.FLOOR_RADIUS + 1,
                                station.south() + dz)), "Overlapping station footprint");
                    }
                }
                assertNotEquals(PlanetTestFixtures.marker(face), reference);
                assertNotEquals(PlanetTestFixtures.marker(face), natural);
            }
        }
        assertEquals(252, lanes.size());
        // V7 fits entirely within the same 41x33 pad: all interstitial
        // 5x5 zones are disjoint from old references, player lanes and supports.
        assertEquals(42 * 25 * 6, reservedSlots.size());
        assertTrue(names.contains("oak_log_y"));
        assertTrue(names.contains("oak_log_x"));
        assertTrue(names.contains("stripped_oak_log_z"));
        assertTrue(names.contains("chain_waterlogged"));
        assertTrue(names.contains("basalt_x"));
        assertTrue(names.contains("hay_block_y"));
        assertTrue(names.contains("infested_deepslate_z"));
        assertTrue(names.contains("crimson_stem_x"));
        assertTrue(names.containsAll(Set.of(
                "oak_gate_plain", "spruce_gate_one_wall", "cherry_gate_two_walls",
                "bamboo_gate_open", "crimson_gate_wall", "warped_gate_wall",
                "mangrove_gate_plain", "dark_oak_gate_open",
                "furnace_north", "smoker_east", "blast_furnace_south",
                "furnace_west", "smoker_lit", "blast_furnace_lit",
                "furnace_lit", "smoker_north"
        )));
        assertTrue(names.containsAll(Set.of(
                "crafter_front_north", "crafter_front_south",
                "crafter_front_east", "crafter_front_west",
                "crafter_front_up", "crafter_front_down",
                "jigsaw_front_north", "jigsaw_front_south",
                "jigsaw_front_east", "jigsaw_front_west",
                "jigsaw_front_up", "jigsaw_front_down"
        )));
        assertEquals(6, names.stream().filter(n -> n.startsWith("crafter_front_")).count());
        assertEquals(6, names.stream().filter(n -> n.startsWith("jigsaw_front_")).count());
        assertTrue(names.containsAll(Set.of(
                "oak_sign_yaw_0", "spruce_sign_yaw_90",
                "white_banner_yaw_180", "player_head_yaw_270",
                "oak_ceiling_sign_attached", "cherry_ceiling_sign_rigid"
        )));
        assertEquals(42, names.size(), "All station names must be unique");
        assertFalse(names.contains("end_rod"), "Only current placement families");
        assertTrue(PlanetTestFixtures.gridLegend().contains("ACTIVE V8"));
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
