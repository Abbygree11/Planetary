package dev.planetary.worldgen;

import dev.planetary.PlanetaryMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public final class PlanetWorldKeys {
    public static final ResourceLocation PLANET_ID =
            ResourceLocation.fromNamespaceAndPath(
                    PlanetaryMod.MOD_ID,
                    "planet"
            );

    public static final ResourceKey<DimensionType>
            PLANET_DIMENSION_TYPE =
            ResourceKey.create(
                    Registries.DIMENSION_TYPE,
                    PLANET_ID
            );

    public static final ResourceKey<WorldPreset>
            PLANET_WORLD_PRESET =
            ResourceKey.create(
                    Registries.WORLD_PRESET,
                    PLANET_ID
            );

    private PlanetWorldKeys() {
    }
}
