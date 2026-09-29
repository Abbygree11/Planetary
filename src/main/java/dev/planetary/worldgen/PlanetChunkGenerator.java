package dev.planetary.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class PlanetChunkGenerator extends ChunkGenerator {
    public static final MapCodec<PlanetChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            BiomeSource.CODEC
                                    .fieldOf("biome_source")
                                    .forGetter(PlanetChunkGenerator::getBiomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("noise_settings")
                                    .forGetter(PlanetChunkGenerator::noiseSettings)
                    ).apply(
                            instance,
                            instance.stable(PlanetChunkGenerator::new)
                    )
            );

    private final Holder<NoiseGeneratorSettings> noiseSettings;

    public PlanetChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> noiseSettings
    ) {
        super(biomeSource);
        this.noiseSettings = noiseSettings;
    }

    public Holder<NoiseGeneratorSettings> noiseSettings() {
        return noiseSettings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
        int minWorldX = chunk.getPos().getMinBlockX();
        int minWorldZ = chunk.getPos().getMinBlockZ();
        int minY = Math.max(
                chunk.getMinBuildHeight(),
                PlanetWorldSettings.CORE_Y - PlanetWorldSettings.RADIUS
        );
        int maxY = Math.min(
                chunk.getMaxBuildHeight() - 1,
                PlanetWorldSettings.CORE_Y + PlanetWorldSettings.RADIUS
        );

        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(
                Heightmap.Types.OCEAN_FLOOR_WG
        );
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(
                Heightmap.Types.WORLD_SURFACE_WG
        );

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = minWorldX + localX;
            int dx = Math.abs(worldX - PlanetWorldSettings.CORE_X);
            if (dx > PlanetWorldSettings.RADIUS) {
                continue;
            }

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = minWorldZ + localZ;
                int dz = Math.abs(worldZ - PlanetWorldSettings.CORE_Z);
                if (Math.max(dx, dz) > PlanetWorldSettings.RADIUS) {
                    continue;
                }

                for (int y = minY; y <= maxY; y++) {
                    BlockState state = PlanetInitialTerrain.blockStateAt(
                            worldX, y, worldZ
                    );
                    if (state.isAir()) {
                        continue;
                    }

                    cursor.set(localX, y, localZ);
                    chunk.setBlockState(cursor, state, false);
                    oceanFloor.update(localX, y, localZ, state);
                    worldSurface.update(localX, y, localZ, state);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState random,
            ChunkAccess chunk
    ) {
    }

    @Override
    public void applyBiomeDecoration(
            WorldGenLevel level,
            ChunkAccess chunk,
            StructureManager structureManager
    ) {
        // Intentionally disabled until features are routed through
        // PlanetGenerationSpace.
    }

    @Override
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step
    ) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return PlanetWorldSettings.CORE_Y
                + PlanetWorldSettings.RADIUS
                + 1;
    }

    @Override
    public int getGenDepth() {
        return PlanetWorldSettings.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return PlanetWorldSettings.CORE_Y;
    }

    @Override
    public int getMinY() {
        return PlanetWorldSettings.MIN_Y;
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types type,
            LevelHeightAccessor level,
            RandomState random
    ) {
        int dx = Math.abs(x - PlanetWorldSettings.CORE_X);
        int dz = Math.abs(z - PlanetWorldSettings.CORE_Z);

        if (Math.max(dx, dz) > PlanetWorldSettings.RADIUS) {
            return level.getMinBuildHeight();
        }

        return PlanetWorldSettings.CORE_Y
                + PlanetWorldSettings.RADIUS
                + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(
            int x,
            int z,
            LevelHeightAccessor height,
            RandomState random
    ) {
        int minY = height.getMinBuildHeight();
        BlockState[] states = new BlockState[height.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());

        int dx = Math.abs(x - PlanetWorldSettings.CORE_X);
        int dz = Math.abs(z - PlanetWorldSettings.CORE_Z);

        if (Math.max(dx, dz) <= PlanetWorldSettings.RADIUS) {
            int fromY = Math.max(
                    minY,
                    PlanetWorldSettings.CORE_Y - PlanetWorldSettings.RADIUS
            );
            int toY = Math.min(
                    height.getMaxBuildHeight() - 1,
                    PlanetWorldSettings.CORE_Y + PlanetWorldSettings.RADIUS
            );

            for (int y = fromY; y <= toY; y++) {
                states[y - minY] =
                        PlanetInitialTerrain.blockStateAt(x, y, z);
            }
        }

        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(
            List<String> info,
            RandomState random,
            BlockPos pos
    ) {
        PlanetGenerationSpace space =
                new PlanetGenerationSpace(PlanetWorldSettings.core());

        info.add(
                "Planet shell: "
                        + String.format(
                                "%.2f",
                                space.shellRadius(
                                        pos.getX() + 0.5D,
                                        pos.getY() + 0.5D,
                                        pos.getZ() + 0.5D
                                )
                        )
        );
    }
}
