package dev.planetary.debug;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.worldgen.PlanetChunkGenerator;
import dev.planetary.worldgen.PlanetWorldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Replaceable, versioned *server-side* development acceptance scene.
 *
 * <p>Each of the six faces has an identical physical layout. CYAN floor cells
 * carry directly installed canonical-local reference states; LIME floor cells
 * next to them are empty for testing real player BlockItem/useOn placement.
 * A reference fixture NEVER proves that natural placement works.</p>
 *
 * <p>Only a marker-owned dev scene is automatically migrated between
 * versions. Unmarked occupied terrain and all cells outside the bounded
 * lab are preserved. Historical visual fixtures are not retained.</p>
 */
public final class PlanetTestFixtures {
    public static final String AUTO_PROPERTY = "planetary.debug.fixtures.auto";

    public static final int FLOOR_RADIUS = PlanetWorldSettings.RADIUS + 3;
    public static final int FLOOR_MIN_X = -20;
    public static final int FLOOR_MAX_X = 20;
    public static final int FLOOR_MIN_Z = -16;
    public static final int FLOOR_MAX_Z = 16;
    private static final int MAX_UP_OFFSET = 6;

    /** V7: 2.4A-9 plus retained 2.4A-6/7/8 catch-up stations. */
    public static final BlockState CURRENT_MARKER = Blocks.CRYING_OBSIDIAN.defaultBlockState();
    public static final BlockState V6_MARKER = Blocks.OBSIDIAN.defaultBlockState();
    public static final BlockState V5_MARKER = Blocks.AMETHYST_BLOCK.defaultBlockState();
    public static final BlockState LEGACY_MARKER = Blocks.NETHERITE_BLOCK.defaultBlockState();
    public static final BlockState OLDEST_MARKER = Blocks.EMERALD_BLOCK.defaultBlockState();
    public static final BlockState PREVIOUS_MARKER = Blocks.DIAMOND_BLOCK.defaultBlockState();
    public static final BlockState FIRST_MARKER = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final int LEGACY_WING_MIN_X = 21;
    private static final int LEGACY_WING_MAX_X = 40;
    private static final int LEGACY_WING_MIN_Z = -12;
    private static final int LEGACY_WING_MAX_Z = 12;


    // V7 inserts three halfway columns; the original V6 column IDs remain stable.
    private static final int[] EAST_GRID = {-15, -5, 5, 15, -10, 0, 10};
    private static final int[] SOUTH_GRID = {-12, -7, -2, 3, 8, 13};

    private static final int UPDATE_FLAGS =
            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public record Station(String name, int column, int row) {
        public Station {
            Objects.requireNonNull(name, "name");
            if (column < 0 || column >= EAST_GRID.length
                    || row < 0 || row >= SOUTH_GRID.length) {
                throw new IllegalArgumentException("station outside grid");
            }
        }

        public int east() {
            return EAST_GRID[column];
        }

        public int south() {
            return SOUTH_GRID[row];
        }
    }

