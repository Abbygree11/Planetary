package dev.planetary.debug;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Temporary real-block planet used to validate gravity and collision before
 * the dedicated Planet world type and worldgen are implemented.
 *
 * <p>Unlike the old visual prototype, every block here lives in the ordinary
 * Minecraft XYZ grid. Edge and corner blocks therefore exist exactly once.</p>
 */
public final class PlanetDebugWorld {
    public static final int RADIUS = 6;
    public static final int DIAMETER = RADIUS * 2 + 1;
    public static final int CLEARANCE = 3;
    public static final int GRAVITY_ACTIVE_RADIUS =
            RADIUS + 12;

    public static final BlockPos CORE_POS =
            new BlockPos(24, 280, 24);

    private static final PlanetCore CORE =
            new PlanetCore(
                    CORE_POS.getX(),
                    CORE_POS.getY(),
                    CORE_POS.getZ(),
                    RADIUS
            );

    private static final PlanetGravityField GRAVITY_FIELD =
            new PlanetGravityField(CORE);

    private static final Set<ServerLevel> PREPARED_LEVELS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private PlanetDebugWorld() {
    }

    public static PlanetGravityField gravityField() {
        return GRAVITY_FIELD;
    }

    public static PlanetCore core() {
        return CORE;
    }

    public static Vec3 playerSpawn() {
        return new Vec3(
                CORE_POS.getX() + 0.5D,
                CORE_POS.getY() + RADIUS + 1.0D,
                CORE_POS.getZ() + 0.5D
        );
    }

    public static void attach(ServerLevel level) {
        prepare(level);
        PlanetGravityRuntime.bind(
                level,
                GRAVITY_FIELD,
                PlanetDebugWorld::isInGravityTestArea
        );
    }

    public static boolean isInGravityTestArea(
            double x,
            double y,
            double z
    ) {
        double dx = Math.abs(
                x - (CORE_POS.getX() + 0.5D)
        );
        double dy = Math.abs(
                y - (CORE_POS.getY() + 0.5D)
        );
        double dz = Math.abs(
                z - (CORE_POS.getZ() + 0.5D)
        );

        return Math.max(dx, Math.max(dy, dz))
                <= GRAVITY_ACTIVE_RADIUS;
    }

    public static void detach(ServerLevel level) {
        PREPARED_LEVELS.remove(level);
        PlanetGravityRuntime.unbind(level, GRAVITY_FIELD);
    }

    public static void prepare(ServerLevel level) {
        if (!PREPARED_LEVELS.add(level)) {
            return;
        }

        int outerRadius = RADIUS + CLEARANCE;
        BlockPos.MutableBlockPos cursor =
                new BlockPos.MutableBlockPos();

        int updateFlags =
                Block.UPDATE_CLIENTS
                        | Block.UPDATE_KNOWN_SHAPE;

        for (int dx = -outerRadius; dx <= outerRadius; dx++) {
            for (int dy = -outerRadius; dy <= outerRadius; dy++) {
                for (int dz = -outerRadius; dz <= outerRadius; dz++) {
                    int shell = Math.max(
                            Math.abs(dx),
                            Math.max(
                                    Math.abs(dy),
                                    Math.abs(dz)
                            )
                    );

                    cursor.set(
                            CORE_POS.getX() + dx,
                            CORE_POS.getY() + dy,
                            CORE_POS.getZ() + dz
                    );

                    BlockState target =
                            shell > RADIUS
                                    ? Blocks.AIR.defaultBlockState()
                                    : blockStateAtOffset(
                                            dx,
                                            dy,
                                            dz
                                    );

                    if (!level.getBlockState(cursor)
                            .equals(target)) {
                        level.setBlock(
                                cursor,
                                target,
                                updateFlags
                        );
                    }
                }
            }
        }
    }

    public static BlockState blockStateAtOffset(
            int dx,
            int dy,
            int dz
    ) {
        if (dx == 0 && dy == 0 && dz == 0) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        int shell = Math.max(
                Math.abs(dx),
                Math.max(
                        Math.abs(dy),
                        Math.abs(dz)
                )
        );

        if (shell > RADIUS) {
            return Blocks.AIR.defaultBlockState();
        }
        if (shell < RADIUS) {
            return Blocks.STONE.defaultBlockState();
        }

        boolean xSurface = Math.abs(dx) == RADIUS;
        boolean ySurface = Math.abs(dy) == RADIUS;
        boolean zSurface = Math.abs(dz) == RADIUS;

        int surfaceCount =
                (xSurface ? 1 : 0)
                        + (ySurface ? 1 : 0)
                        + (zSurface ? 1 : 0);

        if (surfaceCount == 3) {
            return Blocks.GOLD_BLOCK.defaultBlockState();
        }
        if (surfaceCount == 2) {
            return Blocks.IRON_BLOCK.defaultBlockState();
        }

        boolean faceCenter =
                (xSurface && dy == 0 && dz == 0)
                        || (ySurface && dx == 0 && dz == 0)
                        || (zSurface && dx == 0 && dy == 0);

        if (faceCenter) {
            return Blocks.SEA_LANTERN.defaultBlockState();
        }

        if (xSurface) {
            return dx > 0
                    ? Blocks.RED_CONCRETE.defaultBlockState()
                    : Blocks.BLUE_CONCRETE.defaultBlockState();
        }
        if (ySurface) {
            return dy > 0
                    ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : Blocks.DEEPSLATE.defaultBlockState();
        }

        return dz > 0
                ? Blocks.LIME_CONCRETE.defaultBlockState()
                : Blocks.YELLOW_CONCRETE.defaultBlockState();
    }
}
