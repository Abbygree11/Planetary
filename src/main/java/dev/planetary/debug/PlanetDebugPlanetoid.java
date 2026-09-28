package dev.planetary.debug;

import dev.planetary.topology.PlanetFace;
import dev.planetary.world.PlanetBlockPos;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small deterministic planet used while the real renderer/physics stack is
 * being brought online.
 *
 * <p>It deliberately uses the exact same PlanetWorldAccess and six-face
 * topology as the eventual generated world. Only the terrain recipe is
 * temporary.</p>
 */
public final class PlanetDebugPlanetoid {
    public static final int FACE_SIZE = 12;
    public static final int SURFACE_Y = 0;
    public static final int MIN_LOCAL_Y = -4;
    public static final int MAX_LOCAL_Y = 4;

    private PlanetDebugPlanetoid() {
    }

    public static PlanetWorldAccess create() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                MIN_LOCAL_Y,
                MAX_LOCAL_Y
        );
        populate(world);
        return world;
    }

    public static void populate(PlanetWorldAccess world) {
        if (world.faceSizeBlocks() != FACE_SIZE) {
            throw new IllegalArgumentException(
                    "Debug planetoid requires face size " + FACE_SIZE
            );
        }

        for (PlanetFace face : PlanetFace.values()) {
            for (int x = 0; x < FACE_SIZE; x++) {
                for (int z = 0; z < FACE_SIZE; z++) {
                    world.setBlockState(
                            new PlanetBlockPos(
                                    face,
                                    x,
                                    SURFACE_Y,
                                    z
                            ),
                            surfaceState(face, x, z)
                    );
                }
            }
        }
    }

    public static BlockState surfaceState(
            PlanetFace face,
            int x,
            int z
    ) {
        boolean xEdge = x == 0 || x == FACE_SIZE - 1;
        boolean zEdge = z == 0 || z == FACE_SIZE - 1;

        if (xEdge && zEdge) {
            return Blocks.GOLD_BLOCK.defaultBlockState();
        }
        if (xEdge || zEdge) {
            return Blocks.IRON_BLOCK.defaultBlockState();
        }

        int center = FACE_SIZE / 2;
        if (x == center && z == center) {
            return Blocks.SEA_LANTERN.defaultBlockState();
        }

        return switch (face) {
            case POS_X -> Blocks.RED_CONCRETE.defaultBlockState();
            case NEG_X -> Blocks.BLUE_CONCRETE.defaultBlockState();
            case POS_Y -> Blocks.GRASS_BLOCK.defaultBlockState();
            case NEG_Y -> Blocks.DEEPSLATE.defaultBlockState();
            case POS_Z -> Blocks.LIME_CONCRETE.defaultBlockState();
            case NEG_Z -> Blocks.YELLOW_CONCRETE.defaultBlockState();
        };
    }
}