    /**
     * ACTIVE acceptance cases only. Retired fixture scenarios are covered
     * by JUnit/ASM; the dev pad is intentionally not a historical museum.
     */
    public static final List<Station> STATIONS = List.of(
            // 2.4A-6 — gate owner: placement, wall graph and opening interaction.
            new Station("oak_gate_plain", 0, 0),
            new Station("spruce_gate_one_wall", 1, 0),
            new Station("cherry_gate_two_walls", 2, 0),
            new Station("bamboo_gate_open", 3, 0),
            new Station("crimson_gate_wall", 0, 1),
            new Station("warped_gate_wall", 1, 1),
            new Station("mangrove_gate_plain", 2, 1),
            new Station("dark_oak_gate_open", 3, 1),
            // 2.4A-7 — inherited furnace/smoker/blast furnace author.
            new Station("furnace_north", 0, 2),
            new Station("smoker_east", 1, 2),
            new Station("blast_furnace_south", 2, 2),
            new Station("furnace_west", 3, 2),
            new Station("smoker_lit", 0, 3),
            new Station("blast_furnace_lit", 1, 3),
            new Station("furnace_lit", 2, 3),
            new Station("smoker_north", 3, 3),
            // 2.4A-8 — inherited/independent AXIS authors.
            new Station("oak_log_y", 0, 4),
            new Station("oak_log_x", 1, 4),
            new Station("stripped_oak_log_z", 2, 4),
            new Station("chain_waterlogged", 3, 4),
            new Station("basalt_x", 0, 5),
            new Station("hay_block_y", 1, 5),
            new Station("infested_deepslate_z", 2, 5),
            new Station("crimson_stem_x", 3, 5),
            // 2.4A-9: six distinct Crafter fronts and six Jigsaw clicked-face fronts.
            // Interstitial columns use independent 5x5 test footprints.
            new Station("crafter_front_north", 4, 0),
            new Station("crafter_front_south", 5, 0),
            new Station("crafter_front_east", 6, 0),
            new Station("crafter_front_west", 4, 1),
            new Station("crafter_front_up", 5, 1),
            new Station("crafter_front_down", 6, 1),
            new Station("jigsaw_front_north", 4, 2),
            new Station("jigsaw_front_south", 5, 2),
            new Station("jigsaw_front_east", 6, 2),
            new Station("jigsaw_front_west", 4, 3),
            new Station("jigsaw_front_up", 5, 3),
            new Station("jigsaw_front_down", 6, 3)
    );

    public record BuildStats(int built, int alreadyPresent, int occupied) {
        public BuildStats plus(BuildStats other) {
            return new BuildStats(
                    built + other.built,
                    alreadyPresent + other.alreadyPresent,
                    occupied + other.occupied
            );
        }
    }

    private PlanetTestFixtures() {
    }

    public static boolean isPlanet(ServerLevel level) {
        return level.getChunkSource().getGenerator()
                instanceof PlanetChunkGenerator;
    }

    public static BlockPos physical(
            PlanetFace face,
            int east,
            int radial,
            int south
    ) {
        PlanetGravityFrame frame = new PlanetGravityFrame(face);
        PlanetVector x = frame.worldEast();
        PlanetVector y = frame.worldUp();
        PlanetVector z = frame.worldSouth();
        return new BlockPos(
                PlanetWorldSettings.CORE_X
                        + east * x.x() + radial * y.x() + south * z.x(),
                PlanetWorldSettings.CORE_Y
                        + east * x.y() + radial * y.y() + south * z.y(),
                PlanetWorldSettings.CORE_Z
                        + east * x.z() + radial * y.z() + south * z.z()
        );
    }

    public static BlockPos arrival(PlanetFace face) {
        return physical(face, 0, FLOOR_RADIUS + 3, -7);
    }

    public static BlockPos marker(PlanetFace face) {
        return physical(face, 0, FLOOR_RADIUS, 0);
    }

    public enum BuildAction { ALREADY_CURRENT, REBUILD, PRESERVE_OCCUPIED }

    /**
     * A known previous version authorizes replacement ONLY within this
     * bounded dev-lab volume; unmarked occupied land is never cleared.
     * Make this a new marker block when the active suite changes again.
     */
    public static BuildAction planBuild(BlockState existingMarker,
                                        boolean reservedAirspaceEmpty,
                                        boolean explicitRebuild) {
        Objects.requireNonNull(existingMarker, "existingMarker");
        if (explicitRebuild) return BuildAction.REBUILD;
        if (existingMarker.equals(CURRENT_MARKER)) return BuildAction.ALREADY_CURRENT;
        if (existingMarker.equals(V6_MARKER)
                || existingMarker.equals(V5_MARKER)
                || existingMarker.equals(LEGACY_MARKER)
                || existingMarker.equals(OLDEST_MARKER)
                || existingMarker.equals(PREVIOUS_MARKER)
                || existingMarker.equals(FIRST_MARKER)) return BuildAction.REBUILD;
        return reservedAirspaceEmpty ? BuildAction.REBUILD : BuildAction.PRESERVE_OCCUPIED;
    }

