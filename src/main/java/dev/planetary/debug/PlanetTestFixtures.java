package dev.planetary.debug;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.worldgen.PlanetChunkGenerator;
import dev.planetary.worldgen.PlanetWorldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
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

    /** V2: change this marker when acceptance scenarios change. */
    public static final BlockState CURRENT_MARKER = Blocks.DIAMOND_BLOCK.defaultBlockState();
    public static final BlockState LEGACY_MARKER = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final int LEGACY_WING_MIN_X = 21;
    private static final int LEGACY_WING_MAX_X = 40;
    private static final int LEGACY_WING_MIN_Z = -12;
    private static final int LEGACY_WING_MAX_Z = 12;


    private static final int[] EAST_GRID = {-16, -8, 0, 8, 16};
    private static final int[] SOUTH_GRID = {-12, -4, 4, 12};

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
            new Station("oak_trapdoor", 1, 0),
            new Station("oak_stairs", 3, 0),
            new Station("iron_trapdoor", 1, 1),
            new Station("quartz_stairs_outer_left", 3, 1),
            new Station("copper_trapdoor", 1, 2),
            new Station("copper_stairs_inner_right", 3, 2),
            new Station("waxed_copper_trapdoor", 1, 3),
            new Station("waxed_copper_stairs_top", 3, 3)
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
        if (existingMarker.equals(LEGACY_MARKER)) return BuildAction.REBUILD;
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
                || previousMarker.equals(LEGACY_MARKER)
                || isReservedAirspaceEmpty(level, face);
        BuildAction action = planBuild(previousMarker, empty, force);
        if (action == BuildAction.ALREADY_CURRENT) return new BuildStats(0, 1, 0);
        if (action == BuildAction.PRESERVE_OCCUPIED) return new BuildStats(0, 0, 1);

        // Replace only a known owned pad or the user's explicit rebuild area;
        // NEVER reset unmarked occupied terrain during dev login.
        if (force || previousMarker.equals(LEGACY_MARKER)) {
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
        switch (station.name()) {
            case "oak_trapdoor" -> put(level, reference,
                    Blocks.OAK_TRAPDOOR.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF,
                                    net.minecraft.world.level.block.state.properties.Half.BOTTOM));
            case "iron_trapdoor" -> put(level, reference,
                    Blocks.IRON_TRAPDOOR.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.EAST)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF,
                                    net.minecraft.world.level.block.state.properties.Half.TOP));
            case "copper_trapdoor" -> put(level, reference,
                    Blocks.COPPER_TRAPDOOR.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.SOUTH));
            case "waxed_copper_trapdoor" -> put(level, reference,
                    Blocks.WAXED_COPPER_TRAPDOOR.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.WEST));
            case "oak_stairs" -> put(level, reference,
                    Blocks.OAK_STAIRS.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH));
            case "quartz_stairs_outer_left" -> {
                put(level, reference,
                        Blocks.QUARTZ_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH)
                                .setValue(net.minecraft.world.level.block.StairBlock.SHAPE,
                                        net.minecraft.world.level.block.state.properties.StairsShape.OUTER_LEFT));
                put(level, physical(face, x, up, south - 1),
                        Blocks.QUARTZ_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST));
                put(level, physical(face, station.east() + 1, up, south - 1),
                        Blocks.QUARTZ_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST));
            }
            case "copper_stairs_inner_right" -> {
                put(level, reference,
                        Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH)
                                .setValue(net.minecraft.world.level.block.StairBlock.SHAPE,
                                        net.minecraft.world.level.block.state.properties.StairsShape.INNER_RIGHT));
                put(level, physical(face, x, up, south + 1),
                        Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST));
                put(level, physical(face, station.east() + 1, up, south + 1),
                        Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST));
            }
            case "waxed_copper_stairs_top" -> put(level, reference,
                    Blocks.WAXED_CUT_COPPER_STAIRS.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH)
                            .setValue(net.minecraft.world.level.block.StairBlock.HALF,
                                    net.minecraft.world.level.block.state.properties.Half.TOP));
            default -> throw new IllegalStateException("Unrecognized wing station " + station.name());
        }
    }

    /**
     * Keep furnace emitters active after the first server tick. A naked
     * LIT=true block with no inputs would promptly extinguish and produce
     * a misleading "particle broken" test result.
     */
    private static void primeFurnace(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.world.item.Item input
    ) {
        if (level.getBlockEntity(pos)
                instanceof AbstractFurnaceBlockEntity furnace) {
            furnace.setItem(0, new ItemStack(input, 64));
            furnace.setItem(1, new ItemStack(Items.COAL, 64));
            furnace.setChanged();
        }
    }

    private static void buildBookshelves(
            ServerLevel level,
            PlanetFace face,
            int x,
            int z,
            int radial
    ) {
        put(level, physical(face, x, radial, z),
                Blocks.ENCHANTING_TABLE.defaultBlockState());
        for (int dx : new int[]{-2, 2}) {
            for (int dz : new int[]{-2, 2}) {
                put(level, physical(face, x + dx, radial, z + dz),
                        Blocks.BOOKSHELF.defaultBlockState());
            }
        }
    }

    private static void buildPortalFrame(
            ServerLevel level,
            PlanetFace face,
            int x,
            int z
    ) {
        // Build the ring in the SOUTH/UP plane, not EAST/UP:
        // x + 2 is reserved as a free *natural placement* lane.
        for (int dz = -2; dz <= 2; dz++) {
            for (int up = 1; up <= 5; up++) {
                if (Math.abs(dz) == 2 || up == 1 || up == 5) {
                    put(level, physical(face, x,
                            FLOOR_RADIUS + up, z + dz),
                            Blocks.OBSIDIAN.defaultBlockState());
                }
            }
        }
        // Interior is deliberately NOT ignited. This station tests
        // FlintAndSteelItem/useOn and Phase-9 portal creation itself.
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
                "ACTIVE V2 placement tests (CYAN = reference, LIME = player placement): ");
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
