package dev.planetary.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PlanetInitialTerrain {
    private PlanetInitialTerrain() {
    }

    public static BlockState blockStateAt(
            int worldX,
            int worldY,
            int worldZ
    ) {
        int dx = worldX - PlanetWorldSettings.CORE_X;
        int dy = worldY - PlanetWorldSettings.CORE_Y;
        int dz = worldZ - PlanetWorldSettings.CORE_Z;

        int shell = Math.max(
                Math.abs(dx),
                Math.max(Math.abs(dy), Math.abs(dz))
        );

        if (shell > PlanetWorldSettings.RADIUS) {
            return Blocks.AIR.defaultBlockState();
        }
        if (shell == 0) {
            return Blocks.BEDROCK.defaultBlockState();
        }
        if (shell == PlanetWorldSettings.RADIUS) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }
}