    public static BuildStats buildAll(ServerLevel level, boolean force) {
        requirePlanet(level);
        BuildStats total = new BuildStats(0, 0, 0);
        for (PlanetFace face : PlanetFace.values()) {
            total = total.plus(buildFace(level, face, force));
        }
        return total;
    }

    public static BuildStats buildFace(ServerLevel level, PlanetFace face, boolean force) {
        requirePlanet(level);
        Objects.requireNonNull(face, "face");

        BlockState previousMarker = level.getBlockState(marker(face));
        // Avoid scanning 9,471 cells if this is already an owned/current pad
        // or an explicitly requested full reset.
        boolean empty = force || previousMarker.equals(CURRENT_MARKER)
                || previousMarker.equals(V6_MARKER)
                || previousMarker.equals(V5_MARKER)
                || previousMarker.equals(LEGACY_MARKER)
                || previousMarker.equals(OLDEST_MARKER)
                || previousMarker.equals(PREVIOUS_MARKER)
                || previousMarker.equals(FIRST_MARKER)
                || isReservedAirspaceEmpty(level, face);
        BuildAction action = planBuild(previousMarker, empty, force);
        if (action == BuildAction.ALREADY_CURRENT) return new BuildStats(0, 1, 0);
        if (action == BuildAction.PRESERVE_OCCUPIED) return new BuildStats(0, 0, 1);

        // Replace only a known owned pad or the user's explicit rebuild area;
        // NEVER reset unmarked occupied terrain during dev login.
        if (force || previousMarker.equals(V6_MARKER)
                || previousMarker.equals(V5_MARKER)
                || previousMarker.equals(LEGACY_MARKER)
                || previousMarker.equals(OLDEST_MARKER)
                || previousMarker.equals(PREVIOUS_MARKER)
                || previousMarker.equals(FIRST_MARKER)) {
            clearVolume(level, face, FLOOR_MIN_X, FLOOR_MAX_X, FLOOR_MIN_Z, FLOOR_MAX_Z);
            // V1 had a second placement wing with its own lapis marker.
            // Retire it during migration rather than accumulating arenas.
            BlockPos oldWingMarker = physical(face, 23, FLOOR_RADIUS, -11);
            if (level.getBlockState(oldWingMarker).is(Blocks.LAPIS_BLOCK)) {
                clearVolume(level, face, LEGACY_WING_MIN_X, LEGACY_WING_MAX_X,
                        LEGACY_WING_MIN_Z, LEGACY_WING_MAX_Z);
            }
        }

        for (int east = FLOOR_MIN_X; east <= FLOOR_MAX_X; east++) {
            for (int south = FLOOR_MIN_Z; south <= FLOOR_MAX_Z; south++) {
                put(level, physical(face, east, FLOOR_RADIUS, south),
                        Blocks.POLISHED_ANDESITE.defaultBlockState());
            }
        }

        for (Station station : STATIONS) {
            int x = station.east();
            int z = station.south();
            put(level, physical(face, x - 1, FLOOR_RADIUS, z),
                    Blocks.CYAN_CONCRETE.defaultBlockState());
            put(level, physical(face, x + 1, FLOOR_RADIUS, z),
                    Blocks.LIME_CONCRETE.defaultBlockState());
            buildReference(level, face, station);
        }

        // Marker last so a failed build is never misreported as current.
        put(level, marker(face), CURRENT_MARKER);
        return new BuildStats(1, 0, 0);
    }

