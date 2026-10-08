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
 * Reproducible *server-side* acceptance museum for the dedicated Planet world.
 *
 * <p>Each of the six faces has an identical physical layout. CYAN floor cells
 * carry directly installed canonical-local reference states; LIME floor cells
 * next to them are empty for testing real player BlockItem/useOn placement.
 * A reference fixture NEVER proves that natural placement works.</p>
 *
 * <p>One-time auto-creation is enabled only by the runClient development JVM
 * property. Existing scene cells are not overwritten by auto install.
 * Explicit rebuild commands are intentionally destructive inside the reserved
 * 41x33 floor footprint, not outside of it.</p>
 */
public final class PlanetTestFixtures {
    public static final String AUTO_PROPERTY = "planetary.debug.fixtures.auto";

    public static final int FLOOR_RADIUS = PlanetWorldSettings.RADIUS + 3;
    public static final int FLOOR_MIN_X = -20;
    public static final int FLOOR_MAX_X = 20;
    public static final int FLOOR_MIN_Z = -16;
    public static final int FLOOR_MAX_Z = 16;
    private static final int MAX_UP_OFFSET = 6;

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

    public static final List<Station> STATIONS = List.of(
            new Station("end_rod", 0, 0),
            new Station("ender_chest", 1, 0),
            new Station("enchanting_table", 2, 0),
            new Station("spore_blossom", 3, 0),
            new Station("water_drip", 4, 0),
            new Station("candle", 0, 1),
            new Station("candle_cake", 1, 1),
            new Station("torch", 2, 1),
            new Station("cherry_leaves", 3, 1),
            new Station("redstone_torch", 4, 1),
            new Station("campfire", 0, 2),
            new Station("soul_campfire", 1, 2),
            new Station("furnace", 2, 2),
            new Station("blast_furnace", 3, 2),
            new Station("smoker", 4, 2),
            new Station("brewing_stand", 0, 3),
            new Station("respawn_anchor", 1, 3),
            new Station("bookshelves", 2, 3),
            new Station("portal_ignition", 3, 3),
            new Station("flint_steel", 4, 3)
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

    /**
     * Auto creation is collision-safe: if even one source cell in a face's
     * reserved airspace is occupied, preserve that face untouched.
     */
    public static BuildStats buildAll(ServerLevel level, boolean force) {
        requirePlanet(level);
        BuildStats total = new BuildStats(0, 0, 0);
        for (PlanetFace face : PlanetFace.values()) {
            total = total.plus(buildFace(level, face, force));
        }
        return total;
    }

    public static BuildStats buildFace(
            ServerLevel level,
            PlanetFace face,
            boolean force
    ) {
        requirePlanet(level);
        Objects.requireNonNull(face, "face");

        if (!force && level.getBlockState(marker(face))
                .is(Blocks.GOLD_BLOCK)) {
            return new BuildStats(0, 1, 0);
        }

        if (!force && !isReservedAirspaceEmpty(level, face)) {
            return new BuildStats(0, 0, 1);
        }

        // Clear only our bounded development footprint on explicit rebuild.
        // This can remove PLAYER builds within that footprint: no implicit
        // force path is used during a login.
        if (force) {
            for (int east = FLOOR_MIN_X; east <= FLOOR_MAX_X; east++) {
                for (int south = FLOOR_MIN_Z; south <= FLOOR_MAX_Z; south++) {
                    for (int radial = FLOOR_RADIUS;
                            radial <= FLOOR_RADIUS + MAX_UP_OFFSET;
                            radial++) {
                        put(level, physical(face, east, radial, south),
                                Blocks.AIR.defaultBlockState());
                    }
                }
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

            // Adjacent green location must remain EMPTY so actual
            // BlockItem/placement and canSurvive can be tested by player.
            put(level, physical(face, x - 1, FLOOR_RADIUS, z),
                    Blocks.CYAN_CONCRETE.defaultBlockState());
            put(level, physical(face, x + 1, FLOOR_RADIUS, z),
                    Blocks.LIME_CONCRETE.defaultBlockState());

            buildReference(level, face, station);
        }

        // The persistent gold marker is written LAST. It is never used as
        // proof of fixture correctness, only as a duplicate-install guard.
        put(level, marker(face), Blocks.GOLD_BLOCK.defaultBlockState());
        return new BuildStats(1, 0, 0);
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
            ServerLevel level,
            PlanetFace face,
            Station station
    ) {
        int x = station.east() - 1;
        int z = station.south();
        int radial = FLOOR_RADIUS + 1;
        BlockPos pos = physical(face, x, radial, z);

        switch (station.name()) {
            case "end_rod" -> put(level, pos,
                    Blocks.END_ROD.defaultBlockState()
                            .setValue(EndRodBlock.FACING, Direction.UP));
            case "ender_chest" -> put(level, pos,
                    Blocks.ENDER_CHEST.defaultBlockState().setValue(
                            HorizontalDirectionalBlock.FACING,
                            Direction.NORTH));
            case "enchanting_table" -> put(level, pos,
                    Blocks.ENCHANTING_TABLE.defaultBlockState());
            case "spore_blossom" -> {
                put(level, physical(face, x, radial + 1, z),
                        Blocks.STONE.defaultBlockState());
                put(level, pos, Blocks.SPORE_BLOSSOM.defaultBlockState());
            }
            case "water_drip" -> {
                // Scaffold only: fluid topology / drip source is part
                // of Phase 5. Leave open for interactive water setup.
                put(level, pos, Blocks.POINTED_DRIPSTONE.defaultBlockState());
            }
            case "candle" -> put(level, pos,
                    Blocks.CANDLE.defaultBlockState()
                            .setValue(CandleBlock.LIT, true)
                            .setValue(CandleBlock.CANDLES, 4));
            case "candle_cake" -> put(level, pos,
                    Blocks.CANDLE_CAKE.defaultBlockState()
                            .setValue(CandleCakeBlock.LIT, true));
            case "torch" -> put(level, pos,
                    Blocks.TORCH.defaultBlockState());
            case "cherry_leaves" -> put(level, pos,
                    Blocks.CHERRY_LEAVES.defaultBlockState()
                            .setValue(CherryLeavesBlock.PERSISTENT, true));
            case "redstone_torch" -> put(level, pos,
                    Blocks.REDSTONE_TORCH.defaultBlockState());
            case "campfire" -> put(level, pos,
                    Blocks.CAMPFIRE.defaultBlockState()
                            .setValue(CampfireBlock.LIT, true));
            case "soul_campfire" -> put(level, pos,
                    Blocks.SOUL_CAMPFIRE.defaultBlockState()
                            .setValue(CampfireBlock.LIT, true));
            case "furnace" -> {
                put(level, pos, Blocks.FURNACE.defaultBlockState()
                        .setValue(AbstractFurnaceBlock.LIT, true));
                primeFurnace(level, pos, Items.RAW_IRON);
            }
            case "blast_furnace" -> {
                put(level, pos, Blocks.BLAST_FURNACE.defaultBlockState()
                        .setValue(AbstractFurnaceBlock.LIT, true));
                primeFurnace(level, pos, Items.RAW_IRON);
            }
            case "smoker" -> {
                put(level, pos, Blocks.SMOKER.defaultBlockState()
                        .setValue(AbstractFurnaceBlock.LIT, true));
                primeFurnace(level, pos, Items.BEEF);
            }
            case "brewing_stand" -> put(level, pos,
                    Blocks.BREWING_STAND.defaultBlockState());
            case "respawn_anchor" -> put(level, pos,
                    Blocks.RESPAWN_ANCHOR.defaultBlockState()
                            .setValue(RespawnAnchorBlock.CHARGE, 2));
            case "bookshelves" -> buildBookshelves(level, face, x, z, radial);
            case "portal_ignition" -> buildPortalFrame(level, face, x, z);
            case "flint_steel" -> {
                put(level, pos, Blocks.OBSIDIAN.defaultBlockState());
                // Adjacent placement lane stays empty for real useOn.
            }
            default -> throw new IllegalStateException(
                    "Unexpected test station: " + station.name()
            );
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
                "CYAN = preset reference; LIME = natural placement slot. "
        );
        for (int row = 0; row < SOUTH_GRID.length; row++) {
            if (row != 0) {
                result.append(" | ");
            }
            result.append("row ").append(row + 1).append(": ");
            for (int col = 0; col < EAST_GRID.length; col++) {
                if (col > 0) {
                    result.append(", ");
                }
                int desiredCol = col;
                int desiredRow = row;
                Station s = STATIONS.stream()
                        .filter(station -> station.column() == desiredCol
                                && station.row() == desiredRow)
                        .findFirst().orElseThrow();
                result.append(s.name().replace('_', ' '));
            }
        }
        return result.toString();
    }

    public static String faceName(PlanetFace face) {
        return face.name().toLowerCase(Locale.ROOT);
    }
}