    private static void clearVolume(ServerLevel level, PlanetFace face,
                                    int minEast, int maxEast, int minSouth, int maxSouth) {
        for (int east = minEast; east <= maxEast; east++) {
            for (int south = minSouth; south <= maxSouth; south++) {
                for (int radial = FLOOR_RADIUS; radial <= FLOOR_RADIUS + MAX_UP_OFFSET; radial++) {
                    put(level, physical(face, east, radial, south),
                            Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    public static boolean isReservedAirspaceEmpty(
            ServerLevel level,
            PlanetFace face
    ) {
        for (int east = FLOOR_MIN_X; east <= FLOOR_MAX_X; east++) {
            for (int south = FLOOR_MIN_Z; south <= FLOOR_MAX_Z; south++) {
                for (int radial = FLOOR_RADIUS;
                        radial <= FLOOR_RADIUS + MAX_UP_OFFSET;
                        radial++) {
                    if (!level.getBlockState(
                            physical(face, east, radial, south)
                    ).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void buildReference(
            ServerLevel level, PlanetFace face, Station station
    ) {
        int x = station.east() - 1;
        int south = station.south();
        int up = FLOOR_RADIUS + 1;
        BlockPos reference = physical(face, x, up, south);
        BlockState state = switch (station.name()) {
            // 2.4A-6: original V3 gate state cases.
            case "oak_gate_plain" -> Blocks.OAK_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.NORTH);
            case "spruce_gate_one_wall" -> Blocks.SPRUCE_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.NORTH)
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.IN_WALL, true);
            case "cherry_gate_two_walls" -> Blocks.CHERRY_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.SOUTH)
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.IN_WALL, true);
            case "bamboo_gate_open" -> Blocks.BAMBOO_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.EAST)
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true);
            case "crimson_gate_wall" -> Blocks.CRIMSON_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.NORTH)
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.IN_WALL, true);
            case "warped_gate_wall" -> Blocks.WARPED_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.NORTH)
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.IN_WALL, true);
            case "mangrove_gate_plain" -> Blocks.MANGROVE_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.WEST);
            case "dark_oak_gate_open" -> Blocks.DARK_OAK_FENCE_GATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.WEST)
                    .setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true);
            // 2.4A-7: original V4 furnace state cases.
            case "furnace_north" -> Blocks.FURNACE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.NORTH);
            case "smoker_east" -> Blocks.SMOKER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.EAST);
            case "blast_furnace_south" -> Blocks.BLAST_FURNACE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.SOUTH);
            case "furnace_west" -> Blocks.FURNACE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.WEST);
            case "smoker_lit" -> Blocks.SMOKER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, true);
            case "blast_furnace_lit" -> Blocks.BLAST_FURNACE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, true);
            case "furnace_lit" -> Blocks.FURNACE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, true);
            case "smoker_north" -> Blocks.SMOKER.defaultBlockState();
            // 2.4A-8: original V5 AXIS cases.
            case "oak_log_y" -> Blocks.OAK_LOG.defaultBlockState();
            case "oak_log_x" -> Blocks.OAK_LOG.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X);
            case "stripped_oak_log_z" -> Blocks.STRIPPED_OAK_LOG.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z);
            case "chain_waterlogged" -> Blocks.CHAIN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChainBlock.WATERLOGGED, true);
            case "basalt_x" -> Blocks.BASALT.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X);
            case "hay_block_y" -> Blocks.HAY_BLOCK.defaultBlockState();
            case "infested_deepslate_z" -> Blocks.INFESTED_DEEPSLATE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z);
            case "crimson_stem_x" -> Blocks.CRIMSON_STEM.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X);
            // 2.4A-9: only reference states. True BlockItem placement is LIME.
            case "crafter_front_north" -> Blocks.CRAFTER.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.NORTH_UP);
            case "crafter_front_south" -> Blocks.CRAFTER.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.SOUTH_UP);
            case "crafter_front_east" -> Blocks.CRAFTER.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.EAST_UP);
            case "crafter_front_west" -> Blocks.CRAFTER.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.WEST_UP);
            case "crafter_front_up" -> Blocks.CRAFTER.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH);
            case "crafter_front_down" -> Blocks.CRAFTER.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.DOWN_EAST);
            case "jigsaw_front_north" -> Blocks.JIGSAW.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.NORTH_UP);
            case "jigsaw_front_south" -> Blocks.JIGSAW.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.SOUTH_UP);
            case "jigsaw_front_east" -> Blocks.JIGSAW.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.EAST_UP);
            case "jigsaw_front_west" -> Blocks.JIGSAW.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.WEST_UP);
            case "jigsaw_front_up" -> Blocks.JIGSAW.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_SOUTH);
            case "jigsaw_front_down" -> Blocks.JIGSAW.defaultBlockState()
                    .setValue(BlockStateProperties.ORIENTATION, FrontAndTop.DOWN_WEST);
            default -> throw new IllegalStateException("Unknown V7 station " + station.name());
        };

        // Gate wall graph: center wall touches both CYAN and LIME lanes.
        // The cherry two-wall case tests one wall on each outer side too.
        if (station.name().contains("gate") && station.name().contains("wall")) {
            put(level, physical(face, station.east(), up, south),
                    Blocks.COBBLESTONE_WALL.defaultBlockState());
        }
        if (station.name().equals("cherry_gate_two_walls")) {
            put(level, physical(face, station.east() - 2, up, south),
                    Blocks.COBBLESTONE_WALL.defaultBlockState());
            put(level, physical(face, station.east() + 2, up, south),
                    Blocks.COBBLESTONE_WALL.defaultBlockState());
        }

        // AXIS horizontal side-click supports, never inside the LIME cell.
        if (station.name().endsWith("_x")) {
            put(level, physical(face, station.east() + 2, up, south),
                    Blocks.POLISHED_ANDESITE.defaultBlockState());
        }
        if (station.name().endsWith("_z")) {
            put(level, physical(face, station.east() + 1, up, south + 1),
                    Blocks.POLISHED_ANDESITE.defaultBlockState());
        }
        // Jigsaw authors choose FRONT from the *clicked* physical face.
        // Supply a real support face next to LIME without preplacing the item.
        // For negative local UP normal, use an overhead underside support.
        // Crafter DOWN front requires looking UP, so it gets the same support.
        if (station.name().startsWith("jigsaw_front_")) {
            switch (station.name()) {
                case "jigsaw_front_north" ->
                        put(level, physical(face, station.east() + 1, up, south + 1),
                                Blocks.POLISHED_ANDESITE.defaultBlockState());
                case "jigsaw_front_south" ->
                        put(level, physical(face, station.east() + 1, up, south - 1),
                                Blocks.POLISHED_ANDESITE.defaultBlockState());
                case "jigsaw_front_east" ->
                        put(level, physical(face, station.east(), up, south),
                                Blocks.POLISHED_ANDESITE.defaultBlockState());
                case "jigsaw_front_west" ->
                        put(level, physical(face, station.east() + 2, up, south),
                                Blocks.POLISHED_ANDESITE.defaultBlockState());
                case "jigsaw_front_down" ->
                        put(level, physical(face, station.east() + 1, up + 1, south),
                                Blocks.POLISHED_ANDESITE.defaultBlockState());
                default -> { /* UP uses the existing LIME floor. */ }
            }
        }
        if (station.name().equals("crafter_front_down")) {
            put(level, physical(face, station.east() + 1, up + 1, south),
                    Blocks.POLISHED_ANDESITE.defaultBlockState());
        }
        put(level, reference, state);
    }

    private static void put(
            ServerLevel level,
            BlockPos pos,
            BlockState state
    ) {
        if (!level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, state, UPDATE_FLAGS);
        }
    }

    private static void requirePlanet(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        if (!isPlanet(level)) {
            throw new IllegalArgumentException(
                    "Fixture generation is only allowed in the Planet world"
            );
        }
    }

    public static String gridLegend() {
        StringBuilder result = new StringBuilder(
                "ACTIVE V7: 24 preserved gates/furnaces/AXIS plus 6 Crafter and 6 Jigsaw FrontAndTop in intermediate EAST columns; CYAN = reference, LIME = actual item: ");
        for (Station station : STATIONS) {
            result.append(station.name().replace('_', ' '))
                    .append("[row ").append(station.row() + 1)
                    .append(", east ").append(station.east()).append("]; ");
        }
        return result.toString();
    }

    public static String faceName(PlanetFace face) {
        return face.name().toLowerCase(Locale.ROOT);
    }
}
